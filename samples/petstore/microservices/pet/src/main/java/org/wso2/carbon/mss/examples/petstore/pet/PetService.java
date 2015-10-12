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
package org.wso2.carbon.mss.examples.petstore.pet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.MicroservicesRunner;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * Pet microservice
 */
@Path("/pet")
public class PetService {
    private static final Logger log = LoggerFactory.getLogger(PetService.class);


    @PUT
    public void addPet() {
        log.info("Added pet");
    }

    @DELETE
    public void deletePet() {
        log.info("Deleted pet");
    }

    @POST
    public void updatePet() {
        log.info("Updated pet");
    }

    @GET
    public void getPet() {
        log.info("Got pet");
    }

    public static void main(String[] args) {
        new MicroservicesRunner().deploy(new PetService()).start();
    }
}
