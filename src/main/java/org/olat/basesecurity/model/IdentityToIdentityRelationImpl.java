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

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.IdentityToIdentityRelationManagedFlag;
import org.olat.basesecurity.RelationRole;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="identitytoidentity")
@Table(name="o_bs_identity_to_identity")
public class IdentityToIdentityRelationImpl implements IdentityToIdentityRelation, Persistable {

	private static final long serialVersionUID = -8417977304577506811L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="g_external_id", nullable=true, insertable=true, updatable=false)
	private String externalId;
	@Column(name="g_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_source_id", nullable=false, insertable=true, updatable=false)
	private Identity source;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_target_id", nullable=false, insertable=true, updatable=false)
	private Identity target;
	
	@ManyToOne(targetEntity=RelationRoleImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_role_id", nullable=false, insertable=true, updatable=false)
	private RelationRole role;

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
	public String getExternalId() {
		return externalId;
	}

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
	public IdentityToIdentityRelationManagedFlag[] getManagedFlags() {
		return IdentityToIdentityRelationManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public Identity getSource() {
		return source;
	}

	public void setSource(Identity source) {
		this.source = source;
	}

	@Override
	public Identity getTarget() {
		return target;
	}

	public void setTarget(Identity target) {
		this.target = target;
	}

	@Override
	public RelationRole getRole() {
		return role;
	}

	public void setRole(RelationRole role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 69615671 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof IdentityToIdentityRelationImpl) {
			IdentityToIdentityRelationImpl rel = (IdentityToIdentityRelationImpl)obj;
			return getKey() != null && getKey().equals(rel.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
