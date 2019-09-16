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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.StatusEvaluator;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 26 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultLinearStatusEvaluator implements StatusEvaluator {

	private static final Logger log = Tracing.createLoggerFor(DefaultLinearStatusEvaluator.class);

	@Override
	public AssessmentEntryStatus getStatus(AssessmentEvaluation previousEvaluation, AssessmentEvaluation currentEvaluation) {
		AssessmentEntryStatus currentStatus = currentEvaluation.getAssessmentStatus();
		AssessmentEntryStatus status = currentStatus;
		if (isNotReadyYet(currentStatus)) {
			if (isAccessible(previousEvaluation)) {
				status = AssessmentEntryStatus.notStarted;
			} else {
				status = AssessmentEntryStatus.notReady;
			}
		}

		log.debug("Previous: fully assessed '{}', obligation '{}', status '{}'"
				, previousEvaluation != null? previousEvaluation.getFullyAssessed(): null
				, previousEvaluation != null? previousEvaluation.getObligation(): null
				, previousEvaluation != null? previousEvaluation.getAssessmentStatus(): null);
		log.debug("Current:  fully assessed '{}', obligation '{}, status '{}', new status '{}'"
				, currentEvaluation.getFullyAssessed()
				, currentEvaluation.getObligation()
				, currentEvaluation.getAssessmentStatus()
				, status);

		return status;
	}

	private boolean isNotReadyYet(AssessmentEntryStatus currentStatus) {
		return currentStatus == null || AssessmentEntryStatus.notReady.equals(currentStatus);
	}
	
	private boolean isAccessible(AssessmentEvaluation assessmentEvaluation) {
		return isRoot(assessmentEvaluation)
				|| isFullyAssessed(assessmentEvaluation)
				|| isOptionalAndReady(assessmentEvaluation);
	}

	private boolean isRoot(AssessmentEvaluation assessmentEvaluation) {
		return assessmentEvaluation == null;
	}

	private boolean isFullyAssessed(AssessmentEvaluation assessmentEvaluation) {
		return assessmentEvaluation.getFullyAssessed() != null && assessmentEvaluation.getFullyAssessed();
	}

	private boolean isOptionalAndReady(AssessmentEvaluation assessmentEvaluation) {
		return !AssessmentObligation.mandatory.equals(assessmentEvaluation.getObligation())
				&& !AssessmentEntryStatus.notReady.equals(assessmentEvaluation.getAssessmentStatus());
	}

	@Override
	public AssessmentEntryStatus getStatus(AssessmentEvaluation currentEvaluation,
			List<AssessmentEvaluation> children) {
		return currentEvaluation.getAssessmentStatus();
	}

}
