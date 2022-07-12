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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateManagedFlag;

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
	private Long identityKey;
	private String courseTitle;
	private Long courseResourceKey;
	private String uuid;
	private String externalId;
	private String managedFlags;
	
	public CertificateVO() {
		// make JAX-RS happy
	}
	
	public static CertificateVO valueOf(CertificateLight certificate) {
		CertificateVO vo = new CertificateVO();
		vo.setKey(certificate.getKey());
		vo.setIdentityKey(certificate.getIdentityKey());
		vo.setCourseTitle(certificate.getCourseTitle());
		vo.setCourseResourceKey(certificate.getOlatResourceKey());
		vo.setUuid(certificate.getUuid());
		vo.setExternalId(certificate.getExternalId());
		vo.setManagedFlags(CertificateManagedFlag.toString(certificate.getManagedFlags()));
		return vo;
	}
	
	public static CertificateVO valueOf(Certificate certificate) {
		CertificateVO vo = new CertificateVO();
		vo.setKey(certificate.getKey());
		vo.setIdentityKey(certificate.getIdentity().getKey());
		vo.setCourseTitle(certificate.getCourseTitle());
		vo.setCourseResourceKey(certificate.getArchivedResourceKey());
		vo.setUuid(certificate.getUuid());
		vo.setExternalId(certificate.getExternalId());
		vo.setManagedFlags(CertificateManagedFlag.toString(certificate.getManagedFlags()));
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
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
}
