/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.analytics.internal.DataHolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utils contains utility methods to use analytic purposes
 */
public class AnalyticUtils {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticUtils.class);
    private static final String DEPLOYMENT_YAML_SYS_PROPERTY = "msf4j.conf";
    private static final String DEPLOYMENT_YAML_FILE = "deployment.yaml";

    /**
     * Retrieve Configuration Provider Object to read analytic configurations
     * @return configProvider object
     */
    public static ConfigProvider getConfigurationProvider() {
        ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();
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
                deploymentYamlPath = DEPLOYMENT_YAML_FILE;
                try (InputStream configInputStream = AnalyticUtils.class.getClassLoader()
                        .getResourceAsStream(DEPLOYMENT_YAML_FILE)) {
                    if (configInputStream == null) {
                        throw new RuntimeException("Couldn't find " + deploymentYamlPath);
                    }
                    if (Files.notExists(Paths.get(deploymentYamlPath))) {
                        Files.copy(configInputStream, Paths.get(deploymentYamlPath));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't read configuration from file " + deploymentYamlPath, e);
                }
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
        return configProvider;
    }
}
