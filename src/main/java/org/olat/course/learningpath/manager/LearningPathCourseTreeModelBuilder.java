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

import org.olat.core.CoreSpringFactory;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.SequenceConfig;
import org.olat.course.learningpath.evaluation.AccessEvaluator;
import org.olat.course.learningpath.evaluation.LinearAccessEvaluator;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.CourseTreeModelBuilder;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathCourseTreeModelBuilder extends CourseTreeModelBuilder {

	private static final AccessEvaluator accessEvaluator = new LinearAccessEvaluator();
	
	private boolean showExcluded = false;
	
	@Autowired
	private LearningPathService learningPathService;
	
	public LearningPathCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
		CoreSpringFactory.autowireObject(this);
	}
	
	public void setShowExcluded(boolean showExcluded) {
		this.showExcluded = showExcluded;
	}

	@Override
	protected CourseTreeNode createCourseTreeNode(CourseNode courseNode, CourseTreeNode parent, int treeLevel) {
		AssessmentEvaluation assessmentEvaluation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
		SequenceConfig sequenceConfig = learningPathService.getSequenceConfig(courseNode);
		LearningPathTreeNode learningPathTreeNode = new LearningPathTreeNode(courseNode, treeLevel, sequenceConfig, assessmentEvaluation);
				
		boolean visible = showExcluded || isVisible(userCourseEnv, assessmentEvaluation);
		learningPathTreeNode.setVisible(visible);
		boolean accessible = accessEvaluator.isAccessible(learningPathTreeNode, userCourseEnv);
		learningPathTreeNode.setAccessible(accessible);
		String iconDecorator1CssClass = getIconDecorator1CssClass(sequenceConfig, assessmentEvaluation, userCourseEnv);
		if (userCourseEnv.isParticipant()) {
			learningPathTreeNode.setIconCssClass(iconDecorator1CssClass);
			learningPathTreeNode.setCssClass(iconDecorator1CssClass);
		} else {
			learningPathTreeNode.setIconDecorator1CssClass(iconDecorator1CssClass);
		}
		
		return learningPathTreeNode;
	}

	private boolean isVisible(UserCourseEnvironment userCourseEnv, AssessmentEvaluation assessmentEvaluation) {
		if (assessmentEvaluation == null || userCourseEnv.isAdmin() || userCourseEnv.isCoach()) return true;
		
		return AssessmentObligation.excluded != assessmentEvaluation.getObligation().getCurrent();
	}

	private String getIconDecorator1CssClass(SequenceConfig sequenceConfig, AssessmentEvaluation assessmentEvaluation, UserCourseEnvironment userCourseEnv) {
		if (assessmentEvaluation == null || userCourseEnv.isAdmin() || userCourseEnv.isCoach()) return null;
		
		String cssClasses = LearningPathStatus.of(assessmentEvaluation).getCssClass();
		
		if (sequenceConfig.isInSequence()) {
			// nodes that take part in a linear sequence
			cssClasses += " o_lp_in_sequence";			
		} else {
			// nodes that can be accessed in free order
			cssClasses += " o_lp_not_in_sequence";			
		}
		// marker for nodes that contains sequenced children
		if (sequenceConfig.hasSequentialChildren()) {
			cssClasses += " o_lp_contains_sequence";
		} else {
			cssClasses += " o_lp_contains_no_sequence";
		}	
		
		return cssClasses;
	}

}
