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
import java.util.Map;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
@Service("selfRegistrationManager")
public class RegistrationManager {
	
	private static final OLog log = Tracing.createLoggerFor(RegistrationManager.class);

	public static final String PW_CHANGE = "PW_CHANGE";
	public static final String REGISTRATION = "REGISTRATION";
	public static final String EMAIL_CHANGE = "EMAIL_CHANGE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private RegistrationModule registrationModule;


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
				log.error("Error matching an email adress", e);
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
		List<String> errors = new ArrayList<>();
		for(String domain:list) {
			try {
				String pattern = convertDomainPattern(domain);
				emailDomain.matches(pattern);
			} catch (Exception e) {
				errors.add(domain);
				log.error("Error matching an email adress", e);
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
	public Identity createNewUserAndIdentityFromTemporaryKey(String login, String pwd, User myUser, TemporaryKey tk) {
		Identity identity = securityManager.createAndPersistIdentityAndUserWithDefaultProviderAndUserGroup(login, null, pwd, myUser, null);
		if (identity == null) {
			return null;
		}
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
			log.error("Could not send registration notification message, bad mail address", e);
			return;
		}
		MailerResult result = new MailerResult();
		User user = newIdentity.getUser();
		Locale loc = I18nModule.getDefaultLocale();
		String[] userParams = new  String[] {
				newIdentity.getName(), 
				user.getProperty(UserConstants.FIRSTNAME, loc), 
				user.getProperty(UserConstants.LASTNAME, loc), 
				UserManager.getInstance().getUserDisplayEmail(user, loc),
				user.getPreferences().getLanguage(), 
				Settings.getServerDomainName() + WebappHelper.getServletContextPath() };
		Translator trans = Util.createPackageTranslator(RegistrationManager.class, loc);
		String subject = trans.translate("reg.notiEmail.subject", userParams);
		String body = trans.translate("reg.notiEmail.body", userParams);
		
		MimeMessage msg = mailManager.createMimeMessage(from, to, null, null, body, subject, null, result);
		mailManager.sendMessage(msg, result);
		if (result.getReturnCode() != MailerResult.OK ) {
			log.error("Could not send registration notification message, MailerResult was ::" + result.getReturnCode(), null);			
		}
	}
	
	/**
	 * A temporary key is loaded or created.
	 * 
	 * @param email address of new user
	 * @param ip address of new user
	 * @param action
	 * 
	 * @return 
	 */
	public TemporaryKey loadOrCreateTemporaryKeyByEmail(String email, String ip, String action) {
		List<TemporaryKey> tks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTemporaryKeyByEmailAddress", TemporaryKey.class)
				.setParameter("email", email)
				.getResultList();
		TemporaryKey tk;
		if ((tks == null) || (tks.size() != 1)) { // no user found, create a new one
			tk = createAndPersistTemporaryKey(email, ip, action);
		} else {
			tk = tks.get(0);
		}
		return tk;
	}
	
	/**
	 * An identity is allowed to only have one temporary key for an action. So this
	 * method first deletes the old temporary key and afterwards it creates and
	 * persists a new temporary key. This mechanism guarantees to have an updated
	 * expiration of the temporary key.
	 * 
	 * @param identityKey
	 * @param email
	 * @param ip
	 * @param action
	 * @return
	 */
	public TemporaryKey createAndDeleteOldTemporaryKey(Long identityKey, String email, String ip, String action) {
		deleteTemporaryKeys(identityKey, action);
		return createAndPersistTemporaryKey(identityKey, email, ip, action);
	}

	private TemporaryKey createAndPersistTemporaryKey(String emailaddress, String ipaddress, String action) {
		return createAndPersistTemporaryKey(null, emailaddress, ipaddress, action);
	}

	private TemporaryKey createAndPersistTemporaryKey(Long identityKey, String email, String ip, String action) {
		TemporaryKeyImpl tk = new TemporaryKeyImpl();
		tk.setCreationDate(new Date());
		tk.setIdentityKey(identityKey);
		tk.setEmailAddress(email);
		tk.setIpAddress(ip);
		tk.setRegistrationKey(createRegistrationKey(email, ip));
		tk.setRegAction(action);
		dbInstance.getCurrentEntityManager().persist(tk);
		return tk;
	}
	
	public void deleteTemporaryKey(TemporaryKey key) {
		TemporaryKeyImpl reloadedKey = dbInstance.getCurrentEntityManager()
				.getReference(TemporaryKeyImpl.class, key.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedKey);
	}

	private void deleteTemporaryKeys(Long identityKey, String action) {
		if (identityKey == null || action == null) return;
		
		dbInstance.getCurrentEntityManager()
				.createNamedQuery("deleteTemporaryKeyByIdentityAndAction") 
				.setParameter("identityKey", identityKey)
				.setParameter("action", action)
				.executeUpdate();
	}

	/**
	 * returns an existing TemporaryKey by a given email address or null if none
	 * found
	 * 
	 * @param email
	 * 
	 * @return the found temporary key or null if none is found
	 */
	public TemporaryKey loadTemporaryKeyByEmail(String email) {
		List<TemporaryKey> tks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTemporaryKeyByEmailAddress", TemporaryKey.class)
				.setParameter("email", email)
				.getResultList();
		if (tks.size() == 1) {
			return tks.get(0);
		}
		return null;
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
		List<TemporaryKey> tks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTemporaryKeyByRegAction", TemporaryKey.class)
				.setParameter("action", action)
				.getResultList();
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
	public TemporaryKey loadTemporaryKeyByRegistrationKey(String regkey) {
		List<TemporaryKey> tks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTemporaryKeyByRegKey", TemporaryKey.class)
				.setParameter("regkey", regkey)
				.getResultList();
		
		if (tks.size() == 1) {
			return tks.get(0);
		}
		return null;
	}

	public List<TemporaryKey> loadTemporaryKeyByIdentity(Long identityKey, String action) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTemporaryKeyByIdentity", TemporaryKey.class)
				.setParameter("identityKey", identityKey)
				.setParameter("action", action)
				.getResultList();
	}

	/**
	 * Delete a temporary key.
	 * @param keyValue
	 */
	public void deleteTemporaryKeyWithId(String keyValue) {
		TemporaryKey tKey = loadTemporaryKeyByRegistrationKey(keyValue);
		if(tKey != null) {
			deleteTemporaryKey(tKey);
		}
	}
	
	public boolean isEmailReserved(String emailAddress) {
		if (!StringHelper.containsNonWhitespace(emailAddress)) return false;
		
		RegistrationManager rm = CoreSpringFactory.getImpl(RegistrationManager.class);
		List<TemporaryKey> tk = rm.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
		if (tk != null) {
			for (TemporaryKey temporaryKey : tk) {
				XStream xml = XStreamHelper.createXStreamInstance();
				@SuppressWarnings("unchecked")
				Map<String, String> mails = (Map<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
				if (emailAddress.equalsIgnoreCase(mails.get("changedEMail"))) {
					return true;
				}
			}
		}
		return isRegistrationPending(emailAddress);
	}

	public boolean isRegistrationPending(String emailAddress) {
		List<TemporaryKey> temporaryKeys = loadTemporaryKeyByAction(RegistrationManager.REGISTRATION);
		if (temporaryKeys != null) {
			for (TemporaryKey temporaryKey : temporaryKeys) {
				if (emailAddress.equalsIgnoreCase(temporaryKey.getEmailAddress())) {
					return true;
				}
			}
		}
		return false;
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
		if (registrationModule.isDisclaimerEnabled()) {
			// don't use the discrete method to be more robust in case that more than one
			// property is found
			List<Property> disclaimerProperties = propertyManager.listProperties(identity, null, null, "user", "dislaimer_accepted");
			needsToConfirm = disclaimerProperties.isEmpty();
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
		Property disclaimerProperty = propertyManager.createUserPropertyInstance(identity, "user", "dislaimer_accepted", null, 1l, null, null);
		propertyManager.saveProperty(disclaimerProperty);
	}

	/**
	 * Remove all disclaimer confirmations. This means that every user on the
	 * system must accept the disclaimer again.
	 */
	public void revokeAllconfirmedDisclaimers() {
		propertyManager.deleteProperties(null, null, null, "user", "dislaimer_accepted");		
	}

	/**
	 * Remove the disclaimer confirmation for the specified identity. This means
	 * that this user must accept the disclaimer again.
	 * 
	 * @param identity
	 */
	public void revokeConfirmedDisclaimer(Identity identity) {
		propertyManager.deleteProperties(identity, null, null, "user", "dislaimer_accepted");		
	}

	private String createRegistrationKey(String email, String ip) {
		String random = UUID.randomUUID().toString();
		return Encoder.md5hash(email + ip + random);
	}
}