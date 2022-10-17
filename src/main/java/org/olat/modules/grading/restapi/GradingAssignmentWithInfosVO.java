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
package org.olat.modules.grading.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.assessment.restapi.AssessmentEntryVO;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.model.GradingAssignmentWithInfos;

/**
 * 
 * Initial date: 8 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "gradingAssignmentWithInfosVO")
public class GradingAssignmentWithInfosVO {
	
	private Long key;
	private String assignmentStatus;
	
	private Long assessedIdentityKey;
	private AssessmentEntryVO assessmentEntry;
	
	

	
	public GradingAssignmentWithInfosVO() {
		//
	}
	
	public static final GradingAssignmentWithInfosVO valueOf(GradingAssignmentWithInfos assignmentInfos) {
		GradingAssignmentWithInfosVO vo = new GradingAssignmentWithInfosVO();
		vo.setAssessedIdentityKey(assignmentInfos.getAssessedIdentity().getKey());
		
		AssessmentEntryVO assessmentEntryVo = AssessmentEntryVO.valueOf(assignmentInfos.getAssessmentEntry());
		vo.setAssessmentEntry(assessmentEntryVo);
		
		GradingAssignment assignment = assignmentInfos.getAssignment();
		vo.setKey(assignment.getKey());
		vo.setAssignmentStatus(assignment.getAssignmentStatus().name());
		
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Long getAssessedIdentityKey() {
		return assessedIdentityKey;
	}

	public void setAssessedIdentityKey(Long assessedIdentityKey) {
		this.assessedIdentityKey = assessedIdentityKey;
	}

	public AssessmentEntryVO getAssessmentEntry() {
		return assessmentEntry;
	}

	public void setAssessmentEntry(AssessmentEntryVO assessmentEntry) {
		this.assessmentEntry = assessmentEntry;
	}

	public String getAssignmentStatus() {
		return assignmentStatus;
	}

	public void setAssignmentStatus(String assignmentStatus) {
		this.assignmentStatus = assignmentStatus;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 276789 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof GradingAssignmentWithInfosVO) {
			GradingAssignmentWithInfosVO vo = (GradingAssignmentWithInfosVO)obj;
			return getKey() != null && getKey().equals(vo.getKey());
		}
		return super.equals(obj);
	}
}
