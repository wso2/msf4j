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
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.Parameter;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.NettyListener;
import org.wso2.msf4j.internal.MSF4JMessageProcessor;
import org.wso2.msf4j.internal.MicroservicesRegistry;
import org.wso2.msf4j.internal.swagger.MSF4JBeanConfig;
import org.wso2.msf4j.internal.swagger.SwaggerDefinitionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This runner initializes the microservices runtime, deploys the microservices and service interceptors,
 * and starts the relevant transports.
 */
public class MicroservicesRunner {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private TransportManager transportManager = new TransportManager();
    private long startTime = System.currentTimeMillis();
    private boolean isStarted;
    private MicroservicesRegistry msRegistry = MicroservicesRegistry.newInstance();
    private MSF4JBeanConfig swaggerBeanConfig = new MSF4JBeanConfig();

    /**
     * Creates a MicroservicesRunner instance which will be used for deploying microservices. Allows specifying
     * ports on which the microservices in this MicroservicesRunner are deployed.
     *
     * @param ports The port on which the microservices are exposed
     */
    public MicroservicesRunner(int... ports) {
        for (int port : ports) {
            NettyTransportContextHolder nettyTransportContextHolder = NettyTransportContextHolder.getInstance();
            ListenerConfiguration listenerConfiguration =
                    new ListenerConfiguration("netty-" + port, "0.0.0.0", port);
            //TODO: remove this default param workaround when transport supports default configurations
            listenerConfiguration.setEnableDisruptor(String.valueOf(false));
            listenerConfiguration.setParameters(getDefaultTransportParams());
            NettyListener listener = new NettyListener(listenerConfiguration);
            nettyTransportContextHolder.setHandlerExecutor(new HandlerExecutor());
            nettyTransportContextHolder.addMessageProcessor(new MSF4JMessageProcessor(msRegistry));
            transportManager.registerTransport(listener);
        }
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
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<ListenerConfiguration> listenerConfigurations = trpConfig.getListenerConfigurations();
        NettyTransportContextHolder nettyTransportContextHolder = NettyTransportContextHolder.getInstance();
        for (ListenerConfiguration listenerConfiguration : listenerConfigurations) {
            //TODO: remove this default param workaround when transport supports default configurations
            if (listenerConfiguration.getEnableDisruptor() == null) {
                listenerConfiguration.setEnableDisruptor(String.valueOf(false));
                listenerConfiguration.setParameters(getDefaultTransportParams());
            } else if (!Boolean.valueOf(listenerConfiguration.getEnableDisruptor()) &&
                    (listenerConfiguration.getParameters() == null || listenerConfiguration.getParameters()
                            .stream()
                            .filter(parameter -> Constants.EXECUTOR_WORKER_POOL_SIZE.equals(parameter.getName()))
                            .findFirst()
                            .isPresent())) {
                listenerConfiguration.setParameters(getDefaultTransportParams());
            }
            NettyListener listener = new NettyListener(listenerConfiguration);
            nettyTransportContextHolder.setHandlerExecutor(new HandlerExecutor());
            nettyTransportContextHolder.addMessageProcessor(new MSF4JMessageProcessor(msRegistry));
            transportManager.registerTransport(listener);
        }
    }

    /**
     * Deploy a microservice.
     *
     * @param microservice The microservice which is to be deployed
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner deploy(Object... microservice) {
        checkState();
        Arrays.stream(microservice).parallel().forEach(service -> {
            swaggerBeanConfig.addServiceClass(service.getClass());
            msRegistry.addHttpService(service);
        });
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
        Arrays.stream(interceptor).parallel().forEach(i -> {
            msRegistry.addInterceptor(i);
        });
        return this;
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
        // Deploy the Swagger definition service which will return the Swagger definition.
        deploy(new SwaggerDefinitionService(swaggerBeanConfig));

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
    public MicroservicesRegistry getMsRegistry() {
        return msRegistry;
    }

    private void handleServiceLifecycleMethods() {
        msRegistry.initServices();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                msRegistry.preDestroyServices();
            }
        });
    }

    /**
     * Temporary method to add default transport parameters.
     *
     * @return transports parameter list
     */
    //TODO: remove this default param workaround when transport supports default configurations
    private List<Parameter> getDefaultTransportParams() {
        Parameter param1 = new Parameter();
        param1.setName(Constants.EXECUTOR_WORKER_POOL_SIZE);
        param1.setValue("1024");
        return Collections.singletonList(param1);
    }
}
