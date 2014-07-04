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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.olat.admin.quota.GenericQuotaEditController;
import org.olat.admin.sysinfo.SysinfoController;
import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.UserChangePasswordController;
import org.olat.admin.user.UserCreateController;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.events.NewIdentityCreatedEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.Encoder;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.resource.OresHelper;
import org.olat.login.LoginModule;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.OLATResourceManager;
import org.olat.user.ChangePasswordController;
import org.olat.user.PersonalSettingsController;
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
public class BaseSecurityManager extends BasicManager implements BaseSecurity {
	private DB dbInstance;
	private OLATResourceManager orm;
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
	 * @see org.olat.basesecurity.Manager#init()
	 */
	public void init() { // called only once at startup and only from one thread
		// init the system level groups and its policies
		initSysGroupAdmin();
		DBFactory.getInstance(false).intermediateCommit();
		initSysGroupAuthors();
		DBFactory.getInstance(false).intermediateCommit();
		initSysGroupGroupmanagers();
		DBFactory.getInstance(false).intermediateCommit();
		initSysGroupPoolsmanagers();
		DBFactory.getInstance(false).intermediateCommit();
		initSysGroupUsermanagers();
		DBFactory.getInstance(false).intermediateCommit();
		initSysGroupUsers();
		DBFactory.getInstance(false).intermediateCommit();
		initSysGroupAnonymous();
		DBFactory.getInstance(false).intermediateCommit();
		initSysGroupInstitutionalResourceManager();
		DBFactory.getInstance(false).intermediateCommit();
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
		createAndPersistPolicyIfNotExists(olatuserGroup, Constants.PERMISSION_ACCESS, OresHelper.lookupType(PersonalSettingsController.class));
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
	
	@Override
	public List<Policy> getPoliciesOfSecurityGroup(List<SecurityGroup> secGroups, OLATResource... resources) {
		if(secGroups == null || secGroups.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select poi from ").append(PolicyImpl.class.getName()).append(" as poi")
		  .append(" inner join fetch poi.securityGroup as secGroup")
		  .append(" inner join fetch poi.olatResource as resource")
		  .append(" where secGroup.key in (:secGroupKeys)");
		if(resources != null && resources.length > 0) {
			sb.append(" and resource.key in (:resourceKeys)");
		}

		List<Long> secGroupKeys = PersistenceHelper.toKeys(secGroups);
		TypedQuery<Policy> queryPolicies = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Policy.class)
				.setParameter("secGroupKeys", secGroupKeys);
		if(resources != null && resources.length > 0) {
			List<Long> resourceKeys = PersistenceHelper.toKeys(resources);
			queryPolicies.setParameter("resourceKeys", resourceKeys);
		}	
		List<Policy> policies =	queryPolicies.getResultList();
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
	public List<Identity> getIdentitiesWithPermissionWithOlatResourceableType(
			String permission, String olatResourceableTypeName) {
		// if the olatResourceable is not persisted as OLATResource, then the answer
		// is false, therefore we can use the query assuming there is an OLATResource
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct im from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi,")
		  .append(IdentityImpl.class.getName()).append(" as im,")
		  .append(PolicyImpl.class.getName()).append(" as poi,")
		  .append(OLATResourceImpl.class.getName()).append(" as ori ")
		  .append("where im=sgmsi.identity and sgmsi.securityGroup=poi.securityGroup ")
		  .append(" and poi.permission=:permission and poi.olatResource=ori and ori.resName=:resName");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Identity.class)
				.setParameter("permission", permission)
				.setParameter("resName", olatResourceableTypeName)
				.getResultList();
	}
	
	@Override
	public List<String> getIdentityPermissionOnresourceable(Identity identity, OLATResourceable olatResourceable) {
		Long oresid = olatResourceable.getResourceableId();
		if (oresid == null) {
			oresid = new Long(0);
		}
		List<String> permissions = dbInstance.getCurrentEntityManager()
				.createNamedQuery("getIdentityPermissionsOnResourceableCheckType", String.class)
			.setParameter("identitykey", identity.getKey())
			.setParameter("resid", oresid)
			.setParameter("resname", olatResourceable.getResourceableTypeName())
			.getResultList();
		return permissions;
	}

	@Override
	public boolean isIdentityPermittedOnResourceable(Identity identity, String permission, OLATResourceable olatResourceable) {
		return isIdentityPermittedOnResourceable(identity, permission, olatResourceable, true);
	}

	/**
	 * @see org.olat.basesecurity.Manager#isIdentityPermittedOnResourceable(org.olat.core.id.Identity, java.lang.String, org.olat.core.id.OLATResourceable boolean)
	 */
	@Override
	public boolean isIdentityPermittedOnResourceable(Identity identity, String permission, OLATResourceable olatResourceable, boolean checkTypeRight) {
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
	public Roles getRoles(Identity identity) {
		
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
			isInvitee = isIdentityInvited(identity);
			isGuestOnly = isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY);
		}
		
		return new Roles(admin, userManager, groupManager, author, isGuestOnly, resourceManager, poolManager, isInvitee);
	}

