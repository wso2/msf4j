/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mss.example;

import org.wso2.carbon.mss.template.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Template service resource class. This service has
 * endpoints that renders content from several
 * template engines.
 */
@Path("/template")
public class TemplateService {

    @GET
    @Path("/mustache/{name}")
    public String helloMustache(@PathParam("name") String name) {
        Map map = new HashMap<>();
        map.put("name", name);
        return MustacheTemplateEngine.instance().render("hello.mustache", map);
    }

}
