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
package org.olat.course.run.scoring;

import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 9 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class StatusCompletionEvaluator implements CompletionEvaluator {

	@Override
	public Double getCompletion(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ScoreAccounting scoreAccounting, RepositoryEntryRef courseEntry) {
		return getCompletion(currentEvaluation);
	}

	public Double getCompletion(AssessmentEvaluation evaluation) {
		return getCompletion(evaluation.getFullyAssessed(), evaluation.getAssessmentStatus());
	}
	
	public Double getCompletion(Boolean fullyAssessed, AssessmentEntryStatus assessmentStatus) {
		if (fullyAssessed != null && fullyAssessed.booleanValue()) return 1.0;
		if (assessmentStatus == null) return 0.0;
		
		switch (assessmentStatus) {
		case notReady: return 0.0;
		case notStarted: return 0.0;
		case inProgress: return 0.5;
		case inReview: return 0.75;
		case done: return 0.9; // 1.0 is reached when fully assessed
		default: return 0.0;
		}
	}

}
