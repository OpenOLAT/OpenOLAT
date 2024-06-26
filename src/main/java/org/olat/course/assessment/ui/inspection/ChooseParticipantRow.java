/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseParticipantRow extends UserPropertiesRow {
	
	private final Boolean userVisibility;
	private final AssessmentEntryStatus assessmentStatus;
	private final Boolean passed;
	private final Boolean passedOverriden;
	private final Boolean compensation;
	
	public ChooseParticipantRow(Identity identity, AssessmentEntry entry, Boolean compensation,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.compensation = compensation;
		
		if(entry != null) {
			userVisibility = entry.getUserVisibility();
			assessmentStatus = entry.getAssessmentStatus();
			passed = entry.getPassed();
			passedOverriden = Boolean.valueOf(entry.getPassedOverridable().isOverridden());
		} else {
			userVisibility = null;
			assessmentStatus = AssessmentEntryStatus.notStarted;
			passed = null;
			passedOverriden = null;
		}
	}
	
	public Boolean getUserVisibility() {
		return userVisibility;
	}

	public AssessmentEntryStatus getAssessmentStatus() {
		return assessmentStatus;
	}

	public Boolean getPassed() {
		return passed;
	}

	public Boolean getPassedOverriden() {
		return passedOverriden;
	}

	public Boolean getCompensation() {
		return compensation;
	}
}
