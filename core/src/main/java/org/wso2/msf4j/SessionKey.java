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

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a key for a specific MSF4J session.
 */
public class SessionKey implements Serializable {

    private static final long serialVersionUID = -687991492884005033L;
    private static final String SESSION_ID_SEPARATOR = "-";

    private String serviceKey;
    private String sessionId;

    public SessionKey(String sessionKey) {
        String[] spliced = sessionKey.split(SESSION_ID_SEPARATOR);
        if (spliced.length != 2) {
            throw new IllegalArgumentException("Invalid session key. Session key should consist of a micro-service id" +
                    " and a session id separated by a " + SESSION_ID_SEPARATOR);
        }
        this.serviceKey = spliced[0];
        this.sessionId = spliced[1];
    }

    public SessionKey(String serviceKey, String sessionId) {
        this.serviceKey = serviceKey;
        this.sessionId = sessionId;
    }

    /**
     * Get the service key.
     *
     * @return service key
     */
    public String getServiceKey() {
        return serviceKey;
    }

    /**
     * Get the session id.
     *
     * @return session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(serviceKey, sessionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SessionKey)) {
            return false;
        }
        final SessionKey other = (SessionKey) obj;
        return this.sessionId.equals(other.sessionId) && this.serviceKey.equals(other.serviceKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return serviceKey + SESSION_ID_SEPARATOR + sessionId;
    }
}
