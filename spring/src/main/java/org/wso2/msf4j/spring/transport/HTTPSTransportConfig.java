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

/**
 * TransportConfig bean for HTTPS transport
 *
 * @since 2.0.0
 */
public class HTTPSTransportConfig extends TransportConfig {

    @Value("${https.id:msf4j-https}")
    public void setId(String id) {
        super.setId(id);
    }

    @Value("${https.enabled:false}")
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Value("${https.port:8443}")
    public void setPort(int port) {
        super.setPort(port);
    }

    @Value("${https.host:0.0.0.0}")
    public void setHost(String host) {
        super.setHost(host);
    }

    @Value("${https.scheme:https}")
    public void setScheme(String scheme) {
        super.setScheme(scheme);
    }

    @Value("${https.keyStoreFile:wso2carbon.jks}")
    public void setKeyStoreFile(String keyStoreFile) {
        super.setKeyStoreFile(keyStoreFile);
    }

    @Value("${https.keyStorePass:wso2carbon}")
    public void setKeyStorePass(String keyStorePass) {
        super.setKeyStorePass(keyStorePass);
    }

    @Value("${https.certPass:wso2carbon}")
    public void setCertPass(String certPass) {
        super.setCertPass(certPass);
    }
}
