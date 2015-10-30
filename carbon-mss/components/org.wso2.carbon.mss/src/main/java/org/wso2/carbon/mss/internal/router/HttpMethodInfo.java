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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.HttpStreaming;
import org.wso2.carbon.mss.StreamingInput;
import org.wso2.carbon.mss.internal.router.beanconversion.BeanConversionException;

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
    private final HttpRequest request;
    private final HttpResponder responder;
    private final Object[] args;
    private final ExceptionHandler exceptionHandler;
    private final String mediaType;

    private StreamingInput streamingInput;

    HttpMethodInfo(Method method, Object handler, HttpRequest request,
                   HttpResponder responder, Object[] args,
                   ExceptionHandler exceptionHandler, String mediaType) {
        this.method = method;
        this.handler = handler;
        this.request = request;
        this.responder = responder;
        this.exceptionHandler = exceptionHandler;
        this.mediaType = mediaType;

        // The actual arguments list to invoke handler method
        this.args = args;
    }

    HttpMethodInfo(Method method, Object handler, HttpRequest request,
                   HttpResponder responder, Object[] args,
                   ExceptionHandler exceptionHandler, String mediaType,
                   HttpStreaming httpStreaming) throws HandlerException {
        this(method, handler, request, responder, args, exceptionHandler, mediaType);

        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "Resource method should be void if it accepts chunked requests");
        }
        try {
            method.invoke(handler, args);
        } catch (InvocationTargetException e) {
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "Resource method invocation failed", e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "Resource method invocation access failed", e);
        }
        streamingInput = httpStreaming.getStreamingInput();
        if (streamingInput == null) {
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "Streaming unsupported");
        }
    }

    /**
     * Calls the httpHandler method.
     */
    void invoke() {
        try {
            Object returnVal = method.invoke(handler, args);
            //sending return value as output
            new HttpMethodResponseHandler()
                    .setResponder(responder)
                    .setEntity(returnVal)
                    .setMediaType(mediaType)
                    .send();
        } catch (InvocationTargetException e) {
            exceptionHandler.handle(e.getTargetException(), request, responder);
        } catch (IllegalAccessException | BeanConversionException e) {
            exceptionHandler.handle(e, request, responder);
        }
    }

    void chunk(HttpContent chunk) {
        if (streamingInput == null) {
            // If the handler method doesn't want to handle chunk request, the streamingInput will be null.
            // It applies to case when the handler method inspects the request and decides to decline it.
            // Usually the handler also closes the connection after declining the request.
            // However, depending on the closing time and the request,
            // there may be some chunk of data already sent by the client.
            return;
        }
        try {
            if (chunk instanceof LastHttpContent) {
                bodyConsumerFinish(chunk.content());
            } else {
                bodyConsumerChunk(chunk.content());
            }
        } catch (HandlerException e) {
            exceptionHandler.handle(e, request, responder);
        }
    }

    void error(Throwable e) {
        try {
            if (streamingInput != null) {
                bodyConsumerError(e);
            }
            exceptionHandler.handle(e, request, responder);
        } catch (HandlerException ex) {
            exceptionHandler.handle(ex, request, responder);
        }
    }

    /**
     * Calls the {@link StreamingInput#chunk(io.netty.buffer.ByteBuf,
     * org.wso2.carbon.mss.HttpResponder)} method.
     * <p/>
     * If the chunk method calls throws exception,
     * the {@link StreamingInput#handleError(Throwable)} will be called and
     * this method will throw {@link org.wso2.carbon.mss.internal.router.HandlerException}.
     */
    private void bodyConsumerChunk(ByteBuf buffer) throws HandlerException {
        try {
            streamingInput.chunk(buffer, responder);
        } catch (Throwable t) {
            bodyConsumerError(t);
        }
    }

    /**
     * Calls {@link StreamingInput#finished(io.netty.buffer.ByteBuf, org.wso2.carbon.mss.HttpResponder)}
     * method. The current streamingInput will be set to {@code null} after the call.
     */
    private void bodyConsumerFinish(ByteBuf buffer) throws HandlerException {
        try {
            StreamingInput consumer = streamingInput;
            streamingInput = null;
            consumer.finished(buffer, responder);
        } catch (Throwable t) {
            bodyConsumerError(t);
        }
    }

    /**
     * Calls {@link StreamingInput#handleError(Throwable)} and
     * throws {@link org.wso2.carbon.mss.internal.router.HandlerException}. The current streamingInput will be set
     * to {@code null} after the call.
     */
    private void bodyConsumerError(Throwable cause) throws HandlerException {
        StreamingInput consumer = streamingInput;
        streamingInput = null;
        consumer.handleError(cause);

        throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "", cause);
    }
}
