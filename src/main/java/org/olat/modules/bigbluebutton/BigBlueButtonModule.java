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
package org.olat.modules.bigbluebutton;

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
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String PROP_ENABLED = "vc.bigbluebutton.enabled";
	private static final String PROP_GROUP_ENABLED = "vc.bigbluebutton.groups";
	private static final String PROP_COURSE_ENABLED = "vc.bigbluebutton.courses";
	private static final String PROP_CLEAN_MEETINGS = "vc.bigbluebutton.cleanupMeetings";
	private static final String PROP_DAYS_TO_KEEP = "vc.bigbluebutton.daysToKeep";
	private static final String PROP_SECRET = "vc.bigbluebutton.secret";
	private static final String PROP_SHARED_SECRET = "vc.bigbluebutton.shared.secret";
	private static final String PROP_PROTOCOL = "vc.bigbluebutton.protocol";
	private static final String PROP_PORT = "vc.bigbluebutton.port";
	private static final String PROP_BASEURL = "vc.bigbluebutton.baseurl";
	private static final String PROP_CONTEXTPATH = "vc.bigbluebutton.contextpath";
	
	@Value("${vc.bigbluebutton.enabled}")
	private boolean enabled;
	
	@Value("${vc.bigbluebutton.protocol:https}")
	private String protocol;
	@Value("${vc.bigbluebutton.port:443}")
	private int port;
	@Value("${vc.bigbluebutton.baseurl}")
	private String baseUrl;
	@Value("${vc.bigbluebutton.context:/api/xml}")
	private String contextPath;
	
	@Value("${vc.bigbluebutton.groups:true}")
	private String groupsEnabled;
	@Value("${vc.bigbluebutton.courses:true}")
	private String coursesEnabled;	
	@Value("${vc.bigbluebutton.cleanupMeetings:false}")
	private String cleanupMeetings;
	@Value("${vc.bigbluebutton.daysToKeep:}")
	private String daysToKeep;
	@Value("${vc.bigbluebutton.secret}")
	private String secret;
	@Value("${vc.bigbluebutton.shared.secret}")
	private String sharedSecret;
	
	
	@Autowired
	public BigBlueButtonModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(PROP_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		protocol = getStringPropertyValue(PROP_PROTOCOL, protocol);
		String portObj = getStringPropertyValue(PROP_PORT, true);
		if(StringHelper.containsNonWhitespace(portObj)) {
			port = Integer.parseInt(portObj);
		}
		baseUrl = getStringPropertyValue(PROP_BASEURL, baseUrl);
		contextPath = getStringPropertyValue(PROP_CONTEXTPATH, contextPath);
		cleanupMeetings = getStringPropertyValue(PROP_CLEAN_MEETINGS, cleanupMeetings);
		daysToKeep = getStringPropertyValue(PROP_DAYS_TO_KEEP, daysToKeep);
		secret = getStringPropertyValue(PROP_SECRET, secret);
		sharedSecret = getStringPropertyValue(PROP_SHARED_SECRET, sharedSecret);
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
		setBooleanProperty(PROP_ENABLED, enabled, true);
	}
	
	public boolean isGroupsEnabled() {
		return "true".equals(groupsEnabled);
	}

	public void setGroupsEnabled(boolean enabled) {
		groupsEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_GROUP_ENABLED, groupsEnabled, true);
	}

	public boolean isCoursesEnabled() {
		return "true".equals(coursesEnabled);
	}

	public void setCoursesEnabled(boolean enabled) {
		coursesEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_COURSE_ENABLED, coursesEnabled, true);
	}
	
	public URI getBigBlueButtonURI() {
		if(StringHelper.containsNonWhitespace(baseUrl)) {
			UriBuilder builder = getBigBlueButtonUriBuilder();
			return builder.build();
		}
		return null;
	}
	
	public UriBuilder getBigBlueButtonUriBuilder() {
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
	
	public UriBuilder getBigBlueButtonHostUriBuilder() {
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
	
	public void setBigBlueButtonURI(URI uri) {
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
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		setStringProperty(PROP_BASEURL, baseUrl, true);
	}
	
	public String getContextPath() {
		return contextPath;
	}
	
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
		setStringProperty(PROP_CONTEXTPATH, contextPath, true);
	}
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
		setStringProperty(PROP_PROTOCOL, protocol, true);
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
		setStringProperty(PROP_PORT, String.valueOf(port), true);
	}
	
	public String getSecret() {
		return secret;
	}
	
	public void setSecret(String secret) {
		this.secret = secret;
		setStringProperty(PROP_SECRET, secret, true);
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
		setStringProperty(PROP_SHARED_SECRET, sharedSecret, true);
	}

	public boolean isCleanupMeetings() {
		return "true".equals(cleanupMeetings);
	}

	public void setCleanupMeetings(boolean enable) {
		cleanupMeetings = enable ? "true" : "false";
		setStringProperty(PROP_CLEAN_MEETINGS, cleanupMeetings, true);
	}
	
	public long getDaysToKeep() {
		if(StringHelper.isLong(daysToKeep)) {
			return Long.parseLong(daysToKeep);
		}
		return -1l;
	}
	
	public void setDaysToKeep(String days) {
		this.daysToKeep = days;
		setStringProperty(PROP_DAYS_TO_KEEP, days, true);
		
	}

}
