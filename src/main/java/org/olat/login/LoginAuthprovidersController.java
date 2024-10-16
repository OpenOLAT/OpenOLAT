/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
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
* <a href="http://www.openolat.org">
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
import org.olat.registration.RegistrationController;
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
	
	private Link registerLink;
	private ExternalLink catalogLink;
	private ExternalLink faqLink;
	private StackedPanel dmzPanel;
	private VelocityContainer content;
	private Component changePasswordLink;
	private final List<Controller> authenticationCtrlList = new ArrayList<>();
	
	private CloseableModalController cmc;
	private PwChangeController pwChangeCtrl;
	private RegistrationController registrationCtrl;
	
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private UserModule userModule;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private InfoMessageManager infoMessageMgr;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private CatalogV2Module catalogV2Module;
	
	public LoginAuthprovidersController(UserRequest ureq, WindowControl wControl) {
		// Use fallback translator from full webapp package to translate accessibility stuff
		super(ureq, wControl, Util.createPackageTranslator(BaseFullWebappController.class, ureq.getLocale()));
		
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
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				openRegistration(ureq).activate(ureq, subEntries, entry.getTransientState());
			}
		} else if("changepw".equals(type)) {
			String email = null;
			if(entries.size() > 1) {
				email = entries.get(1).getOLATResourceable().getResourceableTypeName();
			}
			openChangePassword(ureq, email);
		}
	}

	private VelocityContainer initLoginContent(UserRequest ureq) {
		// in every case we build the container for pages to fill the panel
		VelocityContainer contentBorn = createVelocityContainer("main_loging", "login");

		// browser not supported messages
		// true if browserwarning should be showed
		boolean bwo = Settings.isBrowserAjaxBlacklisted(ureq);
		contentBorn.contextPut("browserWarningOn", bwo ? Boolean.TRUE : Boolean.FALSE);
		
		Collection<AuthenticationProvider> providers = loginModule.getAuthenticationProviders();
		List<String> providerCmpIds = new ArrayList<>();
		authenticationCtrlList.clear();
	
		int count = 0;
		boolean multiProvidersController = false;
		String multiProvidersControllerCmpId = null;
		boolean multiProvidersControllerDefault = false;
		for (AuthenticationProvider prov : providers) {
			if(!prov.isEnabled()) continue;

			String cmpId = "dormant_" + count++;
			Controller controller = prov.createController(ureq, getWindowControl());
			if(controller instanceof OLATAuthenticationController) {// Create it only once
				if(prov.isDefault()) {
					multiProvidersControllerDefault = true;
				}
				if(multiProvidersController) {
					continue;
				} else {
					multiProvidersController = true;
					multiProvidersControllerCmpId = cmpId;
				}
			}

			authenticationCtrlList.add(controller);
			listenTo(controller);
			
			contentBorn.put(cmpId, controller.getInitialComponent());
			if(prov.isDefault() && !authenticationCtrlList.isEmpty()) {
				providerCmpIds.add(0, cmpId);
			} else {
				providerCmpIds.add(cmpId);
			}
		}

		if(multiProvidersControllerDefault) {
			swapOLATAuthenticationController(providerCmpIds, multiProvidersControllerCmpId);
		}
		
		contentBorn.contextPut("providers", providerCmpIds);
		contentBorn.contextPut("locale", getLocale());

		// prepare info message
		SysInfoMessage sysInfoMsg = infoMessageMgr.getInfoMessage();
		if (sysInfoMsg.hasMessage()) {
			String infomsg = sysInfoMsg.getTimedMessage();
			if (StringHelper.containsNonWhitespace(infomsg)) {
				contentBorn.contextPut("infomsg", infomsg);				
			}
		}

		SysInfoMessage sysInfoNodeMsg = infoMessageMgr.getInfoMessageNodeOnly();
		if (sysInfoNodeMsg.hasMessage()) {
			String infomsgNode = sysInfoNodeMsg.getTimedMessage();
			if (infomsgNode.length() > 0) {
				contentBorn.contextPut("infomsgNode", infomsgNode);
			}
		}
		
		// add additional login intro message for custom content
		String customMsg = translate("login.custommsg");
		if(!StringUtils.isBlank(customMsg)) {
			contentBorn.contextPut("logincustommsg",customMsg);
		}
		// add additional login footer message for custom content
		String footerMsg = translate("login.customfootermsg");
		if(!StringUtils.isBlank(footerMsg)) {
			contentBorn.contextPut("loginfootermsg",footerMsg);
		}
		
		// add additional login footer message for custom content
		String helpMsg = translate("login.customhelpmsg");
		if(!StringUtils.isBlank(helpMsg)) {
			contentBorn.contextPut("loginhelpmsg",helpMsg);
		}
		
		//login is blocked?
		if(AuthHelper.isLoginBlocked()) {
			contentBorn.contextPut("loginBlocked", Boolean.TRUE);
		}
		
		// guest link
		contentBorn.contextPut("guestLogin", Boolean.valueOf(loginModule.isGuestLoginEnabled()));
		contentBorn.contextPut("startLogin", Boolean.FALSE);
		
		if (catalogV2Module.isEnabled() && catalogV2Module.isWebPublishEnabled() && catalogV2Module.isWebPublishLoginSite()) {
			catalogLink = LinkFactory.createExternalLink("login.catalog", "log.catalog", WebCatalogDispatcher.getBaseUrl().toString());
			catalogLink.setElementCssClass("o_login_catalog_button btn btn-default");
			catalogLink.setName(translate("login.catalog"));
			catalogLink.setIconLeftCSS("o_icon o_icon_catalog");
			catalogLink.setTarget("_self");
			contentBorn.put("login.catalog", catalogLink);
		}
		
		String loginUrl = loginModule.getLoginFaqUrl();
		if(StringHelper.containsNonWhitespace(loginUrl)) {
			if (helpModule.isHelpEnabled() && !loginUrl.startsWith("http")) {
				loginUrl = helpModule.getManualProvider().getURL(getLocale(), loginUrl);
			}
			
			faqLink = LinkFactory.createExternalLink("faq", translate("login.faq"), loginUrl);
			faqLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_right");
			faqLink.setName(translate("login.faq"));
			faqLink.setElementCssClass("o_login_faq");
			contentBorn.put("faq", faqLink);
		}
		
		return contentBorn;
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
			openRegistration(ureq);
		} else if (source == changePasswordLink) {
			openChangePassword(ureq, null);
		} else if (ACTION_LOGIN.equals(event.getCommand())
				&& "guest".equalsIgnoreCase(ureq.getParameter(ATTR_LOGIN_PROVIDER))) { 
			doGuestLogin(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(pwChangeCtrl == source || registrationCtrl == source) {
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
			cmc.deactivate();
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
		removeAsListenerAndDispose(registrationCtrl);
		removeAsListenerAndDispose(pwChangeCtrl);
		removeAsListenerAndDispose(cmc);
		registrationCtrl = null;
		pwChangeCtrl = null;
		cmc = null;
	}
	
	private void doBack() {
		for(Controller ctrl:authenticationCtrlList) {
			ctrl.getInitialComponent().setVisible(true);
		}
		switchVisibility(registerLink, true);
		content.contextPut("guestLogin", Boolean.valueOf(loginModule.isGuestLoginEnabled()));
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

	private void doAuthentication(UserRequest ureq, AuthenticationEvent authEvent) {
		Identity identity = authEvent.getIdentity();
		String provider = authEvent.getProvider() == null ? BaseSecurityModule.getDefaultAuthProviderIdentifier() : authEvent.getProvider();
		int loginStatus = AuthHelper.doLogin(identity, provider, ureq);
		if (loginStatus == AuthHelper.LOGIN_OK) {
			// it's ok
		} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
		} else if (loginStatus == AuthHelper.LOGIN_INACTIVE) {
			getWindowControl().setError(translate("login.error.inactive", WebappHelper.getMailConfig("mailSupport")));
		} else {
			getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailReplyTo")));
		}
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
		browserCheck.contextPut("isBrowserAjaxReady", Boolean.valueOf(!Settings.isBrowserAjaxBlacklisted(ureq)));
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

	private RegistrationController openRegistration(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(registrationCtrl);
		
		registrationCtrl = new RegistrationController(ureq, getWindowControl());
		listenTo(registrationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), registrationCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
		return registrationCtrl;
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