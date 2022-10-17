/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.basesecurity.model;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeManagedFlag;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="organisationtype")
@Table(name="o_org_organisation_type")
public class OrganisationTypeImpl implements Persistable, OrganisationType {

	private static final long serialVersionUID = 6040557847374292600L;

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
	
	@Column(name="o_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="o_displayname", nullable=true, insertable=true, updatable=true)
	private String displayName;
	@Column(name="o_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="o_css_class", nullable=true, insertable=true, updatable=true)
	private String cssClass;
	@Column(name="o_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="o_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@OneToMany(targetEntity=OrganisationTypeToTypeImpl.class, fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="fk_type")
	public Set<OrganisationTypeToType> allowedSubTypes;
	
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
	public void setLastModified(Date date) {
		lastModified = date;
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
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getCssClass() {
		return cssClass;
	}

	@Override
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public OrganisationTypeManagedFlag[] getManagedFlags() {
		return OrganisationTypeManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public void setManagedFlags(OrganisationTypeManagedFlag[] flags) {
		managedFlagsString = OrganisationTypeManagedFlag.toString(flags);
	}

	@Override
	public Set<OrganisationTypeToType> getAllowedSubTypes() {
		return allowedSubTypes;
	}

	public void setAllowedSubTypes(Set<OrganisationTypeToType> allowedSubTypes) {
		this.allowedSubTypes = allowedSubTypes;
	}

	@Override
	public int hashCode() {
		return key == null ? 147518 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrganisationTypeImpl) {
			OrganisationTypeImpl org = (OrganisationTypeImpl)obj;
			return key != null && key.equals(org.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(255);
		sb.append("organisationType[key=").append(getKey() == null ? "" : getKey().toString())
		  .append(":displayName=").append(displayName == null ? "" : displayName)
		  .append(":identifier=").append(identifier == null ? "" : identifier)
		  .append("]")
		  .append(super.toString());
		return sb.toString();
	}
}
