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

package org.wso2.developerstudio.mss.artifact.project.nature;

import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.DEFAULT_MAIN_CLASS_PROPERTY_VALUE;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.JAVAX_SERVLET_DEPENDENCY_ARTIFACT_ID;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.JAVAX_SERVLET_DEPENDENCY_GROUP_ID;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.JAVAX_SERVLET_DEPENDENCY_VERSION;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MAVEN_DEPENDENCY_RESOLVER_TAG;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MICRO_SERVICE_MAIN_CLASS_PROPERTY;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MSS_SERVICE_PARENT_ARTIFACT_ID;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MSS_SERVICE_PARENT_GROUP_ID;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MSS_SERVICE_PARENT_VERSION;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.POM_FILE;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.SWAGGER_ANNOTATIONS_DEPENDENCY_ARTIFACT_ID;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.SWAGGER_ANNOTATIONS_DEPENDENCY_GROUP_ID;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.SWAGGER_ANNOTATIONS_DEPENDENCY_VERSION;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.maven.util.MavenUtils;
import org.wso2.developerstudio.eclipse.platform.core.nature.AbstractWSO2ProjectNature;
import org.wso2.developerstudio.mss.artifact.Activator;
import org.wso2.developerstudio.mss.artifact.util.MSSMavenDependencyResolverJob;

/**
 * Class for represent the nature of a Microservices project inside Eclipse workspace
 */
public class MSSArtifactProjectNature extends AbstractWSO2ProjectNature {

    private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

    @Override
    public void configure() throws CoreException {
        try {
            updatePom(getProject());
        } catch (IOException | XmlPullParserException e) {
            log.error("Error while updating pom.xml file of created Microservices project", e);
            IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "Error while updating pom.xml file of created Microservices project");
            throw new CoreException(status);
        }
    }

    @Override
    public void deconfigure() throws CoreException {

    }

    /**
     * Update created pom.xml file with necessary dependencies and plug-ins so that it works with WSO2 Microservices
     * server
     * @throws IOException
     * @throws XmlPullParserException
     *
     */
    private void updatePom(IProject project) throws IOException, XmlPullParserException {
        File mavenProjectPomLocation = project.getFile(POM_FILE).getLocation().toFile();
        MavenProject mavenProject = MavenUtils.getMavenProject(mavenProjectPomLocation);
        Parent mssServiceParent = new Parent();
        mssServiceParent.setGroupId(MSS_SERVICE_PARENT_GROUP_ID);
        mssServiceParent.setArtifactId(MSS_SERVICE_PARENT_ARTIFACT_ID);
        mssServiceParent.setVersion(MSS_SERVICE_PARENT_VERSION);
        mavenProject.getModel().setParent(mssServiceParent);

        List<Dependency> generatedDependencyList = mavenProject.getModel().getDependencies();
        mavenProject.getModel().removeDependency(generatedDependencyList.get(0));

        Properties generatedProperties = mavenProject.getModel().getProperties();
        generatedProperties.clear();

        mavenProject.getModel().addProperty(MICRO_SERVICE_MAIN_CLASS_PROPERTY, DEFAULT_MAIN_CLASS_PROPERTY_VALUE);
        Dependency servletDependency = new Dependency();
        servletDependency.setGroupId(JAVAX_SERVLET_DEPENDENCY_GROUP_ID);
        servletDependency.setArtifactId(JAVAX_SERVLET_DEPENDENCY_ARTIFACT_ID);
        servletDependency.setVersion(JAVAX_SERVLET_DEPENDENCY_VERSION);
        List<Dependency> dependencyList = new ArrayList<>();
        Dependency swaggerAnotationDependency = new Dependency();
        swaggerAnotationDependency.setGroupId(SWAGGER_ANNOTATIONS_DEPENDENCY_GROUP_ID);
        swaggerAnotationDependency.setArtifactId(SWAGGER_ANNOTATIONS_DEPENDENCY_ARTIFACT_ID);
        swaggerAnotationDependency.setVersion(SWAGGER_ANNOTATIONS_DEPENDENCY_VERSION);
        dependencyList.add(servletDependency);
        dependencyList.add(swaggerAnotationDependency);

        // Save updated pom.xml
        MavenUtils.addMavenDependency(mavenProject, dependencyList);
        MavenUtils.saveMavenProject(mavenProject, mavenProjectPomLocation);

        MSSMavenDependencyResolverJob dependencyResolver = new MSSMavenDependencyResolverJob(
                MAVEN_DEPENDENCY_RESOLVER_TAG, project);
        dependencyResolver.schedule();

    }

}
