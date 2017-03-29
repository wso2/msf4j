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
package org.wso2.msf4j;

import org.testng.collections.Maps;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Base class of MSF4J Interceptor tests.
 */
class InterceptorTestBase {

    static URI baseURI;

    int doGet(String resource) throws Exception {
        return doGet(resource, Collections.unmodifiableMap(Collections.emptyMap()));
    }

    int doGet(String resource, String key, String value, String... keyValues) throws Exception {
        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put(key, value);

        for (int i = 0; i < keyValues.length; i += 2) {
            headerMap.put(keyValues[i], keyValues[i + 1]);
        }
        return doGet(resource, headerMap);
    }

    private int doGet(String resource, Map<String, String> headers) throws Exception {
        URL url = baseURI.resolve(resource).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        try {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    urlConn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            return urlConn.getResponseCode();
        } finally {
            urlConn.disconnect();
        }
    }
}
