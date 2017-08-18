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

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Build {@link HTTPMonitoringConfig} from the YAML file
 */
public final class HTTPMonitoringConfigBuilder {

    private static final Logger logger = LoggerFactory.getLogger(HTTPMonitoringConfigBuilder.class);
    private static final String DEPLOYMENT_YAML_SYS_PROPERTY = "msf4j.conf";
    private static final String DEPLOYMENT_YAML_FILE = "deployment.yaml";

    public static HTTPMonitoringConfig build() {
        ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();
        HTTPMonitoringConfig configurationObject;
        if (configProvider == null) {
            if (DataHolder.getInstance().getBundleContext() != null) {
                throw new RuntimeException(
                        "Failed to populate HTTPMonitoringConfig Configuration. Config Provider is Null.");
            }
            //Standalone mode
            String deploymentYamlPath = System.getProperty(DEPLOYMENT_YAML_SYS_PROPERTY);
            if (deploymentYamlPath == null || deploymentYamlPath.isEmpty()) {
                logger.info("System property '" + DEPLOYMENT_YAML_SYS_PROPERTY +
                            "' is not set. Default deployment.yaml file will be used.");
                deploymentYamlPath = HTTPMonitoringConfig.class.getResource("/" + DEPLOYMENT_YAML_FILE).getPath();
            } else if (!Files.exists(Paths.get(deploymentYamlPath))) {
                throw new RuntimeException("Couldn't find " + deploymentYamlPath);
            }

            try {
                configProvider = ConfigProviderFactory.getConfigProvider(Paths.get(deploymentYamlPath), null);
                DataHolder.getInstance().setConfigProvider(configProvider);
            } catch (ConfigurationException e) {
                throw new RuntimeException("Error loading deployment.yaml Configuration", e);
            }
        }

        try {
            configurationObject =
                    DataHolder.getInstance().getConfigProvider().getConfigurationObject(HTTPMonitoringConfig.class);
        } catch (ConfigurationException e) {
            throw new RuntimeException(
                    "Error while loading " + HTTPMonitoringConfig.class.getName() + " from config provider", e);
        }

        return configurationObject;
    }

}
