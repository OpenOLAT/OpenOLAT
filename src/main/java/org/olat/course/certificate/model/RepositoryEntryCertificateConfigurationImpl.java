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
package org.olat.course.certificate.model;

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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.core.id.Persistable;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificationTimeUnit;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * 
 * Initial date: 5 avr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="certificateentryconfig")
@Table(name="o_cer_entry_config")
@NamedQuery(name="enabledCertification", query="select config.key from certificateentryconfig config where config.entry.key=:entryKey and (config.automaticCertificationEnabled = true or config.manualCertificationEnabled = true)")
@NamedQuery(name="enabledAutomaticCertification", query="select config.key from certificateentryconfig config where config.entry.key=:entryKey and config.automaticCertificationEnabled = true")
public class RepositoryEntryCertificateConfigurationImpl implements Persistable, RepositoryEntryCertificateConfiguration {
	
	private static final long serialVersionUID = 7224394983939663682L;

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
	
	@Column(name="c_cer_auto_enabled", nullable=true, insertable=true, updatable=true)
	private boolean automaticCertificationEnabled;
	@Column(name="c_cer_manual_enabled", nullable=true, insertable=true, updatable=true)
	private boolean manualCertificationEnabled;
	
	@Column(name="c_cer_custom_1", nullable=true, insertable=true, updatable=true)
	private String certificateCustom1;
	@Column(name="c_cer_custom_2", nullable=true, insertable=true, updatable=true)
	private String certificateCustom2;
	@Column(name="c_cer_custom_3", nullable=true, insertable=true, updatable=true)
	private String certificateCustom3;

	@Column(name="c_validity_enabled", nullable=true, insertable=true, updatable=true)
	private boolean validityEnabled;
	@Column(name="c_validity_timelapse", nullable=true, insertable=true, updatable=true)
	private int validityTimelapse;
	@Enumerated(EnumType.STRING)
	@Column(name="c_validity_timelapse_unit", nullable=true, insertable=true, updatable=true)
	private CertificationTimeUnit validityTimelapseUnit;
	
	@Column(name="c_recer_enabled", nullable=true, insertable=true, updatable=true)
	private boolean recertificationEnabled;
	@Column(name="c_recer_leadtime_enabled", nullable=true, insertable=true, updatable=true)
	private boolean recertificationLeadTimeEnabled;
	@Column(name="c_recer_leadtime_days", nullable=true, insertable=true, updatable=true)
	private int recertificationLeadTimeInDays;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false, unique=true)
	private RepositoryEntry entry;
	
	@ManyToOne(targetEntity=CertificateTemplateImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_template", nullable=true, insertable=true, updatable=true, unique=false)
	private CertificateTemplate template;
	
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
	@Transient
	public boolean isCertificateEnabled() {
		return isAutomaticCertificationEnabled() || isManualCertificationEnabled();
	}

	@Override
	public boolean isAutomaticCertificationEnabled() {
		return automaticCertificationEnabled;
	}

	@Override
	public void setAutomaticCertificationEnabled(boolean automaticCertificationEnabled) {
		this.automaticCertificationEnabled = automaticCertificationEnabled;
	}

	@Override
	public boolean isManualCertificationEnabled() {
		return manualCertificationEnabled;
	}

	@Override
	public void setManualCertificationEnabled(boolean manualCertificationEnabled) {
		this.manualCertificationEnabled = manualCertificationEnabled;
	}

	@Override
	public String getCertificateCustom1() {
		return certificateCustom1;
	}

	@Override
	public void setCertificateCustom1(String certificateCustom1) {
		this.certificateCustom1 = certificateCustom1;
	}

	@Override
	public String getCertificateCustom2() {
		return certificateCustom2;
	}

	@Override
	public void setCertificateCustom2(String certificateCustom2) {
		this.certificateCustom2 = certificateCustom2;
	}

	@Override
	public String getCertificateCustom3() {
		return certificateCustom3;
	}

	@Override
	public void setCertificateCustom3(String certificateCustom3) {
		this.certificateCustom3 = certificateCustom3;
	}

	@Override
	public boolean isValidityEnabled() {
		return validityEnabled;
	}

	@Override
	public void setValidityEnabled(boolean enabled) {
		this.validityEnabled = enabled;
	}

	@Override
	public int getValidityTimelapse() {
		return validityTimelapse;
	}

	@Override
	public void setValidityTimelapse(int timelapse) {
		this.validityTimelapse = timelapse;
	}

	@Override
	public CertificationTimeUnit getValidityTimelapseUnit() {
		return validityTimelapseUnit;
	}

	@Override
	public void setValidityTimelapseUnit(CertificationTimeUnit unit) {
		this.validityTimelapseUnit = unit;
	}
	
	@Override
	public boolean isRecertificationEnabled() {
		return recertificationEnabled;
	}

	@Override
	public void setRecertificationEnabled(boolean recertificationEnabled) {
		this.recertificationEnabled = recertificationEnabled;
	}

	@Override
	public boolean isRecertificationLeadTimeEnabled() {
		return recertificationLeadTimeEnabled;
	}

	@Override
	public void setRecertificationLeadTimeEnabled(boolean recertificationLeadTimeEnabled) {
		this.recertificationLeadTimeEnabled = recertificationLeadTimeEnabled;
	}

	@Override
	public int getRecertificationLeadTimeInDays() {
		return recertificationLeadTimeInDays;
	}

	@Override
	public void setRecertificationLeadTimeInDays(int recertificationLeadTimeInDays) {
		this.recertificationLeadTimeInDays = recertificationLeadTimeInDays;
	}

	@Override
	public CertificateTemplate getTemplate() {
		return template;
	}

	@Override
	public void setTemplate(CertificateTemplate template) {
		this.template = template;
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
		return getKey() == null ? 7894857 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RepositoryEntryCertificateConfigurationImpl config) {
			return getKey() != null && getKey().equals(config.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
