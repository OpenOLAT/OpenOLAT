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

package org.olat.modules.sharedfolder;

import java.util.List;

import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * 
 * Initial Date: Aug 29, 2005 <br>
 * 
 * @author Alexander Schneider, Gregor Wassmann
 */
public class SharedFolderWebDAVProvider implements WebDAVProvider {
	private static List<String> publiclyReadableFolders;
	protected static final VFSSecurityCallback readOnlyCallback = new ReadOnlyCallback();
	
	public SharedFolderWebDAVProvider() {
		//
	}

	/**
	 * Spring setter.
	 * <p>
	 * In /olat3/webapp/WEB-INF/olat_extensions.xml the bean
	 * 'webdav_sharedfolders' has an optional property called
	 * 'publiclyReadableFolders':
	 * 
	 * <pre>
	 * &lt;property name=&quot;publiclyReadableFolders&quot;&gt;
	 *   &lt;list&gt;
	 *     &lt;value&gt;7045120&lt;/value&gt;
	 *     &lt;value&gt;{another repository entry key}&lt;/value&gt;
	 *   &lt;/list&gt;
	 * &lt;/property&gt;
	 * </pre>
	 * 
	 * It's a list of repositoryEntryKeys belonging to resource folders. These
	 * folders will then be displayed (in readonly mode) in WebDAV provided that
	 * the repository entry allows access from all users or guests.
	 * <p>
	 * Alternatively, use '*' as the first value in the list to indicate that all
	 * resource folders should be listed in WebDAV.
	 * 
	 * @param folders
	 */
	public void setPubliclyReadableFolders(List<String> repositoryEntryKeys) {
		publiclyReadableFolders = repositoryEntryKeys;
	}

	/**
	 * @see org.olat.core.commons.services.webdav.WebDAVProvider#getMountPoint()
	 */
	@Override
	public String getMountPoint() {
		return "sharedfolders";
	}
	
	@Override
	public boolean hasAccess(IdentityEnvironment identityEnv) {
		return identityEnv != null;
	}

	/**
	 * @see org.olat.core.commons.services.webdav.WebDAVProvider#getContainer(org.olat.core.id.Identity)
	 */
	@Override
	public VFSContainer getContainer(IdentityEnvironment identityEnv) {
		return new SharedFolderWebDAVMergeSource(identityEnv.getIdentity(), publiclyReadableFolders);
	}
}