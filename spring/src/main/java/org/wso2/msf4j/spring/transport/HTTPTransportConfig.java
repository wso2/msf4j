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
 * TransportConfig bean for HTTP transport
 *
 * @since 2.0.0
 */
public class HTTPTransportConfig extends TransportConfig {

    @Value("${http.id:msf4j-http}")
    public void setId(String id) {
        super.setId(id);
    }

    @Value("${http.enabled:true}")
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Value("${http.port:8080}")
    public void setPort(int port) {
        super.setPort(port);
    }

    @Value("${http.host:0.0.0.0}")
    public void setHost(String host) {
        super.setHost(host);
    }

    @Value("${http.scheme:http}")
    public void setScheme(String scheme) {
        super.setScheme(scheme);
    }
}
