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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.model.PerformanceClassImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PerformanceClassDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GradeSystemDAO gradeSystemDAO;
	
	@Autowired
	private PerformanceClassDAO sut;
	
	@Test
	public void shouldCreatePerformanceClass() {
		GradeSystem gradeSystem = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		dbInstance.commitAndCloseSession();
		
		String identifier = random();
		PerformanceClass performanceClass = sut.create(gradeSystem, identifier);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(((PerformanceClassImpl)performanceClass).getKey()).isNotNull();
		softly.assertThat(((PerformanceClassImpl)performanceClass).getCreationDate()).isNotNull();
		softly.assertThat(((PerformanceClassImpl)performanceClass).getLastModified()).isNotNull();
		softly.assertThat(performanceClass.getGradeSystem()).isEqualTo(gradeSystem);
		softly.assertThat(performanceClass.getIdentifier()).isEqualTo(identifier);
		softly.assertThat(performanceClass.getBestToLowest()).isEqualTo(1000);
		softly.assertThat(performanceClass.isPassed()).isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldSave() {
		GradeSystem gradeSystem = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		PerformanceClass performanceClass = sut.create(gradeSystem, random());
		dbInstance.commitAndCloseSession();
		
		int bestToLowest = 3;
		performanceClass.setBestToLowest(bestToLowest);
		performanceClass.setPassed(true);
		performanceClass = sut.save(performanceClass);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(performanceClass.getBestToLowest()).isEqualTo(bestToLowest);
		softly.assertThat(performanceClass.isPassed()).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadByGradeSystem() {
		GradeSystem gradeSystem1 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		PerformanceClass performanceClass11 = sut.create(gradeSystem1, random());
		PerformanceClass performanceClass12 = sut.create(gradeSystem1, random());
		GradeSystem gradeSystem2 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		PerformanceClass performanceClass21 = sut.create(gradeSystem2, random());
		dbInstance.commitAndCloseSession();
		
		List<PerformanceClass> performanceClasses = sut.load(gradeSystem1);
		
		assertThat(performanceClasses)
				.containsExactlyInAnyOrder(performanceClass11, performanceClass12)
				.doesNotContain(performanceClass21);
	}

	@Test
	public void shouldDeleteByKey() {
		GradeSystem gradeSystem1 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		PerformanceClass performanceClass11 = sut.create(gradeSystem1, random());
		PerformanceClass performanceClass12 = sut.create(gradeSystem1, random());
		GradeSystem gradeSystem2 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		PerformanceClass performanceClass21 = sut.create(gradeSystem2, random());
		dbInstance.commitAndCloseSession();
		
		sut.delete(performanceClass11);
		dbInstance.commitAndCloseSession();
		
		List<PerformanceClass> performanceClasses = sut.load(gradeSystem1);
		
		assertThat(performanceClasses)
				.containsExactlyInAnyOrder(performanceClass12)
				.doesNotContain(performanceClass11, performanceClass21);
	}

	@Test
	public void shouldDeleteByGradSystem() {
		GradeSystem gradeSystem1 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		sut.create(gradeSystem1, random());
		sut.create(gradeSystem1, random());
		GradeSystem gradeSystem2 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		sut.create(gradeSystem2, random());
		dbInstance.commitAndCloseSession();
		
		sut.delete(gradeSystem1);
		dbInstance.commitAndCloseSession();
		
		List<PerformanceClass> performanceClasses = sut.load(gradeSystem1);
		assertThat(performanceClasses).isEmpty();
		performanceClasses = sut.load(gradeSystem2);
		assertThat(performanceClasses).hasSize(1);
	}

}
