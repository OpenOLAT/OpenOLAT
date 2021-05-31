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
package org.olat.admin.registration;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.repository.RepositoryManager;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

@Service
public class SystemRegistrationManager implements InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(SystemRegistrationManager.class);

	private static final String SCHEDULER_NAME = "system.registration";
	private static final String TRIGGER = "system_registration_trigger";
	public static final String PRODUCT = "openolat";

	@Value("${cluster.mode}")
	private String clusterMode;
	
	@Autowired
	private DB database;
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private HttpClientService httpClientService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private SystemRegistrationModule registrationModule;

	private static final String REGISTRATION_SERVER = "http://registration.openolat.org/registration/restapi/registration/openolat";
	//private static final String REGISTRATION_SERVER = "http://localhost:8083/registration/restapi/registration/openolat";
	
	/**
	 * Initialize the configuration
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		setupRegistrationBackgroundThread();
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
		int day = RandomUtils.nextInt(6) + 1;
		return "0 " + min + " " + hour + " ? * "+ day;
	}

	public String getLocationCoordinates(String textLocation){
		if (textLocation == null || textLocation.length()==0) {
			return null;
		}
		
		String csvCoordinates = null;
		try(CloseableHttpClient client = httpClientService.getThreadSafeHttpClient(true)) {
			URIBuilder uriBuilder = new URIBuilder("http://maps.google.com/maps/geo");
			List<NameValuePair> nvps = new ArrayList<>(5);
			nvps.add(new BasicNameValuePair("q",textLocation));
			nvps.add(new BasicNameValuePair("output","csv"));
			nvps.add(new BasicNameValuePair("oe","utf8"));
			nvps.add(new BasicNameValuePair("sensor","false"));
			nvps.add(new BasicNameValuePair("key","ABQIAAAAq5BZJrKbG-xh--W4MrciXRQZTOqTGVCcmpRMgrUbtlJvJ3buAhSfG7H7hgE66BCW17_gLyhitMNP4A"));
			uriBuilder.addParameters(nvps);

			HttpGet getCall = new HttpGet(uriBuilder.build());
			HttpResponse response = client.execute(getCall);
			String resp = null;
			if(response.getStatusLine().getStatusCode() == 200){
				resp = EntityUtils.toString(response.getEntity());
				String[] split = resp.split(",");
				csvCoordinates = split[2]+","+split[3];
			}
		} catch (Exception e) {
			//
		}
		return csvCoordinates;
	}
	
	public void send() {
		try {
			scheduler.triggerJob(new JobKey(SCHEDULER_NAME, Scheduler.DEFAULT_GROUP));
		} catch (SchedulerException e) {
			log.error("", e);
		}
	}
	
	/**
	 * Send the registration data now. If the user configured nothing to send,
	 * nothing will be sent.
	 */
	protected void sendRegistrationData() {
		HttpPut method = null;

		try(CloseableHttpClient client = httpClientService.getThreadSafeHttpClient(true)) {
			// Do it optimistic and try to generate the XML message. If the message
			// doesn't contain anything, the user does not want to register this
			// instance
			Map<String,String> registrationData = getRegistrationPropertiesMessage();
			// only send when there is something to send
			UriBuilder builder = UriBuilder.fromUri(REGISTRATION_SERVER);
			for(Map.Entry<String, String> entry:registrationData.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if(StringHelper.containsNonWhitespace(value)) {
					builder.queryParam(key, value);
				}
			}
			
			builder.queryParam("instanceId", registrationModule.getInstanceIdentifier());
			if(StringHelper.containsNonWhitespace(registrationModule.getSecretKey())) {
				String secretKey = registrationModule.getSecretKey();
				builder.queryParam("secretKey", secretKey);
			}
			builder.queryParam("product", PRODUCT);

			String url = builder.build().toString();
			method = new HttpPut(url);
			HttpResponse response = client.execute(method);
			int status = response.getStatusLine().getStatusCode();
			if(status == HttpStatus.SC_CREATED) {
				log.info("Successfully registered OpenOlat installation on openolat.org server, thank you for your support!");
				String registrationKey = EntityUtils.toString(response.getEntity());
				registrationModule.setSecretKey(registrationKey);
			} else if (status == HttpStatus.SC_NOT_MODIFIED || status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
				log.info("Successfully registered OpenOlat installation on openolat.org server, thank you for your support!");
			} else if (status == HttpStatus.SC_NOT_FOUND) {
				log.error("Registration server not found: " + response.getStatusLine().toString());
			} else if(status == HttpStatus.SC_NO_CONTENT){
				log.info(response.getStatusLine().toString() + " " + EntityUtils.toString(response.getEntity()));
			} else {
				log.error("Unexpected HTTP Status: " + response.getStatusLine().toString() + " during registration call");
			}
		} catch (Exception e) {
			log.error("Unexpected exception during registration call", e);
		} finally {
			database.commitAndCloseSession();
			if(method != null) {
				method.releaseConnection();
			}
		}
	}

	public Map<String,String> getRegistrationPropertiesMessage() {
		Map<String,String> msgProperties = new HashMap<>();
		
		boolean website = registrationModule.isPublishWebsite();
		boolean notify = registrationModule.isNotifyReleases();
		//OLAT version
		msgProperties.put("appName", Settings.getApplicationName());
		msgProperties.put("version", Settings.getFullVersionInfo());
		
		//Location
		msgProperties.put("location", registrationModule.getLocation());
		msgProperties.put("locationCSV", registrationModule.getLocationCoordinates());
		
		// System config
		msgProperties.put("instantMessagingEnabled", String.valueOf(CoreSpringFactory.getImpl(InstantMessagingModule.class).isEnabled()));
		msgProperties.put("enabledLanguages", CoreSpringFactory.getImpl(I18nModule.class).getEnabledLanguageKeys().toString());
		msgProperties.put("clusterEnabled", clusterMode);
		msgProperties.put("debuggingEnabled", String.valueOf(Settings.isDebuging()));
		
		// Course counts
		int allCourses = repositoryManager.countByType(CourseModule.ORES_TYPE_COURSE);
		int publishedCourses = repositoryManager.countPublished(CourseModule.ORES_TYPE_COURSE);
		msgProperties.put("courses", String.valueOf(allCourses));
		msgProperties.put("coursesPublished", String.valueOf(publishedCourses));
		
		// User counts
		long visible = securityManager.countIdentitiesByPowerSearch(null, null, true, new OrganisationRoles[] { OrganisationRoles.user },
				null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		msgProperties.put("usersEnabled", String.valueOf(visible));

		long authors = securityManager.countIdentitiesByPowerSearch(null, null, true, new OrganisationRoles[] { OrganisationRoles.author },
				null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		msgProperties.put("authors", String.valueOf(authors));
		
		// Activity
		Calendar lastLoginLimit = Calendar.getInstance();
		lastLoginLimit.add(Calendar.DAY_OF_YEAR, -6); // -1 - 6 = -7 for last week
		Long activeUsersLastWeek = securityManager.countUniqueUserLoginsSince(lastLoginLimit.getTime());
		msgProperties.put("activeUsersLastWeek", String.valueOf(activeUsersLastWeek));
		lastLoginLimit = Calendar.getInstance();
		lastLoginLimit.add(Calendar.MONTH, -1);
		Long activeUsersLastMonth = securityManager.countUniqueUserLoginsSince(lastLoginLimit.getTime());
		msgProperties.put("activeUsersLastMonth", String.valueOf(activeUsersLastMonth));
		
		// Groups
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		int groups = businessGroupService.countBusinessGroups(params, null);
		msgProperties.put("buddyGroups", String.valueOf(groups));
		msgProperties.put("learningGroups", String.valueOf(groups));
		msgProperties.put("rightGroups", String.valueOf(groups));
		msgProperties.put("groups", String.valueOf(groups));

		// URL
		msgProperties.put("url", Settings.getServerContextPathURI());
		msgProperties.put("publishWebsite", String.valueOf(website));
		// Description
		String desc = registrationModule.getWebsiteDescription();
		msgProperties.put("description", desc);
	
		if (notify) {
			// Email
			String email = registrationModule.getEmail();
			msgProperties.put("email", email);
		}

		database.commitAndCloseSession();
		return msgProperties;
	}

	/**
	 * Method to initialize the registration submission scheduler. The scheduler
	 * normally runs once a week and submitts the most current data.
	 */
	public void setupRegistrationBackgroundThread() {
		// Only run scheduler on first cluster node
		// This is accomplished by the SystemRegistrationJobStarter which is configured and ensured to run only once in a cluster from within 
		// the olatextconfig.xml. This Job uses this method to setup the cronjob defined with the cronexpressioin from the properties.
		// 
		
		// Don't run in jUnit mode
		if (Settings.isJUnitTest()) return;

		String cronExpression = createCronTriggerExpression();
		try {
			JobDetail jobDetail = newJob(SystemRegistrationJob.class)
					.withIdentity(SCHEDULER_NAME, Scheduler.DEFAULT_GROUP)
					.build();
			Trigger trigger = newTrigger()
				    .withIdentity(TRIGGER)
				    .withSchedule(cronSchedule(cronExpression))
				    .build();
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (Exception e) {
			log.error("Illegal cron expression for system registration", e);
		}
		log.info("Registration background job successfully started: "+cronExpression);
	}
}