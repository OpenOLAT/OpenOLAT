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
package org.olat.modules.adobeconnect;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AdobeConnectModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String ENABLED = "vc.adobe.enabled";
	private static final String PROTOCOL = "vc.adobe.protocol";
	private static final String PORT = "vc.adobe.port";
	private static final String USERTYPE = "vc.adobe.usertype";
	private static final String BASEURL = "vc.adobe.baseurl";
	private static final String CONTEXTPATH = "vc.adobe.contextpath";
	private static final String ADMIN_LOGIN = "vc.adobe.adminlogin";
	private static final String ADMIN_CRED = "vc.adobe.adminpassword";
	private static final String ACCOUNTID = "vc.adobe.accountid";
	private static final String PROVIDERID = "vc.adobe.providerid";
	private static final String CLEAN_MEETINGS = "vc.adobe.cleanupMeetings";
	private static final String DAYS_TO_KEEP = "vc.adobe.daysToKeep";
	private static final String SINGLE_MEETING_MODE = "vc.adobe.single.meeting.mode";
	private static final String CREATE_MEETING_IMMEDIATELY = "vc.adobe.createMeetingImmediately";

	@Value("${vc.adobe.enabled}")
	private boolean enabled;
	@Value("${vc.adobe.protocol:https}")
	private String protocol;
	@Value("${vc.adobe.port:443}")
	private int port;
	@Value("${vc.adobe.usertype:user}")
	private String userType;

	@Value("${vc.adobe.baseurl}")
	private String baseUrl;
	@Value("${vc.adobe.context:/api/xml}")
	private String contextPath;
	
	@Value("${vc.adobe.adminlogin}")
	private String adminLogin;
	@Value("${vc.adobe.adminpassword}")
	private String adminPassword;
	@Value("${vc.adobe.accountid:#{null}}")
	private String accountId;
	@Value("${vc.adobe.provider:connect9}")
	private String providerId;
	@Value("${vc.adobe.cleanupMeetings:false}")
	private String cleanupMeetings;
	@Value("${vc.adobe.daysToKeep:}")
	private String daysToKeep;
	@Value("${vc.adobe.single.meeting.mode:true}")
	private String singleMeetingMode;
	@Value("${vc.adobe.createMeetingImmediately:true}")
	private String createMeetingImmediately;
	@Value("${vc.adobe.login.compatibility.mode:false}")
	private String loginCompatibilityMode;
	
	@Autowired
	public AdobeConnectModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		protocol = getStringPropertyValue(PROTOCOL, protocol);
		String portObj = getStringPropertyValue(PORT, true);
		if(StringHelper.containsNonWhitespace(portObj)) {
			port = Integer.parseInt(portObj);
		}
		userType = getStringPropertyValue(USERTYPE, userType);
		adminLogin = getStringPropertyValue(ADMIN_LOGIN, adminLogin);
		adminPassword = getStringPropertyValue(ADMIN_CRED, adminPassword);
		baseUrl = getStringPropertyValue(BASEURL, baseUrl);
		contextPath = getStringPropertyValue(CONTEXTPATH, contextPath);
		accountId = getStringPropertyValue(ACCOUNTID, accountId);
		providerId = getStringPropertyValue(PROVIDERID, providerId);
		cleanupMeetings = getStringPropertyValue(CLEAN_MEETINGS, cleanupMeetings);
		daysToKeep = getStringPropertyValue(DAYS_TO_KEEP, daysToKeep);
		singleMeetingMode = getStringPropertyValue(SINGLE_MEETING_MODE, singleMeetingMode);
		createMeetingImmediately = getStringPropertyValue(CREATE_MEETING_IMMEDIATELY, createMeetingImmediately);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setBooleanProperty(ENABLED, enabled, true);
	}
	
	public URI getAdobeConnectURI() {
		if(StringHelper.containsNonWhitespace(baseUrl)) {
			UriBuilder builder = getAdobeConnectUriBuilder();
			return builder.build();
		}
		return null;
	}
	
	public UriBuilder getAdobeConnectUriBuilder() {
		String acProtocol = getProtocol();
		UriBuilder builder = UriBuilder.fromUri(acProtocol + "://" + getBaseUrl());
		int acPort = getPort();
		if(acPort > 0
				&& !(acPort == 443 && "https".equals(acProtocol))
				&& !(acPort == 80 && "http".equals(acProtocol))) {
			builder = builder.port(getPort());
		}
		if(StringHelper.containsNonWhitespace(getContextPath())) {
			builder = builder.path(getContextPath());
		}
		return builder;
	}
	
	public UriBuilder getAdobeConnectHostUriBuilder() {
		String acProtocol = getProtocol();
		UriBuilder builder = UriBuilder.fromUri(acProtocol + "://" + getBaseUrl());
		int acPort = getPort();
		if(acPort > 0
				&& !(acPort == 443 && "https".equals(acProtocol))
				&& !(acPort == 80 && "http".equals(acProtocol))) {
			builder = builder.port(getPort());
		}
		return builder;
	}
	
	public void setAdobeConnectURI(URI uri) {
		if(uri == null) {
			setBaseUrl(null);
			setContextPath(null);
			setProtocol(null);
		} else {
			String host = uri.getHost();
			setBaseUrl(host);
			int omPort = uri.getPort();
			setPort(omPort);
			String path = uri.getPath();
			if(StringHelper.containsNonWhitespace(path) && path.startsWith("/")) {
				path = path.substring(1, path.length());
			}
			setContextPath(path);
			String scheme = uri.getScheme();
			setProtocol(scheme);
		}
	}
	
	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
		setStringProperty(PROVIDERID, providerId, true);
	}

	public String getAdminLogin() {
		return adminLogin;
	}

	public void setAdminLogin(String adminLogin) {
		this.adminLogin = adminLogin;
		setStringProperty(ADMIN_LOGIN, adminLogin, true);
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
		setSecretStringProperty(ADMIN_CRED, adminPassword, true);
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
		setStringProperty(PROTOCOL, protocol, true);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		setStringProperty(BASEURL, baseUrl, true);
	}
	
	public String getContextPath() {
		return contextPath;
	}
	
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
		setStringProperty(CONTEXTPATH, contextPath, true);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
		setStringProperty(PORT, String.valueOf(port), true);
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
		setStringProperty(USERTYPE, userType, true);
	}

	public String getAccountId() {
		if("-".equals(accountId)) {
			return null;
		}
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
		setStringProperty(ACCOUNTID, StringHelper.containsNonWhitespace(accountId) ? accountId : "-" , true);
	}

	public boolean isCleanupMeetings() {
		return "true".equals(cleanupMeetings);
	}

	public void setCleanupMeetings(boolean enable) {
		cleanupMeetings = enable ? "true" : "false";
		setStringProperty(CLEAN_MEETINGS, cleanupMeetings, true);
	}

	public long getDaysToKeep() {
		if(StringHelper.isLong(daysToKeep)) {
			return Long.parseLong(daysToKeep);
		}
		return -1;
	}

	public void setDaysToKeep(String daysToKeep) {
		this.daysToKeep = daysToKeep;
		setStringProperty(DAYS_TO_KEEP, daysToKeep, true);
	}

	public boolean isSingleMeetingMode() {
		return "true".equals(singleMeetingMode);
	}

	public void setSingleMeetingMode(boolean enable) {
		singleMeetingMode = enable ? "true" : "false";
		setStringProperty(SINGLE_MEETING_MODE, singleMeetingMode, true);
	}

	public boolean isCreateMeetingImmediately() {
		return "true".equals(createMeetingImmediately);
	}

	public void setCreateMeetingImmediately(boolean enable) {
		createMeetingImmediately = enable ? "true" : "false";
		setStringProperty(CREATE_MEETING_IMMEDIATELY, createMeetingImmediately, true);
	}

	/**
	 * 
	 * @return true if the app. needs to be compatible with the
	 * 		login and password format of the old implementation.
	 */
	public boolean isLoginCompatibilityMode() {
		return "true".equals(loginCompatibilityMode);
	}
}
