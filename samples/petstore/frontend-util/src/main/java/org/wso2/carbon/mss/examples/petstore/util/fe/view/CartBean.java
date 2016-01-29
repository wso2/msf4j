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

import org.wso2.msf4j.examples.petstore.util.fe.model.Cart;
import org.wso2.msf4j.examples.petstore.util.model.Pet;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

/**
 * Bean classes used for JSF model.
 */
@ManagedBean
@ViewScoped
public class CartBean {

    @Nullable
    @ManagedProperty("#{cart}")
    private Cart cart;

    private Pet selected;

    public void addToCart(Pet pet) {
        cart.addItem(pet);
    }
    public void removeFromCart(Pet pet) {
        cart.removeItem(pet);
    }
    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Pet getSelected() {
        return selected;
    }

    public void setSelected(Pet selected) {
        this.selected = selected;
    }
}
