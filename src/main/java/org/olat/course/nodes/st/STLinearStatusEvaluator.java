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
package org.olat.course.nodes.st;

import java.util.List;

import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.evaluation.DefaultLinearStatusEvaluator;
import org.olat.course.learningpath.evaluation.StatusEvaluator;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLinearStatusEvaluator implements StatusEvaluator {

	private final StatusEvaluator previousEvaluator = new DefaultLinearStatusEvaluator();
	
	@Override
	public boolean isStatusDependingOnPreviousNode() {
		return previousEvaluator.isStatusDependingOnPreviousNode();
	}

	@Override
	public LearningPathStatus getStatus(LearningPathTreeNode previousNode, AssessmentEntryStatus statusCurrentNode) {
		return previousEvaluator.getStatus(previousNode, statusCurrentNode);
	}

	@Override
	public boolean isStatusDependingOnChildNodes() {
		return true;
	}

	@Override
	public LearningPathStatus getStatus(LearningPathTreeNode currentNode, List<LearningPathTreeNode> children) {
		boolean allDone = true;
		boolean inProgress = false;
		for (LearningPathTreeNode child : children) {
			if (allDone && isChildNotDone(child)) {
				allDone = false;
			}
			if (isChildInProgess(child)) {
				inProgress = true;
			}
		}
		
		if (allDone)     return LearningPathStatus.done;
		if (inProgress)  return LearningPathStatus.inProgress;
		                 return currentNode.getStatus();
	}

	private boolean isChildInProgess(LearningPathTreeNode child) {
		return LearningPathStatus.inProgress.equals(child.getStatus())
				|| LearningPathStatus.done.equals(child.getStatus());
	}

	private boolean isChildNotDone(LearningPathTreeNode child) {
		return !LearningPathStatus.done.equals(child.getStatus());
	}

}
