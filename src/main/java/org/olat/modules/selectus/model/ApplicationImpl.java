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
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.ApplicationStatus;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="rapplication")
@Table(name="o_selectus_application")
@NamedQuery(name="lastApplicationByModificationAndPosition", query="select max(app.lastModified) from rapplication app where app.position.key=:positionKey and app.valid=true")
@NamedQuery(name="numOfApplicationsByPositionAndMail", query="select count(app.id) from rapplication app where app.position.key=:positionKey and lower(app.person.email)=:mail")
@NamedQuery(name="nextApplicationId", query="select max(app.id) from rapplication app where app.position=:position")
@NamedQuery(name="loadApplicationByKey", query="select app from rapplication app inner join fetch app.position as pos where app.key=:key")
@NamedQuery(name="loadValidApplicationByKey", query="select app from rapplication app inner join fetch app.position as pos where app.key=:key and app.valid=true")
@NamedQuery(name="loadApplicationDecisionByKey", query="select app.decision from rapplication app where app.key=:key")
public class ApplicationImpl implements Application, CreateInfo, Persistable {

	private static final long serialVersionUID = 7241885035110830055L;
	
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
	@Column(name="app_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key = null;
	
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="last_modified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="application_lid", nullable=true, insertable=true, updatable=true)
	private Integer id;
	@Column(name="is_valid", nullable=false, insertable=true, updatable=true)
	private boolean valid = true;

	/**
	 * This is the afterClosing flag recycled
	 */
	@Column(name="after_position_closed", nullable=false, insertable=true, updatable=true)
	private boolean submittedByStaff;
	@Column(name="withdrawn", nullable=false, insertable=true, updatable=true)
	private boolean withdrawn;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="withdrawn_date", nullable=true, insertable=true, updatable=true)
	private Date withdrawnDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="onhold_date", nullable=true, insertable=true, updatable=true)
	private Date onholdDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="rejected_date", nullable=true, insertable=true, updatable=true)
	private Date rejectedDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="noteligible_date", nullable=true, insertable=true, updatable=true)
	private Date notEligibleDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="granted_date", nullable=true, insertable=true, updatable=true)
	private Date grantedDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="hired_date", nullable=true, insertable=true, updatable=true)
	private Date hiredDate;

	@Column(name="status", nullable=true, insertable=false, updatable=true)
	private String status;
	@Column(name="status_comment", nullable=true, insertable=false, updatable=true)
	private String statusComment;
	
	@Column(name="decision", nullable=true, insertable=false, updatable=false)
	private Integer decision;
	@Column(name="jobad", nullable=true, insertable=true, updatable=true)
	private String jobAd;
	@Column(name="applanguage", nullable=true, insertable=true, updatable=true)
	private String language;
	@Column(name="acceptterms", nullable=true, insertable=true, updatable=true)
	private Boolean acceptTerms;
	@Column(name="expertconsent", nullable=true, insertable=true, updatable=true)
	private Boolean expertConsent;
	@Column(name="expertblacklist", nullable=true, insertable=true, updatable=true)
	private String expertBlackList;
	
	@Column(name="public_feedback_enable", nullable=true, insertable=true, updatable=true)
	private boolean publicFeedbackEnabled = false;
	@Column(name="public_feedback_deadline", nullable=true, insertable=true, updatable=true)
	private Date publicFeedbackDeadline;
	@Column(name="public_feedback_key", nullable=true, insertable=true, updatable=true)
	private String publicFeedbackKey;
	
	@Column(name="memo", nullable=true, unique=false, insertable=true, updatable=true)
	private String memo;
	@Column(name="committeecomment", nullable=true, unique=false, insertable=true, updatable=true)
	private String committeeComment;
	

	@Column(name="report_ratings_2", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfRatingsA;
	@Column(name="report_ratings_1", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfRatingsB;
	@Column(name="report_ratings_0", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfRatingsC;
	@Column(name="report_ratings_abstentions", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfRatingsAbsentions;
	
	@Column(name="report_experts", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfExperts;
	@Column(name="report_experts_letters", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfExpertsLetters;
	@Column(name="report_referees", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfReferees;
	@Column(name="report_referees_letters", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfRefereesLetters;
	@Column(name="report_comp_experts", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfComparativeExperts;
	@Column(name="report_comp_experts_letters", nullable=true, unique=false, insertable=false, updatable=true)
	private Integer reportingNumOfComparativeExpertsLetters;
	
	@Embedded
	private PersonImpl person;
	@Embedded
	private AddressImpl address;
	@Embedded
	private BusinessAddressImpl businessAddress;
	@Embedded
	private BusinessInformationsImpl businessInformations;
	@Embedded
	private AcademicalBackgroundImpl academicalBackground;
	@Embedded
	private AttachmentsImpl attachments;
	@Embedded
	private ProjectImpl project;

	@OneToMany(targetEntity=ApplicationAttributeImpl.class, mappedBy="application",
			cascade= { CascadeType.ALL })
	private Set<ApplicationAttribute> attributes;	
	
	@ManyToOne(targetEntity=PositionImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_position_id", nullable=false, insertable=true, updatable=true)
	private Position position;
	
	
	@Column(name="applicanturl", nullable=true, unique=false, insertable=true, updatable=true)
	private String applicantUrl;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity_id", nullable=true, insertable=true, updatable=true)
	private Identity identity;

	public ApplicationImpl() {
		//
	}

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
	public String getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}	

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public String getResourceableTypeName() {
		return "RecruitingApplicationImpl";
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public boolean isSubmittedByStaff() {
		return submittedByStaff;
	}

	@Override
	public void setSubmittedByStaff(boolean submittedByStaff) {
		this.submittedByStaff = submittedByStaff;
	}

	public boolean isWithdrawn() {
		return withdrawn;
	}

	public void setWithdrawn(boolean withdrawn) {
		this.withdrawn = withdrawn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	@Transient
	public ApplicationStatus getApplicationStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return ApplicationStatus.valueOf(status);
		}
		return ApplicationStatus.active;
	}

	@Override
	@Transient
	public void setApplicationStatus(ApplicationStatus status) {
		if(status == null) {
			this.status = ApplicationStatus.active.name();
		} else {
			this.status = status.name();
		}
	}

	@Override
	public String getStatusComment() {
		return statusComment;
	}

	@Override
	public void setStatusComment(String statusComment) {
		this.statusComment = statusComment;
	}

	@Override
	public Date getOnholdDate() {
		return onholdDate;
	}

	public void setOnholdDate(Date onholdDate) {
		this.onholdDate = onholdDate;
	}

	@Override
	public Date getRejectedDate() {
		return rejectedDate;
	}

	public void setRejectedDate(Date rejectedDate) {
		this.rejectedDate = rejectedDate;
	}

	@Override
	public Date getWithdrawnDate() {
		return withdrawnDate;
	}

	@Override
	public void setWithdrawnDate(Date withdrawnDate) {
		this.withdrawnDate = withdrawnDate;
	}

	@Override
	public Date getNotEligibleDate() {
		return notEligibleDate;
	}

	@Override
	public void setNotEligibleDate(Date notEligibleDate) {
		this.notEligibleDate = notEligibleDate;
	}

	@Override
	public Date getGrantedDate() {
		return grantedDate;
	}

	@Override
	public void setGrantedDate(Date grantedDate) {
		this.grantedDate = grantedDate;
	}
	
	@Override
	public Date getHiredDate() {
		return hiredDate;
	}

	@Override
	public void setHiredDate(Date hiredDate) {
		this.hiredDate = hiredDate;
	}

	@Override
	public Date getStatusDate() {
		switch(getApplicationStatus()) {
			case active: return getCreationDate();
			case onhold: return getOnholdDate();
			case withdrawn: return getWithdrawnDate();
			case rejected: return getRejectedDate();
			case noteligible: return getNotEligibleDate();
			case granted: return getGrantedDate();
			case hired: return getHiredDate();
			default: return getCreationDate();
		}
	}

	@Override
	public Integer getDecision() {
		return decision;
	}

	@Override
	public String getMemo() {
		return memo;
	}

	@Override
	public void setMemo(String memo) {
		this.memo = memo;
	}

	@Override
	public String getCommitteeComment() {
		return committeeComment;
	}

	@Override
	public void setCommitteeComment(String committeeComment) {
		this.committeeComment = committeeComment;
	}
	
	

	public String getApplicantUrl() {
		return applicantUrl;
	}

	public void setApplicantUrl(String applicantUrl) {
		this.applicantUrl = applicantUrl;
	}

	@Override
	public String getJobAd() {
		return jobAd;
	}

	@Override
	public void setJobAd(String jobAd) {
		this.jobAd = jobAd;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public Boolean getAcceptTerms() {
		return acceptTerms;
	}

	@Override
	public void setAcceptTerms(Boolean acceptTerms) {
		this.acceptTerms = acceptTerms;
	}

	@Override
	public Boolean getExpertConsent() {
		return expertConsent;
	}

	@Override
	public void setExpertConsent(Boolean expertConsent) {
		this.expertConsent = expertConsent;
	}

	@Override
	public String getExpertBlackList() {
		return expertBlackList;
	}

	@Override
	public void setExpertBlackList(String expertBlackList) {
		this.expertBlackList = expertBlackList;
	}

	@Override
	public boolean isPublicFeedbackEnabled() {
		return publicFeedbackEnabled;
	}

	@Override
	public void setPublicFeedbackEnabled(boolean publicFeedbackEnabled) {
		this.publicFeedbackEnabled = publicFeedbackEnabled;
	}

	@Override
	public Date getPublicFeedbackDeadline() {
		return publicFeedbackDeadline;
	}

	@Override
	public void setPublicFeedbackDeadline(Date publicFeedbackDeadline) {
		this.publicFeedbackDeadline = publicFeedbackDeadline;
	}
	
	@Override
	public String getPublicFeedbackKey() {
		return publicFeedbackKey;
	}

	public void setPublicFeedbackKey(String publicFeedbackKey) {
		this.publicFeedbackKey = publicFeedbackKey;
	}

	@Override
	public Person getPerson() {
		return person;
	}

	@Override
	public void setPerson(Person person) {
		this.person = (PersonImpl)person;
	}

	@Override
	public Address getAddress() {
		if(address == null) {
			address = new AddressImpl();
		}
		return address;
	}

	@Override
	public void setAddress(Address address) {
		this.address = (AddressImpl)address;
	}

	@Override
	public BusinessAddress getBusinessAddress() {
		if(businessAddress == null) {
			businessAddress = new BusinessAddressImpl();
		}
		return businessAddress;
	}

	@Override
	public void setBusinessAddress(BusinessAddress businessAddress) {
		this.businessAddress = (BusinessAddressImpl)businessAddress;
	}

	@Override
	public BusinessInformations getBusinessInformations() {
		if(businessInformations == null) {
			businessInformations = new BusinessInformationsImpl();
		}
		return businessInformations;
	}

	@Override
	public void setBusinessInformations(BusinessInformations businessInformations) {
		this.businessInformations = (BusinessInformationsImpl)businessInformations;
	}

	@Override
	public AcademicalBackground getAcademicalBackground() {
		if(academicalBackground == null) {
			academicalBackground = new AcademicalBackgroundImpl();
		}
		return academicalBackground;
	}

	@Override
	public void setAcademicalBackground(AcademicalBackground academicalBackground) {
		this.academicalBackground = (AcademicalBackgroundImpl)academicalBackground;
	}

	@Override
	public Project getProject() {
		if(project == null) {
			project = new ProjectImpl();
		}
		return project;
	}

	@Override
	public void setProject(Project project) {
		this.project = (ProjectImpl)project;
	}

	@Override
	public Attachments getAttachments() {
		if(attachments == null) {
			attachments = new AttachmentsImpl();
		}
		return attachments;
	}

	@Override
	public void setAttachments(Attachments attachments) {
		this.attachments = (AttachmentsImpl)attachments;
	}

	@Override
	public Set<ApplicationAttribute> getAttributes() {
		if(attributes == null) {
			attributes = new HashSet<>();
		}
		return attributes;
	}

	@Override
	public void setAttributes(Set<ApplicationAttribute> attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public Integer getReportingNumOfRatingsA() {
		return reportingNumOfRatingsA;
	}

	@Override
	public void setReportingNumOfRatingsA(Integer reportingNumOfRatingsA) {
		this.reportingNumOfRatingsA = reportingNumOfRatingsA;
	}

	@Override
	public Integer getReportingNumOfRatingsB() {
		return reportingNumOfRatingsB;
	}

	@Override
	public void setReportingNumOfRatingsB(Integer reportingNumOfRatingsB) {
		this.reportingNumOfRatingsB = reportingNumOfRatingsB;
	}

	@Override
	public Integer getReportingNumOfRatingsC() {
		return reportingNumOfRatingsC;
	}

	@Override
	public void setReportingNumOfRatingsC(Integer reportingNumOfRatingsC) {
		this.reportingNumOfRatingsC = reportingNumOfRatingsC;
	}

	@Override
	public Integer getReportingNumOfRatingsAbsentions() {
		return reportingNumOfRatingsAbsentions;
	}

	@Override
	public void setReportingNumOfRatingsAbsentions(Integer reportingNumOfRatingsAbsentions) {
		this.reportingNumOfRatingsAbsentions = reportingNumOfRatingsAbsentions;
	}

	@Override
	public Integer getReportingNumOfExperts() {
		return reportingNumOfExperts;
	}

	@Override
	public void setReportingNumOfExperts(Integer reportingNumOfExperts) {
		this.reportingNumOfExperts = reportingNumOfExperts;
	}

	@Override
	public Integer getReportingNumOfExpertsLetters() {
		return reportingNumOfExpertsLetters;
	}

	@Override
	public void setReportingNumOfExpertsLetters(Integer reportingNumOfExpertsLetters) {
		this.reportingNumOfExpertsLetters = reportingNumOfExpertsLetters;
	}

	@Override
	public Integer getReportingNumOfReferees() {
		return reportingNumOfReferees;
	}

	@Override
	public void setReportingNumOfReferees(Integer reportingNumOfReferees) {
		this.reportingNumOfReferees = reportingNumOfReferees;
	}

	@Override
	public Integer getReportingNumOfRefereesLetters() {
		return reportingNumOfRefereesLetters;
	}

	@Override
	public void setReportingNumOfRefereesLetters(Integer reportingNumOfRefereesLetters) {
		this.reportingNumOfRefereesLetters = reportingNumOfRefereesLetters;
	}

	@Override
	public Integer getReportingNumOfComparativeExperts() {
		return reportingNumOfComparativeExperts;
	}

	@Override
	public void setReportingNumOfComparativeExperts(Integer reportingNumOfComparativeExperts) {
		this.reportingNumOfComparativeExperts = reportingNumOfComparativeExperts;
	}

	@Override
	public Integer getReportingNumOfComparativeExpertsLetters() {
		return reportingNumOfComparativeExpertsLetters;
	}

	@Override
	public void setReportingNumOfComparativeExpertsLetters(Integer reportingNumOfComparativeExpertsLetters) {
		this.reportingNumOfComparativeExpertsLetters = reportingNumOfComparativeExpertsLetters;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 394857 : getKey().intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ApplicationImpl) {
			ApplicationImpl app = (ApplicationImpl)obj;
			return getKey() != null && getKey().equals(app.getKey());
		}
		return false;
	}

	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("application[key:").append(getKey() == null ? "" : getKey()).append(";")
			.append(person == null ? "" : person.toString())
			.append(address == null ? "" : address.toString())
			.append(academicalBackground == null ? "" : academicalBackground.toString())
			.append(attachments == null ? "" : attachments.toString());
		return sb.toString();
	}
}
