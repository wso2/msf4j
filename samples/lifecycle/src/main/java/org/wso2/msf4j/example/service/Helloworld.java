/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.msf4j.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Helloworld resource class.
 */
@Path("/hello")
public class Helloworld {

    private static final Logger LOG = LoggerFactory.getLogger(Helloworld.class);

    @PostConstruct
    public void init() {
        LOG.info("Helloworld :: calling PostConstruct method");
    }

    @PreDestroy
    public void close() {
        LOG.info("Helloworld :: calling PreDestroy method");
    }

    @GET
    @Path("/{user}")
    public String getUser(@PathParam("user") String user) {
        return "Hello " + user;
    }

}
