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

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;

public class MSF4JProjectImporter {
	
	public void importMSF4JProject(MavenProject mavenProject, File pomFile, IProgressMonitor monitor) throws CoreException {
		String operationText;
		if (pomFile.exists()) {
               try {
                   IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
                   MavenModelManager mavenModelManager = MavenPlugin.getMavenModelManager();
                   LocalProjectScanner scanner = new LocalProjectScanner(ResourcesPlugin.getWorkspace().getRoot()
                           .getLocation().toFile(), //
                           mavenProject.getName(), false, mavenModelManager);
                   operationText = "Scanning maven project.";
                   monitor.subTask(operationText);
                   scanner.run(new SubProgressMonitor(monitor, 15));

                   Set<MavenProjectInfo> projectSet = configurationManager.collectProjects(scanner.getProjects());

                   ProjectImportConfiguration configuration = new ProjectImportConfiguration();
                   operationText = "importing maven project.";
                   monitor.subTask(operationText);
                   List<IMavenProjectImportResult> importResults = configurationManager.importProjects(projectSet,
                           configuration, new SubProgressMonitor(monitor, 60));
               } catch (Exception e) {
//                   log.error("Failed to import project using m2e. Now attempting a normal import.", e);
               }
           } else {
           }
	}

}
