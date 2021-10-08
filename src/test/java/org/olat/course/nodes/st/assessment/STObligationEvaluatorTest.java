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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.evaluation.ExceptionalObligationEvaluator;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 23 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STObligationEvaluatorTest {
	
	@Test
	public void shouldReturnExtendedIfExceptionalObligationExtended() {
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		Set<AssessmentObligation> exceptionalObligations = Collections.singleton(AssessmentObligation.excluded);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(exceptionalObligations);
		
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.getObligation()).thenReturn(AssessmentObligation.evaluated);
		ExceptionalObligation exceptionalObligation = mock(ExceptionalObligation.class);
		when(exceptionalObligation.getObligation()).thenReturn(AssessmentObligation.excluded);
		when(configs.getExceptionalObligations()).thenReturn(Collections.singletonList(exceptionalObligation));
		LearningPathService learningPathService = mock(LearningPathService.class);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		
		STObligationEvaluator sut = new STObligationEvaluator();
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(child1);
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, courseNode, exceptionalObligationEvaluator, children);
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.excluded);
	}
	
	@Test
	public void shouldReturnEvaluatedIfExceptionalObligationEvaluated() {
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		Set<AssessmentObligation> exceptionalObligations = Collections.singleton(AssessmentObligation.evaluated);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(exceptionalObligations);
		
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.getObligation()).thenReturn(AssessmentObligation.excluded);
		ExceptionalObligation exceptionalObligation = mock(ExceptionalObligation.class);
		when(exceptionalObligation.getObligation()).thenReturn(AssessmentObligation.evaluated);
		when(configs.getExceptionalObligations()).thenReturn(Collections.singletonList(exceptionalObligation));
		LearningPathService learningPathService = mock(LearningPathService.class);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		
		STObligationEvaluator sut = new STObligationEvaluator();
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(child1);
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, courseNode, exceptionalObligationEvaluator, children);
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.mandatory);
	}
	
	@Test
	public void shouldReturnMandatoryIfAtLeastOneChildIsMandatory() {
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(Collections.emptySet());
		
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.getObligation()).thenReturn(AssessmentObligation.evaluated);
		LearningPathService learningPathService = mock(LearningPathService.class);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		
		STObligationEvaluator sut = new STObligationEvaluator();
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(AssessmentObligation.mandatory);
		AssessmentEvaluation child2 = getAssessmentEvaluation(AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, courseNode, exceptionalObligationEvaluator, children);
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.mandatory);
	}

	@Test
	public void shouldReturnOptionalIfAllChildrenAreOptional() {
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(Collections.emptySet());
		
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.getObligation()).thenReturn(AssessmentObligation.evaluated);
		LearningPathService learningPathService = mock(LearningPathService.class);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		
		STObligationEvaluator sut = new STObligationEvaluator();
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(null);
		AssessmentEvaluation child2 = getAssessmentEvaluation(AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, courseNode, exceptionalObligationEvaluator, children);
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.optional);
	}
	
	@Test
	public void shouldReturnChildObligationlIfExcludedIsOverriden() {
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(Collections.emptySet());
		
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.getObligation()).thenReturn(AssessmentObligation.excluded);
		LearningPathService learningPathService = mock(LearningPathService.class);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		
		STObligationEvaluator sut = new STObligationEvaluator();
		sut.setLearningPathService(learningPathService);

		ObligationOverridable currentObligation = ObligationOverridable.of(AssessmentObligation.excluded);
		currentObligation.override(AssessmentObligation.evaluated, null, new Date());
		AssessmentEvaluation currentEvaluation = new AssessmentEvaluation(null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null, 0, null, null, null, null, null, null,
				currentObligation, null, null, null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(null);
		AssessmentEvaluation child2 = getAssessmentEvaluation(AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, courseNode, exceptionalObligationEvaluator, children);
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.optional);
	}
	
	@Test
	public void shouldNotDependOnCourseNode() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(Collections.emptySet());
		CourseNode courseNode = new STCourseNode();
		courseNode.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_KEY_OBLIGATION, AssessmentObligation.evaluated.name());
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(AssessmentObligation.optional);
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, courseNode, exceptionalObligationEvaluator);
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.optional);
	}

	private AssessmentEvaluation getAssessmentEvaluation(AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, 0, null, null, null, null, null, null, ObligationOverridable.of(obligation), null, null, null);
	}
	
}
