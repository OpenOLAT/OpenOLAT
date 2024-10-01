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

import java.util.List;

import org.junit.Test;
import org.olat.course.nodes.st.assessment.SequentialBlocker;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.model.ObligationOverridableImpl;

/**
 * 
 * Initial date: 18 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSFullyAssesssedEvaluatorTest {

	@Test
	public void shouldReturnFalseIfConfigNotValid() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		CNSFullyAssesssedEvaluator sut = new CNSFullyAssesssedEvaluator();
		assertThat(sut.getFullyAssessed("abc", blocker, List.of(getAssessmentEvaluation(null, null)))).isEqualTo(Boolean.FALSE);
		assertThat(blocker.isBlocked()).isTrue();
	}
	
	@Test
	public void shouldReturnFalseIfConfigNoChildren() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		CNSFullyAssesssedEvaluator sut = new CNSFullyAssesssedEvaluator();
		assertThat(sut.getFullyAssessed("1", blocker, List.of())).isEqualTo(Boolean.FALSE);
		assertThat(blocker.isBlocked()).isTrue();
	}
	
	@Test
	public void shouldTrueIfRequiredSelectionsDone() {
		AssessmentEvaluation child1 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.excluded), Boolean.FALSE);
		AssessmentEvaluation child2 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.excluded), Boolean.FALSE);
		AssessmentEvaluation child3 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.excluded), Boolean.FALSE);
		
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		CNSFullyAssesssedEvaluator sut = new CNSFullyAssesssedEvaluator();
		assertThat(sut.getFullyAssessed("2", blocker, List.of(child1, child2, child3))).isEqualTo(Boolean.FALSE);
		assertThat(blocker.isBlocked()).isTrue();
		
		blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		child1 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.mandatory), Boolean.TRUE);
		assertThat(sut.getFullyAssessed("2", blocker, List.of(child1, child2, child3))).isEqualTo(Boolean.FALSE);
		assertThat(blocker.isBlocked()).isTrue();
		
		blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		child2 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.mandatory), Boolean.TRUE);
		assertThat(sut.getFullyAssessed("2", blocker, List.of(child1, child2, child3))).isEqualTo(Boolean.TRUE);
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldReturnFalseIfNotAllMandatoryDone() {
		AssessmentEvaluation child1 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.optional), Boolean.TRUE);
		AssessmentEvaluation child2 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.mandatory), Boolean.FALSE);
		AssessmentEvaluation child3 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.optional), Boolean.TRUE);
		
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		CNSFullyAssesssedEvaluator sut = new CNSFullyAssesssedEvaluator();
		assertThat(sut.getFullyAssessed("2", blocker, List.of(child1, child2, child3))).isEqualTo(Boolean.FALSE);
		assertThat(blocker.isBlocked()).isTrue();
		
		blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		child1 = getAssessmentEvaluation(ofCurrent(AssessmentObligation.mandatory), Boolean.TRUE);
		assertThat(sut.getFullyAssessed("2", blocker, List.of(child1, child2, child3))).isEqualTo(Boolean.FALSE);
		assertThat(blocker.isBlocked()).isTrue();
	}
	
	private ObligationOverridable ofCurrent(AssessmentObligation obligation) {
		return new ObligationOverridableImpl(obligation, null, null, null, null, null, null, null);
	}
	
	private AssessmentEvaluation getAssessmentEvaluation(ObligationOverridable obligation, Boolean fullyAssessed) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, fullyAssessed, null, null, null, null, null, null, null, 0, null, null, null, null, null,
				null, obligation, null, null, null);
	}

}
