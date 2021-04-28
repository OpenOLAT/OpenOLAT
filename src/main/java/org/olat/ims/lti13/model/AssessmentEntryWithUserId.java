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
package org.olat.ims.lti13.model;

import org.olat.modules.assessment.AssessmentEntry;

/**
 * 
 * Initial date: 28 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEntryWithUserId {
	
	private final AssessmentEntry assessmentEntry;
	private final String userId;
	
	public AssessmentEntryWithUserId(AssessmentEntry entry, String userId) {
		this.assessmentEntry = entry;
		this.userId = userId;
	}

	public AssessmentEntry getAssessmentEntry() {
		return assessmentEntry;
	}

	public String getUserId() {
		return userId;
	}

	@Override
	public int hashCode() {
		return assessmentEntry == null ? 28648 : assessmentEntry.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AssessmentEntryWithUserId) {
			AssessmentEntryWithUserId entry = (AssessmentEntryWithUserId)obj;
			return assessmentEntry != null && assessmentEntry.equals(entry.assessmentEntry);
		}
		return false;
	}
}
