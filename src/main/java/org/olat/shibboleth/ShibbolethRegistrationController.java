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

package org.olat.shibboleth;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.LocaleChangedEvent;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.registration.DisclaimerController;
import org.olat.registration.LanguageChooserController;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.registration.UserNameCreationInterceptor;
import org.olat.shibboleth.util.ShibbolethHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  09.08.2004
 *
 * @author Mike Stock
 * 
 * Comment:
 * User wants ShibbolethAuthentication
 * - Basic flow:
 * System asks User for username and create olataccount with ShibbolethAuthentication
 * Branches:
 * 1. no email in shibbolethAttributesMap
 * 		- System asks for emailaddress (no institutionalEmail is set !!!)
 * 2. no email in shibbolethAttributesMap and User already exists in System
 * 		- System asks for password (no institutionalEmail is set !!!)
 * 		
 */

public class ShibbolethRegistrationController extends DefaultController implements ControllerEventListener {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ShibbolethModule.class);
	private static final String KEY_SHIBATTRIBUTES = "shibattr";
	private static final String KEY_SHIBUNIQUEID = "shibuid";
	
	private VelocityContainer mainContainer;
	private ShibbolethRegistrationForm regForm;
	private ShibbolethMigrationForm migrationForm;
	private ShibbolethRegistrationWithEmailForm regWithEmailForm;
	private DisclaimerController dclController;
	private LanguageChooserController languageChooserController;
	
	private Translator translator;
	private Map<String,String> shibbolethAttributesMap;
	private String shibbolethUniqueID;
	
	private int state = STATE_UNDEFINED;
	private static final int STATE_UNDEFINED = 0;
	private static final int STATE_NEW_SHIB_USER = 1;
	private static final int STATE_MIGRATED_SHIB_USER = 2;
	private String proposedUsername;
	
	private boolean hasEmailInShibAttr;
	
	@Autowired
	private ShibbolethModule shibbolethModule;
	
	/**
	 * Implements the shibboleth registration workflow.
	 * @param ureq
	 * @param wControl 
	 */
	public ShibbolethRegistrationController(UserRequest ureq, WindowControl wControl) {
		super(wControl);

		translator = Util.createPackageTranslator(ShibbolethModule.class, ureq.getLocale());
		shibbolethAttributesMap = (Map<String,String>)ureq.getUserSession().getEntry(KEY_SHIBATTRIBUTES);
		shibbolethUniqueID = (String)ureq.getUserSession().getEntry(KEY_SHIBUNIQUEID);
		
		if (shibbolethUniqueID == null) {			
			ChiefController msgcc = MessageWindowController.createMessageChiefController(ureq,
					new AssertException("ShibbolethRegistrationController was unable to fetch ShibbolethUniqueID from session."), translator.translate("error.shibboleth.generic"), null);
			msgcc.getWindow().dispatchRequest(ureq, true);
			return;
		}

		if (shibbolethAttributesMap == null)
			throw new AssertException("ShibbolethRegistrationController was unable to fetch ShibbolethAttribuitesMap from session.");

		hasEmailInShibAttr = (shibbolethModule.getEMail() == null) ? false : true;
		
		Locale locale = (Locale)ureq.getUserSession().getEntry(LocaleNegotiator.NEGOTIATED_LOCALE);
		if(locale == null) {
			String preferedLanguage = shibbolethModule.getPreferedLanguage();
			if(preferedLanguage == null) {
				locale = LocaleNegotiator.getPreferedLocale(ureq);
			} else {
				locale = LocaleNegotiator.getNegotiatedLocale(preferedLanguage);
				if(locale == null) {
					locale = LocaleNegotiator.getPreferedLocale(ureq);
				}
			}
		}
		ureq.getUserSession().setLocale(locale);
		I18nManager.updateLocaleInfoToThread(ureq.getUserSession());
		ureq.getUserSession().putEntry(LocaleNegotiator.NEGOTIATED_LOCALE, locale);
		
		translator = Util.createPackageTranslator(ShibbolethModule.class, ureq.getLocale());
		mainContainer = new VelocityContainer("main", VELOCITY_ROOT + "/langchooser.html", translator, this);
	
		languageChooserController = new LanguageChooserController(ureq, wControl, false);
		languageChooserController.addControllerListener(this);
		mainContainer.put("select.language", languageChooserController.getInitialComponent());
		mainContainer.contextPut("languageCode", locale.getLanguage());
		
		if(RegistrationModule.getUsernamePresetBean() != null) {
			UserNameCreationInterceptor interceptor = RegistrationModule.getUsernamePresetBean();
			proposedUsername = interceptor.getUsernameFor(shibbolethAttributesMap);
			if(proposedUsername == null) {
				if(interceptor.allowChangeOfUsername()) {
					setRegistrationForm(ureq, wControl, proposedUsername);
				} else {
					setErrorPage("sm.error.no_username", wControl);
				}
			} else {
				Identity identity = BaseSecurityManager.getInstance().findIdentityByName(proposedUsername);
				if(identity != null) {
					if(interceptor.allowChangeOfUsername()) {
						setRegistrationForm(ureq, wControl, proposedUsername);
					} else {
						setErrorPage("sm.error.username_in_use", wControl);
					}
				} else if(interceptor.allowChangeOfUsername()) {
					setRegistrationForm(ureq, wControl, proposedUsername);
				} else {
					if(hasEmailInShibAttr) {
						state = STATE_NEW_SHIB_USER;
						mainContainer.setPage(VELOCITY_ROOT + "/disclaimer.html");
					} else {
						regWithEmailForm = new ShibbolethRegistrationWithEmailForm(ureq, wControl, proposedUsername);
						regWithEmailForm.addControllerListener(this);
						//mainContainer.put("regWithEmailForm", regWithEmailForm);
						mainContainer.setPage(VELOCITY_ROOT + "/registerwithemail.html");
					}	
				}
			}
		} else {
			setRegistrationForm(ureq, wControl, null);
		}

		dclController = new DisclaimerController(ureq, getWindowControl());
		dclController.addControllerListener(this);
		mainContainer.put("dclComp", dclController.getInitialComponent());
		
		// load view in layout
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, mainContainer, null);
		setInitialComponent(layoutCtr.getInitialComponent());
	}
	
	private void setErrorPage(String errorKey, WindowControl wControl) {
		String error = translator.translate(errorKey);
		wControl.setError(error);
		mainContainer.contextPut("error_msg", error);
		mainContainer.setPage(VELOCITY_ROOT + "/error.html");
	}
	
	private void setRegistrationForm(UserRequest ureq, WindowControl wControl, String proposedUsername) {
		regForm = new ShibbolethRegistrationForm(ureq, wControl, proposedUsername);
		regForm.addControllerListener(this);
		mainContainer.put("regForm", regForm.getInitialComponent());
	}
	
	/**
	 * Put shibboleth attributes map in reqest for later usage.
	 * @param req
	 * @param attributes
	 */
	public static final void putShibAttributes(HttpServletRequest req, Map<String,String> attributes) {
		CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(req).putEntry(KEY_SHIBATTRIBUTES, attributes);
	}
	
	/**
	 * Put shibboleth unique identifier in request for later usage.
	 * @param req
	 * @param uniqueID
	 */
	public static final void putShibUniqueID(HttpServletRequest req, String uniqueID) {
		CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(req).putEntry(KEY_SHIBUNIQUEID, uniqueID);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof LocaleChangedEvent) {
			LocaleChangedEvent lce = (LocaleChangedEvent)event;
			Locale newLocale = lce.getNewLocale();
			translator.setLocale(newLocale);
			dclController.changeLocale(newLocale);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == migrationForm) {
			if (event == Event.CANCELLED_EVENT) {
				mainContainer.setPage(VELOCITY_ROOT + "/register.html");
			} else if (event == Event.DONE_EVENT) {
				state = STATE_MIGRATED_SHIB_USER;
				mainContainer.setPage(VELOCITY_ROOT + "/disclaimer.html");
			}
		} else if (source == regWithEmailForm){
			if (event == Event.CANCELLED_EVENT) {
				mainContainer.setPage(VELOCITY_ROOT + "/register.html");
			} else if (event == Event.DONE_EVENT) {
				state = STATE_NEW_SHIB_USER;
				mainContainer.setPage(VELOCITY_ROOT + "/disclaimer.html");
			}		
		} else if (source == regForm) {
			if (event == Event.DONE_EVENT) {
				String choosenLogin = regForm.getLogin();
				BaseSecurity secMgr = BaseSecurityManager.getInstance();
				Identity identity = secMgr.findIdentityByName(choosenLogin);

				
				if (identity == null) { // ok, create new user
					if (!hasEmailInShibAttr){
						regWithEmailForm = new ShibbolethRegistrationWithEmailForm(ureq, getWindowControl(), choosenLogin);
						regWithEmailForm.addControllerListener(this);
						mainContainer.put("regWithEmailForm", regWithEmailForm.getInitialComponent());
						mainContainer.setPage(VELOCITY_ROOT + "/registerwithemail.html");
					} else { // there is an emailaddress
						state = STATE_NEW_SHIB_USER;
						mainContainer.setPage(VELOCITY_ROOT + "/disclaimer.html");
					}
				} else { // offer identity migration, if OLAT provider exists
					Authentication auth = secMgr.findAuthentication(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier());					
					if (auth == null) { // no OLAT provider, migration not possible...
						getWindowControl().setError(translator.translate("sr.error.loginexists", new String[] {WebappHelper.getMailConfig("mailSupport")}));
					}	else { // OLAT provider exists, offer migration...
						migrationForm = new ShibbolethMigrationForm(ureq, getWindowControl(), auth);
						migrationForm.addControllerListener(this);
						mainContainer.put("migrationForm", migrationForm.getInitialComponent());
						mainContainer.setPage(VELOCITY_ROOT + "/migration.html");
					}
				}
			}
		} else if (source == languageChooserController) {
			if (event == Event.DONE_EVENT) { // language choosed
				mainContainer.setPage(VELOCITY_ROOT + "/register.html");
				ureq.getUserSession().removeEntry(LocaleNegotiator.NEGOTIATED_LOCALE);
			} else if (event instanceof LanguageChangedEvent) {
				LanguageChangedEvent lcev = (LanguageChangedEvent)event;
				translator.setLocale(lcev.getNewLocale());
				dclController.changeLocale(lcev.getNewLocale());
			}
		} else if (source == dclController) {
			if (event == Event.DONE_EVENT) { // disclaimer accepted...
				if (state == STATE_NEW_SHIB_USER) { // ...proceed and create user
					String choosenLogin;
					if(regForm == null ) {
						choosenLogin = proposedUsername;
					} else {
						choosenLogin = regForm.getLogin();
					}
					
					// check if login has been taken by another user in the meantime...
					BaseSecurity secMgr = BaseSecurityManager.getInstance();

					// check if login has been taken by another user in the meantime...
					Identity identity = secMgr.findIdentityByName(choosenLogin);
					if (identity != null) {
						getWindowControl().setError(translator.translate("sr.login.meantimetaken"));
						mainContainer.setPage(VELOCITY_ROOT + "/register.html");
						state = STATE_UNDEFINED;
						return;
					}

					String email;
					if(!hasEmailInShibAttr){
						email = regWithEmailForm.getEmail();
					} else {
						email = ShibbolethHelper.getFirstValueOf(shibbolethModule.getEMail(), shibbolethAttributesMap);
					}

					User user = null;
					Identity id = UserManager.getInstance().findIdentityByEmail(email);
					if (id != null) {
						user = id.getUser();
					}
					
					if (user != null) {
						// error, email already exists. should actually not happen if OLAT Authenticator has
						// been set after removing shibboleth authenticator
						getWindowControl().setError(translator.translate("sr.error.emailexists", new String[] {WebappHelper.getMailConfig("mailSupport")}));
						mainContainer.setPage(VELOCITY_ROOT + "/register.html");
						state = STATE_UNDEFINED;
						return;
					}

					String firstName = shibbolethAttributesMap.get(shibbolethModule.getFirstName());
					String lastName = shibbolethAttributesMap.get(shibbolethModule.getLastName());
					user = UserManager.getInstance().createUser(firstName, lastName, email);
					user.setProperty(UserConstants.INSTITUTIONALNAME, shibbolethAttributesMap.get(shibbolethModule.getInstitutionalName()));
					if(hasEmailInShibAttr){
						String institutionalEmail = ShibbolethHelper.getFirstValueOf(shibbolethModule.getInstitutionalEMail(), shibbolethAttributesMap);
						user.setProperty(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
					}
					user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, shibbolethAttributesMap.get(shibbolethModule.getInstitutionalUserIdentifier()));
					identity = secMgr.createAndPersistIdentityAndUser(choosenLogin, null, user, ShibbolethDispatcher.PROVIDER_SHIB, shibbolethUniqueID);
					SecurityGroup olatUserGroup = secMgr.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
					secMgr.addIdentityToSecurityGroup(identity, olatUserGroup);
					// tell system that this user did accept the disclaimer
					CoreSpringFactory.getImpl(RegistrationManager.class).setHasConfirmedDislaimer(identity);
					doLogin(identity, ureq);
					return;
				} else if (state == STATE_MIGRATED_SHIB_USER) { // ...proceed and migrate user
					// create additional authentication
					Authentication auth = migrationForm.getAuthentication();
					Identity authenticationedIdentity = auth.getIdentity();
					BaseSecurity secMgr = BaseSecurityManager.getInstance();
					secMgr.createAndPersistAuthentication(authenticationedIdentity, ShibbolethDispatcher.PROVIDER_SHIB, shibbolethUniqueID, null, null);
					
					// update user profile
					User user = authenticationedIdentity.getUser();
					String s = shibbolethAttributesMap.get(shibbolethModule.getFirstName());
					if (s != null) user.setProperty(UserConstants.FIRSTNAME, s);
					s = shibbolethAttributesMap.get(shibbolethModule.getLastName());
					if (s != null) user.setProperty(UserConstants.LASTNAME, s);
					s = shibbolethAttributesMap.get(shibbolethModule.getInstitutionalName());
					if (s != null) user.setProperty(UserConstants.INSTITUTIONALNAME, s);		
					s = ShibbolethHelper.getFirstValueOf(shibbolethModule.getInstitutionalEMail(), shibbolethAttributesMap);
					if (s != null) user.setProperty(UserConstants.INSTITUTIONALEMAIL, s);
					s = shibbolethAttributesMap.get(shibbolethModule.getInstitutionalUserIdentifier());
					if (s != null) user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, s);
					UserManager.getInstance().updateUser(user);
					doLogin(authenticationedIdentity, ureq);
					return;
				}
			} else if (event == Event.CANCELLED_EVENT) {
				mainContainer.setPage(VELOCITY_ROOT + "/register.html");
				getWindowControl().setError(translator.translate("sr.error.disclaimer"));
			}
		}
	}

	private void doLogin(Identity identity, UserRequest ureq) {
		int loginStatus = AuthHelper.doLogin(identity, ShibbolethDispatcher.PROVIDER_SHIB, ureq);
		if (loginStatus != AuthHelper.LOGIN_OK) {
			//REVIEW:2010-01-11:revisited:pb: do not redirect if already MediaResource is set before
			//ureq.getDispatchResult().setResultingMediaResource(resultingMediaResource);
			//instead set the media resource accordingly
			//pb -> provide a DispatcherAction.getDefaultDispatcherRedirectMediaresource();
			//to be used here. (and some more places like CatalogController.
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp()); // error, redirect to login screen
			return;
		}
		// successfull login
		ureq.getUserSession().getIdentityEnvironment().addAttributes(
				shibbolethModule.getAttributeTranslator().translateAttributesMap(shibbolethAttributesMap));
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (dclController != null) {
			dclController.dispose();
			dclController = null;
		}
		
		if (languageChooserController != null) {
			languageChooserController.dispose();
			languageChooserController = null;
		}
	}

}
