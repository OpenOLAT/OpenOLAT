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

import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchAssessmentInspectionParameters {
	
	private RepositoryEntry entry;
	
	private Boolean activeInspections;
	private List<String> subIdents;
	private List<Long> configurationsKeys;
	private List<AssessmentEntryStatus> assessmentStatus;
	private List<AssessmentInspectionStatusEnum> inspectionStatus;
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	public boolean hasSubIdents() {
		return subIdents != null && !subIdents.isEmpty();
	}
	
	public List<String> getSubIdents() {
		return subIdents;
	}
	
	public void setSubIdents(List<String> subIdents) {
		this.subIdents = subIdents;
	}
	
	public Boolean getActiveInspections() {
		return activeInspections;
	}

	public void setActiveInspections(Boolean activeInspections) {
		this.activeInspections = activeInspections;
	}

	public boolean hasInspectionStatus() {
		return inspectionStatus != null && !inspectionStatus.isEmpty();
	}

	public List<AssessmentInspectionStatusEnum> getInspectionStatus() {
		return inspectionStatus;
	}

	public void setInspectionStatus(List<AssessmentInspectionStatusEnum> status) {
		this.inspectionStatus = status;
	}
	
	public boolean hasAssessmentStatus() {
		return assessmentStatus != null && !assessmentStatus.isEmpty();
	}

	public List<AssessmentEntryStatus> getAssessmentStatus() {
		return assessmentStatus;
	}

	public void setAssessmentStatus(List<AssessmentEntryStatus> assessmentStatus) {
		this.assessmentStatus = assessmentStatus;
	}
	
	public boolean hasConfigurationsKeys() {
		return configurationsKeys != null && !configurationsKeys.isEmpty();
	}

	public List<Long> getConfigurationsKeys() {
		return configurationsKeys;
	}

	public void setConfigurationsKeys(List<Long> configurationsKeys) {
		this.configurationsKeys = configurationsKeys;
	}
	
	

}
