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
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.core.CourseNodeService;
import org.olat.group.BusinessGroupModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.10.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_16_1_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_16_1_0.class);

	private static final String VERSION = "OLAT_16.1.0";
	private static final String UPDATE_ASSESSMENT_OBLIGATION = "UPDATE ASSESSMENT OBLIGATION";
	private static final String INIT_COURSE_ELEMENT = "INIT COURSE ELEMENT";
	private static final String GROUP_LIFECYCLE = "GROUP LIFECYCLE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CourseNodeService courseNodeService;
	@Autowired
	private BusinessGroupModule businessGroupModule;

	public OLATUpgrade_16_1_0() {
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
		
		allOk &= updateGroupLifecycle(upgradeManager, uhd);
		allOk &= updateAssessmentObligation(upgradeManager, uhd);
		allOk &= initCourseElements(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_16_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_16_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updateGroupLifecycle(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(GROUP_LIFECYCLE)) {
			try {
				businessGroupModule.setAutomaticGroupInactivationEnabled("disabled");
				businessGroupModule.setAutomaticGroupSoftDeleteEnabled("disabled");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
			uhd.setBooleanDataValue(GROUP_LIFECYCLE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private boolean updateAssessmentObligation(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(UPDATE_ASSESSMENT_OBLIGATION)) {
			try {
				String query = "update assessmententry ae set ae.obligationConfig = ae.obligation";
				dbInstance.getCurrentEntityManager()
						.createQuery(query)
						.executeUpdate();
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
			
			uhd.setBooleanDataValue(UPDATE_ASSESSMENT_OBLIGATION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private boolean initCourseElements(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_COURSE_ELEMENT)) {
			try {
				initCourseElements();
				dbInstance.commitAndCloseSession();
				log.info("All course elements initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INIT_COURSE_ELEMENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void initCourseElements() {
		List<RepositoryEntry> courseEntries = getCourseEntries();

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (RepositoryEntry repositoryEntry : courseEntries) {
			initCourseElements(repositoryEntry);
			migrationCounter.incrementAndGet();
			dbInstance.commitAndCloseSession();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Init course elements: num. of courses migrated: {}", migrationCounter);
			}
		}
	}
	
	private List<RepositoryEntry> getCourseEntries() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select re");
		sb.append("  from repositoryentry re");
		sb.append("       inner join re.olatResource as ores");
		sb.and().append(" ores.resName = 'CourseModule'");
		sb.append(" order by re.statistics.lastUsage desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.getResultList();
	}
	
	private void initCourseElements(RepositoryEntry repositoryEntry) {
		try {
			ICourse course = CourseFactory.loadCourse(repositoryEntry);
			if (course != null) {
				Structure runStructure = course.getCourseEnvironment().getRunStructure();
				if (runStructure != null) {
					courseNodeService.syncCourseElements(course);
					log.info("Init course elements done: course {} ({}).",
							repositoryEntry.getKey(), repositoryEntry.getDisplayname());
				}
			}
		} catch (CorruptedCourseException cce) {
			log.warn("CorruptedCourseException in init course elements of course {} ({})",
					repositoryEntry.getKey(), repositoryEntry.getDisplayname());
		} catch (Exception e) {
			log.error("Error in init course elements of course {} ({}).", repositoryEntry.getKey(),
					repositoryEntry.getDisplayname());
			log.error("", e);
		}
	}
	
}
