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
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.messaging.websocket.BinaryWebSocketMessage;
import org.wso2.carbon.messaging.websocket.CloseWebSocketMessage;
import org.wso2.carbon.messaging.websocket.TextWebSocketMessage;
import org.wso2.carbon.messaging.websocket.WebSocketHandshaker;
import org.wso2.carbon.messaging.websocket.WebSocketMessage;
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
import org.wso2.msf4j.internal.websocket.DispatchedEndpoint;
import org.wso2.msf4j.internal.websocket.EndpointsRegistryImpl;
import org.wso2.msf4j.internal.websocket.SessionManager;
import org.wso2.msf4j.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.websocket.CloseReason;
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

    public MSF4JMessageProcessor() {
    }

    public MSF4JMessageProcessor(String channelId, MicroservicesRegistryImpl microservicesRegistry) {
        DataHolder.getInstance().getMicroservicesRegistries().put(channelId, microservicesRegistry);
    }

    /**
     * Carbon message handler.
     */
    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws InvocationTargetException, IllegalAccessException, IOException, URISyntaxException {
        if (carbonMessage instanceof WebSocketMessage) {
            log.info("WebSocketMessage Received");
            WebSocketMessage webSocketMessage = (WebSocketMessage) carbonMessage;
            EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
            PatternPathRouter.RoutableDestination<DispatchedEndpoint>
                    routableEndpoint = endpointsRegistry.getRoutableEndpoint(webSocketMessage);
            dispatchWebSocketMethod(routableEndpoint, webSocketMessage);
            return true;
        } else {
            // If we are running on OSGi mode need to get the registry based on the channel_id.
            String connection = (String) carbonMessage.getProperty(Constants.CONNECTION);
            if (connection != null && connection.equalsIgnoreCase("Upgrade")) {
                String upgrade = (String) carbonMessage.getProperty(Constants.UPGRADE);
                if (upgrade.equalsIgnoreCase("websocket")) {
                    handleWebSocketHandshake(carbonMessage);
                }
                return true;
            }


            log.debug("HTTPCarbonMessage Received");
            MicroservicesRegistryImpl currentMicroservicesRegistry = DataHolder.getInstance()
                    .getMicroservicesRegistries().get(carbonMessage.getProperty(MSF4JConstants.CHANNEL_ID));
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
                        javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), HttpUtil.EMPTY_BODY));
            } catch (Throwable t) {
                handleThrowable(currentMicroservicesRegistry, t, carbonCallback, request);
            } finally {
                // Calling the release method to make sure that there won't be any memory leaks from netty
                carbonMessage.release();
            }
            return true;
        }
    }


    /**
     * Dispatch the message to correct WebSocket endpoint method
     * @param routableEndpoint dispatched endpoint for a given endpoint
     * @param webSocketMessage incoming webSocketMessage
     * @throws InvocationTargetException problem with invocation of the given method
     * @throws IllegalAccessException Illegal access when invoking the method
     */
    private void dispatchWebSocketMethod(PatternPathRouter.RoutableDestination<DispatchedEndpoint> routableEndpoint,
                                         WebSocketMessage webSocketMessage)
            throws InvocationTargetException, IllegalAccessException, IOException {

        //Invoke correct method with correct parameters
        if (webSocketMessage instanceof TextWebSocketMessage) {
            TextWebSocketMessage textWebSocketMessage =
                    (TextWebSocketMessage) webSocketMessage;
            handleTextWebSocketMessage(textWebSocketMessage, routableEndpoint);

        } else if (webSocketMessage instanceof BinaryWebSocketMessage) {
            BinaryWebSocketMessage binaryWebSocketMessage =
                    (BinaryWebSocketMessage) webSocketMessage;
            handleBinaryWebSocketMessage(binaryWebSocketMessage, routableEndpoint);

        } else if (webSocketMessage instanceof CloseWebSocketMessage) {
            CloseWebSocketMessage closeWebSocketMessage =
                    (CloseWebSocketMessage) webSocketMessage;
            handleCloseWebSocketMessage(closeWebSocketMessage, routableEndpoint);
        }
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


    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public String getId() {
        return MSF4J_MSG_PROC_ID;
    }


    /*
    Handle WebSocket handshake
     */
    private void handleWebSocketHandshake(CarbonMessage carbonMessage)
            throws URISyntaxException, InvocationTargetException, IllegalAccessException {
        WebSocketHandshaker webSocketHandshaker =
                (WebSocketHandshaker) carbonMessage.getProperty(Constants.WEBSOCKET_HANDSHAKER);
        EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
        PatternPathRouter.RoutableDestination<DispatchedEndpoint>
                routableEndpoint = endpointsRegistry.getRoutableEndpoint(carbonMessage);

        if (routableEndpoint == null) {
            /*
            If DispatchedEndpoint cannot be found that means there is no registered
            endpoint for requested URI.
            So cancel the handshake request.
             */
            webSocketHandshaker.cancel();
        } else {
            /*
            If DispatchedEndpoint exists do the needful to handshake and register the client.
             */
            webSocketHandshaker.handshake();
            SessionManager sessionManager = SessionManager.getInstance();
            Session session = sessionManager.createSession(carbonMessage);
            Method method = routableEndpoint.getDestination().getOnOpenMethod();
            List<Object> parameterList = new LinkedList<>();
            Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
            Arrays.stream(method.getParameters()).forEach(
                    parameter -> {
                        if (parameter.getType() == Session.class) {
                            parameterList.add(session);
                        } else if (parameter.getType() == String.class) {
                            PathParam pathParam = parameter.getAnnotation(PathParam.class);
                            if (pathParam != null) {
                                parameterList.add(paramValues.get(pathParam.value()));
                            } else {
                                throw new IllegalArgumentException("String parameters without @PathParam annotation");
                            }
                        } else {
                            parameterList.add(null);
                        }
                    }
            );
            method.invoke(routableEndpoint.getDestination().getWebSocketEndpoint(), parameterList.toArray());
        }
    }

    /*
    Handle Text WebSocket Message
     */
    private void handleTextWebSocketMessage(TextWebSocketMessage textWebSocketMessage,
                                           PatternPathRouter.RoutableDestination<DispatchedEndpoint> routableEndpoint)
            throws InvocationTargetException, IllegalAccessException, IOException {
        DispatchedEndpoint dispatchedEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Method method = dispatchedEndpoint.getOnStringMessageMethod();
        SessionManager sessionManager = SessionManager.getInstance();
        Session session = sessionManager.getSession(textWebSocketMessage);
        List<Object> parameterList = new LinkedList<>();
        boolean isStringSatifsfied = false;
        Arrays.stream(method.getParameters()).forEach(
                parameter -> {
                    if (parameter.getType() == String.class) {
                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                        if (pathParam == null) {
                            parameterList.add(textWebSocketMessage.getText());
                        } else {
                            if (isStringSatifsfied == false) {
                                parameterList.add(paramValues.get(pathParam.value()));
                            } else {
                                throw new IllegalArgumentException("More than one String parameter without " +
                                                                           "@PathParam annotation");
                            }
                        }
                    } else if (parameter.getType() == Session.class) {

                        parameterList.add(session);
                    } else {
                         parameterList.add(null);
                    }
                }
        );

        if (method.getReturnType() == String.class) {
            String returnValue = (String) method.invoke(
                    dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());
            session.getBasicRemote().sendText(returnValue);
        } else {
            method.invoke(dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());
        }
    }

    /*
    Handle Binary WebSocket Message
     */

    private void handleBinaryWebSocketMessage(BinaryWebSocketMessage binaryWebSocketMessage,
                                              PatternPathRouter.RoutableDestination<DispatchedEndpoint>
                                                      routableEndpoint)
            throws InvocationTargetException, IllegalAccessException, IOException {
        DispatchedEndpoint dispatchedEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Method method = dispatchedEndpoint.getOnBinaryMessageMethod();
        SessionManager sessionManager = SessionManager.getInstance();
        Session session = sessionManager.getSession(binaryWebSocketMessage);
        List<Object> parameterList = new LinkedList<>();
        Arrays.stream(method.getParameters()).forEach(
                parameter -> {
                    if (parameter.getType() == ByteBuffer.class) {
                        parameterList.add(binaryWebSocketMessage.readBytes());
                    } else if (parameter.getType() == byte[].class) {
                        parameterList.add(binaryWebSocketMessage.readBytes().array());
                    } else if (parameter.getType() == boolean.class) {
                        parameterList.add(binaryWebSocketMessage.isFinalFragment());
                    } else if (parameter.getType() == Session.class) {
                        parameterList.add(session);
                    } else if (parameter.getType() == String.class) {
                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                        if (pathParam != null) {
                            parameterList.add(paramValues.get(pathParam.value()));
                        } else {
                            throw new IllegalArgumentException("String parameters without @PathParam annotation");
                        }
                    } else {
                        parameterList.add(null);
                    }
                }
        );

        if (method.getReturnType() == ByteBuffer.class) {
            ByteBuffer byteBuffer = (ByteBuffer) method.invoke(
                    dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());
            session.getBasicRemote().sendBinary(byteBuffer);
        } else if (method.getReturnType() == byte[].class) {
            byte[] bytes = (byte[]) method.invoke(
                    dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(bytes));
        } else {
            method.invoke(dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());
        }
    }

    /*
    Handle close WebSocket Message
     */
    private void handleCloseWebSocketMessage(CloseWebSocketMessage closeWebSocketMessage,
                                             PatternPathRouter.RoutableDestination<DispatchedEndpoint> routableEndpoint)
            throws InvocationTargetException, IllegalAccessException, IOException {
        DispatchedEndpoint dispatchedEndpoint = routableEndpoint.getDestination();
        Map<String, String> paramValues = routableEndpoint.getGroupNameValues();
        Method method = dispatchedEndpoint.getOnCloseMethod();
        if (method != null) {
            List<Object> parameterList = new LinkedList<>();
            Arrays.stream(method.getParameters()).forEach(
                    parameter -> {
                        if (parameter.getType() == CloseReason.class) {
                            CloseReason.CloseCode closeCode = new CloseCodeImpl(
                                    closeWebSocketMessage.getStatusCode());
                            CloseReason closeReason = new CloseReason(
                                    closeCode, closeWebSocketMessage.getReasonText());
                            parameterList.add(closeReason);
                        } else if (parameter.getType() == Session.class) {
                            SessionManager sessionManager = SessionManager.getInstance();
                            parameterList.add(sessionManager.getSession(closeWebSocketMessage));
                        } else if (parameter.getType() == String.class) {
                            PathParam pathParam = parameter.getAnnotation(PathParam.class);
                            if (pathParam != null) {
                                parameterList.add(paramValues.get(pathParam.value()));
                            } else {
                                throw new IllegalArgumentException("String parameters without @PathParam annotation");
                            }
                        } else {
                            parameterList.add(null);
                        }
                    }
            );
            method.invoke(dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());
            SessionManager.getInstance().removeSession(closeWebSocketMessage);
        }
    }
}
