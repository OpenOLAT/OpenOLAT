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
package org.olat.course.learningpath;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.nodes.INode;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathStatusRefresher {
	
	private LearningPathStatusEvaluatorProvider evaluatorProvider;
	private ScoreAccounting scoreAccounting;
	private LearningPathTreeNode previousNode;

	public void refresh(GenericTreeModel treeModel, LearningPathStatusEvaluatorProvider evaluatorProvider,
			ScoreAccounting scoreAccounting) {
		this.evaluatorProvider = evaluatorProvider;
		this.scoreAccounting = scoreAccounting;
		this.scoreAccounting.evaluateAll();
		this.previousNode = null;
		
		TreeNode node = treeModel.getRootNode();
		if (node instanceof LearningPathTreeNode) {
			LearningPathTreeNode currentNode = (LearningPathTreeNode)node;
			refreshNodeAndChildren(currentNode);
		}
	}

	private void refreshNodeAndChildren(LearningPathTreeNode currentNode) {
		LearningPathStatusEvaluator evaluator = evaluatorProvider.getEvaluator(currentNode.getCourseNode());
		refreshDependingOnPreviousNode(evaluator, currentNode, previousNode);
		previousNode = currentNode;
		
		int childCount = currentNode.getChildCount();
		List<LearningPathTreeNode> children = new ArrayList<>(childCount);
		for (int childIndex = 0; childIndex < childCount; childIndex++) {
			INode child = currentNode.getChildAt(childIndex);
			if (child instanceof LearningPathTreeNode) {
				LearningPathTreeNode childNode = (LearningPathTreeNode)child;
				refreshNodeAndChildren(childNode);
				children.add(childNode);
			}
		}
		
		refreshDependingOnChildren(evaluator, currentNode, children);
	}

	private void refreshDependingOnPreviousNode(LearningPathStatusEvaluator evaluator, LearningPathTreeNode currentNode,
			LearningPathTreeNode previousNode) {
		if (evaluator.isStatusDependingOnPreviousNode()) {
			AssessmentEntryStatus assessmentStatus = getAssessmentStatus(currentNode);
			LearningPathStatus status = evaluator.getStatus(previousNode, assessmentStatus);
			currentNode.setStatus(status);
		}
	}

	private AssessmentEntryStatus getAssessmentStatus(LearningPathTreeNode currentNode) {
		AssessmentEvaluation scoreEvaluation = scoreAccounting.getScoreEvaluation(currentNode.getCourseNode());
		AssessmentEntryStatus assessmentStatus = scoreEvaluation != null
				? scoreEvaluation.getAssessmentStatus()
				: AssessmentEntryStatus.notStarted;
		return assessmentStatus;
	}

	private void refreshDependingOnChildren(LearningPathStatusEvaluator evaluator, LearningPathTreeNode currentNode,
			List<LearningPathTreeNode> children) {
		if (evaluator.isStatusDependingOnChildNodes()) {
			LearningPathStatus status = evaluator.getStatus(currentNode, children);
			currentNode.setStatus(status);
		}
	}
}
