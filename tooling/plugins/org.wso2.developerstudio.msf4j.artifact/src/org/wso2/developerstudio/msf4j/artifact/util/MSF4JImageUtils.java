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

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.wso2.developerstudio.eclipse.utils.ui.ImageUtils;
import org.wso2.developerstudio.msf4j.artifact.Activator;

public class MSF4JImageUtils extends ImageUtils {

	private static ImageUtils instance;

	public static ImageUtils getInstance() {
		if (instance == null) {
			instance = new MSF4JImageUtils();
		}
		return instance;
	}

	public Bundle getBundle() {
		return Platform.getBundle(Activator.PLUGIN_ID);
	}

}
