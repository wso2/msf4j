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
import org.wso2.carbon.kernel.transports.TransportManager;
import org.wso2.carbon.messaging.handler.HandlerExecutor;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.NettyListener;
import org.wso2.msf4j.internal.MSF4JMessageProcessor;
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;
import org.wso2.msf4j.internal.router.RuntimeAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * This runner initializes the microservices runtime, deploys the microservices and service interceptors,
 * and starts the relevant transports.
 */
public class MicroservicesRunner {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private TransportManager transportManager = new TransportManager();
    private long startTime = System.currentTimeMillis();
    private boolean isStarted;
    private MicroservicesRegistryImpl msRegistry = new MicroservicesRegistryImpl();

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
     * @param basePath The context path for the service
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner deploy(String basePath, Object microservice) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("value", basePath);
        RuntimeAnnotations.putAnnotation(microservice.getClass(), Path.class, valuesMap);
        msRegistry.addService(basePath, microservice);
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
     * Add an interceptor which will get called before &amp; after the deployed microservices are invoked. Multiple
     * interceptors can be added.
     *
     * @param interceptor The interceptor to be added.
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner addInterceptor(Interceptor... interceptor) {
        checkState();
        msRegistry.addInterceptor(interceptor);
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
        NettyTransportContextHolder nettyTransportContextHolder = NettyTransportContextHolder.getInstance();
        nettyTransportContextHolder.setHandlerExecutor(new HandlerExecutor());

        for (int port : ports) {
            ListenerConfiguration listenerConfiguration =
                    new ListenerConfiguration("netty-" + port, "0.0.0.0", port);
            NettyListener listener = new NettyListener(listenerConfiguration);
            transportManager.registerTransport(listener);
            nettyTransportContextHolder.setMessageProcessor(new MSF4JMessageProcessor("netty-" + port, msRegistry));
        }
    }

    /**
     * Method to configure transports.
     */
    protected void configureTransport() {
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<ListenerConfiguration> listenerConfigurations = trpConfig.getListenerConfigurations();
        NettyTransportContextHolder nettyTransportContextHolder = NettyTransportContextHolder.getInstance();
        nettyTransportContextHolder.setHandlerExecutor(new HandlerExecutor());

        for (ListenerConfiguration listenerConfiguration : listenerConfigurations) {
            NettyListener listener = new NettyListener(listenerConfiguration);
            transportManager.registerTransport(listener);
            nettyTransportContextHolder
                    .setMessageProcessor(new MSF4JMessageProcessor(listenerConfiguration.getId(), msRegistry));
        }
    }


    /**
     * Method to register NettyListeners.
     *
     * @param listener The NettyListener to be added
     */
    protected void registerTransport(NettyListener listener) {
        transportManager.registerTransport(listener);
    }

    private void checkState() {
        if (isStarted) {
            throw new IllegalStateException("Microservices runner already started");
        }
    }

    /**
     * Start this Microservices runner. This will startup all the Netty transports.
     */
    public void start() {
        msRegistry.getSessionManager().init();
        handleServiceLifecycleMethods();
        transportManager.startTransports();
        isStarted = true;
        log.info("Microservices server started in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * Stop this Microservices runner. This will stop all the Netty transports.
     */
    public void stop() {
        transportManager.stopTransports();
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
