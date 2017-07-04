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

import java.util.Arrays;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_0_0 extends OLATUpgrade {

	private static final String VERSION = "OLAT_12.0.0";
	private static final String FEED_XML_TO_DB = "FEED XML TO DB";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	FeedManager feedManager;
	
	public OLATUpgrade_12_0_0() {
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
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= upgradeBlogXmlToDb(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_12_0_0 successfully!");
		} else {
			log.audit("OLATUpgrade_12_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeBlogXmlToDb(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(FEED_XML_TO_DB)) {
			
			List<String> feedTypes = Arrays.asList(BlogFileResource.TYPE_NAME, PodcastFileResource.TYPE_NAME);
			List<OLATResource> feeds = OLATResourceManager.getInstance().findResourceByTypes(feedTypes);
			log.info("Number of feeds to upgrade: " + feeds.size());
			for (OLATResource ores : feeds) {
				log.info("Upgrade feed " + "(" + ores.getResourceableTypeName() + "): " + ores.getResourceableId());
				try {
					feedManager.importFeedFromXML(ores, false);
				} catch (Exception e) {
					allOk &= false;
					log.error("", e);
				}
				dbInstance.commitAndCloseSession();
			}
			
			uhd.setBooleanDataValue(FEED_XML_TO_DB, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

}
