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
package org.olat.upgrade;

import java.util.List;

import org.hibernate.ObjectDeletedException;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.util.StringHelper;

public class OLATUpgrade_7_0_1 extends OLATUpgrade {
	
	private static final String TASK_CHECK_NOTIFICATIONS = "Check notifications publishers";
	
	private static final String VERSION = "OLAT_7.0.1";

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPostSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}
		
		migrateNotificationPublishers(upgradeManager, uhd);
			
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		log.audit("Finished OLATUpgrade_7_0_1 successfully!" );
		return true;
	}
	


	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPreSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}
	
	private void migrateNotificationPublishers(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_CHECK_NOTIFICATIONS)) {
			log.audit("+-----------------------------------------------------------------------------+");
			log.audit("+... Check the businesspath for the publishers (notifications)             ...+");
			log.audit("+-----------------------------------------------------------------------------+");

			int counter = 0;
			NotificationsManager notificationMgr = NotificationsManager.getInstance();
			List<Publisher> allPublishers = notificationMgr.getAllPublisher();
			if (log.isDebug()) log.info("Found " + allPublishers.size() + " publishers to check.");

			for(Publisher publisher:allPublishers) {
				if(publisher != null && StringHelper.containsNonWhitespace(publisher.getBusinessPath())
						&& (publisher.getBusinessPath().startsWith("[Identity") || publisher.getBusinessPath().startsWith("ROOT[Identity"))) {
					try {
						String businessPath = publisher.getBusinessPath();
						int startIndex = businessPath.indexOf("[Identity");
						int stopIndex = businessPath.indexOf("]", startIndex);
						int wide = stopIndex - startIndex;
						if(wide > 30) {
							//Identity:326394598 cannot be too wide
							continue;
						} else if (stopIndex + 1 >= businessPath.length()) {
							//only identity
							continue;
						}
						
						String correctPath = businessPath.substring(stopIndex + 1);
						publisher.setBusinessPath(correctPath);
						DBFactory.getInstance().updateObject(publisher);
					} catch (ObjectDeletedException e) {
						log.warn("Publisher was already deleted, no update possible! Publisher key: "+publisher.getKey() );
					} catch (Exception e) {
						log.warn("Publisher was already deleted, no update possible! Publisher key: "+publisher.getKey() );
					}
					counter++;
				}
				if (counter > 0 && counter % 100 == 0) {
					log.audit("Another 100 publishers done");
					DBFactory.getInstance().intermediateCommit();
				}
			}

			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Checked " + counter + " publishers. ****");

			uhd.setBooleanDataValue(TASK_CHECK_NOTIFICATIONS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#getVersion()
	 */
	@Override
	public String getVersion() {
		return VERSION;
	}
}
