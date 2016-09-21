/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.msf4j.swagger;

import io.swagger.util.Json;
import org.wso2.msf4j.MicroservicesRegistry;
import org.wso2.msf4j.SwaggerService;
import org.wso2.msf4j.internal.router.RuntimeAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This service returns the Swagger definition of all the APIs of the microservices deployed in this runtime.
 */
@Path("/swagger")
public class SwaggerDefinitionService implements SwaggerService {
    private static final String GLOBAL = "global";

    private Map<String, MSF4JBeanConfig> swaggerBeans = new HashMap<>();
    private MicroservicesRegistry serviceRegistry;

    public SwaggerDefinitionService() {
    }

    public SwaggerDefinitionService(MicroservicesRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void init(MicroservicesRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSwaggerDefinition(@QueryParam("path") String path) throws Exception {
        MSF4JBeanConfig msf4JBeanConfig;
        if (path == null) {
            msf4JBeanConfig = swaggerBeans.get(GLOBAL);
            if (msf4JBeanConfig == null) {
                MSF4JBeanConfig beanConfig = new MSF4JBeanConfig();
                serviceRegistry.getHttpServices().stream().
                        filter(service -> !service.getClass().equals(SwaggerDefinitionService.class)).
                        forEach(service -> beanConfig.addServiceClass(service.getClass()));
                beanConfig.setScan(true);
                msf4JBeanConfig = beanConfig;
                swaggerBeans.put(GLOBAL, msf4JBeanConfig);
            }
        } else {
            msf4JBeanConfig = swaggerBeans.get(path);
            if (msf4JBeanConfig == null) {
                Optional<Map.Entry<String, Object>> service = serviceRegistry.getServiceWithBasePath(path);
                if (service.isPresent()) {
                    MSF4JBeanConfig beanConfig = new MSF4JBeanConfig();
                    Map<String, Object> valuesMap = new HashMap<>();
                    valuesMap.put("value", path);
                    RuntimeAnnotations.putAnnotation(service.get().getValue().getClass(), Path.class, valuesMap);
                    beanConfig.addServiceClass(service.get().getValue().getClass());
                    beanConfig.setBasePath(service.get().getKey());
                    beanConfig.setScan(true);
                    msf4JBeanConfig = beanConfig;
                    swaggerBeans.put(path, msf4JBeanConfig);
                }
            }
        }
        return (msf4JBeanConfig == null) ?
                Response.status(Response.Status.NOT_FOUND).
                        entity("{\"error\": \"Swagger definition not found for path " + path + "\"}").build() :
                Response.status(Response.Status.OK).
                        entity(Json.mapper().
                                writerWithDefaultPrettyPrinter().writeValueAsString(msf4JBeanConfig.getSwagger())).
                        build();
    }
}
