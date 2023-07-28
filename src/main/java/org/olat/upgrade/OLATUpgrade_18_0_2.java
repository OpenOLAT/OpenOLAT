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
import org.olat.commons.info.InfoMessage;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_18_0_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_0_2.class);

	private static final int BATCH_SIZE = 1000;

	private static final String VERSION = "OLAT_18.0.2";
	private static final String INIT_INFO_MESSAGES_SCHEDULER_UPDATE = "INIT INFO MESSAGES MISSING PUBLISHED DATES";

	@Autowired
	private DB dbInstance;

	public OLATUpgrade_18_0_2() {
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
		allOk &= initInfoMessageSchedulerUpdate(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_0_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_0_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean initInfoMessageSchedulerUpdate(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(INIT_INFO_MESSAGES_SCHEDULER_UPDATE)) {
			try {
				log.info("Start updating info messages with missing published dates");

				int counter = 0;
				List<InfoMessage> infoMessages;
				do {
					infoMessages = getInfoMessages(counter, BATCH_SIZE);
					for (InfoMessage infoMessage : infoMessages) {
						//set initial value to true, because there was no scheduler option before
						infoMessage.setPublished(true);
						//set initial value to creationDate, because there was no scheduler option before
						infoMessage.setPublishDate(infoMessage.getCreationDate());
					}
					counter += infoMessages.size();
					log.info(Tracing.M_AUDIT, "Updated info messages: {} total processed ({})", infoMessages.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (counter == BATCH_SIZE);

				log.info("Update for infoMessages with missing published dates finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INIT_INFO_MESSAGES_SCHEDULER_UPDATE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private List<InfoMessage> getInfoMessages(int firstResult, int maxResults) {
		String query = "select msg from infomessage as msg where msg.publishDate is null order by msg.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, InfoMessage.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
	}
}
