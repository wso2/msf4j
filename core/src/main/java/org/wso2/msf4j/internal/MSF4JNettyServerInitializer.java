/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.msf4j.internal;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;
import org.wso2.msf4j.internal.router.HttpDispatcher;
import org.wso2.msf4j.internal.router.RequestRouter;

import java.util.Map;

/**
 * Netty Transport ServerInitializer for the Microservices Server.
 */
public class MSF4JNettyServerInitializer implements CarbonNettyServerInitializer {

    private DefaultEventExecutorGroup eventExecutorGroup;

    private MicroservicesRegistry microservicesRegistry;

    public MSF4JNettyServerInitializer(MicroservicesRegistry microservicesRegistry) {
        this.microservicesRegistry = microservicesRegistry;
    }

    @Override
    public void setup(Map<String, String> map) {
        eventExecutorGroup =
                new DefaultEventExecutorGroup(Integer.parseInt(map.get(MSF4JConstants.EXECUTOR_THREAD_POOL_SIZE_KEY)));
    }

    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("compressor", new HttpContentCompressor());
        pipeline.addLast("streamer", new ChunkedWriteHandler());
        pipeline.addLast("router",
                new RequestRouter(microservicesRegistry.getHttpResourceHandler(), 0));
        pipeline.addLast(eventExecutorGroup, "dispatcher", new HttpDispatcher());
    }
}
