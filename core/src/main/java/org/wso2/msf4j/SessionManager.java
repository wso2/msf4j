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

import java.util.Map;

/**
 * Manage transport sessions.
 */
public interface SessionManager {

    /**
     * Initialize the SessionManager.
     */
    void init();

    /**
     * Retrieve a session with ID.
     *
     * @param sessionId ID of the session
     * @return Session with id <code>sessionId</code> if it exists, and null otherwise.
     */
    Session getSession(String sessionId);

    /**
     * Create a new session.
     *
     * @return the newly created session
     */
    Session createSession();

    /**
     * Invalidate a session.
     *
     * @param session The session to be invalidated.
     */
    void invalidateSession(Session session);

    /**
     * The maximum time in minutes a session could be inactive before it is expired.
     *
     * @return max inactive interval
     */
    int getDefaultMaxInactiveInterval();

    /**
     * The maximum number of active sessions that can exist.
     *
     * @return max active sessions
     */
    int getDefaultMaxActiveSessions();

    /**
     * @return Length of the session ID in bytes.
     */
    int getSessionIdLength();

    /**
     * Load sessions from a persistent storage, for example, into memory.
     *
     * @param sessions The map into which the sessions are to be loaded.
     */
    void loadSessions(Map<String, Session> sessions);

    /**
     * Read a session from a persistent storage, for example, into memory.
     *
     * @param sessionId ID of the session to be loaded or read.
     * @return The Session object.
     */
    Session readSession(String sessionId);

    /**
     * Persist a new session.
     *
     * @param session the new session to be persisted.
     */
    void saveSession(Session session);

    /**
     * Delete a session which has been persisted.
     *
     * @param session Session to be deleted
     */
    void deleteSession(Session session);


    /**
     * Update a session in persistent storage.
     *
     * @param session Session to to be updated
     */
    void updateSession(Session session);

    /**
     * Stop this SessionManager.
     */
    void stop();
}
