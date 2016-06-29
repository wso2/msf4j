package org.wso2.developerstudio.msf4j.artifact.util;

import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.API;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.API_EXCEPTION_JAVA;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.API_ORIGIN_FILTER_JAVA;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.API_RESPONSE_MESSAGE_JAVA;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.GEN_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.JAVA_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MAIN_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MAVEN2_PROJECT_NATURE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MSF4J_PROJECT_NATURE;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.NOT_FOUND_EXCEPTION_JAVA;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.OK_BUTTON;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.RESOURCES_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.SRC_DIRECTORY;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.WEBAPP_DIRECTORY;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.progress.UIJob;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.maven.util.MavenUtils;
import org.wso2.developerstudio.eclipse.utils.jdt.JavaUtils;
import org.wso2.developerstudio.eclipse.utils.project.ProjectUtils;
import org.wso2.developerstudio.msf4j.artifact.Activator;
import org.wso2.developerstudio.msf4j.artifact.model.MSF4JProjectModel;

public class MSF4JDependencyResolverJob extends Job {

	MSF4JProjectModel msf4jArtifactModel;
	private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);
	private static final String PERSPECTIVE_ID = "org.eclipse.ui.articles.perspective.msf4jperspective";

	public MSF4JDependencyResolverJob(String name, MSF4JProjectModel projectModel) {
		super(name);
		this.msf4jArtifactModel = projectModel;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			projectDependencyResolver(monitor);
			final IWorkbench workbench = PlatformUI.getWorkbench();
			new UIJob("Switching perspectives") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						workbench.showPerspective(PERSPECTIVE_ID, workbench.getActiveWorkbenchWindow());
					} catch (WorkbenchException e) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error while switching perspectives", e);
					}
					return Status.OK_STATUS;
				}
			}.run(new NullProgressMonitor());

		} catch (CoreException | IOException e) {
			log.error("error in resolving project dependencies", e);
			Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
			MessageDialog errorDialog = new MessageDialog(shell, "Error", null,
					"Error while creating MSF4J project for given Swagger API", MessageDialog.ERROR,
					new String[] { OK_BUTTON }, 0);
			errorDialog.open();
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	private void projectDependencyResolver(IProgressMonitor monitor)
			throws CoreException, IOException, JavaModelException {
		MSF4JProjectImporter msf4jProjectImporter = new MSF4JProjectImporter();
		File pomFile = new File(msf4jArtifactModel.getProjectFolder().getPath() + File.separator + "pom.xml");
		msf4jProjectImporter.importMSF4JProject(msf4jArtifactModel, msf4jArtifactModel.getCreatedProjectFile(), pomFile,
				monitor);

		IWorkspaceRoot myWorkspaceRoot = resourceAlteration();

		// refresh the workspace after deleting files from the file
		// system
		myWorkspaceRoot.refreshLocal(0, new NullProgressMonitor());
	}

	private IWorkspaceRoot resourceAlteration() throws IOException, CoreException, JavaModelException {
		// Renaming generated folder structure to match with WSO2
		// conventional directory structure
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspace.getProject(msf4jArtifactModel.getProjectName());
		msf4jArtifactModel.setProject(project);
		IFolder resourceFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
				MAIN_DIRECTORY);
		File resourcePhysicalFolder = resourceFolder.getRawLocation().makeAbsolute().toFile();
		File newResourcePhysicalFolder = new File(
				resourcePhysicalFolder.getParent() + File.separator + RESOURCES_DIRECTORY);
		resourcePhysicalFolder.renameTo(newResourcePhysicalFolder);

		IFolder sourceFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
				GEN_DIRECTORY);
		File sourcePhysicalFolder = sourceFolder.getRawLocation().makeAbsolute().toFile();
		File newSourcePhysicalFolder = new File(sourcePhysicalFolder.getParent() + File.separator + MAIN_DIRECTORY);
		sourcePhysicalFolder.renameTo(newSourcePhysicalFolder);

		// Moving src/resources to src/main
		resourceFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
				RESOURCES_DIRECTORY);
		resourcePhysicalFolder = resourceFolder.getRawLocation().makeAbsolute().toFile();
		sourceFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY, MAIN_DIRECTORY);
		sourcePhysicalFolder = sourceFolder.getRawLocation().makeAbsolute().toFile();
		FileUtils.moveDirectoryToDirectory(resourcePhysicalFolder, sourcePhysicalFolder, true);

		// Adding Java support to the source folder src/main/java
		// delete the project target folder
		IFolder targetFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(),
				MSF4JArtifactConstants.TRGET_DIRECTORY);
		targetFolder.delete(true, new NullProgressMonitor());
		IFolder mainFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
				MAIN_DIRECTORY, JAVA_DIRECTORY);
		JavaUtils.addJavaSupportAndSourceFolder(msf4jArtifactModel.getProject(), mainFolder);

		// removing the webapps folder generated by the tool
		IFolder webAppFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
				MAIN_DIRECTORY, RESOURCES_DIRECTORY, WEBAPP_DIRECTORY);
		File webAppPhysicalFolder = webAppFolder.getRawLocation().makeAbsolute().toFile();
		if (webAppPhysicalFolder.exists()) {
			FileUtils.forceDelete(webAppPhysicalFolder);
		}

		// removing unnecessary classes generated by the tool
		IProject newMSF4JProject = workspace.getProject(msf4jArtifactModel.getProject().getName());
		String[] filesToBeDeleted = { NOT_FOUND_EXCEPTION_JAVA, API_ORIGIN_FILTER_JAVA, API_RESPONSE_MESSAGE_JAVA,
				API_EXCEPTION_JAVA };

		for (String fileToBeDeleted : filesToBeDeleted) {
			IResource originFilterFile = newMSF4JProject
					.getFile(SRC_DIRECTORY + File.separator + MAIN_DIRECTORY + File.separator + JAVA_DIRECTORY
							+ File.separator + msf4jArtifactModel.getPackageName().replace(".", File.separator)
							+ File.separator + API + File.separator + fileToBeDeleted);
			File fileToDelete = originFilterFile.getRawLocation().makeAbsolute().toFile();
			if (fileToDelete.exists()) {
				FileUtils.forceDelete(fileToDelete);
			}
		}
		ProjectUtils.addNatureToProject(project, false, MAVEN2_PROJECT_NATURE);
		ProjectUtils.addNatureToProject(project, false, MSF4J_PROJECT_NATURE);
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		return workspace;
	}
}
