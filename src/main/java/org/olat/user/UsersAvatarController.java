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
package org.olat.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.instantMessaging.model.Presence;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UsersAvatarController extends FormBasicController {
	
	private static final String USER_PROPS_LIST_ID = UsersAvatarController.class.getName();

	private final Collection<Identity> identities;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserPortraitService userPortraitService;

	public UsersAvatarController(UserRequest ureq, WindowControl wControl, Collection<Identity> identities) {
		super(ureq, wControl, "users_avatars");
		this.identities = identities;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UsersAvatarController.USER_PROPS_LIST_ID, isAdministrativeUser);
		
		initForm(ureq);
	}

	public UsersAvatarController(UserRequest ureq, WindowControl wControl, Form mainForm, Collection<? extends IdentityRef> identityRefs) {
		super(ureq, wControl, LAYOUT_CUSTOM, "users_avatars", mainForm);
		this.identities = securityManager.loadIdentityByRefs(identityRefs);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UsersAvatarController.USER_PROPS_LIST_ID, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<IdentityItem> identityItems = identities.stream().map(this::createMemberView).collect(Collectors.toList());
		flc.contextPut("identities", identityItems);
		
		flc.contextPut("userPropertyHandlers", userPropertyHandlers);
		Map<String, Integer> handlerLookupMap = new HashMap<>(userPropertyHandlers.size());
		for(int i=userPropertyHandlers.size(); i-->0; ) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			handlerLookupMap.put(handler.getName(), i);
		}
		flc.contextPut("handlerLookupMap", handlerLookupMap);
	}

	private IdentityItem createMemberView(Identity identity) {
		IdentityItem item = new IdentityItem(identity, userPropertyHandlers, getLocale());
		
		PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), identity);
		item.setDisplayName(portraitUser.getDisplayName());
		if (!identity.equals(getIdentity())) {
			item.setOnlineIconCss(getPresenceIconCss(portraitUser.getPresence()));
		}
		
		UserPortraitComponent userPortraitComp = UserPortraitFactory.createUserPortrait(
				"user_avatar_" + identity.getKey(), flc.getFormItemComponent(), getLocale());
		userPortraitComp.setPortraitUser(portraitUser);
		item.setUserPortraitComp(userPortraitComp);
		
		return item;
	}
	
	private String getPresenceIconCss(Presence presence) {
		return switch (presence) {
		case available -> "o_icon o_icon_status_available";
		case dnd -> "o_icon o_icon_status_dnd";
		case unavailable -> "o_icon o_icon_status_unavailable";
		default -> "";
		};
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static final class IdentityItem extends UserPropertiesRow {
		
		public IdentityItem(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
			super(identity, userPropertyHandlers, locale);
		}
		
		private String displayName;
		private String onlineIconCss;
		private UserPortraitComponent userPortraitComp;

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public UserPortraitComponent getUserPortraitComp() {
			return userPortraitComp;
		}

		public void setUserPortraitComp(UserPortraitComponent userPortraitComp) {
			this.userPortraitComp = userPortraitComp;
		}

		public String getOnlineIconCss() {
			return onlineIconCss;
		}

		public void setOnlineIconCss(String onlineIconCss) {
			this.onlineIconCss = onlineIconCss;
		}
		
	}

}
