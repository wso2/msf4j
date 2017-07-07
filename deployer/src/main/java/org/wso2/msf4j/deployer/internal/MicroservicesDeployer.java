/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.deployer.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.MicroservicesRegistry;
import org.wso2.msf4j.deployer.MicroserviceDeploymentException;
import org.wso2.msf4j.deployer.MicroserviceDeploymentUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Path;

/**
 * Implementation of the micro service deployer.
 * This will be picked by the DeploymentEngine service according to the whiteboard pattern to deploy
 * micro service POJO artifacts.
 */
public class MicroservicesDeployer implements Deployer {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesDeployer.class);
    private static final String DEPLOYMENT_PATH = "file:microservices";
    private static final String SUPPORTED_EXTENSIONS[] = {"jar", "zip"};
    private static final String MICROSERVICE_ARTIFACT_TYPE = "microservices";
    private URL deploymentLocation;
    private ArtifactType artifactType;
    private Map<Object, List<Object>> deployedArtifacts = new HashMap<>();

    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("microservice deployer initializing");
        }
        artifactType = new ArtifactType<>(MICROSERVICE_ARTIFACT_TYPE);
        try {
            deploymentLocation = new URL(DEPLOYMENT_PATH);
        } catch (MalformedURLException e) {
            log.error("microservices deployer location error | location: " + DEPLOYMENT_PATH , e);
        }
    }

    /**
     * Deploy and artifact in the netty-http service.
     *
     * @param artifact the artifact to be deployed
     * @return A key to identify the deployed artifact
     * @throws CarbonDeploymentException If deployment fails
     */
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        if (artifact == null || artifact.getFile() == null) {
            throw new CarbonDeploymentException("Deployment artifact cannot be null");
        }

        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        if (isSupportedFile(artifactFile)) {
            log.info("Deploying microservice artifact: {}", artifactPath);
            List<Object> resourcesList;
            try {
                resourcesList = MicroserviceDeploymentUtils.getRourceInstances(artifactFile);
            } catch (MicroserviceDeploymentException e) {
                throw new CarbonDeploymentException("Error while processing the artifact: " + artifactPath, e);
            }
            if (resourcesList.size() == 0) {
                throw new CarbonDeploymentException("No classes to initialize in artifact: " + artifactPath);
            }

            boolean deployed = resourcesList.stream()
                    .filter(resource -> resource instanceof Microservice)
                    .map(resource -> addService((Microservice) resource))
                    .anyMatch(result -> result == Boolean.FALSE);

            if (!deployed) {
                // If one service not deployed correctly, process should retry later.
                artifact.setKey(artifactPath);
                deployedArtifacts.put(artifactPath, resourcesList);
                return artifactPath;
            }
        }
        return null;
    }

    /**
     * Undeploy the artifact with the key from the netty-http service.
     *
     * @param key Key to identify the artifact
     * @throws CarbonDeploymentException If an error occurs while undeploying
     */
    public void undeploy(Object key) throws CarbonDeploymentException {
        log.info("Undeploying artifact: {}", key);
        List<Object> resourcesList = deployedArtifacts.get(key);
        if (resourcesList != null) {
            resourcesList.forEach(resource -> {
                if (resource instanceof Microservice) {
                    removeService((Microservice) resource);
                }
            });
        }
    }

    /**
     * Update the artifact from the netty-http service.
     * @param artifact the artifact to be deployed
     * @return A key to identify the deployed artifact
     * @throws CarbonDeploymentException If update fails
     */
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        if (isSupportedFile(artifactFile)) {
            log.info("Updating artifact: {}", artifactPath);
            undeploy(artifact.getKey());
            deploy(artifact);
        }
        return artifactPath;
    }

    /**
     * @return Artifact deployment location relative to the
     * server dir in repository
     */
    public URL getLocation() {
        return deploymentLocation;
    }

    /**
     * @return Artifact deployment location
     */
    public ArtifactType getArtifactType() {
        return artifactType;
    }


    /**
     * Checks whether the artifact file type is supported in Microservices Deployer.
     * @param file artifact file
     * @return true, if file type is supported, false, if not
     */
    private boolean isSupportedFile(File file) {
        return Arrays
                .stream(SUPPORTED_EXTENSIONS)
                .filter(
                        s -> s.equalsIgnoreCase(getFileExtension(file))
                ).findAny().isPresent();
    }

    /**
     * Returns the extension of the artifact file.
     * @param file artifact file
     * @return file extension
     */
    private String getFileExtension(File file) {
        String extension = "";
        if (file == null) {
            return extension;
        }

        String fileName = file.getName();
        if (file.isFile()) {
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i + 1);
            }
        }
        return extension;
    }

    /**
     * Add micro services instance to micro services registry.
     * @param service object to be added to registry
     */
    private boolean addService(Microservice service) {
        Map<String, MicroservicesRegistry> microservicesRegistries =
                DataHolder.getInstance().getMicroserviceRegistries();
        if (microservicesRegistries.isEmpty()) {
            log.error("Microservice deployment failed. Microservices Registry doesn't exist to register microservice.");
            return false;
        }
        microservicesRegistries.values().forEach(registry -> registry.addService(service));
        log.info("Microservice {} deployed successfully", service.getClass().getName());
        return true;
    }

    /**
     * Remove micro services instance to micro services registry.
     * @param service object to be removed from registry,
     */
    private boolean removeService(Microservice service) {
        Map<String, MicroservicesRegistry> microservicesRegistries =
                DataHolder.getInstance().getMicroserviceRegistries();
        if (microservicesRegistries.isEmpty()) {
            log.error("Microservice removal failed. Microservices Registry doesn't exist to register microservice.");
            return false;
        }

        microservicesRegistries.values().forEach(registry -> {
            registry.removeService(service.getClass().getAnnotation(Path.class).value());
            registry.preDestroyService(service);
        });
        log.info("Microservice {} undeployed successfully", service.getClass().getName());
        return true;
    }
}
