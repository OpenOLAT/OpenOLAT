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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;


/**
 * 
 * Initial date: 17.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Embeddable
public class BusinessAddressImpl implements BusinessAddress, Serializable {

	private static final long serialVersionUID = 7512290425394917473L;
	
	@Column(name="biz_addressline1", nullable=true, insertable=true, updatable=true)
	private String addressLine1;
	@Column(name="biz_addressline2", nullable=true, insertable=true, updatable=true)
	private String addressLine2;
	@Column(name="biz_addressline3", nullable=true, insertable=true, updatable=true)
	private String addressLine3;
	@Column(name="biz_zipcode", nullable=true, insertable=true, updatable=true)
	private String zipCode;
	@Column(name="biz_city", nullable=true, insertable=true, updatable=true)
	private String city;
	@Column(name="biz_country", nullable=true, insertable=true, updatable=true)
	private String country;
	@Column(name="biz_phone", nullable=true, insertable=true, updatable=true)
	private String phone;
	@Column(name="biz_mail", nullable=true, insertable=true, updatable=true)
	private String email;
	
	public BusinessAddressImpl() {
		//
	}
	
	@Override
	public Type getType() {
		return Type.BUSINESS;
	}

	@Override
	public void setType(Type type) {
		//
	}

	@Override
	public String getAddressLine1() {
		return addressLine1;
	}

	@Override
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Override
	public String getAddressLine2() {
		return addressLine2;
	}

	@Override
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Override
	public String getAddressLine3() {
		return addressLine3;
	}

	@Override
	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	@Override
	public String getZipCode() {
		return zipCode;
	}

	@Override
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	@Override
	public String getCity() {
		return city;
	}

	@Override
	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public void setCountry(String country) {
		this.country = country;
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
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("address[")
			.append("line1=").append(addressLine1 == null ? "null" : addressLine1).append(";")
			.append("line2=").append(addressLine2 == null ? "null" : addressLine2).append(";")
			.append("line3=").append(addressLine3 == null ? "null" : addressLine3).append(";")
			.append("zipcode=").append(city == null ? "null" : city).append(";")
			.append("city=").append(city == null ? "null" : city).append(";")
			.append("country=").append(country == null ? "null" : country)
			.append("]");
		return sb.toString();
	}
}
