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
package org.wso2.msf4j.analytics.httpmonitoring;

/**
 * Configuration for HTTP Monitoring
 */
public class HTTPMonitoringConfig {

    private String type = "thrift";

    private String receiverURL = "tcp://localhost:7611";

    private String authURL;

    private String username = "admin";

    private String password = "admin";

    private String dataAgentConfigPath = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReceiverURL() {
        return receiverURL;
    }

    public void setReceiverURL(String receiverURL) {
        this.receiverURL = receiverURL;
    }

    public String getAuthURL() {
        return authURL;
    }

    public void setAuthURL(String authURL) {
        this.authURL = authURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDataAgentConfigPath() {
        return dataAgentConfigPath;
    }

    public void setDataAgentConfigPath(String dataAgentConfigPath) {
        this.dataAgentConfigPath = dataAgentConfigPath;
    }
}
