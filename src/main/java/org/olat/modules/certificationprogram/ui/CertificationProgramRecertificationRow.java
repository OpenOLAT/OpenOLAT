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
package org.olat.modules.certificationprogram.ui;

import java.util.Date;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.ui.component.NextRecertificationInDays;

/**
 * 
 * Initial date: 11 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramRecertificationRow {
	
	private final Certificate certificate;
	private final CertificationStatus certificationStatus;
	private final NextRecertificationInDays nextRecertification;
	
	private final FormLink toolsLink;
	
	public CertificationProgramRecertificationRow(Certificate certificate, NextRecertificationInDays nextRecertification,
			CertificationStatus certificationStatus, FormLink toolsLink) {
		this.certificate = certificate;
		this.certificationStatus = certificationStatus;
		this.nextRecertification = nextRecertification;
		this.toolsLink = toolsLink;
	}
	
	public Long getCertificateKey() {
		return certificate.getKey();
	}
	
	public Long getRecertificationCount() {
		return certificate.getRecertificationCount();
	}
	
	public Date getCertificationDate() {
		return certificate.getCreationDate();
	}
	
	public VFSMetadata getCertificateMetadata() {
		return certificate.getMetadata();
	}
	
	public VFSMetadata getCertificatePrintMetadata() {
		return certificate.getPrintMetadata();
	}
	
	public Date getNextRecertificationDate() {
		return certificate.getNextRecertificationDate();
	}
	
	public NextRecertificationInDays getNextRecertification() {
		return nextRecertification;
	}

	public Date getRecertificationWindowDate() {
		return certificate.getRecertificationWindowDate();
	}
	
	public Date getRevocationDate() {
		return certificate.getRevocationDate();
	}

	public CertificationStatus getCertificationStatus() {
		return certificationStatus;
	}
	
	public String getSerialNumber() {
		return certificate.getSerialNumber();
	}
	
	public Certificate getCertificate() {
		return certificate;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}

}
