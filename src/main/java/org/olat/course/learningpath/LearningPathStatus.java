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
package org.olat.course.learningpath;

import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 26 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum LearningPathStatus {
	
	notAccessible("o_lp_not_accessible", "assessment.status.notReady"),
	ready("o_lp_ready", "assessment.status.notStart"),
	inProgress("o_lp_in_progress", "assessment.status.inProgress"),
	done("o_lp_done", "fully.assessed");

	private final String cssClass;
	private final String i18nKey;

	private LearningPathStatus(String cssClass, String i18nKey) {
		this.cssClass = cssClass;
		this.i18nKey = i18nKey;
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public static LearningPathStatus of(AssessmentEvaluation evaluation) {
		if (evaluation == null) {
			return notAccessible;
		}
		
		if (evaluation.getFullyAssessed() != null && evaluation.getFullyAssessed().booleanValue()) {
			return done;
		}
		
		AssessmentEntryStatus status = evaluation.getAssessmentStatus();
		if (status != null) {
			switch(status) {
			case notReady: return notAccessible;
			case notStarted: return ready;
			case inProgress: return inProgress;
			case inReview: return inProgress;
			case done: return inProgress;
			default: return notAccessible;
			}
		}
		
		return notAccessible;
	}
	
	public static boolean isAccessible(LearningPathStatus status) {
		return status == ready
				|| status == inProgress
				|| status == done;
	}
	
}
