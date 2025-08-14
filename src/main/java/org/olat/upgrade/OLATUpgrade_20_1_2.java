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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_20_1_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_1_2.class);

	private static final String VERSION = "OLAT_20.1.2";
	private static final String UPDATE_CURRICULUM_DEFAULT_ELEMENT = "CURRICULUM DEFAULT ELEMENT";

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
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
		allOk &= updateCoursesCurriculumDefaultElement(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_1_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_1_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean updateCoursesCurriculumDefaultElement(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(UPDATE_CURRICULUM_DEFAULT_ELEMENT)) {
			try {
				log.info("Update repository entry to curriculum default element relation");
				
				int count = 0;
				List<Long> entriesKeys = getRepositoryEntriesWithElements();
				for(Long entryKey:entriesKeys) {
					boolean updated = updateCourseCurriculumDefaultElement(entryKey);
					dbInstance.commitAndCloseSession();
					if(updated) {
						count++;
					}
				}
				
				log.info("End update repository entry to curriculum default element relation: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(UPDATE_CURRICULUM_DEFAULT_ELEMENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private boolean updateCourseCurriculumDefaultElement(Long entryKey) {
		RepositoryEntry entry = repositoryEntryDao.loadReferenceByKey(entryKey);
		boolean defaultElement = repositoryEntryRelationDao.hasDefaultElement(entry);
		if(!defaultElement) {
			List<RepositoryEntryToGroupRelation> relations =  repositoryEntryRelationDao.getCurriculumRelations(entry);
			if(!relations.isEmpty()) {
				if(relations.size() > 1) {
					Collections.sort(relations, new CurriculumElementComparator());
				}

				RepositoryEntryToGroupRelation relation = relations.get(0);
				relation.setDefaultElement(true);
				dbInstance.getCurrentEntityManager().merge(relation);
				return true;
			}
		}
		return false;
	}
	
	private List<Long> getRepositoryEntriesWithElements() {
		String query = """
				select distinct rel.entry.key from repoentrytogroup as relGroup
				inner join relGroup.group as bGroup
				inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)
				inner join curriculumelement as curEl on (rel.group.key=curEl.group.key)""";

		return dbInstance.getCurrentEntityManager()
			.createQuery(query, Long.class)
			.getResultList();
	}
	
	private static class CurriculumElementComparator implements Comparator<RepositoryEntryToGroupRelation> {

		@Override
		public int compare(RepositoryEntryToGroupRelation o1, RepositoryEntryToGroupRelation o2) {
			Date cd1 = o1.getCreationDate();
			Date cd2 = o2.getCreationDate();
			
			int c = cd1.compareTo(cd2);
			if(c == 0) {
				c = o1.getKey().compareTo(o2.getKey());
			}
			return c;
		}
	}
}
