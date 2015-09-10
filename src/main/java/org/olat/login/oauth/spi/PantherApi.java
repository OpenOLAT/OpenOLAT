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
import org.scribe.utils.OAuthEncoder;

/**
 * 
 * Initial date: 10.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PantherApi extends DefaultApi20 {
	
	private static final String AUTHORIZE_URL = "https://author.hamilton-medical.com/.oauth/auth?client_id=%s&redirect_uri=%s&response_type=code";

	@Override
	public String getAccessTokenEndpoint() {
		return "https://author.hamilton-medical.com/.oauth/token";
	}

	@Override
	public String getAuthorizationUrl(OAuthConfig config) {
		return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
	}

	@Override
	public OAuthService createService(OAuthConfig config) {
		return new PantherOAuth2Service(this, config);
	}

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.POST;
	}

	@Override
	public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
	}
	
    private final static class PantherOAuth2Service extends OAuth20ServiceImpl {

        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
        private DefaultApi20 api;
        private OAuthConfig config;

        public PantherOAuth2Service(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }
        
        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
            switch (api.getAccessTokenVerb()) {
	            case POST:
	                request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
	                request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
	                request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
	                request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
	                request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
	                break;
	            case GET:
	            default:
	                request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
	                request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
	                request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
	                request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
	                if(config.hasScope()) request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
            }
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }
    }
}
