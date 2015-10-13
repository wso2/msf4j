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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.wso2.carbon.mss.internal.router.HandlerHook;
import org.wso2.carbon.mss.internal.router.HttpResourceHandler;
import org.wso2.carbon.mss.internal.router.RequestRouter;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Netty Transport ServerInitializer for the Microservices Server
 */
public class MSSNettyServerInitializer implements CarbonNettyServerInitializer {

    private DefaultEventExecutorGroup eventExecutorGroup;
    private List<HandlerHook> hooks = new ArrayList<>();

    @Override
    public void setup(Map<String, String> map) {
        eventExecutorGroup = new DefaultEventExecutorGroup(200);
    }

    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        //        pipeline.addLast("tracker", connectionTracker);
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));  //TODO: fix
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("compressor", new HttpContentCompressor());

        HttpResourceHandler resourceHandler =
                new HttpResourceHandler(MicroservicesRegistry.getInstance().getHttpServices(),
                       hooks, null, null);
        pipeline.addLast(eventExecutorGroup, "router", new RequestRouter(resourceHandler, 0)); //TODO: remove limit

        //TODO: see what can be done
            /*if (pipelineModifier != null) {
                pipelineModifier.apply(pipeline);
            }*/
    }

    public void addHandlerHook(HandlerHook hook) {
        hooks.add(hook);
    }
}
