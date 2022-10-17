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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.net.URI;
import java.util.Calendar;

import jakarta.ws.rs.core.UriBuilder;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.vitero.manager.ViteroZombieSlayerJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ViteroModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final Logger log = Tracing.createLoggerFor(ViteroModule.class);
	
	private static final String PROP_ENABLED = "vc.vitero.enabled";
	private static final String PROP_PROTOCOL = "protocol";
	private static final String PROP_PORT = "port";
	private static final String PROP_BASE_URL = "baseUrl";
	private static final String PROP_CONTEXT_PATH = "contextPath";
	private static final String PROP_ADMIN_LOGIN = "adminLogin";
	private static final String PROP_ADMIN_CREDENTIAL = "adminPassword";
	private static final String PROP_CUSTOMER_ID = "customerId";
	private static final String PROP_OLAT_TIMEZONE_ID = "olatTimeZoneId";
	private static final String PROP_INSPIRE = "inspire";
	
	@Value("${vc.vitero.enabled}")
	private boolean enabled;
	@Value("${vc.vitero.protocol}")
	private String protocol;
	@Value("${vc.vitero.port}")
	private int port;
	@Value("${vc.vitero.baseurl}")
	private String baseUrl;
	@Value("${vc.vitero.contextPath}")
	private String contextPath;
	@Value("${vc.vitero.adminlogin}")
	private String adminLogin;
	@Value("${vc.vitero.adminpassword}")
	private String adminPassword;
	@Value("${vc.vitero.customerid}")
	private int customerId;
	@Value("${vc.vitero.olatTimeZoneId}")
	private String olatTimeZoneId;
	@Value("${vc.vitero.cron:0 15 */12 * * ?}")
	private String cronExpression;
	@Value("${vc.vitero.deleteVmsUserOnUserDelete}")
	private boolean deleteVmsUserOnUserDelete;
	@Value("${vc.vitero.inspire:true}")
	private boolean inspire;
	
	private final Scheduler scheduler;
	
	@Autowired
	public ViteroModule(CoordinatorManager coordinatorManager, Scheduler scheduler) {
		super(coordinatorManager);
		this.scheduler = scheduler;
	}
	
	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(PROP_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String protocolObj = getStringPropertyValue(PROP_PROTOCOL, true);
		if(StringHelper.containsNonWhitespace(protocolObj)) {
			protocol = protocolObj;
		}
		String portObj = getStringPropertyValue(PROP_PORT, true);
		if(StringHelper.containsNonWhitespace(portObj)) {
			port = Integer.parseInt(portObj);
		}
		String baseUrlObj = getStringPropertyValue(PROP_BASE_URL, true);
		if(StringHelper.containsNonWhitespace(baseUrlObj)) {
			baseUrl = baseUrlObj;
		}
		String contextPathObj = getStringPropertyValue(PROP_CONTEXT_PATH, true);
		if(StringHelper.containsNonWhitespace(contextPathObj)) {
			contextPath = contextPathObj;
		}
		String adminLoginObj = getStringPropertyValue(PROP_ADMIN_LOGIN, true);
		if(StringHelper.containsNonWhitespace(adminLoginObj)) {
			adminLogin = adminLoginObj;
		}
		String adminPasswordObj = getStringPropertyValue(PROP_ADMIN_CREDENTIAL, true);
		if(StringHelper.containsNonWhitespace(adminPasswordObj)) {
			adminPassword = adminPasswordObj;
		}
		String customerIdObj = getStringPropertyValue(PROP_CUSTOMER_ID, true);
		if(StringHelper.containsNonWhitespace(customerIdObj)) {
			customerId = Integer.parseInt(customerIdObj);
		}
		String olatTimeZoneIdObj = getStringPropertyValue(PROP_OLAT_TIMEZONE_ID, true);
		if(StringHelper.containsNonWhitespace(olatTimeZoneIdObj)) {
			olatTimeZoneId = olatTimeZoneIdObj;
		}
		
		String inspireObj = getStringPropertyValue(PROP_INSPIRE, true);
		if(StringHelper.containsNonWhitespace(inspireObj)) {
			inspire = "true".equals(inspireObj);
		}
		
		initCronJob();
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	private void initCronJob() {
		try {
			TriggerKey triggerKey = new TriggerKey("Vitero_Cleaner_Cron_Trigger", Scheduler.DEFAULT_GROUP);
			if(scheduler.getTrigger(triggerKey) == null) {
				
				// Create job with cron trigger configuration
				JobDetail jobDetail = newJob(ViteroZombieSlayerJob.class)
						.withIdentity("Vitero_Cleaner_Cron_Job", Scheduler.DEFAULT_GROUP)
						.build();
				
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, 30);
				Trigger trigger = newTrigger()
					    .withIdentity("Vitero_Cleaner_Cron_Trigger")
					    .withSchedule(cronSchedule(cronExpression))
					    .startAt(cal.getTime())
					    .build();

				scheduler.scheduleJob(jobDetail, trigger);
			}
		} catch (Exception e) {
			log.error("Cannot start the Quartz Job which clean the Vitero rooms", e);
		}
	}

	public boolean isDeleteVmsUserOnUserDelete() {
		return deleteVmsUserOnUserDelete;
	}

	/**
	 * [user by String]
	 * @param cronExpression
	 */
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	
	/**
	 * [user by Spring]
	 * @param deleteVmsUserOnUserDelete
	 */
	public void setDeleteVmsUserOnUserDelete(boolean deleteVmsUserOnUserDelete) {
		this.deleteVmsUserOnUserDelete = deleteVmsUserOnUserDelete;
	}

	public URI getVmsURI() {
		UriBuilder builder = UriBuilder.fromUri(getProtocol() + "://" + getBaseUrl());
		if(getPort() > 0) {
			builder = builder.port(getPort());
		}
	    if(StringHelper.containsNonWhitespace(getContextPath())) {
	    	builder = builder.path(getContextPath());
	    }
		return builder.build();
	}
	
	public void setVmsURI(URI uri) {
		String vmsHost = uri.getHost();
		setBaseUrl(vmsHost);
		int vmsPort = uri.getPort();
		setPort(vmsPort);
		String vmsPath = uri.getPath();
		if(StringHelper.containsNonWhitespace(vmsPath) && vmsPath.startsWith("/")) {
			vmsPath = vmsPath.substring(1, vmsPath.length());
		}
		setContextPath(vmsPath);
		String vmsScheme = uri.getScheme();
		setProtocol(vmsScheme);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setBooleanProperty(PROP_ENABLED, enabled, true);
	}
	
	/**
	 * Return the time zone of OLAT within the IDS allowed by Vitero
	 * @return
	 */
	public String getTimeZoneId() {
		return olatTimeZoneId;
	}
	
	public void setTimeZoneId(String timeZoneId) {
		this.olatTimeZoneId = timeZoneId;
		setStringProperty(PROP_OLAT_TIMEZONE_ID, timeZoneId, true);
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
		setStringProperty(PROP_PROTOCOL, protocol == null ? "" : protocol, true);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
		setStringProperty(PROP_PORT, Integer.toString(port), true);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		setStringProperty(PROP_BASE_URL, baseUrl == null ? "" : baseUrl, true);
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
		setStringProperty(PROP_CONTEXT_PATH, contextPath == null ? "" : contextPath, true);
	}

	public String getAdminLogin() {
		return adminLogin;
	}

	public void setAdminLogin(String adminLogin) {
		this.adminLogin = adminLogin;
		setStringProperty(PROP_ADMIN_LOGIN, adminLogin, true);
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
		setStringProperty(PROP_ADMIN_CREDENTIAL, adminPassword, true);
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
		setStringProperty(PROP_CUSTOMER_ID, Integer.toString(customerId), true);
	}

	public boolean isInspire() {
		return inspire;
	}

	public void setInspire(boolean enableInspire) {
		inspire = enableInspire;
		setStringProperty(PROP_INSPIRE, Boolean.toString(enableInspire), true);
	}
	
	
}