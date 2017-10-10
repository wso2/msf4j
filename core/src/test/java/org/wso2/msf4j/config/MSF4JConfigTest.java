/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.config;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MSF4JConfigTest {

    @Test
    public void testMSF4JConfig() {
        String threadPoolName = "Test-MSF4J-request-pool";
        MSF4JConfig msf4JConfig = new MSF4JConfig();
        msf4JConfig.setThreadCount(100);
        msf4JConfig.setThreadPoolName(threadPoolName);

        assertEquals(msf4JConfig.getThreadCount(), 100);
        assertEquals(msf4JConfig.getThreadPoolName(), threadPoolName);
    }
}
