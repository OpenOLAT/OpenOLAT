/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Description:<br>
 * Initial Date:  12.07.2005 <br>
 *
 * @author Mike Stock
 */
public class ClasspathExtensionResource implements ExtensionResource {

	private URL classpathResource;
	private String targetName;
	private String targetSubpath;
	
	/**
	 * @param clazz
	 * @param resourcePath
	 */
	public ClasspathExtensionResource(Class clazz, String resourcePath) {
		classpathResource = clazz.getResource(resourcePath);
		
		// derive target name from path
		targetName = classpathResource.getFile();
		int lastSlash = targetName.lastIndexOf('/');
		if (lastSlash != -1) targetName = targetName.substring(lastSlash + 1);
		
		// set empty target path
		targetSubpath = "";
	}
	
	/**
	 * @see org.olat.core.extensions.ExtensionResource#getTargetName()
	 */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * @see org.olat.core.extensions.ExtensionResource#getTargetSubpath()
	 */
	public String getTargetSubpath() {
		return targetSubpath;
	}

	/**
	 * @see org.olat.core.extensions.ExtensionResource#getContent()
	 */
	public InputStream getContent() throws IOException {
		return classpathResource.openStream();
	}

}
