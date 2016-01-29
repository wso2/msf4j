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

package org.wso2.msf4j.internal.router;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.AttributeKey;


/**
 * HttpDispatcher that invokes the appropriate http-handler method. The handler and the arguments are read
 * from the {@code RequestRouter} context.
 */
public class HttpDispatcher extends SimpleChannelInboundHandler<HttpObject> {

    private HttpMethodInfoBuilder httpMethodInfoBuilder;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws HandlerException {
        Object httpMethodInfoBuilderObj = ctx.pipeline()
                .context(RequestRouter.class)
                .attr(AttributeKey.valueOf(RequestRouter.METHOD_INFO_BUILDER))
                .get();
        if (httpMethodInfoBuilderObj instanceof HttpMethodInfoBuilder) {
            httpMethodInfoBuilder = (HttpMethodInfoBuilder) httpMethodInfoBuilderObj;
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
                httpMethodInfoBuilder
                        .httpRequest(fullHttpRequest)
                        .build()
                        .invoke();
            } else if (msg instanceof HttpContent) {
                httpMethodInfoBuilder
                        .build()
                        .chunk((HttpContent) msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws HandlerException {
        if (httpMethodInfoBuilder != null) {
            httpMethodInfoBuilder.getHttpResourceModel()
                    .getExceptionHandler()
                    .handle(cause,
                            httpMethodInfoBuilder.getRequest(),
                            httpMethodInfoBuilder.getResponder());
        }
    }
}