	@Override
	public List<String> getRolesAsString(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ngroup.groupName from ").append(NamedGroupImpl.class.getName()).append(" as ngroup ")
		  .append(" inner join ngroup.securityGroup sgi ")
		  .append(" where exists (")
		  .append("   select sgmsi from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi where sgmsi.identity.key=:identityKey and sgmsi.securityGroup=sgi")
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
			logAudit("User::" + actingIdentity.getName() + " added system role::" + groupName + " to user::" + updatedIdentity.getName(), null);
		} else if (hasBeenInGroup && !isNowInGroup) {
			// user not anymore in security group, remove him
			removeIdentityFromSecurityGroup(updatedIdentity, securityGroup);
			logAudit("User::" + actingIdentity.getName() + " removed system role::" + groupName + " from user::" + updatedIdentity.getName(), null);
		}
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
	
	@Override
	public void updatePolicy(Policy policy, Date from, Date to) {
		((PolicyImpl)policy).setFrom(from);
		((PolicyImpl)policy).setTo(to);
		DBFactory.getInstance().updateObject(policy);
	}

	/**
	 * @see org.olat.basesecurity.Manager#isIdentityInSecurityGroup(org.olat.core.id.Identity, org.olat.basesecurity.SecurityGroup)
	 */
	public boolean isIdentityInSecurityGroup(Identity identity, SecurityGroup secGroup) {
		if (secGroup == null) return false;
		String queryString = "select count(sgmsi) from  org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi where sgmsi.identity = :identitykey and sgmsi.securityGroup = :securityGroup";
		DBQuery query = DBFactory.getInstance().createQuery(queryString);
		query.setLong("identitykey", identity.getKey());
		query.setLong("securityGroup", secGroup.getKey());
		query.setCacheable(true);
		List res = query.list();
		Long cntL = (Long) res.get(0);
		if (cntL.longValue() != 0 && cntL.longValue() != 1) throw new AssertException("unique n-to-n must always yield 0 or 1");
		return (cntL.longValue() == 1);
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
	public void deleteSecurityGroup(SecurityGroup secGroup) {
		// we do not use hibernate cascade="delete", but implement our own (to be
		// sure to understand our code)
		DB db = DBFactory.getInstance();
		//FIXME: fj: Please review: Create rep entry, restart olat, delete the rep
		// entry. previous implementation resulted in orange screen
		// secGroup = (SecurityGroup)db.loadObject(secGroup); // so we can later
		// delete it (hibernate needs an associated session)
		secGroup = (SecurityGroup) db.loadObject(secGroup);
		//o_clusterREVIEW
		//db.reputInHibernateSessionCache(secGroup);

		/*
		 * if (!db.contains(secGroup)) { secGroup = (SecurityGroupImpl)
		 * db.loadObject(SecurityGroupImpl.class, secGroup.getKey()); }
		 */
		// 1) delete associated users (need to do it manually, hibernate knows
		// nothing about
		// the membership, modeled manually via many-to-one and not via set)
		db.delete("from org.olat.basesecurity.SecurityGroupMembershipImpl as msi where msi.securityGroup.key = ?", new Object[] { secGroup
				.getKey() }, new Type[] { StandardBasicTypes.LONG });
		// 2) delete all policies
		db.delete("from org.olat.basesecurity.PolicyImpl as poi where poi.securityGroup = ?", new Object[] { secGroup.getKey() },
				new Type[] { StandardBasicTypes.LONG });
		// 3) delete security group
		db.deleteObject(secGroup);
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
		return createAndPersistPolicy(secGroup, permission, null, null, olatResourceable);
	}
	
	/**
	 * @see org.olat.basesecurity.BaseSecurity#createAndPersistPolicy(org.olat.basesecurity.SecurityGroup, java.lang.String, java.util.Date, java.util.Date, org.olat.core.id.OLATResourceable)
	 */
	@Override
	public Policy createAndPersistPolicy(SecurityGroup secGroup, String permission, Date from, Date to, OLATResourceable olatResourceable) {
		OLATResource olatResource = orm.findOrPersistResourceable(olatResourceable);
		return createAndPersistPolicyWithResource(secGroup, permission, from, to, olatResource);
	}

	/**
	 * @see org.olat.basesecurity.BaseSecurity#createAndPersistPolicyWithResource(org.olat.basesecurity.SecurityGroup, java.lang.String, org.olat.resource.OLATResource)
	 */
	@Override
	public Policy createAndPersistPolicyWithResource(SecurityGroup secGroup, String permission, OLATResource olatResource) {
		return createAndPersistPolicyWithResource(secGroup, permission, null, null, olatResource);
	}

/**
 * Creates a policy and persists on the database
 * @see org.olat.basesecurity.BaseSecurity#createAndPersistPolicyWithResource(org.olat.basesecurity.SecurityGroup, java.lang.String, java.util.Date, java.util.Date, org.olat.resource.OLATResource)
 */
	@Override
	public Policy createAndPersistPolicyWithResource(SecurityGroup secGroup, String permission, Date from, Date to, OLATResource olatResource) {
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
	
	private void deletePolicy(Policy policy) {
		DBFactory.getInstance().deleteObject(policy);
	}

	@Override
	public boolean deletePolicies(Collection<SecurityGroup> secGroups, Collection<OLATResource> resources) {	
		if(secGroups == null || secGroups.isEmpty() || resources == null || resources.isEmpty()) return false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(PolicyImpl.class.getName()).append(" as poi ")
		  .append(" where poi.olatResource.key in (:resourceKey) and poi.securityGroup.key in (:secGroupKeys)");

		List<Long> secGroupKeys = PersistenceHelper.toKeys(secGroups);
		List<Long> resourceKeys = PersistenceHelper.toKeys(resources);
		int rows = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("resourceKey", resourceKeys)
				.setParameter("secGroupKeys", secGroupKeys)
				.executeUpdate();
		return rows > 0;
	}

	/**
	 * @see org.olat.basesecurity.Manager#deletePolicy(org.olat.basesecurity.SecurityGroup, java.lang.String, org.olat.core.id.OLATResourceable
	 */
	@Override
	public void deletePolicy(SecurityGroup secGroup, String permission, OLATResource resource) {		 
		if (resource == null) throw new AssertException("cannot delete policy of a null olatresourceable!");
		Policy p = findPolicy(secGroup, permission, resource);
		// fj: introduced strict testing here on purpose
		if (p != null) {
			deletePolicy(p);
		}
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
		if(isLogDebugEnabled()) {
			logDebug(rowDeleted + " policies deleted");
		}
	}

	/**
	 * 
	 * @see org.olat.basesecurity.BaseSecurity#createAndPersistInvitation()
	 */
	@Override
	public Invitation createAndPersistInvitation() {
		SecurityGroup secGroup = new SecurityGroupImpl();
		DBFactory.getInstance().saveObject(secGroup);
		
		InvitationImpl invitation = new InvitationImpl();
		invitation.setToken(UUID.randomUUID().toString());
		invitation.setSecurityGroup(secGroup);
		DBFactory.getInstance().saveObject(invitation);
		return invitation;
	}
	
	/**
	 * @see org.olat.basesecurity.BaseSecurity#updateInvitation(org.olat.basesecurity.Invitation)
	 */
	@Override
	public void updateInvitation(Invitation invitation) {
		DBFactory.getInstance().updateObject(invitation);
	}

	/**
	 * @see org.olat.basesecurity.BaseSecurity#hasInvitationPolicies(java.lang.String, java.util.Date)
	 */
	@Override
	public boolean hasInvitationPolicies(String token, Date atDate) {
		StringBuilder sb = new StringBuilder();
	  sb.append("select count(policy) from ").append(PolicyImpl.class.getName()).append(" as policy, ")
	  	.append(InvitationImpl.class.getName()).append(" as invitation ")
	  	.append(" inner join policy.securityGroup secGroup ")
      .append(" where invitation.securityGroup=secGroup ")
	  	.append(" and invitation.token=:token");
	  if(atDate != null) {
      sb.append(" and (policy.from is null or policy.from<=:date)")
				.append(" and (policy.to is null or policy.to>=:date)");
	  }

	  DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
	  query.setString("token", token);
	  if(atDate != null) {
	  	query.setDate("date", atDate);
	  }
	  
	  Number counter = (Number)query.uniqueResult();
    return counter.intValue() > 0;
	}
	
	/**
	 * @see org.olat.basesecurity.BaseSecurity#findInvitation(org.olat.basesecurity.SecurityGroup)
	 */
	@Override
	public Invitation findInvitation(SecurityGroup secGroup) {
		StringBuilder sb = new StringBuilder();
	  sb.append("select invitation from ").append(InvitationImpl.class.getName()).append(" as invitation ")
	  	.append(" where invitation.securityGroup=:secGroup ");

	  DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
	  query.setEntity("secGroup", secGroup);
	  
	  List<Invitation> invitations = (List<Invitation>)query.list();
	  if(invitations.isEmpty()) return null;
    return invitations.get(0);
	}
	
	/**
	 * @see org.olat.basesecurity.BaseSecurity#findInvitation(java.lang.String)
	 */
	@Override
	public Invitation findInvitation(String token) {
		StringBuilder sb = new StringBuilder();
	  sb.append("select invitation from ").append(InvitationImpl.class.getName()).append(" as invitation ")
	  	.append(" where invitation.token=:token");

	  DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
	  query.setString("token", token);
	  
	  List<Invitation> invitations = (List<Invitation>)query.list();
	  if(invitations.isEmpty()) return null;
    return invitations.get(0);
	}
	
	/**
	 * @see org.olat.basesecurity.BaseSecurity#isIdentityInvited(org.olat.core.id.Identity)
	 */
	@Override
	public boolean isIdentityInvited(Identity identity) {
		StringBuilder sb = new StringBuilder();
	  sb.append("select count(invitation) from ").append(InvitationImpl.class.getName()).append(" as invitation ")
	  	.append("inner join invitation.securityGroup secGroup ")
	  	.append("where secGroup in (")
	  	.append(" select membership.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as membership")
	  	.append("  where membership.identity=:identity")
	  	.append(")");
	  
	  DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
	  query.setEntity("identity", identity);

	  Number invitations = (Number)query.uniqueResult();
    return invitations.intValue() > 0;
	}
	
	/**
	 * @see org.olat.basesecurity.BaseSecurity#isIdentityInvited(org.olat.core.id.Identity)
	 */
	@Override
	public long countInvitations() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(invitation) from ").append(InvitationImpl.class.getName()).append(" as invitation");
	  
		Number invitations = (Number)DBFactory.getInstance().createQuery(sb.toString()).uniqueResult();
		return invitations.longValue();
	}
	
	/**
	 * @see org.olat.basesecurity.BaseSecurity#deleteInvitation(org.olat.basesecurity.Invitation)
	 */
	@Override
	public void deleteInvitation(Invitation invitation) {
		//fxdiff: FXOLAT-251: nothing persisted, nothing to delete
		if(invitation == null || invitation.getKey() == null) return;
		DBFactory.getInstance().deleteObject(invitation);
	}

	/**
	 * @see org.olat.basesecurity.BaseSecurity#cleanUpInvitations()
	 */
	@Override
	public void cleanUpInvitations() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Date currentTime = cal.getTime();
		cal.add(Calendar.HOUR, -6);
		Date dateLimit = cal.getTime();

		StringBuilder sb = new StringBuilder();
	  sb.append("select invitation from ").append(InvitationImpl.class.getName()).append(" as invitation ")
	  	.append(" inner join invitation.securityGroup secGroup ")
      .append(" where invitation.creationDate<:dateLimit")//someone can create an invitation but not add it to a policy within millisecond
	  	.append(" and secGroup not in (")
      //select all valid policies from this security group
      .append("  select policy.securityGroup from ").append(PolicyImpl.class.getName()).append(" as policy ")
      .append("   where (policy.from is null or policy.from<=:currentDate)")
			.append("   and (policy.to is null or policy.to>=:currentDate)")
	  	.append("  )");

	  DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
	  query.setDate("currentDate", currentTime);
	  query.setDate("dateLimit", dateLimit);
	  List<Invitation> oldInvitations = (List<Invitation>)query.list();
	  if(oldInvitations.isEmpty()) {
	  	return;
	  }
	  
	  SecurityGroup olatUserSecGroup = findSecurityGroupByName(Constants.GROUP_OLATUSERS);
	  for(Invitation invitation:oldInvitations) {
	  	List<Identity> identities = getIdentitiesOfSecurityGroup(invitation.getSecurityGroup());
	  	//normally only one identity
	  	for(Identity identity:identities) {
	  		if(identity.getStatus().compareTo(Identity.STATUS_VISIBLE_LIMIT) >= 0) {
	  			//already deleted
	  		} else if(isIdentityInSecurityGroup(identity, olatUserSecGroup)) {
	  			//out of scope
	  		} else {
	  			//delete user
	  			UserDeletionManager.getInstance().deleteIdentity(identity);
	  		}
	  	}
	  	DBFactory.getInstance().deleteObject(invitation);
	  	DBFactory.getInstance().intermediateCommit();
	  }
	}

