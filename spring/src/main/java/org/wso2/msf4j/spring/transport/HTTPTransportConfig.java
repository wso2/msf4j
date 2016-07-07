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

package org.wso2.msf4j.spring.transport;

import org.springframework.beans.factory.annotation.Value;
import org.wso2.msf4j.spring.SpringConstants;

/**
 * TransportConfig bean for HTTP transport
 *
 * @since 2.0.0
 */
public class HTTPTransportConfig extends TransportConfig {

    public HTTPTransportConfig() {
        super.setScheme(SpringConstants.HTTP_TRANSPORT);
    }

    public HTTPTransportConfig(int port) {
        this();
        setPort(port);
    }

    @Value("${http.id:}")
    public void setId(String id) {
        super.setId(id);
    }

    @Value("${http.enabled:}")
    public void setEnabledProperty(String enabled) {
        super.setEnabledProperty(enabled);
    }

    @Value("${http.port:}")
    public void setPortProperty(String port) {
        super.setPortProperty(port);
    }

    @Value("${http.host:}")
    public void setHostProperty(String host) {
        super.setHostProperty(host);
    }

    public HTTPTransportConfig port(int port) {
        setPort(port);
        return this;
    }

    public HTTPTransportConfig host(String host) {
        setHost(host);
        return this;
    }

    public HTTPTransportConfig enabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    public HTTPTransportConfig enabled() {
        setEnabled(true);
        return this;
    }
}
