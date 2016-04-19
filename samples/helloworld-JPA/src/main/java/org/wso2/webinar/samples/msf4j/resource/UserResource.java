/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.webinar.samples.msf4j.resource;

import org.wso2.webinar.samples.msf4j.dao.UserRepository;
import org.wso2.webinar.samples.msf4j.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserResource {

    private UserRepository users;

    public UserResource(UserRepository users) {
        this.users = users;
    }

    @POST
    @Path("/fname/{fname}/lname/{lname}")
    public Response addUser(@PathParam("fname") String fname,
                            @PathParam("lname") String lname) {
        User user = new User(fname, lname);
        users.createUser(user);
        return Response.accepted().build();
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") long id) {
        User user = users.findUser(id);
        if (user != null) {
            return Response.status(Response.Status.ACCEPTED).entity(user).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/")
    public Response getUsers() {
        return Response.status(Response.Status.ACCEPTED).entity(users.findUsers()).build();
    }

}