	/**
	 * @param username the username
	 * @param user the presisted User
	 * @param authusername the username used as authentication credential
	 *          (=username for provider "OLAT")
	 * @param provider the provider of the authentication ("OLAT" or "AAI"). If null, no 
	 * authentication token is generated.
	 * @param credential the credentials or null if not used
	 * @return Identity
	 */
	@Override
	public Identity createAndPersistIdentity(String username, User user, String provider, String authusername, String credential) {
		IdentityImpl iimpl = new IdentityImpl(username, user);
		dbInstance.getCurrentEntityManager().persist(iimpl);
		if (provider != null) { 
			createAndPersistAuthentication(iimpl, provider, authusername, credential, LoginModule.getDefaultHashAlgorithm());
		}
		notifyNewIdentityCreated(iimpl);
		return iimpl;
	}

	/**
	 * @param username The username
	 * @param user The unpresisted User
	 * @param provider The provider of the authentication ("OLAT" or "AAI"). If null, no authentication token is generated.
	 * @param authusername The username used as authentication credential (=username for provider "OLAT")
	 * @return Identity
	 */
	@Override
	public Identity createAndPersistIdentityAndUser(String username, User user, String provider, String authusername) {
		dbInstance.getCurrentEntityManager().persist(user);
		IdentityImpl iimpl = new IdentityImpl(username, user);
		dbInstance.getCurrentEntityManager().persist(iimpl);
		if (provider != null) { 
			createAndPersistAuthentication(iimpl, provider, authusername, null, null);
		}
		notifyNewIdentityCreated(iimpl);
		return iimpl;
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
	public Identity createAndPersistIdentityAndUser(String username, User user, String provider, String authusername, String credential) {
		dbInstance.getCurrentEntityManager().persist(user);
		IdentityImpl iimpl = new IdentityImpl(username, user);
		dbInstance.getCurrentEntityManager().persist(iimpl);
		if (provider != null) { 
			createAndPersistAuthentication(iimpl, provider, authusername, credential, LoginModule.getDefaultHashAlgorithm());
		}
		notifyNewIdentityCreated(iimpl);
		return iimpl;
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
		
		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("secGroups", secGroups)
				.getResultList();
	}
	
	@Override
	//fxdiff: FXOLAT-219 decrease the load for synching groups
	public List<IdentityShort> getIdentitiesShortOfSecurityGroups(List<SecurityGroup> secGroups, int firstResult, int maxResults) {
		if (secGroups == null || secGroups.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select id from ").append(IdentityShort.class.getName()).append(" as id ")
		.append(" where id.key in (")
		.append("   select sgmsi.identity.key from  ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
		.append("   where sgmsi.securityGroup in (:secGroups)")
		.append(" )");

		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setParameterList("secGroups", secGroups);
		List<IdentityShort> idents = query.list();
		return idents;
	}

	/**
	 * @see org.olat.basesecurity.Manager#getIdentitiesAndDateOfSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 */
	public List getIdentitiesAndDateOfSecurityGroup(SecurityGroup secGroup) {
		return getIdentitiesAndDateOfSecurityGroup(secGroup,false);
	}
	
	/**
	 * @see org.olat.basesecurity.Manager#getIdentitiesAndDateOfSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 * @param sortedByAddDate true= return list of idenities sorted by added date
	 */
	public List getIdentitiesAndDateOfSecurityGroup(SecurityGroup secGroup, boolean sortedByAddDate) {
	   StringBuilder queryString = new StringBuilder();
	   queryString.append("select ii, sgmsi.lastModified from"
         + " org.olat.basesecurity.IdentityImpl as ii inner join fetch ii.user as iuser, "
         + " org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi "
         + " where sgmsi.securityGroup = ? and sgmsi.identity = ii");
	   if (sortedByAddDate) {
	  	 queryString.append(" order by sgmsi.lastModified, sgmsi.key");
	   } 
		 List identAndDate = DBFactory.getInstance().find(queryString.toString(),
         new Object[] { secGroup.getKey() },
         new Type[] { StandardBasicTypes.LONG });
     return identAndDate;
	}

	/**
	 * 
	 * @see org.olat.basesecurity.Manager#getSecurityGroupJoinDateForIdentity(org.olat.basesecurity.SecurityGroup, org.olat.core.id.Identity)
	 */
	public Date getSecurityGroupJoinDateForIdentity(SecurityGroup secGroup, Identity identity){
		String query = "select creationDate from " + "  org.olat.basesecurity.SecurityGroupMembershipImpl as sgi "
				+ " where sgi.securityGroup = :secGroup and sgi.identity = :identId";
		
		DB db = DBFactory.getInstance();
		DBQuery dbq = db.createQuery(query);
		dbq.setLong("identId", identity.getKey().longValue());
		dbq.setLong("secGroup", secGroup.getKey());
		List result = dbq.list();
		if (result.size()==0){
			return null;
		}
		else {
			return (Date)result.get(0);
		}
	}
	

	/**
	 * @see org.olat.basesecurity.Manager#countIdentitiesOfSecurityGroup(org.olat.basesecurity.SecurityGroup)
	 */
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
		sb.append("select sgi from ").append(NamedGroupImpl.class.getName()).append(" as ngroup, ")
		  .append(SecurityGroupImpl.class.getName()).append("  as sgi ")
		  .append(" where ngroup.groupName=:groupName and ngroup.securityGroup=sgi");

		List<SecurityGroup> group = this.dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), SecurityGroup.class)
				.setParameter("groupName", securityGroupName)
				.getResultList();

		int size = group.size();
		if (size == 0) return null;
		if (size != 1) throw new AssertException("non unique name in namedgroup: " + securityGroupName);
		SecurityGroup sg = (SecurityGroup) group.get(0);
		return sg;
	}

