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
package org.olat.course.nodes.st.learningpath;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.olat.course.learningpath.LearningPathObligation;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.evaluation.StatusEvaluator.Result;
import org.olat.course.learningpath.ui.LearningPathTreeNode;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLinearStatusEvaluatorTest {
	
	private STLinearStatusEvaluator sut = new STLinearStatusEvaluator();
	
	@Test
	public void shouldDependOnPreviousNode() {
		assertThat(sut.isStatusDependingOnPreviousNode()).isTrue();
	}
	
	@Test
	public void shouldDependOnChildNodes() {
		assertThat(sut.isStatusDependingOnChildNodes()).isTrue();
	}
	
	@Test
	public void shouldReturnDoneIfAllChildNodesAreDone() {
		LearningPathTreeNode currentNode = new LearningPathTreeNode(null, 0);
		currentNode.setStatus(LearningPathStatus.ready);
		List<LearningPathTreeNode> children = new ArrayList<>();
		LearningPathTreeNode child1 = new LearningPathTreeNode(null, 1);
		child1.setStatus(LearningPathStatus.done);
		child1.setObligation(LearningPathObligation.mandatory);
		children.add(child1);
		LearningPathTreeNode child2 = new LearningPathTreeNode(null, 1);
		child2.setStatus(LearningPathStatus.done);
		child2.setObligation(LearningPathObligation.mandatory);
		children.add(child2);
		// Optional nodes should not be treated
		LearningPathTreeNode child3 = new LearningPathTreeNode(null, 1);
		child3.setStatus(LearningPathStatus.ready);
		child3.setObligation(LearningPathObligation.optional);
		children.add(child3);
		
		Result result = sut.getStatus(currentNode, children);
		
		assertThat(result.getStatus()).isEqualTo(LearningPathStatus.done);
	}
	
	@Test
	public void shouldReturnInProgressIfAtLeastOneChildNodeIsInProgess() {
		LearningPathTreeNode currentNode = new LearningPathTreeNode(null, 0);
		currentNode.setStatus(LearningPathStatus.ready);
		List<LearningPathTreeNode> children = new ArrayList<>();
		LearningPathTreeNode child1 = new LearningPathTreeNode(null, 1);
		child1.setStatus(LearningPathStatus.done);
		children.add(child1);
		LearningPathTreeNode child2 = new LearningPathTreeNode(null, 1);
		child2.setStatus(LearningPathStatus.inProgress);
		children.add(child2);
		LearningPathTreeNode child3 = new LearningPathTreeNode(null, 1);
		child3.setStatus(LearningPathStatus.notAccessible);
		children.add(child3);
		
		Result result = sut.getStatus(currentNode, children);
		
		assertThat(result.getStatus()).isEqualTo(LearningPathStatus.inProgress);
	}
	
	@Test
	public void shouldReturnInProgressIfAtLeastOneChildNodeIsDone() {
		LearningPathTreeNode currentNode = new LearningPathTreeNode(null, 0);
		currentNode.setStatus(LearningPathStatus.ready);
		List<LearningPathTreeNode> children = new ArrayList<>();
		LearningPathTreeNode child1 = new LearningPathTreeNode(null, 1);
		child1.setStatus(LearningPathStatus.done);
		children.add(child1);
		LearningPathTreeNode child2 = new LearningPathTreeNode(null, 1);
		child2.setStatus(LearningPathStatus.ready);
		children.add(child2);
		LearningPathTreeNode child3 = new LearningPathTreeNode(null, 1);
		child3.setStatus(LearningPathStatus.notAccessible);
		children.add(child3);
		
		Result result = sut.getStatus(currentNode, children);
		
		assertThat(result.getStatus()).isEqualTo(LearningPathStatus.inProgress);
	}

	@Test
	public void shouldNotChangeStatusIfNoChildrenIsStarted() {
		LearningPathStatus statusBeforeChildrenEvaluation = LearningPathStatus.ready;
		LearningPathTreeNode currentNode = new LearningPathTreeNode(null, 0);
		currentNode.setStatus(statusBeforeChildrenEvaluation);
		List<LearningPathTreeNode> children = new ArrayList<>();
		LearningPathTreeNode child1 = new LearningPathTreeNode(null, 1);
		child1.setStatus(LearningPathStatus.ready);
		children.add(child1);
		LearningPathTreeNode child2 = new LearningPathTreeNode(null, 1);
		child2.setStatus(LearningPathStatus.notAccessible);
		children.add(child2);
		
		Result result = sut.getStatus(currentNode, children);
		
		assertThat(result.getStatus()).isEqualTo(statusBeforeChildrenEvaluation);
	}
	
	@Test
	public void shouldReturnLatestDoneDate() {
		LearningPathTreeNode currentNode = new LearningPathTreeNode(null, 0);
		currentNode.setStatus(LearningPathStatus.ready);
		List<LearningPathTreeNode> children = new ArrayList<>();
		LearningPathTreeNode child1 = new LearningPathTreeNode(null, 1);
		child1.setStatus(LearningPathStatus.done);
		child1.setDateDone(new GregorianCalendar(2013,1,1).getTime());
		children.add(child1);
		LearningPathTreeNode child2 = new LearningPathTreeNode(null, 1);
		child2.setStatus(LearningPathStatus.done);
		Date latest = new GregorianCalendar(2013,2,1).getTime();
		child2.setDateDone(latest);
		children.add(child2);
		// test status / date mismatch
		LearningPathTreeNode child3 = new LearningPathTreeNode(null, 1);
		child3.setStatus(LearningPathStatus.notAccessible);
		child3.setDateDone(new GregorianCalendar(2013,9,1).getTime());
		children.add(child3);
		// no date
		LearningPathTreeNode child4 = new LearningPathTreeNode(null, 1);
		child4.setStatus(LearningPathStatus.done);
		children.add(child4);
		
		Result result = sut.getStatus(currentNode, children);
		
		assertThat(result.getDoneDate()).isEqualTo(latest);
	}

}
