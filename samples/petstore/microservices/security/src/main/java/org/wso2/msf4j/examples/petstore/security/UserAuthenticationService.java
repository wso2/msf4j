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

package org.wso2.msf4j.examples.petstore.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.annotation.Timed;
import org.wso2.msf4j.examples.petstore.security.ldap.LDAPUserStoreManager;
import org.wso2.msf4j.examples.petstore.util.model.User;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitored;
import org.wso2.msf4j.util.SystemVariableUtil;

import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * UserAuthentication micro service.
 */
@HTTPMonitored
@Path("/user")
public class UserAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationService.class);
    private static final String JWT_HEADER = "X-JWT-Assertion";
    private String host = SystemVariableUtil.getValue("LDAP_HOST", "localhost");
    private int port = Integer.parseInt(SystemVariableUtil.getValue("LDAP_PORT", "10389"));
    private String connectionName = SystemVariableUtil.getValue("LDAP_CONNECTION_NAME", "uid=admin,ou=system");
    private String connectionPassword = SystemVariableUtil.getValue("LDAP_CONNECTION_PASSWORD", "admin");

    @POST
    @Consumes("application/json")
    @Path("/login")
    @Timed
    public Response authenticate(User user) {
        String name = user.getName();
        log.info("Authenticating user " + name + " ..");
        String jwt;
        boolean isAuthenticated;
        try {
            LDAPUserStoreManager ldapUserStoreManager = LDAPUserStoreManager.
                    getInstance(host, port, connectionName, connectionPassword);
            isAuthenticated = ldapUserStoreManager.isValidUser(name, user.getPassword());
            if (isAuthenticated) {
                User userFromUserStore = new User();
                userFromUserStore.setName(name);
                userFromUserStore.setEmail(ldapUserStoreManager.getAttributeValue(name, "mail"));
                userFromUserStore.setLastName(ldapUserStoreManager.getAttributeValue(name, "sn"));

                JWTGenerator jwtGenerator = new JWTGenerator();
                jwt = jwtGenerator.generateJWT(userFromUserStore);
                String msg = "User " + name + " authenticated successfully";
                log.info(msg);
                return Response.ok(msg).header(JWT_HEADER, jwt).build();
            }
        } catch (Exception e) {
            log.error("Exception occurred while trying to authenticate user " + name, e);
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
        log.warn("Failed login attempt by user " + name);
        return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid login attempt.").build();
    }

    @POST
    @Consumes("application/json")
    @Path("/add")
    @Timed
    public Response addUser(User user) {
        String name = user.getName();
        log.info("Adding new user " + name + " ..");
        LDAPUserStoreManager ldapUserStoreManager;

        try {
            ldapUserStoreManager = LDAPUserStoreManager
                    .getInstance(host, port, connectionName, connectionPassword);

            //Create groups
            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (String role : user.getRoles()) {
                    ldapUserStoreManager.addGroup(role, role);
                }
            }

            ldapUserStoreManager.addUserAndAssignGroups(user.getName(), user.getFirstName(), user.getLastName(),
                    user.getPassword(), user.getEmail(), user.getRoles());
            log.info("User " + name + " successfully added ..");
        } catch (NamingException e) {
            log.error("Exception occurred while adding user " + name, e);
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
        return Response.status(Response.Status.OK).entity("User " + name + " successfully added").build();
    }

    @POST
    @Consumes("application/json")
    @Path("/ldapgroup/{name}/{description}")
    @Timed
    public Response addLDAPGroup(@PathParam("name") String name, @PathParam("description") String description) {
        log.info("Adding new ldap group " + name + " ..");
        LDAPUserStoreManager ldapUserStoreManager;
        try {
            ldapUserStoreManager = LDAPUserStoreManager
                    .getInstance(host, port, connectionName, connectionPassword);
            ldapUserStoreManager.addGroup(name, description);
        } catch (NamingException e) {
            log.error("Error occurred while adding LDAP group " + name, e);
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
        return Response.status(Response.Status.OK).entity("LDAP group " + name + " successfully added").build();
    }
}
