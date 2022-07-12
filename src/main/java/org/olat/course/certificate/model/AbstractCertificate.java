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

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateManagedFlag;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.EmailStatus;

/**
 * 
 * Initial date: 19.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@MappedSuperclass
public abstract class AbstractCertificate implements Certificate, Persistable {

	private static final long serialVersionUID = 2614314930775241116L;

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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="c_status", nullable=false, insertable=true, updatable=true)
	private String statusString;
	@Column(name="c_email_status", nullable=true, insertable=true, updatable=true)
	private String emailStatusString;
	
	@Column(name="c_uuid", nullable=false, insertable=true, updatable=false)
	private String uuid;
	
	@Column(name="c_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="c_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@Column(name="c_next_recertification", nullable=true, insertable=true, updatable=true)
	private Date nextRecertificationDate;

	@Column(name="c_path", nullable=true, insertable=true, updatable=true)
	private String path;
	@Column(name="c_last", nullable=false, insertable=true, updatable=true)
	private boolean last;
	@Column(name="c_course_title", nullable=true, insertable=true, updatable=false)
	private String courseTitle;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	
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

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public String getEmailStatusString() {
		return emailStatusString;
	}

	public void setEmailStatusString(String emailStatusString) {
		this.emailStatusString = emailStatusString;
	}
	
	public EmailStatus getEmailStatus() {
		return emailStatusString == null ? null : EmailStatus.valueOf(emailStatusString);
	}
	
	public void setEmailStatus(EmailStatus emailStatus) {
		if(emailStatus == null) {
			emailStatusString = null;
		} else {
			emailStatusString = emailStatus.name();
		}
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	@Override
	public CertificateStatus getStatus() {
		return CertificateStatus.valueOf(statusString);
	}

	public void setStatus(CertificateStatus status) {
		this.statusString = status.name();
	}
	
	@Override
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public CertificateManagedFlag[] getManagedFlags() {
		return CertificateManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public Date getNextRecertificationDate() {
		return nextRecertificationDate;
	}

	@Override
	public void setNextRecertificationDate(Date nextRecertificationDate) {
		this.nextRecertificationDate = nextRecertificationDate;
	}

	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public int hashCode() {
		return key == null ? -23984 : key.hashCode();
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
