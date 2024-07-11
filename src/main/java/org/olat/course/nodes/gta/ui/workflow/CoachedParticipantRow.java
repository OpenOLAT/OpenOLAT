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
package org.olat.course.nodes.gta.ui.workflow;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.id.Identity;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.model.TaskImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 25 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachedParticipantRow extends UserPropertiesRow {

	private final Task task;
	private final Identity coach;
	private final Identity assessedIdentity;
	private final TaskDefinition taskDefinition;
	private final AssessmentEntry assessmentEntry;
	
	private DueDate asssignmentDueDate;
	
	private DueDate submissionDueDate;
	private DueDate lateSubmissionDueDate;
	private boolean lateSubmission;
	private boolean canCollectSubmission;
	private boolean canViewSubmittedDocuments;

	private boolean correctionsDoneWithoutDocuments;
	
	private boolean revisions;
	private TaskRevision lastRevision;
	
	private DueDate solutionDueDate;
	
	private FormLink markLink;
	private String coachFullName;
	private Long coachKey;
	private CoachedParticipantStatus status;

	private FormLink toolsLink;
	private FormLink openTaskFileLink;
	private FormLink backToSubmissionLink;
	private FormLink collectDocumentsLink;
	private DownloadLink downloadTaskFileLink;
	
	private Controller detailsCtrl;
	private FormItem controllerDetailsEl;
	
	public CoachedParticipantRow(Identity assessedIdentity, Task task, TaskDefinition taskDefinition, AssessmentEntry assessmentEntry,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(assessedIdentity, userPropertyHandlers, locale);
		this.task = task;
		this.assessedIdentity = assessedIdentity;
		this.coach = assessmentEntry == null ? null : assessmentEntry.getCoach();
		this.taskDefinition = taskDefinition;
		this.assessmentEntry = assessmentEntry;
	}

	public String getTaskName() {
		return task == null ? null : task.getTaskName();
	}
	
	public String getTaskTitle() {
		return taskDefinition == null ? null : taskDefinition.getTitle();
	}

	public TaskProcess getTaskStatus() {
		return task == null ? null : task.getTaskStatus();
	}

	public Date getAsssignmentDate() {
		return task == null ? null : task.getAssignmentDate();
	}
	
	public Date getAcceptationDate() {
		return task == null ? null : task.getAcceptationDate();
	}
	
	public DueDate getAssignmentDueDate() {
		return asssignmentDueDate;
	}

	public void setAssignmentDueDate(DueDate asssignmentDueDate) {
		this.asssignmentDueDate = asssignmentDueDate;
	}

	public Date getSubmissionDate() {
		return task == null ? null : task.getSubmissionDate();
	}

	public DueDate getSubmissionDueDate() {
		return submissionDueDate;
	}
	
	public void setSubmissionDueDate(DueDate dueDate) {
		submissionDueDate = dueDate;
	}

	public DueDate getLateSubmissionDueDate() {
		return lateSubmissionDueDate;
	}
	
	public void setLateSubmissionDueDate(DueDate dueDate) {
		lateSubmissionDueDate = dueDate;
	}

	public Date getSubmissionRevisionsDate() {
		return task == null ? null : task.getSubmissionRevisionsDate();
	}
	
	public boolean isLateSubmission() {
		return lateSubmission;
	}

	public void setLateSubmission(boolean lateSubmission) {
		this.lateSubmission = lateSubmission;
	}
	
	public boolean isCanCollectSubmission() {
		return canCollectSubmission;
	}

	public void setCanCollectSubmission(boolean canCollectSubmission) {
		this.canCollectSubmission = canCollectSubmission;
	}

	public boolean isCanViewSubmittedDocuments() {
		return canViewSubmittedDocuments;
	}

	public void setCanViewSubmittedDocuments(boolean canViewSubmittedDocuments) {
		this.canViewSubmittedDocuments = canViewSubmittedDocuments;
	}

	public boolean isSubmissionExtended() {
		Date overridenDate = getSubmissionDueDate() == null ? null : getSubmissionDueDate().getOverridenDueDate();
		Date lateOverridenDate = getLateSubmissionDueDate() == null ? null : getLateSubmissionDueDate().getOverridenDueDate();
		return overridenDate != null || lateOverridenDate != null;
	}

	public Date getCollectionDate() {
		return task == null ? null : task.getCollectionDate();
	}

	public DueDate getSolutionDueDate() {
		return solutionDueDate;
	}

	public void setSolutionDueDate(DueDate solutionDueDate) {
		this.solutionDueDate = solutionDueDate;
	}

	public Task getTask() {
		return task;
	}
	
	public Identity getCoach() {
		return coach;
	}
	
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public Boolean getUserVisibility() {
		return assessmentEntry == null ? null : assessmentEntry.getUserVisibility();
	}

	public BigDecimal getScore() {
		return assessmentEntry == null ? null : assessmentEntry.getScore();
	}

	public Boolean getPassed() {
		return assessmentEntry == null ? null : assessmentEntry.getPassed();
	}
	
	public AssessmentEntryStatus getAssessmentStatus() {
		return assessmentEntry == null ? null : assessmentEntry.getAssessmentStatus();
	}
	
	public Date getAssessmentDone() {
		return assessmentEntry == null ? null : assessmentEntry.getAssessmentDone();
	}

	public CoachedParticipantStatus getStatus() {
		return status;
	}

	public void setStatus(CoachedParticipantStatus status) {
		this.status = status;
	}

	public boolean hasSubmittedDocs() {
		return task != null && task.getSubmissionNumOfDocs() != null && task.getSubmissionNumOfDocs().intValue() > 0;
	}
	
	public Integer getSubmissionNumOfDocs() {
		return task == null ? null : task.getSubmissionNumOfDocs();
	}
	
	public Integer getCollectionNumOfDocs() {
		return task == null ? null : task.getCollectionNumOfDocs();
	}
	
	public Date getCollectionRevisionsDate() {
		return task == null ? null : task.getCollectionRevisionsDate();
	}
	
	public Integer getSubmissionRevisionsNumOfDocs() {
		return task instanceof TaskImpl ti ? ti.getSubmissionRevisionsNumOfDocs() : null;
	}
	
	public Integer getCollectionRevisionsNumOfDocs() {
		return task instanceof TaskImpl ti ? ti.getCollectionRevisionsNumOfDocs() : null;
	}
	
	public boolean isCorrectionsDoneWithoutDocuments() {
		return correctionsDoneWithoutDocuments;
	}

	public void setCorrectionsDoneWithoutDocuments(boolean correctionsDoneWithoutdocuments) {
		this.correctionsDoneWithoutDocuments = correctionsDoneWithoutdocuments;
	}

	public boolean hasRevisions() {
		return revisions;
	}
	
	public void setRevisions(boolean revisions) {
		this.revisions = revisions;
	}
	
	public Integer getNumOfRevisionLoop() {
		return lastRevision == null ? null : lastRevision.getRevisionLoop();
	}
	
	public TaskRevision getLastRevision() {
		return lastRevision;
	}

	public void setLastRevision(TaskRevision lastRevision) {
		this.lastRevision = lastRevision;
	}

	public String getCoachFullName() {
		return coachFullName;
	}
	
	public Long getCoachKey() {
		return coachKey;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}
	
	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public DownloadLink getDownloadTaskFileLink() {
		return downloadTaskFileLink;
	}

	public void setDownloadTaskFileLink(DownloadLink downloadTaskFileLink) {
		this.downloadTaskFileLink = downloadTaskFileLink;
	}

	public FormLink getOpenTaskFileLink() {
		return openTaskFileLink;
	}

	public void setOpenTaskFileLink(FormLink openTaskFileLink) {
		this.openTaskFileLink = openTaskFileLink;
	}
	
	public FormLink getBackToSubmissionLink() {
		return backToSubmissionLink;
	}
	
	public String getBackToSubmissionLinkName() {
		return backToSubmissionLink == null ? null : backToSubmissionLink.getComponent().getComponentName();
	}

	public void setBackToSubmissionLink(FormLink backToSubmissionLink) {
		this.backToSubmissionLink = backToSubmissionLink;
	}
	
	public FormLink getCollectDocumentsLink() {
		return collectDocumentsLink;
	}
	
	public String getCollectDocumentsLinkName() {
		return collectDocumentsLink == null ? null : collectDocumentsLink.getComponent().getComponentName();
	}

	public void setCollectDocumentsLink(FormLink collectDocumentsLink) {
		this.collectDocumentsLink = collectDocumentsLink;
	}

	public Controller getDetailsCtrl() {
		return detailsCtrl;
	}

	public void setDetailsCtrl(Controller detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}

	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl instanceof FormBasicController formCtrl) {
			return formCtrl.getInitialFormItem().isVisible();
		}
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialComponent().isVisible();
		}
		return controllerDetailsEl != null && controllerDetailsEl.getComponent().isVisible();
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl instanceof FormBasicController formCtrl) {
			return formCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialComponent().getComponentName();
		}
		return controllerDetailsEl == null ? null : controllerDetailsEl.getComponent().getComponentName();
	}

	public FormItem getDetailsControllerEl() {
		return controllerDetailsEl;
	}

	public void setDetailsControllerEl(FormItem controllerDetailsEl) {
		this.controllerDetailsEl = controllerDetailsEl;
	}
}

