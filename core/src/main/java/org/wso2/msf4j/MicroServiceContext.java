/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.msf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold the micro service instance and micro service specific data.
 */
public class MicroServiceContext {

    private String serviceKey;
    private Object service;

    private Map<String, Session> sessions = new ConcurrentHashMap<>();

    public MicroServiceContext(String serviceKey, Object service) {
        this.serviceKey = serviceKey;
        this.service = service;
    }

    /**
     * Get the unique key specific to the service.
     *
     * @return service key
     */
    public String getServiceKey() {
        return serviceKey;
    }

    /**
     * Get the service instance.
     *
     * @return service
     */
    public Object getService() {
        return service;
    }

    /**
     * Get sessions map.
     *
     * @return sessions
     */
    Map<String, Session> getSessions() {
        return sessions;
    }

    /**
     * Get specific session.
     *
     * @param sessionKey session id
     * @return session
     */
    Session getSession(String sessionKey) {
        return sessions.get(sessionKey);
    }

    /**
     * Put session if absent.
     *
     * @param sessionKey session id
     * @param session    session instance
     * @return session
     */
    Session putSession(String sessionKey, Session session) {
        session.setMicroServiceContext(this);
        return sessions.put(sessionKey, session);
    }

    /**
     * Remove session from service context.
     *
     * @param sessionKey session key
     */
    void removeSession(String sessionKey) {
        sessions.remove(sessionKey);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof MicroServiceContext)) {
            return false;
        }
        MicroServiceContext other = (MicroServiceContext) o;
        return Objects.equals(this.getServiceKey(), other.getServiceKey());
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceKey, service);
    }
}
