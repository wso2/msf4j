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
package org.wso2.msf4j.service;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.filter.TestRequestFilter;
import org.wso2.msf4j.filter.TestResponseFilter;
import org.wso2.msf4j.filter.annotation.FilterRequest;
import org.wso2.msf4j.filter.annotation.FilterResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Micro Service to test filters
 */
@SuppressWarnings("UnusedParameters")
@Path("/test/filterTest")
@FilterRequest(TestRequestFilter.class)
@FilterResponse(TestResponseFilter.class)
public class FilterTestMicroService implements Microservice {

    @GET
    @Path("/requestFilterTest")
    @FilterRequest(TestRequestFilter.class)
    public String requestFilterTest() {
        return "Request filter test";
    }

    @GET
    @Path("/responseFilterTest")
    @FilterResponse(TestResponseFilter.class)
    public String responseFilterTest() {
        return "Response filter test";
    }
}
