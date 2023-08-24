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
package org.olat.course.assessment.ui.tool;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateStatus;

/**
 * 
 * Initial date: 27 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCertificateRow {
	
	private FormLink deleteLink;
	private FormLink downloadLink;
	
	private final String url;
	private final String filename;
	private final Certificate certificate;
	private final Long expiredInDays;
	private final boolean hasThumbnail;
	
	public IdentityCertificateRow(Certificate certificate, String filename, String url, Long expiredInDays, boolean hasThumbnail) {
		this.url = url;
		this.filename = filename;
		this.certificate = certificate;
		this.expiredInDays = expiredInDays;
		this.hasThumbnail = hasThumbnail;
	}
	
	public Long getKey() {
		return certificate.getKey();
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public Date getCreationDate() {
		return certificate.getCreationDate();
	}
	
	public Date getNextRecertificationDate() {
		return certificate.getNextRecertificationDate();
	}
	
	public boolean isThumbnailAvailable() {
		return certificate.getMetadata() != null && hasThumbnail;
	}
	
	public boolean hasExpired() {
		return expiredInDays != null && expiredInDays.longValue() > 0l;
	}
	
	public String getExpiredInDays() {
		return expiredInDays == null || expiredInDays.longValue() < 0l ? "" : expiredInDays.toString();
	}
	
	public boolean isLast() {
		return certificate.isLast();
	}
	
	public String getStatus() {
		return certificate.getStatus() == null ? CertificateStatus.error.name() : certificate.getStatus().name();
	}
	
	public Certificate getCertificate() {
		return certificate;
	}
	
	public FormLink getDeleteLink() {
		return deleteLink;
	}
	
	public void setDeleteLink(FormLink deleteLink) {
		this.deleteLink = deleteLink;
	}
	
	public FormLink getDownloadLink() {
		return downloadLink;
	}
	
	public void setDownloadLink(FormLink downloadLink) {
		this.downloadLink = downloadLink;
	}
}
