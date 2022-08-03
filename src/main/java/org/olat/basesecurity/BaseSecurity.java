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

package org.olat.basesecurity;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.basesecurity.model.FindNamedIdentityCollection;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.id.User;
import org.olat.core.util.Encoder;

/**
 * 
 * @author Felix Jost
 */
public interface BaseSecurity {
	
	public static final String DEFAULT_ISSUER = "DEFAULT";

	
	/**
	 * Get the identity's roles
	 * 
	 * @param identity
	 * @return The roles of the identity
	 */
	public Roles getRoles(IdentityRef identity);
	
	public Roles getRoles(IdentityRef identity, boolean withInherited);
	
	/**
	 * Get the list of roles as string without inheritance (an admin
	 * has only admin role and not the user manager role...).
	 * @param identity
	 * @return
	 */
	public List<String> getRolesAsString(IdentityRef identity);
	
	public List<String> getRolesAsString(IdentityRef identity, OrganisationRef organisation);
	
	/**
	 * This method need several queries to catch all roles.
	 * 
	 * @param identity
	 * @return
	 */
	public List<String> getRolesSummaryWithResources(IdentityRef identity);
	
	
	/**
	 * Update the roles
	 * @param actingIdentity The identity who is performing the change
	 * @param updatedIdentity The identity that is changed
	 * @param roles The new roles to set
	 */
	public void updateRoles(Identity actingIdentity, Identity updatedIdentity, RolesByOrganisation organisation);

	public boolean isGuest(IdentityRef identity);
	
	public Identity findIdentityByLogin(String login);
	
	public Identity findIdentityByNickName(String name);
	
	/**
	 * Search an identity by identity name, nick name and authentications
	 * names.
	 * 
	 * @param username
	 * @return
	 */
	public Identity findIdentityByUsernames(String username);

	/**
	 * Find an identity by its name. This is an exact match. Use the
	 * getIdentititesByPowerSearch() method if you also want to find substrings.
	 * <p>
	 * Be aware that this method does <b>not</b> check the identities status!
	 * This method returns identities with any state, also deleted identities!
	 * 
	 * @param identityName
	 * @return the identity or null if not found
	 */
	public Identity findIdentityByName(String identityName);
	
	public Identity findIdentityByNameCaseInsensitive(String identityName);

	/**
	 * Specialized method to import identities in courses, groups... The query
	 * search in the legacy name of identity, in authentication user name,
	 * concat first and last name, email, institutional number, institutional
	 * email. It's all case insensitive.
	 * 
	 * @param identityNames A list of names
	 * @return A list of identities
	 */
	public List<FindNamedIdentity> findIdentitiesBy(Collection<String> names);
	
	/**
	 * Find the identities by
	 * {@link #findIdentityByNameCaseInsensitive(Collection<String>)} and separate
	 * them in unique identities, duplicate identities and not found identity names.
	 *
	 * @param names
	 * @return
	 */
	public FindNamedIdentityCollection findAndCollectIdentitiesBy(Collection<String> names);
	
	/**
	 * Find the identities by
	 * {@link #findIdentityByNameCaseInsensitive(Collection<String>)} and separate
	 * them in unique identities, duplicate identities and not found identity names.
	 *
	 * @param names The list of strings to search for
	 * @param organisations Limit to the specified list of organizations
	 * @return
	 */
	public FindNamedIdentityCollection findAndCollectIdentitiesBy(Collection<String> names, List<Organisation> organisations);
	
	/**
	 * Find an identity by its user
	 * @param user
	 * @return The identity or null if not found
	 */
	public Identity findIdentityByUser(User user);
	
	/**
	 * Find identities by names. This is an exact match.
	 * <p>
	 * Be aware that this method does <b>not</b> check the identities status!
	 * This method returns identities with any state, also deleted identities!
	 * 
	 * @param identityNames
	 * @return The identities
	 */
	public List<IdentityShort> findShortIdentitiesByName(Collection<String> identityName);
	
	/**
	 * Find identities by keys. This is an exact match.
	 * <p>
	 * Be aware that this method does <b>not</b> check the identities status!
	 * This method returns identities with any state, also deleted identities!
	 * 
	 * @param identityNames
	 * @return The identities
	 */
	public List<IdentityShort> findShortIdentitiesByKey(Collection<Long> identityKeys);

	/**
	 * find an identity by the key instead of the username. Prefer this method as
	 * findByName will become deprecated soon.
	 * 
	 * @param identityKey the key of the identity to load; may not be null or zero
	 * @return the identity or an exception if not found
	 */
	public Identity loadIdentityByKey(Long identityKey);
	
