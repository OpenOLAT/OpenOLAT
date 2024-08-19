/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.user;

import org.olat.core.id.Identity;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Presence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class UserInfoService {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private DisplayPortraitManager portraitManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	
	public UserInfoProfileConfig createProfileConfig() {
		UserInfoProfileConfig profileConfig = new UserInfoProfileConfig();
		boolean chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		profileConfig.setChatEnabled(chatEnabled);
		return profileConfig;
	}
	
	public UserInfoProfile createProfile(Identity identity) {
		String displayName = userManager.getUserDisplayName(identity);
		String initials = identity.getUser().getFirstName().substring(0, 1).toUpperCase()
				+ identity.getUser().getLastName().substring(0, 1).toUpperCase();
		String initialsCss = "o_user_initials_dark_blue";
		boolean portraitAvailable = portraitManager.hasPortrait(identity);
		Presence presence = null;
		if (imModule.isEnabled() && imModule.isPrivateEnabled()) {
			if (imModule.isOnlineStatusEnabled()) {
				if (imService.isOnline(identity)) {
					String status = imService.getStatus(identity.getKey());
					if (status == null || Presence.available.name().equals(status)) {
						presence = Presence.available;
					} else if(Presence.dnd.name().equals(status)) {
						presence = Presence.dnd;
					}
				}
				if (presence == null) {
					presence = Presence.unavailable;
				}
			}
		}
		
		return new UserInfoProfileImpl(identity.getKey(), identity.getName(), displayName, initials, initialsCss,
				portraitAvailable, presence);
	}
	
	public UserInfoProfile create(Long identityKey, String username, String displayName, String initials,
			String initialsCss, boolean portraitAvailable, Presence presence) {
		return new UserInfoProfileImpl(identityKey, username, displayName, initials, initialsCss, portraitAvailable,
				presence);
	}
	
	public UserInfoProfile copyOf(UserInfoProfile profile) {
		return create(profile.getIdentityKey(),
				profile.getUsername(),
				profile.getDisplayName(),
				profile.getInitials(),
				profile.getInitialsCss(),
				profile.isPortraitAvailable(),
				profile.getPresence()
			);
	}
	
	private final static class UserInfoProfileImpl implements UserInfoProfile {
		
		private final Long identityKey;
		private final String username;
		private final String displayName;
		private final String initials;
		private final String initialsCss;
		private final boolean portraitAvailable;
		private final Presence presence;
		
		public UserInfoProfileImpl(Long identityKey, String username, String displayName, String initials,
				String initialsCss, boolean portraitAvailable, Presence presence) {
			this.identityKey = identityKey;
			this.username = username;
			this.displayName = displayName;
			this.initials = initials;
			this.initialsCss = initialsCss;
			this.portraitAvailable = portraitAvailable;
			this.presence = presence;
		}

		@Override
		public Long getIdentityKey() {
			return identityKey;
		}

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public String getInitials() {
			return initials;
		}

		@Override
		public String getInitialsCss() {
			return initialsCss;
		}

		@Override
		public boolean isPortraitAvailable() {
			return portraitAvailable;
		}

		@Override
		public Presence getPresence() {
			return presence;
		}
		
	}

}
