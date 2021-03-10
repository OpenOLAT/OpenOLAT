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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
@Service("org.olat.ldap.LDAPLoginModule")
public class LDAPLoginModule extends AbstractSpringModule {
	// Connection configuration
	
	public static final long WARNING_LIMIT = 15 *1000 * 1000 * 1000;
	
	@Value("${ldap.ldapUrl}")
	private String ldapUrl;
	@Value("${ldap.enable:false}")
	private boolean ldapEnabled;
	@Value("${ldap.activeDirectory:false}")
	private boolean activeDirectory;
	@Value("${ldap.dateFormat}")
	private String ldapDateFormat;
	
	//SSL configuration
	@Value("${ldap.sslEnabled}")
	private boolean sslEnabled;
	@Value("${ldap.trustStoreLocation}")
	private String trustStoreLoc;
	@Value("${ldap.trustStorePwd}")
	private String trustStorePass;
	@Value("${ldap.trustStoreType}")
	private String trustStoreTyp;
	
	// System user: used for getting all users and connection testing
	@Value("${ldap.ldapSystemDN}")
	private String systemDN;
	@Value("${ldap.ldapSystemPW}")
	private String systemPW;
	@Value("${ldap.connectionTimeout}")
	private Integer connectionTimeout;
	@Value("${ldap.batch.size:50}")
	private Integer batchSize;
	/**
	 * Create LDAP users on the fly when authenticated successfully
	 */
	@Value("${ldap.ldapCreateUsersOnLogin}")
	private boolean createUsersOnLogin;
	/**
	 * When users log in via LDAP, the system can keep a copy of the password as encrypted
	 * hash in the database. This makes OLAT more independent from an offline LDAP server 
	 * and users can use their LDAP password to use the WebDAV functionality.
	 * When setting to true (recommended), make sure you configured pwdchange=false in the
	 * org.olat.user.UserModule olat.propertes.
	 */
	@Value("${ldap.cacheLDAPPwdAsOLATPwdOnLogin}")
	private boolean cacheLDAPPwdAsOLATPwdOnLogin;
	/**
	 * Try to fallback to OLAT provider.
	 */
	@Value("${ldap.tryFallbackToOLATPwdOnLogin}")
	private boolean tryFallbackToOLATPwdOnLogin;
	/**
	 * When the system detects an LDAP user that does already exist in OLAT but is not marked
	 * as LDAP user, the OLAT user can be converted to an LDAP managed user. 
	 * When enabling this feature you should make sure that you don't have a user 'administrator'
	 * in your ldapBases (not a problem but not recommended)
	 */
	@Value("${ldap.convertExistingLocalUsersToLDAPUsers}")
	private boolean convertExistingLocalUsersToLDAPUsers;
	/**
	 * Users that have been created via LDAP sync but now can't be found on the LDAP anymore
	 * can be deleted automatically. If unsure, set to false and delete those users manually
	 * in the user management.
	 */
	@Value("${ldap.deleteRemovedLDAPUsersOnSync}")
	private boolean deleteRemovedLDAPUsersOnSync;
	/**
	 * Sanity check when deleteRemovedLDAPUsersOnSync is set to 'true': if more than the defined
	 * percentages of user accounts are not found on the LDAP server and thus recognized as to be
	 * deleted, the LDAP sync will not happen and require a manual triggering of the delete job
	 * from the admin interface. This should prevent accidential deletion of OLAT user because of
	 * temporary LDAP problems or user relocation on the LDAP side. 
	 * Value= 0 (never delete) to 100 (always delete). 
	 */
	@Value("${ldap.deleteRemovedLDAPUsersPercentage}")
	private int deleteRemovedLDAPUsersPercentage;
	// Propagate the password changes onto the LDAP server
	@Value("${ldap.propagatePasswordChangedOnLdapServer}")
	private boolean propagatePasswordChangedOnLdapServer;
	@Value("${ldap.resetLockTimoutOnPasswordChange}")
	private boolean resetLockTimoutOnPasswordChange;
	@Value("${ldap.changePasswordUrl}")
	private String changePasswordUrl;
	// Configuration for syncing user attributes

	
	// Should users be created and synchronized automatically? If you set this
	// configuration to false, the users will be generated on-the-fly when they
	// log in
	@Value("${ldap.ldapSyncOnStartup}")
	private boolean ldapSyncOnStartup;
	@Value("${ldap.ldapSyncCronSync}")
	private boolean ldapSyncCronSync;
	@Value("${ldap.ldapSyncCronSyncExpression}")
	private String ldapSyncCronSyncExpression;
	// User LDAP attributes to be synced and a map with the mandatory attributes


	private static final Logger log = Tracing.createLoggerFor(LDAPLoginModule.class);
	
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private LDAPSyncConfiguration syncConfiguration;
	
	@Autowired
	public LDAPLoginModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		// Check if LDAP is enabled
		if (!isLDAPEnabled()) {
			log.info("LDAP login is disabled");
			return;
		}
		log.info("Starting LDAP module");
		
