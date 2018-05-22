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

import java.io.File;
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

import org.olat.admin.quota.GenericQuotaEditController;
import org.olat.admin.sysinfo.SysinfoController;
import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.UserChangePasswordController;
import org.olat.admin.user.UserCreateController;
import org.olat.basesecurity.events.NewIdentityCreatedEvent;
import org.olat.basesecurity.manager.AuthenticationHistoryDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
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
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.resource.OresHelper;
import org.olat.login.LoginModule;
import org.olat.portfolio.manager.InvitationDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.ChangePasswordController;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserImpl;
import org.olat.user.UserManager;

/**
 * <h3>Description:</h3>
 * The PersistingManager implements the security manager and provide methods to
 * manage identities and user objects based on a database persistence mechanism
 * using hibernate.
 * <p>
 * 
 * @author Felix Jost, Florian Gnaegi
 */
public class BaseSecurityManager implements BaseSecurity, UserDataDeletable {
	
	private static final OLog log = Tracing.createLoggerFor(BaseSecurityManager.class);
	
	private DB dbInstance;
	private LoginModule loginModule;
	private OLATResourceManager orm;
	private InvitationDAO invitationDao;
	private AuthenticationHistoryDAO authenticationHistoryDao;
	private String dbVendor = "";
	private static BaseSecurityManager INSTANCE;
	private static String GUEST_USERNAME_PREFIX = "guest_";
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
	
	public void setLoginModule(LoginModule loginModule) {
		this.loginModule = loginModule;
	}
	
	/**
	 * [used by spring]
	 * @param orm
	 */
	public void setResourceManager(OLATResourceManager orm) {
		this.orm = orm;
	}
	
	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	/**
	 * [used by Spring]
	 * @param invitationDao
	 */
	public void setInvitationDao(InvitationDAO invitationDao) {
		this.invitationDao = invitationDao;
	}
	
	/**
	 * [used by Spring]
	 * @param authenticationHistoryDao
	 */
	public void setAuthenticationHistoryDao(AuthenticationHistoryDAO authenticationHistoryDao) {
		this.authenticationHistoryDao = authenticationHistoryDao;
	}

	/**
	 * @see org.olat.basesecurity.Manager#init()
	 */
	public void init() { // called only once at startup and only from one thread
		// init the system level groups and its policies
		initSysGroupAdmin();
		dbInstance.commit();
		initSysGroupAuthors();
		dbInstance.commit();
		initSysGroupGroupmanagers();
		dbInstance.commit();
		initSysGroupPoolsmanagers();
		dbInstance.commit();
		initSysGroupUsermanagers();
		dbInstance.commit();
		initSysGroupUsers();
		dbInstance.commit();
		initSysGroupAnonymous();
		dbInstance.commit();
		initSysGroupInstitutionalResourceManager();
		dbInstance.commitAndCloseSession();
	}

	/**
	 * OLAT system administrators, root, good, whatever you name it...
	 */
	private void initSysGroupAdmin() {
		SecurityGroup adminGroup = findSecurityGroupByName(Constants.GROUP_ADMIN);
		if (adminGroup == null) 
			adminGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_ADMIN);

