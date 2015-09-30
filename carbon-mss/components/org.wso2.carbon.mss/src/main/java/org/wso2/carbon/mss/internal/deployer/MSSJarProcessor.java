/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mss.internal.deployer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Dynamically initialising a set of objects from a given jar
 * according to the manifest entry 'microservices'
 */
public class MSSJarProcessor {
    private static final Logger log = LoggerFactory.getLogger(MSSJarProcessor.class);
    private static final String MICROSERVICES_MANIFEST_KEY = "microservices";
    private File file;
    private List<Object> resourceInstances = new ArrayList<>();

    /**
     * @param file jar file to be processed
     */
    public MSSJarProcessor setArtifact(File file) {
        this.file = file;
        return this;
    }

    /**
     * Initialize a list of objects from the jar for the specified
     * classes in the jar's manifest file under 'microservices' key
     */
    public MSSJarProcessor process() {
        String jarPath = file.getAbsolutePath();
        final String[] mssClassNames = readMSSManifestEntry(jarPath);

        //Parent class loader is required to provide classes that are outside of the jar
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    URLClassLoader classLoader =
                            new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
                    for (String className : mssClassNames) {
                        try {
                            Class classToLoad = classLoader.loadClass(className);
                            resourceInstances.add(classToLoad.newInstance());
                            log.info("initialized class: " + className);
                        } catch (ClassNotFoundException e) {
                            log.error("Class: " + className + " not found");
                        } catch (InstantiationException e1) {
                            log.error("Failed to initialize class: " + className);
                        } catch (IllegalAccessException e1) {
                            log.error("Failed to access class: " + className);
                        }
                    }
                } catch (MalformedURLException e) {
                    log.error("Path to jar is invalid");
                }
                return null;
            }
        });

        return this;
    }

    /**
     * Extracts the comma separated list of fully qualified class
     * names of the 'microservices' key of the jar's maifest file
     *
     * @param jarPath absolute path to the jar file
     * @return String array of fully qualified class names
     */
    private String[] readMSSManifestEntry(String jarPath) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                log.error("Error retrieving manifest: " + jarPath);
                return new String[0];
            }
            Attributes mainAttributes = manifest.getMainAttributes();
            String mssEntry = mainAttributes.getValue(MICROSERVICES_MANIFEST_KEY);
            if (mssEntry == null) {
                log.error("\'microservices\' manifest entry not found: " + jarPath);
                return new String[0];
            }
            return mssEntry.split("\\s*,\\s*");
        } catch (IOException e) {
            log.error("Error retrieving manifest: " + jarPath);
            return new String[0];
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    log.warn("Cannot close jar file: " + jarPath, e);
                }
            }
        }
    }

    /**
     * Return the initialized objects
     *
     * @return List of initialized objects
     */
    public List<Object> getResourceInstances() {
        return resourceInstances;
    }
}
