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
import org.olat.course.nodes.st.assessment.STLinearStatusEvaluator;
import org.olat.course.run.scoring.AssessmentEvaluation;
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
	public void shouldReturnInProgressIfChildIsDone() {
		assertStatus(AssessmentEntryStatus.notStarted, AssessmentEntryStatus.done, AssessmentEntryStatus.inProgress);
	}
	
	@Test
	public void shouldReturnInProgessEvenIfChildIsNot() {
		assertStatus(AssessmentEntryStatus.inProgress, AssessmentEntryStatus.notStarted, AssessmentEntryStatus.inProgress);
	}
	
	@Test
	public void shouldReturnDoneEvenIfChildIsNot() {
		assertStatus(AssessmentEntryStatus.done, AssessmentEntryStatus.notStarted, AssessmentEntryStatus.done);
	}
	
	public void assertStatus(AssessmentEntryStatus currentStatus, AssessmentEntryStatus childStatus, AssessmentEntryStatus expectedtStatus) {
		AssessmentEvaluation currentEvaluation = getAssessmentEvaluation(null, currentStatus, null);
		AssessmentEvaluation child = getAssessmentEvaluation(Boolean.TRUE, childStatus, null);
		List<AssessmentEvaluation> children = Arrays.asList(child);
		
		AssessmentEntryStatus status = sut.getStatus(currentEvaluation, children);
		
		assertThat(status).isEqualTo(expectedtStatus);
	}

	private AssessmentEvaluation getAssessmentEvaluation(Boolean fullyAssessd, AssessmentEntryStatus assessmentStatus, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, assessmentStatus, null, fullyAssessd, null, null, null, null, null, 0, null, null, null, null, obligation, null);
	}

}
