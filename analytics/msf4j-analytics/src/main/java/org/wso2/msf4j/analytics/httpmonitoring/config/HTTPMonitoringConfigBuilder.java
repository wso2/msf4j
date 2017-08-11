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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.analytics.httpmonitoring.config.model.HTTPMonitoringConfig;
import org.wso2.msf4j.analytics.internal.DataHolder;
import org.wso2.msf4j.internal.MSF4JConstants;

import java.nio.file.Paths;

/**
 * Build {@link HTTPMonitoringConfig} from the YAML file
 */
public final class HTTPMonitoringConfigBuilder {

    private static final Logger logger = LoggerFactory.getLogger(HTTPMonitoringConfigBuilder.class);

    public static HTTPMonitoringConfig build() {
        HTTPMonitoringConfig configurationObject;
        ConfigProvider configProvider;
        if (DataHolder.getInstance().getBundleContext() != null) {
            //OSGi mode
            configProvider = DataHolder.getInstance().getConfigProvider();
            if (configProvider != null) {
                try {
                    configurationObject = configProvider.getConfigurationObject(HTTPMonitoringConfig.class);
                } catch (ConfigurationException e) {
                    logger.error("Error loading HTTPMonitoringConfig Configuration", e);
                    configurationObject = new HTTPMonitoringConfig();
                }
            } else {
                // Ideally this shouldn't happen
                logger.error("Failed to populate HTTP Monitoring Configuration. Config Provider is Null.");
                configurationObject = new HTTPMonitoringConfig();
            }
        } else {
            //Non OSGi mode
            String deploymentYamlPath = System.getProperty(MSF4JConstants.DEPLOYMENT_YAML_SYS_PROPERTY);
            if (deploymentYamlPath == null || deploymentYamlPath.isEmpty()) {
                throw new RuntimeException(
                        "Failed to populate HTTP Monitoring Configuration. msf4j.conf is not set.");
            }
            try {
                configProvider = ConfigProviderFactory.getConfigProvider(Paths.get(deploymentYamlPath));
                configurationObject = configProvider.getConfigurationObject(HTTPMonitoringConfig.class);
            } catch (ConfigurationException e) {
                logger.error("Error loading deployment.yaml Configuration", e);
                configurationObject = new HTTPMonitoringConfig();
            }
        }
        return configurationObject;
    }

}
