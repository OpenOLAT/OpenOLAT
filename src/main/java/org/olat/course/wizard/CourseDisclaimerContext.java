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

import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 09.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CourseDisclaimerContext {
	
	private RepositoryEntry entry; 
	
	private boolean termsOfUseEnabled;
	private boolean dataProtectionEnabled;
	
	private String termsOfUseTitle;
	private String termsOfUseContent;
	private String termsOfUseLabel1;
	private String termsOfUseLabel2;
	
	private String dataProtectionTitle;
	private String dataProtectionContent;
	private String dataProtectionLabel1;
	private String dataProtectionLabel2;
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	public boolean isTermsOfUseEnabled() {
		return termsOfUseEnabled;
	}
	
	public void setTermsOfUseEnabled(boolean termsOfUseEnabled) {
		this.termsOfUseEnabled = termsOfUseEnabled;
	}
	
	public boolean isDataProtectionEnabled() {
		return dataProtectionEnabled;
	}
	
	public void setDataProtectionEnabled(boolean dataProtectionEnabled) {
		this.dataProtectionEnabled = dataProtectionEnabled;
	}
	
	public String getTermsOfUseTitle() {
		return termsOfUseTitle;
	}
	
	public void setTermsOfUseTitle(String termsOfUseTitle) {
		this.termsOfUseTitle = termsOfUseTitle;
	}
	
	public String getTermsOfUseContent() {
		return termsOfUseContent;
	}
	
	public void setTermsOfUseContent(String termsOfUseContent) {
		this.termsOfUseContent = termsOfUseContent;
	}
	
	public String getTermsOfUseLabel1() {
		return termsOfUseLabel1;
	}
	
	public void setTermsOfUseLabel1(String termsOfUseLabel1) {
		this.termsOfUseLabel1 = termsOfUseLabel1;
	}
	
	public String getTermsOfUseLabel2() {
		return termsOfUseLabel2;
	}
	
	public void setTermsOfUseLabel2(String termsOfUseLabel2) {
		this.termsOfUseLabel2 = termsOfUseLabel2;
	}
	
	public String getDataProtectionTitle() {
		return dataProtectionTitle;
	}
	
	public void setDataProtectionTitle(String dataProtectionTitle) {
		this.dataProtectionTitle = dataProtectionTitle;
	}
	
	public String getDataProtectionContent() {
		return dataProtectionContent;
	}
	
	public void setDataProtectionContent(String dataProtectionContent) {
		this.dataProtectionContent = dataProtectionContent;
	}
	
	public String getDataProtectionLabel1() {
		return dataProtectionLabel1;
	}
	
	public void setDataProtectionLabel1(String dataProtectionLabel1) {
		this.dataProtectionLabel1 = dataProtectionLabel1;
	}
	
	public String getDataProtectionLabel2() {
		return dataProtectionLabel2;
	}
	
	public void setDataProtectionLabel2(String dataProtectionLabel2) {
		this.dataProtectionLabel2 = dataProtectionLabel2;
	}
}
