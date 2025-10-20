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
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.admin.sysinfo.InfoMessageManager;
import org.olat.admin.sysinfo.SysInfoMessage;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Invitation;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
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
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.resource.OresHelper;
import org.olat.ldap.LDAPLoginModule;
import org.olat.login.auth.AuthenticationEvent;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.WebCatalogDispatcher;
import org.olat.modules.invitation.InvitationService;
import org.olat.registration.PwChangeController;
import org.olat.registration.RegWizardConstants;
import org.olat.registration.RegistrationAdditionalPersonalDataController;
import org.olat.registration.RegistrationLangStep00;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.registration.RegistrationPersonalDataController;
import org.olat.registration.SelfRegistrationAdvanceOrderInput;
import org.olat.registration.TemporaryKey;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A container for all the authentications methods available to the user.
 * 
 * <P>
 * Initial Date:  02.09.2007 <br>
 * @author patrickb
 */
public class LoginAuthprovidersController extends MainLayoutBasicController implements Activateable2 {

	private static final Logger log = Tracing.createLoggerFor(LoginAuthprovidersController.class);

	private static final String ACTION_LOGIN = "login";
	public  static final String ATTR_LOGIN_PROVIDER = "lp";

	private Invitation invitation;
	private final StackedPanel dmzPanel;
	private final List<Controller> authenticationCtrlList = new ArrayList<>();

	private Link registerLink;
	private final VelocityContainer content;
	private Component changePasswordLink;

