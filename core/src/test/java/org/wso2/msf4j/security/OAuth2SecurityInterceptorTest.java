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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.security.oauth2.OAuth2SecurityInterceptor;
import org.wso2.msf4j.service.IntrospectService;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class OAuth2SecurityInterceptorTest {

    private MicroservicesRunner microservicesRunner;

    @BeforeClass
    public void setup() {
        System.setProperty("AUTH_SERVER_URL", "http://localhost:10000/introspect");
        microservicesRunner = new MicroservicesRunner(10000);
        microservicesRunner.deploy(new IntrospectService()).start();
    }

    @Test
    public void testOAuth2SecurityInterceptor() throws Exception {
        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        Request request = new Request(carbonMessage);
        Response response = new Response(carbonMessage1 -> {
        });
        OAuth2SecurityInterceptor oAuth2SecurityInterceptor = new OAuth2SecurityInterceptor();
        boolean isValid = oAuth2SecurityInterceptor.interceptRequest(request, response);
        assertFalse(isValid);
        assertEquals(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusCode());
        assertEquals("OAuth2", response.getHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE));

        carbonMessage.setHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION, "Bearer ded91567bbc7573d5c47e77e700f62ac");
        request = new Request(carbonMessage);
        response = new Response(carbonMessage1 -> {
        });
        oAuth2SecurityInterceptor = new OAuth2SecurityInterceptor();
        assertTrue(oAuth2SecurityInterceptor.interceptRequest(request, response));
    }

    @AfterClass
    public void clean() {
        microservicesRunner.stop();
    }
}
