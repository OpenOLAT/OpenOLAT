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

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.directory.Attributes;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.configuration.Initializable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * Description: 
 * This Module loads all needed configuration for the LDAP Login. 
 * All configuration is done in the spring olatextconfig.xml file.
 * <p>
 * LDAPLoginModule
 * <p>
 * 
 * @author maurus.rohrer@gmail.com
 */
public class LDAPLoginModule implements Initializable {
	// Connection configuration
	private static String ldapUrl;
	private static boolean ldapEnabled;
	private static boolean activeDirectory;
	private static String ldapDateFormat;
	//SSL configuration
	private static boolean sslEnabled;
	private static String trustStoreLoc;
	private static String trustStorePass;
	private static String trustStoreTyp;
	// System user: used for getting all users and connection testing
	private static String systemDN;
	private static String systemPW;
	// List of bases where to find users
	private static List<String> ldapBases;
	// Use a valid ldap password and save it as olat password to reduce dependency
	// to LDAP server availability and allow WeDAV access
	private static boolean cacheLDAPPwdAsOLATPwdOnLogin;
	// When the system detects an LDAP user that does already exist in OLAT but is not marked
	// as LDAP user, the OLAT user can be converted to an LDAP managed user. 
	// When enabling this feature you should make sure that you don't have a user 'administrator'
	// in your ldapBases (not a problem but not recommended)
	private static boolean convertExistingLocalUsersToLDAPUsers;
	// Users that have been created vial LDAP sync but now can't be found on the LDAP anymore
	// can be deleted automatically. If unsure, set to false and delete those users manually
	// in the user management.
	private static boolean deleteRemovedLDAPUsersOnSync;
	// LDAP sync will not delete users if more than deleteRemovedLDAPUserPercentage are found to be deleted.
	private static int deleteRemovedLDAPUsersPercentage;
	// Propagate the password changes onto the LDAP server
	private static boolean propagatePasswordChangedOnLdapServer;
	// Configuration for syncing user attributes
	private static String ldapUserObjectClass;
	private static String ldapUserCreatedTimestampAttribute;
	private static String ldapUserLastModifiedTimestampAttribute;
	private static String ldapUserPasswordAttribute;
	// Should users be created and synchronized automatically? If you set this
	// configuration to false, the users will be generated on-the-fly when they
	// log in
	private static boolean ldapSyncOnStartup;
	private static boolean ldapSyncCronSync;
	private static String ldapSyncCronSyncExpression;
	// User LDAP attributes to be synced and a map with the mandatory attributes
	private static Map<String, String> userAttrMap;
	private static Map<String, String> reqAttr;
	private static Set<String> syncOnlyOnCreateProperties;
	private static String[] userAttr;
	// Static user properties that should be added to user when syncing
	private static Map<String, String> staticUserProperties;
	private static OLog log = Tracing.createLoggerFor(LDAPLoginModule.class);
	
	private final Scheduler scheduler;
	private final BaseSecurity securityManager;
	private final LDAPLoginManager ldapManager;
	private final UserManager userManager;
	
	/**
	 * [used by spring]
	 */
	private LDAPLoginModule(LDAPLoginManager ldapManager, BaseSecurity securityManager, UserManager userManager, Scheduler scheduler) {
		this.ldapManager = ldapManager;
		this.securityManager = securityManager;
		this.userManager = userManager;
		this.scheduler = scheduler;
	}

