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
package org.olat.course.nodes.pf.manager;

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class ReadOnlyCallback implements VFSSecurityCallback {
	
	private SubscriptionContext subsContext;
	private Quota quota;
	private final String folderPath;
	private final String defaultQuota;

	public ReadOnlyCallback(SubscriptionContext subsContext, String folderPath) {
		super();
		this.subsContext = subsContext;
		this.folderPath = folderPath;
		this.defaultQuota = QuotaConstants.IDENTIFIER_DEFAULT_PFNODES;
	}

	@Override
	public boolean canRead() {
		return true;
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
		if(quota == null) {
			QuotaManager qm = CoreSpringFactory.getImpl(QuotaManager.class);
			Quota q = qm.getCustomQuota(folderPath);
			if (q == null) {
				Quota defQuota = qm.getDefaultQuota(defaultQuota);
				q = qm.createQuota(folderPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
			}
			setQuota(q);
		}
		return quota;
	}

	@Override
	public void setQuota(Quota quota) {
		this.quota = quota;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return subsContext;
	}

}
