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
package org.olat.ims.lti13;

import java.io.OutputStream;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.logging.Tracing;
import org.olat.login.oauth.spi.JSONWebToken;
import org.olat.login.oauth.spi.OpenIDVerifier;

import com.github.scribejava.apis.openid.OpenIdJsonTokenExtractor;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * This is not a service, every exchange has its own instance of the API.
 * 
 * Initial date: 23 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OIDCApi extends DefaultApi20 {
	
	private static final Logger log = Tracing.createLoggerFor(OIDCApi.class);

	private LTI13SharedTool sharedTool;
	private LTI13SharedToolDeployment deployment;
	
	public OIDCApi() {
		//
	}
	
	public OIDCApi(LTI13SharedToolDeployment deployment, LTI13SharedTool sharedTool) {
		this.sharedTool = sharedTool;
		this.deployment = deployment;
	}
	
	public LTI13SharedTool getTool() {
		return sharedTool;
	}
	
	public LTI13SharedToolDeployment getDeployment() {
		return deployment;
	}
	
	public String getDeploymentId() {
		return deployment.getDeploymentId();
	}
	
	public String getJwksSetUri() {
		return sharedTool.getJwkSetUri();
	}

	@Override
	public String getAccessTokenEndpoint() {
		return sharedTool.getTokenUri();
	}

	@Override
	public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
		return OpenIdJsonTokenExtractor.instance();
	}

	@Override
	protected String getAuthorizationBaseUrl() {
	   	StringBuilder authorizeUrl = new StringBuilder();
	   	String authorizationUri = sharedTool.getAuthorizationUri();
    	authorizeUrl
    		.append(authorizationUri).append("?")
    		.append("nonce=").append(UUID.randomUUID().toString());		
    	return authorizeUrl.toString();
	}
	
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
    
    @Override
    public OAuth20Service createService(String apiKey, String apiSecret, String callback, String defaultScope,
            String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        return new OIDCService(this, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
    }
    
    public class OIDCService extends OAuth20Service {

        public OIDCService(DefaultApi20 api, String apiKey, String apiSecret, String callback, String defaultScope,
                String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
            super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
        }
        
        @Override
        public OIDCApi getApi() {
        	return (OIDCApi)super.getApi();
        }
        
        public OAuth2AccessToken getAccessToken(OpenIDVerifier oVerifier) {
        	try {
				String idToken = oVerifier.getIdToken();
				JSONObject idJson = JSONWebToken.parse(idToken).getJsonPayload();
				JSONObject accessJson = JSONWebToken.parse(oVerifier.getAccessToken()).getJsonPayload();
				
				boolean allOk = true;
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
				
				if(accessJson == null) {
					allOk &= false;
					log.info("session hasn't a paylod");
				}
				
				return allOk ? new OAuth2AccessToken(idToken, oVerifier.getState()) : null;
			} catch (JSONException e) {
				log.error("", e);
				return null;
			}
        }
    }
}
