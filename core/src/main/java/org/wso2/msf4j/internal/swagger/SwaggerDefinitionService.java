/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.internal.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.util.Json;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * This service returns the Swagger definition of all the APIs of the microservices deployed in this runtime.
 */
@Path("/swagger")
public class SwaggerDefinitionService {

    private MSF4JBeanConfig swaggerBeanConfig;

    public SwaggerDefinitionService(MSF4JBeanConfig swaggerBeanConfig) {
        this.swaggerBeanConfig = swaggerBeanConfig;
    }

    @GET
    public String getSwaggerDefinition() throws JsonProcessingException {
        return Json.mapper().
                writerWithDefaultPrettyPrinter().writeValueAsString(swaggerBeanConfig.getSwagger());
    }
}
