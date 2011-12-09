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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.resource.OLATResource;

/**
 * Description: <br>
 * TODO: Class Description
 * <P>
 * 
 * @author Felix Jost
 */
public interface BaseSecurity {

	/**
	 * 
	 */
	public void init();

	/**
	 * is allowed to....
	 * 
	 * @param identity
	 * @param permission
	 * @param olatResourceable
	 * @return true if permitted
	 */
	public boolean isIdentityPermittedOnResourceable(Identity identity, String permission, OLATResourceable olatResourceable);

	/**
	 * Get the identity's roles
	 * 
	 * @param identity
	 * @return The roles of the identity
	 */
	public Roles getRoles(Identity identity);

	/**
	 * @param identity
	 * @param permission
	 * @param olatResourceable
	 * @param checkTypeRight
	 * @return true if permitted
	 */
	public boolean isIdentityPermittedOnResourceable(Identity identity, String permission, OLATResourceable olatResourceable,
			boolean checkTypeRight);

	/**
	 * use only if really needed. Normally better use
	 * isIdentityPermittedOnResourceable!
	 * 
	 * @param identity
	 * @param secGroup
	 * @return true if the identity is in the group
	 */
	public boolean isIdentityInSecurityGroup(Identity identity, SecurityGroup secGroup);

	/**
	 * search
	 * 
	 * @param secGroup
	 * @return list of Identities
	 */
	public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup);
	
	public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup, int firstResult, int maxResults);
	/**
	 * Return the primary key of
	 * @param secGroups
	 * @return
	 */
	public List<Identity> getIdentitiesOfSecurityGroups(List<SecurityGroup> secGroups);

	//fxdiff: FXOLAT-219 decrease the load for synching groups
	public List<IdentityShort> getIdentitiesShortOfSecurityGroups(List<SecurityGroup> secGroups);

	/**
	 * @param secGroup
	 * @return a List of Object[] with the array[0] = Identity, array[1] =
	 *         addedToGroupTimestamp
	 */
	public List<Object[]> getIdentitiesAndDateOfSecurityGroup(SecurityGroup secGroup);

	/**
	 * @see org.olat.basesecurity.Manager#getIdentitiesAndDateOfSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 * @param sortedByAddDate true= return list of idenities sorted by added date
	 */
	public List<Object[]> getIdentitiesAndDateOfSecurityGroup(SecurityGroup secGroup, boolean sortedByAddDate);
	
	
	/**
	 * Get date where identity joined a security group
	 * @param secGroup
	 * @param identity
	 * @return joindate of given securityGroup. May return null if group doesn't exist or user isn't in this group
	 */
	public Date getSecurityGroupJoinDateForIdentity(SecurityGroup secGroup, Identity identity);
	
	/**
	 * @param securityGroupName
	 * @return the securitygroup
	 */
	public SecurityGroup findSecurityGroupByName(String securityGroupName);

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

	/**
	 * find an identity by the key instead of the username. Prefer this method as
	 * findByName will become deprecated soon.
	 * 
	 * @param identityKey the key of the identity to load; may not be null or zero
	 * @return the identity or an exception if not found
	 */
	public Identity loadIdentityByKey(Long identityKey);
	
	/**
	 * find an identity by the key or return null if no identity found
	 * 
	 * @param identityKey the key of the identity to load; may not be null or zero
	 * @return the identity or null
	 */
	public Identity loadIdentityByKey(Long identityKey, boolean strict);

	/**
	 * get number of users with last login greater than lastLoginLimit
	 * @param lastLoginLimit
	 * @return
	 */
	public Long countUniqueUserLoginsSince (Date lastLoginLimit);
	
	/**
	 * @param secGroup
	 * @return nr of members in the securitygroup
	 */
	public int countIdentitiesOfSecurityGroup(SecurityGroup secGroup);

	/**
	 * @param username the username
	 * @param user the unpresisted User
	 * @param provider the provider of the authentication ("OLAT" or "AAI"). If
	 *          null, no authentication token is generated.
	 * @param authusername the username used as authentication credential
	 *          (=username for provider "OLAT")
	 * @param credential the credentials or null if not used
	 * @return the new identity
	 */
	public Identity createAndPersistIdentity(String username, User user, String provider, String authusername, String credential);

	/**
	 * @param username the username
	 * @param user the unpresisted User
	 * @param provider the provider of the authentication ("OLAT" or "AAI"). If
	 *          null, no authentication token is generated.
	 * @param authusername the username used as authentication credential
	 *          (=username for provider "OLAT")
	 * @param credential the credentials or null if not used
	 * @return the new identity
	 */
	public Identity createAndPersistIdentityAndUser(String username, User user, String provider, String authusername, String credential);

	/**
	 * Return the List of associated Authentications.
	 * 
	 * @param identity
	 * @return a list of Authentication
	 */
	public List<Authentication> getAuthentications(Identity identity);

	/**
	 * @param identity
	 * @param provider
	 * @return Authentication for this identitity and provider or NULL if not
	 *         found
	 */
	public Authentication findAuthentication(Identity identity, String provider);

	//fxdiff: FXOLAT-219 decrease the load for synching groups
	public boolean hasAuthentication(Long identityKey, String provider);

	/**
	 * @param identity
	 * @param provider
	 * @param authUsername
	 * @param credential
	 * @return an Authentication
	 */
	public Authentication createAndPersistAuthentication(Identity identity, String provider, String authUsername, String credential);

	/**
	 * @param authentication
	 */
	public void deleteAuthentication(Authentication authentication);

	// --- SecGroup management

	/**
	 * create only makes no sense, since there are no attibutes to set
	 * 
	 * @return a new persisted SecurityGroup or throws an Exception
	 */
	public SecurityGroup createAndPersistSecurityGroup();

	/**
	 * create only makes no sense, since there are no attibutes to set
	 * 
	 * @param groupName
	 * @return the newly created securitygroup
	 */
	public SecurityGroup createAndPersistNamedSecurityGroup(String groupName); // 

	/**
	 * removes the group with all the idendities contained in it, the idenities
	 * itself are of course not deleted.
	 * 
	 * @param secGroup
	 */
	public void deleteSecurityGroup(SecurityGroup secGroup);

	/**
	 * @param identity
	 * @param secGroup
	 */
	public void addIdentityToSecurityGroup(Identity identity, SecurityGroup secGroup);

	/**
	 * Removes the identity from this security group or does nothing if the
	 * identity is not in the group at all.
	 * 
	 * @param identity
	 * @param secGroup
	 */
	public void removeIdentityFromSecurityGroup(Identity identity, SecurityGroup secGroup);

	// --- Policy management
	// again no pure RAM creation, since all attributes are mandatory and given by
	// the system, not by user input
	/**
	 * the olatResourceable is not required to have some persisted implementation,
	 * but the manager will use the OLATResource to persist it. If the
	 * olatResourceable used OLATResource as its persister, then the same
	 * OLATResource (same row in table) will be used by the manager use as
	 * internal reference in the Policy table
	 * 
	 * @param secGroup
	 * @param permission
	 * @param olatResourceable
	 * @return the newly created policy
	 */
	public Policy createAndPersistPolicy(SecurityGroup secGroup, String permission, OLATResourceable olatResourceable);
	
	public Policy createAndPersistPolicy(SecurityGroup secGroup, String permission, Date from, Date to, OLATResourceable olatResourceable);


	/**
	 * Creates and persist a policy for certain OLAT-resource (instead of OLAT-resourceable)
	 * 
	 * @param secGroup
	 * @param permission
	 * @param olatResource
	 * @return the newly created policy
	 */
	public Policy createAndPersistPolicyWithResource(SecurityGroup secGroup, String permission, OLATResource olatResource);

	public Policy createAndPersistPolicyWithResource(SecurityGroup secGroup, String permission, Date from, Date to, OLATResource olatResource);

	
	/**
	 * Create and persist an invitation with its security group and security token.
	 * @return
	 */
	public Invitation createAndPersistInvitation();
	
	/**
	 * Update the invitation
	 * @param invitation
	 */
	public void updateInvitation(Invitation invitation);
	
	/**
	 * Is the invitation linked to any valid policies
	 * @param token
	 * @param atDate
	 * @return
	 */
	public boolean hasInvitationPolicies(String token, Date atDate);
	
	/**
	 * Find an invitation by its security group
	 * @param secGroup
	 * @return The invitation or null if not found
	 */
	public Invitation findInvitation(SecurityGroup secGroup);
	
	/**
	 * Find an invitation by its security token
	 * @param token
	 * @return The invitation or null if not found
	 */
	public Invitation findInvitation(String token);
	
	/**
	 * Check if the identity has an invitation, valid or not
	 * @param identity
	 * @return
	 */
	public boolean isIdentityInvited(Identity identity);
	
	/**
	 * Delete an invitation
	 * @param invitation
	 */
	public void deleteInvitation(Invitation invitation);
	
	/**
	 * Clean up old invitation and set to deleted temporary users
	 */
	public void cleanUpInvitations();
	
	/**
	 * @param secGroup
	 * @param permission
	 * @param olatResourceable
	 */
	public void deletePolicy(SecurityGroup secGroup, String permission, OLATResourceable olatResourceable);

	// public void deletePolicy(Policy policy); //just deletes the policy, but not
	// the resource

	// some queries mainly for the group/groupcontext management
	/**
	 * @param secGroup
	 * @return a list of Policy objects
	 */
	public List<Policy> getPoliciesOfSecurityGroup(SecurityGroup secGroup);

