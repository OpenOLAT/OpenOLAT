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
package org.olat.login.oauth;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OAuthLoginModule extends AbstractSpringModule {
	
	private boolean allowUserCreation;
	
	private boolean linkedInEnabled;
	private String linkedInApiKey;
	private String linkedInApiSecret;
	private String linkedInScopes;
	
	private boolean twitterEnabled;
	private String twitterApiKey;
	private String twitterApiSecret;
	
	private boolean googleEnabled;
	private String googleApiKey;
	private String googleApiSecret;
	
	private boolean facebookEnabled;
	private String facebookApiKey;
	private String facebookApiSecret;
	
	private boolean adfsEnabled;
	private boolean adfsRootEnabled;
	private String adfsApiKey;
	private String adfsOAuth2Endpoint;
	
	
	@Autowired
	private List<OAuthSPI> oauthSPIs;
	
	@Autowired
	public OAuthLoginModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager, true);
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
		String allowUserCreationObj = getStringPropertyValue("allowUserCreation", true);
		allowUserCreation = "true".equals(allowUserCreationObj);
		
		//linkedin
		String linkedInEnabledObj = getStringPropertyValue("linkedInEnabled", true);
		linkedInEnabled = "true".equals(linkedInEnabledObj);
		linkedInApiKey = getStringPropertyValue("linkedInApiKey", false);
		linkedInApiSecret = getStringPropertyValue("linkedInApiSecret", false);
		linkedInScopes = getStringPropertyValue("linkedInScopes", false);	
		
		//twitter
		String twitterEnabledObj = getStringPropertyValue("twitterEnabled", true);
		twitterEnabled = "true".equals(twitterEnabledObj);
		twitterApiKey = getStringPropertyValue("twitterApiKey", false);
		twitterApiSecret = getStringPropertyValue("twitterApiSecret", false);
		
		//google
		String googleEnabledObj = getStringPropertyValue("googleEnabled", true);
		googleEnabled = "true".equals(googleEnabledObj);
		googleApiKey = getStringPropertyValue("googleApiKey", false);
		googleApiSecret = getStringPropertyValue("googleApiSecret", false);
		
		//facebook
		String facebookEnabledObj = getStringPropertyValue("facebookEnabled", true);
		facebookEnabled = "true".equals(facebookEnabledObj);
		facebookApiKey = getStringPropertyValue("facebookApiKey", false);
		facebookApiSecret = getStringPropertyValue("facebookApiSecret", false);
		
		//adfs
		String adfsEnabledObj = getStringPropertyValue("adfsEnabled", true);
		adfsEnabled = "true".equals(adfsEnabledObj);
		String adfsRootEnabledObj = getStringPropertyValue("adfsRootEnabled", true);
		adfsRootEnabled = "true".equals(adfsRootEnabledObj);
		adfsApiKey = getStringPropertyValue("adfsApiKey", false);
		adfsOAuth2Endpoint = getStringPropertyValue("adfsOAuth2Endpoint", false);
	}
	
	public List<OAuthSPI> getAllSPIs() {
		return new ArrayList<>(oauthSPIs);
	}
	
	public List<OAuthSPI> getEnableSPIs() {
		List<OAuthSPI> enabledSpis = new ArrayList<>();
		if(oauthSPIs != null) {
			for(OAuthSPI spi:oauthSPIs) {
				if(spi.isEnabled()) {
					enabledSpis.add(spi);
				}
			}
		}
		return enabledSpis;
	}
	
	public boolean isRoot() {
		return getRootProvider() != null;
	}
	
	public OAuthSPI getRootProvider() {
		OAuthSPI rootSpi = null;
		if(oauthSPIs != null) {
			for(OAuthSPI spi:oauthSPIs) {
				if(spi.isEnabled() && spi.isRootEnabled()) {
					if(adfsRootEnabled) {
						rootSpi = spi;
					}
				}
			}
		}
		return rootSpi;
	}

	public boolean isAllowUserCreation() {
		return allowUserCreation;
	}

	public void setAllowUserCreation(boolean allowUserCreation) {
		this.allowUserCreation = allowUserCreation;
		setStringProperty("allowUserCreation", allowUserCreation ? "true" : "false", true);
	}

	public boolean isLinkedInEnabled() {
		return linkedInEnabled;
	}

	public void setLinkedInEnabled(boolean linkedInEnabled) {
		this.linkedInEnabled = linkedInEnabled;
		setStringProperty("linkedInEnabled", linkedInEnabled ? "true" : "false", true);
	}

	public String getLinkedInApiKey() {
		return linkedInApiKey;
	}

	public void setLinkedInApiKey(String linkedInApiKey) {
		this.linkedInApiKey = linkedInApiKey;
		setStringProperty("linkedInApiKey", linkedInApiKey, true);
	}

	public String getLinkedInApiSecret() {
		return linkedInApiSecret;
	}

	public void setLinkedInApiSecret(String linkedInApiSecret) {
		this.linkedInApiSecret = linkedInApiSecret;
		setSecretStringProperty("linkedInApiSecret", linkedInApiSecret, true);
	}

	public String getLinkedInScopes() {
		return linkedInScopes;
	}

	public void setLinkedInScopes(String linkedInScopes) {
		this.linkedInScopes = linkedInScopes;
		setStringProperty("linkedInScopes", linkedInScopes, true);
	}

	public boolean isTwitterEnabled() {
		return twitterEnabled;
	}

	public void setTwitterEnabled(boolean twitterEnabled) {
		this.twitterEnabled = twitterEnabled;
		setStringProperty("twitterEnabled", twitterEnabled ? "true" : "false", true);
	}

	public String getTwitterApiKey() {
		return twitterApiKey;
	}

	public void setTwitterApiKey(String twitterApiKey) {
		this.twitterApiKey = twitterApiKey;
		setStringProperty("twitterApiKey", twitterApiKey, true);
	}

	public String getTwitterApiSecret() {
		return twitterApiSecret;
	}

	public void setTwitterApiSecret(String twitterApiSecret) {
		this.twitterApiSecret = twitterApiSecret;
		setSecretStringProperty("twitterApiSecret", twitterApiSecret, true);
	}

	public boolean isGoogleEnabled() {
		return googleEnabled;
	}

	public void setGoogleEnabled(boolean googleEnabled) {
		this.googleEnabled = googleEnabled;
		setStringProperty("googleEnabled", googleEnabled ? "true" : "false", true);
	}

	public String getGoogleApiKey() {
		return googleApiKey;
	}

	public void setGoogleApiKey(String googleApiKey) {
		this.googleApiKey = googleApiKey;
		setStringProperty("googleApiKey", googleApiKey, true);
	}

	public String getGoogleApiSecret() {
		return googleApiSecret;
	}

	public void setGoogleApiSecret(String googleApiSecret) {
		this.googleApiSecret = googleApiSecret;
		setSecretStringProperty("googleApiSecret", googleApiSecret, true);
	}

	public boolean isFacebookEnabled() {
		return facebookEnabled;
	}

	public void setFacebookEnabled(boolean facebookEnabled) {
		this.facebookEnabled = facebookEnabled;
		setStringProperty("facebookEnabled", facebookEnabled ? "true" : "false", true);
	}

	public String getFacebookApiKey() {
		return facebookApiKey;
	}

	public void setFacebookApiKey(String facebookApiKey) {
		this.facebookApiKey = facebookApiKey;
		setStringProperty("facebookApiKey", facebookApiKey, true);
	}

	public String getFacebookApiSecret() {
		return facebookApiSecret;
	}

	public void setFacebookApiSecret(String facebookApiSecret) {
		this.facebookApiSecret = facebookApiSecret;
		setSecretStringProperty("facebookApiSecret", facebookApiSecret, true);
	}

	public boolean isAdfsEnabled() {
		return adfsEnabled;
	}

	public void setAdfsEnabled(boolean adfsEnabled) {
		this.adfsEnabled = adfsEnabled;
		setStringProperty("adfsEnabled", adfsEnabled ? "true" : "false", true);
	}

	public boolean isAdfsRootEnabled() {
		return adfsRootEnabled;
	}

	public void setAdfsRootEnabled(boolean adfsRootEnabled) {
		this.adfsRootEnabled = adfsRootEnabled;
		setStringProperty("adfsRootEnabled", adfsRootEnabled ? "true" : "false", true);
	}

	public String getAdfsApiKey() {
		return adfsApiKey;
	}

	public void setAdfsApiKey(String adfsApiKey) {
		this.adfsApiKey = adfsApiKey;
		setStringProperty("adfsApiKey", adfsApiKey, true);
	}

	public String getAdfsOAuth2Endpoint() {
		return adfsOAuth2Endpoint;
	}

	public void setAdfsOAuth2Endpoint(String adfsOAuth2Endpoint) {
		this.adfsOAuth2Endpoint = adfsOAuth2Endpoint;
		setStringProperty("adfsOAuth2Endpoint", adfsOAuth2Endpoint, true);
	}
}
