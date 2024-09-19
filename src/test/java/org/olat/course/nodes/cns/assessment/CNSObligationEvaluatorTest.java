/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.cns.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.evaluation.ConfigObligationEvaluator;
import org.olat.course.learningpath.evaluation.ExceptionalObligationEvaluator;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.cns.assessment.CNSObligationEvaluator.CNSConfigObligationEvaluator;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ObligationEvaluator;
import org.olat.course.run.scoring.ObligationEvaluator.DefaultConfigObligationEvaluator;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.model.ObligationOverridableImpl;

/**
 * 
 * Initial date: 18 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSObligationEvaluatorTest {

	@Test
	public void shouldExcludedChildren() {
		LearningPathService learningPathService = mock(LearningPathService.class);
		DefaultConfigObligationEvaluator obligationResolver = new ObligationEvaluator.DefaultConfigObligationEvaluator();
		obligationResolver.setLearningPathService(learningPathService);
		
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(ofConfig(AssessmentObligation.mandatory));
		CourseNode courseNode = new COCourseNode();
		LearningPathConfigs configs = lpConfigs(AssessmentObligation.mandatory);
		when(learningPathService.getConfigs(courseNode)).thenReturn(configs);
		
		ConfigObligationEvaluator sut = new ConfigObligationEvaluator();
		ObligationOverridable obligation = sut.getObligation(currentEvaluation, courseNode,
				AssessmentObligation.mandatory, obligationResolver, noExceptionalObligations())
				.getObligationOverridable();
		assertThat(obligation.getConfigCurrent()).isEqualTo(AssessmentObligation.mandatory);
		
		CNSConfigObligationEvaluator cnsObligationResolver = new CNSObligationEvaluator.CNSConfigObligationEvaluator();
		obligation = sut.getObligation(currentEvaluation, courseNode,
				AssessmentObligation.mandatory, cnsObligationResolver, noExceptionalObligations())
				.getObligationOverridable();
		assertThat(obligation.getConfigCurrent()).isEqualTo(AssessmentObligation.excluded);
	}
	
	private ExceptionalObligationEvaluator noExceptionalObligations() {
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = mock(ExceptionalObligationEvaluator.class);
		when(exceptionalObligationEvaluator.filterAssessmentObligation(any(), any())).thenReturn(Collections.emptySet());
		return exceptionalObligationEvaluator;
	}
	
	private LearningPathConfigs lpConfigs(AssessmentObligation obligation) {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.getObligation()).thenReturn(obligation);
		return configs;
	}
	
	private ObligationOverridable ofConfig(AssessmentObligation obligation) {
		return new ObligationOverridableImpl(null, null, null, obligation, null, null, null, null);
	}
	
	private AssessmentEvaluation getAssessmentEvaluation(ObligationOverridable obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, 0, null, null, null, null, null, null, obligation, null,
				null, null);
	}

}
