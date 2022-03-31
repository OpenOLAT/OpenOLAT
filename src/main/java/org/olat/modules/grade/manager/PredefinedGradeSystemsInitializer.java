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
package org.olat.modules.grade.manager;

import java.math.BigDecimal;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.Rounding;

/**
 * 
 * Initial date: 31 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PredefinedGradeSystemsInitializer {

	private final DB dbInstance;
	private final GradeService gradeService;
	private final GradeSystemDAO gradeSystemDao;
	
	public PredefinedGradeSystemsInitializer(DB dbInstance, GradeService gradeService, GradeSystemDAO gradeSystemDao) {
		this.dbInstance = dbInstance;
		this.gradeService = gradeService;
		this.gradeSystemDao = gradeSystemDao;
	}

	public void init() {
		if (gradeService.isGradeServiceIdentifierAvailable("oo.grades.de")) {
			GradeSystem gradeSystem = gradeSystemDao.create("oo.grades.de", GradeSystemType.numeric, true);
			gradeSystem.setEnabled(true);
			gradeSystem.setResolution(NumericResolution.half);
			gradeSystem.setRounding(Rounding.nearest);
			gradeSystem.setBestGrade(Integer.valueOf(1));
			gradeSystem.setLowestGrade(Integer.valueOf(6));
			gradeSystem.setCutValue(BigDecimal.valueOf(4));
			gradeSystem = gradeService.updateGradeSystem(gradeSystem);
			dbInstance.commitAndCloseSession();
		}
		
		if (gradeService.isGradeServiceIdentifierAvailable("oo.grades.ch")) {
			GradeSystem gradeSystem = gradeSystemDao.create("oo.grades.ch", GradeSystemType.numeric, true);
			gradeSystem.setEnabled(true);
			gradeSystem.setResolution(NumericResolution.half);
			gradeSystem.setRounding(Rounding.nearest);
			gradeSystem.setBestGrade(Integer.valueOf(6));
			gradeSystem.setLowestGrade(Integer.valueOf(1));
			gradeSystem.setCutValue(BigDecimal.valueOf(4));
			gradeSystem = gradeService.updateGradeSystem(gradeSystem);
			dbInstance.commitAndCloseSession();
		}
		
		if (gradeService.isGradeServiceIdentifierAvailable("oo.levels.4")) {
			GradeSystem gradeSystem = gradeSystemDao.create("oo.levels.4", GradeSystemType.text, true);
			gradeSystem.setEnabled(true);
			gradeSystem = gradeService.updateGradeSystem(gradeSystem);
			
			int counter = 1;
			PerformanceClass performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.levels.4.1");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(true);
			gradeService.updatePerformanceClass(performanceClass);
			
			performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.levels.4.2");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(true);
			gradeService.updatePerformanceClass(performanceClass);
			
			performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.levels.4.3");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(true);
			gradeService.updatePerformanceClass(performanceClass);
			
			performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.levels.4.4");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(true);
			gradeService.updatePerformanceClass(performanceClass);
			
			dbInstance.commitAndCloseSession();
		}
		
		if (gradeService.isGradeServiceIdentifierAvailable("oo.emojis.5")) {
			GradeSystem gradeSystem = gradeSystemDao.create("oo.emojis.5", GradeSystemType.text, true);
			gradeSystem.setEnabled(true);
			gradeSystem = gradeService.updateGradeSystem(gradeSystem);
			
			int counter = 1;
			PerformanceClass performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.emojis.5.1");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(true);
			gradeService.updatePerformanceClass(performanceClass);
			
			performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.emojis.5.2");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(true);
			gradeService.updatePerformanceClass(performanceClass);
			
			performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.emojis.5.3");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(true);
			gradeService.updatePerformanceClass(performanceClass);
			
			performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.emojis.5.4");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(false);
			gradeService.updatePerformanceClass(performanceClass);
			
			performanceClass = gradeService.createPerformanceClass(gradeSystem, "oo.emojis.5.5");
			performanceClass.setBestToLowest(counter++);
			performanceClass.setPassed(false);
			gradeService.updatePerformanceClass(performanceClass);
			
			dbInstance.commitAndCloseSession();
		}
	}

}
