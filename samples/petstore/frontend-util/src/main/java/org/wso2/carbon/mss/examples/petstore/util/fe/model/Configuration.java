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

import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;


/**
 * Configuration details such as endpoint references for dependant services.
 */
@ManagedBean
@ApplicationScoped
@SuppressWarnings("unused")
public class Configuration {

    public static final String FILE_SERVICE_HOST_NAME = "FE_FILE_SERVICE_HOST";
    public static final String FILE_SERVICE_PORT_NAME = "FE_FILE_SERVICE_PORT";
    public static final String FILE_SERVICE_NODE_PORT_NAME = "FE_FILE_SERVICE_NODE_PORT";
    // This is useful when file server hot is fixed such as local testing, should not define for Kubernetes.
    public static final String FILE_SERVICE_NODE_HOST_NAME = "FE_FILE_SERVICE_NODE_HOST";

    public static final String PET_SERVICE_HOST_NAME = "FE_PET_SERVICE_HOST";
    public static final String PET_SERVICE_PORT_NAME = "FE_PET_SERVICE_PORT";

    public static final String TXN_SERVICE_HOST_NAME = "FE_TXN_SERVICE_HOST";
    public static final String TXN_SERVICE_PORT_NAME = "FE_TXN_SERVICE_PORT";

    public static final String USER_SERVICE_HOST_NAME = "FE_USER_SERVICE_HOST";
    public static final String USER_SERVICE_PORT_NAME = "FE_USER_SERVICE_PORT";


    public static final String DEFAULT_FILE_SERVICE_HOST = "192.168.99.100";
    public static final String DEFAULT_FILE_SERVICE_PORT = "80";
    public static final String DEFAULT_FILE_SERVICE_NODE_PORT = "80";

    public static final String DEFAULT_PET_SERVICE_HOST = "localhost";
    public static final String DEFAULT_PET_SERVICE_PORT = "8050";

    public static final String DEFAULT_TXN_SERVICE_HOST = "localhost";
    public static final String DEFAULT_TXN_SERVICE_PORT = "8090";

    public static final String DEFAULT_USER_SERVICE_HOST = "localhost";
    public static final String DEFAULT_USER_SERVICE_PORT = "8070";

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private String fileUploadServiceEP;
    private String petServiceEP;
    private String txServiceEP;
    private String userServiceEP;
    private String fileUploadServiceNodePort;
    private String fileUploadServiceNodeHost;


    public Configuration() {
        fileUploadServiceEP = createHTTPEp(getValue(FILE_SERVICE_HOST_NAME, DEFAULT_FILE_SERVICE_HOST),
                                           getValue(FILE_SERVICE_PORT_NAME, DEFAULT_FILE_SERVICE_PORT));

        petServiceEP = createHTTPEp(getValue(PET_SERVICE_HOST_NAME, DEFAULT_PET_SERVICE_HOST),
                                    getValue(PET_SERVICE_PORT_NAME, DEFAULT_PET_SERVICE_PORT));

        txServiceEP = createHTTPEp(getValue(TXN_SERVICE_HOST_NAME, DEFAULT_TXN_SERVICE_HOST),
                                   getValue(TXN_SERVICE_PORT_NAME, DEFAULT_TXN_SERVICE_PORT));

        userServiceEP = createHTTPEp(getValue(USER_SERVICE_HOST_NAME, DEFAULT_USER_SERVICE_HOST),
                                     getValue(USER_SERVICE_PORT_NAME, DEFAULT_USER_SERVICE_PORT));

        fileUploadServiceNodePort = getValue(FILE_SERVICE_NODE_PORT_NAME, DEFAULT_FILE_SERVICE_NODE_PORT);

        fileUploadServiceNodeHost = getValue(FILE_SERVICE_NODE_HOST_NAME, null);

        LOGGER.info("...........INFO...................");
        LOGGER.info("User Service Endpoint : " + userServiceEP);
        LOGGER.info("Pet Service Endpoint : " + petServiceEP);
        LOGGER.info("TXN Service Endpoint : " + txServiceEP);
        LOGGER.info("File Service Endpoint : " + fileUploadServiceEP);
        LOGGER.info("File Service Node Port  : " + fileUploadServiceNodePort);
        LOGGER.info("File Service Node Host  : " + fileUploadServiceNodeHost);
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

    public String getFileUploadServiceNodePort() {
        return fileUploadServiceNodePort;
    }

    public void setFileUploadServiceNodePort(String fileUploadServiceNodePort) {
        this.fileUploadServiceNodePort = fileUploadServiceNodePort;
    }

    public String getFileUploadServiceNodeHost() {
        return fileUploadServiceNodeHost;
    }

    public void setFileUploadServiceNodeHost(String fileUploadServiceNodeHost) {
        this.fileUploadServiceNodeHost = fileUploadServiceNodeHost;
    }

    private String createHTTPEp(String host, String port) {
        return "http://" + host + ":" + port;
    }

    public static String getValue(String variableName, String defaultValue) {
        String value;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            value = defaultValue;
        }
        return value;
    }
}
