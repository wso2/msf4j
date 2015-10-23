/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.mss.examples.petstore.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.examples.petstore.util.model.User;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * UserAuthentication micro service
 */
@Path("/user")
public class UserAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationService.class);
    private static final String JWT_HEADER = "X-JWT-Assertion";
    private static AtomicInteger count = new AtomicInteger(0);
    private Map<String, User> users = new HashMap<>();

    @POST
    @Consumes("application/json")
    @Path("/login")
    public Response authenticate(User user) {
        String name = user.getName();
        log.info("Authenticating user " + name + " ..");
        String jwt;
        User userFromUserStore = users.get(name);

        if (userFromUserStore != null
            && userFromUserStore.getName().equals(user.getName()) 
            && userFromUserStore.getPassword().equals(user.getPassword())) {

            try {
                JWTGenerator jwtGenerator = new JWTGenerator();
                jwt = jwtGenerator.generateJWT(user);

                return Response.ok("User" + name + " authenticated successfully")
                        .header(JWT_HEADER, jwt).build();
            } catch (Exception e) {
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid login attempt.").build();
    }

    @POST
    @Consumes("application/json")
    @Path("/add")
    public Response addUser(User user) {
        String name = user.getName();
        log.info("Adding new user " + name + " ..");
        users.put(user.getName(), user);

        return Response.status(Response.Status.OK)
                .entity("User " + name + " successfully added").build();

    }

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public User getUser(@PathParam("id") String id) {
        return users.get(id);

    }

}
