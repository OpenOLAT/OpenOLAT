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
 * <p>
 */
package org.olat.admin.registration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.math.RandomUtils;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.PermissionOnResourceable;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.Destroyable;
import org.olat.core.configuration.Initializable;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.configuration.PersistedPropertiesChangedEvent;
import org.olat.core.gui.control.Event;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.httpclient.HttpClientFactory;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroup;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * Description:<br>
 * This manager offers methods to store registration preferences and to register
 * the installation on the olat.org server.
 * 
 * <P>
 * Initial Date: 12.12.2008 <br>
 * 
 * @author gnaegi
 */
public class SystemRegistrationManager extends BasicManager implements GenericEventListener, Initializable, Destroyable {
	private static final String POST_PARAMETER_NAME = "registrationData";
	private static SystemRegistrationManager INSTANCE;
	// Version flag for data xml
	private static final String VERSION = "1.0";
	private static final String SCHEDULER_NAME = "system.registration";
	// configuration keys in persisted properties
	private PersistedProperties persitedProperties;
	private Scheduler scheduler;
	private String clusterMode;
	private DB database;
	
	public static final String CONF_KEY_PUBLISH_WEBSITE = "publishWebsite";
	public static final String CONF_KEY_WEBSITE_DESCRIPTION = "websiteDescription";
	public static final String CONF_KEY_NOTIFY_RELEASES = "notifyReleases";
	public static final String CONF_KEY_EMAIL = "email";
	// not configurable by user
	public static final String CONF_KEY_REGISTRATION_CRON = "registrationCron";
	public static final String CONF_KEY_IDENTIFYER = "instanceIdentifyer";
	// Where to post the registration. Don't move this to a config, it should not
	// be that easy to modify the registration server URL!
	private static final String REGISTRATION_SERVER = "http://www.olat.org/olatregistration/registrations/";
	//private static final String REGISTRATION_SERVER = "http://localhost:8088/olatregistration/registrations/";
	//location described by language, e.g. "Winterthurerstrasse 190, Zürich", or "Dresden"....
	public static final String CONF_KEY_LOCATION = "location";
	// the geolocation derived with a google maps service for usage to place markers on a google map
	public static final String CONF_KEY_LOCATION_COORDS="location_coords";
	// on first registration request, the registration.olat.org creates a secret key - needed for future updates
	private static final String CONF_SECRETKEY = "secret_key";
	
	/**
	 * [used by spring]
	 * Use getInstance(), this is a singleton
	 */
	private SystemRegistrationManager(Scheduler scheduler, String clusterMode, DB database) {
		this.scheduler = scheduler;
		INSTANCE = this;
		this.clusterMode = clusterMode;
		this.database = database;
		FrameworkStartupEventChannel.registerForStartupEvent(this);
	}

	/**
	 * [used by spring]
	 * @param persitedProperties
	 */
	public void setPersitedProperties(PersistedProperties persitedProperties) {
		this.persitedProperties = persitedProperties;
	}

