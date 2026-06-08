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

import java.io.Serializable;
import java.util.Date;

import org.olat.core.util.StringHelper;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Embeddable
public class PersonImpl implements Person, Serializable {

	private static final long serialVersionUID = -7229774038913521642L;
	
	@Column(name="persontitle", nullable=true, insertable=true, updatable=true)
	private String titleInternal;
	@Column(name="gender", nullable=true, insertable=true, updatable=true)
	private String gender;
	@Column(name="marital_status", nullable=true, insertable=true, updatable=true)
	private String maritalStatus;
	@Column(name="disability", nullable=true, insertable=true, updatable=true)
	private Boolean disability;
	@Column(name="firstname", nullable=true, insertable=true, updatable=true)
	private String firstName;
	@Column(name="lastname", nullable=true, insertable=true, updatable=true)
	private String lastName;
	@Temporal(TemporalType.DATE)
	@Column(name="birthday", nullable=true, insertable=true, updatable=true)
	private Date birthday;
	@Column(name="nationality", nullable=true, insertable=true, updatable=true)
	private String nationality;
	@Column(name="addnationalities", nullable=true, insertable=true, updatable=true)
	private String additionalNationalities;
	@Column(name="mail", nullable=true, insertable=true, updatable=true)
	private String email;
	@Column(name="phone", nullable=true, insertable=true, updatable=true)
	private String phone;
	@Column(name="mobile_phone", nullable=true, insertable=true, updatable=true)
	private String mobilePhone;
	@Column(name="academictitle", nullable=true, insertable=true, updatable=true)
	private String academicTitle;
	
	public PersonImpl() {
		//
	}

	@Override
	public String getTitle() {
		String title;
		if(StringHelper.containsNonWhitespace(titleInternal)) {
			int index = titleInternal.indexOf('+');
			if(index >= 0) {
				String first = titleInternal.substring(0, index);
				if(PersonTitle.isTitle(first)) {
					title = first;
				} else {
					String second = titleInternal.substring(index+1);
					if(PersonTitle.isTitle(second)) {
						title = second;
					} else {
						title = "";
					}
				}
			} else {
				title = titleInternal;
			}
		} else {
			title = "";
		}
		return title;
	}

	@Override
	public void setTitle(String title) {
		titleInternal = title;
	}

	public String getTitleInternal() {
		return titleInternal;
	}

	public void setTitleInternal(String titleInternal) {
		this.titleInternal = titleInternal;
	}

	@Override
	public String getGender() {
		return gender;
	}

	@Override
	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public String getMaritalStatus() {
		return maritalStatus;
	}

	@Override
	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	@Override
	public Boolean getDisability() {
		return disability;
	}

	@Override
	public void setDisability(Boolean disability) {
		this.disability = disability;
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
	public Date getBirthday() {
		return birthday;
	}

	@Override
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@Override
	public String getNationality() {
		return nationality;
	}

	@Override
	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	@Override
	public String getAdditionalNationalities() {
		return additionalNationalities;
	}

	@Override
	public void setAdditionalNationalities(String additionalNationalities) {
		this.additionalNationalities = additionalNationalities;
	}

	@Override
	public String getMail() {
		return email;
	}

	@Override
	public void setMail(String email) {
		this.email = email;
	}

	@Override
	public String getPhone() {
		return phone;
	}

	@Override
	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String getMobilePhone() {
		return mobilePhone;
	}

	@Override
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	@Override
	public String getAcademicTitle() {
		return academicTitle;
	}

	@Override
	public void setAcademicTitle(String academicTitle) {
		this.academicTitle = academicTitle;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("person[")
		.append("title=").append(titleInternal == null ? "null" : titleInternal).append(";")
		.append("gender=").append(gender == null ? "null" : gender).append(";")
		.append("firstName=").append(firstName == null ? "null" : firstName).append(";")
		.append("lastName=").append(lastName == null ? "null" : lastName).append(";")
		.append("birthday=").append(birthday == null ? "null" : birthday).append(";")
		.append("nationality=").append(nationality == null ? "null" : nationality).append(";")
		.append("email=").append(email == null ? "null" : email).append(";")
		.append("phone=").append(phone == null ? "null" : phone)
		.append("]");
		return sb.toString();
	}
}
