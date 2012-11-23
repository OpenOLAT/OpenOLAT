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
*/

package org.olat.ims.qti;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.olat.commons.servlets.pathhandlers.FilePathHandler;
import org.olat.commons.servlets.util.ResourceDescriptor;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.util.session.UserSessionManager;

/**
 * Initial Date:  16.06.2003
 *
 * @author Mike Stock<br>
 * Comment:
 * 
 * Inherited FilePathHandler.root must be set by IMSCpModule
 */
public class QTIStaticsHandler extends FilePathHandler {

	public QTIStaticsHandler() {
		super();
		setRoot(FolderConfig.getCanonicalRepositoryHome());
	}

	/**
	 * @see org.olat.commons.servlets.pathhandlers.PathHandler#init(com.anthonyeden.lib.config.Configuration)
	 */
	public void init(String config) {
		// no need to do an init...
	}
	
	public InputStream getInputStream(HttpServletRequest request, ResourceDescriptor rd) {
		return super.getInputStream(request, rd);
	}

	public ResourceDescriptor getResourceDescriptor(HttpServletRequest request, String relPath) {
		if (CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request).isAuthenticated()) {
			if (relPath.endsWith("qti.xml")) return null;
			return super.getResourceDescriptor(request, relPath);
		}	else
			return null;
	}

}
