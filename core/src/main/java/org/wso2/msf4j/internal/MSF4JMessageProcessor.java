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
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.delegates.MSF4JResponse;
import org.wso2.msf4j.exception.InterceptorException;
import org.wso2.msf4j.internal.router.HandlerException;
import org.wso2.msf4j.internal.router.HttpMethodInfo;
import org.wso2.msf4j.internal.router.HttpMethodInfoBuilder;
import org.wso2.msf4j.internal.router.HttpResourceModel;
import org.wso2.msf4j.internal.router.PatternPathRouter;
import org.wso2.msf4j.internal.router.Util;
import org.wso2.msf4j.util.HttpUtil;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            MicroservicesRegistryImpl currentMicroservicesRegistry =
                    DataHolder.getInstance().getMicroservicesRegistries()
                              .get(carbonMessage.getProperty(MSF4JConstants.CHANNEL_ID));
            Request request = new Request(carbonMessage);
            request.setSessionManager(currentMicroservicesRegistry.getSessionManager());
            setBaseUri(request);
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
                MSF4JResponse.clearBaseUri();
                // Calling the release method to make sure that there won't be any memory leaks from netty
                carbonMessage.release();
            }
        });
        return true;
    }

    private void setBaseUri(Request request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getProperty(Constants.PROTOCOL).toString().toLowerCase(Locale.US)).append("://")
               .append(request.getHeader(Constants.HOST));
        if (builder.charAt(builder.length() - 1) != '/') {
            builder.append("/");
        }
        try {
            MSF4JResponse.setBaseUri(new URI(builder.toString()));
        } catch (URISyntaxException e) {
            log.error("Error while setting the Base URI. " + e.getMessage(), e);
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
            httpMethodInfo.end(request, httpMethodInfo, currentMicroservicesRegistry);
        } else {
            httpMethodInfo.invoke(destination, request, httpMethodInfo, currentMicroservicesRegistry);
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
