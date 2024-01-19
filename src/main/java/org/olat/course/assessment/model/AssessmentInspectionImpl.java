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
package org.olat.course.assessment.model;

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
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.modules.assessment.Role;

/**
 * 
 * Initial date: 20 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="courseassessmentinspection")
@Table(name="o_as_inspection")
public class AssessmentInspectionImpl implements AssessmentInspection, Persistable {
	
	private static final long serialVersionUID = -2726358558734785110L;

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
    @Column(name="a_from", nullable=false, insertable=true, updatable=true)
	private Date fromDate;
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name="a_to", nullable=false, insertable=true, updatable=true)
	private Date toDate;
	
    @Column(name="a_subident", nullable=true, insertable=true, updatable=false)
	private String subIdent;
	
	@Column(name="a_extra_time", nullable=false, insertable=true, updatable=true)
	private Integer extraTime;
	@Column(name="a_access_code", nullable=false, insertable=true, updatable=true)
	private String accessCode;
	@Column(name="a_effective_duration", nullable=false, insertable=true, updatable=true)
	private Long effectiveDuration;
	@Column(name="a_comment", nullable=false, insertable=true, updatable=true)
	private String comment;
	
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name="a_start_time", nullable=false, insertable=true, updatable=true)
	private Date startTime;
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name="a_end_time", nullable=false, insertable=true, updatable=true)
	private Date endTime;
	@Enumerated(EnumType.STRING)
    @Column(name="a_end_by", nullable=false, insertable=true, updatable=true)
	private Role endBy;

	@Enumerated(EnumType.STRING)
	@Column(name="a_status", nullable=false, insertable=true, updatable=true)
	private AssessmentInspectionStatusEnum inspectionStatus;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, updatable=false)
	private Identity identity;
	
	@ManyToOne(targetEntity=AssessmentInspectionConfigurationImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_configuration", nullable=false, updatable=true)
	private AssessmentInspectionConfiguration configuration;

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
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public Date getFromDate() {
		return fromDate;
	}

	@Override
	public void setFromDate(Date date) {
		this.fromDate = date;
	}

	@Override
	public Date getToDate() {
		return toDate;
	}

	@Override
	public void setToDate(Date date) {
		this.toDate = date;
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
	public String getAccessCode() {
		return accessCode;
	}

	@Override
	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	@Override
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date time) {
		this.startTime = time;
	}

	@Override
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date time) {
		this.endTime = time;
	}

	@Override
	public Role getEndBy() {
		return endBy;
	}

	public void setEndBy(Role endBy) {
		this.endBy = endBy;
	}

	@Override
	public AssessmentInspectionStatusEnum getInspectionStatus() {
		return inspectionStatus;
	}

	@Override
	public void setInspectionStatus(AssessmentInspectionStatusEnum inspectionStatus) {
		this.inspectionStatus = inspectionStatus;
	}

	@Override
	public Long getEffectiveDuration() {
		return effectiveDuration;
	}

	@Override
	public void setEffectiveDuration(Long effectiveDuration) {
		this.effectiveDuration = effectiveDuration;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public AssessmentInspectionConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(AssessmentInspectionConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -890287 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AssessmentInspectionImpl inspection) {
			return getKey() != null && getKey().equals(inspection.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
