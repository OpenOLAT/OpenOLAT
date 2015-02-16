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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hibernate.type.StandardBasicTypes;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class RegistrationManager extends BasicManager {

	public static final String PW_CHANGE = "PW_CHANGE";
	public static final String REGISTRATION = "REGISTRATION";//fxdiff FXOLAT-113: business path in DMZ
	public static final String EMAIL_CHANGE = "EMAIL_CHANGE";
	protected static final int REG_WORKFLOW_STEPS = 5;
	protected static final int PWCHANGE_WORKFLOW_STEPS = 4;
	
	private RegistrationModule registrationModule;
	private MailManager mailManager;
	private BaseSecurity securityManager;

	private RegistrationManager() {
		// singleton
	}

	/**
	 * @return Manager instance.
	 */
	public static RegistrationManager getInstance() {
		return new RegistrationManager();
	}
	
	/**
	 * [used by Spring]
	 * @param mailManager
	 */
	public void setMailManager(MailManager mailManager) {
		this.mailManager = mailManager;
	}

	/**
	 * [used by Spring]
	 * @param registrationModule
	 */
	public void setRegistrationModule(RegistrationModule registrationModule) {
		this.registrationModule = registrationModule;
	}
	
	/**
	 * [used by Spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}

	public boolean validateEmailUsername(String email) {
		List<String> whiteList = registrationModule.getDomainList();
		if(whiteList.isEmpty()) {
			return true;
		}
		
		if(!StringHelper.containsNonWhitespace(email)) {
			return false;
		}
		int index = email.indexOf('@');
		if(index < 0 || index+1 >= email.length()) {
			return false;
		}
		
		String emailDomain = email.substring(index+1);
		boolean valid = false;
		for(String domain:whiteList) {
			try {
				String pattern = convertDomainPattern(domain);
				if(emailDomain.matches(pattern)) {
					valid = true;
					break;
				}
			} catch (Exception e) {
				logError("Error matching an email adress", e);
			}
		}
		return valid;
	}
	
	/**
	 * Validate the white list (prevent exception from regex matcher)
	 * @param list
	 * @return
	 */
	public List<String> validateWhiteList(List<String> list) {
		if(list.isEmpty()) {
			return Collections.emptyList();
		}
		
		String emailDomain = "openolat.org";
		List<String> errors = new ArrayList<String>();
		for(String domain:list) {
			try {
				String pattern = convertDomainPattern(domain);
				emailDomain.matches(pattern);
			} catch (Exception e) {
				errors.add(domain);
				logError("Error matching an email adress", e);
			}
		}
		return errors;
	}
	
	private String convertDomainPattern(String domain) {
		if(domain.indexOf('*') >= 0) {
			domain = domain.replace("*", ".*");
		}
		return domain;
	}

	/**
	 * creates a new user and identity with the data of the temporary key (email) and other
	 * supplied user data (within myUser)
	 * 
	 * @param login Login name
	 * @param pwd Password
	 * @param myUser Not yet persisted user object
	 * @param tk Temporary key
	 * @return the newly created subject or null
	 */
	public Identity createNewUserAndIdentityFromTemporaryKey(String login, String pwd, User myUser, TemporaryKeyImpl tk) {
		Identity identity = securityManager.createAndPersistIdentityAndUserWithDefaultProviderAndUserGroup(login, null, pwd, myUser);
		if (identity == null) return null;
		deleteTemporaryKey(tk);
		return identity;
	}

	/**
	 * Send a notification messaged to the given notification email address about the registratoin of 
	 * the given new identity.
	 * @param notificationMailAddress Email address who should be notified. MUST NOT BE NULL
	 * @param newIdentity The newly registered Identity
	 */
	public void sendNewUserNotificationMessage(String notificationMailAddress, Identity newIdentity) {
		Address from;
		Address[] to;
		try {
			// fxdiff: change from/replyto, see FXOLAT-74
			from = new InternetAddress(WebappHelper.getMailConfig("mailReplyTo"));
			to = new Address[] { new InternetAddress(notificationMailAddress)};
		} catch (AddressException e) {
			logError("Could not send registration notification message, bad mail address", e);
			return;
		}
		MailerResult result = new MailerResult();
		User user = newIdentity.getUser();
		Locale loc = I18nModule.getDefaultLocale();
		String[] userParams = new  String[] {newIdentity.getName(), user.getProperty(UserConstants.FIRSTNAME, loc), user.getProperty(UserConstants.LASTNAME, loc), user.getProperty(UserConstants.EMAIL, loc),
				user.getPreferences().getLanguage(), Settings.getServerDomainName() + WebappHelper.getServletContextPath() };
		Translator trans = Util.createPackageTranslator(RegistrationManager.class, loc);
		String subject = trans.translate("reg.notiEmail.subject", userParams);
		String body = trans.translate("reg.notiEmail.body", userParams);
		
		MimeMessage msg = mailManager.createMimeMessage(from, to, null, null, body, subject, null, result);
		mailManager.sendMessage(msg, result);
		if (result.getReturnCode() != MailerResult.OK ) {
			logError("Could not send registration notification message, MailerResult was ::" + result.getReturnCode(), null);			
		}
	}
	
	/**
	 * A temporary key is created
	 * 
	 * @param email address of new user
	 * @param ip address of new user
	 * @param action REGISTRATION or PWCHANGE
	 * 
	 * @return TemporaryKey
	 */
	public TemporaryKeyImpl createTemporaryKeyByEmail(String email, String ip, String action) {
		TemporaryKeyImpl tk = null;
		DB db = DBFactory.getInstance();
		// check if the user is already registered
		// we also try to find it in the temporarykey list
		List tks = db.find("from org.olat.registration.TemporaryKeyImpl as r where r.emailAddress = ?", email,
				StandardBasicTypes.STRING);
		if ((tks == null) || (tks.size() != 1)) { // no user found, create a new one
			tk = register(email, ip, action);
		} else {
			tk = (TemporaryKeyImpl) tks.get(0);
		}
		return tk;
	}

	/**
	 * deletes a TemporaryKey
	 * 
	 * @param key the temporary key to be deleted
	 * 
	 * @return true if successfully deleted
	 */
	public void deleteTemporaryKey(TemporaryKeyImpl key) {
		DBFactory.getInstance().deleteObject(key);
	}

	/**
	 * returns an existing TemporaryKey by a given email address or null if none
	 * found
	 * 
	 * @param email
	 * 
	 * @return the found temporary key or null if none is found
	 */
	public TemporaryKeyImpl loadTemporaryKeyByEmail(String email) {
		DB db = DBFactory.getInstance();
		List tks = db.find("from r in class org.olat.registration.TemporaryKeyImpl where r.emailAddress = ?", email,
				StandardBasicTypes.STRING);
		if (tks.size() == 1) {
			return (TemporaryKeyImpl) tks.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * returns an existing list of TemporaryKey by a given action or null if none
	 * found
	 * 
	 * @param action
	 * 
	 * @return the found temporary key or null if none is found
	 */
	public List<TemporaryKey> loadTemporaryKeyByAction(String action) {
		DB db = DBFactory.getInstance();
		List<TemporaryKey> tks = db.find("from r in class org.olat.registration.TemporaryKeyImpl where r.regAction = ?", action, StandardBasicTypes.STRING);
		if (tks.size() > 0) {
			return tks;
		} else {
			return null;
		}
	}

	/**
	 * Looks for a TemporaryKey by a given registrationkey
	 * 
	 * @param regkey the encrypted registrationkey
	 * 
	 * @return the found TemporaryKey or null if none is found
	 */
	public TemporaryKeyImpl loadTemporaryKeyByRegistrationKey(String regkey) {
		DB db = DBFactory.getInstance();
		List tks = db.find("from r in class org.olat.registration.TemporaryKeyImpl where r.registrationKey = ?", regkey,
				StandardBasicTypes.STRING);
		if (tks.size() == 1) {
			return (TemporaryKeyImpl) tks.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Creates a TemporaryKey and saves it permanently
	 * 
	 * @param emailaddress
	 * @param ipaddress
	 * @param action REGISTRATION or PWCHANGE
	 * 
	 * @return newly created temporary key
	 */
	public TemporaryKeyImpl register(String emailaddress, String ipaddress, String action) {
		String today = new Date().toString();
		String encryptMe = Encoder.md5hash(emailaddress + ipaddress + today);
		TemporaryKeyImpl tk = new TemporaryKeyImpl(emailaddress, ipaddress, encryptMe, action);
		DBFactory.getInstance().saveObject(tk);
		return tk;
	}

	/**
	 * Delete a temporary key.
	 * @param keyValue
	 */
	public void deleteTemporaryKeyWithId(String keyValue) {
		TemporaryKeyImpl tKey = loadTemporaryKeyByRegistrationKey(keyValue);
		if(tKey != null) {
			deleteTemporaryKey(tKey);
		}
	}

	/**
	 * Evaluates whether the given identity needs to accept a disclaimer before
	 * logging in or not.
	 * 
	 * @param identity
	 * @return true: user must accept the disclaimer; false: user did already
	 *         accept or must not accept a disclaimer
	 */
	public boolean needsToConfirmDisclaimer(Identity identity) {
		boolean needsToConfirm = false; // default is not to confirm
		if (CoreSpringFactory.getImpl(RegistrationModule.class).isDisclaimerEnabled()) {
			// don't use the discrete method to be more robust in case that more than one
			// property is found
			List<Property> disclaimerProperties = PropertyManager.getInstance().listProperties(identity, null, null, "user", "dislaimer_accepted");
			needsToConfirm = ( disclaimerProperties.size() == 0);
		}
		return needsToConfirm;
	}

	/**
	 * Marks the given identity to have confirmed the disclaimer. Note that this
	 * method does not check if the disclaimer does already exist, do this by
	 * calling needsToConfirmDisclaimer() first!
	 * 
	 * @param identity
	 */
	public void setHasConfirmedDislaimer(Identity identity) {		
		PropertyManager propertyMgr = PropertyManager.getInstance();
		Property disclaimerProperty = propertyMgr.createUserPropertyInstance(identity, "user", "dislaimer_accepted", null, 1l, null, null);
		propertyMgr.saveProperty(disclaimerProperty);
	}

	/**
	 * Remove all disclaimer confirmations. This means that every user on the
	 * system must accept the disclaimer again.
	 */
	public void revokeAllconfirmedDisclaimers() {
		PropertyManager propertyMgr = PropertyManager.getInstance();
		propertyMgr.deleteProperties(null, null, null, "user", "dislaimer_accepted");		
	}

	/**
	 * Remove the disclaimer confirmation for the specified identity. This means
	 * that this user must accept the disclaimer again.
	 * 
	 * @param identity
	 */
	public void revokeConfirmedDisclaimer(Identity identity) {
		PropertyManager propertyMgr = PropertyManager.getInstance();
		propertyMgr.deleteProperties(identity, null, null, "user", "dislaimer_accepted");		
	}
	
	/**
	 * Get a list of all users that did already confirm the disclaimer
	 * @return
	 */
	public List<Identity> getIdentitiesWithConfirmedDisclaimer() {
		List<Identity> identities = DBFactory.getInstance().find(
				"select distinct ident from org.olat.core.id.Identity as ident, org.olat.properties.Property as prop " +
				"where prop.identity=ident and prop.category='user' and prop.name='dislaimer_accepted'");
		return identities;
	}

}