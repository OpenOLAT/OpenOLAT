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
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.bigbluebutton.manager.BigBlueButtonNativeRecordingsHandler;
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
	private static final String PROP_APPOINTMENTS_ENABLED = "vc.bigbluebutton.appointments";
	private static final String PROP_SECRET = "vc.bigbluebutton.secret";
	private static final String PROP_SHARED_SECRET = "vc.bigbluebutton.shared.secret";
	private static final String PROP_PROTOCOL = "vc.bigbluebutton.protocol";
	private static final String PROP_PORT = "vc.bigbluebutton.port";
	private static final String PROP_BASEURL = "vc.bigbluebutton.baseurl";
	private static final String PROP_CONTEXTPATH = "vc.bigbluebutton.contextpath";
	private static final String PROP_PERMANENT_MEETING = "vc.bigbluebutton.permanent.meeting";
	private static final String PROP_ADHOC_MEETING = "vc.bigbluebutton.adhoc.meeting";
	private static final String PROP_USER_BANDWIDTH_REQUIREMENT = "vc.bigbluebutton.user.bandwidth.requirement";
	private static final String PROP_RECORDING_HANDLER_ID = "vc.bigbluebutton.recording.handler.id";
	private static final String PROP_MAX_UPLOAD_SIZE = "vc.bigbluebutton.max.upload.size";
	
	public static final Set<String> SLIDES_MIME_TYPES = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png", "application/pdf",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

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
	@Value("${vc.bigbluebutton.appointments:true}")
	private String appointmentsEnabled;
	@Value("${vc.bigbluebutton.secret}")
	private String secret;
	@Value("${vc.bigbluebutton.shared.secret}")
	private String sharedSecret;

	@Value("${vc.bigbluebutton.max.upload.size:100}")
	private Integer maxUploadSize;
	
	@Value("${vc.bigbluebutton.permanent.meeting:false}")
	private String permanentMeetingEnabled;
	@Value("${vc.bigbluebutton.adhoc.meeting:true}")
	private String adhocMeetingEnabled;
	@Value("${vc.bigbluebutton.user.bandwidth.requirement:0.4}")
	private Double userBandwidhtRequirement;
	
	@Value("${vc.http.connect.timeout:30000}")
	private int httpConnectTimeout;
	@Value("${vc.http.connect.request.timeout:30000}")
	private int httpConnectRequestTimeout;
	@Value("${vc.http.connect.socket.timeout:30000}")
	private int httpSocketTimeout;
	
	@Value("${vc.bigbluebutton.recording.handler.id:native}")
	private String recordingHandlerId;
	
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
		secret = getStringPropertyValue(PROP_SECRET, secret);
		sharedSecret = getStringPropertyValue(PROP_SHARED_SECRET, sharedSecret);
		adhocMeetingEnabled = getStringPropertyValue(PROP_ADHOC_MEETING, adhocMeetingEnabled);
		permanentMeetingEnabled = getStringPropertyValue(PROP_PERMANENT_MEETING, permanentMeetingEnabled);
		
		groupsEnabled = getStringPropertyValue(PROP_GROUP_ENABLED, groupsEnabled);
		coursesEnabled = getStringPropertyValue(PROP_COURSE_ENABLED, coursesEnabled);
		appointmentsEnabled = getStringPropertyValue(PROP_APPOINTMENTS_ENABLED, appointmentsEnabled);

		String maxUploadSizeObj = getStringPropertyValue(PROP_MAX_UPLOAD_SIZE, maxUploadSize.toString());
		if(StringHelper.containsNonWhitespace(maxUploadSizeObj)) {
			maxUploadSize = Integer.valueOf(maxUploadSizeObj);
		}
		
		String bandwidthReqObj = getStringPropertyValue(PROP_USER_BANDWIDTH_REQUIREMENT, true);
		if(StringHelper.containsNonWhitespace(bandwidthReqObj)) {
			userBandwidhtRequirement = Double.parseDouble(bandwidthReqObj);
		}

		recordingHandlerId = getStringPropertyValue(PROP_RECORDING_HANDLER_ID, recordingHandlerId);
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
	
	public boolean isAppointmentsEnabled() {
		return "true".equals(appointmentsEnabled);
	}

	public void setAppointmentsEnabled(boolean enabled) {
		appointmentsEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_APPOINTMENTS_ENABLED, appointmentsEnabled, true);
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

	/**
	 * 
	 * @return The max. size of the slides to upload to BigBlueButton servers
	 * 		in MB.
	 */
	public Integer getMaxUploadSize() {
		return maxUploadSize;
	}

	public void setMaxUploadSize(Integer maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
		setStringProperty(PROP_MAX_UPLOAD_SIZE, maxUploadSize.toString(), true);
	}

	public Double getUserBandwidhtRequirement() {
		return userBandwidhtRequirement;
	}

	public void setUserBandwidhtRequirement(Double userBandwidhtRequirement) {
		this.userBandwidhtRequirement = userBandwidhtRequirement;
		setStringProperty(PROP_USER_BANDWIDTH_REQUIREMENT, userBandwidhtRequirement.toString(), true);
	}

	public boolean isPermanentMeetingEnabled() {
		return "true".equals(permanentMeetingEnabled);
	}

	public void setPermanentMeetingEnabled(boolean permanentMeetingEnabled) {
		this.permanentMeetingEnabled = Boolean.toString(permanentMeetingEnabled);
		setStringProperty(PROP_PERMANENT_MEETING, this.permanentMeetingEnabled, true);
	}

	public boolean isAdhocMeetingEnabled() {
		return "true".equals(adhocMeetingEnabled);
	}

	public void setAdhocMeetingEnabled(boolean adhocMeetingEnabled) {
		this.adhocMeetingEnabled = Boolean.toString(adhocMeetingEnabled);
		setStringProperty(PROP_ADHOC_MEETING, this.adhocMeetingEnabled, true);
	}
	
	public String getRecordingHandlerId() {
		return StringHelper.containsNonWhitespace(recordingHandlerId)
				? recordingHandlerId : BigBlueButtonNativeRecordingsHandler.NATIVE_RECORDING_HANDLER_ID;
	}

	public void setRecordingHandlerId(String recordingHandlerId) {
		this.recordingHandlerId = recordingHandlerId;
		setStringProperty(PROP_RECORDING_HANDLER_ID, recordingHandlerId, true);
	}

	public int getHttpConnectTimeout() {
		return httpConnectTimeout;
	}

	public int getHttpConnectRequestTimeout() {
		return httpConnectRequestTimeout;
	}

	public int getHttpSocketTimeout() {
		return httpSocketTimeout;
	}
}
