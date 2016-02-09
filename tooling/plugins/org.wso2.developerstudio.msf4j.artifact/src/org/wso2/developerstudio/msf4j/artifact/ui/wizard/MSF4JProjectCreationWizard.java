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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.wso2.developerstudio.msf4j.artifact.Activator;
import org.wso2.developerstudio.msf4j.artifact.generator.SwaggerToJavaGenerator;
import org.wso2.developerstudio.msf4j.artifact.model.MSF4JProjectModel;
import org.wso2.developerstudio.msf4j.artifact.util.MSF4JImageUtils;

/**
 * Class for creating MSF4J Server project
 */
public class MSF4JProjectCreationWizard extends AbstractWSO2ProjectCreationWizard {

	private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

    private MSF4JProjectModel msf4jArtifactModel;

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
                msf4jArtifactModel.setGeneratedCodeLocation(project.getLocation().toOSString());
                msf4jArtifactModel.setProject(project);

                ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(getShell());
                progressMonitorDialog.create();
                progressMonitorDialog.open();
                progressMonitorDialog.run(false, false, new CodegenJob());
                ProjectUtils.addNatureToProject(project, false, MAVEN2_PROJECT_NATURE);
                ProjectUtils.addNatureToProject(project, false, MSF4J_PROJECT_NATURE);
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
            try {
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

                // Renaming generated folder structure to match with WSO2
                // conventional directory structure
                IFolder resourceFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
                        MAIN_DIRECTORY);
                File resourcePhysicalFolder = resourceFolder.getRawLocation().makeAbsolute().toFile();
                File newResourcePhysicalFolder = new File(resourcePhysicalFolder.getParent() + File.separator
                        + RESOURCES_DIRECTORY);
                resourcePhysicalFolder.renameTo(newResourcePhysicalFolder);

                IFolder sourceFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
                        GEN_DIRECTORY);
                File sourcePhysicalFolder = sourceFolder.getRawLocation().makeAbsolute().toFile();
                File newSourcePhysicalFolder = new File(sourcePhysicalFolder.getParent() + File.separator
                        + MAIN_DIRECTORY);
                sourcePhysicalFolder.renameTo(newSourcePhysicalFolder);

                // Moving src/resources to src/main
                resourceFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
                        RESOURCES_DIRECTORY);
                resourcePhysicalFolder = resourceFolder.getRawLocation().makeAbsolute().toFile();
                sourceFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
                        MAIN_DIRECTORY);
                sourcePhysicalFolder = sourceFolder.getRawLocation().makeAbsolute().toFile();
                FileUtils.moveDirectoryToDirectory(resourcePhysicalFolder, sourcePhysicalFolder, true);

                // Adding Java support to the source folder src/main/java
                IFolder mainFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
                        MAIN_DIRECTORY, JAVA_DIRECTORY);
                JavaUtils.addJavaSupportAndSourceFolder(msf4jArtifactModel.getProject(), mainFolder);
                
                //removing the webapps folder generated by the tool
                IFolder webAppFolder = ProjectUtils.getWorkspaceFolder(msf4jArtifactModel.getProject(), SRC_DIRECTORY,
                        MAIN_DIRECTORY, RESOURCES_DIRECTORY, WEBAPP_DIRECTORY);
                File webAppPhysicalFolder = webAppFolder.getRawLocation().makeAbsolute().toFile();
                if(webAppPhysicalFolder.exists()){
                	 FileUtils.forceDelete(webAppPhysicalFolder);
                }
                
                //removing unnecessary classes generated by the tool
                IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                IProject newMSF4JProject = myWorkspaceRoot.getProject(msf4jArtifactModel.getProject().getName());
                String[] filesToBeDeleted = {NOT_FOUND_EXCEPTION_JAVA, API_ORIGIN_FILTER_JAVA, API_RESPONSE_MESSAGE_JAVA, API_EXCEPTION_JAVA};
                
                for (String fileToBeDeleted : filesToBeDeleted) {
                	IResource originFilterFile = newMSF4JProject.getFile(SRC_DIRECTORY + File.separator + MAIN_DIRECTORY + File.separator + JAVA_DIRECTORY + 
                	                                                     File.separator + msf4jArtifactModel.getPackageName().replace(".", File.separator) + 
                	                                                     File.separator + API + File.separator + fileToBeDeleted);
                	File fileToDelete = originFilterFile.getRawLocation().makeAbsolute().toFile();
                    	if(fileToDelete.exists()){
                    		FileUtils.forceDelete(fileToDelete);
                    	}
                }
                
                //refresh the workspace after deleting files from the file system
                myWorkspaceRoot.refreshLocal(0,new NullProgressMonitor());
            } catch (CoreException | IOException e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    public MSF4JProjectModel getMsf4jModel() {
        return msf4jArtifactModel;
    }

    public void setMsf4JModel(MSF4JProjectModel msf4jModel) {
        this.msf4jArtifactModel = msf4jModel;
    }

}
