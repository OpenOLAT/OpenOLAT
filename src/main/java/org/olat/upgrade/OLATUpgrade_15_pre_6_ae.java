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
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.03.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_pre_6_ae extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_pre_6_ae.class);
	
	static final String VERSION = "OLAT_15.pre.6.ae";
	private static final String CONDITION_COURSE_COMPLETION = "CONDITION COURSES COMPLETION";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public OLATUpgrade_15_pre_6_ae() {
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
		allOk &= migrateAssessmentEntrCompletion(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_pre_6_ae successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_pre_6_ae not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	
	private boolean migrateAssessmentEntrCompletion(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CONDITION_COURSE_COMPLETION)) {
			try {
				migrateCourseAssessmentEntries();
				dbInstance.commitAndCloseSession();
				log.info("All assessment entry completions migrated.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(CONDITION_COURSE_COMPLETION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void migrateCourseAssessmentEntries() {
		List<RepositoryEntry> courseEntries = getCourseEntries();

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (RepositoryEntry repositoryEntry : courseEntries) {
			calculateCourseAssessmentEntries(repositoryEntry);
			migrationCounter.incrementAndGet();
			if(migrationCounter.get() % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
			if(migrationCounter.get() % 100 == 0) {
				log.info("Assessment entries: num. of courses migrated: {}", migrationCounter);
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

	private void calculateCourseAssessmentEntries(RepositoryEntry repositoryEntry) {
		try {
			ICourse course = CourseFactory.loadCourse(repositoryEntry);
			if (course != null) {
				NodeAccessType nodeAccessType = NodeAccessType.of(course);
				if (ConditionNodeAccessProvider.TYPE.equals(nodeAccessType.getType())) {
					Structure runStructure = course.getCourseEnvironment().getRunStructure();
					if (runStructure != null) {
						courseAssessmentService.evaluateAll(course, true);
						log.info("Assessment entry completions calculated: course {} ({}).",
								repositoryEntry.getKey(), repositoryEntry.getDisplayname());
					}
				}
			}
		} catch (CorruptedCourseException cce) {
			log.warn("CorruptedCourseException in migration of assessment entry completion of course {} ({})",
					repositoryEntry.getKey(), repositoryEntry.getDisplayname());
		} catch (Exception e) {
			log.error("Error in migration of assessment entry completion of course {} ({}).", repositoryEntry.getKey(),
					repositoryEntry.getDisplayname());
			log.error("", e);
		}
	}

}
