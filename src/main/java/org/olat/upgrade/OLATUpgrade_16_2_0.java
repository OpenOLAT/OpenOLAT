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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.coach.CoachingUserToolExtension;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 feb. 2022<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_16_2_0 extends OLATUpgrade {
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_16_2_0.class);
	
	private static final String VERSION = "OLAT_16.2.0";
	private static final String MIGRATE_HELP_PROVIDER = "MIGRATE HELP PROVIDER";
	private static final String ADD_USER_TOOL_COACHING = "ADD USER TOOL COACHING";
	private static final String RESET_USER_VISIBILITY = "REST USER VISIBILITY";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private UserToolsModule userToolsModule;

	
	public OLATUpgrade_16_2_0() {
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
		allOk &= migrateHelpProvider(upgradeManager, uhd);
		allOk &= migrateUserToolCoaching(upgradeManager, uhd);
		allOk &= resetUserVisibility(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_16_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_16_2_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	/**
	 * OpenOlat 16.2 has a new manual and a new context help provider. All help
	 * references have been migrated and the old confluence provider is no longer
	 * supported
	 * 
	 * @param upgradeManager
	 * @param uhd
	 * @return
	 */
	private boolean migrateHelpProvider(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_HELP_PROVIDER)) {
			try {
				helpModule.migrateConfluenceToOODocs();
				

				log.info("OO Help provider migrated");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATE_HELP_PROVIDER, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean migrateUserToolCoaching(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ADD_USER_TOOL_COACHING)) {
			try {
				String availableTools = userToolsModule.getAvailableUserTools();
				if(StringHelper.containsNonWhitespace(availableTools) && !availableTools.contains(CoachingUserToolExtension.COACHING_USER_TOOL_ID)) {
					availableTools += "," + CoachingUserToolExtension.COACHING_USER_TOOL_ID;
				}
				userToolsModule.setAvailableUserTools(availableTools);
				log.info("User tool coaching added");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(ADD_USER_TOOL_COACHING, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean resetUserVisibility(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(RESET_USER_VISIBILITY)) {
			try {
				log.info("Start reset user visibility.");
				resetUserVisibilties();
				dbInstance.commitAndCloseSession();
				log.info("Reset user visibility done.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(RESET_USER_VISIBILITY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void resetUserVisibilties() {
		List<AssessmentEntry> assessmentEntries = getAssessmentEntries();

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			assessmentEntry.setUserVisibility(null);
			dbInstance.getCurrentEntityManager().merge(assessmentEntry);
			migrationCounter.incrementAndGet();
			if(migrationCounter.get() % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
			if(migrationCounter.get() % 100 == 0) {
				log.info("Reset user visibility: {}", migrationCounter);
			}
		}
	}
	
	private List<AssessmentEntry> getAssessmentEntries() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select aentry");
		sb.append("  from assessmententry aentry");
		sb.append("       inner join courseelement courseele");
		sb.append("               on courseele.repositoryEntry.key = aentry.repositoryEntry.key");
		sb.append("              and courseele.subIdent = aentry.subIdent");
		sb.append("              and courseele.type = '").append(IQTESTCourseNode.TYPE).append("'");
		sb.and().append("(aentry.status").in(AssessmentEntryStatus.notReady, AssessmentEntryStatus.notStarted, AssessmentEntryStatus.inProgress);
		sb.append(" or aentry.status is null)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.getResultList();
	}
}
