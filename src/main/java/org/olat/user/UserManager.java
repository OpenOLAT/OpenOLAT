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

package org.olat.user;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.IdentityNames;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * <h3>Description:</h3>
 * The user manager provides methods to handle user objects. This includes some
 * search methods and methods for the users property handling.
 * <p>
 * Most search methods are not implemented on the user manager but rather on the
 * security manager from the base security package. See ManagerFactory.getManager()
 * <p>
 * Initial Date: Jun 23, 2004 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public abstract class UserManager {
	protected static UserManager INSTANCE;
	
	// injected by spring configuration
	protected UserPropertiesConfig userPropertiesConfig;

	/**
	 * Use getInstance method
	 */
	protected UserManager() {
		//
	}

	/**
	 * Factory method: Loads the user manager that is configured in the spring
	 * config
	 * 
	 * @return Instance of a UserManager
	 */
	public static final UserManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Create a transient user object. Use SaveUser to persist the object or use
	 * the createAndPersistUser method.
	 * 
	 * @param firstName
	 * @param lastName
	 * @param eMail
	 * @return New user instance
	 */
	public abstract User createUser(String firstName, String lastName, String eMail);

	/**
	 * Find all user database keys where the given property name matches the property
	 * value (exact match)
	 * 
	 * @param propName
	 * @param propValue
	 * @return
	 */
	public abstract List<Long> findUserKeyWithProperty(String propName, String propValue);
	
	/**
	 * Find all identities where the given property name matches the property
	 * value (exact match)
	 * 
	 * @param propName
	 * @param propValue
	 * @return The list of identities or NULL if nothing found
	 */
	public abstract List<Identity> findIdentitiesWithProperty(String propName, String propValue);

	/**
	 * Find the identity by the email address. It is searched by the whole email
	 * address. If no identity is found or if more then one identity is found, null
	 * is returned.
	 * 
	 * @param email
	 * @return the found identity or null
	 */
	public abstract Identity findUniqueIdentityByEmail(String email);
	
	/**
	 * Find the identity (and the user) that match the given email address. The
	 * match is an exact match
	 * 
	 * @param email A list of emails to search with
	 * @return The identities found for these emails
	 */
	public abstract List<Identity> findIdentitiesByEmail(List<String> emails);

	/**
	 * Find all visible identities without an email address.
	 * 
	 * @return
	 */
	public abstract List<Identity> findVisibleIdentitiesWithoutEmail();
	
	/**
	 * Find all visible identities with email duplicates.
	 * 
	 * @return
	 */
	public abstract List<Identity> findVisibleIdentitiesWithEmailDuplicates();
	
	/**
	 * Check if the email of an user can be set or changed to this value.
	 * 
	 * @param email
	 */
	public abstract boolean isEmailAllowed(String email);
	
	/**
	 * Check if the email of an user can be set or changed to this value. This
	 * method returns true if the email is the current email or the current
	 * institutional email of the user as well.
	 * 
	 * @param email
	 * @param user
	 */
	public abstract boolean isEmailAllowed(String email, User user);

	/**
	 * Find user by its key (database primary key)
	 * 
	 * @param key the primary key
	 * @return User if the user has been found or null if not found
	 */
	public abstract User loadUserByKey(Long key);

	/**
	 * Updates a user in the database. 
	 * 
	 * @param identityRef 
	 * @param user The user object to be updated
	 * @return The true if successfully updated
	 */
	public abstract User updateUser(IdentityRef identityRef, User user);

	/**
	 * Updates the user object for a given identity
	 * 
	 * @param identity
	 * @return true if successful.
	 */
	public abstract boolean updateUserFromIdentity(Identity identity);
	
	public abstract void clearAllUserProperties(Identity identity);

	/**
	 * Saves or updates the stringValue of the user's charset property 
	 * 
	 * @param identity
	 * @param charset
	 */
	public abstract void setUserCharset(Identity identity, String charset);

	/**
	 * Normaly returns the stringValue of the user's charset property. If there is
	 * no charset property or it's not supported the default value is returned.
	 * 
	 * @param identity
	 * @return String charset
	 */
	public abstract String getUserCharset(Identity identity);

	public UserPropertiesConfig getUserPropertiesConfig() {
		return userPropertiesConfig;
	}

	public List<UserPropertyHandler> getUserPropertyHandlersFor(String usageIdentifyer, boolean isAdministrativeUser) {
		return userPropertiesConfig.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
	}

	public Translator getPropertyHandlerTranslator(Translator fallBack) {
		return getUserPropertiesConfig().getTranslator(fallBack);
	}

	/**
	 * added to be usable by user-bulkChange
	 * @return
	 */
	public List<UserPropertyHandler> getAllUserPropertyHandlers(){
		return userPropertiesConfig.getAllUserPropertyHandlers();
	}
	
	public boolean isMandatoryUserProperty(String usageIdentifyer, UserPropertyHandler propertyHandler) {
		return userPropertiesConfig.isMandatoryUserProperty(usageIdentifyer, propertyHandler);
	}

	public boolean isUserViewReadOnly(String usageIdentifyer, UserPropertyHandler propertyHandler) {
		return userPropertiesConfig.isUserViewReadOnly(usageIdentifyer, propertyHandler);
	}
	
	/**
	 * Spring setter
	 * @param userPropertiesConfig
	 */
	public void setUserPropertiesConfig(UserPropertiesConfig userPropertiesConfig) {
		this.userPropertiesConfig = userPropertiesConfig;
	}
	
	public abstract int warmUp();
	
	public abstract String getUsername(Long identityKey);

	/**
	 * Returns the users displayable name, e.g. "Firstname Lastname"
	 * 
	 * @param user
	 * @return
	 */
	public abstract String getUserDisplayName(User user);
	
	/**
	 * 
	 * @param identity
	 * @return
	 */
	public abstract String getUserDisplayName(Identity identity);
	
	public abstract String getUserDisplayName(IdentityRef identity);
	
	/**
	 * 
	 * @param identityKeys
	 * @return
	 */
	public abstract Map<Long,String> getUserDisplayNamesByKey(Collection<Long> identityKeys);
	
	/**
	 * Returns the users displayable name, e.g. "Firstname Lastname"
	 * 
	 * @param user
	 * @return
	 */
	public abstract String getUserDisplayName(IdentityNames user);
	
	public abstract String getUserDisplayName(String firstName, String lastName);
	
	public abstract String getUserDisplayName(String username);
	
	public abstract String getUserDisplayName(Long identityKey);
	
	/**
	 * Return a map where the key is the username and the value is
	 * the full name
	 * @param usernames
	 * @return
	 */
	public abstract Map<String,String> getUserDisplayNamesByUserName(Collection<String> usernames);
	
	/**
	 * Return the email address of the user or a placeholder value if the user has no email address.
	 * 
	 * @param identity
	 * @param locale
	 * @return
	 */
	public abstract String getUserDisplayEmail(Identity identity, Locale local);
	
	/**
	 * Return the email address of the user or a placeholder value if the user has no email address.
	 * 
	 * @param identity
	 * @param locale
	 * @return
	 */
	public abstract String getUserDisplayEmail(User user, Locale local);
	
	/**
	 * Return the email address or a placeholder if the value is null.
	 * 
	 * @param email
	 * @param locale 
	 * @return
	 * 
	 */
	public abstract String getUserDisplayEmail(String email, Locale locale);
	
	/**
	 * This method guarantees to return an email address for the user. If the user
	 * has no presided email address, one is generated.
	 * 
	 * @param user
	 * @returns 
	 */
	public abstract String getEnsuredEmail(User user);

}
