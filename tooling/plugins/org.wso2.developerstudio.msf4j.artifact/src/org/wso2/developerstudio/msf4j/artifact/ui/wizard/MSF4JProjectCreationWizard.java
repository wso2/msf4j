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

package org.wso2.developerstudio.msf4j.artifact.ui.wizard;

import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.CODE_GENERATION_TASK;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.GEN_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.IMAGE_FILE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.JAVA_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MAIN_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MAVEN2_PROJECT_NATURE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_PROJECT_CREATION_TASK;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_PROJECT_NATURE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.NEW_MSF4J_PROJECT_CREATION_OPTION;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.OK_BUTTON;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.PROCESSING_CONFIGURATION_TASK;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.PROJECT_WIZARD_WINDOW_TITLE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.RESOURCES_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.WEBAPP_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.SRC_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.NOT_FOUND_EXCEPTION_JAVA;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.API_EXCEPTION_JAVA;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.API_ORIGIN_FILTER_JAVA;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.API_RESPONSE_MESSAGE_JAVA;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.API;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.platform.ui.wizard.AbstractWSO2ProjectCreationWizard;
import org.wso2.developerstudio.eclipse.utils.jdt.JavaUtils;
import org.wso2.developerstudio.eclipse.utils.project.ProjectUtils;
import org.wso2.developerstudio.msf4j.artifact.Activator;
import org.wso2.developerstudio.msf4j.artifact.generator.SwaggerToJavaGenerator;
import org.wso2.developerstudio.msf4j.artifact.model.MSF4JProjectModel;
import org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants;
import org.wso2.developerstudio.msf4j.artifact.util.MSF4JDependencyResolverJob;
import org.wso2.developerstudio.msf4j.artifact.util.MSF4JImageUtils;
import org.wso2.developerstudio.msf4j.artifact.util.MSF4JProjectImporter;

/**
 * Class for creating MSF4J Server project
 */
public class MSF4JProjectCreationWizard extends AbstractWSO2ProjectCreationWizard {

	private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

	private MSF4JProjectModel msf4jArtifactModel;
	private File newJavaFolder;

	public MSF4JProjectCreationWizard() {
		setMsf4JModel(new MSF4JProjectModel());
		setModel(getMsf4jModel());
		setWindowTitle(PROJECT_WIZARD_WINDOW_TITLE);
		setDefaultPageImageDescriptor(MSF4JImageUtils.getInstance().getImageDescriptor(IMAGE_FILE));
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
	}

	@Override
	public boolean performFinish() {

		try {
			if (getModel().getSelectedOption().equals(NEW_MSF4J_PROJECT_CREATION_OPTION)) {

				// Creating new Eclipse project
				IProject project = createNewProject();
				msf4jArtifactModel.setCreatedProjectFile(project.getLocation().toOSString());
				msf4jArtifactModel.setCreatedProjectN(project.getName());
				newJavaFolder = new File(project.getLocation().toOSString());
				project.delete(true, new NullProgressMonitor());
				newJavaFolder.mkdir();
				msf4jArtifactModel.setProjectFolder(newJavaFolder);

				msf4jArtifactModel.setGeneratedCodeLocation(newJavaFolder.getAbsolutePath());
				if (msf4jArtifactModel.getMsf4jVersion() != null) {
					MSF4JArtifactConstants.setMSF4JServiceParentVersion(msf4jArtifactModel.getMsf4jVersion());
				}
				ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(getShell());
				progressMonitorDialog.create();
				progressMonitorDialog.open();
				progressMonitorDialog.run(false, false, new CodegenJob());

			} else {
				log.error("Unsupported MSF4J project creation option" + getModel().getSelectedOption());
				MessageDialog errorDialog = new MessageDialog(getShell(), "Error", null,
						"Unsupported Microserices project creation option", MessageDialog.ERROR,
						new String[] { OK_BUTTON }, 0);
				errorDialog.open();
				return false;
			}
		} catch (CoreException | InvocationTargetException | InterruptedException e) {
			log.error("Error while creating MSF4J project for given Swagger API", e);
			MessageDialog errorDialog = new MessageDialog(getShell(), "Error", null,
					"Error while creating MSF4J project for given Swagger API", MessageDialog.ERROR,
					new String[] { OK_BUTTON }, 0);
			errorDialog.open();
			return false;
		}
		return true;
	}

	@Override
	public IResource getCreatedResource() {
		return null;
	}

	/**
	 * Class responsible for running Codegen job for creation of JAX-RS services
	 */
	private class CodegenJob implements IRunnableWithProgress {

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			String operationText = MSF4J_PROJECT_CREATION_TASK;
			monitor.beginTask(operationText, 100);
			monitor.subTask(PROCESSING_CONFIGURATION_TASK);
			monitor.worked(10);
			monitor.subTask(CODE_GENERATION_TASK);
			monitor.worked(75);

			// Generating MSF4J JAX-RS source code from given
			// Swagger API definition
			String swaggerFilePath = msf4jArtifactModel.getSwaggerFile().getAbsolutePath();
			SwaggerToJavaGenerator sourceGenerator = new SwaggerToJavaGenerator(swaggerFilePath,
					msf4jArtifactModel.getGeneratedCodeLocation(), msf4jArtifactModel.getPackageName());
			sourceGenerator.setGroupId(msf4jArtifactModel.getMavenInfo().getGroupId());
			sourceGenerator.setArtifactId(msf4jArtifactModel.getMavenInfo().getArtifactId());
			sourceGenerator.setArtifactVersion(msf4jArtifactModel.getMavenInfo().getVersion());
			sourceGenerator.generateService();

			MSF4JDependencyResolverJob msf4jDependencyResolverJob = new MSF4JDependencyResolverJob(
					"msf4jDependencyResolverJob", msf4jArtifactModel);
			msf4jDependencyResolverJob.schedule();

		}

	}

	public MSF4JProjectModel getMsf4jModel() {
		return msf4jArtifactModel;
	}

	public void setMsf4JModel(MSF4JProjectModel msf4jModel) {
		this.msf4jArtifactModel = msf4jModel;
	}

}
