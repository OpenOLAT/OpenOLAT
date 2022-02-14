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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_16_1_3 extends OLATUpgrade {
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_16_1_3.class);
	
	private static final String VERSION = "OLAT_16.1.3";
	private static final String INIT_ASSESSMENT_ENTRY_TEST = "INIT ASESSMENT ENTRY TEST PORTFOLIO";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	
	public OLATUpgrade_16_1_3() {
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
		allOk &= initAssessmentEntry(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_16_1_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_16_1_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean initAssessmentEntry(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_ASSESSMENT_ENTRY_TEST)) {
			try {
				migrateRepositoryEntriesAssessmentEntries();
				dbInstance.commitAndCloseSession();
				log.info("All test and portfolio assessment entries initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(INIT_ASSESSMENT_ENTRY_TEST, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateRepositoryEntriesAssessmentEntries() {
		List<RepositoryEntry> entries = getRepositoryEntries();

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (RepositoryEntry repositoryEntry:entries) {
			initializeAssessmentEntries(repositoryEntry);
			migrationCounter.incrementAndGet();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Assessment entries: num. of repository entries initialized: {}", migrationCounter);
			}
		}
	}
	
	private void initializeAssessmentEntries(RepositoryEntry repositoryEntry) {
		List<Identity> participants = repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		for(Identity participant:participants) {
			assessmentService.getOrCreateAssessmentEntry(participant, null, repositoryEntry, null, Boolean.TRUE, repositoryEntry);
		}
		dbInstance.commitAndCloseSession();
		
		if(ImsQTI21Resource.TYPE_NAME.equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
			List<Identity> ownersAndCoachesOnly = repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name(), GroupRoles.coach.name());
			ownersAndCoachesOnly.removeAll(participants);
			for(Identity identity:ownersAndCoachesOnly) {
				AssessmentEntry asEntry = assessmentService.loadAssessmentEntry(identity, repositoryEntry, null, repositoryEntry);
				if(asEntry != null) {
					deleteTestAssessmentEntry(identity, asEntry, repositoryEntry);
				}
			}
		}
	}
	
	/**
	 * Delete all author test sessions, and if no sessions is left, remove the
	 * assessment entry.
	 * 
	 * @param identity The identity
	 * @param asEntry The assessment entry
	 * @param testEntry The test entry
	 */
	private void deleteTestAssessmentEntry(Identity identity, AssessmentEntry asEntry, RepositoryEntry testEntry) {
		try {
			boolean allAuthorMode = true;
			List<AssessmentTestSession> testSessions = getAssessmentTestSessions(testEntry, identity);
			for(AssessmentTestSession testSession:testSessions) {
				if(testSession.isAuthorMode()) {
					qtiService.deleteAuthorAssessmentTestSession(testEntry, testSession);
				}
				allAuthorMode &= testSession.isAuthorMode();
			}
			dbInstance.commit();
			
			if(allAuthorMode) {
				asEntry = assessmentEntryDao.loadAssessmentEntryById(asEntry.getKey());
				dbInstance.getCurrentEntityManager().remove(asEntry);
			}
			dbInstance.commit();
		} catch (Exception e) {
			log.error("Cannot cleanup assessment entry of {} for test {} ({})", identity.getKey(), testEntry.getDisplayname(), testEntry.getKey(), e);
		} finally {
			dbInstance.commitAndCloseSession();
		}
	}
	
	private List<RepositoryEntry> getRepositoryEntries() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select re from repositoryentry re")
		  .append(" inner join fetch re.olatResource as ores")
		  .and().append(" ores.resName ").in(ImsQTI21Resource.TYPE_NAME, BinderTemplateResource.TYPE_NAME)
		  .append(" order by re.statistics.lastUsage desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.getResultList();
	}
	
	private List<AssessmentTestSession> getAssessmentTestSessions(RepositoryEntryRef testEntry, IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" left join fetch session.testEntry testEntry")
		  .append(" left join fetch testEntry.olatResource testResource")
		  .and().append(" session.repositoryEntry.key=:repositoryEntryKey")
		  .and().append(" session.testEntry.key=:repositoryEntryKey")
		  .and().append(" session.identity.key=:identityKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("repositoryEntryKey", testEntry.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

}
