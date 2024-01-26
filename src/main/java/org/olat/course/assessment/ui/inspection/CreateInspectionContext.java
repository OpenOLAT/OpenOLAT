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
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateInspectionContext {
	
	private Date startDate;
	private Date endDate;
	private boolean accessCode;
	private CourseNode courseNode;
	private MailTemplate mailTemplate;
	private List<IdentityRef> participants;
	private List<DisadvantageCompensation> participantsCompensations;
	private List<InspectionCompensation> inspectionCompensations;
	private AssessmentInspectionConfiguration inspectionConfiguration;
	
	private AssessmentInspection editedInspection;
	private DisadvantageCompensation editedCompensation;
	
	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback secCallback;
	
	public CreateInspectionContext(RepositoryEntry courseEntry, AssessmentToolSecurityCallback secCallback) {
		this.courseEntry = courseEntry;
		this.secCallback = secCallback;
	}
	
	public RepositoryEntry getCourseEntry() {
		return courseEntry;
	}

	public AssessmentToolSecurityCallback getSecCallback() {
		return secCallback;
	}

	public CourseNode getCourseNode() {
		return courseNode;
	}

	public void setCourseNode(CourseNode courseNode) {
		this.courseNode = courseNode;
	}

	public List<IdentityRef> getParticipants() {
		return participants;
	}
	
	public List<DisadvantageCompensation> getParticipantsCompensations() {
		return participantsCompensations;
	}

	public void setParticipants(List<IdentityRef> participants, List<DisadvantageCompensation> compensations) {
		this.participants = participants;
		this.participantsCompensations = compensations;
	}

	public AssessmentInspectionConfiguration getInspectionConfiguration() {
		return inspectionConfiguration;
	}

	public void setInspectionConfiguration(AssessmentInspectionConfiguration inspectionConfiguration) {
		this.inspectionConfiguration = inspectionConfiguration;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isAccessCode() {
		return accessCode;
	}

	public void setAccessCode(boolean accessCode) {
		this.accessCode = accessCode;
	}

	public MailTemplate getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(MailTemplate mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

	public AssessmentInspection getEditedInspection() {
		return editedInspection;
	}
	
	public DisadvantageCompensation getEditedCompensation() {
		return editedCompensation;
	}

	public void setEditedInspection(AssessmentInspection editedInspection, DisadvantageCompensation compensation) {
		this.editedInspection = editedInspection;
		this.editedCompensation = compensation;
	}
	
	public List<InspectionCompensation> getInspectionCompensations() {
		return inspectionCompensations;
	}

	public void setInspectionCompensations(List<InspectionCompensation> inspectionCompensations) {
		this.inspectionCompensations = inspectionCompensations;
	}

	public static record InspectionCompensation(IdentityRef identity, int extraTimeInSeconds) {
		//
	}
}
