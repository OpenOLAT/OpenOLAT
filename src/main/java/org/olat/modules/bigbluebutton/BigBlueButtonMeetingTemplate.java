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
package org.olat.modules.bigbluebutton;

import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 19 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BigBlueButtonMeetingTemplate extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public boolean isSystem();
	
	public boolean isEnabled();

	public void setEnabled(boolean enabled);
	
	public String getExternalId();
	
	public String getName();

	public void setName(String name);

	public String getDescription();

	public void setDescription(String description);
	
	public boolean isExternalUsersAllowed();
	
	public void setExternalUsersAllowed(boolean external);
	
	public Integer getMaxConcurrentMeetings();

	public void setMaxConcurrentMeetings(Integer maxConcurrentMeetings);
	
	public Integer getMaxParticipants();

	public void setMaxParticipants(Integer maxParticipants);
	
	/**
	 * @return The maximum duration of a meeting in minutes
	 */
	public Integer getMaxDuration();

	/**
	 * @param maxDuration Set the maximum duration of a meeting in minutes
	 */
	public void setMaxDuration(Integer maxDuration);
	
	public Boolean getRecord();

	public void setRecord(Boolean record);
	
	public Boolean getBreakoutRoomsEnabled();

	public void setBreakoutRoomsEnabled(Boolean breakoutRoomsEnabled);
	
	public Boolean getMuteOnStart();

	public void setMuteOnStart(Boolean muteOnStart);

	public Boolean getAutoStartRecording();

	public void setAutoStartRecording(Boolean autoStartRecording);

	public Boolean getAllowStartStopRecording();

	public void setAllowStartStopRecording(Boolean allowStartStopRecording);

	public Boolean getWebcamsOnlyForModerator();

	public void setWebcamsOnlyForModerator(Boolean webcamsOnlyForModerator);

	public Boolean getAllowModsToUnmuteUsers();

	public void setAllowModsToUnmuteUsers(Boolean allowModsToUnmuteUsers);
	
	public Boolean getLockSettingsDisableCam();

	public void setLockSettingsDisableCam(Boolean lockSettingsDisableCam);

	public Boolean getLockSettingsDisableMic();

	public void setLockSettingsDisableMic(Boolean lockSettingsDisableMic);

	public Boolean getLockSettingsDisablePrivateChat();

	public void setLockSettingsDisablePrivateChat(Boolean lockSettingsDisablePrivateChat);

	public Boolean getLockSettingsDisablePublicChat();

	public void setLockSettingsDisablePublicChat(Boolean lockSettingsDisablePublicChat);

	public Boolean getLockSettingsDisableNote();

	public void setLockSettingsDisableNote(Boolean lockSettingsDisableNote);

	public Boolean getLockSettingsLockedLayout();

	public void setLockSettingsLockedLayout(Boolean lockSettingsLockedLayout);
	
	public Boolean getLockSettingsHideUserList();

	public void setLockSettingsHideUserList(Boolean lockSettingsHideUserList);

	public Boolean getLockSettingsLockOnJoin();

	public void setLockSettingsLockOnJoin(Boolean lockSettingsLockOnJoin);

	public Boolean getLockSettingsLockOnJoinConfigurable();

	public void setLockSettingsLockOnJoinConfigurable(Boolean lockSettingsLockOnJoinConfigurable);
	
	public JoinPolicyEnum getJoinPolicyEnum();
	
	public void setJoinPolicyEnum(JoinPolicyEnum policy);
	
	public List<BigBlueButtonTemplatePermissions> getPermissions();
	
	public void setPermissions(List<BigBlueButtonTemplatePermissions> roles);
	
	public GuestPolicyEnum getGuestPolicyEnum();

	public void setGuestPolicyEnum(GuestPolicyEnum guestPolicy);
	
	public default boolean availableTo(List<BigBlueButtonTemplatePermissions> permissions) {
		List<BigBlueButtonTemplatePermissions> roles = getPermissions();
		for(BigBlueButtonTemplatePermissions role:roles) {
			for(BigBlueButtonTemplatePermissions editionRole:permissions) {
				if(role.accept(editionRole)) {
					return true;
				}
			}
		}
		return false;
	}
}
