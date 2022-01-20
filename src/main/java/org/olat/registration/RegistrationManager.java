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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.TemporalType;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Encoder;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.filter.impl.HtmlFilter;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.UserManager;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
@Service("selfRegistrationManager")
public class RegistrationManager implements UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(RegistrationManager.class);
	
	private static final XStream xmlXStream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				HashMap.class
			};
		xmlXStream.addPermission(new ExplicitTypePermission(types));
	}

	private static final int VALID_UNTIL_30_DAYS = 30*24;

	public static final String PW_CHANGE = "PW_CHANGE";
	public static final String REGISTRATION = "REGISTRATION";
	public static final String EMAIL_CHANGE = "EMAIL_CHANGE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public Organisation getOrganisationForRegistration() {
		String key = registrationModule.getSelfRegistrationOrganisationKey();
		
		Organisation organisation = null;
		if(StringHelper.containsNonWhitespace(key) && !"default".equals(key) && StringHelper.isLong(key)) {
			organisation = organisationService.getOrganisation(new OrganisationRefImpl(Long.valueOf(key)));
		}
		if(organisation == null) {
			organisation = organisationService.getDefaultOrganisation();
		}
		return organisation;
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
	 * Creates a new user and identity with the data of the temporary key (email) and other
	 * supplied user data (within myUser). The user will be added to the default organisation
	 * and an other one if this is configured as such.
	 * 
	 * @param login Login name
	 * @param pwd Password
	 * @param user Not yet persisted user object
	 * @param tk Temporary key
	 * @return the newly created subject or null
	 */
	public Identity createNewUserAndIdentityFromTemporaryKey(String login, String pwd, User user, TemporaryKey tk) {
		Date expirationDate = null;
		Integer expiration = registrationModule.getAccountExpirationInDays();
		if(expiration != null && expiration.intValue() > 0) {
			expirationDate = DateUtils.addDays(new Date(), expiration.intValue());
			expirationDate = CalendarUtils.endOfDay(expirationDate);
		}
		
		Organisation organisation = getOrganisationForRegistration();
		Identity identity = securityManager
				.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
						BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, login, pwd,  organisation, expirationDate);
		if(!OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER.equals(organisation.getIdentifier())) {
			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			organisationService.addMember(defOrganisation, identity, OrganisationRoles.user);
		}
		if (identity == null) {
			return null;
		} else if(pending(identity)) {
			identity = securityManager.saveIdentityStatus(identity, Identity.STATUS_PENDING, identity);
		}
		
		
		deleteTemporaryKey(tk);
		return identity;
	}
	
	private boolean pending(Identity identity) {
		boolean pending = false;
		RegistrationPendingStatus status = registrationModule.getRegistrationPendingStatus();
		if(status == RegistrationPendingStatus.pending) {
			pending = true;
		} else if(status == RegistrationPendingStatus.pendingMatchingProperties) {
			User user = identity.getUser();
			@SuppressWarnings("static-access")
			Locale locale = i18nModule.getDefaultLocale();
			pending |= matchProperty(user, registrationModule.getRegistrationPendingPropertyName1(), registrationModule.getRegistrationPendingPropertyValue1(), locale);
			pending |= matchProperty(user, registrationModule.getRegistrationPendingPropertyName2(), registrationModule.getRegistrationPendingPropertyValue2(), locale);
			pending |= matchProperty(user, registrationModule.getRegistrationPendingPropertyName3(), registrationModule.getRegistrationPendingPropertyValue3(), locale);
			pending |= matchProperty(user, registrationModule.getRegistrationPendingPropertyName4(), registrationModule.getRegistrationPendingPropertyValue4(), locale);
			pending |= matchProperty(user, registrationModule.getRegistrationPendingPropertyName5(), registrationModule.getRegistrationPendingPropertyValue5(), locale);
		}
		return pending;
	}
	
	private boolean matchProperty(User user, String propName, String propValue, Locale locale) {
		boolean match = false;
		
		if(StringHelper.containsNonWhitespace(propName) && StringHelper.containsNonWhitespace(propValue)) {
			String val = user.getProperty(propName, locale);
			
			if(propValue.equalsIgnoreCase(val)) {
				match = true;
			} else if(UserConstants.EMAIL.equals(propName) || UserConstants.INSTITUTIONALEMAIL.equals(propName)) {
				String valLow = val.toLowerCase();
				String propValueLow = propValue.toLowerCase();
				match = valLow.contains(propValueLow);
			}
		}
		
		return match;
	}

	/**
	 * Send a notification messaged to the given notification email address about the registration of 
	 * the given new identity.
	 * 
	 * @param notificationMailAddress Email address who should be notified. MUST NOT BE NULL
	 * @param newIdentity The newly registered Identity
	 */
	public void sendNewUserNotificationMessage(String notificationMailAddress, Identity newIdentity) {
		Address from;
		Address[] to;
		try {
			from = new InternetAddress(WebappHelper.getMailConfig("mailReplyTo"));
			String[] notificationMailAddressArr = notificationMailAddress.split("[,]");
			List<Address> toList = new ArrayList<>();
			for(int i=notificationMailAddressArr.length; i-->0; ) {
				if(StringHelper.containsNonWhitespace(notificationMailAddressArr[i])) {
					toList.add(new InternetAddress(notificationMailAddressArr[i]));
				}
			}
			to = toList.toArray(new Address[toList.size()]);
		} catch (AddressException e) {
			log.error("Could not send registration notification message, bad mail address", e);
			return;
		}
		
		// http://localhost:8080/auth/UserAdminSite/0/usearch/0/table/0/Identity/720896/roles/0
		
		String userPath = "[UserAdminSite:0][usearch:0][table:0][Identity:" + newIdentity.getKey() + "][roles:0]";
		String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(userPath);
		
		MailerResult result = new MailerResult();
		User user = newIdentity.getUser();
		Locale loc = I18nModule.getDefaultLocale();
		String[] userParams = new  String[] {
				newIdentity.getName(), 													// 0
				user.getProperty(UserConstants.FIRSTNAME, loc), 						// 1
				user.getProperty(UserConstants.LASTNAME, loc),							// 2
				UserManager.getInstance().getUserDisplayEmail(user, loc),				// 3
				user.getPreferences().getLanguage(),									// 4
				Settings.getServerDomainName() + WebappHelper.getServletContextPath(),	// 5
				url																		// 6
			};
		Translator trans = Util.createPackageTranslator(RegistrationManager.class, loc);
		String subject = trans.translate("reg.notiEmail.subject", userParams);
		String body = trans.translate("reg.notiEmail.body", userParams);
		String decoratedBody = mailManager.decorateMailBody(body, loc);
		
		MimeMessage msg = mailManager.createMimeMessage(from, to, null, null, subject, decoratedBody, null, result);
		mailManager.sendMessage(msg, result);
		if (result.getReturnCode() != MailerResult.OK ) {
			log.error("Could not send registration notification message, MailerResult was ::{}", result.getReturnCode());			
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
	public TemporaryKey loadOrCreateTemporaryKeyByEmail(String email, String ip, String action, Integer validForHours) {
		List<TemporaryKey> tks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTemporaryKeyByEmailAddress", TemporaryKey.class)
				.setParameter("email", email)
				.getResultList();
		TemporaryKey tk;
		if ((tks == null) || (tks.size() != 1)) { // no user found, create a new one
			tk = createAndPersistTemporaryKey(email, ip, action, validForHours);
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
	public TemporaryKey createAndDeleteOldTemporaryKey(Long identityKey, String email, String ip, String action, Integer validForHours) {
		deleteTemporaryKeys(identityKey, action);
		return createAndPersistTemporaryKey(identityKey, email, ip, action, validForHours);
	}

	private TemporaryKey createAndPersistTemporaryKey(String emailaddress, String ipaddress, String action, Integer validForHours) {
		return createAndPersistTemporaryKey(null, emailaddress, ipaddress, action, validForHours);
	}

	private TemporaryKey createAndPersistTemporaryKey(Long identityKey, String email, String ip, String action, Integer validForHours) {
		TemporaryKeyImpl tk = new TemporaryKeyImpl();
		tk.setCreationDate(new Date());
		tk.setIdentityKey(identityKey);
		tk.setEmailAddress(email);
		tk.setIpAddress(ip);
		tk.setRegistrationKey(createRegistrationKey(email, ip));
		Integer validHours = validForHours != null? validForHours: VALID_UNTIL_30_DAYS;
		Date validUntil = addHours(tk.getCreationDate(), validHours);
		tk.setValidUntil(validUntil);
		tk.setRegAction(action);
		dbInstance.getCurrentEntityManager().persist(tk);
		return tk;
	}
	
	private Date addHours(Date date, Integer seconds) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR, seconds);
		return c.getTime();
	}
	
	public void deleteTemporaryKey(TemporaryKey key) {
		if (key == null) return;
		
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

	private void deleteAllTemporaryKeys(Long identityKey) {
		if (identityKey == null) return;		
		dbInstance.getCurrentEntityManager()
				.createNamedQuery("deleteTemporaryKeyByIdentity") 
				.setParameter("identityKey", identityKey)
				.executeUpdate();
	}
	
	public int deleteInvalidTemporaryKeys() {
		String query = "delete from otemporarykey tk where tk.validUntil < :validUntil";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query) 
				.setParameter("validUntil", new Date(), TemporalType.TIMESTAMP)
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
				.setParameter("email", email.toLowerCase())
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
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTemporaryKeyByRegAction", TemporaryKey.class)
				.setParameter("action", action)
				.getResultList();
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

	public List<TemporaryKey> loadAll() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAll", TemporaryKey.class)
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
		
		List<TemporaryKey> tk = loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
		for (TemporaryKey temporaryKey : tk) {
			Map<String, String> mails = readTemporaryValue(temporaryKey.getEmailAddress());
			if (emailAddress.equalsIgnoreCase(mails.get("changedEMail"))) {
				return true;
			}
		}
		return isRegistrationPending(emailAddress);
	}

	public boolean isRegistrationPending(String emailAddress) {
		List<TemporaryKey> temporaryKeys = loadTemporaryKeyByAction(RegistrationManager.REGISTRATION);
		for (TemporaryKey temporaryKey : temporaryKeys) {
			if (emailAddress.equalsIgnoreCase(temporaryKey.getEmailAddress())) {
				return true;
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
	
	public Date getDisclaimerConfirmationDate(Identity identity) {
		if(identity == null) return null;
		
		List<Property> disclaimerProperties = propertyManager.listProperties(identity, null, null, "user", "dislaimer_accepted");
		return disclaimerProperties.isEmpty() ? null : disclaimerProperties.get(0).getLastModified();
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
	
	@SuppressWarnings("unchecked")
	public Map<String, String> readTemporaryValue(String value) {
		return (Map<String, String>)xmlXStream.fromXML(value);
	}
	
	public String temporaryValueToString(Map<String, String> map) {
		return xmlXStream.toXML(map);
	}

	private String createRegistrationKey(String email, String ip) {
		String random = UUID.randomUUID().toString();
		return Encoder.md5hash(email + ip + random);
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		// Delete temporary keys used in change email or password workflow 
		deleteAllTemporaryKeys(identity.getKey());
	}

	@Override
	public String getExporterID() {
		return "disclaimer";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		List<Property> disclaimerProperties = propertyManager.listProperties(identity, null, null, "user", "dislaimer_accepted");
		if(disclaimerProperties.isEmpty()) return;
		
		Translator translator = Util.createPackageTranslator(RegistrationManager.class, locale);
		File disclaimerArchive = new File(archiveDirectory, "Disclaimer.txt");
		try(Writer out = new FileWriter(disclaimerArchive)) {
			for(Property disclaimerProperty:disclaimerProperties) {
				out.write(FilterFactory.getHtmlTagsFilter().filter(translator.translate("disclaimer.terms.of.usage")));
				out.write('\n');
				out.write("Accepted: " + Formatter.getInstance(locale).formatDateAndTime(disclaimerProperty.getCreationDate()));
				out.write('\n');

				StringBuilder sb = new StringBuilder();
				sb.append(translator.translate("disclaimer.paragraph1"))
				  .append("\n")
				  .append(translator.translate("disclaimer.paragraph2"));
				String disclaimer = new HtmlFilter().filter(sb.toString(), true);
				out.write(disclaimer);
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile(disclaimerArchive.getName());
	}
	
}