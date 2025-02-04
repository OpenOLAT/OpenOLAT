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

import java.io.File;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Presence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: Feb 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class UserPortraitServiceImpl implements UserPortraitService {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private DisplayPortraitManager portraitManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	
	@Override
	public UserInfoProfileConfig createProfileConfig() {
		UserInfoProfileConfig profileConfig = new UserInfoProfileConfig();
		boolean chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		profileConfig.setChatEnabled(chatEnabled);
		return profileConfig;
	}
	
	@Override
	public PortraitUser createPortraitUser(Long identityKey, String username, boolean portraitAvailable,
			String portraitCacheIdentifier, String initials, String initialsCss, String displayName,
			Presence presence) {
		return new PortraitUserImpl(identityKey, username, portraitAvailable, portraitCacheIdentifier, initials,
				initialsCss, displayName, presence);
	}
	
	@Override
	public PortraitUser createPortraitUser(Identity identity) {
		String displayName = userManager.getUserDisplayName(identity);
		String initials = userManager.getInitials(identity.getUser());
		String initialsCss = userManager.getInitialsColorCss(identity.getKey());
		
		File portraitFile = portraitManager.getMasterPortrait(identity);
		boolean portraitAvailable = portraitFile != null;
		String portraitCacheIdentifier= portraitFile != null? String.valueOf(portraitFile.lastModified()): null;

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
		
		return createPortraitUser(identity.getKey(), identity.getName(), portraitAvailable, portraitCacheIdentifier,
				initials, initialsCss, displayName, presence);
	}

	@Override
	public List<PortraitUser> createPortraitUsers(List<Identity> identities) {
		return identities.stream().map(this::createPortraitUser).toList();
	}
	
	private static final class PortraitUserImpl implements PortraitUser {
		
		private final Long identityKey;
		private final String username;
		private final boolean portraitAvailable;
		private final String portraitCacheIdentifier;
		private final String initials;
		private final String initialsCss;
		private final String displayName;
		private final Presence presence;
		
		private PortraitUserImpl(Long identityKey, String username, boolean portraitAvailable,
				String portraitCacheIdentifier, String initials, String initialsCss, String displayName,
				Presence presence) {
			this.identityKey = identityKey;
			this.username = username;
			this.portraitAvailable = portraitAvailable;
			this.portraitCacheIdentifier = portraitCacheIdentifier;
			this.initials = initials;
			this.initialsCss = initialsCss;
			this.displayName = displayName;
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
		public boolean isPortraitAvailable() {
			return portraitAvailable;
		}

		@Override
		public String getPortraitCacheIdentifier() {
			return portraitCacheIdentifier;
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
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public Presence getPresence() {
			return presence;
		}
		
	}

}
