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
package org.olat.course.nodes.st.assessment;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.CompletionEvaluator;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 23 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class NumberOfNodesCompletionEvaluator implements CompletionEvaluator {
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	@Override
	public Double getCompletion(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			CourseConfig courseConfig, ScoreAccounting scoreAccounting) {
		
		List<CourseNode> children = new ArrayList<>();
		collectChildren(children, courseNode);
		
		int count = 0;
		double completion = 0.0;
		for (CourseNode child: children) {
			AssessmentEvaluation assessmentEvaluation = scoreAccounting.evalCourseNode(child);
			if (isMandatory(assessmentEvaluation)) {
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(child);
				if (Mode.setByNode.equals(assessmentConfig.getCompletionMode())) {
					count++;
					completion += assessmentEvaluation.getCompletion() != null
							? assessmentEvaluation.getCompletion().doubleValue()
							: 0.0;
				} else if (Mode.none.equals(assessmentConfig.getCompletionMode())) {
					count++;
					completion += getCompletion(assessmentEvaluation);
				}
			}
		}
		
		return count > 0? completion / count: null;
	}

	private void collectChildren(List<CourseNode> children, CourseNode courseNode) {
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			INode child = courseNode.getChildAt(i);
			if (child instanceof CourseNode) {
				CourseNode childCourseNode = (CourseNode) child;
				children.add(childCourseNode);
				collectChildren(children, childCourseNode);
			}
		}
	}

	private boolean isMandatory(AssessmentEvaluation evaluation) {
		return evaluation.getObligation() != null && AssessmentObligation.mandatory.equals(evaluation.getObligation());
	}

	private double getCompletion(AssessmentEvaluation evaluation) {
		if (evaluation.getFullyAssessed() != null && evaluation.getFullyAssessed().booleanValue()) return 1.0;
		
		AssessmentEntryStatus assessmentStatus = evaluation.getAssessmentStatus();
		if (assessmentStatus == null) return 0.0;
		
		switch (assessmentStatus) {
		case notReady: return 0.0;
		case notStarted: return 0.0;
		case inProgress: return 0.5;
		case inReview: return 0.75;
		case done: return 1.0;
		default: return 0.0;
		}
	}

}
