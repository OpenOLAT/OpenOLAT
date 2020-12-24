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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.StatusEvaluator;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 18 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLearningPathStatusEvaluator implements StatusEvaluator {

	private static final Logger log = Tracing.createLoggerFor(STLearningPathStatusEvaluator.class);

	@Override
	public AssessmentEntryStatus getStatus(AssessmentEvaluation currentEvaluation,
			Blocker blocker) {
		AssessmentEntryStatus status = currentEvaluation.getAssessmentStatus();
		if (isBlocked(blocker)) {
			status = AssessmentEntryStatus.notReady;
		} else {
			status = AssessmentEntryStatus.notStarted;
		}

		log.debug("         fully assessed '{}', obligation '{}, blocked: '{}', status '{}', new status '{}'"
				, currentEvaluation.getFullyAssessed()
				, currentEvaluation.getObligation().getCurrent()
				, isBlocked(blocker)
				, currentEvaluation.getAssessmentStatus()
				, status);

		return status;
	}

	private boolean isBlocked(Blocker blocker) {
		return blocker != null && blocker.isBlocked();
	}

	@Override
	public AssessmentEntryStatus getStatus(AssessmentEvaluation currentEvaluation,
			List<AssessmentEvaluation> children) {
		boolean notStarted = false;
		boolean inProgress = false;
		boolean hasMandatory = false;
		boolean done = true;
		for (AssessmentEvaluation child : children) {
			if (isNotStarted(child)) {
				notStarted = true;
			}
			if (isInProgess(child)) {
				inProgress = true;
			}
			if (isMandatory(child)) {
				hasMandatory = true;
				if (isNotFullyAssessed(child)) {
					done = false;
				}
			}
		}
		
		// Only optional, but no one is started.
		if (!hasMandatory && !inProgress && done && AssessmentEntryStatus.notReady == currentEvaluation.getAssessmentStatus()) {
			return AssessmentEntryStatus.notReady;
		}
		
		if (done)        return AssessmentEntryStatus.done;
		if (inProgress)  return AssessmentEntryStatus.inProgress;
		if (notStarted)  return AssessmentEntryStatus.notStarted;
		                 return currentEvaluation.getAssessmentStatus();
	}
	
	private boolean isNotStarted(AssessmentEvaluation assessmentEvaluation) {
		return isInProgess(assessmentEvaluation)
				|| AssessmentEntryStatus.notStarted.equals(assessmentEvaluation.getAssessmentStatus());
	}

	private boolean isInProgess(AssessmentEvaluation assessmentEvaluation) {
		return AssessmentEntryStatus.inProgress.equals(assessmentEvaluation.getAssessmentStatus())
				|| AssessmentEntryStatus.inReview.equals(assessmentEvaluation.getAssessmentStatus())
				|| AssessmentEntryStatus.done.equals(assessmentEvaluation.getAssessmentStatus());
	}

	private boolean isMandatory(AssessmentEvaluation evaluation) {
		return evaluation.getObligation() != null && AssessmentObligation.mandatory == evaluation.getObligation().getCurrent();
	}
	private boolean isNotFullyAssessed(AssessmentEvaluation assessmentEvaluation) {
		return !Boolean.TRUE.equals(assessmentEvaluation.getFullyAssessed());
	}

}
