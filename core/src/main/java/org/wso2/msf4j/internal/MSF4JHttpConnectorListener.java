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

import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.config.MSF4JConfig;
import org.wso2.msf4j.delegates.MSF4JResponse;
import org.wso2.msf4j.interceptor.InterceptorExecutor;
import org.wso2.msf4j.internal.router.HandlerException;
import org.wso2.msf4j.internal.router.HttpMethodInfo;
import org.wso2.msf4j.internal.router.HttpMethodInfoBuilder;
import org.wso2.msf4j.internal.router.HttpResourceModel;
import org.wso2.msf4j.internal.router.PatternPathRouter;
import org.wso2.msf4j.internal.router.Util;
import org.wso2.msf4j.util.HttpUtil;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Process carbon messages for MSF4J.
 *
 * @since 2.5.0
 */
public class MSF4JHttpConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(MSF4JHttpConnectorListener.class);
    private ExecutorService executorService;

    public MSF4JHttpConnectorListener() {
        ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();
        MSF4JConfig msf4JConfig;
        if (configProvider == null) {
            if (DataHolder.getInstance().getBundleContext() != null) {
                throw new RuntimeException("Failed to populate MSF4J Configuration. Config Provider is Null.");
            }
            //Standalone mode
            String deploymentYamlPath = System.getProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY);

            try {
                if (deploymentYamlPath != null && Files.exists(Paths.get(deploymentYamlPath))) {
                    configProvider = ConfigProviderFactory.getConfigProvider(Paths.get(deploymentYamlPath), null);
                    DataHolder.getInstance().setConfigProvider(configProvider);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("MSF4J Configuration file is not provided. either system property '" + MSF4JConstants
                                .DEPLOYMENT_YAML_SYS_PROPERTY + "' is not set or provided file path not exist. Hence " +
                                "using default configuration.");
                    }
                }
            } catch (ConfigurationException e) {
                throw new RuntimeException("Error loading deployment.yaml Configuration", e);
            }
        }

        try {
            if (configProvider != null) {
                msf4JConfig = DataHolder.getInstance().getConfigProvider().getConfigurationObject(MSF4JConfig.class);
            } else {
                msf4JConfig = new MSF4JConfig();
            }
        } catch (ConfigurationException e) {
            throw new RuntimeException("Error while loading " + MSF4JConfig.class.getName() + " from config provider",
                    e);
        }

        executorService = Executors.newFixedThreadPool(msf4JConfig.getThreadCount(), new MSF4JThreadFactory(
                new ThreadGroup(msf4JConfig.getThreadPoolName())));
    }

    public MSF4JHttpConnectorListener(String channelId, MicroservicesRegistryImpl microservicesRegistry) {
        DataHolder.getInstance().getMicroservicesRegistries().put(channelId, microservicesRegistry);
    }

    /**
     * Carbon message handler.
     */
    @Override
    public void onMessage(HttpCarbonMessage httpCarbonMessage) {
        // If we are running on OSGi mode need to get the registry based on the channel_id.
        executorService.execute(() -> {
            //Identify the protocol name before doing the processing
            MicroservicesRegistryImpl currentMicroservicesRegistry =
                    DataHolder.getInstance().getMicroservicesRegistries()
                              .get(httpCarbonMessage.getProperty(MSF4JConstants.CHANNEL_ID));

            Request request = new Request(httpCarbonMessage);
            request.setSessionManager(currentMicroservicesRegistry.getSessionManager());
            setBaseUri(request);
            Response response = new Response(request);
            try {
                dispatchMethod(currentMicroservicesRegistry, request, response);
            } catch (HandlerException e) {
                handleHandlerException(e, request);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                if (targetException instanceof HandlerException) {
                    handleHandlerException((HandlerException) targetException, request);
                } else {
                    handleThrowable(currentMicroservicesRegistry, targetException, request);
                }
            } catch (Throwable t) {
                handleThrowable(currentMicroservicesRegistry, t, request);
            } finally {
                MSF4JResponse.clearBaseUri();
                // Calling the release method to make sure that there won't be any memory leaks from netty
                if (!httpCarbonMessage.isEmpty()) {
                    httpCarbonMessage.getHttpContent().release();
                }
            }
        });
    }

    private void setBaseUri(Request request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getProperty(Constants.PROTOCOL).toString().toLowerCase(Locale.US)).append("://")
                .append(request.getHeader(HttpHeaderNames.HOST.toString()));
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
    private void dispatchMethod(MicroservicesRegistryImpl registry, Request request, Response response)
            throws Exception {
        HttpUtil.setConnectionHeader(request, response);
        PatternPathRouter.RoutableDestination<HttpResourceModel> destination = registry.getMetadata()
                .getDestinationMethod(
                        request.getUri(),
                        request.getHttpMethod(),
                        request.getContentType(),
                        request.getAcceptTypes());
        HttpResourceModel resourceModel = destination.getDestination();
        response.setMediaType(Util.getResponseType(request.getAcceptTypes(), resourceModel.getProducesMediaTypes()));
        HttpMethodInfoBuilder httpMethodInfoBuilder =
                new HttpMethodInfoBuilder().httpResourceModel(resourceModel).httpRequest(request)
                        .httpResponder(response).requestInfo(destination.getGroupNameValues());
        HttpMethodInfo httpMethodInfo = httpMethodInfoBuilder.build();
        if (httpMethodInfo.isStreamingSupported()) {
            Method method = resourceModel.getMethod();
            Class<?> clazz = method.getDeclaringClass();
            // Execute request interceptors
            if (InterceptorExecutor.executeGlobalRequestInterceptors(registry, request, response)
                    // Execute class level request interceptors
                    && InterceptorExecutor.executeClassLevelRequestInterceptors(request, response, clazz)
                    // Execute method level request interceptors
                    && InterceptorExecutor.executeMethodLevelRequestInterceptors(request, response, method)) {

                HttpCarbonMessage carbonMessage = getHttpCarbonMessage(request);
                HttpContent httpContent = carbonMessage.getHttpContent();
                while (true) {
                    if (httpContent == null) {
                        break;
                    }
                    httpMethodInfo.chunk(httpContent.content().nioBuffer());
                    httpContent.release();
                    // Exit the loop at the end of the content
                    if (httpContent instanceof LastHttpContent) {
                        break;
                    }
                    httpContent = carbonMessage.getHttpContent();
                }
                boolean isResponseInterceptorsSuccessful =
                        InterceptorExecutor.executeMethodLevelResponseInterceptors(request, response, method)
                                // Execute class level interceptors (first in - last out order)
                                && InterceptorExecutor.executeClassLevelResponseInterceptors(request, response,
                                clazz)
                                // Execute global interceptors
                                && InterceptorExecutor.executeGlobalResponseInterceptors(registry, request,
                                response);
                httpMethodInfo.end(isResponseInterceptorsSuccessful);
            }
        } else {
            httpMethodInfo.invoke(destination, request, httpMethodInfo, registry);
        }
    }

    private HttpCarbonMessage getHttpCarbonMessage(Request request) throws HandlerException {
        Class<?> clazz = request.getClass();
        try {
            Method retrieveCarbonMsg = clazz.getDeclaredMethod("getHttpCarbonMessage");
            retrieveCarbonMsg.setAccessible(true);
            return (HttpCarbonMessage) retrieveCarbonMsg.invoke(request);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    String.format("Error in executing request: %s %s", request.getHttpMethod(),
                            request.getUri()), e);
        }
    }

    private void handleThrowable(MicroservicesRegistryImpl currentMicroservicesRegistry, Throwable throwable,
                                 Request request) {
        Optional<ExceptionMapper> exceptionMapper = currentMicroservicesRegistry.getExceptionMapper(throwable);
        if (exceptionMapper.isPresent()) {
            org.wso2.msf4j.Response msf4jResponse = new org.wso2.msf4j.Response(request);
            msf4jResponse.setEntity(exceptionMapper.get().toResponse(throwable));
            msf4jResponse.send();
        } else {
            log.warn("Unmapped exception", throwable);
            try {
                HttpCarbonMessage response = HttpUtil.createTextResponse(
                        javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        "Exception occurred :" + throwable.getMessage());
                response.addHttpContent(new DefaultLastHttpContent());
                request.respond(response);
            } catch (ServerConnectorException e) {
                log.error("Error while sending the response.", e);
            }
        }
    }

    private void handleHandlerException(HandlerException e, Request request) {
        try {
            HttpCarbonMessage failureResponse = e.getFailureResponse();
            failureResponse.addHttpContent(new DefaultLastHttpContent());
            request.respond(failureResponse);
        } catch (ServerConnectorException e1) {
            log.error("Error while sending the response.", e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in http connector listener", throwable);
    }
}
