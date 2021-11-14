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

package org.olat.registration;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.wizard.WizardInfoController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description:<br>
 * Controls the registration workflow.
 * 
 * <P>
 * @author Sabina Jeger
 */
public class RegistrationController extends BasicController implements Activateable2 {

	private static final String SEPARATOR = "____________________________________________________________________\n";
	private static final String STEP_LANG = "lang";
	private static final String STEP_DISCLAIMER = "disclaimer";
	private static final String STEP_EMAIL = "email";
	private static final String STEP_REGISTRATION_FORM = "registration";
	private static final String STEP_ADDITIONAL_REGISTRATION_FORM = "additional_registration";
	private static final String STEP_FINAL = "final";

	private Panel regarea;
	private Link loginButton;
	private VelocityContainer myContent;	
	
	private WizardInfoController wizInfoController;
	private DisclaimerController disclaimerController;
	private EmailSendingForm emailSendForm;
	private RegistrationForm2 registrationForm;
	private RegistrationAdditionalForm registrationAdditionalForm;
	private LanguageChooserController langChooserController;
	
	private TemporaryKey tempKey;
	private String uniqueRegistrationKey;
	private final Steps steps;
	private final boolean additionalRegistrationForm;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserModule userModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private UserPropertiesConfig userPropertiesConfig;
	@Autowired
	private AutoAccessManager autoAccessManager;

