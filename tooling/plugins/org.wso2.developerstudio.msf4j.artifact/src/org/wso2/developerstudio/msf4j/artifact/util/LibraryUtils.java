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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.wso2.developerstudio.eclipse.utils.ide.EclipseUtils;
import org.wso2.developerstudio.msf4j.artifact.Activator;

public class LibraryUtils {

	public static File getDependencyPath(String dependencyName, boolean isRelativePath) {
		String dependencyPath = getLibLocation() + dependencyName;
		URL resource = getResourceURL(dependencyPath);
		return getDependencyPath(resource, isRelativePath);
	}

	public static File getDependencyPath(String dependencyName) {
		return getDependencyPath(dependencyName, true);
	}

	public static URL getResourceURL(String dependencyPath) {
		return Platform.getBundle(Activator.PLUGIN_ID).getResource(dependencyPath);
	}

	public static File getDependencyPath(URL resource) {
		return getDependencyPath(resource, true);
	}

	public static File getDependencyPath(URL resource, boolean isRelativePath) {
		if (resource != null) {
			IPath path = Activator.getDefault().getStateLocation();
			IPath libFolder = path.append("lib");
			String[] paths = resource.getFile().split("/");
			IPath library = libFolder.append(paths[paths.length - 1]);
			File libraryFile = library.toFile();
			if (!libraryFile.exists()) {
				try {
					writeToFile(libraryFile, resource.openStream());
				} catch (IOException e) {
					return null;
				}
			}
			if (isRelativePath) {
				IPath relativePath = EclipseUtils.getWorkspaceRelativePath(library);
				relativePath = new Path("ECLIPSE_WORKSPACE").append(relativePath);
				return relativePath.toFile();
			} else {
				return library.toFile();
			}
		} else {
			return null;
		}
	}

	private static String getLibLocation() {
		return "lib/";
	}

	private static void writeToFile(File file, InputStream stream) throws IOException {
		file.getParentFile().mkdirs();
		OutputStream out = new FileOutputStream(file);
		byte buf[] = new byte[1024];
		int len;
		while ((len = stream.read(buf)) > 0)
			out.write(buf, 0, len);
		out.close();
		stream.close();
	}

}
