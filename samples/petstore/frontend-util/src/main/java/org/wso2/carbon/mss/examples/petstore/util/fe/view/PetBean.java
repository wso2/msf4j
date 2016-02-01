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

package org.wso2.msf4j.examples.petstore.util.fe.view;

import org.wso2.msf4j.examples.petstore.util.fe.client.ImageServiceClient;
import org.wso2.msf4j.examples.petstore.util.fe.dao.PetService;
import org.wso2.msf4j.examples.petstore.util.fe.model.ImageServiceException;
import org.wso2.msf4j.examples.petstore.util.fe.model.PetServiceException;
import org.wso2.msf4j.examples.petstore.util.model.Category;
import org.wso2.msf4j.examples.petstore.util.model.Pet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.Part;

/**
 * Bean classes used for JSF model.
 */
@ManagedBean
@RequestScoped
public class PetBean {

    private static final Logger LOGGER = Logger.getLogger(PetBean.class.getName());

    private Pet pet;
    private Part file;

    @ManagedProperty("#{petService}")
    private PetService petService;

    @ManagedProperty("#{imageServiceClient}")
    private ImageServiceClient imageServiceClient;


    public PetBean() {
        this.pet = new Pet();
        pet.setCategory(new Category());
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }


    public void addPet() throws ImageServiceException, PetServiceException {
        String imageURL = null;
        try {
            imageURL = imageServiceClient.uploadImage(file);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ImageServiceException("Exception while uploading image, try again later");
        }

        if (imageURL != null && !imageURL.isEmpty()) {
            LOGGER.info("Pet image URL : " + imageURL);
            pet.setImage(imageURL);
        } else {
            throw new ImageServiceException("Exception while uploading image, try again later");
        }
        petService.addPet(pet);

        String result = "Pet added successfully.";
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Flash flash = facesContext.getExternalContext().getFlash();
        flash.setKeepMessages(true);
        flash.setRedirect(true);
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, result, null));
        pet = null;
    }

    public void removePet(String id) throws PetServiceException {
        petService.remove(id);
    }

    public PetService getPetService() {
        return petService;
    }

    public void setPetService(PetService petService) {
        this.petService = petService;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    public ImageServiceClient getImageServiceClient() {
        return imageServiceClient;
    }

    public void setImageServiceClient(ImageServiceClient imageServiceClient) {
        this.imageServiceClient = imageServiceClient;
    }

}