	/**
	 * Call this to shutdown the cron scheduler and remove cluster event listeners
	 * from the PersistedProperties infrastructure
	 */
	public void destroy() {
		// remove properties
		if (persitedProperties != null) {
			persitedProperties.destroy();
		}
		// Stop registration job
		Scheduler scheduler = (Scheduler) CoreSpringFactory.getBean("schedulerFactoryBean");
		try {
			scheduler.deleteJob(SCHEDULER_NAME, Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			logError("Could not shut down job::" + SCHEDULER_NAME, e);
		}
	}

	/**
	 * Initialize the configuration
	 */
	public void init() {
		//TODO why does this get set with each start?		
		persitedProperties.setBooleanPropertyDefault(CONF_KEY_PUBLISH_WEBSITE, false);
		persitedProperties.setStringPropertyDefault(CONF_KEY_WEBSITE_DESCRIPTION, "");
		persitedProperties.setBooleanPropertyDefault(CONF_KEY_NOTIFY_RELEASES, false);
		persitedProperties.setStringPropertyDefault(CONF_KEY_EMAIL, WebappHelper.getMailConfig("mailSupport"));
		// Check if cron property exist
		if (persitedProperties.getStringPropertyValue(CONF_KEY_REGISTRATION_CRON, false) == null) {
			String cronExpression = createCronTriggerExpression();
			// persist so that next startup we have same trigger
			persitedProperties.setStringProperty(CONF_KEY_REGISTRATION_CRON, cronExpression, true);
		}
		// Check if instance identifyer property exists
		if (persitedProperties.getStringPropertyValue(CONF_KEY_IDENTIFYER, false) == null) {
			String uniqueID = CodeHelper.getGlobalForeverUniqueID();
		  MessageDigest digester;
			try {
				digester = MessageDigest.getInstance("MD5");
				digester.update(uniqueID.getBytes(),0,uniqueID.length());
				persitedProperties.setStringProperty(CONF_KEY_IDENTIFYER, new BigInteger(1,digester.digest()).toString(16), true);
			} catch (NoSuchAlgorithmException e) {
				// using no encoding instead
				persitedProperties.setStringProperty(CONF_KEY_IDENTIFYER, uniqueID, true);
			}
		}
	}

	/**
	 * Helper method to create a cron trigger expression. The method makes sure
	 * that not every olat installation submits at the same time
	 * 
	 * @return
	 */
	private String createCronTriggerExpression() {
		// Create a random hour and minute for the cronjob so that not every
		// installation registers at the same time
		int min = RandomUtils.nextInt(59);
		int hour = RandomUtils.nextInt(23);
		int day = RandomUtils.nextInt(6);
		String cronExpression = "0 " + min + " " + hour + " ? * "+ day;
		return cronExpression;
	}

	/**
	 * Singleton, use this to get a handle to the manager
	 * 
	 * @return
	 */
	public static SystemRegistrationManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * [used by spring]
	 * @param persistedProperties
	 */
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.persitedProperties = persistedProperties;
	}

	/**
	 * @return The persisted configuration
	 */
	PersistedProperties getRegistrationConfiguration() {
		return persitedProperties;
	}

	String getLocationCoordinates(String textLocation){
		String csvCoordinates = null;
		
		if (textLocation == null || textLocation.length()==0) {
			return null;
		}
		
		HttpClient client = HttpClientFactory.getHttpClientInstance();
		String url = "http://maps.google.com/maps/geo";
		NameValuePair[] nvps = new NameValuePair[5];
		nvps[0] = new NameValuePair("q",textLocation);
		nvps[1] = new NameValuePair("output","csv");
		nvps[2] = new NameValuePair("oe","utf8");
		nvps[3] = new NameValuePair("sensor","false");
		nvps[4] = new NameValuePair("key","ABQIAAAAq5BZJrKbG-xh--W4MrciXRQZTOqTGVCcmpRMgrUbtlJvJ3buAhSfG7H7hgE66BCW17_gLyhitMNP4A");
		
		GetMethod getCall = new GetMethod(url);
		getCall.setQueryString(nvps);
		
		try {
			client.executeMethod(getCall);
			String resp = null;
			if(getCall.getStatusCode() == 200){
				resp = getCall.getResponseBodyAsString();
				String[] split = resp.split(",");
				csvCoordinates = split[2]+","+split[3];
			}
		} catch (HttpException e) {
			//
		} catch (IOException e) {
			//
		}
		
		return csvCoordinates;
	}
	
	
	/**
	 * Send the registration data now. If the user configured nothing to send,
	 * nothing will be sent.
	 */
	public void sendRegistrationData() {
		// Do it optimistic and try to generate the XML message. If the message
		// doesn't contain anything, the user does not want to register this
		// instance
		String registrationData = getRegistrationPropertiesMessage(null);
		String registrationKey = persitedProperties.getStringPropertyValue(CONF_SECRETKEY, false);
		if (StringHelper.containsNonWhitespace(registrationData)) {
			// only send when there is something to send
			HttpClient client = HttpClientFactory.getHttpClientInstance();
			client.getParams().setParameter("http.useragent", "OLAT Registration Agent ; " + VERSION);
			String url = REGISTRATION_SERVER+persitedProperties.getStringPropertyValue(CONF_KEY_IDENTIFYER, false)+"/";
			logInfo("URL:"+url, null);
			PutMethod method = new PutMethod(url);
			if(registrationKey != null){
				//updating
				method.setRequestHeader("Authorization",registrationKey);
				if(isLogDebugEnabled()){
					logDebug("Authorization: "+registrationKey,null);
				}else{
					logDebug("Authorization: EXISTS",null);
				}
			}else{
				logInfo("Authorization: NONE",null);
			}
			method.setRequestHeader("Content-Type", "application/xml; charset=utf-8");			
			try {
				method.setRequestEntity(new StringRequestEntity(registrationData, "application/xml", "UTF8"));
				client.executeMethod(method);
				int status = method.getStatusCode();
				if (status == HttpStatus.SC_NOT_MODIFIED || status == HttpStatus.SC_OK) {
					logInfo("Successfully registered OLAT installation on olat.org server, thank you for your support!", null);
					registrationKey = method.getResponseBodyAsString();
					persitedProperties.setStringProperty(CONF_SECRETKEY, registrationKey, false);
					persitedProperties.savePropertiesAndFireChangedEvent();
				} else if (method.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					logError("File could be created not on registration server::" + method.getStatusLine().toString(), null);
				} else if(method.getStatusCode() == HttpStatus.SC_NO_CONTENT){
					logInfo(method.getResponseBodyAsString(), method.getStatusText());
				}
				else {
					logError("Unexpected HTTP Status::" + method.getStatusLine().toString() + " during registration call", null);
				}
			} catch (Exception e) {
				logError("Unexpected exception during registration call", e);
			}
		} else {
			logWarn(
					"****************************************************************************************************************************************************************************",
					null);
			logWarn(
					"* This OLAT installation is not registered. Please, help us with your statistical data and register your installation under Adminisration - Systemregistration. THANK YOU! *",
					null);
			logWarn(
					"****************************************************************************************************************************************************************************",
					null);
		}
	}

