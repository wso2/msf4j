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
import org.wso2.msf4j.examples.petstore.util.model.Pet;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Client to access PetServiceClient.
 */
@ManagedBean
@ApplicationScoped
public class PetServiceClient extends AbstractServiceClient {

    private static final Logger LOGGER = Logger.getLogger(PetServiceClient.class.getName());
    public static final String HTTP_HEADER_HOST = "host";
    public static final String HTTP_HEADER_ORIGIN = "origin";

    @Nullable
    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    public boolean addPet(Pet pet) throws IOException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/pet");
        Gson gson = new Gson();
        LOGGER.info("Connecting to pet service on " + configuration.getPetServiceEP());
        final Response response = target.request().header(LoginBean.X_JWT_ASSERTION, getJWTToken())
                .post(Entity.entity(gson.toJson(pet), MediaType.APPLICATION_JSON));
        LOGGER.info("Returned from pet service " + configuration.getPetServiceEP());
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            return true;
        }
        LOGGER.log(Level.SEVERE, "Pet service return code is  " + response.getStatus() + " , " +
                                 "hence can't proceed with the response");
        return false;
    }


    public boolean removePet(String id) throws IOException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/pet/" + id);
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

    public List<Pet> list() {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getPetServiceEP() + "/pet/all");
        LOGGER.info("Connecting to pet service on " + configuration.getPetServiceEP());
        final Response response = target.request().header(LoginBean.X_JWT_ASSERTION, getJWTToken()).get();
        LOGGER.info("Returned from pet service " + configuration.getPetServiceEP());
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            String body = response.readEntity(String.class);
            Gson gson = new Gson();
            Type listType = new PetTypeToken().getType();
            return modifyImageUrls(gson.fromJson(body, listType));
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


    private List<Pet> modifyImageUrls(List<Pet> pets) {
        String serverIP = getServerIP();
        for (Pet pet : pets) {
            pet.setImage(modifyImageURL(pet.getImage(), serverIP));
        }
        return pets;
    }

    private String getServerIP() {

        // case 1 - Fixed IP for file server
        if (configuration.getFileUploadServiceNodeHost() != null) {
            LOGGER.info("Env variable FE_FILE_SERVICE_NODE_HOST found hence use that value");
            return configuration.getFileUploadServiceNodeHost();
        }

        // case 2 - Not fixed IP for file server
        LOGGER.info("Env variable FE_FILE_SERVICE_NODE_HOST not found hence try to use HOST header");
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) extContext.getRequest();
        String host = request.getHeader(HTTP_HEADER_HOST);
        if (host == null || host.isEmpty()) {
            host = request.getHeader(HTTP_HEADER_ORIGIN);
        }
        return host.substring(0, host.indexOf(":"));
    }

    private String modifyImageURL(String imageName, String serverIP) {
        LOGGER.info("Current Image URL " + imageName);
        Integer nodePort = Integer.valueOf(configuration.getFileUploadServiceNodePort());
        String imageFile = "/fs/".concat(imageName);
        try {
            URL newURL = new URL("http", serverIP, nodePort, imageFile);
            LOGGER.info("New Image URL " + newURL.toString());
            return newURL.toString();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return imageName;
        }
    }

    static class PetTypeToken extends TypeToken<List<Pet>> {
        private static final long serialVersionUID = -3401766631953404086L;
    }
}
