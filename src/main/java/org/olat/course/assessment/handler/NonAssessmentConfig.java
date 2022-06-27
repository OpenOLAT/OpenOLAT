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
package org.olat.course.assessment.handler;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NonAssessmentConfig implements AssessmentConfig {
	
	private static final AssessmentConfig INSATNCE = new NonAssessmentConfig();
	
	public static final AssessmentConfig create() {
		return INSATNCE;
	}

	@Override
	public boolean isAssessable() {
		return false;
	}
	
	@Override
	public boolean ignoreInCourseAssessment() {
		return true;
	}

	@Override
	public void setIgnoreInCourseAssessment(boolean ignoreInCourseAssessment) {
		//
	}

	private NonAssessmentConfig() {
		//
	}

	@Override
	public Mode getScoreMode() {
		return Mode.none;
	}

	@Override
	public Float getMaxScore() {
		return null;
	}

	@Override
	public Float getMinScore() {
		return null;
	}

	@Override
	public boolean hasGrade() {
		return false;
	}
	
	@Override
	public boolean isAutoGrade() {
		return false;
	}

	@Override
	public Mode getPassedMode() {
		return Mode.none;
	}

	@Override
	public Float getCutValue() {
		return null;
	}

	@Override
	public boolean isPassedOverridable() {
		return false;
	}
	
	@Override
	public Boolean getInitialUserVisibility(boolean done, boolean coachCanNotEdit) {
		return null;
	}

	@Override
	public Mode getCompletionMode() {
		return Mode.none;
	}

	@Override
	public boolean hasAttempts() {
		return false;
	}

	@Override
	public boolean hasMaxAttempts() {
		return false;
	}

	@Override
	public Integer getMaxAttempts() {
		return null;
	}

	@Override
	public boolean hasComment() {
		return false;
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return false;
	}

	@Override
	public boolean hasStatus() {
		return false;
	}

	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isBulkEditable() {
		return false;
	}

	@Override
	public boolean hasEditableDetails() {
		return false;
	}
	
	@Override
	public boolean isExternalGrading() {
		return false;
	}

	@Override
	public boolean isObligationOverridable() {
		return false;
	}

}
