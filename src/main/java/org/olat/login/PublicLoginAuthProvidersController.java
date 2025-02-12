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
package org.olat.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.ldap.LDAPLoginModule;
import org.olat.login.auth.AuthenticationEvent;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.registration.PwChangeController;
import org.olat.registration.RegistrationModule;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Okt 22, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PublicLoginAuthProvidersController extends MainLayoutBasicController implements Activateable2 {


	private final StackedPanel dmzPanel;
	private final List<Controller> authenticationCtrlList = new ArrayList<>();

	private Link registerLink;
	private VelocityContainer content;
	private Component changePasswordLink;

	private CloseableModalController cmc;
	private PwChangeController pwChangeCtrl;

	@Autowired
	private HelpModule helpModule;
	@Autowired
	private UserModule userModule;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private RegistrationModule registrationModule;

	public PublicLoginAuthProvidersController(UserRequest ureq, WindowControl wControl) {
		// Use fallback translator from full webapp package to translate accessibility stuff
		super(ureq, wControl, Util.createPackageTranslator(BaseFullWebappController.class, ureq.getLocale()));

		UserSession usess = ureq.getUserSession();
		if (usess.getEntry("error.change.email") != null) {
			wControl.setError(usess.getEntry("error.change.email").toString());
			usess.removeEntryFromNonClearedStore("error.change.email");
		}
		if (usess.getEntry("error.change.email.time") != null) {
			wControl.setError(usess.getEntry("error.change.email.time").toString());
			usess.removeEntryFromNonClearedStore("error.change.email.time");
		}

		MainPanel panel = new MainPanel("content");

		content = initLoginContent(ureq);
		initChangePassword(content);
		panel.pushContent(content);

		dmzPanel = putInitialPanel(panel);
	}

	private VelocityContainer initLoginContent(UserRequest ureq) {
		VelocityContainer loginContent = createVelocityContainer("public_login");

		// Check if the browser is supported and show a warning if needed
		boolean isBrowserWarningOn = Settings.isBrowserAjaxBlacklisted(ureq);
		loginContent.contextPut("browserWarningOn", isBrowserWarningOn ? Boolean.TRUE : Boolean.FALSE);

		initAuthProviders(ureq, loginContent);

		// Check if login is blocked
		if (AuthHelper.isLoginBlocked()) {
			loginContent.contextPut("loginBlocked", Boolean.TRUE);
		}

		// Add self-registration link if registration is enabled
		addSelfRegistrationLink(loginContent);

		String loginUrl = loginModule.getLoginFaqUrl();
		if(StringHelper.containsNonWhitespace(loginUrl)) {
			if (helpModule.isHelpEnabled() && !loginUrl.startsWith("https")) {
				loginUrl = helpModule.getManualProvider().getURL(getLocale(), loginUrl);
			}

			ExternalLink faqLink = LinkFactory.createExternalLink("faq", translate("login.faq"), loginUrl);
			faqLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_right");
			faqLink.setName(translate("login.faq"));
			faqLink.setElementCssClass("o_login_faq");
			loginContent.put("faq", faqLink);

			// add additional login footer message for custom content
			String helpMsg = translate("login.customhelpmsg");
			if(!StringUtils.isBlank(helpMsg)) {
				loginContent.contextPut("loginhelpmsg",helpMsg);
			}

			loginContent.contextPut("startLogin", Boolean.FALSE);
		}

		return loginContent;
	}

	private void initAuthProviders(UserRequest ureq, VelocityContainer loginContent) {
		Collection<AuthenticationProvider> providers = loginModule.getAuthenticationProviders();
		List<String> providerComponentIds = new ArrayList<>();
		authenticationCtrlList.clear();

		int providerIndex = 0;
		boolean multiProviderControllerAdded = false;

		for (AuthenticationProvider provider : providers) {
			if (!provider.isEnabled()) {
				continue;
			}

			String componentId = "authProvider_" + providerIndex++;
			Controller authController = provider.createController(ureq, getWindowControl());

			if (authController instanceof OLATAuthenticationController) {
				if (multiProviderControllerAdded) continue; // Only add one OLAT controller
				multiProviderControllerAdded = true;
			}

			authenticationCtrlList.add(authController);
			listenTo(authController);

			loginContent.put(componentId, authController.getInitialComponent());

			// Ensure the default provider is placed first
			if (provider.isDefault() && !authenticationCtrlList.isEmpty()) {
				providerComponentIds.add(0, componentId);
			} else {
				providerComponentIds.add(componentId);
			}
		}

		loginContent.contextPut("providers", providerComponentIds);
	}

	private void addSelfRegistrationLink(VelocityContainer loginContent) {
		if (registrationModule.isSelfRegistrationEnabled() && registrationModule.isSelfRegistrationLoginEnabled()) {
			registerLink = LinkFactory.createLink("_olat_login_register", "menu.register", loginContent, this);
			registerLink.setElementCssClass("o_login_register");
			registerLink.setTitle("menu.register.alt");
		}
	}


	private void initChangePassword(VelocityContainer container) {
		if (ldapLoginModule.isLDAPEnabled()) {
			if (ldapLoginModule.isPropagatePasswordChangedOnLdapServer()) {
				Link link = LinkFactory.createLink("_ldap_login_change_pwd", "menu.pw", container, this);
				link.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_right");
				link.setElementCssClass("o_login_pwd");
				changePasswordLink = link;
			} else if (StringHelper.containsNonWhitespace(ldapLoginModule.getChangePasswordUrl())) {
				ExternalLink link = new ExternalLink("_ldap_login_change_pwd", "menu.pw");
				link.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_right");
				link.setElementCssClass("o_login_pwd");
				link.setName(translate("menu.pw"));
				link.setUrl(ldapLoginModule.getChangePasswordUrl());
				link.setTarget("_blank");
				container.put("menu.pw", link);
				changePasswordLink = link;
			}
		} else if (userModule.isAnyPasswordChangeAllowed()) {
			Link link = LinkFactory.createLink("_olat_login_change_pwd", "menu.pw", container, this);
			link.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_right");
			link.setElementCssClass("o_login_pwd");
			link.setVisible(!loginModule.isOlatProviderLoginButton());
			changePasswordLink = link;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == registerLink) {
			fireEvent(ureq, LoginProcessEvent.REGISTER_EVENT);
		} else if (source == changePasswordLink) {
			fireEvent(ureq, LoginProcessEvent.PWCHANGE_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == pwChangeCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				content = initLoginContent(ureq);
				initChangePassword(content);
				dmzPanel.pushContent(content);
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
			if (pwChangeCtrl == source) {
				cmc.deactivate();
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if (event instanceof AuthenticationEvent authEvent) {
			doAuthentication(ureq, authEvent);
		} else if (event instanceof LoginEvent) {
			doStart(source);
		} else if (event == Event.BACK_EVENT) {
			doBack();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(pwChangeCtrl);
		removeAsListenerAndDispose(cmc);
		pwChangeCtrl = null;
		cmc = null;
	}

	protected void doAuthentication(UserRequest ureq, AuthenticationEvent authEvent) {
		Identity identity = authEvent.getIdentity();
		String provider = authEvent.getProvider() == null ? BaseSecurityModule.getDefaultAuthProviderIdentifier() : authEvent.getProvider();

		LoginProcessController loginProcessEventCtrl = new LoginProcessController(ureq, getWindowControl(), dmzPanel, null);
		loginProcessEventCtrl.doLogin(ureq, identity, provider);
	}

	private void doStart(Controller source) {
		for(Controller ctrl:authenticationCtrlList) {
			ctrl.getInitialComponent().setVisible(source == ctrl);
		}
		registerLink.setVisible(false);
		content.contextPut("startLogin", Boolean.TRUE);
		content.setDirty(true);

		if(changePasswordLink != null) {
			changePasswordLink.setVisible(true);
		}
	}

	private void doBack() {
		for(Controller ctrl:authenticationCtrlList) {
			ctrl.getInitialComponent().setVisible(true);
		}
		registerLink.setVisible(true);
		content.contextPut("startLogin", Boolean.FALSE);
		content.setDirty(true);

		if(changePasswordLink != null) {
			changePasswordLink.setVisible(!loginModule.isOlatProviderLoginButton());
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
}
