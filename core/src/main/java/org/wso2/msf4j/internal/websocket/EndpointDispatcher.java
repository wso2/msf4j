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
     * @return the URI of the Endpoint as a String.
     * @throws WebSocketEndpointAnnotationException throws if {@link ServerEndpoint} is not found.
     */
    public String getUri(Object webSocketEndpoint) throws WebSocketEndpointAnnotationException {
        ServerEndpoint serverEndpointAnnotation = webSocketEndpoint.getClass().getAnnotation(ServerEndpoint.class);
        if (serverEndpointAnnotation == null) {
            throw new WebSocketEndpointAnnotationException("ServerEndpoint is not declared");
        }
        return serverEndpointAnnotation.value();
    }

    /**
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method to handle new connection if exists else null.
     */
    public Method getOnOpenMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnOpen.class)) {
                return method;
            }
        }
        return null;
    }

    /**
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method to handle connection closure if exists else null.
     */
    public Method getOnCloseMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnClose.class)) {
                return method;
            }
        }
        return null;
    }

    /**
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method to handle errors if exists else null.
     */
    public Method getOnErrorMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnError.class)) {
                return method;
            }
        }
        return null;
    }

    /**
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method to handle String messages if exists else null.
     */
    public Method getOnStringMessageMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnMessage.class)) {
                //Adding OnMessage according to their types
                Class<?>[] paraTypes = method.getParameterTypes();
                List<Class<?>> paraList = Arrays.asList(paraTypes);
                if (paraList.contains(String.class)) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method to handle binary messages if exists else null.
     */
    public Method getOnBinaryMessageMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnMessage.class)) {
                //Adding OnMessage according to their types
                Class<?>[] paraTypes = method.getParameterTypes();
                List<Class<?>> paraList = Arrays.asList(paraTypes);
                if (paraList.contains(byte[].class) || paraList.contains(ByteBuffer.class)) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * @param webSocketEndpoint Endpoint to extract method.
     * @return method to handle pong messages if exists else null.
     */
    public Method getOnPongMessageMethod(Object webSocketEndpoint) {
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnMessage.class)) {
                //Adding OnMessage according to their types
                Class<?>[] paraTypes = method.getParameterTypes();
                List<Class<?>> paraList = Arrays.asList(paraTypes);
                if (paraList.contains(PongMessage.class)) {
                    return method;
                }
            }
        }
        return null;
    }
}
