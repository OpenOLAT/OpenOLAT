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

	private NodeEvaluation ne;
	private Quota nodeFolderQuota;
	private boolean isOlatAdmin;
	private boolean isGuestOnly;
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
		this.isOlatAdmin = isOlatAdmin;
		this.isGuestOnly = isGuestOnly;
		this.nodefolderSubContext = nodefolderSubContext;
		QuotaManager qm = QuotaManager.getInstance();
		nodeFolderQuota = qm.getCustomQuota(relPath);
		if (nodeFolderQuota == null) {
			Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
			nodeFolderQuota = qm.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#canList(org.olat.modules.bc.Path)
	 */
	public boolean canList() {
		return ne.isCapabilityAccessible("download") || ne.isCapabilityAccessible("upload") || isOlatAdmin;
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#canRead(org.olat.modules.bc.Path)
	 */
	public boolean canRead() {
		return ne.isCapabilityAccessible("download") || ne.isCapabilityAccessible("upload") || isOlatAdmin;
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#canWrite(org.olat.modules.bc.Path)
	 */
	public boolean canWrite() {
	    if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("upload") || isOlatAdmin;
	}

	@Override
	public boolean canCreateFolder() {
		return canWrite();
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#canDelete(org.olat.modules.bc.Path)
	 */
	public boolean canDelete() {
	    if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("upload") || isOlatAdmin;
	}

	/**
	 * @see org.olat.core.util.vfs.callbacks.VFSSecurityCallback#canCopy()
	 */
	public boolean canCopy() {
		return canRead() && canWrite();
	}
	
	public boolean canDeleteRevisionsPermanently() {
		return isOlatAdmin;
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#getQuotaKB(org.olat.modules.bc.Path)
	 */
	public Quota getQuota() {
		return nodeFolderQuota;
	}

	/**
	 * @see org.olat.core.util.vfs.callbacks.VFSSecurityCallback#setQuota(org.olat.admin.quota.Quota)
	 */
	public void setQuota(Quota quota) {
		nodeFolderQuota = quota;
	}

	/**
	 * @see org.olat.modules.bc.callbacks.SecurityCallback#getSubscriptionContext()
	 */
	public SubscriptionContext getSubscriptionContext() {
		return nodefolderSubContext;
	}
}