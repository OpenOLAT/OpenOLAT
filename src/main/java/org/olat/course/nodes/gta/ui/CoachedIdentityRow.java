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
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.user.UserPropertiesRow;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachedIdentityRow implements CoachedElementRow {

	private final TaskLight task;
	private final TaskDefinition taskDefinition;
	private final Date submissionDueDate;
	private final Date syntheticSubmissionDate;
	private final boolean hasSubmittedDocuments;
	private final UserPropertiesRow identity;
	private final FormLink markLink;
	private final Boolean userVisibility;
	private final BigDecimal score;
	private final Boolean passed;
	private final int numOfSubmissionDocs;
	private final int numOfCollectedDocs;
	
	private DownloadLink downloadTaskFileLink;
	
	public CoachedIdentityRow(UserPropertiesRow identity, TaskLight task, TaskDefinition taskDefinition, Date submissionDueDate,
			Date syntheticSubmissionDate, boolean hasSubmittedDocuments, FormLink markLink, Boolean userVisibility,
			BigDecimal score, Boolean passed, int numOfSubmissionDocs, int numOfCollectedDocs) {
		this.identity = identity;
		this.task = task;
		this.taskDefinition = taskDefinition;
		this.submissionDueDate = submissionDueDate;
		this.hasSubmittedDocuments = hasSubmittedDocuments;
		this.syntheticSubmissionDate = syntheticSubmissionDate;
		this.markLink = markLink;
		this.userVisibility = userVisibility;
		this.score = score;
		this.passed = passed;
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
	public Date getSubmissionDueDate() {
		return submissionDueDate;
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

	public UserPropertiesRow getIdentity() {
		return identity;
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public Boolean getUserVisibility() {
		return userVisibility;
	}

	public BigDecimal getScore() {
		return score;
	}

	public Boolean getPassed() {
		return passed;
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
