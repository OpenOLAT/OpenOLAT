/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;

/**
 * 
 * Initial date: 28 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderQuota {
	
	private long actualUsage = 0;
	private long quotaKB = Quota.UNLIMITED;
	private long uploadLimitKB = Quota.UNLIMITED;
	private long remainingQuotaKB;
	
	public FolderQuota(UserRequest ureq, Quota quota, long actualUsage) {
		this.actualUsage = actualUsage;
		if (quota != null) {
			quotaKB = quota.getQuotaKB().longValue();
			uploadLimitKB = quota.getUlLimitKB().longValue();
		}
		
		// set default ulLimit if none is defined...
		if (uploadLimitKB == Quota.UNLIMITED) {
			uploadLimitKB = CoreSpringFactory.getImpl(QuotaManager.class).getDefaultQuotaDependingOnRole(ureq.getIdentity(), ureq.getUserSession().getRoles()).getUlLimitKB().longValue();
		}
		
		if (quotaKB == Quota.UNLIMITED) {
			remainingQuotaKB = quotaKB;
		} else if (quotaKB - actualUsage < 0) {
			remainingQuotaKB = 0;
		} else {
			remainingQuotaKB = quotaKB - actualUsage;
		}
	}

	public long getActualUsage() {
		return actualUsage;
	}

	public long getQuotaKB() {
		return quotaKB;
	}

	public long getUploadLimitKB() {
		return uploadLimitKB;
	}

	public long getRemainingQuotaKB() {
		return remainingQuotaKB;
	}
	
	public boolean isExceeded() {
		return remainingQuotaKB == 0;
	}
	
	public boolean isUnlimited() {
		return quotaKB == Quota.UNLIMITED;
	}

}
