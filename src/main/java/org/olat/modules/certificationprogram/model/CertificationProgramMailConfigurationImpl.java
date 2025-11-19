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
package org.olat.modules.certificationprogram.model;

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
import jakarta.persistence.Transient;

import org.olat.core.id.Persistable;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailConfigurationStatus;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationType;

/**
 * 
 * Initial date: 11 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="certificationprogrammailconfiguration")
@Table(name="o_cer_program_mail_config")
public class CertificationProgramMailConfigurationImpl implements CertificationProgramMailConfiguration, Persistable {

	private static final long serialVersionUID = -8780833614122463461L;

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
	
	@Enumerated(EnumType.STRING)
	@Column(name="c_type", nullable=false, insertable=true, updatable=false)
	private CertificationProgramMailType type;
	@Enumerated(EnumType.STRING)
	@Column(name="c_status", nullable=false, insertable=true, updatable=true)
	private CertificationProgramMailConfigurationStatus status;

	@Column(name="c_title", nullable=true, insertable=true, updatable=true)
	private String title;
	
	@Column(name="c_time", nullable=true, insertable=true, updatable=true)
	private int time;
	@Enumerated(EnumType.STRING)
	@Column(name="c_time_unit", nullable=true, insertable=true, updatable=true)
	private DurationType timeUnit;

	@Column(name="c_balance_too_low", nullable=false, insertable=true, updatable=true)
	private boolean creditBalanceTooLow;
	
	@Column(name="c_i18n_suffix", nullable=false, insertable=true, updatable=true)
	private String i18nSuffix;
	@Column(name="c_i18n_customized", nullable=false, insertable=true, updatable=true)
	private boolean customized;
	
	@ManyToOne(targetEntity=CertificationProgramImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_program", nullable=false, insertable=true, updatable=false)
	private CertificationProgram certificationProgram;
	
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
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public CertificationProgramMailType getType() {
		return type;
	}

	public void setType(CertificationProgramMailType type) {
		this.type = type;
	}

	@Override
	public CertificationProgramMailConfigurationStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(CertificationProgramMailConfigurationStatus status) {
		this.status = status;
	}

	@Override
	public int getTime() {
		return time;
	}

	@Override
	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public DurationType getTimeUnit() {
		return timeUnit;
	}

	@Override
	public void setTimeUnit(DurationType timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	@Override
	@Transient
	public Duration getTimeDuration() {
		if(getTimeUnit() != null && getTime() > 0) {
			return new Duration(getTime(), getTimeUnit());
		}
		return null;
	}

	@Override
	public boolean isCreditBalanceTooLow() {
		return creditBalanceTooLow;
	}

	@Override
	public void setCreditBalanceTooLow(boolean creditBalanceTooLow) {
		this.creditBalanceTooLow = creditBalanceTooLow;
	}

	@Override
	public String getI18nSuffix() {
		return i18nSuffix;
	}

	public void setI18nSuffix(String i18nSuffix) {
		this.i18nSuffix = i18nSuffix;
	}

	@Override
	public boolean isCustomized() {
		return customized;
	}

	@Override
	public void setCustomized(boolean customized) {
		this.customized = customized;
	}

	@Override
	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

	public void setCertificationProgram(CertificationProgram certificationProgram) {
		this.certificationProgram = certificationProgram;
	}

	@Override
	public int hashCode() {
		return key == null ? -2467896 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CertificationProgramMailConfigurationImpl config) {
			return getKey() != null && getKey().equals(config.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("CertificationProgramMailConfiguration[id=").append(getKey() == null ? "NULL" : getKey().toString())
		  .append("]");
		return sb.toString();
	}
}