		// check for valid configuration
		if (!checkConfigParameterIsNotEmpty(ldapUrl)) return;
		if (!checkConfigParameterIsNotEmpty(systemDN)) return;
		if (!checkConfigParameterIsNotEmpty(systemPW)) return;
		if (syncConfiguration.getLdapBases() == null || syncConfiguration.getLdapBases().isEmpty()) {
			log.error("Missing configuration 'ldapBases'. Add at least one LDAP Base to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if (syncConfiguration.getLdapUserFilter() != null) {
			if (!syncConfiguration.getLdapUserFilter().startsWith("(") || !syncConfiguration.getLdapUserFilter().endsWith(")")) {
				log.error("Wrong configuration 'ldapUserFilter'. Set filter to emtpy value or enclose filter in brackets like '(objectClass=person)'. Disabling LDAP");
				setEnableLDAPLogins(false);
				return;
			}
		}
		
		if (!checkConfigParameterIsNotEmpty(syncConfiguration.getLdapUserCreatedTimestampAttribute())) {
			return;
		}
		if (!checkConfigParameterIsNotEmpty(syncConfiguration.getLdapUserLastModifiedTimestampAttribute())) {
			return;
		}
		if (syncConfiguration.getUserAttributeMap() == null || syncConfiguration.getUserAttributeMap().isEmpty()) {
			log.error("Missing configuration 'userAttrMap'. Add at least the email propery to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if (syncConfiguration.getRequestAttributes() == null || syncConfiguration.getRequestAttributes().isEmpty()) {
			log.error("Missing configuration 'reqAttr'. Add at least the email propery to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		// check if OLAT user properties is defined in olat_userconfig.xml, if not disable the LDAP module
		if(!syncConfiguration.checkIfOlatPropertiesExists(syncConfiguration.getUserAttributeMap())){
			log.error("Invalid LDAP OLAT properties mapping configuration (userAttrMap). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if(!syncConfiguration.checkIfOlatPropertiesExists(syncConfiguration.getRequestAttributes())){
			log.error("Invalid LDAP OLAT properties mapping configuration (reqAttr). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if(syncConfiguration.getSyncOnlyOnCreateProperties() != null
				&& !syncConfiguration.checkIfStaticOlatPropertiesExists(syncConfiguration.getSyncOnlyOnCreateProperties())){
			log.error("Invalid LDAP OLAT syncOnlyOnCreateProperties configuration. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if(syncConfiguration.getStaticUserProperties() != null
				&& !syncConfiguration.checkIfStaticOlatPropertiesExists(syncConfiguration.getStaticUserProperties().keySet())){
			log.error("Invalid static OLAT properties configuration (staticUserProperties). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		
		// check SSL certifications, throws Startup Exception if certificate is not found
		if(isSslEnabled()) {
			if (!checkServerCertValidity(0)) {
				log.error("LDAP enabled but no valid server certificate found. Please fix!");
			} else if (!checkServerCertValidity(30)) {
				log.warn("Server Certificate will expire in less than 30 days.");
			}
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

	@Override
	protected void initFromChangedProperties() {
		//
	}

	/**
	 * Internal helper to initialize the cron syncer job
	 */
	private void initCronSyncJob() {
		try {
			// Create job with cron trigger configuration
			JobDetail jobDetail = newJob(LDAPUserSynchronizerJob.class)
					.withIdentity("LDAP_Cron_Syncer_Job", Scheduler.DEFAULT_GROUP)
					.build();
			Trigger trigger = newTrigger()
				    .withIdentity("LDAP_Cron_Syncer_Trigger")
				    .withSchedule(cronSchedule(ldapSyncCronSyncExpression))
				    .build();

			// Schedule job now
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("LDAP cron syncer is enabled with expression::{}", ldapSyncCronSyncExpression);
		} catch (Exception e) {
			setLdapSyncCronSync(false);
			log.error("LDAP configuration in attribute 'ldapSyncCronSyncExpression' is not valid ({}). See http://quartz.sourceforge.net/javadoc/org/quartz/CronTrigger.html to learn more about the cron syntax. Disabling LDAP cron syncing",
				ldapSyncCronSyncExpression, e);
		}
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
	private boolean checkServerCertValidity(int daysFromNow) {
		KeyStore keyStore;
		try(FileInputStream in=new FileInputStream(getTrustStoreLocation())) {
			keyStore = KeyStore.getInstance(getTrustStoreType());
			keyStore.load(in, (getTrustStorePwd() != null) ? getTrustStorePwd().toCharArray() : null);
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				Certificate cert = keyStore.getCertificate(alias);
				if (cert instanceof X509Certificate) {
					return isCertificateValid((X509Certificate)cert, daysFromNow);
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	private boolean isCertificateValid(X509Certificate x509Cert, int daysFromNow) {
		try {
			x509Cert.checkValidity();
			if (daysFromNow > 0) {
				Date nowPlusDays = new Date(System.currentTimeMillis() + (daysFromNow * 24l * 60l * 60l * 1000l));
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
			log.error("Missing configuration '{}'. Add this configuration to olatextconfig.xml first. Disabling LDAP", param);
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

	public String getLdapSystemDN() {
		return systemDN;
	}

	public void setLdapSystemDN(String ldapSystemDN) {
		systemDN = ldapSystemDN.trim();
	}
	
	public String getLdapSystemPW() {
		return systemPW;
	}

	public void setLdapSystemPW(String ldapSystemPW) {
		systemPW = ldapSystemPW.trim();
	}
	
	public String getLdapUrl() {
		return ldapUrl;
	}

	public void setLdapUrl(String ldapUrlConfig) {
		ldapUrl = ldapUrlConfig.trim();
	}
	
	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public Integer getLdapConnectionTimeout() {
		return connectionTimeout;
	}
	
	public void setLdapConnectionTimeout(Integer timeout) {
		connectionTimeout = timeout;
	}

	public void setLdapSyncCronSync(boolean ldapSyncCronSync) {
		this.ldapSyncCronSync = ldapSyncCronSync;
	}

	public void setLdapSyncCronSyncExpression(String ldapSyncCronSyncExpression) {
		this.ldapSyncCronSyncExpression = ldapSyncCronSyncExpression.trim();
	}
	
	public void setCacheLDAPPwdAsOLATPwdOnLogin(boolean cacheLDAPPwdAsOLATPwdOnLogin) {
		this.cacheLDAPPwdAsOLATPwdOnLogin = cacheLDAPPwdAsOLATPwdOnLogin;
	}

	public void setTryFallbackToOLATPwdOnLogin(boolean tryFallbackToOLATPwdOnLogin) {
		this.tryFallbackToOLATPwdOnLogin = tryFallbackToOLATPwdOnLogin;
	}

	public void setCreateUsersOnLogin(boolean createUsersOnLogin) {
		this.createUsersOnLogin = createUsersOnLogin;
	}

	public void setConvertExistingLocalUsersToLDAPUsers(boolean convertExistingLocalUsersToLDAPUsers) {
		this.convertExistingLocalUsersToLDAPUsers = convertExistingLocalUsersToLDAPUsers;
	}

	public void setDeleteRemovedLDAPUsersOnSync(boolean deleteRemovedLDAPUsersOnSync) {
		this.deleteRemovedLDAPUsersOnSync = deleteRemovedLDAPUsersOnSync;
	}
	
	public void setDeleteRemovedLDAPUsersPercentage(int deleteRemovedLDAPUsersPercentage){
		this.deleteRemovedLDAPUsersPercentage = deleteRemovedLDAPUsersPercentage;
	}

	public void setPropagatePasswordChangedOnLdapServer(boolean propagatePasswordChangedOnServer) {
		this.propagatePasswordChangedOnLdapServer = propagatePasswordChangedOnServer;
	}

	public void setResetLockTimoutOnPasswordChange(boolean resetLockTimoutOnPasswordChange) {
		this.resetLockTimoutOnPasswordChange = resetLockTimoutOnPasswordChange;
	}

	public boolean isLDAPEnabled() {
		return ldapEnabled;
	}

	public boolean isSslEnabled() {
		return sslEnabled;
	}
	
	public boolean isActiveDirectory() {
		return activeDirectory;
	}
	
	public String getLdapDateFormat() {
		if(StringHelper.containsNonWhitespace(ldapDateFormat)) {
			return ldapDateFormat;
		}
		return "yyyyMMddHHmmss'Z'";//default
	}
	
	public String getTrustStoreLocation(){
		return trustStoreLoc;
	}
	
	public String getTrustStorePwd(){
		return trustStorePass;
	}
	
	public String getTrustStoreType(){
		return trustStoreTyp;
	}

	public boolean isLdapSyncOnStartup() {
		return ldapSyncOnStartup;
	}

	public boolean isLdapSyncCronSync() {
		return ldapSyncCronSync;
	}

	public String getLdapSyncCronSyncExpression() {
		return ldapSyncCronSyncExpression;
	}
	
	public boolean isCreateUsersOnLogin() {
		return createUsersOnLogin;
	}

	public boolean isCacheLDAPPwdAsOLATPwdOnLogin() {
		return cacheLDAPPwdAsOLATPwdOnLogin;
	}
	
	public boolean isTryFallbackToOLATPwdOnLogin() {
		return tryFallbackToOLATPwdOnLogin;
	}

	public boolean isConvertExistingLocalUsersToLDAPUsers() {
		return convertExistingLocalUsersToLDAPUsers;
	}

	public boolean isDeleteRemovedLDAPUsersOnSync() {
		return deleteRemovedLDAPUsersOnSync;
	}
	
	public int getDeleteRemovedLDAPUsersPercentage(){
		return deleteRemovedLDAPUsersPercentage;
	}

	public boolean isPropagatePasswordChangedOnLdapServer(){
		return propagatePasswordChangedOnLdapServer;
	}

	public boolean isResetLockTimoutOnPasswordChange() {
		return resetLockTimoutOnPasswordChange;
	}

	public String getChangePasswordUrl() {
		return changePasswordUrl;
	}
}
