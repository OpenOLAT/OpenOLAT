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
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import org.olat.core.util.StringHelper;

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

/**
 * 
 * Initial date: 10.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PantherApi extends DefaultApi20 {
	
	private static final String AUTHORIZE_URL = "/auth";
	
	private final PantherProvider provider;
	
	public PantherApi(PantherProvider provider) {
		this.provider = provider;
	}

	@Override
	public String getAccessTokenEndpoint() {
		return provider.getEndpoint() + "/token";
	}

	@Override
	public String getAuthorizationBaseUrl() {
		return provider.getEndpoint() + AUTHORIZE_URL;
	}

	@Override
	public PantherOAuth2Service createService(String apiKey, String apiSecret, String callback, String defaultScope,
            String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
		return new PantherOAuth2Service(this, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
	}

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.POST;
	}
	
	@Override
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return PantherOAuth2AccessTokenJsonExtractor.instance();
	}
	
	
	
    private final class PantherOAuth2Service extends OAuth20Service {

        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
        private DefaultApi20 api;

        public PantherOAuth2Service(DefaultApi20 api, String apiKey, String apiSecret, String callback, String defaultScope,
                String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
            super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
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
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
            switch (api.getAccessTokenVerb()) {
	            case POST:
	                request.addBodyParameter(OAuthConstants.CLIENT_ID, getApiKey());
	                request.addBodyParameter(OAuthConstants.CLIENT_SECRET, getApiSecret());
	                request.addBodyParameter(OAuthConstants.CODE, code);
	                request.addBodyParameter(OAuthConstants.REDIRECT_URI, getCallback());
	                request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
	                break;
	            case GET:
	            default:
	                request.addQuerystringParameter(OAuthConstants.CLIENT_ID, getApiKey());
	                request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, getApiSecret());
	                request.addQuerystringParameter(OAuthConstants.CODE, code);
	                request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, getCallback());
	                if(StringHelper.containsNonWhitespace(getDefaultScope())) {
	                	request.addQuerystringParameter(OAuthConstants.SCOPE, getDefaultScope());
	                }
            }
            Response response = execute(request);
            return api.getAccessTokenExtractor().extract(response);
        }
    }
}
