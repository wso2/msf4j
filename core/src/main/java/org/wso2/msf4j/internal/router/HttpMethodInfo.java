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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * HttpMethodInfo is a helper class having state information about the http handler method to be invoked, the handler
 * and arguments required for invocation by the Dispatcher. RequestRouter populates this class and stores in its
 * context as attachment.
 */
public class HttpMethodInfo {

    private final Method method;
    private final Object handler;
    private final Object[] args;
    private Response responder;
    private HttpStreamHandler httpStreamHandler;
    private static final Logger log = LoggerFactory.getLogger(HttpMethodInfo.class);

    /**
     * Construct HttpMethodInfo object for a handler
     * method that does not support streaming.
     *
     * @param method    handler method
     * @param handler   object of the handler method
     * @param args      method arguments array
     * @param responder responder object
     */
    public HttpMethodInfo(Method method,
                          Object handler,
                          Object[] args,
                          Response responder) {
        this.method = method;
        this.handler = handler;
        this.args = Arrays.copyOf(args, args.length);
        this.responder = responder;
    }

    /**
     * Construct HttpMethodInfo object for a streaming
     * supported handler method.
     *
     * @param method       handler method
     * @param handler      object of the handler method
     * @param args         method arguments array
     * @param responder    responder object
     * @param httpStreamer streaming handler
     * @throws HandlerException throws when HttpMethodInfo construction is unsuccessful
     */
    public HttpMethodInfo(Method method,
                          Object handler,
                          Object[] args,
                          Response responder,
                          HttpStreamer httpStreamer) throws HandlerException {
        this(method, handler, args, responder);

        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method should be void if it accepts chunked requests");
        }
        try {
            method.invoke(handler, args);
        } catch (InvocationTargetException e) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method invocation failed", e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method invocation access failed", e);
        }
        httpStreamHandler = httpStreamer.getHttpStreamHandler();
        if (httpStreamHandler == null) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Streaming unsupported");
        }
        httpStreamHandler.init(this.responder);
    }

    /**
     * Calls the http resource method.
     *
     * @throws Exception if error occurs while invoking the resource method
     */
    public void invoke() throws Exception {
        Object returnVal = method.invoke(handler, args);
        responder.setEntity(returnVal);
        responder.send();
    }

    /**
     * If chunk handling is supported provide chunks directly.
     *
     * @param chunk chunk content
     * @throws Exception if error occurs while invoking streaming handlers
     */
    public void chunk(ByteBuffer chunk) throws Exception {
        try {
            httpStreamHandler.chunk(chunk);
        } catch (Throwable e) {
            log.error("Exception while invoking streaming handlers", e);
            httpStreamHandler.error(e);
            throw e;
        }
    }

    /**
     * If chunk handling is supported end streaming chunks.
     *
     * @throws Exception if error occurs while stopping streaming handlers
     */
    public void end() throws Exception {
        try {
            httpStreamHandler.end();
        } catch (Throwable e) {
            log.error("Exception while invoking streaming handlers", e);
            httpStreamHandler.error(e);
            throw e;
        }
    }

    /**
     * Return true if the handler method supports streaming.
     *
     * @return boolean true if streaming is supported
     */
    public boolean isStreamingSupported() {
        return httpStreamHandler != null;
    }
}
