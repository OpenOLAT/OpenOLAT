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

package org.olat.ldap;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import net.fortuna.ical4j.util.TimeZones;

import org.apache.commons.lang.ArrayUtils;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.MailHelper;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.user.UserManager;

/**
 * Description: This manager handles  communication between LDAP and OLAT. LDAP access is done by JNDI.
 * The synching is done only on node 1 of a cluster.
 * <p>
 * LDAPLoginMangerImpl
 * <p>
 * 
 * @author Maurus Rohrer
 */
public class LDAPLoginManagerImpl extends LDAPLoginManager implements GenericEventListener {

	private static final String TIMEOUT_KEY = "com.sun.jndi.ldap.connect.timeout";
	private static final TimeZone UTC_TIME_ZONE;
	private static boolean batchSyncIsRunning = false;
	private static Date lastSyncDate = null; // first sync is always a full sync
	
	private static final int PAGE_SIZE = 50;
	private static final String PAGED_RESULT_CONTROL_OID = "1.2.840.113556.1.4.319";
	
	private Coordinator coordinator;
	private TaskExecutorManager taskExecutorManager;
	
	private BaseSecurity securityManager;
	private UserManager userManager;
	private UserDeletionManager userDeletionManager;
	private boolean pagingSupportedAlreadyFound;

	static {
		UTC_TIME_ZONE = TimeZone.getTimeZone(TimeZones.UTC_ID);
	}

	/**
	 * Private constructor. Use LDAPLoginManager.getInstance() method instead
	 */
	private LDAPLoginManagerImpl(CoordinatorManager coordinatorManager, TaskExecutorManager taskExecutorManager) {
		super();
		this.coordinator = coordinatorManager.getCoordinator();
		this.taskExecutorManager = taskExecutorManager;
		
		coordinator.getEventBus().registerFor(this, null, ldapSyncLockOres);
	}
	
	/**
	 * [used by Spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}

	/**
	 * [used by Spring]
	 * @param userManager
	 */
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
	
	/**
	 * [used by Spring]
	 * @param userDeletionManager
	 */
	public void setUserDeletionManager(UserDeletionManager userDeletionManager) {
		this.userDeletionManager = userDeletionManager;
	}

	@Override
	public void event(Event event) {
		if(event instanceof LDAPEvent) {
			if(LDAPEvent.SYNCHING.equals(event.getCommand())) {
				batchSyncIsRunning = true;
			} else if(LDAPEvent.SYNCHING_ENDED.equals(event.getCommand())) {
				batchSyncIsRunning = false;
				lastSyncDate = ((LDAPEvent)event).getTimestamp();
			} else if(LDAPEvent.DO_SYNCHING.equals(event.getCommand())) {
				doHandleBatchSync();
			} else if(LDAPEvent.DO_FULL_SYNCHING.equals(event.getCommand())) {
				lastSyncDate = null;
				doHandleBatchSync();
			}
		}
	}
	
	private void doHandleBatchSync() {
		//fxdiff: also run on nodes != 1 as nodeid = tomcat-id in fx-environment
//		if(WebappHelper.getNodeId() != 1) return;
		
		Runnable batchSyncTask = new Runnable() {
			public void run() {
				LDAPError errors = new LDAPError();
				doBatchSync(errors);
			}				
		};
		taskExecutorManager.execute(batchSyncTask);		
	}