/**
 * Return the policies
 * @param resource The resource (mandatory)
 * @param securityGroup The securityGroup (optional)
 * @return
 */
	public List<Policy> getPoliciesOfResource(OLATResourceable resource, SecurityGroup securityGroup);
	
	/**
	 * Update the policy valid dates
	 * @param policy
	 * @param from
	 * @param to
	 */
	public void updatePolicy(Policy policy, Date from, Date to);
	
	/**
	 * use for testing ONLY.
	 * 
	 * @param permission
	 * @param olatResourceable
	 * @return a list of SecurityGroup objects
	 */
	public List<SecurityGroup> getGroupsWithPermissionOnOlatResourceable(String permission, OLATResourceable olatResourceable);

	/**
	 * use for testing ONLY.
	 * 
	 * @param permission
	 * @param olatResourceable
	 * @return a list of Identity objects
	 */
	public List<Identity> getIdentitiesWithPermissionOnOlatResourceable(String permission, OLATResourceable olatResourceable);

	/**
	 * for debugging and info by the olat admins:
	 * 
	 * @param identity
	 * @return scalar query return list of object[] with SecurityGroupImpl,
	 *         PolicyImpl, OLATResourceImpl
	 */
	public List<Identity> getPoliciesOfIdentity(Identity identity);

	/**
	 * @param authusername
	 * @param provider
	 * @return Authentication for this authusername and provider or NULL if not
	 *         found
	 */
	public Authentication findAuthenticationByAuthusername(String authusername, String provider);


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
	public List<Identity> getVisibleIdentitiesByPowerSearch(String login, Map<String, String> userProperties, boolean userPropertiesAsIntersectionSearch, SecurityGroup[] groups, PermissionOnResourceable[] permissionOnResources, String[] authProviders, Date createdAfter,
			Date createdBefore);
	
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
			SecurityGroup[] groups, PermissionOnResourceable[] permissionOnResources, String[] authProviders, Date createdAfter,
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
			SecurityGroup[] groups, PermissionOnResourceable[] permissionOnResources, String[] authProviders, Date createdAfter,
			Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status);
	
	
	/** Save an identity
	 * @param identity  Save this identity
	 */
	public void saveIdentityStatus(Identity identity, Integer status);
	
	/**
	 * Check if identity is visible. Deleted or login-denied users are not visible.
	 * @param identityName
	 * @return
	 */
	public boolean isIdentityVisible(String identityName);
	
	/**
	 * Get all SecurtityGroups an Identity is in
	 * @param identity
	 * @return List with SecurityGroups
	 */
	public List<SecurityGroup> getSecurityGroupsForIdentity(Identity identity);

	/**
	 * Returns the anonymous identity for a given locale, normally used to log in
	 * as guest user
	 * 
	 * @param locale
	 * @return The identity
	 */
	public Identity getAndUpdateAnonymousUserForLanguage(Locale locale);

}