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

import org.wso2.msf4j.examples.petstore.util.fe.dao.UserService;
import org.wso2.msf4j.examples.petstore.util.fe.model.User;
import org.wso2.msf4j.examples.petstore.util.fe.model.UserServiceException;

import javax.annotation.Nullable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

/**
 * Bean classes used for JSF model.
 */
@ManagedBean
@ViewScoped
public class UserBean {

    public static final String ROLE_USER = "user";

    @Nullable
    @ManagedProperty("#{userService}")
    private UserService userService;

    @Nullable
    @ManagedProperty("#{navigationBean}")
    private NavigationBean navigationBean;

    private User user = new User();

    public String addUser() throws UserServiceException {
        user.addRole(ROLE_USER);
        userService.addUser(user.getUser());
        return navigationBean.toLogin();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public NavigationBean getNavigationBean() {
        return navigationBean;
    }

    public void setNavigationBean(NavigationBean navigationBean) {
        this.navigationBean = navigationBean;
    }
}
