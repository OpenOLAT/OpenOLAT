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

import org.junit.Test;
import org.olat.course.learningpath.LearningPathObligation;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.evaluation.DefaultLinearStatusEvaluator;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
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
		LearningPathStatus status = sut.getStatus(null, AssessmentEntryStatus.done);
		
		assertThat(status).isEqualTo(LearningPathStatus.done);
	}
	
	@Test
	public void shouldReturnReadyIfIsRootNodeAndNotAlreadyDone() {
		LearningPathStatus status = sut.getStatus(null, AssessmentEntryStatus.notStarted);
		
		assertThat(status).isEqualTo(LearningPathStatus.ready);
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
	public void shouldReturnNotAccessibleIfPreviousNodeIsNotDoneAndHasNoObligation() {
		assertStatus(LearningPathStatus.inProgress, null, AssessmentEntryStatus.notStarted, LearningPathStatus.notAccessible);
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

	private void assertStatus(LearningPathStatus previousStatus, LearningPathObligation previousObligation,
			AssessmentEntryStatus currentStatus, LearningPathStatus expected) {
		LearningPathTreeNode previousNode = new LearningPathTreeNode(null, 0);
		previousNode.setStatus(previousStatus);
		previousNode.setObligation(previousObligation);
		
		LearningPathStatus status = sut.getStatus(previousNode, currentStatus);
		
		assertThat(status).isEqualTo(expected);
	}

}
