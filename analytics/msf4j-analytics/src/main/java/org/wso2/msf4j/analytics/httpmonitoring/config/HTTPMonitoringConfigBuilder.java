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

import org.wso2.carbon.metrics.core.utils.Utils;
import org.wso2.msf4j.analytics.httpmonitoring.config.model.HTTPMonitoringConfig;
import org.yaml.snakeyaml.Yaml;

import java.util.Optional;

/**
 * Build {@link HTTPMonitoringConfig} from the YAML file
 */
public final class HTTPMonitoringConfigBuilder {

    public static HTTPMonitoringConfig build() {
        Optional<String> metricsConfigFileContent = Utils.readFile("http-monitoring.conf", "http-monitoring.yml");
        if (metricsConfigFileContent.isPresent()) {
            try {
                Yaml yaml = new Yaml();
                return yaml.loadAs(metricsConfigFileContent.get(), HTTPMonitoringConfig.class);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to populate HTTP Monitoring Configuration", e);
            }
        } else {
            return new HTTPMonitoringConfig();
        }
    }

}
