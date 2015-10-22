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

package org.wso2.carbon.mss.examples.petstore.admin.client;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.wso2.carbon.mss.examples.petstore.admin.model.Configuration;
import org.wso2.carbon.mss.examples.petstore.util.model.Category;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@ManagedBean
@ApplicationScoped
public class PetCategoryServiceClient {

    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    public boolean addPetCategory(Category category) throws IOException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/category");
        Gson gson = new Gson();
        final Response response = target.request().post(Entity.entity(gson.toJson(category), MediaType.APPLICATION_JSON));
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            return true;
        }
        return false;
    }


    public boolean removePetCategory(Category category) throws IOException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/category/" + category.getName());
        final Response response = target.request().delete();
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            return true;
        }
        return false;
    }

    public List<Category> list() {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/category/all");
        final Response response = target.request().get();
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            String body = response.readEntity(String.class);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Category>>() {
            }.getType();
            return gson.fromJson(body, listType);
        }
        return Collections.emptyList();
    }


    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
