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
	
	private static final int BATCH_SIZE = 50000;
	
	private static final String VERSION = "OLAT_15.0.3";
	private static final String ASSESSMENT_LAST_ATTEMPTS = "ASSESSMENT LAST ATTEMPTS";
	
	private final AtomicInteger migrationLogCounter = new AtomicInteger(0);
	private final AtomicInteger migrationEntryCounter = new AtomicInteger(0);
	
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
				int counter = 0;
				List<LoggingObject> loggedLaunches;
				do {
					loggedLaunches = getLoggedLastAttempts(counter, BATCH_SIZE);
					migrateLoggedAttempts(loggedLaunches);
					counter += loggedLaunches.size();
					log.info(Tracing.M_AUDIT, "Log launch processed: {}, total processed ({})", loggedLaunches.size(), counter);
					dbInstance.commitAndCloseSession();
				} while(loggedLaunches.size() == BATCH_SIZE);

				log.info(Tracing.M_AUDIT, "Log launch processing successful, total processed: {}", migrationLogCounter);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			try {
				
				int counter = 0;
				List<AssessmentEntry> entries;
				do {
					entries = getEmptyLastAttempts(counter, BATCH_SIZE);
					migrateEmptyLastAttempts(entries);
					counter += entries.size();
					log.info(Tracing.M_AUDIT, "Empty last attempts processed: {}, total processed ({})", entries.size(), counter);
					dbInstance.commitAndCloseSession();
				} while(entries.size() == BATCH_SIZE);
				
				log.info("Assessment entry last attemps processing successful, total processed: {}", migrationEntryCounter);
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

	private List<LoggingObject> getLoggedLastAttempts(int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select log");
		sb.append("  from loggingobject log");
		sb.append(" where log.actionObject = 'testattempts'");
		sb.append(" order by log.creationDate desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LoggingObject.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private void migrateLoggedAttempts(List<LoggingObject> loggedAttempts) {
		Map<Long, Identity> identityCache = new HashMap<>();
		Map<Long, RepositoryEntry> entryCache = new HashMap<>();
		
		for (LoggingObject loggingObject : loggedAttempts) {
			try {
				migrateLastAttempt(loggingObject, identityCache, entryCache);
				migrationLogCounter.incrementAndGet();
			} catch (Exception e) {
				log.warn("Assessment last attempt (log table) not migrated. Id={}", loggingObject.getKey());
			}
			if(migrationLogCounter.get() % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
			if(migrationLogCounter.get() % 100 == 0) {
				log.info("Assessment: num. of last attempts (log table): {}", migrationLogCounter);
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
	
	private List<AssessmentEntry> getEmptyLastAttempts(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select data from assessmententry data");
		sb.append(" where data.attempts > 0 and lastAttempt is null");
		sb.append(" order by data.key asc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}

	private void migrateEmptyLastAttempts(List<AssessmentEntry> entries) {
		for (AssessmentEntry assessmentEntry: entries) {
			try {
				if (assessmentEntry.getAttempts() != null && assessmentEntry.getAttempts().intValue() > 0 && assessmentEntry.getLastAttempt() == null) {
					Date lastAttemps = assessmentEntry.getLastUserModified()!= null
						? assessmentEntry.getLastUserModified()
						: assessmentEntry.getLastModified();
					assessmentEntry.setLastAttempt(lastAttemps);
					assessmentService.updateAssessmentEntry(assessmentEntry);
				}
				migrationEntryCounter.incrementAndGet();
			} catch (Exception e) {
				log.warn("Assessment last attempt (empty) not migrated. Id={}", assessmentEntry.getKey());
			}
			if(migrationEntryCounter.get() % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
			if(migrationEntryCounter.get() % 100 == 0) {
				log.info("Assessment: num. of last attempts (empty): {}", migrationEntryCounter);
			}
		}	
	}


}
