/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.model;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.resource.accesscontrol.BillingAddress;

/**
 * 
 * Initial date: Feb 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TransientBillingAddress implements BillingAddress {

	private String identifier;
	private String nameLine1;
	private String nameLine2;
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String addressLine4;
	private String poBox;
	private String region;
	private String zip;
	private String city;
	private String country;
	private boolean enabled;
	private Organisation organisation;
	private Identity identity;
	
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
	public void setLastModified(Date lastModified) {
		//
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getNameLine1() {
		return nameLine1;
	}

	@Override
	public void setNameLine1(String nameLine1) {
		this.nameLine1 = nameLine1;
	}

	@Override
	public String getNameLine2() {
		return nameLine2;
	}

	@Override
	public void setNameLine2(String nameLine2) {
		this.nameLine2 = nameLine2;
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
	public String getAddressLine4() {
		return addressLine4;
	}

	@Override
	public void setAddressLine4(String addressLine4) {
		this.addressLine4 = addressLine4;
	}

	@Override
	public String getPoBox() {
		return poBox;
	}

	@Override
	public void setPoBox(String poBox) {
		this.poBox = poBox;
	}

	@Override
	public String getRegion() {
		return region;
	}

	@Override
	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public String getZip() {
		return zip;
	}

	@Override
	public void setZip(String zip) {
		this.zip = zip;
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
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

}
