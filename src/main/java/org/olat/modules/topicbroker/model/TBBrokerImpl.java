/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.topicbroker.model;

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
import org.olat.modules.topicbroker.TBBroker;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="topicbrokerbroker")
@Table(name="o_tb_broker")
public class TBBrokerImpl implements TBBroker, Persistable {
	
	private static final long serialVersionUID = 3835062005677591385L;
	
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
	
	@Column(name="t_max_selections", nullable=true, insertable=true, updatable=true)
	private Integer maxSelections;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_selection_start_date", nullable=true, insertable=true, updatable=true)
	private Date selectionStartDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_selection_end_date", nullable=true, insertable=true, updatable=true)
	private Date selectionEndDate;
	
	@Column(name="t_required_enrollments", nullable=true, insertable=true, updatable=true)
	private Integer requiredEnrollments;
	@Column(name="t_p_can_edit_r_enrollments", nullable=true, insertable=true, updatable=true)
	private boolean participantCanEditRequiredEnrollments;
	@Column(name="t_auto_enrollment", nullable=true, insertable=true, updatable=true)
	private boolean autoEnrollment;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_enrollment_start_date", nullable=true, insertable=true, updatable=true)
	private Date enrollmentStartDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_enrollment_done_date", nullable=true, insertable=true, updatable=true)
	private Date enrollmentDoneDate;
	
	@Column(name="t_p_can_withdraw", nullable=true, insertable=true, updatable=true)
	private boolean participantCanWithdraw;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_withdraw_end_date", nullable=true, insertable=true, updatable=true)
	private Date withdrawEndDate;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false)
	private RepositoryEntry repositoryEntry;
	@Column(name="t_subident", nullable=false, insertable=true, updatable=false)
	private String subIdent;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public String getResourceableTypeName() {
		return "tb-broker";
	}

	@Override
	public Long getResourceableId() {
		return key;
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
	public Integer getMaxSelections() {
		return maxSelections;
	}

	@Override
	public void setMaxSelections(Integer maxSelections) {
		this.maxSelections = maxSelections;
	}

	@Override
	public Date getSelectionStartDate() {
		return selectionStartDate;
	}

	@Override
	public void setSelectionStartDate(Date selectionStartDate) {
		this.selectionStartDate = selectionStartDate;
	}

	@Override
	public Date getSelectionEndDate() {
		return selectionEndDate;
	}

	@Override
	public void setSelectionEndDate(Date selectionEndDate) {
		this.selectionEndDate = selectionEndDate;
	}

	@Override
	public Integer getRequiredEnrollments() {
		return requiredEnrollments;
	}

	@Override
	public void setRequiredEnrollments(Integer requiredEnrollments) {
		this.requiredEnrollments = requiredEnrollments;
	}

	@Override
	public boolean isParticipantCanEditRequiredEnrollments() {
		return participantCanEditRequiredEnrollments;
	}

	@Override
	public void setParticipantCanEditRequiredEnrollments(boolean participantCanEditRequiredEnrollments) {
		this.participantCanEditRequiredEnrollments = participantCanEditRequiredEnrollments;
	}

	@Override
	public boolean isAutoEnrollment() {
		return autoEnrollment;
	}

	@Override
	public void setAutoEnrollment(boolean autoEnrollment) {
		this.autoEnrollment = autoEnrollment;
	}

	@Override
	public Date getEnrollmentStartDate() {
		return enrollmentStartDate;
	}

	public void setEnrollmentStartDate(Date enrollmentStartDate) {
		this.enrollmentStartDate = enrollmentStartDate;
	}

	@Override
	public Date getEnrollmentDoneDate() {
		return enrollmentDoneDate;
	}

	public void setEnrollmentDoneDate(Date enrollmentDoneDate) {
		this.enrollmentDoneDate = enrollmentDoneDate;
	}

	@Override
	public boolean isParticipantCanWithdraw() {
		return participantCanWithdraw;
	}

	@Override
	public void setParticipantCanWithdraw(boolean participantCanWithdraw) {
		this.participantCanWithdraw = participantCanWithdraw;
	}

	@Override
	public Date getWithdrawEndDate() {
		return withdrawEndDate;
	}

	@Override
	public void setWithdrawEndDate(Date withdrawEndDate) {
		this.withdrawEndDate = withdrawEndDate;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TBBrokerImpl other = (TBBrokerImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
