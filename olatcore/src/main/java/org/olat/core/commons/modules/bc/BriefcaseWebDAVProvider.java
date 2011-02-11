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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.commons.modules.bc;

import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.servlets.WebDAVProvider;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
/**
 * 
 */
public class BriefcaseWebDAVProvider  extends BasicManager implements WebDAVProvider {

	private static final String MOUNTPOINT = "home";
	
	public String getMountPoint() { return MOUNTPOINT; }

	/**
	 * @see org.olat.commons.servlets.util.WebDAVProvider#getContainer(org.olat.core.id.Identity)
	 */
	public VFSContainer getContainer(Identity identity) {
		// merge /public and /private
		MergeSource homeMergeSource = new MergeSource(null, identity.getName());
		
		// mount /public
		OlatRootFolderImpl vfsPublic = new OlatRootFolderImpl(getRootPathFor(identity) + "/public", homeMergeSource);
		vfsPublic.getBasefile().mkdirs(); // lazy initialize folders
		// we do a little trick here and wrap it again in a NamedContainerImpl so
		// it doesn't show up as a OlatRootFolderImpl to prevent it from editing its MetaData
		OlatNamedContainerImpl vfsNamedPublic = new OlatNamedContainerImpl("public", vfsPublic);
		
		// mount /private
		OlatRootFolderImpl vfsPrivate = new OlatRootFolderImpl(getRootPathFor(identity) + "/private", homeMergeSource);
		vfsPrivate.getBasefile().mkdirs(); // lazy initialize folders
		// we do a little trick here and wrap it again in a NamedContainerImpl so
		// it doesn't show up as a OlatRootFolderImpl to prevent it from editing its MetaData
		OlatNamedContainerImpl vfsNamedPrivate = new OlatNamedContainerImpl("private", vfsPrivate);
		
		// set quota for this merge source
		QuotaManager qm = QuotaManager.getInstance();
		Quota quota = qm.getCustomQuotaOrDefaultDependingOnRole(identity, getRootPathFor(identity));
		FullAccessWithQuotaCallback secCallback = new FullAccessWithQuotaCallback(quota);

		homeMergeSource.setLocalSecurityCallback(secCallback);
		homeMergeSource.addContainer(vfsNamedPublic);
		homeMergeSource.addContainer(vfsNamedPrivate);
		
		return homeMergeSource;
	}
	
	protected String getRootPathFor(Identity identity) {
		return FolderConfig.getUserHomes() + "/" + identity.getName();
	}

}
