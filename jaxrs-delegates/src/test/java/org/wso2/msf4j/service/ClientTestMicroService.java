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

package org.wso2.msf4j.service;

import org.wso2.msf4j.models.SampleEntity;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This microservice is used to test the MSF4J client
 */
@Path("/test")
public class ClientTestMicroService {

    @GET
    @Path("hello")
    public String getHello() {
        return "Hello";
    }

    @POST
    @Path("hello")
    public String postHello() {
        return "PostHello";
    }

    @PUT
    @Path("hello")
    public String putHello() {
        return "PutHello";
    }

    @DELETE
    @Path("hello")
    public String deleteHello() {
        return "DeleteHello";
    }

    @POST
    @Path("sample-entity-json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String postSampleEntityJson(SampleEntity sampleEntity, @HeaderParam("Content-Length") int len) {
        boolean isAllSuccess = true;
        if (len <= 0) {
            isAllSuccess = false;
        }
        if (sampleEntity.getField1() != 1) {
            isAllSuccess = false;
        }
        if (sampleEntity.getField2() != 2) {
            isAllSuccess = false;
        }
        if (!sampleEntity.getField3().equals("val1")) {
            isAllSuccess = false;
        }
        if (!sampleEntity.getField4().equals("val2")) {
            isAllSuccess = false;
        }
        if (!sampleEntity.getField5().equals("val3")) {
            isAllSuccess = false;
        }
        return (isAllSuccess) ? "success" : "failed";
    }

}
