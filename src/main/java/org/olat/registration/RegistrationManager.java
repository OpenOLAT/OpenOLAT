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
* <p>
*/ 

package org.olat.registration;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hibernate.Hibernate;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.Encoder;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.manager.MailManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class RegistrationManager extends BasicManager {

	public final String PW_CHANGE = "PW_CHANGE";
	public final String REGISTRATION = "REGISTRATION";//fxdiff FXOLAT-113: business path in DMZ
	public static final String EMAIL_CHANGE = "EMAIL_CHANGE";
	protected static final int REG_WORKFLOW_STEPS = 5;
	protected static final int PWCHANGE_WORKFLOW_STEPS = 4;

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
		Identity identity = AuthHelper.createAndPersistIdentityAndUserWithUserGroup(login, pwd, myUser);
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
			Tracing.logError("Could not send registration notification message, bad mail address", e, RegistrationManager.class);
			return;
		}
		MailerResult result = new MailerResult();
		User user = newIdentity.getUser();
		Locale loc = I18nModule.getDefaultLocale();
		String[] userParams = new  String[] {newIdentity.getName(), user.getProperty(UserConstants.FIRSTNAME, loc), user.getProperty(UserConstants.LASTNAME, loc), user.getProperty(UserConstants.EMAIL, loc),
				user.getPreferences().getLanguage(), Settings.getServerconfig("server_fqdn") + WebappHelper.getServletContextPath() };
		Translator trans = new PackageTranslator(Util.getPackageName(RegistrationManager.class), loc);
		String subject = trans.translate("reg.notiEmail.subject", userParams);
		String body = trans.translate("reg.notiEmail.body", userParams);
		
		//fxdiff VCRP-16: intern mail system
		MimeMessage msg = MailManager.getInstance().createMimeMessage(from, to, null, null, body, subject, null, result);
		MailManager.getInstance().sendMessage(msg, result);
		if (result.getReturnCode() != MailerResult.OK ) {
			Tracing.logError("Could not send registration notification message, MailerResult was ::" + result.getReturnCode(), RegistrationManager.class);			
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
				Hibernate.STRING);
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
				Hibernate.STRING);
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
		List<TemporaryKey> tks = db.find("from r in class org.olat.registration.TemporaryKeyImpl where r.regAction = ?", action, Hibernate.STRING);
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
				Hibernate.STRING);
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
		String encryptMe = Encoder.encrypt(emailaddress + ipaddress + today);
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
		deleteTemporaryKey(tKey);
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
		if (RegistrationModule.isDisclaimerEnabled()) {
			// don't use the discrete method to be more robust in case that more than one
			// property is found
			List disclaimerProperties = PropertyManager.getInstance().listProperties(identity, null, null, "user", "dislaimer_accepted");
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