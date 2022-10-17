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
package org.olat.course.nodes.gta.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

@Entity(name="gtatasklight")
@Table(name="o_gta_task")
public class TaskLightImpl implements TaskLight, CreateInfo, Persistable, ModifiedInfo {

	private static final long serialVersionUID = 4202873369981813454L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=false, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=false, updatable=false)
	private Date lastModified;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_assignment_date", nullable=true, insertable=true, updatable=true)
	private Date assignmentDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_submission_date", nullable=true, insertable=true, updatable=true)
	private Date submissionDate;
	@Column(name="g_submission_ndocs", nullable=true, insertable=true, updatable=true)
	private Integer submissionNumOfDocs;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_submission_revisions_date", nullable=true, insertable=true, updatable=true)
	private Date submissionRevisionsDate;
	@Column(name="g_submission_revisions_ndocs", nullable=true, insertable=true, updatable=true)
	private Integer submissionRevisionsNumOfDocs;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_collection_date", nullable=true, insertable=true, updatable=true)
	private Date collectionDate;
	@Column(name="g_collection_ndocs", nullable=true, insertable=true, updatable=true)
	private Integer collectionNumOfDocs;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_assignment_due_date", nullable=true, insertable=true, updatable=false)
	private Date assignmentDueDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_submission_due_date", nullable=true, insertable=true, updatable=false)
	private Date submissionDueDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_revisions_due_date", nullable=true, insertable=true, updatable=false)
	private Date revisionsDueDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_solution_due_date", nullable=true, insertable=true, updatable=false)
	private Date solutionDueDate;
	
	@Column(name="g_status", nullable=false, insertable=false, updatable=false)
	private String status;
	
	@Column(name="g_rev_loop", nullable=false, insertable=false, updatable=false)
	private int revisionLoop;
	
	@Column(name="g_taskname", nullable=true, insertable=false, updatable=false)
	private String taskName;

	@ManyToOne(targetEntity=TaskListImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_tasklist", nullable=false, insertable=true, updatable=false)
	private TaskList taskList;
	
	@Column(name="fk_identity", nullable=true, insertable=false, updatable=false)
	private Long identityKey;
	
	@Column(name="fk_businessgroup", nullable=true, insertable=false, updatable=false)
	private Long businessGroupKey;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date date) {
		this.creationDate = date;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	public TaskList getTaskList() {
		return taskList;
	}

	public void setTaskList(TaskList taskList) {
		this.taskList = taskList;
	}

	@Override
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public Long getBusinessGroupKey() {
		return businessGroupKey;
	}

	public void setBusinessGroupKey(Long businessGroupKey) {
		this.businessGroupKey = businessGroupKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public Date getAssignmentDate() {
		return assignmentDate;
	}

	public void setAssignmentDate(Date assignmentDate) {
		this.assignmentDate = assignmentDate;
	}
	
	@Override
	public Date getSubmissionDate() {
		return submissionDate;
	}

	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}
	
	@Override
	public Integer getSubmissionNumOfDocs() {
		return submissionNumOfDocs;
	}

	public void setSubmissionNumOfDocs(Integer submissionNumOfDocs) {
		this.submissionNumOfDocs = submissionNumOfDocs;
	}

	@Override
	public Date getSubmissionRevisionsDate() {
		return submissionRevisionsDate;
	}

	public void setSubmissionRevisionsDate(Date submissionRevisionsDate) {
		this.submissionRevisionsDate = submissionRevisionsDate;
	}
	
	@Override
	public Integer getSubmissionRevisionsNumOfDocs() {
		return submissionRevisionsNumOfDocs;
	}

	public void setSubmissionRevisionsNumOfDocs(Integer submissionRevisionsNumOfDocs) {
		this.submissionRevisionsNumOfDocs = submissionRevisionsNumOfDocs;
	}

	@Override
	public Date getCollectionDate() {
		return collectionDate;
	}

	public void setCollectionDate(Date collectionDate) {
		this.collectionDate = collectionDate;
	}
	
	@Override
	public Integer getCollectionNumOfDocs() {
		return collectionNumOfDocs;
	}

	public void setCollectionNumOfDocs(Integer collectionNumOfDocs) {
		this.collectionNumOfDocs = collectionNumOfDocs;
	}

	@Override
	public Date getAssignmentDueDate() {
		return assignmentDueDate;
	}

	public void setAssignmentDueDate(Date assignmentDueDate) {
		this.assignmentDueDate = assignmentDueDate;
	}

	@Override
	public Date getSubmissionDueDate() {
		return submissionDueDate;
	}

	public void setSubmissionDueDate(Date submissionDueDate) {
		this.submissionDueDate = submissionDueDate;
	}

	@Override
	public Date getRevisionsDueDate() {
		return revisionsDueDate;
	}

	public void setRevisionsDueDate(Date revisionsDueDate) {
		this.revisionsDueDate = revisionsDueDate;
	}

	@Override
	public Date getSolutionDueDate() {
		return solutionDueDate;
	}

	public void setSolutionDueDate(Date solutionDueDate) {
		this.solutionDueDate = solutionDueDate;
	}

	@Override
	public TaskProcess getTaskStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return TaskProcess.valueOf(status);
		}
		return null;
	}
	
	public void setTaskStatus(TaskProcess taskStatus) {
		if(taskStatus == null) {
			status = null;
		} else {
			status = taskStatus.name();
		}
	}
	
	@Override
	public int getRevisionLoop() {
		return revisionLoop;
	}

	public void setRevisionLoop(int revisionLoop) {
		this.revisionLoop = revisionLoop;
	}

	@Override
	public int hashCode() {
		return key == null ? 83544 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof TaskLightImpl) {
			TaskLightImpl task = (TaskLightImpl)obj;
			return key != null && key.equals(task.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}