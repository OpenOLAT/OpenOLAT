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
package org.olat.course.certificate.restapi;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateManagedFlag;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.user.restapi.UserVO;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

/**
 * 
 * Initial date: 13 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "certificateVO")
public class CertificateVO {
	
	private Long key;
	private Date creationDate;
	private Long identityKey;
	private String courseTitle;
	private Long courseResourceKey;
	private Long certificationProgramKey;
	private Date nextCertificationDate;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The count is increment automatically")
	private Long recertificationCount;
	@Schema(accessMode = AccessMode.READ_WRITE, description = "If recertification is enabled, and field null, it will recalculated from the settings of the program")
	private Date recertificationWindowDate;
	private String uuid;
	private String externalId;
	private String managedFlags;
	
	private UserVO user;
	
	public CertificateVO() {
		// make JAX-RS happy
	}
	
	public static CertificateVO valueOf(Certificate certificate, UserVO user) {
		CertificateVO vo = new CertificateVO();
		vo.setKey(certificate.getKey());
		vo.setCreationDate(certificate.getCreationDate());
		vo.setNextCertificationDate(certificate.getNextRecertificationDate());
		vo.setIdentityKey(certificate.getIdentity().getKey());
		vo.setCourseTitle(certificate.getCourseTitle());
		vo.setCourseResourceKey(certificate.getArchivedResourceKey());
		if(certificate instanceof CertificateImpl impl) {
			vo.setRecertificationCount(impl.getRecertificationCount());
			vo.setRecertificationWindowDate(impl.getRecertificationWindowDate());
			if(impl.getCertificationProgram() != null) {
				vo.setCertificationProgramKey(impl.getCertificationProgram().getKey());
			}
		}
		vo.setUuid(certificate.getUuid());
		vo.setExternalId(certificate.getExternalId());
		vo.setManagedFlags(CertificateManagedFlag.toString(certificate.getManagedFlags()));
		vo.setUser(user);
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getNextCertificationDate() {
		return nextCertificationDate;
	}

	public void setNextCertificationDate(Date nextCertificationDate) {
		this.nextCertificationDate = nextCertificationDate;
	}

	public Long getRecertificationCount() {
		return recertificationCount;
	}

	public void setRecertificationCount(Long recertificationCount) {
		this.recertificationCount = recertificationCount;
	}

	public Date getRecertificationWindowDate() {
		return recertificationWindowDate;
	}

	public void setRecertificationWindowDate(Date recertificationWindowDate) {
		this.recertificationWindowDate = recertificationWindowDate;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getCourseResourceKey() {
		return courseResourceKey;
	}

	public void setCourseResourceKey(Long courseResourceKey) {
		this.courseResourceKey = courseResourceKey;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public Long getCertificationProgramKey() {
		return certificationProgramKey;
	}

	public void setCertificationProgramKey(Long certificationProgramKey) {
		this.certificationProgramKey = certificationProgramKey;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}
	
	public UserVO getUser() {
		return user;
	}

	public void setUser(UserVO user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 236489 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CertificateVO cert) {
			return getKey() != null && getKey().equals(cert.getKey());
		}
		return false;
	}
}