	/**
	 * 
	 * @param search The search
	 * @return A list of identities (short version)
	 */
	public List<IdentityShort> searchIdentityShort(String search, int maxResults);
	
	/**
	 * The search look into name, first name, last name and email.
	 * 
	 * @param search Search string
	 * @param searchableOrganisations The organisations where the identities are
	 * @param repositoryEntryRole restrict to role
	 * @param maxResults The max results or -1
	 * @return A list of identities (short version)
	 */
	public List<IdentityShort> searchIdentityShort(String search, List<? extends OrganisationRef> searchableOrganisations,
			GroupRoles repositoryEntryRole, OrganisationRoles[] excludedRoles, int maxResults);

	public IdentityShort loadIdentityShortByKey(Long identityKey);
	
	/**
	 * Load a list of identities by their keys.
	 * 
	 * @param identityKeys
	 * @return A list of identities
	 */
	public List<Identity> loadIdentityByKeys(Collection<Long> identityKeys);
	
	public List<Identity> loadIdentityByRefs(Collection<IdentityRef> identityRefs);
	
	
	/**
	 * Load a list of identities (short) by their keys
	 * @param identityKeys
	 * @return
	 */
	public List<IdentityShort> loadIdentityShortByKeys(Collection<Long> identityKeys);
	
	/**
	 * find an identity by the key or return null if no identity found
	 * 
	 * @param identityKey the key of the identity to load; may not be null or zero
	 * @return the identity or null
	 */
	public Identity loadIdentityByKey(Long identityKey, boolean strict);
	
	/**
	 * Method to load all the visible identities. Paging is mandatory!
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<Identity> loadVisibleIdentities(int firstResult, int maxResults);
	
	/**
	 * 
	 * @return The keys of identities where status < STATUS_VISIBLE_LIMIT
	 */
	public List<Long> loadVisibleIdentityKeys();

	/**
	 * get number of users with last login greater than lastLoginLimit
	 * @param lastLoginLimit
	 * @return
	 */
	public Long countUniqueUserLoginsSince (Date lastLoginLimit);
	

	/**
	 * @param identityName Optionally set the name of identity (if null it will be generated), must be unique and cannot be changed
	 * @param nickName An optional nickname
	 * @param externalId An optional external id
	 * @param user The unpersisted user
	 * @param provider The provider of the authentication ("OLAT", "LDAP"...). If
	 *          null, no authentication token is generated.
	 * @param authusername The user name used as authentication credential
	 *          (=username for provider "OLAT")
	 * @param password The password which will be used as credentials (not hashed it)
	 * @return The new persisted identity
	 */
	public Identity createAndPersistIdentityAndUser(String identityName, String nickName, String externalId,
			User user, String provider, String issuer, String authusername, String password, Date expirationDate);
	
	/**
	 * Persists the given user, creates an identity for it and adds the user to
	 * the specified organization or the default one, create an authentication for
	 * an external provider if the provider is specified.
	 * 
	 * @param identityName Optionally set the name of identity (if null it will be generated), must be unique and cannot be changed
	 * @param nickName An optional nickname
	 * @param externalId An optional external id
	 * @param user The unpersisted user
	 * @param provider The provider of the authentication ("OLAT", "LDAP"...). If
	 *          null, no authentication token is generated.
	 * @param authusername The user name used as authentication credential
	 *          (=username for provider "OLAT")
	 * @param pwd The password which will be used as credentials (not hashed it)
	 * @param organisation The organization or null, null will be the default organization
	 * @return The new persisted identity
	 */
	public Identity createAndPersistIdentityAndUserWithOrganisation(String identityName, String nickName, String externalId,
			User user, String provider, String issuer, String authusername, String password, Organisation organisation, Date expirationDate);
	

	/**
	 * Return the List of associated Authentications.
	 * 
	 * @param identity
	 * @return a list of Authentication
	 */
	public List<Authentication> getAuthentications(IdentityRef identity);

	/**
	 * @param identity
	 * @param provider
	 * @return Authentication for this identity and provider or NULL if not
	 *         found
	 */
	public Authentication findAuthentication(IdentityRef identity, String provider, String issuer);
	
	public List<Authentication> findAuthentications(IdentityRef identity, List<String> providers);
	
	/**
	 * This returns the user name by priority: first LDAP, Shibboleth, OLAT
	 * and after the social logins. The WebDAV logins are excluded. The method
	 * returns always the same name.
	 * 
	 * @param identity The identity
	 * @return A user name
	 */
	public String findAuthenticationName(IdentityRef identity);
	
