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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.maven.util.MavenUtils;
import org.wso2.developerstudio.eclipse.platform.core.nature.AbstractWSO2ProjectNature;
import org.wso2.developerstudio.mss.artifact.Activator;

/**
 * Class for represent the nature of a Microservices project inside Eclipse workspace
 */
public class MSSArtifactProjectNature extends AbstractWSO2ProjectNature {
	private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

	@Override
	public void configure() throws CoreException {
		try {
			updatePom(getProject());
		} catch (Exception e) {
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
	 * Update created pom.xml file with necessary dependencies and plugins so that it works with WSO2 Microservices
	 * server
	 * 
	 * @throws XmlPullParserException
	 */
	private void updatePom(IProject project) throws Exception {
		File mavenProjectPomLocation = project.getFile("pom.xml").getLocation().toFile();
		MavenProject mavenProject = MavenUtils.getMavenProject(mavenProjectPomLocation);

		// Adding required dependencies
		List<Dependency> dependencyList = new ArrayList<Dependency>();
		Dependency mssDependency = new Dependency();
		mssDependency.setGroupId("org.wso2.carbon.mss");
		mssDependency.setArtifactId("org.wso2.carbon.mss");
		mssDependency.setVersion("1.0.0-SNAPSHOT");
		dependencyList.add(mssDependency);

		Dependency javaxDependency = new Dependency();
		javaxDependency.setGroupId("javax.ws.rs");
		javaxDependency.setArtifactId("javax.ws.rs-api");
		javaxDependency.setVersion("2.0.1");
		dependencyList.add(javaxDependency);

		MavenUtils.addMavenDependency(mavenProject, dependencyList);

		// Adding required plugin
		Plugin plugin = MavenUtils.createPluginEntry(mavenProject, "org.apache.maven.plugins", "maven-shade-plugin",
				"2.4.1", false);
		PluginExecution execution = new PluginExecution();
		plugin.addExecution(execution);

		execution.setPhase("package");
		execution.addGoal("shade");

		Xpp3Dom configurationNode = MavenUtils.createMainConfigurationNode();
		Xpp3Dom filtersNode = MavenUtils.createXpp3Node(configurationNode, "filters");
		Xpp3Dom filterNode = MavenUtils.createXpp3Node(filtersNode, "filter");
		Xpp3Dom artifactNode = MavenUtils.createXpp3Node(filterNode, "artifact");
		artifactNode.setValue("*:*");
		Xpp3Dom excludesNode = MavenUtils.createXpp3Node(filterNode, "excludes");
		Xpp3Dom excludeNodeSF = MavenUtils.createXpp3Node(excludesNode, "exclude");
		excludeNodeSF.setValue("META-INF/*.SF");
		Xpp3Dom excludeNodeDSA = MavenUtils.createXpp3Node(excludesNode, "exclude");
		excludeNodeDSA.setValue("META-INF/*.DSA");
		Xpp3Dom excludeNodeRSA = MavenUtils.createXpp3Node(excludesNode, "exclude");
		excludeNodeRSA.setValue("META-INF/*.RSA");

		Xpp3Dom transformersNode = MavenUtils.createXpp3Node(configurationNode, "transformers");
		Xpp3Dom transformerNode = MavenUtils.createXpp3Node(transformersNode, "transformer");
		transformerNode.setAttribute("implementation",
				"org.apache.maven.plugins.shade.resource.ManifestResourceTransformer");
		Xpp3Dom mainClassNode = MavenUtils.createXpp3Node(transformerNode, "mainClass");
		mainClassNode.setValue("TODO : Update this place with fully qualified service class name");

		execution.setConfiguration(configurationNode);

		// Save updated pom.xml
		MavenUtils.saveMavenProject(mavenProject, mavenProjectPomLocation);
	}

}
