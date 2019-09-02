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

import java.util.Date;
import java.util.List;

import org.olat.course.learningpath.LearningPathObligation;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.evaluation.DefaultLinearStatusEvaluator;
import org.olat.course.learningpath.evaluation.StatusEvaluator;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.run.scoring.AssessmentEvaluation;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class STLinearStatusEvaluator implements StatusEvaluator {

	private final StatusEvaluator previousEvaluator = new DefaultLinearStatusEvaluator();
	
	@Override
	public boolean isStatusDependingOnPreviousNode() {
		return previousEvaluator.isStatusDependingOnPreviousNode();
	}

	@Override
	public Result getStatus(LearningPathTreeNode previousNode, AssessmentEvaluation assessmentEvaluation) {
		return previousEvaluator.getStatus(previousNode, assessmentEvaluation);
	}

	@Override
	public boolean isStatusDependingOnChildNodes() {
		return true;
	}

	@Override
	public Result getStatus(LearningPathTreeNode currentNode, List<LearningPathTreeNode> children) {
		boolean allDone = true;
		boolean inProgress = false;
		Date latestDoneDate = currentNode.getDateDone();
		for (LearningPathTreeNode child : children) {
			if (allDone && isNotOptional(child) && isNotDone(child)) {
				allDone = false;
			}
			if (isInProgess(child)) {
				inProgress = true;
			}
			if (isDone(child) && isDoneLater(child, latestDoneDate)) {
				latestDoneDate = child.getDateDone();
			}
		}
		
		LearningPathStatus status = currentNode.getStatus();
		if (allDone) {
			status = LearningPathStatus.done;
		} else if (inProgress) {
			status =  LearningPathStatus.inProgress;
		}
		return StatusEvaluator.result(status, latestDoneDate);
	}

	private boolean isDoneLater(LearningPathTreeNode child, Date latestDoneDate) {
		if (latestDoneDate == null) return true;
		
		return child.getDateDone() != null && child.getDateDone().after(latestDoneDate);
	}

	private boolean isNotOptional(LearningPathTreeNode node) {
		return !LearningPathObligation.optional.equals(node.getObligation());
	}

	private boolean isInProgess(LearningPathTreeNode node) {
		return LearningPathStatus.inProgress.equals(node.getStatus())
				|| LearningPathStatus.done.equals(node.getStatus());
	}

	private boolean isDone(LearningPathTreeNode node) {
		return LearningPathStatus.done.equals(node.getStatus());
	}

	private boolean isNotDone(LearningPathTreeNode node) {
		return !isDone(node);
	}

}
