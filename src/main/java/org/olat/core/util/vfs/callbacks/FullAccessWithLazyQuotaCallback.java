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
import org.olat.core.util.vfs.QuotaManager;

/**
 * 
 * Initial date: 14 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullAccessWithLazyQuotaCallback extends FullAccessWithQuotaCallback {
	
	private final String folderPath;
	private final String defaultQuota;
	
	public FullAccessWithLazyQuotaCallback(String folderPath, String defaultQuota) {
		super(null);
		this.folderPath = folderPath;
		this.defaultQuota = defaultQuota;
	}
	
	public FullAccessWithLazyQuotaCallback(String folderPath, String defaultQuota, SubscriptionContext subscriptionContext) {
		super(null, subscriptionContext);
		this.folderPath = folderPath;
		this.defaultQuota = defaultQuota;
	}
	
	@Override
	public Quota getQuota() {
		if(super.getQuota() == null) {
			QuotaManager qm = QuotaManager.getInstance();
			Quota q = qm.getCustomQuota(folderPath);
			if (q == null) {
				Quota defQuota = qm.getDefaultQuota(defaultQuota);
				q = QuotaManager.getInstance().createQuota(folderPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
			}
			super.setQuota(q);
		}
		return super.getQuota();
	}
}
