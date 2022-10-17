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
package org.olat.modules.grading.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gradingassignment")
@Table(name="o_grad_assignment")
public class GradingAssignmentImpl implements GradingAssignment, Persistable {

	private static final long serialVersionUID = -720453001309954166L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="g_status", nullable=false, insertable=true, updatable=true)
	private String status;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_assessment_date", nullable=true, insertable=true, updatable=true)
	private Date assessmentDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_assignment_date", nullable=true, insertable=true, updatable=true)
	private Date assignmentDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_assignment_notification", nullable=true, insertable=true, updatable=true)
	private Date assignmentNotificationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_reminder_1", nullable=true, insertable=true, updatable=true)
	private Date reminder1Date;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_reminder_2", nullable=true, insertable=true, updatable=true)
	private Date reminder2Date;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_deadline", nullable=true, insertable=true, updatable=true)
	private Date deadline;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_extended_deadline", nullable=true, insertable=true, updatable=true)
	private Date extendedDeadline;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_closed", nullable=true, insertable=true, updatable=true)
	private Date closingDate;
	
	@ManyToOne(targetEntity=GraderToIdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_grader", nullable=true, insertable=true, updatable=true)
	private GraderToIdentity grader;
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_reference_entry", nullable=false, insertable=true, updatable=false)
	private RepositoryEntry referenceEntry;
	@ManyToOne(targetEntity=AssessmentEntryImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_assessment_entry", nullable=false, insertable=true, updatable=false)
	private AssessmentEntry assessmentEntry;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public GradingAssignmentStatus getAssignmentStatus() {
		return status == null ? GradingAssignmentStatus.unassigned : GradingAssignmentStatus.valueOf(status);
	}
	
	@Override
	public void setAssignmentStatus(GradingAssignmentStatus assignmentStatus) {
		if(assignmentStatus == null) {
			status = GradingAssignmentStatus.unassigned.name();
		} else {
			status = assignmentStatus.name();
		}
	}

	@Override
	public Date getAssessmentDate() {
		return assessmentDate;
	}

	@Override
	public void setAssessmentDate(Date assessmentDate) {
		this.assessmentDate = assessmentDate;
	}

	@Override
	public Date getAssignmentDate() {
		return assignmentDate;
	}

	@Override
	public void setAssignmentDate(Date assignmentDate) {
		this.assignmentDate = assignmentDate;
	}

	@Override
	public Date getAssignmentNotificationDate() {
		return assignmentNotificationDate;
	}

	@Override
	public void setAssignmentNotificationDate(Date assignmentNotificationDate) {
		this.assignmentNotificationDate = assignmentNotificationDate;
	}

	@Override
	public Date getReminder1Date() {
		return reminder1Date;
	}

	@Override
	public void setReminder1Date(Date reminder1Date) {
		this.reminder1Date = reminder1Date;
	}

	@Override
	public Date getReminder2Date() {
		return reminder2Date;
	}

	@Override
	public void setReminder2Date(Date reminder2Date) {
		this.reminder2Date = reminder2Date;
	}

	@Override
	public Date getDeadline() {
		return deadline;
	}

	@Override
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	@Override
	public Date getExtendedDeadline() {
		return extendedDeadline;
	}

	@Override
	public void setExtendedDeadline(Date extendedDeadline) {
		this.extendedDeadline = extendedDeadline;
	}

	@Override
	public Date getClosingDate() {
		return closingDate;
	}

	@Override
	public void setClosingDate(Date closingDate) {
		this.closingDate = closingDate;
	}

	@Override
	public GraderToIdentity getGrader() {
		return grader;
	}

	@Override
	public void setGrader(GraderToIdentity grader) {
		this.grader = grader;
	}

	@Override
	public AssessmentEntry getAssessmentEntry() {
		return assessmentEntry;
	}

	public void setAssessmentEntry(AssessmentEntry assessmentEntry) {
		this.assessmentEntry = assessmentEntry;
	}

	@Override
	public RepositoryEntry getReferenceEntry() {
		return referenceEntry;
	}

	public void setReferenceEntry(RepositoryEntry referenceEntry) {
		this.referenceEntry = referenceEntry;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -71451756 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof GradingAssignmentImpl) {
			GradingAssignmentImpl assignment = (GradingAssignmentImpl)obj;
			return getKey() != null && getKey().equals(assignment.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
