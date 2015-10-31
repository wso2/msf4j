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

package org.wso2.carbon.mss.example.hook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Demonstrate usage of  AbstractBasicAuthHook, just check username and password are equal or not.
 */
public class UsernamePasswordSecurityInterceptor extends AbstractBasicAuthInterceptor {

    private final Log log = LogFactory.getLog(UsernamePasswordSecurityInterceptor.class);

    @Override
    protected boolean authenticate(String username, String password) {

        // The authentication logic goes in here. For simplicity, we just check that username == password
        if (username.equals(password)) {
            return true;
        }
        return false;
    }
}
