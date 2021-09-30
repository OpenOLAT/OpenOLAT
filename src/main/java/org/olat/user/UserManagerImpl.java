/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.user;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityNames;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.resource.OresHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.registration.RegistrationManager;
import org.olat.user.manager.ManifestBuilder;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3>
 * This implementation of the user manager manipulates user objects based on a
 * hibernate implementation
 * <p>
 * Initial Date: 31.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class UserManagerImpl extends UserManager implements UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(UserManagerImpl.class);
	
  // used to save user data in the properties table 
  private static final String CHARSET = "charset";
  private UserDisplayNameCreator userDisplayNameCreator;
  
  @Autowired
  private DB dbInstance;
  @Autowired
  private UserDAO userDAO;
  @Autowired
  private UserModule userModule;
  @Autowired
  private BaseSecurity securityManager;
  @Autowired
  private RegistrationManager registrationManager;
  @Autowired
  private LoginModule loginModule;
  @Autowired
  private CoordinatorManager coordinatorManager;

	private CacheWrapper<Serializable,String> userToFullnameCache;
	private CacheWrapper<Long,String> userToNameCache;
  
	/**
	 * Use UserManager.getInstance(), this is a spring factory method to load the
	 * correct user manager
	 */
	UserManagerImpl() {
		INSTANCE = this;
	}
	
	@PostConstruct
	public void init() {
		userToFullnameCache = coordinatorManager.getCoordinator().getCacher()
				.getCache(UserManager.class.getSimpleName(), "userfullname");
		userToNameCache = coordinatorManager.getCoordinator().getCacher()
				.getCache(UserManager.class.getSimpleName(), "username");
		
	}

	@Override
	public User createUser(String firstName, String lastName, String eMail) {
		UserImpl newUser = new UserImpl();
		newUser.setFirstName(firstName);
		newUser.setLastName(lastName);
		newUser.setEmail(eMail);
		newUser.setCreationDate(new Date());
		
		Preferences prefs = newUser.getPreferences();
		Locale loc;
		// for junit test case: use German Locale
		if (Settings.isJUnitTest()) { 
			loc = Locale.GERMAN;
		} else {
			loc = I18nModule.getDefaultLocale();
		}
		//Locale loc
		prefs.setLanguage(loc.toString());
		prefs.setPresenceMessagesPublic(false);
		prefs.setInformSessionTimeout(false);
		return newUser;
	}
	
	@Override
	public List<Long> findUserKeyWithProperty(String propName, String propValue) {
		StringBuilder sb = new StringBuilder();
		sb.append("select user.key from ").append(UserImpl.class.getName()).append(" user ")
		  .append(" where user.").append(propName).append("=:propValue");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("propValue", propValue)
				.getResultList();
	}
	
	@Override
	public List<Identity> findIdentitiesWithProperty(String propName, String propValue) {
		StringBuilder sb = new StringBuilder("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
			.append(" inner join fetch identity.user user ")
			.append(" where user.").append(propName).append("=:propValue");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("propValue", propValue).getResultList();
	}

	@Override
	public Identity findUniqueIdentityByEmail(String email) {
		return userDAO.findUniqueIdentityByEmail(email);
	}
	
	@Override
	public List<Identity> findIdentitiesByEmail(List<String> emailList) {
		List<String> emails = new ArrayList<>(emailList);
		for (int i=0; i<emails.size(); i++) {
			String email = emails.get(i).toLowerCase();
			if (!MailHelper.isValidEmailAddress(email)) {
				emails.remove(i);
				log.warn("Invalid email address: " + email);
			}
			else {
				emails.set(i, email);
			}
		}
		if(emails.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
			.append(" inner join fetch identity.user user ")
			.append(" where ");
		
		boolean mysql = "mysql".equals(dbInstance.getDbVendor());
		//search email
		StringBuilder emailSb = new StringBuilder(sb);
		if(mysql) {
			emailSb.append(" user.").append(UserConstants.EMAIL).append(" in (:emails) ");
		} else {
			emailSb.append(" lower(user.").append(UserConstants.EMAIL).append(") in (:emails)");
		}

		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(emailSb.toString(), Identity.class)
				.setParameter("emails", emails).getResultList();

		//search institutional email
		StringBuilder institutionalSb = new StringBuilder(sb);
		if(mysql) {
			institutionalSb.append(" user.").append(UserConstants.INSTITUTIONALEMAIL).append(" in (:emails) ");
		} else {
			institutionalSb.append(" lower(user.").append(UserConstants.INSTITUTIONALEMAIL).append(") in (:emails)");
		}
		if(!identities.isEmpty()) {
			institutionalSb.append(" and identity not in (:identities) ");
		}
		TypedQuery<Identity> institutionalQuery = dbInstance.getCurrentEntityManager()
				.createQuery(institutionalSb.toString(), Identity.class)
				.setParameter("emails", emails);
		if(!identities.isEmpty()) {
			institutionalQuery.setParameter("identities", identities);
		}
		List<Identity> instIdentities = institutionalQuery.getResultList();
		identities.addAll(instIdentities);
		return identities;
	}

	@Override
	public List<Identity> findVisibleIdentitiesWithoutEmail() {
		return userDAO.findVisibleIdentitiesWithoutEmail();
	}

	@Override
	public List<Identity> findVisibleIdentitiesWithEmailDuplicates() {
		return userDAO.findVisibleIdentitiesWithEmailDuplicates();
	}

	@Override
	public boolean isEmailAllowed(String email, User user) {
		if (isEmailOfUser(email, user)) return true;
		if (isEmailAllowed(email)) return true;
		return false;
	}

	private boolean isEmailOfUser(String email, User user) {
		if (!StringHelper.containsNonWhitespace(email) || user == null) return false;
		
		boolean isOwnEmail = email.equalsIgnoreCase(user.getEmail()) ;
		boolean isOwnInstitutionalEmail = email.equalsIgnoreCase(user.getInstitutionalEmail());
		return isOwnEmail || isOwnInstitutionalEmail;
	}
	
	@Override
	public boolean isEmailAllowed(String email) {
		if (email == null && !userModule.isEmailMandatory()) return true;
		if (email != null && !userModule.isEmailUnique()) return true;
		if (email != null && isEmailNotInUse(email)) return true;
		return false;
	}
	
	private boolean isEmailNotInUse(String email) {
		boolean emailIsNotInUse = !userDAO.isEmailInUse(email);
		boolean emailIsNotReserved = !registrationManager.isEmailReserved(email);
		return emailIsNotInUse && emailIsNotReserved;
	}
	
	@Override
	public User loadUserByKey(Long key) {
		return dbInstance.getCurrentEntityManager().find(UserImpl.class, key);
		// User not loaded yet (lazy initialization). Need to access
		// a field first to really load user from database.
	}

	@Override
	public User updateUser(IdentityRef identityRef, User user) {
		if (user == null) throw new AssertException("User object is null!");
		
		// Detach to load the old values from the database.
		dbInstance.getCurrentEntityManager().detach(user);
		User oldUser = loadUserByKey(user.getKey());
		// Get the changes
		List<UserPropertyChangedEvent> events = getChangedEvents(identityRef, oldUser, user);
		// Update in the database
		User updatedUser = dbInstance.getCurrentEntityManager().merge(user);
		// Send the events
		sendDeferredEvents(identityRef, events);
		return updatedUser;
	}
	
	List<UserPropertyChangedEvent> getChangedEvents(IdentityRef identityRef, User oldUser, User updatedUser) {
		if (identityRef == null || oldUser == null) return Collections.emptyList();
		
		List<UserPropertyHandler> userPropertyHandlers = userPropertiesConfig.getAllUserPropertyHandlers();
		List<UserPropertyChangedEvent> events = new ArrayList<>();
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			String name = propertyHandler.getName();
			String oldValue = oldUser.getProperty(name);
			String newValue = updatedUser.getProperty(name);
			if (!Objects.equals(oldValue, newValue)) {
				events.add(new UserPropertyChangedEvent(identityRef.getKey(), name, oldValue, newValue));
			}
		}
		return events;
	}
	
	private void sendDeferredEvents(IdentityRef identityRef, List<? extends MultiUserEvent> events) {
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, identityRef.getKey());
		for(MultiUserEvent event:events) {
			eventBus.fireEventToListenersOf(event, ores);
		}
	}

	@Override
	public boolean updateUserFromIdentity(Identity identity) {
		try {
			String fullName = getUserDisplayName(identity);
			updateUsernameCache(identity.getKey(), identity.getName(), fullName);
		} catch (Exception e) {
			log.warn("Error update usernames cache", e);
		}
		User user = updateUser(identity, identity.getUser());
		((IdentityImpl)identity).setUser(user);
		return true;
	}

	@Override
	public void setUserCharset(Identity identity, String charset){
	    PropertyManager pm = PropertyManager.getInstance();
	    Property p = pm.findProperty(identity, null, null, null, CHARSET);
	    
	    if(p != null){
	        p.setStringValue(charset);
	        pm.updateProperty(p);
		} else {
	        Property newP = pm.createUserPropertyInstance(identity, null, CHARSET, null, null, charset, null);
	        pm.saveProperty(newP);
	    }
	}

	@Override
	public String getUserCharset(Identity identity){
	   String charset;
	   PropertyManager pm = PropertyManager.getInstance();
	   Property p = pm.findProperty(identity, null, null, null, CHARSET);
	   if(p != null){
	       charset = p.getStringValue();
			// if after migration the system does not support the charset choosen by a
			// user
	       // (a rather rare case)
	       if(!Charset.isSupported(charset)){
	           charset = WebappHelper.getDefaultCharset();
	       }
		} else {
	       charset = WebappHelper.getDefaultCharset();
	   }
	   return charset;
	}

	@Override
	public int warmUp() {
		EntityManager em = dbInstance.getCurrentEntityManager();
		
		int batchSize = 5000;
		TypedQuery<IdentityShort> query = em
				.createNamedQuery("selectAllIdentitiesShortUnordered", IdentityShort.class)
				.setMaxResults(batchSize);
		
		int count = 0;
		long maxCount = userToNameCache.maxCount();
		List<IdentityShort> identities;
		do {
			identities = query.setFirstResult(count).getResultList();
			em.clear();
			for(IdentityShort identity:identities) {
				if(identity.getStatus() < Identity.STATUS_DELETED) {
					getUserDisplayName(identity);
				}
			}
			count += identities.size();
		} while(identities.size() >= batchSize && count < maxCount);
		
		return count;
	}

	@Override
	public String getUsername(Long identityKey) {
		if(identityKey == null || identityKey.longValue() <= 0) {
			return null;
		}
		
		String username = userToNameCache.get(identityKey);
		if(username == null) {
			IdentityShort identity = securityManager.loadIdentityShortByKey(identityKey);
			getUserDisplayName(identity);//fill the cache
			username = identity.getName();
		}
		return username;
	}

	@Override
	public String getUserDisplayName(String username) {
		if(username == null) return null;
		String fullName = userToFullnameCache.get(username);
		if(fullName == null) {
			List<IdentityShort> identities = securityManager.findShortIdentitiesByName(Collections.singletonList(username));
			for(IdentityShort identity:identities) {
				fullName = getUserDisplayName(identity);
			}
		}
		return fullName;
	}

	@Override
	public String getUserDisplayName(Long identityKey) {
		if(identityKey == null || identityKey.longValue() <= 0) {
			return "";
		}
		
		String fullName = userToFullnameCache.get(identityKey);
		if(fullName == null) {
			IdentityShort identity = securityManager.loadIdentityShortByKey(identityKey);
			fullName = getUserDisplayName(identity);
		}
		return fullName;
	}

	@Override
	public Map<String, String> getUserDisplayNamesByUserName(Collection<String> usernames) {
		if(usernames == null || usernames.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, String> fullNames = new HashMap<>();
		List<String> newUsernames = new ArrayList<>();
		for(String username:usernames) {
			String fullName = userToFullnameCache.get(username);
			if(fullName != null) {
				fullNames.put(username, fullName);
			} else {
				newUsernames.add(username);
			}
		}

		List<IdentityShort> identities = securityManager.findShortIdentitiesByName(newUsernames);
		for(IdentityShort identity:identities) {
			String fullName = getUserDisplayName(identity);
			fullNames.put(identity.getName(), fullName);
			newUsernames.remove(identity.getName());
		}
		//not found
		for(String notFound:newUsernames) {
			userToFullnameCache.put(notFound, notFound);
		}
		return fullNames;
	}

	@Override
	public String getUserDisplayName(IdentityRef identity) {
		if(identity instanceof Identity) {
			return getUserDisplayName((Identity)identity);
		}
		return getUserDisplayName(identity.getKey());
	}

	@Override
	public String getUserDisplayName(Identity identity) {
		if (userDisplayNameCreator == null || identity == null) return "";
		String fullName = getUserDisplayName(identity.getUser());
		updateUsernameCache(identity.getKey(), identity.getName(), fullName);
		return fullName;
	}

	@Override
	public String getUserDisplayName(User user) {
		if (userDisplayNameCreator == null || user == null) return "";
		return userDisplayNameCreator.getUserDisplayName(user);
	}

	@Override
	public String getUserDisplayName(IdentityNames identity) {
		if (userDisplayNameCreator == null || identity == null) return "";
		String fullName = userDisplayNameCreator.getUserDisplayName(identity);
		updateUsernameCache(identity.getKey(), identity.getName(), fullName);
		return fullName;
	}

	@Override
	public String getUserDisplayName(String firstName, String lastName) {
		return userDisplayNameCreator.getDisplayName(firstName, lastName);
	}

	@Override
	public Map<Long, String> getUserDisplayNamesByKey(Collection<Long> identityKeys) {
		if(identityKeys == null || identityKeys.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<Long, String> fullNames = new HashMap<>();
		List<Long> newIdentityKeys = new ArrayList<>();
		for(Long identityKey:identityKeys) {
			String fullName = userToFullnameCache.get(identityKey);
			if(fullName != null) {
				fullNames.put(identityKey, fullName);
			} else {
				newIdentityKeys.add(identityKey);
			}
		}

		List<IdentityShort> identities = securityManager.loadIdentityShortByKeys(newIdentityKeys);
		for(IdentityShort identity:identities) {
			String fullName = getUserDisplayName(identity);
			updateUsernameCache(identity.getKey(), identity.getName(), fullName);
			fullNames.put(identity.getKey(), fullName);
		}

		return fullNames;
	}
	
	private void updateUsernameCache(Long identityKey, String username, String fullName) {
		if(fullName == null) return;
		
		if(identityKey != null) {
			userToFullnameCache.put(identityKey, fullName);
		}
		if(username != null) {
			userToFullnameCache.put(username, fullName);
		}
		if(username != null && identityKey != null) {
			userToNameCache.put(identityKey, username);
		}
	}

	/**
	 * Sping setter method
	 * @param userDisplayNameCreator the userDisplayNameCreator to set
	 */
	public void setUserDisplayNameCreator(UserDisplayNameCreator userDisplayNameCreator) {
		this.userDisplayNameCreator = userDisplayNameCreator;
	}
	
	@Override
	public String getUserDisplayEmail(Identity identity, Locale locale) {
		User user = identity.getUser();
		return getUserDisplayEmail(user, locale);
	}
	
	@Override
	public String getUserDisplayEmail(User user, Locale locale) {
		String email = user.getProperty(UserConstants.EMAIL, locale);
		return getUserDisplayEmail(email, locale);
	}
	
	@Override
	public String getUserDisplayEmail(String email, Locale locale) {
		Translator translator = Util.createPackageTranslator(UserManager.class, locale);
		return getUserDisplayEmail(email, translator);
	}

	public String getUserDisplayEmail(String email, Translator translator) {
		String transaltedEmail;
		if (StringHelper.containsNonWhitespace(email)) {
			transaltedEmail = email;
		} else {
			transaltedEmail = translator.translate("email.not.available");
		}
		return transaltedEmail;
	}

	@Override
	public String getEnsuredEmail(User user) {
		String ensuredEmail;
		if (user == null) {
			ensuredEmail = "-1@" + getDomain();
		} else if (StringHelper.containsNonWhitespace(user.getEmail())) {
			ensuredEmail = user.getEmail();
		} else {
			ensuredEmail = user.getKey() + "@" + getDomain();
		}
		return ensuredEmail;
	}

	private String getDomain() {
		AuthenticationProvider authenticationProvider = loginModule.getAuthenticationProvider("OLAT");
		String issuer = authenticationProvider.getIssuerIdentifier(null);
		return issuer.startsWith("https://")? issuer.substring(8): issuer;
	}

	@Override
	public int deleteUserDataPriority() {
		// delete with high priority
		return 100;
	}
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		identity = securityManager.loadIdentityByKey(identity.getKey());
		
		String roles = ((IdentityImpl)identity).getDeletedRoles();
		boolean isAdministrativeUser = roles != null && (roles.contains("admins") || roles.contains("authors")
				|| roles.contains("groupmanagers") || roles.contains("poolsmanager") || roles.contains("usermanagers")
				|| roles.contains("owners") || roles.contains(GroupRoles.owner.name())
				|| roles.contains(OrganisationRoles.administrator.name()) || roles.contains(OrganisationRoles.author.name())
				|| roles.contains(OrganisationRoles.curriculummanager.name()) || roles.contains(OrganisationRoles.groupmanager.name())
				|| roles.contains(OrganisationRoles.learnresourcemanager.name()) || roles.contains(OrganisationRoles.poolmanager.name())
				|| roles.contains(OrganisationRoles.sysadmin.name()) || roles.contains(OrganisationRoles.usermanager.name()));

		User persistedUser = identity.getUser();
		List<UserPropertyHandler> userPropertyHandlers = getAllUserPropertyHandlers();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			String actualProperty = userPropertyHandler.getName();
			if (UserConstants.USERNAME.equals(actualProperty)) {
				// Skip, user name will be anonymised by BaseSecurityManager
			} else if(isAdministrativeUser && (UserConstants.FIRSTNAME.equals(actualProperty) || UserConstants.LASTNAME.equals(actualProperty))) {
				// Skip first name and last name of user with administrative functions
			} else {
				persistedUser.setProperty(actualProperty, null);
				log.debug("Deleted user-property::" + actualProperty + " for identity::+" + identity.getKey());
			}
		}
		updateUserFromIdentity(identity);
		log.info("deleteUserProperties user::" + persistedUser.getKey() + " from identity::" + identity.getKey());
		dbInstance.commit();
	}

	@Override
	public String getExporterID() {
		return "profile";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File profileArchive = new File(archiveDirectory, "UserProfile.xlsx");
		try(OutputStream out = new FileOutputStream(profileArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			
			Row row = sheet.newRow();
			row.addCell(0, "Created");
			row.addCell(1, identity.getCreationDate(), workbook.getStyles().getDateTimeStyle());
			row.addCell(0, "User name");
			row.addCell(1, identity.getName());
			
			User user = identity.getUser();
			Translator translator = getPropertyHandlerTranslator(Util.createPackageTranslator(UserManager.class, locale));
			List<UserPropertyHandler> userPropertyHandlers = getAllUserPropertyHandlers();
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				String actualProperty = userPropertyHandler.getName();
				if(UserConstants.USERNAME.equals(actualProperty) || "creationDateDisplayProperty".equals(actualProperty)
						|| "lastloginDateDisplayProperty".equals(actualProperty)) {
					continue;//
				}
				String key = translator.translate("form.name." + actualProperty);
				String value = user.getProperty(actualProperty, locale);
				exportKeyValue(key, value, sheet);
			}
			
			sheet.newRow();
			row = sheet.newRow();
			row.addCell(0, "Last login");
			row.addCell(1, identity.getLastLogin(), workbook.getStyles().getDateTimeStyle());
			exportKeyValue("External ID", identity.getExternalId(), sheet);

			// preferences
			sheet.newRow();
			sheet.newRow().addCell(0, "Settings");
			Preferences preferences = user.getPreferences();
			exportKeyValue("Language", preferences.getLanguage(), sheet);
			exportKeyValue("Notification", preferences.getNotificationInterval(), sheet);
			exportKeyValue("Real mail", preferences.getReceiveRealMail(), sheet);
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile(profileArchive.getName());
	}
	
	private void exportKeyValue(String key, String value, OpenXMLWorksheet sheet) {
		if(StringHelper.containsNonWhitespace(value)) {
			Row row = sheet.newRow();
			row.addCell(0, key);
			row.addCell(1, value);
		}
	}

	@Override
	public void clearAllUserProperties(Identity identity) {
		User persistedUser = identity.getUser();
		List<UserPropertyHandler> userPropertyHandlers = getAllUserPropertyHandlers();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			String actualProperty = userPropertyHandler.getName();
			if (UserConstants.USERNAME.equals(actualProperty)) {
				// Skip, user name will be anonymised by BaseSecurityManager
			} else {
				persistedUser.setProperty(actualProperty, null);
				log.debug("Deleted user-property::" + actualProperty + " for identity::+" + identity.getKey());
			}
		}
		updateUserFromIdentity(identity);
		log.info("clearUserProperties user::" + persistedUser.getKey() + " from identity::" + identity.getKey());
		dbInstance.commit();
	}
}
