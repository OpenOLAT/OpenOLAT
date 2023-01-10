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

package org.olat.ldap.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.MailHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.ldap.LDAPConstants;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPEvent;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.LDAPSyncConfiguration;
import org.olat.ldap.model.LDAPGroup;
import org.olat.ldap.model.LDAPOrganisationGroup;
import org.olat.ldap.model.LDAPUser;
import org.olat.ldap.model.LDAPValidationResult;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.auth.AuthenticationProviderSPI;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.ValidationResult;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description: This manager handles  communication between LDAP and OLAT.
 * The synching is done only on node 1 of a cluster.
 * 
 * @author Maurus Rohrer
 */
@Service("org.olat.ldap.LDAPLoginManager")
public class LDAPLoginManagerImpl implements LDAPLoginManager, AuthenticationProviderSPI, GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(LDAPLoginManagerImpl.class);

	private static final String MULTI_VALUES_SEPARATOR = ",";// same as GenericSelectionPropertyHandler.KEY_DELIMITER
	private static final String TIMEOUT_KEY = "com.sun.jndi.ldap.connect.timeout";
	protected static final String LOST_AND_FOUND_ORGANISATION = "Lost and found";
	
	private static boolean batchSyncIsRunning = false;
	private static Date lastSyncDate = null; // first sync is always a full sync
	
	
	private Coordinator coordinator;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LDAPDAO ldapDao;
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private LDAPSyncConfiguration syncConfiguration;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;

	@Autowired
	public LDAPLoginManagerImpl(CoordinatorManager coordinatorManager) {
		this.coordinator = coordinatorManager.getCoordinator();
		coordinator.getEventBus().registerFor(this, null, ldapSyncLockOres);
		FrameworkStartupEventChannel.registerForStartupEvent(this);
	}

	@Override
	public List<String> getProviderNames() {
		return Collections.singletonList("LDAP");
	}

	@Override
	public boolean canChangeAuthenticationUsername(String provider) {
		return "LDAP".equals(provider);
	}

	@Override
	public boolean changeAuthenticationUsername(Authentication authentication, String newUsername) {
		authentication.setAuthusername(newUsername);
		authentication = authenticationDao.updateAuthentication(authentication);
		return authentication != null;
	}

	@Override
	public ValidationResult validateAuthenticationUsername(String name, Identity identity) {

		LdapContext ctx = bindSystem();
		if(ctx != null) {
			String userDN = ldapDao.searchUserForLogin(name, ctx);
			if(userDN == null) {
				userDN = ldapDao.searchUserDNByUid(name, ctx);
			}
			if(StringHelper.containsNonWhitespace(userDN)) {
				Authentication currentAuth = authenticationDao.getAuthentication(name, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
				if(currentAuth == null || currentAuth.getIdentity().equals(identity)) {
					return LDAPValidationResult.allOk();
				}
				return LDAPValidationResult.error("error.user.already.in.use");
			}
			return LDAPValidationResult.error("error.user.not.found");
		}
		return LDAPValidationResult.error("delete.error.connection");
	}

	@Override
	public void event(Event event) {
		if(event instanceof LDAPEvent) {
			LDAPEvent ldape = (LDAPEvent)event;
			if(LDAPEvent.SYNCHING.equals(event.getCommand())) {
				if(!ldape.isEventOnThisNode()) {
					batchSyncIsRunning = true;
				}
			} else if(LDAPEvent.SYNCHING_ENDED.equals(event.getCommand())) {
				if(!ldape.isEventOnThisNode()) {
					batchSyncIsRunning = false;
					lastSyncDate = ((LDAPEvent)event).getTimestamp();
				}
			} else if(LDAPEvent.DO_SYNCHING.equals(event.getCommand())) {
				doHandleBatchSync();
			}
		} else if(event instanceof FrameworkStartedEvent) {
			try {
				init();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	private void init() {
		if(ldapLoginModule.isLDAPEnabled()) {
			if (bindSystem() == null) {
				// don't disable ldap, maybe just a temporary problem, but still report
				// problem in logfile
				log.error("LDAP connection test failed during module initialization, edit config or contact network administrator");
			} else {
				log.info("LDAP login is enabled");
				
				// Start LDAP cron sync job
				if (ldapLoginModule.isLdapSyncCronSync()) {
					doHandleBatchSync();
				} else {
					log.info("LDAP cron sync is disabled");
				}
			}
		}
	}
	
	private void doHandleBatchSync() {
		try {
			scheduler.triggerJob(new JobKey("LDAP_Cron_Syncer_Job", Scheduler.DEFAULT_GROUP));
		} catch (SchedulerException e) {
			log.error("Cannot intterupt the reservation job.", e);
		}
	}

	/**
	 * Connect to the LDAP server with System DN and Password
	 * 
	 * Configuration: LDAP URL = ldapContext.xml (property=ldapURL) System DN =
	 * ldapContext.xml (property=ldapSystemDN) System PW = ldapContext.xml
	 * (property=ldapSystemPW)
	 * 
	 * @return The LDAP connection (LdapContext) or NULL if connect fails
	 * 
	 * @throws NamingException
	 */
	@Override
	public LdapContext bindSystem() {
		// set LDAP connection attributes
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapLoginModule.getLdapUrl());
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ldapLoginModule.getLdapSystemDN());
		env.put(Context.SECURITY_CREDENTIALS, ldapLoginModule.getLdapSystemPW());
		if(ldapLoginModule.getLdapConnectionTimeout() != null) {
			env.put(TIMEOUT_KEY, ldapLoginModule.getLdapConnectionTimeout().toString());
		}

		// check ssl
		if (ldapLoginModule.isSslEnabled()) {
			enableSSL(env);
		}

		try {
			InitialLdapContext ctx = new InitialLdapContext(env, new Control[]{});
			ctx.getConnectControls();
			return ctx;
		} catch (NamingException e) {
			log.error("NamingException when trying to bind system with DN::{} and PW::{} on URL::{}",
					ldapLoginModule.getLdapSystemDN(), ldapLoginModule.getLdapSystemPW(), ldapLoginModule.getLdapUrl(), e);
			return null;
		} catch (Exception e) {
			log.error("Exception when trying to bind system with DN::{} and PW::{} on URL::{}",
					 ldapLoginModule.getLdapSystemDN(), ldapLoginModule.getLdapSystemPW(), ldapLoginModule.getLdapUrl(), e);
			return null;
		}
	}

	/**
	 * 
	 * Connect to LDAP with the User-Name and Password given as parameters
	 * 
	 * Configuration: LDAP URL = ldapContext.xml (property=ldapURL) LDAP Base =
	 * ldapContext.xml (property=ldapBase) LDAP Attributes Map =
	 * ldapContext.xml (property=userAttrs)
	 * 
	 * 
	 * @param uid The users LDAP login name (can't be null)
	 * @param pwd The users LDAP password (can't be null)
	 * 
	 * @return After successful bind Attributes otherwise NULL
	 * 
	 * @throws NamingException
	 */
	@Override
	public Attributes bindUser(String login, String pwd, LDAPError errors) {
		// get user name, password and attributes
		String ldapUrl = ldapLoginModule.getLdapUrl();
		String[] userAttr = syncConfiguration.getUserAttributes();

		if (login == null || pwd == null) {
			log.debug("Error when trying to bind user, missing username or password. Username::{} pwd::{}", login, pwd);
			errors.insert("Username and password must be selected");
			return null;
		}
		
		dbInstance.commit();
		LdapContext ctx = bindSystem();
		if (ctx == null) {
			errors.insert("LDAP connection error");
			return null;
		}
		String userDN = ldapDao.searchUserForLogin(login, ctx);
		if (userDN == null) {
			log.info("Error when trying to bind user with username::{} - user not found on LDAP server {}"
					, login, (ldapLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin() ? ", trying with OLAT login provider" : ""));
			errors.insert("Username or password incorrect");
			return null;
		}
		
		// Ok, so far so good, user exists. Now try to fetch attributes using the
		// users credentials
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, userDN);
		env.put(Context.SECURITY_CREDENTIALS, pwd);
		if(ldapLoginModule.getLdapConnectionTimeout() != null) {
			env.put(TIMEOUT_KEY, ldapLoginModule.getLdapConnectionTimeout().toString());
		}
		if(ldapLoginModule.isSslEnabled()) {
			enableSSL(env);
		}

		try {
			dbInstance.commit();
			Control[] connectCtls = new Control[]{};
			LdapContext userBind = new InitialLdapContext(env, connectCtls);
			Attributes attributes = userBind.getAttributes(userDN, userAttr);
			userBind.close();
			return attributes;
		} catch (AuthenticationException e) {
			log.info("Error when trying to bind user with username::{} - invalid LDAP password", login);
			errors.insert("Username or password incorrect");
			return null;
		} catch (NamingException e) {
			log.error("NamingException when trying to get attributes after binding user with username::{}", login, e);
			errors.insert("Username or password incorrect");
			return null;
		}
	}
	
	@Override
	public Identity authenticate(String username, String pwd, LDAPError ldapError) {
		long start = System.nanoTime();
		//authenticate against LDAP server
		Attributes attrs = bindUser(username, pwd, ldapError);
		long takes = System.nanoTime() - start;
		if(takes > LDAPLoginModule.WARNING_LIMIT) {
			log.warn("LDAP Authentication takes (ms): ({})", (takes / 1000000));
		}
		
		if (ldapError.isEmpty() && attrs != null) { 
			Authentication auth = findAuthenticationByLdapAuthentication(attrs, ldapError);
			if (!ldapError.isEmpty()) {
				return null;
			}
			
			Identity identity = null;
			if (auth == null) {
				if(ldapLoginModule.isCreateUsersOnLogin()) {
					// User authenticated but not yet existing - create as new OLAT user
					identity = createAndPersistUser(attrs);
					auth = findAuthenticationByLdapAuthentication(attrs, ldapError);
				} else {
					ldapError.insert("login.notauthenticated");
				}
			} else {
				// User does already exist - just sync attributes
				identity = auth.getIdentity();
				Map<String, String> olatProToSync = prepareUserPropertyForSync(attrs, identity);
				if (olatProToSync != null) {
					identity = syncUser(olatProToSync, identity);
				}
			}
			// Add or update an OLAT authentication token for this user if configured in the module
			if (identity != null && auth != null) {
				if(ldapLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()) {
					// there is no WEBDAV token but an HA1, the HA1 is linked to the OLAT one.
					CoreSpringFactory.getImpl(OLATAuthManager.class)
						.synchronizeOlatPasswordAndUsername(identity, identity, auth.getAuthusername(), pwd);
				}
				
				if(syncConfiguration.syncGroupWithLDAPGroup()
						|| syncConfiguration.syncGroupWithAttribute()
						|| syncConfiguration.syncOrganisationWithLDAPGroup()) {
					syncUser(identity, auth);
				}
			}
			return identity;
		} 
		return null;
	}
	
	/**
	 * Change the password on the LDAP server.
	 */
	@Override
	public boolean changePassword(Authentication auth, String pwd, LDAPError errors) {
		String uid = auth.getAuthusername();
	
		String ldapUserPasswordAttribute = syncConfiguration.getLdapUserPasswordAttribute();
		try {
			LdapContext ctx = bindSystem();
			String dn = ldapDao.searchUserDNByUid(uid, ctx);
			if(dn == null) {
				dn = ldapDao.searchUserForLogin(uid, ctx);
			}

			List<ModificationItem> modificationItemList = new ArrayList<>();
			if(ldapLoginModule.isActiveDirectory()) {
				boolean resetLockoutTime = false;
				if(ldapLoginModule.isResetLockTimoutOnPasswordChange()) {
					String[] attrs = syncConfiguration.getUserAttributes();
					List<String> attrList = new ArrayList<>(Arrays.asList(attrs));
					attrList.add("lockoutTime");
					attrs = attrList.toArray(new String[attrList.size()]);
					Attributes attributes = ctx.getAttributes(dn, attrs);
					Attribute lockoutTimeAttr = attributes.get("lockoutTime");
					if(lockoutTimeAttr != null && lockoutTimeAttr.size() > 0) {
						Object lockoutTime = lockoutTimeAttr.get();
						if(lockoutTime != null && !lockoutTime.equals("0")) {
							resetLockoutTime = true;
						}
					}
				}

				//active directory need the password enquoted and unicoded (but little-endian)
				String quotedPassword = "\"" + pwd + "\"";
				char[] unicodePwd = quotedPassword.toCharArray();
				byte[] pwdArray = new byte[unicodePwd.length * 2];
				for (int i=0; i<unicodePwd.length; i++) {
					pwdArray[i*2 + 1] = (byte) (unicodePwd[i] >>> 8);
					pwdArray[i*2 + 0] = (byte) (unicodePwd[i] & 0xff);
				}
				BasicAttribute userPasswordAttribute = new BasicAttribute(ldapUserPasswordAttribute, pwdArray );
				modificationItemList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPasswordAttribute));
				if(resetLockoutTime) {
					BasicAttribute lockTimeoutAttribute = new BasicAttribute("lockoutTime", "0");
					modificationItemList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, lockTimeoutAttribute));
				}
			} else {
				BasicAttribute userPasswordAttribute = new BasicAttribute(ldapUserPasswordAttribute, pwd);
				modificationItemList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPasswordAttribute));
			}

			ModificationItem[] modificationItems = modificationItemList.toArray(new ModificationItem[modificationItemList.size()]);
			ctx.modifyAttributes(dn, modificationItems);
			ctx.close();
			return true;
		} catch (NamingException e) {
			log.error("NamingException when trying to change password with username::{}", uid, e);
			errors.insert("Cannot change the password");
			return false;
		} catch(Exception e) {
			log.error("Unexpected exception when trying to change password with username::{}", uid, e);
			errors.insert("Cannot change the password");
			return false;
		}
	}

	/**
	 * Delete all Identities in List and removes them from LDAPSecurityGroup
	 * 
	 * @param identityList List of Identities to delete
	 */
	@Override
	public void deleteIdentities(List<Identity> identityList, Identity doer) {
		for (Identity identity:  identityList) {
			if(Identity.STATUS_PERMANENT.equals(identity.getStatus())) {
				log.info(Tracing.M_AUDIT, "{} was not deleted because is status is permanent.", identity.getKey());
				continue;
			}
			
			userLifecycleManager.deleteIdentity(identity, doer);
		}
	}
	
	@Override
	public void inactivateIdentities(List<Identity> identityList, Identity doer) {
		for (Identity identity:  identityList) {
			if(Identity.STATUS_PERMANENT.equals(identity.getStatus())) {
				log.info(Tracing.M_AUDIT, "{} was not deactivated because is status is permanent.", identity.getKey());
			} else if(Identity.STATUS_INACTIVE.equals(identity.getStatus())) {
				// already inactive
			} else {
				securityManager.saveIdentityStatus(identity, Identity.STATUS_INACTIVE, doer);
			}
		}
	}

	/**
	 * Sync all OLATPropertys in Map of Identity
	 * 
	 * @param olatPropertyMap Map of changed OLAT properties
	 *          (OLATProperty,LDAPValue)
	 * @param identity Identity to sync
	 */
	private Identity syncUser(Map<String, String> olatPropertyMap, IdentityRef identityRef) {
		if (identityRef == null) {
			log.warn("Identiy is null - should not happen");
			return null;
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityRef.getKey());
		User user = identity.getUser();
		// remove user identifyer - can not be changed later
		olatPropertyMap.remove(LDAPConstants.LDAP_USER_IDENTIFYER);
		// remove attributes that are defined as sync-only-on-create
		Set<String> syncOnlyOnCreateProperties = syncConfiguration.getSyncOnlyOnCreateProperties();
		if (syncOnlyOnCreateProperties != null) {
			for (String syncOnlyOnCreateKey : syncOnlyOnCreateProperties) {
				olatPropertyMap.remove(syncOnlyOnCreateKey);
			}			
		}

		for(Map.Entry<String, String> keyValuePair : olatPropertyMap.entrySet()) {
			String propName = keyValuePair.getKey();
			String value = keyValuePair.getValue();
			if(value == null) {
				if(user.getProperty(propName, null) != null) {
					log.debug("removed property {} for identity {}", propName, identity);
					user.setProperty(propName, value);
				}
			} else {
				if(value.length() > 255) {
					value = value.substring(0, 255);
				}
				user.setProperty(propName, value);
			}
		}

		// Add static user properties from the configuration
		Map<String, String> staticProperties = syncConfiguration.getStaticUserProperties();
		if (staticProperties != null && staticProperties.size() > 0) {
			for (Map.Entry<String, String> staticProperty : staticProperties.entrySet()) {
				user.setProperty(staticProperty.getKey(), staticProperty.getValue());
			}
		}
		userManager.updateUser(identityRef, user);
		dbInstance.commit();
		
		// check WebDAV authentication
		CoreSpringFactory.getImpl(OLATAuthManager.class).synchronizeCredentials(identity, identity);
		
		// Check that the user is at least in 1 organisation
		if(!syncConfiguration.syncOrganisationWithLDAPGroup()) {
			List<Organisation> organisationAsUsers = organisationService.getOrganisations(identity, OrganisationRoles.user);
			if(organisationAsUsers.isEmpty()) {
				organisationService.addMember(identity, OrganisationRoles.user);
			}
		}
		return identity;
	}

	@Override
	public Identity createAndPersistUser(String uid) {
		String ldapUserIDAttribute = syncConfiguration.getOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
		String filter = ldapDao.buildSearchUserFilter(ldapUserIDAttribute, uid);
		LdapContext ctx = bindSystem();
		String userDN = ldapDao.searchUserDNByUid(uid, ctx);
		log.info("create and persist user identifier by userDN: {} with filter: {}", userDN, filter);
		LDAPUserVisitor visitor = new LDAPUserVisitor(syncConfiguration);	
		ldapDao.search(visitor, userDN, filter, syncConfiguration.getUserAttributes(), ctx);

		Identity newIdentity = null;
		List<LDAPUser> ldapUser = visitor.getLdapUserList();
		if(ldapUser != null && !ldapUser.isEmpty()) {
			Attributes userAttributes = ldapUser.get(0).getAttributes();
			newIdentity = createAndPersistUser(userAttributes);
		}
		return newIdentity;
	}

	/**
	 * Creates User in OLAT and ads user to LDAP securityGroup Required Attributes
	 * have to be checked before this method.
	 * 
	 * @param userAttributes Set of LDAP Attribute of User to be created
	 */
	@Override
	public Identity createAndPersistUser(Attributes userAttributes) {
		// Get and Check Config
		List<String> reqAttrs = syncConfiguration.checkRequestAttributes(userAttributes, userModule.isEmailMandatory());
		if (!reqAttrs.isEmpty()) {
			log.warn("Can not create and persist user, the following attributes are missing::{}", reqAttrs);
			return null;
		}
		
		String uid = getSingleAttributeValue(userAttributes.get(syncConfiguration
				.getOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER)));
		String email = getSingleAttributeValue(userAttributes.get(syncConfiguration.getOlatPropertyToLdapAttribute(UserConstants.EMAIL)));
		// Lookup user
		if (securityManager.findIdentityByLogin(uid) != null) {
			log.warn("Can't create user with username='{}', this username does already exist in the database", uid);
			return null;
		}
		if(!securityModule.isIdentityNameAutoGenerated() && securityManager.findIdentityByName(uid) != null) {
			log.warn("Can't create user with username='{}', this identity name does already exist in the database", uid);
			return null;
		}
		if(StringHelper.containsNonWhitespace(email) || userModule.isEmailMandatory()) {
			if (!MailHelper.isValidEmailAddress(email)) {
				// needed to prevent possibly an AssertException in findIdentityByEmail breaking the sync!
				log.error("Cannot try to lookup user {} by email with an invalid email::{}", uid, email);
				return null;
			}
			if (!userManager.isEmailAllowed(email)) {
				log.warn("Can't create user {} with email='{}', a user with that email does already exist in the database", uid, email);
				return null;
			}
		}
		
		// Create User (first and lastname is added in next step)
		User user = userManager.createUser(null, null, email);
		// Set User Property's (Iterates over Attributes and gets OLAT Property out
		// of olatexconfig.xml)
		NamingEnumeration<? extends Attribute> neAttr = userAttributes.getAll();
		try {
			while (neAttr.hasMore()) {
				Attribute attr = neAttr.next();
				String olatProperty = mapLdapAttributeToOlatProperty(attr.getID());
				if (!attr.getID().equalsIgnoreCase(syncConfiguration.getOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER)) ) {
					String ldapValue = getAttributeValue(attr);
					if (olatProperty == null || ldapValue == null) {
						continue;
					} else if(ldapValue != null && ldapValue.length() > 250) {
						ldapValue = ldapValue.substring(0, 250);
					}
					user.setProperty(olatProperty, ldapValue);
				} 
			}
			// Add static user properties from the configuration
			Map<String, String> staticProperties = syncConfiguration.getStaticUserProperties();
			if (staticProperties != null && staticProperties.size() > 0) {
				for (Entry<String, String> staticProperty : staticProperties.entrySet()) {
					user.setProperty(staticProperty.getKey(), staticProperty.getValue());
				}
			}
		} catch (NamingException e) {
			log.error("NamingException when trying to create and persist LDAP user with username::{}", uid, e);
			return null;
		} catch (Exception e) {
			// catch any exception here to properly log error
			log.error("Unknown exception when trying to create and persist LDAP user with username::{}", uid, e);
			return null;
		}

		// Create Identity and add it to the default organization
		String identityName = securityModule.isIdentityNameAutoGenerated() ? null : uid;
		Identity identity;
		if(syncConfiguration.syncOrganisationWithLDAPGroup()) {
			// Organization will be added at a later stage
			identity = securityManager.createAndPersistIdentityAndUser(identityName, uid, null, user,
					LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER, uid, null, null);
		} else {
			identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(identityName, uid, null, user,
					LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER, uid, null, null, null);
		}
		log.info("Created LDAP user username::{}", uid);
		return identity;
	}

	/**
	 * Checks if LDAP properties are different then OLAT properties of a User. If
	 * they are different a Map (OlatPropertyName,LDAPValue) is returned.
	 * 
	 * @param attributes Set of LDAP Attribute of Identity
	 * @param identity Identity to compare
	 * 
	 * @return Map(OlatPropertyName,LDAPValue) of properties Identity, where
	 *         property has changed. NULL is returned it no attributes have to be synced
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> prepareUserPropertyForSync(Attributes attributes, Identity identity) {
		Map<String, String> olatPropertyMap = new HashMap<>();
		User user = identity.getUser();
		NamingEnumeration<Attribute> neAttrs = (NamingEnumeration<Attribute>) attributes.getAll();
		try {
			while (neAttrs.hasMore()) {
				Attribute attr = neAttrs.next();
				
				String olatProperty = mapLdapAttributeToOlatProperty(attr.getID());
				if(olatProperty == null) {
					continue;
				}
				String ldapValue = getAttributeValue(attr);
				String olatValue = user.getProperty(olatProperty, null);
				if (olatValue == null) {
					// new property or user ID (will always be null, pseudo property)
					olatPropertyMap.put(olatProperty, ldapValue);
				} else {
					if (ldapValue.compareTo(olatValue) != 0) {
						olatPropertyMap.put(olatProperty, ldapValue);
					}
				}
			}
			if (olatPropertyMap.size() == 1 && olatPropertyMap.get(LDAPConstants.LDAP_USER_IDENTIFYER) != null) {
				log.debug("propertymap for identity {} contains only userID, NOTHING TO SYNC!", identity.getKey());
				return null;
			} else {
				log.debug("propertymap for identity {} contains {} items ({}) to be synced later on",
						identity.getKey(), olatPropertyMap.size(), olatPropertyMap.keySet());
				return olatPropertyMap;
			}

		} catch (NamingException e) {
			log.error("NamingException when trying to prepare user properties for LDAP sync", e);
			return null;
		}
	}
	
	/**
	 * Maps LDAP Attributes to the OLAT Property 
	 * 
	 * Configuration: LDAP Attributes Map = ldapContext.xml (property=userAttrs)
	 * 
	 * @param attrID LDAP Attribute
	 * @return OLAT Property
	 */
	private String mapLdapAttributeToOlatProperty(String attrID) {
		Map<String, String> userAttrMapper = syncConfiguration.getUserAttributeMap();
		return userAttrMapper.get(attrID);
	}
	
	/**
	 * Extracts Value out of LDAP Attribute
	 * 
	 * 
	 * @param attribute LDAP Naming Attribute 
	 * @return String value of Attribute, null on Exception
	 * 
	 * @throws NamingException
	 */
	private String getSingleAttributeValue(Attribute attribute) {
		if(attribute == null) return null;
		
		try {
			return (String)attribute.get();
		} catch (NamingException e) {
			log.error("NamingException when trying to get attribute value for attribute::{}", attribute, e);
			return null;
		}
	}
	
	private String getAttributeValue(Attribute attribute) {
		if(attribute == null) return null;

		try {
			int valueSize = attribute.size();
			if(valueSize == 0) {
				return null;
			}
			if(valueSize == 1) {
				return (String)attribute.get();
			}
			
			StringBuilder sb = new StringBuilder(64);
			for(NamingEnumeration<?> values=attribute.getAll(); values.hasMore(); ) {
				if(sb.length() > 0) sb.append(MULTI_VALUES_SEPARATOR);
				
				Object val = values.next();
				if(val != null) {
					sb.append(val.toString());
				}
			}

			return sb.toString();
		} catch (NamingException e) {
			log.error("NamingException when trying to get attribute value for attribute::{}", attribute, e);
			return null;
		}
	}

	/**
	 * The method search in LDAP the user, search the groups
	 * of which it is member of, and sync the groups. The method doesn't
	 * work if the login attribute is not the same as the user identifier.
	 * 
	 * @param identity The identity to sync
	 */
	@Override
	public void syncUser(Identity identity) {	
		// This doesn't work if the login attribute is not the same as the user identifier.
		Authentication authentication = authenticationDao.getAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		if(authentication == null) {
			return; // not an LDAP user, nothing to do
		}
		syncUser(identity, authentication);
	}
	
	private void syncUser(Identity identity, Authentication authentication) {
		LdapContext ctx = null;
		try {
			ctx = bindSystem();
			if (ctx == null) {
				log.error("could not bind to ldap");
				return;
			}

			LDAPUser lUser = ldapDao.searchLDAPUserByUid(authentication.getAuthusername(), ctx);
			if(lUser != null) {
				LDAPError errors = new LDAPError();
				searchUserRolesInGroups(ctx, lUser, errors);
				if(syncConfiguration.syncGroupWithLDAPGroup() || syncConfiguration.syncGroupWithAttribute()) {
					syncUserBusinessGroups(ctx, identity, lUser);
				}
				
				if(syncConfiguration.syncOrganisationWithLDAPGroup()) {
					syncUserOrganisations(ctx, identity, lUser);
				} else {
					//Synchronize role on the default organisation
					syncUserDefaultOrganisations(identity, lUser);
				}
			}
		} catch (NamingException e) {
			log.error("", e);
		} finally {
			closeContext(ctx);
		}
	}
	
	private void syncUserDefaultOrganisations(Identity identity, LDAPUser ldapUser)
	throws NamingException {
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		List<OrganisationRoles> synchronizedRoles = syncConfiguration.getSynchronizedRoles();
		syncRole(synchronizedRoles, true, identity, defaultOrganisation, ldapUser.getRoles());
	}

	private void syncUserOrganisations(LdapContext ctx, Identity identity, LDAPUser ldapUser)
	throws NamingException {
		String userDn = ldapUser.getDn();
		List<String> groupBases = syncConfiguration.getLdapOrganisationsGroupBases();
		String groupFilter = "(&" + syncConfiguration.getLdapOrganisationsGroupFilter() + "(member=" + userDn + "))";
		List<LDAPGroup> groups = ldapDao.searchGroups(ctx, groupBases, groupFilter);
		
		List<OrganisationRoles> synchronizedRoles = syncConfiguration.getSynchronizedRoles();

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		List<Organisation> currentUserOrganisations = organisationDao.getManagedOrganisations(identity, defOrganisation);
		for(LDAPGroup group:groups) {
			String groupCommonName = group.getCommonName();
			
			Organisation organisation = currentUserOrganisations.stream()
				.filter(org -> groupCommonName.equalsIgnoreCase(org.getExternalId())
						|| groupCommonName.equalsIgnoreCase(org.getDisplayName())
						|| groupCommonName.equalsIgnoreCase(org.getIdentifier()))
				.findFirst().orElse(null);
			if(organisation == null) {
				organisation = syncManagedOrganisation(groupCommonName);
			}
			syncRole(synchronizedRoles, true, identity, organisation, ldapUser.getRoles());
			currentUserOrganisations.remove(organisation);			
		}
		
		for(Organisation currentUserOrganisation:currentUserOrganisations) {
			if(!LOST_AND_FOUND_ORGANISATION.equals(currentUserOrganisation.getExternalId())) {
				unsyncRole(identity, currentUserOrganisation);
			}
		}

		Organisation lostAndFoundOrganisation = syncManagedOrganisation(LOST_AND_FOUND_ORGANISATION);
		if(groups.isEmpty()) {
			syncRole(synchronizedRoles, true, identity, lostAndFoundOrganisation, List.of(OrganisationRoles.user));
		} else {
			unsyncRole(identity, lostAndFoundOrganisation);
		}
	}
	
	/**
	 * Search roles in dedicated groups:
	 * 
	 * @param ctx
	 * @param ldapUsers
	 * @param dnToIdentityKeyMap
	 * @param errors
	 */
	private void searchUserRolesInGroups(LdapContext ctx, LDAPUser ldapUser, LDAPError errors)
	throws NamingException {

		String filter = "(&" + syncConfiguration.getLdapGroupFilter() + "(member=" + ldapUser.getDn() + "))";
		
		//authors
		if(syncConfiguration.getAuthorsGroupBase() != null && !syncConfiguration.getAuthorsGroupBase().isEmpty()) {
			List<LDAPGroup> authorGroups = ldapDao.searchGroups(ctx, syncConfiguration.getAuthorsGroupBase(), filter);
			syncRoleIfGroupsNotEmpty(authorGroups, OrganisationRoles.author, ldapUser);
		}
		//user managers
		if(syncConfiguration.getUserManagersGroupBase() != null && !syncConfiguration.getUserManagersGroupBase().isEmpty()) {
			List<LDAPGroup> userManagerGroups = ldapDao.searchGroups(ctx, syncConfiguration.getUserManagersGroupBase(), filter);
			syncRoleIfGroupsNotEmpty(userManagerGroups, OrganisationRoles.usermanager, ldapUser);
		}
		//group managers
		if(syncConfiguration.getGroupManagersGroupBase() != null && !syncConfiguration.getGroupManagersGroupBase().isEmpty()) {
			List<LDAPGroup> groupManagerGroups = ldapDao.searchGroups(ctx, syncConfiguration.getGroupManagersGroupBase(), filter);
			syncRoleIfGroupsNotEmpty(groupManagerGroups, OrganisationRoles.groupmanager, ldapUser);
		}
		//question pool managers
		if(syncConfiguration.getQpoolManagersGroupBase() != null && !syncConfiguration.getQpoolManagersGroupBase().isEmpty()) {
			List<LDAPGroup> qpoolManagerGroups = ldapDao.searchGroups(ctx, syncConfiguration.getQpoolManagersGroupBase(), filter);
			syncRoleIfGroupsNotEmpty(qpoolManagerGroups, OrganisationRoles.poolmanager, ldapUser);
		}
		//curriculum managers
		if(syncConfiguration.getCurriculumManagersGroupBase() != null && !syncConfiguration.getCurriculumManagersGroupBase().isEmpty()) {
			List<LDAPGroup> curriculumManagerGroups = ldapDao.searchGroups(ctx, syncConfiguration.getCurriculumManagersGroupBase(), filter);
			syncRoleIfGroupsNotEmpty(curriculumManagerGroups, OrganisationRoles.curriculummanager, ldapUser);
		}
		//learning resource manager
		if(syncConfiguration.getLearningResourceManagersGroupBase() != null && !syncConfiguration.getLearningResourceManagersGroupBase().isEmpty()) {
			List<LDAPGroup> resourceManagerGroups = ldapDao.searchGroups(ctx, syncConfiguration.getLearningResourceManagersGroupBase(), filter);
			syncRoleIfGroupsNotEmpty(resourceManagerGroups, OrganisationRoles.learnresourcemanager, ldapUser);
		}
	}
	
	private void syncRoleIfGroupsNotEmpty(List<LDAPGroup> groups, OrganisationRoles role, LDAPUser ldapUser) {
		if(!groups.isEmpty()) {
			ldapUser.addRole(role);
		}
	}
	
	private void syncUserBusinessGroups(LdapContext ctx, Identity identity,  LDAPUser ldapUser) {
		List<String> groupDNs = syncConfiguration.getLdapGroupBases();
		String groupFilter = syncConfiguration.getLdapGroupFilter();
		groupFilter = "(&" + groupFilter + "(member=" + ldapUser.getDn() + "))";
		final List<LDAPGroup> groups = ldapDao.searchGroups(ctx, groupDNs, groupFilter);
		mergeLDAPGroups(groups, ldapUser.getGroupIds());
		mergeLDAPGroups(groups, ldapUser.getCoachedGroupIds());

		final Set<BusinessGroup> managedGroups = getIdentityManagedGroups(identity);
		final List<String> coachedGroupList = ldapUser.getCoachedGroupIds();

		for(LDAPGroup group:groups) {
			BusinessGroup managedGroup = getManagerBusinessGroup(group.getCommonName());
			if(managedGroup != null) {
				managedGroups.remove(managedGroup);

				boolean coach = coachedGroupList != null && coachedGroupList.contains(group.getCommonName());
				syncMemberships(managedGroup, identity, coach, true);
			}
		}
		
		for(BusinessGroup managedGroup:managedGroups) {
			businessGroupRelationDao.removeRole(identity, managedGroup, GroupRoles.coach.name());
			businessGroupRelationDao.removeRole(identity, managedGroup, GroupRoles.participant.name());
		}
	}
	
	private void mergeLDAPGroups(List<LDAPGroup> groups, List<String> groupIds) {
		for(String groupId:groupIds) {
			LDAPGroup newGroup = new LDAPGroup(groupId);
			if(!groups.contains(newGroup)) {
				groups.add(newGroup);
			}
		}
	}

	private Set<BusinessGroup> getIdentityManagedGroups(Identity identity) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setIdentity(identity);
		params.setOwner(true);
		params.setAttendee(true);
		params.setWaiting(true);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		return groups.stream().filter(group -> StringHelper.containsNonWhitespace(group.getExternalId()))
				.collect(Collectors.toSet());
	}

	/**
	 * Searches for identity with the login attribute, with fallback to the uid attribute.
	 * If not found and configured to, try to convert an identity with an OLAT authentication
	 * to LDAP, or if the identity name are manually generated, try to convert an identity
	 * with the right name to LDAP.
	 * 
	 * @param uid Name of Identity
	 * @param errors LDAPError Object if user exits but not member of
	 *          LDAPSecurityGroup
	 * 
	 * @return Identity if it's found and member of LDAPSecurityGroup, null
	 *         otherwise (if user exists but not managed by LDAP, error Object is
	 *         modified)
	 */
	@Override
	public Identity findIdentityByLdapAuthentication(Attributes attrs, LDAPError errors) {
		Authentication auth = findAuthenticationByLdapAuthentication(attrs, errors);
		return auth == null ? null : auth.getIdentity();
	}
	
	private Authentication findAuthenticationByLdapAuthentication(Attributes attrs, LDAPError errors) {
		if(attrs == null) {
			errors.insert("findIdentyByLdapAuthentication: attrs::null");
			return null;
		}
		
		String uidAttribute = syncConfiguration.getOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
		List<String> loginAttributes = syncConfiguration.getLdapUserLoginAttributes();
		
		String token = null;
		for(String loginAttribute:loginAttributes) {
			String loginToken = getSingleAttributeValue(attrs.get(loginAttribute.trim()));
			if(StringHelper.containsNonWhitespace(loginToken)) {
				Authentication ldapAuth = authenticationDao.getAuthentication(loginToken, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
				if(ldapAuth != null) {
					return ldapAuth;
				}
				// prefer the not uid attribute
				if((loginAttributes.size() == 1 || !loginAttribute.equals(uidAttribute)) && token == null) {
					token = loginToken;
				}
			}
		}

		String uid = getSingleAttributeValue(attrs.get(uidAttribute));
		Authentication ldapAuth = authenticationDao.getAuthentication(uid, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		if(ldapAuth != null) {
			if(StringHelper.containsNonWhitespace(token) && !token.equals(ldapAuth.getAuthusername())) {
				ldapAuth.setAuthusername(token);
				ldapAuth = securityManager.updateAuthentication(ldapAuth);
			}
			return ldapAuth;
		}
		
		if(ldapLoginModule.isConvertExistingLocalUsersToLDAPUsers()) {
			Authentication defaultAuth = authenticationDao.getAuthentication(uid, "OLAT", BaseSecurity.DEFAULT_ISSUER);
			if(defaultAuth != null) {
				// Add user to LDAP security group and add the ldap provider
				securityManager.createAndPersistAuthentication(defaultAuth.getIdentity(), LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER,
						token, null, null);
				log.info("Found identity by LDAP username that was not yet in LDAP security group. Converted user::{} to be an LDAP managed user", uid);
				return defaultAuth;
			}
			
			Identity identity = null;
			if(securityModule.isIdentityNameAutoGenerated()) {
				identity = securityManager.findIdentityByNickName(uid);
			} else {
				identity = securityManager.findIdentityByName(uid);
			}
			if(identity != null) {
				ldapAuth = securityManager.createAndPersistAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER,
						token, null, null);
				log.info(Tracing.M_AUDIT, "Found identity by identity name that was not yet in LDAP security group. Converted user::{} to be an LDAP managed user", uid);
				return ldapAuth;
			}
		}
		return null;
	}

	/**
	 * 
	 * Creates list of all OLAT Users which have been deleted out of the LDAP
	 * directory but still exits in OLAT
	 * 
	 * Configuration: Required Attributes = ldapContext.xml (property=reqAttrs)
	 * LDAP Base = ldapContext.xml (property=ldapBase)
	 * 
	 * @param syncTime The time to search in LDAP for changes since this time.
	 *          SyncTime has to formatted: JJJJMMddHHmm
	 * @param ctx The LDAP system connection, if NULL or closed NamingExecpiton is
	 *          thrown
	 * 
	 * @return Returns list of Identity from the user which have been deleted in
	 *         LDAP
	 * 
	 * @throws NamingException
	 */
	@Override
	public List<Identity> getIdentitiesDeletedInLdap(LdapContext ctx) {
		if (ctx == null) {
			return Collections.emptyList();
		}
		
		// Find all LDAP Users
		List<String> returningAttrList = new ArrayList<>(2);
		String userID = syncConfiguration.getOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
		returningAttrList.add(userID);
		List<String> loginAttrs = syncConfiguration.getLdapUserLoginAttributes();
		returningAttrList.addAll(loginAttrs);
		
		String[] returningAttrs = returningAttrList.toArray(new String[returningAttrList.size()]);
		String userFilter = syncConfiguration.getLdapUserFilter();
		final Set<String> ldapList = new HashSet<>();
		
		ldapDao.searchInLdap(new LDAPVisitor() {
			@Override
			public void visit(SearchResult result) throws NamingException {
				Attributes attrs = result.getAttributes();
				NamingEnumeration<? extends Attribute> aEnum = attrs.getAll();
				while (aEnum.hasMore()) {
					Attribute attr = aEnum.next();
					// use lowercase username
					ldapList.add(attr.get().toString().toLowerCase());
				}
			}
		}, (userFilter == null ? "" : userFilter), returningAttrs, ctx);

		if (ldapList.isEmpty()) {
			log.warn("No users in LDAP found, can't create the deletion list.");
			return Collections.emptyList();
		}
		
		List<Identity> identityListToDelete = new ArrayList<>();
		List<Authentication> ldapAuthentications = authenticationDao.getAuthentications(LDAPAuthenticationController.PROVIDER_LDAP);
		for (Authentication ldapAuthentication:ldapAuthentications) {
			if (!ldapList.contains(ldapAuthentication.getAuthusername().toLowerCase())) {
				identityListToDelete.add(ldapAuthentication.getIdentity());
			}
		}
		dbInstance.commitAndCloseSession();
		return identityListToDelete;
	}

	/**
	 * Execute Batch Sync. Will update all Attributes of LDAP users in OLAt, create new users and delete users in OLAT.
	 * Can be configured in ldapContext.xml
	 * 
	 * @param LDAPError
	 * 
	 */
	@Override
	public boolean doBatchSync(LDAPError errors) {
		//fxdiff: also run on nodes != 1 as nodeid = tomcat-id in fx-environment
//		if(WebappHelper.getNodeId() != 1) {
//			log.warn("Sync happens only on node 1", null);
//			return false;
//		}
		
		// o_clusterNOK
		// Synchronize on class so that only one thread can read the
		// batchSyncIsRunning flag Only this read operation is synchronized to not
		// block the whole execution of the do BatchSync method. The method is used
		// in automatic cron scheduler job and also in GUI controllers that can't
		// wait for the concurrent running request to finish first, an immediate
		// feedback about the concurrent job is needed. -> only synchronize on the
		// property read.
		synchronized (LDAPLoginManagerImpl.class) {
			if (batchSyncIsRunning) {
				// don't run twice, skip this execution
				log.info("LDAP user doBatchSync started, but another job is still running - skipping this sync");
				errors.insert("BatchSync already running by concurrent process");
				return false;
			}
		}
		
		WorkThreadInformations.setLongRunningTask("ldapSync");
		
		coordinator.getEventBus().fireEventToListenersOf(new LDAPEvent(LDAPEvent.SYNCHING), ldapSyncLockOres);
		
		lastSyncDate = null;
		
		LdapContext ctx = null;
		boolean success = false;
		try {
			acquireSyncLock();
			long startTime = System.currentTimeMillis();
			ctx = bindSystem();
			if (ctx == null) {
				errors.insert("LDAP connection ERROR");
				log.error("LDAP batch sync: LDAP connection empty");
				freeSyncLock();
				return success;
			}
			Date timeBeforeSync = new Date();

			//check server capabilities
			// Get time before sync to have a save sync time when sync is successful
			String sinceSentence = (lastSyncDate == null ? "" : " since last sync from " + lastSyncDate);
			if (ldapLoginModule.isRemoveLDAPUsersOnSyncEnabled()) {
				doBatchSyncDeletedUsers(ctx, sinceSentence);
				// bind again to use an initial unmodified context. lookup of server-properties might fail otherwise!
				ctx.close();
				ctx = bindSystem();
			}
			
			Map<String,LDAPUser> dnToIdentityKeyMap = new HashMap<>();
			List<LDAPUser> ldapUsers = doBatchSyncNewAndModifiedUsers(ctx, sinceSentence, dnToIdentityKeyMap, errors);
			ctx.close();
			ctx = bindSystem();
			//sync groups by LDAP groups or attributes
			doBatchSyncGroups(ctx, ldapUsers, dnToIdentityKeyMap, errors);
			//sync roles
			doBatchSyncRoles(ctx, ldapUsers, dnToIdentityKeyMap, errors);
			
			// update sync time and set running flag
			lastSyncDate = timeBeforeSync;
			
			ctx.close();
			success = true;
			log.info(Tracing.M_AUDIT, "LDAP batch sync done: {} in {}s", success, ((System.currentTimeMillis() - startTime) / 1000));
			return success;
		} catch (Exception e) {

			errors.insert("Unknown error");
			log.error("LDAP batch sync, unknown reason", e);
			success = false;
			return success;
		} finally {
			WorkThreadInformations.unsetLongRunningTask("ldapSync");
			freeSyncLock();
			closeContext(ctx);
			LDAPEvent endEvent = new LDAPEvent(LDAPEvent.SYNCHING_ENDED);
			endEvent.setTimestamp(new Date());
			endEvent.setSuccess(success);
			endEvent.setErrors(errors);
			coordinator.getEventBus().fireEventToListenersOf(endEvent, ldapSyncLockOres);
		}
	}

	/**
	 *
	 * @param ctx
	 * @param ldapUsers
	 * @param dnToIdentityKeyMap
	 * @param errors
	 * @throws NamingException
	 */
	private void doBatchSyncRoles(LdapContext ctx, List<LDAPUser> ldapUsers, Map<String,LDAPUser> dnToIdentityKeyMap, LDAPError errors)
	throws NamingException {
		// Search the roles of LDAP users in specified groups bases
		doBatchSearchRolesInGroups(ctx, dnToIdentityKeyMap, errors);
		
		// Sync organizations if configured
		List<LDAPOrganisationGroup> organisationsMapping = doBatchSyncOrganisations(ctx);
		if(organisationsMapping == null) {
			// Map all on default organization (users are only added in the default organization, never removed)
			doBatchSyncRolesToDefaultOrganisation(ldapUsers);
		} else {
			// Map on organizations with full synchronization, add and remove.
			List<OrganisationRoles> synchronizedRoles = syncConfiguration.getSynchronizedRoles();
			for(LDAPOrganisationGroup organisationGroup:organisationsMapping) {
				doBatchSyncRolesToOrganisations(synchronizedRoles, organisationGroup, ldapUsers);
			}
			// Synchronize Lost+Found organization
			doBatchSyncLostAndFound(organisationsMapping, ldapUsers);
		}
	}
	
	private List<LDAPOrganisationGroup> doBatchSyncOrganisations(LdapContext ctx)
	throws NamingException {
		ctx.close();
		ctx = bindSystem();

		List<LDAPOrganisationGroup> organisationsGroups = null;
		List<String> groupDNs = syncConfiguration.getLdapOrganisationsGroupBases();
		if(groupDNs != null && !groupDNs.isEmpty()) {
			String filter = syncConfiguration.getLdapOrganisationsGroupFilter();
			List<LDAPGroup> groups = ldapDao.searchGroupsWithMembers(ctx, groupDNs, filter);
			organisationsGroups = new ArrayList<>(groups.size());
			for(LDAPGroup group:groups) {
				String commonName = group.getCommonName();
				Organisation organisation = syncManagedOrganisation(commonName);
				organisationsGroups.add(LDAPOrganisationGroup.valueOf(group, organisation));
			}
		}
		dbInstance.commitAndCloseSession();
		return organisationsGroups;
	}
	
	private Organisation syncManagedOrganisation(String commonName) {
		Organisation organisation;
		List<Organisation> organisations = organisationDao.loadByLabel(commonName);
		if(organisations.size() == 1) {
			organisation = organisations.get(0);
			if(!StringHelper.containsNonWhitespace(organisation.getExternalId())) {
				organisation.setExternalId(commonName);
				organisation.setManagedFlags(OrganisationManagedFlag.toEnum("externalId,delete,members"));
				organisation = organisationService.updateOrganisation(organisation);
			}
		} else {
			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			organisation = organisationService.createOrganisation(commonName, commonName, null, defOrganisation, null);
			organisation.setExternalId(commonName);
			organisation.setManagedFlags(OrganisationManagedFlag.toEnum("externalId,delete,members"));
			organisation = organisationService.updateOrganisation(organisation);
		}
		return organisation;
	}
	
	/**
	 * Search roles in dedicated groups:
	 * 
	 * @param ctx
	 * @param ldapUsers
	 * @param dnToIdentityKeyMap
	 * @param errors
	 */
	private void doBatchSearchRolesInGroups(LdapContext ctx, Map<String,LDAPUser> dnToIdentityKeyMap, LDAPError errors)
	throws NamingException {
		ctx.close();
		ctx = bindSystem();
		
		String filter = syncConfiguration.getLdapGroupFilter();
		
		//authors
		if(syncConfiguration.getAuthorsGroupBase() != null && !syncConfiguration.getAuthorsGroupBase().isEmpty()) {
			List<LDAPGroup> authorGroups = ldapDao.searchGroupsWithMembers(ctx, syncConfiguration.getAuthorsGroupBase(), filter);
			syncRole(ctx, authorGroups, OrganisationRoles.author, dnToIdentityKeyMap, errors);
		}
		//user managers
		if(syncConfiguration.getUserManagersGroupBase() != null && !syncConfiguration.getUserManagersGroupBase().isEmpty()) {
			List<LDAPGroup> userManagerGroups = ldapDao.searchGroupsWithMembers(ctx, syncConfiguration.getUserManagersGroupBase(), filter);
			syncRole(ctx, userManagerGroups, OrganisationRoles.usermanager, dnToIdentityKeyMap, errors);
		}
		//group managers
		if(syncConfiguration.getGroupManagersGroupBase() != null && !syncConfiguration.getGroupManagersGroupBase().isEmpty()) {
			List<LDAPGroup> groupManagerGroups = ldapDao.searchGroupsWithMembers(ctx, syncConfiguration.getGroupManagersGroupBase(), filter);
			syncRole(ctx, groupManagerGroups, OrganisationRoles.groupmanager, dnToIdentityKeyMap, errors);
		}
		//question pool managers
		if(syncConfiguration.getQpoolManagersGroupBase() != null && !syncConfiguration.getQpoolManagersGroupBase().isEmpty()) {
			List<LDAPGroup> qpoolManagerGroups = ldapDao.searchGroupsWithMembers(ctx, syncConfiguration.getQpoolManagersGroupBase(), filter);
			syncRole(ctx, qpoolManagerGroups, OrganisationRoles.poolmanager, dnToIdentityKeyMap, errors);
		}
		//curriculum managers
		if(syncConfiguration.getCurriculumManagersGroupBase() != null && !syncConfiguration.getCurriculumManagersGroupBase().isEmpty()) {
			List<LDAPGroup> curriculumManagerGroups = ldapDao.searchGroupsWithMembers(ctx, syncConfiguration.getCurriculumManagersGroupBase(), filter);
			syncRole(ctx, curriculumManagerGroups, OrganisationRoles.curriculummanager, dnToIdentityKeyMap, errors);
		}
		//learning resource manager
		if(syncConfiguration.getLearningResourceManagersGroupBase() != null && !syncConfiguration.getLearningResourceManagersGroupBase().isEmpty()) {
			List<LDAPGroup> resourceManagerGroups = ldapDao.searchGroupsWithMembers(ctx, syncConfiguration.getLearningResourceManagersGroupBase(), filter);
			syncRole(ctx, resourceManagerGroups, OrganisationRoles.learnresourcemanager, dnToIdentityKeyMap, errors);
		}
	}
	
	private void doBatchSyncLostAndFound(List<LDAPOrganisationGroup> organisationsGroups, List<LDAPUser> ldapUsers) {
		Organisation lostAndFoundOrganisation = syncManagedOrganisation(LOST_AND_FOUND_ORGANISATION);
		Set<Long> organisationUserKeys = organisationDao.getMemberKeySet(lostAndFoundOrganisation, OrganisationRoles.user);
		dbInstance.commitAndCloseSession();
		
		for(LDAPUser ldapUser:ldapUsers) {
			if(ldapUser.getCachedIdentity() == null) {
				continue;
			}
			
			Long identityKey = ldapUser.getCachedIdentity().getKey();
			
			boolean isMemberOf = false;
			for(LDAPOrganisationGroup organisationGroup:organisationsGroups) {
				isMemberOf |= organisationGroup.isMember(ldapUser.getDn());
			}
			
			if(isMemberOf) {
				// Will be removed after
			} else {
				boolean removed = organisationUserKeys.remove(identityKey);
				if(!removed) {
					Identity identity = securityManager.loadIdentityByKey(identityKey);
					syncRole(List.of(), true, identity, lostAndFoundOrganisation, List.of());
					dbInstance.commitAndCloseSession();
				}
			}
		}
		
		for(Long lostAndFoundKey:organisationUserKeys) {
			unsyncRole(new IdentityRefImpl(lostAndFoundKey), lostAndFoundOrganisation);
			dbInstance.commitAndCloseSession();
		}
	}

	private void doBatchSyncRolesToOrganisations(List<OrganisationRoles> synchronizedRoles,
			LDAPOrganisationGroup organisationGroup, List<LDAPUser> ldapUsers) {

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationGroup.getOrganisation();
		List<OrganisationRoles> orgRoles = new ArrayList<>(synchronizedRoles);
		orgRoles.add(OrganisationRoles.user);
		Set<Long> organisationUserKeys = organisationDao
				.getMemberKeySet(organisation, orgRoles.toArray(new OrganisationRoles[orgRoles.size()]));
		
		int count = 0;
		for(LDAPUser ldapUser:ldapUsers) {
			if(ldapUser.getCachedIdentity() == null) {
				continue;
			}
			Long identityKey = ldapUser.getCachedIdentity().getKey();
			Identity identity = securityManager.loadIdentityByKey(identityKey);

			List<OrganisationRoles> roles = ldapUser.getRoles();
			boolean member = organisationGroup.isMember(ldapUser.getDn());
			if(member) {
				syncRole(synchronizedRoles, true, identity, organisation, roles);
				organisationUserKeys.remove(identity.getKey());
			}
			
			if(organisationDao.hasRole(identity, null, defOrganisation, OrganisationRoles.user.name())) {
				organisationService.addMember(defOrganisation, identity, OrganisationRoles.user);
			}

			if(!roles.isEmpty() || count % 25 == 0) {
				dbInstance.commitAndCloseSession();
			}
			if(count % 1000 == 0) {
				log.info("Synchronize {}/{} users in LDAP server on organisation: {}", count, ldapUsers.size(), organisationGroup.getCommonName());
			}
			count++;
		}
		
		int unCount = 0;
		for(Long organisationUserKey:organisationUserKeys) {
			Identity identity = securityManager.loadIdentityByKey(organisationUserKey);
			unsyncRole(identity, organisation);
			
			if(unCount % 25 == 0) {
				dbInstance.commitAndCloseSession();
			}
			if(unCount % 1000 == 0) {
				log.info("Unsynchronize {}/{} users in LDAP server on organisation: {}", unCount, organisationUserKeys.size(), organisationGroup.getCommonName());
			}
			unCount++;
		}
	}
	
	private void doBatchSyncRolesToDefaultOrganisation(List<LDAPUser> ldapUsers) {
		List<OrganisationRoles> synchronizedRoles = syncConfiguration.getSynchronizedRoles();
		if(synchronizedRoles.isEmpty()) return;

		int count = 0;
		List<Organisation> organisations = organisationDao.loadDefaultOrganisation();
		Organisation defOrganisation = organisations.get(0);
		for(LDAPUser ldapUser:ldapUsers) {
			if(ldapUser.getCachedIdentity() == null) {
				continue;
			}
			
			List<OrganisationRoles> roles = ldapUser.getRoles();
			Identity identity = securityManager.loadIdentityByKey(ldapUser.getCachedIdentity().getKey());
			syncRole(synchronizedRoles, false, identity, defOrganisation, roles);
			if(!roles.isEmpty()) {
				dbInstance.commitAndCloseSession();
			}
			if(count % 1000 == 0) {
				log.info("Synchronize {}/{} users in LDAP server on default organisation", count, ldapUsers.size());
			}
			count++;
		}
	}
	
	private void syncRole(LdapContext ctx, List<LDAPGroup> groups, OrganisationRoles role,
			Map<String,LDAPUser> dnToIdentityKeyMap, LDAPError errors) {
		if(groups == null || groups.isEmpty()) return;
		
		for(LDAPGroup group:groups) {
			List<String> members = group.getMembers();
			if(members != null && !members.isEmpty()) {
				for(String member:members) {
					LDAPUser ldapUser = getLDAPUser(ctx, member, dnToIdentityKeyMap, errors);
					if(ldapUser != null && ldapUser.getCachedIdentity() != null) {
						ldapUser.addRole(role);
					}
				}
			}
			dbInstance.commitAndCloseSession();
		}	
	}

	private void syncRole(List<OrganisationRoles> synchronizedRoles, boolean fullSync,
			Identity identity, Organisation organisation, List<OrganisationRoles> ldapRoles) {
		
		Roles roles = securityManager.getRoles(identity);

		boolean author = syncRole(synchronizedRoles, fullSync, ldapRoles, OrganisationRoles.author, roles.isAuthor());
		boolean groupManager = syncRole(synchronizedRoles, fullSync, ldapRoles, OrganisationRoles.groupmanager, roles.isGroupManager());
		boolean poolManager = syncRole(synchronizedRoles, fullSync, ldapRoles, OrganisationRoles.poolmanager, roles.isPoolManager());
		boolean curriculumManager = syncRole(synchronizedRoles, fullSync, ldapRoles, OrganisationRoles.curriculummanager, roles.isCurriculumManager());
		boolean userManager = syncRole(synchronizedRoles, fullSync, ldapRoles, OrganisationRoles.usermanager, roles.isUserManager());
		boolean learnResourceManager = syncRole(synchronizedRoles, fullSync, ldapRoles, OrganisationRoles.learnresourcemanager, roles.isLearnResourceManager());
		boolean administrator = syncRole(synchronizedRoles, fullSync, ldapRoles, OrganisationRoles.administrator, roles.isAdministrator());

		RolesByOrganisation modifiedRoles = RolesByOrganisation.roles(organisation, false, false, true,
				author, groupManager, poolManager, curriculumManager, userManager, learnResourceManager, administrator);
		securityManager.updateRoles(null, identity, modifiedRoles);
	}
	
	private void unsyncRole(IdentityRef identityRef, Organisation organisation) {
		Identity identity = securityManager.loadIdentityByKey(identityRef.getKey());
		RolesByOrganisation modifiedRoles = RolesByOrganisation.roles(organisation, false, false, false,
				false, false, false, false, false, false, false);
		securityManager.updateRoles(null, identity, modifiedRoles);
	}
	
	private boolean syncRole(List<OrganisationRoles> synchronizedRoles, boolean fullSync, List<OrganisationRoles> ldapRoles,
			OrganisationRoles roleToModify, boolean currentValue) {
		if(synchronizedRoles.contains(roleToModify)) {
			if(fullSync) {
				currentValue = ldapRoles.contains(roleToModify);
			} else {
				currentValue |= ldapRoles.contains(roleToModify);
			}	
		}
		return currentValue;
	}
	
	private void doBatchSyncDeletedUsers(LdapContext ctx, String sinceSentence) {
		// create User to Delete List
		List<Identity> deletedUserList = getIdentitiesDeletedInLdap(ctx);
		// delete old users
		if (deletedUserList == null || deletedUserList.isEmpty()) {
			log.info("LDAP batch sync: no users to delete {}", sinceSentence);
		} else {
			int deletedUserListSize = deletedUserList.size();
			if (ldapLoginModule.isRemoveLDAPUsersOnSyncEnabled()) {
				// check if more not more than the defined percentages of
				// users managed in LDAP should be deleted
				// if they are over the percentage, they will not be deleted
				// by the sync job
				long olatListIdentitySize = authenticationDao.countIdentitiesWithAuthentication(LDAPAuthenticationController.PROVIDER_LDAP);
				if (olatListIdentitySize == 0) {
					log.info("No users managed by LDAP, can't delete users");
				} else {
					int prozente = (int) (((float)deletedUserListSize / (float)olatListIdentitySize) * 100.0);
					int cutValue = ldapLoginModule.getDeleteRemovedLDAPUsersPercentage();
					String removeOption = ldapLoginModule.getRemoveLDAPUsersOnSyncOption();
					if (prozente >= cutValue) {
						log.info("LDAP batch sync: more than {}% of LDAP managed users should be deleted. Please use Admin Deletion Job. Or increase deleteRemovedLDAPUsersPercentage. {}% tried to delete.",
								cutValue, prozente);
					} else if("true".equals(removeOption)){
						// delete users
						deleteIdentities(deletedUserList, null);
						log.info("LDAP batch sync: {} users deleted {}", deletedUserListSize, sinceSentence);
					} else if("deactivate".equals(removeOption)) {
						// delete users
						inactivateIdentities(deletedUserList, null);
						log.info("LDAP batch sync: {} users deactivated {}", deletedUserListSize, sinceSentence);
					}
				}
			} else {
				// Do nothing, only log users to logfile
				StringBuilder users = new StringBuilder(deletedUserListSize * 42);
				for (Identity toBeDeleted : deletedUserList) {
					users.append(toBeDeleted.getKey()).append(',');
				}
				log.info("LDAP batch sync: {} users detected as to be deleted {}. Automatic deleting is disabled in LDAPLoginModule, delete these users manually::[{}]",
						deletedUserListSize, sinceSentence, users);
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private List<LDAPUser> doBatchSyncNewAndModifiedUsers(LdapContext ctx, String sinceSentence, Map<String,LDAPUser> dnToIdentityKeyMap, LDAPError errors) {
		// Get new and modified users from LDAP
		int count = 0;
		List<LDAPUser> ldapUserList = ldapDao.getUserAttributesModifiedSince(lastSyncDate, ctx);
		
		// Check for new and modified users
		List<LDAPUser> newLdapUserList = new ArrayList<>();
		Map<IdentityRef, Map<String, String>> changedMapIdentityMap = new HashMap<>();
		for (LDAPUser ldapUser: ldapUserList) {
			String user = null;
			try {
				Attributes userAttrs = ldapUser.getAttributes();
				String uidProp = syncConfiguration.getOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
				user = getSingleAttributeValue(userAttrs.get(uidProp));
				Identity identity = findIdentityByLdapAuthentication(userAttrs, errors);
				if (identity != null) {
					Map<String, String> changedAttrMap = prepareUserPropertyForSync(userAttrs, identity);
					if (changedAttrMap != null) {
						changedMapIdentityMap.put(identity, changedAttrMap);
					}
					if(StringHelper.containsNonWhitespace(ldapUser.getDn())) {
						dnToIdentityKeyMap.put(ldapUser.getDn(), ldapUser);
						ldapUser.setCachedIdentity(new IdentityRefImpl(identity.getKey()));
					}
				} else if (errors.isEmpty()) {
					List<String> reqAttrs = syncConfiguration.checkRequestAttributes(userAttrs, userModule.isEmailMandatory());
					if (reqAttrs.isEmpty()) {
						newLdapUserList.add(ldapUser);
					} else {
						log.warn("LDAP batch sync: can't create user with username::{} : missing required attributes::{}", user, reqAttrs);
					}
				} else {
					log.warn(errors.get());
				}
			} catch (Exception e) {
				// catch here to go on with other users on exceptions!
				log.error("some error occured in looping over set of changed user-attributes, actual user {}. Will still continue with others.", user, e);
				errors.insert("Cannot sync user: " + user);
			} finally {
				dbInstance.commit();
				if(count % 10 == 0) {
					dbInstance.closeSession();
				}
			}
			if(count % 1000 == 0) {
				log.info("Retrieve {}/{} users in LDAP server", count, ldapUserList.size());
			}
			count++;
		}
		
		// sync existing users
		if (changedMapIdentityMap == null || changedMapIdentityMap.isEmpty()) {
			log.info("LDAP batch sync: no users to sync {}", sinceSentence);
		} else {
			int syncCount = 0;
			for (IdentityRef ident : changedMapIdentityMap.keySet()) {
				// sync user is exception save, no try/catch needed
				try {
					syncCount++;
					syncUser(changedMapIdentityMap.get(ident), ident);
				} catch (Exception e) {
					errors.insert("Cannot sync user: " + ident);
				} finally {
					dbInstance.commit();
					if(syncCount % 20 == 0) {
						dbInstance.closeSession();
					}
				}
				if(syncCount % 1000 == 0) {
					log.info("Update {}/{} LDAP users", syncCount, changedMapIdentityMap.size());
				}
			}
			log.info("LDAP batch sync: {} users synced {}", changedMapIdentityMap.size(), sinceSentence);
		}
		
		// create new users
		if (newLdapUserList.isEmpty()) {
			log.info("LDAP batch sync: no users to create {}", sinceSentence);
		} else {			
			int newCount = 0;
			for (LDAPUser ldapUser: newLdapUserList) {
				Attributes userAttrs = ldapUser.getAttributes();
				try {
					newCount++;
					Identity identity = createAndPersistUser(userAttrs);
					if(identity != null && StringHelper.containsNonWhitespace(ldapUser.getDn())) {
						dnToIdentityKeyMap.put(ldapUser.getDn(), ldapUser);
						ldapUser.setCachedIdentity(new IdentityRefImpl(identity.getKey()));
					}
				} catch (Exception e) {
					// catch here to go on with other users on exeptions!
					log.error("some error occured while creating new users, actual userAttribs {}. Will still continue with others.", userAttrs, e);
				} finally {
					dbInstance.commit();
					if(newCount % 20 == 0) {
						dbInstance.closeSession();
					}
				}
				
				if(newCount % 1000 == 0) {
					log.info("Create {}/{} LDAP users", count, newLdapUserList.size());
				}
			}
			log.info("LDAP batch sync: {} users created {}", newLdapUserList.size(), sinceSentence);
		}

		dbInstance.commitAndCloseSession();
		return ldapUserList;
	}
	
	private void doBatchSyncGroups(LdapContext ctx, List<LDAPUser> ldapUsers, Map<String,LDAPUser> dnToIdentityKeyMap, LDAPError errors)
	throws NamingException {
		ctx.close();
		
		log.info("LDAP batch sync LDAP user to OO groups");
		
		ctx = bindSystem();
		//sync groups by LDAP groups or attributes
		Map<String,LDAPGroup> cnToGroupMap = new HashMap<>();
		
		// retrieve all ldap group's with their list of members
		if(syncConfiguration.syncGroupWithLDAPGroup()) {
			String filter = syncConfiguration.getLdapGroupFilter();
			List<String> groupDNs = syncConfiguration.getLdapGroupBases();
			List<LDAPGroup> ldapGroups = ldapDao.searchGroupsWithMembers(ctx, groupDNs, filter);
			for(LDAPGroup ldapGroup:ldapGroups) {
				cnToGroupMap.put(ldapGroup.getCommonName(), ldapGroup);
			}
		}
		if(syncConfiguration.syncGroupWithAttribute()) {
			doSyncGroupByAttribute(ldapUsers, cnToGroupMap);
		}
		
		if(syncConfiguration.syncGroupWithLDAPGroup() || syncConfiguration.syncGroupWithAttribute()) {
			int syncGroupCount = 0;
			// exclusion list to prevent loading several times user which cannot be found
			Set<String> excludedMembers = new HashSet<>();
			for(LDAPGroup group:cnToGroupMap.values()) {
				BusinessGroup managedGroup = getManagerBusinessGroup(group.getCommonName());
				if(managedGroup != null) {
					syncBusinessGroup(ctx, managedGroup, group, dnToIdentityKeyMap, excludedMembers, errors);
				}
				dbInstance.commitAndCloseSession();
				if(syncGroupCount % 100 == 0) {
					log.info("Synched {}/{} LDAP groups", syncGroupCount, cnToGroupMap.size());
				}
				syncGroupCount++;
			}
		}
	}
	
	private void doSyncGroupByAttribute(List<LDAPUser> ldapUsers, Map<String,LDAPGroup> cnToGroupMap) {
		for(LDAPUser ldapUser:ldapUsers) {
			List<String> groupIds = ldapUser.getGroupIds();
			List<String> coachedGroupIds = ldapUser.getCoachedGroupIds();
			if((groupIds != null && !groupIds.isEmpty()) || (coachedGroupIds != null && !coachedGroupIds.isEmpty())) {
				IdentityRef identity = ldapUser.getCachedIdentity();
				if(identity == null) {
					log.error("Identity with dn={} not found", ldapUser.getDn());
				} else {
					if(groupIds != null && !groupIds.isEmpty()) {
						for(String groupId:groupIds) {
							if(!cnToGroupMap.containsKey(groupId)) {
								cnToGroupMap.put(groupId, new LDAPGroup(groupId));
							}
							cnToGroupMap.get(groupId).getParticipants().add(ldapUser);
						}
					}
					
					if(coachedGroupIds != null && !coachedGroupIds.isEmpty()) {
						for(String coachedGroupId:coachedGroupIds) {
							if(!cnToGroupMap.containsKey(coachedGroupId)) {
								cnToGroupMap.put(coachedGroupId, new LDAPGroup(coachedGroupId));
							}
							cnToGroupMap.get(coachedGroupId).getCoaches().add(ldapUser);
						}
					}
				}
			}
		}
	}
	
	private void syncBusinessGroup(LdapContext ctx, BusinessGroup businessGroup, LDAPGroup ldapGroup,
			Map<String,LDAPUser> dnToIdentityKeyMap, Set<String> excludes, LDAPError errors) {
		List<Identity> currentMembers = businessGroupRelationDao
				.getMembers(businessGroup, GroupRoles.coach.name(), GroupRoles.participant.name());
		Set<Long> currentMemberKeys = new HashSet<>();
		for(Identity currentMember:currentMembers) {
			currentMemberKeys.add(currentMember.getKey());
		}

		int count = 0;
		Set<LDAPUser> coaches = new HashSet<>(ldapGroup.getCoaches());
		Set<LDAPUser> participants = new HashSet<>(ldapGroup.getParticipants());
		// transfer member cn's to the participants list
		for(String member:ldapGroup.getMembers()) {
			try {
				if(excludes.contains(member)) {
					continue;
				}
				
				LDAPUser ldapUser = getLDAPUser(ctx, member, dnToIdentityKeyMap, errors);
				if(ldapUser == null) {
					excludes.add(member);
				} else if(!participants.contains(ldapUser)) {
					participants.add(ldapUser);
				}
			} catch (Exception e) {
				log.error("Cannot retrieve this LDAP group member: {}", member, e);
			}

			if(count % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
			count++;
		}
		// transfer to ldap user flagged as coach to the coach list
		for(Iterator<LDAPUser> participantIt=participants.iterator(); participantIt.hasNext(); ) {
			LDAPUser participant = participantIt.next();
			if(participant.isCoach()) {
				if(!coaches.contains(participant)) {
					coaches.add(participant);
				}
				participantIt.remove();
			}
		}
		
		Set<LDAPUser> members = new HashSet<>(participants);
		members.addAll(coaches);
		for(LDAPUser member:members) {
			boolean participant = participants.contains(member);
			boolean coach = coaches.contains(member);
			IdentityRef memberIdentity = member.getCachedIdentity();
			if(memberIdentity != null && memberIdentity.getKey() != null) {
				syncMemberships(businessGroup, memberIdentity, coach, participant);
				currentMemberKeys.remove(memberIdentity.getKey());
			}
			if(count % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
			count++;
		}
		
		for(Long currentMemberKey:currentMemberKeys) {
			Identity currentMember = securityManager.loadIdentityByKey(currentMemberKey);
			List<String> roles = businessGroupRelationDao.getRoles(currentMember, businessGroup);
			for(String role:roles) {
				businessGroupRelationDao.removeRole(currentMember, businessGroup, role);
			}
			
			if(count % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
			count++;
		}
		dbInstance.commitAndCloseSession();
	}

	private void syncMemberships(BusinessGroup businessGroup, IdentityRef identityRef, boolean coach, boolean participant) {
		if(identityRef == null || businessGroup == null) return;
		
		// Propagate coach to participant role or exclude participant role
		if(syncConfiguration.isGroupCoachParticipant()) {
			if(coach && !participant) {
				participant = true;
			}
		} else if(coach && participant) {
			participant = false;
		}

		List<String> roles = businessGroupRelationDao.getRoles(identityRef, businessGroup);
		if((coach && participant && roles.size() == 2 && roles.contains(GroupRoles.coach.name()) && roles.contains(GroupRoles.participant.name()))
				|| (coach && !participant && roles.size() == 1 && roles.contains(GroupRoles.coach.name()))
				|| (!coach && participant && roles.size() == 1 && roles.contains(GroupRoles.participant.name()))
				|| (!coach && !participant && roles.isEmpty())) {
			return;// fail fast
		}

		Identity identity = securityManager.loadIdentityByKey(identityRef.getKey());
		if(coach && !roles.contains(GroupRoles.coach.name())) {
			businessGroupRelationDao.addRole(identity, businessGroup, GroupRoles.coach.name());
		} else if(!coach && roles.contains(GroupRoles.coach.name())) {
			businessGroupRelationDao.removeRole(identity, businessGroup, GroupRoles.coach.name());
		}

		if(participant && !roles.contains(GroupRoles.participant.name())) {
			businessGroupRelationDao.addRole(identity, businessGroup, GroupRoles.participant.name());
		} else if(!participant && roles.contains(GroupRoles.participant.name())) {
			businessGroupRelationDao.removeRole(identity, businessGroup, GroupRoles.participant.name());
		}
	}
	
	private BusinessGroup getManagerBusinessGroup(String externalId) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExternalId(externalId);
		List<BusinessGroup> businessGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		
		BusinessGroup managedBusinessGroup;
		if(businessGroups.isEmpty()) {
			String managedFlags = BusinessGroupManagedFlag.membersmanagement.name() + "," + BusinessGroupManagedFlag.delete.name();
			managedBusinessGroup = businessGroupService
					.createBusinessGroup(null, externalId, externalId, BusinessGroup.BUSINESS_TYPE,
							externalId, managedFlags, null, null, false, false, null);

		} else if(businessGroups.size() == 1) {
			managedBusinessGroup = businessGroups.get(0);
		} else {
			log.error("{} managed groups found with the following external id: {}", businessGroups.size(), externalId);
			managedBusinessGroup = null;
		}
		return managedBusinessGroup;
	}
	
	private LDAPUser getLDAPUser(LdapContext ctx, String member, Map<String,LDAPUser> dnToIdentityKeyMap, LDAPError errors) {
		LDAPUser ldapUser = dnToIdentityKeyMap.get(member);

		IdentityRef identity = ldapUser == null ? null : ldapUser.getCachedIdentity();
		if(identity == null) {
			String userFilter = syncConfiguration.getLdapUserFilter();
			
			String userDN = member;
			LDAPUserVisitor visitor = new LDAPUserVisitor(syncConfiguration);
			ldapDao.search(visitor, userDN, userFilter, syncConfiguration.getUserAttributes(), ctx);
			
			List<LDAPUser> ldapUserList = visitor.getLdapUserList();
			if(ldapUserList.size() == 1) {
				ldapUser = ldapUserList.get(0);
				Attributes userAttrs = ldapUser.getAttributes();
				identity = findIdentityByLdapAuthentication(userAttrs, errors);
				if(identity != null) {
					dnToIdentityKeyMap.put(userDN, ldapUser);
					ldapUser.setCachedIdentity(new IdentityRefImpl(identity.getKey()));
				}
			}
		}
		return ldapUser;
	}
	
	/**
	 * Used by the Panther provider
	 */
	@Override
	public void doSyncSingleUserWithLoginAttribute(Identity ident) {
		LdapContext ctx = bindSystem();
		if (ctx == null) {
			log.error("could not bind to ldap");
		}
		
		Authentication authentication = authenticationDao.getAuthentication(ident, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		
		List<String> ldapUserIDAttribute = syncConfiguration.getLdapUserLoginAttributes();
		String filter = ldapDao.buildSearchUserFilter(ldapUserIDAttribute, authentication.getAuthusername());
		
		List<Attributes> ldapUserAttrs = new ArrayList<>();
		ldapDao.searchInLdap(result -> ldapUserAttrs.add(result.getAttributes()),
				filter, syncConfiguration.getUserAttributes(), ctx);
		
		if(ldapUserAttrs.size() == 1) {
			Attributes attrs = ldapUserAttrs.get(0);
			Map<String, String> olatProToSync = prepareUserPropertyForSync(attrs, ident);
			if (olatProToSync != null) {
				syncUser(olatProToSync, ident);
			}
		} else {
			log.error("Cannot sync the user because it was not found on LDAP server: {}", ident);
		}
	}

	@Override
	public Date getLastSyncDate() {
		return lastSyncDate;
	}

	/**
	 * Internal helper to add the SSL protocol to the environment
	 * 
	 * @param env
	 */
	private void enableSSL(Hashtable<String, String> env) {
		env.put(Context.SECURITY_PROTOCOL, "ssl");
		if(StringHelper.containsNonWhitespace(ldapLoginModule.getTrustStoreLocation())) {
			System.setProperty("javax.net.ssl.trustStore", ldapLoginModule.getTrustStoreLocation());
		}
	}
	
	/**
	 * Acquire lock for administration jobs
	 * 
	 */
	@Override
	public synchronized boolean acquireSyncLock(){
		if(batchSyncIsRunning){
			return false;
		}
		batchSyncIsRunning=true;
		return true;
	}
	
	/**
	 * Release lock for administration jobs
	 * 
	 */
	@Override
	public synchronized void freeSyncLock() {
		batchSyncIsRunning = false;
	}

	/**
	 * remove all cached authentications for fallback-login. useful if users logged in first with a default pw and changed it outside in AD/LDAP, but OLAT doesn't know about.
	 * removing fallback-auths means login is only possible by AD/LDAP and if server is reachable!
	 */
	@Override
	public void removeFallBackAuthentications() {
		if (ldapLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()){
			List<Identity> ldapIdents = authenticationDao.getIdentitiesWithAuthentication(LDAPAuthenticationController.PROVIDER_LDAP);
			log.info("found {} identies in ldap security group", ldapIdents.size());
			int count=0;
			for (Identity identity : ldapIdents) {
				Authentication auth = securityManager.findAuthentication(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER);				
				if (auth!=null){
					securityManager.deleteAuthentication(auth);
					count++;
				}
				if (count % 20 == 0){
					dbInstance.intermediateCommit();
				}
			}
			log.info("removed cached authentications (fallback login provider: {}) for {} users.",
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), count);			
		}
	}

	@Override
	public boolean isIdentityInLDAPSecGroup(Identity ident) {
		return authenticationDao.hasAuthentication(ident, LDAPAuthenticationController.PROVIDER_LDAP);
	}
	
	private void closeContext(Context ctx) {
		try {
			if(ctx != null) {
				ctx.close();
			}
		} catch (NamingException e) {
			log.warn("", e);
		}
		
	}
}