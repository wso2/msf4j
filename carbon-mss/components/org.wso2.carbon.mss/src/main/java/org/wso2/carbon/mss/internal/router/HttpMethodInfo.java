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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMultimap;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.router.beanconversion.BeanConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * HttpMethodInfo is a helper class having state information about the http handler method to be invoked, the handler
 * and arguments required for invocation by the Dispatcher. RequestRouter populates this class and stores in its
 * context as attachment.
 */
class HttpMethodInfo {

    private final Method method;
    private final Object handler;
    private final boolean isChunkedRequest;
    private final ByteBuf requestContent;
    private final HttpRequest request;
    private final HttpResponder responder;
    private final Object[] args;
    private final boolean isStreaming;
    private final ExceptionHandler exceptionHandler;
    private final String mediaType;

    private BodyConsumer bodyConsumer;

    HttpMethodInfo(Method method, Object handler, HttpRequest request, HttpResponder responder, Object[] args,
                   ExceptionHandler exceptionHandler, String mediaType) {
        this.method = method;
        this.handler = handler;
        this.isChunkedRequest = request instanceof HttpContent;
        if (request instanceof HttpContent) {
            requestContent = ((HttpContent) request).content();
        } else {
            this.requestContent = null;
        }
        this.isStreaming = BodyConsumer.class.isAssignableFrom(method.getReturnType());
        this.request = rewriteRequest(request, isStreaming);
        this.responder = responder;
        this.exceptionHandler = exceptionHandler;
        this.mediaType = mediaType;

        // The actual arguments list to invoke handler method
        this.args = args;
    }

    /**
     * Calls the httpHandler method.
     */
    void invoke() throws Exception {
        if (isStreaming) {
            // Casting guarantee to be succeeded.
            bodyConsumer = (BodyConsumer) method.invoke(handler, args);
            if (bodyConsumer != null) {
                if (requestContent.isReadable()) {
                    bodyConsumerChunk(requestContent);
                }
                if (!isChunkedRequest) {
                    bodyConsumerFinish();
                }
            }
        } else {
            // Actually <T> would be void
            bodyConsumer = null;
            try {
                Object returnVal = method.invoke(handler, args);
                Object convertedVal = BeanConverter.instance(mediaType)
                        .toMedia(returnVal);
                //sending return value as output
                new HttpMethodResponseHandler()
                        .setResponder(responder)
                        .setEntity(convertedVal)
                        .setMediaType(mediaType)
                        .send();
            } catch (InvocationTargetException e) {
                exceptionHandler.handle(e.getTargetException(), request, responder);
            }
        }
    }

    void chunk(HttpContent chunk) throws Exception {
        if (bodyConsumer == null) {
            // If the handler method doesn't want to handle chunk request, the bodyConsumer will be null.
            // It applies to case when the handler method inspects the request and decides to decline it.
            // Usually the handler also closes the connection after declining the request.
            // However, depending on the closing time and the request,
            // there may be some chunk of data already sent by the client.
            return;
        }
        if (chunk instanceof LastHttpContent) {  //TODO: azeez
            bodyConsumerFinish();
        } else {
            bodyConsumerChunk(chunk.content());
        }
    }

    /**
     * Calls the {@link BodyConsumer#chunk(ByteBuf, HttpResponder)} method. If the chunk method call
     * throws exception, the {@link BodyConsumer#handleError(Throwable)} will be called and this method will
     * throw {@link HandlerException}.
     */
    private void bodyConsumerChunk(ByteBuf buffer) throws HandlerException {
        try {
            bodyConsumer.chunk(buffer, responder);
        } catch (Throwable t) {
            throw bodyConsumerError(t);
        }
    }

    /**
     * Calls {@link BodyConsumer#finished(HttpResponder)} method. The current bodyConsumer will be set to {@code null}
     * after the call.
     */
    private void bodyConsumerFinish() {
        BodyConsumer consumer = bodyConsumer;
        bodyConsumer = null;
        consumer.finished(responder);
    }

    /**
     * Calls {@link BodyConsumer#handleError(Throwable)} and throws {@link HandlerException}. The current
     * bodyConsumer will be set to {@code null} after the call.
     */
    private HandlerException bodyConsumerError(Throwable cause) throws HandlerException {
        BodyConsumer consumer = bodyConsumer;
        bodyConsumer = null;
        consumer.handleError(cause);

        throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "", cause);
    }

    /**
     * Sends the error to responder.
     */
    void sendError(HttpResponseStatus status, Throwable ex) {
        String msg;

        if (ex instanceof InvocationTargetException) {
            msg = String.format("Exception Encountered while processing request : %s",
                    Objects.firstNonNull(ex.getCause(), ex).getMessage());
        } else {
            msg = String.format("Exception Encountered while processing request: %s", ex.getMessage());
        }

        // Send the status and message, followed by closing of the connection.
        responder.sendString(status, msg, ImmutableMultimap.of(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE));
    }

    /**
     * Returns true if the handler method's return type is BodyConsumer.
     */
    boolean isStreaming() {
        return isStreaming;
    }

    private HttpRequest rewriteRequest(HttpRequest request, boolean isStreaming) {
        if (!isStreaming) {
            return request;
        }

        //TODO: Azeez handle chunks
    /*boolean isChunked = request.headers().contains(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true)
    if (!isChunked || request.content().readable()) {
      request.setChunked(true);
      request.setContent(Unpooled.EMPTY_BUFFER);
    }*/
        return request;
    }
}
