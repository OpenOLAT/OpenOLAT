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

import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_0_0_beta1 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.0.0.beta1";
	private static final String MIGRATE_QUALITY_DATA_COLLECTION_TO_ORGANISATION = "MIGRATE QUALITY DATA COLLECTION TO ORGANISATION";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private OrganisationService organisationService;
	
	public OLATUpgrade_13_0_0_beta1() {
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
		allOk &= migrateQualityDataCollectionToOrganisation(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_0_0_beta successfully!");
		} else {
			log.audit("OLATUpgrade_13_0_0_beta not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean migrateQualityDataCollectionToOrganisation(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_QUALITY_DATA_COLLECTION_TO_ORGANISATION)) {
			try {
				Organisation organisation = organisationService.getDefaultOrganisation();
				List<Organisation> organisations = Collections.singletonList(organisation);
				List<QualityDataCollection> dataCollections = qualityService.loadAllDataCollections();
				for (int i=0; i<dataCollections.size(); i++) {
					QualityDataCollection dataCollection = dataCollections.get(i);
					qualityService.updateDataCollectionOrganisations(dataCollection, organisations);
					log.info("Migration quality data collection to organisations: " + dataCollection.getTitle());
					if (i % 50 == 0) {
						log.info("Migration quality data collection to organisations: " + i + " / " + dataCollections.size());
					}
				}
				log.info("Migration quality data collection to organisations: " + dataCollections.size());
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_QUALITY_DATA_COLLECTION_TO_ORGANISATION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

}
