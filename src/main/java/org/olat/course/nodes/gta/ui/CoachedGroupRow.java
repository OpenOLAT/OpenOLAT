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

import java.util.Date;

import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.group.BusinessGroup;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachedGroupRow implements CoachedElementRow {
	
	private final TaskLight task;
	private final Date submissionDueDate;
	private final Date syntheticSubmissionDate;
	private final boolean hasSubmittedDocuments;
	private final BusinessGroup businessGroup;
	
	public CoachedGroupRow(BusinessGroup businessGroup, TaskLight task, Date submissionDueDate,
			Date syntheticSubmissionDate, boolean hasSubmittedDocuments) {
		this.task = task;
		this.businessGroup = businessGroup;
		this.submissionDueDate = submissionDueDate;
		this.hasSubmittedDocuments = hasSubmittedDocuments;
		this.syntheticSubmissionDate = syntheticSubmissionDate;
	}
	
	public String getName() {
		return businessGroup.getName();
	}

	@Override
	public String getTaskName() {
		return task == null ? null : task.getTaskName();
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
	
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}
}
