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
package org.wso2.msf4j;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Headers;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class ResponseTest {

    private Response response;

    @BeforeClass
    public void setup() {
        response = new Response(CarbonMessage::release);
    }

    @Test
    public void testIsEomAddedsET() {
        assertFalse(response.isEomAdded());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(response.isEmpty());
    }

    @Test
    public void testHeaders() {
        String key = "Client";
        String value = "WSO2";

        Headers headers = response.getHeaders();
        assertTrue(headers.size() == 0);

        response.setHeader(key, value);
        assertTrue(response.getHeaders().size() == 1);
        assertEquals(response.getHeader(key), value);

        String header1Key = "Browser";
        String header1Val = "Chrome";
        String header2Key = "Region";
        String header2Val = "US";
        String header3Key = "Secure";
        String header3Val = "true";
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(header1Key, header1Val);
        customHeaders.put(header2Key, header2Val);
        customHeaders.put(header3Key, header3Val);
        response.setHeaders(customHeaders);
        assertTrue(response.getHeaders().size() == 4);
        assertEquals(response.getHeader(header1Key), header1Val);
        assertEquals(response.getHeader(header2Key), header2Val);
        assertEquals(response.getHeader(header3Key), header3Val);

        response.removeHeader(header1Key);
        assertNull(response.getHeader(header1Key));
        assertTrue(response.getHeaders().size() == 3);
    }

    @Test
    public void testProperties() {
        String key = "key";
        Object value = new Request(new DefaultCarbonMessage());

        Map<String, Object> properties = response.getProperties();
        assertTrue(properties.size() == 0);

        response.setProperty(key, value);
        assertTrue(response.getProperties().size() == 1);
        assertEquals(response.getProperty(key), value);

        response.removeProperty(key);
        assertTrue(response.getProperties().size() == 0);
        assertNull(response.getProperty(key));
    }

    @Test
    public void testCarbonMessage() {
        CarbonMessage carbonMessage = response.getCarbonMessage();
        assertTrue(carbonMessage.isEmpty());
        assertTrue(carbonMessage.isBufferContent());
        assertFalse(carbonMessage.isEndOfMsgAdded());
        assertFalse(carbonMessage.isAlreadyRead());
        assertFalse(carbonMessage.isFaulty());
    }
}
