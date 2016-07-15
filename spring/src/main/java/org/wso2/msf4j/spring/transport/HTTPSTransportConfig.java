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
 * TransportConfig bean for HTTPS transport
 *
 * @since 2.0.0
 */
public class HTTPSTransportConfig extends TransportConfig {

    public HTTPSTransportConfig() {
        super.setScheme(SpringConstants.HTTPS_TRANSPORT);
    }

    @Value("${https.id:}")
    public void setId(String id) {
        super.setId(id);
    }

    @Value("${https.enabled:}")
    public void setEnabledProperty(String enabled) {
        super.setEnabledProperty(enabled);
    }

    @Value("${https.port:}")
    public void setPortProperty(String port) {
        super.setPortProperty(port);
    }

    @Value("${https.host:}")
    public void setHostProperty(String host) {
        super.setHostProperty(host);
    }

    @Value("${https.keyStoreFile:}")
    public void setKeyStoreFileProperty(String keyStoreFile) {
        super.setKeyStoreFileProperty(keyStoreFile);
    }

    @Value("${https.keyStorePass:}")
    public void setKeyStorePassProperty(String keyStorePass) {
        super.setKeyStorePassProperty(keyStorePass);
    }

    @Value("${https.certPass:}")
    public void setCertPassProperty(String certPass) {
        super.setCertPassProperty(certPass);
    }

    public HTTPSTransportConfig port(int port) {
        setPort(port);
        return this;
    }

    public HTTPSTransportConfig host(String host) {
        setHost(host);
        return this;
    }

    public HTTPSTransportConfig keyStore(String keyStore) {
        setKeyStoreFile(keyStore);
        return this;
    }

    public HTTPSTransportConfig keyStorePass(String keyStorePass) {
        setKeyStorePass(keyStorePass);
        return this;
    }

    public HTTPSTransportConfig certPass(String certPass) {
        setCertPass(certPass);
        return this;
    }

    public HTTPSTransportConfig enabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    public HTTPSTransportConfig enabled() {
        setEnabled(true);
        return this;
    }
}
