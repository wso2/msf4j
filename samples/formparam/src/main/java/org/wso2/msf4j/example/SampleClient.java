package org.wso2.msf4j.example;/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.formparam.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Sample Client to invoke the {{@link FormService}}
 */
public class SampleClient {
    private static final Logger log = LoggerFactory.getLogger(SampleClient.class);

    public static void main(String[] args) throws IOException {
        HttpEntity messageEntity = createMessageForComplexForm();
        // Uncomment the required message body based on the method that want to be used
        //HttpEntity messageEntity = createMessageForMultipleFiles();
        //HttpEntity messageEntity = createMessageForSimpleFormStreaming();

        String serverUrl = "http://localhost:8080/formService/complexForm";
        // Uncomment the required service url based on the method that want to be used
        //String serverUrl = "http://localhost:8080/formService/multipleFiles";
        //String serverUrl = "http://localhost:8080/formService/simpleFormStreaming";

        URL url = URI.create(serverUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", messageEntity.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            messageEntity.writeTo(out);
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        System.out.println(response);

    }

    private static HttpEntity createMessageForSimpleFormStreaming() {
        HttpEntity reqEntity = null;
        try {
            reqEntity = MultipartEntityBuilder.create().addTextBody("name", "WSO2").addTextBody("age", "10")
                                              .addBinaryBody("file",
                                                             new File(Thread.currentThread().getContextClassLoader().getResource("sample.txt").toURI()),
                                                             ContentType.DEFAULT_BINARY, "sample.txt").build();
        } catch (URISyntaxException e) {
            log.error("Error while getting the file from resource." + e.getMessage(), e);
        }
        return reqEntity;
    }

    private static HttpEntity createMessageForComplexForm() {
        HttpEntity reqEntity = null;
        try {
            StringBody companyText = new StringBody("{\"type\": \"Open Source\"}", ContentType.APPLICATION_JSON);
            StringBody personList = new StringBody(
                    "[{\"name\":\"Richard Stallman\",\"age\":63}, {\"name\":\"Linus Torvalds\",\"age\":46}]",
                    ContentType.APPLICATION_JSON);
            reqEntity = MultipartEntityBuilder.create().addTextBody("id", "1")
                                              .addPart("company", companyText)
                                              .addPart("people", personList).addBinaryBody("file", new File(
                            Thread.currentThread().getContextClassLoader().getResource("sample.txt").toURI()), ContentType.DEFAULT_BINARY, "sample.txt")
                                              .build();
        } catch (URISyntaxException e) {
            log.error("Error while getting the file from resource." + e.getMessage(), e);
        }
        return reqEntity;
    }

    private static HttpEntity createMessageForMultipleFiles() {
        HttpEntity reqEntity = null;
        try {
            reqEntity = MultipartEntityBuilder.create().addBinaryBody("files", new File(
                    Thread.currentThread().getContextClassLoader().getResource("sample.txt").toURI()), ContentType.DEFAULT_BINARY, "sample.txt")
                                              .addBinaryBody("files",
                                                             new File(Thread.currentThread().getContextClassLoader().getResource("sample.jpg").toURI()),
                                                             ContentType.DEFAULT_BINARY, "sample.jpg").build();
        } catch (URISyntaxException e) {
            log.error("Error while getting the file from resource." + e.getMessage(), e);
        }
        return reqEntity;
    }
}
