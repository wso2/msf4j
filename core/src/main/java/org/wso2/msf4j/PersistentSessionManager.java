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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

/**
 * This session manager persists sessions in the local file system.
 */
public class PersistentSessionManager extends AbstractSessionManager {
    private static final Logger log = LoggerFactory.getLogger(PersistentSessionManager.class);
    private static final String SESSION_DIR = ".sessions";

    public PersistentSessionManager() {
        File dir = new File(SESSION_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create .tmp directory");
        }
    }

    @Override
    public void loadSessions(Map<SessionKey, Session> sessions) {
        File dir = new File(SESSION_DIR);
        if (!dir.exists()) {
            return;
        }
        String path = Paths.get(SESSION_DIR).toString();

        Arrays.stream(new File(path).listFiles()).parallel().forEach(file -> {
            SessionKey sessionKey = new SessionKey(file.getName());
            Session session = readSession(sessionKey);

            // Delete expired session files
            if (System.currentTimeMillis() - session.getLastAccessedTime() >=
                    session.getMaxInactiveInterval() * 60 * 1000 && !file.delete()) {
                log.warn("Couldn't delete expired session file " + file.getAbsolutePath());
            } else {
                sessions.put(session.getSessionKey(), session);
            }
        });
    }

    @Override
    public Session readSession(SessionKey sessionKey) {
        String path = Paths.get(SESSION_DIR, sessionKey.toString()).toString();
        if (!new File(path).exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis);) {

            Session session = (Session) ois.readObject();
            session.setManager(this);
            return session;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot read session " + sessionKey, e);
        }
    }

    @Override
    public void saveSession(Session session) {
        try (FileOutputStream fout = new FileOutputStream(Paths.get(SESSION_DIR, session.getSessionKey().toString())
                .toString());
             ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            oos.writeObject(session);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save session " + session.getSessionKey(), e);
        }
    }

    @Override
    public void deleteSession(Session session) {
        String pathname = Paths.get(SESSION_DIR, session.getSessionKey().toString()).toString();
        if (!new File(pathname).delete()) {
            throw new IllegalStateException("File " + pathname + " deletion failed");
        }
    }

    @Override
    public void updateSession(Session session) {
        saveSession(session);
    }
}
