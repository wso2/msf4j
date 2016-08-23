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

import org.wso2.msf4j.internal.session.SessionManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a transport session.
 */
public class Session {

    private SessionManager manager;
    private String id;
    private long creationTime;
    private Map<String, Object> attributes = new ConcurrentHashMap<>();
    private long lastAccessedTime;
    private int maxInactiveInterval;
    private boolean isValid = true;

    public Session(SessionManager manager, String id, int maxInactiveInterval) {
        this.manager = manager;
        this.id = id;
        this.maxInactiveInterval = maxInactiveInterval;
        creationTime = System.currentTimeMillis();
        lastAccessedTime = creationTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getId() {
        return id;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    Session setAccessed() {
        lastAccessedTime = System.currentTimeMillis();
        return this;
    }

    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void invalidate() {
        manager.invalidateSession(this);
        attributes.clear();
        isValid = false;
    }

    boolean isValid() {
        return isValid;
    }
}

