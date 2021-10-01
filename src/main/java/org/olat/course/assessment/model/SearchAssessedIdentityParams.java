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
package org.olat.course.assessment.model;

import java.util.Collection;
import java.util.List;

import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchAssessedIdentityParams {
	
	private final RepositoryEntry entry;
	private final RepositoryEntry referenceEntry;
	private final String subIdent;
	
	private final boolean admin;
	private final boolean nonMembers;
	private final boolean coach;
	
	private boolean passed;
	private boolean failed;
	private List<AssessmentEntryStatus> assessmentStatus;
	private Collection<AssessmentObligation> assessmentObligations;
	
	private String searchString;
	private List<Long> businessGroupKeys;
	private List<Long> curriculumElementKeys;
	
	public SearchAssessedIdentityParams(RepositoryEntry entry, String subIdent, RepositoryEntry referenceEntry, 
			AssessmentToolSecurityCallback secCallback) {
		this.entry = entry;
		this.referenceEntry = referenceEntry;
		this.subIdent = subIdent;
		admin = secCallback.isAdmin();
		nonMembers = secCallback.canAssessNonMembers();
		coach = secCallback.canAssessRepositoryEntryMembers()
				|| secCallback.canAssessBusinessGoupMembers()
				|| secCallback.canAssessCurriculumMembers();
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public RepositoryEntry getReferenceEntry() {
		return referenceEntry;
	}

	public String getSubIdent() {
		return subIdent;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isNonMembers() {
		return nonMembers;
	}

	public boolean isCoach() {
		return coach;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public List<AssessmentEntryStatus> getAssessmentStatus() {
		return assessmentStatus;
	}

	public void setAssessmentStatus(List<AssessmentEntryStatus> assessmentStatus) {
		this.assessmentStatus = assessmentStatus;
	}
	
	public Collection<AssessmentObligation> getAssessmentObligations() {
		return assessmentObligations;
	}

	public void setAssessmentObligations(Collection<AssessmentObligation> assessmentObligations) {
		this.assessmentObligations = assessmentObligations;
	}

	public boolean hasBusinessGroupKeys() {
		return businessGroupKeys != null && !businessGroupKeys.isEmpty();
	}

	public List<Long> getBusinessGroupKeys() {
		return businessGroupKeys;
	}

	public void setBusinessGroupKeys(List<Long> businessGroupKeys) {
		this.businessGroupKeys = businessGroupKeys;
	}
	
	public boolean hasCurriculumElementKeys() {
		return curriculumElementKeys != null && !curriculumElementKeys.isEmpty();
	}

	public List<Long> getCurriculumElementKeys() {
		return curriculumElementKeys;
	}

	public void setCurriculumElementKeys(List<Long> curriculumElementKeys) {
		this.curriculumElementKeys = curriculumElementKeys;
	}
}
