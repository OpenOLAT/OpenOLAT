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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_20_2_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_2_0.class);

	private static final String VERSION = "OLAT_20.2.0";
	private static final String MIGRATE_CATALOG_CARD_VIEW = "MIGRATE CATALOG CARD_VIEW";
	private static final String MIGRATE_CURRICULUM_MANAGERS = "MIGRATE CURRICULUM MANAGERS";

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumService curriculumService;
	
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
		allOk &= migrateCatalogCardView(upgradeManager, uhd);
		allOk &= migrateCurriculumManagers(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_2_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean migrateCatalogCardView(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_CATALOG_CARD_VIEW)) {
			try {
				String userDataDirectory = WebappHelper.getUserDataRoot();
				Path propsPath = Paths.get(userDataDirectory, "system", "configuration", "org.olat.modules.catalog.CatalogV2Module.properties");
				if (Files.exists(propsPath)) {
					Properties props = new Properties();
					try (FileInputStream input = new FileInputStream(propsPath.toFile())) {
						props.load(input);
					}
					
					Object prop = props.get("catalog.v2.card.view");
					if (prop instanceof String propertyValue) {
						propertyValue = propertyValue.replace("externalRef", "extRef,type");
						props.setProperty("catalog.v2.card.view", propertyValue);
						try (FileOutputStream output = new FileOutputStream(propsPath.toFile())) {
							props.store(output, "");
							log.info("Catalog card view migrated.");
						} catch (Exception e) {
							log.error("", e);
						}
					}
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_CATALOG_CARD_VIEW, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean migrateCurriculumManagers(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATE_CURRICULUM_MANAGERS)) {
			try {
				log.info("Migrate curriculum managers");
				
				List<Long> curriculumsKeys = loadAllCurriculumsKeys();
				for(Long curriculumKey:curriculumsKeys) {
					Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
					List<Identity> managers = curriculumDao.getMembersIdentity(curriculum, CurriculumRoles.curriculummanager.name());
					for(Identity manager:managers) {
						curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculumowner);
						curriculumService.removeMember(curriculum, manager, CurriculumRoles.curriculummanager);
					}
					dbInstance.commitAndCloseSession();
				}
				
				log.info("End migration of curriculum managers");
				allOk = true;
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATE_CURRICULUM_MANAGERS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private List<Long> loadAllCurriculumsKeys() {
		String query = "select cur.key from curriculum cur";
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, Long.class)
			.getResultList();
	}
	
}
