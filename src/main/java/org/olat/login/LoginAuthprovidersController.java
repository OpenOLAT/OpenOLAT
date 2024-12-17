/**
* OLAT - Online Learning and Training<br>
* https://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* https://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="https://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.olat.admin.sysinfo.InfoMessageManager;
import org.olat.admin.sysinfo.SysInfoMessage;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.dispatcher.DispatcherModule;
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
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.ldap.LDAPLoginModule;
import org.olat.login.auth.AuthenticationEvent;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.WebCatalogDispatcher;
import org.olat.registration.PwChangeController;
import org.olat.registration.RegistrationModule;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A container for all the authentications methods available to the user.
 * 
 * <P>
 * Initial Date:  02.09.2007 <br>
 * @author patrickb
 */
public class LoginAuthprovidersController extends MainLayoutBasicController implements Activateable2 {

	private static final String ACTION_LOGIN = "login";
	public  static final String ATTR_LOGIN_PROVIDER = "lp";

	private final Invitation invitation;
	private final StackedPanel dmzPanel;
	private final List<Controller> authenticationCtrlList = new ArrayList<>();

	private Link registerLink;
	private VelocityContainer content;
	private Component changePasswordLink;

	private CloseableModalController cmc;
	private PwChangeController pwChangeCtrl;
	private LoginProcessController loginProcessCtrl;

	@Autowired
	private HelpModule helpModule;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private UserModule userModule;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private InfoMessageManager infoMessageMgr;
	@Autowired
	private RegistrationModule registrationModule;

