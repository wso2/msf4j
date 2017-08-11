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
package org.wso2.msf4j.analytics.httpmonitoring.config.model;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Configuration for HTTP Monitoring
 */
@Configuration(namespace = "org.wso2.msf4j.analytics.configuration", description = "MSF4J Analytics configuration")
public class HTTPMonitoringConfig {

    @Element(description = "Whether HTTP Monitoring is enables or not")
    private boolean enabled = false;

    @Element(description = "Configuration for DAS")
    private DasConfig das = new DasConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DasConfig getDas() {
        return das;
    }

    public void setDas(DasConfig das) {
        this.das = das;
    }
}
