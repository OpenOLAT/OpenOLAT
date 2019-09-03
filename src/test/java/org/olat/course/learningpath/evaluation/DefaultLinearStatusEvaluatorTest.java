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
import static org.olat.course.learningpath.LearningPathObligation.mandatory;
import static org.olat.course.learningpath.LearningPathObligation.optional;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.olat.course.learningpath.LearningPathObligation;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.evaluation.StatusEvaluator.Result;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultLinearStatusEvaluatorTest {
	
	DefaultLinearStatusEvaluator sut = new DefaultLinearStatusEvaluator();
	
	@Test
	public void shouldDependOnPreviousNode() {
		assertThat(sut.isStatusDependingOnPreviousNode()).isTrue();
	}
	
	@Test
	public void shouldNotDependOnChildren() {
		assertThat(sut.isStatusDependingOnChildNodes()).isFalse();
	}

	@Test
	public void shouldReturnDoneIfAssessmentStatusIsDone() {
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(AssessmentEntryStatus.done, null);
		
		Result result = sut.getStatus(null, assessmentEvaluation);
		
		assertThat(result.getStatus()).isEqualTo(LearningPathStatus.done);
	}
	
	@Test
	public void shouldReturnReadyIfIsRootNodeAndNotAlreadyDone() {
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(AssessmentEntryStatus.notStarted, null);;
		
		Result result = sut.getStatus(null, assessmentEvaluation);
		
		assertThat(result.getStatus()).isEqualTo(LearningPathStatus.ready);
	}

	@Test
	public void shouldReturnReadyIfPreviousNodeIsDone() {
		assertStatus(LearningPathStatus.done, null, AssessmentEntryStatus.notStarted, LearningPathStatus.ready);
	}

	@Test
	public void shouldReturnReadyIfPreviousNodeIsReadyAndIsOptional() {
		assertStatus(LearningPathStatus.ready, optional, AssessmentEntryStatus.notStarted, LearningPathStatus.ready);
	}

	@Test
	public void shouldReturnNotAccessibleIfPreviousNodeIsInProgressAndIsMandatory() {
		assertStatus(LearningPathStatus.inProgress, mandatory, AssessmentEntryStatus.notStarted, LearningPathStatus.notAccessible);
	}

	@Test
	public void shouldReturnReadyIfPreviousNodeIsStartedAndHasNoObligation() {
		// No obligation means optional. E.g. the STCourseNode has no obligation and is
		// always the first node in a course tree
		assertStatus(LearningPathStatus.inProgress, null, AssessmentEntryStatus.notStarted, LearningPathStatus.ready);
	}

	@Test
	public void shouldReturnAccessibleIfPreviousNodeIsReadyAndIsMandatory() {
		assertStatus(LearningPathStatus.ready, mandatory, AssessmentEntryStatus.notStarted, LearningPathStatus.notAccessible);
	}

	@Test
	public void shouldReturnAccessibleIfPreviousNodeIsInProgressAndIsMandatory() {
		assertStatus(LearningPathStatus.inProgress, mandatory, AssessmentEntryStatus.notStarted, LearningPathStatus.notAccessible);
	}

	@Test
	public void shouldReturnNotAccessibleIfPreviousNodeIsNotAccessibleAndIsMandatory() {
		assertStatus(LearningPathStatus.notAccessible, mandatory, AssessmentEntryStatus.notStarted, LearningPathStatus.notAccessible);
	}

	@Test
	public void shouldReturnNotAccessibleIfPreviousNodeIsNotAccessibleAndIsOptional() {
		assertStatus(LearningPathStatus.notAccessible, optional, AssessmentEntryStatus.notStarted, LearningPathStatus.notAccessible);
	}
	@Test
	public void shouldReturnNotAccessibleIfPreviousNodeNotAccessibleAndHasNoObligation() {
		assertStatus(LearningPathStatus.notAccessible, null, AssessmentEntryStatus.notStarted, LearningPathStatus.notAccessible);
	}

	private void assertStatus(LearningPathStatus previousStatus, LearningPathObligation previousObligation,
			AssessmentEntryStatus currentStatus, LearningPathStatus expected) {
		LearningPathTreeNode previousNode = new LearningPathTreeNode(null, 0);
		previousNode.setStatus(previousStatus);
		previousNode.setObligation(previousObligation);
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(currentStatus, null);
		
		Result result = sut.getStatus(previousNode, assessmentEvaluation);
		
		assertThat(result.getStatus()).isEqualTo(expected);
	}
	
	@Test
	public void shouldReturnDateDone() {
		LearningPathTreeNode previousNode = new LearningPathTreeNode(null, 0);
		Date dateDone = new GregorianCalendar(2019, 3, 1).getTime();
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(AssessmentEntryStatus.done, dateDone);
		
		Result result = sut.getStatus(previousNode, assessmentEvaluation);
		
		assertThat(result.getDoneDate()).isEqualTo(dateDone);
	}
	
	@Test
	public void shouldReturnDateDoneOnlyIfStatusIsDone() {
		LearningPathTreeNode previousNode = new LearningPathTreeNode(null, 0);
		Date dateDone = new GregorianCalendar(2019, 3, 1).getTime();
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(AssessmentEntryStatus.inProgress, dateDone);
		
		Result result = sut.getStatus(previousNode, assessmentEvaluation);
		
		assertThat(result.getDoneDate()).isEqualTo(null);
	}

	private AssessmentEvaluation getAssessmentEvaluation(AssessmentEntryStatus assessmentStatus, Date assessmentDone) {
		return new AssessmentEvaluation(null, null, null, assessmentStatus, null, null, null, null, null, null, null, 0, null, null, null, assessmentDone);
	}

}
