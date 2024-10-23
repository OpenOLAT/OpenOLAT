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

import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 21 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="organisationemaildomain")
@Table(name="o_org_email_domain")
public class OrganisationEmailDomainImpl implements Persistable, OrganisationEmailDomain {

	private static final long serialVersionUID = -1176572945044334618L;

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
	
	@Column(name="o_domain", nullable=false, insertable=true, updatable=true)
	private String domain;
	@Column(name="o_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;
	@Column(name="o_subdomains_allowed", nullable=false, insertable=true, updatable=true)
	private boolean subdomainsAllowed;
	
	@ManyToOne(targetEntity=OrganisationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_organisation", nullable=false, insertable=true, updatable=false)
	private Organisation organisation;

	@Override
	public Long getKey() {
		return key;
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
	public String getDomain() {
		return domain;
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
	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public boolean isSubdomainsAllowed() {
		return subdomainsAllowed;
	}

	@Override
	public void setSubdomainsAllowed(boolean subdomainsAllowed) {
		this.subdomainsAllowed = subdomainsAllowed;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	@Override
	public int hashCode() {
		return key == null ? 147514 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrganisationEmailDomainImpl) {
			OrganisationEmailDomainImpl org = (OrganisationEmailDomainImpl)obj;
			return key != null && key.equals(org.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
}
