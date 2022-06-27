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

import org.junit.Test;
import org.olat.course.nodes.st.assessment.SequentialBlocker;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultLearningPathStatusEvaluatorTest {
	
	private DefaultLearningPathStatusEvaluator sut = new DefaultLearningPathStatusEvaluator(AssessmentEntryStatus.inReview);
	
	@Test
	public void shouldBlockIfMandatoryAndNotFullyAssessed() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.FALSE, null, AssessmentObligation.mandatory);

		sut.getStatus(currentEvaluation, blocker);
		
		assertThat(blocker.isBlocked()).isTrue();
	}
	
	@Test
	public void shouldNotBlockIfNotMandatoryAndNotFullyAssessed() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.FALSE, null, AssessmentObligation.optional);

		sut.getStatus(currentEvaluation, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldNotBlockIfMandatoryAndFullyAssessed() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.TRUE, null, AssessmentObligation.mandatory);

		sut.getStatus(currentEvaluation, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldNotChangeStatusIfBlockedButInProgress() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		blocker.block();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, AssessmentEntryStatus.inProgress, null);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, blocker);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.inProgress);
	}
	
	@Test
	public void shouldNotChangeStatusIfBlockedButInReview() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		blocker.block();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, AssessmentEntryStatus.inReview, null);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, blocker);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.inReview);
	}
	
	@Test
	public void shouldNotChangeStatusIfBlockedButDone() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		blocker.block();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, AssessmentEntryStatus.done, null);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, blocker);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.done);
	}
	
	@Test
	public void shouldSetInitialStatusIfNotBlocked() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, null, null);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, blocker);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.inReview);
	}
	
	@Test
	public void shouldSetNotReadyIfBlocked() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		blocker.block();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, null, null);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, blocker);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.notReady);
	}

	private AssessmentEvaluation getAssessmentEvaluation(Boolean fullyAssessd, AssessmentEntryStatus assessmentStatus, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, assessmentStatus,
				null, fullyAssessd, null, null, null, null, null, null, null, 0, null, null, null, null, null, null,
				ObligationOverridable.of(obligation), null, null, null);
	}

}
