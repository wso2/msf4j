/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mss.internal;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.mss.internal.router.HttpDispatcher;
import org.wso2.carbon.mss.internal.router.RequestRouter;

import java.util.Map;

/**
 * Netty Transport ServerInitializer for the Microservices Server.
 */
public class MSSNettyServerInitializer implements CarbonTransportInitializer {

    private DefaultEventExecutorGroup eventExecutorGroup;

    private MicroservicesRegistry microservicesRegistry;

    public MSSNettyServerInitializer(MicroservicesRegistry microservicesRegistry) {
        this.microservicesRegistry = microservicesRegistry;
    }

    @Override
    public void setup(Map<String, String> map) {
        eventExecutorGroup =
                new DefaultEventExecutorGroup(Integer.parseInt(map.get(MSSConstants.EXECUTOR_THREAD_POOL_SIZE_KEY)));
    }

    @Override
    public void initChannel(Object objectChannel) {
        SocketChannel channel = (SocketChannel) objectChannel;
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("compressor", new HttpContentCompressor());
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("streamer", new ChunkedWriteHandler());
        pipeline.addLast("router",
                new RequestRouter(microservicesRegistry.getHttpResourceHandler(), 0));
        pipeline.addLast(eventExecutorGroup, "dispatcher", new HttpDispatcher());
    }

    @Override
    public boolean isServerInitializer() {
        return true;
    }
}
