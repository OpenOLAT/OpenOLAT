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

import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 30 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface LearningPathConfigs {
	
	public Integer getDuration();
	
	public void setDuration(Integer duration);

	public AssessmentObligation getObligation();
	
	public FullyAssessedResult isFullyAssessedOnNodeVisited();
	
	public FullyAssessedResult isFullyAssessedOnConfirmation(boolean confirmed);

	public FullyAssessedResult isFullyAssessedOnScore(Float score, Boolean userVisibility);

	public FullyAssessedResult isFullyAssessedOnPassed(Boolean passed, Boolean userVisibility);
	
	public FullyAssessedResult isFullyAssessedOnStatus(AssessmentEntryStatus status);
	
	
	public static FullyAssessedResult notFullyAssessed() {
		return new FullyAssessedResultImpl(false, false, false);
	}
	
	public static FullyAssessedResult fullyAssessed(boolean enabled, boolean fullyAssessed, boolean done) {
		return new FullyAssessedResultImpl(enabled, fullyAssessed, done);
	}
	
	public interface FullyAssessedResult {
		
		public boolean isEnabled();
		
		public boolean isFullyAssessed();
		
		public boolean isDone();
	}

}
