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
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * Initial Date:  10.02.2005
 * @author Mike Stock
 */

public class FolderNodeCallback implements VFSSecurityCallback {

	private final String relPath;
	private Quota nodeFolderQuota;
	private final boolean canDownload;
	private final boolean canUpload;
	private final boolean isAdministrator;
	private final boolean isGuestOnly;
	private final SubscriptionContext nodefolderSubContext;

	/**
	 * Folder node security callback constructor
	 * 
	 * @param relPath
	 * @param canDownload 
	 * @param canUpload 
	 * @param isAdministrator admins will have full access, regardless of the canDownload and canUpload settings
	 * @param isGuestOnly true if the current user has guest rights 
	 * @param nodefolderSubContext
	 */
	public FolderNodeCallback(String relPath, boolean canDownload, boolean canUpload, boolean isAdministrator,
			boolean isGuestOnly, SubscriptionContext nodefolderSubContext) {
		this.canDownload = canDownload;
		this.canUpload = canUpload;
		this.relPath = relPath;
		this.isAdministrator = isAdministrator;
		this.isGuestOnly = isGuestOnly;
		this.nodefolderSubContext = nodefolderSubContext;
	}

	@Override
	public boolean canList() {
		return isAdministrator || canDownload || canUpload;
	}

	@Override
	public boolean canRead() {
		return isAdministrator || canDownload || canUpload;
	}

	@Override
	public boolean canWrite() {
		if (isGuestOnly) return false;
		return isAdministrator || canUpload;
	}

	@Override
	public boolean canCreateFolder() {
		return canWrite();
	}

	@Override
	public boolean canDelete() {
		if (isGuestOnly) return false;
		return isAdministrator || canUpload;
	}

	@Override
	public boolean canCopy() {
		return canRead() && canWrite();
	}

	@Override
	public boolean canDeleteRevisionsPermanently() {
		return isAdministrator;
	}

	@Override
	public Quota getQuota() {
		if(nodeFolderQuota == null) {
			QuotaManager qm = CoreSpringFactory.getImpl(QuotaManager.class);
			nodeFolderQuota = qm.getCustomQuota(relPath);
			if (nodeFolderQuota == null) {
				Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
				nodeFolderQuota = qm.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
			}
		}
		return nodeFolderQuota;
	}

	@Override
	public void setQuota(Quota quota) {
		nodeFolderQuota = quota;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return nodefolderSubContext;
	}
}