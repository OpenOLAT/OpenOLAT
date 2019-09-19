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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthLoginModule;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.utils.OAuthEncoder;

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
	public String getAuthorizationBaseUrl() {
		OAuthLoginModule oauthModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
		String endpoint = oauthModule.getAdfsOAuth2Endpoint();
		if(!endpoint.endsWith("/")) {
			endpoint += "/";
		}
		
		String authorizationUrl = endpoint + "authorize";
		String resource = OAuthEncoder.encode(Settings.getServerContextPathURI());
		return authorizationUrl + "?resource=" + resource;
	}
	
    @Override
	public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
		return super.getAccessTokenExtractor();
	}

	@Override
    public ADFSOAuth2Service createService(String apiKey, String apiSecret, String callback, String defaultScope,
            String responseType, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        return new ADFSOAuth2Service(this, apiKey, apiSecret, callback, defaultScope, responseType, userAgent,
                httpClientConfig, httpClient);
    }
    
    private class ADFSOAuth2Service extends OAuth20Service {
    	
        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
    	
		private final ADFSApi api;
    	
        public ADFSOAuth2Service(ADFSApi api, String apiKey, String apiSecret, String callback, String defaultScope,
                String responseType, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
            super(api, apiKey, apiSecret, callback, defaultScope, responseType, userAgent, httpClientConfig, httpClient);
            this.api = api;
        }

		@Override
		public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
			signRequest(accessToken.getAccessToken(), request);
		}

		@Override
		public void signRequest(String accessToken, OAuthRequest request) {
		    request.addQuerystringParameter(OAuthConstants.ACCESS_TOKEN, accessToken);
		}

		@Override
		public OAuth2AccessToken getAccessToken(String code)
		throws InterruptedException, ExecutionException, IOException {
			OAuthRequest request = new OAuthRequest(Verb.POST, api.getAccessTokenEndpoint());
		    request.addBodyParameter(OAuthConstants.CLIENT_ID, getApiKey());
            if(StringHelper.containsNonWhitespace(getApiSecret())) {
            	request.addBodyParameter(OAuthConstants.CLIENT_SECRET, getApiSecret());
            }
            request.addBodyParameter(OAuthConstants.CODE, code);
            request.addBodyParameter(OAuthConstants.REDIRECT_URI, getCallback());
            request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
		    Response response = execute(request);
		    return api.getAccessTokenExtractor().extract(response);
		}
    }
}
