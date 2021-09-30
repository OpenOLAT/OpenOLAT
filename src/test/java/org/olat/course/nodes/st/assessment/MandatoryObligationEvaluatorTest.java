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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.NullObligationContext;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 23 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MandatoryObligationEvaluatorTest {
	
	private MandatoryObligationEvaluator sut = new MandatoryObligationEvaluator();
	
	@Test
	public void shouldReturnMandatoryIfAtLeastOneChildIsMandatory() {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(AssessmentObligation.mandatory);
		AssessmentEvaluation child2 = getAssessmentEvaluation(AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, children);
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.mandatory);
	}

	@Test
	public void shouldReturnOptionalIfAllChildrenAreOptional() {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(null);
		AssessmentEvaluation child2 = getAssessmentEvaluation(AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, children);
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.optional);

	}
	
	@Test
	public void shouldNotDependOnCourseNode() {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(AssessmentObligation.optional);
		CourseNode courseNode = new STCourseNode();
		
		Overridable<AssessmentObligation> obligation = sut.getObligation(currentEvaluation, courseNode, null, null, null, NullObligationContext.create());
		
		assertThat(obligation.getCurrent()).isEqualTo(AssessmentObligation.optional);
	}

	private AssessmentEvaluation getAssessmentEvaluation(AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, 0, null, null, null, null, null, null, Overridable.of(obligation), null, null, null);
	}
	
}
