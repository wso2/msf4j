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
package org.wso2.msf4j.analytics.metrics;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;

/**
 * Metrics OSGi Component to Initialize/Destroy Metrics.
 */
@Component(
    name = "org.wso2.msf4j.analytics.metrics.MetricsComponent",
    immediate = true)
public class MetricsComponent {

    private static final String METRICS_ENABLED = "METRICS_ENABLED";
    private static final String METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH = "METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH";

    @Activate
    protected void init() {
        if (Boolean.parseBoolean(SystemVariableUtil.getValue(METRICS_ENABLED, Boolean.FALSE.toString()))) {
            String dataAgentConfigPath =
                    SystemVariableUtil.getValue(METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH, Utils.getCarbonConfigHome()
                            + File.separator + "data-bridge" + File.separator + "data-agent-conf.xml");
            System.setProperty(METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH, dataAgentConfigPath);
            Metrics.init(MetricReporter.JMX, MetricReporter.DAS);
        }
    }

    @Deactivate
    protected void destroy() {
        if (Boolean.parseBoolean(SystemVariableUtil.getValue(METRICS_ENABLED, Boolean.FALSE.toString()))) {
            Metrics.destroy();
        }
    }
}
