/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.msf4j;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.wso2.msf4j.formparam.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

/**
 * Base class for testing interceptors.
 */
abstract class InterceptorTestBase {

    static URI baseURI;
    final String microServiceBaseUrl = "/test/interceptorTest/";
    final String priorityMicroServiceBaseUrl = "/test/priorityInterceptorTest/";
    private final Gson gson = new Gson();

    /**
     * Get http url connection for the path and method specified.
     *
     * @param path      http path
     * @param method    http method
     * @param keepAlive should the connection be kept alive
     * @return http url connection instance
     * @throws IOException on error creating http url connection
     */
    protected HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty("CONNECTION", "CLOSE");
        }
        return urlConn;
    }

    /**
     * @param path      http path
     * @param keepAlive should the connection be kept alive
     * @param tClass    type of the expected object
     * @param <T>       type of the expected object
     * @return object from response
     * @throws Exception on any exception
     */
    protected <T> T getAndGetResponseObject(String path, boolean keepAlive, Class<T> tClass)
            throws Exception {
        HttpURLConnection urlConn = request(path, HttpMethod.GET, keepAlive);
        return getResponseObject(urlConn, tClass);
    }

    /**
     * @param path      http path
     * @param keepAlive should the connection be kept alive
     * @return object from response
     * @throws Exception on any exception
     */
    protected String getAndGetResponseString(String path, boolean keepAlive)
            throws Exception {
        HttpURLConnection urlConn = request(path, HttpMethod.GET, keepAlive);
        return getResponseString(urlConn);
    }

    /**
     * @param path      http path
     * @param rawData   data to post
     * @param keepAlive should the connection be kept alive
     * @param tClass    type of the expected object
     * @param <T>       type of the expected object
     * @return object from response
     * @throws Exception on any exception
     */
    protected <T> T postAndGetResponse(String path, String rawData, boolean keepAlive, Class<T> tClass)
            throws Exception {
        HttpURLConnection urlConn = request(path, HttpMethod.POST, keepAlive);
        ByteBuffer encodedData = Charset.defaultCharset().encode(rawData);
        urlConn.setRequestMethod("POST");
        urlConn.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        urlConn.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = urlConn.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }
        return getResponseObject(urlConn, tClass);
    }

    /**
     * Get java object from http url connection.
     *
     * @param urlConn http url connection
     * @param tClass  type of the expected object
     * @param <T>     type of the expected object
     * @return object from response
     * @throws Exception on any exception
     */
    protected <T> T getResponseObject(HttpURLConnection urlConn, Class<T> tClass) throws Exception {
        return gson.fromJson(getResponseString(urlConn), tClass);
    }

    /**
     * Get java object from http url connection.
     *
     * @param urlConn http url connection
     * @return string from response
     * @throws Exception on any exception
     */
    protected String getResponseString(HttpURLConnection urlConn) throws Exception {
        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        return response;
    }
}
