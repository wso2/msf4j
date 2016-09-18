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
package org.wso2.msf4j.service.sub;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Simple RESTful web service for Team.
 */
@Path("/")
public class Team {

    private String teamType;
    private String countryName;

    public String getCountryId() {
        return countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getTeamType() {
        return teamType;
    }

    public void setTeamType(String teamType) {
        this.teamType = teamType;
    }

    private String countryId;

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public Team(String countryId) {
        this.countryId = countryId;
        teamType = "Cricket";
    }

    public Team(String countryId, String countryName) {
        this.countryName = countryName;
        this.countryId = countryId;
        teamType = "Cricket";
    }

    @Path("/{playerId}")
    public Player getPlayerObj(@PathParam("countryId") String countryId, @PathParam("playerId") int playerId) {
        return new Player(countryId, playerId);
    }

    @GET
    @Path("")
    public Team getCountryTeam(@PathParam("countryId") String countryId) {
        return new Team(countryId);
    }

    @POST
    @Path("")
    public Team getCountryTeamFromPost(@PathParam("countryId") String countryId,
                                       @FormParam("countryName") String countryName) {
        return new Team(countryId, countryName);
    }
}
