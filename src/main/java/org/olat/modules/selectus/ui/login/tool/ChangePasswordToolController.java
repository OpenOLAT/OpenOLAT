/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.login.tool;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.oauth.spi.MicrosoftAzureADFSProvider;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Initial date: 25 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangePasswordToolController extends BasicController {
	
	private final Link changePassword;
	
	private CloseableModalController cmc;
	private ChangePasswordController changePasswordController;
	
	@Autowired
	private BaseSecurity baseSecurity;
	
	public ChangePasswordToolController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		
		VelocityContainer mainVC = createVelocityContainer("change_password_tool");
		changePassword = LinkFactory.createLink("change.password", "change.password", mainVC, this);
		changePassword.setIconLeftCSS("o_icon o_icon_change_password");
		mainVC.put("change.password", changePassword);
		StackedPanel p = new SimpleStackedPanel("changePasswordPanel");
		p.setContent(mainVC);
		putInitialPanel(p);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(changePassword == source) {
			doChangePassword(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(changePasswordController == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(changePasswordController);
		removeAsListenerAndDispose(cmc);
		changePasswordController = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doChangePassword(UserRequest ureq) {
		final String provider = ureq.getUserSession().getSessionInfo().getAuthProvider();
		boolean ldapAuth = LDAPAuthenticationController.PROVIDER_LDAP.equals(provider);
		boolean oauthAuth = MicrosoftAzureADFSProvider.PROVIDER.equals(provider);
		boolean olatAuth = baseSecurity.findAuthentication(getIdentity(), OLATAuthManager.PROVIDER_OLAT, BaseSecurity.DEFAULT_ISSUER) != null;
		
		if(ldapAuth || oauthAuth || !olatAuth) {
			showWarning("change.password.ldap_user");
		} else {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(changePasswordController);
			changePasswordController = new ChangePasswordController(ureq, getWindowControl());
			listenTo(changePasswordController);
			String title = translate("change.password.title");
			cmc = new CloseableModalController(getWindowControl(), "c", changePasswordController.getInitialComponent(), title);
			cmc.activate();
			listenTo(cmc);
		}
	}
}