	public LoginAuthprovidersController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null, false);
	}

	public LoginAuthprovidersController(UserRequest ureq, WindowControl wControl, Invitation invitation, boolean hasModuleUri) {
		// Use fallback translator from full webapp package to translate accessibility stuff
		super(ureq, wControl, Util.createPackageTranslator(BaseFullWebappController.class, ureq.getLocale()));
		this.invitation = invitation;

		UserSession usess = ureq.getUserSession();
		if(usess.getEntry("error.change.email") != null) {
			wControl.setError(usess.getEntry("error.change.email").toString());
			usess.removeEntryFromNonClearedStore("error.change.email");
		}
		if(usess.getEntry("error.change.email.time") != null) {
			wControl.setError(usess.getEntry("error.change.email.time").toString());
			usess.removeEntryFromNonClearedStore("error.change.email.time");
		}

		MainPanel panel = new MainPanel("content");
		panel.setCssClass("o_loginscreen");
		content = initLoginContent(ureq);
		initChangePassword(content);
		panel.pushContent(content);

		if (registrationModule.isSelfRegistrationEnabled()
				&& registrationModule.isSelfRegistrationLoginEnabled()) {
			registerLink = LinkFactory.createLink("_olat_login_register", "menu.register", content, this);
			registerLink.setElementCssClass("o_login_register");
			registerLink.setTitle("menu.register.alt");
		}

		dmzPanel = putInitialPanel(panel);

		// if the call is from an invitation or as rest call, directly open Registration
		if (invitation != null || hasModuleUri) {
			doOpenRegistration(ureq);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("browsercheck".equals(type)) {
			showBrowserCheckPage(ureq);
		} else if ("accessibility".equals(type)) {
			showAccessibilityPage();
		} else if ("about".equals(type)) {
			showAboutPage();
		} else if ("registration".equals(type)) {
			// make sure the OLAT authentication controller is activated as only this one can handle registration requests
			AuthenticationProvider olatProvider = loginModule.getAuthenticationProvider(BaseSecurityModule.getDefaultAuthProviderIdentifier());
			if (olatProvider.isEnabled() && registrationModule.isSelfRegistrationEnabled()
					&& registrationModule.isSelfRegistrationLinkEnabled()) {
				doOpenRegistration(ureq);
			}
		} else if("changepw".equals(type)) {
			String email = null;
			if(entries.size() > 1) {
				email = entries.get(1).getOLATResourceable().getResourceableTypeName();
			}
			openChangePassword(ureq, email);
		}
	}

	private void doOpenRegistration(UserRequest ureq) {
		loginProcessCtrl = new LoginProcessController(ureq, getWindowControl(), dmzPanel, invitation);
		listenTo(loginProcessCtrl);
		loginProcessCtrl.doOpenRegistration(ureq);
	}

	private VelocityContainer initLoginContent(UserRequest ureq) {
		// in every case we build the container for pages to fill the panel
		VelocityContainer contentBorn = createVelocityContainer("main_loging", "login");

		// browser not supported messages
		// true if browserwarning should be showed
		boolean isBrowserWarningOn = Settings.isBrowserAjaxBlacklisted(ureq);
		contentBorn.contextPut("browserWarningOn", isBrowserWarningOn ? Boolean.TRUE : Boolean.FALSE);

		initAuthProviders(ureq, contentBorn); // Authentication init

		// prepare info message
		addSystemAndNodeMessages(contentBorn);
		addCustomLoginMessages(contentBorn);
		// login is blocked?
		setupLoginRestrictions(contentBorn);

		// guest link
		contentBorn.contextPut("guestLogin", loginModule.isGuestLoginEnabled());
		contentBorn.contextPut("startLogin", Boolean.FALSE);

		addCatalogLinkIfApplicable(contentBorn); // Catalog link if enabled
		addFaqLinkIfAvailable(contentBorn);     // FAQ link if available

		return contentBorn;
	}

	private void initAuthProviders(UserRequest ureq, VelocityContainer contentBorn) {
		Collection<AuthenticationProvider> providers = loginModule.getAuthenticationProviders();
		List<String> providerCmpIds = new ArrayList<>();
		authenticationCtrlList.clear();

		boolean multiProvidersController = false;
		String multiProvidersControllerCmpId = null;
		boolean multiProvidersControllerDefault = false;
		int count = 0;

		for (AuthenticationProvider provider : providers) {
			if (!provider.isEnabled()) {
				continue;
			}

			String cmpId = "dormant_" + count++;
			Controller controller = provider.createController(ureq, getWindowControl());

			if (controller instanceof OLATAuthenticationController) { // Create it only once
				if (provider.isDefault()) {
					multiProvidersControllerDefault = true;
				}
				if (multiProvidersController) {
					continue;
				}
				multiProvidersController = true;
				multiProvidersControllerCmpId = cmpId;
			}

			authenticationCtrlList.add(controller);
			listenTo(controller);
			contentBorn.put(cmpId, controller.getInitialComponent());

			if (provider.isDefault() && !authenticationCtrlList.isEmpty()) {
				providerCmpIds.add(0, cmpId);
			} else {
				providerCmpIds.add(cmpId);
			}
		}

		if (multiProvidersControllerDefault) {
			swapOLATAuthenticationController(providerCmpIds, multiProvidersControllerCmpId);
		}

		contentBorn.contextPut("providers", providerCmpIds);
		contentBorn.contextPut("locale", getLocale());
	}

	private void addSystemAndNodeMessages(VelocityContainer contentBorn) {
		SysInfoMessage sysInfoMsg = infoMessageMgr.getInfoMessage();
		if (sysInfoMsg.hasMessage()) {
			String infoMsg = sysInfoMsg.getTimedMessage();
			if (StringHelper.containsNonWhitespace(infoMsg)) {
				contentBorn.contextPut("infomsg", infoMsg);
			}
		}

		SysInfoMessage sysInfoNodeMsg = infoMessageMgr.getInfoMessageNodeOnly();
		if (sysInfoNodeMsg.hasMessage()) {
			String infoMsgNode = sysInfoNodeMsg.getTimedMessage();
			if (!infoMsgNode.isEmpty()) {
				contentBorn.contextPut("infomsgNode", infoMsgNode);
			}
		}
	}

	private void addCustomLoginMessages(VelocityContainer contentBorn) {
		// add additional login intro message for custom content
		String customMsg = translate("login.custommsg");
		if (!StringUtils.isBlank(customMsg)) {
			contentBorn.contextPut("logincustommsg", customMsg);
		}

		// add additional login footer message for custom content
		String footerMsg = translate("login.customfootermsg");
		if (!StringUtils.isBlank(footerMsg)) {
			contentBorn.contextPut("loginfootermsg", footerMsg);
		}

		// add additional login footer message for custom content
		String helpMsg = translate("login.customhelpmsg");
		if (!StringUtils.isBlank(helpMsg)) {
			contentBorn.contextPut("loginhelpmsg", helpMsg);
		}
	}

	private void setupLoginRestrictions(VelocityContainer contentBorn) {
		if (AuthHelper.isLoginBlocked()) {
			contentBorn.contextPut("loginBlocked", Boolean.TRUE);
		}
	}

	private void addCatalogLinkIfApplicable(VelocityContainer contentBorn) {
		if (catalogV2Module.isEnabled() && catalogV2Module.isWebPublishEnabled() && catalogV2Module.isWebPublishLoginSite()) {
			ExternalLink catalogLink = LinkFactory.createExternalLink("login.catalog", "", WebCatalogDispatcher.getBaseUrl().toString());
			catalogLink.setElementCssClass("o_login_catalog_button btn btn-default o_login_btn_icon_right");
			catalogLink.setName(translate("login.catalog.explore"));
			catalogLink.setIconRightCSS("o_icon o_icon_arrow_right");
			catalogLink.setTarget("_self");
			contentBorn.put("login.catalog", catalogLink);
		}
	}

	private void addFaqLinkIfAvailable(VelocityContainer contentBorn) {
		String loginUrl = loginModule.getLoginFaqUrl();
		if (StringHelper.containsNonWhitespace(loginUrl)) {
			if (helpModule.isHelpEnabled() && !loginUrl.startsWith("http")) {
				loginUrl = helpModule.getManualProvider().getURL(getLocale(), loginUrl);
			}
			ExternalLink faqLink = LinkFactory.createExternalLink("faq", translate("login.faq"), loginUrl);
			faqLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_right");
			faqLink.setName(translate("login.faq"));
			faqLink.setElementCssClass("o_login_faq");
			contentBorn.put("faq", faqLink);
		}
	}


	private void swapOLATAuthenticationController(List<String> cmpIdsList, String cmpId) {
		if(authenticationCtrlList.size() <= 1 || authenticationCtrlList.get(0) instanceof OLATAuthenticationController) {
			return;
		}

		if(cmpIdsList.remove(cmpId)) {
			cmpIdsList.add(0, cmpId);
		}
	}

	private void initChangePassword(VelocityContainer container) {
		if(ldapLoginModule.isLDAPEnabled()) {
			if(ldapLoginModule.isPropagatePasswordChangedOnLdapServer()) {
				Link link = LinkFactory.createLink("_ldap_login_change_pwd", "menu.pw", container, this);
				link.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_right");
				link.setElementCssClass("o_login_pwd");
				changePasswordLink = link;
			} else if(StringHelper.containsNonWhitespace(ldapLoginModule.getChangePasswordUrl())) {
				ExternalLink link = new ExternalLink("_ldap_login_change_pwd", "menu.pw");
				link.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_right");
				link.setElementCssClass("o_login_pwd");
				link.setName(translate("menu.pw"));
				link.setUrl(ldapLoginModule.getChangePasswordUrl());
				link.setTarget("_blank");
				container.put("menu.pw", link);
				changePasswordLink = link;
			}
		} else if(userModule.isAnyPasswordChangeAllowed()) {
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
			doOpenRegistration(ureq);
		} else if (source == changePasswordLink) {
			openChangePassword(ureq, null);
		} else if (ACTION_LOGIN.equals(event.getCommand())
				&& "guest".equalsIgnoreCase(ureq.getParameter(ATTR_LOGIN_PROVIDER))) {
			doGuestLogin(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(pwChangeCtrl == source) {
			if (event == Event.CANCELLED_EVENT) {
				if (loginModule.getAuthenticationProvider(ShibbolethDispatcher.PROVIDER_SHIB) != null) {
					// Redirect to context path to prevent Javascript error when using Shibboleth provider
					ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
				} else {
					// is a Form cancelled, show Login Form
					content = initLoginContent(ureq);
					initChangePassword(content);
					dmzPanel.setContent(content);
				}
			}
			if (pwChangeCtrl == source) {
				cmc.deactivate();
			}
			cleanUp();
		} else if (event instanceof LoginProcessEvent) {
			dmzPanel.popContent();
			cleanUp();
			doOpenRegistration(ureq);
		} else if (cmc == source) {
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
		removeAsListenerAndDispose(loginProcessCtrl);
		removeAsListenerAndDispose(pwChangeCtrl);
		removeAsListenerAndDispose(cmc);
		loginProcessCtrl = null;
		pwChangeCtrl = null;
		cmc = null;
	}

	private void doBack() {
		for(Controller ctrl:authenticationCtrlList) {
			ctrl.getInitialComponent().setVisible(true);
		}
		switchVisibility(registerLink, true);
		content.contextPut("guestLogin", loginModule.isGuestLoginEnabled());
		content.contextPut("startLogin", Boolean.FALSE);
		content.setDirty(true);

		if(changePasswordLink != null) {
			changePasswordLink.setVisible(!loginModule.isOlatProviderLoginButton());
		}
	}

	private void doStart(Controller source) {
		for(Controller ctrl:authenticationCtrlList) {
			ctrl.getInitialComponent().setVisible(source == ctrl);
		}
		switchVisibility(registerLink, false);
		content.contextPut("guestLogin", Boolean.FALSE);
		content.contextPut("startLogin", Boolean.TRUE);
		content.setDirty(true);

		if(changePasswordLink != null) {
			changePasswordLink.setVisible(true);
		}
	}

	private void switchVisibility(Component cmp, boolean visible) {
		if(cmp != null) {
			cmp.setVisible(visible);
		}
	}

	protected void doAuthentication(UserRequest ureq, AuthenticationEvent authEvent) {
		Identity identity = authEvent.getIdentity();
		String provider = authEvent.getProvider() == null ? BaseSecurityModule.getDefaultAuthProviderIdentifier() : authEvent.getProvider();

		LoginProcessController loginProcessEventCtrl = new LoginProcessController(ureq, getWindowControl(), dmzPanel, invitation);
		loginProcessEventCtrl.doLogin(ureq, identity, provider);
	}

	private void doGuestLogin(UserRequest ureq) {
		if (loginModule.isGuestLoginEnabled()) {
			int loginStatus = AuthHelper.doAnonymousLogin(ureq, ureq.getLocale());
			if (loginStatus == AuthHelper.LOGIN_OK) {
				//
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
				DispatcherModule.redirectToServiceNotAvailable( ureq.getHttpResp() );
			} else if(loginStatus == AuthHelper.LOGIN_DENIED) {
				getWindowControl().setError(translate("error.guest.login", WebappHelper.getMailConfig("mailSupport")));
			} else {
				getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
			}
		} else {
			DispatcherModule.redirectToServiceNotAvailable( ureq.getHttpResp() );
		}
	}

	protected void showAccessibilityPage() {
		VelocityContainer accessibilityVC = createVelocityContainer("accessibility");
		dmzPanel.pushContent(accessibilityVC);
	}

	protected void showBrowserCheckPage(UserRequest ureq) {
		VelocityContainer browserCheck = createVelocityContainer("browsercheck");
		browserCheck.contextPut("isBrowserAjaxReady", !Settings.isBrowserAjaxBlacklisted(ureq));
		dmzPanel.pushContent(browserCheck);
	}

	protected void showAboutPage() {
		VelocityContainer aboutVC = createVelocityContainer("about");
		// Add version info and licenses
		aboutVC.contextPut("version", Settings.getFullVersionInfo());
		// Add translator and languages info
		I18nManager i18nMgr = I18nManager.getInstance();
		Collection<String> enabledKeysSet = i18nModule.getEnabledLanguageKeys();
		Map<String, String> langNames = new HashMap<>();
		Map<String, String> langTranslators = new HashMap<>();
		String[] enabledKeys = ArrayHelper.toArray(enabledKeysSet);
		String[] names = new String[enabledKeys.length];
		for (int i = 0; i < enabledKeys.length; i++) {
			String key = enabledKeys[i];
			String langName = i18nMgr.getLanguageInEnglish(key, i18nModule.isOverlayEnabled());
			langNames.put(key, langName);
			names[i] = langName;
			String author = i18nMgr.getLanguageAuthor(key);
			langTranslators.put(key, author);
		}
		ArrayHelper.sort(enabledKeys, names, true, true, true);
		aboutVC.contextPut("enabledKeys", enabledKeys);
		aboutVC.contextPut("langNames", langNames);
		aboutVC.contextPut("langTranslators", langTranslators);
		dmzPanel.pushContent(aboutVC);
	}

	private void openChangePassword(UserRequest ureq, String initialEmail) {
		// double-check if allowed first
		if (userModule.isAnyPasswordChangeAllowed()) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(pwChangeCtrl);

			pwChangeCtrl = new PwChangeController(ureq, getWindowControl(), initialEmail, true);
			listenTo(pwChangeCtrl);

			String title = pwChangeCtrl.getWizardTitle();
			cmc = new CloseableModalController(getWindowControl(), translate("close"), pwChangeCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else {
			showWarning("warning.not.allowed.to.change.pwd", new String[]  {WebappHelper.getMailConfig("mailSupport") });
		}
	}
}