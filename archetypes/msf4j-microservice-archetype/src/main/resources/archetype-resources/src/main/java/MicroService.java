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

package ${package};

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.xml.ws.Response;

/**
 * MicroService resource class.
 *
 * @since ${version}
 */
@Path("/microservice")
public class MicroService {

    @GET
    @Path("/get")
    public String get() {
        // Implementation for HTTP GET request
        return "Hello";
    }

    @POST
    @Path("/post")
    public void post() {
        // Implementation for HTTP POST request
    }

    @PUT
    @Path("/put")
    public void put() {
        // Implementation for HTTP PUT request
    }

    @DELETE
    @Path("/delete")
    public void delete() {
        // Implementation for HTTP DELETE request
    }

}
