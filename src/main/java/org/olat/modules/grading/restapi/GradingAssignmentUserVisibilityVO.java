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

import org.olat.modules.grading.GradingAssignment;

/**
 * 
 * Initial date: 8 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "gradingAssignmentUserVisibilityVO")
public class GradingAssignmentUserVisibilityVO {
	
	private Long assignmentKey;
	private Boolean userVisibility;
	
	public GradingAssignmentUserVisibilityVO() {
		//
	}
	
	public static final GradingAssignmentUserVisibilityVO valueOf(GradingAssignment assignment) {
		GradingAssignmentUserVisibilityVO vo = new GradingAssignmentUserVisibilityVO();
		vo.setAssignmentKey(assignment.getKey());
		vo.setUserVisibility(assignment.getAssessmentEntry().getUserVisibility());
		return vo;
	}

	public Long getAssignmentKey() {
		return assignmentKey;
	}

	public void setAssignmentKey(Long assignmentKey) {
		this.assignmentKey = assignmentKey;
	}

	public Boolean getUserVisibility() {
		return userVisibility;
	}

	public void setUserVisibility(Boolean userVisibility) {
		this.userVisibility = userVisibility;
	}
}
