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
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Embeddable
public class AddressImpl implements Address, Serializable {

	private static final long serialVersionUID = 1675700604295467121L;

	@Column(name="addr_type", nullable=true, insertable=true, updatable=true)
	private String typeStr;

	@Column(name="addressline1", nullable=true, insertable=true, updatable=true)
	private String addressLine1;
	@Column(name="addressline2", nullable=true, insertable=true, updatable=true)
	private String addressLine2;
	@Column(name="addressline3", nullable=true, insertable=true, updatable=true)
	private String addressLine3;
	@Column(name="zipcode", nullable=true, insertable=true, updatable=true)
	private String zipCode;
	@Column(name="city", nullable=true, insertable=true, updatable=true)
	private String city;
	@Column(name="country", nullable=true, insertable=true, updatable=true)
	private String country;
	
	public AddressImpl() {
		//
	}
	
	@Override
	public Type getType() {
		return Type.toType(typeStr);
	}

	@Override
	public void setType(Type type) {
		typeStr = type == null ? null : type.getType();
	}

	public String getTypeStr() {
		return typeStr;
	}

	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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
