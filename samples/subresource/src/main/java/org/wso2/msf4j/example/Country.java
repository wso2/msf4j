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
package org.wso2.msf4j.example;

import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/country")
@Produces(MediaType.APPLICATION_JSON)
public class Country {

    private String name;
    private String id;
    private static HashMap<String, Country> countries = new HashMap<>();

    static {
        countries.put("SL", new Country("Sri Lanka", "SL"));
        countries.put("US", new Country("United States", "US"));
        countries.put("UK", new Country("United Kingdom", "UK"));
        countries.put("AUS", new Country("Australia", "AUS"));
    }

    public Country() {
        name = "Sri Lanka";
        id = "SL";
    }

    public Country(String name, String id) {
        this.name = name;
        this.id = id;
    }

    @Path("/{countryId}/team")
    public Team getCountryTeam(@PathParam("countryId") String countryId) {
        return new Team(countryId);
    }

    @GET
    @Path("")
    public Country getDefaultCountry() {
        return new Country();
    }

    @GET
    @Path("/{countryId}")
    public Country getCountryId(@PathParam("countryId") String countryId) {
        return countries.get(countryId);
    }
}