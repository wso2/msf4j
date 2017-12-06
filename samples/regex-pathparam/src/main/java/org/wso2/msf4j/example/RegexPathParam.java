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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/PathParamWithRegex")
public class RegexPathParam {

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    public Response getAssetState(@PathParam("assetType") String assetType, @PathParam("id") String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("Asset Type = ").append(assetType).append(", Asset Id = ").append(id);
        return Response.ok().entity(sb.toString()).build();
    }

    @GET
    @Path("/endpoints/{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    public Response createEndpoint(@PathParam("assetType") String assetType, @PathParam("id") String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("Asset Type = ").append(assetType).append(", Asset Id = ").append(id);
        return Response.ok().entity(sb.toString()).build();
    }
}
