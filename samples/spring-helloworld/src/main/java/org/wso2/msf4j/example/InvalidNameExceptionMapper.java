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

import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * MSF4J ExceptionMapper for InvalidNameExceptionMapper  Exception.
 */
@Component
public class InvalidNameExceptionMapper implements ExceptionMapper<InvalidNameException> {
    @Override
    public Response toResponse(InvalidNameException exception) {
        return Response.status(500).
                entity(exception.getMessage()).
                type("text/plain").
                build();
    }
}
