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

package org.wso2.msf4j.examples.petstore.util.fe.dao;

import org.wso2.msf4j.examples.petstore.util.fe.client.PetCategoryServiceClient;
import org.wso2.msf4j.examples.petstore.util.fe.client.PetServiceClient;
import org.wso2.msf4j.examples.petstore.util.fe.model.PetServiceException;
import org.wso2.msf4j.examples.petstore.util.model.Category;
import org.wso2.msf4j.examples.petstore.util.model.Pet;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

/**
 * RemotePetService implementation of PaymentService class.
 */
@ManagedBean(name = "petService")
@ApplicationScoped
public class RemotePetService implements PetService {

    private static final Logger LOGGER = Logger.getLogger(RemotePetService.class.getName());

    @ManagedProperty("#{petCategoryServiceClient}")
    private PetCategoryServiceClient petCategoryServiceClient;

    @ManagedProperty("#{petServiceClient}")
    private PetServiceClient petServiceClient;

    @Override
    public boolean addPetType(Category category) throws PetServiceException {
        try {
            return petCategoryServiceClient.addPetCategory(category);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PetServiceException("Can't add new category");
        }
    }

    @Override
    public boolean removePetType(Category category) throws PetServiceException {
        try {
            return petCategoryServiceClient.removePetCategory(category);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PetServiceException("Can't remove the category");
        }
    }

    @Override
    public List<Category> listPetTypes() {
        return petCategoryServiceClient.list();

    }

    @Override
    public Pet getPet(String id) {
        //TODO -
        return null;
    }


    @Override
    public boolean addPet(Pet pet) throws PetServiceException {
        try {
            return petServiceClient.addPet(pet);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PetServiceException("Can't add pet");
        }
    }

    @Override
    public boolean remove(String id) throws PetServiceException {
        try {
            return petServiceClient.removePet(id);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PetServiceException("Can't remove the pet");
        }
    }

    @Override
    public List<Pet> listPets() {
        return petServiceClient.list();
    }

    public PetCategoryServiceClient getPetCategoryServiceClient() {
        return petCategoryServiceClient;
    }

    public void setPetCategoryServiceClient(PetCategoryServiceClient petCategoryServiceClient) {
        this.petCategoryServiceClient = petCategoryServiceClient;
    }

    public PetServiceClient getPetServiceClient() {
        return petServiceClient;
    }

    public void setPetServiceClient(PetServiceClient petServiceClient) {
        this.petServiceClient = petServiceClient;
    }
}
