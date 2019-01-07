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
package org.olat.modules.edusharing;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdusharingModule extends AbstractSpringModule implements ConfigOnOff {

	private static final OLog log = Tracing.createLoggerFor(EdusharingModule.class);

	private static final String EDUSHARING_ENABLED = "edusharing.enabled";
	private static final String EDUSHARING_BASE_URL = "edusharing.url";
	private static final String EDUSHARING_APP_ID = "edusharing.app.id";
	private static final String EDUSHARING_HOST = "edusharing.host";
	private static final String EDUSHARING_TICKET_VALID_SECONDS = "edusharing.ticket.valid.seconds";
	private static final String EDUSHARING_SOAP_PUBLIC_KEY = "edusharing.soap.public.key";
	private static final String EDUSHARING_SOAP_PRIVATE_KEY = "edusharing.soap.private.key";
	private static final String EDUSHARING_REPO_PUBLIC_KEY = "edusharing.repo.public.key";
	
	@Value("${edusharing.enabled:false}")
	private boolean enabled;
	@Value("${edusharing.url}")
	private String baseUrl;
	@Value("${edusharing.app.id}")
	private String appId;
	@Value("${edusharing.host}")
	private String host;
	@Value("${edusharing.ticket.valid.seconds:10}")
	private int ticketValidSeconds;
	private KeyPair soapKeys;
	private String soapPublicKey;
	private String soapPrivateKey;
	// public key of the edu-sharing (home) repository.
	private PublicKey repoPublicKey;
	private String repoPublicKeyString;

	@Autowired
	private EdusharingSecurityService edusharingSignature;

	@Autowired
	private EdusharingModule(CoordinatorManager coordinateManager) {
		super(coordinateManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(EDUSHARING_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String baseUrlObj = getStringPropertyValue(EDUSHARING_BASE_URL, true);
		if(StringHelper.containsNonWhitespace(baseUrlObj)) {
			baseUrl = baseUrlObj;
		}
		
		String appIdObj = getStringPropertyValue(EDUSHARING_APP_ID, true);
		if(StringHelper.containsNonWhitespace(appIdObj)) {
			appId = appIdObj;
		}
		
		String hostObj = getStringPropertyValue(EDUSHARING_HOST, true);
		if(StringHelper.containsNonWhitespace(hostObj)) {
			host = hostObj;
		}
		
		ticketValidSeconds = getIntPropertyValue(EDUSHARING_TICKET_VALID_SECONDS);
		
		String soapPublicKeyObj = getStringPropertyValue(EDUSHARING_SOAP_PUBLIC_KEY, true);
		if(StringHelper.containsNonWhitespace(soapPublicKeyObj)) {
			soapPublicKey = soapPublicKeyObj;
		}
		
		String soapPrivateKeyObj = getStringPropertyValue(EDUSHARING_SOAP_PRIVATE_KEY, true);
		if(StringHelper.containsNonWhitespace(soapPrivateKeyObj)) {
			soapPrivateKey = soapPrivateKeyObj;
		}
		
		String repoPublicKeyObj = getStringPropertyValue(EDUSHARING_REPO_PUBLIC_KEY, true);
		if(StringHelper.containsNonWhitespace(repoPublicKeyObj)) {
			repoPublicKeyString = repoPublicKeyObj;
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(EDUSHARING_ENABLED, Boolean.toString(enabled), true);
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		setStringProperty(EDUSHARING_BASE_URL, baseUrl, true);
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
		setStringProperty(EDUSHARING_APP_ID, appId, true);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
		setStringProperty(EDUSHARING_HOST, host, true);
	}

	public int getTicketValidSeconds() {
		return ticketValidSeconds;
	}

	public void setTicketValidSeconds(int ticketValidSeconds) {
		this.ticketValidSeconds = ticketValidSeconds;
		setIntProperty(EDUSHARING_TICKET_VALID_SECONDS, ticketValidSeconds, true);
	}

	public KeyPair getSoapKeys() {
		if (soapKeys == null && StringHelper.containsNonWhitespace(soapPublicKey) && StringHelper.containsNonWhitespace(soapPrivateKey)) {
			try {
				PublicKey publicKey = edusharingSignature.toPublicKey(soapPublicKey);
				PrivateKey privateKey = edusharingSignature.toPrivateKey(soapPrivateKey);
				soapKeys = new KeyPair(publicKey, privateKey);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return soapKeys;
	}

	public void setSoapKeys(KeyPair soapKeys) {
		this.soapKeys = soapKeys;
		this.soapPublicKey = edusharingSignature.getPublicKey(soapKeys);
		setStringProperty(EDUSHARING_SOAP_PUBLIC_KEY, soapPublicKey, true);
		this.soapPrivateKey = edusharingSignature.getPrivateKey(soapKeys);
		setStringProperty(EDUSHARING_SOAP_PRIVATE_KEY, soapPrivateKey, true);
	}

	public PublicKey getRepoPublicKey() {
		if (repoPublicKey == null && StringHelper.containsNonWhitespace(repoPublicKeyString)) {
			try {
				repoPublicKey = edusharingSignature.toPublicKey(repoPublicKeyString);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return repoPublicKey;
	}

	public String getRepoPublicKeyString() {
		return repoPublicKeyString;
	}

	public void setRepoPublicKeyString(String repoPublicKeyString) {
		this.repoPublicKey = null;
		this.repoPublicKeyString = repoPublicKeyString;
		setStringProperty(EDUSHARING_REPO_PUBLIC_KEY, repoPublicKeyString, true);
	}
	
}
