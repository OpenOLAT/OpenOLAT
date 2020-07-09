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
package org.olat.modules.assessment.restapi;

import org.olat.modules.assessment.AssessmentEntry;

/**
 * 
 * Initial date: 8 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEntryVO {
	
	private Long key;
	private String assessmentStatus;
	private Boolean userVisibility;
	
	private Long repositoryEntryKey;
	private String subIdent;
	private Long referenceEntryKey;
	
	public AssessmentEntryVO() {
		//
	}
	
	public static final AssessmentEntryVO valueOf(AssessmentEntry entry) {
		AssessmentEntryVO vo = new AssessmentEntryVO();
		
		vo.setKey(entry.getKey());
		if(entry.getAssessmentStatus() != null) {
			vo.setAssessmentStatus(entry.getAssessmentStatus().name());
		}
		vo.setUserVisibility(entry.getUserVisibility());
		if(entry.getRepositoryEntry() != null) {
			vo.setRepositoryEntryKey(entry.getRepositoryEntry().getKey());
		}
		vo.setSubIdent(entry.getSubIdent());
		if(entry.getReferenceEntry() != null) {
			vo.setReferenceEntryKey(entry.getReferenceEntry().getKey());
		}
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getAssessmentStatus() {
		return assessmentStatus;
	}

	public void setAssessmentStatus(String assessmentStatus) {
		this.assessmentStatus = assessmentStatus;
	}

	public Boolean getUserVisibility() {
		return userVisibility;
	}

	public void setUserVisibility(Boolean userVisibility) {
		this.userVisibility = userVisibility;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public void setRepositoryEntryKey(Long repositoryEntryKey) {
		this.repositoryEntryKey = repositoryEntryKey;
	}

	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	public Long getReferenceEntryKey() {
		return referenceEntryKey;
	}

	public void setReferenceEntryKey(Long referenceEntryKey) {
		this.referenceEntryKey = referenceEntryKey;
	}
	
	

}
