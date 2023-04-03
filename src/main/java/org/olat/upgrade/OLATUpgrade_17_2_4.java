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
package org.olat.upgrade;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Mär 27, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OLATUpgrade_17_2_4 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_17_2_4.class);

	private static final int BATCH_SIZE = 1000;

	private static final String VERSION = "OLAT_17.2.4";
	private static final String INIT_REPO_STATUS_CHANGES = "INIT REPO STATUS CHANGES";

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private BaseSecurity securityManager;

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

		allOk &= initRepoStatusChanges(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_17_2_4 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_17_2_4 not finished, try to restart OpenOlat!");
		}

		return allOk;
	}

	private boolean initRepoStatusChanges(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(INIT_REPO_STATUS_CHANGES)) {
			try {
				log.info("Start setting subcriptions for course owners.");

				int counter = 0;
				List<Identity> identities;
				do {
					identities = getIdentities(counter, BATCH_SIZE);
					migrateCourseOwnerSubscriptions(identities);
					counter += identities.size();
					log.info(Tracing.M_AUDIT, "Update identities subscribed to learning resources: {} total processed ({})", identities.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				log.info("Learning resources subscription for owners update finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(INIT_REPO_STATUS_CHANGES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}

	private int migrateCourseOwnerSubscriptions(List<Identity> courseOwnerIdentities) {
		PublisherData publisherData = repositoryService.getPublisherData();
		SubscriptionContext subscriptionContext = repositoryService.getSubscriptionContext();

		if (subscriptionContext != null) {
			int counter = 0;
			for (Identity identity : courseOwnerIdentities) {
				Subscriber sub;
				if (notificationsManager.getSubscriber(identity, subscriptionContext) != null) {
					sub = notificationsManager.getSubscriber(identity, subscriptionContext);
				} else {
					sub = notificationsManager.subscribe(identity, subscriptionContext, publisherData);
				}
				if (sub != null && !repositoryModule.isNotificationRepoStatusChanged()) {
					counter++;
					notificationsManager.mergeSubscriber(sub);
					sub.setEnabled(false);
					if ((counter % 10) == 0) {
						dbInstance.commitAndCloseSession();
					}
				}
			}
		}
		return courseOwnerIdentities.size();
	}

	private List<Identity> getIdentities(int firstResult, int maxResults) {
		SearchIdentityParams params = SearchIdentityParams.resources(GroupRoles.owner, true, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		return securityManager.getIdentitiesByPowerSearch(params, firstResult, maxResults);
	}
}
