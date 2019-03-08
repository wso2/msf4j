/*
 *  Copyright (c) 2019 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.config;


import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.msf4j.internal.MSF4JConstants;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * YAML File representation of a transport configuration.
 */
@Configuration(namespace = MSF4JConstants.WSO2_TRANSPORT_HTTP_CONFIG_NAMESPACE,
        description = "HTTP transport configuration")
public class TransportsFileConfiguration {

    private Set<TransportProperty> transportProperties = new HashSet<>();
    private Set<ListenerFileConfiguration> listenerConfigurations;
    private Set<SenderConfiguration> senderConfigurations;

    public TransportsFileConfiguration() {
        ListenerFileConfiguration listenerConfiguration = new ListenerFileConfiguration();
        listenerConfigurations = new HashSet<>();
        listenerConfigurations.add(listenerConfiguration);

        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfigurations = new HashSet<>();
        senderConfigurations.add(senderConfiguration);
    }


    public Set<ListenerFileConfiguration> getListenerConfigurations() {
        if (listenerConfigurations == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(listenerConfigurations);
    }

    public Set<SenderConfiguration> getSenderConfigurations() {
        if (senderConfigurations == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(senderConfigurations);
    }

    public void setListenerConfigurations(Set<ListenerFileConfiguration> listenerConfigurations) {
        this.listenerConfigurations = Collections.unmodifiableSet(listenerConfigurations);
    }

    public void setSenderConfigurations(Set<SenderConfiguration> senderConfigurations) {
        this.senderConfigurations = Collections.unmodifiableSet(senderConfigurations);
    }

    public Set<TransportProperty> getTransportProperties() {
        return transportProperties;
    }

    public void setTransportProperties(Set<TransportProperty> transportProperties) {
        this.transportProperties = transportProperties;
    }
}
