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

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.wso2.msf4j.conf.Constants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.HttpMethod;

import static org.testng.AssertJUnit.assertEquals;

public class TestUtil {

    public static final String HEADER_KEY_CONNECTION = "CONNECTION";
    public static final String HEADER_VAL_CLOSE = "CLOSE";

    public static Socket createRawSocket(URL url) throws IOException {
        return new Socket(url.getHost(), url.getPort());
    }

    public static void testContent(URI baseURI, String path, String content) throws IOException {
        testContent(baseURI, path, content, HttpMethod.GET);
    }

    public static void testContent(URI baseURI, String path, String content, String method) throws IOException {
        HttpURLConnection urlConn = request(baseURI, path, method);
        assertEquals(200, urlConn.getResponseCode());
        assertEquals(content, getContent(urlConn));
        urlConn.disconnect();
    }

    public static HttpURLConnection request(URI baseURI, String path, String method) throws IOException {
        return request(baseURI, path, method, false);
    }

    public static HttpURLConnection request(URI baseURI, String path, String method, boolean keepAlive)
            throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

    public static HttpURLConnection request(String path, String method, boolean keepAlive, int port)
            throws IOException {
        URL url = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, port)).resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

    public static String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }

    public static void writeContent(HttpURLConnection urlConn, String content) throws IOException {
        urlConn.getOutputStream().write(content.getBytes(Charsets.UTF_8));
    }

    public static boolean isStreamEqual(InputStream input1, InputStream input2) throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }
        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }
        int ch2 = input2.read();
        return (ch2 == -1);
    }
}
