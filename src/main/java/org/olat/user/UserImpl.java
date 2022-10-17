/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.user;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description: 
 * <p>
 * The user represents an known OLAT user. A user can log into OLAT
 * and user the system.
 * <p>
 * The user properties are wrapped in UserPropertyHandler objects that wrap the logic needed
 * to display and edit those properties in forms. Use the UserManager to access
 * those wrappers. For your convinience you can set and get user field values
 * directly from this user implementation, you don't have to use the manager for
 * this.
 * <p>
 * Note that setting any values on the user object does not persist anything.
 * Whenever a field is modified use the UserManager to save the object.
 * <p>
 * @author Florian Gnägi
 */
@Entity
@Table(name="o_user")
public class UserImpl implements User {

	private static final long serialVersionUID = -2872102058369727753L;
	private static final Logger log = Tracing.createLoggerFor(UserImpl.class);

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
	@Column(name="user_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@OneToOne(targetEntity=IdentityImpl.class, optional=true, fetch=FetchType.LAZY)
	@JoinColumn (name="fk_identity")
	private Identity identity;
	
	@Transient
	private boolean webdav;

	@Column(name="u_firstname", nullable=true, insertable=true, updatable=true)
	private String firstName;
	@Column(name="u_lastname", nullable=true, insertable=true, updatable=true)
	private String lastName;
	@Column(name="u_email", nullable=true, insertable=true, updatable=true)
	private String email;
	@Column(name="u_privateemail", nullable=true, insertable=true, updatable=true)
	private String privateEmail;
	@Column(name="u_nickname", nullable=true, insertable=true, updatable=true)
	private String nickName;
	
	@Column(name="u_gender", nullable=true, insertable=true, updatable=true)
	private String gender;
	@Column(name="u_birthday", nullable=true, insertable=true, updatable=true)
	private String birthDay;
	@Column(name="u_socialsecuritynumber", nullable=true, insertable=true, updatable=true)
	private String socialSecurityNumber;
	@Column(name="u_userinterests", nullable=true, insertable=true, updatable=true)
	private String userInterests;
	@Column(name="u_usersearchedinterests", nullable=true, insertable=true, updatable=true)
	private String userSearchedInterests;
	
	@Column(name="u_telprivate", nullable=true, insertable=true, updatable=true)
	private String telPrivate;
	@Column(name="u_telmobile", nullable=true, insertable=true, updatable=true)
	private String telMobile;
	@Column(name="u_smstelmobile", nullable=true, insertable=true, updatable=true)
	private String smsTelMobile;
	@Column(name="u_skype", nullable=true, insertable=true, updatable=true)
	private String skype;
	@Column(name="u_xing", nullable=true, insertable=true, updatable=true)
	private String xing;
	@Column(name="u_linkedin", nullable=true, insertable=true, updatable=true)
	private String linkedin;
	@Column(name="u_icq", nullable=true, insertable=true, updatable=true)
	private String icq;
	
	@Column(name="u_homepage", nullable=true, insertable=true, updatable=true)
	private String homepage;
	@Column(name="u_street", nullable=true, insertable=true, updatable=true)
	private String street;
	@Column(name="u_extendedaddress", nullable=true, insertable=true, updatable=true)
	private String extendedAddress;
	@Column(name="u_pobox", nullable=true, insertable=true, updatable=true)
	private String poBox;
	@Column(name="u_zipcode", nullable=true, insertable=true, updatable=true)
	private String zipCode;
	@Column(name="u_region", nullable=true, insertable=true, updatable=true)
	private String region;
	@Column(name="u_city", nullable=true, insertable=true, updatable=true)
	private String city;
	@Column(name="u_country", nullable=true, insertable=true, updatable=true)
	private String country;
	@Column(name="u_countrycode", nullable=true, insertable=true, updatable=true)
	private String countryCode;
	
	@Column(name="u_degree", nullable=true, insertable=true, updatable=true)
	private String degree;
	@Column(name="u_graduation", nullable=true, insertable=true, updatable=true)
	private String graduation;
	@Column(name="u_studysubject", nullable=true, insertable=true, updatable=true)
	private String studySubject;
	@Column(name="u_institutionalname", nullable=true, insertable=true, updatable=true)
	private String institutionalName;
	@Column(name="u_institutionaluseridentifier", nullable=true, insertable=true, updatable=true)
	private String institutionalUserIdentifier;
	@Column(name="u_institutionalemail", nullable=true, insertable=true, updatable=true)
	private String institutionalEmail;

	@Column(name="u_emchangekey", nullable=true, insertable=true, updatable=true)
	private String emchangeKey;
	@Column(name="u_emaildisabled", nullable=true, insertable=true, updatable=true)
	private String emailDisabled;
	
	@Column(name="u_teloffice", nullable=true, insertable=true, updatable=true)
	private String telOffice;
	@Column(name="u_officestreet", nullable=true, insertable=true, updatable=true)
	private String officeStreet;
	@Column(name="u_extendedofficeaddress", nullable=true, insertable=true, updatable=true)
	private String extendedOfficeAddress;
	@Column(name="u_officepobox", nullable=true, insertable=true, updatable=true)
	private String officePoBox;
	@Column(name="u_officezipcode", nullable=true, insertable=true, updatable=true)
	private String officeZipCode;
	@Column(name="u_officecity", nullable=true, insertable=true, updatable=true)
	private String officeCity;
	@Column(name="u_officecountry", nullable=true, insertable=true, updatable=true)
	private String officeCountry;
	@Column(name="u_officemobilephone", nullable=true, insertable=true, updatable=true)
	private String officeMobilePhone;
	
	@Column(name="u_rank", nullable=true, insertable=true, updatable=true)
	private String rank;
	@Column(name="u_position", nullable=true, insertable=true, updatable=true)
	private String position;
	@Column(name="u_typeofuser", nullable=true, insertable=true, updatable=true)
	private String typeOfUser;
	@Column(name="u_orgunit", nullable=true, insertable=true, updatable=true)
	private String orgUnit;
	@Column(name="u_department", nullable=true, insertable=true, updatable=true)
	private String department;
	@Column(name="u_employeenumber", nullable=true, insertable=true, updatable=true)
	private String employeeNumber;
	@Column(name="u_organizationalunit", nullable=true, insertable=true, updatable=true)
	private String organizationalUnit;
	
	@Column(name="u_genericselectionproperty", nullable=true, insertable=true, updatable=true)
	private String genericSelectionProperty;
	@Column(name="u_genericselectionproperty2", nullable=true, insertable=true, updatable=true)
	private String genericSelectionProperty2;
	@Column(name="u_genericselectionproperty3", nullable=true, insertable=true, updatable=true)
	private String genericSelectionProperty3; // delete
	
	@Column(name="u_generictextproperty", nullable=true, insertable=true, updatable=true)
	private String genericTextProperty;
	@Column(name="u_generictextproperty2", nullable=true, insertable=true, updatable=true)
	private String genericTextProperty2;
	@Column(name="u_generictextproperty3", nullable=true, insertable=true, updatable=true)
	private String genericTextProperty3;
	@Column(name="u_generictextproperty4", nullable=true, insertable=true, updatable=true)
	private String genericTextProperty4;
	@Column(name="u_generictextproperty5", nullable=true, insertable=true, updatable=true)
	private String genericTextProperty5;
	
	@Column(name="u_genericuniquetextproperty", nullable=true, insertable=true, updatable=true)
	private String genericUniqueTextProperty;
	@Column(name="u_genericuniquetextproperty2", nullable=true, insertable=true, updatable=true)
	private String genericUniqueTextProperty2; // delete
	@Column(name="u_genericuniquetextproperty3", nullable=true, insertable=true, updatable=true)
	private String genericUniqueTextProperty3; // delete
	
	@Column(name="u_genericemailproperty1", nullable=true, insertable=true, updatable=true)
	private String genericEmailProperty1;
	
	@Column(name="u_genericcheckboxproperty", nullable=true, insertable=true, updatable=true)
	private String genericCheckboxProperty;
	@Column(name="u_genericcheckboxproperty2", nullable=true, insertable=true, updatable=true)
	private String genericCheckboxProperty2; // delete
	@Column(name="u_genericcheckboxproperty3", nullable=true, insertable=true, updatable=true)
	private String genericCheckboxProperty3; // delete
	
	@Column(name="u_edupersonaffiliation", nullable=true, insertable=true, updatable=true)
	private String eduPersonAffiliation;
	@Column(name="u_swissedupersonhomeorg", nullable=true, insertable=true, updatable=true)
	private String swissEduPersonHomeOrganization;
	@Column(name="u_swissedupersonstudylevel", nullable=true, insertable=true, updatable=true)
	private String swissEduPersonStudyLevel;
	@Column(name="u_swissedupersonhomeorgtype", nullable=true, insertable=true, updatable=true)
	private String swissEduPersonHomeOrganizationType;
	@Column(name="u_swissedupersonstaffcategory", nullable=true, insertable=true, updatable=true)
	private String swissEduPersonStaffCategory;
	@Column(name="u_swissedupersonstudybranch1", nullable=true, insertable=true, updatable=true)
	private String swissEduPersonStudyBranch1;
	@Column(name="u_swissedupersonstudybranch2", nullable=true, insertable=true, updatable=true)
	private String swissEduPersonStudyBranch2;
	@Column(name="u_swissedupersonstudybranch3", nullable=true, insertable=true, updatable=true)
	private String swissEduPersonStudyBranch3;

	@Embedded
	private PreferencesImpl preferences;
	
	// the volatile attributes which are set during log in
	// but must be made available to the usertracking LoggingObject
	@Transient
	private Map<String, String> identEnvAttribs;
	
	public UserImpl() {
		//
	}


	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Override
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@Override
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String getInstitutionalEmail() {
		return institutionalEmail;
	}
	
	public void setInstitutionalEmail(String institutionalEmail) {
		this.institutionalEmail = institutionalEmail;
	}
	
	@Override
	public String getSmsTelMobile() {
		return smsTelMobile;
	}

	public void setSmsTelMobile(String smsTelMobile) {
		this.smsTelMobile = smsTelMobile;
	}

	public boolean isWebdav() {
		return webdav;
	}

	public void setWebdav(boolean isWebdav) {
		this.webdav = isWebdav;
	}

	@Override
	public Preferences getPreferences() {
		if(preferences == null) {
			preferences = new PreferencesImpl();
		}
		return preferences;	
	}
	
	@Override
	public void setPreferences(Preferences prefs){
		this.preferences = (PreferencesImpl)prefs;	
	}
	
	@Override
	public String getProperty(String name) {
		switch(name) {
			case UserConstants.FIRSTNAME: return firstName;
			case UserConstants.LASTNAME: return lastName;
			case UserConstants.EMAIL: return email;
			case UserConstants.NICKNAME: return nickName;
			case "birthDay": return birthDay;
			case "graduation": return graduation;
			case "gender": return gender;
			case "telPrivate": return telPrivate;
			case "telMobile": return telMobile;
			case "smsTelMobile": return smsTelMobile;
			case "telOffice": return telOffice;
			case "skype": return skype;
			case "xing": return xing;
			case "linkedin": return linkedin;
			case "icq": return icq;
			case "homepage": return homepage;
			case "street": return street;
			case "extendedAddress": return extendedAddress;
			case "poBox": return poBox;
			case "zipCode": return zipCode;
			case "region": return region;
			case "city": return city;
			case "country": return country;
			case "countryCode": return countryCode;
			case "institutionalName": return institutionalName;
			case "institutionalUserIdentifier": return institutionalUserIdentifier;
			case "institutionalEmail": return institutionalEmail;
			case "orgUnit": return orgUnit;
			case "studySubject": return studySubject;
			case "emchangeKey": return emchangeKey;
			case "emailDisabled": return emailDisabled;
			case "typeOfUser": return typeOfUser;
			case "socialSecurityNumber": return socialSecurityNumber;
			case "genericSelectionProperty": return genericSelectionProperty;
			case "genericSelectionProperty2": return genericSelectionProperty2;
			case "genericSelectionProperty3": return genericSelectionProperty3;
			case "genericTextProperty": return genericTextProperty;
			case "genericTextProperty2": return genericTextProperty2;
			case "genericTextProperty3": return genericTextProperty3;
			case "genericTextProperty4": return genericTextProperty4;
			case "genericTextProperty5": return genericTextProperty5;
			case "genericUniqueTextProperty": return genericUniqueTextProperty;
			case "genericUniqueTextProperty2": return genericUniqueTextProperty2;
			case "genericUniqueTextProperty3": return genericUniqueTextProperty3;
			case "genericEmailProperty1": return genericEmailProperty1;
			case "genericCheckboxProperty": return genericCheckboxProperty;
			case "genericCheckboxProperty2": return genericCheckboxProperty2;
			case "genericCheckboxProperty3": return genericCheckboxProperty3;
			case "rank": return rank;
			case "degree": return degree;
			case "position": return position;
			case "userInterests": return userInterests;
			case "officeStreet": return officeStreet;
			case "extendedOfficeAddress": return extendedOfficeAddress;
			case "officePoBox": return officePoBox;
			case "officeZipCode": return officeZipCode;
			case "officeCity": return officeCity;
			case "officeCountry": return officeCountry;
			case "officeMobilePhone": return officeMobilePhone;
			case "department": return department;
			case "privateEmail": return privateEmail;
			case "eduPersonAffiliation": return eduPersonAffiliation;
			case "swissEduPersonHomeOrganization": return swissEduPersonHomeOrganization;
			case "swissEduPersonStudyLevel": return swissEduPersonStudyLevel;
			case "swissEduPersonHomeOrganizationType": return swissEduPersonHomeOrganizationType;
			case "employeeNumber": return employeeNumber;
			case "swissEduPersonStaffCategory": return swissEduPersonStaffCategory;
			case "organizationalUnit": return organizationalUnit;
			case "swissEduPersonStudyBranch1": return swissEduPersonStudyBranch1;
			case "swissEduPersonStudyBranch2": return swissEduPersonStudyBranch2;
			case "swissEduPersonStudyBranch3": return swissEduPersonStudyBranch3;
			default: return null;
		}
	}
	
	public void setUserProperty(String name, String value) {
		switch(name) {
			case UserConstants.FIRSTNAME: firstName = value; break;
			case UserConstants.LASTNAME: lastName = value; break;
			case UserConstants.EMAIL: email = value; break;
			case UserConstants.NICKNAME: nickName = value; break;
			case "birthDay": birthDay = value; break;
			case "graduation": graduation = value; break;
			case "gender": gender = value; break;
			case "telPrivate": telPrivate = value; break;
			case "telMobile": telMobile = value; break;
			case "smsTelMobile": smsTelMobile = value; break;
			case "telOffice": telOffice = value; break;
			case "skype": skype = value; break;
			case "xing": xing = value; break;
			case "linkedin": linkedin = value; break;
			case "icq": icq = value; break;
			case "homepage": homepage = value; break;
			case "street": street = value; break;
			case "extendedAddress": extendedAddress = value; break;
			case "poBox": poBox = value; break;
			case "zipCode": zipCode = value; break;
			case "region": region = value; break;
			case "city": city = value; break;
			case "country": country = value; break;
			case "countryCode": countryCode = value; break;
			case "institutionalName": institutionalName = value; break;
			case "institutionalUserIdentifier": institutionalUserIdentifier = value; break;
			case "institutionalEmail": institutionalEmail = value; break;
			case "orgUnit": orgUnit = value; break;
			case "studySubject": studySubject = value; break;
			case "emchangeKey": emchangeKey = value; break;
			case "emailDisabled": emailDisabled = value; break;
			case "typeOfUser": typeOfUser = value; break;
			case "socialSecurityNumber": socialSecurityNumber = value; break;
			case "genericSelectionProperty": genericSelectionProperty = value; break;
			case "genericSelectionProperty2": genericSelectionProperty2 = value; break;
			case "genericSelectionProperty3": genericSelectionProperty3 = value; break;
			case "genericTextProperty": genericTextProperty = value; break;
			case "genericTextProperty2": genericTextProperty2 = value; break;
			case "genericTextProperty3": genericTextProperty3 = value; break;
			case "genericTextProperty4": genericTextProperty4 = value; break;
			case "genericTextProperty5": genericTextProperty5 = value; break;
			case "genericUniqueTextProperty": genericUniqueTextProperty = value; break;
			case "genericUniqueTextProperty2": genericUniqueTextProperty2 = value; break;
			case "genericUniqueTextProperty3": genericUniqueTextProperty3 = value; break;
			case "genericEmailProperty1": genericEmailProperty1 = value; break;
			case "genericCheckboxProperty": genericCheckboxProperty = value; break;
			case "genericCheckboxProperty2": genericCheckboxProperty2 = value; break;
			case "genericCheckboxProperty3": genericCheckboxProperty3 = value; break;
			case "rank": rank = value; break;
			case "degree": degree = value; break;
			case "position": position = value; break;
			case "userInterests": userInterests = value; break;
			case "officeStreet": officeStreet = value; break;
			case "extendedOfficeAddress": extendedOfficeAddress = value; break;
			case "officePoBox": officePoBox = value; break;
			case "officeZipCode": officeZipCode = value; break;
			case "officeCity": officeCity = value; break;
			case "officeCountry": officeCountry = value; break;
			case "officeMobilePhone": officeMobilePhone = value; break;
			case "department": department = value; break;
			case "privateEmail": privateEmail = value; break;
			case "eduPersonAffiliation": eduPersonAffiliation = value; break;
			case "swissEduPersonHomeOrganization": swissEduPersonHomeOrganization = value; break;
			case "swissEduPersonStudyLevel": swissEduPersonStudyLevel = value; break;
			case "swissEduPersonHomeOrganizationType": swissEduPersonHomeOrganizationType = value; break;
			case "employeeNumber": employeeNumber = value; break;
			case "swissEduPersonStaffCategory": swissEduPersonStaffCategory = value; break;
			case "organizationalUnit": organizationalUnit = value; break;
			case "swissEduPersonStudyBranch1": swissEduPersonStudyBranch1 = value; break;
			case "swissEduPersonStudyBranch2": swissEduPersonStudyBranch2 = value; break;
			case "swissEduPersonStudyBranch3": swissEduPersonStudyBranch3 = value; break;
		}
	}

	/**
	 * Two users are equal if their key is equal.
	 * @param obj
	 * @return true if users are the same
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		// object must be UserImpl at this point
		UserImpl user = (UserImpl)obj;
		return getKey() != null && getKey().equals(user.getKey());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash;
		hash = 31 * hash + (null == this.getKey() ? 0 : this.getKey().hashCode());
		return hash;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	/**
	 * Returns the users firstname, lastname, email and database key.
	 * @return String user info
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("UserImpl(").append(getKey() == null ? "NULL" :  getKey()).append("[")
		  .append(firstName == null ? "NULL" : firstName).append(" ")
		  .append(lastName == null ? "NULL" : lastName).append(" ")
		  .append(email == null ? "NULL" : email).append("]")
		  .append(super.toString());
		return sb.toString();
	}

	@Override
	public String getProperty(String propertyName, Locale locale) {
		UserManager um = UserManager.getInstance();
		UserPropertyHandler propertyHandler = um.getUserPropertiesConfig().getPropertyHandler(propertyName);
		if (propertyHandler == null)
			return null;
		return propertyHandler.getUserProperty(this, locale);
	}

	@Override
	public void setProperty(String propertyName, String propertyValue) {
		UserManager um = UserManager.getInstance();
		UserPropertyHandler propertyHandler = um.getUserPropertiesConfig().getPropertyHandler(propertyName);
		if(propertyHandler == null) {
			log.error("Try to set unkown property: {} for user: {}", propertyName, getKey());
		} else {
			propertyHandler.setUserProperty(this, propertyValue);
		}
	}

	@Override
	public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {
		this.identEnvAttribs = identEnvAttribs;
	}

	@Override
	public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale){
		String retVal = getProperty(propertyName, locale);
		if(retVal == null && identEnvAttribs != null){
			retVal = identEnvAttribs.get(propertyName);
		}
		return retVal;
	}
}