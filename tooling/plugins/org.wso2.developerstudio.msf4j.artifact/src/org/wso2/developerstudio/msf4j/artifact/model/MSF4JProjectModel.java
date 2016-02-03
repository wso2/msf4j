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

package org.wso2.developerstudio.msf4j.artifact.model;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.wso2.developerstudio.eclipse.platform.core.exception.ObserverFailedException;
import org.wso2.developerstudio.eclipse.platform.core.project.model.ProjectDataModel;

public class MSF4JProjectModel extends ProjectDataModel {
	private File swaggerFile;
	private IProject project;
	private String sourceFolder;
	private String generatedCodeLocation;
	private String packageName;

	public Object getModelPropertyValue(String key) {
		Object modelPropertyValue = super.getModelPropertyValue(key);
		if (modelPropertyValue == null) {
			if (key.equalsIgnoreCase("swagger.file")) {
				modelPropertyValue = getSwaggerFile();
			} else if (key.equalsIgnoreCase("service.class.package.name")) {
				modelPropertyValue = getPackageName();
			}
		}
		return modelPropertyValue;
	}

	public boolean setModelPropertyValue(String key, Object data) throws ObserverFailedException {
		boolean returnValue = super.setModelPropertyValue(key, data);
		if (key.equals("swagger.file")) {
			if (data != null && data instanceof File) {
				setSwaggerFile((File) data);
			}
		} else if (key.equals("service.class.package.name")) {
			setPackageName(data.toString());
		}
		return returnValue;
	}

	public File getSwaggerFile() {
		return swaggerFile;
	}

	public void setSwaggerFile(File swaggerFile) {
		this.swaggerFile = swaggerFile;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public String getSourceFolder() {
		return sourceFolder;
	}

	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public String getGeneratedCodeLocation() {
		return generatedCodeLocation;
	}

	public void setGeneratedCodeLocation(String generatedCodeLocation) {
		this.generatedCodeLocation = generatedCodeLocation;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
