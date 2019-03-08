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
package org.wso2.msf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.msf4j.config.TransportsFileConfiguration;
import org.wso2.msf4j.interceptor.RequestInterceptor;
import org.wso2.msf4j.interceptor.ResponseInterceptor;
import org.wso2.msf4j.internal.DataHolder;
import org.wso2.msf4j.internal.HttpConnectorPortBindingListener;
import org.wso2.msf4j.internal.MSF4JHttpConnectorListener;
import org.wso2.msf4j.internal.MSF4JWSConnectorListener;
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;
import org.wso2.msf4j.internal.websocket.EndpointsRegistryImpl;
import org.wso2.msf4j.util.Utils;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * This runner initializes the microservices runtime, deploys the microservices and service interceptors,
 * and starts the relevant transports.
 */
public class MicroservicesRunner {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    /**
     * Default host used when using microservice runner starts with {@link #MicroservicesRunner(int...)}.
     */
    private static final String DEFAULT_HOST = "0.0.0.0";
    /**
     * The environment variable which overrides the {@link #DEFAULT_HOST}.
     */
    private static final String MSF4J_HOST = "msf4j.host";
    /**
     * The environment variable which have netty transport configuration file path.
     */
    private static final String TRANSPORTS_NETTY_CONF = "transports.netty.conf";
    protected List<ServerConnector> serverConnectors = new ArrayList<>();
    private EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
    private MicroservicesRegistryImpl msRegistry = new MicroservicesRegistryImpl();
    private long startTime = System.currentTimeMillis();
    private boolean isStarted;

    /**
     * Creates a MicroservicesRunner instance which will be used for deploying microservices. Allows specifying
     * ports on which the microservices in this MicroservicesRunner are deployed.
     *
     * @param ports The port on which the microservices are exposed
     */
    public MicroservicesRunner(int... ports) {
        configureTransport(ports);
    }

    /**
     * Default constructor which will take care of initializing Netty transports in the file pointed to by the
     * System property <code>transports.netty.conf</code>.
     * <p>
     * If that System property is not specified, it will start a single Netty transport on port 8080.
     * <p>
     * {@link #MicroservicesRunner(int...)}
     */
    public MicroservicesRunner() {
        configureTransport();
    }

    /**
     * Creates a MicroservicesRunner instance which will be used for deploying microservices. Allows specifying
     * transport configuration on which the microservices in this MicroservicesRunner are deployed.
     *
     * @param transportsFileConfiguration The transport configuration on which the microservices are exposed
     */
    public MicroservicesRunner(TransportsFileConfiguration transportsFileConfiguration) {
        configureTransport(transportsFileConfiguration);
    }

    /**
     * Deploy a microservice.
     *
     * @param microservice The microservice which is to be deployed
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner deploy(Object... microservice) {
        checkState();
        msRegistry.addService(microservice);
        return this;
    }

    /**
     * Deploy a microservice with dynamic path.
     *
     * @param microservice The microservice which is to be deployed
     * @param basePath     The context path for the service
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner deploy(String basePath, Object microservice) {
        msRegistry.addService(basePath, microservice);
        return this;
    }

    /**
     * Add WebSocket endpoint to the MicroserviceRunner
     *
     * @param webSocketEndpoint webSocketEndpoint endpoint which is to be added.
     * @return this MicroservicesRunner object.
     */
    public MicroservicesRunner deployWebSocketEndpoint(Object webSocketEndpoint) {
        endpointsRegistry.addEndpoint(webSocketEndpoint);
        return this;
    }

    /**
     * Register a custom {@link SessionManager}.
     *
     * @param sessionManager The SessionManager instance to be registered.
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner setSessionManager(SessionManager sessionManager) {
        msRegistry.setSessionManager(sessionManager);
        return this;
    }

    /**
     * Register request interceptors.
     *
     * @param requestInterceptor interceptor instances
     */
    public MicroservicesRunner addGlobalRequestInterceptor(RequestInterceptor... requestInterceptor) {
        msRegistry.addGlobalRequestInterceptor(requestInterceptor);
        return this;
    }

