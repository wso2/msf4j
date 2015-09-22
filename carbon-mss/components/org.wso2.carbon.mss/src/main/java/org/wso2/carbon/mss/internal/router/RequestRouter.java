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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class RequestRouter extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger LOG = LoggerFactory.getLogger(RequestRouter.class);

    private final int chunkMemoryLimit;
    private final HttpResourceHandler httpMethodHandler;
    private final AtomicBoolean exceptionRaised;

    private HttpMethodInfo methodInfo;

    public RequestRouter(HttpResourceHandler methodHandler, int chunkMemoryLimit) {
        this.httpMethodHandler = methodHandler;
        this.chunkMemoryLimit = chunkMemoryLimit;
        this.exceptionRaised = new AtomicBoolean(false);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            if (handleRequest(request, ctx.channel(), ctx)) {
//                if (msg instanceof HttpContent) {
//                    methodInfo.chunk((HttpContent) msg);
//                } else {
                methodInfo.invoke();
//                }
                ctx.fireChannelReadComplete();
            }
        }
    }

    private boolean handleRequest(HttpRequest httpRequest, Channel channel, ChannelHandlerContext ctx) throws Exception {
        methodInfo = httpMethodHandler.getDestinationMethod(
                httpRequest, new BasicHttpResponder(channel, HttpHeaders.isKeepAlive(httpRequest)));
        return methodInfo != null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String exceptionMessage = "Exception caught in channel processing.";
        if (!exceptionRaised.get()) {
            exceptionRaised.set(true);

            if (methodInfo != null) {
                LOG.error(exceptionMessage, cause);
                methodInfo.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, cause);
                methodInfo = null;
            } else {
                HttpResponse response;
                if (cause instanceof HandlerException) {
                    response = ((HandlerException) cause).createFailureResponse();
                    // trace logs for user errors, error logs for internal server errors
                    if (isUserError(response)) {
                        LOG.trace(exceptionMessage, cause);
                    } else {
                        LOG.error(exceptionMessage, cause);
                    }
                } else {
                    LOG.error(exceptionMessage, cause);
                    response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }
                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            LOG.trace(exceptionMessage, cause);
        }
    }

    private boolean isUserError(HttpResponse response) {
        int code = response.getStatus().code();
        return code == HttpResponseStatus.BAD_REQUEST.code() || code == HttpResponseStatus.NOT_FOUND.code() ||
                code == HttpResponseStatus.METHOD_NOT_ALLOWED.code();
    }
}
