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

package org.olat.course.nodes.bc;

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;

/**
 * Initial Date:  10.02.2005
 * @author Mike Stock
 */

public class FolderNodeCallback implements VFSSecurityCallback {

	private final String relPath;
	private Quota nodeFolderQuota;
	private final NodeEvaluation ne;
	private final boolean isOlatAdmin;
	private final boolean isGuestOnly;
	private final SubscriptionContext nodefolderSubContext;

	/**
	 * Folder node security callback constructor
	 * @param ne the current node evaluation
	 * @param isOlatAdmin true if the current user has admin rights, false otherwhise. 
	 * @param isGuestOnly true if the current user has guest rights, false otherwhise. 
	 * admins will have full access, regardless of their node evaluation
	 * @param nodefolderSubContext
	 */
	public FolderNodeCallback(String relPath, NodeEvaluation ne, boolean isOlatAdmin, boolean isGuestOnly, SubscriptionContext nodefolderSubContext) {
		this.ne = ne;
		this.relPath = relPath;
		this.isOlatAdmin = isOlatAdmin;
		this.isGuestOnly = isGuestOnly;
		this.nodefolderSubContext = nodefolderSubContext;
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#canList(org.olat.modules.bc.Path)
	 */
	@Override
	public boolean canList() {
		return isOlatAdmin || ne.isCapabilityAccessible("download") || ne.isCapabilityAccessible("upload");
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#canRead(org.olat.modules.bc.Path)
	 */
	@Override
	public boolean canRead() {
		return isOlatAdmin || ne.isCapabilityAccessible("download") || ne.isCapabilityAccessible("upload");
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#canWrite(org.olat.modules.bc.Path)
	 */
	@Override
	public boolean canWrite() {
	    if (isGuestOnly) return false;
		return isOlatAdmin || ne.isCapabilityAccessible("upload");
	}

	@Override
	public boolean canCreateFolder() {
		return canWrite();
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#canDelete(org.olat.modules.bc.Path)
	 */
	@Override
	public boolean canDelete() {
	    if (isGuestOnly) return false;
		return isOlatAdmin || ne.isCapabilityAccessible("upload");
	}

	/**
	 * @see org.olat.core.util.vfs.callbacks.VFSSecurityCallback#canCopy()
	 */
	@Override
	public boolean canCopy() {
		return canRead() && canWrite();
	}

	@Override
	public boolean canDeleteRevisionsPermanently() {
		return isOlatAdmin;
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#getQuotaKB(org.olat.modules.bc.Path)
	 */
	@Override
	public Quota getQuota() {
		if(nodeFolderQuota == null) {
			QuotaManager qm = QuotaManager.getInstance();
			nodeFolderQuota = qm.getCustomQuota(relPath);
			if (nodeFolderQuota == null) {
				Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
				nodeFolderQuota = qm.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
			}
		}
		return nodeFolderQuota;
	}

	/**
	 * @see org.olat.core.util.vfs.callbacks.VFSSecurityCallback#setQuota(org.olat.admin.quota.Quota)
	 */
	@Override
	public void setQuota(Quota quota) {
		nodeFolderQuota = quota;
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#getSubscriptionContext()
	 */
	@Override
	public SubscriptionContext getSubscriptionContext() {
		return nodefolderSubContext;
	}
}