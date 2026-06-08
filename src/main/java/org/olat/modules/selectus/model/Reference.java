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
package org.olat.modules.selectus.model;

import java.util.Date;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Reference extends PersonName {
	
	public Long getKey();

	public Date getCreationDate();

	public Date getLastModified();
	
	public void setTitle(String title);
	
	public void setFirstName(String firstName);
	
	public void setLastName(String lastName);

	public String getInstitution();

	public void setInstitution(String institution);

	public String getEmail();

	public void setEmail(String email);
	
	public boolean isDisclaimer();
	
	public void setDisclaimer(boolean accept);
	
	public boolean isPrivacyDisclaimer();
	
	public void setPrivacyDisclaimer(boolean accept);
	
	public String getSubmissionUrl();

	public Date getSubmissionDeadline();

	public void setSubmissionDeadline(Date submissionDeadline);
	
	public Date getSubmissionDate();
	
	public void setSubmissionDate(Date date);

	public ReferenceType getReferenceType();

	public void setReferenceType(ReferenceType type);
	
	public ReferenceStatus getReferenceStatus();
	
	public void setReferenceStatus(ReferenceStatus referenceStatus);
	
	
	public ReferenceRequestStatus getRequestStatus();
	
	public void setRequestStatus(ReferenceRequestStatus referenceStatus);
	
	public String getAdminNote();
	
	public void setAdminNote(String note);
	
	public Date getDateConsent();

	public void setDateConsent(Date dateConsent);

	public Boolean getConsentByStaff();

	public void setConsentByStaff(Boolean consentByStaff);
	
	
	public Date getDateInvitation();

	public void setDateInvitation(Date dateInvitation);

	public Date getDateLastReminder();

	public void setDateLastReminder(Date dateLastReminder);
	
	public int getRemindersByApplicant();

	public void setRemindersByApplicant(int reminderByApplicant);

	public Attachment getLetter();
	
	public void setLetter(Attachment letter);
	
	public Application getApplication();
	
	public void setApplication(Application application);

}
