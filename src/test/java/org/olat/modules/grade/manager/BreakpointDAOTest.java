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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.model.BreakpointImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BreakpointDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GradeScaleDAO gradeScaleDAO;
	
	@Autowired
	private BreakpointDAO sut;
	
	@Test
	public void shouldCreateBreakpoint() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		GradeScale gradeScale = gradeScaleDAO.create(repositoryEntry, random());
		dbInstance.commitAndCloseSession();
		
		Breakpoint breakpoint = sut.create(gradeScale);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(((BreakpointImpl)breakpoint).getKey()).isNotNull();
		softly.assertThat(((BreakpointImpl)breakpoint).getCreationDate()).isNotNull();
		softly.assertThat(((BreakpointImpl)breakpoint).getLastModified()).isNotNull();
		softly.assertThat(breakpoint.getGradeScale()).isEqualTo(gradeScale);
		softly.assertThat(breakpoint.getValue()).isNull();
		softly.assertThat(breakpoint.getGrade()).isNull();
		softly.assertThat(breakpoint.getBestToLowest()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldSave() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		GradeScale gradeScale = gradeScaleDAO.create(repositoryEntry, random());
		Breakpoint breakpoint = sut.create(gradeScale);
		dbInstance.commitAndCloseSession();
		
		BigDecimal value = BigDecimal.valueOf(3.3);
		breakpoint.setValue(value);
		String grade = random();
		breakpoint.setGrade(grade);
		Integer bestToLowest = Integer.valueOf(3);
		breakpoint.setBestToLowest(bestToLowest);
		breakpoint = sut.save(breakpoint);
		dbInstance.commitAndCloseSession();
		
		breakpoint = sut.load(gradeScale).get(0);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(breakpoint.getValue()).isEqualByComparingTo(value);
		softly.assertThat(breakpoint.getGrade()).isEqualTo(grade);
		softly.assertThat(breakpoint.getBestToLowest()).isEqualTo(bestToLowest);
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadByGradeScale() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		GradeScale gradeScale1 = gradeScaleDAO.create(repositoryEntry, random());
		Breakpoint breakpoint11 = sut.create(gradeScale1);
		Breakpoint breakpoint12 = sut.create(gradeScale1);
		GradeScale gradeScale2 = gradeScaleDAO.create(repositoryEntry, random());
		Breakpoint breakpoint21 = sut.create(gradeScale2);
		dbInstance.commitAndCloseSession();
		
		List<Breakpoint> breakpointes = sut.load(gradeScale1);
		
		assertThat(breakpointes)
				.containsExactlyInAnyOrder(breakpoint11, breakpoint12)
				.doesNotContain(breakpoint21);
	}

	@Test
	public void shouldDeleteByKey() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		GradeScale gradeScale1 = gradeScaleDAO.create(repositoryEntry, random());
		Breakpoint breakpoint11 = sut.create(gradeScale1);
		Breakpoint breakpoint12 = sut.create(gradeScale1);
		GradeScale gradeScale2 = gradeScaleDAO.create(repositoryEntry, random());
		Breakpoint breakpoint21 = sut.create(gradeScale2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(Collections.singletonList(breakpoint11.getKey()));
		dbInstance.commitAndCloseSession();
		
		List<Breakpoint> breakpointes = sut.load(gradeScale1);
		
		assertThat(breakpointes)
				.containsExactlyInAnyOrder(breakpoint12)
				.doesNotContain(breakpoint11, breakpoint21);
	}

	@Test
	public void shouldDeleteByGradScale() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		GradeScale gradeScale1 = gradeScaleDAO.create(repositoryEntry, random());
		sut.create(gradeScale1);
		sut.create(gradeScale1);
		GradeScale gradeScale2 = gradeScaleDAO.create(repositoryEntry, random());
		sut.create(gradeScale2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(gradeScale1);
		dbInstance.commitAndCloseSession();
		
		List<Breakpoint> breakpointes = sut.load(gradeScale1);
		assertThat(breakpointes).isEmpty();
		breakpointes = sut.load(gradeScale2);
		assertThat(breakpointes).hasSize(1);
	}

	@Test
	public void shouldDeleteRepositoryEntry() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry repositoryEntry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		GradeScale gradeScale1 = gradeScaleDAO.create(repositoryEntry, subIdent);
		sut.create(gradeScale1);
		GradeScale gradeScale2 = gradeScaleDAO.create(repositoryEntry, random());
		Breakpoint breakpoint2 = sut.create(gradeScale2);
		GradeScale gradeScale3 = gradeScaleDAO.create(repositoryEntry2, subIdent);
		Breakpoint breakpoint3 = sut.create(gradeScale3);
		dbInstance.commitAndCloseSession();
		
		sut.delete(repositoryEntry, subIdent);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.load(gradeScale1)).isEmpty();
		assertThat(sut.load(gradeScale2)).containsExactlyInAnyOrder(breakpoint2);
		assertThat(sut.load(gradeScale3)).containsExactlyInAnyOrder(breakpoint3);
	}
}
