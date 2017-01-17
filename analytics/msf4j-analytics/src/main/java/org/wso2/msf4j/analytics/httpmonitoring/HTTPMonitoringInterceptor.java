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
package org.wso2.msf4j.analytics.httpmonitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.Headers;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.analytics.httpmonitoring.config.HTTPMonitoringConfigBuilder;
import org.wso2.msf4j.analytics.httpmonitoring.config.model.HTTPMonitoringConfig;
import org.wso2.msf4j.interceptor.MSF4JRequestInterceptor;
import org.wso2.msf4j.interceptor.MSF4JResponseInterceptor;
import org.wso2.msf4j.internal.MSF4JConstants;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;

/**
 * Monitor HTTP Requests for methods with {@link HTTPMonitored} annotations.
 */
public class HTTPMonitoringInterceptor implements MSF4JRequestInterceptor, MSF4JResponseInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HTTPMonitoringInterceptor.class);

    public static final String REFERER = "Referer";

    private Map<Method, MethodInterceptor> map = new ConcurrentHashMap<>();

    private final boolean enabled;

    private final HTTPMonitoringDataPublisher httpMonitoringDataPublisher;

    public HTTPMonitoringInterceptor() {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating HTTP Monitoring Interceptor");
        }
        HTTPMonitoringConfig httpMonitoringConfig = HTTPMonitoringConfigBuilder.build();
        enabled = httpMonitoringConfig.isEnabled();
        httpMonitoringDataPublisher = enabled ? new HTTPMonitoringDataPublisher(httpMonitoringConfig.getDas()) :
                null;
    }

    /**
     * Returns the final annotation that is application to the given method. For example,
     * the {@link HTTPMonitored} annotation can be mentioned in class level, and also in
     * the target method, but the method only have tracing enabled. Then we should get the
     * setting as tracing is disabled for that specific method.
     */
    private HTTPMonitored extractFinalAnnotation(Method method) {
        HTTPMonitored httpMon = method.getAnnotation(HTTPMonitored.class);
        if (httpMon == null) {
            httpMon = method.getDeclaringClass().getAnnotation(HTTPMonitored.class);
        }
        return httpMon;
    }

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        if (!enabled) {
            return true;
        }
        Method method = (Method) request.getProperty(MSF4JConstants.METHOD_PROPERTY_NAME);
        MethodInterceptor methodInterceptor = map.get(method);
        if (methodInterceptor == null || !methodInterceptor.annotationScanned) {
            HTTPMonitored httpMon = extractFinalAnnotation(method);
            MSF4JRequestInterceptor msf4JRequestInterceptor = null;
            MSF4JResponseInterceptor msf4JResponseInterceptor = null;
            if (httpMon != null) {
                HTTPInterceptor httpInterceptor = new HTTPInterceptor(httpMon.tracing());
                msf4JRequestInterceptor = httpInterceptor;
                msf4JResponseInterceptor = httpInterceptor;
            }

            methodInterceptor = new MethodInterceptor(true, msf4JRequestInterceptor, msf4JResponseInterceptor);
            map.put(method, methodInterceptor);
        }
        return methodInterceptor.interceptRequest(request, response);
    }

    @Override
    public boolean interceptResponse(Request request, Response response) throws Exception {
        if (!enabled) {
            return true; // Proceed with the interception flow
        }
        Method method = (Method) request.getProperty(MSF4JConstants.METHOD_PROPERTY_NAME);
        MethodInterceptor methodInterceptor = map.get(method);
        return !(methodInterceptor != null && !methodInterceptor.interceptResponse(request, response));
    }

    private static class MethodInterceptor implements MSF4JRequestInterceptor, MSF4JResponseInterceptor {

        private final boolean annotationScanned;

        private final MSF4JRequestInterceptor msf4JRequestInterceptor;
        private final MSF4JResponseInterceptor msf4JResponseInterceptor;

        MethodInterceptor(boolean annotationScanned, MSF4JRequestInterceptor requestInterceptor,
                          MSF4JResponseInterceptor responseInterceptor) {
            this.annotationScanned = annotationScanned;
            this.msf4JRequestInterceptor = requestInterceptor;
            this.msf4JResponseInterceptor = responseInterceptor;
        }

        @Override
        public boolean interceptRequest(Request request, Response response) throws Exception {
            return msf4JRequestInterceptor == null || msf4JRequestInterceptor.interceptRequest(request, response);
        }

        @Override
        public boolean interceptResponse(Request request, Response response) throws Exception {
            return msf4JResponseInterceptor == null || msf4JResponseInterceptor.interceptResponse(request, response);
        }
    }

    private class HTTPInterceptor implements MSF4JRequestInterceptor, MSF4JResponseInterceptor {

        private static final String DEFAULT_TRACE_ID = "DEFAULT";

        private static final String DEFAULT_PARENT_REQUEST = "DEFAULT";

        private static final String MONITORING_EVENT = "MONITORING_EVENT";

        private static final String ACTIVITY_ID = "activity-id";

        private static final String PARENT_REQUEST = "parent-request";

        private String serviceClass;
        private String serviceName;
        private String serviceMethod;
        private String servicePath;

        private boolean tracing;

        private HTTPInterceptor(boolean tracing) {
            this.tracing = tracing;
        }

        boolean isTracing() {
            return tracing;
        }

        private String generateTraceId() {
            return UUID.randomUUID().toString();
        }

        private void handleTracing(Request request, HTTPMonitoringEvent httpMonitoringEvent) {
            String traceId;
            String parentRequest;
            if (this.isTracing()) {
                traceId = request.getHeader(ACTIVITY_ID);
                if (traceId == null) {
                    traceId = this.generateTraceId();
                }
                parentRequest = request.getHeader(PARENT_REQUEST);
            } else {
                traceId = DEFAULT_TRACE_ID;
                parentRequest = DEFAULT_PARENT_REQUEST;
            }
            httpMonitoringEvent.setActivityId(traceId);
            httpMonitoringEvent.setParentRequest(parentRequest);
        }

        @Override
        public boolean interceptRequest(Request request, Response response) throws Exception {
            HTTPMonitoringEvent httpMonitoringEvent = new HTTPMonitoringEvent();
            httpMonitoringEvent.setTimestamp(System.currentTimeMillis());
            httpMonitoringEvent.setStartNanoTime(System.nanoTime());
            if (serviceClass == null) {
                Method method = (Method) request.getProperty(MSF4JConstants.METHOD_PROPERTY_NAME);
                Class<?> clazz = method.getDeclaringClass();
                serviceClass = clazz.getName();
                serviceName = clazz.getSimpleName();
                serviceMethod = method.getName();
                if (clazz.isAnnotationPresent(Path.class)) {
                    Path path = clazz.getAnnotation(Path.class);
                    servicePath = path.value();
                }
            }
            httpMonitoringEvent.setServiceClass(serviceClass);
            httpMonitoringEvent.setServiceName(serviceName);
            httpMonitoringEvent.setServiceMethod(serviceMethod);
            httpMonitoringEvent.setRequestUri(request.getUri());
            httpMonitoringEvent.setServiceContext(servicePath);

            Headers httpHeaders = request.getHeaders();

            httpMonitoringEvent.setHttpMethod(request.getHttpMethod());
            httpMonitoringEvent.setContentType(httpHeaders.get(HttpHeaders.CONTENT_TYPE));
            String contentLength = httpHeaders.get(HttpHeaders.CONTENT_LENGTH);
            if (contentLength != null) {
                httpMonitoringEvent.setRequestSizeBytes(Long.parseLong(contentLength));
            }
            httpMonitoringEvent.setReferrer(httpHeaders.get(REFERER));
            this.handleTracing(request, httpMonitoringEvent);
            request.setProperty(MONITORING_EVENT, httpMonitoringEvent);
            return true;
        }

        @Override
        public boolean interceptResponse(Request request, Response response) throws Exception {
            HTTPMonitoringEvent httpMonitoringEvent =
                    (HTTPMonitoringEvent) request.getProperty(MONITORING_EVENT);
            httpMonitoringEvent.setResponseTime(
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - httpMonitoringEvent.getStartNanoTime()));
            httpMonitoringEvent.setResponseHttpStatusCode(response.getStatusCode());
            httpMonitoringDataPublisher.publishEvent(httpMonitoringEvent);
            return true;
        }
    }
}
