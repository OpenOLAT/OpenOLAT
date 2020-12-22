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
import org.olat.course.run.scoring.Blocker;
import org.olat.modules.assessment.Overridable;
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
		Blocker blocker = new SequentialBlocker();
		AssessmentEvaluation childMandatoryAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.mandatory);
		AssessmentEvaluation childOptoinalAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.optional);
		AssessmentEvaluation childOptoinalNotAssessed = createAssessmentEvaluation(Boolean.FALSE, AssessmentObligation.optional);
		AssessmentEvaluation childOptoinalNullAssessed = createAssessmentEvaluation(null, AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(childMandatoryAssessed, childOptoinalAssessed, childOptoinalNotAssessed, childOptoinalNullAssessed);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children, blocker);
		
		assertThat(fullyAssessed).isTrue();
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldReturnTrueIfAllMandatoryChildrenAreFullyAssessedEvenIfItIsBlocked() {
		Blocker blocker = new SequentialBlocker();
		blocker.block();
		AssessmentEvaluation childMandatoryAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(childMandatoryAssessed);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children, blocker);
		
		assertThat(fullyAssessed).isTrue();
	}
	
	@Test
	public void shouldReturnTrueIfItHasNoChildren() {
		Blocker blocker = new SequentialBlocker();
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, Collections.emptyList(), blocker);
		
		assertThat(fullyAssessed).isTrue();
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldReturnFalseIfAllAtLeastOneMandatoryChildrenIsNotFullyAssessed() {
		Blocker blocker = new SequentialBlocker();
		AssessmentEvaluation childMandatoryAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.mandatory);
		AssessmentEvaluation childMandatoryNotAssessed = createAssessmentEvaluation(Boolean.FALSE, AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(childMandatoryAssessed, childMandatoryNotAssessed);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children, blocker);
		
		assertThat(fullyAssessed).isFalse();
		assertThat(blocker.isBlocked()).isTrue();
	}
	@Test
	public void shouldReturnFalseIfAllAtLeastOneMandatoryChildrenIsNotAssessed() {
		Blocker blocker = new SequentialBlocker();
		AssessmentEvaluation childMandatoryAssessed = createAssessmentEvaluation(Boolean.TRUE, AssessmentObligation.mandatory);
		AssessmentEvaluation childMandatoryNotAssessed = createAssessmentEvaluation(null, AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(childMandatoryAssessed, childMandatoryNotAssessed);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children, blocker);
		
		assertThat(fullyAssessed).isFalse();
		assertThat(blocker.isBlocked()).isTrue();
	}
	
	@Test
	public void shouldReturnTrueIfItHasOnlyOptionalChildren() {
		Blocker blocker = new SequentialBlocker();
		AssessmentEvaluation childOptoinalNotAssessed = createAssessmentEvaluation(Boolean.FALSE, AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(childOptoinalNotAssessed);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children, blocker);
		
		assertThat(fullyAssessed).isTrue();
	}
	
	@Test
	public void shouldReturnFalseIfItHasOnlyOptionalChildrenButItIsBlocked() {
		Blocker blocker = new SequentialBlocker();
		blocker.block();
		AssessmentEvaluation childOptoinalNotAssessed1 = createAssessmentEvaluation(Boolean.FALSE, AssessmentObligation.optional);
		AssessmentEvaluation childOptoinalNotAssessed2 = createAssessmentEvaluation(null, AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(childOptoinalNotAssessed1, childOptoinalNotAssessed2);
		
		Boolean fullyAssessed = sut.getFullyAssessed(null, children, blocker);
		
		assertThat(fullyAssessed).isFalse();
	}
	
	private AssessmentEvaluation createAssessmentEvaluation(Boolean fullyAssessd, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, fullyAssessd, null, null, null,
				null, null, null, null, 0, null, null, null, null, null, null, Overridable.of(obligation), null, null, null);
	}

}
