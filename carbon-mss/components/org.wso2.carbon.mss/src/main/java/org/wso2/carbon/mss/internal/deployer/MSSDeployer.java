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


import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.deployment.Artifact;
import org.wso2.carbon.kernel.deployment.ArtifactType;
import org.wso2.carbon.kernel.deployment.Deployer;
import org.wso2.carbon.kernel.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.mss.internal.MicroservicesRegistry;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of the MSS service deployer. This will be picked by the
 * DeploymentEngine service according to the whiteboard pattern to deploy
 * MSS POJO artifacts.
 */
@Component(
        name = "org.wso2.carbon.mss.internal.deployer.MSSDeployer",
        service = Deployer.class,
        immediate = true
)
@SuppressWarnings("unused")
public class MSSDeployer implements Deployer {
    private static final Logger log = LoggerFactory.getLogger(MSSDeployer.class);
    private static final String DEPLOYMENT_PATH = "file:microservices";
    private URL deploymentLocation;
    private ArtifactType artifactType;
    private HashMap<Object, List<Object>> deployedArtifacts = new HashMap<>();

    /**
     * Activator is required to write the startup
     * resolver related headers correctly.
     */
    @Activate
    protected void start(BundleContext bundleContext) {
    }

    public void init() {
        log.info("MSSDeployer initializing");
        artifactType = new ArtifactType<>("MSS");
        try {
            deploymentLocation = new URL(DEPLOYMENT_PATH);
        } catch (MalformedURLException e) {
            log.error("MSS deployer location error");
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
        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        log.info("Deploying artifact: {}", artifactPath);
        List<Object> resourcesList;
        try {
            resourcesList = new MSSJarProcessor().setArtifact(artifactFile).process().getResourceInstances();
        } catch (MSSJarProcessorException e) {
            throw new CarbonDeploymentException("Error while processing the artifact: " + artifactPath, e);
        }
        if (resourcesList.size() == 0) {
            throw new CarbonDeploymentException("No classes to initialize in artifact: " + artifactPath);
        }
        artifact.setKey(artifactPath);
        deployedArtifacts.put(artifactPath, resourcesList);
        MicroservicesRegistry msRegistry = MicroservicesRegistry.getInstance();
        for (Object resource : resourcesList) {
            msRegistry.addHttpService(resource);
            msRegistry.initService(resource);
        }
        return artifactPath;
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
        MicroservicesRegistry msRegistry = MicroservicesRegistry.getInstance();
        for (Object resource : resourcesList) {
            msRegistry.removeHttpService(resource);
            msRegistry.preDestroyService(resource);
        }
    }

    public Object update(Artifact artifact) throws CarbonDeploymentException {
        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        log.info("Updating artifact: {}", artifactPath);
        undeploy(artifact.getKey());
        deploy(artifact);
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
}
