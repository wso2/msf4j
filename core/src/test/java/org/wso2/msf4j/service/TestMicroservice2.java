/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 * Test service.
 */
@SuppressWarnings("UnusedParameters")
@Path("/test2/v1")
public class TestMicroservice2 implements Microservice {
    private static final String SAMPLE_STRING = "foo";

    /**
     * Operation which retrieves value set in the session. This method is used to test session encapsulation
     * between micro-service. Setting the session is done in another micro-service.
     * Null should be returned since no attribute
     */
    @GET
    @Path("/get-session")
    public String getObjectFromSession(@Context Request request) {
        return (String) request.getSession().getAttribute(SAMPLE_STRING);
    }
}
