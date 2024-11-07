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

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 1 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="bgroupmemberhistory")
@Table(name="o_bs_group_member_history")
public class GroupMembershipHistoryImpl implements GroupMembershipHistory, Persistable {
	
	private static final long serialVersionUID = 742328101941105189L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="g_role", nullable=false, insertable=true, updatable=false)
	private String role;
	
	@Column(name="g_note", nullable=true, insertable=true, updatable=true)
	private String note;
	
	@Column(name="g_admin_note", nullable=true, insertable=true, updatable=true)
	private String adminNote;

	@Enumerated(EnumType.STRING)
	@Column(name="g_status", nullable=false, insertable=true, updatable=false)
	private GroupMembershipStatus status;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_transfer_origin_id", nullable=false, insertable=true, updatable=false)
	private OLATResource transferOrigin;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_transfer_destination_id", nullable=false, insertable=true, updatable=false)
	private OLATResource transferDestination;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_creator_id", nullable=true, insertable=true, updatable=false)
	private Identity creator;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group group;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity_id", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	
	public GroupMembershipHistoryImpl() {
		//
	}

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
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public GroupMembershipStatus getStatus() {
		return status;
	}

	public void setStatus(GroupMembershipStatus status) {
		this.status = status;
	}

	@Override
	public OLATResource getTransferOrigin() {
		return transferOrigin;
	}

	public void setTransferOrigin(OLATResource transferOrigin) {
		this.transferOrigin = transferOrigin;
	}

	@Override
	public OLATResource getTransferDestination() {
		return transferDestination;
	}

	public void setTransferDestination(OLATResource transferDestination) {
		this.transferDestination = transferDestination;
	}

	@Override
	public String getNote() {
		return note;
	}

	@Override
	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String getAdminNote() {
		return adminNote;
	}

	@Override
	public void setAdminNote(String adminNote) {
		this.adminNote = adminNote;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
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
		return getKey() == null ? 6945213 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof GroupMembershipHistoryImpl hist) {
			return getKey().equals(hist.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
