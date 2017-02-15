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
import org.wso2.msf4j.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This session manager persists sessions in the local file system.
 */
public class PersistentSessionManager extends AbstractSessionManager {
    private static final Logger log = LoggerFactory.getLogger(PersistentSessionManager.class);
    private static final String SESSION_DIR = ".sessions";

    public PersistentSessionManager(MicroservicesRegistry microservicesRegistry) {
        super(microservicesRegistry);
        File dir = new File(SESSION_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create .tmp directory");
        }
    }

    @Override
    public Map<String, Map<String, Session>> loadSessions() {
        File dir = new File(SESSION_DIR);
        if (!dir.exists()) {
            return null;
        }
        Map<String, Map<String, Session>> sessions = new HashMap<>();
        getMicroservicesRegistry().getHttpServiceContexts().parallelStream().forEach(context -> {
            String path = Paths.get(SESSION_DIR + "/" + context.getService().getClass().getName()).toString();
            File[] fileList = new File(path).listFiles();
            if (fileList != null) {
                Map<String, Session> sessionMap = Arrays.stream(fileList).parallel()
                        .filter(file -> !file.isDirectory())
                        .map(file -> loadSessionIfNotExpired(readSession(file.getName(), context), file, context))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Session::getId, session -> session));
                sessions.put(context.getServiceKey(), sessionMap);
            }
        });
        return sessions;
    }

    @Override
    public Session readSession(String sessionId, MicroServiceContext microServiceContext) {
        String microServiceSessionDir = microServiceContext.getService().getClass().getName();
        String path = Paths.get(SESSION_DIR + "/" + microServiceSessionDir, sessionId).toString();
        if (!new File(path).exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis);) {

            Session session = (Session) ois.readObject();
            session.setManager(this);
            session.setMicroServiceContext(microServiceContext);
            return session;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot read session " + sessionId, e);
        }
    }

    @Override
    public void saveSession(Session session, MicroServiceContext microServiceContext) {
        String microServiceSessionDir = microServiceContext.getService().getClass().getName();
        String directory = SESSION_DIR + "/" + microServiceSessionDir;
        FileUtil.createDirectoryIfNotExists(directory);
        String path = Paths.get(directory, session.getId()).toString();
        try (FileOutputStream fout = new FileOutputStream(path);
             ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            oos.writeObject(session);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save session " + session.getId(), e);
        }
    }

    @Override
    public void deleteSession(Session session, MicroServiceContext microServiceContext) {
        String microServiceSessionDir = microServiceContext.getService().getClass().getName();
        String pathname = Paths.get(SESSION_DIR + "/" + microServiceSessionDir, session.getId()).toString();
        if (!new File(pathname).delete()) {
            throw new IllegalStateException("File " + pathname + " deletion failed");
        }
    }

    @Override
    public void updateSession(Session session, MicroServiceContext microServiceContext) {
        saveSession(session, microServiceContext);
    }

    /**
     * Load session if an only if not expired.
     *
     * @param session             session instance
     * @param file                file of the session
     * @param microServiceContext micro-service context
     * @return added session instance
     */
    private Session loadSessionIfNotExpired(Session session, File file, MicroServiceContext microServiceContext) {
        // Delete session file if expired
        if (System.currentTimeMillis() - session.getLastAccessedTime() >=
                (long) session.getMaxInactiveInterval() * 60L * 1000L && !file.delete()) {
            log.warn("Couldn't delete expired session file " + file.getAbsolutePath());
            return null;
        } else {
            session.setManager(this);
            session.setMicroServiceContext(microServiceContext);
            return microServiceContext.putSession(session.getId(), session);
        }
    }
}
