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
import org.olat.core.logging.Tracing;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemSearchParams;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.PerformanceClass;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.04.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_16_2_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_16_2_3.class);

	private static final String VERSION = "OLAT_16.2.3";
	private static final String GRADE_SYSTEM_PASSED = "GRADE SYSTEM PASSED";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GradeService gradeService;
	
	public OLATUpgrade_16_2_3() {
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
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_16_2_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_16_2_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean initGradeSystemIdents(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(GRADE_SYSTEM_PASSED)) {
			try {
				log.info("Start grade system passed initialization.");
				initCourseElements();
				dbInstance.commitAndCloseSession();
				log.info("All grade system passed initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(GRADE_SYSTEM_PASSED, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void initCourseElements() {
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		List<GradeSystem> gradeSystems = gradeService.getGradeSystems(searchParams);

		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (GradeSystem gradeSystem : gradeSystems) {
			initGradeSystempassed(gradeSystem);
			migrationCounter.incrementAndGet();
			dbInstance.commitAndCloseSession();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Init grade system passed: num. of grade systems: {}", migrationCounter);
			}
		}
	}
	
	private void initGradeSystempassed(GradeSystem gradeSystem) {
		boolean hasPassed = false;
		if (GradeSystemType.numeric == gradeSystem.getType() && gradeSystem.getCutValue() != null) {
			hasPassed = true;
		} else if (GradeSystemType.text == gradeSystem.getType()) {
			hasPassed = gradeService.getPerformanceClasses(gradeSystem).stream().anyMatch(PerformanceClass::isPassed);
		}
		gradeSystem.setPassed(hasPassed);
		gradeService.updateGradeSystem(gradeSystem);
	}
	
}
