/*
 *  Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.msf4j.config;

import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration adopter for transport configuration file to bean
 **/
public class ConfigurationAdopter {

    private static ConfigurationAdopter instance = new ConfigurationAdopter();

    public static ConfigurationAdopter getInstance() {
        return instance;
    }

    private ConfigurationAdopter() {
    }

    public TransportsConfiguration getTransportConfiguration(TransportsFileConfiguration transportsFileConfiguration) {
        TransportsConfiguration transportsConfiguration = new TransportsConfiguration();
        transportsConfiguration.setTransportProperties(transportsFileConfiguration.getTransportProperties());
        transportsConfiguration.setSenderConfigurations(transportsFileConfiguration.getSenderConfigurations());

        Set<ListenerConfiguration> listenerConfigurations = transportsFileConfiguration.getListenerConfigurations()
                .stream().map(listenerFileConfiguration -> {
                    ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
                    listenerConfiguration.setId(listenerFileConfiguration.getId());
                    listenerConfiguration.setScheme(listenerFileConfiguration.getScheme());
                    listenerConfiguration.setHost(listenerFileConfiguration.getHost());
                    listenerConfiguration.setPort(listenerFileConfiguration.getPort());
                    if (listenerFileConfiguration.getKeyStoreFile() != null) {
                        listenerConfiguration.setKeyStoreFile(listenerFileConfiguration.getKeyStoreFile());
                    }
                    listenerConfiguration.setKeyStorePass(listenerFileConfiguration.getKeyStorePass());
                    listenerConfiguration.setBindOnStartup(listenerFileConfiguration.isBindOnStartup());
                    listenerConfiguration.setVersion(listenerFileConfiguration.getVersion());
                    listenerConfiguration.setMessageProcessorId(listenerFileConfiguration.getMessageProcessorId());
                    listenerConfiguration.setSocketIdleTimeout(listenerFileConfiguration.getSocketIdleTimeout(0));
                    listenerConfiguration.setHttpTraceLogEnabled(listenerFileConfiguration.isHttpTraceLogEnabled());
                    listenerConfiguration.setHttpAccessLogEnabled(listenerFileConfiguration.isHttpAccessLogEnabled());
                    listenerConfiguration.setRequestSizeValidationConfig(
                            listenerFileConfiguration.getRequestSizeValidationConfig());
                    listenerConfiguration.setChunkConfig(listenerFileConfiguration.getChunkConfig());
                    listenerConfiguration.setKeepAliveConfig(listenerFileConfiguration.getKeepAliveConfig());
                    listenerConfiguration.setServerHeader(listenerFileConfiguration.getServerHeader());
                    return listenerConfiguration;
                }).collect(Collectors.toSet());
        transportsConfiguration.setListenerConfigurations(listenerConfigurations);
        return transportsConfiguration;
    }
}
