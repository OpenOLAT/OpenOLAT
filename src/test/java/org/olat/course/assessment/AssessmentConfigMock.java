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
package org.olat.course.assessment;

import org.olat.course.assessment.handler.AssessmentConfig;

/**
 * 
 * Initial date: 3 Dec 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentConfigMock implements AssessmentConfig {
	
	private boolean assessable;
	private boolean ignoreInCourseAssessment;
	private Mode scoreMode;
	private Float minScore;
	private Float maxScore;
	private Mode passedMode;
	private Float cutValue;
	private boolean passedOverridable;
	private Mode completionMode;
	private boolean attempts;
	private boolean comment;
	private boolean individualAsssessmentDocuments;
	private boolean hasStatus;
	private boolean assessedBusinessGroups;
	private boolean editable;
	private boolean bulkEditable;
	private boolean editableDetails;
	private boolean externalGrading;
	private boolean obligationOverridable;

	@Override
	public boolean isAssessable() {
		return assessable;
	}

	public void setAssessable(boolean assessable) {
		this.assessable = assessable;
	}

	@Override
	public boolean ignoreInCourseAssessment() {
		return ignoreInCourseAssessment;
	}

	@Override
	public void setIgnoreInCourseAssessment(boolean ignoreInCourseAssessment) {
		this.ignoreInCourseAssessment = ignoreInCourseAssessment;
	}

	@Override
	public Mode getScoreMode() {
		return scoreMode;
	}

	public void setScoreMode(Mode scoreMode) {
		this.scoreMode = scoreMode;
	}

	@Override
	public Float getMinScore() {
		return minScore;
	}

	public void setMinScore(Float minScore) {
		this.minScore = minScore;
	}

	@Override
	public Float getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Float maxScore) {
		this.maxScore = maxScore;
	}

	@Override
	public Mode getPassedMode() {
		return passedMode;
	}

	public void setPassedMode(Mode passedMode) {
		this.passedMode = passedMode;
	}

	@Override
	public Float getCutValue() {
		return cutValue;
	}

	public void setCutValue(Float cutValue) {
		this.cutValue = cutValue;
	}

	@Override
	public boolean isPassedOverridable() {
		return passedOverridable;
	}

	public void setPassedOverridable(boolean passedOverridable) {
		this.passedOverridable = passedOverridable;
	}

	@Override
	public Mode getCompletionMode() {
		return completionMode;
	}

	public void setCompletionMode(Mode completionMode) {
		this.completionMode = completionMode;
	}

	@Override
	public boolean hasAttempts() {
		return attempts;
	}

	public void setAttempts(boolean attempts) {
		this.attempts = attempts;
	}

	@Override
	public boolean hasComment() {
		return comment;
	}

	public void hasComment(boolean comment) {
		this.comment = comment;
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return individualAsssessmentDocuments;
	}

	public void setIndividualAsssessmentDocuments(boolean individualAsssessmentDocuments) {
		this.individualAsssessmentDocuments = individualAsssessmentDocuments;
	}

	@Override
	public boolean hasStatus() {
		return hasStatus;
	}

	public void setHasStatus(boolean hasStatus) {
		this.hasStatus = hasStatus;
	}

	@Override
	public boolean isAssessedBusinessGroups() {
		return assessedBusinessGroups;
	}

	public void setAssessedBusinessGroup(boolean assessedBusinessGroups) {
		this.assessedBusinessGroups = assessedBusinessGroups;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@Override
	public boolean isBulkEditable() {
		return bulkEditable;
	}

	public void setBulkEditable(boolean bulkEditable) {
		this.bulkEditable = bulkEditable;
	}

	@Override
	public boolean hasEditableDetails() {
		return editableDetails;
	}

	public void setEditableDetails(boolean editableDetails) {
		this.editableDetails = editableDetails;
	}

	@Override
	public boolean isExternalGrading() {
		return externalGrading;
	}

	public void setExternalGrading(boolean externalGrading) {
		this.externalGrading = externalGrading;
	}

	@Override
	public boolean isObligationOverridable() {
		return obligationOverridable;
	}

	public void setObligationOverridable(boolean obligationOverridable) {
		this.obligationOverridable = obligationOverridable;
	}

}
