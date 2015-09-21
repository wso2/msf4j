/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.mss.internal.router;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.OpenSslServerContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Webservice implemented using the netty framework. Implements Guava's Service interface to manage the states
 * of the webservice.
 */
public final class NettyHttpService extends AbstractIdleService {

    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpService.class);

    private static final int CLOSE_CHANNEL_TIMEOUT = 5;
    private final int bossThreadPoolSize;
    private final int workerThreadPoolSize;
    private final int execThreadPoolSize;
    private final Map<ChannelOption, Object> channelConfigs;
    private final RejectedExecutionHandler rejectedExecutionHandler;
    private final HandlerContext handlerContext;
    private final ChannelGroup channelGroup;
    private final HttpResourceHandler resourceHandler;
    private final Function<ChannelPipeline, ChannelPipeline> pipelineModifier;
    private final int httpChunkLimit;
    private final SSLHandlerFactory sslHandlerFactory;

    private ServerBootstrap bootstrap;
    private InetSocketAddress bindAddress;

    private DefaultEventExecutorGroup eventExecutorGroup;

    /**
     * Initialize NettyHttpService.
     *
     * @param bindAddress              Address for the service to bind to.
     * @param bossThreadPoolSize       Size of the boss thread pool.
     * @param workerThreadPoolSize     Size of the worker thread pool.
     * @param execThreadPoolSize       Size of the thread pool for the executor.
     * @param channelConfigs           Configurations for the server socket channel.
     * @param rejectedExecutionHandler rejection policy for executor.
     * @param urlRewriter              URLRewriter to rewrite incoming URLs.
     * @param httpHandlers             HttpHandlers to handle the calls.
     * @param handlerHooks             Hooks to be called before/after request processing by httpHandlers.
     * @deprecated Use {@link NettyHttpService.Builder} instead.
     */
    @Deprecated
    public NettyHttpService(InetSocketAddress bindAddress, int bossThreadPoolSize, int workerThreadPoolSize,
                            int execThreadPoolSize,
                            Map<ChannelOption, Object> channelConfigs,
                            RejectedExecutionHandler rejectedExecutionHandler, URLRewriter urlRewriter,
                            Iterable<? extends HttpHandler> httpHandlers,
                            Iterable<? extends HandlerHook> handlerHooks, int httpChunkLimit) {
        this(bindAddress, bossThreadPoolSize, workerThreadPoolSize, execThreadPoolSize,
                channelConfigs, rejectedExecutionHandler, urlRewriter, httpHandlers, handlerHooks, httpChunkLimit,
                null, null, new ExceptionHandler());
    }

    /**
     * Initialize NettyHttpService. Also includes SSL implementation.
     *
     * @param bindAddress              Address for the service to bind to.
     * @param bossThreadPoolSize       Size of the boss thread pool.
     * @param workerThreadPoolSize     Size of the worker thread pool.
     * @param execThreadPoolSize       Size of the thread pool for the executor.
     * @param channelConfigs           Configurations for the server socket channel.
     * @param rejectedExecutionHandler rejection policy for executor.
     * @param urlRewriter              URLRewriter to rewrite incoming URLs.
     * @param httpHandlers             HttpHandlers to handle the calls.
     * @param handlerHooks             Hooks to be called before/after request processing by httpHandlers.
     * @param pipelineModifier         Function used to modify the pipeline.
     * @param sslHandlerFactory        Object used to share SSL certificate details
     * @param exceptionHandler         Handles exceptions from calling handler methods
     */
    private NettyHttpService(InetSocketAddress bindAddress, int bossThreadPoolSize, int workerThreadPoolSize,
                             int execThreadPoolSize,
                             Map<ChannelOption, Object> channelConfigs,
                             RejectedExecutionHandler rejectedExecutionHandler, URLRewriter urlRewriter,
                             Iterable<? extends HttpHandler> httpHandlers,
                             Iterable<? extends HandlerHook> handlerHooks, int httpChunkLimit,
                             Function<ChannelPipeline, ChannelPipeline> pipelineModifier,
                             SSLHandlerFactory sslHandlerFactory, ExceptionHandler exceptionHandler) {
        this.bindAddress = bindAddress;
        this.bossThreadPoolSize = bossThreadPoolSize;
        this.workerThreadPoolSize = workerThreadPoolSize;
        this.execThreadPoolSize = execThreadPoolSize;
        this.channelConfigs = ImmutableMap.copyOf(channelConfigs);
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        this.resourceHandler = new HttpResourceHandler(httpHandlers, handlerHooks, urlRewriter, exceptionHandler);
        this.handlerContext = new BasicHandlerContext(this.resourceHandler);
        this.httpChunkLimit = httpChunkLimit;
        this.pipelineModifier = pipelineModifier;
        this.sslHandlerFactory = sslHandlerFactory;
    }

    /**
     * Bootstrap the pipeline.
     * <ul>
     * <li>Create Execution handler</li>
     * <li>Setup Http resource handler</li>
     * <li>Setup the netty pipeline</li>
     * </ul>
     *
     * @param eventExecutor Executor group
     */
    private void bootStrap(final DefaultEventExecutorGroup eventExecutor) throws Exception {

        NioEventLoopGroup bossGroup =
                new NioEventLoopGroup(bossThreadPoolSize,
                        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("netty-boss-thread").build());

        NioEventLoopGroup workerGroup =
                new NioEventLoopGroup(workerThreadPoolSize,
                        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("netty-worker-thread").build());

        //Server bootstrap with default worker threads (2 * number of cores)
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.handler(new LoggingHandler(LogLevel.INFO));

        for (Map.Entry<ChannelOption, Object> entry : channelConfigs.entrySet()) {
            bootstrap.option(entry.getKey(), entry.getValue());
        }

        resourceHandler.init(handlerContext);

        //TODO: Check how to handle this
        final ChannelInboundHandlerAdapter connectionTracker = new ChannelInboundHandlerAdapter() {

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                channelGroup.add(ctx.channel());
                super.channelActive(ctx);
            }
        };
        bootstrap.childHandler(new ServerInitializer(eventExecutor, httpChunkLimit, sslHandlerFactory,
                resourceHandler, pipelineModifier));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void startUp() throws Exception {
        LOG.info("Starting service on address {}...", bindAddress);
        eventExecutorGroup = new DefaultEventExecutorGroup(execThreadPoolSize);
        bootStrap(eventExecutorGroup);
        ChannelFuture channelFuture = bootstrap.bind(bindAddress).sync();
        channelGroup.add(channelFuture.channel());
        bindAddress = ((InetSocketAddress) channelFuture.channel().localAddress());
        LOG.info("Started service on address {}", bindAddress);
    }

    /**
     * @return port where the service is running.
     */
    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    @Override
    protected void shutDown() throws Exception {
        LOG.info("Stopping service on address {}...", bindAddress);
        eventExecutorGroup.shutdownGracefully();
        try {
            if (!channelGroup.close().await(CLOSE_CHANNEL_TIMEOUT, TimeUnit.SECONDS)) {
                LOG.warn("Timeout when closing all channels.");
            }
        } finally {
            resourceHandler.destroy(handlerContext);
        }
        LOG.info("Done stopping service on address {}", bindAddress);
    }

    /**
     * Builder to help create the NettyHttpService.
     */
    public static class Builder {

        private static final int DEFAULT_BOSS_THREAD_POOL_SIZE = 1;
        private static final int DEFAULT_WORKER_THREAD_POOL_SIZE = 10;
        private static final int DEFAULT_CONNECTION_BACKLOG = 1000;
        private static final int DEFAULT_EXEC_HANDLER_THREAD_POOL_SIZE = 60;
        private static final RejectedExecutionHandler DEFAULT_REJECTED_EXECUTION_HANDLER =
                new ThreadPoolExecutor.CallerRunsPolicy();
        private static final int DEFAULT_HTTP_CHUNK_LIMIT = 150 * 1024 * 1024;

        private final Map<ChannelOption, Object> channelConfigs;

        private Iterable<? extends HttpHandler> handlers;
        private Iterable<? extends HandlerHook> handlerHooks = ImmutableList.of();
        private URLRewriter urlRewriter = null;
        private int bossThreadPoolSize;
        private int workerThreadPoolSize;
        private int execThreadPoolSize;
        private String host;
        private int port;
        private RejectedExecutionHandler rejectedExecutionHandler;
        private int httpChunkLimit;
        private SSLHandlerFactory sslHandlerFactory;
        private Function<ChannelPipeline, ChannelPipeline> pipelineModifier;
        private ExceptionHandler exceptionHandler;

        // Protected constructor to prevent instantiating Builder instance directly.
        protected Builder() {
            bossThreadPoolSize = DEFAULT_BOSS_THREAD_POOL_SIZE;
            workerThreadPoolSize = DEFAULT_WORKER_THREAD_POOL_SIZE;
            execThreadPoolSize = DEFAULT_EXEC_HANDLER_THREAD_POOL_SIZE;
            rejectedExecutionHandler = DEFAULT_REJECTED_EXECUTION_HANDLER;
            httpChunkLimit = DEFAULT_HTTP_CHUNK_LIMIT;
            port = 0;
            channelConfigs = Maps.newHashMap();
            channelConfigs.put(ChannelOption.SO_BACKLOG, DEFAULT_CONNECTION_BACKLOG);
            sslHandlerFactory = null;
            exceptionHandler = new ExceptionHandler();
        }

        /**
         * Modify the pipeline upon build by applying the function.
         *
         * @param function Function that modifies and returns a pipeline.
         * @return builder
         */
        public Builder modifyChannelPipeline(Function<ChannelPipeline, ChannelPipeline> function) {
            this.pipelineModifier = function;
            return this;
        }

        /**
         * Add HttpHandlers that service the request.
         *
         * @param handlers Iterable of HttpHandlers.
         * @return instance of {@code Builder}.
         */
        public Builder addHttpHandlers(Iterable<? extends HttpHandler> handlers) {
            this.handlers = handlers;
            return this;
        }

        /**
         * Set HandlerHooks to be executed pre and post handler calls. They are executed in the same order as specified
         * by the iterable.
         *
         * @param handlerHooks Iterable of HandlerHooks.
         * @return an instance of {@code Builder}.
         */
        public Builder setHandlerHooks(Iterable<? extends HandlerHook> handlerHooks) {
            this.handlerHooks = handlerHooks;
            return this;
        }

        /**
         * Set URLRewriter to re-write URL of an incoming request before any handlers or their hooks are called.
         *
         * @param urlRewriter instance of URLRewriter.
         * @return an instance of {@code Builder}.
         */
        public Builder setUrlRewriter(URLRewriter urlRewriter) {
            this.urlRewriter = urlRewriter;
            return this;
        }

        /**
         * Set size of bossThreadPool in netty default value is 1 if it is not set.
         *
         * @param bossThreadPoolSize size of bossThreadPool.
         * @return an instance of {@code Builder}.
         */
        public Builder setBossThreadPoolSize(int bossThreadPoolSize) {
            this.bossThreadPoolSize = bossThreadPoolSize;
            return this;
        }


        /**
         * Set size of workerThreadPool in netty default value is 10 if it is not set.
         *
         * @param workerThreadPoolSize size of workerThreadPool.
         * @return an instance of {@code Builder}.
         */
        public Builder setWorkerThreadPoolSize(int workerThreadPoolSize) {
            this.workerThreadPoolSize = workerThreadPoolSize;
            return this;
        }

        /**
         * Set size of backlog in netty service - size of accept queue of the TCP stack.
         *
         * @param connectionBacklog backlog in netty server. Default value is 1000.
         * @return an instance of {@code Builder}.
         */
        public Builder setConnectionBacklog(int connectionBacklog) {
            channelConfigs.put(ChannelOption.SO_BACKLOG, connectionBacklog);
            return this;
        }

        /**
         * Sets channel configuration for the the netty service.
         *
         * @param key   Name of the configuration.
         * @param value Value of the configuration.
         * @return an instance of {@code Builder}.
         * @see ChannelConfig
         */
        public Builder setChannelConfig(ChannelOption key, Object value) {
            channelConfigs.put(key, value);
            return this;
        }

        /**
         * Set size of executorThreadPool in netty default value is 60 if it is not set.
         * If the size is {@code 0}, then no executor will be used, hence calls to {@link HttpHandler} would be made from
         * worker threads directly.
         *
         * @param execThreadPoolSize size of workerThreadPool.
         * @return an instance of {@code Builder}.
         */
        public Builder setExecThreadPoolSize(int execThreadPoolSize) {
            this.execThreadPoolSize = execThreadPoolSize;
            return this;
        }

        /**
         * Set RejectedExecutionHandler - rejection policy for executor.
         *
         * @param rejectedExecutionHandler rejectionExecutionHandler.
         * @return an instance of {@code Builder}.
         */
        public Builder setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
            return this;
        }

        /**
         * Set the port on which the service should listen to.
         * By default the service will run on a random port.
         *
         * @param port port on which the service should listen to.
         * @return instance of {@code Builder}.
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set the bindAddress for the service. Default value is localhost.
         *
         * @param host bindAddress for the service.
         * @return instance of {@code Builder}.
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setHttpChunkLimit(int value) {
            this.httpChunkLimit = value;
            return this;
        }

        /**
         * Enable SSL by using the provided SSL information.
         */
        public Builder enableSSL(SSLConfig sslConfig) {
            this.sslHandlerFactory = new SSLHandlerFactory(sslConfig);
            return this;
        }

        public Builder setExceptionHandler(ExceptionHandler exceptionHandler) {
            Preconditions.checkNotNull(exceptionHandler, "exceptionHandler cannot be null");
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        /**
         * @return instance of {@code NettyHttpService}
         */
        public NettyHttpService build() {
            InetSocketAddress bindAddress;
            if (host == null) {
                bindAddress = new InetSocketAddress("localhost", port);
            } else {
                bindAddress = new InetSocketAddress(host, port);
            }

            return new NettyHttpService(bindAddress, bossThreadPoolSize, workerThreadPoolSize,
                    execThreadPoolSize, channelConfigs, rejectedExecutionHandler,
                    urlRewriter, handlers, handlerHooks, httpChunkLimit, pipelineModifier,
                    sslHandlerFactory, exceptionHandler);
        }
    }
}
