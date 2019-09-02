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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.nodes.INode;
import org.olat.course.learningpath.LearningPathObligation;
import org.olat.course.learningpath.LearningPathRoles;
import org.olat.course.learningpath.evaluation.StatusEvaluator.Result;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathEvaluator {
	
	private ObligationEvaluatorProvider obligationEvaluatorProvider;
	private StatusEvaluatorProvider statusEvaluatorProvider;
	private ScoreAccounting scoreAccounting;
	private DurationEvaluatorProvider durationEvaluatorProvider;
	private AccessEvaluator accessEvaluator;
	private LearningPathRoles roles;
	
	private LearningPathTreeNode previousNode;
	
	private LearningPathEvaluator(LearningPathEvaluatorBuilder builder) {
		this.obligationEvaluatorProvider = builder.obligationEvaluatorProvider;
		this.statusEvaluatorProvider = builder.statusEvaluatorProvider;
		this.scoreAccounting = builder.scoreAccounting;
		this.durationEvaluatorProvider = builder.durationEvaluatorProvider;
		this.accessEvaluator = builder.assessEvaluator;
		this.roles = builder.roles;
	}
	
	public void refresh(GenericTreeModel treeModel) {
		this.scoreAccounting.evaluateAll();
		this.previousNode = null;
		
		TreeNode node = treeModel.getRootNode();
		if (node instanceof LearningPathTreeNode) {
			LearningPathTreeNode currentNode = (LearningPathTreeNode)node;
			refreshNodeAndChildren(currentNode);
		}
	}

	private void refreshNodeAndChildren(LearningPathTreeNode currentNode) {
		refreshObligation(currentNode);
		refreshStatus(currentNode, previousNode);
		refreshDuration(currentNode);
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
		
		refreshStatus(currentNode, children);
		refreshDuration(currentNode, children);
		
		refreshAccess(currentNode);
	}

	private void refreshObligation(LearningPathTreeNode currentNode) {
		if (obligationEvaluatorProvider != null) {
			ObligationEvaluator evaluator = obligationEvaluatorProvider.getEvaluator(currentNode.getCourseNode());
			LearningPathObligation obligation = evaluator.getObligation(currentNode.getCourseNode());
			currentNode.setObligation(obligation);
		}
	}

	private void refreshStatus(LearningPathTreeNode currentNode, LearningPathTreeNode previousNode) {
		if (statusEvaluatorProvider != null) {
			StatusEvaluator evaluator = statusEvaluatorProvider.getEvaluator(currentNode.getCourseNode());
			if (evaluator.isStatusDependingOnPreviousNode()) {
				AssessmentEvaluation assessmentEvaluation = scoreAccounting.getScoreEvaluation(currentNode.getCourseNode());
				Result result = evaluator.getStatus(previousNode, assessmentEvaluation);
				currentNode.setStatus(result.getStatus());
				currentNode.setDateDone(result.getDoneDate());
			}
		}
	}

	private void refreshDuration(LearningPathTreeNode currentNode) {
		if (durationEvaluatorProvider != null) {
			DurationEvaluator evaluator = durationEvaluatorProvider.getEvaluator(currentNode.getCourseNode());
			if (evaluator.isDependingOnCurrentNode()) {
				Integer duration = evaluator.getDuration(currentNode.getCourseNode());
				currentNode.setDuration(duration);
			}
		}
	}

	private void refreshStatus(LearningPathTreeNode currentNode, List<LearningPathTreeNode> children) {
		if (statusEvaluatorProvider != null) {
			StatusEvaluator evaluator = statusEvaluatorProvider.getEvaluator(currentNode.getCourseNode());
			if (evaluator.isStatusDependingOnChildNodes()) {
				Result result = evaluator.getStatus(currentNode, children);
				currentNode.setStatus(result.getStatus());
				currentNode.setDateDone(result.getDoneDate());
			}
		}
	}

	private void refreshDuration(LearningPathTreeNode currentNode, List<LearningPathTreeNode> children) {
		if (durationEvaluatorProvider != null) {
			DurationEvaluator evaluator = durationEvaluatorProvider.getEvaluator(currentNode.getCourseNode());
			if (evaluator.isdependingOnChildNodes()) {
				Integer duration = evaluator.getDuration(children);
				currentNode.setDuration(duration);
			}
		}
	}
	
	private void refreshAccess(LearningPathTreeNode currentNode) {
		if (accessEvaluator != null) {
			boolean accessible = accessEvaluator.isAccessible(currentNode, roles);
			currentNode.setAccessible(accessible);
		}
	}
	
	public static LearningPathEvaluatorBuilder builder() {
		return new LearningPathEvaluatorBuilder();
	}
	
	public static class LearningPathEvaluatorBuilder {

		private ObligationEvaluatorProvider obligationEvaluatorProvider;
		private StatusEvaluatorProvider statusEvaluatorProvider;
		private ScoreAccounting scoreAccounting;
		private DurationEvaluatorProvider durationEvaluatorProvider;
		private AccessEvaluator assessEvaluator;
		private LearningPathRoles roles;
		
		private LearningPathEvaluatorBuilder() {
			//
		}
		
		public LearningPathEvaluator build() {
			return new LearningPathEvaluator(this);
		}
		
		public LearningPathEvaluatorBuilder refreshObligation(ObligationEvaluatorProvider obligationEvaluatorProvider) {
			this.obligationEvaluatorProvider = obligationEvaluatorProvider;
			return this;
		}
		
		public LearningPathEvaluatorBuilder refreshStatus(StatusEvaluatorProvider statusEvaluatorProvider, ScoreAccounting scoreAccounting) {
			this.statusEvaluatorProvider = statusEvaluatorProvider;
			this.scoreAccounting = scoreAccounting;
			return this;
		}
		
		public LearningPathEvaluatorBuilder refreshDuration(DurationEvaluatorProvider durationEvaluatorProvider) {
			this.durationEvaluatorProvider = durationEvaluatorProvider;
			return this;
		}
		
		public LearningPathEvaluatorBuilder refreshAccess(AccessEvaluator accessEvaluator, LearningPathRoles roles) {
			this.assessEvaluator = accessEvaluator;
			this.roles = roles;
			return this;
		}
	}
}
