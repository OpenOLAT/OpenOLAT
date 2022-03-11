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
* <p>
*/ 

package org.olat.instantMessaging.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.LockModeType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.instantMessaging.ImPreferences;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="impreferences")
@Table(name="o_im_preferences")
@NamedQuery(name="loadIMRosterStatusByIdentity", query="select msg.rosterDefaultStatus from impreferences msg where msg.identity.key=:identityKey")
@NamedQuery(name="loadIMPreferencesByIdentity", query="select msg from impreferences msg where msg.identity.key=:identityKey")
@NamedQuery(name="loadIMPreferencesForUpdate", query="select msg from impreferences msg where msg.identity.key=:identityKey",
	lockMode=LockModeType.PESSIMISTIC_WRITE)
@NamedQuery(name="countAvailableBuddiesIn", query="select count(msg.identity.key) from impreferences msg where msg.identity.key in(:buddyKeys) and msg.rosterDefaultStatus='available'")
@NamedQuery(name="mapStatusByBuddiesIn", query="select msg.identity.key, msg.rosterDefaultStatus from impreferences msg where msg.identity.key in (:buddyKeys)")
@NamedQuery(name="updateIMPreferencesStatusByIdentity", query="update impreferences set rosterDefaultStatus=:status where identity.key=:identityKey")
@NamedQuery(name="updateIMPreferencesVisibilityByIdentity", query="update impreferences set visibleToOthers=:visible where identity.key=:identityKey")
public class ImPreferencesImpl implements ImPreferences, Persistable, CreateInfo {

	private static final long serialVersionUID = -7269061512818714778L;

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
	
	@OneToOne(targetEntity=IdentityImpl.class, cascade={})
	@JoinColumn(name="fk_from_identity_id", nullable=false, insertable=true, updatable=false)
	private Identity identity;

	@Column(name="visible_to_others", nullable=true, insertable=true, updatable=true)
	private boolean visibleToOthers;

	@Column(name="roster_def_status", nullable=true, insertable=true, updatable=true)
	private String rosterDefaultStatus;

	public ImPreferencesImpl() {
		//
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	/**
	 * 
	 * @return boolean true if this user is visible on the onlinelist to other users
	 */
	public boolean isVisibleToOthers(){
    return visibleToOthers;
	}
	/**
	 * @param isVisible
	 */
	public void setVisibleToOthers(boolean isVisible){
		this.visibleToOthers = isVisible;
	}

	/**
	 * @return the default status 
	 */
	public String getRosterDefaultStatus(){
		return rosterDefaultStatus;
	}
	
	public void setRosterDefaultStatus(String defaultStatus) {
		this.rosterDefaultStatus = defaultStatus;
	}
	
	@Override
	public int hashCode() {
		return key == null ? -23984 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ImPreferencesImpl) {
			ImPreferencesImpl prefs = (ImPreferencesImpl)obj;
			return key != null && key.equals(prefs.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	
}