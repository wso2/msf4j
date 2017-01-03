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
import org.wso2.msf4j.internal.websocket.DispatchedEndpoint;
import org.wso2.msf4j.internal.websocket.EndpointsRegistryImpl;
import org.wso2.msf4j.internal.websocket.SessionManager;
import org.wso2.msf4j.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.websocket.Session;
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
            throws InvocationTargetException, IllegalAccessException, IOException {
        if (carbonMessage instanceof WebSocketMessage) {
            log.info("WebSocketCarbonMessage Received");
            WebSocketMessage webSocketMessage = (WebSocketMessage) carbonMessage;
            EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
            DispatchedEndpoint dispatchedEndpoint = endpointsRegistry.getDispatchedEndpoint(webSocketMessage);
            dispatchWebSocketMethod(dispatchedEndpoint, webSocketMessage);
            return true;
        } else {
            // If we are running on OSGi mode need to get the registry based on the channel_id.

            String connection = (String) carbonMessage.getProperty(Constants.CONNECTION);
            if (connection.equalsIgnoreCase("Upgrade")) {
                String upgrade = (String) carbonMessage.getProperty(Constants.UPGRADE);
                if (upgrade.equalsIgnoreCase("websocket")) {
                    WebSocketHandshaker webSocketHandshaker =
                            (WebSocketHandshaker) carbonMessage.getProperty(Constants.WEBSOCKET_HANDSHAKER);
                    EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
                    if (endpointsRegistry.getDispatchedEndpoint(carbonMessage) == null) {
                        webSocketHandshaker.cancel();
                        log.info("Handshake is cancelled. Requested endpoint not found.");
                    } else {
                        webSocketHandshaker.handshake();
                        log.info("Handshake is done.");
                    }

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
     * @param dispatchedEndpoint dispatched endpoint for a given endpoint
     * @param webSocketMessage incoming webSocketMessage
     * @throws InvocationTargetException problem with invocation of the given method
     * @throws IllegalAccessException Illegal access when invoking the method
     */
    private void dispatchWebSocketMethod(DispatchedEndpoint dispatchedEndpoint,
                                         WebSocketMessage webSocketMessage)
            throws InvocationTargetException, IllegalAccessException {

        //Invoke correct method with correct parameters
        if (webSocketMessage instanceof TextWebSocketMessage) {
            TextWebSocketMessage textWebSocketMessage =
                    (TextWebSocketMessage) webSocketMessage;
            Method method = dispatchedEndpoint.getOnStringMessageMethod();
            List<Object> parameterList = new LinkedList<>();
            Arrays.stream(method.getParameterTypes()).forEach(
                    parameterType -> {
                        if (parameterType == String.class) {
                            parameterList.add(textWebSocketMessage.getText());
                        } else if (parameterType == Session.class) {
                            SessionManager sessionManager = SessionManager.getInstance();
                            Session session = sessionManager.getSession(webSocketMessage);
                            parameterList.add(session);
                        } else {
                            parameterList.add(null);
                        }
                    }
            );

            method.invoke(dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());

        } else if (webSocketMessage instanceof BinaryWebSocketMessage) {
            BinaryWebSocketMessage binaryWebSocketMessage =
                    (BinaryWebSocketMessage) webSocketMessage;
            Method method = dispatchedEndpoint.getOnBinaryMessageMethod();
            List<Object> parameterList = new LinkedList<>();
            Arrays.stream(method.getParameterTypes()).forEach(
                    parameterType -> {
                        if (parameterType == ByteBuffer.class) {
                            parameterList.add(binaryWebSocketMessage.readBytes());
                        } else if (parameterType == byte[].class) {
                            parameterList.add(binaryWebSocketMessage.readBytes().array());
                        } else if (parameterType == boolean.class) {
                            parameterList.add(binaryWebSocketMessage.isFinalFragment());
                        } else if (parameterType == Session.class) {
                            SessionManager sessionManager = SessionManager.getInstance();
                            Session session = sessionManager.getSession(binaryWebSocketMessage);
                            parameterList.add(session);
                        } else {
                            parameterList.add(null);
                        }
                    }
            );
            method.invoke(dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());

        } else if (webSocketMessage instanceof CloseWebSocketMessage) {
            CloseWebSocketMessage closeWebSocketMessage =
                    (CloseWebSocketMessage) webSocketMessage;

            Method method = dispatchedEndpoint.getOnCloseMethod();
            if (method != null) {
                List<Object> parameterList = new LinkedList<>();
                Arrays.stream(method.getParameterTypes()).forEach(
                        parameterType -> {
                            if (parameterType == String.class) {
                                parameterList.add(closeWebSocketMessage.getReasonText());
                            } else if (parameterType == int.class) {
                                parameterList.add(closeWebSocketMessage.getStatusCode());
                            } else {
                                parameterList.add(null);
                            }
                        }
                );

                method.invoke(dispatchedEndpoint.getWebSocketEndpoint(), parameterList.toArray());
            }
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
}
