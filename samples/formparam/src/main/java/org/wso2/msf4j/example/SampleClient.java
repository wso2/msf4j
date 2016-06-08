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

import com.google.common.io.Resources;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Sample Client to invoke the {{@link FormService}}
 */
public class SampleClient {

    public static void main(String[] args) throws IOException {
       // HttpPost httppost = new HttpPost("http://localhost:8080/formService/multipleFiles");
       // HttpPost httppost = new HttpPost("http://localhost:8080/formService/complexForm");
        HttpPost httppost = new HttpPost("http://localhost:8080/formService/simpleFormStreaming");
        httppost.setEntity(createMessageForSimpleFormStreaming());
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse execute = httpclient.execute(httppost);
    }

    private static HttpEntity createMessageForSimpleFormStreaming(){
        HttpEntity reqEntity = null;
        try {
            reqEntity = MultipartEntityBuilder.create()
                                              .addTextBody("name", "WSO2")
                                              .addTextBody("age", "10")
                                              .addBinaryBody("file", new File(
                                                                     Resources.getResource("sample.txt").toURI()),
                                                             ContentType.DEFAULT_BINARY, "sample.txt")
                                              .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return reqEntity;
    }

    private static HttpEntity createMessageForComplexForm(){
        HttpEntity reqEntity = null;
        try {
            StringBody companyText = new StringBody("{\"type\": \"Open Source\"}", ContentType.APPLICATION_JSON);
            StringBody personList = new StringBody(
                    "[{\"name\":\"Richard Stallman\",\"age\":63}, {\"name\":\"Linus Torvalds\",\"age\":46}]",
                    ContentType.APPLICATION_JSON);
            reqEntity = MultipartEntityBuilder.create()
                                              .addTextBody("id", "1")
                                              .addPart("company", companyText)
                                              .addPart("people", personList)
                                              .addBinaryBody("file", new File(
                                                                     Resources.getResource("sample.txt").toURI()),
                                                             ContentType.DEFAULT_BINARY, "sample.txt")
                                              .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return reqEntity;
    }

    private static HttpEntity createMessageForMultipleFiles(){
        HttpEntity reqEntity = null;
        try {
            reqEntity = MultipartEntityBuilder.create()
                                              .addBinaryBody("files", new File(
                                                                     Resources.getResource("sample.txt").toURI()),
                                                             ContentType.DEFAULT_BINARY, "sample.txt")
                                              .addBinaryBody("files", new File(
                                                                     Resources.getResource("sample.jpg").toURI()),
                                                             ContentType.DEFAULT_BINARY, "sample.jpg")
                                              .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return reqEntity;
    }
}
