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

package org.wso2.carbon.mss.examples.petstore.admin.dao;

import org.wso2.carbon.mss.examples.petstore.admin.client.PetCategoryServiceClient;
import org.wso2.carbon.mss.examples.petstore.admin.client.PetServiceClient;
import org.wso2.carbon.mss.examples.petstore.admin.model.PetServiceException;
import org.wso2.carbon.mss.examples.petstore.util.model.Category;
import org.wso2.carbon.mss.examples.petstore.util.model.Pet;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean(name = "petService")
@ApplicationScoped
public class RemotePetService implements PetService, Serializable {

    private final static Logger LOGGER = Logger.getLogger(RemotePetService.class.getName());

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
