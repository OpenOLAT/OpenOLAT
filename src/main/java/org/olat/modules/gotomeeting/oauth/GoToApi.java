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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import org.olat.core.util.StringHelper;

import com.github.scribejava.core.builder.api.DefaultApi20;
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
 * Initial date: 14 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToApi extends DefaultApi20 {

	public static final String GETGO_CALLBACK = "/getgocallback";
	private static final String AUTHORIZE_URL = "https://api.getgo.com/oauth/v2/authorize";

	@Override
	public String getAccessTokenEndpoint() {
		return "https://api.getgo.com/oauth/v2/token";
	}

	@Override
	public String getAuthorizationBaseUrl() {
        return AUTHORIZE_URL;
	}
	
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
    
    @Override
    public GoToOAuth2Service createService(String apiKey, String apiSecret, String callback, String defaultScope,
            String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        return new GoToOAuth2Service(this, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
    }
    
    private class GoToOAuth2Service extends OAuth20Service {

        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
        private GoToApi api;

        public GoToOAuth2Service(GoToApi api, String apiKey, String apiSecret, String callback, String defaultScope,
                String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
            super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
            this.api = api;
        }
        
        @Override
        public OAuth2AccessToken getAccessToken(String code)
        throws InterruptedException, ExecutionException, IOException {
        	OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
        	// header
        	request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        	request.addHeader("Accept", "application/json");
        	String authVal = getApiKey() + ":" + getApiSecret();
        	request.addHeader("Authorization", "Basic " + StringHelper.encodeBase64(authVal));
        	// body
            request.addBodyParameter(OAuthConstants.CODE, code);
            request.addBodyParameter(OAuthConstants.REDIRECT_URI, getCallback());
            request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
            Response response = execute(request);
            return api.getAccessTokenExtractor().extract(response);
        }
    }
}
