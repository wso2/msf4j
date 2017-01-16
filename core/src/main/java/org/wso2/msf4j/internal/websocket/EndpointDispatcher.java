/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.msf4j.internal.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.WebSocketEndpoint;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.server.ServerEndpoint;

/**
 * Dispatch the registered endpoints.
 *
 * @since 1.0.0
 */
public class EndpointDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointDispatcher.class);
    private String uri;
    private Method onOpenMethod = null;
    private Method onStringMessageMethod = null;
    private Method onBinaryMessageMethod = null;
    private Method onPongMessageMethod = null;
    private Method onCloseMethod = null;
    private WebSocketEndpoint webSocketEndpoint = null;

    public EndpointDispatcher(WebSocketEndpoint webSocketEndpoint) throws Exception {
        this.webSocketEndpoint = webSocketEndpoint;
        dispatch(webSocketEndpoint);
    }

    public DispatchedEndpoint getDispatchedEndpoint() {
        return new DispatchedEndpoint(uri, onOpenMethod, onStringMessageMethod, onBinaryMessageMethod,
                                      onPongMessageMethod, onCloseMethod, webSocketEndpoint);
    }

    //Dispatch the WebSocketEndpoint
    private void dispatch(WebSocketEndpoint webSocketEndpoint) throws Exception {

        ServerEndpoint serverEndpointAnnotation = webSocketEndpoint.getClass().getAnnotation(ServerEndpoint.class);
        if (serverEndpointAnnotation == null) {
            throw new Exception("ServerEndpoint is not declared");
        }

        uri = serverEndpointAnnotation.value();
        Method[] methods = webSocketEndpoint.getClass().getMethods();
        Arrays.stream(methods).forEach(
                method -> {
                    if (method.isAnnotationPresent(OnMessage.class)) {
                        //Adding OnMessage according to their types
                        Class<?>[] paraTypes = method.getParameterTypes();
                        List<Class<?>> paraList = Arrays.asList(paraTypes);
                        if (paraList.contains(byte[].class) || paraList.contains(ByteBuffer.class)) {
                            onBinaryMessageMethod = method;
                        } else if (paraList.contains(PongMessage.class)) {
                            onPongMessageMethod = method;
                        } else if (paraList.contains(String.class)) {
                            onStringMessageMethod = method;
                        }
                    } else if (method.isAnnotationPresent(OnOpen.class)) {
                        onOpenMethod = method;
                    } else if (method.isAnnotationPresent(OnClose.class)) {
                        onCloseMethod = method;
                    }
                }
        );
    }
}
