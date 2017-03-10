/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.msf4j.internal.websocket;

import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.server.ServerEndpoint;

/**
 * Dispatch the registered endpoints.
 * This class will find the best matching resource of an endpoint for a given message type.
 */
public class EndpointDispatcher {

    /**
     * Validate the endpoint against the {@link ServerEndpoint} since without {@link ServerEndpoint} definition
     * there can't be a WebSocket endpoint.
     * @param websocketEndpoint endpoint which should be validated.
     * @throws WebSocketEndpointAnnotationException if endpoint does not contain class level {@link ServerEndpoint}
     */
    public void validateEndpointUri(Object websocketEndpoint) throws WebSocketEndpointAnnotationException {
        if (websocketEndpoint.getClass().isAnnotationPresent(ServerEndpoint.class)) {
            throw new WebSocketEndpointAnnotationException("Cannot find server endpoint url in " +
                                                                   websocketEndpoint.getClass().getName());
        }
    }

    /**
     * Extract the URI from the endpoint.
     * <b>Note that it is better use validateEndpointUri method to validate the endpoint uri
     * before getting it out if needed. Otherwise it will cause issues. Use this method only and only if
     * it is sure that endpoint contains {@link ServerEndpoint} defined.</b>
     *
     * @param webSocketEndpoint WebSocket endpoint which the URI should be extracted.
     * @return the URI of the Endpoint as a String.
     */
    public String getUri(Object webSocketEndpoint) {
        ServerEndpoint serverEndpointAnnotation = webSocketEndpoint.getClass().getAnnotation(ServerEndpoint.class);
        return serverEndpointAnnotation.value();
    }

    /**
     * Extract OnOpen method from the endpoint if exists.
     *
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method optional to handle new connection.
     */
    public Optional<Method> getOnOpenMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        Method returnMethod = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnOpen.class)) {
                returnMethod = method;
                break;
            }
        }
        return Optional.ofNullable(returnMethod);
    }

    /**
     * Extract OnClose method from the endpoint if exists.
     *
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method optional to handle new connection.
     */
    public Optional<Method> getOnCloseMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        Method returnMethod = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnClose.class)) {
                returnMethod = method;
                break;
            }
        }
        return Optional.ofNullable(returnMethod);
    }

    /**
     * Extract OnError method from the endpoint if exists
     *
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method optional to handle errors.
     */
    public Optional<Method> getOnErrorMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        Method returnMethod = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnError.class)) {
                returnMethod = method;
            }
        }
        return Optional.ofNullable(returnMethod);
    }

    /**
     * Extract OnMessage method for String from the endpoint if exists.
     *
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method optional to handle String messages.
     */
    public Optional<Method> getOnStringMessageMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        Method returnMethod = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnMessage.class)) {
                //Adding OnMessage according to their types
                Class<?>[] paraTypes = method.getParameterTypes();
                List<Class<?>> paraList = Arrays.asList(paraTypes);
                if (paraList.contains(String.class)) {
                    returnMethod = method;
                }
            }
        }
        return Optional.ofNullable(returnMethod);
    }

    /**
     * Extract OnMessage method for Binary from the endpoint if exists.
     *
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method optional to handle binary messages.
     */
    public Optional<Method> getOnBinaryMessageMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        Method returnMethod = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnMessage.class)) {
                //Adding OnMessage according to their types
                Class<?>[] paraTypes = method.getParameterTypes();
                List<Class<?>> paraList = Arrays.asList(paraTypes);
                if (paraList.contains(byte[].class) || paraList.contains(ByteBuffer.class)) {
                    returnMethod = method;
                }
            }
        }
        return Optional.ofNullable(returnMethod);
    }

    /**
     * Extract OnMessage method for Pong from the endpoint if exists.
     *
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method optional to handle pong messages.
     */
    public Optional<Method> getOnPongMessageMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        Method returnMethod = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnMessage.class)) {
                //Adding OnMessage according to their types
                Class<?>[] paraTypes = method.getParameterTypes();
                List<Class<?>> paraList = Arrays.asList(paraTypes);
                if (paraList.contains(PongMessage.class)) {
                    returnMethod = method;
                }
            }
        }
        return Optional.ofNullable(returnMethod);
    }
}
