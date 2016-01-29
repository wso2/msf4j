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

package org.wso2.msf4j.examples.petstore.util.fe.model;

import org.wso2.msf4j.examples.petstore.util.model.Pet;

import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * This represent shopping cart for this sample.
 */
@ManagedBean
@SessionScoped
public class Cart {

    private List<Pet> items = new ArrayList<>();
    private float total = 0;

    public List<Pet> getItems() {
        return items;
    }

    public void setItems(List<Pet> items) {
        this.items = items;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public void addItem(Pet pet) {
        total = total + pet.getPrice();
        items.add(pet);
    }

    public void removeItem(Pet pet) {
        total = total - pet.getPrice();
        items.remove(pet);

    }

    public void clear() {
        items.clear();
        total = 0;
    }
}
