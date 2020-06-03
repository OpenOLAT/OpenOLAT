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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.util.StringHelper;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.05.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_0_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_0_3.class);
	
	private static final String VERSION = "OLAT_15.0.3";
	private static final String ASSESSMENT_LAST_ATTEMPTS = "ASSESSMENT LAST ATTEMPTS";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private BaseSecurity securityManager;
	
	public OLATUpgrade_15_0_3() {
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
		allOk &= migrateLastAttempts(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_0_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_0_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean migrateLastAttempts(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ASSESSMENT_LAST_ATTEMPTS)) {
			try {
				List<LoggingObject> loggedLaunches = getLoggedLastAttempts();
				migrateLoggedAttempts(loggedLaunches);
				List<AssessmentEntry> entries = getEmptyLastAttempts();
				migrateEmptyLastAttempts(entries);
				log.info("Assessment last modified migrated.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(ASSESSMENT_LAST_ATTEMPTS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<LoggingObject> getLoggedLastAttempts() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select log");
		sb.append("  from loggingobject log");
		sb.append(" where log.actionObject = 'testattempts'");
		sb.append(" order by log.creationDate desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LoggingObject.class)
				.getResultList();
	}
	
	private void migrateLoggedAttempts(List<LoggingObject> loggedAttempts) {
		log.info("Migraton of {} assessment last attemps (log table) started.", loggedAttempts.size());
		
		Map<Long, Identity> identityCache = new HashMap<>();
		Map<Long, RepositoryEntry> entryCache = new HashMap<>();
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (LoggingObject loggingObject : loggedAttempts) {
			try {
				migrateLastAttempt(loggingObject, identityCache, entryCache);
				migrationCounter.incrementAndGet();
			} catch (Exception e) {
				log.warn("Assessment last attempt (log table) not migrated. Id={}", loggingObject.getKey());
			}
			if(migrationCounter.get() % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
			if(migrationCounter.get() % 100 == 0) {
				log.info("Assessment: num. of last attempts (log table): {}", migrationCounter);
			}
		}
	}

	private void migrateLastAttempt(LoggingObject loggingObject, Map<Long, Identity> identityCache, Map<Long, RepositoryEntry> entryCache) {
		String entryKeyStr = null;
		String nodeIdStr = null;
		String businessPath = loggingObject.getBusinessPath();
		
		int entryStart = businessPath.indexOf("[RepositoryEntry:");
		int entryEnd = businessPath.indexOf("]");
		if (entryStart > -1) {
			entryKeyStr = businessPath.substring(entryStart + "[RepositoryEntry:".length(), entryEnd);
		}
		
		int nodeStart = businessPath.indexOf("[CourseNode:");
		int nodeEnd = businessPath.indexOf("]", entryEnd + 2);
		if (nodeStart > -1) {
			nodeIdStr = businessPath.substring(nodeStart + "[CourseNode:".length(), nodeEnd);
		}
		
		if (StringHelper.containsNonWhitespace(entryKeyStr) && StringHelper.containsNonWhitespace(nodeIdStr)) {
			Long entryKey = Long.valueOf(entryKeyStr);
			Long userId = Long.valueOf(loggingObject.getUserId());
			Identity identity = identityCache.get(userId);
			if (identity == null) {
				identity = securityManager.loadIdentityByKey(userId);
				if (identity != null) {
					identityCache.put(userId, identity);
				}
			}
			
			if (identity != null) {
				RepositoryEntry entry = entryCache.get(entryKey);
				if (entry == null) {
					entry = repositoryEntryDao.loadByKey(entryKey);
					if (entry != null) {
						entryCache.put(entryKey, entry);
					}
				}
				
				if (entry != null) {
					AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(identity, null, entry, nodeIdStr, false, null);
					if (assessmentEntry.getAttempts() != null && assessmentEntry.getAttempts().intValue() > 0 && assessmentEntry.getLastAttempt() == null) {
						assessmentEntry.setLastAttempt(loggingObject.getCreationDate());
						assessmentService.updateAssessmentEntry(assessmentEntry);
					}
				}
			}
		}
	}
	
	private List<AssessmentEntry> getEmptyLastAttempts() {
		StringBuilder sb = new StringBuilder();
		sb.append("select data from assessmententry data");
		sb.append(" where data.attempts > 0 and lastAttempt is null");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.getResultList();
	}

	private void migrateEmptyLastAttempts(List<AssessmentEntry> entries) {
		log.info("Migraton of {} assessment last attemps (empty) started.", entries.size());
		
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (AssessmentEntry assessmentEntry: entries) {
			try {
				if (assessmentEntry.getAttempts() != null && assessmentEntry.getAttempts().intValue() > 0 && assessmentEntry.getLastAttempt() == null) {
					Date lastAttemps = assessmentEntry.getLastUserModified()!= null
						? assessmentEntry.getLastUserModified()
						: assessmentEntry.getLastModified();
					assessmentEntry.setLastAttempt(lastAttemps);
					assessmentService.updateAssessmentEntry(assessmentEntry);
				}
				migrationCounter.incrementAndGet();
			} catch (Exception e) {
				log.warn("Assessment last attempt (empty) not migrated. Id={}", assessmentEntry.getKey());
			}
			if(migrationCounter.get() % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
			if(migrationCounter.get() % 100 == 0) {
				log.info("Assessment: num. of last attempts (empty): {}", migrationCounter);
			}
		}	
	}


}
