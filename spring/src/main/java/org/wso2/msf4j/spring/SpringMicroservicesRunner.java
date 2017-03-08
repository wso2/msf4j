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
package org.wso2.msf4j.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.Parameter;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnectorProvider;
import org.wso2.carbon.transport.http.netty.listener.ServerConnectorController;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.internal.DataHolder;
import org.wso2.msf4j.internal.MSF4JMessageProcessor;
import org.wso2.msf4j.spring.transport.HTTPSTransportConfig;
import org.wso2.msf4j.spring.transport.TransportConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * This runner initializes the microservices runtime based on provided Spring configuration, deploys microservices,
 * service interceptors and starts the relevant transports.
 *
 * @since 2.0.0
 */

@Component
public class SpringMicroservicesRunner extends MicroservicesRunner implements ApplicationContextAware,
        InitializingBean {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    public SpringMicroservicesRunner() {
    }

    public SpringMicroservicesRunner(int... ports) {
        super(ports);
    }

    public void init() {

        for (Map.Entry<String, Object> entry : applicationContext.getBeansWithAnnotation(Path.class).entrySet()) {
            log.info("Deploying " + entry.getKey() + " bean as a resource");
            deploy(entry.getValue());
        }

        for (Map.Entry<String, Interceptor> entry : applicationContext.getBeansOfType(Interceptor.class).entrySet()) {
            log.info("Adding " + entry.getKey() + "  Interceptor");
            addInterceptor(entry.getValue());
        }

        for (Map.Entry<String, ExceptionMapper> exceptionMapper :
                applicationContext.getBeansOfType(ExceptionMapper.class).entrySet()) {
            log.info("Adding " + exceptionMapper.getKey() + "  ExceptionMapper");
            addExceptionMapper(exceptionMapper.getValue());
        }

        configureTransport(applicationContext.getBeansOfType(ListenerConfiguration.class).values(),
                applicationContext.getBeansOfType(TransportConfig.class).values());

        start();
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    protected void configureTransport() {
        //Do nothing, just avoid super class's transport configuring options as we use Spring to configure transports.
    }

    protected void configureTransport(Collection<ListenerConfiguration> listeners,
                                      Collection<TransportConfig> transportConfigs) {
        /*
        If -Dtransports.netty.conf=netty-transports.yml present as a command line argument or system property use
        that setting instead of Spring specific options.
         */
        if (System.getProperty(SpringConstants.TRANSPORTS_NETTY_CONF_FILE) != null) {
            super.configureTransport();
            return;

        }

        Set<TransportProperty> transportProperties = new HashSet<>();
        TransportProperty transportProperty = new TransportProperty();
        int bossGroupSize = Runtime.getRuntime().availableProcessors();
        transportProperty.setName(Constants.SERVER_BOOTSTRAP_BOSS_GROUP_SIZE);
        transportProperty.setValue(bossGroupSize);
        TransportProperty workerGroup = new TransportProperty();
        int workerGroupSize = Runtime.getRuntime().availableProcessors() * 2;
        workerGroup.setName(Constants.SERVER_BOOTSTRAP_WORKER_GROUP_SIZE);
        workerGroup.setValue(workerGroupSize);
        transportProperties.add(transportProperty);
        transportProperties.add(workerGroup);

        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        ServerConnectorController serverConnectorController = new ServerConnectorController(transportsConfiguration);
        serverConnectorController.start();
        HTTPServerConnectorProvider httpServerConnectorProvider = new HTTPServerConnectorProvider();
        transportsConfiguration.setTransportProperties(transportProperties);
        Set<ListenerConfiguration> listenerConfigurations = new HashSet<>();

        //Add ListenerConfigurations if available on Spring Configuration
        listeners.forEach(listenerConfiguration -> {
            listenerConfigurations.add(listenerConfiguration);
            DataHolder.getInstance().getMicroservicesRegistries().put(listenerConfiguration.getId(), getMsRegistry());
        });

        //Add NettyTransportConfig if available on Spring Configuration
        for (TransportConfig transportConfig : transportConfigs) {
            if (transportConfig.isEnabled()) {
                ListenerConfiguration listenerConfiguration = createListenerConfiguration(transportConfig);
                listenerConfigurations.add(listenerConfiguration);
                DataHolder.getInstance().getMicroservicesRegistries().put(transportConfig.getId(), getMsRegistry());
            }
        }

        transportsConfiguration.setListenerConfigurations(listenerConfigurations);
        serverConnectors = httpServerConnectorProvider.initializeConnectors(transportsConfiguration);
        serverConnectors.forEach(serverConnector -> serverConnector.setMessageProcessor(new MSF4JMessageProcessor()));
    }

    private ListenerConfiguration createListenerConfiguration(TransportConfig transportConfig) {

        ListenerConfiguration listenerConfig =
                new ListenerConfiguration(transportConfig.getId(), transportConfig.getHost(),
                                          transportConfig.getPort());
        listenerConfig.setScheme(transportConfig.getScheme());
        List<Parameter> parameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : transportConfig.getParameters().entrySet()) {
            parameters.add(createParameter(entry.getKey(), entry.getValue()));
        }

        listenerConfig.setParameters(parameters);
        if (SpringConstants.HTTPS_TRANSPORT.equals(transportConfig.getScheme())) {
            HTTPSTransportConfig httpsTransportConfig = (HTTPSTransportConfig) transportConfig;
            listenerConfig.setKeyStoreFile(httpsTransportConfig.getKeyStoreFile());
            listenerConfig.setKeyStorePass(httpsTransportConfig.getKeyStorePass());
            listenerConfig.setCertPass(httpsTransportConfig.getCertPass());
        }

        return listenerConfig;
    }

    private Parameter createParameter(String key, String value) {
        Parameter parameter = new Parameter();
        parameter.setName(key);
        parameter.setValue(value);
        return parameter;
    }
}
