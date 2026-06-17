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
package org.olat.modules.grading.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.grading.GradingAssignmentLog;
import org.olat.modules.grading.GradingAssignmentStatus;

/**
 * 
 * Initial date: 18 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="gradingassignmentlog")
@Table(name="o_grad_assignment_log")
public class GradingAssignmentLogImpl implements GradingAssignmentLog, Persistable {
	
	private static final long serialVersionUID = -8433337447620449952L;

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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_closed", nullable=true, insertable=true, updatable=true)
	private Date closingDate;
	@Enumerated(EnumType.STRING)
	@Column(name="g_status", nullable=false, insertable=true, updatable=true)
	private GradingAssignmentStatus status;

	@Column(name="g_deleted", nullable=false, insertable=true, updatable=true)
	private boolean deleted = false;
	
	@Column(name="g_reference_entry_id", nullable=false, insertable=true, updatable=false)
	private Long referenceEntryKey;
	@Column(name="g_reference_entry_displayname", nullable=true, insertable=true, updatable=false)
    private String referenceEntryDisplayName;
	@Column(name="g_reference_entry_external_ref", nullable=true, insertable=true, updatable=false)
    private String referenceEntryExternalRef;

	@Column(name="g_entry_id", nullable=false, insertable=true, updatable=false)
    private Long repositoryEntryKey;
	@Column(name="g_entry_displayname", nullable=true, insertable=true, updatable=false)
    private String repositoryEntryDisplayName;
	@Column(name="g_entry_external_ref", nullable=true, insertable=true, updatable=false)
    private String repositoryEntryExternalRef;
	
	@Column(name="g_time", nullable=false, insertable=true, updatable=false)
	private long time;
	@Column(name="g_metadata_time", nullable=false, insertable=true, updatable=true)
	private long metadataTime;
	
	@Column(name="g_assignment_id", nullable=false, insertable=true, updatable=false)
    private Long gradingAssignmentKey;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_grader", nullable=true, insertable=true, updatable=false)
	private Identity grader;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_assignee", nullable=true, insertable=true, updatable=false)
	private Identity assignee;
	
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

	@Override
	public Date getClosingDate() {
		return closingDate;
	}

	public void setClosingDate(Date closingDate) {
		this.closingDate = closingDate;
	}

	@Override
	public GradingAssignmentStatus getStatus() {
		return status;
	}

	public void setStatus(GradingAssignmentStatus status) {
		this.status = status;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Long getReferenceEntryKey() {
		return referenceEntryKey;
	}

	public void setReferenceEntryKey(Long referenceEntryKey) {
		this.referenceEntryKey = referenceEntryKey;
	}

	@Override
	public String getReferenceEntryDisplayName() {
		return referenceEntryDisplayName;
	}

	public void setReferenceEntryDisplayName(String referenceEntryDisplayName) {
		this.referenceEntryDisplayName = referenceEntryDisplayName;
	}

	public String getReferenceEntryExternalRef() {
		return referenceEntryExternalRef;
	}

	public void setReferenceEntryExternalRef(String referenceEntryExternalRef) {
		this.referenceEntryExternalRef = referenceEntryExternalRef;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public void setRepositoryEntryKey(Long repositoryEntryKey) {
		this.repositoryEntryKey = repositoryEntryKey;
	}

	public Long getGradingAssignmentKey() {
		return gradingAssignmentKey;
	}

	public void setGradingAssignmentKey(Long gradingAssignmentKey) {
		this.gradingAssignmentKey = gradingAssignmentKey;
	}

	@Override
	public String getRepositoryEntryDisplayName() {
		return repositoryEntryDisplayName;
	}

	public void setRepositoryEntryDisplayName(String repositoryEntryDisplayName) {
		this.repositoryEntryDisplayName = repositoryEntryDisplayName;
	}

	@Override
	public String getRepositoryEntryExternalRef() {
		return repositoryEntryExternalRef;
	}

	public void setRepositoryEntryExternalRef(String repositoryEntryExternalRef) {
		this.repositoryEntryExternalRef = repositoryEntryExternalRef;
	}

	@Override
	public Identity getGrader() {
		return grader;
	}

	public void setGrader(Identity grader) {
		this.grader = grader;
	}

	@Override
	public Identity getAssignee() {
		return assignee;
	}

	public void setAssignee(Identity assignee) {
		this.assignee = assignee;
	}

	@Override
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public long getMetadataTime() {
		return metadataTime;
	}

	public void setMetadataTime(long metadataTime) {
		this.metadataTime = metadataTime;
	}
	
	

	@Override
	public int hashCode() {
		return getKey() == null ? 29145756 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof GradingAssignmentLogImpl log) {
			return getKey() != null && getKey().equals(log.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
