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
 * Default SessionManager to manage MSF4J transport sessions.
 */
public class DefaultSessionManager extends AbstractSessionManager {

    @Override
    public void loadSessions(Map<String, Session> sessions) {
        checkValidity();
        // Nothing to do because this is an in-memory implementation
    }

    @Override
    public void saveSession(Session session) {
        checkValidity();
        // Nothing to do because this is an in-memory implementation
    }

    @Override
    public Session readSession(String sessionId) {
        checkValidity();
        // Nothing to do because this is an in-memory implementation
        return null;
    }

    @Override
    public void deleteSession(Session session) {
        checkValidity();
        // Nothing to do because this is an in-memory implementation
    }

    @Override
    public void updateSession(Session session) {
        checkValidity();
        // Nothing to do because this is an in-memory implementation
    }
}
