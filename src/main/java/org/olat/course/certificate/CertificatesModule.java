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
	
	private static final String CERTIFICATE_BCC = "certificate.bcc";
	private static final String CERTIFICATE_LINEMANAGER = "certificate.linemanager";
	
	@Value("${certificate.bcc}")
	private String certificateBcc;
	@Value("${certificate.linemanager:false}")
	private boolean certificateLinemanager;

	@Autowired
	public CertificatesModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String bccObj = getStringPropertyValue(CERTIFICATE_BCC, true);
		if(StringHelper.containsNonWhitespace(bccObj)) {
			certificateBcc = bccObj;
		}
		
		String linemangerObj = getStringPropertyValue(CERTIFICATE_LINEMANAGER, true);
		if(StringHelper.containsNonWhitespace(linemangerObj)) {
			certificateLinemanager = "true".equals(linemangerObj);
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
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
	
}
