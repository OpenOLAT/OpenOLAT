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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.olat.test.JunitTestHelper.random;

import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.course.assessment.MappedScoreAccounting;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.RootPassedEvaluator.GradePassed;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.model.GradeScoreRangeImpl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 30 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STRootGradeEvaluatorTest {
	
	private RepositoryEntry dummyEntry = new RepositoryEntry();
	
	@Test
	public void shouldReturnGradeAndPassed() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(3f, random(), random(), random(), null);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		String grade = random();
		String gradeSystemIdent = random();
		String performanceClassIdent = random();
		Boolean passed = Boolean.TRUE;
		GradeScoreRange gradeScoreRange = new GradeScoreRangeImpl(0, grade, gradeSystemIdent, performanceClassIdent, null, null, false, null, false, passed);
		STRootGradeEvaluator sut = createSut(Boolean.TRUE, gradeScoreRange);
		GradePassed gradePassed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry, null);
		
		assertThat(gradePassed.getGrade()).isEqualTo(grade);
		assertThat(gradePassed.getGradeSystemIdent()).isEqualTo(gradeSystemIdent);
		assertThat(gradePassed.getPerformanceClassIdent()).isEqualTo(performanceClassIdent);
		assertThat(gradePassed.getPassed()).isEqualTo(passed);
	}
	
	@Test
	public void shouldReturnNullIfNoScore() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, random(), random(), random(), null);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		STRootGradeEvaluator sut = createSut(Boolean.TRUE, null);
		GradePassed gradePassed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry, null);
		
		assertThat(gradePassed.getGrade()).isNull();
		assertThat(gradePassed.getGradeSystemIdent()).isNull();
		assertThat(gradePassed.getPerformanceClassIdent()).isNull();
		assertThat(gradePassed.getPassed()).isNull();
	}
	
	@Test
	public void shouldReturnNullIfGradeNotApplied() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(null, random(), random(), random(), null);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		STRootGradeEvaluator sut = createSut(Boolean.TRUE, null);
		GradePassed gradePassed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry, null);
		
		assertThat(gradePassed.getGrade()).isNull();
		assertThat(gradePassed.getGradeSystemIdent()).isNull();
		assertThat(gradePassed.getPerformanceClassIdent()).isNull();
		assertThat(gradePassed.getPassed()).isNull();
	}
	
	@Test
	public void shouldReturnNullIfModuleDisabled() {
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(3f, random(), random(), random(), null);
		CourseNode courseNode = new STCourseNode();
		ScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		STRootGradeEvaluator sut = createSut(Boolean.FALSE, null);
		GradePassed gradePassed = sut.getPassed(currentEvaluation, courseNode, scoreAccounting, dummyEntry, null);
		
		assertThat(gradePassed.getGrade()).isNull();
		assertThat(gradePassed.getGradeSystemIdent()).isNull();
		assertThat(gradePassed.getPerformanceClassIdent()).isNull();
		assertThat(gradePassed.getPassed()).isNull();
	}
	
	private STRootGradeEvaluator createSut(Boolean moduleEnabled, GradeScoreRange gradeScoreRange) {
		STRootGradeEvaluator sut = new TestingSTRootGradeEvaluator(gradeScoreRange);
		GradeModule gradeModule = mock(GradeModule.class);
		when(gradeModule.isEnabled()).thenReturn(moduleEnabled);
		sut.setGradeModule(gradeModule);
		return sut;
	}

	private AssessmentEvaluation createAssessmentEvaluation(Float score, String grade, String gradeSystemIdent,
			String performanceClassIdent, Boolean passed) {
		return new AssessmentEvaluation(score, null, grade, gradeSystemIdent, performanceClassIdent, passed,
				Overridable.of(passed), null, null, null, null, null, null, null, null, null, null, null, null, null, 0,
				null, null, null, null, null, null, null, null, null, null);
	}
	
	private static final class TestingSTRootGradeEvaluator extends STRootGradeEvaluator {
		
		private final GradeScoreRange gradeScoreRange;
		
		private TestingSTRootGradeEvaluator(GradeScoreRange gradeScoreRange) {
			this.gradeScoreRange = gradeScoreRange;
		}

		@Override
		GradeScoreRange getGradeScoreRange(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
				RepositoryEntry courseEntry, Identity assessedIdentity) {
			return gradeScoreRange;
		}
		
	}

}
