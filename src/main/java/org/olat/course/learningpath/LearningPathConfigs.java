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

import java.util.Date;

import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Configuration of a single course node.
 * 
 * Initial date: 30 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface LearningPathConfigs {
	
	public static final AssessmentObligation OBLIGATION_DEFAULT = AssessmentObligation.mandatory;
	public static final FullyAssessedTrigger LEGACY_TRIGGER_DEFAULT = FullyAssessedTrigger.confirmed;

	
	public Boolean hasSequentialChildren();
	
	public Integer getDuration();
	
	public void setDuration(Integer duration);

	public AssessmentObligation getObligation();
	
	public void setObligation(AssessmentObligation obligation);
	
	public Date getStartDate();
	
	public void setStartDate(Date start);
	
	public Date getEndDate();
	
	public void setEndDate(Date end);
	
	public FullyAssessedTrigger getFullyAssessedTrigger();
	
	public void setFullyAssessedTrigger(FullyAssessedTrigger trigger);
	
	public Integer getScoreTriggerValue();
	
	public void setScoreTriggerValue(Integer score);
	
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
