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
package org.olat.modules.gotomeeting.oauth;

import org.olat.core.util.StringHelper;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;

/**
 * 
 * Initial date: 14 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToApi extends DefaultApi20 {
	
	public static final String GETGO_CALLBACK = "/getgocallback";
	private static final String AUTHORIZE_URL = "https://api.getgo.com/oauth/v2/authorize?response_type=code&client_id=%s";

	@Override
	public String getAccessTokenEndpoint() {
		return "https://api.getgo.com/oauth/v2/token";
	}

	@Override
	public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(AUTHORIZE_URL, config.getApiKey());
	}
	
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
    
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
      return new JsonTokenExtractor();
    }
    
    @Override
    public OAuthService createService(OAuthConfig config) {
        return new GoToOAuth2Service(this, config);
    }
    
    private class GoToOAuth2Service extends OAuth20ServiceImpl {

        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
        private GoToApi api;
        private OAuthConfig config;

        public GoToOAuth2Service(GoToApi api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }
        
        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
        	OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
        	// header
        	request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        	request.addHeader("Accept", "application/json");
        	String authVal = config.getApiKey() + ":" +config.getApiSecret();
        	request.addHeader("Authorization", "Basic " + StringHelper.encodeBase64(authVal));
        	// body
            request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
            request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
            request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }
    }
}
