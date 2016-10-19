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

package org.wso2.msf4j.example;

import org.wso2.msf4j.analytics.common.tracing.MSF4JClientTracingFilter;

import java.util.Random;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * TraceableService resource class.
 */
@Path("/service")
public class TraceableService {

    final private Random random = new Random();

    @GET
    @Path("/aaaa")
    public String aaaaEndpoint() throws InterruptedException {
        Client client = ClientBuilder.newClient()
                .register(new MSF4JClientTracingFilter("Client-A"));
        String responseB = client.target("http://0.0.0.0:8080").path("service/bbbb").request()
                .get()
                .getEntity().toString();
        String responseC = client.target("http://0.0.0.0:8080").path("service/cccc").request()
                .get()
                .getEntity().toString();
        Thread.sleep(random.nextInt(1000));
        return "aaaa-res:" + responseB + ":" + responseC;
    }

    @GET
    @Path("/bbbb")
    public String bbbbEndpoint() throws InterruptedException {
        Client client = ClientBuilder.newClient()
                .register(new MSF4JClientTracingFilter("Client-B"));
        String responseD = client.target("http://0.0.0.0:8080").path("service/dddd").request()
                .get()
                .getEntity().toString();
        String responseE = client.target("http://0.0.0.0:8080").path("service/eeee").request()
                .get()
                .getEntity().toString();
        Thread.sleep(random.nextInt(1000));
        return "bbbb-res:" + responseD + ":" + responseE;
    }

    @GET
    @Path("/cccc")
    public String ccccEndpoint() throws InterruptedException {
        String responseF = ClientBuilder.newClient()
                .register(new MSF4JClientTracingFilter("Client-C"))
                .target("http://0.0.0.0:8080").path("service/ffff").request()
                .get()
                .getEntity().toString();
        Thread.sleep(random.nextInt(1000));
        return "cccc-res:" + responseF;
    }

    @GET
    @Path("/dddd")
    public String ddddEndpoint() throws InterruptedException {
        Thread.sleep(random.nextInt(1000));
        return "dddd";
    }

    @GET
    @Path("/eeee")
    public String eeeeEndpoint() throws InterruptedException {
        Thread.sleep(random.nextInt(1000));
        return "eeee";
    }

    @GET
    @Path("/ffff")
    public String ffffEndpoint() throws InterruptedException {
        String responseG = ClientBuilder.newClient()
                .register(new MSF4JClientTracingFilter("Client-F"))
                .target("http://0.0.0.0:8080").path("service/gggg").request()
                .get()
                .getEntity().toString();
        Thread.sleep(random.nextInt(1000));
        return "ffff-res:" + responseG;
    }

    @GET
    @Path("/gggg")
    public String ggggEndpoint() throws InterruptedException {
        Thread.sleep(random.nextInt(1000));
        return "gggg";
    }
}
