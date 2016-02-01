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

import de.larmic.butterfaces.event.TableSingleSelectionListener;
import de.larmic.butterfaces.model.table.DefaultTableModel;
import de.larmic.butterfaces.model.table.TableModel;
import org.wso2.msf4j.examples.petstore.util.fe.model.Cart;
import org.wso2.msf4j.examples.petstore.util.model.Pet;

import java.util.List;
import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 * Bean classes used for JSF model.
 */
@ManagedBean
@SessionScoped
public class CartListBean {

    @Nullable
    @ManagedProperty("#{cart}")
    private Cart cart;
    private TableModel tableModel = new DefaultTableModel();
    private Pet selectedValue;

    public TableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    public List<Pet> getPets() {
        return cart.getItems();
    }

    public Pet getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(Pet selectedValue) {
        this.selectedValue = selectedValue;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public TableSingleSelectionListener<Pet> getTableSelectionListener() {
        return new TableSingleSelectionListener<Pet>() {
            @Override
            public void processTableSelection(Pet data) {
                selectedValue = data;
            }

            @Override
            public boolean isValueSelected(Pet data) {
                return selectedValue != null ? data.getId().equals(selectedValue.getId()) : false;
            }
        };
    }

}
