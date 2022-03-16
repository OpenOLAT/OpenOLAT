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
package org.olat.modules.grade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.grade.model.BreakpointWrapper;
import org.olat.modules.grade.model.GradeScaleImpl;
import org.olat.modules.grade.model.GradeScaleWrapper;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
	private GradeService sut;
	
	@Test
	public void shouldUpdateOrCreateGradeScale() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		GradeSystem gradeSystem = sut.createGradeSystem(random(), GradeSystemType.text);
		GradeScaleWrapper gradeScale = new GradeScaleWrapper();
		gradeScale.setGradeSystem(gradeSystem);
		BigDecimal minScore = BigDecimal.valueOf(1.1);
		gradeScale.setMinScore(minScore);
		BigDecimal maxScore = BigDecimal.valueOf(9.9);
		gradeScale.setMaxScore(maxScore);
		dbInstance.commitAndCloseSession();
		
		sut.updateOrCreateGradeScale(repositoryEntry, subIdent, gradeScale);
		dbInstance.commitAndCloseSession();
		
		GradeScale reloaded = sut.getGradeScale(repositoryEntry, subIdent);
		assertThat(reloaded).isInstanceOf(GradeScaleImpl.class);
		assertThat(((GradeScaleImpl)reloaded).getKey()).isNotNull();
		assertThat(((GradeScaleImpl)reloaded).getCreationDate()).isNotNull();
		assertThat(reloaded.getGradeSystem()).isEqualTo(gradeSystem);
		assertThat(reloaded.getRepositoryEntry()).isEqualTo(repositoryEntry);
		assertThat(reloaded.getSubIdent()).isEqualTo(subIdent);
		assertThat(reloaded.getMinScore()).isEqualByComparingTo(minScore);
		assertThat(reloaded.getMaxScore()).isEqualByComparingTo(maxScore);
	}

	@Test
	public void shouldUpdateOrCreateBreakpoints_numeric() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		GradeSystem gradeSystem = sut.createGradeSystem(random(), GradeSystemType.numeric);
		GradeScaleWrapper gradeScaleWrapper = new GradeScaleWrapper();
		gradeScaleWrapper.setGradeSystem(gradeSystem);
		GradeScale gradeScale = sut.updateOrCreateGradeScale(repositoryEntry, subIdent, gradeScaleWrapper);
		
		// Create 2 breakpoints
		BreakpointWrapper breakpoint11 = new BreakpointWrapper();
		breakpoint11.setValue(new BigDecimal(11));
		breakpoint11.setGrade("g11");
		BreakpointWrapper breakpoint12 = new BreakpointWrapper();
		breakpoint12.setValue(new BigDecimal(12));
		breakpoint12.setGrade("g12");
		dbInstance.commitAndCloseSession();
		
		sut.updateOrCreateBreakpoints(gradeScale, List.of(breakpoint11, breakpoint12));
		dbInstance.commitAndCloseSession();
		
		Map<String, Breakpoint> positionToBreakpoint = sut.getBreakpoints(gradeScale).stream()
				.collect(Collectors.toMap(Breakpoint::getGrade, Function.identity()));
		assertThat(positionToBreakpoint).hasSize(2);
		assertThat(positionToBreakpoint.get("g11").getValue()).isEqualByComparingTo(new BigDecimal(11));
		assertThat(positionToBreakpoint.get("g12").getValue()).isEqualByComparingTo(new BigDecimal(12));
		
		
		// Create a new breakpoint, updeate a breakpoint and delete a breakpoint
		BreakpointWrapper breakpoint21 = new BreakpointWrapper();
		breakpoint21.setValue(new BigDecimal(11));
		breakpoint21.setGrade("g21");
		BreakpointWrapper breakpoint23 = new BreakpointWrapper();
		breakpoint23.setValue(new BigDecimal(23));
		breakpoint23.setGrade("g23");
		
		sut.updateOrCreateBreakpoints(gradeScale, List.of(breakpoint21, breakpoint23));
		dbInstance.commitAndCloseSession();
		
		positionToBreakpoint = sut.getBreakpoints(gradeScale).stream()
				.collect(Collectors.toMap(Breakpoint::getGrade, Function.identity()));
		assertThat(positionToBreakpoint).hasSize(2);
		assertThat(positionToBreakpoint.get("g21").getValue()).isEqualByComparingTo(new BigDecimal(11));
		assertThat(positionToBreakpoint.get("g23").getValue()).isEqualByComparingTo(new BigDecimal(23));
	}

	@Test
	public void shouldUpdateOrCreateBreakpoints_text() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		GradeSystem gradeSystem = sut.createGradeSystem(random(), GradeSystemType.text);
		GradeScaleWrapper gradeScaleWrapper = new GradeScaleWrapper();
		gradeScaleWrapper.setGradeSystem(gradeSystem);
		GradeScale gradeScale = sut.updateOrCreateGradeScale(repositoryEntry, subIdent, gradeScaleWrapper);
		
		// Create 2 breakpoints
		BreakpointWrapper breakpoint11 = new BreakpointWrapper();
		breakpoint11.setBestToLowest(Integer.valueOf(1));
		breakpoint11.setValue(new BigDecimal(11));
		BreakpointWrapper breakpoint12 = new BreakpointWrapper();
		breakpoint12.setBestToLowest(Integer.valueOf(2));
		breakpoint12.setValue(new BigDecimal(12));
		dbInstance.commitAndCloseSession();
		
		sut.updateOrCreateBreakpoints(gradeScale, List.of(breakpoint11, breakpoint12));
		dbInstance.commitAndCloseSession();
		
		Map<Integer, Breakpoint> positionToBreakpoint = sut.getBreakpoints(gradeScale).stream()
				.collect(Collectors.toMap(Breakpoint::getBestToLowest, Function.identity()));
		assertThat(positionToBreakpoint).hasSize(2);
		assertThat(positionToBreakpoint.get(Integer.valueOf(1)).getValue()).isEqualByComparingTo(new BigDecimal(11));
		assertThat(positionToBreakpoint.get(Integer.valueOf(2)).getValue()).isEqualByComparingTo(new BigDecimal(12));
		
		
		// Create a new breakpoint, updeate a breakpoint and delete a breakpoint
		BreakpointWrapper breakpoint21 = new BreakpointWrapper();
		breakpoint21.setBestToLowest(Integer.valueOf(1));
		breakpoint21.setValue(new BigDecimal(11));
		BreakpointWrapper breakpoint23 = new BreakpointWrapper();
		breakpoint23.setValue(new BigDecimal(23));
		breakpoint23.setBestToLowest(Integer.valueOf(3));
		
		sut.updateOrCreateBreakpoints(gradeScale, List.of(breakpoint21, breakpoint23));
		dbInstance.commitAndCloseSession();
		
		positionToBreakpoint = sut.getBreakpoints(gradeScale).stream()
				.collect(Collectors.toMap(Breakpoint::getBestToLowest, Function.identity()));
		assertThat(positionToBreakpoint).hasSize(2);
		assertThat(positionToBreakpoint.get(Integer.valueOf(1)).getValue()).isEqualByComparingTo(new BigDecimal(11));
		assertThat(positionToBreakpoint.get(Integer.valueOf(3)).getValue()).isEqualByComparingTo(new BigDecimal(23));
	}

}
