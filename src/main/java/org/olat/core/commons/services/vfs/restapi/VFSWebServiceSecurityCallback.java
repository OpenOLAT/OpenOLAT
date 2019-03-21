/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.vfs.restapi;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  26 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class VFSWebServiceSecurityCallback implements VFSSecurityCallback {

	private final boolean canWrite;
	private final boolean canDelete;
	private final boolean canCopy;
	private Quota quota;
	private final SubscriptionContext subscriptionContext;
	
	public VFSWebServiceSecurityCallback(boolean canWrite, boolean canDelete, boolean canCopy, Quota quota, SubscriptionContext subscriptionContext) {
		this.canWrite = canWrite;
		this.canDelete = canDelete;
		this.canCopy = canCopy;
		this.quota = quota;
		this.subscriptionContext = subscriptionContext;
	}
	
	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return canWrite;
	}

	@Override
	public boolean canCreateFolder() {
		return canWrite;
	}

	@Override
	public boolean canDelete() {
		return canDelete;
	}

	@Override
	public boolean canList() {
		return true;
	}

	@Override
	public boolean canCopy() {
		return canCopy;
	}

	@Override
	public boolean canDeleteRevisionsPermanently() {
		return false;
	}

	@Override
	public Quota getQuota() {
		return quota;
	}

	@Override
	public void setQuota(Quota quota) {
		this.quota = quota;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return subscriptionContext;
	}
}
