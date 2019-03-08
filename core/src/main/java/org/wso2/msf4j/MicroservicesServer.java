/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j;

import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;

import java.util.Map;

/**
 * Interface to handle server details of Microservices server. transport details etc.
 * <p>
 * This interface handles server details of microservices server in OSGi environment.
 *
 * @since 4.5.0
 */
public interface MicroservicesServer {

    /**
     * Provide Listener Configuration details with server connector id used in microservices server.
     *
     * @return ListenerConfigurationMap
     */
    Map<String, ListenerConfiguration> getListenerConfigurations();
}
