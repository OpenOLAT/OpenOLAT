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

import java.util.Collections;
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
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.model.ObligationOverridableImpl;

/**
 * 
 * Initial date: 23 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STObligationEvaluatorTest {
	
	@Test
	public void shouldInheritIfParentExcluded() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.mandatory));
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = lpConfigs(AssessmentObligation.mandatory);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, AssessmentObligation.excluded, noExceptionalObligations());
		
		assertThat(obligation.getInherited()).isEqualTo(AssessmentObligation.excluded);
	}
	
	@Test
	public void shouldNotInheritIfParentNotExcluded() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.mandatory));
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = lpConfigs(AssessmentObligation.mandatory);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, AssessmentObligation.optional, noExceptionalObligations());
		
		assertThat(obligation.getInherited()).isNull();
	}

	@Test
	public void shouldSetConfig() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.mandatory));
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = lpConfigs(AssessmentObligation.optional);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, AssessmentObligation.evaluated, noExceptionalObligations());
		
		assertThat(obligation.getConfigCurrent()).isEqualTo(AssessmentObligation.optional);
	}

	@Test
	public void shouldSetConfigToExceptionalObligation() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.excluded));
		CourseNode courseNode = new STCourseNode();
		LearningPathConfigs configs = lpConfigs(AssessmentObligation.optional, AssessmentObligation.mandatory);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, AssessmentObligation.evaluated, exceptionalObligations(AssessmentObligation.mandatory));
		
		assertThat(obligation.getConfigCurrent()).isEqualTo(AssessmentObligation.mandatory);
	}
	
	@Test
	public void shouldGetCurrent() {
		STObligationEvaluator sut = new STObligationEvaluator();
		
		assertThat(sut.getCurrentObligation(new ObligationOverridableImpl(null, null, null, AssessmentObligation.optional, null, null, null))).isEqualTo(AssessmentObligation.optional);
		assertThat(sut.getCurrentObligation(new ObligationOverridableImpl(null, null, null, AssessmentObligation.mandatory, null, null, null))).isEqualTo(AssessmentObligation.mandatory);
		assertThat(sut.getCurrentObligation(new ObligationOverridableImpl(null, null, AssessmentObligation.mandatory, AssessmentObligation.evaluated, null, null, null))).isEqualTo(AssessmentObligation.mandatory);
		assertThat(sut.getCurrentObligation(new ObligationOverridableImpl(null, AssessmentObligation.excluded, null, AssessmentObligation.optional, null, null, null))).isEqualTo(AssessmentObligation.excluded);
	}

	@Test
	public void evaluateOptionalIfNoChildren() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.evaluated));
		CourseNode courseNode = new STCourseNode();
		List<AssessmentEvaluation> children = Collections.emptyList();
		
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, noExceptionalObligations(), children);
		
		assertThat(obligation.getEvaluated()).isEqualTo(AssessmentObligation.optional);
	}
	
	@Test
	public void evaluateOptionalIfOnlyOptionaldExcludedAnChildren() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.evaluated));
		CourseNode courseNode = new STCourseNode();
		List<AssessmentEvaluation> children = List.of(
				getAssessmentEvaluation(ofConfig(AssessmentObligation.optional)),
				getAssessmentEvaluation(new ObligationOverridableImpl(AssessmentObligation.excluded, null, null, AssessmentObligation.excluded, null, null, null)),
				getAssessmentEvaluation(ofConfig(AssessmentObligation.optional)));
		
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, noExceptionalObligations(), children);
		
		assertThat(obligation.getEvaluated()).isEqualTo(AssessmentObligation.optional);
	}
	
	@Test
	public void evaluateMandatoryIfAtLeastOneMandatory() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.evaluated));
		CourseNode courseNode = new STCourseNode();
		List<AssessmentEvaluation> children = List.of(
				getAssessmentEvaluation(ofConfig(AssessmentObligation.optional)),
				getAssessmentEvaluation(ofConfig(AssessmentObligation.mandatory)),
				getAssessmentEvaluation(ofConfig(AssessmentObligation.optional)));
		
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, noExceptionalObligations(), children);
		
		assertThat(obligation.getEvaluated()).isEqualTo(AssessmentObligation.mandatory);
	}
	
	@Test
	public void evaluateShouldUseEvaluatedObligation() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.evaluated));
		CourseNode courseNode = new STCourseNode();
		List<AssessmentEvaluation> children = List.of(
				getAssessmentEvaluation(ofConfig(AssessmentObligation.optional)),
				getAssessmentEvaluation(new ObligationOverridableImpl(null, null, AssessmentObligation.mandatory, AssessmentObligation.evaluated, null, null, null)),
				getAssessmentEvaluation(ofConfig(AssessmentObligation.optional)));
		
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, noExceptionalObligations(), children);
		
		assertThat(obligation.getEvaluated()).isEqualTo(AssessmentObligation.mandatory);
	}
	
	@Test
	public void evaluateShouldUseConfigObligationEvenIfInheritedIsExcluded() {
		STObligationEvaluator sut = new STObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		sut.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.evaluated));
		CourseNode courseNode = new STCourseNode();
		List<AssessmentEvaluation> children = List.of(
				getAssessmentEvaluation(ofConfig(AssessmentObligation.optional)),
				getAssessmentEvaluation(new ObligationOverridableImpl(AssessmentObligation.excluded, AssessmentObligation.excluded, null, AssessmentObligation.mandatory, null, null, null)),
				getAssessmentEvaluation(ofConfig(AssessmentObligation.optional)));
		
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode, noExceptionalObligations(), children);
		
		assertThat(obligation.getEvaluated()).isEqualTo(AssessmentObligation.mandatory);
	}

	private ExceptionalObligationEvaluator noExceptionalObligations() {
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(Collections.emptySet());
		return exceptionalObligationEvaluator;
	}
	
	private ExceptionalObligationEvaluator exceptionalObligations(AssessmentObligation obligation) {
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		Set<AssessmentObligation> exceptionalObligations = Collections.singleton(obligation);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(exceptionalObligations);
		return exceptionalObligationEvaluator;
	}
	
	private LearningPathConfigs lpConfigs(AssessmentObligation obligation) {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.getObligation()).thenReturn(obligation);
		return configs;
	}
	
	private LearningPathConfigs lpConfigs(AssessmentObligation obligation, AssessmentObligation exceptionalObligation) {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.getObligation()).thenReturn(obligation);
		ExceptionalObligation exObligation = mock(ExceptionalObligation.class);
		when(exObligation.getObligation()).thenReturn(exceptionalObligation);
		when(configs.getExceptionalObligations()).thenReturn(Collections.singletonList(exObligation));
		return configs;
	}
	
	private ObligationOverridable ofConfig(AssessmentObligation obligation) {
		return new ObligationOverridableImpl(null, null, null, obligation, null, null, null);
	}

	private AssessmentEvaluation getAssessmentEvaluation(ObligationOverridable obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, 0, null, null, null, null, null, null, obligation, null, null, null);
	}
	
}
