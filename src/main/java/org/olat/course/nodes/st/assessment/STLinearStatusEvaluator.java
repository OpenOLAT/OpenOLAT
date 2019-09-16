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

import java.util.List;

import org.olat.course.learningpath.evaluation.DefaultLinearStatusEvaluator;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.StatusEvaluator;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 18 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLinearStatusEvaluator implements StatusEvaluator {
	
	private final StatusEvaluator defaultEvaluator = new DefaultLinearStatusEvaluator();

	@Override
	public AssessmentEntryStatus getStatus(AssessmentEvaluation previousEvaluation,
			AssessmentEvaluation currentEvaluation) {
		return defaultEvaluator.getStatus(previousEvaluation, currentEvaluation);
	}

	@Override
	public AssessmentEntryStatus getStatus(AssessmentEvaluation currentEvaluation,
			List<AssessmentEvaluation> children) {
		for (AssessmentEvaluation child : children) {
			if (isInProgess(child)) {
				return AssessmentEntryStatus.inProgress;
			}
		}
		return currentEvaluation.getAssessmentStatus();
	}
	
	private boolean isInProgess(AssessmentEvaluation assessmentEvaluation) {
		return AssessmentEntryStatus.inProgress.equals(assessmentEvaluation.getAssessmentStatus())
				|| AssessmentEntryStatus.inReview.equals(assessmentEvaluation.getAssessmentStatus())
				|| AssessmentEntryStatus.done.equals(assessmentEvaluation.getAssessmentStatus());
	}

}
