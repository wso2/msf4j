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
package org.wso2.msf4j.internal.session;

import org.wso2.msf4j.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manage transport sessions.
 */
public class SessionManager {
    /**
     * The default maximum inactive interval, in minutes, for Sessions created by
     * this Manager.
     */
    private static final int DEFAULT_MAX_INACTIVE_INTERVAL = 1;  // In minutes

    /**
     * Max number of sessions that can be active at a given time.
     */
    private static final int DEFAULT_MAX_ACTIVE_SESSIONS = 100000;

    /**
     * The session id length of Sessions created by this Manager.
     */
    private static final int SESSION_ID_LENGTH = 16;

    private Map<String, Session> sessions = new ConcurrentHashMap<>();
    private SessionIdGenerator sessionIdGenerator = new SessionIdGenerator();

    public SessionManager() {
        sessionIdGenerator.setSessionIdLength(SESSION_ID_LENGTH);

        // Session expiry scheduled task
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() ->
                        sessions.values().parallelStream()
                                .filter(session ->
                                        (System.currentTimeMillis() - session.getLastAccessedTime() >=
                                                session.getMaxInactiveInterval() * 60 * 1000))
                                .forEach(Session::invalidate),
                30, 30, TimeUnit.SECONDS);
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public Session createSession() {
        if (sessions.size() >= DEFAULT_MAX_ACTIVE_SESSIONS) {
            throw new IllegalStateException("Too many active sessions");
        }
        Session session = new Session(this, sessionIdGenerator.generateSessionId(""), DEFAULT_MAX_INACTIVE_INTERVAL);
        sessions.put(session.getId(), session);
        return session;
    }

    public void invalidateSession(Session session) {
        sessions.remove(session.getId());
    }
}
