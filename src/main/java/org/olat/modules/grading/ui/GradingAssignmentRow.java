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
package org.olat.modules.grading.ui;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentRef;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingTimeRecord;
import org.olat.modules.grading.model.GradingAssignmentWithInfos;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 23 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAssignmentRow implements GradingAssignmentRef {
	
	private final boolean canGrade;
	private final boolean assessedIdentityVisible;
	private final Identity assessedIdentity;
	private final GradingAssignment assignment;
	private final AssessmentEntry assessmentEntry;
	private final RepositoryEntry referenceEntry;
	private final GradingTimeRecord timeRecord;
	
	private final String courseElementTitle;
	private final String taxonomyLevels;
	
	private FormLink toolsLink;
	
	public GradingAssignmentRow(GradingAssignmentWithInfos assignmentInfos, boolean canGrade) {
		this.assignment = assignmentInfos.getAssignment();
		referenceEntry = assignmentInfos.getReferenceEntry();
		assessmentEntry = assignmentInfos.getAssessmentEntry();
		assessedIdentity = assignmentInfos.getAssessedIdentity();
		assessedIdentityVisible = assignmentInfos.isAssessedIdentityVisible();
		courseElementTitle = assignmentInfos.getCourseElementTitle();
		taxonomyLevels = assignmentInfos.getTaxonomyLevels();
		timeRecord = assignmentInfos.getTimeRecord();
		this.canGrade = canGrade;
	}
	
	@Override
	public Long getKey() {
		return assignment.getKey();
	}
	
	public RepositoryEntry getReferenceEntry() {
		return referenceEntry;
	}
	
	public RepositoryEntry getEntry() {
		return assessmentEntry.getRepositoryEntry();
	}
	
	public String getSubIdent() {
		return assessmentEntry.getSubIdent();
	}
	
	public String getEntryDisplayname() {
		return assessmentEntry.getRepositoryEntry().getDisplayname();
	}
	
	public String getEntryExternalRef() {
		return assessmentEntry.getRepositoryEntry().getExternalRef();
	}
	
	public Long getRecordedSeconds() {
		return timeRecord == null ? null : timeRecord.getTime();
	}
	
	public Long getRecordedMetadataSeconds() {
		return timeRecord == null ? null : timeRecord.getMetadataTime();
	}
	
	public GradingAssignmentStatus getAssignmentStatus() {
		return assignment.getAssignmentStatus();
	}
	
	public Date getAssessmentDate() {
		return assignment.getAssessmentDate();
	}
	
	public Date getAssignmentDate() {
		return assignment.getAssignmentDate();
	}
	
	public Date getDoneDate() {
		return assignment.getClosingDate();
	}
	
	public Date getDeadline() {
		return assignment.getDeadline();
	}
	
	public Date getExtendedDeadline() {
		return assignment.getExtendedDeadline();
	}
	
	public BigDecimal getScore() {
		return assessmentEntry.getScore();
	}
	
	public Boolean getPassed() {
		return assessmentEntry.getPassed();
	}
	
	public String getCourseElementTitle() {
		return courseElementTitle;
	}

	public String getTaxonomyLevels() {
		return taxonomyLevels;
	}

	public GradingAssignment getAssignment() {
		return assignment;
	}
	
	public boolean canGrade() {
		return canGrade;
	}
	
	public boolean isAssessedIdentityVisible() {
		return assessedIdentityVisible;
	}
	
	public boolean hasGrader() {
		return assignment.getGrader() != null;
	}
	
	public Identity getGrader() {
		if(assignment.getGrader() != null) {
			return assignment.getGrader().getIdentity();
		}
		return null;
	}
	
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	

}
