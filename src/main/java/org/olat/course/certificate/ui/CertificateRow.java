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
package org.olat.course.certificate.ui;

import java.util.Date;

import org.olat.core.util.StringHelper;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.ui.CertificationStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 23 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificateRow {

	private final String origin;
	private final String points;
	private final String filename;
	private final String uploadedByName;
	private final String statusExplained;
	private final Certificate certificate;
	private final RepositoryEntry course;
	private final CertificationStatus status;
	private final Long recertificationCount;
	private final CertificationProgram certificationProgram;
	private final RecertificationInDays recertificationInDays;
	private final RepositoryEntryCertificateConfiguration certificateConfig;
	
	public CertificateRow(Certificate certificate, RepositoryEntry course,
			RepositoryEntryCertificateConfiguration certificateConfig, CertificationProgram certificationProgram,
			String uploadedByName, CertificationStatus status, String statusExplained,
			RecertificationInDays recertificationInDays, Long recertificationCount,
			String filename, String origin, String points) {
		this.status = status;
		this.points = points;
		this.course = course;
		this.origin = origin;
		this.filename = filename;
		this.certificate = certificate;
		this.certificateConfig = certificateConfig;
		this.statusExplained = statusExplained;
		this.uploadedByName = uploadedByName;
		this.certificationProgram = certificationProgram;
		this.recertificationInDays = recertificationInDays;
		this.recertificationCount = recertificationCount;
	}
	
	public Long getKey() {
		return certificate.getKey();
	}
	
	public Certificate getCertificate() {
		return certificate;
	}
	
	public CertificationStatus getStatus() {
		return status;
	}
	
	public String getStatusString() {
		return status == null ? "" : status.name().toLowerCase();
	}
	
	public String getStatusExplained() {
		return statusExplained;
	}
	
	public String getAwardedBy() {
		if(certificationProgram != null) {
			return certificationProgram.getDisplayName();
		}
		if(course != null) {
			return course.getDisplayname();
		}
		if(StringHelper.containsNonWhitespace(uploadedByName)) {
			return uploadedByName;
		}
		if(StringHelper.containsNonWhitespace(certificate.getCourseTitle())) {
			return certificate.getCourseTitle();
		}
		return null;
	}
	
	public String getAwardedByIconCSS() {
		if(certificationProgram != null) {
			return "o_icon_certificate";
		}
		if(course != null) {
			return "o_CourseModule_icon";
		}
		if(StringHelper.containsNonWhitespace(uploadedByName)) {
			return "o_icon_user";
		}
		if(StringHelper.containsNonWhitespace(certificate.getCourseTitle())) {
			return "o_CourseModule_icon";
		}
		return null;
	}
	
	public boolean isUploaded() {
		return StringHelper.containsNonWhitespace(uploadedByName);
	}
	
	public boolean isCourse() {
		OLATResource resource = certificate.getOlatResource();
		return resource != null && "CourseModule".equals(resource.getResourceableTypeName());
	}
	
	public boolean isCertificationProgram() {
		OLATResource resource = certificate.getOlatResource();
		return resource != null && "CertificationProgram".equals(resource.getResourceableTypeName());
	}
	
	public boolean isWithRecertification() {
		return (certificationProgram != null
				&& certificationProgram.isValidityEnabled()
				&& certificationProgram.isRecertificationEnabled())
				|| (certificateConfig != null
					&& certificateConfig.isRecertificationEnabled());
	}
	
	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public Date getCreationDate() {
		return certificate.getCreationDate();
	}
	
	public RepositoryEntry getCourse() {
		return course;
	}
	
	public String getCourseTitle() {
		return course.getDisplayname();
	}
	
	public boolean hasNextRecertificationDate() {
		return certificate != null && certificate.getNextRecertificationDate() != null;
	}
	
	public Date getNextRecertificationDate() {
		return certificate == null
				? null
				: certificate.getNextRecertificationDate();
	}
	
	public Long getRecertificationCount() {
		return recertificationCount;
	}

	public RecertificationInDays getRecertificationInDays() {
		return recertificationInDays;
	}
	
	public boolean isRecertificationWindowOpen() {
		return recertificationInDays != null && recertificationInDays.windowOpen() != null
				&& recertificationInDays.windowOpen().booleanValue();
	}
	
	public Date getEndDateOfRecertificationWindow() {
		return recertificationInDays == null
				? null
				: recertificationInDays.endDateOfRecertificationWindow();
	}
	
	public String getPoints() {
		return points;
	}

	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(certificate.getPath());
	}
	
	public String getPath() {
		return certificate.getPath();
	}
	
	public String getFilename() {
		return filename;
	}
}
