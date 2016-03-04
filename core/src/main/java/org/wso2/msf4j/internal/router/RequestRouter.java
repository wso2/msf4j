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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMultimap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This Netty handler handles routing of requests to the relevant microservice.
 */
public class RequestRouter extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger log = LoggerFactory.getLogger(RequestRouter.class);

    private final MicroserviceMetadata httpMethodHandler;
    private final AtomicBoolean exceptionRaised;

    public static final String METHOD_INFO_BUILDER = "METHOD_INFO_BUILDER";

    private HttpMethodInfoBuilder httpMethodInfoBuilder;

    public RequestRouter(MicroserviceMetadata methodHandler, int chunkMemoryLimit) {
        this.httpMethodHandler = methodHandler;
        this.exceptionRaised = new AtomicBoolean(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        Channel channel = ctx.channel();
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            if (handleRequest(request, channel, ctx)) {
                if (httpMethodInfoBuilder.getHttpResourceModel()
                        .isStreamingReqSupported() &&
                        channel.pipeline().get("aggregator") != null) {
                    channel.pipeline().remove("aggregator");
                } else if (!httpMethodInfoBuilder.getHttpResourceModel()
                        .isStreamingReqSupported() &&
                        channel.pipeline().get("aggregator") == null) {
                    channel.pipeline().addAfter("router", "aggregator",
                            new HttpObjectAggregator(Integer.MAX_VALUE));
                }
            }
            ReferenceCountUtil.retain(msg);
            ctx.fireChannelRead(msg);
        } else if (msg instanceof HttpContent) {
            ReferenceCountUtil.retain(msg);
            ctx.fireChannelRead(msg);
        }
    }

    private boolean handleRequest(HttpRequest httpRequest, Channel channel,
                                  ChannelHandlerContext ctx) throws HandlerException {
        httpMethodInfoBuilder = httpMethodHandler.getDestinationMethod(
                httpRequest, new BasicHttpResponder(channel, HttpHeaders.isKeepAlive(httpRequest)));
        ctx.attr(AttributeKey.valueOf(METHOD_INFO_BUILDER)).set(httpMethodInfoBuilder);
        return httpMethodInfoBuilder != null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String exceptionMessage = "Exception caught in channel processing.";
        if (!exceptionRaised.get()) {
            exceptionRaised.set(true);

            if (httpMethodInfoBuilder != null) {
                log.error(exceptionMessage, cause);
                sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, cause);
                httpMethodInfoBuilder = null;
            } else {
                HttpResponse response;
                if (cause instanceof HandlerException) {
                    response = ((HandlerException) cause).createFailureResponse();
                    // trace logs for user errors, error logs for internal server errors
                    if (isUserError(response)) {
                        log.trace(exceptionMessage, cause);
                    } else {
                        log.error(exceptionMessage, cause);
                    }
                } else {
                    log.error(exceptionMessage, cause);
                    response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }
                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            log.trace(exceptionMessage, cause);
        }
    }

    private boolean isUserError(HttpResponse response) {
        int code = response.getStatus().code();
        return code == HttpResponseStatus.BAD_REQUEST.code() || code == HttpResponseStatus.NOT_FOUND.code() ||
                code == HttpResponseStatus.METHOD_NOT_ALLOWED.code();
    }


    /**
     * Sends the error to responder.
     */
    private void sendError(HttpResponseStatus status, Throwable ex) {
        String msg;

        if (ex instanceof InvocationTargetException) {
            msg = String.format("Exception Encountered while processing request : %s",
                    Objects.firstNonNull(ex.getCause(), ex).getMessage());
        } else {
            msg = String.format("Exception Encountered while processing request: %s", ex.getMessage());
        }

        // Send the status and message, followed by closing of the connection.
        httpMethodInfoBuilder.getResponder()
                .sendString(status, msg,
                        ImmutableMultimap.of(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE));
    }
}
