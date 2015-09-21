/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mss.internal.router;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.util.ArrayList;

public class HttpSnoopServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final HttpResourceHandler resourceHandler;
    private final HandlerContext handlerContext;

    public HttpSnoopServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
        ArrayList<HttpHandler> handlers = new ArrayList<>();
        handlers.add(new FooTestHandler());
        this.resourceHandler = new HttpResourceHandler(handlers, new ArrayList<HandlerHook>(), null,
                new ExceptionHandler());
        this.handlerContext = new BasicHandlerContext(this.resourceHandler);
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        p.addLast(new HttpObjectAggregator(1048576));
        p.addLast(new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        p.addLast(new HttpContentCompressor());
        p.addLast(new DefaultEventExecutorGroup(200),
                new RequestRouter(resourceHandler, 150 * 1024 * 1024));
    }
}
