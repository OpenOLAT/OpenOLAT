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
package org.olat.modules.dcompensation.model;

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

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="dcompensation")
@Table(name="o_as_compensation")
public class DisadvantageCompensationImpl implements DisadvantageCompensation, Persistable {

	private static final long serialVersionUID = -6202811126579600583L;

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
	
	@Column(name="a_subident", nullable=false, insertable=true, updatable=true)
	private String subIdent;
	@Column(name="a_subident_name", nullable=false, insertable=true, updatable=true)
	private String subIdentName;
	
	@Column(name="a_extra_time", nullable=false, insertable=true, updatable=true)
	private Integer extraTime;
	@Column(name="a_approved_by", nullable=true, insertable=true, updatable=true)
	private String approvedBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="a_approval", nullable=false, insertable=true, updatable=true)
	private Date approval;
	@Column(name="a_status", nullable=false, insertable=true, updatable=true)
	private String status;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
    private Identity identity;

	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_creator", nullable=false, insertable=true, updatable=false)
    private Identity creator;
	
	@ManyToOne(targetEntity=RepositoryEntry.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false)
    private RepositoryEntry entry;
	
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
	public Integer getExtraTime() {
		return extraTime;
	}

	@Override
	public void setExtraTime(Integer extraTime) {
		this.extraTime = extraTime;
	}

	@Override
	public String getApprovedBy() {
		return approvedBy;
	}

	@Override
	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	@Override
	public Date getApproval() {
		return approval;
	}

	@Override
	public void setApproval(Date approval) {
		this.approval = approval;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public DisadvantageCompensationStatusEnum getStatusEnum() {
		return DisadvantageCompensationStatusEnum.secureValueOf(status);
	}

	@Override
	public void setStatusEnum(DisadvantageCompensationStatusEnum status) {
		this.status = (status == null ? null : status.name());
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	@Override
	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public String getSubIdentName() {
		return subIdentName;
	}

	@Override
	public void setSubIdentName(String subIdentName) {
		this.subIdentName = subIdentName;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public int hashCode() {
		return key == null ? -864687 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DisadvantageCompensationImpl) {
			DisadvantageCompensationImpl compensation = (DisadvantageCompensationImpl)obj;
			return getKey() != null && getKey().equals(compensation.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
