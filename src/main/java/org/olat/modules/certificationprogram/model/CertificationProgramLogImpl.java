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

import java.time.LocalDateTime;

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

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramLog;
import org.olat.modules.certificationprogram.CertificationProgramLogAction;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;

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
	
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private LocalDateTime creationDate;

	@Enumerated(EnumType.STRING)
	@Column(name="c_action", nullable=true, insertable=true, updatable=true)
	private CertificationProgramLogAction action;
	
	@Column(name="c_before", nullable=true, insertable=true, updatable=false)
	private String before;
	@Column(name="c_before_status", nullable=true, insertable=true, updatable=false)
	private String beforeStatus;
	@Column(name="c_after", nullable=true, insertable=true, updatable=false)
	private String after;
	@Column(name="c_after_status", nullable=true, insertable=true, updatable=false)
	private String afterStatus;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_doer", nullable=false, insertable=true, updatable=false)
	private Identity doer;
	
	@ManyToOne(targetEntity=CertificationProgramImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_program", nullable=true, insertable=true, updatable=true)
	private CertificationProgram certificationProgram;
	
	// Context
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=CurriculumElementImpl.class,fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_element", nullable=true, insertable=true, updatable=false)
	private CurriculumElement curriculumElement;
	@ManyToOne(targetEntity=CertificateImpl.class,fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_certificate", nullable=true, insertable=true, updatable=false)
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

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public CertificationProgramLogAction getAction() {
		return action;
	}

	public void setAction(CertificationProgramLogAction action) {
		this.action = action;
	}

	@Override
	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}
	
	@Override
	public String getBeforeStatus() {
		return beforeStatus;
	}

	public void setBeforeStatus(String beforeStatus) {
		this.beforeStatus = beforeStatus;
	}

	@Override
	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}
	
	@Override
	public String getAfterStatus() {
		return afterStatus;
	}

	public void setAfterStatus(String afterStatus) {
		this.afterStatus = afterStatus;
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
	public Identity getDoer() {
		return doer;
	}

	public void setDoer(Identity doer) {
		this.doer = doer;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

	public void setCertificationProgram(CertificationProgram certificationProgram) {
		this.certificationProgram = certificationProgram;
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
