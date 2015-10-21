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

package org.wso2.carbon.mss.examples.petstore.store.view;

import de.larmic.butterfaces.event.TableSingleSelectionListener;
import de.larmic.butterfaces.model.table.DefaultTableModel;
import de.larmic.butterfaces.model.table.TableModel;
import org.wso2.carbon.mss.examples.petstore.store.dao.PetService;
import org.wso2.carbon.mss.examples.petstore.store.model.PetServiceException;
import org.wso2.carbon.mss.examples.petstore.util.model.Category;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;

@ManagedBean
@ViewScoped
public class PetTypeListBean implements Serializable {

    @ManagedProperty("#{petService}")
    private PetService petService;
    private List<Category> petTypes;
    private TableModel tableModel = new DefaultTableModel();
    private Category selectedValue;

    @PostConstruct
    public void init() {
        petTypes = petService.listPetTypes();
    }

    public List<Category> getPetTypes() {
        return petTypes;
    }


    public void removePetType(Category category) throws PetServiceException {
        petService.removePetType(category);
        petTypes = petService.listPetTypes();
    }

    public void setPetTypes(List<Category> petTypes) {
        this.petTypes = petTypes;
    }

    public PetService getPetService() {
        return petService;
    }

    public void setPetService(PetService petService) {
        this.petService = petService;
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
    }


    public TableSingleSelectionListener<Category> getTableSelectionListener() {
        return new TableSingleSelectionListener<Category>() {
            @Override
            public void processTableSelection(Category category) {
                selectedValue = category;
            }


            @Override
            public boolean isValueSelected(Category category) {
                if (category != null && selectedValue != null) {
                    return category.getName() != null ? category.getName().equals(selectedValue.getName()) : false;
                }
                return false;
            }
        };

    }
}
