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

import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.CODE_GENERATION_TASK;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.GEN_DIRECTORY;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.IMAGE_FILE;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.JAVA_DIRECTORY;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MAIN_DIRECTORY;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MAVEN2_PROJECT_NATURE;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MICROSERVICES_PROJECT_CREATION_TASK;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.MSS_PROJECT_NATURE;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.NEW_MSS_PROJECT_CREATION_OPTION;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.OK_BUTTON;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.PROCESSING_CONFIGURATION_TASK;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.PROJECT_WIZARD_WINDOW_TITLE;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.RESOURCES_DIRECTORY;
import static org.wso2.developerstudio.mss.artifact.util.MSSArtifactConstants.SRC_DIRECTORY;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.wso2.developerstudio.mss.artifact.util.MSSImageUtils;

/**
 * Class for creating Microservices Server project
 */
public class MSSProjectCreationWizard extends AbstractWSO2ProjectCreationWizard {
    private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

    private MSSProjectModel mssArtifactModel;

    public MSSProjectCreationWizard() {
        setMssModel(new MSSProjectModel());
        setModel(getMssModel());
        setWindowTitle(PROJECT_WIZARD_WINDOW_TITLE);
        setDefaultPageImageDescriptor(MSSImageUtils.getInstance().getImageDescriptor(IMAGE_FILE));
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        super.init(workbench, selection);
    }

    @Override
    public boolean performFinish() {
        try {
            if (getModel().getSelectedOption().equals(NEW_MSS_PROJECT_CREATION_OPTION)) {

                // Creating new Eclipse project
                IProject project = createNewProject();
                mssArtifactModel.setGeneratedCodeLocation(project.getLocation().toOSString());
                mssArtifactModel.setProject(project);

                ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(getShell());
                progressMonitorDialog.create();
                progressMonitorDialog.open();
                progressMonitorDialog.run(false, false, new CodegenJob());
                ProjectUtils.addNatureToProject(project, false, MAVEN2_PROJECT_NATURE);
                ProjectUtils.addNatureToProject(project, false, MSS_PROJECT_NATURE);
            } else {
                log.error("Unsupported Microservices project creation option" + getModel().getSelectedOption());
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

        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            String operationText = MICROSERVICES_PROJECT_CREATION_TASK;
            monitor.beginTask(operationText, 100);
            monitor.subTask(PROCESSING_CONFIGURATION_TASK);
            monitor.worked(10);
            try {
                monitor.subTask(CODE_GENERATION_TASK);
                monitor.worked(75);

                // Generating Microservices JAX-RS source code from given
                // Swagger API definition
                String swaggerFilePath = mssArtifactModel.getSwaggerFile().getAbsolutePath();
                SwaggerToJavaGenerator sourceGenerator = new SwaggerToJavaGenerator(swaggerFilePath,
                        mssArtifactModel.getGeneratedCodeLocation(), mssArtifactModel.getPackageName());
                sourceGenerator.setGroupId(mssArtifactModel.getMavenInfo().getGroupId());
                sourceGenerator.setArtifactId(mssArtifactModel.getMavenInfo().getArtifactId());
                sourceGenerator.setArtifactVersion(mssArtifactModel.getMavenInfo().getVersion());
                sourceGenerator.generateService();

                // Renaming generated folder structure to match with WSO2
                // conventional directory structure
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
