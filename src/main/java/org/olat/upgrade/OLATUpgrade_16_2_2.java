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
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.04.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_16_2_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_16_2_2.class);

	private static final String VERSION = "OLAT_16.2.2";
	private static final String GRADE_SYSTEM_IDENT = "GRADE SYSTEM IDENT";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private AssessmentService assessmentService;
	
	public OLATUpgrade_16_2_2() {
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
		
		allOk &= initGradeSystemIdents(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_16_2_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_16_2_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean initGradeSystemIdents(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(GRADE_SYSTEM_IDENT)) {
			try {
				log.info("Start grade system ident initialization.");
				initCourseElements();
				dbInstance.commitAndCloseSession();
				log.info("All grade system idents initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(GRADE_SYSTEM_IDENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void initCourseElements() {
		List<AssessmentEntry> assessmentEntries = getAssessmentEntriesWithGrade();

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			initGradeSystemIdent(assessmentEntry);
			migrationCounter.incrementAndGet();
			dbInstance.commitAndCloseSession();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Init grade system idents: num. of assessment entries: {}", migrationCounter);
			}
		}
	}
	
	private List<AssessmentEntry> getAssessmentEntriesWithGrade() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae");
		sb.append("  from assessmententry ae");
		sb.and().append("ae.grade is not null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.getResultList();
	}
	
	private void initGradeSystemIdent(AssessmentEntry assessmentEntry) {
		try {
			GradeSystem gradeSystem = gradeService.getGradeSystem(() -> assessmentEntry.getRepositoryEntry().getKey(), assessmentEntry.getSubIdent());
			if (gradeSystem != null) {
				assessmentEntry.setGradeSystemIdent(gradeSystem.getIdentifier());
				assessmentService.updateAssessmentEntry(assessmentEntry);
			}
		} catch (Exception e) {
			log.error("Error in init grade system ident {} ({}).", assessmentEntry.getKey(),
					assessmentEntry.getGrade());
			log.error("", e);
		}
	}
	
}
