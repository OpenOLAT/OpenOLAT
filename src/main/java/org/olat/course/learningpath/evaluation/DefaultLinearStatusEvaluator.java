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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.learningpath.LearningPathObligation;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 26 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultLinearStatusEvaluator implements StatusEvaluator {

	private static final Logger log = Tracing.createLoggerFor(DefaultLinearStatusEvaluator.class);

	@Override
	public boolean isStatusDependingOnPreviousNode() {
		return true;
	}
	
	@Override
	public Result getStatus(LearningPathTreeNode previousNode, AssessmentEvaluation assessmentEvaluation) {
		LearningPathStatus status = LearningPathStatus.notAccessible;
		AssessmentEntryStatus assessmentStatus = assessmentEvaluation.getAssessmentStatus();
		if (AssessmentEntryStatus.done.equals(assessmentStatus)) {
			status = LearningPathStatus.done;
		} else if (AssessmentEntryStatus.inProgress.equals(assessmentStatus) || AssessmentEntryStatus.inReview.equals(assessmentStatus)) {
			status = LearningPathStatus.inProgress;
		} else if (previousNode == null) {
			status = LearningPathStatus.ready;
		} else if (LearningPathStatus.done.equals(previousNode.getStatus())) {
			status = LearningPathStatus.ready;
		} else if (!LearningPathObligation.mandatory.equals(previousNode.getObligation()) && LearningPathStatus.isAccessible(previousNode.getStatus())) {
			status = LearningPathStatus.ready;
		}
		log.debug("previous node type: {}, previous learning path status: {}, previous obligation: {}, current assessment status: {}, current learning path status: {}"
				, previousNode != null && previousNode.getCourseNode() != null? previousNode.getCourseNode().getType(): null
				, previousNode != null? previousNode.getStatus(): null
				, previousNode != null? previousNode.getObligation(): null
				, assessmentEvaluation
				, status);

		Date dateDone = LearningPathStatus.done.equals(status)? assessmentEvaluation.getAssessmentDone(): null;
		return StatusEvaluator.result(status, dateDone);
	}

	@Override
	public boolean isStatusDependingOnChildNodes() {
		return false;
	}

	@Override
	public Result getStatus(LearningPathTreeNode currentNode, List<LearningPathTreeNode> children) {
		return null;
	}

}
