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

import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.login.oauth.OAuthLoginModule;
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
 * Initial date: 06.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ADFSApi extends DefaultApi20 {

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.POST;
	}
	
	@Override
	public String getAccessTokenEndpoint() {
		OAuthLoginModule oauthModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
		String endpoint = oauthModule.getAdfsOAuth2Endpoint();
		if(!endpoint.endsWith("/")) {
			endpoint += "/";
		}
		endpoint += "token";
		return endpoint;
	}

	//https://adfs.hamilton.ch/adfs/oauth2/authorize?response_type=code&client_id=25e53ef4-659e-11e4-b116-123b93f75cba&redirect_uri=https://kivik.frentix.com/olat/oauthcallback&resource=https://kivik.frentix.com/olat

	@Override
	public String getAuthorizationUrl(OAuthConfig config) {
		OAuthLoginModule oauthModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
		String endpoint = oauthModule.getAdfsOAuth2Endpoint();
		if(!endpoint.endsWith("/")) {
			endpoint += "/";
		}
		String authorizationUrl = endpoint + "authorize?response_type=code&client_id=%s&redirect_uri=%s&resource=%s";
		String resource = Settings.getServerContextPathURI();
		String url = String.format(authorizationUrl, config.getApiKey(),
				OAuthEncoder.encode(config.getCallback()),
				OAuthEncoder.encode(resource)
			);
		return url;
	}

    @Override
	public AccessTokenExtractor getAccessTokenExtractor() {
		return new JsonTokenExtractor();
	}

	@Override
    public OAuthService createService(OAuthConfig config) {
        return new ADFSOAuth2Service(this, config);
    }
    
    private class ADFSOAuth2Service extends OAuth20ServiceImpl {
    	
        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
    	
		private final ADFSApi api;
		private OAuthConfig config;
    	
        public ADFSOAuth2Service(ADFSApi api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }

		@Override
		public Token getAccessToken(Token requestToken, Verifier verifier) {
			OAuthRequest request = new OAuthRequest(Verb.POST, api.getAccessTokenEndpoint());
		    request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
            request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
            request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
            request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
            request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
		    Response response = request.send();
		    return api.getAccessTokenExtractor().extract(response.getBody());
		}
    }
}
