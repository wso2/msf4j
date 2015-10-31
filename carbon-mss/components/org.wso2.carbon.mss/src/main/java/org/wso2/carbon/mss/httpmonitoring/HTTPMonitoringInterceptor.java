/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mss.httpmonitoring;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.router.HandlerInfo;
import org.wso2.carbon.mss.internal.router.Interceptor;
import org.wso2.carbon.mss.util.SystemVariableUtil;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Path;

/**
 * Monitor HTTP Requests for methods with {@link HTTPMonitoring} annotations.
 */
public class HTTPMonitoringInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(HTTPMonitoringInterceptor.class);

    private Map<Method, Interceptor> map = new ConcurrentHashMap<>();

    private static final String HTTP_MONITORING_DAS_TYPE = "HTTP_MONITORING_DAS_TYPE";
    private static final String HTTP_MONITORING_DAS_RECEIVER_URL = "HTTP_MONITORING_DAS_RECEIVERURL";
    private static final String HTTP_MONITORING_DAS_AUTH_URL = "HTTP_MONITORING_DAS_AUTHURL";
    private static final String HTTP_MONITORING_DAS_USERNAME = "HTTP_MONITORING_DAS_USERNAME";
    private static final String HTTP_MONITORING_DAS_PASSWORD = "HTTP_MONITORING_DAS_PASSWORD";
    private static final String HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH = "HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH";

    private final DataPublisher dataPublisher;

    private static final String HTTP_MONITORING_STREAM = "org.wso2.carbon.mss.httpmonitoring";

    private static final String VERSION = "1.0.0";

    private static final String HTTP_MONITORING_STREAM_ID;

    private static final String MICROSERVICE = "Microservice";

    private static final String SERVER_HOST_ADDRESS;
    private static final String SERVER_HOSTNAME;

    static {
        HTTP_MONITORING_STREAM_ID = DataBridgeCommonsUtils.generateStreamId(HTTP_MONITORING_STREAM, VERSION);
        try {
            InetAddress localAddress = getLocalAddress();
            SERVER_HOST_ADDRESS = localAddress.getHostAddress();
            SERVER_HOSTNAME = localAddress.getHostName();
        } catch (SocketException | UnknownHostException e) {
            throw new IllegalStateException("Cannot determine server host address", e);
        }
    }

    public static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr;
                }
            }
        }
        return InetAddress.getLocalHost();
    }

    /**
     * A builder for {@link HTTPMonitoringInterceptor} instance.
     */
    public static class Builder {

        private String type = "thrift";

        private String receiverURL = "tcp://localhost:7611";

        private String authURL;

        private String username = "admin";

        private String password = "admin";

        private String dataAgentConfigPath;

        public Builder() {
            this.type = SystemVariableUtil.getValue(HTTP_MONITORING_DAS_TYPE, type);
            this.receiverURL = SystemVariableUtil.getValue(HTTP_MONITORING_DAS_RECEIVER_URL, receiverURL);
            this.authURL = SystemVariableUtil.getValue(HTTP_MONITORING_DAS_AUTH_URL, null);
            this.username = SystemVariableUtil.getValue(HTTP_MONITORING_DAS_USERNAME, username);
            this.password = SystemVariableUtil.getValue(HTTP_MONITORING_DAS_PASSWORD, password);
            this.dataAgentConfigPath = SystemVariableUtil.getValue(HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH, null);
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setReceiverURL(String receiverURL) {
            this.receiverURL = receiverURL;
            return this;
        }

        public Builder setAuthURL(String authURL) {
            this.authURL = authURL;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setDataAgentConfigPath(String dataAgentConfigPath) {
            this.dataAgentConfigPath = dataAgentConfigPath;
            return this;
        }

        public HTTPMonitoringInterceptor build() {
            return new HTTPMonitoringInterceptor(type, receiverURL, authURL, username, password, dataAgentConfigPath);
        }

    }

    private HTTPMonitoringInterceptor(String type, String receiverURL, String authURL, String username, String password,
            String dataAgentConfigPath) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating HTTP Monitoring Interceptor");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (receiverURL == null) {
            throw new IllegalArgumentException("Data Receiver URL cannot be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (dataAgentConfigPath == null) {
            throw new IllegalArgumentException("Data Agent Configuration Path cannot be null");
        }
        AgentHolder.setConfigPath(dataAgentConfigPath);
        try {
            dataPublisher = new DataPublisher(type, receiverURL, authURL, username, password);
        } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException
                | DataEndpointAuthenticationException | TransportException e) {
            throw new IllegalStateException("Error when initializing the Data Publisher", e);
        }
    }

    @Override
    public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
        Method method = handlerInfo.getMethod();
        Interceptor interceptor = map.get(method);
        if (interceptor == null) {
            if (method.isAnnotationPresent(HTTPMonitoring.class)
                    || method.getDeclaringClass().isAnnotationPresent(HTTPMonitoring.class)) {
                interceptor = new HTTPInterceptor();
                map.put(method, interceptor);
            }
        }

        if (interceptor != null) {
            interceptor.preCall(request, responder, handlerInfo);
        }

        return true;
    }

    @Override
    public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {
        Method method = handlerInfo.getMethod();
        Interceptor interceptor = map.get(method);
        if (interceptor != null) {
            interceptor.postCall(request, status, handlerInfo);
        }
    }

    private class HTTPInterceptor implements Interceptor {

        private static final String MONITORING_EVENT = "MONITORING_EVENT";

        private String serviceClass;
        private String serviceName;
        private String serviceMethod;
        private String servicePath;

        private HTTPInterceptor() {
        }

        @Override
        public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
            HTTPMonitoringEvent httpMonitoringEvent = new HTTPMonitoringEvent();
            httpMonitoringEvent.setTimestamp(System.currentTimeMillis());
            httpMonitoringEvent.setStartNanoTime(System.nanoTime());
            if (serviceClass == null) {
                Method method = handlerInfo.getMethod();
                Class<?> serviceClass = method.getDeclaringClass();
                this.serviceClass = serviceClass.getName();
                serviceName = serviceClass.getSimpleName();
                serviceMethod = method.getName();
                if (serviceClass.isAnnotationPresent(Path.class)) {
                    Path path = serviceClass.getAnnotation(Path.class);
                    servicePath = path.value();
                }
            }
            httpMonitoringEvent.setServiceClass(serviceClass);
            httpMonitoringEvent.setServiceName(serviceName);
            httpMonitoringEvent.setServiceMethod(serviceMethod);
            httpMonitoringEvent.setRequestUri(request.getUri());
            httpMonitoringEvent.setServiceContext(servicePath);

            HttpHeaders httpHeaders = request.headers();

            httpMonitoringEvent.setHttpMethod(request.getMethod().name());
            httpMonitoringEvent.setContentType(httpHeaders.get(HttpHeaders.Names.CONTENT_TYPE));
            String contentLength = httpHeaders.get(HttpHeaders.Names.CONTENT_LENGTH);
            if (contentLength != null) {
                httpMonitoringEvent.setRequestSizeBytes(Long.parseLong(contentLength));
            }
            httpMonitoringEvent.setReferrer(httpHeaders.get(HttpHeaders.Names.REFERER));

            handlerInfo.setAttribute(MONITORING_EVENT, httpMonitoringEvent);

            return true;
        }

        @Override
        public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {
            HTTPMonitoringEvent httpMonitoringEvent = (HTTPMonitoringEvent) handlerInfo.getAttribute(MONITORING_EVENT);
            httpMonitoringEvent.setResponseTime(
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - httpMonitoringEvent.getStartNanoTime()));
            httpMonitoringEvent.setResponseHttpStatusCode(status.code());

            Object[] meta = new Object[] { httpMonitoringEvent.getTimestamp(), SERVER_HOST_ADDRESS, SERVER_HOSTNAME,
                    MICROSERVICE };
            Object[] payload = new Object[11];
            payload[0] = httpMonitoringEvent.getServiceClass();
            payload[1] = httpMonitoringEvent.getServiceName();
            payload[2] = httpMonitoringEvent.getServiceMethod();
            payload[3] = httpMonitoringEvent.getRequestUri();
            payload[4] = httpMonitoringEvent.getServiceContext();
            payload[5] = httpMonitoringEvent.getHttpMethod();
            payload[6] = httpMonitoringEvent.getContentType();
            payload[7] = httpMonitoringEvent.getRequestSizeBytes();
            payload[8] = httpMonitoringEvent.getReferrer();
            payload[9] = httpMonitoringEvent.getResponseHttpStatusCode();
            payload[10] = httpMonitoringEvent.getResponseTime();
            Event event = new Event(HTTP_MONITORING_STREAM_ID, httpMonitoringEvent.getTimestamp(), meta, null, payload);
            dataPublisher.publish(event);
        }
    }
}

