/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.manager.FeedDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_19_0_6 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_0_6.class);
	
	private static final String VERSION = "OLAT_19.0.6";

	private static final String BLOG_EXTERNAL_INTERNAL = "BLOG EXTERNAL INTERNAL";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private FeedDAO feedDao;

	public OLATUpgrade_19_0_6() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= missingExternalInternalFeed(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_0_6 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_0_6 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean missingExternalInternalFeed(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(BLOG_EXTERNAL_INTERNAL)) {
			try {
				List<Long> feedKeys = feedsWithMissingFlag();
				log.info("Start setting missing external/internal flag for {} feeds.", feedKeys.size());
				
				for(Long feedKey:feedKeys) {
					Feed feed = feedDao.loadFeed(feedKey);
					if(feed.getExternal() == null) {
						if(StringHelper.containsNonWhitespace(feed.getExternalFeedUrl())) {
							feed.setExternal(Boolean.TRUE);
						} else {
							feed.setExternal(Boolean.FALSE);
						}
						feedDao.updateFeed(feed);
						dbInstance.commitAndCloseSession();
					}	
				}

				log.info("End setting missing external/internal flag for {} feeds.", feedKeys.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(BLOG_EXTERNAL_INTERNAL, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
	private List<Long> feedsWithMissingFlag() {
		String query = """
				select feed.key from feed as feed
				where feed.isExternal is null
				""";
		return dbInstance.getCurrentEntityManager().createQuery(query, Long.class)
				.getResultList();
	}
}
