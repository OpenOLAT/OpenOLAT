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
package org.olat.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * Configuration of the REST API
 * 
 * <P>
 * Initial Date:  18 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service("restModule")
public class RestModule extends AbstractSpringModule implements ConfigOnOff {
	
	public static final String RESTAPI_AUTH = "API-Key";
	
	private static final String ENABLED = "enabled";
	private static final String USER_ALLOWED_GENERATE_APIKEY = "restapi.user.generate.apikey";

	@Value("${restapi.enable:false}")
	private boolean enabled;
	@Value("${restapi.ips.system}")
	private String ipsByPass;
	@Value("${restapi.user.generate.apikey:false}")
	private boolean userAllowedGenerateApiKey;
	

	@Autowired
	public RestModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "enabled".equals(enabledObj);
		}
		
		String enabledGenerateApiKeyObj = getStringPropertyValue(USER_ALLOWED_GENERATE_APIKEY, true);
		if(StringHelper.containsNonWhitespace(enabledGenerateApiKeyObj)) {
			userAllowedGenerateApiKey = "enabled".equals(enabledGenerateApiKeyObj);
		}
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
		String enabledStr = enabled ? "enabled" : "disabled";
		setStringProperty(ENABLED, enabledStr, true);
	}
	
	public boolean isUserAllowedGenerateApiKey() {
		return userAllowedGenerateApiKey;
	}

	public void setUserAllowedGenerateApiKey(boolean enable) {
		userAllowedGenerateApiKey = enable;
		String enabledStr = enabled ? "enabled" : "disabled";
		setStringProperty(USER_ALLOWED_GENERATE_APIKEY, enabledStr, true);
	}

	public String getIpsByPass() {
		return ipsByPass;
	}
	
	public List<String> getIpsWithSystemAccess() {
		List<String> ips = new ArrayList<>();
		for(StringTokenizer tokenizer=new StringTokenizer(ipsByPass, ",;|"); tokenizer.hasMoreTokens(); ) {
			String token = tokenizer.nextToken();
			if(StringHelper.containsNonWhitespace(token)) {
				ips.add(token);
			}
		}
		return ips;
	}

	public void setIpsByPass(String ipsByPass) {
		this.ipsByPass = ipsByPass;
	}
}
