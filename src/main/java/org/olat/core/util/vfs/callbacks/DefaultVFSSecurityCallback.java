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
package org.olat.core.util.vfs.callbacks;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;

/**
 * 
 * Initial date: 4 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DefaultVFSSecurityCallback implements VFSSecurityCallback {
	
	private Quota quota;

	@Override
	public boolean canRead() {
		return false;
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public boolean canCreateFolder() {
		return false;
	}

	@Override
	public boolean canDelete() {
		return false;
	}

	@Override
	public boolean canList() {
		return false;
	}

	@Override
	public boolean canCopy() {
		return false;
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
		return null;
	}
}
