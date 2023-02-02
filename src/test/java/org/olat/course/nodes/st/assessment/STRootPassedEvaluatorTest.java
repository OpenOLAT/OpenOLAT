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
package org.olat.course.nodes.st.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.assertj.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.olat.course.assessment.MappedScoreAccounting;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.assessment.PassCounter.Counts;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.Overridable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 13 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STRootPassedEvaluatorTest {
	
	private RepositoryEntry dummyEntry = new RepositoryEntry();
	private PassCounter oneHalfPassed;
	
	@Before
	public void setUp() {
		Counts counts = new CountsImpl(3, 2, 1);
		oneHalfPassed = mock(PassCounter.class);
		when(oneHalfPassed.getCounts(any(), any(), any(), any())).thenReturn(counts);
	}
	
	@Test
	public void shouldReturnNullPerDefault() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, null);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}

	@Test
	public void shouldReturnCurrentTrueIfNoConfiguration() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, Boolean.TRUE, null);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isTrue();
	}

	@Test
	public void shouldReturnCurrentFalseIfNoConfiguration() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, Boolean.FALSE, null);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();

		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isFalse();
	}
	
	@Test
	public void shouldReturnTrueIfFullyAssessed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, Boolean.FALSE, Boolean.TRUE);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();

		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isTrue();
	}
	
	@Test
	public void shouldReturnCurrentIfNotFullyAssessed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, Boolean.FALSE);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();

		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnNotResetIfNotFullyAssessed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, Boolean.TRUE, Boolean.FALSE);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();

		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isTrue();
	}
	
	@Test
	public void shouldReturnTrueIfPointCutReached() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_POINTS, true);
		courseNode.getModuleConfiguration().setIntValue(STCourseNode.CONFIG_PASSED_POINTS_CUT, 10);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();

		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isTrue();
	}
	
	@Test
	public void shouldReturnTrueIfPointCutReachedExactly() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(10), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_POINTS, true);
		courseNode.getModuleConfiguration().setIntValue(STCourseNode.CONFIG_PASSED_POINTS_CUT, 10);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();

		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isTrue();
	}
	
	@Test
	public void shouldReturnCurrentIfPointCutNotReached() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(2), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_POINTS, true);
		courseNode.getModuleConfiguration().setIntValue(STCourseNode.CONFIG_PASSED_POINTS_CUT, 10);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();

		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldNotResetIfPointCutNotReached() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(2), Boolean.TRUE, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_POINTS, true);
		courseNode.getModuleConfiguration().setIntValue(STCourseNode.CONFIG_PASSED_POINTS_CUT, 10);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();

		STRootPassedEvaluator sut = new STRootPassedEvaluator(oneHalfPassed);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isTrue();
	}
	
	@Test
	public void shouldReturnTrueIfAllPassed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(3, 3, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isTrue();
	}
	
	@Test
	public void shouldReturnFailedIfAllNotPassedAndOnlyCriterion() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(3, 1, 1);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isFalse();
	}
	
	@Test
	public void shouldReturnNullIfAllWithoutDecisionAndOnlyCriterion() {
		// To reset former failed e.g. if a test is made a second time.
		
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), Boolean.FALSE, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(3, 1, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldNotReturnNullIfAllNotPassedAndMultipleCriterion() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(3, 2, 1);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnTrueIfNumberPassed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_NUMBER, true);
		courseNode.getModuleConfiguration().setIntValue(STCourseNode.CONFIG_PASSED_NUMBER_CUT, 2);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(4, 2, 1);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isTrue();
	}
	
	@Test
	public void shouldReturnNullIfNumberNotPassedAndOnlyCriterion() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_NUMBER, true);
		courseNode.getModuleConfiguration().setIntValue(STCourseNode.CONFIG_PASSED_NUMBER_CUT, 2);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(4, 1, 1);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldNotReturnNullIfNumberNotPassedAndMultipleCriterion() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_NUMBER, true);
		courseNode.getModuleConfiguration().setIntValue(STCourseNode.CONFIG_PASSED_NUMBER_CUT, 2);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(4, 1, 1);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnNullIfNotAllAssessed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(Float.valueOf(20), null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(3, 2, 1);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnNullIfCourseHasEndedAndItIsNotPassedButItsToday() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		RepositoryEntryLifecycle lifecycle = new RepositoryEntryLifecycle();
		lifecycle.setValidTo(new Date());
		RepositoryEntry endedEntry = new RepositoryEntry();
		endedEntry.setLifecycle(lifecycle);
		
		Counts counts = new CountsImpl(3, 2, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, endedEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnFailedIfCourseHasEndedAndItIsNotPassed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		RepositoryEntryLifecycle lifecycle = new RepositoryEntryLifecycle();
		lifecycle.setValidTo(DateUtil.yesterday());
		RepositoryEntry endedEntry = new RepositoryEntry();
		endedEntry.setLifecycle(lifecycle);
		
		Counts counts = new CountsImpl(3, 2, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, endedEntry);
		
		assertThat(passed).isFalse();
	}
	
	@Test
	public void shouldReturnNullIfCourseHasNotEndedAndItIsNotPassed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		RepositoryEntryLifecycle lifecycle = new RepositoryEntryLifecycle();
		lifecycle.setValidTo(DateUtil.tomorrow());
		RepositoryEntry runningEntry = new RepositoryEntry();
		runningEntry.setLifecycle(lifecycle);
		
		Counts counts = new CountsImpl(3, 2, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, runningEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnNullIfCourseHasEndedAndItIsNotPassedAndHasNoPassableNodes() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		RepositoryEntryLifecycle lifecycle = new RepositoryEntryLifecycle();
		lifecycle.setValidTo(DateUtil.yesterday());
		RepositoryEntry endedEntry = new RepositoryEntry();
		endedEntry.setLifecycle(lifecycle);
		
		Counts counts = new CountsImpl(0, 0, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, endedEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnNullIfCourseHasEndedAndItIsNotPassedAndHAsNoPassConfigs() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, null);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		RepositoryEntryLifecycle lifecycle = new RepositoryEntryLifecycle();
		lifecycle.setValidTo(DateUtil.yesterday());
		RepositoryEntry endedEntry = new RepositoryEntry();
		endedEntry.setLifecycle(lifecycle);
		
		Counts counts = new CountsImpl(3, 2, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, endedEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnFailedIfCourseIsFullyAssessedAndItIsNotPassed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, Boolean.TRUE);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(3, 2, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isFalse();
	}
	
	@Test
	public void shouldReturnNullIfCourseIsNotFullyAssessedAndItIsNotPassed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, null);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(3, 2, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnNullIfCourseIsFullyAssessedButHasNoPassableNodes() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, Boolean.TRUE);
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(0, 0, 0);
		PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
	}
	
	@Test
	public void shouldReturnNullIfCourseIsFullyAssessedButHassNoPassConfigs() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, null, Boolean.TRUE);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		Counts counts = new CountsImpl(3, 2, 0);
			PassCounter passCounter = mock(PassCounter.class);
		when(passCounter.getCounts(any(), any(), any(), any())).thenReturn(counts);
		STRootPassedEvaluator sut = new STRootPassedEvaluator(passCounter);
		
		Boolean passed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry);
		
		assertThat(passed).isNull();
		}

	private AssessmentEvaluation createAssessmentEvaluation(Float score, final Boolean passed, Boolean fullyAssessed) {
		return new AssessmentEvaluation(score, null, null, null, null, passed, Overridable.of(passed), null, null, null,
				null, null, fullyAssessed, null, null, null, null, null, null, null, 0, null, null, null, null, null,
				null, null, null, null, null);
	}
	
	private final static class CountsImpl implements Counts {

		private final int passable;
		private final int passed;
		private final int failed;
		
		public CountsImpl(int passable, int passed, int failed) {
			super();
			this.passable = passable;
			this.passed = passed;
			this.failed = failed;
		}

		@Override
		public int getPassable() {
			return passable;
		}

		@Override
		public int getPassed() {
			return passed;
		}

		@Override
		public int getFailed() {
			return failed;
		}
		
		@Override
		public boolean isAllAssessed() {
			return passable == passed + failed;
		}
		
	}

}
