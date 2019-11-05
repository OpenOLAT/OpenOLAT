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
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 18 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STFullyAssessedEvaluatorTest {

	private STFullyAssessedEvaluator sut = new STFullyAssessedEvaluator();
	
	@Test
	public void shouldReturnTrueIfAllMandatoryChildrenAreFullyAssessed() {
		AssessmentEvaluation childMandatoryAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.mandatory);
		AssessmentEvaluation childOptoinalAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.optional);
		AssessmentEvaluation childOptoinalNotAssessed = createAssessmentEvaluation(Boolean.FALSE, AssessmentObligation.optional);
		AssessmentEvaluation childOptoinalNullAssessed = createAssessmentEvaluation(null, AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(childMandatoryAssessed, childOptoinalAssessed, childOptoinalNotAssessed, childOptoinalNullAssessed);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children);
		
		assertThat(fullyAssessed).isTrue();
	}
	
	@Test
	public void shouldReturnTrueIfItHasNoChildren() {
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, Collections.emptyList());
		
		assertThat(fullyAssessed).isTrue();
	}
	
	@Test
	public void shouldReturnFalseIfAllAtLeastOneMandatoryChildrenIsNotFullyAssessed() {
		AssessmentEvaluation childMandatoryAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.mandatory);
		AssessmentEvaluation childMandatoryNotAssessed = createAssessmentEvaluation(Boolean.FALSE, AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(childMandatoryAssessed, childMandatoryNotAssessed);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children);
		
		assertThat(fullyAssessed).isFalse();
	}
	@Test
	public void shouldReturnFalseIfAllAtLeastOneMandatoryChildrenIsNotAssessed() {
		AssessmentEvaluation childMandatoryAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.mandatory);
		AssessmentEvaluation childMandatoryNotAssessed = createAssessmentEvaluation(null, AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(childMandatoryAssessed, childMandatoryNotAssessed);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children);
		
		assertThat(fullyAssessed).isFalse();
	}
	

	private AssessmentEvaluation createAssessmentEvaluation(Boolean fullyAssessd, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, fullyAssessd, null, null, null, null, null,
				0, null, null, null, null, null, obligation, null);
	}

}
