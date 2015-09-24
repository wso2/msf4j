/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss.internal.router;

import com.google.common.base.Function;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.EventExecutorGroup;


/**
 * TODO: class level comment
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final EventExecutorGroup eventExecutor;
    private final int httpChunkLimit;
    private final SSLHandlerFactory sslHandlerFactory;
    private final HttpResourceHandler resourceHandler;
    private final Function<ChannelPipeline, ChannelPipeline> pipelineModifier;

    public ServerInitializer(EventExecutorGroup eventExecutor, int httpChunkLimit, SSLHandlerFactory sslHandlerFactory,
                             HttpResourceHandler resourceHandler,
                             Function<ChannelPipeline, ChannelPipeline> pipelineModifier) {
        this.eventExecutor = eventExecutor;
        this.httpChunkLimit = httpChunkLimit;
        this.sslHandlerFactory = sslHandlerFactory;
        this.resourceHandler = resourceHandler;
        this.pipelineModifier = pipelineModifier;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        if (sslHandlerFactory != null) {
            pipeline.addLast("ssl", sslHandlerFactory.create());
        }
//        pipeline.addLast("tracker", connectionTracker);
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("decoder", new HttpRequestDecoder());
//        pipeline.addLast("aggregator", new HttpObjectAggregator(httpChunkLimit));
        pipeline.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE)); //TODO: put a proper value
        pipeline.addLast("chunkWriter", new ChunkedWriteHandler());  //TODO: add this only if chunking is enabled (BodyConsumer implemented?)
        pipeline.addLast("compressor", new HttpContentCompressor());
        pipeline.addLast(eventExecutor, "router", new RequestRouter(resourceHandler, httpChunkLimit));

        if (pipelineModifier != null) {
            pipelineModifier.apply(pipeline);
        }
    }
}
