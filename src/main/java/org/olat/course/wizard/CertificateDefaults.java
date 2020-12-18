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
package org.olat.course.wizard;

import org.olat.course.certificate.CertificateTemplate;

/**
 * 
 * Initial date: 11 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CertificateDefaults {
	
	private boolean automaticCertificationEnabled;
	private boolean manualCertificationEnabled;
	private CertificateTemplate template;
	private String certificateCustom1;
	private String certificateCustom2;
	private String certificateCustom3;
	
	public boolean isAutomaticCertificationEnabled() {
		return automaticCertificationEnabled;
	}
	
	public void setAutomaticCertificationEnabled(boolean automaticCertificationEnabled) {
		this.automaticCertificationEnabled = automaticCertificationEnabled;
	}

	public boolean isManualCertificationEnabled() {
		return manualCertificationEnabled;
	}

	public void setManualCertificationEnabled(boolean manualCertificationEnabled) {
		this.manualCertificationEnabled = manualCertificationEnabled;
	}

	public CertificateTemplate getTemplate() {
		return template;
	}

	public void setTemplate(CertificateTemplate template) {
		this.template = template;
	}

	public String getCertificateCustom1() {
		return certificateCustom1;
	}

	public void setCertificateCustom1(String certificateCustom1) {
		this.certificateCustom1 = certificateCustom1;
	}

	public String getCertificateCustom2() {
		return certificateCustom2;
	}

	public void setCertificateCustom2(String certificateCustom2) {
		this.certificateCustom2 = certificateCustom2;
	}

	public String getCertificateCustom3() {
		return certificateCustom3;
	}

	public void setCertificateCustom3(String certificateCustom3) {
		this.certificateCustom3 = certificateCustom3;
	}
	
}
