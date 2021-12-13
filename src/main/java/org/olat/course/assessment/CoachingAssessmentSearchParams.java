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

import org.olat.core.id.Identity;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 24 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CoachingAssessmentSearchParams {
	
	private String searchString;
	private Identity coach;
	private AssessmentEntryStatus status;
	private Boolean userVisibility;
	private boolean userVisibilitySettable;
	
	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Identity getCoach() {
		return coach;
	}

	public void setCoach(Identity coach) {
		this.coach = coach;
	}

	public AssessmentEntryStatus getStatus() {
		return status;
	}

	public void setStatus(AssessmentEntryStatus status) {
		this.status = status;
	}

	public Boolean getUserVisibility() {
		return userVisibility;
	}

	public void setUserVisibility(Boolean userVisibility) {
		this.userVisibility = userVisibility;
	}

	public boolean isUserVisibilitySettable() {
		return userVisibilitySettable;
	}

	public void setUserVisibilitySettable(boolean userVisibilitySettable) {
		this.userVisibilitySettable = userVisibilitySettable;
	}

}
