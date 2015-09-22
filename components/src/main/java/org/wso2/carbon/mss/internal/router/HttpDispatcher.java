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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpDispatcher that invokes the appropriate http-handler method. The handler and the arguments are read
 * from the {@code RequestRouter} context.
 */

public class HttpDispatcher extends SimpleChannelInboundHandler<HttpRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpDispatcher.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        /*HttpMethodInfo methodInfo = ctx.channel().attr(RequestRouter.METHOD_INFO).get();
        if (msg instanceof HttpContent) {
            methodInfo.chunk((HttpContent) msg);
        } else {
            methodInfo.invoke();
        }*/
    }
}