		// we check everthing by policies, so we must give admins the hasRole
		// permission on the type resource "Admin"
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN);

		//admins have role "authors" by default
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);

		//admins have a groupmanager policy and access permissions to groupmanaging tools
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GROUPMANAGER);

		//admins have a usemanager policy and access permissions to usermanagement tools
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERMANAGER);

		//admins are also regular users
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERS);

		//olat admins have access to all security groups
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, Constants.ORESOURCE_SECURITYGROUPS);

		// and to all courses
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ADMIN, Constants.ORESOURCE_COURSES);

		// and to pool admiistration
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ADMIN, Constants.ORESOURCE_POOLS);

		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(SysinfoController.class));
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(UserAdminController.class));
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(UserChangePasswordController.class));
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(UserCreateController.class));
		createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(GenericQuotaEditController.class));
	}

	/**
	 * Every active user that is an active user is in the user group. exceptions: logonDenied and anonymous users
	 */
	private void initSysGroupUsers() {
		SecurityGroup olatuserGroup = findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		if (olatuserGroup == null) 
			olatuserGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_OLATUSERS);

		//users have a user policy
		createAndPersistPolicyIfNotExists(olatuserGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERS);

		createAndPersistPolicyIfNotExists(olatuserGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(ChangePasswordController.class));
	}

	/**
	 * Users with access to group context management (groupmanagement that can be used in multiple courses
	 */
	private void initSysGroupGroupmanagers() {
		SecurityGroup olatGroupmanagerGroup = findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
		if (olatGroupmanagerGroup == null) 
			olatGroupmanagerGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_GROUPMANAGERS);
		//gropumanagers have a groupmanager policy and access permissions to groupmanaging tools
		createAndPersistPolicyIfNotExists(olatGroupmanagerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GROUPMANAGER);
	}
	
	/**
	 * Users with access to group context management (groupmanagement that can be used in multiple courses
	 */
	private void initSysGroupPoolsmanagers() {
		SecurityGroup secGroup = findSecurityGroupByName(Constants.GROUP_POOL_MANAGER);
		if (secGroup == null) 
			secGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_POOL_MANAGER);
		//pools managers have a goupmanager policy and access permissions to groupmanaging tools
		createAndPersistPolicyIfNotExists(secGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_POOLS);
	}

	/**
	 * Users with access to user management
	 */
	private void initSysGroupUsermanagers() {
		SecurityGroup olatUsermanagerGroup = findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
		if (olatUsermanagerGroup == null) 
			olatUsermanagerGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_USERMANAGERS);
		//gropumanagers have a groupmanager policy and access permissions to groupmanaging tools
		createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERMANAGER);
		createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(UserAdminController.class));
		createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(UserChangePasswordController.class));
		createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(UserCreateController.class));
		createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(GenericQuotaEditController.class));
	}

	/**
	 * Users with access to the authoring parts of the learning ressources repository
	 */
	private void initSysGroupAuthors() {
		SecurityGroup olatauthorGroup = findSecurityGroupByName(Constants.GROUP_AUTHORS);
		if (olatauthorGroup == null) 
			olatauthorGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_AUTHORS);
		//authors have a author policy and access permissions to authoring tools
		createAndPersistPolicyIfNotExists(olatauthorGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
	}
	
	/**
	 * Users with access to the authoring parts of the learning ressources repository (all resources in his university)
	 */
	private void initSysGroupInstitutionalResourceManager() {
		SecurityGroup institutionalResourceManagerGroup = findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
		if (institutionalResourceManagerGroup == null) 
			institutionalResourceManagerGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_INST_ORES_MANAGER);
		//manager have a author policy and access permissions to authoring tools
		createAndPersistPolicyIfNotExists(institutionalResourceManagerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_INSTORESMANAGER);
		createAndPersistPolicyIfNotExists(institutionalResourceManagerGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(GenericQuotaEditController.class));
	}

	/**
	 * Unknown users with guest only rights
	 */
	private void initSysGroupAnonymous() {
		SecurityGroup guestGroup = findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
		if (guestGroup == null) 
			guestGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_ANONYMOUS);
		//guest(=anonymous) have a guest policy
		createAndPersistPolicyIfNotExists(guestGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY);
	}
	
	/**
	 * @see org.olat.basesecurity.Manager#getPoliciesOfSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 */
	@Override
	public List<Policy> getPoliciesOfSecurityGroup(SecurityGroup secGroup) {
		if(secGroup == null ) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select poi from ").append(PolicyImpl.class.getName()).append(" as poi where poi.securityGroup.key=:secGroupKey");

		List<Policy> policies = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Policy.class)
				.setParameter("secGroupKey", secGroup.getKey())
				.getResultList();
		return policies;
	}

	/**
	 * @see org.olat.basesecurity.BaseSecurity#getPoliciesOfResource(org.olat.core.id.OLATResourceable)
	 */
	@Override
	public List<Policy> getPoliciesOfResource(OLATResource resource, SecurityGroup secGroup) {
		StringBuilder sb = new StringBuilder();
		sb.append("select poi from ").append(PolicyImpl.class.getName()).append(" poi where ")
			.append(" poi.olatResource.key=:resourceKey ");
		if(secGroup != null) {
			sb.append(" and poi.securityGroup.key=:secGroupKey");
		}
		
		TypedQuery<Policy> query = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Policy.class)
				.setParameter("resourceKey", resource.getKey());
		if(secGroup != null) {
			query.setParameter("secGroupKey", secGroup.getKey());
		}
		return query.getResultList();
	}

	@Override
	public boolean isIdentityPermittedOnResourceable(IdentityRef identity, String permission, OLATResourceable olatResourceable) {
		return isIdentityPermittedOnResourceable(identity, permission, olatResourceable, true);
	}

	/**
	 * @see org.olat.basesecurity.Manager#isIdentityPermittedOnResourceable(org.olat.core.id.Identity, java.lang.String, org.olat.core.id.OLATResourceable boolean)
	 */
	@Override
	public boolean isIdentityPermittedOnResourceable(IdentityRef identity, String permission, OLATResourceable olatResourceable, boolean checkTypeRight) {
		if(identity == null || identity.getKey() == null) return false;//no identity, no permission

		Long oresid = olatResourceable.getResourceableId();
		if (oresid == null) oresid = new Long(0); //TODO: make a method in
		// OLATResorceManager, since this
		// is implementation detail
		String oresName = olatResourceable.getResourceableTypeName();
		// if the olatResourceable is not persisted as OLATResource, then the answer
		// is false,
		// therefore we can use the query assuming there is an OLATResource

		TypedQuery<Number> query;
		if(checkTypeRight) {
			query = DBFactory.getInstance().getCurrentEntityManager().createNamedQuery("isIdentityPermittedOnResourceableCheckType", Number.class);
		} else {
			query = DBFactory.getInstance().getCurrentEntityManager().createNamedQuery("isIdentityPermittedOnResourceable", Number.class);
		}
		
		Number count = query.setParameter("identitykey", identity.getKey())
				.setParameter("permission", permission)
				.setParameter("resid", oresid)
				.setParameter("resname", oresName)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getSingleResult();
		return count.longValue() > 0;
	}

	/**
	 * @see org.olat.basesecurity.Manager#getRoles(org.olat.core.id.Identity)
	 */
	@Override
	public Roles getRoles(IdentityRef identity) {
		boolean isGuestOnly = false;
		boolean isInvitee = false;

		List<String> rolesStr = getRolesAsString(identity);
		boolean admin = rolesStr.contains(Constants.GROUP_ADMIN);
		boolean author = admin || rolesStr.contains(Constants.GROUP_AUTHORS);
		boolean groupManager = admin || rolesStr.contains(Constants.GROUP_GROUPMANAGERS);
		boolean userManager = admin || rolesStr.contains(Constants.GROUP_USERMANAGERS);
		boolean resourceManager = rolesStr.contains(Constants.GROUP_INST_ORES_MANAGER);
		boolean poolManager = admin || rolesStr.contains(Constants.GROUP_POOL_MANAGER);
		
		if(!rolesStr.contains(Constants.GROUP_OLATUSERS)) {
			isInvitee = invitationDao.isInvitee(identity);
			isGuestOnly = isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY);
		}
		
		return new Roles(admin, userManager, groupManager, author, isGuestOnly, resourceManager, poolManager, isInvitee);
	}

	@Override
	public List<String> getRolesAsString(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ngroup.groupName from ").append(NamedGroupImpl.class.getName()).append(" as ngroup ")
		  .append(" where exists (")
		  .append("   select sgmsi from ").append(SecurityGroupMembershipImpl.class.getName())
		  .append("      as sgmsi where sgmsi.identity.key=:identityKey and sgmsi.securityGroup=ngroup.securityGroup")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	@Override
	public void updateRoles(Identity actingIdentity, Identity updatedIdentity, Roles roles) {
		SecurityGroup anonymousGroup = findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
		boolean hasBeenAnonymous = isIdentityInSecurityGroup(updatedIdentity, anonymousGroup);
		updateRolesInSecurityGroup(actingIdentity, updatedIdentity, anonymousGroup, hasBeenAnonymous, roles.isGuestOnly(), Constants.GROUP_ANONYMOUS);
		
		// system users - opposite of anonymous users
		SecurityGroup usersGroup = findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		boolean hasBeenUser = isIdentityInSecurityGroup(updatedIdentity, usersGroup);
		updateRolesInSecurityGroup(actingIdentity, updatedIdentity,  usersGroup, hasBeenUser, !roles.isGuestOnly(), Constants.GROUP_OLATUSERS);

		SecurityGroup groupManagerGroup = findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
		boolean hasBeenGroupManager = isIdentityInSecurityGroup(updatedIdentity, groupManagerGroup);
		boolean groupManager = roles.isGroupManager()
				&& !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(actingIdentity, updatedIdentity, groupManagerGroup, hasBeenGroupManager, groupManager, Constants.GROUP_GROUPMANAGERS);

		// author
		SecurityGroup authorGroup = findSecurityGroupByName(Constants.GROUP_AUTHORS);
		boolean hasBeenAuthor = isIdentityInSecurityGroup(updatedIdentity, authorGroup);
		boolean isAuthor = (roles.isAuthor() || roles.isInstitutionalResourceManager())
				&& !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(actingIdentity, updatedIdentity, authorGroup, hasBeenAuthor, isAuthor, Constants.GROUP_AUTHORS);

		// user manager, only allowed by admin
		SecurityGroup userManagerGroup = findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
		boolean hasBeenUserManager = isIdentityInSecurityGroup(updatedIdentity, userManagerGroup);
		boolean userManager = roles.isUserManager()
				&& !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(actingIdentity, updatedIdentity,  userManagerGroup, hasBeenUserManager, userManager, Constants.GROUP_USERMANAGERS);

 		// institutional resource manager
		SecurityGroup institutionalResourceManagerGroup = findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
		boolean hasBeenInstitutionalResourceManager = isIdentityInSecurityGroup(updatedIdentity, institutionalResourceManagerGroup);
		boolean institutionalResourceManager = roles.isInstitutionalResourceManager()
				&& !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(actingIdentity, updatedIdentity, institutionalResourceManagerGroup, hasBeenInstitutionalResourceManager, institutionalResourceManager, Constants.GROUP_INST_ORES_MANAGER);

		// institutional resource manager
		SecurityGroup poolManagerGroup = findSecurityGroupByName(Constants.GROUP_POOL_MANAGER);
		boolean hasBeenPoolManager = isIdentityInSecurityGroup(updatedIdentity, poolManagerGroup);
		boolean poolManager = roles.isPoolAdmin()	&& !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(actingIdentity, updatedIdentity, poolManagerGroup, hasBeenPoolManager, poolManager, Constants.GROUP_POOL_MANAGER);

		// system administrator
		SecurityGroup adminGroup = findSecurityGroupByName(Constants.GROUP_ADMIN);
		boolean hasBeenAdmin = isIdentityInSecurityGroup(updatedIdentity, adminGroup);
		boolean isOLATAdmin = roles.isOLATAdmin() && !roles.isGuestOnly() && !roles.isInvitee();
		updateRolesInSecurityGroup(actingIdentity, updatedIdentity, adminGroup, hasBeenAdmin, isOLATAdmin, Constants.GROUP_ADMIN);		
	}
	
	private void updateRolesInSecurityGroup(Identity actingIdentity, Identity updatedIdentity, SecurityGroup securityGroup, boolean hasBeenInGroup, boolean isNowInGroup, String groupName) {
		if (!hasBeenInGroup && isNowInGroup) {
			// user not yet in security group, add him
			addIdentityToSecurityGroup(updatedIdentity, securityGroup);
			log.audit("User::" + (actingIdentity == null ? "unkown" : actingIdentity.getKey()) + " added system role::" + groupName + " to user::" + updatedIdentity.getKey(), null);
		} else if (hasBeenInGroup && !isNowInGroup) {
			// user not anymore in security group, remove him
			removeIdentityFromSecurityGroup(updatedIdentity, securityGroup);
			log.audit("User::" + (actingIdentity == null ? "unkown" : actingIdentity.getKey()) + " removed system role::" + groupName + " from user::" + updatedIdentity.getKey(), null);
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

	/**
	 * scalar query : select sgi, poi, ori
	 * @param identity
	 * @return List of policies
	 */
	@Override
	public List<Policy> getPoliciesOfIdentity(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select poi from ").append(PolicyImpl.class.getName()).append(" as poi ")
		  .append("inner join fetch poi.securityGroup as secGroup ")
		  .append("inner join fetch poi.olatResource as resource ")
		  .append("where secGroup in (select sgmi.securityGroup from ")
		  .append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi where sgmi.identity.key=:identityKey)");
		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Policy.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	/**
	 * @see org.olat.basesecurity.Manager#isIdentityInSecurityGroup(org.olat.core.id.Identity, org.olat.basesecurity.SecurityGroup)
	 */
	@Override
	public boolean isIdentityInSecurityGroup(IdentityRef identity, SecurityGroup secGroup) {
		if (secGroup == null || identity == null) return false;
		String queryString = "select sgmsi.key from org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi where sgmsi.identity.key=:identitykey and sgmsi.securityGroup.key=:securityGroupKey";

		List<Long> membership = dbInstance.getCurrentEntityManager()
			.createQuery(queryString, Long.class)
			.setParameter("identitykey", identity.getKey())
			.setParameter("securityGroupKey", secGroup.getKey())
			.setHint("org.hibernate.cacheable", Boolean.TRUE)
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return membership != null && !membership.isEmpty() && membership.get(0) != null;
	}

	@Override
	public void touchMembership(Identity identity, List<SecurityGroup> secGroups) {
		if (secGroups == null || secGroups.isEmpty()) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select sgmsi from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
		  .append("where sgmsi.identity.key=:identityKey and sgmsi.securityGroup in (:securityGroups)");
		
		List<ModifiedInfo> infos = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), ModifiedInfo.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("securityGroups", secGroups)
				.getResultList();
		
		for(ModifiedInfo info:infos) {
			info.setLastModified(new Date());
			DBFactory.getInstance().getCurrentEntityManager().merge(info);
		}
	}

	/**
	 * @see org.olat.basesecurity.Manager#createAndPersistSecurityGroup()
	 */
	public SecurityGroup createAndPersistSecurityGroup() {
		SecurityGroupImpl sgi = new SecurityGroupImpl();
		DBFactory.getInstance().saveObject(sgi);
		return sgi;
	}

	/**
	 * @see org.olat.basesecurity.Manager#deleteSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 */
	@Override
	public void deleteSecurityGroup(SecurityGroup secGroup) {
		// we do not use hibernate cascade="delete", but implement our own (to be
		// sure to understand our code)
		StringBuilder sb = new StringBuilder();
		sb.append("select secGroup from ").append(SecurityGroupImpl.class.getName()).append(" as secGroup ")
		  .append("where secGroup.key=:securityGroupKey");
		List<SecurityGroup> reloadedSecGroups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), SecurityGroup.class)
				.setParameter("securityGroupKey", secGroup.getKey())
				.getResultList();
		if(reloadedSecGroups.size() == 1) {
			secGroup = reloadedSecGroups.get(0);
			
			// 1) delete associated users (need to do it manually, hibernate knows
			// nothing about
			// the membership, modeled manually via many-to-one and not via set)
			dbInstance.getCurrentEntityManager()
				.createQuery("delete from org.olat.basesecurity.SecurityGroupMembershipImpl where securityGroup=:securityGroup")
				.setParameter("securityGroup", secGroup)
				.executeUpdate();
			// 2) delete all policies
	
			dbInstance.getCurrentEntityManager()
				.createQuery("delete from org.olat.basesecurity.PolicyImpl where securityGroup=:securityGroup")
				.setParameter("securityGroup", secGroup)
				.executeUpdate();
			// 3) delete security group
			dbInstance.getCurrentEntityManager()
				.remove(secGroup);
		}
	}

	/**
	 * 
	 * 
	 * @see org.olat.basesecurity.Manager#addIdentityToSecurityGroup(org.olat.core.id.Identity, org.olat.basesecurity.SecurityGroup)
	 */
	@Override
	public void addIdentityToSecurityGroup(Identity identity, SecurityGroup secGroup) {
		SecurityGroupMembershipImpl sgmsi = new SecurityGroupMembershipImpl();
		sgmsi.setIdentity(identity);
		sgmsi.setSecurityGroup(secGroup);
		sgmsi.setLastModified(new Date());
		dbInstance.getCurrentEntityManager().persist(sgmsi);
	}

	/**
	 * @see org.olat.basesecurity.Manager#removeIdentityFromSecurityGroup(org.olat.core.id.Identity, org.olat.basesecurity.SecurityGroup)
	 */
	@Override
	public boolean removeIdentityFromSecurityGroup(Identity identity, SecurityGroup secGroup) {
		return removeIdentityFromSecurityGroups(Collections.singletonList(identity), Collections.singletonList(secGroup));
	}

	@Override
	public boolean removeIdentityFromSecurityGroups(List<Identity> identities, List<SecurityGroup> secGroups) {
		if(identities == null || identities.isEmpty()) return true;//nothing to do
		if(secGroups == null || secGroups.isEmpty()) return true;//nothing to do
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as msi ")
		  .append("  where msi.identity.key in (:identityKeys) and msi.securityGroup.key in (:secGroupKeys)");
		
		List<Long> identityKeys = new ArrayList<Long>();
		for(Identity identity:identities) {
			identityKeys.add(identity.getKey());
		}
		List<Long> secGroupKeys = new ArrayList<Long>();
		for(SecurityGroup secGroup:secGroups) {
			secGroupKeys.add(secGroup.getKey());
		}
		int rowsAffected = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("identityKeys", identityKeys)
				.setParameter("secGroupKeys", secGroupKeys)
				.executeUpdate();
		return rowsAffected > 0;
	}

	/**
	 * @see org.olat.basesecurity.Manager#createAndPersistPolicy(org.olat.basesecurity.SecurityGroup, java.lang.String, org.olat.core.id.OLATResourceable
	 */
	@Override
	public Policy createAndPersistPolicy(SecurityGroup secGroup, String permission, OLATResourceable olatResourceable) {
		OLATResource olatResource = orm.findOrPersistResourceable(olatResourceable);
		return createAndPersistPolicyWithResource(secGroup, permission, null, null, olatResource);
	}

	/**
	 * Creates a policy and persists on the database
	 * @see org.olat.basesecurity.BaseSecurity#createAndPersistPolicyWithResource(org.olat.basesecurity.SecurityGroup, java.lang.String, java.util.Date, java.util.Date, org.olat.resource.OLATResource)
	 */
	private Policy createAndPersistPolicyWithResource(SecurityGroup secGroup, String permission, Date from, Date to, OLATResource olatResource) {
		PolicyImpl pi = new PolicyImpl();
		pi.setSecurityGroup(secGroup);
		pi.setOlatResource(olatResource);
		pi.setPermission(permission);
		pi.setFrom(from);
		pi.setTo(to);
		DBFactory.getInstance().saveObject(pi);
		return pi;
	}	
	
	/**
	 * Helper method that only creates a policy only if no such policy exists in the database
	 * @param secGroup
	 * @param permission
	 * @param olatResourceable
	 * @return Policy
	 */
	private Policy createAndPersistPolicyIfNotExists(SecurityGroup secGroup, String permission, OLATResourceable olatResourceable) {
		OLATResource olatResource = orm.findOrPersistResourceable(olatResourceable);
		Policy existingPolicy = findPolicy(secGroup, permission, olatResource);
		if (existingPolicy == null) {
			return createAndPersistPolicy(secGroup, permission, olatResource);
		}
		return existingPolicy;
	}

	public Policy findPolicy(SecurityGroup secGroup, String permission, OLATResource olatResource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select poi from ").append(PolicyImpl.class.getName()).append(" as poi ")
		  .append(" where poi.permission=:permission and poi.olatResource.key=:resourceKey and poi.securityGroup.key=:secGroupKey");

		List<Policy> policies = DBFactory.getInstance().getCurrentEntityManager()
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

	@Override
	public void deletePolicies(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(PolicyImpl.class.getName()).append(" as poi ")
		  .append(" where poi.olatResource.key=:resourceKey");

		int rowDeleted = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("resourceKey", resource.getKey())
				.executeUpdate();
		if(log.isDebug()) {
			log.debug(rowDeleted + " policies deleted");
		}
	}

	/**
	 * @param username the username
	 * @param user The persisted user (mandatory)
	 * @param authusername the username used as authentication credential
	 *          (=username for provider "OLAT")
	 * @param provider the provider of the authentication ("OLAT" or "AAI"). If null, no 
	 * authentication token is generated.
	 * @param credential the credentials or null if not used
	 * @return Identity
	 *
	@Override
	public Identity createAndPersistIdentity(String username, User user, String provider, String authusername, String credential) {
		IdentityImpl iimpl = new IdentityImpl(username, user);
		dbInstance.getCurrentEntityManager().persist(iimpl);
		((UserImpl)user).setIdentity(iimpl);
		if (provider != null) { 
			createAndPersistAuthenticationIntern(iimpl, provider, authusername, credential, loginModule.getDefaultHashAlgorithm());
		}
		notifyNewIdentityCreated(iimpl);
		return iimpl;
	}*/

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
	public Identity createAndPersistIdentityAndUserWithDefaultProviderAndUserGroup(String loginName, String externalId, String pwd,  User newUser) {
		Identity ident = null;
		if (pwd == null) {
			// when no password is used the provider must be set to null to not generate
			// an OLAT authentication token. See method doku.
			ident = createAndPersistIdentityAndUser(loginName, externalId, newUser, null, null);
			log.audit("Create an identity without authentication (login=" + loginName + ")");
 		} else {
			ident = createAndPersistIdentityAndUser(loginName, externalId, newUser, BaseSecurityModule.getDefaultAuthProviderIdentifier(), loginName, pwd);
			log.audit("Create an identity with " + BaseSecurityModule.getDefaultAuthProviderIdentifier() + " authentication (login=" + loginName + ")");
		}

		// Add user to system users group
		SecurityGroup olatuserGroup = findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		addIdentityToSecurityGroup(ident, olatuserGroup);
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
		// Add user to system users group
		SecurityGroup olatuserGroup = findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		addIdentityToSecurityGroup(ident, olatuserGroup);
		return ident;
	}
	
	private void notifyNewIdentityCreated(Identity newIdentity) {
		//Save the identity on the DB. So can the listeners of the event retrieve it
		//in cluster mode
		DBFactory.getInstance().commit();
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new NewIdentityCreatedEvent(newIdentity), IDENTITY_EVENT_CHANNEL);
	}

	/**
	 * @see org.olat.basesecurity.Manager#getIdentitiesOfSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 */
	public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup) {
		if (secGroup == null) {
			throw new AssertException("getIdentitiesOfSecurityGroup: ERROR secGroup was null !!");
		} 
		DB db = DBFactory.getInstance();
		if (db == null) {
			throw new AssertException("getIdentitiesOfSecurityGroup: ERROR db was null !!");
		} 

		List<Identity> idents = getIdentitiesOfSecurityGroup(secGroup, 0, -1);
		return idents;
	}
	
	@Override
	public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup, int firstResult, int maxResults) {
		if (secGroup == null) {
			throw new AssertException("getIdentitiesOfSecurityGroup: ERROR secGroup was null !!");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select identity from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
	   .append(" inner join sgmsi.identity identity ")
	   .append(" inner join fetch  identity.user user ")
			.append(" where sgmsi.securityGroup=:secGroup");

		TypedQuery<Identity> query = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("secGroup", secGroup);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}

	/**
	 * Return a list of unique identities which are in the list of security groups
	 * @see org.olat.basesecurity.BaseSecurity#getIdentitiesOfSecurityGroups(java.util.List)
	 */
	@Override
	public List<Identity> getIdentitiesOfSecurityGroups(List<SecurityGroup> secGroups) {
		if (secGroups == null || secGroups.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(identity) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
		  .append(" inner join sgmsi.identity identity ")
		  .append(" inner join fetch  identity.user user ")
		  .append(" where sgmsi.securityGroup in (:secGroups)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("secGroups", secGroups)
				.getResultList();
	}

	/**
	 * @see org.olat.basesecurity.Manager#getIdentitiesAndDateOfSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 */
	@Override
	public List<Object[]> getIdentitiesAndDateOfSecurityGroup(SecurityGroup secGroup) {
	   StringBuilder sb = new StringBuilder();
	   sb.append("select ii, sgmsi.lastModified from ").append(IdentityImpl.class.getName()).append(" as ii")
	     .append(" inner join fetch ii.user as iuser, ")
	     .append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi")
	     .append(" where sgmsi.securityGroup=:secGroup and sgmsi.identity = ii");
	 
	   return dbInstance.getCurrentEntityManager()
				 .createQuery(sb.toString(), Object[].class)
				 .setParameter("secGroup", secGroup)
				 .getResultList();
	}

	/**
	 * @see org.olat.basesecurity.Manager#countIdentitiesOfSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 */
	@Override
	public int countIdentitiesOfSecurityGroup(SecurityGroup secGroup) {
		DB db = DBFactory.getInstance();
		String q = "select count(sgm) from org.olat.basesecurity.SecurityGroupMembershipImpl sgm where sgm.securityGroup = :group";
		DBQuery query = db.createQuery(q);
		query.setEntity("group", secGroup);
		query.setCacheable(true);
		int result = ((Long) query.list().get(0)).intValue();
		return result;
	}

	/**
	 * @see org.olat.basesecurity.Manager#createAndPersistNamedSecurityGroup(java.lang.String)
	 */
	@Override
	public SecurityGroup createAndPersistNamedSecurityGroup(String groupName) {
		SecurityGroup secG = createAndPersistSecurityGroup();
		NamedGroupImpl ngi = new NamedGroupImpl(groupName, secG);
		DBFactory.getInstance().saveObject(ngi);
		return secG;
	}

	/**
	 * @see org.olat.basesecurity.Manager#findSecurityGroupByName(java.lang.String)
	 */
	@Override
	public SecurityGroup findSecurityGroupByName(String securityGroupName) {
		StringBuilder sb = new StringBuilder();
		sb.append("select sgi from ").append(NamedGroupImpl.class.getName()).append(" as ngroup ")
		  .append(" inner join ngroup.securityGroup sgi")
		  .append(" where ngroup.groupName=:groupName");

		List<SecurityGroup> group = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), SecurityGroup.class)
				.setParameter("groupName", securityGroupName)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();

		int size = group.size();
		if (size == 0) return null;
		if (size != 1) throw new AssertException("non unique name in namedgroup: " + securityGroupName);
		SecurityGroup sg = group.get(0);
		return sg;
	}

	/**
	 * @see org.olat.basesecurity.Manager#findIdentityByName(java.lang.String)
	 */
	@Override
	public Identity findIdentityByName(String identityName) {
		if (identityName == null) throw new AssertException("findIdentitybyName: name was null");

		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.name=:username");
		
		List<Identity> identities = DBFactory.getInstance().getCurrentEntityManager()
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
		
		List<Identity> identities = DBFactory.getInstance().getCurrentEntityManager()
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
		Map<String, String> userPropertiesSearch = new HashMap<String, String>();
		// institutional identifier
		userPropertiesSearch.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, identityNumber);
		List<Identity> identities = getIdentitiesByPowerSearch(null, userPropertiesSearch, true, null, null, null, null, null, null, null, null);

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

		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.name in (:username)");
		
		List<Identity> identities = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", identityNames)
				.getResultList();
		return identities;
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
		
		List<Identity> identities = DBFactory.getInstance().getCurrentEntityManager()
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
		List<String> names = new ArrayList<String>(identityNames);
		List<IdentityShort> shortIdentities = new ArrayList<IdentityShort>(names.size());
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
		List<IdentityShort> shortIdentities = new ArrayList<IdentityShort>(names.size());
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
				
				if (searchArr[i].contains("_") && dbVendor.equals("oracle")) {
					//oracle needs special ESCAPE sequence to search for escaped strings
					sb.append(" lower(ident.").append(attribute).append(") like :search").append(i).append(" ESCAPE '\\'");
				} else if (dbVendor.equals("mysql")) {
					sb.append(" ident.").append(attribute).append(" like :search").append(i);
				} else {
					sb.append(" lower(ident.").append(attribute).append(") like :search").append(i);
				}
			}
		}
		sb.append(")");
		
		TypedQuery<IdentityShort> searchQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class);
		for(int i=searchArr.length; i-->0; ) {
			searchQuery.setParameter("search" + i, PersistenceHelper.makeFuzzyQueryString(searchArr[i]));
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

	/**
	 * 
	 * @see org.olat.basesecurity.Manager#countUniqueUserLoginsSince(java.util.Date)
	 */
	@Override
	public Long countUniqueUserLoginsSince (Date lastLoginLimit){
		String queryStr ="Select count(ident) from org.olat.core.id.Identity as ident where " 
			+ "ident.lastLogin > :lastLoginLimit and ident.lastLogin != ident.creationDate";	
		DBQuery dbq = DBFactory.getInstance().createQuery(queryStr);
		dbq.setDate("lastLoginLimit", lastLoginLimit);
		List res = dbq.list();
		Long cntL = (Long) res.get(0);
		return cntL;
	}	
	
	/**
	 * @see org.olat.basesecurity.Manager#getAuthentications(org.olat.core.id.Identity)
	 */
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

	/**
	 * @see org.olat.basesecurity.Manager#createAndPersistAuthentication(org.olat.core.id.Identity, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Authentication createAndPersistAuthentication(final Identity ident, final String provider, final String authUserName,
			final String credentials, final Encoder.Algorithm algorithm) {
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstanceWithoutCheck(provider, ident.getKey());
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resourceable, new SyncerCallback<Authentication>() {
			@Override
			public Authentication execute() {
				Authentication auth = findAuthentication(ident, provider);
				if(auth == null) {
					auth = createAndPersistAuthenticationIntern(ident, provider,  authUserName, credentials, algorithm);
				}
				return auth;
			}
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
		log.audit("Create " + provider + " authentication (identity=" + ident.getKey() + ",authusername=" + authUserName + ")");
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
		if (results == null || results.size() == 0) return null;
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
		if (results == null || results.size() == 0) return null;
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
		
		List<Authentication> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("credential", securityToken)
				.setParameter("provider", provider)
				.getResultList();
		return results;
	}
	
	@Override
	public List<Authentication> findOldAuthentication(String provider, Date creationDate) {
		if (provider == null || creationDate == null) {
			throw new IllegalArgumentException("provider and token must not be null");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName())
		  .append(" as auth where auth.provider=:provider and auth.creationDate<:creationDate");
		
		List<Authentication> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("creationDate", creationDate, TemporalType.TIMESTAMP)
				.setParameter("provider", provider)
				.getResultList();
		return results;
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
		if (results.size() == 0) return null;
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

	/**
	 * @see org.olat.basesecurity.Manager#getVisibleIdentitiesByPowerSearch(java.lang.String, java.util.Map, boolean, org.olat.basesecurity.SecurityGroup[], org.olat.basesecurity.PermissionOnResourceable[], java.lang.String[], java.util.Date, java.util.Date)
	 */
  @Override
	public List<Identity> getVisibleIdentitiesByPowerSearch(String login, Map<String, String> userproperties, boolean userPropertiesAsIntersectionSearch,
			SecurityGroup[] groups, PermissionOnResourceable[] permissionOnResources, String[] authProviders, Date createdAfter, Date createdBefore) {
		return getIdentitiesByPowerSearch(new SearchIdentityParams(login, userproperties, userPropertiesAsIntersectionSearch, groups, permissionOnResources, 
				                              authProviders, createdAfter, createdBefore, null, null, Identity.STATUS_VISIBLE_LIMIT), 0, -1); 
	}
	
  @Override
	public List<Identity> getVisibleIdentitiesByPowerSearch(String login, Map<String, String> userProperties,
			boolean userPropertiesAsIntersectionSearch, SecurityGroup[] groups, PermissionOnResourceable[] permissionOnResources,
			String[] authProviders, Date createdAfter, Date createdBefore, int firstResult, int maxResults) {
		return getIdentitiesByPowerSearch(new SearchIdentityParams(login, userProperties, userPropertiesAsIntersectionSearch, groups, permissionOnResources, 
        authProviders, createdAfter, createdBefore, null, null, Identity.STATUS_VISIBLE_LIMIT), firstResult, maxResults); 
	}

  @Override
	public long countIdentitiesByPowerSearch(String login, Map<String, String> userproperties, boolean userPropertiesAsIntersectionSearch, SecurityGroup[] groups,
			PermissionOnResourceable[] permissionOnResources, String[] authProviders, Date createdAfter, Date createdBefore, Date userLoginAfter, Date userLoginBefore,  Integer status) {
		DBQuery dbq = createIdentitiesByPowerQuery(new SearchIdentityParams(login, userproperties, userPropertiesAsIntersectionSearch, groups, permissionOnResources, authProviders, createdAfter, createdBefore, userLoginAfter, userLoginBefore, status), true);
		Number count = (Number)dbq.uniqueResult();
		return count.longValue();
	}

	/**
	 * @see org.olat.basesecurity.Manager#getIdentitiesByPowerSearch(java.lang.String, java.util.Map, boolean, org.olat.basesecurity.SecurityGroup[], org.olat.basesecurity.PermissionOnResourceable[], java.lang.String[], java.util.Date, java.util.Date, java.lang.Integer)
   */
  @Override
	public List<Identity> getIdentitiesByPowerSearch(String login, Map<String, String> userproperties, boolean userPropertiesAsIntersectionSearch, SecurityGroup[] groups,
			PermissionOnResourceable[] permissionOnResources, String[] authProviders, Date createdAfter, Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status) {
		DBQuery dbq = createIdentitiesByPowerQuery(new SearchIdentityParams(login, userproperties, userPropertiesAsIntersectionSearch, groups, permissionOnResources, authProviders, createdAfter, createdBefore, userLoginAfter, userLoginBefore, status), false);
		return dbq.list();
	}
  
	@Override
	public int countIdentitiesByPowerSearch(SearchIdentityParams params) {
		DBQuery dbq = createIdentitiesByPowerQuery(params, true);
		Number count = (Number)dbq.uniqueResult();
		return count.intValue();
	}
	
	@Override
	public List<Identity> getIdentitiesByPowerSearch(SearchIdentityParams params, int firstResult, int maxResults) {
		DBQuery dbq = createIdentitiesByPowerQuery(params, false);
		if(firstResult >= 0) {
			dbq.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			dbq.setMaxResults(maxResults);
		}
		@SuppressWarnings("unchecked")
		List<Identity> identities = dbq.list();
		return identities;
	}

	private DBQuery createIdentitiesByPowerQuery(SearchIdentityParams params, boolean count) {
		boolean hasGroups = (params.getGroups() != null && params.getGroups().length > 0);
		boolean hasPermissionOnResources = (params.getPermissionOnResources() != null && params.getPermissionOnResources().length > 0);
		boolean hasAuthProviders = (params.getAuthProviders() != null && params.getAuthProviders().length > 0);

		// select identity and inner join with user to optimize query
		StringBuilder sb = new StringBuilder(5000);
		if (hasAuthProviders) {
			// I know, it looks wrong but I need to do the join reversed since it is not possible to 
			// do this query with a left join that starts with the identity using hibernate HQL. A left
			// or right join is necessary since it is totally ok to have null values as authentication
			// providers (e.g. when searching for users that do not have any authentication providers at all!).
			// It took my quite a while to make this work, so think twice before you change anything here!
			if(count) {
				sb.append("select count(distinct ident.key) from org.olat.basesecurity.AuthenticationImpl as auth  ")
				  .append(" right join auth.identity as ident")
				  .append(" inner join ident.user as user ");
			} else {
				sb.append("select distinct ident from org.olat.basesecurity.AuthenticationImpl as auth ")
				  .append(" right join auth.identity as ident")
				  .append(" inner join fetch ident.user as user ");
			}
		} else {
			if(count) {
				sb.append("select count(distinct ident.key) from org.olat.core.id.Identity as ident ")
				  .append(" inner join ident.user as user ");
			} else {
				sb.append("select distinct ident from org.olat.core.id.Identity as ident ")
				  .append(" inner join fetch ident.user as user ");
			}
		}
		// In any case join with the user. Don't join-fetch user, this breaks the query
		// because of the user fields (don't know exactly why this behaves like
		// this)

		if (hasGroups) {
			// join over security group memberships
			sb.append(" ,org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi ");
		}
		if (hasPermissionOnResources) {
			// join over policies
			sb.append(" ,org.olat.basesecurity.SecurityGroupMembershipImpl as policyGroupMembership "); 
			sb.append(" ,org.olat.basesecurity.PolicyImpl as policy "); 
			sb.append(" ,org.olat.resource.OLATResourceImpl as resource ");
		}	
		
		String login = params.getLogin();
		Map<String,String> userproperties = params.getUserProperties();
		Date createdAfter = params.getCreatedAfter();
		Date createdBefore = params.getCreatedBefore();
		Integer status = params.getStatus();
		Collection<Long> identityKeys = params.getIdentityKeys();
		Boolean managed = params.getManaged();
		
		// complex where clause only when values are available
		if (login != null || (userproperties != null && !userproperties.isEmpty())
				|| (identityKeys != null && !identityKeys.isEmpty()) || createdAfter != null	|| createdBefore != null
				|| hasAuthProviders || hasGroups || hasPermissionOnResources || status != null
				|| managed != null) {
			
			sb.append(" where ");		
			boolean needsAnd = false;
			boolean needsUserPropertiesJoin = false;
			
			// treat login and userProperties as one element in this query
			if (login != null && (userproperties != null && !userproperties.isEmpty())) {
				sb.append(" ( ");			
			}
			// append query for login
			if (login != null) {
				login = makeFuzzyQueryString(login);
				if (login.contains("_") && dbVendor.equals("oracle")) {
					//oracle needs special ESCAPE sequence to search for escaped strings
					sb.append(" lower(ident.name) like :login ESCAPE '\\'");
				} else if (dbVendor.equals("mysql")) {
					sb.append(" ident.name like :login");
				} else {
					sb.append(" lower(ident.name) like :login");
				}
				// if user fields follow a join element is needed
				needsUserPropertiesJoin = true;
				// at least one user field used, after this and is required
				needsAnd = true;
			}

			// append queries for user fields
			if (userproperties != null && !userproperties.isEmpty()) {
				Map<String, String> emailProperties = new HashMap<String, String>();
				Map<String, String> otherProperties = new HashMap<String, String>();
	
				// split the user fields into two groups
				for (String key : userproperties.keySet()) {
					if (key.toLowerCase().contains("email")) {
						emailProperties.put(key, userproperties.get(key));
					} else {
						otherProperties.put(key, userproperties.get(key));
					}
				}
	
				// handle email fields special: search in all email fields
				if (!emailProperties.isEmpty()) {
					needsUserPropertiesJoin = checkIntersectionInUserProperties(sb, needsUserPropertiesJoin, params.isUserPropertiesAsIntersectionSearch());
					boolean moreThanOne = emailProperties.size() > 1;
					if (moreThanOne) sb.append("(");
					boolean needsOr = false;
					for (String key : emailProperties.keySet()) {
						if (needsOr) sb.append(" or ");
						if(dbVendor.equals("mysql")) {
							sb.append(" user.").append(key).append(" like :").append(key).append("_value ");
						} else {
							sb.append(" lower(user.").append(key).append(") like :").append(key).append("_value ");
						}
						if(dbVendor.equals("oracle")) {
							sb.append(" escape '\\'");
						}
						needsOr = true;
					}
					if (moreThanOne) sb.append(")");
					// cleanup
					emailProperties.clear();
				}
	
				// add other fields
				for (String key : otherProperties.keySet()) {
					needsUserPropertiesJoin = checkIntersectionInUserProperties(sb, needsUserPropertiesJoin, params.isUserPropertiesAsIntersectionSearch());
					
					if(dbVendor.equals("mysql")) {
						sb.append(" user.").append(key).append(" like :").append(key).append("_value ");
					} else {
						sb.append(" lower(user.").append(key).append(") like :").append(key).append("_value ");
					}
					if(dbVendor.equals("oracle")) {
						sb.append(" escape '\\'");
					}
					needsAnd = true;
				}
				// cleanup
				otherProperties.clear();
				// at least one user field used, after this and is required
				needsAnd = true;
			}
			// end of user fields and login part
			if (login != null && (userproperties != null && !userproperties.isEmpty())) {
				sb.append(" ) ");
			}
			// now continue with the other elements. They are joined with an AND connection
	
			// append query for identity primary keys
			if(identityKeys != null && !identityKeys.isEmpty()) {
				needsAnd = checkAnd(sb, needsAnd);
				sb.append("ident.key in (:identityKeys)");
			}
			
			if(managed != null) {
				needsAnd = checkAnd(sb, needsAnd);
				if(managed.booleanValue()) {
					sb.append("ident.externalId is not null");
				} else {
					sb.append("ident.externalId is null");
				}	
			}
			
			// append query for named security groups
			if (hasGroups) {
				SecurityGroup[] groups = params.getGroups();
				needsAnd = checkAnd(sb, needsAnd);
				sb.append(" (");
				for (int i = 0; i < groups.length; i++) {
					sb.append(" sgmsi.securityGroup=:group_").append(i);
					if (i < (groups.length - 1)) sb.append(" or ");
				}
				sb.append(") ");
				sb.append(" and sgmsi.identity=ident ");
			}
	
			// append query for policies
			if (hasPermissionOnResources) {
				needsAnd = checkAnd(sb, needsAnd);
				sb.append(" (");
				PermissionOnResourceable[] permissionOnResources = params.getPermissionOnResources();
				for (int i = 0; i < permissionOnResources.length; i++) {
					sb.append(" (");
					sb.append(" policy.permission=:permission_").append(i);
					sb.append(" and policy.olatResource = resource ");
					sb.append(" and resource.resId = :resourceId_").append(i);
					sb.append(" and resource.resName = :resourceName_").append(i);
					sb.append(" ) ");
					if (i < (permissionOnResources.length - 1)) sb.append(" or ");
				}
				sb.append(") ");
				sb.append(" and policy.securityGroup=policyGroupMembership.securityGroup ");
				sb.append(" and policyGroupMembership.identity=ident ");
			}
	    
			// append query for authentication providers
			if (hasAuthProviders) {
				needsAnd = checkAnd(sb, needsAnd);
				sb.append(" (");
				String[] authProviders = params.getAuthProviders();
				for (int i = 0; i < authProviders.length; i++) {
					// special case for null auth provider
					if (authProviders[i] == null) {
						sb.append(" auth is null ");
					} else {
						sb.append(" auth.provider=:authProvider_").append(i);
					}
					if (i < (authProviders.length - 1)) sb.append(" or ");
				}
				sb.append(") ");
			}
	
			// append query for creation date restrictions
			if (createdAfter != null) {
				needsAnd = checkAnd(sb, needsAnd);
				sb.append(" ident.creationDate >= :createdAfter ");
			}
			if (createdBefore != null) {
				needsAnd = checkAnd(sb, needsAnd);
				sb.append(" ident.creationDate <= :createdBefore ");
			}
			if(params.getUserLoginAfter() != null){
				needsAnd = checkAnd(sb, needsAnd);
				sb.append(" ident.lastLogin >= :lastloginAfter ");
			}
			if(params.getUserLoginBefore() != null){
				needsAnd = checkAnd(sb, needsAnd);
				sb.append(" ident.lastLogin <= :lastloginBefore ");
			}
			
			if (status != null) {
				if (status.equals(Identity.STATUS_VISIBLE_LIMIT)) {
					// search for all status smaller than visible limit 
					needsAnd = checkAnd(sb, needsAnd);
					sb.append(" ident.status < :status ");
				} else {
					// search for certain status
					needsAnd = checkAnd(sb, needsAnd);
					sb.append(" ident.status = :status ");
				}
			} 
		}
			
		// create query object now from string
		String query = sb.toString();
		DBQuery dbq = dbInstance.createQuery(query);
		
		// add user attributes
		if (login != null) {
			dbq.setString("login", login.toLowerCase());
		}
		
		if(identityKeys != null && !identityKeys.isEmpty()) {
			dbq.setParameterList("identityKeys", identityKeys);
		}

		//	 add user properties attributes
		if (userproperties != null && !userproperties.isEmpty()) {
			for (String key : userproperties.keySet()) {
				String value = userproperties.get(key);
				value = makeFuzzyQueryString(value);
				dbq.setString(key + "_value", value.toLowerCase());
			}
		}

		// add named security group names
		if (hasGroups) {
			SecurityGroup[] groups = params.getGroups();
			for (int i = 0; i < groups.length; i++) {
				SecurityGroupImpl group = (SecurityGroupImpl) groups[i]; // need to work with impls
				dbq.setEntity("group_" + i, group);
			}
		}
		
		// add policies
		if (hasPermissionOnResources) {
			PermissionOnResourceable[] permissionOnResources = params.getPermissionOnResources();
			for (int i = 0; i < permissionOnResources.length; i++) {
				PermissionOnResourceable permissionOnResource = permissionOnResources[i];
				dbq.setString("permission_" + i, permissionOnResource.getPermission());
				Long id = permissionOnResource.getOlatResourceable().getResourceableId();
				dbq.setLong("resourceId_" + i, (id == null ? 0 : id.longValue()));
				dbq.setString("resourceName_" + i, permissionOnResource.getOlatResourceable().getResourceableTypeName());
			}
		}

		// add authentication providers
		if (hasAuthProviders) {
			String[] authProviders = params.getAuthProviders();
			for (int i = 0; i < authProviders.length; i++) {
				String authProvider = authProviders[i];
				if (authProvider != null) {
					dbq.setString("authProvider_" + i, authProvider);
				}
				// ignore null auth provider, already set to null in query
			}
		}
		
		// add date restrictions
		if (createdAfter != null) {
			dbq.setDate("createdAfter", createdAfter);
		}
		if (createdBefore != null) {
			dbq.setDate("createdBefore", createdBefore);
		}
		if(params.getUserLoginAfter() != null){
			dbq.setDate("lastloginAfter", params.getUserLoginAfter());
		}
		if(params.getUserLoginBefore() != null){
			dbq.setDate("lastloginBefore", params.getUserLoginBefore());
		}
		
		if (status != null) {
			dbq.setInteger("status", status);
		}
		// execute query
		return dbq;
	}
	
	/**
	 * 
	 * @param dbVendor
	 */
	public void setDbVendor(String dbVendor) {
		this.dbVendor = dbVendor;
	}

	@Override
	public boolean isIdentityVisible(Identity identity) {
		if(identity == null) return false;
		Integer status = identity.getStatus();
		return (status != null && status.intValue() < Identity.STATUS_VISIBLE_LIMIT);
	}

	private boolean checkAnd(StringBuilder sb, boolean needsAnd) {
		if (needsAnd) 	sb.append(" and ");
		return true;
	}

	
	private boolean checkIntersectionInUserProperties(StringBuilder sb, boolean needsJoin, boolean userPropertiesAsIntersectionSearch) {
		if (needsJoin) 	{
			if (userPropertiesAsIntersectionSearch) {
				sb.append(" and ");								
			} else {
				sb.append(" or ");				
			}
		}
		return true;
	}

	/**
	 * Helper method that replaces * with % and appends and
	 * prepends % to the string to make fuzzy SQL match when using like 
	 * @param email
	 * @return fuzzized string
	 */
	private String makeFuzzyQueryString(String string) {
		// By default only fuzzyfy at the end. Usually it makes no sense to do a
		// fuzzy search with % at the beginning, but it makes the query very very
		// slow since it can not use any index and must perform a fulltext search.
		// User can always use * to make it a really fuzzy search query
		// fxdiff FXOLAT-252: use "" to disable this feature and use exact match
		if (string.length() > 1 && string.startsWith("\"") && string.endsWith("\"")) {			
			string = string.substring(1, string.length()-1);
		} else {
			string = string + "%";
			string = string.replace('*', '%');
		}
		// with 'LIKE' the character '_' is a wildcard which matches exactly one character.
		// To test for literal instances of '_', we have to escape it.
		string = string.replace("_", "\\_");
		return string;
	}

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
			dbInstance.commit();
		}
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
			dbInstance.commit();
		}
		return reloadedIdentity;
	}
	
	@Override
	public Identity setExternalId(Identity identity, String externalId) {
		IdentityImpl reloadedIdentity = loadForUpdate(identity);
		if(reloadedIdentity != null) {
			reloadedIdentity.setExternalId(externalId);
			reloadedIdentity = dbInstance.getCurrentEntityManager().merge(reloadedIdentity);
			dbInstance.commit();
		}
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
	public List<SecurityGroup> getSecurityGroupsForIdentity(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select sgi from ").append(SecurityGroupImpl.class.getName()).append(" as sgi, ")
		  .append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
		  .append(" where sgmsi.securityGroup=sgi and sgmsi.identity.key=:identityKey");

	  List<SecurityGroup> secGroups = DBFactory.getInstance().getCurrentEntityManager()
	  		.createQuery(sb.toString(), SecurityGroup.class)
	  		.setParameter("identityKey", identity.getKey())
	  		.getResultList();

  	return secGroups;
	}
	

	/**
	 * @see org.olat.basesecurity.Manager#getAndUpdateAnonymousUserForLanguage(java.util.Locale)
	 */
	public Identity getAndUpdateAnonymousUserForLanguage(Locale locale) {
		Translator trans = Util.createPackageTranslator(UserManager.class, locale);
		String guestUsername = GUEST_USERNAME_PREFIX + locale.toString();		
		Identity guestIdentity = findIdentityByName(guestUsername);
		if (guestIdentity == null) {
			// Create it lazy on demand
			User guestUser = UserManager.getInstance().createUser(trans.translate("user.guest"), null, null);
			guestUser.getPreferences().setLanguage(locale.toString());
			guestIdentity = createAndPersistIdentityAndUser(guestUsername, null, guestUser, null, null, null);
			SecurityGroup anonymousGroup = findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
			addIdentityToSecurityGroup(guestIdentity, anonymousGroup);
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
	public void deleteUserData(Identity identity, String newDeletedUserName, File archivePath) {
		// 1) delete all authentication tokens
		List<Authentication> authentications = getAuthentications(identity);
		for (Authentication auth:authentications) {
			deleteAuthentication(auth);
			log.info("Delete authentication provider::" + auth.getProvider() + "  of identity="  + identity);
		}
		
		// 2) Delete the authentication history
		authenticationHistoryDao.deleteAuthenticationHistory(identity);		

		// 3) Remove legacy security group memberships
		List<SecurityGroup> securityGroups = getSecurityGroupsForIdentity(identity);
		for (SecurityGroup secGroup : securityGroups) {
			removeIdentityFromSecurityGroup(identity, secGroup);
			log.info("Removing identity::" + identity.getKey() + " from security group::" + secGroup.getKey()
					+ ", resourceableTypeName::" + secGroup.getResourceableTypeName() + ", resourceableId"
					+ secGroup.getResourceableId());
		}
		// new security groups / memberships are deleted in UserDeletionManager
	}
}