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

package org.wso2.carbon.mss.examples.petstore.util.fe.model;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import static org.wso2.carbon.mss.examples.petstore.util.SystemVariableUtil.getValue;

/**
 * Configuration details such as endpoint references for dependant services.
 */
@ManagedBean
@ApplicationScoped
public class Configuration {

    public static final String FILE_SERVICE_HOST_NAME = "FE_FILE_SERVICE_HOST";
    public static final String FILE_SERVICE_PORT_NAME = "FE_FILE_SERVICE_PORT";

    public static final String PET_SERVICE_HOST_NAME = "FE_PET_SERVICE_HOST";
    public static final String PET_SERVICE_PORT_NAME = "FE_PET_SERVICE_PORT";

    public static final String TX_SERVICE_HOST_NAME = "FE_PET_SERVICE_HOST";
    public static final String TX_SERVICE_PORT_NAME = "FE_PET_SERVICE_PORT";

    public static final String USER_SERVICE_HOST_NAME = "FE_USER_SERVICE_HOST";
    public static final String USER_SERVICE_PORT_NAME = "FE_USER_SERVICE_PORT";


    public static final String DEFAULT_FILE_SERVICE_HOST = "192.168.99.100";
    public static final String DEFAULT_FILE_SERVICE_PORT = "80";

    public static final String DEFAULT_PET_SERVICE_HOST = "localhost";
    public static final String DEFAULT_PET_SERVICE_PORT = "8080";

    public static final String DEFAULT_TX_SERVICE_HOST = "localhost";
    public static final String DEFAULT_TX_SERVICE_PORT = "8090";

    public static final String DEFAULT_USER_SERVICE_HOST = "localhost";
    public static final String DEFAULT_USER_SERVICE_PORT = "8070";


    private String fileUploadServiceEP;
    private String petServiceEP;
    private String txServiceEP;
    private String userServiceEP;

    public Configuration() {
        fileUploadServiceEP = createHTTPEp(getValue(FILE_SERVICE_HOST_NAME, DEFAULT_FILE_SERVICE_HOST),
                                           getValue(FILE_SERVICE_PORT_NAME, DEFAULT_FILE_SERVICE_PORT));

        petServiceEP = createHTTPEp(getValue(PET_SERVICE_HOST_NAME, DEFAULT_PET_SERVICE_HOST),
                                    getValue(PET_SERVICE_PORT_NAME, DEFAULT_PET_SERVICE_PORT));

        txServiceEP = createHTTPEp(getValue(TX_SERVICE_HOST_NAME, DEFAULT_TX_SERVICE_HOST),
                                   getValue(TX_SERVICE_PORT_NAME, DEFAULT_TX_SERVICE_PORT));

        userServiceEP = createHTTPEp(getValue(USER_SERVICE_HOST_NAME, DEFAULT_USER_SERVICE_HOST),
                                     getValue(USER_SERVICE_PORT_NAME, DEFAULT_USER_SERVICE_PORT));
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

    private String createHTTPEp(String host, String port) {
        StringBuilder builder = new StringBuilder();
        builder.append("http://")
                .append(host)
                .append(":")
                .append(port);
        return builder.toString();
    }
}
