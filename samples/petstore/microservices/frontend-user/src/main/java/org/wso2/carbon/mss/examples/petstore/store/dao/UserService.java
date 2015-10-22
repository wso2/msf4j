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

import org.wso2.carbon.mss.examples.petstore.store.client.UserServiceClient;
import org.wso2.carbon.mss.examples.petstore.store.model.UserServiceException;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import java.io.Serializable;
import java.util.logging.Logger;

@ManagedBean
@ApplicationScoped
public class UserService implements Serializable {

    private final static Logger LOGGER = Logger.getLogger(UserService.class.getName());

    @ManagedProperty("#{userServiceClient}")
    private UserServiceClient userServiceClient;

    public String authenticate(String username, String password) throws UserServiceException {
        return userServiceClient.login(username, password);
    }

    public UserServiceClient getUserServiceClient() {
        return userServiceClient;
    }

    public void setUserServiceClient(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }
}
