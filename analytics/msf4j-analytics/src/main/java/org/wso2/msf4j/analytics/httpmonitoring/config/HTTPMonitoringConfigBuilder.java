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
package org.wso2.msf4j.analytics.httpmonitoring.config;

import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.analytics.AnalyticUtils;
import org.wso2.msf4j.analytics.httpmonitoring.config.model.HTTPMonitoringConfig;


/**
 * Build {@link HTTPMonitoringConfig} from the YAML file
 */
public final class HTTPMonitoringConfigBuilder {

    public static HTTPMonitoringConfig build() {
        HTTPMonitoringConfig configurationObject;
        ConfigProvider configProvider = AnalyticUtils.getConfigurationProvider();

        try {
            configurationObject =
                    configProvider.getConfigurationObject(HTTPMonitoringConfig.class);
        } catch (ConfigurationException e) {
            throw new RuntimeException(
                    "Error while loading " + HTTPMonitoringConfig.class.getName() + " from config provider", e);
        }

        return configurationObject;
    }

}