    /**
     * Register response interceptors.
     *
     * @param responseInterceptor interceptor instances
     */
    public MicroservicesRunner addGlobalResponseInterceptor(ResponseInterceptor... responseInterceptor) {
        msRegistry.addGlobalResponseInterceptor(responseInterceptor);
        return this;
    }

    /**
     * Add an interceptor which will get called before &amp; after the deployed microservices are invoked.
     * Multiple interceptors can be added
     *
     * @param interceptor interceptor The interceptor to be added.
     * @return this MicroservicesRunner object
     * @deprecated
     */
    public MicroservicesRunner addInterceptor(Interceptor... interceptor) {
        msRegistry.addGlobalRequestInterceptor(interceptor);
        msRegistry.addGlobalResponseInterceptor(interceptor);
        return this;
    }

    /**
     * Add javax.ws.rs.ext.ExceptionMapper objects.
     *
     * @param exceptionMapper The ExceptionMapper to be added
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner addExceptionMapper(ExceptionMapper... exceptionMapper) {
        checkState();
        msRegistry.addExceptionMapper(exceptionMapper);
        return this;
    }

    /**
     * Method to configure transports.
     *
     * @param ports The port on which the microservices are exposed
     */
    protected void configureTransport(int... ports) {
        HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory();
        ServerBootstrapConfiguration bootstrapConfiguration = new ServerBootstrapConfiguration(new HashMap<>());
        for (int port : ports) {
            ListenerConfiguration listenerConfiguration = new ListenerConfiguration("netty-" + port, System
                    .getProperty(MSF4J_HOST, DEFAULT_HOST), port);

            DataHolder.getInstance().getMicroservicesRegistries()
                    .put(Util.createServerConnectorID(listenerConfiguration.getHost(),
                            listenerConfiguration.getPort()), msRegistry);
            ServerConnector serverConnector =
                    connectorFactory.createServerConnector(bootstrapConfiguration, listenerConfiguration);
            serverConnectors.add(serverConnector);
        }
    }

    /**
     * Method to configure transports.
     */
    protected void configureTransport() {
        String transportYaml = System.getProperty(TRANSPORTS_NETTY_CONF);
        if (transportYaml == null || transportYaml.isEmpty()) {
            HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory();
            ServerBootstrapConfiguration bootstrapConfiguration = new ServerBootstrapConfiguration(new HashMap<>());
            ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
            ServerConnector serverConnector =
                    connectorFactory.createServerConnector(bootstrapConfiguration, listenerConfiguration);
            DataHolder.getInstance().getMicroservicesRegistries()
                    .put(Util.createServerConnectorID(listenerConfiguration.getHost(),
                            listenerConfiguration.getPort()), msRegistry);
            serverConnectors.add(serverConnector);
        } else {
            try {
                TransportsFileConfiguration transportsFileConfiguration =
                        ConfigProviderFactory.getConfigProvider(Paths.get(transportYaml), null)
                        .getConfigurationObject(TransportsFileConfiguration.class);
                TransportsConfiguration transportsConfiguration = Utils.
                        transformTransportConfiguration(transportsFileConfiguration);

                Map<String, Object> transportProperties = HttpConnectorUtil
                        .getTransportProperties(transportsConfiguration);
                int bossGroup = transportProperties.get(Constants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE) != null ? (Integer)
                        transportProperties.get(Constants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE) : Runtime.getRuntime()
                        .availableProcessors();
                int workerGroup = transportProperties.get(Constants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE) != null ?
                        (Integer) transportProperties.get(Constants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE) :
                        Runtime.getRuntime().availableProcessors() * 2;
                HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory(bossGroup, workerGroup,
                        workerGroup);
                ServerBootstrapConfiguration serverBootstrapConfiguration = HttpConnectorUtil
                        .getServerBootstrapConfiguration(transportsConfiguration.getTransportProperties());

                for (ListenerConfiguration listenerConfiguration :
                        transportsConfiguration.getListenerConfigurations()) {
                    ServerConnector serverConnector =
                            connectorFactory.createServerConnector(serverBootstrapConfiguration, listenerConfiguration);
                    DataHolder.getInstance().getMicroservicesRegistries()
                            .put(Util.createServerConnectorID(listenerConfiguration.getHost(),
                                    listenerConfiguration.getPort()), msRegistry);
                    serverConnectors.add(serverConnector);
                }
            } catch (ConfigurationException e) {
                throw new RuntimeException("Error loading yaml Configuration", e);
            }
        }
    }

