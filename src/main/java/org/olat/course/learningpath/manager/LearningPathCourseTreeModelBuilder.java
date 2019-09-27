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
package org.olat.course.learningpath.manager;

import org.olat.course.learningpath.evaluation.AccessEvaluator;
import org.olat.course.learningpath.evaluation.LinearAccessEvaluator;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.CourseTreeModelBuilder;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 25 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathCourseTreeModelBuilder extends CourseTreeModelBuilder {

	private static final AccessEvaluator accessEvaluator = new LinearAccessEvaluator();
	
	public LearningPathCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
		userCourseEnv.getScoreAccounting().evaluateAll(true);
	}

	@Override
	protected CourseTreeNode createCourseTreeNode(CourseNode courseNode, int treeLevel) {
		AssessmentEvaluation assessmentEvaluation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
		LearningPathTreeNode learningPathTreeNode = new LearningPathTreeNode(courseNode, treeLevel, assessmentEvaluation);
		
		learningPathTreeNode.setVisible(true);
		boolean accessible = accessEvaluator.isAccessible(learningPathTreeNode, userCourseEnv);
		learningPathTreeNode.setAccessible(accessible);
		String iconDecorator1CssClass = getIconDecorator1CssClass(assessmentEvaluation, userCourseEnv);
		learningPathTreeNode.setIconDecorator1CssClass(iconDecorator1CssClass);
		
		return learningPathTreeNode;
	}

	private String getIconDecorator1CssClass(AssessmentEvaluation assessmentEvaluation, UserCourseEnvironment userCourseEnv) {
		if (assessmentEvaluation == null || userCourseEnv.isAdmin() || userCourseEnv.isCoach()) return null;
		
		if (assessmentEvaluation.getFullyAssessed() != null && assessmentEvaluation.getFullyAssessed().booleanValue()) {
			return "o_lp_done";
		}
		
		AssessmentEntryStatus status = assessmentEvaluation.getAssessmentStatus();
		if (status != null) {
			switch(status) {
			case notReady: return "o_lp_not_accessible";
			case notStarted: return "o_lp_ready";
			case inProgress: return "o_lp_in_progress";
			case inReview: return "o_lp_in_progress";
			case done: return "o_lp_in_progress";
			default:
			}
		}
		return null;
	}

}
