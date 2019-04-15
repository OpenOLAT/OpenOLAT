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

import java.util.List;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class CountingCallback implements VFSSecurityCallback {
	
	private static final VFSItemFilter attachmentExcludeFilter = new VFSSystemItemFilter();

	private SubscriptionContext subsContext;
	private VFSContainer dropbox;
	private int limit;
	private boolean alterFile;

	public CountingCallback(SubscriptionContext subsContext, VFSContainer dropbox, int limit, boolean alterFile) {
		super();
		this.subsContext = subsContext;
		this.dropbox = dropbox;
		this.limit = limit;
		this.alterFile = alterFile;
	}
	
	private int countFiles(VFSContainer vfsContainer) {
		int counter = 0;
		if (vfsContainer.exists()) {
			List<VFSItem> children = vfsContainer.getItems(attachmentExcludeFilter);
			for (VFSItem vfsItem : children) {
				if (vfsItem instanceof VFSContainer){
					counter += countFiles((VFSContainer)vfsItem);
				} else {
					counter++;										
				}
			}
		}
		return counter;
	}
	
	private boolean limitReached () {
		return countFiles(dropbox) <= limit;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return limitReached();
	}

	@Override
	public boolean canCreateFolder() {
		return limitReached();
	}

	@Override
	public boolean canDelete() {
		return alterFile;
	}

	@Override
	public boolean canList() {
		return true;
	}

	@Override
	public boolean canCopy() {
		return limitReached();
	}

	@Override
	public boolean canDeleteRevisionsPermanently() {
		return false;
	}

	@Override
	public Quota getQuota() {
		return null;
	}

	@Override
	public void setQuota(Quota quota) {
		//
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return subsContext;
	}

}
