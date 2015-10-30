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

package org.wso2.developerstudio.mss.artifact.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.wso2.developerstudio.mss.artifact.Activator;
import org.wso2.developerstudio.mss.artifact.generator.SwaggerToJavaGenerator;
import org.wso2.developerstudio.mss.artifact.model.MSSProjectModel;
import org.wso2.developerstudio.mss.artifact.util.LibraryUtils;
import org.wso2.developerstudio.mss.artifact.util.MSSImageUtils;

/**
 * Class for creating Microservices Server project
 */
public class MSSProjectCreationWizard extends AbstractWSO2ProjectCreationWizard {
	private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

	private static final String PROJECT_WIZARD_WINDOW_TITLE = "New Microservices Project";
	private static final String MSS_PROJECT_NATURE = "org.wso2.developerstudio.eclipse.mss.project.nature";
	private static final String OK_BUTTON = "Ok";

	private MSSProjectModel mssArtifactModel;

	public MSSProjectCreationWizard() {
		setMssModel(new MSSProjectModel());
		setModel(getMssModel());
		setWindowTitle(PROJECT_WIZARD_WINDOW_TITLE);
		setDefaultPageImageDescriptor(MSSImageUtils.getInstance().getImageDescriptor("mss-wizard.png"));
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
	}

	@Override
	public boolean performFinish() {
		try {
			if (getModel().getSelectedOption().equals("new.MSS")) {
				// Creating new Eclipse project
				IProject project = createNewProject();
				mssArtifactModel.setGeneratedCodeLocation(project.getLocation().toOSString());
				mssArtifactModel.setProject(project);

				ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(getShell());
				progressMonitorDialog.create();
				progressMonitorDialog.open();
				progressMonitorDialog.run(false, false, new CodegenJob());

				// Adding Microservices project nature to created project
				ProjectUtils.addNatureToProject(project, false, MSS_PROJECT_NATURE);
				// Sync physical location with Eclipse workspace
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} else {
				log.error("Unsupported Microserices project creation option");
				MessageDialog errorDialog = new MessageDialog(getShell(), "Error", null,
						"Unsupported Microserices project creation option", MessageDialog.ERROR,
						new String[] { OK_BUTTON }, 0);
				errorDialog.open();
				return false;
			}
		} catch (CoreException | InvocationTargetException | InterruptedException e) {
			log.error("Error while creating Microservices project for given Swagger API", e);
			MessageDialog errorDialog = new MessageDialog(getShell(), "Error", null,
					"Error while creating Microservices project for given Swagger API", MessageDialog.ERROR,
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
		private static final String GEN_DIRECTORY = "gen";
		private static final String RESOURCES_DIRECTORY = "resources";
		private static final String JAVA_DIRECTORY = "java";
		private static final String MAIN_DIRECTORY = "main";
		private static final String SRC_DIRECTORY = "src";
		private static final String SWAGGER_ANNOTATIONS_JAR = "swagger-annotations-1.5.0.jar";
		private static final String JACKSON_ANNOTATIONS_JAR = "jackson-annotations-2.6.3.jar";
		private static final String JAVAX_SERVLET_API_JAR = "javax.servlet-api-4.0.0-b01.jar";
		private static final String JERSEY_MULTIPART_JAR = "jersey-multipart-1.19.jar";
		private static final String JERSEY_BUNDLE_JAR = "jersey-bundle-1.19.jar";
		private static final String JAVAX_WS_RS_API_JAR = "javax.ws.rs-api-2.0.1.jar";

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			String operationText = "Creating Microservices Prject...";
			monitor.beginTask(operationText, 100);
			monitor.subTask("Processing configuration...");
			monitor.worked(10);
			try {
				monitor.subTask("Generating code...");
				monitor.worked(75);
				monitor.subTask("Adding dependent libraries to the project...");

				// Generating Microservices JAX-RS source code from given Swagger API definition
				String swaggerFilePath = mssArtifactModel.getSwaggerFile().getAbsolutePath();
				SwaggerToJavaGenerator sourceGenerator = new SwaggerToJavaGenerator(swaggerFilePath,
						mssArtifactModel.getGeneratedCodeLocation(), mssArtifactModel.getPackageName());
				sourceGenerator.setGroupId(mssArtifactModel.getMavenInfo().getGroupId());
				sourceGenerator.setArtifactId(mssArtifactModel.getMavenInfo().getArtifactId());
				sourceGenerator.setArtifactVersion(mssArtifactModel.getMavenInfo().getVersion());
				sourceGenerator.generateService();

				// Renaming generated folder structure to match with WSO2 conventional directory structure
				IFolder resourceFolder = ProjectUtils.getWorkspaceFolder(mssArtifactModel.getProject(), SRC_DIRECTORY,
						MAIN_DIRECTORY);
				File resourcePhysicalFolder = resourceFolder.getRawLocation().makeAbsolute().toFile();
				File newResourcePhysicalFolder = new File(resourcePhysicalFolder.getParent() + File.separator
						+ RESOURCES_DIRECTORY);
				resourcePhysicalFolder.renameTo(newResourcePhysicalFolder);

				IFolder sourceFolder = ProjectUtils.getWorkspaceFolder(mssArtifactModel.getProject(), SRC_DIRECTORY,
						GEN_DIRECTORY);
				File sourcePhysicalFolder = sourceFolder.getRawLocation().makeAbsolute().toFile();
				File newSourcePhysicalFolder = new File(sourcePhysicalFolder.getParent() + File.separator
						+ MAIN_DIRECTORY);
				sourcePhysicalFolder.renameTo(newSourcePhysicalFolder);

				// Moving src/resources to src/main
				resourceFolder = ProjectUtils.getWorkspaceFolder(mssArtifactModel.getProject(), SRC_DIRECTORY,
						RESOURCES_DIRECTORY);
				resourcePhysicalFolder = resourceFolder.getRawLocation().makeAbsolute().toFile();
				sourceFolder = ProjectUtils.getWorkspaceFolder(mssArtifactModel.getProject(), SRC_DIRECTORY,
						MAIN_DIRECTORY);
				sourcePhysicalFolder = sourceFolder.getRawLocation().makeAbsolute().toFile();
				FileUtils.moveDirectoryToDirectory(resourcePhysicalFolder, sourcePhysicalFolder, true);

				// Adding Java support to the source folder src/main/java
				IFolder mainFolder = ProjectUtils.getWorkspaceFolder(mssArtifactModel.getProject(), SRC_DIRECTORY,
						MAIN_DIRECTORY, JAVA_DIRECTORY);
				JavaUtils.addJavaSupportAndSourceFolder(mssArtifactModel.getProject(), mainFolder);

				// Adding required dependencies to created Microservices project
				JavaUtils.addJarLibraryToProject(mssArtifactModel.getProject(),
						LibraryUtils.getDependencyPath(JAVAX_WS_RS_API_JAR));
				JavaUtils.addJarLibraryToProject(mssArtifactModel.getProject(),
						LibraryUtils.getDependencyPath(JERSEY_BUNDLE_JAR));
				JavaUtils.addJarLibraryToProject(mssArtifactModel.getProject(),
						LibraryUtils.getDependencyPath(JERSEY_MULTIPART_JAR));
				JavaUtils.addJarLibraryToProject(mssArtifactModel.getProject(),
						LibraryUtils.getDependencyPath(JAVAX_SERVLET_API_JAR));
				JavaUtils.addJarLibraryToProject(mssArtifactModel.getProject(),
						LibraryUtils.getDependencyPath(JACKSON_ANNOTATIONS_JAR));
				JavaUtils.addJarLibraryToProject(mssArtifactModel.getProject(),
						LibraryUtils.getDependencyPath(SWAGGER_ANNOTATIONS_JAR));

				monitor.worked(10);
				monitor.subTask("Refreshing project...");
				mssArtifactModel.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				monitor.worked(5);
				monitor.done();
			} catch (CoreException | IOException e) {
				throw new InvocationTargetException(e);
			}
		}
	}

	public MSSProjectModel getMssModel() {
		return mssArtifactModel;
	}

	public void setMssModel(MSSProjectModel mssModel) {
		this.mssArtifactModel = mssModel;
	}

}
