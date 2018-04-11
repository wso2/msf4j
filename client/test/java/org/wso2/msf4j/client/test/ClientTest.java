/*
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
package org.wso2.msf4j.client.test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.client.codec.RestErrorResponseMapper;
import org.wso2.msf4j.client.exception.RestServiceException;
import org.wso2.msf4j.client.test.client.exception.InvoiceNotFoundResponseMapper;
import org.wso2.msf4j.client.test.client.exception.InvoiceNotFoundRestServiceException;
import org.wso2.msf4j.client.test.exception.CustomerNotFoundMapper;
import org.wso2.msf4j.client.test.exception.InvoiceNotFoundMapper;
import org.wso2.msf4j.client.test.service.CustomerService;
import org.wso2.msf4j.client.test.service.InvoiceService;
import org.wso2.msf4j.client.test.service.ReportService;
import org.wso2.msf4j.formparam.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.HttpMethod;

public class ClientTest {
    private static final String HEADER_VAL_CLOSE = "CLOSE";
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8090;
    private MicroservicesRunner microservicesRunner1;
    private MicroservicesRunner microservicesRunner2;
    private MicroservicesRunner microservicesRunner3;
    private URI baseURI;

    public static void main(String[] args) {
        MicroservicesRunner microservicesRunner2 = new MicroservicesRunner(8089);
        microservicesRunner2.addExceptionMapper(new InvoiceNotFoundMapper())
                            .deploy(new InvoiceService()).start();
        MicroservicesRunner microservicesRunner1 = new MicroservicesRunner(8088);
        microservicesRunner1.addExceptionMapper(new CustomerNotFoundMapper())
                            .deploy(new CustomerService()).start();


        MicroservicesRunner microservicesRunner3 = new MicroservicesRunner(PORT);
        microservicesRunner3.addExceptionMapper(new CustomerNotFoundMapper(), new InvoiceNotFoundMapper())
                            .deploy(new ReportService()).start();
    }

    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, PORT));
        microservicesRunner1 = new MicroservicesRunner(8088);
        microservicesRunner1.addExceptionMapper(new CustomerNotFoundMapper())
                            .deploy(new CustomerService()).start();

        microservicesRunner2 = new MicroservicesRunner(8089);
        microservicesRunner2.addExceptionMapper(new InvoiceNotFoundMapper())
                            .deploy(new InvoiceService()).start();

        microservicesRunner3 = new MicroservicesRunner(PORT);
        microservicesRunner3.addExceptionMapper(new CustomerNotFoundMapper(), new InvoiceNotFoundMapper())
                            .deploy(new ReportService()).start();
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner1.stop();
        microservicesRunner2.stop();
        microservicesRunner3.stop();
    }

    @Test
    public void testClient() throws Exception {
        HttpURLConnection urlConn = request("/report/invoice/I001", HttpMethod.GET, false);
        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        Assert.assertEquals(response,
                            "{\"id\":\"I001\",\"customer\":{\"id\":\"C001\",\"firstName\":\"WSO2\",\"lastName\":" +
                            "\"Inc\",\"address\":\"Colombo\"},\"amount\":250.15}");

        urlConn = request("/report/invoice/I002", HttpMethod.GET, false);
        int responseCode = urlConn.getResponseCode();
        Assert.assertEquals(responseCode, 404);
        inputStream = urlConn.getErrorStream();
        response = StreamUtil.asString(inputStream);
        Gson gson = new Gson();
        InvoiceNotFoundResponseMapper invoiceNotFoundResponseMapper =
                gson.fromJson(response, InvoiceNotFoundResponseMapper.class);
        IOUtils.closeQuietly(inputStream);
        Assert.assertEquals(invoiceNotFoundResponseMapper.getExceptionKey(), "30002");
        Assert.assertEquals(invoiceNotFoundResponseMapper.getExceptionClass(),
                            InvoiceNotFoundRestServiceException.class);
        urlConn.disconnect();
    }

    protected HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HttpHeaderNames.CONNECTION.toString(), HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

}
