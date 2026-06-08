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

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
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

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="rapplicationlight")
@Table(name="o_selectus_application")
@NamedQueries({
	@NamedQuery(name="loadApplicationLightByKey", query="select app from rapplicationlight app where app.key=:key")
})
public class ApplicationLightImpl implements ApplicationLight, CreateInfo, Persistable {

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
	
	@Column(name="decision", nullable=true, insertable=true, updatable=true)
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
	
	@Column(name="applicanturl", nullable=true, unique=false, insertable=true, updatable=true)
	private String applicantUrl;
	
	@Transient
	private String[] additionalValues;
	@Transient
	private PositionAttributeDefinitionConfiguration[] additionalValuesTypes;
	
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
	private ProjectImpl project;
	
	@Column(name="fk_position_id", nullable=false, insertable=false, updatable=false)
	private Long positionKey;

	public ApplicationLightImpl() {
		//
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
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
	public String getLanguage() {
		return language;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public boolean isSubmittedByStaff() {
		return submittedByStaff;
	}

	public boolean isWithdrawn() {
		return withdrawn;
	}
	
	public String getStatus() {
		return status;
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
	public Date getOnholdDate() {
		return onholdDate;
	}

	@Override
	public Date getRejectedDate() {
		return rejectedDate;
	}

	@Override
	public Date getWithdrawnDate() {
		return withdrawnDate;
	}

	@Override
	public Date getNotEligibleDate() {
		return notEligibleDate;
	}

	public void setNotEligibleDate(Date notEligibleDate) {
		this.notEligibleDate = notEligibleDate;
	}

	@Override
	public Date getGrantedDate() {
		return grantedDate;
	}

	public void setGrantedDate(Date grantedDate) {
		this.grantedDate = grantedDate;
	}

	@Override
	public Date getHiredDate() {
		return hiredDate;
	}

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
	public Boolean getAcceptTerms() {
		return acceptTerms;
	}

	public void setAcceptTerms(Boolean acceptTerms) {
		this.acceptTerms = acceptTerms;
	}

	@Override
	public Boolean getExpertConsent() {
		return expertConsent;
	}

	public void setExpertConsent(Boolean expertConsent) {
		this.expertConsent = expertConsent;
	}

	@Override
	public String getExpertBlackList() {
		return expertBlackList;
	}

	public void setExpertBlackList(String expertBlackList) {
		this.expertBlackList = expertBlackList;
	}
	
	@Override
	public boolean isPublicFeedbackEnabled() {
		return publicFeedbackEnabled;
	}

	public void setPublicFeedbackEnabled(boolean publicFeedbackEnabled) {
		this.publicFeedbackEnabled = publicFeedbackEnabled;
	}

	@Override
	public Date getPublicFeedbackDeadline() {
		return publicFeedbackDeadline;
	}

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
	public Integer getDecision() {
		return decision;
	}

	@Override
	public void setDecision(Integer decision) {
		this.decision = decision;
	}
	
	@Override
	public String getMemo() {
		return memo;
	}

	@Override
	public String getCommitteeComment() {
		return committeeComment;
	}

	@Override
	public String getApplicantUrl() {
		return applicantUrl;
	}

	@Override
	public Long getPositionKey() {
		return positionKey;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public String getJobAd() {
		return jobAd;
	}

	@Override
	public Person getPerson() {
		if(person == null) {
			person = new PersonImpl();
		}
		return person;
	}

	@Override
	public Address getAddress() {
		if(address == null) {
			address = new AddressImpl();
		}
		return address;
	}

	@Override
	public BusinessAddress getBusinessAddress() {
		if(businessAddress == null) {
			businessAddress = new BusinessAddressImpl();
		}
		return businessAddress;
	}

	@Override
	public BusinessInformations getBusinessInformations() {
		if(businessInformations == null) {
			businessInformations = new BusinessInformationsImpl();
		}
		return businessInformations;
	}

	@Override
	public AcademicalBackground getAcademicalBackground() {
		if(academicalBackground == null) {
			academicalBackground = new AcademicalBackgroundImpl();
		}
		return academicalBackground;
	}
	
	@Override
	public Project getProject() {
		if(project == null) {
			project = new ProjectImpl();
		}
		return project;
	}
	
	@Override
	public String[] getAdditionalValues() {
		return additionalValues;
	}

	public void setAdditionalValues(String[] additionalValues) {
		this.additionalValues = additionalValues;
	}

	@Override
	public String getAdditionalValue(int index) {
		if(additionalValues != null && index >= 0 && index < additionalValues.length) {
			return additionalValues[index];
		}
		return null;
	}

	@Override
	public PositionAttributeDefinitionConfiguration[] getAdditionalTypes() {
		return additionalValuesTypes;
	}

	@Override
	public PositionAttributeDefinitionConfiguration getAdditionalType(int index) {
		if(additionalValuesTypes != null && index >= 0 && index < additionalValuesTypes.length) {
			return additionalValuesTypes[index];
		}
		return null;
	}

	public void setAdditionalValuesTypes(PositionAttributeDefinitionConfiguration[] types) {
		this.additionalValuesTypes = types;
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
		if(obj instanceof ApplicationLightImpl) {
			ApplicationLightImpl app = (ApplicationLightImpl)obj;
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
		sb.append("applicationLight[key=").append(getKey() == null ? "" : getKey()).append(";")
			.append("positionKey=").append(positionKey == null ? "" : positionKey).append(";")
			.append(getPerson().toString())
			.append(getAddress().toString())
			.append(getAcademicalBackground().toString());
		return sb.toString();
	}
}
