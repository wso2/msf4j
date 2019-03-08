/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.internal;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.internal.router.PatternPathRouter;
import org.wso2.msf4j.internal.websocket.CloseCodeImpl;
import org.wso2.msf4j.internal.websocket.EndpointDispatcher;
import org.wso2.msf4j.internal.websocket.EndpointsRegistryImpl;
import org.wso2.msf4j.internal.websocket.WebSocketPongMessage;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointMethodReturnTypeException;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.server.PathParam;

/**
 * WebSocketConnectorListener implementation for MSF4J.
 *
 * This will process all the websocket messages which are coming to MSF4J.
 */
@Component(
        name = "org.wso2.msf4j.internal.MSF4JWSConnectorListener",
        immediate = true,
        service = WebSocketConnectorListener.class
)
public class MSF4JWSConnectorListener implements WebSocketConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(MSF4JWSConnectorListener.class);

    @Override
    public void onHandshake(WebSocketHandshaker webSocketHandshaker) {
        ServerHandshakeFuture handshakeFuture = webSocketHandshaker.handshake();
        handshakeFuture.setHandshakeListener(new ServerHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection) {
                handleWebSocketHandshake(webSocketHandshaker, webSocketConnection);
                webSocketConnection.startReadingFrames();
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Unexpected error occur while handshake.", throwable);
            }
        });
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
        String uri = webSocketTextMessage.getTarget();
        PatternPathRouter.RoutableDestination<Object> routableEndpoint = endpointsRegistry.getRoutableEndpoint(uri);
        handleTextWebSocketMessage(webSocketTextMessage, routableEndpoint, webSocketTextMessage
                .getWebSocketConnection());
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
        String uri = webSocketBinaryMessage.getTarget();
        PatternPathRouter.RoutableDestination<Object> routableEndpoint = endpointsRegistry.getRoutableEndpoint(uri);
        handleBinaryWebSocketMessage(webSocketBinaryMessage, routableEndpoint,
                webSocketBinaryMessage.getWebSocketConnection());
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
        String uri = webSocketControlMessage.getTarget();
        PatternPathRouter.RoutableDestination<Object> routableEndpoint = endpointsRegistry.getRoutableEndpoint(uri);
        handleControlCarbonMessage(webSocketControlMessage, routableEndpoint,
                webSocketControlMessage.getWebSocketConnection());
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
        String uri = webSocketCloseMessage.getTarget();
        PatternPathRouter.RoutableDestination<Object> routableEndpoint = endpointsRegistry.getRoutableEndpoint(uri);
        handleCloseWebSocketMessage(webSocketCloseMessage, routableEndpoint, webSocketCloseMessage
                .getWebSocketConnection());
    }

    @Override
    public void onClose(WebSocketConnection webSocketConnection) {
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        log.error("Unexpected error occur.", throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage webSocketControlMessage) {

    }

    private boolean handleWebSocketHandshake(WebSocketHandshaker carbonMessage,
                                             WebSocketConnection webSocketConnection) {
        EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
        String requestUri = carbonMessage.getTarget();
        PatternPathRouter.RoutableDestination<Object> routableEndpoint =
                endpointsRegistry.getRoutableEndpoint(requestUri);
        if (routableEndpoint == null) {
            throw new RuntimeException("Error while connecting to server. Routable endpoint is not registered for the" +
                    " request uri:" + requestUri);
        }
        Optional<Method> methodOptional = new EndpointDispatcher().getOnOpenMethod(routableEndpoint.getDestination());
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        try {
            List<Object> parameterList = new LinkedList<>();
            methodOptional.ifPresent(method -> {
                Arrays.stream(method.getParameters()).forEach(parameter -> {
                    if (parameter.getType() == WebSocketConnection.class) {
                        parameterList.add(webSocketConnection);
                    } else if (parameter.getType() == String.class) {
                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                        if (pathParam != null) {
                            parameterList.add(paramValues.get(pathParam.value()));
                        }
                    } else {
                        parameterList.add(null);
                    }
                });
                executeMethod(method, routableEndpoint.getDestination(), parameterList, webSocketConnection);
            });
            return true;
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, webSocketConnection);
            return false;
        }
    }

    private void handleTextWebSocketMessage(WebSocketTextMessage textCarbonMessage,
                                            PatternPathRouter.RoutableDestination<Object> routableEndpoint,
                                            WebSocketConnection webSocketConnection) {
        if (routableEndpoint == null) {
            throw new RuntimeException("Error while handling the message. Routable endpoint is not registered for the" +
                    " request uri:" + textCarbonMessage.getTarget());
        }
        Object endpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnStringMessageMethod(endpoint);
        try {
            methodOptional.ifPresent(
                    method -> {
                        List<Object> parameterList = new LinkedList<>();
                        Arrays.stream(method.getParameters()).forEach(
                                parameter -> {
                                    if (parameter.getType() == String.class) {
                                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                                        if (pathParam == null) {
                                            parameterList.add(textCarbonMessage.getText());
                                        } else {
                                            parameterList.add(paramValues.get(pathParam.value()));
                                        }
                                    } else if (parameter.getType() == WebSocketConnection.class) {
                                        parameterList.add(webSocketConnection);
                                    } else {
                                        parameterList.add(null);
                                    }
                                }
                        );
                        executeMethod(method, endpoint, parameterList, webSocketConnection);
                    }
            );
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, webSocketConnection);
        }
    }

    private void handleBinaryWebSocketMessage(WebSocketBinaryMessage binaryCarbonMessage,
                                              PatternPathRouter.RoutableDestination<Object> routableEndpoint,
                                              WebSocketConnection webSocketConnection) {
        if (routableEndpoint == null) {
            throw new RuntimeException("Error while handling the message. Routable endpoint is not registered for the" +
                    " request uri:" + binaryCarbonMessage.getTarget());
        }
        Object webSocketEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnBinaryMessageMethod(webSocketEndpoint);
        try {
            methodOptional.ifPresent(method -> {
                List<Object> parameterList = new LinkedList<>();
                Arrays.stream(method.getParameters()).forEach(parameter -> {
                    if (parameter.getType() == ByteBuffer.class) {
                        parameterList.add(binaryCarbonMessage.getByteBuffer());
                    } else if (parameter.getType() == byte[].class) {
                        ByteBuffer buffer = binaryCarbonMessage.getByteBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        for (int i = 0; i < buffer.capacity(); i++) {
                            bytes[i] = buffer.get();
                        }
                        parameterList.add(bytes);
                    } else if (parameter.getType() == boolean.class) {
                        parameterList.add(binaryCarbonMessage.isFinalFragment());
                    } else if (parameter.getType() == WebSocketConnection.class) {
                        parameterList.add(webSocketConnection);
                    } else if (parameter.getType() == String.class) {
                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                        if (pathParam != null) {
                            parameterList.add(paramValues.get(pathParam.value()));
                        }
                    } else {
                        parameterList.add(null);
                    }
                });
                executeMethod(method, webSocketEndpoint, parameterList, webSocketConnection);
            });
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, webSocketConnection);
        }
    }

    private void handleCloseWebSocketMessage(WebSocketCloseMessage closeCarbonMessage,
                                             PatternPathRouter.RoutableDestination<Object> routableEndpoint,
                                             WebSocketConnection webSocketConnection) {
        if (routableEndpoint == null) {
            throw new RuntimeException("Error while handling the message. Routable endpoint is not registered for the" +
                    " request uri:" + closeCarbonMessage.getTarget());
        }
        Object webSocketEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnCloseMethod(webSocketEndpoint);
        try {
            methodOptional.ifPresent(method -> {
                List<Object> parameterList = new LinkedList<>();
                Arrays.stream(method.getParameters()).forEach(parameter -> {
                    if (parameter.getType() == CloseReason.class) {
                        CloseReason.CloseCode closeCode = new CloseCodeImpl(closeCarbonMessage.getCloseCode());
                        CloseReason closeReason = new CloseReason(closeCode, closeCarbonMessage.getCloseReason());
                        parameterList.add(closeReason);
                    } else if (parameter.getType() == WebSocketConnection.class) {
                        parameterList.add(webSocketConnection);
                    } else if (parameter.getType() == String.class) {
                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                        if (pathParam != null) {
                            parameterList.add(paramValues.get(pathParam.value()));
                        }
                    } else {
                        parameterList.add(null);
                    }
                });
                executeMethod(method, webSocketEndpoint, parameterList, webSocketConnection);
            });
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, webSocketConnection);
        } finally {
            // TODO: this is temp fix assuming websocket connection is blocking call.
            if (webSocketConnection.isOpen()) {
                webSocketConnection.terminateConnection();
            }
        }

    }

    private void handleControlCarbonMessage(WebSocketControlMessage controlCarbonMessage, PatternPathRouter.
            RoutableDestination<Object> routableEndpoint, WebSocketConnection webSocketConnection) {
        if (routableEndpoint == null) {
            throw new RuntimeException("Error while handling the message. Routable endpoint is not registered for the" +
                    " request uri:" + controlCarbonMessage.getTarget());
        }
        Object webSocketEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnPongMessageMethod(webSocketEndpoint);
        try {
            methodOptional.ifPresent(method -> {
                List<Object> parameterList = new LinkedList<>();
                Arrays.stream(method.getParameters()).forEach(parameter -> {
                    if (parameter.getType() == PongMessage.class) {
                        parameterList.add(new WebSocketPongMessage(controlCarbonMessage.getByteBuffer()));
                    } else if (parameter.getType() == WebSocketConnection.class) {
                        parameterList.add(webSocketConnection);
                    } else if (parameter.getType() == String.class) {
                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                        if (pathParam != null) {
                            parameterList.add(paramValues.get(pathParam.value()));
                        }
                    } else {
                        parameterList.add(null);
                    }
                });
                executeMethod(method, webSocketEndpoint, parameterList, webSocketConnection);
            });
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, webSocketConnection);
        }
    }

    private void handleError(Throwable throwable, PatternPathRouter.RoutableDestination<Object> routableEndpoint,
                             WebSocketConnection webSocketConnection) {
        Object webSocketEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnErrorMethod(webSocketEndpoint);
        methodOptional.ifPresent(method -> {
            List<Object> parameterList = new LinkedList<>();
            Arrays.stream(method.getParameters()).forEach(parameter -> {
                if (parameter.getType() == Throwable.class) {
                    parameterList.add(throwable);
                } else if (parameter.getType() == WebSocketConnection.class) {
                    parameterList.add(webSocketConnection);
                } else if (parameter.getType() == String.class) {
                    PathParam pathParam = parameter.getAnnotation(PathParam.class);
                    if (pathParam != null) {
                        parameterList.add(paramValues.get(pathParam.value()));
                    }
                } else {
                    parameterList.add(null);
                }
            });
            executeMethod(method, webSocketEndpoint, parameterList, webSocketConnection);
        });
    }

    private void executeMethod(Method method, Object webSocketEndpoint, List<Object> parameterList,
                               WebSocketConnection webSocketConnection) {
        try {
            if (method.getReturnType() == String.class) {
                String returnStr = (String) method.invoke(webSocketEndpoint, parameterList.toArray());
                webSocketConnection.pushText(returnStr);
            } else if (method.getReturnType() == ByteBuffer.class) {
                ByteBuffer buffer = (ByteBuffer) method.invoke(webSocketEndpoint, parameterList.toArray());
                webSocketConnection.pushBinary(buffer);
            } else if (method.getReturnType() == byte[].class) {
                byte[] bytes = (byte[]) method.invoke(webSocketEndpoint, parameterList.toArray());
                webSocketConnection.pushBinary(ByteBuffer.wrap(bytes));
            } else if (method.getReturnType() == void.class) {
                method.invoke(webSocketEndpoint, parameterList.toArray());
            } else if (method.getReturnType() == PongMessage.class) {
                PongMessage pongMessage = (PongMessage) method.invoke(webSocketEndpoint, parameterList.toArray());
                webSocketConnection.pong(pongMessage.getApplicationData());
            } else {
                throw new WebSocketEndpointMethodReturnTypeException("Unknown return type.");
            }
        } catch (IllegalAccessException e) {
            log.error("Illegal access exception occurred: " + e.toString());
        } catch (InvocationTargetException e) {
            log.error("Method invocation failed: " + e.toString());
        } catch (WebSocketEndpointMethodReturnTypeException e) {
            log.error("WebSocket method return type exception occurred: " + e.toString());
        }
    }
}
