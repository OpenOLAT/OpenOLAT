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
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_20_3_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_3_0.class);

	private static final int BATCH_SIZE = 20;
	private static final String VERSION = "OLAT_20.3.0";
	private static final String MIGRATE_RELATION_TO_IMPLEMENTATION = "RELATION TO IMPLEMENTATION";


	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	
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
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_3_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_3_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean migrateCatalogCardView(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_RELATION_TO_IMPLEMENTATION)) {
			try {
				log.info("Migration relations to implementations");
				
				int count = 0;
				List<CurriculumElement> elements = getElements(BATCH_SIZE);
				do {
					for(CurriculumElement element:elements) {
						List<Long> path = element.getMaterializedPathKeysList();
						if(path != null && !path.isEmpty()) {
							Long implementationKey = path.get(0);
							CurriculumElement implementation = curriculumElementDao.loadReference(implementationKey);
							((CurriculumElementImpl)element).setImplementation(implementation);
							curriculumService.updateCurriculumElement(element);
						}
						
						if(count++ % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					dbInstance.commitAndCloseSession();
					elements = getElements(BATCH_SIZE);
					
				} while(!elements.isEmpty());
				
				log.info("End migration relations to implementations: {}", count);
				
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_RELATION_TO_IMPLEMENTATION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<CurriculumElement> getElements(int maxResults) {
		String query = """
				select curEl from curriculumelement as curEl
				where curEl.parent.key is not null and curEl.implementation.key is null
				order by curEl.key""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CurriculumElement.class)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}
}