    /**
     * Method to configure transports with external transport configuration
     *
     * @param transportsFileConfiguration the external transports configuration
     */
    protected void configureTransport(TransportsFileConfiguration transportsFileConfiguration) {
        if (transportsFileConfiguration != null) {
            TransportsConfiguration transportsConfiguration =
                    Utils.transformTransportConfiguration(transportsFileConfiguration);

            Map<String, Object> transportProperties = HttpConnectorUtil.getTransportProperties(transportsConfiguration);
            int bossGroup = transportProperties.get(Constants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE) != null ? (Integer)
                    transportProperties.get(Constants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE) : Runtime.getRuntime()
                    .availableProcessors();
            int workerGroup = transportProperties.get(Constants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE) != null ? (Integer)
                    transportProperties.get(Constants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE) : Runtime.getRuntime()
                    .availableProcessors() * 2;
            HttpWsConnectorFactory connectorFactory = new DefaultHttpWsConnectorFactory(bossGroup, workerGroup,
                    workerGroup);
            ServerBootstrapConfiguration serverBootstrapConfiguration =
                    HttpConnectorUtil.getServerBootstrapConfiguration(transportsConfiguration.getTransportProperties());
            for (ListenerConfiguration listenerConfiguration : transportsConfiguration.getListenerConfigurations()) {
                ServerConnector serverConnector =
                        connectorFactory.createServerConnector(serverBootstrapConfiguration, listenerConfiguration);
                DataHolder.getInstance().getMicroservicesRegistries()
                        .put(Util.createServerConnectorID(listenerConfiguration.getHost(),
                                listenerConfiguration.getPort()), msRegistry);
                serverConnectors.add(serverConnector);
            }
        } else {
            configureTransport();
        }
    }

    private void checkState() {
        if (isStarted) {
            throw new IllegalStateException("Microservices runner already started");
        }
    }

    /**
     * Start this Microservices runner. This will startup all the HTTP transports.
     */
    public void start() {
        msRegistry.getSessionManager().init();
        handleServiceLifecycleMethods();
        MSF4JHttpConnectorListener msf4JHttpConnectorListener = new MSF4JHttpConnectorListener();
        MSF4JWSConnectorListener msf4JWSConnectorListener = new MSF4JWSConnectorListener();
        serverConnectors.forEach(serverConnector -> {
            ServerConnectorFuture serverConnectorFuture = serverConnector.start();
            serverConnectorFuture.setHttpConnectorListener(msf4JHttpConnectorListener);
            serverConnectorFuture.setWebSocketConnectorListener(msf4JWSConnectorListener);
            serverConnectorFuture.setPortBindingEventListener(new HttpConnectorPortBindingListener());
            isStarted = true;
            log.info("Microservices server started in " + (System.currentTimeMillis() - startTime) + "ms");
        });
    }

    /**
     * Stop this Microservices runner. This will stop all the HTTP Transports.
     */
    public void stop() {
        serverConnectors.forEach(ServerConnector::stop);
        log.info("Microservices server stopped");
    }

    /**
     * Get the MicroservicesRegistry instance of this runner.
     *
     * @return MicroservicesRegistry instance of this runner
     */
    public MicroservicesRegistryImpl getMsRegistry() {
        return msRegistry;
    }

    protected void handleServiceLifecycleMethods() {
        msRegistry.initServices();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                msRegistry.preDestroyServices();
            }
        });
    }
}
