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

import org.wso2.msf4j.pojo.ResponseDataHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Base class for testing filters.
 */
abstract class FilterTestBase {

    static URI baseURI;
    final String microServiceBaseUrl = "/test/filterTest/";
    final String priorityMicroServiceBaseUrl = "/test/priorityFilterTest/";

    /**
     * Do a GET request.
     *
     * @param resource uri path / resource
     * @return ResponseDataHolder containing status code and response content
     * @throws Exception on any exception
     */
    ResponseDataHolder doGet(String resource) throws Exception {
        return doGet(resource, Collections.unmodifiableMap(Collections.emptyMap()));
    }

    /**
     * Do a GET request.
     *
     * @param resource uri path / resource
     * @param headers  headers map
     * @return ResponseDataHolder containing status code and response content
     * @throws Exception on any exception
     */
    ResponseDataHolder doGet(String resource, Map<String, String> headers) throws Exception {
        URL url = baseURI.resolve(resource).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        try {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    urlConn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            return new ResponseDataHolder(urlConn.getResponseCode(), getResponseOutput(urlConn));
        } finally {
            urlConn.disconnect();
        }
    }

    /**
     * Get string output from connection.
     *
     * @param connection HttpURLConnection
     * @return string response output
     * @throws IOException on obtaining input stream
     */
    private String getResponseOutput(HttpURLConnection connection) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        return sb.toString();
    }
}
