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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="bgroupmember")
@Table(name="o_bs_group_member")
@NamedQuery(name="membershipsByGroup", query="select membership from bgroupmember as membership where membership.group.key=:groupKey")
@NamedQuery(name="membershipsByGroupAndRole", query="select membership from bgroupmember as membership where membership.group.key=:groupKey and membership.role=:role")
@NamedQuery(name="membershipByGroupIdentityAndRole", query="select membership from bgroupmember as membership where membership.group.key=:groupKey and membership.identity.key=:identityKey and membership.role=:role")
@NamedQuery(name="deleteMembershipsByGroupAndRole", query="delete from bgroupmember as membership where membership.group.key=:groupKey and membership.role=:role")
@NamedQuery(name="membershipsByGroupAndIdentity", query="select membership from bgroupmember as membership where membership.group.key=:groupKey and membership.identity.key=:identityKey")
@NamedQuery(name="membershipsByGroupIdentityAndRole", query="select membership from bgroupmember as membership where membership.group.key=:groupKey and membership.identity.key=:identityKey and membership.role=:role")
@NamedQuery(name="countMembersByGroup", query="select count(membership.key) from bgroupmember as membership where membership.group.key=:groupKey")
@NamedQuery(name="countMembersByGroupAndRole", query="select count(membership.key) from bgroupmember as membership where membership.group.key=:groupKey and membership.role=:role")
@NamedQuery(name="membersByGroupAndRole", query="select distinct membership.identity from bgroupmember as membership where membership.group.key=:groupKey and membership.role=:role")
@NamedQuery(name="membersByGroupsAndRole", query="select distinct membership.identity from bgroupmember as membership where membership.group.key in(:groupKeys) and membership.role=:role")
@NamedQuery(name="hasRoleByGroupIdentityAndRole", query="select count(membership.key) from bgroupmember as membership where membership.group.key=:groupKey and membership.identity.key=:identityKey and membership.role=:role")
public class GroupMembershipImpl implements GroupMembership, ModifiedInfo, Persistable {

	private static final long serialVersionUID = -194666973136469187L;

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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="g_role", nullable=false, insertable=true, updatable=false)
	private String role;
	
	@Column(name="g_inheritance_mode", nullable=false, insertable=true, updatable=true)
	private String inheritanceModeString;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group group;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity_id", nullable=false, insertable=true, updatable=false)
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
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getInheritanceModeString() {
		return inheritanceModeString;
	}

	public void setInheritanceModeString(String inheritanceModeString) {
		this.inheritanceModeString = inheritanceModeString;
	}

	@Override
	public GroupMembershipInheritance getInheritanceMode() {
		return StringHelper.containsNonWhitespace(inheritanceModeString)
				? GroupMembershipInheritance.valueOf(inheritanceModeString) : GroupMembershipInheritance.none;
	}

	@Override
	public void setInheritanceMode(GroupMembershipInheritance mode) {
		inheritanceModeString = mode == null ? GroupMembershipInheritance.none.name() : mode.name(); 
	}

	@Override
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
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
		return key == null ? 6945213 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof GroupMembershipImpl) {
			GroupMembershipImpl other = (GroupMembershipImpl)obj;
			return getKey().equals(other.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
