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
package org.olat.course.learningpath.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.course.duedate.DueDateConfig;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 30 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UnsupportedLearningPathConfigs implements LearningPathConfigs {

	@Override
	public Boolean hasSequentialChildren() {
		return null;
	}
	
	@Override
	public Integer getDuration() {
		return null;
	}

	@Override
	public void setDuration(Integer duration) {
		//
	}

	@Override
	public Set<AssessmentObligation> getAvailableObligations() {
		return Collections.emptySet();
	}

	@Override
	public AssessmentObligation getObligation() {
		return AssessmentObligation.optional;
	}

	@Override
	public void setObligation(AssessmentObligation obligation) {
		//
	}

	@Override
	public List<ExceptionalObligation> getExceptionalObligations() {
		return null;
	}

	@Override
	public void setExceptionalObligations(List<ExceptionalObligation> exeptionalObligations) {
		//
	}

	@Override
	public boolean isRelativeDates() {
		return false;
	}

	@Override
	public void setRelativeDates(boolean relativeDates) {
		//
	}

	@Override
	public DueDateConfig getStartDateConfig() {
		return null;
	}

	@Override
	public void setStartDateConfig(DueDateConfig start) {
		//
	}

	@Override
	public DueDateConfig getEndDateConfig() {
		return null;
	}

	@Override
	public void setEndDateConfig(DueDateConfig end) {
		//
	}

	@Override
	public FullyAssessedTrigger getFullyAssessedTrigger() {
		return null;
	}

	@Override
	public void setFullyAssessedTrigger(FullyAssessedTrigger trigger) {
		//
	}

	@Override
	public Integer getScoreTriggerValue() {
		return null;
	}

	@Override
	public void setScoreTriggerValue(Integer score) {
		//
	}
	
	@Override
	public FullyAssessedResult isFullyAssessedOnNodeVisited() {
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnConfirmation(boolean confirmed) {
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnScore(Float score, Boolean userVisibility) {
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnPassed(Boolean passed, Boolean userVisibility) {
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnStatus(AssessmentEntryStatus status) {
		return LearningPathConfigs.notFullyAssessed();
	}

}
