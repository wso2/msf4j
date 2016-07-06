/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.msf4j.analytics;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.analytics.httpmonitoring.config.HTTPMonitoringConfigBuilder;
import org.wso2.msf4j.analytics.httpmonitoring.config.model.DasConfig;
import org.wso2.msf4j.analytics.httpmonitoring.config.model.HTTPMonitoringConfig;

/**
 * Test Cases for {@link HTTPMonitoringConfig}
 */
public class HTTPMonitoringConfigTest {

    private static HTTPMonitoringConfig httpMonitoringConfig;

    @BeforeClass
    private void load() {
        httpMonitoringConfig = HTTPMonitoringConfigBuilder.build();
    }

    @Test
    public void testEnabled() {
        Assert.assertFalse(httpMonitoringConfig.isEnabled());
    }

    @Test
    public void testDasConfigLoad() {
        DasConfig config = httpMonitoringConfig.getDas();
        Assert.assertEquals(config.getReceiverURL(), "tcp://localhost:7611");
        Assert.assertNull(config.getAuthURL());
        Assert.assertEquals(config.getType(), "thrift");
        Assert.assertEquals(config.getUsername(), "admin");
        Assert.assertEquals(config.getPassword(), "admin");
        Assert.assertEquals(config.getDataAgentConfigPath(), "data-agent-config.xml");
    }
}
