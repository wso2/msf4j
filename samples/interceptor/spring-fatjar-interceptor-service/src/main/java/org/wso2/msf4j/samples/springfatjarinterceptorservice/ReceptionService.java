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
package org.wso2.msf4j.samples.springfatjarinterceptorservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;
import org.wso2.msf4j.interceptor.annotation.ResponseInterceptor;
import org.wso2.msf4j.samples.interceptor.common.HTTPRequestLogger;
import org.wso2.msf4j.samples.interceptor.common.HTTPResponseLogger;
import org.wso2.msf4j.samples.interceptor.common.LogTextRequestInterceptor;
import org.wso2.msf4j.samples.interceptor.common.LogTextResponseInterceptor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Interceptor example MSF4J spring micro-service class.
 */
@Component
@Path("/reception-service")
public class ReceptionService {

    private static final Logger log = LoggerFactory.getLogger(ReceptionService.class);

    @Autowired
    private CustomerService customerService;

    /**
     * Method for saying hello to a customer.
     * curl http://localhost:8080/reception-service/say-hello/John
     *
     * @return hello message for the customer
     */
    @GET
    @Path("/say-hello/{name}")
    @RequestInterceptor({HTTPRequestLogger.class, LogTextRequestInterceptor.class})
    @ResponseInterceptor({HTTPResponseLogger.class, LogTextResponseInterceptor.class})
    public String sayHello(@PathParam("name") String name) throws NoSuchMethodException {
        log.info("HTTP Method Execution - " + customerService.getClass().getMethod("sayHello", String.class));
        return customerService.sayHello(name);
    }
}