	public String findAuthenticationName(IdentityRef identity, String provider, String issuer);
	
	
	/**
	 * Find authentication which are older than a specific date.
	 * @param provider The provider
	 * @param creationDate The date's limit
	 * @return
	 */
	public List<Authentication> findOldAuthentication(String provider, Date creationDate);
	
	/**
	 * Authentication with a security token
	 * @param provider The provider
	 * @param securityToken The security token
	 * @return
	 */
	public List<Authentication> findAuthenticationByToken(String provider, String securityToken);

	/**
	 * @param identity
	 * @param provider
	 * @param authUsername
	 * @param credential
	 * @return an Authentication
	 */
	public Authentication createAndPersistAuthentication(Identity identity, String provider, String issuer,
			String authUsername, String password, Encoder.Algorithm algoritm);

	/**
	 * @param authentication
	 */
	public void deleteAuthentication(Authentication authentication);
	
	/**
	 * Deletes invalid authentications with the specified email address in the field
	 * username. An authentication is invalid if no unique user with that email
	 * exists. Use this method to clean old authentications after the change of the
	 * email address of a user to avoid duplicate authentications. Because a user
	 * can have an email address as his username, the authentication of the OLAT
	 * authentication provider is never deleted.
	 * 
	 * @param email
	 */
	public void deleteInvalidAuthenticationsByEmail(String email);
	
	/**
	 * 
	 * @param authentication
	 */
	public Authentication updateAuthentication(Authentication authentication);
	
	/**
	 * Check if the password is allowed.
	 * 
	 * @param identity
	 * @param provider
	 * @param password
	 * @param historyLength
	 * @return
	 */
	public boolean checkCredentialHistory(Identity identity, String provider, String password, int historyLength);
	
	/**
	 * 
	 * @param authentication
	 * @param password
	 * @param algorithm
	 * @return
	 */
	public boolean checkCredentials(Authentication authentication, String password);
	
	/**
	 * Updated the hashed password to a new one
	 * @param authentication
	 * @param password
	 * @param algorithm
	 * @return
	 */
	public Authentication updateCredentials(Authentication authentication, String password, Encoder.Algorithm algorithm);

	/**
	 * @param authusername The authentication user name
	 * @param provider The provider (OLAT, LDAP...)
	 * @param issuer The issuer (DEFAULT)
	 * @return Authentication for this authentication user name and provider or NULL if not
	 *         found
	 */
	public Authentication findAuthenticationByAuthusername(String authusername, String provider, String issuer);
	
	public Authentication findAuthenticationByKey(Long authenticationKey);

	public List<Authentication> findAuthenticationsByAuthusername(String authusername, List<String> providers);


	/**
	 * Get a list of identities that match the following conditions. All
	 * parameters are additive. NULL values mean "no constraints" (e.g. all
	 * parameters NULL would result in a list with all identities of the entire
	 * system)
	 * 
	 * @param login
	 * @param userPropertyHandlers Map of user properties that needs to be
	 *          matched.
	 * @param userPropertiesAsIntersectionSearch true: user properties and login
	 *          name are combined with an AND query; false: user properties and
	 *          login name are combined with an OR query
	 * @param groups Array of SecurityGroups the user participates in. Search
	 *          machtches if user is in any of the groups (OR query)
	 * @param permissionOnResources Array of resource permissions the user has.
	 *          Search machtches if user has any of the permissions (OR query)
	 * @param authProviders Array of authenticaton providers the user has. Search
	 *          machtches if user has any of the authProviders (OR query)
	 * @param createdAfter date after which the user has been created
	 * @param createdBefore date before which the user has been created
	 * @return List of identities
	 */
	public List<Identity> getVisibleIdentitiesByPowerSearch(String login,
			Map<String, String> userProperties, boolean userPropertiesAsIntersectionSearch,
			OrganisationRoles[] role, String[] authProviders, Date createdAfter,
			Date createdBefore);
	
	public int countIdentitiesByPowerSearch(SearchIdentityParams params);
	
	/**
	 * Like the following method but compact
	 * @param params
	 * @return
	 */
	public List<Identity> getIdentitiesByPowerSearch(SearchIdentityParams params, int firstResult, int maxResults);
	
	public List<Identity> getVisibleIdentitiesByPowerSearch(String login,
			Map<String, String> userProperties, boolean userPropertiesAsIntersectionSearch,
			OrganisationRoles[] roles, String[] authProviders, Date createdAfter,
			Date createdBefore, int firstResult, int maxResults);
	
