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

package org.olat.group;

import org.olat.collaboration.CollaborationManager;
import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.vfs.VFSContainer;
/**
 * 
 * Description:<br>
 */
public class GroupfoldersWebDAVProvider implements WebDAVProvider {

	private static final String MOUNTPOINT = "groupfolders";
	
	private CollaborationManager collaborationManager;

	/**
	 * [used by Spring]
	 * @param collaborationManager
	 */
	public void setCollaborationManager(CollaborationManager collaborationManager) {
		this.collaborationManager = collaborationManager;
	}
	
	@Override
	public boolean hasAccess(IdentityEnvironment identityEnv) {
		return identityEnv != null;
	}

	@Override
	public String getMountPoint() {
		return MOUNTPOINT;
	}

	@Override
	public VFSContainer getContainer(IdentityEnvironment identityEnv) {
		return new GroupfoldersWebDAVMergeSource(identityEnv.getIdentity(), collaborationManager);
	}
}