	/**
	 * Controller implementing registration workflow.
	 * @param ureq
	 * @param wControl
	 */
	public RegistrationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.steps = new Steps();
		if (!registrationModule.isSelfRegistrationEnabled()) {
			String contact = WebappHelper.getMailConfig("mailSupport");
			String text = translate("reg.error.disabled.body", new String[]{ contact });
			MessageController msg = MessageUIFactory.createWarnMessage(ureq, getWindowControl(), null, text);
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, msg.getInitialComponent(), null);
			listenTo(layoutCtr);
			putInitialPanel(layoutCtr.getInitialComponent());
			this.additionalRegistrationForm = false;
			return;
		}
		this.additionalRegistrationForm = !userManager
				.getUserPropertyHandlersFor(RegistrationAdditionalForm.USERPROPERTIES_FORM_IDENTIFIER, false).isEmpty();
		
		// Init steps
		steps.add(STEP_LANG);
		steps.add(STEP_DISCLAIMER);
		if (registrationModule.isEmailValidationEnabled()) {
			steps.add(STEP_EMAIL);
		}
		steps.add(STEP_REGISTRATION_FORM);
		if (additionalRegistrationForm) {
			steps.add(STEP_ADDITIONAL_REGISTRATION_FORM);
		}
		steps.add(STEP_FINAL);
		
		// override language when not the same as in ureq and add fallback to
		// property handler translator for user properties
		String lang = ureq.getParameter("language");
		if (lang == null) {
			// support for legacy lang parameter
			lang = ureq.getParameter("lang");
		}
		if (lang != null && ! lang.equals(i18nModule.getLocaleKey(getLocale()))) {
			Locale loc = i18nManager.getLocaleOrDefault(lang);
			ureq.getUserSession().setLocale(loc);
			setLocale(loc, true);
			setTranslator(userManager.getPropertyHandlerTranslator(Util.createPackageTranslator(this.getClass(), loc)));			
		}	else {
			// set fallback only
			setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));			
		}
		
		//construct content
		myContent = createVelocityContainer("reg");
		wizInfoController = new WizardInfoController(ureq, steps.getNumberOfSteps());
		listenTo(wizInfoController);
		myContent.put("regwizard", wizInfoController.getInitialComponent());
		regarea = new Panel("regarea");
		myContent.put("regarea", regarea);
		uniqueRegistrationKey = ureq.getHttpReq().getParameter("key");
		if (uniqueRegistrationKey == null || uniqueRegistrationKey.equals("")) {
			// no temporary key is given, we assume step 1. If this is the case, we
			// render in a modal dialog, no need to add the 3cols layout controller
			// wrapper
			displayLanguageChooserStep(ureq);
			putInitialPanel(myContent);
		} else {
			// we check if given key is a valid temporary key
			tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(uniqueRegistrationKey);
			// if key is not valid we redirect to first page
			if (tempKey == null) {
				// error, there should be an entry
				showError("regkey.missingentry");
				displayLanguageChooserStep(ureq);
			} else {
				displayRegistrationForm(ureq);
			}
			// load view in layout
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, myContent, null);
			listenTo(layoutCtr);
			putInitialPanel(layoutCtr.getInitialComponent());
		}		
	}
	
	public String getWizardTitle() {
		return translate("step1.reg.title");
	}
	
	private VelocityContainer setErrorPage(String errorKey, WindowControl wControl) {
		String error = getTranslator().translate(errorKey);
		wControl.setError(error);
		VelocityContainer errorContainer = createVelocityContainer("error");
		errorContainer.contextPut("errorMsg", error);
		return errorContainer;
	}
	
	private void createLanguageForm(UserRequest ureq, WindowControl wControl) {
		removeAsListenerAndDispose(langChooserController);
		langChooserController = new LanguageChooserController(ureq, wControl, true);
		listenTo(langChooserController);
		myContent.contextPut("text", translate("select.language.description"));
		regarea.setContent(langChooserController.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == loginButton) {
			Identity persistedIdentity = (Identity)loginButton.getUserObject();
			doLogin(ureq, persistedIdentity);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event == Event.CANCELLED_EVENT) {
			cancel(ureq);
		} else if (source == langChooserController) {
			if (event == Event.DONE_EVENT) {
				displayDisclaimer(ureq);
				ureq.getUserSession().removeEntry(LocaleNegotiator.NEGOTIATED_LOCALE);
			} else if (event instanceof LanguageChangedEvent) {
				LanguageChangedEvent lcev = (LanguageChangedEvent)event;
				setLocale(lcev.getNewLocale(), true);
				myContent.contextPut("text", translate("select.language.description"));
			}
		} else if (source == disclaimerController) {
			if (event == Event.DONE_EVENT) {
				if (registrationModule.isEmailValidationEnabled()) {
					displayEmailForm(ureq);
				} else {
					displayRegistrationForm(ureq);
				}
			}
		} else if (source == emailSendForm) {
			if (event == Event.DONE_EVENT) { // form
				processEmail(ureq);
			}
		}  else if (source == registrationForm) {
			// Userdata entered
			if (event == Event.DONE_EVENT) {
				if(additionalRegistrationForm) {
					displayRegistrationAdditionalForm(ureq);
				} else {
					Identity persitedIdentity = createNewUserAfterRegistration();
					if(persitedIdentity == null) {
						cancel(ureq);
					} else {
						displayFinalStep(persitedIdentity);
					}
				}
			}
		} else if(source == registrationAdditionalForm) {
			if (event == Event.DONE_EVENT) {
				Identity persitedIdentity = createNewUserAfterRegistration();
				if(persitedIdentity == null) {
					cancel(ureq);
				} else {
					displayFinalStep(persitedIdentity);
				}
			}
		}
	}
	
	private void cancel(UserRequest ureq) {
		ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
	}
	
	/**
	 * Display the language chooser or jump to the email form if
	 * not needed.
	 * 
	 * @param ureq The user request
	 */
	private void displayLanguageChooserStep(UserRequest ureq) {
		if(i18nModule.getEnabledLanguageKeys().size() == 1) {
			displayDisclaimer(ureq);
		} else {
			createLanguageForm(ureq, getWindowControl());
		}
	}
	
	/**
	 * Display the disclaimer if enabled, else jump to the
	 * email form.
	 * 
	 * @param ureq The user request
	 */
	private void displayDisclaimer(UserRequest ureq) {
		if(registrationModule.isDisclaimerEnabled()) {
			wizInfoController.setCurStep(steps.getStep(STEP_DISCLAIMER));
			myContent.contextPut("text", translate("step4.reg.text"));
			
			removeAsListenerAndDispose(disclaimerController);
			disclaimerController = new DisclaimerController(ureq, getWindowControl(), null, false);
			listenTo(disclaimerController);
			
			regarea.setContent(disclaimerController.getInitialComponent());
		} else {
			displayEmailForm(ureq);
		}
	}
	
	private void displayEmailForm(UserRequest ureq) {
		wizInfoController.setCurStep(steps.getStep(STEP_EMAIL));

		removeAsListenerAndDispose(emailSendForm);
		emailSendForm = new EmailSendingForm(ureq, getWindowControl());
		listenTo(emailSendForm);
		
		myContent.contextPut("text", translate("step1.reg.text"));
		regarea.setContent(emailSendForm.getInitialComponent());
	}
	
	private void processEmail(UserRequest ureq) {
		// validation
		// was ok
		wizInfoController.setCurStep(steps.getStep(STEP_EMAIL));
		// Email requested for tempkey
		//save the fields somewhere
		String email = emailSendForm.getEmailAddress();
		myContent.contextPut("email", email);
		myContent.contextPut("text", translate("step2.reg.text", email));
		regarea.setVisible(false);
		// get remote address
		String ip = ureq.getHttpReq().getRemoteAddr();
		String serverpath = Settings.getServerContextPathURI();
		String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
		String[] whereFromAttrs = new String[]{ serverpath, today };

		if (registrationManager.isRegistrationPending(email) || userManager.isEmailAllowed(email)) {
			TemporaryKey tk = null;
			if (userModule.isEmailUnique()) {
				tk = registrationManager.loadTemporaryKeyByEmail(email);
			}
			if (tk == null) {
				tk = registrationManager.loadOrCreateTemporaryKeyByEmail(email, ip,
						RegistrationManager.REGISTRATION, registrationModule.getValidUntilHoursGui());
			}
			myContent.contextPut("regKey", tk.getRegistrationKey());
			
			String link = serverpath + DispatcherModule.getPathDefault() + "registration/index.html?key=" + tk.getRegistrationKey() + "&language=" + i18nModule.getLocaleKey(ureq.getLocale());
			String[] bodyAttrs = new String[]{
				serverpath,										//0
				tk.getRegistrationKey(),						//1
				i18nModule.getLocaleKey(ureq.getLocale()),		//2
				"<a href=\"" + link + "\">" + link + "</a>"		//3
			};
			
			String body = translate("reg.body", bodyAttrs);
			boolean htmlBody = StringHelper.isHtml(body);
			if(!htmlBody) {
				body += SEPARATOR + translate("reg.wherefrom", whereFromAttrs);
			}
			
			if(sendMessage(email, translate("reg.subject"), body)) {
				showInfo("email.sent");
			} else {
				showError("email.notsent");
			}
		} else {
			// if users with this email address exists, they are informed.
			List<Identity> identities = userManager.findIdentitiesByEmail(Collections.singletonList(email));
			for (Identity identity: identities) {
				String subject = translate("login.subject");
				String username = StringHelper.containsNonWhitespace(identity.getUser().getNickName()) ? identity.getUser().getNickName() : identity.getName();
				String body = translate("login.body", username) + SEPARATOR + translate("reg.wherefrom", whereFromAttrs);
				sendMessage(email, subject, body);
			}
			showError("email.notsent");
		}
	}
	
	private boolean sendMessage(String email, String subject, String body) {
		boolean isMailSent = false;
		
		try {
			MailBundle bundle = new MailBundle();
			bundle.setTo(email);
			bundle.setContent(subject, body);
			boolean htmlBody = StringHelper.isHtml(body);
			MailerResult result = mailManager.sendExternMessage(bundle, null, htmlBody);
			if (result.isSuccessful()) {
				isMailSent = true;
			}
		} catch (Exception e) {
			// nothing to do, emailSent flag is false, errors will be reported to user
		}
		
		return isMailSent;
	}
	
	private void displayRegistrationForm(UserRequest ureq) {
		wizInfoController.setCurStep(steps.getStep(STEP_REGISTRATION_FORM));
		Map<String,String> userAttrs = new HashMap<>();

		myContent.contextPut("text", translate("step3.reg.text"));
		if (tempKey != null) {
			myContent.contextPut("email", tempKey.getEmailAddress());
			userAttrs.put("email", tempKey.getEmailAddress());
		}
		
		if(registrationModule.getUsernamePresetBean() != null) {
			UserNameCreationInterceptor interceptor = registrationModule.getUsernamePresetBean();
			String proposedUsername = interceptor.getUsernameFor(userAttrs);
			if(proposedUsername == null) {
				if(interceptor.allowChangeOfUsername()) {
					createRegForm2(ureq, null, userAttrs.get("email"), false, false);
				} else {
					myContent = setErrorPage("reg.error.no_username", getWindowControl());
				}
			} else {
				Identity identity = securityManager.findIdentityByName(proposedUsername);
				if(identity != null) {
					if(interceptor.allowChangeOfUsername()) {
						createRegForm2(ureq, proposedUsername, userAttrs.get("email"), true, false);
					} else {
						myContent = setErrorPage("reg.error.user_in_use", getWindowControl());
					}
				} else {
					createRegForm2(ureq, proposedUsername, userAttrs.get("email"), false, !interceptor.allowChangeOfUsername());
				}
			}
		} else {
			createRegForm2(ureq, null, userAttrs.get("email"), false, false);
		}
	}
	
	private void createRegForm2(UserRequest ureq, String proposedUsername, String email, boolean userInUse, boolean usernameReadonly) {
		registrationForm = new RegistrationForm2(ureq, getWindowControl(), i18nModule.getLocaleKey(getLocale()),
				proposedUsername, email, userInUse, usernameReadonly);
		listenTo(registrationForm);
		regarea.setContent(registrationForm.getInitialComponent());
	}
	
	private void displayRegistrationAdditionalForm(UserRequest ureq) {
		wizInfoController.setCurStep(steps.getStep(STEP_ADDITIONAL_REGISTRATION_FORM));
		myContent.contextPut("text", translate("step.add.reg.text"));
		String emailAddress = tempKey != null? tempKey.getEmailAddress(): null;
		myContent.contextPut("email", emailAddress);

		Map<String,String> userAttrs = new HashMap<>();
		userAttrs.put("email", emailAddress);
		
		registrationAdditionalForm = new RegistrationAdditionalForm(ureq, getWindowControl());
		listenTo(registrationAdditionalForm);
		regarea.setContent(registrationAdditionalForm.getInitialComponent());
	}

	/**
	 * OO-92
	 * 
	 * displays the final step of the registration process. (step5)<br />
	 * see also _content/finish.html
	 * 
	 * @param User
	 *            The newly created User from which to display information
	 * 
	 */
	private void displayFinalStep(Identity persitedIdentity){
		wizInfoController.setCurStep(steps.getStep(STEP_FINAL));
		
		// hide the text we don't need anymore 
		myContent.contextPut("text", "");
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(RegistrationForm2.USERPROPERTIES_FORM_IDENTIFIER, false);
		List<UserPropertyHandler> aggregatedUserPropertyHandlers = new ArrayList<>(userPropertyHandlers);
		if(registrationAdditionalForm != null) {
			List<UserPropertyHandler> addUserPropertyHandlers = userManager.getUserPropertyHandlersFor(RegistrationAdditionalForm.USERPROPERTIES_FORM_IDENTIFIER, false);
			aggregatedUserPropertyHandlers.addAll(addUserPropertyHandlers);
		}

		VelocityContainer finishVC = createVelocityContainer("finish");
		finishVC.contextPut("userPropertyHandlers", aggregatedUserPropertyHandlers);
		finishVC.contextPut("user", persitedIdentity.getUser());
		finishVC.contextPut("locale", getLocale());
		finishVC.contextPut("username", registrationForm.getLogin());
		
		boolean pending = persitedIdentity.getStatus().equals(Identity.STATUS_PENDING);
		if(pending) {
			finishVC.contextPut("text", translate("step5.reg.pending", new String[]{ registrationForm.getLogin() }));
		} else {
			finishVC.contextPut("text", translate("step5.reg.text", new String[]{ registrationForm.getLogin() }));
		}
		finishVC.contextPut("pending", Boolean.valueOf(pending));
		loginButton = LinkFactory.createButton("form.login", finishVC, this);
		loginButton.setCustomEnabledLinkCSS("btn btn-primary");
		loginButton.setUserObject(persitedIdentity);
		finishVC.put("loginhref", loginButton);
		
		regarea.setContent(finishVC);
	}
	
	/**
	 * OO-92
	 * this will finally create the user, set all it's userproperties
	 * 
	 * @return User the newly created, persisted User Object
	 */
	private Identity createNewUserAfterRegistration() {
		// create user with mandatory fields from registration-form
		User volatileUser = userManager.createUser(registrationForm.getFirstName(), registrationForm.getLastName(), registrationForm.getEmail());
		// set user configured language
		Preferences preferences = volatileUser.getPreferences();

		preferences.setLanguage(registrationForm.getLangKey());
		volatileUser.setPreferences(preferences);
		// create an identity with the given username / pwd and the user object
		String login = registrationForm.getLogin();
		String pwd = registrationForm.getPassword();
		Identity persistedIdentity = registrationManager.createNewUserAndIdentityFromTemporaryKey(login, pwd, volatileUser, tempKey);
		if (persistedIdentity == null) {
			showError("user.notregistered");
			return null;
		} else {
			// update other user properties from form
			User persistedUser = persistedIdentity.getUser();
			
			// Enrol user to auto enroled courses
			SelfRegistrationAdvanceOrderInput input = new SelfRegistrationAdvanceOrderInput();
			input.setIdentity(persistedIdentity);
			input.setRawValues(registrationModule.getAutoEnrolmentRawValue());
			autoAccessManager.createAdvanceOrders(input);
			autoAccessManager.grantAccessToCourse(persistedIdentity);
			
			//add eventually static value
			if(registrationModule.isStaticPropertyMappingEnabled()) {
				String propertyName = registrationModule.getStaticPropertyMappingName();
				String propertyValue = registrationModule.getStaticPropertyMappingValue();
				if(StringHelper.containsNonWhitespace(propertyName)
						&& StringHelper.containsNonWhitespace(propertyValue)
						&& userPropertiesConfig.getPropertyHandler(propertyName) != null) {
					try {
						persistedUser.setProperty(propertyName, propertyValue);
					} catch (Exception e) {
						logError("Cannot set the static property value", e);
					}
				}
			}

			// add value of registration form
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(RegistrationForm2.USERPROPERTIES_FORM_IDENTIFIER, false);
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				FormItem fi = registrationForm.getPropFormItem(userPropertyHandler.getName());
				userPropertyHandler.updateUserFromFormItem(persistedUser, fi);
			}
			
			// add value of additional registration form
			if(registrationAdditionalForm != null) {
				List<UserPropertyHandler> addUserPropertyHandlers = userManager.getUserPropertyHandlersFor(RegistrationAdditionalForm.USERPROPERTIES_FORM_IDENTIFIER, false);
				for (UserPropertyHandler userPropertyHandler : addUserPropertyHandlers) {
					FormItem fi = registrationAdditionalForm.getPropFormItem(userPropertyHandler.getName());
					userPropertyHandler.updateUserFromFormItem(persistedUser, fi);
				}
			}
			
			// persist changes in db
			userManager.updateUserFromIdentity(persistedIdentity);
			// send notification mail to sys admin
			String notiEmail = registrationModule.getRegistrationNotificationEmail();
			if (notiEmail != null) {
				registrationManager.sendNewUserNotificationMessage(notiEmail, persistedIdentity);
			}

			// tell system that this user did accept the disclaimer
			registrationManager.setHasConfirmedDislaimer(persistedIdentity);
			return persistedIdentity;
		}
	}
	
	private void doLogin(UserRequest ureq, Identity persistedIdentity) {
		int loginStatus = AuthHelper.doLogin(persistedIdentity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq);
		if (loginStatus == AuthHelper.LOGIN_OK) {
			//youppi
		} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
		} else if (loginStatus == AuthHelper.LOGIN_INACTIVE) {
			getWindowControl().setError(translate("login.error.inactive", WebappHelper.getMailConfig("mailSupport")));
		} else {
			getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailReplyTo")));
		}
	}
	
	private static class Steps {
		
		private List<String> steps = new ArrayList<>();
		
		private void add(String name) {
			steps.add(name);
		}
		
		private int getStep(String name) {
			for (int i = 0; i < steps.size(); i++) {
				String stepName = steps.get(i);
				if (name.equals(stepName)) {
					return ++i;
				}
			}
			return -1;
		}
		
		private int getNumberOfSteps() {
			return steps.size();
		}

	}

}