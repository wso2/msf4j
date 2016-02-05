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

import org.wso2.msf4j.examples.petstore.util.fe.dao.PetService;
import org.wso2.msf4j.examples.petstore.util.fe.model.PetServiceException;
import org.wso2.msf4j.examples.petstore.util.model.Category;

import java.util.List;
import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

/**
 * Bean classes used for JSF model.
 */
@ManagedBean
@RequestScoped
public class PetTypeBean {

    @Nullable
    @ManagedProperty("#{petService}")
    private PetService petService;

    private Category petType;
    private String result;

    public PetTypeBean() {
        petType = new Category();
    }

    public void addPetType() throws PetServiceException {
        petService.addPetType(petType);
        result = "Added " + petType.getName() + " successfully.";
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Flash flash = facesContext.getExternalContext().getFlash();
        flash.setKeepMessages(true);
        flash.setRedirect(true);
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, result, null));
        petType = null;
    }

    public List<Category> listPetTypes() {
        return petService.listPetTypes();
    }

    public PetService getPetService() {
        return petService;
    }

    public void setPetService(PetService petService) {
        this.petService = petService;
    }

    public Category getPetType() {
        return petType;
    }

    public void setPetType(Category petType) {
        this.petType = petType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