	String getRegistrationPropertiesMessage(Properties tempConfiguration) {
		Properties msgProperties = new Properties();
		if (tempConfiguration == null) {
			// Create temp properties from persisted properties
			tempConfiguration = persitedProperties.createPropertiesFromPersistedProperties();
		}
		
		boolean website = Boolean.parseBoolean(tempConfiguration.getProperty(CONF_KEY_PUBLISH_WEBSITE));
		boolean notify = Boolean.parseBoolean(tempConfiguration.getProperty(CONF_KEY_NOTIFY_RELEASES));
		
		if (website || notify) {
			msgProperties = tempConfiguration;
			
			msgProperties.setProperty("RegistrationVersion", "1.0");
			
				// OLAT version
				msgProperties.setProperty("olatAppName", Settings.getApplicationName());
				msgProperties.setProperty("olatVersion", Settings.getFullVersionInfo());
				// System config
				msgProperties.setProperty("configInstantMessagingEnabled", String.valueOf(InstantMessagingModule.isEnabled()));
				msgProperties.setProperty("configLanguages", I18nModule.getEnabledLanguageKeys().toString());
				msgProperties.setProperty("configClusterEnabled", clusterMode);
				msgProperties.setProperty("configDebugginEnabled", String.valueOf(Settings.isDebuging()));
				// Course counts
				RepositoryManager repoMgr = RepositoryManager.getInstance();
				int allCourses = repoMgr.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_OWNERS);
				int publishedCourses = repoMgr.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_USERS);
				msgProperties.setProperty("courseCountAll", String.valueOf(allCourses));
				msgProperties.setProperty("courseCountPublished", String.valueOf(publishedCourses));
				// User counts
				BaseSecurity secMgr = BaseSecurityManager.getInstance();
				SecurityGroup olatuserGroup = secMgr.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
				int users = secMgr.countIdentitiesOfSecurityGroup(olatuserGroup);
				int disabled = secMgr.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_LOGIN_DENIED)
						.size();
				msgProperties.setProperty("usersEnabled", String.valueOf(users - disabled));
				
				PermissionOnResourceable[] permissions = { new PermissionOnResourceable(Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR) };
				List<Identity> authorsList = secMgr.getIdentitiesByPowerSearch(null, null, true, null, permissions, null, null, null, null, null, null);
				int authors = authorsList.size();
				msgProperties.setProperty("usersAuthors", String.valueOf(authors));
				// Activity
				Calendar lastLoginLimit = Calendar.getInstance();
				lastLoginLimit.add(Calendar.DAY_OF_YEAR, -6); // -1 - 6 = -7 for last
																											// week
				msgProperties.setProperty("activeUsersLastWeek", String.valueOf(secMgr.countUniqueUserLoginsSince(lastLoginLimit.getTime())));
				lastLoginLimit.add(Calendar.MONTH, -1);
				msgProperties.setProperty("activeUsersLastMonth", String.valueOf(secMgr.countUniqueUserLoginsSince(lastLoginLimit.getTime())));
				// Groups
				BGContextManager groupMgr = BGContextManagerImpl.getInstance();
				int buddyGroups = groupMgr.countGroupsOfType(BusinessGroup.TYPE_BUDDYGROUP);
				msgProperties.setProperty("groupCountBuddyGroups", String.valueOf(buddyGroups));
				int learningGroups = groupMgr.countGroupsOfType(BusinessGroup.TYPE_LEARNINGROUP);
				msgProperties.setProperty("groupCountLearningGroups", String.valueOf(learningGroups));
				int rightGroups = groupMgr.countGroupsOfType(BusinessGroup.TYPE_RIGHTGROUP);
				msgProperties.setProperty("groupCountRightGroups", String.valueOf(rightGroups));
			
			if (website) {
				// URL
				msgProperties.setProperty("websiteURL", Settings.getServerContextPathURI());
				// Description
				String desc = tempConfiguration.getProperty(CONF_KEY_WEBSITE_DESCRIPTION);
				msgProperties.setProperty("websiteDescription", desc);
			}
			if (notify) {
				// Email
				String email = tempConfiguration.getProperty(CONF_KEY_EMAIL);
				msgProperties.setProperty("email", email);
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			msgProperties.storeToXML(baos, "OLAT Registration Data, since 6.1.1 Release");
		} catch (IOException e) {
			throw new OLATRuntimeException("OLAT Registration failed",e);
		}
		String retVal = null;
		try {
			retVal =  baos.toString("UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new OLATRuntimeException("OLAT Registration failed",e);
		}
		return retVal;
	}

	/**
	 * Method to initialize the registration submission scheduler. The scheduler
	 * normally runs once a week and submitts the most current data.
	 */
	void setupRegistrationBackgroundThread() {
		// Only run scheduler on first cluster node
		// This is accomplished by the SystemRegistrationJobStarter which is configured and ensured to run only once in a cluster from within 
		// the olatextconfig.xml. This Job uses this method to setup the cronjob defined with the cronexpressioin from the properties.
		// 
		
		// Don't run in jUnit mode
		if (Settings.isJUnitTest()) return;
		// create a crontrigger inside because cron expression is random generated -> this can not be done by config? REVIEW:gs:
		String cronExpression = "ERROR";
		try {
			// Create job with cron trigger configuration
			JobDetail jobDetail = new JobDetail(SCHEDULER_NAME, Scheduler.DEFAULT_GROUP, SystemRegistrationJob.class);
			CronTrigger trigger = new CronTrigger();
			trigger.setName("system_registration_trigger");
			cronExpression = persitedProperties.getStringPropertyValue(CONF_KEY_REGISTRATION_CRON, true);
			if (!CronExpression.isValidExpression(cronExpression)) {
				cronExpression = createCronTriggerExpression();
				persitedProperties.setStringPropertyDefault(CONF_KEY_REGISTRATION_CRON, cronExpression);
			}
			// Use this cron expression for debugging, tries to send data every minute
			//trigger.setCronExpression("0 * * * * ?");
			trigger.setCronExpression(cronExpression);
			// Schedule job now
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (ParseException e) {

			logError("Illegal cron expression for system registration", e);
		} catch (SchedulerException e) {
			logError("Can not start system registration scheduler", e);
		}
		
		logInfo("Registration background job successfully started: "+cronExpression, null);
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof PersistedPropertiesChangedEvent) {
			init();
		} else if (event instanceof FrameworkStartedEvent) {
			// trigger first execution of registration when framework is started
			boolean success = false;
			try {
				sendRegistrationData();
				success = true;
				 database.commitAndCloseSession();
			} finally {
				if (!success) {
					database.rollbackAndCloseSession();
				}
			}
		}
	}
	
}
