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
import org.olat.course.learningpath.LearningPathRoles;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

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
		LearningPathTreeNode currentNode = new LearningPathTreeNode(null, 0);
		currentNode.setStatus(status);
		LearningPathRoles roles = LearningPathRoles.of(true, false, false);
		
		boolean accessible = sut.isAccessible(currentNode, roles);
		
		assertThat(accessible).isEqualTo(expected);
	}
	
	@Test
	public void shouldAlwysBeAccessibleAsCoach() {
		LearningPathTreeNode currentNode = new LearningPathTreeNode(null, 0);
		currentNode.setStatus(AssessmentEntryStatus.notReady);
		LearningPathRoles roles = LearningPathRoles.of(true, true, false);
		
		boolean accessible = sut.isAccessible(currentNode, roles);
		
		assertThat(accessible).isEqualTo(true);
	}
	
	@Test
	public void shouldAlwysBeAccessibleAsAdmin() {
		LearningPathTreeNode currentNode = new LearningPathTreeNode(null, 0);
		currentNode.setStatus(AssessmentEntryStatus.notReady);
		LearningPathRoles roles = LearningPathRoles.of(true, false, true);
		
		boolean accessible = sut.isAccessible(currentNode, roles);
		
		assertThat(accessible).isEqualTo(true);
	}


}
