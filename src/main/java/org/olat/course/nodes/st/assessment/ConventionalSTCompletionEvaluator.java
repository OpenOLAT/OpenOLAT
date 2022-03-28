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

import org.olat.core.CoreSpringFactory;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.CompletionEvaluator;
import org.olat.course.run.scoring.ScoreAccounting;

/**
 * 
 * Initial date: 22 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConventionalSTCompletionEvaluator implements CompletionEvaluator {
	
	private final CourseAssessmentService courseAssessmentService;
	
	public ConventionalSTCompletionEvaluator() {
		this(CoreSpringFactory.getImpl(CourseAssessmentService.class));
	}
	
	public ConventionalSTCompletionEvaluator(CourseAssessmentService courseAssessmentService) {
		this.courseAssessmentService = courseAssessmentService;
	}
	
	@Override
	public Double getCompletion(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ScoreAccounting scoreAccounting) {
		
		Double completion = null;
		
		if (courseNode.getParent() == null) {
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
			if (isPassedConfigurated(assessmentConfig)) {
				completion = isNodePassed(currentEvaluation)? Double.valueOf(1.0): Double.valueOf(0.0);
			}
		}
		
		return completion;
	}

	private boolean isNodePassed(AssessmentEvaluation assessmentEvaluation) {
		return Boolean.TRUE.equals(assessmentEvaluation.getPassed())
				&& assessmentEvaluation.getUserVisible() != null && assessmentEvaluation.getUserVisible().booleanValue();
	}

	private boolean isPassedConfigurated(AssessmentConfig assessmentConfig) {
		return Mode.setByNode == assessmentConfig.getPassedMode();
	}

}
