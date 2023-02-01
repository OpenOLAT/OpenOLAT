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
package org.olat.modules.project.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.session.UserSessionManager;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Presence;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMembersAvatarController extends FormBasicController {
	
	private static final String USER_PROPS_LIST_ID = ProjMembersAvatarController.class.getName();

	private final Set<Identity> members;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final String avatarBaseURL;
	private final boolean chatEnabled;

	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DisplayPortraitManager portraitManager;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;

	public ProjMembersAvatarController(UserRequest ureq, WindowControl wControl, Form mainForm, Set<Identity> members) {
		super(ureq, wControl, LAYOUT_CUSTOM, "members_avatars", mainForm);
		this.members = members;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(ProjMembersAvatarController.USER_PROPS_LIST_ID, isAdministrativeUser);
		
		avatarBaseURL = registerCacheableMapper(ureq, "avatars-members", new UserAvatarMapper(true));
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<MemberItem> memberItems = members.stream().map(this::createMemberView).collect(Collectors.toList());
		appendOnlineStatus(memberItems);
		flc.contextPut("members", memberItems);
		flc.contextPut("avatarBaseURL", avatarBaseURL);
		
		flc.contextPut("userPropertyHandlers", userPropertyHandlers);
		Map<String, Integer> handlerLookupMap = new HashMap<>(userPropertyHandlers.size());
		for(int i=userPropertyHandlers.size(); i-->0; ) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			handlerLookupMap.put(handler.getName(), i);
		}
		flc.contextPut("handlerLookupMap", handlerLookupMap);
	}

	private MemberItem createMemberView(Identity identity) {
		MemberItem item = new MemberItem(identity, userPropertyHandlers, getLocale());
		item.setDisplayName(userManager.getUserDisplayName(identity.getKey()));
		
		boolean portraitAvailable = portraitManager.hasPortrait(identity);
		item.setPortraitAvailable(portraitAvailable);
		
		String portraitCssClass;
		String gender = identity.getUser().getProperty(UserConstants.GENDER, Locale.ENGLISH);
		if ("male".equalsIgnoreCase(gender)) {
			portraitCssClass = DisplayPortraitManager.DUMMY_MALE_BIG_CSS_CLASS;
		} else if ("female".equalsIgnoreCase(gender)) {
			portraitCssClass = DisplayPortraitManager.DUMMY_FEMALE_BIG_CSS_CLASS;
		} else {
			portraitCssClass = DisplayPortraitManager.DUMMY_BIG_CSS_CLASS;
		}
		item.setPortraitCssClass(portraitCssClass);
		
		return item;
	}
	
	private void appendOnlineStatus(List<MemberItem> members) {
		if (chatEnabled) {
			Long me = getIdentity().getKey();
			if (imModule.isOnlineStatusEnabled()) {
				Map<Long, MemberItem> loadStatus = new HashMap<>();
				
				for (MemberItem member : members) {
					if (member.getIdentityKey().equals(me)) {
						// No icon for my self
					} else if (sessionManager.isOnline(member.getIdentityKey())) {
						loadStatus.put(member.getIdentityKey(), member);
					} else {
						member.setOnlineIconCss("o_icon o_icon_status_unavailable");
					}
				}
				
				if(loadStatus.size() > 0) {
					List<Long> statusToLoadList = new ArrayList<>(loadStatus.keySet());
					Map<Long,String> statusMap = imService.getBuddyStatus(statusToLoadList);
					for(Long toLoad : statusToLoadList) {
						String status = statusMap.get(toLoad);
						MemberItem member = loadStatus.get(toLoad);
						if(status == null || Presence.available.name().equals(status)) {
							member.setOnlineIconCss("o_icon o_icon_status_available");
						} else if(Presence.dnd.name().equals(status)) {
							member.setOnlineIconCss("o_icon o_icon_status_dnd");
						} else {
							member.setOnlineIconCss("o_icon o_icon_status_unavailable");
						}
					}
				}
			} else {
				for (MemberItem member:members) {
					if(member.getIdentityKey().equals(me)) {
						// No icon for my self
					} else {
						member.setOnlineIconCss("o_icon o_icon_status_chat");
					}
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static final class MemberItem extends UserPropertiesRow {
		
		public MemberItem(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
			super(identity, userPropertyHandlers, locale);
		}
		
		private String displayName;
		private boolean portraitAvailable;
		private String portraitCssClass;
		private String onlineIconCss;

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public boolean isPortraitAvailable() {
			return portraitAvailable;
		}

		public void setPortraitAvailable(boolean portraitAvailable) {
			this.portraitAvailable = portraitAvailable;
		}

		public String getPortraitCssClass() {
			return portraitCssClass;
		}

		public void setPortraitCssClass(String portraitCssClass) {
			this.portraitCssClass = portraitCssClass;
		}

		public String getOnlineIconCss() {
			return onlineIconCss;
		}

		public void setOnlineIconCss(String onlineIconCss) {
			this.onlineIconCss = onlineIconCss;
		}
		
	}

}
