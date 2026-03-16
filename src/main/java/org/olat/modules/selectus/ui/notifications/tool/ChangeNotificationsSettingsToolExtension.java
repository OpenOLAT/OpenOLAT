/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.notifications.tool;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolCategory;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.UserSession;

import org.olat.modules.selectus.RecruitingModule;

/**
 * 
 * Initial date: 25 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangeNotificationsSettingsToolExtension extends UserToolExtension {
	
	@Override
	public boolean isShortCutOnly() {
		return true;
	}
	
	@Override
	public UserToolCategory getUserToolCategory() {
		return UserToolCategory.personal;
	}
	
	@Override
	public String getShortCutCssId() {
		return "o_navbar_notifications_settings";
	}
	
	@Override
	public String getShortCutCssClass() {
		return null;
	}

	@Override
	public String getUniqueExtensionID() {
		return "org.olat.home.HomeMainController:org.olat.modules.selectus.ui.committee.tool.InstantMessagingMainController";
	}

	@Override
	public UserTool createUserTool(UserRequest ureq, WindowControl wControl, Locale locale) {
		if(ureq == null) return null;
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null || usess.getRoles().isGuestOnly() || usess.getRoles().isInvitee()) {
			return null;
		}
		return new ChangeNotificationsSettingsTool(wControl);
	}

	@Override
	public boolean isEnabled() {
		return CoreSpringFactory.getImpl(RecruitingModule.class).isNotificationsToolEnabled() && super.isEnabled();
	}

}
