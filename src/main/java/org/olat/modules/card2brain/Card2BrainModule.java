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
package org.olat.modules.card2brain;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 07.04.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class Card2BrainModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String CARD2BRAIN_ENABLED = "card2brain.enabled";
	public static final String CARD2BRAIN_ENTERPRISE_LOGIN_ENABLED = "card2brain.enterpriseLoginEnabled";
	public static final String CARD2BRAIN_ENTERPRISE_KEY = "card2brain.enterpriseKey";
	public static final String CARD2BRAIN_ENTERPRISE_SECRET= "card2brain.enterpriseSecret";
	public static final String CARD2BRAIN_BASE_URL = "card2brain.baseUrl";
	public static final String CARD2BRAIN_PEEK_VIEW_URL = "card2brain.peekViewUrl";
	
	@Value("${card2brain.enabled:false}")
	private boolean enabled;
	@Value("${card2brain.enterpriseLoginEnalbled:false}")
	private boolean enterpriseLoginEnabled;
	private String enterpriseKey;
	private String enterpriseSecret;
	@Value("${card2brain.baseUrl:null}")
	private String baseUrl;
	@Value("${card2brain.peekViewUrl:null}")
	private String peekViewUrl;
	
	@Autowired
	public Card2BrainModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(CARD2BRAIN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String enterpriseLoginEnabledObj = getStringPropertyValue(CARD2BRAIN_ENTERPRISE_LOGIN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enterpriseLoginEnabledObj)) {
			enterpriseLoginEnabled = "true".equals(enterpriseLoginEnabledObj);
		}

		String enterpriseKeyObj = getStringPropertyValue(CARD2BRAIN_ENTERPRISE_KEY, true);
		if(StringHelper.containsNonWhitespace(enterpriseKeyObj)) {
			enterpriseKey = enterpriseKeyObj;
		}

		String enterpriseSecretObj = getStringPropertyValue(CARD2BRAIN_ENTERPRISE_SECRET, true);
		if(StringHelper.containsNonWhitespace(enterpriseSecretObj)) {
			enterpriseSecret = enterpriseSecretObj;
		}
		
		String baseUrlObj = getStringPropertyValue(CARD2BRAIN_BASE_URL, true);
		if(StringHelper.containsNonWhitespace(baseUrlObj)) {
			baseUrl = baseUrlObj;
		}

		String peekViewUrlObj = getStringPropertyValue(CARD2BRAIN_PEEK_VIEW_URL, true);
		if(StringHelper.containsNonWhitespace(peekViewUrlObj)) {
			peekViewUrl = peekViewUrlObj;
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
		setStringProperty(CARD2BRAIN_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isEnterpriseLoginEnabled() {
		return enterpriseLoginEnabled;
	}
	
	public void setEnterpriseLoginEnabled(boolean enterpriseLoginEnalbled) {
		this.enterpriseLoginEnabled = enterpriseLoginEnalbled;
		setStringProperty(CARD2BRAIN_ENTERPRISE_LOGIN_ENABLED, Boolean.toString(enterpriseLoginEnalbled), true);
	}

	public String getEnterpriseKey() {
		return enterpriseKey;
	}

	public void setEnterpriseKey(String enterpriseKey) {
		this.enterpriseKey = enterpriseKey;
		setStringProperty(CARD2BRAIN_ENTERPRISE_KEY, enterpriseKey, true);
	}

	public String getEnterpriseSecret() {
		return enterpriseSecret;
	}

	public void setEnterpriseSecret(String enterpriseSecret) {
		this.enterpriseSecret = enterpriseSecret;
		setStringProperty(CARD2BRAIN_ENTERPRISE_SECRET, enterpriseSecret, true);
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		setStringProperty(CARD2BRAIN_BASE_URL, baseUrl, true);
	}
	
	public String getPeekViewUrl() {
		return peekViewUrl;
	}

	public void setPeekViewUrl(String peekViewUrl) {
		this.peekViewUrl = peekViewUrl;
		setStringProperty(CARD2BRAIN_PEEK_VIEW_URL, peekViewUrl, true);
	}
	
	/**
	 * Check if the use of a certain login is safe.<br>
	 * - If the enterprise login is enable, it is safe to use a enterprise login.<br>
	 * - If the enterprise login is not enabled, it is only safe to not use a enterprise login.
	 * 
	 * @param enterpriseLogin true to check a enterprise login
	 * @return whether the enterprise can be used safe
	 */
	public boolean isLoginSafe(boolean enterpriseLogin) {
		return this.isEnterpriseLoginEnabled() || !enterpriseLogin;
	}

}
