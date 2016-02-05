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

import java.util.ArrayList;
import java.util.List;

/**
 * User bean class.
 */
public class User {

    private String username;
    private String password;
    private String fullName;
    private String email;
    private List<String> roles = new ArrayList<>();


    public User() {

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        roles.add(role);
    }
    public org.wso2.msf4j.examples.petstore.util.model.User getUser() {
        org.wso2.msf4j.examples.petstore.util.model.User user = new org.wso2.msf4j.examples.petstore.util
                .model.User();
        user.setName(getUsername());
        user.setPassword(getPassword());
        user.setEmail(getEmail());
        user.setRoles(getRoles());
        return user;
    }
}
