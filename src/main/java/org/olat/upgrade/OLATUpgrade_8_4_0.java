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
package org.olat.upgrade;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;
import org.olat.upgrade.model.BookmarkImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * upgrade code for OLAT 7.1.0 -> OLAT 7.1.1
 * - fixing invalid structures being built by synchronisation, see OLAT-6316 and OLAT-6306
 * - merges all yet found data to last valid node 
 * 
 * <P>
 * Initial Date: 24.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_8_4_0 extends OLATUpgrade {

	private static final String TASK_BOOKMARKS = "Upgrade bookmarks";
	private static final int BATCH_SIZE = 20;
	private static final String VERSION = "OLAT_8.4.0";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private MarkManager markManager;

	public OLATUpgrade_8_4_0() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) {
				return false;
			}
		}
		
		boolean allOk = upgradeBookmarks(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_8_3_0 successfully!");
		} else {
			log.audit("OLATUpgrade_8_3_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeBookmarks(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_BOOKMARKS)) {
			int counter = 0;
			List<BookmarkImpl> bookmarks;
			do {
				bookmarks = findBookmarks(counter, BATCH_SIZE);
				for(BookmarkImpl bookmark:bookmarks) {
					processBookmarks(bookmark);
				}
				counter += bookmarks.size();
				log.audit("Processed context: " + bookmarks.size());
				dbInstance.intermediateCommit();
			} while(bookmarks.size() == BATCH_SIZE);
			
			uhd.setBooleanDataValue(TASK_BOOKMARKS, false);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processBookmarks(BookmarkImpl bookmark) {
		String resName = bookmark.getOlatrestype();
		Long resId = bookmark.getOlatreskey();
		Identity owner = bookmark.getOwner();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
		String businessPath = null;
		if("RepositoryEntry".equals(resName) || "CatalogEntry".equals(resName)) {
			businessPath = "[" + resName + ":" + resId + "]";
		}
		markManager.setMark(ores, owner, null, businessPath);
	}
	
	private List<BookmarkImpl> findBookmarks(int counter, int batchSize) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bookmark from ").append(BookmarkImpl.class.getName()).append(" as bookmark")
		  .append(" inner join fetch bookmark.owner as identity")
		  .append(" inner join fetch identity.user as user");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BookmarkImpl.class)
				.setFirstResult(counter)
				.setMaxResults(batchSize)
				.getResultList();
	}
}
