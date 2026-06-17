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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.AttachmentImpl;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rreference")
@Table(name="o_selectus_reference")
public class ReferenceImpl implements Reference, CreateInfo, Persistable {

	private static final long serialVersionUID = 8908016638654225807L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="ref_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key = null;
	
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="title", nullable=true, unique=false, insertable=true, updatable=true)
	private String title;
	@Column(name="firstname", nullable=true, unique=false, insertable=true, updatable=true)
	private String firstName;
	@Column(name="lastname", nullable=true, unique=false, insertable=true, updatable=true)
	private String lastName;
	
	@Column(name="institution", nullable=true, unique=false, insertable=true, updatable=true)
	private String institution;
	@Column(name="mail", nullable=true, unique=false, insertable=true, updatable=true)
	private String email;
	
	@Column(name="disclaimer", nullable=true, unique=false, insertable=true, updatable=true)
	private boolean disclaimer;
	@Column(name="privacydisclaimer", nullable=true, unique=false, insertable=true, updatable=true)
	private boolean privacyDisclaimer;
	@Column(name="submissionurl", nullable=true, unique=false, insertable=true, updatable=true)
	private String submissionUrl;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="submissiondate", nullable=true, unique=false, insertable=true, updatable=true)
	private Date submissionDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="submissiondeadline", nullable=true, unique=false, insertable=true, updatable=true)
	private Date submissionDeadline;
	
	@Column(name="dateinvitation", nullable=true, unique=false, insertable=true, updatable=true)
	private Date dateInvitation;
	@Column(name="datelastreminder", nullable=true, unique=false, insertable=true, updatable=true)
	private Date dateLastReminder;
	
	@Column(name="remindersbyapplicant", nullable=true, unique=false, insertable=true, updatable=true)
	private int remindersByApplicant;

	@Column(name="reftype", nullable=true, unique=false, insertable=true, updatable=true)
	private String type;
	@Column(name="status", nullable=true, unique=false, insertable=true, updatable=true)
	private String status;
	@Enumerated(EnumType.STRING)
	@Column(name="requeststatus", nullable=true, unique=false, insertable=true, updatable=true)
	private ReferenceRequestStatus requestStatus;

	@Column(name="adminnote", nullable=true, unique=false, insertable=true, updatable=true)
	private String adminNote;
	
	@Column(name="dateconsent", nullable=true, unique=false, insertable=true, updatable=true)
	private Date dateConsent;
	@Column(name="consentbystaff", nullable=true, unique=false, insertable=true, updatable=true)
	private Boolean consentByStaff;
	
	@ManyToOne(targetEntity=ApplicationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_application_id", nullable=true, insertable=true, updatable=true)
	private Application application;
	
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true,cascade={CascadeType.REMOVE})
	@JoinColumn(name="fk_letter_id", nullable=true, insertable=true, updatable=true)
	private Attachment letter;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String getInstitution() {
		return institution;
	}

	@Override
	public void setInstitution(String institution) {
		this.institution = institution;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean isDisclaimer() {
		return disclaimer;
	}

	@Override
	public void setDisclaimer(boolean disclaimer) {
		this.disclaimer = disclaimer;
	}

	@Override
	public boolean isPrivacyDisclaimer() {
		return privacyDisclaimer;
	}

	@Override
	public void setPrivacyDisclaimer(boolean privacyDisclaimer) {
		this.privacyDisclaimer = privacyDisclaimer;
	}

	@Override
	public String getSubmissionUrl() {
		return submissionUrl;
	}

	public void setSubmissionUrl(String submissionUrl) {
		this.submissionUrl = submissionUrl;
	}

	@Override
	public Date getSubmissionDate() {
		return submissionDate;
	}

	@Override
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}

	@Override
	public Date getSubmissionDeadline() {
		return submissionDeadline;
	}

	@Override
	public void setSubmissionDeadline(Date submissionDeadline) {
		this.submissionDeadline = submissionDeadline;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public Date getDateInvitation() {
		return dateInvitation;
	}

	@Override
	public void setDateInvitation(Date dateInvitation) {
		this.dateInvitation = dateInvitation;
	}

	@Override
	public Date getDateLastReminder() {
		return dateLastReminder;
	}

	@Override
	public void setDateLastReminder(Date dateLastReminder) {
		this.dateLastReminder = dateLastReminder;
	}
	
	@Override
	public int getRemindersByApplicant() {
		return remindersByApplicant;
	}

	@Override
	public void setRemindersByApplicant(int remindersByApplicant) {
		this.remindersByApplicant = remindersByApplicant;
	}

	@Transient
	@Override
	public ReferenceStatus getReferenceStatus() {
		return StringHelper.containsNonWhitespace(status) ? ReferenceStatus.valueOf(status) : null;
	}
	
	@Override
	public void setReferenceStatus(ReferenceStatus referenceStatus) {
		if(referenceStatus == null) {
			status = null;
		} else {
			status = referenceStatus.name();
		}
	}

	@Override
	public ReferenceRequestStatus getRequestStatus() {
		return requestStatus;
	}

	@Override
	public void setRequestStatus(ReferenceRequestStatus requestStatus) {
		this.requestStatus = requestStatus;
	}

	@Override
	public String getAdminNote() {
		return adminNote;
	}

	@Override
	public void setAdminNote(String adminNote) {
		this.adminNote = adminNote;
	}

	@Override
	public Date getDateConsent() {
		return dateConsent;
	}

	@Override
	public void setDateConsent(Date dateConsent) {
		this.dateConsent = dateConsent;
	}

	@Override
	public Boolean getConsentByStaff() {
		return consentByStaff;
	}

	@Override
	public void setConsentByStaff(Boolean consentByStaff) {
		this.consentByStaff = consentByStaff;
	}

	@Transient
	@Override
	public ReferenceType getReferenceType() {
		return StringHelper.containsNonWhitespace(type) ? ReferenceType.valueOf(type) : null;
	}

	@Override
	public void setReferenceType(ReferenceType referenceType) {
		if(referenceType == null) {
			type = null;
		} else {
			type = referenceType.name();
		}
	}

	@Override
	public Application getApplication() {
		return application;
	}

	@Override
	public void setApplication(Application application) {
		this.application = application;
	}

	@Override
	public Attachment getLetter() {
		return letter;
	}

	@Override
	public void setLetter(Attachment letter) {
		this.letter = letter;
	}

	@Override
	public int hashCode() {
		return key == null ? 72659 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ReferenceImpl) {
			ReferenceImpl ref = (ReferenceImpl)obj;
			return key != null && key.equals(ref.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("reference[key:").append(getKey())
		  .append(";lastName:").append(lastName == null ? "" : lastName)
		  .append(";firstName:").append(firstName == null ? "" : firstName)
		  .append(";email:").append(email == null ? "" : email)
		  .append(";type:").append(type == null ? "" : type).append("]");
		return sb.toString();
	}
}