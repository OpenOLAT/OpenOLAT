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
package org.olat.login.oauth.spi;

import java.io.OutputStream;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.login.oauth.OAuthLoginModule;

import com.github.scribejava.apis.openid.OpenIdJsonTokenExtractor;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * 
 * Initial date: 15.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenIdConnectApi extends DefaultApi20 {
	
	private static final Logger log = Tracing.createLoggerFor(OpenIdConnectApi.class);
	
	private final OpenIdConnectProvider provider;
	
	public OpenIdConnectApi(OpenIdConnectProvider provider) {
		this.provider = provider;
	}

    @Override
    public String getAccessTokenEndpoint() {
        return null;
    }
    
    @Override
	public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
		return OpenIdJsonTokenExtractor.instance();
	}

    @Override
    public String getAuthorizationBaseUrl() {
    	String url = provider.getEndPoint();
    	StringBuilder authorizeUrl = new StringBuilder();
    	authorizeUrl
    		.append(url).append("?")
    		.append("&nonce=").append(UUID.randomUUID().toString());		
    	return authorizeUrl.toString();
    }
    
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
    
    @Override
    public OAuth20Service createService(String apiKey, String apiSecret, String callback, String defaultScope,
            String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        return new OpenIdConnectService(this, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
    }
    
    public class OpenIdConnectService extends OAuth20Service {

        public OpenIdConnectService(DefaultApi20 api, String apiKey, String apiSecret, String callback, String defaultScope,
                String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
            super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
        }
        
        public OAuth2AccessToken getAccessToken(OpenIDVerifier oVerifier) {
        	OAuthLoginModule oauthModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
        	try {
				String idToken = oVerifier.getIdToken();
				JSONObject idJson = JSONWebToken.parse(idToken).getJsonPayload();
				JSONObject accessJson = JSONWebToken.parse(oVerifier.getAccessToken()).getJsonPayload();
				
				boolean allOk = true;
				if(!oauthModule.getOpenIdConnectIFIssuer().equals(idJson.get("iss"))
						|| !oauthModule.getOpenIdConnectIFIssuer().equals(accessJson.get("iss"))) {
					allOk &= false;
					log.info("iss don't match issuer");
				}
				
				if(!getApiKey().equals(idJson.get("aud"))) {
					allOk &= false;
					log.info("aud don't match application key");
				}
				if(!oVerifier.getState().equals(oVerifier.getSessionState())) {
					allOk &= false;
					log.info("state doesn't match session state");
				}
				
				if(!oVerifier.getSessionNonce().equals(idJson.get("nonce"))) {
					allOk &= false;
					log.info("session nonce don't match verifier nonce");
				}
				
				return allOk ? new OAuth2AccessToken(idToken, oVerifier.getState()) : null;
			} catch (JSONException e) {
				log.error("", e);
				return null;
			}
        }
    }
}
