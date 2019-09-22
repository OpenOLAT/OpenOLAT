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
package org.olat.course.learningpath.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.assessment.model.AssessmentEntryStatus.done;
import static org.olat.modules.assessment.model.AssessmentEntryStatus.notReady;
import static org.olat.modules.assessment.model.AssessmentEntryStatus.notStarted;
import static org.olat.modules.assessment.model.AssessmentObligation.mandatory;
import static org.olat.modules.assessment.model.AssessmentObligation.optional;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultLinearStatusEvaluatorTest {
	
	private DefaultLinearStatusEvaluator sut = new DefaultLinearStatusEvaluator();

	@Test
	public void shouldReturnDoneIfAssessmentStatusIsDone() {
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(null, done, null);
		
		AssessmentEntryStatus status = sut.getStatus(null, assessmentEvaluation);
		
		assertThat(status).isEqualTo(done);
	}
	
	@Test
	public void shouldReturnReadyIfIsRootNodeAndNotAlreadyDone() {
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(null, notStarted, null);
		
		AssessmentEntryStatus status = sut.getStatus(null, assessmentEvaluation);
		
		assertThat(status).isEqualTo(notStarted);
	}
	
	@Test
	public void shouldReturnReadyIfIsRootNodeAndIsNotStartedYet() {
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(null, notReady, null);
		
		AssessmentEntryStatus status = sut.getStatus(null, assessmentEvaluation);
		
		assertThat(status).isEqualTo(notStarted);
	}
	
	@Test
	public void shouldReturnReadyIfIsRootNodeAndHasNoStatusYet() {
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(null, null, null);
		
		AssessmentEntryStatus status = sut.getStatus(null, assessmentEvaluation);
		
		assertThat(status).isEqualTo(notStarted);
	}

	@Test
	public void shouldReturnReadyIfPreviousNodeFullyAssessed() {
		assertStatus(Boolean.TRUE, null, null, null, notStarted);
	}

	@Test
	public void shouldReturnReadyIfPreviousNodeFullyAssessedAndIsMandatory() {
		assertStatus(Boolean.TRUE, null, mandatory, null, notStarted);
	}

	@Test
	public void shouldReturnReadyIfPreviousNodeIsOptionalAndIsReady() {
		assertStatus(Boolean.FALSE, notStarted, optional, notReady, notStarted);
	}

	@Test
	public void shouldReturnReadyIfPreviousNodeHasNoObligationAndIsReady() {
		// No obligation means optional. E.g. the STCourseNode has no obligation and is
		// always the first node in a course tree
		assertStatus(Boolean.FALSE, notStarted, null, notReady, notStarted);
	}

	@Test
	public void shouldReturnNotReadyIfPreviousNodeIsOptionalAndIsNotReady() {
		assertStatus(Boolean.FALSE, notReady, optional, notReady, notReady);
	}

	@Test
	public void shouldReturnNotReadyIfPreviousNodeIsMandatoryAndNotFullyAssessedYet() {
		assertStatus(Boolean.FALSE, null, mandatory, null, notReady);
	}

	@Test
	public void shouldReturnNotReadyIfPreviousNodeIsMandatoryAndFullyAssessedIsNull() {
		assertStatus(null, null, mandatory, null, notReady);
	}

	private void assertStatus(Boolean previousFullyAssessd, AssessmentEntryStatus previousStatus,
			AssessmentObligation previousObligation, AssessmentEntryStatus currentStatus,
			AssessmentEntryStatus expected) {
		AssessmentEvaluation previousEvaluation = getAssessmentEvaluation(previousFullyAssessd, previousStatus, previousObligation);
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, currentStatus, null);
		
		AssessmentEntryStatus status = sut.getStatus(previousEvaluation, currentEvaluation);
		
		assertThat(status).isEqualTo(expected);
	}
	
	@Test
	public void shouldNotChangeStatusWhenCheckingAgainstChildren() {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, notStarted, null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(Boolean.TRUE, done, null);
		AssessmentEvaluation child2 = getAssessmentEvaluation(Boolean.TRUE, done, null);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, children);
		
		assertThat(status).isEqualTo(notStarted);
	}


	private AssessmentEvaluation getAssessmentEvaluation(Boolean fullyAssessd, AssessmentEntryStatus assessmentStatus, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, assessmentStatus, null, fullyAssessd, null, null, null, null, null, 0, null, null, null, null, obligation, null);
	}

}
