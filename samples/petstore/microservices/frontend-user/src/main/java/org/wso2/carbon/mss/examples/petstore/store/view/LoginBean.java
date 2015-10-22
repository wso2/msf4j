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

import org.wso2.carbon.mss.examples.petstore.store.dao.UserService;
import org.wso2.carbon.mss.examples.petstore.store.model.UserServiceException;

import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * Bean classes used for JSF model.
 */
@ManagedBean
@SessionScoped
public class LoginBean {

    public static final String X_JWT_ASSERTION = "X-JWT-Assertion";
    private String username;
    private String password;
    private String token;
    private boolean loggedIn;

    @Nullable
    @ManagedProperty("#{navigationBean}")
    private NavigationBean navigationBean;

    @Nullable
    @ManagedProperty("#{userService}")
    private UserService userService;

    public String doLogin() {
        try {
            token = userService.authenticate(username, password);
            loggedIn = true;
            password = null;
            return navigationBean.redirectToWelcome();
        } catch (UserServiceException e) {
            FacesMessage msg = new FacesMessage("Login error!", "ERROR MSG");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return navigationBean.toLogin();
        }
    }

    public String doLogout() {
        loggedIn = false;
        FacesMessage msg = new FacesMessage("Logout success!", "INFO MSG");
        msg.setSeverity(FacesMessage.SEVERITY_INFO);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).invalidate();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return navigationBean.toLogin();
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public NavigationBean getNavigationBean() {
        return navigationBean;
    }

    public void setNavigationBean(NavigationBean navigationBean) {
        this.navigationBean = navigationBean;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
