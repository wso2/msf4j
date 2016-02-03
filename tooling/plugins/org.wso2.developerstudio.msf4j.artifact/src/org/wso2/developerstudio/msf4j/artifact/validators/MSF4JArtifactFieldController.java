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

package org.wso2.developerstudio.msf4j.artifact.validators;

import java.io.File;

import org.wso2.developerstudio.eclipse.platform.core.exception.FieldValidationException;
import org.wso2.developerstudio.eclipse.platform.core.model.AbstractFieldController;
import org.wso2.developerstudio.eclipse.platform.core.project.model.ProjectDataModel;
import org.wso2.developerstudio.eclipse.platform.ui.validator.CommonFieldValidator;

public class MSF4JArtifactFieldController extends AbstractFieldController {

	@Override
	public void validate(String modelProperty, Object value, ProjectDataModel model) throws FieldValidationException {
		if (modelProperty.equals("project.name")) {
			CommonFieldValidator.validateProjectField(value);
		} else if (modelProperty.equals("service.class.package.name")) {
			CommonFieldValidator.validateJavaPackageNameField(value);
		} else if (modelProperty.equals("swagger.file")) {
			if (value == null) {
				throw new FieldValidationException("Specified Swagger file location is invalid");
			}
			File importFile = (File) value;
			if (!importFile.exists()) {
				throw new FieldValidationException("Specified Swagger file doesn't exist");
			}
		}
	}

}
