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
package org.olat.course.nodes.gta.ui;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.user.UserPropertiesRow;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachedIdentityRow extends UserPropertiesRow implements CoachedElementRow {

	private final TaskLight task;
	private final TaskDefinition taskDefinition;
	private final DueDate submissionDueDate;
	private final DueDate lateSubmissionDueDate;
	private final Date syntheticSubmissionDate;
	private final boolean hasSubmittedDocuments;
	private final FormLink markLink;
	private final AssessmentEntry assessmentEntry;

	private final int numOfSubmissionDocs;
	private final int numOfCollectedDocs;
	
	private DownloadLink downloadTaskFileLink;
	
	public CoachedIdentityRow(UserPropertiesRow identity, TaskLight task, TaskDefinition taskDefinition,
			DueDate submissionDueDate, DueDate lateSubmissionDueDate, Date syntheticSubmissionDate,
			boolean hasSubmittedDocuments, FormLink markLink, AssessmentEntry assessmentEntry,
			int numOfSubmissionDocs, int numOfCollectedDocs) {
		super(identity);
		this.task = task;
		this.taskDefinition = taskDefinition;
		this.submissionDueDate = submissionDueDate;
		this.lateSubmissionDueDate = lateSubmissionDueDate;
		this.hasSubmittedDocuments = hasSubmittedDocuments;
		this.syntheticSubmissionDate = syntheticSubmissionDate;
		this.markLink = markLink;
		this.assessmentEntry = assessmentEntry;
		this.numOfSubmissionDocs = numOfSubmissionDocs;
		this.numOfCollectedDocs = numOfCollectedDocs;
	}

	@Override
	public String getTaskName() {
		return task == null ? null : task.getTaskName();
	}
	
	@Override
	public String getTaskTitle() {
		return taskDefinition == null ? null : taskDefinition.getTitle();
	}

	@Override
	public TaskProcess getTaskStatus() {
		return task == null ? null : task.getTaskStatus();
	}

	@Override
	public Date getSubmissionDate() {
		return task == null ? null : task.getSubmissionDate();
	}

	@Override
	public DueDate getSubmissionDueDate() {
		return submissionDueDate;
	}

	@Override
	public DueDate getLateSubmissionDueDate() {
		return lateSubmissionDueDate;
	}

	@Override
	public Date getSubmissionRevisionsDate() {
		return task == null ? null : task.getSubmissionRevisionsDate();
	}

	@Override
	public Date getCollectionDate() {
		return task == null ? null : task.getCollectionDate();
	}

	@Override
	public Date getSyntheticSubmissionDate() {
		return syntheticSubmissionDate;
	}

	@Override
	public boolean getHasSubmittedDocuments() {
		return hasSubmittedDocuments;
	}
	
	@Override
	public TaskLight getTask() {
		return task;
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

	public int getNumOfSubmissionDocs() {
		return numOfSubmissionDocs;
	}
	
	public int getNumOfCollectedDocs() {
		return numOfCollectedDocs;
	}

	public DownloadLink getDownloadTaskFileLink() {
		return downloadTaskFileLink;
	}

	public void setDownloadTaskFileLink(DownloadLink downloadTaskFileLink) {
		this.downloadTaskFileLink = downloadTaskFileLink;
	}
}
