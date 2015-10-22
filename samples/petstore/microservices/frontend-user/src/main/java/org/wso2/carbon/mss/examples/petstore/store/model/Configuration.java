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

package org.wso2.carbon.mss.examples.petstore.store.model;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 * Configuration details such as endpoint references for dependant services.
 */
@ManagedBean
@ApplicationScoped
public class Configuration {
    //    String petServiceEP = "http://172.17.4.201:32270";
    public static final String FILE_UPLOAD_SERVICE_EP_NAME = "UPLOAD_SERVICE_EP";
    public static final String PET_SERVICE_EP_NAME = "PET_SERVICE_EP";
    public static final String TX_SERVICE_EP_NAME = "TX_SERVICE_EP";
    public static final String USER_SERVICE_EP_NAME = "USER_SERVICE_EP";


    public static final String DEFAULT_FILE_UPLOAD_SERVICE_EP = "http://192.168.99.100";
    public static final String DEFAULT_USER_SERVICE_EP = "http://127.0.0.1:8070";
    public static final String DEFAULT_PET_SERVICE_EP = "http://localhost:8080";
    public static final String DEFAULT_TX_SERVICE_EP = "http://127.0.0.1:8090";


    private String fileUploadServiceEP;
    private String petServiceEP;
    private String txServiceEP;
    private String userServiceEP;

    public Configuration() {
        fileUploadServiceEP = setProperty(FILE_UPLOAD_SERVICE_EP_NAME, DEFAULT_FILE_UPLOAD_SERVICE_EP);
        petServiceEP = setProperty(PET_SERVICE_EP_NAME, DEFAULT_PET_SERVICE_EP);
        txServiceEP = setProperty(TX_SERVICE_EP_NAME, DEFAULT_TX_SERVICE_EP);
        userServiceEP = setProperty(USER_SERVICE_EP_NAME, DEFAULT_USER_SERVICE_EP);
    }

    private String setProperty(String propertyName, String defaultPropertValue) {
        String propertyValue = System.getenv(propertyName);
        if (propertyValue == null) {
            propertyValue = System.getProperty(propertyName);
        }
        if (propertyValue == null) {
            propertyValue = defaultPropertValue;
        }
        return propertyValue;
    }


    public String getFileUploadServiceEP() {
        return fileUploadServiceEP;
    }

    public void setFileUploadServiceEP(String fileUploadServiceEP) {
        this.fileUploadServiceEP = fileUploadServiceEP;
    }

    public String getPetServiceEP() {
        return petServiceEP;
    }

    public void setPetServiceEP(String petServiceEP) {
        this.petServiceEP = petServiceEP;
    }

    public String getTxServiceEP() {
        return txServiceEP;
    }

    public void setTxServiceEP(String txServiceEP) {
        this.txServiceEP = txServiceEP;
    }

    public String getUserServiceEP() {
        return userServiceEP;
    }

    public void setUserServiceEP(String userServiceEP) {
        this.userServiceEP = userServiceEP;
    }
}
