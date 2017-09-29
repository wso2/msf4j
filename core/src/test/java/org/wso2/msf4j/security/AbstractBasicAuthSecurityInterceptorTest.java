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
package org.wso2.msf4j.security;

import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.security.basic.AbstractBasicAuthSecurityInterceptor;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class AbstractBasicAuthSecurityInterceptorTest {

    @Test
    public void testAbstractBasicAuthSecurityInterceptor() throws Exception {
        AbstractBasicAuthSecurityInterceptor abstractBasicAuthSecurityInterceptor =
                new AbstractBasicAuthSecurityInterceptor() {
                    @Override
                    protected boolean authenticate(String username, String password) {
                        return username.equals("wso2") && password.equals("msf4j");
                    }
                };

        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        carbonMessage.setHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION, "Basic d3NvMjptc2Y0ag==");
        Request request = new Request(carbonMessage);
        Response response = new Response(carbonMessage1 -> {
        });
        assertTrue(abstractBasicAuthSecurityInterceptor.interceptRequest(request, response));

        carbonMessage = new DefaultCarbonMessage();
        carbonMessage.setHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION, "Basic d3NvMjptc2Y0ajE=");
        request = new Request(carbonMessage);
        assertFalse(abstractBasicAuthSecurityInterceptor.interceptRequest(request, response));
    }
}