	/**
	 * Get a list of identities that match the following conditions. All
	 * parameters are additive. NULL values mean "no constraints" (e.g. all
	 * parameters NULL would result in a list with all identities of the entire
	 * system)
	 * 
	 * @param login
	 * @param userPropertyHandlers Map of user properties that needs to be
	 *          matched.
	 * @param userPropertiesAsIntersectionSearch true: user properties and login
	 *          name are combined with an AND query; false: user properties and
	 *          login name are combined with an OR query
	 * @param groups Array of SecurityGroups the user participates in. Search
	 *          machtches if user is in any of the groups (OR query)
	 * @param permissionOnResources Array of resource permissions the user has.
	 *          Search machtches if user has any of the permissions (OR query)
	 * @param authProviders Array of authenticaton providers the user has. Search
	 *          machtches if user has any of the authProviders (OR query)
	 * @param createdAfter date after which the user has been created
	 * @param createdBefore date before which the user has been created
	 * @param userLoginBefore date before the user has logged in the last time
	 * @param userLoginAfter date after the user has logged in the last time
	 * @param status identity status, define in interface Identity e.g. ACTIV,
	 *          LOGIN_DENIED, DELETED
	 * @return List of identities
	 */
	public List<Identity> getIdentitiesByPowerSearch(String login, Map<String, String> userProperties, boolean userPropertiesAsIntersectionSearch, 
			OrganisationRoles[] roles, String[] authProviders, Date createdAfter,
			Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status);
	
	/**
	 * See the method above.
	 * @param login
	 * @param userProperties
	 * @param userPropertiesAsIntersectionSearch
	 * @param groups
	 * @param permissionOnResources
	 * @param authProviders
	 * @param createdAfter
	 * @param createdBefore
	 * @param userLoginAfter
	 * @param userLoginBefore
	 * @param status
	 * @return
	 */
	public long countIdentitiesByPowerSearch(String login, Map<String, String> userProperties, boolean userPropertiesAsIntersectionSearch, 
			OrganisationRoles[] roles, String[] authProviders, Date createdAfter,
			Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status);
	
	/**
	 * 
	 * @param identity The identity with a new status
	 * @param status The status to set
	 * @param doer The identity which is acting
	 * @return
	 */
	public Identity saveIdentityStatus(Identity identity, Integer status, Identity doer);
	
	public Identity saveIdentityExpirationDate(Identity identity, Date expirationDate, Identity doer);
	
	/**
	 * Set the status of the identity to active and reset all inactivation dates:
	 * inactivation date, inactivation email date and reactivation date. A call
	 * to @see setIdentityLastLogin is needed to prevent the status to be
	 * set to inactive again.
	 * 
	 * @param identity the identity to reactivate
	 * @return The merged identity
	 */
	public Identity reactivatedIdentity(Identity identity);
	
	/**
	 * Set the date of the last login
	 * @param identity
	 * @return
	 */
	public void setIdentityLastLogin(IdentityRef identity);
	
	/**
	 * Set the identity name. 
	 * <p><b>NOTE: do not use this to rename identities during
	 * lifetime! This is currently not supported. This method does only rename
	 * the identity on the database, however it does NOT rename anything on the
	 * filesystem. </b></p>
	 * <p>Unfortunately there are references to the identity name on
	 * the filesystem, thus just using this method is not save at all. This
	 * method is intended for renaming the identity after the delete process.
	 * </p>
	 * @param identity The identity to be renamed
	 * @param newName The new identity name
	 * @return The reloaded and renamed identity
	 */
	public Identity saveIdentityName(Identity identity, String newName, String newExertnalId);
	
	/**
	 * the method doesn't set the status deleted, it will set the user
	 * who deleted the specified identity, the list of roles and the date.
	 * 
	 * @param identity The identity to set the data of
	 * @param doer The identity which is acting
	 * @return The merged identity
	 */
	public Identity saveDeletedByData(Identity identity, Identity doer);
	
	/**
	 * Set an external id if the identity is managed by an external system.
	 * 
	 * @param identity
	 * @param externalId
	 * @return
	 */
	public Identity setExternalId(Identity identity, String externalId);
	
	/**
	 * Check if identity is visible. Deleted or login-denied users are not visible.
	 * @param identity
	 * @return
	 */
	public boolean isIdentityVisible(Identity identity);

	/**
	 * Check if identity is allowed to log in. Deleted, login-denied or pending users
	 * are not allowed to login.
	 * 
	 * @param identity The identity
	 * @param provider  The name of the authentication provider
	 * @return true if active, inactive...
	 */
	public boolean isIdentityLoginAllowed(Identity identity, String provider);
	

	/**
	 * Returns the anonymous identity for a given locale, normally used to log in
	 * as guest user
	 * 
	 * @param locale
	 * @return The identity
	 */
	public Identity getAndUpdateAnonymousUserForLanguage(Locale locale);

}