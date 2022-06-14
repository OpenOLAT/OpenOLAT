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
import java.util.Set;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.helpers.Settings;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.login.oauth.spi.OpenIdConnectFullConfigurableProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OAuthLoginModule extends AbstractSpringModule {
	
	private static final String OPEN_ID_IF_START_MARKER = "openIdConnectIF.";
	private static final String OPEN_ID_IF_END_MARKER = ".Enabled";
	
	private static final String KEYCLOAK_ENABLED = "keycloakEnabled";
	private static final String KEYCLOAK_ROOT_ENABLED = "keycloakRootEnabled";
	private static final String KEYCLOAK_ENDPOINT = "keycloakEndpoint";
	private static final String KEYCLOAK_REALM = "keycloakRealm";
	private static final String KEYCLOAK_CLIENT_ID = "keycloakClientId";
	private static final String KEYCLOAK_CLIENT_SECRET = "keycloakClientSecret";
	
	private static final String SKIP_DISCLAIMER_DIALOG = "skip.disclaimer.dialog";
	private static final String SKIP_REGISTRATION_DIALOG = "skip.registration.dialog";
	
	private boolean allowUserCreation;
	private boolean skipDisclaimerDialog;
	private boolean skipRegistrationDialog;
	
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
	private String adfsApiSecret;
	private String adfsOAuth2Endpoint;
	
	private boolean azureAdfsEnabled;
	private boolean azureAdfsRootEnabled;
	private String azureAdfsApiKey;
	private String azureAdfsApiSecret;
	private String azureAdfsTenant;
	
	private boolean tequilaEnabled;
	private String tequilaApiKey;
	private String tequilaApiSecret;
	private String tequilaOAuth2Endpoint;
	
	private boolean openIdConnectIFEnabled;
	private boolean openIdConnectIFRootEnabled;
	private String openIdConnectIFApiKey;
	private String openIdConnectIFApiSecret;
	private String openIdConnectIFIssuer;
	private String openIdConnectIFAuthorizationEndPoint;
	
	@Value("${oauth.keycloak.enabled:false}")
	private boolean keycloakEnabled;
	@Value("${oauth.keycloak.root:false}")
	private boolean keycloakRootEnabled;
	@Value("${oauth.keycloak.client.id}")
	private String keycloakClientId;
	@Value("${oauth.keycloak.client.secret}")
	private String keycloakClientSecret;
	@Value("${oauth.keycloak.endpoint}")
	private String keycloakEndpoint;
	@Value("${oauth.keycloak.realm}")
	private String keycloakRealm;
	
	@Autowired
	private List<OAuthSPI> oauthSPIs;
	
	private List<OAuthSPI> configurableOauthSPIs;
	
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
		String skipDisclaimerDialogObj = getStringPropertyValue(SKIP_DISCLAIMER_DIALOG, true);
		skipDisclaimerDialog = "true".equals(skipDisclaimerDialogObj);
		String skipRegistrationDialogObj = getStringPropertyValue(SKIP_REGISTRATION_DIALOG, true);
		skipRegistrationDialog = "true".equals(skipRegistrationDialogObj);
		
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
		adfsApiSecret = getStringPropertyValue("adfsApiSecret", false);
		adfsOAuth2Endpoint = getStringPropertyValue("adfsOAuth2Endpoint", false);
		
		//Azure ADFS
		String azureAdfsEnabledObj = getStringPropertyValue("azureAdfsEnabled", true);
		azureAdfsEnabled = "true".equals(azureAdfsEnabledObj);
		String azureAdfsRootEnabledObj = getStringPropertyValue("azureAdfsRootEnabled", true);
		azureAdfsRootEnabled = "true".equals(azureAdfsRootEnabledObj);
		azureAdfsApiKey = getStringPropertyValue("azureAdfsApiKey", false);
		azureAdfsApiSecret = getStringPropertyValue("azureAdfsApiSecret", false);
		azureAdfsTenant = getStringPropertyValue("azureAdfsTenant", false);
		
		//tequila
		String tequilaEnabledObj = getStringPropertyValue("tequilaEnabled", true);
		tequilaEnabled = "true".equals(tequilaEnabledObj);
		tequilaApiKey = getStringPropertyValue("tequilaApiKey", false);
		tequilaApiSecret = getStringPropertyValue("tequilaApiSecret", false);
		tequilaOAuth2Endpoint = getStringPropertyValue("tequilaOAuth2Endpoint", false);
		
		// Keycloak
		String keycloakEnabledObj = getStringPropertyValue(KEYCLOAK_ENABLED, Boolean.toString(keycloakEnabled));
		keycloakEnabled = "true".equals(keycloakEnabledObj);
		String keycloakRootEnabledObj = getStringPropertyValue(KEYCLOAK_ROOT_ENABLED, Boolean.toString(keycloakRootEnabled));
		keycloakRootEnabled = "true".equals(keycloakRootEnabledObj);
		keycloakEndpoint = getStringPropertyValue(KEYCLOAK_ENDPOINT, keycloakEndpoint);
		keycloakRealm = getStringPropertyValue(KEYCLOAK_REALM, keycloakRealm);
		keycloakClientId = getStringPropertyValue(KEYCLOAK_CLIENT_ID, keycloakClientId);
		keycloakClientSecret = getStringPropertyValue(KEYCLOAK_CLIENT_SECRET, keycloakClientSecret);
		
		String openIdConnectIFEnabledObj = getStringPropertyValue("openIdConnectIFEnabled", true);
		openIdConnectIFEnabled = "true".equals(openIdConnectIFEnabledObj);
		String openIdConnectIFRootEnabledObj = getStringPropertyValue("openIdConnectIFRootEnabled", true);
		openIdConnectIFRootEnabled = "true".equals(openIdConnectIFRootEnabledObj);
		openIdConnectIFApiKey = getStringPropertyValue("openIdConnectIFApiKey", false);
		openIdConnectIFApiSecret = getStringPropertyValue("openIdConnectIFApiSecret", false);
		openIdConnectIFIssuer = getStringPropertyValue("openIdConnectIFIssuer", false);
		openIdConnectIFAuthorizationEndPoint = getStringPropertyValue("openIdConnectIFAuthorizationEndPoint", false);

		Set<Object> allPropertyKeys = getPropertyKeys();
		List<OAuthSPI> otherOAuthSPies = new ArrayList<>();
		for(Object propertyKey:allPropertyKeys) {
			if(propertyKey instanceof String) {
				String key = (String)propertyKey;
				if(key.startsWith(OPEN_ID_IF_START_MARKER) && key.endsWith(OPEN_ID_IF_END_MARKER)) {
					 OAuthSPI spi = getAdditionalOpenIDConnectIF(key);
					 if(spi != null) {
						 otherOAuthSPies.add(spi);
					 }
				}
			}
		}
		configurableOauthSPIs = otherOAuthSPies;
	}
	
	private OAuthSPI getAdditionalOpenIDConnectIF(String enableKey) {
		String providerName = enableKey.substring(OPEN_ID_IF_START_MARKER.length(), enableKey.length() - OPEN_ID_IF_END_MARKER.length());

		String rootEnabledObj = getStringPropertyValue("openIdConnectIF." + providerName + ".RootEnabled", true);
		boolean rootEnabled = "true".equals(rootEnabledObj);
		String apiKey = getStringPropertyValue("openIdConnectIF." + providerName + ".ApiKey", true);
		String apiSecret = getStringPropertyValue("openIdConnectIF." + providerName + ".ApiSecret", true);
		String issuer = getStringPropertyValue("openIdConnectIF." + providerName + ".Issuer", true);
		String endPoint = getStringPropertyValue("openIdConnectIF." + providerName + ".AuthorizationEndPoint", true);
		String displayName = getStringPropertyValue("openIdConnectIF." + providerName + ".DisplayName", true);
		
		return new OpenIdConnectFullConfigurableProvider(providerName, displayName, providerName,
				apiKey, apiSecret, issuer, endPoint, rootEnabled, this);
	}
	
	public List<OAuthSPI> getAllSPIs() {
		List<OAuthSPI> spies = new ArrayList<>(oauthSPIs);
		if(configurableOauthSPIs != null) {
			spies.addAll(configurableOauthSPIs);
		}
		return spies;
	}
	
	public List<OAuthSPI> getAllConfigurableSPIs() {
		List<OAuthSPI> spies = new ArrayList<>();
		if(configurableOauthSPIs != null) {
			spies.addAll(configurableOauthSPIs);
		}
		return spies;
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
		if(configurableOauthSPIs != null) {
			for(OAuthSPI spi:configurableOauthSPIs) {
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
					rootSpi = spi;
				}
			}
		}
		if(rootSpi == null && configurableOauthSPIs != null) {
			for(OAuthSPI spi:configurableOauthSPIs) {
				if(spi.isEnabled() && spi.isRootEnabled()) {
					rootSpi = spi;
				}
			}
		}
		return rootSpi;
	}
	
	public OAuthSPI getProvider(String providerName) {
		OAuthSPI spi = null;
		if(oauthSPIs != null) {
			for(OAuthSPI oauthSpi:oauthSPIs) {
				if(providerName.equals(oauthSpi.getProviderName())) {
					spi = oauthSpi;
				}
			}
		}
		if(spi == null && configurableOauthSPIs != null) {
			for(OAuthSPI oauthSpi:configurableOauthSPIs) {
				if(providerName.equals(oauthSpi.getProviderName())) {
					spi = oauthSpi;
				}
			}
		}
		return spi;
	}
	
	public String getCallbackUrl() {
		return Settings.getServerContextPathURI() + OAuthConstants.CALLBACK_PATH;
	}

	public boolean isAllowUserCreation() {
		return allowUserCreation;
	}

	public void setAllowUserCreation(boolean allowUserCreation) {
		this.allowUserCreation = allowUserCreation;
		setStringProperty("allowUserCreation", allowUserCreation ? "true" : "false", true);
	}
	
	public boolean isSkipDisclaimerDialog() {
		return skipDisclaimerDialog;
	}
	
	public void setSkipDisclaimerDialog(boolean skipDisclaimerDialog) {
		this.skipDisclaimerDialog = skipDisclaimerDialog;
		setStringProperty(SKIP_DISCLAIMER_DIALOG, skipDisclaimerDialog ? "true" : "false", true);
	}
	
	public boolean isSkipRegistrationDialog() {
		return skipRegistrationDialog;
	}
	
	public void setSkipRegistrationDialog(boolean skipRegistrationDialog) {
		this.skipRegistrationDialog = skipDisclaimerDialog;
		setStringProperty(SKIP_REGISTRATION_DIALOG, skipRegistrationDialog ? "true" : "false", true);
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
	
	public String getAdfsApiSecret() {
		return adfsApiSecret;
	}

	public void setAdfsApiSecret(String adfsApiSecret) {
		this.adfsApiSecret = adfsApiSecret;
		setStringProperty("adfsApiSecret", adfsApiSecret, true);
	}

	public String getAdfsOAuth2Endpoint() {
		return adfsOAuth2Endpoint;
	}

	public void setAdfsOAuth2Endpoint(String adfsOAuth2Endpoint) {
		this.adfsOAuth2Endpoint = adfsOAuth2Endpoint;
		setStringProperty("adfsOAuth2Endpoint", adfsOAuth2Endpoint, true);
	}
	
	public boolean isAzureAdfsEnabled() {
		return azureAdfsEnabled;
	}

	public void setAzureAdfsEnabled(boolean azureAdfsEnabled) {
		this.azureAdfsEnabled = azureAdfsEnabled;
		setStringProperty("azureAdfsEnabled", azureAdfsEnabled ? "true" : "false", true);
	}

	public boolean isAzureAdfsRootEnabled() {
		return azureAdfsRootEnabled;
	}

	public void setAzureAdfsRootEnabled(boolean azureAdfsRootEnabled) {
		this.azureAdfsRootEnabled = azureAdfsRootEnabled;
		setStringProperty("azureAdfsRootEnabled", azureAdfsRootEnabled ? "true" : "false", true);
	}

	public String getAzureAdfsApiKey() {
		return azureAdfsApiKey;
	}

	public void setAzureAdfsApiKey(String azureAdfsApiKey) {
		this.azureAdfsApiKey = azureAdfsApiKey;
		setStringProperty("azureAdfsApiKey", azureAdfsApiKey, true);
	}
	
	public String getAzureAdfsApiSecret() {
		return azureAdfsApiSecret;
	}

	public void setAzureAdfsApiSecret(String azureAdfsApiSecret) {
		this.azureAdfsApiSecret = azureAdfsApiSecret;
		setStringProperty("azureAdfsApiSecret", azureAdfsApiSecret, true);
	}

	public String getAzureAdfsTenant() {
		return azureAdfsTenant;
	}

	public void setAzureAdfsTenant(String azureAdfsTenant) {
		this.azureAdfsTenant = azureAdfsTenant;
		setStringProperty("azureAdfsTenant", azureAdfsTenant, true);
	}

	public boolean isTequilaEnabled() {
		return tequilaEnabled;
	}

	public void setTequilaEnabled(boolean tequilaEnabled) {
		this.tequilaEnabled = tequilaEnabled;
		setStringProperty("tequilaEnabled", tequilaEnabled ? "true" : "false", true);
	}

	public String getTequilaApiKey() {
		return tequilaApiKey;
	}

	public void setTequilaApiKey(String tequilaApiKey) {
		this.tequilaApiKey = tequilaApiKey;
		setStringProperty("tequilaApiKey", tequilaApiKey, true);
	}

	public String getTequilaApiSecret() {
		return tequilaApiSecret;
	}

	public void setTequilaApiSecret(String tequilaApiSecret) {
		this.tequilaApiSecret = tequilaApiSecret;
		setStringProperty("tequilaApiSecret", tequilaApiSecret, true);
	}

	public String getTequilaOAuth2Endpoint() {
		return tequilaOAuth2Endpoint;
	}

	public void setTequilaOAuth2Endpoint(String tequilaOAuth2Endpoint) {
		this.tequilaOAuth2Endpoint = tequilaOAuth2Endpoint;
		setStringProperty("tequilaOAuth2Endpoint", tequilaOAuth2Endpoint, true);
	}
	
	public boolean isKeycloakEnabled() {
		return keycloakEnabled;
	}
	
	public void setKeycloakEnabled(boolean enabled) {
		keycloakEnabled = enabled;
		setStringProperty(KEYCLOAK_ENABLED, enabled ? "true" : "false", true);
	}
	
	public boolean isKeycloakRootEnabled() {
		return keycloakRootEnabled;
	}
	
	public void setKeycloakRootEnabled(boolean enabled) {
		keycloakRootEnabled = enabled;
		setStringProperty(KEYCLOAK_ROOT_ENABLED, enabled ? "true" : "false", true);
	}
	
	public String getKeycloakClientId() {
		return keycloakClientId;
	}
	
	public void setKeycloakClientId(String clientId) {
		keycloakClientId = clientId;
		setStringProperty(KEYCLOAK_CLIENT_ID, clientId, true);
	}
	
	public String getKeycloakClientSecret() {
		return keycloakClientSecret;
	}
	
	public void setKeycloakClientSecret(String clientSecret) {
		keycloakClientSecret = clientSecret;
		setStringProperty(KEYCLOAK_CLIENT_SECRET, clientSecret, true);
	}
	
	public String getKeycloakEndpoint() {
		return keycloakEndpoint;
	}
	
	public void setKeycloakEndpoint(String url) {
		keycloakEndpoint = url;
		setStringProperty(KEYCLOAK_ENDPOINT, url, true);
	}
	
	public String getKeycloakRealm() {
		return keycloakRealm;
	}
	
	public void setKeycloakRealm(String realm) {
		keycloakRealm = realm;
		setStringProperty(KEYCLOAK_REALM, realm, true);
	}

	public boolean isOpenIdConnectIFEnabled() {
		return openIdConnectIFEnabled;
	}

	public void setOpenIdConnectIFEnabled(boolean openIdConnectIFEnabled) {
		this.openIdConnectIFEnabled = openIdConnectIFEnabled;
		setStringProperty("openIdConnectIFEnabled", openIdConnectIFEnabled ? "true" : "false", true);
	}

	public boolean isOpenIdConnectIFRootEnabled() {
		return openIdConnectIFRootEnabled;
	}

	public void setOpenIdConnectIFRootEnabled(boolean openIdConnectIFRootEnabled) {
		this.openIdConnectIFRootEnabled = openIdConnectIFRootEnabled;
		setStringProperty("openIdConnectIFRootEnabled", openIdConnectIFRootEnabled ? "true" : "false", true);
	}

	public String getOpenIdConnectIFApiKey() {
		return openIdConnectIFApiKey;
	}

	public void setOpenIdConnectIFApiKey(String openIdConnectIFApiKey) {
		this.openIdConnectIFApiKey = openIdConnectIFApiKey;
		setStringProperty("openIdConnectIFApiKey", openIdConnectIFApiKey, true);
	}

	public String getOpenIdConnectIFApiSecret() {
		return openIdConnectIFApiSecret;
	}

	public void setOpenIdConnectIFApiSecret(String openIdConnectIFApiSecret) {
		this.openIdConnectIFApiSecret = openIdConnectIFApiSecret;
		setStringProperty("openIdConnectIFApiSecret", openIdConnectIFApiSecret, true);
	}

	public String getOpenIdConnectIFIssuer() {
		return openIdConnectIFIssuer;
	}

	public void setOpenIdConnectIFIssuer(String openIdConnectIFIssuer) {
		this.openIdConnectIFIssuer = openIdConnectIFIssuer;
		setStringProperty("openIdConnectIFIssuer", openIdConnectIFIssuer, true);
	}

	public String getOpenIdConnectIFAuthorizationEndPoint() {
		return openIdConnectIFAuthorizationEndPoint;
	}

	public void setOpenIdConnectIFAuthorizationEndPoint(String openIdConnectIFAuthorizationEndPoint) {
		this.openIdConnectIFAuthorizationEndPoint = openIdConnectIFAuthorizationEndPoint;
		setStringProperty("openIdConnectIFAuthorizationEndPoint", openIdConnectIFAuthorizationEndPoint, true);
	}
	
	public void setAdditionalOpenIDConnectIF(String providerName, String displayName, boolean rootEnabled, String issuer, String endPoint, String apiKey, String apiSecret) {
		setStringProperty("openIdConnectIF." + providerName + ".Enabled", "true", true);
		setStringProperty("openIdConnectIF." + providerName + ".RootEnabled", rootEnabled ? "true" : "false", true);
		setStringProperty("openIdConnectIF." + providerName + ".ApiKey", apiKey, true);
		setStringProperty("openIdConnectIF." + providerName + ".ApiSecret", apiSecret, true);
		setStringProperty("openIdConnectIF." + providerName + ".Issuer", issuer, true);
		setStringProperty("openIdConnectIF." + providerName + ".DisplayName", displayName, true);
		setStringProperty("openIdConnectIF." + providerName + ".AuthorizationEndPoint", endPoint, true);
		updateProperties();
	}
	
	public void removeAdditionalOpenIDConnectIF(String providerName) {
		removeProperty("openIdConnectIF." + providerName + ".Enabled", true);
		removeProperty("openIdConnectIF." + providerName + ".RootEnabled", true);
		removeProperty("openIdConnectIF." + providerName + ".ApiKey", true);
		removeProperty("openIdConnectIF." + providerName + ".ApiSecret", true);
		removeProperty("openIdConnectIF." + providerName + ".Issuer", true);
		removeProperty("openIdConnectIF." + providerName + ".DisplayName", true);
		removeProperty("openIdConnectIF." + providerName + ".AuthorizationEndPoint", true);
		updateProperties();
	}
}
