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
package org.olat.instantMessaging.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.instantMessaging.RosterEntry;

/**
 * 
 * Initial date: 20.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="imrosterentry")
@Table(name="o_im_roster_entry")
@NamedQuery(name="loadIMRosterEntryByIdentityandResource", query="select entry from imrosterentry entry where entry.identityKey=:identityKey and entry.resourceId=:resid and entry.resourceTypeName=:resname")
@NamedQuery(name="loadIMRosterEntryForUpdate", query="select entry from imrosterentry entry where entry.key=:entryKey",
	lockMode=LockModeType.PESSIMISTIC_WRITE)
@NamedQuery(name="loadIMRosterEntry", query="select entry from imrosterentry entry where entry.identityKey=:identityKey and entry.resourceId=:resid and entry.resourceTypeName=:resname")
@NamedQuery(name="loadIMRosterEntryByResourceNo", query="select entry from imrosterentry entry where entry.resourceId=:resid and entry.resourceTypeName=:resname")
public class RosterEntryImpl implements Persistable, RosterEntry {

	private static final long serialVersionUID = -4265724240924748369L;

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

	@Column(name="fk_identity_id", nullable=false, insertable=true, updatable=false)
	private Long identityKey;
	@Column(name="r_nickname", nullable=true, insertable=true, updatable=true)
	private String nickName;
	@Column(name="r_fullname", nullable=true, insertable=true, updatable=true)
	private String fullName;
	@Column(name="r_anonym", nullable=true, insertable=true, updatable=true)
	private boolean anonym;
	@Column(name="r_vip", nullable=true, insertable=true, updatable=false)
	private boolean vip;
	
	@Column(name="r_resname", nullable=false, insertable=true, updatable=false)
	private String resourceTypeName;
	@Column(name="r_resid", nullable=false, insertable=true, updatable=false)
	private Long resourceId;
	@Column(name="r_ressubpath", nullable=true, insertable=true, updatable=false)
	private String resSubPath;
	@Column(name="r_channel", nullable=true, insertable=true, updatable=false)
	private String channel;
	
	@Column(name="r_persistent", nullable=false, insertable=true, updatable=true)
	private boolean persistent;
	@Column(name="r_active", nullable=false, insertable=true, updatable=true)
	private boolean active;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_read_upto", nullable=true, insertable=true, updatable=true)
	private Date lastSeen;

	
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
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public String getNickName() {
		return nickName;
	}

	@Override
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@Override
	public String getFullName() {
		return fullName;
	}

	@Override
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public boolean isVip() {
		return vip;
	}

	@Override
	public void setVip(boolean vip) {
		this.vip = vip;
	}

	@Override
	public boolean isAnonym() {
		return anonym;
	}

	@Override
	public void setAnonym(boolean anonym) {
		this.anonym = anonym;
	}

	@Override
	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	@Override
	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public String getResSubPath() {
		return resSubPath;
	}

	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	@Override
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	@Override
	public int hashCode() {
		return key == null ? 92867 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RosterEntryImpl) {
			RosterEntryImpl entry = (RosterEntryImpl)obj;
			return key != null && key.equals(entry.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {	
		return equals(persistable);
	}
}