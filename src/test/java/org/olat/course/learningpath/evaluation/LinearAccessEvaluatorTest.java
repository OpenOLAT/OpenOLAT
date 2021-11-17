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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.test.JunitTestHelper;

/**
 * 
 * Initial date: 2 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LinearAccessEvaluatorTest {
	
	LinearAccessEvaluator sut = new LinearAccessEvaluator();

	@Test
	public void shouldBeAccessibleIfIsDone() {
		assertAccessible(AssessmentEntryStatus.done, true);
	}

	@Test
	public void shouldBeAccessibleIfIsInReview() {
		assertAccessible(AssessmentEntryStatus.inReview, true);
	}


	@Test
	public void shouldBeAccessibleIfIsInProgress() {
		assertAccessible(AssessmentEntryStatus.inProgress, true);
	}

	@Test
	public void shouldBeAccessibleIfIsReady() {
		assertAccessible(AssessmentEntryStatus.notStarted, true);
	}

	@Test
	public void shouldNotBeAccessibleIfIsNotAccessible() {
		assertAccessible(AssessmentEntryStatus.notReady, false);
	}

	@Test
	public void shouldNotBeAccessibleIfHasNoStatus() {
		assertAccessible(null, false);
	}

	private void assertAccessible(AssessmentEntryStatus status, boolean expected) {
		AssessmentEvaluation ae = createAssessmentEvaluation(status);
		LearningPathTreeNode currentNode = new LearningPathTreeNode(createCourseNode(), 0, null, ae, false);
		UserCourseEnvironment userCourseEnv = mock(UserCourseEnvironment.class);
		when(userCourseEnv.isParticipant()).thenReturn(Boolean.TRUE);
		
		boolean accessible = sut.isAccessible(currentNode, userCourseEnv);
		
		assertThat(accessible).isEqualTo(expected);
	}
	
	@Test
	public void shouldAlwaysBeAccessibleAsCoach() {
		AssessmentEvaluation ae = createAssessmentEvaluation(AssessmentEntryStatus.notReady);
		LearningPathTreeNode currentNode = new LearningPathTreeNode(createCourseNode(), 0, null, ae, false);
		UserCourseEnvironment userCourseEnv = mock(UserCourseEnvironment.class);
		when(userCourseEnv.isCoach()).thenReturn(Boolean.TRUE);
		
		boolean accessible = sut.isAccessible(currentNode, userCourseEnv);
		
		assertThat(accessible).isEqualTo(true);
	}
	
	@Test
	public void shouldAlwaysBeAccessibleAsAdmin() {
		AssessmentEvaluation ae = createAssessmentEvaluation(AssessmentEntryStatus.notReady);
		LearningPathTreeNode currentNode = new LearningPathTreeNode(createCourseNode(), 0, null, ae, false);
		UserCourseEnvironment userCourseEnv = mock(UserCourseEnvironment.class);
		when(userCourseEnv.isAdmin()).thenReturn(Boolean.TRUE);
		
		boolean accessible = sut.isAccessible(currentNode, userCourseEnv);
		
		assertThat(accessible).isEqualTo(true);
	}

	private CourseNode createCourseNode() {
		CourseNode courseNode = new STCourseNode();
		courseNode.setIdent(JunitTestHelper.random());
		return courseNode;
	}

	private AssessmentEvaluation createAssessmentEvaluation(AssessmentEntryStatus status) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, status, null, null, null, null, null,
				null, null, null, null, 0, null, null, null, null, null, null, null, null, null, null);
	}

}
