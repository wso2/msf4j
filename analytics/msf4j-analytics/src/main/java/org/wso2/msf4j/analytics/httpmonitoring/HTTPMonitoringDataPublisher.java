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
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

/**
 * A utility class to initialize/destroy HTTP Monitoring Data Publisher for DAS.
 */
public final class HTTPMonitoringDataPublisher {

    private static final Logger logger = LoggerFactory.getLogger(HTTPMonitoringDataPublisher.class);

    private static final String HTTP_MONITORING_STREAM = "org.wso2.msf4j.analytics.httpmonitoring";
    private static final String VERSION = "1.0.0";
    private static final String HTTP_MONITORING_STREAM_ID;

    private static final String SERVER_HOST_ADDRESS;
    private static final String SERVER_HOSTNAME;

    private static final String MICROSERVICE = "Microservice";

    private DataPublisher dataPublisher;
    private Map<String, String> arbitraryAttributes;

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

    private HTTPMonitoringDataPublisher() {
        HTTPMonitoringConfig httpMonitoringConfig = HTTPMonitoringConfigBuilder.build();
        init(httpMonitoringConfig);
        // Destroy data publisher at shutdown
        Thread thread = new Thread(() -> destroy());
        Runtime.getRuntime().addShutdownHook(thread);
    }

    /**
     * Initializes the HTTPMonitoringDataPublisher instance
     */
    private static class HTTPMonitoringDataPublisherHolder {
        private static final HTTPMonitoringDataPublisher INSTANCE = new HTTPMonitoringDataPublisher();
    }

    /**
     * This returns the HTTPMonitoringDataPublisher singleton instance.
     *
     * @return The HTTPMonitoringDataPublisher instance
     */
    public static HTTPMonitoringDataPublisher getInstance() {
        return HTTPMonitoringDataPublisherHolder.INSTANCE;
    }

    private static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            if (iface.isUp()) {
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }

        }
        return InetAddress.getLocalHost();
    }

    private void init(HTTPMonitoringConfig httpMonitoringConfig) {
        if (logger.isInfoEnabled()) {
            logger.info("Initializing HTTP Monitoring Data Publisher");
        }

        String type = httpMonitoringConfig.getType();
        String receiverURL = httpMonitoringConfig.getReceiverURL();
        String authURL = httpMonitoringConfig.getAuthURL();
        String username = httpMonitoringConfig.getUsername();
        String password = httpMonitoringConfig.getPassword();
        String dataAgentConfigPath = httpMonitoringConfig.getDataAgentConfigPath();

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
        arbitraryAttributes = SystemVariableUtil.getArbitraryAttributes();
        try {
            dataPublisher = new DataPublisher(type, receiverURL, authURL, username, password);
        } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException
                | DataEndpointAuthenticationException | TransportException e) {
            throw new IllegalStateException("Error when initializing the Data Publisher", e);
        }
    }

    private void destroy() {
        if (dataPublisher != null) {
            try {
                dataPublisher.shutdownWithAgent();
            } catch (DataEndpointException e) {
                logger.error("Error shutting down the data publisher with agent", e);
            } finally {
                dataPublisher = null;
            }
        }
    }

    public void publishEvent(HTTPMonitoringEvent httpMonitoringEvent) {
        Object[] meta = new Object[4];
        meta[0] = httpMonitoringEvent.getTimestamp();
        meta[1] = SERVER_HOST_ADDRESS;
        meta[2] = SERVER_HOSTNAME;
        meta[3] = MICROSERVICE;
        Object[] correlation = new Object[2];
        correlation[0] = httpMonitoringEvent.getActivityId();
        correlation[1] = httpMonitoringEvent.getParentRequest();
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

        Event event = new Event(HTTP_MONITORING_STREAM_ID, httpMonitoringEvent.getTimestamp(), meta, correlation,
                payload, arbitraryAttributes);
        dataPublisher.publish(event);
    }

}
