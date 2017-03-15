/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.internal.router.HandlerException;
import org.wso2.msf4j.internal.router.HttpMethodInfo;
import org.wso2.msf4j.internal.router.HttpMethodInfoBuilder;
import org.wso2.msf4j.internal.router.HttpResourceModel;
import org.wso2.msf4j.internal.router.PatternPathRouter;
import org.wso2.msf4j.internal.router.Util;
import org.wso2.msf4j.internal.websocket.CloseCodeImpl;
import org.wso2.msf4j.internal.websocket.EndpointDispatcher;
import org.wso2.msf4j.internal.websocket.EndpointsRegistryImpl;
import org.wso2.msf4j.internal.websocket.WebSocketPongMessage;
import org.wso2.msf4j.util.HttpUtil;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Process carbon messages for MSF4J.
 */
@Component(
        name = "org.wso2.msf4j.internal.MSF4JMessageProcessor",
        immediate = true,
        service = CarbonMessageProcessor.class
)
public class MSF4JMessageProcessor implements CarbonMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(MSF4JMessageProcessor.class);
    private static final String MSF4J_MSG_PROC_ID = "MSF4J-CM-PROCESSOR";

    //TODO need to way to configure the pool size
    private ExecutorService executorService =
            Executors.newFixedThreadPool(60, new MSF4JThreadFactory(new ThreadGroup("msf4j.executor.workerpool")));

    public MSF4JMessageProcessor() {
    }

    public MSF4JMessageProcessor(String channelId, MicroservicesRegistryImpl microservicesRegistry) {
        DataHolder.getInstance().getMicroservicesRegistries().put(channelId, microservicesRegistry);
    }

    /**
     * Carbon message handler.
     */
    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        // If we are running on OSGi mode need to get the registry based on the channel_id.
        executorService.execute(() -> {
            //Identify the protocol name before doing the processing
            String protocolName = (String) carbonMessage.getProperty(Constants.PROTOCOL);

            if (Constants.HTTP_PROTOCOL_NAME.equalsIgnoreCase(protocolName)) {
                MicroservicesRegistryImpl currentMicroservicesRegistry =
                        DataHolder.getInstance().getMicroservicesRegistries()
                                .get(carbonMessage.getProperty(MSF4JConstants.CHANNEL_ID));
                Request request = new Request(carbonMessage);
                request.setSessionManager(currentMicroservicesRegistry.getSessionManager());
                Response response = new Response(carbonCallback, request);
                try {
                    dispatchMethod(currentMicroservicesRegistry, request, response);
                } catch (HandlerException e) {
                    handleHandlerException(e, carbonCallback);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof HandlerException) {
                        handleHandlerException((HandlerException) targetException, carbonCallback);
                    } else {
                        handleThrowable(currentMicroservicesRegistry, targetException, carbonCallback, request);
                    }
                } catch (InterceptorException e) {
                    log.warn("Interceptors threw an exception", e);
                    // TODO: improve the response
                    carbonCallback.done(HttpUtil.createTextResponse(
                            javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            HttpUtil.EMPTY_BODY));
                } catch (Throwable t) {
                    handleThrowable(currentMicroservicesRegistry, t, carbonCallback, request);
                } finally {
                    // Calling the release method to make sure that there won't be any memory leaks from netty
                    carbonMessage.release();
                }
            } else if (Constants.WEBSOCKET_PROTOCOL_NAME.equalsIgnoreCase(protocolName)) {
                EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
                PatternPathRouter.RoutableDestination<Object> routableEndpoint = null;
                Session session = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SESSION);
                String uri = (String) carbonMessage.getProperty(Constants.TO);
                try {
                    routableEndpoint = endpointsRegistry.getRoutableEndpoint(uri);
                    dispatchWebSocketMethod(routableEndpoint, carbonMessage);
                } catch (WebSocketEndpointAnnotationException e) {
                    handleError(e, routableEndpoint, session);
                }

            } else  {
                log.error("Cannot find the protocol to dispatch.");
            }
        });
        return true;
    }

    /**
     * Dispatch appropriate resource method.
     */
    private void dispatchMethod(MicroservicesRegistryImpl currentMicroservicesRegistry, Request request,
                                Response response) throws Exception {
        HttpUtil.setConnectionHeader(request, response);
        PatternPathRouter.RoutableDestination<HttpResourceModel> destination =
                currentMicroservicesRegistry.
                        getMetadata().
                        getDestinationMethod(request.getUri(), request.getHttpMethod(), request.getContentType(),
                                request.getAcceptTypes());
        HttpResourceModel resourceModel = destination.getDestination();
        response.setMediaType(Util.getResponseType(request.getAcceptTypes(),
                resourceModel.getProducesMediaTypes()));
        InterceptorExecutor interceptorExecutor = new InterceptorExecutor(resourceModel, request, response,
                                                                          currentMicroservicesRegistry
                                                                                  .getInterceptors());
        if (interceptorExecutor.execPreCalls()) { // preCalls can throw exceptions

            HttpMethodInfoBuilder httpMethodInfoBuilder =
                    new HttpMethodInfoBuilder().
                            httpResourceModel(resourceModel).
                            httpRequest(request).
                            httpResponder(response).
                            requestInfo(destination.getGroupNameValues());

            HttpMethodInfo httpMethodInfo = httpMethodInfoBuilder.build();
            if (httpMethodInfo.isStreamingSupported()) {
                while (!(request.isEmpty() && request.isEomAdded())) {
                    httpMethodInfo.chunk(request.getMessageBody());
                }
                httpMethodInfo.end();
            } else {
                httpMethodInfo.invoke(request, destination);
            }
            interceptorExecutor.execPostCalls(response.getStatusCode()); // postCalls can throw exceptions
        }
    }

    private void handleThrowable(MicroservicesRegistryImpl currentMicroservicesRegistry, Throwable throwable,
                                 CarbonCallback carbonCallback, Request request) {
        Optional<ExceptionMapper> exceptionMapper = currentMicroservicesRegistry.getExceptionMapper(throwable);
        if (exceptionMapper.isPresent()) {
            org.wso2.msf4j.Response msf4jResponse =
                    new org.wso2.msf4j.Response(carbonCallback, request);
            msf4jResponse.setEntity(exceptionMapper.get().toResponse(throwable));
            msf4jResponse.send();
        } else {
            log.warn("Unmapped exception", throwable);
            carbonCallback.done(HttpUtil.
                    createTextResponse(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            "Exception occurred :" + throwable.getMessage()));
        }
    }

    private void handleHandlerException(HandlerException e, CarbonCallback carbonCallback) {
        carbonCallback.done(e.getFailureResponse());
    }

    /**
     * Dispatch the message to correct WebSocket endpoint method.
     *
     * @param carbonMessage incoming carbonMessage.
     * @param routableEndpoint dispatched endpoint for a given endpoint.
     */
    private void dispatchWebSocketMethod(PatternPathRouter.RoutableDestination<Object> routableEndpoint,
                                         CarbonMessage carbonMessage) throws WebSocketEndpointAnnotationException {
        Session session = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SESSION);
        if (session == null) {
            throw new IllegalStateException("WebSocket session not found.");
        }
        //Invoke correct method with correct parameters
        if (carbonMessage instanceof TextCarbonMessage) {
            TextCarbonMessage textCarbonMessage = (TextCarbonMessage) carbonMessage;
            handleTextWebSocketMessage(textCarbonMessage, routableEndpoint, session);
        } else if (carbonMessage instanceof BinaryCarbonMessage) {
            BinaryCarbonMessage binaryCarbonMessage = (BinaryCarbonMessage) carbonMessage;
            handleBinaryWebSocketMessage(binaryCarbonMessage, routableEndpoint, session);
        } else if (carbonMessage instanceof StatusCarbonMessage) {
            StatusCarbonMessage statusCarbonMessage = (StatusCarbonMessage) carbonMessage;
            if (org.wso2.carbon.messaging.Constants.STATUS_OPEN.equals(statusCarbonMessage.getStatus())) {
                String connection = (String) carbonMessage.getProperty(Constants.CONNECTION);
                String upgrade = (String) carbonMessage.getProperty(Constants.UPGRADE);
                if (Constants.UPGRADE.equalsIgnoreCase(connection) &&
                        Constants.WEBSOCKET_UPGRADE.equalsIgnoreCase(upgrade)) {
                    handleWebSocketHandshake(carbonMessage, session);
                }
            } else if (org.wso2.carbon.messaging.Constants.STATUS_CLOSE.equals(statusCarbonMessage.getStatus())) {
                handleCloseWebSocketMessage(statusCarbonMessage, routableEndpoint, session);
            }
        } else if (carbonMessage instanceof ControlCarbonMessage) {
            ControlCarbonMessage controlCarbonMessage = (ControlCarbonMessage) carbonMessage;
            handleControlCarbonMessage(controlCarbonMessage, routableEndpoint, session);
        }
    }

    private boolean handleWebSocketHandshake(CarbonMessage carbonMessage, Session session) {
        EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
        String requestUri = (String) carbonMessage.getProperty(Constants.TO);
        PatternPathRouter.RoutableDestination<Object>
                routableEndpoint = endpointsRegistry.getRoutableEndpoint(requestUri);
        Optional<Method> methodOptional = new EndpointDispatcher().getOnOpenMethod(routableEndpoint.getDestination());
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        try {
            List<Object> parameterList = new LinkedList<>();
            methodOptional.ifPresent(
                    method -> {
                        Arrays.stream(method.getParameters()).forEach(
                                parameter -> {
                                    if (parameter.getType() == Session.class) {
                                        parameterList.add(session);
                                    } else if (parameter.getType() == String.class) {
                                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                                        if (pathParam != null) {
                                            parameterList.add(paramValues.get(pathParam.value()));
                                        } else {
                                            throw new IllegalArgumentException("String parameters without" +
                                                                                       " @PathParam annotation");
                                        }
                                    } else {
                                        parameterList.add(null);
                                    }
                                }
                        );
                        executeMethod(method, routableEndpoint.getDestination(), parameterList, session);
                    }
            );
            return true;
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, session);
            return false;
        }
    }

    private void handleTextWebSocketMessage(TextCarbonMessage textCarbonMessage,
                                            PatternPathRouter.RoutableDestination<Object> routableEndpoint,
                                            Session session) {
        Object endpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnStringMessageMethod(endpoint);
        try {
            methodOptional.ifPresent(
                    method -> {
                        List<Object> parameterList = new LinkedList<>();
                        boolean isStringSatifsfied = false;
                        Arrays.stream(method.getParameters()).forEach(
                                parameter -> {
                                    if (parameter.getType() == String.class) {
                                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                                        if (pathParam == null) {
                                            parameterList.add(textCarbonMessage.getText());
                                        } else {
                                            if (!isStringSatifsfied) {
                                                parameterList.add(paramValues.get(pathParam.value()));
                                            } else {
                                                throw new IllegalArgumentException("String parameters without" +
                                                                                           " @PathParam annotation");
                                            }
                                        }
                                    } else if (parameter.getType() == Session.class) {
                                        parameterList.add(session);
                                    } else {
                                        parameterList.add(null);
                                    }
                                }
                        );
                        executeMethod(method, endpoint, parameterList, session);
                    }
            );
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, session);
        }
    }

    private void handleBinaryWebSocketMessage(BinaryCarbonMessage binaryCarbonMessage,
                                              PatternPathRouter.RoutableDestination<Object>
                                                      routableEndpoint, Session session) {
        Object webSocketEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnBinaryMessageMethod(webSocketEndpoint);
        try {
            methodOptional.ifPresent(
                    method -> {
                        List<Object> parameterList = new LinkedList<>();
                        Arrays.stream(method.getParameters()).forEach(
                                parameter -> {
                                    if (parameter.getType() == ByteBuffer.class) {
                                        parameterList.add(binaryCarbonMessage.readBytes());
                                    } else if (parameter.getType() == byte[].class) {
                                        ByteBuffer buffer = binaryCarbonMessage.readBytes();
                                        byte[] bytes = new byte[buffer.capacity()];
                                        for (int i = 0; i < buffer.capacity(); i++) {
                                            bytes[i] = buffer.get();
                                        }
                                        parameterList.add(bytes);
                                    } else if (parameter.getType() == boolean.class) {
                                        parameterList.add(binaryCarbonMessage.isFinalFragment());
                                    } else if (parameter.getType() == Session.class) {
                                        parameterList.add(session);
                                    } else if (parameter.getType() == String.class) {
                                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                                        if (pathParam != null) {
                                            parameterList.add(paramValues.get(pathParam.value()));
                                        } else {
                                            throw new IllegalArgumentException("String parameters without" +
                                                                                       " @PathParam annotation");
                                        }
                                    } else {
                                        parameterList.add(null);
                                    }
                                }
                        );
                        executeMethod(method, webSocketEndpoint, parameterList, session);
                    }
            );
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, session);
        }
    }

    private void handleCloseWebSocketMessage(StatusCarbonMessage closeCarbonMessage,
                                             PatternPathRouter.RoutableDestination<Object>
                                                     routableEndpoint, Session session) {
        Object webSocketEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnCloseMethod(webSocketEndpoint);
        try {
            methodOptional.ifPresent(
                    method -> {
                        List<Object> parameterList = new LinkedList<>();
                        Arrays.stream(method.getParameters()).forEach(
                                parameter -> {
                                    if (parameter.getType() == CloseReason.class) {
                                        CloseReason.CloseCode closeCode = new CloseCodeImpl(
                                                closeCarbonMessage.getStatusCode());
                                        CloseReason closeReason = new CloseReason(
                                                closeCode, closeCarbonMessage.getReasonText());
                                        parameterList.add(closeReason);
                                    } else if (parameter.getType() == Session.class) {
                                        parameterList.add(session);
                                    } else if (parameter.getType() == String.class) {
                                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                                        if (pathParam != null) {
                                            parameterList.add(paramValues.get(pathParam.value()));
                                        } else {
                                            throw new IllegalArgumentException("String parameters without" +
                                                                                       " @PathParam annotation");
                                        }
                                    } else {
                                        parameterList.add(null);
                                    }
                                }
                        );
                        executeMethod(method, webSocketEndpoint, parameterList, session);
                    }
            );
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, session);
        }
    }

    private void handleControlCarbonMessage(ControlCarbonMessage controlCarbonMessage, PatternPathRouter.
            RoutableDestination<Object> routableEndpoint, Session session) {
        Object webSocketEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnPongMessageMethod(webSocketEndpoint);
        try {
            methodOptional.ifPresent(
                    method -> {
                        List<Object> parameterList = new LinkedList<>();
                        Arrays.stream(method.getParameters()).forEach(
                                parameter -> {
                                    if (parameter.getType() == PongMessage.class) {
                                        parameterList.add(new WebSocketPongMessage(controlCarbonMessage.readBytes()));
                                    } else if (parameter.getType() == Session.class) {
                                        parameterList.add(session);
                                    } else if (parameter.getType() == String.class) {
                                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                                        if (pathParam != null) {
                                            parameterList.add(paramValues.get(pathParam.value()));
                                        } else {
                                            throw new IllegalArgumentException("String parameters " +
                                                                                       "without @PathParam annotation");
                                        }
                                    } else {
                                        parameterList.add(null);
                                    }
                                }
                        );
                        executeMethod(method, webSocketEndpoint, parameterList, session);
                    }
            );
        } catch (Throwable throwable) {
            handleError(throwable, routableEndpoint, session);
        }
    }

    private void handleError(Throwable throwable, PatternPathRouter.RoutableDestination<Object> routableEndpoint,
                             Session session) {
        Object webSocketEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Optional<Method> methodOptional = new EndpointDispatcher().getOnErrorMethod(webSocketEndpoint);
        methodOptional.ifPresent(
                method -> {
                    List<Object> parameterList = new LinkedList<>();
                    Arrays.stream(method.getParameters()).forEach(
                            parameter -> {
                                if (parameter.getType() == Throwable.class) {
                                    parameterList.add(throwable);
                                } else if (parameter.getType() == Session.class) {
                                    parameterList.add(session);
                                } else if (parameter.getType() == String.class) {
                                    PathParam pathParam = parameter.getAnnotation(PathParam.class);
                                    if (pathParam != null) {
                                        parameterList.add(paramValues.get(pathParam.value()));
                                    } else {
                                        throw new IllegalArgumentException("String parameters " +
                                                                                   "without @PathParam annotation");
                                    }
                                } else {
                                    parameterList.add(null);
                                }
                            }
                    );
                    executeMethod(method, webSocketEndpoint, parameterList, session);
                }
        );
    }

    private void executeMethod(Method method, Object webSocketEndpoint,
                               List<Object> parameterList, Session session) {
        try {
            if (method.getReturnType() == String.class) {
                String returnStr = (String) method.invoke(webSocketEndpoint, parameterList.toArray());
                session.getBasicRemote().sendText(returnStr);
            } else if (method.getReturnType() == ByteBuffer.class) {
                ByteBuffer buffer = (ByteBuffer) method.invoke(webSocketEndpoint, parameterList.toArray());
                session.getBasicRemote().sendBinary(buffer);
            } else if (method.getReturnType() == byte[].class) {
                byte[] bytes = (byte[]) method.invoke(webSocketEndpoint, parameterList.toArray());
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(bytes));
            } else if (method.getReturnType() == void.class) {
                method.invoke(webSocketEndpoint, parameterList.toArray());
            } else if (method.getReturnType() == PongMessage.class) {
                PongMessage pongMessage = (PongMessage) method.invoke(webSocketEndpoint, parameterList.toArray());
                session.getBasicRemote().sendPong(pongMessage.getApplicationData());
            } else {
                throw new IllegalArgumentException("Unknown return type.");
            }
        } catch (IllegalAccessException e) {
            log.error("Illegal access exception occurred: " + e.toString());
        } catch (InvocationTargetException e) {
            log.error("Method invocation failed: " + e.toString());
        } catch (IOException e) {
            log.error("IOException occurred: " + e.toString());
        }
    }


    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
    }

    @Override
    public String getId() {
        return MSF4J_MSG_PROC_ID;
    }
}