	private CloseableModalController cmc;
	private StepsMainRunController registrationWizardCtrl;
	private PwChangeController pwChangeCtrl;

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
	@Autowired
	private UserManager userManager;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private AutoAccessManager autoAccessManager;
	@Autowired
	private UserPropertiesConfig userPropertiesConfig;

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
		if ("about".equals(type)) {
			doOpenAboutPage();
		} else if ("registration".equals(type)) {
			// make sure the OLAT authentication controller is activated as only this one can handle registration requests
			AuthenticationProvider olatProvider = loginModule.getAuthenticationProvider(BaseSecurityModule.getDefaultAuthProviderIdentifier());
			if (olatProvider.isEnabled() && registrationModule.isSelfRegistrationEnabled()
					&& registrationModule.isSelfRegistrationLinkEnabled()) {
				doOpenRegistration(ureq);
			}
		} else if("changepw".equals(type)) {
			if(entries.size() == 1) {
				doOpenChangePassword(ureq);
			} else if(entries.size() > 1) {
				String email = entries.get(1).getOLATResourceable().getResourceableTypeName();
				doOpenChangePassword(ureq, email);
			}
		} else if("invitationregistration".equalsIgnoreCase(type)) {
			invitation = (Invitation)ureq.getUserSession().getEntry(AuthHelper.ATTRIBUTE_INVITATION);
			doOpenRegistration(ureq);
		}
	}

	private void doOpenRegistration(UserRequest ureq) {
		boolean isAdditionalRegistrationFormEnabled = !userManager
				.getUserPropertyHandlersFor(RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, false).isEmpty();
		boolean emailValidation = registrationModule.isEmailValidationEnabled();
		boolean allowRecurringUserEnabled = registrationModule.isAllowRecurringUserEnabled();
		
		Step startReg = new RegistrationLangStep00(ureq, invitation, registrationModule.isDisclaimerEnabled(),
				emailValidation, isAdditionalRegistrationFormEnabled, allowRecurringUserEnabled);
		// Skip the language step if there is only one language enabled - default
		// language will be used. Use the calculated next step as start step instead.
		if (i18nModule.getEnabledLanguageKeys().size() == 1) {	
			startReg = startReg.nextStep();			
		}
		
		registrationWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), startReg, new RegisterFinishCallback(),
				new RegCancelCallback(), translate("menu.register"), "o_sel_registration_start_wizard");
		listenTo(registrationWizardCtrl);
		getWindowControl().pushAsModalDialog(registrationWizardCtrl.getInitialComponent());
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
			catalogLink.setElementCssClass("o_login_catalog_button btn btn-default o_button_primary_light o_login_btn_icon_right");
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
			doOpenChangePassword(ureq);
		} else if (ACTION_LOGIN.equals(event.getCommand())
				&& "guest".equalsIgnoreCase(ureq.getParameter(ATTR_LOGIN_PROVIDER))) {
			doGuestLogin(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == registrationWizardCtrl) {
			if (event == StepsEvent.RELOAD) {
				getWindowControl().pop();
				cleanUp();
				updateLanguage(ureq);
				doOpenRegistration(ureq);
			} else {
				if (invitation != null) {
					ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
					dmzPanel.popContent();
				} else {
					getWindowControl().pop();
				}
				cleanUp();
			}
		} else if (source == pwChangeCtrl) {
			if (event == Event.CANCELLED_EVENT
					&& loginModule.getAuthenticationProvider(ShibbolethDispatcher.PROVIDER_SHIB) != null) {
				// Redirect to context path to prevent Javascript error when using Shibboleth provider OO-7777
				ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
			}
			getWindowControl().pop();
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
		removeAsListenerAndDispose(registrationWizardCtrl);
		removeAsListenerAndDispose(pwChangeCtrl);
		removeAsListenerAndDispose(cmc);
		registrationWizardCtrl = null;
		pwChangeCtrl = null;
		cmc = null;
	}
	
	private void updateLanguage(UserRequest ureq) {
		Locale locale = ureq.getUserSession().getLocale();
		setTranslator(Util.createPackageTranslator(LoginAuthprovidersController.class, locale,
				Util.createPackageTranslator(BaseFullWebappController.class, locale)));
		MultiUserEvent mue = new LanguageChangedEvent(ureq.getUserSession().getLocale(), ureq);
		OLATResourceable wrappedLocale = OresHelper.createOLATResourceableType(Locale.class);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(mue, wrappedLocale);
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

	protected void doOpenAboutPage() {
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

	/**
	 * Start the change password workflow (if allowed) from the login panel.
	 * 
	 * @param ureq The user request
	 */
	private void doOpenChangePassword(UserRequest ureq) {
		getWindowControl().getWindowBackOffice().getWindowManager().setAjaxEnabled(true);

		if (userModule.isAnyPasswordChangeAllowed()) {
			pwChangeCtrl = new PwChangeController(ureq, getWindowControl(), null, false);
			listenTo(pwChangeCtrl);
			getWindowControl().pushAsModalDialog(pwChangeCtrl.getInitialComponent());
		} else {
			showWarning("warning.not.allowed.to.change.pwd", new String[]  {WebappHelper.getMailConfig("mailSupport") });
		}
	}
	
	/**
	 * Start the change password workflow (if allowed) from an url backed
	 * by a temporary key.
	 * 
	 * @param ureq The user request
	 * @param initialEmail The email (mandatory)
	 */
	private void doOpenChangePassword(UserRequest ureq, String initialEmail) {
		getWindowControl().getWindowBackOffice().getWindowManager().setAjaxEnabled(true);

		if (userModule.isAnyPasswordChangeAllowed() && StringHelper.containsNonWhitespace(initialEmail)
				&& canChangePasswordWithEmail(initialEmail)) {
			pwChangeCtrl = new PwChangeController(ureq, getWindowControl(), initialEmail, false);
			listenTo(pwChangeCtrl);
			getWindowControl().pushAsModalDialog(pwChangeCtrl.getInitialComponent());
		} else {
			showWarning("warning.not.allowed.to.change.pwd", new String[]  {WebappHelper.getMailConfig("mailSupport") });
		}
	}
	
	private boolean canChangePasswordWithEmail(String initialEmail) {
		if(registrationManager.hasTemporaryKeyByEmail(initialEmail, RegistrationManager.PW_CHANGE)) {
			return true;
		}
		List<Identity> ids = userManager.findIdentitiesByEmail(List.of(initialEmail));
		return ids != null && ids.size() == 1 && userModule.isPwdChangeAllowed(ids.get(0));
	}

	private static class RegCancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			TemporaryKey temporaryKey = (TemporaryKey) runContext.get(RegWizardConstants.TEMPORARYKEY);
			// remove temporaryKey entry, if process gets canceled
			if (temporaryKey != null) {
				CoreSpringFactory.getImpl(RegistrationManager.class).deleteTemporaryKey(temporaryKey);
			}
			return Step.NOSTEP;
		}
	}

	private class RegisterFinishCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			if (runContext.get(RegWizardConstants.RECURRINGDETAILS) == null) {
				Identity identity = (invitation != null && invitation.getIdentity() != null) ? invitation.getIdentity() : null;

				// Make sure we have an identity
				if (identity == null) {
					identity = createNewUser(runContext);
					if (identity == null) {
						showError("user.notregistered");
						return null;
					}
				} else {
					handleExistingIdentity(identity, runContext);
				}

				updateUserData(identity, runContext);
				if (invitation != null) {
					invitationService.acceptInvitation(invitation, identity);
				}

				doLogin(ureq, identity, BaseSecurityModule.getDefaultAuthProviderIdentifier());
			}

			return StepsMainRunController.DONE_MODIFIED;
		}

		public void doLogin(UserRequest ureq, Identity persistedIdentity, String authProvider) {
			int loginStatus = AuthHelper.doLogin(persistedIdentity, authProvider, ureq);
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

		private void handleExistingIdentity(Identity identity, StepsRunContext runContext) {
			String username = (String) runContext.get(RegWizardConstants.USERNAME);
			String password = (String) runContext.get(RegWizardConstants.PASSWORD);
			List<Authentication> passkeys = (List<Authentication>) runContext.get(RegWizardConstants.PASSKEYS);

			if (StringHelper.containsNonWhitespace(password)) {
				ensurePasswordAuthentication(identity, username, password);
			}

			if (passkeys != null && !passkeys.isEmpty()) {
				securityManager.persistAuthentications(identity, passkeys);
			}
		}

		private void ensurePasswordAuthentication(Identity identity, String username, String password) {
			Authentication auth = securityManager.findAuthentication(identity,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER);
			if (auth == null) {
				securityManager.createAndPersistAuthentication(identity,
						BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, null,
						username, password, loginModule.getDefaultHashAlgorithm());
			}
		}

		private Identity createNewUser(StepsRunContext runContext) {
			String firstName = (String) runContext.get(RegWizardConstants.FIRSTNAME);
			String lastName = (String) runContext.get(RegWizardConstants.LASTNAME);
			String email = (String) runContext.get(RegWizardConstants.EMAIL);
			String username = (String) runContext.get(RegWizardConstants.USERNAME);
			String password = (String) runContext.get(RegWizardConstants.PASSWORD);

			// create user with mandatory fields from registration-form
			User volatileUser = userManager.createUser(firstName, lastName, email);

			// create an identity with the given username / pwd and the user object
			List<Authentication> passkeys = (List<Authentication>) runContext.get(RegWizardConstants.PASSKEYS);

			// if organisation module and emailDomain is enabled, then set the selected orgaKey
			// otherwise selectedOrgaKey is null
			String selectedOrgaKey = (String) runContext.get(RegWizardConstants.SELECTEDORGANIZATIONKEY);

			TemporaryKey temporaryKey = (TemporaryKey) runContext.get(RegWizardConstants.TEMPORARYKEY);
			Identity identity = registrationManager.createNewUserAndIdentityFromTemporaryKey(username, password, volatileUser, temporaryKey, selectedOrgaKey);

			if (identity != null && passkeys != null && !passkeys.isEmpty()) {
				securityManager.persistAuthentications(identity, passkeys);
			}
			return identity;
		}

		private void updateUserData(Identity identity, StepsRunContext runContext) {
			User user = identity.getUser();

			// Set user configured language
			Preferences preferences = user.getPreferences();
			// can be null if language step is skipped - in this case the default language
			// is used in the preferences.setLanguage() method
			preferences.setLanguage((String) runContext.get(RegWizardConstants.CHOSEN_LANG));
			user.setPreferences(preferences);

			// Enroll user to auto-enrolled courses if not invited
			if (invitation == null) {
				autoEnrollUser(identity);
			}

			// Add static properties if enabled and not invited
			if (invitation == null && registrationModule.isStaticPropertyMappingEnabled()) {
				addStaticProperty(user);
			}

			// Add user property values from registration forms
			populateUserPropertiesFromForm(user, RegistrationPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, (Map<String, FormItem>) runContext.get(RegWizardConstants.PROPFORMITEMS));

			boolean isAdditionalRegistrationFormEnabled = !userManager
					.getUserPropertyHandlersFor(RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, false).isEmpty();
			if (isAdditionalRegistrationFormEnabled) {
				populateUserPropertiesFromForm(user, RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, (Map<String, FormItem>) runContext.get(RegWizardConstants.ADDITIONALPROPFORMITEMS));
			}

			// Persist changes and send notifications
			userManager.updateUserFromIdentity(identity);
			notifyAdminOnNewUser(identity);

			// Register user's disclaimer acceptance
			registrationManager.setHasConfirmedDislaimer(identity);

			if (invitation != null && invitation.getIdentity() == null) {
				invitation = invitationService.update(invitation, identity);
			}
		}

		private void autoEnrollUser(Identity identity) {
			SelfRegistrationAdvanceOrderInput input = new SelfRegistrationAdvanceOrderInput();
			input.setIdentity(identity);
			input.setRawValues(registrationModule.getAutoEnrolmentRawValue());
			autoAccessManager.createAdvanceOrders(input);
			autoAccessManager.grantAccessToCourse(identity);
		}

		private void addStaticProperty(User user) {
			String propertyName = registrationModule.getStaticPropertyMappingName();
			String propertyValue = registrationModule.getStaticPropertyMappingValue();

			if (StringHelper.containsNonWhitespace(propertyName) && StringHelper.containsNonWhitespace(propertyValue)
					&& userPropertiesConfig.getPropertyHandler(propertyName) != null) {
				try {
					user.setProperty(propertyName, propertyValue);
				} catch (Exception e) {
					log.error("Cannot set the static property value", e);
				}
			}
		}

		private void populateUserPropertiesFromForm(User user, String formIdentifier, Map<String, FormItem> propFormItems) {
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifier, false);
			for (UserPropertyHandler handler : userPropertyHandlers) {
				FormItem formItem = propFormItems.get(handler.getName());
				handler.updateUserFromFormItem(user, formItem);
			}
		}

		private void notifyAdminOnNewUser(Identity identity) {
			String notificationEmail = registrationModule.getRegistrationNotificationEmail();
			if (notificationEmail != null) {
				registrationManager.sendNewUserNotificationMessage(notificationEmail, identity);
			}
		}
	}
}