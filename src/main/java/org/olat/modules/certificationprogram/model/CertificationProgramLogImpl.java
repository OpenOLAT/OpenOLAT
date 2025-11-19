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
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramLog;

/**
 * 
 * Initial date: 14 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="certificationprogramlog")
@Table(name="o_cer_program_log")
public class CertificationProgramLogImpl implements CertificationProgramLog, Persistable {
	
	private static final long serialVersionUID = 7801002083995196103L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@ManyToOne(targetEntity=CertificateImpl.class,fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_certificate", nullable=false, insertable=true, updatable=false)
	private Certificate certificate;
	
	@ManyToOne(targetEntity=CertificationProgramMailConfigurationImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_mail_configuration", nullable=true, insertable=true, updatable=false)
	private CertificationProgramMailConfiguration mailConfiguration;

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
	public Certificate getCertificate() {
		return certificate;
	}

	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}

	@Override
	public CertificationProgramMailConfiguration getMailConfiguration() {
		return mailConfiguration;
	}

	public void setMailConfiguration(CertificationProgramMailConfiguration mailConfiguration) {
		this.mailConfiguration = mailConfiguration;
	}

	@Override
	public int hashCode() {
		return key == null ? 4069065 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CertificationProgramLogImpl mailLog) {
			return getKey() != null && getKey().equals(mailLog.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
