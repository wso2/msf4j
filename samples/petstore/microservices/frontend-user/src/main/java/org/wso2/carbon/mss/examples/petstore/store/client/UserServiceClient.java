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

package org.wso2.carbon.mss.examples.petstore.store.client;

import com.google.gson.Gson;
import org.wso2.carbon.mss.examples.petstore.store.model.Configuration;
import org.wso2.carbon.mss.examples.petstore.store.model.UserServiceException;
import org.wso2.carbon.mss.examples.petstore.store.view.LoginBean;
import org.wso2.carbon.mss.examples.petstore.util.model.User;

import javax.annotation.Nullable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Client to access UserServiceClient.
 */
@ManagedBean
@ApplicationScoped
public class UserServiceClient extends AbstractServiceClient {

    @Nullable
    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    public String login(String username, String password) throws UserServiceException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getUserServiceEP() + "/user/login");
        User user = new User();
        user.setName(username);
        user.setPassword(password);
        Gson gson = new Gson();
        final Response response = target.request().post(Entity.entity(gson.toJson(user), MediaType.APPLICATION_JSON));
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            return response.getHeaderString(LoginBean.X_JWT_ASSERTION);
        } else {
            throw new UserServiceException("Can't authenticate the user");
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
