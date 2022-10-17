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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.OrganisationRoleRight;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="organisationroleright")
@Table(name="o_org_role_to_right")
public class OrganisationRoleRightImpl implements OrganisationRoleRight, Persistable {

	private static final long serialVersionUID = 5176649780644160612L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="o_role", nullable=false, insertable=true, updatable=true)
	@Enumerated(EnumType.STRING)
	private OrganisationRoles role;

	@ManyToOne(targetEntity= OrganisationImpl.class,fetch= FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_organisation", nullable=false, insertable=true, updatable=false)
	private Organisation organisation;

	@Column(name="o_right", nullable=false, insertable=true, updatable=false)
	private String right;

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
	public OrganisationRoles getRole() {
		return role;
	}

	@Override
	public void setRole(OrganisationRoles role) {
		this.role = role;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	@Override
	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	@Override
	public String getRight() {
		return right;
	}

	@Override
	public void setRight(String right) {
		this.right = right;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 1456092 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrganisationRoleRightImpl) {
			OrganisationRoleRightImpl organisationRoleRight = (OrganisationRoleRightImpl)obj;
			return getKey() != null && getKey().equals(organisationRoleRight.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