	/**
	 * Connect to the LDAP server with System DN and Password
	 * 
	 * Configuration: LDAP URL = olatextconfig.xml (property=ldapURL) System DN =
	 * olatextconfig.xml (property=ldapSystemDN) System PW = olatextconfig.xml
	 * (property=ldapSystemPW)
	 * 
	 * @return The LDAP connection (LdapContext) or NULL if connect fails
	 * 
	 * @throws NamingException
	 */
	public LdapContext bindSystem() {
		// set LDAP connection attributes
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, LDAPLoginModule.getLdapUrl());
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, LDAPLoginModule.getLdapSystemDN());
		env.put(Context.SECURITY_CREDENTIALS, LDAPLoginModule.getLdapSystemPW());
		if(LDAPLoginModule.getLdapConnectionTimeout() != null) {
			env.put(TIMEOUT_KEY, LDAPLoginModule.getLdapConnectionTimeout().toString());
		}

		// check ssl
		if (LDAPLoginModule.isSslEnabled()) {
			enableSSL(env);
		}

		try {
			InitialLdapContext ctx = new InitialLdapContext(env, new Control[]{});
			ctx.getConnectControls();
			return ctx;
		} catch (NamingException e) {
			logError("NamingException when trying to bind system with DN::" + LDAPLoginModule.getLdapSystemDN() + " and PW::"
					+ LDAPLoginModule.getLdapSystemPW() + " on URL::" + LDAPLoginModule.getLdapUrl(), e);
			return null;
		} catch (Exception e) {
			logError("Exception when trying to bind system with DN::" + LDAPLoginModule.getLdapSystemDN() + " and PW::"
					+ LDAPLoginModule.getLdapSystemPW() + " on URL::" + LDAPLoginModule.getLdapUrl(), e);
			return null;
		}

	}

	/**
	 * 
	 * Connect to LDAP with the User-Name and Password given as parameters
	 * 
	 * Configuration: LDAP URL = olatextconfig.xml (property=ldapURL) LDAP Base =
	 * olatextconfig.xml (property=ldapBase) LDAP Attributes Map =
	 * olatextconfig.xml (property=userAttrs)
	 * 
	 * 
	 * @param uid The users LDAP login name (can't be null)
	 * @param pwd The users LDAP password (can't be null)
	 * 
	 * @return After successful bind Attributes otherwise NULL
	 * 
	 * @throws NamingException
	 */
	public Attributes bindUser(String uid, String pwd, LDAPError errors) {
		// get user name, password and attributes
		String ldapUrl = LDAPLoginModule.getLdapUrl();
		String[] userAttr = LDAPLoginModule.getUserAttrs();

		if (uid == null || pwd == null) {
			if (isLogDebugEnabled()) logDebug("Error when trying to bind user, missing username or password. Username::" + uid + " pwd::" + pwd);
			errors.insert("Username and password must be selected");
			return null;
		}
		
		LdapContext ctx = bindSystem();
		if (ctx == null) {
			errors.insert("LDAP connection error");
			return null;
		}
		String userDN = searchUserDN(uid, ctx);
		if (userDN == null) {
			logInfo("Error when trying to bind user with username::" + uid + " - user not found on LDAP server"
					+ (LDAPLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin() ? ", trying with OLAT login provider" : ""));
			errors.insert("Username or password incorrect");
			return null;
		}
		
		// Ok, so far so good, user exists. Now try to fetch attributes using the
		// users credentials
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, userDN);
		env.put(Context.SECURITY_CREDENTIALS, pwd);
		if(LDAPLoginModule.getLdapConnectionTimeout() != null) {
			env.put(TIMEOUT_KEY, LDAPLoginModule.getLdapConnectionTimeout().toString());
		}
		if (LDAPLoginModule.isSslEnabled()) {
			enableSSL(env);
		}

		try {
			Control[] connectCtls = new Control[]{};
			LdapContext userBind = new InitialLdapContext(env, connectCtls);
			Attributes attributes = userBind.getAttributes(userDN, userAttr);
			userBind.close();
			return attributes;
		} catch (AuthenticationException e) {
			logInfo("Error when trying to bind user with username::" + uid + " - invalid LDAP password");
			errors.insert("Username or password incorrect");
			return null;
		} catch (NamingException e) {
			logError("NamingException when trying to get attributes after binding user with username::" + uid, e);
			errors.insert("Username or password incorrect");
			return null;
		}
	}
	

	/**
	 * Change the password on the LDAP server.
	 * @see org.olat.ldap.LDAPLoginManager#changePassword(org.olat.core.id.Identity, java.lang.String, org.olat.ldap.LDAPError)
	 */
	@Override
	public void changePassword(Identity identity, String pwd, LDAPError errors) {
		String uid = identity.getName();
		String ldapUserPasswordAttribute = LDAPLoginModule.getLdapUserPasswordAttribute();
		try {
			DirContext ctx = bindSystem();
			String dn = searchUserDN(uid, ctx);
			
			ModificationItem [] modificationItems = new ModificationItem [ 1 ];
			
			Attribute userPasswordAttribute;
			if(LDAPLoginModule.isActiveDirectory()) {
				//active directory need the password enquoted and unicoded (but little-endian)
				String quotedPassword = "\"" + pwd + "\"";
	      char unicodePwd[] = quotedPassword.toCharArray();
				byte pwdArray[] = new byte[unicodePwd.length * 2];
	      for (int i=0; i<unicodePwd.length; i++) {
	        pwdArray[i*2 + 1] = (byte) (unicodePwd[i] >>> 8);
	        pwdArray[i*2 + 0] = (byte) (unicodePwd[i] & 0xff);
	      }
				userPasswordAttribute = new BasicAttribute ( ldapUserPasswordAttribute, pwdArray );
			} else {
				userPasswordAttribute = new BasicAttribute ( ldapUserPasswordAttribute, pwd );
			}

			modificationItems [ 0 ] = new ModificationItem ( DirContext.REPLACE_ATTRIBUTE, userPasswordAttribute );
			ctx.modifyAttributes ( dn, modificationItems );
			ctx.close();
		} catch (NamingException e) {
			logError("NamingException when trying to change password with username::" + uid, e);
			errors.insert("Cannot change the password");
		}
	}
	
	/**
	 * Find the user dn with its uid
	 * @param uid
	 * @param ctx
	 * @return user's dn
	 */
	private String searchUserDN(String uid, DirContext ctx) {
		if(ctx == null)
			return null;

		List<String> ldapBases = LDAPLoginModule.getLdapBases();
		String[] serachAttr = { "dn" };
		
		String filter = buildSearchUserFilter(uid);
		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(serachAttr);

		String userDN = null;
		for (String ldapBase : ldapBases) {
			try {
				NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
				while (enm.hasMore()) {
					SearchResult result = enm.next();
					userDN = result.getNameInNamespace();
				}
				if (userDN != null) {
					break;
				}
			} catch (NamingException e) {
				logError("NamingException when trying to bind user with username::" + uid + " on ldapBase::" + ldapBase, e);
			}
		}
		
		return userDN;
	}

	/**
	 * Build an LDAP search filter for the given user ID using the preconfigured filters
	 * @param uid the user ID
	 * @return the filter String
	 */
	private String buildSearchUserFilter(String uid) {
		String ldapUserIDAttribute = LDAPLoginModule.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
		String ldapUserFilter = LDAPLoginModule.getLdapUserFilter();
		StringBuilder filter = new StringBuilder();
		if (ldapUserFilter != null) {
			// merge preconfigured filter (e.g. object class, group filters) with username using AND rule
			filter.append("(&").append(ldapUserFilter);	
		}
		filter.append("(").append(ldapUserIDAttribute).append("=").append(uid).append(")");
		if (ldapUserFilter != null) {
			filter.append(")");	
		}
		return filter.toString();
	}

	/**
	 * 
	 * Creates list of all LDAP Users or changed Users since syncTime
	 * 
	 * Configuration: userAttr = olatextconfig.xml (property=userAttrs) LDAP Base =
	 * olatextconfig.xml (property=ldapBase)
	 * 
	 * 
	 * 
	 * @param syncTime The time to search in LDAP for changes since this time.
	 *          SyncTime has to formatted: JJJJMMddHHmm
	 * @param ctx The LDAP system connection, if NULL or closed NamingExecpiton is
	 *          thrown
	 * 
	 * @return Returns list of Arguments of found users or empty list if search
	 *         fails or nothing is changed
	 * 
	 * @throws NamingException
	 */
	public List<Attributes> getUserAttributesModifiedSince(Date syncTime, LdapContext ctx) {
		String userFilter = LDAPLoginModule.getLdapUserFilter();
		StringBuilder filter = new StringBuilder();
		if (syncTime == null) {
			logDebug("LDAP get user attribs since never -> full sync!");
			if (filter != null) {
				filter.append(userFilter);				
			}
		} else {
			String dateFormat = LDAPLoginModule.getLdapDateFormat();
			SimpleDateFormat generalizedTimeFormatter = new SimpleDateFormat(dateFormat);
			generalizedTimeFormatter.setTimeZone(UTC_TIME_ZONE);
			String syncTimeForm = generalizedTimeFormatter.format(syncTime);
			logDebug("LDAP get user attribs since " + syncTime + " -> means search with date restriction-filter: " + syncTimeForm);
			if (userFilter != null) {
				// merge user filter with time fileter using and rule
				filter.append("(&").append(userFilter);				
			}
			filter.append("(|(");								
			filter.append(LDAPLoginModule.getLdapUserLastModifiedTimestampAttribute()).append(">=").append(syncTimeForm);
			filter.append(")(");
			filter.append(LDAPLoginModule.getLdapUserCreatedTimestampAttribute()).append(">=").append(syncTimeForm);
			filter.append("))");
			if (userFilter != null) {
				filter.append(")");				
			}
		}
		final List<Attributes> ldapUserList = new ArrayList<Attributes>();

		searchInLdap(new LdapVisitor() {
			public void visit(SearchResult result) {
				Attributes resAttribs = result.getAttributes();
				logDebug("        found : " + resAttribs.size() + " attributes in result " + result.getName());
				ldapUserList.add(resAttribs);
			}
		}, filter.toString(), LDAPLoginModule.getUserAttrs(), ctx);
		
		logDebug("attrib search returned " + ldapUserList.size() + " results");
		
		return ldapUserList;
	}
	

	/**
	 * Delete all Identities in List and removes them from LDAPSecurityGroup
	 * 
	 * @param identityList List of Identities to delete
	 */
	public void deletIdentities(List<Identity> identityList) {
		SecurityGroup secGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
		
		for (Identity identity:  identityList) {
			securityManager.removeIdentityFromSecurityGroup(identity, secGroup);
			userDeletionManager.deleteIdentity(identity);
			DBFactory.getInstance().intermediateCommit();
		}
	}

	/**
	 * Sync all OLATPropertys in Map of Identity
	 * 
	 * @param olatPropertyMap Map of changed OLAT properties
	 *          (OLATProperty,LDAPValue)
	 * @param identity Identity to sync
	 */
	public void syncUser(Map<String, String> olatPropertyMap, Identity identity) {
		if (identity == null) {
			logWarn("Identiy is null - should not happen", null);
			return;
		}
		User user = identity.getUser();
		// remove user identifyer - can not be changed later
		olatPropertyMap.remove(LDAPConstants.LDAP_USER_IDENTIFYER);
		// remove attributes that are defined as sync-only-on-create
		Set<String> syncOnlyOnCreateProperties = LDAPLoginModule.getSyncOnlyOnCreateProperties();
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
					logDebug("removed property " + propName + " for identity " + identity);
					user.setProperty(propName, value);
				}
			} else {
				user.setProperty(propName, value);
			}
		}

		// Add static user properties from the configuration
		Map<String, String> staticProperties = LDAPLoginModule.getStaticUserProperties();
		if (staticProperties != null && staticProperties.size() > 0) {
			for (Map.Entry<String, String> staticProperty : staticProperties.entrySet()) {
				user.setProperty(staticProperty.getKey(), staticProperty.getValue());
			}
		}
		//fxdiff: FXOLAT-228: update user
		userManager.updateUser(user);
	}

	/**
	 * Creates User in OLAT and ads user to LDAP securityGroup Required Attributes
	 * have to be checked before this method.
	 * 
	 * @param userAttributes Set of LDAP Attribute of User to be created
	 */
	@SuppressWarnings("unchecked")
	public void createAndPersistUser(Attributes userAttributes) {
		// Get and Check Config
		String[] reqAttrs = LDAPLoginModule.checkReqAttr(userAttributes);
		if (reqAttrs != null) {
			logWarn("Can not create and persist user, the following attributes are missing::" + ArrayUtils.toString(reqAttrs), null);
			return;
		}
		
		String uid = getAttributeValue(userAttributes.get(LDAPLoginModule
				.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER)));
		String email = getAttributeValue(userAttributes.get(LDAPLoginModule.mapOlatPropertyToLdapAttribute(UserConstants.EMAIL)));
		// Lookup user
		if (securityManager.findIdentityByName(uid) != null) {
			logError("Can't create user with username='" + uid + "', this username does already exist in OLAT database", null);
			return;
		}
		if (!MailHelper.isValidEmailAddress(email)) {
			// needed to prevent possibly an AssertException in findIdentityByEmail breaking the sync!
			logError("Cannot try to lookup user " + uid + " by email with an invalid email::" + email, null);
			return;
		}
		if (userManager.userExist(email) ) {
			logError("Can't create user with email='" + email + "', a user with that email does already exist in OLAT database", null);
			return;
		}
		
		// Create User (first and lastname is added in next step)
		User user = userManager.createUser(null, null, email);
		// Set User Property's (Iterates over Attributes and gets OLAT Property out
		// of olatexconfig.xml)
		NamingEnumeration<Attribute> neAttr = (NamingEnumeration<Attribute>) userAttributes.getAll();
		try {
			while (neAttr.hasMore()) {
				Attribute attr = neAttr.next();
				String olatProperty = mapLdapAttributeToOlatProperty(attr.getID());
				if (!attr.getID().equalsIgnoreCase(LDAPLoginModule.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER)) ) {
					String ldapValue = getAttributeValue(attr);
					if (olatProperty == null || ldapValue == null) continue;
					user.setProperty(olatProperty, ldapValue);
				} 
			}
			// Add static user properties from the configuration
			Map<String, String> staticProperties = LDAPLoginModule.getStaticUserProperties();
			if (staticProperties != null && staticProperties.size() > 0) {
				for (Entry<String, String> staticProperty : staticProperties.entrySet()) {
					user.setProperty(staticProperty.getKey(), staticProperty.getValue());
				}
			}
		} catch (NamingException e) {
			logError("NamingException when trying to create and persist LDAP user with username::" + uid, e);
			return;
		} catch (Exception e) {
			// catch any exception here to properly log error
			logError("Unknown exception when trying to create and persist LDAP user with username::" + uid, e);
			return;
		}

		// Create Identity
		Identity identity = securityManager.createAndPersistIdentityAndUser(uid, user, LDAPAuthenticationController.PROVIDER_LDAP, uid);
		// Add to SecurityGroup LDAP
		SecurityGroup secGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
		securityManager.addIdentityToSecurityGroup(identity, secGroup);
		// Add to SecurityGroup OLATUSERS
		secGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		securityManager.addIdentityToSecurityGroup(identity, secGroup);
		logInfo("Created LDAP user username::" + uid);

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
	public Map<String, String> prepareUserPropertyForSync(Attributes attributes, Identity identity) {
		Map<String, String> olatPropertyMap = new HashMap<String, String>();
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
				logDebug("propertymap for identity " + identity.getName() + " contains only userID, NOTHING TO SYNC!");
				return null;
			} else {
				logDebug("propertymap for identity " + identity.getName() + " contains " + olatPropertyMap.size() + " items (" + olatPropertyMap.keySet() + ") to be synced later on");
				return olatPropertyMap;
			}

		} catch (NamingException e) {
			logError("NamingException when trying to prepare user properties for LDAP sync", e);
			return null;
		}
	}
	
	/**
	 * Maps LDAP Attributes to the OLAT Property 
	 * 
	 * Configuration: LDAP Attributes Map = olatextconfig.xml (property=userAttrs)
	 * 
	 * @param attrID LDAP Attribute
	 * @return OLAT Property
	 */
	private String mapLdapAttributeToOlatProperty(String attrID) {
		Map<String, String> userAttrMapper = LDAPLoginModule.getUserAttributeMapper();
		String olatProperty = userAttrMapper.get(attrID);
		return olatProperty;
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
	private String getAttributeValue(Attribute attribute) {
		try {
			String attrValue = (String)attribute.get();
			return attrValue;
		} catch (NamingException e) {
			logError("NamingException when trying to get attribute value for attribute::" + attribute, e);
			return null;
		}
	}

	/**
	 * Searches for Identity in OLAT.
	 * 
	 * @param uid Name of Identity
	 * @param errors LDAPError Object if user exits but not member of
	 *          LDAPSecurityGroup
	 * 
	 * @return Identity if it's found and member of LDAPSecurityGroup, null
	 *         otherwise (if user exists but not managed by LDAP, error Object is
	 *         modified)
	 */
	public Identity findIdentyByLdapAuthentication(String uid, LDAPError errors) {
		Identity identity = securityManager.findIdentityByName(uid);
		if (identity == null) {
			return null;
		} else {
			SecurityGroup ldapGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
			if (ldapGroup == null) {
				logError("Error getting user from OLAT security group '" + LDAPConstants.SECURITY_GROUP_LDAP + "' : group does not exist", null);
				return null;
			}
			if (securityManager.isIdentityInSecurityGroup(identity, ldapGroup)) {
				Authentication ldapAuth = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP);
				if(ldapAuth == null) {
					//BUG Fixe: update the user and test if it has a ldap provider
					securityManager.createAndPersistAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, identity.getName(), null, null);
				}
				return identity;
			}
			else {
				if (LDAPLoginModule.isConvertExistingLocalUsersToLDAPUsers()) {
					// Add user to LDAP security group and add the ldap provider
					securityManager.createAndPersistAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, identity.getName(), null, null);
					securityManager.addIdentityToSecurityGroup(identity, ldapGroup);
					logInfo("Found identity by LDAP username that was not yet in LDAP security group. Converted user::" + uid
							+ " to be an LDAP managed user");
					return identity;
				} else {
					errors.insert("findIdentyByLdapAuthentication: User with username::" + uid + " exist but not Managed by LDAP");
					return null;
				}

			}
		}
	}

	/**
	 * 
	 * Creates list of all OLAT Users which have been deleted out of the LDAP
	 * directory but still exits in OLAT
	 * 
	 * Configuration: Required Attributes = olatextconfig.xml (property=reqAttrs)
	 * LDAP Base = olatextconfig.xml (property=ldapBase)
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
	public List<Identity> getIdentitysDeletedInLdap(LdapContext ctx) {
		if (ctx == null) return null;
		// Find all LDAP Users
		String userID = LDAPLoginModule.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
		String userFilter = LDAPLoginModule.getLdapUserFilter();
		final List<String> ldapList = new ArrayList<String>();
		
		searchInLdap(new LdapVisitor() {
			public void visit(SearchResult result) throws NamingException {
				Attributes attrs = result.getAttributes();
				NamingEnumeration<? extends Attribute> aEnum = attrs.getAll();
				while (aEnum.hasMore()) {
					Attribute attr = aEnum.next();
					// use lowercase username
					ldapList.add(attr.get().toString().toLowerCase());
				}
			}
		}, (userFilter == null ? "" : userFilter), new String[] { userID }, ctx);

		if (ldapList.isEmpty()) {
			logWarn("No users in LDAP found, can't create deletionList!!", null);
			return null;
		}

		// Find all User in OLAT, members of LDAPSecurityGroup
		SecurityGroup ldapGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
		if (ldapGroup == null) {
			logError("Error getting users from OLAT security group '" + LDAPConstants.SECURITY_GROUP_LDAP + "' : group does not exist", null);
			return null;
		}

		List<Identity> identityListToDelete = new ArrayList<Identity>();
		List<Identity> olatListIdentity = securityManager.getIdentitiesOfSecurityGroup(ldapGroup);
		for (Identity ida:olatListIdentity) {
			// compare usernames with lowercase
			if (!ldapList.contains(ida.getName().toLowerCase())) {
				identityListToDelete.add(ida);
			}
		}
		return identityListToDelete;
	}
	
	private void searchInLdap(LdapVisitor visitor, String filter, String[] returningAttrs, LdapContext ctx) {
		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(returningAttrs);
		ctls.setCountLimit(0); // set no limits
		
		boolean paging = isPagedResultControlSupported(ctx);
		for (String ldapBase : LDAPLoginModule.getLdapBases()) {
			int counter = 0;
			try {
				if(paging) {
					byte[] cookie = null;
					ctx.setRequestControls(new Control[] { new PagedResultsControl(PAGE_SIZE, Control.CRITICAL) });
					do {
						NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
						while (enm.hasMore()) {
							visitor.visit(enm.next());
						}
				    cookie = getCookie(ctx);
					} while (cookie != null);
				} else {
					ctx.setRequestControls(null); // reset on failure, see FXOLAT-299
					NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
					while (enm.hasMore()) {
						visitor.visit(enm.next());
					}
					counter++;
				}
			} catch (SizeLimitExceededException e) {
				logError("SizeLimitExceededException after "
								+ counter
								+ " records when getting all users from LDAP, reconfigure your LDAP server, hints: http://www.ldapbrowser.com/forum/viewtopic.php?t=14", null);
			} catch (NamingException e) {
				logError("NamingException when trying to fetch deleted users from LDAP using ldapBase::" + ldapBase + " on row::" + counter, e);
			} catch (Exception e) {
				logError("Exception when trying to fetch deleted users from LDAP using ldapBase::" + ldapBase + " on row::" + counter, e);
			}
			logDebug("finished search for ldapBase:: " + ldapBase);
		}
	}
	
	private byte[] getCookie(LdapContext ctx) throws NamingException, IOException {
		byte[] cookie = null;
		// Examine the paged results control response
		Control[] controls = ctx.getResponseControls();
		if (controls != null) {
		  for (int i = 0; i < controls.length; i++) {
		    if (controls[i] instanceof PagedResultsResponseControl) {
		      PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
		      cookie = prrc.getCookie();
		    }
		  }
		}
		// Re-activate paged results
		ctx.setRequestControls(new Control[] { new PagedResultsControl(PAGE_SIZE, cookie, Control.CRITICAL) });
		return cookie;
	}
	
	private boolean isPagedResultControlSupported(LdapContext ctx) {
		// FXOLAT-299, might return false on 2nd execution
		if (pagingSupportedAlreadyFound == true) return true;
		try {
			SearchControls ctl = new SearchControls();
			ctl.setReturningAttributes(new String[]{"supportedControl"});
			ctl.setSearchScope(SearchControls.OBJECT_SCOPE);

			/* search for the rootDSE object */
			NamingEnumeration<SearchResult> results = ctx.search("", "(objectClass=*)", ctl);

			while(results.hasMore()){
				SearchResult entry = results.next();
				NamingEnumeration<? extends Attribute> attrs = entry.getAttributes().getAll();
				while (attrs.hasMore()){
					Attribute attr = attrs.next();
					NamingEnumeration<?> vals = attr.getAll();
					while (vals.hasMore()){
						String value = (String) vals.next();
						if(value.equals(PAGED_RESULT_CONTROL_OID))
							pagingSupportedAlreadyFound = true;
							return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			logError("Exception when trying to know if the server support paged results.", e);
			return false;
		}
	}

	/**
	 * Execute Batch Sync. Will update all Attributes of LDAP users in OLAt, create new users and delete users in OLAT.
	 * Can be configured in olatextconfig.xml
	 * 
	 * @param LDAPError
	 * 
	 */
	public boolean doBatchSync(LDAPError errors) {
		//fxdiff: also run on nodes != 1 as nodeid = tomcat-id in fx-environment
//		if(WebappHelper.getNodeId() != 1) {
//			logWarn("Sync happens only on node 1", null);
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
				logInfo("LDAP user doBatchSync started, but another job is still running - skipping this sync");
				errors.insert("BatchSync already running by concurrent process");
				return false;
			}
		}
		
		WorkThreadInformations.setLongRunningTask("ldapSync");
		
		coordinator.getEventBus().fireEventToListenersOf(new LDAPEvent(LDAPEvent.SYNCHING), ldapSyncLockOres);
		
		LdapContext ctx = null;
		boolean success = false;
		try {
			acquireSyncLock();
			ctx = bindSystem();
			if (ctx == null) {
				errors.insert("LDAP connection ERROR");
				logError("Error in LDAP batch sync: LDAP connection empty", null);
				freeSyncLock();
				success = false;
				return success;
			}
			Date timeBeforeSync = new Date();

			//check server capabilities
			// Get time before sync to have a save sync time when sync is successful
			String sinceSentence = (lastSyncDate == null ? " (full sync)" : " since last sync from " + lastSyncDate);
			doBatchSyncDeletedUsers(ctx, sinceSentence);
			// fxdiff: see FXOLAT-299
			// bind again to use an initial unmodified context. lookup of server-properties might fail otherwise!
			ctx = bindSystem();
			doBatchSyncNewAndModifiedUsers(ctx, sinceSentence, errors);
			
			// update sync time and set running flag
			lastSyncDate = timeBeforeSync;
			
			ctx.close();
			success = true;
			return success;
		} catch (Exception e) {

			errors.insert("Unknown error");
			logError("Error in LDAP batch sync, unknown reason", e);
			success = false;
			return success;
		} finally {
			WorkThreadInformations.unsetLongRunningTask("ldapSync");
			freeSyncLock();
			if(ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					//try but failed silently
				}
			}
			LDAPEvent endEvent = new LDAPEvent(LDAPEvent.SYNCHING_ENDED);
			endEvent.setTimestamp(new Date());
			endEvent.setSuccess(success);
			endEvent.setErrors(errors);
			coordinator.getEventBus().fireEventToListenersOf(endEvent, ldapSyncLockOres);
		}
	}
	
	private void doBatchSyncDeletedUsers(LdapContext ctx, String sinceSentence) {
		// create User to Delete List
		List<Identity> deletedUserList = getIdentitysDeletedInLdap(ctx);
		// delete old users
		if (deletedUserList == null || deletedUserList.size() == 0) {
			logInfo("LDAP batch sync: no users to delete" + sinceSentence);
		} else {
			if (LDAPLoginModule.isDeleteRemovedLDAPUsersOnSync()) {
				// check if more not more than the defined percentages of
				// users managed in LDAP should be deleted
				// if they are over the percentage, they will not be deleted
				// by the sync job
				SecurityGroup ldapGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
				List<Identity> olatListIdentity = securityManager.getIdentitiesOfSecurityGroup(ldapGroup);
				if (olatListIdentity.isEmpty())
					logInfo("No users managed by LDAP, can't delete users");
				else {
					int prozente = (int) (((float)deletedUserList.size() / (float) olatListIdentity.size())*100);
					if (prozente >= LDAPLoginModule.getDeleteRemovedLDAPUsersPercentage()) {
						logInfo("LDAP batch sync: more than "
										+ LDAPLoginModule.getDeleteRemovedLDAPUsersPercentage()
										+ "% of LDAP managed users should be deleted. Please use Admin Deletion Job. Or increase deleteRemovedLDAPUsersPercentage. "
										+ prozente
										+ "% tried to delete.");
					} else {
						// delete users
						deletIdentities(deletedUserList);
						logInfo("LDAP batch sync: "
								+ deletedUserList.size() + " users deleted"
								+ sinceSentence);
					}
				}
			} else {
				// Do nothing, only log users to logfile
				StringBuilder users = new StringBuilder();
				for (Identity toBeDeleted : deletedUserList) {
					users.append(toBeDeleted.getName()).append(',');
				}
				logInfo("LDAP batch sync: "
					+ deletedUserList.size()
					+ " users detected as to be deleted"
					+ sinceSentence
					+ ". Automatic deleting is disabled in LDAPLoginModule, delete these users manually::["
					+ users.toString() + "]");
			}
		}
	}
	
	private void doBatchSyncNewAndModifiedUsers(LdapContext ctx, String sinceSentence, LDAPError errors) {
		// Get new and modified users from LDAP
		int count = 0;
		List<Attributes> ldapUserList = getUserAttributesModifiedSince(lastSyncDate, ctx);
		
		// Check for new and modified users
		List<Attributes> newLdapUserList = new ArrayList<Attributes>();
		Map<Identity, Map<String, String>> changedMapIdentityMap = new HashMap<Identity, Map<String, String>>();
		for (Attributes userAttrs: ldapUserList) {
			String user = null;
			try {				
				user = getAttributeValue(userAttrs.get(LDAPLoginModule.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER)));
				Identity identity = findIdentyByLdapAuthentication(user, errors);
				if (identity != null) {
					Map<String, String> changedAttrMap = prepareUserPropertyForSync(userAttrs, identity);
					if (changedAttrMap != null) changedMapIdentityMap.put(identity, changedAttrMap);
				} else if (errors.isEmpty()) {
					String[] reqAttrs = LDAPLoginModule.checkReqAttr(userAttrs);
					if (reqAttrs == null) {
						newLdapUserList.add(userAttrs);
					}
					else logWarn("Error in LDAP batch sync: can't create user with username::" + user + " : missing required attributes::"
							+ ArrayUtils.toString(reqAttrs), null);
				} else {
					logWarn(errors.get(), null);
				}
				if(++count % 20 == 0) {
					DBFactory.getInstance().intermediateCommit();
				}
			}	catch (Exception e) {
				// catch here to go on with other users on exeptions!
				logError("some error occured in looping over set of changed user-attributes, actual user " + user + ". Will still continue with others.", e);
			}
		}
		
		// sync existing users
		if (changedMapIdentityMap == null || changedMapIdentityMap.isEmpty()) {
			logInfo("LDAP batch sync: no users to sync" + sinceSentence);
		} else {
			for (Identity ident : changedMapIdentityMap.keySet()) {
				// sync user is exception save, no try/catch needed
				syncUser(changedMapIdentityMap.get(ident), ident);
				//REVIEW Identity are not saved???
				if(++count % 20 == 0) {
					DBFactory.getInstance().intermediateCommit();
				}
			}
			logInfo("LDAP batch sync: " + changedMapIdentityMap.size() + " users synced" + sinceSentence);
		}
		
		// create new users
		if (newLdapUserList.isEmpty()) {
			logInfo("LDAP batch sync: no users to create" + sinceSentence);
		} else {			
			for (Attributes userAttrs: newLdapUserList) {
				try {
					createAndPersistUser(userAttrs);
					if(++count % 20 == 0) {
						DBFactory.getInstance().intermediateCommit();
					}
				} catch (Exception e) {
					// catch here to go on with other users on exeptions!
					logError("some error occured while creating new users, actual userAttribs " + userAttrs + ". Will still continue with others.", e);
				}
			}
			logInfo("LDAP batch sync: " + newLdapUserList.size() + " users created" + sinceSentence);
		}
		
		//fxdiff: FXOLAT-228: update user
		DBFactory.getInstance().intermediateCommit();
	}
	
	
	// TODO: not finished!
	public void doSyncSingleUser(Identity ident){
		LdapContext ctx = bindSystem();
		if (ctx == null) {
			logError("could not bind to ldap", null);
		}		
		String userDN = searchUserDN(ident.getName(), ctx);

		final List<Attributes> ldapUserList = new ArrayList<Attributes>();
		// TODO: use userDN instead of filter to get users attribs
		searchInLdap(new LdapVisitor() {
			public void visit(SearchResult result) {
				Attributes resAttribs = result.getAttributes();
				logDebug("        found : " + resAttribs.size() + " attributes in result " + result.getName());
				ldapUserList.add(resAttribs);
			}
		}, userDN, LDAPLoginModule.getUserAttrs(), ctx);
		
		Attributes attrs = ldapUserList.get(0);
		Map<String, String> olatProToSync = prepareUserPropertyForSync(attrs, ident);
		if (olatProToSync != null) {
			syncUser(olatProToSync, ident);
		}
	}

	/**
	 * @see org.olat.ldap.LDAPLoginManager#getLastSyncDate()
	 */
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
		System.setProperty("javax.net.ssl.trustStore", LDAPLoginModule.getTrustStoreLocation());
	}
	
	/**
	 * Acquire lock for administration jobs
	 * 
	 */
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
	public synchronized void freeSyncLock() {
		batchSyncIsRunning=false;
	}
	
	public interface LdapVisitor {
		public void visit(SearchResult searchResult) throws NamingException ;
	}

	/**
	 * remove all cached authentications for fallback-login. useful if users logged in first with a default pw and changed it outside in AD/LDAP, but OLAT doesn't know about.
	 * removing fallback-auths means login is only possible by AD/LDAP and if server is reachable!
	 * see FXOLAT-284
	 */
	@Override
	public void removeFallBackAuthentications() {
		if (LDAPLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()){
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			SecurityGroup ldapGroup = secMgr.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
			if (ldapGroup == null) {
				logError("Error getting user from OLAT security group '" + LDAPConstants.SECURITY_GROUP_LDAP + "' : group does not exist", null);
			}
			List<Identity> ldapIdents = secMgr.getIdentitiesOfSecurityGroup(ldapGroup);
			logInfo("found " + ldapIdents.size() + " identies in ldap security group");
			int count=0;
			for (Identity identity : ldapIdents) {
				Authentication auth = secMgr.findAuthentication(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier());				
				if (auth!=null){
					secMgr.deleteAuthentication(auth);
					count++;
				}
				if (count % 20 == 0){
					DBFactory.getInstance().intermediateCommit();
				}
			}
			logInfo("removed cached authentications (fallback login provider: " + BaseSecurityModule.getDefaultAuthProviderIdentifier() + ") for " + count + " users.");			
		}
	}

	@Override
	public boolean isIdentityInLDAPSecGroup(Identity ident) {
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		SecurityGroup ldapSecurityGroup = secMgr.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
		return ldapSecurityGroup != null && secMgr.isIdentityInSecurityGroup(ident, ldapSecurityGroup);
	}
}