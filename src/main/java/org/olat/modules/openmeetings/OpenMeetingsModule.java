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
package org.olat.modules.openmeetings;

import java.net.URI;

import jakarta.ws.rs.core.UriBuilder;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("openmeetingsModule")
public class OpenMeetingsModule  extends AbstractSpringModule implements ConfigOnOff {

	private static final String ENABLED = "vc.openmeetings.enabled";
	private static final String PROTOCOL = "protocol";
	private static final String PORT = "port";
	private static final String BASE_URL = "baseUrl";
	private static final String CONTEXT_PATH = "contextPath";
	private static final String ADMIN_LOGIN = "adminLogin";
	private static final String ADMIN_CREDENTIAL = "adminPassword";
	
	@Value("${vc.openmeetings.enabled}")
	private boolean enabled;
	private String displayName;
	@Value("${vc.openmeetings.protocol}")
	private String protocol;
	@Value("${vc.openmeetings.port}")
	private int port;
	@Value("${vc.openmeetings.baseurl}")
	private String baseUrl;
	@Value("${vc.openmeetings.contextPath}")
	private String contextPath;
	@Value("${vc.openmeetings.adminlogin}")
	private String adminLogin;
	@Value("${vc.openmeetings.adminpassword}")
	private String adminPassword;
	@Value("${vc.openmeetings.supportemail:#{null}}")
	private String supportEmail;

	
	@Autowired
	public OpenMeetingsModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String protocolObj = getStringPropertyValue(PROTOCOL, true);
		if(StringHelper.containsNonWhitespace(protocolObj)) {
			protocol = protocolObj;
		}
		String portObj = getStringPropertyValue(PORT, true);
		if(StringHelper.containsNonWhitespace(portObj)) {
			port = Integer.parseInt(portObj);
		}
		String baseUrlObj = getStringPropertyValue(BASE_URL, true);
		if(StringHelper.containsNonWhitespace(baseUrlObj)) {
			baseUrl = baseUrlObj;
		}
		String contextPathObj = getStringPropertyValue(CONTEXT_PATH, true);
		if(StringHelper.containsNonWhitespace(contextPathObj)) {
			contextPath = contextPathObj;
		}
		String adminLoginObj = getStringPropertyValue(ADMIN_LOGIN, true);
		if(StringHelper.containsNonWhitespace(adminLoginObj)) {
			adminLogin = adminLoginObj;
		}
		String adminPasswordObj = getStringPropertyValue(ADMIN_CREDENTIAL, true);
		if(StringHelper.containsNonWhitespace(adminPasswordObj)) {
			adminPassword = adminPasswordObj;
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public URI getOpenMeetingsURI() {
		UriBuilder builder = UriBuilder.fromUri(getProtocol() + "://" + getBaseUrl());
		if(getPort() > 0) {
			builder = builder.port(getPort());
		}
		if(StringHelper.containsNonWhitespace(getContextPath())) {
			builder = builder.path(getContextPath());
		}
		return builder.build();
	}
	
	public void setOpenMeetingsURI(URI uri) {
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
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		setBooleanProperty(ENABLED, enabled, true);
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		setStringProperty(PROTOCOL, protocol, true);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		setStringProperty(PORT, Integer.toString(port), true);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		setStringProperty(BASE_URL, baseUrl, true);
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		setStringProperty(CONTEXT_PATH, contextPath, true);
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAdminLogin() {
		return adminLogin;
	}

	public void setAdminLogin(String adminLogin) {
		setStringProperty(ADMIN_LOGIN, adminLogin, true);
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		setSecretStringProperty(ADMIN_CREDENTIAL, adminPassword, true);
	}

	public String getSupportEmail() {
		return supportEmail;
	}

	public void setSupportEmail(String supportEmail) {
		this.supportEmail = supportEmail;
	}
	
	
}
