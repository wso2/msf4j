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
package org.wso2.developerstudio.msf4j.artifact.util;

import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.DEPENDENCY_RESOLVING_TASK;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.ERROR_TAG;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.EXECUTING_MAVEN_REQUEST_TASK;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.MAVEN_ECLIPSE_ECLIPSE_GOAL;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.OK_BUTTON;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.POM_FILE_PREFIX;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.PROJECT_REFRESH_TASK;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.READING_POM_TASK;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.SETTING_MAVEN_INVOKER_TASK;
import static org.wso2.developerstudio.msf4j.artifact.util.MSF4JArtifactConstants.XML_EXTENTION;

import java.io.File;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.utils.file.FileUtils;
import org.wso2.developerstudio.msf4j.artifact.Activator;

/**
 * This class extended from {@link Job} class and handles the maven dependency resolving operation of given maven
 * project
 *
 */
public class MSF4JMavenDependencyResolverJob extends Job {

    private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

    private IProject project;

    public MSF4JMavenDependencyResolverJob(String name, IProject project) {
        super(name);
        this.project = project;
        setUser(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        String operationText = DEPENDENCY_RESOLVING_TASK;
        IStatus status = Status.OK_STATUS;
        try {
            String mavenHome = getMavenHome();
            monitor.beginTask(operationText, 100);
            monitor.subTask(READING_POM_TASK);
            monitor.worked(10);
            InvocationRequest request = new DefaultInvocationRequest();
            File pomFile = FileUtils.getMatchingFiles(project.getLocation().toOSString(), POM_FILE_PREFIX,
                    XML_EXTENTION)[0];
            request.setPomFile(pomFile);
            request.setGoals(Collections.singletonList(MAVEN_ECLIPSE_ECLIPSE_GOAL));
            monitor.subTask(SETTING_MAVEN_INVOKER_TASK);
            monitor.worked(25);
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(new File(mavenHome));
            monitor.subTask(EXECUTING_MAVEN_REQUEST_TASK);
            monitor.worked(70);
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new MavenInvocationException("Maven Invoker was unable to execute the request");
            }
            monitor.subTask(PROJECT_REFRESH_TASK);
            monitor.worked(90);
        } catch (MavenInvocationException e) {
            log.error("Error while resolving dependencies from the project pom file", e);
            status = new Status(0, Activator.PLUGIN_ID,
                    "Error occured while resolving dependencies of the file in eclipse.\n"
                            + " Please execute \"mvn eclipse:eclipse\" command in the generated project "
                            + "folder to resolve dependencies.", e);
        } catch (NoSuchFieldException e) {
            log.error("Maven Home system variable is not set as a environment variable");
            status = new Status(0, Activator.PLUGIN_ID, "Maven Home system variable is not set."
                    + " Please execute \"mvn eclipse:eclipse\" command in the generated project "
                    + "folder to resolve dependencies.");
        }

        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } catch (CoreException e) {
            log.error("Error while refreshing the project", e);
        } finally {
            monitor.done();
        }
        if (status != Status.OK_STATUS) {
            final String message = status.getMessage();
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                    MessageDialog errorDialog = new MessageDialog(shell, ERROR_TAG, null, message, MessageDialog.ERROR,
                            new String[] { OK_BUTTON }, 0);
                    errorDialog.open();
                }
            });
        }
        return status;
    }

    public static String getMavenHome() throws NoSuchFieldException {
        if (System.getenv("M2_HOME") != null) {
            return System.getenv("M2_HOME");
        } else if (System.getenv("MAVEN_HOME") != null) {
            return System.getenv("MAVEN_HOME");
        } else if (System.getenv("M3_HOME") != null) {
            return System.getenv("M3_HOME");
        } else if (System.getProperty("maven.home") != null) {
            return System.getProperty("maven.home");
        } else {
            log.error("Maven Home variable value is not found in system properties or in environment variable list");
            throw new NoSuchFieldException("Maven Home variable is not set as a environment variable");
        }
    }

}
