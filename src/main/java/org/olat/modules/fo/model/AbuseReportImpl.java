/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.modules.fo.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.fo.AbuseReport;
import org.olat.modules.fo.Message;

/**
 * Entity implementation for abuse reports on forum messages.
 * 
 * Initial date: January 2026
 * @author OpenOLAT community
 */
@Entity(name="foabusereport")
@Table(name="o_fo_abuse_report")
public class AbuseReportImpl implements AbuseReport, CreateInfo, Persistable {

	private static final long serialVersionUID = -8272614239273891723L;

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
	
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@ManyToOne(targetEntity=MessageImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="message_id", nullable=false, insertable=true, updatable=false)
	private Message message;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="reporter_id", nullable=false, insertable=true, updatable=false)
	private Identity reporter;
	
	@Column(name="reason", nullable=false, insertable=true, updatable=false)
	private String reason;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status", nullable=false, insertable=true, updatable=true)
	private AbuseReportStatus status = AbuseReportStatus.PENDING;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="resolution_date", nullable=true, insertable=true, updatable=true)
	private Date resolutionDate;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="resolved_by_id", nullable=true, insertable=true, updatable=true)
	private Identity resolvedBy;
	
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
	public Message getMessage() {
		return message;
	}
	
	@Override
	public void setMessage(Message message) {
		this.message = message;
	}
	
	@Override
	public Identity getReporter() {
		return reporter;
	}
	
	@Override
	public void setReporter(Identity reporter) {
		this.reporter = reporter;
	}
	
	@Override
	public String getReason() {
		return reason;
	}
	
	@Override
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	@Override
	public AbuseReportStatus getStatus() {
		return status;
	}
	
	@Override
	public void setStatus(AbuseReportStatus status) {
		this.status = status;
	}
	
	@Override
	public Date getResolutionDate() {
		return resolutionDate;
	}
	
	@Override
	public void setResolutionDate(Date resolutionDate) {
		this.resolutionDate = resolutionDate;
	}
	
	@Override
	public Identity getResolvedBy() {
		return resolvedBy;
	}
	
	@Override
	public void setResolvedBy(Identity resolvedBy) {
		this.resolvedBy = resolvedBy;
	}
	
	@Override
	public int hashCode() {
		return key == null ? 945821 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof AbuseReportImpl) {
			AbuseReportImpl other = (AbuseReportImpl) obj;
			return getKey() != null && getKey().equals(other.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
