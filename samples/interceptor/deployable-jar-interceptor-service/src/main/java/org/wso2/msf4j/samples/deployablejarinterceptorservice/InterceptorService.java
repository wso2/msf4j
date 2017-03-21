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
package org.wso2.msf4j.samples.deployablejarinterceptorservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;
import org.wso2.msf4j.interceptor.annotation.ResponseInterceptor;
import org.wso2.msf4j.internal.DataHolder;
import org.wso2.msf4j.internal.MSF4JConstants;
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;
import org.wso2.msf4j.samples.deployablejarinterceptorservice.interceptors.HTTPRequestLogger;
import org.wso2.msf4j.samples.deployablejarinterceptorservice.interceptors.HTTPResponseLogger;
import org.wso2.msf4j.samples.deployablejarinterceptorservice.interceptors.LogTextRequestInterceptor;
import org.wso2.msf4j.samples.deployablejarinterceptorservice.interceptors.LogTextResponseInterceptor;
import org.wso2.msf4j.samples.deployablejarinterceptorservice.interceptors.PropertyAddRequestInterceptor;
import org.wso2.msf4j.samples.deployablejarinterceptorservice.interceptors.PropertyGetResponseInterceptor;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Interceptor example micro-service class.
 */
@Path("/interceptor-service")
public class InterceptorService implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(InterceptorService.class);

    public InterceptorService() {
        Map<String, MicroservicesRegistryImpl> microservicesRegistryMap = DataHolder.getInstance()
                .getMicroservicesRegistries();
        MicroservicesRegistryImpl microServicesRegistry = microservicesRegistryMap.get(MSF4JConstants.CHANNEL_ID);

        if (microServicesRegistry != null) {
            addInterceptorsToRegistry(microServicesRegistry);
        } else {
            microservicesRegistryMap.values().forEach(this::addInterceptorsToRegistry);
        }
    }

    /**
     * Method for getting the micro-service name.
     * curl http://localhost:9090/interceptor-service/service-name
     *
     * @return name of the micro-service.
     */
    @GET
    @Path("/service-name")
    @RequestInterceptor({HTTPRequestLogger.class, LogTextRequestInterceptor.class})
    @ResponseInterceptor({HTTPResponseLogger.class, LogTextResponseInterceptor.class})
    public String getServiceName() {
        log.info("HTTP Method Execution - getServiceName()");
        return "Interceptor example micro-service";
    }

    /**
     * Add interceptors for the micro-services registry.
     *
     * @param microServicesRegistry MicroService Registry instance
     */
    private void addInterceptorsToRegistry(MicroservicesRegistryImpl microServicesRegistry) {
        microServicesRegistry
                .addGlobalRequestInterceptor(new LogTextRequestInterceptor(), new PropertyAddRequestInterceptor());
        microServicesRegistry
                .addGlobalResponseInterceptor(new LogTextResponseInterceptor(), new PropertyGetResponseInterceptor());
    }
}
