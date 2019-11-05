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
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 18 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLinearStatusEvaluatorTest {
	
	private STLinearStatusEvaluator sut = new STLinearStatusEvaluator();
	@Test
	public void shouldNotBlockIfMandatoryAndNotFullyAssessed() {
		Blocker blocker = new Blocker();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.FALSE, null, AssessmentObligation.mandatory);

		sut.getStatus(currentEvaluation, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldNotBlockIfNotMandatoryAndNotFullyAssessed() {
		Blocker blocker = new Blocker();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.FALSE, null, AssessmentObligation.optional);

		sut.getStatus(currentEvaluation, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldNotBlockIfMandatoryAndFullyAssessed() {
		Blocker blocker = new Blocker();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.TRUE, null, AssessmentObligation.mandatory);

		sut.getStatus(currentEvaluation, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldInitStatusNotStartedIfNotBlocked() {
		Blocker blocker = new Blocker();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, AssessmentEntryStatus.done, null);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, blocker);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.notStarted);
	}
	
	@Test
	public void shouldInitStatusNotReadyIfBlocked() {
		Blocker blocker = new Blocker();
		blocker.block();
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, AssessmentEntryStatus.done, null);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, blocker);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.notReady);
	}

	@Test
	public void shouldReturnInProgressIfChildIsInProgressNull() {
		assertStatus(null, AssessmentEntryStatus.inProgress, AssessmentEntryStatus.inProgress);
	}
	
	@Test
	public void shouldReturnInProgressIfChildIsInProgress() {
		assertStatus(AssessmentEntryStatus.notStarted, AssessmentEntryStatus.inProgress, AssessmentEntryStatus.inProgress);
	}
	
	@Test
	public void shouldReturnInProgressIfChildIsInReview() {
		assertStatus(AssessmentEntryStatus.notStarted, AssessmentEntryStatus.inReview, AssessmentEntryStatus.inProgress);
	}
	
	@Test
	public void shouldReturnNotStartedIfNoChildIsStarted() {
		assertStatus(AssessmentEntryStatus.inProgress, AssessmentEntryStatus.notStarted, AssessmentEntryStatus.notStarted);
	}
	
	private void assertStatus(AssessmentEntryStatus currentStatus, AssessmentEntryStatus childStatus, AssessmentEntryStatus expectedtStatus) {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, currentStatus, null);
		AssessmentEvaluation child = getAssessmentEvaluation(Boolean.TRUE, childStatus, AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(child);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, children);
		
		assertThat(status).isEqualTo(expectedtStatus);
	}
	
	@Test
	public void shouldReturnDoneIfAllMandatoryChildrenAreDone() {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.FALSE, AssessmentEntryStatus.inProgress, null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(Boolean.TRUE, AssessmentEntryStatus.done,
				AssessmentObligation.mandatory);
		AssessmentEvaluation child2 = getAssessmentEvaluation(Boolean.TRUE, AssessmentEntryStatus.inProgress,
				AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, children);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.done);
	}
	
	@Test
	public void shouldNotReturnDoneIfNotAllMandatoryChildrenAreDone() {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.TRUE, AssessmentEntryStatus.done, null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(Boolean.TRUE, AssessmentEntryStatus.inProgress,
				AssessmentObligation.mandatory);
		AssessmentEvaluation child2 = getAssessmentEvaluation(Boolean.TRUE, AssessmentEntryStatus.done,
				AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, children);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.inProgress);
	}
	
	@Test
	public void shouldReturnDoneIfItHAsOnlyOptionalChildren() {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(Boolean.TRUE, AssessmentEntryStatus.notStarted, null);
		AssessmentEvaluation child1 = getAssessmentEvaluation(Boolean.FALSE, AssessmentEntryStatus.inProgress,
				AssessmentObligation.optional);
		AssessmentEvaluation child2 = getAssessmentEvaluation(Boolean.FALSE, AssessmentEntryStatus.notStarted,
				AssessmentObligation.optional);
		List<AssessmentEvaluation> children = Arrays.asList(child1, child2);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, children);
		
		assertThat(status).isEqualTo(AssessmentEntryStatus.done);
	}

	private AssessmentEvaluation getAssessmentEvaluation(Boolean fullyAssessd, AssessmentEntryStatus assessmentStatus, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, assessmentStatus, null, fullyAssessd, null, null, null,
				null, null, null, 0, null, null, null, null, null, obligation, null, null, null);
	}

}
