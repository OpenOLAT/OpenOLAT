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

import java.util.Collection;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.logging.Tracing;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_18_0_7 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_0_7.class);

	private static final String VERSION = "OLAT_18.0.7";

	private static final String MARK_PROJECT_NEWS = "MARK PROJECT NEWS";
	
	@Autowired
	private DB dbInstance;

	@Autowired
	private ProjectService projectService;
	@Autowired
	private NotificationsManager notificationManager;

	public OLATUpgrade_18_0_7() {
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
		allOk &= markProjectNews(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_0_7 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_0_7 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean markProjectNews(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(MARK_PROJECT_NEWS)) {
			try {
				log.info("Start mark project news");
				
				ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
				searchParams.setActions(ProjActivity.TIMELINE_ACTIONS);
				Collection<ProjActivity> lastActivities = projectService.getProjectKeyToLastActivity(searchParams).values();
				lastActivities.forEach(activity -> {
					Publisher publisher = notificationManager.getPublisher(projectService.getSubscriptionContext(activity.getProject()));
					if (publisher != null) {
						publisher.setLatestNewsDate(activity.getCreationDate());
						dbInstance.getCurrentEntityManager().merge(publisher);
						dbInstance.commit();
					}
				});
				dbInstance.commitAndCloseSession();
				
				log.info("Mark project news finished ({} projects).", lastActivities.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MARK_PROJECT_NEWS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
}
