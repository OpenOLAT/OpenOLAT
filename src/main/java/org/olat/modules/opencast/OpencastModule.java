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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OpencastModule extends AbstractSpringModule implements ConfigOnOff {

	private static final Logger log = Tracing.createLoggerFor(OpencastModule.class);

	private static final String ENABLED = "opencast.enabled";
	private static final String API_URL = "api.url";
	private static final String API_USERNAME = "api.username";
	private static final String API_PASSOWRD = "api.password";
	private static final String LTI_URL = "lti.url";
	private static final String LTI_KEY = "lti.key";
	private static final String LTI_SECRET = "lti.secret";
	
	@Value("${opencast.enabled}")
	private boolean enabled;
	@Value("${opencast.api.url}")
	private String apiUrl;
	@Value("${opencast.api.username}")
	private String apiUsername;
	@Value("${opencast.api.password}")
	private String apiPassword;
	@Value("${opencast.lti.url}")
	private String ltiUrl;
	@Value("${opencast.lti.key}")
	private String ltiKey;
	@Value("${opencast.lti.secret}")
	private String ltiSecret;

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
		apiUsername = getStringPropertyValue(API_USERNAME, apiUsername);
		apiPassword = getStringPropertyValue(API_PASSOWRD, apiPassword);
		refreshApiAutorization();

		ltiUrl = getStringPropertyValue(LTI_URL, ltiUrl);
		ltiKey = getStringPropertyValue(LTI_KEY, ltiKey);
		ltiSecret = getStringPropertyValue(LTI_SECRET, ltiSecret);
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
		
		refreshApiAutorization();
	}
	
	/*
	 * Did not work with BasicCredentialsProvider!?
	 * So let's create the AUTHORIZATION header by ourself.
	 */
	private void refreshApiAutorization() {
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

}
