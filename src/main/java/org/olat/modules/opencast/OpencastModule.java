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
package org.olat.modules.opencast;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.opencast.AuthDelegate.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Aug 2020<br>
 * @admin uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OpencastModule extends AbstractSpringModule implements ConfigOnOff {

	private static final Logger log = Tracing.createLoggerFor(OpencastModule.class);

	private static final String ENABLED = "opencast.enabled";
	private static final String API_URL = "api.url";
	private static final String API_PRESENTATION_URL = "api.presentation.url";
	private static final String API_USERNAME = "api.username";
	private static final String API_PASSOWRD = "api.password";
	private static final String LTI_URL = "lti.url";
	private static final String LTI_SIGN_URL = "lti.sign.url";
	private static final String LTI_KEY = "lti.key";
	private static final String LTI_SECRET = "lti.secret";
	private static final String BBB_ENABLED = "opencast.bbb.enabled";
	private static final String COURSE_NODE_ENABLED = "opencast.course.node.enabled";
	private static final String ROLES_ADMIN = "roles.admin";
	private static final String ROLES_COACH = "roles.coach";
	private static final String ROLES_PARTICIPANT = "roles.participant";
	private static final String AUTH_DELEGATE_TYPE = "auth.delegate";
	private static final String AUTH_DELEGATE_ROLES = "auth.delegate.roles";
	
	@Value("${opencast.enabled}")
	private boolean enabled;
	@Value("${opencast.api.url}")
	private String apiUrl;
	@Value("${opencast.api.presentation.url}")
	private String apiPresentationUrl;
	@Value("${opencast.api.username}")
	private String apiUsername;
	@Value("${opencast.api.password}")
	private String apiPassword;
	@Value("${opencast.lti.url}")
	private String ltiUrl;
	@Value("${opencast.lti.sign.url}")
	private String ltiSignUrl;
	@Value("${opencast.lti.key}")
	private String ltiKey;
	@Value("${opencast.lti.secret}")
	private String ltiSecret;
	@Value("${opencast.bbb.enabled}")
	private boolean bigBlueButtonEnabled;
	@Value("${opencast.course.node.enabled}")
	private boolean courseNodeEnabled;
	@Value("${opencast.course.node.roles.admin}")
	private String rolesAdmin;
	@Value("${opencast.course.node.roles.coach}")
	private String rolesCoach;
	@Value("${opencast.course.node.roles.participant}")
	private String rolesParticipant;
	@Value("${opencast.course.node.auth.delegate}")
	private Type authDelegateType;
	@Value("${opencast.course.node.auth.delegate.roles}")
	private String authDelegateRoles;

	private String apiAuthorizationHeader;
	
	@Autowired
	public OpencastModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		apiUrl = getStringPropertyValue(API_URL, apiUrl);
		apiPresentationUrl = getStringPropertyValue(API_PRESENTATION_URL, apiPresentationUrl);
		apiUsername = getStringPropertyValue(API_USERNAME, apiUsername);
		apiPassword = getStringPropertyValue(API_PASSOWRD, apiPassword);
		refreshApiAuthorization();

		ltiUrl = getStringPropertyValue(LTI_URL, ltiUrl);
		ltiSignUrl = getStringPropertyValue(LTI_SIGN_URL, ltiSignUrl);
		ltiKey = getStringPropertyValue(LTI_KEY, ltiKey);
		ltiSecret = getStringPropertyValue(LTI_SECRET, ltiSecret);
		
		String bbbEnabledObj = getStringPropertyValue(BBB_ENABLED, true);
		if(StringHelper.containsNonWhitespace(bbbEnabledObj)) {
			bigBlueButtonEnabled = "true".equals(bbbEnabledObj);
		}
		
		String courseNodeEnabledObj = getStringPropertyValue(COURSE_NODE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(courseNodeEnabledObj)) {
			courseNodeEnabled = "true".equals(courseNodeEnabledObj);
		}
		
		rolesAdmin = getStringPropertyValue(ROLES_ADMIN, rolesAdmin);
		rolesCoach = getStringPropertyValue(ROLES_COACH, rolesCoach);
		rolesParticipant = getStringPropertyValue(ROLES_PARTICIPANT, rolesParticipant);
		
		String authDelegateObj = getStringPropertyValue(AUTH_DELEGATE_TYPE, true);
		if (StringHelper.containsNonWhitespace(authDelegateObj) && AuthDelegate.Type.isValid(authDelegateObj)) {
			authDelegateType = Type.valueOf(authDelegateObj);
		} else {
			authDelegateType = Type.User;
		}
		authDelegateRoles = getStringPropertyValue(AUTH_DELEGATE_ROLES, authDelegateRoles);
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
		setStringProperty(ENABLED, Boolean.toString(enabled), true);
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
		setStringProperty(API_URL, apiUrl, true);
	}

	public String getApiPresentationUrl() {
		return apiPresentationUrl;
	}

	public void setApiPresentationUrl(String apiPresentationUrl) {
		this.apiPresentationUrl = apiPresentationUrl;
		setStringProperty(API_PRESENTATION_URL, apiPresentationUrl, true);
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public String getApiUsername() {
		return apiUsername;
	}

	public String getApiPassword() {
		return apiPassword;
	}

	public void setApiCredentials(String apiUsername, String apiPassword) {
		this.apiUsername = apiUsername;
		setStringProperty(API_USERNAME, apiUsername, true);
		
		this.apiPassword = apiPassword;
		setSecretStringProperty(API_PASSOWRD, apiPassword, true);
		
		refreshApiAuthorization();
	}
	
	/*
	 * Did not work with BasicCredentialsProvider!?
	 * So let's create the AUTHORIZATION header by ourself.
	 */
	private void refreshApiAuthorization() {
		try {
			String auth = apiUsername + ":" + apiPassword;
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
			apiAuthorizationHeader = "Basic " + new String(encodedAuth);
		} catch (Exception e) {
			log.error("Opencast AUTHORIZATION header not created", e);
		}
	}

	public String getApiAuthorizationHeader() {
		return apiAuthorizationHeader;
	}
	
	public String getLtiUrl() {
		return ltiUrl;
	}
	
	public void setLtiUrl(String ltiUrl) {
		this.ltiUrl = ltiUrl;
		setStringProperty(LTI_URL, ltiUrl, true);
	}

	public String getLtiSignUrl() {
		return StringHelper.containsNonWhitespace(ltiSignUrl)? ltiSignUrl: ltiUrl;
	}
	
	public String getLtiSignUrlRaw() {
		return ltiSignUrl;
	}

	public void setLtiSignUrl(String ltiSignUrl) {
		this.ltiSignUrl = ltiSignUrl;
		setStringProperty(LTI_SIGN_URL, ltiSignUrl, true);
	}

	public String getLtiKey() {
		return ltiKey;
	}

	public void setLtiKey(String ltiKey) {
		this.ltiKey = ltiKey;
		setStringProperty(LTI_KEY, ltiKey, true);
	}

	public String getLtiSecret() {
		return ltiSecret;
	}

	public void setLtiSecret(String ltiSecret) {
		this.ltiSecret = ltiSecret;
		setStringProperty(LTI_SECRET, ltiSecret, true);
	}

	public boolean isBigBlueButtonEnabled() {
		return enabled && bigBlueButtonEnabled;
	}
	
	public boolean isBigBlueButtonEnabledRaw() {
		return bigBlueButtonEnabled;
	}

	public void setBigBlueButtonEnabled(boolean bigBlueButtonEnabled) {
		this.bigBlueButtonEnabled = bigBlueButtonEnabled;
		setStringProperty(BBB_ENABLED, Boolean.toString(bigBlueButtonEnabled), true);
	}

	public boolean isCourseNodeEnabled() {
		return enabled && courseNodeEnabled;
	}
	
	public boolean isCourseNodeEnabledRaw() {
		return courseNodeEnabled;
	}

	public void setCourseNodeEnabled(boolean courseNodeEnabled) {
		this.courseNodeEnabled = courseNodeEnabled;
		setStringProperty(COURSE_NODE_ENABLED, Boolean.toString(courseNodeEnabled), true);
	}

	public String getRolesAdmin() {
		return rolesAdmin;
	}

	public void setRolesAdmin(String rolesAdmin) {
		this.rolesAdmin = rolesAdmin;
		setStringProperty(ROLES_ADMIN, rolesAdmin, true);
	}

	public String getRolesCoach() {
		return rolesCoach;
	}

	public void setRolesCoach(String rolesCoach) {
		this.rolesCoach = rolesCoach;
		setStringProperty(ROLES_COACH, rolesCoach, true);
	}

	public String getRolesParticipant() {
		return rolesParticipant;
	}

	public void setRolesParticipant(String rolesParticipant) {
		this.rolesParticipant = rolesParticipant;
		setStringProperty(ROLES_PARTICIPANT, rolesParticipant, true);
	}

	public Type getAuthDelegateType() {
		return authDelegateType;
	}

	public void setAuthDelegateType(Type authDelegateType) {
		this.authDelegateType = authDelegateType;
		setStringProperty(AUTH_DELEGATE_TYPE, authDelegateType.name(), true);
	}

	public String getAuthDelegateRoles() {
		return authDelegateRoles;
	}

	public void setAuthDelegateRoles(String authDelegateRoles) {
		this.authDelegateRoles = authDelegateRoles;
		setStringProperty(AUTH_DELEGATE_ROLES, authDelegateRoles, true);
	}

}
