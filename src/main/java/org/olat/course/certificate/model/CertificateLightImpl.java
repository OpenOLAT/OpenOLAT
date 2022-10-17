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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateManagedFlag;
import org.olat.course.certificate.CertificateStatus;

/**
 * 
 * Initial date: 21.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="certificatelight")
@Table(name="o_cer_certificate")
public class CertificateLightImpl implements CertificateLight, Persistable {

	private static final long serialVersionUID = 2360631986446191873L;

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
	
	@Column(name="c_status", nullable=false, insertable=true, updatable=true)
	private String statusString;
	
	@Column(name="c_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="c_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@Column(name="c_uuid", nullable=false, insertable=true, updatable=false)
	private String uuid;
	@Column(name="c_next_recertification", nullable=true, insertable=true, updatable=true)
	private Date nextRecertificationDate;
	@Column(name="c_last", nullable=false, insertable=true, updatable=true)
	private boolean last;
	@Column(name="c_course_title", nullable=true, insertable=true, updatable=false)
	private String courseTitle;
	@Column(name="c_archived_resource_id", nullable=false, insertable=true, updatable=false)
	private Long olatResourceKey;
	
	@Column(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Long identityKey;

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
	
	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
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
	public CertificateStatus getStatus() {
		return CertificateStatus.valueOf(statusString);
	}

	@Override
	public Date getNextRecertificationDate() {
		return nextRecertificationDate;
	}

	public void setNextRecertificationDate(Date nextRecertificationDate) {
		this.nextRecertificationDate = nextRecertificationDate;
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

	@Override
	public Long getOlatResourceKey() {
		return olatResourceKey;
	}

	public void setOlatResourceKey(Long olatResourceKey) {
		this.olatResourceKey = olatResourceKey;
	}

	@Override
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public int hashCode() {
		return key == null ? -23984 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CertificateLightImpl) {
			CertificateLightImpl prefs = (CertificateLightImpl)obj;
			return key != null && key.equals(prefs.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	

}
