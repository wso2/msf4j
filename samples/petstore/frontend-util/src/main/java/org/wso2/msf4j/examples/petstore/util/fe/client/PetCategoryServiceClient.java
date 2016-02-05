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

package org.wso2.msf4j.examples.petstore.util.fe.client;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.wso2.msf4j.examples.petstore.util.fe.model.Configuration;
import org.wso2.msf4j.examples.petstore.util.fe.view.LoginBean;
import org.wso2.msf4j.examples.petstore.util.model.Category;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Client to access PetCategoryServiceClient.
 */
@ManagedBean
@ApplicationScoped
public class PetCategoryServiceClient extends AbstractServiceClient {

    private static final Logger LOGGER = Logger.getLogger(PetCategoryServiceClient.class.getName());

    @Nullable
    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    public boolean addPetCategory(Category category) throws IOException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/category");
        Gson gson = new Gson();
        LOGGER.info("Connecting to pet service on " + configuration.getPetServiceEP());
        final Response response = target.request().header(LoginBean.X_JWT_ASSERTION, getJWTToken())
                .post(Entity.entity(gson.toJson(category), MediaType.APPLICATION_JSON));
        LOGGER.info("Returned from pet service " + configuration.getPetServiceEP());
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            return true;
        }
        LOGGER.log(Level.SEVERE, "Pet service return code is  " + response.getStatus() + " , " +
                                 "hence can't proceed with the response");
        return false;
    }


    public boolean removePetCategory(Category category) throws IOException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/category/" + category.getName());
        LOGGER.info("Connecting to pet service on " + configuration.getPetServiceEP());
        final Response response = target.request().header(LoginBean.X_JWT_ASSERTION, getJWTToken()).delete();
        LOGGER.info("Returned from pet service " + configuration.getPetServiceEP());
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            return true;
        }
        LOGGER.log(Level.SEVERE, "Pet service return code is  " + response.getStatus() + " , " +
                                 "hence can't proceed with the response");
        return false;
    }

    public List<Category> list() {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/category/all");
        LOGGER.info("Connecting to pet service on " + configuration.getPetServiceEP());
        final Response response = target.request().header(LoginBean.X_JWT_ASSERTION, getJWTToken()).get();
        LOGGER.info("Returned from pet service " + configuration.getPetServiceEP());
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            String body = response.readEntity(String.class);
            Gson gson = new Gson();
            Type listType = new CategoryTypeToken().getType();
            return gson.fromJson(body, listType);
        }
        LOGGER.log(Level.SEVERE, "Pet service return code is  " + response.getStatus() + " , " +
                                 "hence can't proceed with the response");
        return Collections.emptyList();
    }


    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    static class CategoryTypeToken extends TypeToken<List<Category>> {
        private static final long serialVersionUID = 4534722069729160047L;
    }
}
