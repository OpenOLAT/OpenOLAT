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
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleManagedFlag;
import org.olat.basesecurity.RelationRoleToRight;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="relationrole")
@Table(name="o_bs_relation_role")
@NamedQuery(name="loadRelationRoleByKey", query="select relRole from relationrole relRole where relRole.key=:roleKey")
@NamedQuery(name="loadRelationRoleByRole", query="select relRole from relationrole relRole where relRole.role=:role")
public class RelationRoleImpl implements RelationRole, Persistable {

	private static final long serialVersionUID = 5176649780644160612L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=false)
	private Date lastModified;

	@Column(name="g_role", nullable=false, insertable=true, updatable=true)
	private String role;
	@Column(name="g_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="g_external_ref", nullable=true, insertable=true, updatable=true)
	private String externalRef;
	@Column(name="g_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;

	@OneToMany(targetEntity=RelationRoleToRightImpl.class, fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="fk_role_id")
	private Set<RelationRoleToRight> rights;

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
	public String getRole() {
		return role;
	}

	@Override
	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getExternalRef() {
		return externalRef;
	}

	@Override
	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public RelationRoleManagedFlag[] getManagedFlags() {
		return RelationRoleManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public void setManagedFlags(RelationRoleManagedFlag[] flags) {
		managedFlagsString = RelationRoleManagedFlag.toString(flags);
	}

	@Override
	public Set<RelationRoleToRight> getRights() {
		if(rights == null) {
			rights = new HashSet<>();
		}
		return rights;
	}

	public void setRights(Set<RelationRoleToRight> rights) {
		this.rights = rights;
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
		if(obj instanceof RelationRoleImpl) {
			RelationRoleImpl relation = (RelationRoleImpl)obj;
			return getKey() != null && getKey().equals(relation.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
