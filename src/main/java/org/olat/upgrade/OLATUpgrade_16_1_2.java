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
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_16_1_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_16_1_2.class);
	
	private static final String VERSION = "OLAT_16.1.2";
	private static final String INIT_ASSESSMENT_ENTRY_CONVENTIONAL_COURSES = "INIT ASESSMENT ENTRY CONVETIONAL COURSES";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public OLATUpgrade_16_1_2() {
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

		// OLATUpgrade_15_pre_6_ae has only to run if this upgrade (OLATUpgrade_15_pre_0) 
		// is run before the fix for the completion of assessment entries of conventional courses.
		upgradeManager.setUpgradesHistory(uhd, OLATUpgrade_15_pre_6_ae.VERSION);
		
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_16_1_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_16_1_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	
	private boolean initAssessmentEntry(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_ASSESSMENT_ENTRY_CONVENTIONAL_COURSES)) {
			try {
				migrateCourseAssessmentEntries();
				dbInstance.commitAndCloseSession();
				log.info("All assessment entries initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(INIT_ASSESSMENT_ENTRY_CONVENTIONAL_COURSES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void migrateCourseAssessmentEntries() {
		List<RepositoryEntry> courseEntries = getCourseEntries();

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (RepositoryEntry repositoryEntry : courseEntries) {
			migrateCourseAssessmentEntries(repositoryEntry);
			migrationCounter.incrementAndGet();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Assessment entries: num. of courses migrated: {}", migrationCounter);
			}
		}
	}

	private List<RepositoryEntry> getCourseEntries() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select re");
		sb.append("  from repositoryentry re");
		sb.append("       inner join fetch re.olatResource as ores");
		sb.and().append(" ores.resName = 'CourseModule'");
		sb.and().append(" re.technicalType = 'condition'");
		sb.and().append(" re.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
		sb.append(" order by re.statistics.lastUsage desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.getResultList();
	}

	private void migrateCourseAssessmentEntries(RepositoryEntry repositoryEntry) {
		try {
			ICourse course = CourseFactory.loadCourse(repositoryEntry);
			if (course != null) {
				log.info("Assessment entries migration started: course {} ({}).",
						repositoryEntry.getKey(), repositoryEntry.getDisplayname());
				courseAssessmentService.evaluateAll(course);
				log.info("Assessment entries migrated: course {} ({}).",
						repositoryEntry.getKey(), repositoryEntry.getDisplayname());
			}
		} catch (CorruptedCourseException cce) {
			log.warn("CorruptedCourseException in migration of assessment entries of course {} ({})",
					repositoryEntry.getKey(), repositoryEntry.getDisplayname());
		} catch (Exception e) {
			log.error("Error in migration of assessment entries of course {} ({}).", repositoryEntry.getKey(),
					repositoryEntry.getDisplayname());
			log.error("", e);
		}
	}
	
}
