/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.basesecurity;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Description: <br>
 * Relation between the security group and identity. The object is almost immutable
 * on the hibernate mapping.
 * 
 * 
 * @author Felix Jost
 */

@Entity
@Table(name="o_bs_membership")
public class SecurityGroupMembershipImpl implements Persistable, CreateInfo, ModifiedInfo {
	
	private static final long serialVersionUID = 2466302280763907357L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	/**
	 * The version is disabled, only for backward compatibility purpose.
	 */
	@Column(name="version", nullable=false, insertable=true, updatable=true)
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="identity_id", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="secgroup_id", nullable=false, insertable=true, updatable=false)
	private SecurityGroup securityGroup;
	
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	/**
	 * @return Identity
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @return SecurityGroup
	 */
	public SecurityGroup getSecurityGroup() {
		return securityGroup;
	}

	/**
	 * Sets the identity. The identity cannot be changed and updated.
	 * The identity is only inserted to the database but never updated
	 * 
	 * @param identity The identity to set
	 */
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	/**
	 * Sets the securityGroup. The security group cannot be changed and
	 * updated. It is only inserted to the dabatase but never updated.
	 * 
	 * @param securityGroup The securityGroup to set
	 */
	public void setSecurityGroup(SecurityGroup securityGroup) {
		this.securityGroup = securityGroup;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 627239 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof SecurityGroupMembershipImpl sec) {
			return getKey().equals(sec.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public String toString() {
		return "securityGroupMembership[key=" + key + "], " + super.toString();
	}
}