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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.WizardInfoController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.manager.MailManager;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * Controls the registration workflow.
 * 
 * <P>
 * @author Sabina Jeger
 */
public class RegistrationController extends BasicController {

	private static final String SEPARATOR = "____________________________________________________________________\n";

	private VelocityContainer myContent;	
	private Panel regarea;
	private WizardInfoController wic;
	private DisclaimerController dclController;
	private RegistrationManager rm = RegistrationManager.getInstance();
	private EmailSendingForm ef;
	private RegistrationForm2 rf2;
	private LanguageChooserController lc;
	private String regKey;
	private TemporaryKeyImpl tempKey;

	/**
	 * Controller implementing registration work flow.
	 * @param ureq
	 * @param wControl
	 */
	public RegistrationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);		
		if (!RegistrationModule.isSelfRegistrationEnabled()) { throw new OLATRuntimeException(RegistrationController.class,
				"Registration controller launched but self registration is turned off in the config file", null); }
		// override language when not the same as in ureq and add fallback to
		// property handler translator for user properties
		String lang = ureq.getParameter("lang");
		if (lang != null && ! lang.equals(I18nManager.getInstance().getLocaleKey(getLocale()))) {
			Locale loc = I18nManager.getInstance().getLocaleOrDefault(lang);
			ureq.getUserSession().setLocale(loc);
			setLocale(loc, true);
			setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(Util.createPackageTranslator(this.getClass(), loc)));			
		}	else {
			// set fallback only
			setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));			
		}
		
		//construct content
		myContent = createVelocityContainer("reg");
		wic = new WizardInfoController(ureq, 5);
		listenTo(wic);
		myContent.put("regwizard", wic.getInitialComponent());
		regarea = new Panel("regarea");
		myContent.put("regarea", regarea);
		regKey = ureq.getHttpReq().getParameter("key");
		if (regKey == null || regKey.equals("")) {
			// no temporary key is given, we assume step 1. If this is the case, we
			// render in a modal dialog, no need to add the 3cols layout controller
			// wrapper
			//fxdiff FXOLAT-113: business path in DMZ
			if(I18nModule.getEnabledLanguageKeys().size() == 1) {
				wic.setCurStep(2);
				createEmailForm(ureq);
			} else {
				createLanguageForm(ureq, wControl);
			}
			putInitialPanel(myContent);
		} else {
			// we check if given key is a valid temporary key
			tempKey = rm.loadTemporaryKeyByRegistrationKey(regKey);
			// if key is not valid we redirect to first page
			if (tempKey == null) {
				// error, there should be an entry
				showError("regkey.missingentry");
				//fxdiff FXOLAT-113: business path in DMZ
				if(I18nModule.getEnabledLanguageKeys().size() == 1) {
					wic.setCurStep(2);
					createEmailForm(ureq);
				} else {
					createLanguageForm(ureq, wControl);
				}
			} else {
				wic.setCurStep(3);
				myContent.contextPut("pwdhelp", translate("pwdhelp"));
				myContent.contextPut("loginhelp", translate("loginhelp"));
				myContent.contextPut("text", translate("step3.reg.text"));
				myContent.contextPut("email", tempKey.getEmailAddress());

				Map<String,String> userAttrs = new HashMap<String,String>();
				userAttrs.put("email", tempKey.getEmailAddress());
				
				if(RegistrationModule.getUsernamePresetBean() != null) {
					UserNameCreationInterceptor interceptor = RegistrationModule.getUsernamePresetBean();
					String proposedUsername = interceptor.getUsernameFor(userAttrs);
					if(proposedUsername == null) {
						if(interceptor.allowChangeOfUsername()) {
							createRegForm2(ureq, null, false, false);
						} else {
							myContent = setErrorPage("reg.error.no_username", wControl);
						}
					} else {
						Identity identity = BaseSecurityManager.getInstance().findIdentityByName(proposedUsername);
						if(identity != null) {
							if(interceptor.allowChangeOfUsername()) {
								createRegForm2(ureq, proposedUsername, true, false);
							} else {
								myContent = setErrorPage("reg.error.user_in_use", wControl);
							}
						} else if (interceptor.allowChangeOfUsername()) {
							createRegForm2(ureq, proposedUsername, false, false);
						} else {
							createRegForm2(ureq, proposedUsername, false, true);
						}
					}
				} else {
					createRegForm2(ureq, null, false, false);
				}
			}
			// load view in layout
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, myContent, null);
			listenTo(layoutCtr);
			putInitialPanel(layoutCtr.getInitialComponent());
		}		
	}
	
	private VelocityContainer setErrorPage(String errorKey, WindowControl wControl) {
		String error = getTranslator().translate(errorKey);
		wControl.setError(error);
		VelocityContainer errorContainer = createVelocityContainer("error");
		errorContainer.contextPut("errorMsg", error);
		return errorContainer;
	}
	
	private void createRegForm2(UserRequest ureq, String proposedUsername, boolean userInUse, boolean usernameReadonly) {
		rf2 = new RegistrationForm2(ureq, getWindowControl(), I18nManager.getInstance().getLocaleKey(getLocale()), proposedUsername, userInUse, usernameReadonly);
		listenTo(rf2);
		regarea.setContent(rf2.getInitialComponent());
	}
	
	private void createLanguageForm(UserRequest ureq, WindowControl wControl) {
		removeAsListenerAndDispose(lc);
		lc = new LanguageChooserController(ureq, wControl, true);
		listenTo(lc);
		myContent.contextPut("text", translate("select.language.description"));
		regarea.setContent(lc.getInitialComponent());
	}

	/**
	 * just needed for creating EmailForm
	 */
	private void createEmailForm(UserRequest ureq) {
		removeAsListenerAndDispose(ef);
		ef = new EmailSendingForm(ureq, getWindowControl());
		listenTo(ef);
		
		myContent.contextPut("text", translate("step1.reg.text"));
		regarea.setContent(ef.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == ef) {
			if (event == Event.DONE_EVENT) { // form
				// validation
				// was ok
				wic.setCurStep(2);
				// Email requested for tempkey
				//save the fields somewhere
				String email = ef.getEmailAddress();
				myContent.contextPut("email", email);
				myContent.contextPut("text", translate("step2.reg.text", email));
				//ef.setVisible(false);
				regarea.setVisible(false);
				// look for user in "Person" and "user" tables
				boolean foundUser = UserManager.getInstance().userExist(email);
				// get remote address
				String ip = ureq.getHttpReq().getRemoteAddr();
				String body = null;
				String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
				MailManager mailM = MailManager.getInstance();
				//TODO eMail Vorlagen
				String serverpath = Settings.getServerContextPathURI();
				boolean isMailSent = false;
				if (!foundUser) {
					TemporaryKey tk = rm.loadTemporaryKeyByEmail(email);
					if (tk == null) tk = rm.createTemporaryKeyByEmail(email, ip, rm.REGISTRATION);
					myContent.contextPut("regKey", tk.getRegistrationKey());
					body = getTranslator().translate("reg.body",
							new String[] { serverpath, tk.getRegistrationKey(), I18nManager.getInstance().getLocaleKey(ureq.getLocale()) })
							+ SEPARATOR
							+ getTranslator().translate("reg.wherefrom", new String [] { serverpath, today, ip });
					try {
						MailerResult result = mailM.sendExternMessage(null, null, null, email, null, null, null, translate("reg.subject"), body, null, null);
						if (MailerResult.OK == result.getReturnCode()) isMailSent = true;
					} catch (Exception e) {
						// nothing to do, emailSent flag is false, errors will be reported to user
					}
				} else {
					// a user exists, this is an error in the registration page
					// send email
					Identity identity = UserManager.getInstance().findIdentityByEmail(email);
					body = translate("login.body", identity.getName()) + SEPARATOR
							+ getTranslator().translate("reg.wherefrom", new String[] { serverpath, today, ip });
					try {
						MailerResult result = mailM.sendExternMessage(null, null, null, email, null, null, null, translate("login.subject"), body, null, null);
						if (MailerResult.OK == result.getReturnCode()) isMailSent = true;
					} catch (Exception e) {
						// nothing to do, emailSent flag is false, errors will be reported to user
					}
				}
				if (isMailSent) {
					showInfo("email.sent");
				} else showError("email.notsent");
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if (source == lc) {
			if (event == Event.DONE_EVENT) {
				wic.setCurStep(2);
				createEmailForm(ureq);
				ureq.getUserSession().removeEntry(LocaleNegotiator.NEGOTIATED_LOCALE);
			} else if (event == Event.CANCELLED_EVENT) {
				ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
			} else if (event instanceof LanguageChangedEvent) {
				LanguageChangedEvent lcev = (LanguageChangedEvent)event;
				setLocale(lcev.getNewLocale(), true);
				myContent.contextPut("text", translate("select.language.description"));
			}
		} else if (source == rf2) {
			// Userdata entered
			if (event == Event.DONE_EVENT) {
				String lang = rf2.getLangKey();
				// change language if different then current language
				if (! lang.equals(I18nManager.getInstance().getLocaleKey(ureq.getLocale()))) {
					Locale loc = I18nManager.getInstance().getLocaleOrDefault(lang);
					ureq.getUserSession().setLocale(loc);
					getTranslator().setLocale(loc);					
				}

				
				wic.setCurStep(4);
				myContent.contextPut("pwdhelp", "");
				myContent.contextPut("loginhelp", "");
				myContent.contextPut("text", translate("step4.reg.text"));
				
				removeAsListenerAndDispose(dclController);
				dclController = new DisclaimerController(ureq, getWindowControl());
				listenTo(dclController);
				
				regarea.setContent(dclController.getInitialComponent());
			} else if (event == Event.CANCELLED_EVENT) {
				ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
			}
		} else if (source == dclController) {
			if (event == Event.DONE_EVENT) {

				wic.setCurStep(5);
				myContent.contextRemove("text");
				myContent.contextPut("pwdhelp", "");
				myContent.contextPut("loginhelp", "");
				myContent.contextPut("disclaimer", "");
				//myContent.contextPut("yourdata", translate("step5.reg.yourdata"));
				
				rf2.freeze();
				regarea.setContent(rf2.getInitialComponent());
				
				// create user with mandatory fields from registrationform
				//FIXME
				UserManager um = UserManager.getInstance();
				User volatileUser = um.createUser(
						rf2.getFirstName(),
						rf2.getLastName(),
						tempKey.getEmailAddress()
				);
				// set user configured language
				Preferences preferences = volatileUser.getPreferences();
				
				preferences.setLanguage(rf2.getLangKey());
				volatileUser.setPreferences(preferences);
				// create an identity with the given username / pwd and the user object
				String login = rf2.getLogin();
				String pwd = rf2.getPassword();
				Identity persistedIdentity = rm.createNewUserAndIdentityFromTemporaryKey(login, pwd, volatileUser, tempKey);
				if (persistedIdentity == null) {
					showError("user.notregistered");
				} else {
					// update other user properties from form
					List<UserPropertyHandler> userPropertyHandlers = um.getUserPropertyHandlersFor(RegistrationForm2.USERPROPERTIES_FORM_IDENTIFIER, false);
					User persistedUser = persistedIdentity.getUser();
					for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
						FormItem fi = rf2.getPropFormItem(userPropertyHandler.getName());
						userPropertyHandler.updateUserFromFormItem(persistedUser, fi);
					}
					// persist changes in db
					um.updateUserFromIdentity(persistedIdentity);
					// send notification mail to sys admin
					String notiEmail = RegistrationModule.getRegistrationNotificationEmail();
					if (notiEmail != null) {
						rm.sendNewUserNotificationMessage(notiEmail, persistedIdentity);
					}			
					
					// tell system that this user did accept the disclaimer
					RegistrationManager.getInstance().setHasConfirmedDislaimer(persistedIdentity);
					
					// show last screen
					myContent.contextPut("text", getTranslator().translate(
							"step5.reg.text",
							new String[] { WebappHelper.getServletContextPath(), login }
					));
				}


			} else if (event == Event.CANCELLED_EVENT) {
				ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
			}
		}

	}

	protected void doDispose() {
		//
	}

}