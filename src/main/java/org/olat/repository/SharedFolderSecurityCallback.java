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

package org.olat.repository;

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 *  Initial Date:  29.06.2005
 * 
 *  @author Alexander Schneider
 *  
 *  Comment:  
 *  
 */

public class SharedFolderSecurityCallback implements VFSSecurityCallback {

	private final String relPath;
	private Quota sharedFolderQuota;

	public SharedFolderSecurityCallback(String relPath) {
		this.relPath = relPath;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public boolean canCreateFolder() {
		return true;
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public boolean canList() {
		return true;
	}

	@Override
	public boolean canCopy() {
		return true;
	}

	@Override
	public boolean canDeleteRevisionsPermanently() {
		return true;
	}

	@Override
	public Quota getQuota() {
		if(sharedFolderQuota == null) {
			initSharedFolderQuota(relPath);
		}
		return sharedFolderQuota;
	}

	@Override
	public void setQuota(Quota quota) {
		sharedFolderQuota = quota;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}

	/**
	 * Node folder quota has to be ionitialized only once for all of the paths.
	 * 
	 * @param path
	 */
	private void initSharedFolderQuota(String path) {
		QuotaManager qm = CoreSpringFactory.getImpl(QuotaManager.class);
		sharedFolderQuota = qm.getCustomQuota(path);
		if (sharedFolderQuota == null) {
			Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COURSE);
			sharedFolderQuota = qm.createQuota(path, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
	}

}