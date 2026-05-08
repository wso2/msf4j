/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Sub-resource class representing a bowler, extending the Player sub-resource.
 */
@Path("/")
public class Bowler extends Player {

    private static final Logger log = LoggerFactory.getLogger(Bowler.class);

    private String bowlerType;

    public Bowler(String countryId, int playerId) {
        setCountryId(countryId);
        setPlayerId(playerId);
        bowlerType = "Fast bowler";
    }

    @GET
    @Path("/details/{filed}")
    public Bowler getBowlerProfileFiled(@PathParam("countryId") String countryId, @PathParam("playerId") int playerId,
                                        @PathParam("filed") String field) {
        String msg =
                Bowler.class.getName() + "Inside getPlayerProfileFiled - playerId : " + playerId + " for countryId : " +
                countryId +
                " with filed " + field;
        log.info("{}", msg);
        return new Bowler(countryId, playerId);
    }

    @GET
    @Path("/bowlerType")
    public String getBowlerType(@PathParam("countryId") String countryId, @PathParam("playerId") int playerId) {
        String msg = Bowler.class.getName() + " Inside getBowlerType - playerId : " + playerId + " for countryId : " +
                     countryId;
        log.info("{}", msg);
        return "Fast Bowler";
    }

}
