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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.service.SecondService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class PersistentSessionManagerTest {

    private MicroservicesRunner microservicesRunner;
    private URI baseURI;

    @BeforeClass
    public void setup() {
        baseURI = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, Constants.PORT));
        microservicesRunner = new MicroservicesRunner(Constants.PORT);
        microservicesRunner.setSessionManager(new PersistentSessionManager());
        microservicesRunner.deploy(new SecondService());
        microservicesRunner.start();
    }

    @Test
    public void testSession() throws IOException {
        HttpURLConnection urlConnection = TestUtil.request(baseURI, "/SecondService/session", "GET");
        String content = TestUtil.getContent(urlConnection);
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        assertEquals("1", content);
        urlConnection.disconnect();

        urlConnection = TestUtil.request(baseURI, "/SecondService/session", "GET");
        urlConnection.setRequestProperty("Cookie", headerFields.get("Set-Cookie").get(0));
        content = TestUtil.getContent(urlConnection);
        headerFields = urlConnection.getHeaderFields();
        assertEquals("2", content);
        urlConnection.disconnect();

        urlConnection = TestUtil.request(baseURI, "/SecondService/session", "GET");
        //Since the session is invalidated there shouldn't be a cookie
        assertNull(headerFields.get("Set-Cookie"));
        content = TestUtil.getContent(urlConnection);
        assertEquals("1", content);
        urlConnection.disconnect();
    }

    @Test
    public void testRemoveSessionAttribute() throws IOException {
        HttpURLConnection urlConnection = TestUtil.request(baseURI, "/SecondService/removeSessionAttribute", "GET");
        String content = TestUtil.getContent(urlConnection);
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        assertEquals("1", content);
        urlConnection.disconnect();

        urlConnection = TestUtil.request(baseURI, "/SecondService/removeSessionAttribute", "GET");
        urlConnection.setRequestProperty("Cookie", headerFields.get("Set-Cookie").get(0));
        content = TestUtil.getContent(urlConnection);
        assertEquals("2", content);
        urlConnection.disconnect();

        urlConnection = TestUtil.request(baseURI, "/SecondService/removeSessionAttribute", "GET");
        //Since the session is invalidated there shouldn't be a cookie
        urlConnection.setRequestProperty("Cookie", headerFields.get("Set-Cookie").get(0));
        content = TestUtil.getContent(urlConnection);
        assertEquals("3", content);
        urlConnection.disconnect();
    }

    @AfterClass
    public void clean() {
        microservicesRunner.stop();
    }
}
