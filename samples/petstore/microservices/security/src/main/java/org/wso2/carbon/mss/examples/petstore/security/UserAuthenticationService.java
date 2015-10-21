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
import org.wso2.carbon.mss.examples.petstore.security.util.model.User;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * UserAuthentication micro service
 */
@Path("/user")
public class UserAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationService.class);

    private static final String JWT_HEADER = "X-JWT-Assertion";

    @POST
    @Consumes("application/json")
    @Path("/login")
    public Response authenticate(User user) {
        String name = user.getName();
        log.info("Authenticating user " + name + " ..");
        String jwt = null;
        if (user.getName().equals(user.getPassword())) {
            try {
                JWTGenerator jwtGenerator = new JWTGenerator();
                jwt = jwtGenerator.generateJWT(user.getName());

                return Response.ok("User" + name + " authenticated successfully")
                        .header(JWT_HEADER, jwt).build();
            } catch (Exception e) {
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Invalid login attempt.")
                .build();

    }

}