	/**
	 * @see org.olat.basesecurity.Manager#findIdentityByName(java.lang.String)
	 */
	@Override
	public Identity findIdentityByName(String identityName) {
		if (identityName == null) throw new AssertException("findIdentitybyName: name was null");

		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident where ident.name=:username");
		
		List<Identity> identities = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", identityName)
				.getResultList();
		
		if(identities.isEmpty()) {
			return null;
		}
		return identities.get(0);
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
		sb.append("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
			.append(" inner join identity.user user ")
			.append(" where user.properties['").append(UserConstants.INSTITUTIONALUSERIDENTIFIER).append("'] in (:idNumbers) ");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("idNumbers", identityNumbers)
				.getResultList();
	}

	@Override
	public List<Identity> findIdentitiesByName(Collection<String> identityNames) {
		if (identityNames == null || identityNames.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident where ident.name in (:username)");
		
		List<Identity> identities = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", identityNames)
				.getResultList();
		return identities;
	}

	@Override
	public Identity findIdentityByUser(User user) {
		if (user == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident where ident.user.key=:userKey");
		
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
		sb.append("select ident from ").append(IdentityShort.class.getName()).append(" as ident where ident.name in (:names)");

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
		sb.append("select ident from ").append(IdentityShort.class.getName()).append(" as ident where ident.key in (:keys)");
		
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

	/**
	 * 
	 * @see org.olat.basesecurity.Manager#loadIdentityByKey(java.lang.Long)
	 */
	public Identity loadIdentityByKey(Long identityKey) {
		if (identityKey == null) throw new AssertException("findIdentitybyKey: key is null");
		if(identityKey.equals(Long.valueOf(0))) return null;
		return (Identity) DBFactory.getInstance().loadObject(IdentityImpl.class, identityKey);
	}

	@Override
	public List<Identity> loadIdentityByKeys(Collection<Long> identityKeys) {
		if (identityKeys == null || identityKeys.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(Identity.class.getName()).append(" as ident where ident.key in (:keys)");
		
		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("keys", identityKeys)
				.getResultList();
	}

	/**
	 * @see org.olat.basesecurity.Manager#loadIdentityByKey(java.lang.Long,boolean)
	 */
	public Identity loadIdentityByKey(Long identityKey, boolean strict) {
		if(strict) return loadIdentityByKey(identityKey);
		
		String queryStr = "select ident from org.olat.basesecurity.IdentityImpl as ident where ident.key=:identityKey";
		DBQuery dbq = DBFactory.getInstance().createQuery(queryStr);
		dbq.setLong("identityKey", identityKey);
		List<Identity> identities = dbq.list();
		if (identities.size() == 1) return identities.get(0);
		return null;
	}

	@Override
	public IdentityShort loadIdentityShortByKey(Long identityKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select identity from ").append(IdentityShort.class.getName()).append(" as identity ")
			.append(" where identity.key=:identityKey");
		
		List<IdentityShort> idents = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class)
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
		sb.append("select ident from ").append(IdentityShort.class.getName()).append(" as ident where ident.key in (:keys)");
		
		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class)
				.setParameter("keys", identityKeys)
				.getResultList();
	}
	
	public List<Identity> loadIdentities(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident order by ident.key");
		
		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
		
	}

	/**
	 * 
	 * @see org.olat.basesecurity.Manager#countUniqueUserLoginsSince(java.util.Date)
	 */
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
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resourceable, new SyncerCallback<Authentication>(){
			public Authentication execute() {
				Authentication auth = findAuthentication(ident, provider);
				if(auth == null) {
					if(algorithm != null && credentials != null) {
						String salt = algorithm.isSalted() ? Encoder.getSalt() : null;
						String hash = Encoder.encrypt(credentials, salt, algorithm);
						auth = new AuthenticationImpl(ident, provider, authUserName, hash, salt, algorithm.name());
					} else {
						auth = new AuthenticationImpl(ident, provider, authUserName, credentials);
					}
					dbInstance.getCurrentEntityManager().persist(auth);
				}
				return auth;
			}
		});
	}

	/**
	 * @see org.olat.basesecurity.Manager#findAuthentication(org.olat.core.id.Identity, java.lang.String)
	 */
	@Override
	public Authentication findAuthentication(Identity identity, String provider) {
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
		if (results.size() > 1) throw new AssertException("Found more than one Authentication for a given subject and a given provider.");
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
		return dbInstance.getCurrentEntityManager().merge(authentication);
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
			AuthenticationImpl authRef = dbInstance.getCurrentEntityManager()
					.getReference(AuthenticationImpl.class, auth.getKey());
			dbInstance.getCurrentEntityManager().remove(authRef);
		} catch (EntityNotFoundException e) {
			logError("", e);
		}
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
		StringBuilder sb = new StringBuilder();
		if (hasAuthProviders) {
			// I know, it looks wrong but I need to do the join reversed since it is not possible to 
			// do this query with a left join that starts with the identity using hibernate HQL. A left
			// or right join is necessary since it is totally ok to have null values as authentication
			// providers (e.g. when searching for users that do not have any authentication providers at all!).
			// It took my quite a while to make this work, so think twice before you change anything here!
			if(count) {
				sb = new StringBuilder("select count(distinct ident.key) from org.olat.basesecurity.AuthenticationImpl as auth right join auth.identity as ident ");			
			} else {
				sb = new StringBuilder("select distinct ident from org.olat.basesecurity.AuthenticationImpl as auth right join auth.identity as ident ");			
			}
		} else {
			if(count) {
				sb = new StringBuilder("select count(distinct ident.key) from org.olat.core.id.Identity as ident ");
			} else {
				sb = new StringBuilder("select distinct ident from org.olat.core.id.Identity as ident ");
			}
		}
		// In any case join with the user. Don't join-fetch user, this breaks the query
		// because of the user fields (don't know exactly why this behaves like
		// this)
		sb.append(" join ident.user as user ");
		
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
		
		// complex where clause only when values are available
		if (login != null || (userproperties != null && !userproperties.isEmpty())
				|| (identityKeys != null && !identityKeys.isEmpty()) || createdAfter != null	|| createdBefore != null
				|| hasAuthProviders || hasGroups || hasPermissionOnResources || status != null) {
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
					needsUserPropertiesJoin = checkIntersectionInUserProperties(sb,needsUserPropertiesJoin, params.isUserPropertiesAsIntersectionSearch());
					boolean moreThanOne = emailProperties.size() > 1;
					if (moreThanOne) sb.append("(");
					boolean needsOr = false;
					for (String key : emailProperties.keySet()) {
						if (needsOr) sb.append(" or ");
						//fxdiff
						if(dbVendor.equals("mysql")) {
							sb.append(" user.properties['").append(key).append("'] like :").append(key).append("_value ");
						} else {
							sb.append(" lower(user.properties['").append(key).append("']) like :").append(key).append("_value ");
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
					needsUserPropertiesJoin = checkIntersectionInUserProperties(sb,needsUserPropertiesJoin, params.isUserPropertiesAsIntersectionSearch());
					if(dbVendor.equals("mysql")) {
						sb.append(" user.properties['").append(key).append("'] like :").append(key).append("_value ");
					} else {
						sb.append(" lower(user.properties['").append(key).append("']) like :").append(key).append("_value ");
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
		DB db = DBFactory.getInstance();
		DBQuery dbq = db.createQuery(query);
		
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

  /**
   * @see org.olat.basesecurity.Manager#isIdentityVisible(java.lang.String)
   */
	public boolean isIdentityVisible(String identityName) {
		if (identityName == null) throw new AssertException("findIdentitybyName: name was null");
		String queryString = "select count(ident) from org.olat.basesecurity.IdentityImpl as ident where ident.name = :identityName and ident.status < :status";
		DBQuery dbq = DBFactory.getInstance().createQuery(queryString);
		dbq.setString("identityName", identityName);
		dbq.setInteger("status", Identity.STATUS_VISIBLE_LIMIT);
		List res = dbq.list();
		Long cntL = (Long) res.get(0);
		return (cntL.longValue() > 0);
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

	/**
	 * @see org.olat.basesecurity.Manager#saveIdentityStatus(org.olat.core.id.Identity)
	 */
	@Override
	public Identity saveIdentityStatus(Identity identity, Integer status) {
		Identity reloadedIdentity = loadForUpdate(identity); 
		reloadedIdentity.setStatus(status);
		reloadedIdentity = dbInstance.getCurrentEntityManager().merge(reloadedIdentity);
		dbInstance.commit();
		return reloadedIdentity;
	}
	
	@Override
	public Identity setIdentityLastLogin(Identity identity) {
		Identity reloadedIdentity = loadForUpdate(identity); 
		reloadedIdentity.setLastLogin(new Date());
		reloadedIdentity = dbInstance.getCurrentEntityManager().merge(reloadedIdentity);
		dbInstance.commit();
		return reloadedIdentity;
	}
	
	@Override
	public Identity saveIdentityName(Identity identity, String newName) {
		Identity reloadedIdentity = loadForUpdate(identity); 
		reloadedIdentity.setName(newName);
		reloadedIdentity = dbInstance.getCurrentEntityManager().merge(reloadedIdentity);
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
			guestIdentity = createAndPersistIdentityAndUser(guestUsername, guestUser, null, null, null);
			SecurityGroup anonymousGroup = findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
			addIdentityToSecurityGroup(guestIdentity, anonymousGroup);
		} else if (!guestIdentity.getUser().getProperty(UserConstants.FIRSTNAME, locale).equals(trans.translate("user.guest"))) {
			//Check if guest name has been updated in the i18n tool
			guestIdentity.getUser().setProperty(UserConstants.FIRSTNAME, trans.translate("user.guest"));
			guestIdentity = dbInstance.getCurrentEntityManager().merge(guestIdentity);
		}
		return guestIdentity;
	}
}