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

import org.wso2.msf4j.internal.session.SessionIdGenerator;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstract SessionManager implementation which leave the sessions persistence related method to be implemented.
 */
public abstract class AbstractSessionManager implements SessionManager {
    private boolean isStopped;

    /**
     * The default maximum inactive interval, in minutes, for Sessions created by
     * this Manager.
     */
    private static final int DEFAULT_MAX_INACTIVE_INTERVAL = 15;  // In minutes

    /**
     * Max number of sessions that can be active at a given time.
     */
    private static final int DEFAULT_MAX_ACTIVE_SESSIONS = 100_000;

    /**
     * The session id length of Sessions created by this Manager.
     */
    private static final int SESSION_ID_LENGTH = 16;

    private SessionIdGenerator sessionIdGenerator = new SessionIdGenerator();

    private ScheduledExecutorService sessionExpiryChecker;

    private final MicroservicesRegistry microservicesRegistry;

    public AbstractSessionManager(MicroservicesRegistry microservicesRegistry) {
        this.microservicesRegistry = microservicesRegistry;
    }

    @Override
    public final void init() {
        sessionIdGenerator.setSessionIdLength(SESSION_ID_LENGTH);
        Map<String, Map<String, Session>> sessions = loadSessions();

        // Session expiry scheduled task
        sessionExpiryChecker = Executors.newScheduledThreadPool(1);

        if (sessions != null) {
            sessionExpiryChecker.scheduleAtFixedRate(() ->
                    sessions.values().forEach(entry -> entry.values().parallelStream()
                            .filter(session ->
                                    (System.currentTimeMillis() - session.getLastAccessedTime() >=
                                            session.getMaxInactiveInterval() * 60 * 1000))
                            .forEach(Session::invalidate)), 30, 30, TimeUnit.SECONDS);
        }
    }

    @Override
    public final Session getSession(String sessionId, MicroServiceContext microServiceContext) {
        checkValidity();
        Session session = microServiceContext.getSession(sessionId);
        if (session == null) {
            session = readSession(sessionId, microServiceContext);
        }
        if (session != null) {
            session.setNew(false);
            session = microServiceContext.putSession(sessionId, session);
        }
        return session;
    }

    @Override
    public final Session createSession(MicroServiceContext microServiceContext) {
        String sessionId = sessionIdGenerator.generateSessionId("");
        checkValidity();
        if (getSessionCount() >= DEFAULT_MAX_ACTIVE_SESSIONS) {
            throw new IllegalStateException("Too many active sessions");
        }
        Session session = new Session(sessionId, DEFAULT_MAX_INACTIVE_INTERVAL);
        session.setManager(this);
        microServiceContext.putSession(session.getId(), session);
        saveSession(session, microServiceContext);
        return session;
    }

    @Override
    public final void invalidateSession(Session session, MicroServiceContext microServiceContext) {
        checkValidity();
        microServiceContext.removeSession(session.getId());
        deleteSession(session, microServiceContext);
    }

    @Override
    public final int getDefaultMaxInactiveInterval() {
        return DEFAULT_MAX_INACTIVE_INTERVAL;
    }

    @Override
    public final int getDefaultMaxActiveSessions() {
        return DEFAULT_MAX_ACTIVE_SESSIONS;
    }

    @Override
    public final int getSessionIdLength() {
        return SESSION_ID_LENGTH;
    }

    @Override
    public final void stop() {
        sessionExpiryChecker.shutdown();
        isStopped = true;
    }

    protected final void checkValidity() {
        if (isStopped) {
            throw new IllegalStateException("This SessionManager has been stopped");
        }
    }

    protected MicroservicesRegistry getMicroservicesRegistry() {
        return microservicesRegistry;
    }

    /**
     * Get total number of sessions.
     *
     * @return session count
     */
    private int getSessionCount() {
        int count = 0;
        for (MicroServiceContext context : microservicesRegistry.getHttpServiceContexts()) {
            count += context.getSessions().size();
        }
        return count;
    }
}
