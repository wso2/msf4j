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

package org.wso2.carbon.mss.examples.petstore.store.dao;

import org.wso2.carbon.mss.examples.petstore.store.model.PetServiceException;
import org.wso2.carbon.mss.examples.petstore.util.model.Category;
import org.wso2.carbon.mss.examples.petstore.util.model.Pet;

import java.util.List;

public interface PetService {

    public boolean addPetType(Category type) throws PetServiceException;

    public boolean removePetType(Category category) throws PetServiceException;

    public List<Category> listPetTypes();

    public Pet getPet(String id);

    public boolean addPet(Pet pet) throws PetServiceException;

    public boolean remove(String id) throws PetServiceException;


    public List<Pet> listPets();
}
