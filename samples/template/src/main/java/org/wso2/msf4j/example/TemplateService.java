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

import org.wso2.msf4j.template.MustacheTemplateEngine;

import java.util.Collections;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Template service resource class. This service has
 * endpoints that renders content from several
 * template engines.
 */
@Path("/")
public class TemplateService {

    @GET
    @Path("/{name}")
    public Response helloMustache(@PathParam("name") String name) {
        Map map = Collections.singletonMap("name", name);
        String html = MustacheTemplateEngine.instance().render("hello.mustache", map);
        return Response.ok()
                .type(MediaType.TEXT_HTML)
                .entity(html)
                .build();
    }

}
