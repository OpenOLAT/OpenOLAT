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
package org.olat.modules.selectus.model.references;

import java.util.Date;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;

/**
 * 
 * Initial date: 25 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TransientReference implements Reference {
	
	private final Reference reference;
	private final Date deadline;
	
	public TransientReference(Reference reference, Date deadline) {
		this.reference = reference;
		this.deadline = deadline;
	}

	@Override
	public String getTitle() {
		return reference.getTitle();
	}

	@Override
	public String getFirstName() {
		return reference.getFirstName();
	}

	@Override
	public String getLastName() {
		return reference.getLastName();
	}

	@Override
	public Long getKey() {
		return null;
	}

	@Override
	public Date getCreationDate() {
		return null;
	}

	@Override
	public Date getLastModified() {
		return null;
	}

	@Override
	public void setTitle(String title) {
		//
	}

	@Override
	public void setFirstName(String firstName) {
		//
	}

	@Override
	public void setLastName(String lastName) {
		//
	}

	@Override
	public String getInstitution() {
		return reference.getInstitution();
	}

	@Override
	public void setInstitution(String institution) {
		//
	}

	@Override
	public String getEmail() {
		return reference.getEmail();
	}

	@Override
	public void setEmail(String email) {
		//
	}

	@Override
	public boolean isDisclaimer() {
		return false;
	}

	@Override
	public void setDisclaimer(boolean accept) {
		//
	}

	@Override
	public boolean isPrivacyDisclaimer() {
		return false;
	}

	@Override
	public void setPrivacyDisclaimer(boolean accept) {
		//
	}

	@Override
	public String getSubmissionUrl() {
		return reference.getSubmissionUrl();
	}

	@Override
	public Date getSubmissionDeadline() {
		return deadline;
	}

	@Override
	public void setSubmissionDeadline(Date submissionDeadline) {
		//
	}

	@Override
	public Date getSubmissionDate() {
		return reference.getSubmissionDate();
	}

	@Override
	public void setSubmissionDate(Date date) {
		//
	}

	@Override
	public ReferenceType getReferenceType() {
		return reference.getReferenceType();
	}

	@Override
	public void setReferenceType(ReferenceType type) {
		//
	}

	@Override
	public ReferenceStatus getReferenceStatus() {
		return reference.getReferenceStatus();
	}

	@Override
	public void setReferenceStatus(ReferenceStatus referenceStatus) {
		//
	}

	@Override
	public ReferenceRequestStatus getRequestStatus() {
		return reference.getRequestStatus();
	}

	@Override
	public void setRequestStatus(ReferenceRequestStatus referenceStatus) {
		//
	}
	
	@Override
	public String getAdminNote() {
		return reference.getAdminNote();
	}

	@Override
	public void setAdminNote(String note) {
		//
	}
	
	@Override
	public Date getDateConsent() {
		return null;
	}

	@Override
	public void setDateConsent(Date dateConsent) {
		//
	}
	
	@Override
	public int getRemindersByApplicant() {
		return 0;
	}

	@Override
	public void setRemindersByApplicant(int reminderByApplicant) {
		//
	}

	@Override
	public Boolean getConsentByStaff() {
		return null;
	}

	@Override
	public void setConsentByStaff(Boolean consentByStaff) {
		//
	}

	@Override
	public Date getDateInvitation() {
		return null;
	}

	@Override
	public void setDateInvitation(Date dateInvitation) {
		//
	}

	@Override
	public Date getDateLastReminder() {
		return null;
	}

	@Override
	public void setDateLastReminder(Date dateLastReminder) {
		//
	}

	@Override
	public Attachment getLetter() {
		return null;
	}

	@Override
	public void setLetter(Attachment letter) {
		//
	}

	@Override
	public Application getApplication() {
		return reference.getApplication();
	}

	@Override
	public void setApplication(Application application) {
		reference.setApplication(application);
	}
}
