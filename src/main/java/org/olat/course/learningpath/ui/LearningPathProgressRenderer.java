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
package org.olat.course.learningpath.ui;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.StatusCompletionEvaluator;
import org.olat.modules.assessment.ui.component.AbstractLearningProgressCellRenderer;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 13 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathProgressRenderer extends AbstractLearningProgressCellRenderer {
	
	private static final StatusCompletionEvaluator STATUS_COMPLETION_EVALUATOR = new StatusCompletionEvaluator();
	
	private final RepositoryEntry courseEntry;
	
	private CourseAssessmentService courseAssessmentService;
	
	public LearningPathProgressRenderer(RepositoryEntry courseEntry, Locale locale, boolean chartVisible, boolean labelVisible) {
		super(locale, chartVisible, labelVisible);
		this.courseEntry = courseEntry;
		courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
	}

	@Override
	protected AssessmentEvaluation getAssessmentEvaluation(Object cellValue) {
		if (cellValue instanceof LearningPathTreeNode) {
			LearningPathTreeNode learningPathTreeNode = (LearningPathTreeNode)cellValue;
			return learningPathTreeNode.getAssessmentEvaluation();
		}
		return null;
	}

	@Override
	protected float getActual(Object cellValue) {
		if (cellValue instanceof LearningPathTreeNode) {
			LearningPathTreeNode learningPathTreeNode = (LearningPathTreeNode)cellValue;
			float actual = 0.f;
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, learningPathTreeNode.getCourseNode());
			boolean hasCompletion = !Mode.none.equals(assessmentConfig.getCompletionMode());
			if (hasCompletion) {
				actual = learningPathTreeNode.getCompletion() != null ? learningPathTreeNode.getCompletion().floatValue() : 0.0f;
			} else {
				Double statusCompletion = STATUS_COMPLETION_EVALUATOR.getCompletion(learningPathTreeNode.getFullyAssessed(),
						learningPathTreeNode.getAssessmentStatus());
				actual = statusCompletion != null ? statusCompletion.floatValue() : 0;
			}
			return actual;
		}
		return 0;
	}

}