	/**
	 * @see org.olat.core.configuration.Initializable#init()
	 */
	public void init() {
		// Check if LDAP is enabled
		if (!isLDAPEnabled()) {
			log.info("LDAP login is disabled");
			return;
		}
		// Create LDAP Security Group if not existing. Used to identify users that
		// have to be synced with LDAP
		SecurityGroup ldapGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
		if (ldapGroup == null) {
			ldapGroup = securityManager.createAndPersistNamedSecurityGroup(LDAPConstants.SECURITY_GROUP_LDAP);
		}
		// check for valid configuration
		if (!checkConfigParameterIsNotEmpty(ldapUrl)) return;
		if (!checkConfigParameterIsNotEmpty(systemDN)) return;
		if (!checkConfigParameterIsNotEmpty(systemPW)) return;
		if (ldapBases == null || ldapBases.size() == 0) {
			log
					.error("Missing configuration 'ldapBases'. Add at least one LDAP Base to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if (!checkConfigParameterIsNotEmpty(ldapUserObjectClass)) return;
		if (!checkConfigParameterIsNotEmpty(ldapUserCreatedTimestampAttribute)) return;
		if (!checkConfigParameterIsNotEmpty(ldapUserLastModifiedTimestampAttribute)) return;
		if (userAttrMap == null || userAttrMap.size() == 0) {
			log
					.error("Missing configuration 'userAttrMap'. Add at least the email propery to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if (reqAttr == null || reqAttr.size() == 0) {
			log
					.error("Missing configuration 'reqAttr'. Add at least the email propery to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		// check if OLAT user properties is defined in olat_userconfig.xml, if not disable the LDAP module
		if(!checkIfOlatPropertiesExists(userAttrMap)){
			log.error("Invalid LDAP OLAT properties mapping configuration (userAttrMap). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if(!checkIfOlatPropertiesExists(reqAttr)){
			log.error("Invalid LDAP OLAT properties mapping configuration (reqAttr). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if(syncOnlyOnCreateProperties != null && !checkIfStaticOlatPropertiesExists(syncOnlyOnCreateProperties)){
			log.error("Invalid LDAP OLAT syncOnlyOnCreateProperties configuration. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if( staticUserProperties != null && !checkIfStaticOlatPropertiesExists(staticUserProperties.keySet())){
			log.error("Invalid static OLAT properties configuration (staticUserProperties). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		
		// check SSL certifications, throws Startup Exception if certificate is not found
		if(isSslEnabled()){
			if (!checkServerCertValidity(0))
				throw new StartupException("LDAP enabled but no valid server certificate found. Please fix!");
			if (!checkServerCertValidity(30))
				log.warn("Server Certificate will expire in less than 30 days.");
		}
		
		// Check ldap connection
		if (ldapManager.bindSystem() == null) {
			// don't disable ldap, maybe just a temporary problem, but still report
			// problem in logfile
			log.warn("LDAP connection test failed during module initialization, edit config or contact network administrator");
		}
		// OK, everything finished checkes passed
		log.info("LDAP login is enabled");

		/*
		 * 
		 */
		
		// Sync LDAP Users on Startup
		if (isLdapSyncOnStartup()) {
			initStartSyncJob();
		} else {
			log.info("LDAP start sync is disabled");
		}

		// Start LDAP cron sync job
		if (isLdapSyncCronSync()) {
			initCronSyncJob();
		} else {
			log.info("LDAP cron sync is disabled");
		}
		
		// OK, everything finished checkes passed
		log.info("LDAP login is enabled");
	}

	/**
	 * Internal helper to sync users right away
	 * @param ldapManager
	 */
	private void initStartSyncJob() {
		LDAPError errors = new LDAPError();
		if (ldapManager.doBatchSync(errors)) {
			log.info("LDAP start sync: users synced");
		} else {
			log.warn("LDAP start sync error: " + errors.get());
		}
	}

	/**
	 * Internal helper to initialize the cron syncer job
	 */
	private void initCronSyncJob() {
		try {
			// Create job with cron trigger configuration
			JobDetail jobDetail = new JobDetail("LDAP_Cron_Syncer_Job", Scheduler.DEFAULT_GROUP, LDAPUserSynchronizerJob.class);
			CronTrigger trigger = new CronTrigger();
			trigger.setName("LDAP_Cron_Syncer_Trigger");
			trigger.setCronExpression(ldapSyncCronSyncExpression);
			// Schedule job now
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("LDAP cron syncer is enabled with expression::" + ldapSyncCronSyncExpression);
		} catch (ParseException e) {
			setLdapSyncCronSync(false);
			log
					.error(
							"LDAP configuration in attribute 'ldapSyncCronSyncExpression' is not valid ("
									+ ldapSyncCronSyncExpression
									+ "). See http://quartz.sourceforge.net/javadoc/org/quartz/CronTrigger.html to learn more about the cron syntax. Disabling LDAP cron syncing",
							e);
		} catch (SchedulerException e) {
			log.error("Error while scheduling LDAP cron sync job. Disabling LDAP cron syncing", e);
		}
	}
	
	/**
	 * Maps OLAT Property to the LDAP Attributes 
	 * 
	 * Configuration: LDAP Attributes Map = olatextconfig.xml (property=userAttrs)
	 * 
	 * @param olatProperty OLAT PropertyattrID 
	 * @return LDAP Attribute
	 */
	public static String mapOlatPropertyToLdapAttribute(String olatProperty) {
		Map<String, String> userAttrMapper = getReqAttrs();
		if (userAttrMapper.containsValue(olatProperty)) {
			Iterator<String> itr = userAttrMapper.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				if (userAttrMapper.get(key).compareTo(olatProperty) == 0) return key;
			}
		}
		return null;
	}
	
	/**
	 * Checks if Collection of naming Attributes contain defined required properties for OLAT
	 * 
	 * 	 * Configuration: LDAP Required Map = olatextconfig.xml (property=reqAttrs)
	 * 
	 * @param attributes Collection of LDAP Naming Attribute 
	 * @return null If all required Attributes are found, otherwise String[] of missing Attributes
	 * 
	 */
	public static String[] checkReqAttr(Attributes attrs) {
		Map<String, String> reqAttrMap = getReqAttrs();
		String[] missingAttr = new String[reqAttrMap.size()];
		int y = 0;
		for (String attKey : reqAttrMap.keySet()) {
			attKey = attKey.trim();
			if (attrs.get(attKey) == null) {
				missingAttr[y++] = attKey;
			}
		}
		if (y == 0) return null;
		else return missingAttr;
	}
	
	/**
	 * Checks if defined OLAT Properties in olatextconfig.xml exist in OLAT.
	 * 
	 * 	 Configuration: LDAP Attributes Map = olatextconfig.xml (property=reqAttrs, property=userAttributeMapper)
	 * 
	 * @param attrs Map of OLAT Properties from of the LDAP configuration 
	 * @return true All exist OK, false Error
	 * 
	 */
	private boolean checkIfOlatPropertiesExists(Map<String, String> attrs) {
		List<UserPropertyHandler> upHandler = userManager.getAllUserPropertyHandlers();
		for (String ldapAttribute : attrs.keySet()) {
			boolean propertyExists = false;
			String olatProperty = attrs.get(ldapAttribute);
			if (olatProperty.equals(LDAPConstants.LDAP_USER_IDENTIFYER)) {
				// LDAP user identifyer is not a user propery, it's the username
				continue;
			}
			for (UserPropertyHandler userPropItr : upHandler) {
				if (olatProperty.equals(userPropItr.getName())) {
					// ok, this property exist, continue with next one
					propertyExists = true;
					break;
				}
			}
			if ( ! propertyExists ) {
				log
						.error("Error in checkIfOlatPropertiesExists(): configured LDAP attribute::"
								+ ldapAttribute
								+ " configured to map to OLAT user property::"
								+ olatProperty
								+ " but no such user property configured in olat_userconfig.xml");
				return false;				
			}
		}
		return true;
	}

	/**
	 * Checks if defined Static OLAT Property in olatextconfig.xml exist in OLAT.
	 * 
	 * 	 Configuration: olatextconfig.xml (property=staticUserProperties)
	 * 
	 * @param olatProperties Set of OLAT Properties from of the LDAP configuration 
	 * @return true All exist OK, false Error
	 * 
	 */
	private boolean checkIfStaticOlatPropertiesExists(Set<String> olatProperties) {
		List<UserPropertyHandler> upHandler = userManager.getAllUserPropertyHandlers();
		for (String olatProperty : olatProperties) {
			boolean propertyExists = false;
			for (UserPropertyHandler userPropItr : upHandler) {
				if (olatProperty.equals(userPropItr.getName())) {
					// ok, this property exist, continue with next one
					propertyExists = true;
					break;
				}
			}
			if ( ! propertyExists ) {
				log
				.error("Error in checkIfStaticOlatPropertiesExists(): configured static OLAT user property::"
						+ olatProperty
						+ " is not configured in olat_userconfig.xml");
				return false;				
			}			
		}
		return true;
	}
	
	/**
	 * Checks if SSL certification is know and accepted by Java JRE.
	 * 
	 * 
	 * @param dayFromNow Checks expiration 
	 * @return true Certification accepted, false No valid certification
	 * 
	 * @throws Exception
	 * 
	 */
	private static boolean checkServerCertValidity(int daysFromNow) {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(getTrustStoreType());
			keyStore.load(new FileInputStream(getTrustStoreLocation()), (getTrustStorePwd() != null) ? getTrustStorePwd().toCharArray() : null);
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				Certificate cert = keyStore.getCertificate(alias);
				if (cert instanceof X509Certificate) {
					return isCertificateValid((X509Certificate)cert, daysFromNow);
				}
			}
		}	catch (Exception e) {
			return false;
		}
		return false;
	}
	
	private static boolean isCertificateValid(X509Certificate x509Cert, int daysFromNow) {
		try {
			x509Cert.checkValidity();
			if (daysFromNow > 0) {
				Date nowPlusDays = new Date(System.currentTimeMillis() + (new Long(daysFromNow).longValue() * 24l * 60l * 60l * 1000l));
				x509Cert.checkValidity(nowPlusDays);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Internal helper to check for emtpy config variables
	 * 
	 * @param param
	 * @return true: not empty; false: empty or null
	 */
	private boolean checkConfigParameterIsNotEmpty(String param) {
		if (StringHelper.containsNonWhitespace(param)) {
			return true;
		} else {
			log.error("Missing configuration '" + param + "'. Add this configuration to olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return false;
		}
	}

	/*
	 * Spring setter methods - don't use them to modify values at runtime!
	 */
	public void setEnableLDAPLogins(boolean enableLDAPLogins) {
		ldapEnabled = enableLDAPLogins;
	}

	public void setSslEnabled(boolean sslEnabl) {
		sslEnabled = sslEnabl;
	}
	
	public void setActiveDirectory(boolean aDirectory) {
		activeDirectory = aDirectory;
	}
	
	public void setLdapDateFormat(String dateFormat) {
		ldapDateFormat = dateFormat;
	}
	
	public void setTrustStoreLocation(String trustStoreLocation){
		trustStoreLoc=trustStoreLocation.trim();
	}
	public void setTrustStorePwd(String trustStorePwd){
		trustStorePass=trustStorePwd.trim();
	}
	
	public void setTrustStoreType(String trustStoreType){
		trustStoreTyp= trustStoreType.trim();
	}

	public void setLdapSyncOnStartup(boolean ldapStartSyncs) {
		ldapSyncOnStartup = ldapStartSyncs;
	}

	public void setLdapUserObjectClass(String objectClass) {
		ldapUserObjectClass = objectClass.trim();
	}

	public void setLdapSystemDN(String ldapSystemDN) {
		systemDN = ldapSystemDN.trim();
	}

	public void setLdapSystemPW(String ldapSystemPW) {
		systemPW = ldapSystemPW.trim();
	}

	public void setLdapUrl(String ldapUrlConfig) {
		ldapUrl = ldapUrlConfig.trim();
	}

	public void setLdapBases(List<String> ldapBasesConfig) {
		// fxdiff: FXOLAT-141 allow setting in one line
		ArrayList<String> listToUse = new ArrayList<String>();
		if (ldapBasesConfig != null) {
			for (String baseEntry : ldapBasesConfig) {
				if (StringHelper.containsNonWhitespace(baseEntry) && baseEntry.contains("!#")) {
					String[] oneLineList = baseEntry.split("!#");
					List<String> oneLineListArr = Arrays.asList(oneLineList);
					for (String oneLineEntry : oneLineListArr) {
						if (StringHelper.containsNonWhitespace(oneLineEntry)) {
							listToUse.add(oneLineEntry.trim());
						}
					}
				} else {
					listToUse.add(baseEntry.trim());
				}
			}
		}
		ldapBases = listToUse;
	}

	public void setUserAttributeMapper(Map<String, String> userAttributeMapper) {
		// trim map
		userAttrMap = new HashMap<String, String>();
		for (Entry<String, String>  entry : userAttributeMapper.entrySet()) {
			String ldapAttrib = entry.getKey().trim();
			String olatProp = entry.getValue().trim();
			if (StringHelper.containsNonWhitespace(ldapAttrib) && StringHelper.containsNonWhitespace(olatProp)){
				userAttrMap.put(ldapAttrib, olatProp);
			}
		}		
		// optimizes for later usage
		userAttr = userAttrMap.keySet().toArray(new String[userAttrMap.size()]);
	}

	public void setReqAttrs(Map<String, String> reqAttrs) {
		// trim map
		reqAttr = new HashMap<String, String>();
		for (Entry<String, String>  entry : reqAttrs.entrySet()) {
			reqAttr.put(entry.getKey().trim(), entry.getValue().trim());
		}		
	}
	
	public void setSyncOnlyOnCreateProperties(Set<String> syncOnlyOnCreatePropertiesConfig) {
		// trim map
		syncOnlyOnCreateProperties = new HashSet<String>();
		for (String value : syncOnlyOnCreatePropertiesConfig) {
			if (StringHelper.containsNonWhitespace(value)){
				syncOnlyOnCreateProperties.add(value.trim());
			}
		}		
	}

	public void setStaticUserProperties(Map<String, String> staticUserPropertiesMap) {
		// trim map
		staticUserProperties = new HashMap<String, String>();
		for (Entry<String, String>  entry : staticUserPropertiesMap.entrySet()) {
			String olatPropKey = entry.getKey().trim();
			String staticValue = entry.getValue().trim();
			if (StringHelper.containsNonWhitespace(olatPropKey) && StringHelper.containsNonWhitespace(staticValue)){
				staticUserProperties.put(olatPropKey, staticValue);
			}
		}		
	}

	public void setLdapUserLastModifiedTimestampAttribute(String ldapUserLastModifiedTimestampAttribute) {
		LDAPLoginModule.ldapUserLastModifiedTimestampAttribute = ldapUserLastModifiedTimestampAttribute.trim();
	}

	public void setLdapUserCreatedTimestampAttribute(String ldapUserCreatedTimestampAttribute) {
		LDAPLoginModule.ldapUserCreatedTimestampAttribute = ldapUserCreatedTimestampAttribute.trim();
	}
	
	public void setLdapUserPasswordAttribute(String userPasswordAttribute) {
		LDAPLoginModule.ldapUserPasswordAttribute = userPasswordAttribute;
	}

	public void setLdapSyncCronSync(boolean ldapSyncCronSync) {
		LDAPLoginModule.ldapSyncCronSync = ldapSyncCronSync;
	}

	public void setLdapSyncCronSyncExpression(String ldapSyncCronSyncExpression) {
		LDAPLoginModule.ldapSyncCronSyncExpression = ldapSyncCronSyncExpression.trim();
	}
	
	public void setCacheLDAPPwdAsOLATPwdOnLogin(boolean cacheLDAPPwdAsOLATPwdOnLogin) {
		LDAPLoginModule.cacheLDAPPwdAsOLATPwdOnLogin = cacheLDAPPwdAsOLATPwdOnLogin;
	}
	
	public void setConvertExistingLocalUsersToLDAPUsers(boolean convertExistingLocalUsersToLDAPUsers) {
		LDAPLoginModule.convertExistingLocalUsersToLDAPUsers = convertExistingLocalUsersToLDAPUsers;
	}

	public void setDeleteRemovedLDAPUsersOnSync(boolean deleteRemovedLDAPUsersOnSync) {
		LDAPLoginModule.deleteRemovedLDAPUsersOnSync = deleteRemovedLDAPUsersOnSync;
	}
	
	public void setDeleteRemovedLDAPUsersPercentage(int deleteRemovedLDAPUsersPercentage){
		LDAPLoginModule.deleteRemovedLDAPUsersPercentage = deleteRemovedLDAPUsersPercentage;
	}

	public void setPropagatePasswordChangedOnLdapServer(boolean propagatePasswordChangedOnServer) {
		LDAPLoginModule.propagatePasswordChangedOnLdapServer = propagatePasswordChangedOnServer;
	}

	/*
	 * Getters
	 */
	public static String getLdapSystemDN() {
		return systemDN;
	}

	public static String getLdapSystemPW() {
		return systemPW;
	}

	public static String getLdapUrl() {
		return ldapUrl;
	}

	public static List<String> getLdapBases() {
		return ldapBases;
	}

	public static String getLdapUserObjectClass() {
		return ldapUserObjectClass;
	}

	public static String getLdapUserLastModifiedTimestampAttribute() {
		return ldapUserLastModifiedTimestampAttribute;
	}

	public static String getLdapUserCreatedTimestampAttribute() {
		return ldapUserCreatedTimestampAttribute;
	}
	
	public static String getLdapUserPasswordAttribute() {
		return ldapUserPasswordAttribute;
	}

	/**
	 * @return a map of user properties to set for each LDAP user or NULL if no
	 *         such properties have to be set
	 */
	public static Map<String, String> getStaticUserProperties() {
		return staticUserProperties;
	}

	public static Map<String, String> getUserAttributeMapper() {
		return userAttrMap;
	}

	public static String[] getUserAttrs() {
		return userAttr;
	}

	public static Map<String, String> getReqAttrs() {
		return reqAttr;
	}

	public static Set<String> getSyncOnlyOnCreateProperties() {
		return syncOnlyOnCreateProperties;
	}

	public static boolean isLDAPEnabled() {
		return ldapEnabled;
	}

	public static boolean isSslEnabled() {
		return sslEnabled;
	}
	
	public static boolean isActiveDirectory() {
		return activeDirectory;
	}
	
	public static String getLdapDateFormat() {
		if(StringHelper.containsNonWhitespace(ldapDateFormat)) {
			return ldapDateFormat;
		}
		return "yyyyMMddHHmmss'Z'";//default
	}
	
	public static String getTrustStoreLocation(){
		return trustStoreLoc;
	}
	
	public static String getTrustStorePwd(){
		return trustStorePass;
	}
	
	public static String getTrustStoreType(){
		return trustStoreTyp;
	}

	public static boolean isLdapSyncOnStartup() {
		return ldapSyncOnStartup;
	}

	public static boolean isLdapSyncCronSync() {
		return ldapSyncCronSync;
	}

	public static String getLdapSyncCronSyncExpression() {
		return ldapSyncCronSyncExpression;
	}

	public static boolean isCacheLDAPPwdAsOLATPwdOnLogin() {
		return cacheLDAPPwdAsOLATPwdOnLogin;
	}

	public static boolean isConvertExistingLocalUsersToLDAPUsers() {
		return convertExistingLocalUsersToLDAPUsers;
	}

	public static boolean isDeleteRemovedLDAPUsersOnSync() {
		return deleteRemovedLDAPUsersOnSync;
	}
	
	public static int getDeleteRemovedLDAPUsersPercentage(){
		return deleteRemovedLDAPUsersPercentage;
	}

	public static boolean isPropagatePasswordChangedOnLdapServer(){
		return propagatePasswordChangedOnLdapServer;
	}
}
