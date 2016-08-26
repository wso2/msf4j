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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a transport session.
 */
public class Session implements Serializable {

    private static final long serialVersionUID = -3945729418329160933L;
    private transient SessionManager sessionManager;

    private String id;
    private long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;
    private boolean isValid = true;
    private boolean isNew = true;
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public Session() {
    }

    public Session(String id, int maxInactiveInterval) {
        this.id = id;
        this.maxInactiveInterval = maxInactiveInterval;
        creationTime = System.currentTimeMillis();
        lastAccessedTime = creationTime;
    }

    long getCreationTime() {
        return creationTime;
    }

    String getId() {
        return id;
    }

    void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public Object getAttribute(String name) {
        checkValidity();
        return attributes.get(name);
    }

    public Set<String> getAttributeNames() {
        checkValidity();
        return attributes.keySet();
    }

    public void setAttribute(String name, Object value) {
        checkValidity();
        attributes.put(name, value);
        sessionManager.updateSession(this);
    }

    public void removeAttribute(String name) {
        checkValidity();
        sessionManager.updateSession(this);
        attributes.remove(name);
    }

    private void checkValidity() {
        if (!isValid) {
            throw new IllegalStateException("Session is invalid");
        }
    }

    public void invalidate() {
        sessionManager.invalidateSession(this);
        attributes.clear();
        isValid = false;
    }

    boolean isValid() {
        return isValid;
    }

    boolean isNew() {
        return isNew;
    }

    boolean getIsNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    Session setAccessed() {
        checkValidity();
        lastAccessedTime = System.currentTimeMillis();
        return this;
    }

    long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}

