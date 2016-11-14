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

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

/**
 * 
 * Initial date: 6 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenIdConnectFullConfigurableApi extends DefaultApi20 {
	
	private static final OLog log = Tracing.createLoggerFor(OpenIdConnectFullConfigurableApi.class);
	
	private final OpenIdConnectFullConfigurableProvider provider;
	
	public OpenIdConnectFullConfigurableApi(OpenIdConnectFullConfigurableProvider provider) {
		this.provider = provider;
	}

    @Override
    public String getAccessTokenEndpoint() {
        return null;
    }
    
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return null;
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
    	String url = provider.getEndPoint();
    	StringBuilder authorizeUrl = new StringBuilder();
    	authorizeUrl
    		.append(url).append("?")
    		.append("response_type=").append(OAuthEncoder.encode("id_token token"))
    		.append("&client_id=").append(config.getApiKey())
    		.append("&redirect_uri=").append(OAuthEncoder.encode(config.getCallback()))
    	    .append("&scope=").append(OAuthEncoder.encode("openid email"))
    		.append("&state=").append(UUID.randomUUID().toString())
    		.append("&nonce=").append(UUID.randomUUID().toString());		
    	return authorizeUrl.toString();
    }
    
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
    
    @Override
    public OAuthService createService(OAuthConfig config) {
        return new OpenIdConnectFullConfigurableService(this, config);
    }
    
    public class OpenIdConnectFullConfigurableService extends OAuth20ServiceImpl {

        public OpenIdConnectFullConfigurableService(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
        }
        
        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
        	try {
				OpenIDVerifier oVerifier = (OpenIDVerifier)verifier;
				String idToken = oVerifier.getIdToken();
				JSONObject idJson = JSONWebToken.parse(idToken).getJsonPayload();
				JSONObject accessJson = JSONWebToken.parse(oVerifier.getAccessToken()).getJsonPayload();
				
				boolean allOk = true;
				if(!provider.getIssuer().equals(idJson.get("iss"))) {
					allOk &= false;
					log.error("iss don't match issuer");
				}
				if(!provider.getIssuer().equals(accessJson.get("iss"))) {
					allOk &= false;
					log.error("iss don't match issuer");
				}
				
				if(!provider.getAppKey().equals(idJson.get("aud"))) {
					allOk &= false;
					log.error("aud don't match application key");
				}
				if(!oVerifier.getState().equals(oVerifier.getSessionState())) {
					allOk &= false;
					log.error("state doesn't match session state");
				}
				
				if(!oVerifier.getSessionNonce().equals(idJson.get("nonce"))) {
					allOk &= false;
					log.error("session nonce don't match verifier nonce");
				}
				
				return allOk ? new Token(idToken, oVerifier.getState()) : null;
			} catch (JSONException e) {
				log.error("", e);
				return null;
			}
        }
    }
}
