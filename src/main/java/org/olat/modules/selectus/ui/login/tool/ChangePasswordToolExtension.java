/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.login.tool;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolCategory;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.UserSession;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.auth.OLATAuthManager;

/**
 * 
 * Initial date: 25 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangePasswordToolExtension extends UserToolExtension {
	
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
		return "o_navbar_change_password";
	}
	
	@Override
	public String getShortCutCssClass() {
		return null;
	}

	@Override
	public String getUniqueExtensionID() {
		return "org.olat.home.HomeMainController:org.olat.modules.selectus.ui.login.tool.ChangePasswordController";
	}

	@Override
	public UserTool createUserTool(UserRequest ureq, WindowControl wControl, Locale locale) {
		if(ureq == null) return null;
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null || usess.getRoles().isGuestOnly() || usess.getRoles().isInvitee()) {
			return null;
		}
		
		if(LDAPAuthenticationController.PROVIDER_LDAP.equals(usess.getSessionInfo().getAuthProvider())) {
			//clean up OLAT tokens
			BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
			Authentication olatAuthentication = securityManager.findAuthentication(ureq.getIdentity(), OLATAuthManager.PROVIDER_OLAT, BaseSecurity.DEFAULT_ISSUER);
			if(olatAuthentication != null) {
				securityManager.deleteAuthentication(olatAuthentication);
			}
			return null;
		}
		return new ChangePasswordTool(wControl);
	}
}
