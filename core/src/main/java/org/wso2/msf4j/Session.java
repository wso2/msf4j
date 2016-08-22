/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.msf4j;

import org.wso2.msf4j.internal.SessionManager;

/**
 * Represents a transport session.
 */
public class Session {

    private SessionManager manager;

    Session(SessionManager manager) {

        this.manager = manager;
    }


    public long getCreationTime() {

        return 0;
    }

    public String getId() {

        return null;
    }

    public long getLastAccessedTime() {

        return 0;
    }

    public void setMaxInactiveInterval(int interval) {

    }

    public int getMaxInactiveInterval() {

        return 0;
    }

    public Object getAttribute(String name) {

        return null;
    }

    public java.util.Enumeration<java.lang.String> getAttributeNames() {

        return null;
    }

    public void setAttribute(java.lang.String name, java.lang.Object value) {

    }

    public void removeAttribute(java.lang.String name) {

    }

    public void invalidate() {
       manager.invalidateSession(this);
    }
}

