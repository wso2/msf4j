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
    public PersistentSessionManager() {
        File file = new File(".tmp");
        if (!file.exists() && !file.mkdirs()) {
            throw new IllegalStateException("Cannot create .tmp directory");
        }
    }

    @Override
    public void loadSessions(Map<String, Session> sessions) {
        String path = Paths.get(".tmp").toString();
        if (!new File(path).exists()) {
            return;
        }
        Arrays.stream(new File(path).listFiles()).forEach(file -> {
            Session session = readSession(file.getName());
            sessions.put(session.getId(), session);
        });
    }

    @Override
    public Session readSession(String sessionId) {
        String path = Paths.get(".tmp", sessionId).toString();
        if (!new File(path).exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis);) {

            Session session = (Session) ois.readObject();
            session.setManager(this);
            return session;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot read session " + sessionId, e);
        }
    }

    @Override
    public void saveSession(Session session) {
        try (FileOutputStream fout = new FileOutputStream(Paths.get(".tmp", session.getId()).toString());
             ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            oos.writeObject(session);
            oos.reset();
            oos.flush();
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot save session " + session.getId(), e);
        }
    }

    @Override
    public void deleteSession(Session session) {
        String pathname = Paths.get(".tmp", session.getId()).toString();
        if (!new File(pathname).delete()) {
            throw new IllegalStateException("File " + pathname + " deletion failed");
        }
    }

    @Override
    public void updateSession(Session session) {
        saveSession(session);
    }
}
