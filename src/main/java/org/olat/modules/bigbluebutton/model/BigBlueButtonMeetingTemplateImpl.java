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
package org.olat.modules.bigbluebutton.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.GuestPolicyEnum;
import org.olat.modules.bigbluebutton.JoinPolicyEnum;

/**
 * 
 * Initial date: 19 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="bigbluebuttontemplate")
@Table(name="o_bbb_template")
public class BigBlueButtonMeetingTemplateImpl implements Persistable, BigBlueButtonMeetingTemplate {

	private static final long serialVersionUID = 5348654669384123670L;

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
	
	@Column(name="b_name", nullable=false, insertable=true, updatable=true)
	private String name;
	@Column(name="b_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="b_system", nullable=false, insertable=true, updatable=false)
	private boolean system;
	@Column(name="b_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;
	@Column(name="b_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	
	@Column(name="b_max_concurrent_meetings", nullable=true, insertable=true, updatable=true)
	private Integer maxConcurrentMeetings;

	@Column(name="b_external_users", nullable=false, insertable=true, updatable=true)
	private boolean externalAllowed;
	
	@Column(name="b_max_participants", nullable=true, insertable=true, updatable=true)
	private Integer maxParticipants;
	@Column(name="b_max_duration", nullable=true, insertable=true, updatable=true)
	private Integer maxDuration;
	
	@Column(name="b_mute_on_start", nullable=true, insertable=true, updatable=true)
	private Boolean muteOnStart;
	
	@Column(name="b_record", nullable=true, insertable=true, updatable=true)
	private Boolean record;
	@Column(name="b_breakout", nullable=true, insertable=true, updatable=true)
	private Boolean breakoutRoomsEnabled;
	@Column(name="b_auto_start_recording", nullable=true, insertable=true, updatable=true)
	private Boolean autoStartRecording;
	@Column(name="b_allow_start_stop_recording", nullable=true, insertable=true, updatable=true)
	private Boolean allowStartStopRecording;
	@Column(name="b_webcams_only_for_moderator", nullable=true, insertable=true, updatable=true)
	private Boolean webcamsOnlyForModerator;
	@Column(name="b_allow_mods_to_unmute_users", nullable=true, insertable=true, updatable=true)
	private Boolean allowModsToUnmuteUsers;
	
	@Column(name="b_lock_set_disable_cam", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsDisableCam;
	@Column(name="b_lock_set_disable_mic", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsDisableMic;
	@Column(name="b_lock_set_disable_priv_chat", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsDisablePrivateChat;
	@Column(name="b_lock_set_disable_public_chat", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsDisablePublicChat;
	@Column(name="b_lock_set_disable_note", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsDisableNote;
	@Column(name="b_lock_set_locked_layout", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsLockedLayout;
	
	@Column(name="b_lock_set_hide_user_list", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsHideUserList;
	@Column(name="b_lock_set_lock_on_join", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsLockOnJoin;
	@Column(name="b_lock_set_lock_on_join_conf", nullable=true, insertable=true, updatable=true)
	private Boolean lockSettingsLockOnJoinConfigurable;
	
	@Column(name="b_join_policy", nullable=false, insertable=true, updatable=true)
	private String joinPolicy;
	
	@Column(name="b_permitted_roles", nullable=true, insertable=true, updatable=true)
	private String permittedRoles;

	@Column(name="b_guest_policy", nullable=true, insertable=true, updatable=true)
	private String guestPolicy;
	
	
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
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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
	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
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
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public boolean isExternalUsersAllowed() {
		return externalAllowed;
	}

	@Override
	public void setExternalUsersAllowed(boolean externalAllowed) {
		this.externalAllowed = externalAllowed;
	}

	@Override
	public Integer getMaxConcurrentMeetings() {
		return maxConcurrentMeetings;
	}

	@Override
	public void setMaxConcurrentMeetings(Integer maxConcurrentMeetings) {
		this.maxConcurrentMeetings = maxConcurrentMeetings;
	}

	@Override
	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	@Override
	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	@Override
	public Integer getMaxDuration() {
		return maxDuration;
	}

	@Override
	public void setMaxDuration(Integer maxDuration) {
		this.maxDuration = maxDuration;
	}

	@Override
	public Boolean getRecord() {
		return record;
	}

	@Override
	public void setRecord(Boolean record) {
		this.record = record;
	}

	@Override
	public Boolean getBreakoutRoomsEnabled() {
		return breakoutRoomsEnabled;
	}

	@Override
	public void setBreakoutRoomsEnabled(Boolean breakoutRoomsEnabled) {
		this.breakoutRoomsEnabled = breakoutRoomsEnabled;
	}

	@Override
	public Boolean getMuteOnStart() {
		return muteOnStart;
	}

	@Override
	public void setMuteOnStart(Boolean muteOnStart) {
		this.muteOnStart = muteOnStart;
	}

	@Override
	public Boolean getAutoStartRecording() {
		return autoStartRecording;
	}

	@Override
	public void setAutoStartRecording(Boolean autoStartRecording) {
		this.autoStartRecording = autoStartRecording;
	}

	@Override
	public Boolean getAllowStartStopRecording() {
		return allowStartStopRecording;
	}

	@Override
	public void setAllowStartStopRecording(Boolean allowStartStopRecording) {
		this.allowStartStopRecording = allowStartStopRecording;
	}

	@Override
	public Boolean getWebcamsOnlyForModerator() {
		return webcamsOnlyForModerator;
	}

	@Override
	public void setWebcamsOnlyForModerator(Boolean webcamsOnlyForModerator) {
		this.webcamsOnlyForModerator = webcamsOnlyForModerator;
	}

	@Override
	public Boolean getAllowModsToUnmuteUsers() {
		return allowModsToUnmuteUsers;
	}

	@Override
	public void setAllowModsToUnmuteUsers(Boolean allowModsToUnmuteUsers) {
		this.allowModsToUnmuteUsers = allowModsToUnmuteUsers;
	}

	@Override
	public Boolean getLockSettingsDisableCam() {
		return lockSettingsDisableCam;
	}

	@Override
	public void setLockSettingsDisableCam(Boolean lockSettingsDisableCam) {
		this.lockSettingsDisableCam = lockSettingsDisableCam;
	}

	@Override
	public Boolean getLockSettingsDisableMic() {
		return lockSettingsDisableMic;
	}

	@Override
	public void setLockSettingsDisableMic(Boolean lockSettingsDisableMic) {
		this.lockSettingsDisableMic = lockSettingsDisableMic;
	}

	@Override
	public Boolean getLockSettingsDisablePrivateChat() {
		return lockSettingsDisablePrivateChat;
	}

	@Override
	public void setLockSettingsDisablePrivateChat(Boolean lockSettingsDisablePrivateChat) {
		this.lockSettingsDisablePrivateChat = lockSettingsDisablePrivateChat;
	}

	@Override
	public Boolean getLockSettingsDisablePublicChat() {
		return lockSettingsDisablePublicChat;
	}

	@Override
	public void setLockSettingsDisablePublicChat(Boolean lockSettingsDisablePublicChat) {
		this.lockSettingsDisablePublicChat = lockSettingsDisablePublicChat;
	}

	@Override
	public Boolean getLockSettingsDisableNote() {
		return lockSettingsDisableNote;
	}

	@Override
	public void setLockSettingsDisableNote(Boolean lockSettingsDisableNote) {
		this.lockSettingsDisableNote = lockSettingsDisableNote;
	}

	@Override
	public Boolean getLockSettingsLockedLayout() {
		return lockSettingsLockedLayout;
	}

	@Override
	public void setLockSettingsLockedLayout(Boolean lockSettingsLockedLayout) {
		this.lockSettingsLockedLayout = lockSettingsLockedLayout;
	}
	
	@Override
	public Boolean getLockSettingsHideUserList() {
		return lockSettingsHideUserList;
	}

	@Override
	public void setLockSettingsHideUserList(Boolean lockSettingsHideUserList) {
		this.lockSettingsHideUserList = lockSettingsHideUserList;
	}

	@Override
	public Boolean getLockSettingsLockOnJoin() {
		return lockSettingsLockOnJoin;
	}

	@Override
	public void setLockSettingsLockOnJoin(Boolean lockSettingsLockOnJoin) {
		this.lockSettingsLockOnJoin = lockSettingsLockOnJoin;
	}

	@Override
	public Boolean getLockSettingsLockOnJoinConfigurable() {
		return lockSettingsLockOnJoinConfigurable;
	}

	@Override
	public void setLockSettingsLockOnJoinConfigurable(Boolean lockSettingsLockOnJoinConfigurable) {
		this.lockSettingsLockOnJoinConfigurable = lockSettingsLockOnJoinConfigurable;
	}

	public String getJoinPolicy() {
		return joinPolicy;
	}

	public void setJoinPolicy(String joinPolicy) {
		this.joinPolicy = joinPolicy;
	}

	@Override
	public JoinPolicyEnum getJoinPolicyEnum() {
		if(StringHelper.containsNonWhitespace(joinPolicy)) {
			return JoinPolicyEnum.valueOf(joinPolicy);
		}
		return null;
	}

	@Override
	public void setJoinPolicyEnum(JoinPolicyEnum policy) {
		if(policy == null) {
			setJoinPolicy(null);
		} else {
			setJoinPolicy(policy.name());
		}
	}

	public String getPermittedRoles() {
		return permittedRoles;
	}

	public void setPermittedRoles(String permittedRoles) {
		this.permittedRoles = permittedRoles;
	}

	@Transient
	@Override
	public List<BigBlueButtonTemplatePermissions> getPermissions() {
		return BigBlueButtonTemplatePermissions.toList(permittedRoles);
	}

	@Override
	public void setPermissions(List<BigBlueButtonTemplatePermissions> roles) {
		if(roles == null || roles.isEmpty()) {
			setPermittedRoles(null);
		} else {
			setPermittedRoles(BigBlueButtonTemplatePermissions.toString(roles));
		}
	}

	public String getGuestPolicy() {
		return guestPolicy;
	}

	public void setGuestPolicy(String guestPolicy) {
		this.guestPolicy = guestPolicy;
	}

	@Override
	public GuestPolicyEnum getGuestPolicyEnum() {
		if(StringHelper.containsNonWhitespace(guestPolicy)) {
			return GuestPolicyEnum.valueOf(guestPolicy);
		}
		return GuestPolicyEnum.ALWAYS_DENY;
	}

	@Override
	public void setGuestPolicyEnum(GuestPolicyEnum guestPolicy) {
		if(guestPolicy == null) {
			this.guestPolicy = GuestPolicyEnum.ALWAYS_DENY.name();
		} else {
			this.guestPolicy = guestPolicy.name();
		}
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 964210765 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof BigBlueButtonMeetingImpl) {
			BigBlueButtonMeetingImpl meeting = (BigBlueButtonMeetingImpl)obj;
			return getKey() != null && getKey().equals(meeting.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
