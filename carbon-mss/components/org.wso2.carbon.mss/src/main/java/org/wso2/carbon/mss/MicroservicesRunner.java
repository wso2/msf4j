/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.transports.TransportManager;
import org.wso2.carbon.mss.internal.MSSNettyServerInitializer;
import org.wso2.carbon.mss.internal.MicroservicesRegistry;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.internal.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.internal.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.NettyListener;

import java.util.Set;

/**
 * This runner initializes the microservices runtime, deploys the microservices and service interceptors,
 * This runner initializes the microservices runtime, deploys the microservices &amp; service interceptors,
 * and starts the relevant transports.
 */
public class MicroservicesRunner {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private TransportManager transportManager = new TransportManager();
    private long startTime = System.currentTimeMillis();
    private boolean isStarted;
    private MicroservicesRegistry msRegistry = MicroservicesRegistry.newInstance();

    /**
     * Creates a MicroservicesRunner instance which will be used for deploying microservices. Allows specifying
     * ports on which the microservices in this MicroservicesRunner are deployed.
     *
     * @param ports The port on which the microservices are exposed
     */
    public MicroservicesRunner(int... ports) {
        for (int port : ports) {
            NettyTransportContextHolder nettyTransportDataHolder = NettyTransportContextHolder.getInstance();
            ListenerConfiguration listenerConfiguration =
                    new ListenerConfiguration("netty-" + port, "0.0.0.0", port);
            NettyListener listener = new NettyListener(listenerConfiguration);
            transportManager.registerTransport(listener);
            nettyTransportDataHolder.
                    addNettyChannelInitializer(listenerConfiguration.getId(),
                            new MSSNettyServerInitializer(msRegistry));
        }
    }

    /**
     * Default constructor which will take care of initializing Netty transports in the file pointed to by the
     * System property <code>transports.netty.conf</code>.
     *
     * If that System property is not specified, it will start a single Netty transport on port 8080.
     *
     * {@link #MicroservicesRunner(int...)}
     */
    public MicroservicesRunner() {
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<ListenerConfiguration> listenerConfigurations = trpConfig.getListenerConfigurations();
        NettyTransportContextHolder nettyTransportDataHolder = NettyTransportContextHolder.getInstance();
        for (ListenerConfiguration listenerConfiguration : listenerConfigurations) {
            NettyListener listener = new NettyListener(listenerConfiguration);
            transportManager.registerTransport(listener);
            nettyTransportDataHolder.
                    addNettyChannelInitializer(listenerConfiguration.getId(),
                            new MSSNettyServerInitializer(msRegistry));
        }
    }

    /**
     * Deploy a microservice.
     *
     * @param microservice The microservice which is to be deployed
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner deploy(Object microservice) {
        checkState();
        msRegistry.addHttpService(microservice);
        return this;
    }

    /**
     * Add an interceptor which will get called before &amp; after the deployed microservices are invoked. Multiple
     * interceptors can be added.
     *
     * @param interceptor The interceptor to be added.
     * @return this MicroservicesRunner object
     */
    public MicroservicesRunner addInterceptor(Interceptor interceptor) {
        checkState();
        msRegistry.addInterceptor(interceptor);
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
}
