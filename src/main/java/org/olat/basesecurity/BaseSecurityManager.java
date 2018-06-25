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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.events.NewIdentityCreatedEvent;
import org.olat.basesecurity.manager.AuthenticationHistoryDAO;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.login.LoginModule;
import org.olat.portfolio.manager.InvitationDAO;
import org.olat.resource.OLATResource;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserImpl;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <h3>Description:</h3>
 * The PersistingManager implements the security manager and provide methods to
 * manage identities and user objects based on a database persistence mechanism
 * using hibernate.
 * <p>
 * 
 * @author Felix Jost, Florian Gnaegi
 */
@Service("baseSecurityManager")
public class BaseSecurityManager implements BaseSecurity, UserDataDeletable {
	
	private static final OLog log = Tracing.createLoggerFor(BaseSecurityManager.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private AuthenticationHistoryDAO authenticationHistoryDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private IdentityPowerSearchQueries identityPowerSearchQueries;
	
	private static BaseSecurityManager INSTANCE;
	private static final String GUEST_USERNAME_PREFIX = "guest_";
	public static final OLATResourceable IDENTITY_EVENT_CHANNEL = OresHelper.lookupType(Identity.class);
	
	/**
	 * [used by spring]
	 */
	private BaseSecurityManager() {
		INSTANCE = this;
	}
	
	/**
	 * 
	 * @return the manager
	 */
	public static BaseSecurity getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isGuest(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select membership.key from organisation as org ")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey and membership.role=:role");
		List<Long> memberships = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("role", OrganisationRoles.guest.name())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return memberships != null && !memberships.isEmpty();
	}

	@Override
	public Roles getRoles(IdentityRef identity) {
		boolean isGuestOnly = false;
		boolean isInvitee = false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select org.key, org.identifier, membership.role from organisation as org ")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		Map<OrganisationRef, List<OrganisationRoles>> orgToRoles = new HashMap<>();
		
		boolean sysAdmin = false;
		boolean admin = false;
		
		boolean groupManager = false;
		boolean userManager = false;
		boolean resourceManager = false;
		boolean poolManager = false;
		boolean curriculumnManager = false;
		
		boolean author = false;
		boolean coach = false;
		
		for(Object[] rawObject:rawObjects) {
			Long organisationKey = (Long)rawObject[0];
			String organisationId = (String)rawObject[1];
			String role = (String)rawObject[2];
			if(!OrganisationRoles.isValue(role)) {
				continue;
			}
			
			boolean defOrganisation = OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER.equals(organisationId);
			
			List<OrganisationRoles> roleList = orgToRoles
					.computeIfAbsent(new OrganisationRefImpl(organisationKey), key -> new ArrayList<>());
			roleList.add(OrganisationRoles.valueOf(role));
			
			sysAdmin |= role.equals(OrganisationRoles.sysadmin.name());
			admin |= role.equals(OrganisationRoles.administrator.name());
			
			groupManager |= role.equals(OrganisationRoles.groupmanager.name());
			userManager |= role.equals(OrganisationRoles.usermanager.name());
			resourceManager |= role.equals(OrganisationRoles.learnresourcemanager.name());
			poolManager |= role.equals(OrganisationRoles.poolmanager.name());
			curriculumnManager |= role.equals(OrganisationRoles.curriculummanager.name());

			author |= role.equals(OrganisationRoles.author.name());
			coach |= role.equals(OrganisationRoles.coach.name());
		}
		

		List<String> rolesStr = getRolesAsString(identity);
		if(!rolesStr.contains(OrganisationRoles.user.name())) {
			isInvitee = invitationDao.isInvitee(identity);
			isGuestOnly = rolesStr.contains(OrganisationRoles.guest.name());
		}
		Roles roles = new Roles(sysAdmin, admin, userManager, groupManager, author, isGuestOnly, resourceManager, poolManager, curriculumnManager, coach, isInvitee);
		List<RolesByOrganisation> rolesByOrganisations = new ArrayList<>();
		for(Map.Entry<OrganisationRef, List<OrganisationRoles>> entry:orgToRoles.entrySet()) {
			rolesByOrganisations.add(new RolesByOrganisation(entry.getKey(), entry.getValue()));
		}
		roles.setRolesByOrganisation(rolesByOrganisations);
		return roles;
	}

	@Override
	public List<String> getRolesAsString(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("select membership.role from organisation as org ")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	@Override
	public List<String> getRolesAsString(IdentityRef identity, OrganisationRef organisation) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("select membership.role from organisation as org ")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey and org.key=:organisationKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("organisationKey", organisation.getKey())
				.getResultList();
	}
	
	@Override
	public void updateRoles(Identity actingIdentity, Identity updatedIdentity, RolesByOrganisation roles) {
		Organisation organisation = organisationService.getOrganisation(roles.getOrganisation());
		
		List<String> currentRoles = getRolesAsString(updatedIdentity, organisation);
		
		boolean hasBeenAnonymous = currentRoles.contains(OrganisationRoles.guest.name());
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.guest, hasBeenAnonymous, roles.isGuestOnly());
		
