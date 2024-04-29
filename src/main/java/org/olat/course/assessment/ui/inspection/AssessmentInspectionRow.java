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

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionRow {
	
	private final String fullName;
	private final Identity assessedIdentity;
	private final AssessmentInspection inspection;
	private final AssessmentInspectionConfiguration configuration;
	
	private final String courseNodeTitle;
	private final String courseNodeIconCssClass;
	
	private final AssessmentEntryStatus assessmentStatus;
	private FormLink toolsButton;
	private FormLink cancelButton;
	
	public AssessmentInspectionRow(String fullName, AssessmentInspection inspection,
			AssessmentEntryStatus assessmentStatus, String courseNodeTitle, String courseNodeIconCssClass) {
		this.fullName = fullName;
		this.inspection = inspection;
		this.assessedIdentity = inspection.getIdentity();
		this.assessmentStatus = assessmentStatus;
		this.configuration = inspection.getConfiguration();
		this.courseNodeTitle = courseNodeTitle;
		this.courseNodeIconCssClass = courseNodeIconCssClass;
	}

	public String getFullName() {
		return fullName;
	}
	
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}
	
	public String getAccessCode() {
		return inspection.getAccessCode();
	}
	
	public String getComment() {
		return inspection.getComment();
	}
	
	public Long getEffectiveDuration() {
		return inspection.getEffectiveDuration();
	}
	
	public Date getFromDate() {
		return inspection.getFromDate();
	}
	
	public Date getToDate() {
		return inspection.getToDate();
	}
	
	public Date getEndTime() {
		return inspection.getEndTime();
	}
	
	public String getSubIdent() {
		return inspection.getSubIdent();
	}
	
	public String getCourseNodeTitle() {
		return courseNodeTitle;
	}

	public String getCourseNodeIconCssClass() {
		return courseNodeIconCssClass;
	}
	
	public AssessmentInspectionConfiguration getConfiguration() {
		return configuration;
	}

	public Integer getInspectionDuration() {
		return configuration.getDuration();
	}
	
	public AssessmentInspectionStatusEnum getInspectionStatus() {
		return inspection.getInspectionStatus();
	}

	public AssessmentInspection getInspection() {
		return inspection;
	}

	public AssessmentEntryStatus getAssessmentStatus() {
		return assessmentStatus;
	}

	public FormLink getToolsButton() {
		return toolsButton;
	}

	public void setToolsButton(FormLink toolsButton) {
		this.toolsButton = toolsButton;
	}

	public FormLink getCancelButton() {
		return cancelButton;
	}

	public void setCancelButton(FormLink cancelButton) {
		this.cancelButton = cancelButton;
	}
}
