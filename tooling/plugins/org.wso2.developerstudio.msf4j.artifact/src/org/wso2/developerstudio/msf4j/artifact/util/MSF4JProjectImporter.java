/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.developerstudio.msf4j.artifact.util;

import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.DEFAULT_MAIN_CLASS_PROPERTY_VALUE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.ERROR_TAG;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_MAIN_CLASS_PROPERTY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_SERVICE_PARENT_ARTIFACT_ID;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_SERVICE_PARENT_GROUP_ID;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.OK_BUTTON;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.POM_FILE;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.wso2.developerstudio.eclipse.maven.util.MavenUtils;
import org.wso2.developerstudio.msf4j.artifact.model.MSF4JProjectModel;

public class MSF4JProjectImporter {

	public void importMSF4JProject(MSF4JProjectModel msf4jProjectModel, String projectName, File pomFile,
			IProgressMonitor monitor) throws CoreException {
		String operationText;
		Set<MavenProjectInfo> projectSet = null;
		if (pomFile.exists()) {

			IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
			MavenModelManager mavenModelManager = MavenPlugin.getMavenModelManager();
			LocalProjectScanner scanner = new LocalProjectScanner(
					ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(), //
					projectName, false, mavenModelManager);
			operationText = "Scanning maven project.";
			monitor.subTask(operationText);
			try {
				scanner.run(new SubProgressMonitor(monitor, 15));
				projectSet = configurationManager.collectProjects(scanner.getProjects());
				for (MavenProjectInfo projectInfo : projectSet) {
					if (projectInfo != null) {
						saveMavenParentInfo(projectInfo);
					}
				}
				ProjectImportConfiguration configuration = new ProjectImportConfiguration();
				operationText = "importing maven project.";
				monitor.subTask(operationText);
				if (projectSet != null && !projectSet.isEmpty()) {
					List<IMavenProjectImportResult> importResults = configurationManager.importProjects(projectSet,
							configuration, new SubProgressMonitor(monitor, 60));
				}
			} catch (InterruptedException | IOException | XmlPullParserException e) {
				Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				MessageDialog errorDialog = new MessageDialog(shell, ERROR_TAG, null,
						"Unable to import the project, Error occurred while importing the generated project.",
						MessageDialog.ERROR, new String[] { OK_BUTTON }, 0);
				errorDialog.open();
			}

		} else {
		}
	}

	private void saveMavenParentInfo(MavenProjectInfo projectInfo) throws IOException, XmlPullParserException {
		File mavenProjectPomLocation = projectInfo.getPomFile();// project.getFile(POM_FILE).getLocation().toFile();
		MavenProject mavenProject = null;
		mavenProject = MavenUtils.getMavenProject(mavenProjectPomLocation);
		Parent msf4jParent = new Parent();
		msf4jParent.setGroupId(MSF4J_SERVICE_PARENT_GROUP_ID);
		msf4jParent.setArtifactId(MSF4J_SERVICE_PARENT_ARTIFACT_ID);
		msf4jParent.setVersion(MSF4JArtifactConstants.getMSF4JServiceParentVersion());
		mavenProject.getModel().setParent(msf4jParent);

		Properties generatedProperties = mavenProject.getModel().getProperties();
		generatedProperties.clear();

		mavenProject.getModel().addProperty(MSF4J_MAIN_CLASS_PROPERTY, DEFAULT_MAIN_CLASS_PROPERTY_VALUE);
		MavenUtils.saveMavenProject(mavenProject, mavenProjectPomLocation);
	}

}