		// system users - opposite of anonymous users
		boolean hasBeenUser = currentRoles.contains(OrganisationRoles.user.name());
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.user, hasBeenUser, !roles.isGuestOnly());

		// coach
		boolean hasBeenAuthor = currentRoles.contains(OrganisationRoles.author.name());
		boolean isAuthor = (roles.isAuthor() || roles.isLearnResourceManager()) && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.author, hasBeenAuthor, isAuthor);

		// author
		boolean hasBeenCoach = currentRoles.contains(OrganisationRoles.coach.name());
		boolean isCoach = roles.isCoach() && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.coach, hasBeenCoach, isCoach);

		// group manager
		boolean hasBeenGroupManager = currentRoles.contains(OrganisationRoles.groupmanager.name());
		boolean groupManager = roles.isGroupManager() && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.groupmanager, hasBeenGroupManager, groupManager);
		
		// user manager, only allowed by admin
		boolean hasBeenUserManager = currentRoles.contains(OrganisationRoles.usermanager.name());
		boolean userManager = roles.isUserManager() && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.usermanager, hasBeenUserManager, userManager);

 		// institutional resource manager
		boolean hasBeenInstitutionalResourceManager = currentRoles.contains(OrganisationRoles.learnresourcemanager.name());
		boolean institutionalResourceManager = roles.isLearnResourceManager() && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.learnresourcemanager, hasBeenInstitutionalResourceManager, institutionalResourceManager);

		// institutional resource manager
		boolean hasBeenPoolManager = currentRoles.contains(OrganisationRoles.poolmanager.name());
		boolean poolManager = roles.isPoolManager() && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.poolmanager, hasBeenPoolManager, poolManager);
		
		// institutional resource manager
		boolean hasBeenCurriculumManager = currentRoles.contains(OrganisationRoles.curriculummanager.name());
		boolean curriculumManager = roles.isCurriculumManager() && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.curriculummanager, hasBeenCurriculumManager, curriculumManager);

		// system administrator
		boolean hasBeenAdmin = currentRoles.contains(OrganisationRoles.administrator.name());
		boolean isOLATAdmin = roles.isAdministrator() && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(organisation, actingIdentity, updatedIdentity,
				OrganisationRoles.administrator, hasBeenAdmin, isOLATAdmin);		
	}
	
	
	private void updateRolesInSecurityGroup(Organisation organisation, Identity actingIdentity, Identity updatedIdentity,
			OrganisationRoles role, boolean hasBeen, boolean isNow) {
		if (!hasBeen && isNow) {
			// user not yet in security group, add him
			organisationService.addMember(organisation, updatedIdentity, role);
			log.audit("User::" + (actingIdentity == null ? "unkown" : actingIdentity.getKey()) + " added system role::" + role.name() + " to user::" + updatedIdentity.getKey(), null);
		} else if (hasBeen && !isNow) {
			// user not anymore in security group, remove him
			organisationService.removeMember(organisation, updatedIdentity, role);
			log.audit("User::" + (actingIdentity == null ? "unkown" : actingIdentity.getKey()) + " removed system role::" + role.name() + " from user::" + updatedIdentity.getKey(), null);
		}
	}

	@Override
	public List<String> getRolesSummaryWithResources(IdentityRef identity) {
		List<String> openolatRoles = getRolesAsString(identity);
		
		//repository
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct membership.role from repositoryentry v ")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey");
		List<String> repositoryRoles = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		for(String repositoryRole:repositoryRoles) {
			if(repositoryRole.equals("owner")) {
				openolatRoles.add(repositoryRole);
			} else if(repositoryRole.equals("coach")) {
				openolatRoles.add("repocoach");
			}
		}
		
		// business groups
		StringBuilder gsb = new StringBuilder();
		gsb.append("select distinct membership.role from businessgroup as bgroup ")
		   .append(" inner join bgroup.baseGroup as baseGroup")
		   .append(" inner join baseGroup.members as membership")
		   .append(" where membership.identity.key=:identityKey");
		List<String> groupRoles = dbInstance.getCurrentEntityManager()
				.createQuery(gsb.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		for(String groupRole:groupRoles) {
			if(groupRole.equals("coach")) {
				openolatRoles.add("bgroupcoach");
			}
		}

		return openolatRoles;
	}


	public Policy findPolicy(SecurityGroup secGroup, String permission, OLATResource olatResource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select poi from ").append(PolicyImpl.class.getName()).append(" as poi ")
		  .append(" where poi.permission=:permission and poi.olatResource.key=:resourceKey and poi.securityGroup.key=:secGroupKey");

		List<Policy> policies = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Policy.class)
				.setParameter("permission", permission)
				.setParameter("resourceKey", olatResource.getKey())
				.setParameter("secGroupKey", secGroup.getKey())
				.getResultList();
		  		

		if (policies.isEmpty()) {
			return null;
		}
		return policies.get(0);
	}

	/**
	 * @param username The username
	 * @param user The unpresisted User
	 * @param provider The provider of the authentication ("OLAT" or "AAI"). If null, no authentication token is generated.
	 * @param authusername The username used as authentication credential (=username for provider "OLAT")
	 * @return Identity
	 */
	@Override
	public Identity createAndPersistIdentityAndUser(String username, String externalId, User user, String provider, String authusername) {
		return createAndPersistIdentityAndUser(username, externalId, user, provider, authusername, null);
	}

	/**
	 * @param username the username
	 * @param user the unpresisted User
	 * @param authusername the username used as authentication credential
	 *          (=username for provider "OLAT")
	 * @param provider the provider of the authentication ("OLAT" or "AAI"). If null, no 
	 * authentication token is generated.
	 * @param credential the credentials or null if not used
	 * @return Identity
	 */
	@Override
	public Identity createAndPersistIdentityAndUser(String username, String externalId, User user, String provider, String authusername, String credential) {
		IdentityImpl iimpl = new IdentityImpl();
		iimpl.setUser(user);
		iimpl.setName(username);
		iimpl.setLastLogin(new Date());
		iimpl.setExternalId(externalId);
		iimpl.setStatus(Identity.STATUS_ACTIV);
		((UserImpl)user).setIdentity(iimpl);
		dbInstance.getCurrentEntityManager().persist(iimpl);

		if (provider != null) { 
			createAndPersistAuthenticationIntern(iimpl, provider, authusername, credential, loginModule.getDefaultHashAlgorithm());
		}
		notifyNewIdentityCreated(iimpl);
		return iimpl;
	}
	
	/**
	 * Persists the given user, creates an identity for it and adds the user to
	 * the users system group
	 * 
	 * @param loginName
	 * @param externalId
	 * @param pwd null: no OLAT authentication is generated. If not null, the password will be 
	 * encrypted and and an OLAT authentication is generated.
	 * @param newUser unpersisted users
	 * @return Identity
	 */
	@Override
	public Identity createAndPersistIdentityAndUserWithDefaultProviderAndUserGroup(String loginName, String externalId, String pwd,
			User newUser, Organisation organisation) {
		Identity ident;
		if (pwd == null) {
			// when no password is used the provider must be set to null to not generate
			// an OLAT authentication token. See method doku.
			ident = createAndPersistIdentityAndUser(loginName, externalId, newUser, null, null);
			log.audit("Create an identity without authentication (login=" + loginName + ")");
 		} else {
			ident = createAndPersistIdentityAndUser(loginName, externalId, newUser, BaseSecurityModule.getDefaultAuthProviderIdentifier(), loginName, pwd);
			log.audit("Create an identity with " + BaseSecurityModule.getDefaultAuthProviderIdentifier() + " authentication (login=" + loginName + ")");
		}

		// Add user to the default organization as user
		if(organisation == null) {
			organisationService.addMember(ident, OrganisationRoles.user);
		} else {
			organisationService.addMember(organisation, ident, OrganisationRoles.user);
		}
		return ident;
	}
	
	/**
	 * Persists the given user, creates an identity for it and adds the user to
	 * the users system group, create an authentication for an external provider
	 * 
	 * @param loginName
	 * @param externalId
	 * @param provider
	 * @param authusername
	 * @param newUser
	 * @return
	 */
	@Override
	public Identity createAndPersistIdentityAndUserWithUserGroup(String loginName, String externalId, String provider, String authusername, User newUser) {
		Identity ident = createAndPersistIdentityAndUser(loginName, externalId, newUser, provider, authusername, null);
		log.audit("Create an identity with " + provider + " authentication (login=" + loginName + ",authusername=" + authusername + ")");
		// Add user to the default organization as user
		organisationService.addMember(ident, OrganisationRoles.user);
		return ident;
	}
	
	private void notifyNewIdentityCreated(Identity newIdentity) {
		//Save the identity on the DB. So can the listeners of the event retrieve it
		//in cluster mode
		dbInstance.commit();
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new NewIdentityCreatedEvent(newIdentity), IDENTITY_EVENT_CHANNEL);
	}



	@Override
	public Identity findIdentityByName(String identityName) {
		if (identityName == null) throw new AssertException("findIdentitybyName: name was null");

		StringBuilder sb = new StringBuilder(128);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.name=:username");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", identityName)
				.getResultList();
		
		if(identities.isEmpty()) {
			return null;
		}
		return identities.get(0);
	}
	
	@Override
	public Identity findIdentityByNameCaseInsensitive(String identityName) {
		if (identityName == null) throw new AssertException("findIdentitybyName: name was null");

		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident where lower(ident.name)=:username");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", identityName.toLowerCase())
				.getResultList();
		return identities == null || identities.isEmpty() ? null : identities.get(0);
	}

	/**
	 * Custom search operation by BiWa
	 * find identity by student/institution number 
	 * @return
	 */
	@Override
	public Identity findIdentityByNumber(String identityNumber) {
		//default initializations
		Map<String, String> userPropertiesSearch = Collections.singletonMap(UserConstants.INSTITUTIONALUSERIDENTIFIER, identityNumber);
		List<Identity> identities = getIdentitiesByPowerSearch(null, userPropertiesSearch, true, null, null, null, null, null, null, null);
		//check for unique search result
		if(identities.size() == 1) {
			return identities.get(0);
		}
		return null;
	}

	@Override
	public List<Identity> findIdentitiesByNumber(Collection<String> identityNumbers) {
		if(identityNumbers == null || identityNumbers.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" ident ")
			.append(" inner join fetch ident.user user ")
			.append(" where user.").append(UserConstants.INSTITUTIONALUSERIDENTIFIER).append(" in (:idNumbers) ")
			.append(" and ident.status<:status");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("idNumbers", identityNumbers)
				.setParameter("status", Identity.STATUS_VISIBLE_LIMIT)
				.getResultList();
	}

	@Override
	public List<Identity> findIdentitiesByName(Collection<String> identityNames) {
		if (identityNames == null || identityNames.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder(128);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.name in (:username)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", identityNames)
				.getResultList();
	}
	
	

	@Override
	public List<Identity> findIdentitiesByNameCaseInsensitive(Collection<String> identityNames) {
		if (identityNames == null || identityNames.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where lower(ident.name) in (:usernames)");
		
		List<String> loweredIdentityNames = identityNames.stream()
				.map(id -> id.toLowerCase()).collect(Collectors.toList());

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("usernames", loweredIdentityNames)
				.getResultList();
	}

	@Override
	public Identity findIdentityByUser(User user) {
		if (user == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where user.key=:userKey");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("userKey", user.getKey())
				.getResultList();
		
		if(identities.isEmpty()) {
			return null;
		}
		return identities.get(0);
	}

	@Override
	public List<IdentityShort> findShortIdentitiesByName(Collection<String> identityNames) {
		if (identityNames == null || identityNames.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from bidentityshort as ident where ident.name in (:names)");

		TypedQuery<IdentityShort> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class);

		int count = 0;
		int batch = 500;
		List<String> names = new ArrayList<>(identityNames);
		List<IdentityShort> shortIdentities = new ArrayList<>(names.size());
		do {
			int toIndex = Math.min(count + batch, names.size());
			List<String> toLoad = names.subList(count, toIndex);
			List<IdentityShort> allProperties = query.setParameter("names", toLoad).getResultList();
			shortIdentities.addAll(allProperties);	

			count += batch;
		} while(count < names.size());
		return shortIdentities;
	}
	
	@Override
	public List<IdentityShort> findShortIdentitiesByKey(Collection<Long> identityKeys) {
		if (identityKeys == null || identityKeys.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from bidentityshort as ident where ident.key in (:keys)");
		
		TypedQuery<IdentityShort> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class);

		int count = 0;
		int batch = 500;
		List<Long> names = new ArrayList<Long>(identityKeys);
		List<IdentityShort> shortIdentities = new ArrayList<>(names.size());
		do {
			int toIndex = Math.min(count + batch, names.size());
			List<Long> toLoad = names.subList(count, toIndex);
			List<IdentityShort> allProperties = query.setParameter("keys", toLoad).getResultList();
			shortIdentities.addAll(allProperties);	

			count += batch;
		} while(count < names.size());
		return shortIdentities;
	}
	
	public List<Identity> findIdentitiesWithoutBusinessGroup(Integer status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" inner join fetch ident.user user ")
		  .append(" where not exists (")
		  .append("   select bgroup from businessgroup bgroup, bgroupmember as me")
		  .append("   where  me.group.key=bgroup.baseGroup.key and me.identity.key=ident.key")
		  .append(" )");
		if (status != null) {
			if (status.equals(Identity.STATUS_VISIBLE_LIMIT)) {
				// search for all status smaller than visible limit 
				sb.append(" and ident.status < :status ");
			} else {
				// search for certain status
				sb.append(" and ident.status = :status ");
			}
		} else {
			sb.append(" and ident.status < ").append(Identity.STATUS_DELETED);
		}
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class);
		if (status != null) {
			query.setParameter("status", status);
		}
		return query.getResultList();
	}

	/**
	 * 
	 * @see org.olat.basesecurity.Manager#loadIdentityByKey(java.lang.Long)
	 */
	@Override
	public Identity loadIdentityByKey(Long identityKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(Identity.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.key=:key");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("key", identityKey)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return identities != null && identities.size() == 1 ? identities.get(0) : null;
	}

	@Override
	public List<Identity> loadIdentityByKeys(Collection<Long> identityKeys) {
		if (identityKeys == null || identityKeys.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(Identity.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.key in (:keys)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("keys", identityKeys)
				.getResultList();
	}

	/**
	 * @see org.olat.basesecurity.Manager#loadIdentityByKey(java.lang.Long,boolean)
	 */
	@Override
	public Identity loadIdentityByKey(Long identityKey, boolean strict) {
		if(strict) return loadIdentityByKey(identityKey);

		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(Identity.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.key=:identityKey");

		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("identityKey", identityKey)
				.getResultList();
		return identities.size() == 1 ? identities.get(0) : null;
	}
	

	@Override
	public List<IdentityShort> searchIdentityShort(String search, int maxResults) {
		return searchIdentityShort(search, null, maxResults);
	}

	@Override
	public List<IdentityShort> searchIdentityShort(String search, List<? extends OrganisationRef> searcheableOrgnisations, int maxResults) {
		String[] searchArr = search.split(" ");
		String[] attributes = new String[]{ "name", "firstName", "lastName", "email" };
		
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from bidentityshort as ident ")
		  .append(" where ident.status<").append(Identity.STATUS_VISIBLE_LIMIT).append(" and (");
		
		boolean start = true;
		for(int i=0; i<searchArr.length; i++) {
			for(String attribute:attributes) {
				if(start) {
					start = false;
				} else {
					sb.append(" or ");
				}
				
				if (searchArr[i].contains("_") && dbInstance.isOracle()) {
					//oracle needs special ESCAPE sequence to search for escaped strings
					sb.append(" lower(ident.").append(attribute).append(") like :search").append(i).append(" ESCAPE '\\'");
				} else if (dbInstance.isMySQL()) {
					sb.append(" ident.").append(attribute).append(" like :search").append(i);
				} else {
					sb.append(" lower(ident.").append(attribute).append(") like :search").append(i);
				}
			}
		}
		sb.append(")");
		if(searcheableOrgnisations != null && !searcheableOrgnisations.isEmpty()) {
			sb.append(" and exists (select orgtomember.key from bgroupmember as orgtomember ")
			  .append("  inner join organisation as org on (org.group.key=orgtomember.group.key)")
			  .append("  where orgtomember.identity.key=ident.key and org.key in (:organisationKey))");
		}
		
		TypedQuery<IdentityShort> searchQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class);
		for(int i=searchArr.length; i-->0; ) {
			searchQuery.setParameter("search" + i, PersistenceHelper.makeFuzzyQueryString(searchArr[i]));
		}
		
		if(searcheableOrgnisations != null && !searcheableOrgnisations.isEmpty()) {
			List<Long> organisationKeys = searcheableOrgnisations.stream()
					.map(OrganisationRef::getKey).collect(Collectors.toList());
			searchQuery.setParameter("organisationKey", organisationKeys);
		}

		return searchQuery
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}

	@Override
	public IdentityShort loadIdentityShortByKey(Long identityKey) {
		List<IdentityShort> idents = dbInstance.getCurrentEntityManager()
				.createNamedQuery("getIdentityShortById", IdentityShort.class)
				.setParameter("identityKey", identityKey)
				.getResultList();
		if(idents.isEmpty()) {
			return null;
		}
		return idents.get(0);
	}
	
	@Override
	public List<IdentityShort> loadIdentityShortByKeys(Collection<Long> identityKeys) {
		if (identityKeys == null || identityKeys.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from bidentityshort as ident where ident.key in (:keys)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class)
				.setParameter("keys", identityKeys)
				.getResultList();
	}
	
	@Override
	public List<Identity> loadVisibleIdentities(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" inner join fetch ident.user as user")
		  .append(" where ident.status<").append(Identity.STATUS_VISIBLE_LIMIT).append(" order by ident.key");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	@Override
	public List<Long> loadVisibleIdentityKeys() {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident.key from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" where ident.status<").append(Identity.STATUS_VISIBLE_LIMIT).append(" order by ident.key");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
	}

	@Override
	public Long countUniqueUserLoginsSince (Date lastLoginLimit){
		String queryStr ="Select count(ident) from org.olat.core.id.Identity as ident where " 
			+ "ident.lastLogin > :lastLoginLimit and ident.lastLogin != ident.creationDate";	
		List<Long> res = dbInstance.getCurrentEntityManager()
				.createQuery(queryStr, Long.class)
				.setParameter("lastLoginLimit", lastLoginLimit, TemporalType.TIMESTAMP)
				.getResultList();
		return res.get(0);
	}	

	@Override
	public List<Authentication> getAuthentications(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth ")
		  .append("inner join fetch auth.identity as ident")
		  .append(" where ident.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	@Override
	public Authentication createAndPersistAuthentication(final Identity ident, final String provider, final String authUserName,
			final String credentials, final Encoder.Algorithm algorithm) {
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstanceWithoutCheck(provider, ident.getKey());
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resourceable, () -> {
			Authentication auth = findAuthentication(ident, provider);
			if(auth == null) {
				auth = createAndPersistAuthenticationIntern(ident, provider,  authUserName, credentials, algorithm);
			}
			return auth;
		});
	}
	
	/**
	 * This method is not protected by a doInSync and will not check if the authentication already exists.
	 * @param ident
	 * @param provider
	 * @param authUserName
	 * @param credentials
	 * @param algorithm
	 * @return
	 */
	private Authentication createAndPersistAuthenticationIntern(final Identity ident, final String provider, final String authUserName,
			final String credentials, final Encoder.Algorithm algorithm) {
		AuthenticationImpl auth;
		if(algorithm != null && credentials != null) {
			String salt = algorithm.isSalted() ? Encoder.getSalt() : null;
			String hash = Encoder.encrypt(credentials, salt, algorithm);
			auth = new AuthenticationImpl(ident, provider, authUserName, hash, salt, algorithm.name());
		} else {
			auth = new AuthenticationImpl(ident, provider, authUserName, credentials);
		}
		auth.setCreationDate(new Date());
		auth.setLastModified(auth.getCreationDate());
		dbInstance.getCurrentEntityManager().persist(auth);
		updateAuthenticationHistory(auth, ident);
		dbInstance.commit();
		log.audit("Create " + provider + " authentication (login=" + ident.getKey() + ",authusername=" + authUserName + ")");
		return auth;
	}
	
	/**
	 * Archive the password and clean the history. The method
	 * will let at least one entry per authentication.
	 * 
	 * @param auth The new authentication to archive
	 * @param ident The identity
	 */
	private void updateAuthenticationHistory(Authentication auth, Identity ident) {
		if(BaseSecurityModule.getDefaultAuthProviderIdentifier().equals(auth.getProvider())) {
			authenticationHistoryDao.createHistory(auth, ident);
			int historyLength = loginModule.getPasswordHistory() < 1 ? 1 : loginModule.getPasswordHistory();
			List<AuthenticationHistory> historyToDelete = authenticationHistoryDao
					.loadHistory(ident, auth.getProvider(), historyLength, 5000);
			for(AuthenticationHistory historyPoint:historyToDelete) {
				authenticationHistoryDao.deleteAuthenticationHistory(historyPoint);
			}
		}
	}

	/**
	 * @see org.olat.basesecurity.Manager#findAuthentication(org.olat.core.id.Identity, java.lang.String)
	 */
	@Override
	public Authentication findAuthentication(IdentityRef identity, String provider) {
		if (identity==null) {
			throw new IllegalArgumentException("identity must not be null");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName())
		  .append(" as auth where auth.identity.key=:identityKey and auth.provider=:provider");
		
		List<Authentication> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("provider", provider)
				.getResultList();
		if (results == null || results.isEmpty()) return null;
		if (results.size() > 1) {
			throw new AssertException("Found more than one Authentication for a given subject and a given provider.");
		}
		return results.get(0);
	}
	
	@Override
	public List<Authentication> findAuthentications(IdentityRef identity, List<String> providers) {
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName())
		  .append(" as auth where auth.identity.key=:identityKey and auth.provider in (:providers)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("providers", providers)
				.getResultList();
	}

	@Override
	public String findAuthenticationName(IdentityRef identity, String provider) {
		if (identity==null) {
			throw new IllegalArgumentException("identity must not be null");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select auth.authusername from ").append(AuthenticationImpl.class.getName())
		  .append(" as auth where auth.identity.key=:identityKey and auth.provider=:provider");
		
		List<String> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("provider", provider)
				.getResultList();
		if (results == null || results.isEmpty()) return null;
		if (results.size() > 1) {
			throw new AssertException("Found more than one Authentication for a given subject and a given provider.");
		}
		return results.get(0);
	}
	
	/**
	 * @see org.olat.basesecurity.Manager#findAuthentication(org.olat.core.id.Identity, java.lang.String)
	 */
	@Override
	public List<Authentication> findAuthenticationByToken(String provider, String securityToken) {
		if (provider==null || securityToken==null) {
			throw new IllegalArgumentException("provider and token must not be null");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName())
		  .append(" as auth where auth.credential=:credential and auth.provider=:provider");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("credential", securityToken)
				.setParameter("provider", provider)
				.getResultList();
	}
	
	@Override
	public List<Authentication> findOldAuthentication(String provider, Date creationDate) {
		if (provider == null || creationDate == null) {
			throw new IllegalArgumentException("provider and token must not be null");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName())
		  .append(" as auth where auth.provider=:provider and auth.creationDate<:creationDate");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("creationDate", creationDate, TemporalType.TIMESTAMP)
				.setParameter("provider", provider)
				.getResultList();
	}

	@Override
	public Authentication updateAuthentication(Authentication authentication) {
		((AuthenticationImpl)authentication).setLastModified(new Date());
		authentication = dbInstance.getCurrentEntityManager().merge(authentication);
		updateAuthenticationHistory(authentication, authentication.getIdentity());
		return authentication;
	}

	@Override
	public boolean checkCredentialHistory(Identity identity, String provider, String password) {
		boolean ok = true;
		int historyLength = loginModule.getPasswordHistory();
		if(historyLength > 0) {
			List<AuthenticationHistory> credentialHistory = authenticationHistoryDao
					.loadHistory(identity, provider, 0, historyLength);
			for(AuthenticationHistory oldCredential:credentialHistory) {
				Algorithm algorithm = Algorithm.find(oldCredential.getAlgorithm());
				String hash = Encoder.encrypt(password, oldCredential.getSalt(), algorithm);
				if(oldCredential.getCredential().equals(hash)) {
					ok = false;
					break;
				}
			}
		}
		return ok;
	}

	@Override
	public boolean checkCredentials(Authentication authentication, String password) {
		Algorithm algorithm = Algorithm.find(authentication.getAlgorithm());
		String hash = Encoder.encrypt(password, authentication.getSalt(), algorithm);
		return authentication.getCredential() != null && authentication.getCredential().equals(hash);
	}

	@Override
	public Authentication updateCredentials(Authentication authentication, String password, Algorithm algorithm) {
		if(authentication.getAlgorithm() != null && authentication.getAlgorithm().equals(algorithm.name())) {
			//check if update is needed
			String currentSalt = authentication.getSalt();
			String newCredentials = Encoder.encrypt(password, currentSalt, algorithm);
			if(newCredentials.equals(authentication.getCredential())) {
				//same credentials
				return authentication;
			}
		}

		String salt = algorithm.isSalted() ? Encoder.getSalt() : null;
		String newCredentials = Encoder.encrypt(password, salt, algorithm);
		authentication.setSalt(salt);
		authentication.setCredential(newCredentials);
		authentication.setAlgorithm(algorithm.name());
		return updateAuthentication(authentication);
	}

	/**
	 * @see org.olat.basesecurity.Manager#deleteAuthentication(org.olat.basesecurity.Authentication)
	 */
	@Override
	public void deleteAuthentication(Authentication auth) {
		if(auth == null || auth.getKey() == null) return;//nothing to do
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
			  .append(" where auth.key=:authKey");

			AuthenticationImpl authRef = dbInstance.getCurrentEntityManager().find(AuthenticationImpl.class,  auth.getKey());
			if(authRef != null) {
				dbInstance.getCurrentEntityManager().remove(authRef);
			}
		} catch (EntityNotFoundException e) {
			log.error("", e);
		}
	}
	
	@Override
	public void deleteInvalidAuthenticationsByEmail(String email) {
		if (!StringHelper.containsNonWhitespace(email)) return;
		
		// If a user with this email exists the email is valid.
		Identity identity = UserManager.getInstance().findUniqueIdentityByEmail(email);
		if (identity != null) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(AuthenticationImpl.class.getName()).append(" as auth");
		sb.append(" where auth.authusername=:authusername");
		sb.append("   and auth.provider in (:providers)");
		
		List<String> providers = Arrays.asList(
				WebDAVAuthManager.PROVIDER_HA1_EMAIL,
				WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL,
				WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL,
				WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL);
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("authusername", email)
				.setParameter("providers", providers)
				.executeUpdate();
	}

	/**
	 * @see org.olat.basesecurity.Manager#findAuthenticationByAuthusername(java.lang.String, java.lang.String)
	 */
	@Override
	public Authentication findAuthenticationByAuthusername(String authusername, String provider) {
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity ident")
		  .append(" where auth.provider=:provider and auth.authusername=:authusername");

		List<Authentication> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("provider", provider)
				.setParameter("authusername", authusername)
				.getResultList();
		if (results.isEmpty()) return null;
		if (results.size() != 1) {
			throw new AssertException("more than one entry for the a given authusername and provider, should never happen (even db has a unique constraint on those columns combined) ");
		}
		return results.get(0);
	}
	
	@Override
	public List<Authentication> findAuthenticationByAuthusername(String authusername, List<String> providers) {
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity ident")
		  .append(" where auth.provider in (:providers) and auth.authusername=:authusername");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("providers", providers)
				.setParameter("authusername", authusername)
				.getResultList();
	}

	@Override
	public List<Identity> getVisibleIdentitiesByPowerSearch(String login,
			Map<String, String> userproperties, boolean userPropertiesAsIntersectionSearch,
			OrganisationRoles[] roles, String[] authProviders, Date createdAfter, Date createdBefore) {
		return identityPowerSearchQueries.getIdentitiesByPowerSearch(new SearchIdentityParams(login,
				userproperties, userPropertiesAsIntersectionSearch, roles, 
				authProviders, createdAfter, createdBefore, null, null, Identity.STATUS_VISIBLE_LIMIT),
				0, -1); 
	}
	
  @Override
	public List<Identity> getVisibleIdentitiesByPowerSearch(String login, Map<String, String> userProperties,
			boolean userPropertiesAsIntersectionSearch, OrganisationRoles[] roles,
			String[] authProviders, Date createdAfter, Date createdBefore, int firstResult, int maxResults) {
		return identityPowerSearchQueries.getIdentitiesByPowerSearch(new SearchIdentityParams(login,
				userProperties, userPropertiesAsIntersectionSearch, roles, 
				authProviders, createdAfter, createdBefore, null, null, Identity.STATUS_VISIBLE_LIMIT),
				firstResult, maxResults); 
	}

  @Override
	public long countIdentitiesByPowerSearch(String login, Map<String, String> userproperties, boolean userPropertiesAsIntersectionSearch,
			OrganisationRoles[] roles, String[] authProviders, Date createdAfter, Date createdBefore, Date userLoginAfter, Date userLoginBefore,  Integer status) {
	  	return identityPowerSearchQueries.countIdentitiesByPowerSearch(new SearchIdentityParams(login,
	  			userproperties, userPropertiesAsIntersectionSearch, roles, authProviders,
	  			createdAfter, createdBefore, userLoginAfter, userLoginBefore, status));
	}

	@Override
	public List<Identity> getIdentitiesByPowerSearch(String login, Map<String, String> userproperties, boolean userPropertiesAsIntersectionSearch,
			OrganisationRoles[] roles,
			String[] authProviders, Date createdAfter, Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status) {
		return identityPowerSearchQueries.getIdentitiesByPowerSearch(new SearchIdentityParams(login, userproperties, userPropertiesAsIntersectionSearch,
				roles, authProviders, createdAfter, createdBefore,
				userLoginAfter, userLoginBefore, status), 0, -1);
	}
  
	@Override
	public int countIdentitiesByPowerSearch(SearchIdentityParams params) {
		return identityPowerSearchQueries.countIdentitiesByPowerSearch(params);
	}
	
	@Override
	public List<Identity> getIdentitiesByPowerSearch(SearchIdentityParams params, int firstResult, int maxResults) {
		return identityPowerSearchQueries.getIdentitiesByPowerSearch(params, firstResult, maxResults);
	}


	@Override
	public boolean isIdentityVisible(Identity identity) {
		if(identity == null) return false;
		Integer status = identity.getStatus();
		return (status != null && status.intValue() < Identity.STATUS_VISIBLE_LIMIT);
	}

	/**
	 * @see org.olat.basesecurity.Manager#saveIdentityStatus(org.olat.core.id.Identity)
	 */
	@Override
	public Identity saveIdentityStatus(Identity identity, Integer status, Identity doer) {
		IdentityImpl reloadedIdentity = loadForUpdate(identity);
		if(reloadedIdentity != null) {
			reloadedIdentity.setStatus(status);
			if(status.equals(Identity.STATUS_DELETED)) {
				if(doer != null && reloadedIdentity.getDeletedBy() == null) {
					reloadedIdentity.setDeletedBy(getDeletedByName(doer));
				}
				reloadedIdentity.setDeletedDate(new Date());
			}
			reloadedIdentity = dbInstance.getCurrentEntityManager().merge(reloadedIdentity);
		}
		dbInstance.commit();
		return reloadedIdentity;
	}
	
	@Override
	public Identity saveDeletedByData(Identity identity, Identity doer) {
		IdentityImpl reloadedIdentity = loadForUpdate(identity);
		if(reloadedIdentity != null) {
			reloadedIdentity.setDeletedBy(getDeletedByName(doer));
			reloadedIdentity.setDeletedDate(new Date());
			
			List<String> deletedRoles = getRolesSummaryWithResources(reloadedIdentity);
			StringBuilder deletedRoleBuffer = new StringBuilder();
			for(String deletedRole:deletedRoles) {
				if(deletedRoleBuffer.length() > 0) deletedRoleBuffer.append(",");
				deletedRoleBuffer.append(deletedRole);
			}

			reloadedIdentity.setDeletedRoles(deletedRoleBuffer.toString());
			reloadedIdentity = dbInstance.getCurrentEntityManager().merge(reloadedIdentity);
			dbInstance.commit();
		}
		return reloadedIdentity;
	}
	
	private String getDeletedByName(Identity doer) {
		StringBuilder sb = new StringBuilder(128);
		if(doer != null) {
			if(StringHelper.containsNonWhitespace(doer.getUser().getLastName())) {
				sb.append(doer.getUser().getLastName());
			}
			if(StringHelper.containsNonWhitespace(doer.getUser().getFirstName())) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(doer.getUser().getFirstName());
			}
		}
		if(sb.length() > 128) {
			sb.delete(128, sb.length());
		}
		return sb.toString();
	}

	@Override
	public void setIdentityLastLogin(IdentityRef identity) {
		dbInstance.getCurrentEntityManager()
				.createNamedQuery("updateIdentityLastLogin")
				.setParameter("identityKey", identity.getKey())
				.setParameter("now", new Date())
				.executeUpdate();
		dbInstance.commit();
	}
	
	@Override
	public Identity saveIdentityName(Identity identity, String newName, String newExternalId) {
		IdentityImpl reloadedIdentity = loadForUpdate(identity); 
		if(reloadedIdentity != null) {
			reloadedIdentity.setName(newName);
			reloadedIdentity.setExternalId(newExternalId);
			reloadedIdentity = dbInstance.getCurrentEntityManager().merge(reloadedIdentity);
		}
		dbInstance.commit();
		return reloadedIdentity;
	}
	
	@Override
	public Identity setExternalId(Identity identity, String externalId) {
		IdentityImpl reloadedIdentity = loadForUpdate(identity); 
		if(reloadedIdentity != null) {
			reloadedIdentity.setExternalId(externalId);
			reloadedIdentity = dbInstance.getCurrentEntityManager().merge(reloadedIdentity);
		}
		dbInstance.commit();
		return reloadedIdentity;
	}
	
	
	/**
	 * Don't forget to commit/roolback the transaction as soon as possible
	 * @param identityKey
	 * @return
	 */
	private IdentityImpl loadForUpdate(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select id from ").append(IdentityImpl.class.getName()).append(" as id")
		  .append(" inner join fetch id.user user ")
		  .append(" where id.key=:identityKey");
		
		dbInstance.getCurrentEntityManager().detach(identity);
		List<IdentityImpl> identities = dbInstance.getCurrentEntityManager()
	  		.createQuery(sb.toString(), IdentityImpl.class)
	  		.setParameter("identityKey", identity.getKey())
	  		.setLockMode(LockModeType.PESSIMISTIC_WRITE)
	  		.getResultList();
		if(identities.isEmpty()) {
			return null;
		}
		return identities.get(0);
	}

	@Override
	public Identity getAndUpdateAnonymousUserForLanguage(Locale locale) {
		Translator trans = Util.createPackageTranslator(UserManager.class, locale);
		String guestUsername = GUEST_USERNAME_PREFIX + locale.toString();		
		Identity guestIdentity = findIdentityByName(guestUsername);
		if (guestIdentity == null) {
			// Create it lazy on demand
			User guestUser = UserManager.getInstance().createUser(trans.translate("user.guest"), null, null);
			guestUser.getPreferences().setLanguage(locale.toString());
			guestIdentity = createAndPersistIdentityAndUser(guestUsername, null, guestUser, null, null, null);
			organisationService.addMember(guestIdentity, OrganisationRoles.guest);
		} else if (!guestIdentity.getUser().getProperty(UserConstants.FIRSTNAME, locale).equals(trans.translate("user.guest"))) {
			//Check if guest name has been updated in the i18n tool
			guestIdentity.getUser().setProperty(UserConstants.FIRSTNAME, trans.translate("user.guest"));
			guestIdentity = dbInstance.getCurrentEntityManager().merge(guestIdentity);
		}
		return guestIdentity;
	}	
	
	@Override
	public int deleteUserDataPriority() {
		// delete with low priority at the end of the deletion process
		return 10;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		// 1) delete all authentication tokens
		List<Authentication> authentications = getAuthentications(identity);
		for (Authentication auth:authentications) {
			deleteAuthentication(auth);
			log.info("Delete authentication provider::" + auth.getProvider() + "  of identity="  + identity);
		}
		
		// 2) Delete the authentication history
		authenticationHistoryDao.deleteAuthenticationHistory(identity);		
	}
}