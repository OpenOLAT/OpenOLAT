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
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 23 Apr. 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_21_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_21_0_0.class);

	private static final int BATCH_SIZE = 1000;
	private static final String VERSION = "OLAT_21.0.0";
	private static final String MIGRATE_OFFER_VALID_STATUS = "MIGRATE OFFER VALID STATUS";

	@Autowired
	private DB dbInstance;

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= migrateOfferValidStatus(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_21_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_21_0_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateOfferValidStatus(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_OFFER_VALID_STATUS)) {
			try {
				log.info("Migration offer valid status");

				int count = 0;
				List<OfferImpl> offers = getOffersWithoutValidStatus(BATCH_SIZE);
				do {
					for (OfferImpl offer : offers) {
						if ("CurriculumElement".equals(offer.getResourceTypeName())) {
							offer.setValidStatus(Set.of("preparation", "provisional", "confirmed", "active"));
						} else {
							offer.setValidStatus(Set.of("preparation", "review", "coachpublished", "published"));
						}
						dbInstance.getCurrentEntityManager().merge(offer);
						if (count++ % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					dbInstance.commitAndCloseSession();
					offers = getOffersWithoutValidStatus(BATCH_SIZE);
				} while (!offers.isEmpty());

				log.info("End migration offer valid status: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_OFFER_VALID_STATUS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<OfferImpl> getOffersWithoutValidStatus(int maxResults) {
		String query = """
				select offer from acoffer offer
				 where offer.validStatus is null
				   and (offer.validFrom is not null or offer.validTo is not null)
				 order by offer.key""";
		return dbInstance.getCurrentEntityManager().createQuery(query, OfferImpl.class)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}
}
