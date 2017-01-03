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

import org.wso2.msf4j.analytics.common.tracing.TracingConstants;
import org.wso2.msf4j.client.MSF4JClient;
import org.wso2.msf4j.client.codec.MSF4JDecoder;

import java.util.Random;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * TraceableService resource class.
 */
@Path("/service")
public class TraceableService {

    final private Random random = new Random();

    private MSF4JClient<TraceableServiceInterface> client = new MSF4JClient.Builder<TraceableServiceInterface>()
            .apiClass(TraceableServiceInterface.class)
            .enableTracing()
            .decoder(new MSF4JDecoder())
            .tracingType(TracingConstants.TracingType.ZIPKIN)
            .instanceName("TraceableServiceClient")
            .analyticsEndpoint("http://localhost:9411")
            .serviceEndpoint("http://localhost:8080")
            .build();

    @GET
    @Path("/aaaa")
    public String aaaaEndpoint() throws InterruptedException {
        String responseB = client.api().bbbbEndpoint();
        String responseC = client.api().ccccEndpoint();
        Thread.sleep(random.nextInt(1000));
        return "aaaa-res:" + responseB + ":" + responseC;
    }

    @GET
    @Path("/bbbb")
    public String bbbbEndpoint() throws InterruptedException {
        String responseD = client.api().ddddEndpoint();
        String responseE = client.api().eeeeEndpoint();
        Thread.sleep(random.nextInt(1000));
        return "bbbb-res:" + responseD + ":" + responseE;
    }

    @GET
    @Path("/cccc")
    public String ccccEndpoint() throws InterruptedException {
        String responseF = client.api().ffffEndpoint();
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
        String responseG = client.api().ggggEndpoint();
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
