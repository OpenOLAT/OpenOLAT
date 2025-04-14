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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.references.ReferenceHistory;
import org.olat.resource.references.ReferenceHistoryDAO;
import org.olat.resource.references.ReferenceHistoryImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_19_1_10 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_1_10.class);

	private static final String VERSION = "OLAT_19.1.10";

	private static final String REFERENCE_HISTORY = "REFERENCE HISTORY";

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ReferenceHistoryDAO referenceHistoryDao;

	public OLATUpgrade_19_1_10() {
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
		allOk &= updateQuestionPoolMaxScore(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_1_10 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_1_10 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updateQuestionPoolMaxScore(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(REFERENCE_HISTORY)) {
			try {
				log.info("Start building reference history for QTI course elements.");
				
				List<Long> courseEntriesKeys = getCourseEntriesKeys();
				for(int i=0; i<courseEntriesKeys.size(); i++) {
					RepositoryEntry courseEntry = repositoryService.loadByKey(courseEntriesKeys.get(i));
					buildReferencesHistory(courseEntry);
					dbInstance.commitAndCloseSession();
					if(i % 25 == 0) {
						log.info(Tracing.M_AUDIT, "Course reference history build: {} / {}", i, courseEntriesKeys.size());
					}
				}
				log.info("End building reference history for QTI course elements.");
				
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(REFERENCE_HISTORY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
	private void buildReferencesHistory(RepositoryEntry courseEntry) {
		String sb = """
			select testSession.testEntry.key, testSession.subIdent, min(testSession.creationDate)
			from qtiassessmenttestsession testSession
			where testSession.repositoryEntry.key=:courseEntryKey
			group by testSession.testEntry.key, testSession.subIdent""";
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb, Object[].class)
				.setParameter("courseEntryKey", courseEntry.getKey())
				.getResultList();
		
		List<ReferenceHistory> courseHistory = referenceHistoryDao.loadHistory(courseEntry.getOlatResource(), null);
		
		for(Object[] object:objects) {
			Long testKey = (Long)object[0];
			String subIdent = (String)object[1];
			Date date = (Date)object[2];
			if(testKey != null && testKey.longValue() > 0
					&& StringHelper.containsNonWhitespace(subIdent)
					&& date != null) {
				
				RepositoryEntry testEntry = repositoryService.loadByKey(testKey);
				if(testEntry != null && !hasReferenceHistory(testEntry, subIdent, courseHistory)) {
					addReferenceHistory(courseEntry.getOlatResource(), testEntry.getOlatResource(), subIdent, date);
				}
			}
		}
	}
	
	private ReferenceHistory addReferenceHistory(OLATResource source, OLATResource target, String userdata, Date date) {
		ReferenceHistoryImpl ref = new ReferenceHistoryImpl();
		ref.setCreationDate(date);
		ref.setSource(source);
		ref.setTarget(target);
		ref.setUserdata(userdata);
		dbInstance.getCurrentEntityManager().persist(ref);
		return ref;
	}
	
	private boolean hasReferenceHistory(RepositoryEntry testEntry, String subIdent, List<ReferenceHistory> courseHistory) {
		if(courseHistory != null && !courseHistory.isEmpty()) {
			for(ReferenceHistory ref:courseHistory) {
				if(subIdent.equals(ref.getUserdata())
						&& testEntry.getOlatResource().getKey().equals(ref.getTarget().getKey())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private List<Long> getCourseEntriesKeys() {
		String sb = """
			select re.key from repositoryentry re
			inner join re.olatResource as ores
			where ores.resName = 'CourseModule'
			order by re.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, Long.class)
				.getResultList();
	}
}
