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
package org.olat.modules.coach.ui.em;

import java.util.List;

import org.olat.course.certificate.model.CertificateIdentityConfig;

/**
 * Initial date: 2025-01-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CertificateRow {
	private Long certificateId;
	private String courseDisplayName;
	private String path;
	private List<String> identityProps;

	public CertificateRow(CertificateIdentityConfig certificateIdentityConfig) {
		setCertificateId(certificateIdentityConfig.getCertificate().getKey());
		if (certificateIdentityConfig.getConfig() != null && certificateIdentityConfig.getConfig().getEntry() != null) {
			setCourseDisplayName(certificateIdentityConfig.getConfig().getEntry().getDisplayname());
		}
		setPath(certificateIdentityConfig.getCertificate().getPath());
		setIdentityProps(certificateIdentityConfig.getIdentityProps());
	}

	public void setCertificateId(Long certificateId) {
		this.certificateId = certificateId;
	}

	public Long getCertificateId() {
		return certificateId;
	}

	public void setCourseDisplayName(String courseDisplayName) {
		this.courseDisplayName = courseDisplayName;
	}

	public String getCourseDisplayName() {
		return courseDisplayName;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setIdentityProps(List<String> identityProps) {
		this.identityProps = identityProps;
	}

	public List<String> getIdentityProps() {
		return identityProps;
	}
	
	public String getIdentityProp(int propIdx) {
		if (identityProps == null || propIdx < 0 || propIdx >= identityProps.size()) {
			return "";
		}
		return identityProps.get(propIdx);
	}
}
