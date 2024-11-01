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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.resource.accesscontrol.BillingAddress;

/**
 * 
 * Initial date: 30 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="acbillingaddress")
@Table(name="o_ac_billing_address")
public class BillingAddressImpl implements Persistable, BillingAddress {
	
	private static final long serialVersionUID = -3936280322256795976L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="a_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="a_name_line_1", nullable=true, insertable=true, updatable=true)
	private String nameLine1;
	@Column(name="a_name_line_2", nullable=true, insertable=true, updatable=true)
	private String nameLine2;
	@Column(name="a_address_line_1", nullable=true, insertable=true, updatable=true)
	private String addressLine1;
	@Column(name="a_address_line_2", nullable=true, insertable=true, updatable=true)
	private String addressLine2;
	@Column(name="a_address_line_3", nullable=true, insertable=true, updatable=true)
	private String addressLine3;
	@Column(name="a_address_line_4", nullable=true, insertable=true, updatable=true)
	private String addressLine4;
	@Column(name="a_pobox", nullable=true, insertable=true, updatable=true)
	private String poBox;
	@Column(name="a_region", nullable=true, insertable=true, updatable=true)
	private String region;
	@Column(name="a_zip", nullable=true, insertable=true, updatable=true)
	private String zip;
	@Column(name="a_city", nullable=true, insertable=true, updatable=true)
	private String city;
	@Column(name="a_country", nullable=true, insertable=true, updatable=true)
	private String country;

	@Column(name="a_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;

	@ManyToOne(targetEntity=OrganisationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_organisation", nullable=true, insertable=true, updatable=false)
	private Organisation organisation;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Identity identity;
	
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

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
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

	@Override
	public int hashCode() {
		return getKey() == null ? 93791 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BillingAddressImpl offer) {
			return getKey() != null && getKey().equals(offer.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}