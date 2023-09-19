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

package org.olat.admin.quota;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.FileUtils.Usage;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.cemedia.manager.MediaDAO;


/**
 * Initial Date:  Mar 30, 2004
 *
 * @author Mike Stock
 */
public class QuotaImpl implements Quota {

	private String path;
	private Long quotaKB;
	private Long ulLimitKB;
	
	public QuotaImpl(String path, Long quotaKB, Long ulLimitKB) {
		this.path = path != null ? path : "";
		this.quotaKB = quotaKB;
		this.ulLimitKB = ulLimitKB;
	}

	/**
	 * @return The path
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @return Quota in KB
	 */
	@Override
	public Long getQuotaKB() {
		return quotaKB;
	}

	/**
	 * @return Upload Limit in KB.
	 */
	@Override
	public Long getUlLimitKB() {
		return ulLimitKB;
	}

	@Override
	public Long getRemainingSpace() {
		long quotaInKB = getQuotaKB().longValue();
		long remainingQuotaKB;
		if (quotaInKB == Quota.UNLIMITED) {
			remainingQuotaKB = quotaInKB;
		} else  {
			long actualUsage;
			if(path != null && path.startsWith("/HomeSite/") && path.contains("/MediaCenter/")) {
				Usage usage = CoreSpringFactory.getImpl(MediaDAO.class).getFileUsage(path);
				actualUsage = usage == null ? 0l : usage.getSizeInKB();
			} else {
				VFSContainer container = VFSManager.olatRootContainer(path, null);
				actualUsage = VFSManager.getUsageKB(container);
			}
			if (quotaInKB - actualUsage < 0) {
				remainingQuotaKB = 0l;
			} else {
				remainingQuotaKB = quotaInKB - actualUsage;
			}
		}
		return Long.valueOf(remainingQuotaKB);
	}
}
