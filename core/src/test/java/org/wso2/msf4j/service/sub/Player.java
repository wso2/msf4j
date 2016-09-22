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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Simple RESTful web service for Player.
 */
@Path("/")
public class Player {

    private String name;
    private int playerId;
    private String countryId;
    private String countryName;
    private byte age;
    private String type;

    public Player(String countryId, int playerId) {
        this.countryId = countryId;
        this.playerId = playerId;
        age = 30;
        name = "player_1";
    }

    public Player() {
        name = "Sanath Jayasuriya";
        age = 27;
    }

    @POST
    @Path("/details/{filed}")
    public String getPlayerProfileFiled(@PathParam("countryId") String countryId, @PathParam("playerId") int playerId,
                                        @PathParam("filed") String field, @FormParam("type") String type,
                                        @FormParam("countryName") String countryName) {
        return countryId + "_" + playerId + "_" + field + "_" + type + "_" + countryName;
    }

    @POST
    @Path("")
    public Player getPlayerProfile(@PathParam("countryId") String countryId, @PathParam("playerId") int playerId,
                                        @FormParam("type") String type, @FormParam("countryName") String countryName) {
        Player player = new Player(countryId, playerId);
        player.setType(type);
        player.setCountryName(countryName);
        return player;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAge(byte age) {
        this.age = age;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getName() {
        return name;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public byte getAge() {
        return age;
    }

    public String getType() {
        return type;
    }
}
