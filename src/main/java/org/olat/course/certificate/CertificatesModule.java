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
package org.olat.course.certificate;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 31 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CertificatesModule extends AbstractSpringModule {

	private static final String MANAGED_CERTIFICATES_ENABLED = "managedCertificates";
	private static final String CERTIFICATE_BCC = "certificate.bcc";
	private static final String CERTIFICATE_LINEMANAGER = "certificate.linemanager";
	private static final String CERTIFICATE_USER_MANAGER_UPLOAD = "certificate.user.manager.upload.external";
	private static final String CERTIFICATE_USER_UPLOAD = "certificate.user.upload.external";
	private static final String CERTIFICATE_UPLOAD_LIMIT = "certificate.upload.limit.mb";

	@Value("${certificate.managed}")
	private boolean managedCertificates;
	@Value("${certificate.bcc}")
	private String certificateBcc;
	@Value("${certificate.linemanager:false}")
	private boolean certificateLinemanager;
	@Value("${certificate.user.upload.external:false}")
	private boolean userCanUploadExternalCertificates;
	@Value("${certificate.user.manager.upload.external:false}")
	private boolean userManagerCanUploadExternalCertificates;
	@Value("${certificate.upload.limit.mb}")
	private int uploadLimit;
	
	@Autowired
	public CertificatesModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String managed = getStringPropertyValue(MANAGED_CERTIFICATES_ENABLED, true);
		if(StringHelper.containsNonWhitespace(managed)) {
			managedCertificates = "true".equals(managed);
		}
		
		String bccObj = getStringPropertyValue(CERTIFICATE_BCC, true);
		if(StringHelper.containsNonWhitespace(bccObj)) {
			certificateBcc = bccObj;
		}
		
		String linemangerObj = getStringPropertyValue(CERTIFICATE_LINEMANAGER, true);
		if(StringHelper.containsNonWhitespace(linemangerObj)) {
			certificateLinemanager = "true".equals(linemangerObj);
		}
		
		String userUploadObj = getStringPropertyValue(CERTIFICATE_USER_UPLOAD, true);
		if(StringHelper.containsNonWhitespace(userUploadObj)) {
			userCanUploadExternalCertificates = "true".equals(userUploadObj);
		}
		
		String userManagerUploadObj = getStringPropertyValue(CERTIFICATE_USER_MANAGER_UPLOAD, true);
		if(StringHelper.containsNonWhitespace(userManagerUploadObj)) {
			userManagerCanUploadExternalCertificates = "true".equals(userManagerUploadObj);
		}
		
		uploadLimit = getIntPropertyValue(CERTIFICATE_UPLOAD_LIMIT, uploadLimit);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public boolean isManagedCertificates() {
		return managedCertificates;
	}

	public void setManagedCertificates(boolean enabled) {
		managedCertificates = enabled;
		setStringProperty(MANAGED_CERTIFICATES_ENABLED, Boolean.toString(enabled), true);
	}
	
	public String getCertificateBcc() {
		return certificateBcc;
	}
	
	public void setCertificateBcc(String email) {
		certificateBcc = email;
		setStringProperty(CERTIFICATE_BCC, email, true);
	}
	
	public List<String> getCertificatesBccEmails() {
		return splitEmails(certificateBcc);
	}
	
	public List<String> splitEmails(String emails) {
		List<String> emailList = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(emails)) {
			String[] emailArr = emails.split("[;,]");
			for(String email:emailArr) {
				emailList.add(email);
			}
		}
		return emailList;
	}

	public boolean isCertificateLinemanager() {
		return certificateLinemanager;
	}

	public void setCertificateLinemanager(boolean certificateLinemanager) {
		this.certificateLinemanager = certificateLinemanager;
		setStringProperty(CERTIFICATE_LINEMANAGER, Boolean.toString(certificateLinemanager), true);
	}
	
	public boolean canUserUploadExternalCertificates() {
		return userCanUploadExternalCertificates;
	}
	
	public void setUserCanUploadExternalCertificates(boolean userCanUploadExternalCertificates) {
		this.userCanUploadExternalCertificates = userCanUploadExternalCertificates;
		setStringProperty(CERTIFICATE_USER_UPLOAD, Boolean.toString(userCanUploadExternalCertificates), true);
	}
	
	public boolean canUserManagerUploadExternalCertificates() {
		return userManagerCanUploadExternalCertificates;
	}
	
	public void setUserManagerCanUploadExternalCertificates(boolean userManagerCanUploadExternalCertificates) {
		this.userManagerCanUploadExternalCertificates = userManagerCanUploadExternalCertificates;
		setStringProperty(CERTIFICATE_USER_MANAGER_UPLOAD, Boolean.toString(userManagerCanUploadExternalCertificates), true);
	}
	
	public int getUploadLimit() {
		return uploadLimit;
	}
	
	public void setUploadLimit(int uploadLimit) {
		this.uploadLimit = uploadLimit;
		setIntProperty(CERTIFICATE_UPLOAD_LIMIT, uploadLimit, true);
	}
	
}
