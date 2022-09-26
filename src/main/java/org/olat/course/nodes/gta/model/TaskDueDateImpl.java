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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.TaskDueDate;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.modules.assessment.Role;

/**
 * 
 * Only to update the due dates.
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gtataskduedate")
@Table(name="o_gta_task")
@NamedQuery(name="dueDateTaskByTask", query="select duedates from gtataskduedate duedates where duedates.key=:taskKey")
public class TaskDueDateImpl implements TaskDueDate, CreateInfo, Persistable, ModifiedInfo {

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
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_assignment_date", nullable=true, insertable=true, updatable=false)
	private Date assignmentDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_submission_date", nullable=true, insertable=true, updatable=false)
	private Date submissionDate;
	@Column(name="g_submission_drole", nullable=true, insertable=true, updatable=true)
	private Role submissionDoerRole;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_submission_revisions_date", nullable=true, insertable=true, updatable=false)
	private Date submissionRevisionsDate;
	@Column(name="g_submission_revisions_drole", nullable=true, insertable=true, updatable=true)
	private Role submissionRevisionsDoerRole;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_collection_date", nullable=true, insertable=true, updatable=false)
	private Date collectionDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_collection_revisions_date", nullable=true, insertable=true, updatable=true)
	private Date collectionRevisionsDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_allow_reset_date", nullable=true, insertable=true, updatable=true)
	private Date allowResetDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_acceptation_date", nullable=true, insertable=true, updatable=false)
	private Date acceptationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_solution_date", nullable=true, insertable=true, updatable=false)
	private Date solutionDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_graduation_date", nullable=true, insertable=true, updatable=false)
	private Date graduationDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_assignment_due_date", nullable=true, insertable=true, updatable=true)
	private Date assignmentDueDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_submission_due_date", nullable=true, insertable=true, updatable=true)
	private Date submissionDueDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_revisions_due_date", nullable=true, insertable=true, updatable=true)
	private Date revisionsDueDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_solution_due_date", nullable=true, insertable=true, updatable=true)
	private Date solutionDueDate;
	
	@Column(name="g_status", nullable=false, insertable=true, updatable=false)
	private String status;
	
	@Column(name="g_rev_loop", nullable=false, insertable=true, updatable=false)
	private int revisionLoop;
	
	@Column(name="g_taskname", nullable=true, insertable=true, updatable=false)
	private String taskName;

	@ManyToOne(targetEntity=TaskListImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_tasklist", nullable=false, insertable=true, updatable=false)
	private TaskList taskList;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Identity identity;
	
	@ManyToOne(targetEntity=BusinessGroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_businessgroup", nullable=true, insertable=true, updatable=false)
	private BusinessGroup businessGroup;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_allow_reset_identity", nullable=true, insertable=true, updatable=false)
	private Identity allowResetIdentity;
	
	public TaskDueDateImpl() {
		//
	}
	
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

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public void setBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}

	@Override
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
	public Role getSubmissionDoerRole() {
		return submissionDoerRole;
	}

	public void setSubmissionDoerRole(Role submissionDoerRole) {
		this.submissionDoerRole = submissionDoerRole;
	}

	@Override
	public Date getSubmissionRevisionsDate() {
		return submissionRevisionsDate;
	}

	public void setSubmissionRevisionsDate(Date submissionRevisionsDate) {
		this.submissionRevisionsDate = submissionRevisionsDate;
	}

	@Override
	public Role getSubmissionRevisionsDoerRole() {
		return submissionRevisionsDoerRole;
	}

	public void setSubmissionRevisionsDoerRole(Role submissionRevisionsDoerRole) {
		this.submissionRevisionsDoerRole = submissionRevisionsDoerRole;
	}

	@Override
	public Date getCollectionDate() {
		return collectionDate;
	}

	public void setCollectionDate(Date collectionDate) {
		this.collectionDate = collectionDate;
	}

	@Override
	public Date getCollectionRevisionsDate() {
		return collectionRevisionsDate;
	}

	public void setCollectionRevisionsDate(Date collectionRevisionsDate) {
		this.collectionRevisionsDate = collectionRevisionsDate;
	}

	@Override
	public Date getAllowResetDate() {
		return allowResetDate;
	}

	public void setAllowResetDate(Date allowResetDate) {
		this.allowResetDate = allowResetDate;
	}

	@Override
	public Identity getAllowResetIdentity() {
		return allowResetIdentity;
	}

	public void setAllowResetIdentity(Identity allowResetIdentity) {
		this.allowResetIdentity = allowResetIdentity;
	}

	@Override
	public Date getAcceptationDate() {
		return acceptationDate;
	}

	public void setAcceptationDate(Date acceptationDate) {
		this.acceptationDate = acceptationDate;
	}
	
	@Override
	public Date getSolutionDate() {
		return solutionDate;
	}

	public void setSolutionDate(Date solutionDate) {
		this.solutionDate = solutionDate;
	}

	@Override
	public Date getGraduationDate() {
		return graduationDate;
	}

	public void setGraduationDate(Date graduationDate) {
		this.graduationDate = graduationDate;
	}

	@Override
	public Date getAssignmentDueDate() {
		return assignmentDueDate;
	}

	@Override
	public void setAssignmentDueDate(Date assignmentDueDate) {
		this.assignmentDueDate = assignmentDueDate;
	}

	@Override
	public Date getSubmissionDueDate() {
		return submissionDueDate;
	}

	@Override
	public void setSubmissionDueDate(Date submissionDueDate) {
		this.submissionDueDate = submissionDueDate;
	}

	@Override
	public Date getRevisionsDueDate() {
		return revisionsDueDate;
	}

	@Override
	public void setRevisionsDueDate(Date revisionsDueDate) {
		this.revisionsDueDate = revisionsDueDate;
	}

	@Override
	public Date getSolutionDueDate() {
		return solutionDueDate;
	}

	@Override
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
	public TaskList getTaskList() {
		return taskList;
	}

	public void setTaskList(TaskList taskList) {
		this.taskList = taskList;
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
		if(obj instanceof TaskDueDateImpl) {
			TaskDueDateImpl task = (TaskDueDateImpl)obj;
			return key != null && key.equals(task.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}