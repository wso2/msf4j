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

package org.wso2.developerstudio.msf4j.artifact.project.nature;

import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.DEFAULT_MAIN_CLASS_PROPERTY_VALUE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MAVEN_DEPENDENCY_RESOLVER_TAG;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_MAIN_CLASS_PROPERTY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_SERVICE_PARENT_ARTIFACT_ID;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_SERVICE_PARENT_GROUP_ID;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.POM_FILE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.POM_FILE_PREFIX;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.XML_EXTENTION;

import java.io.File;
import java.io.IOException;
//import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.maven.util.MavenUtils;
import org.wso2.developerstudio.eclipse.platform.core.nature.AbstractWSO2ProjectNature;
import org.wso2.developerstudio.eclipse.utils.file.FileUtils;
import org.wso2.developerstudio.msf4j.artifact.Activator;
import org.wso2.developerstudio.msf4j.artifact.model.MSF4JProjectModel;
import org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants;
import org.wso2.developerstudio.msf4j.artifact.util.MSF4JMavenDependencyResolverJob;
import org.wso2.developerstudio.msf4j.artifact.util.MSF4JProjectImporter;

/**
 * Class for represent the nature of a Microservices project inside Eclipse workspace
 */
public class MSF4JArtifactProjectNature extends AbstractWSO2ProjectNature {

    private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

    @Override
    public void configure() throws CoreException {
        try {
            updatePom(getProject());
        } catch (IOException | XmlPullParserException e) {
            log.error("Error while updating pom.xml file of created MSF4J project", e);
            IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "Error while updating pom.xml file of created MSF4J project");
            throw new CoreException(status);
        }
    }

    @Override
    public void deconfigure() throws CoreException {

    }

    /**
     * Update created pom.xml file with necessary dependencies and plug-ins so that it works with WSO2 MSF4J
     * server
     * @throws IOException
     * @throws XmlPullParserException
     *
     */
    private void updatePom(IProject project) throws IOException, XmlPullParserException {
        File mavenProjectPomLocation = project.getFile(POM_FILE).getLocation().toFile();
        MavenProject mavenProject = MavenUtils.getMavenProject(mavenProjectPomLocation);
        Parent msf4jParent = new Parent();
        msf4jParent.setGroupId(MSF4J_SERVICE_PARENT_GROUP_ID);
        msf4jParent.setArtifactId(MSF4J_SERVICE_PARENT_ARTIFACT_ID);
        msf4jParent.setVersion(MSF4JArtifactConstants.getMSF4JServiceParentVersion());
        mavenProject.getModel().setParent(msf4jParent);

        Properties generatedProperties = mavenProject.getModel().getProperties();
        generatedProperties.clear();

        mavenProject.getModel().addProperty(MSF4J_MAIN_CLASS_PROPERTY, DEFAULT_MAIN_CLASS_PROPERTY_VALUE);
        MavenUtils.saveMavenProject(mavenProject, mavenProjectPomLocation);

        MSF4JMavenDependencyResolverJob dependencyResolver = new MSF4JMavenDependencyResolverJob(
                MAVEN_DEPENDENCY_RESOLVER_TAG, project);
      /*  File pomFile = FileUtils.getMatchingFiles(project.getLocation().toOSString(), POM_FILE_PREFIX,
                XML_EXTENTION)[0];
        MSF4JProjectImporter msf4jProjectImporter = new MSF4JProjectImporter();
        try {
			msf4jProjectImporter.importMSF4JProject(mavenProject, pomFile, new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
		}*/
        dependencyResolver.schedule();

    }

}
