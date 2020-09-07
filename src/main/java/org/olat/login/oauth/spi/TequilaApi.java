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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

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
 * Initial date: 30 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TequilaApi extends DefaultApi20 {
	
	private static final Logger log = Tracing.createLoggerFor(TequilaApi.class);
	
	private final String endPoint;
	
	protected TequilaApi(String endPoint) {
		this.endPoint = endPoint;
	}
	
	@Override
	public String getAccessTokenEndpoint() {
		String tokenEndPoint = endPoint;
		if(!tokenEndPoint.endsWith("/")) {
			tokenEndPoint += "/";
		}
		tokenEndPoint += "token";
		return tokenEndPoint;
	}

	@Override
	public String getAuthorizationBaseUrl() {
		String baseUrl = endPoint;
		if(!baseUrl.endsWith("/")) {
			baseUrl += "/";
		}
		return baseUrl + "auth";
	}

    @Override
	public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
		return new TequilaBearerExtractor();
	}
    
	@Override
    public TequilaAuth2Service createService(String apiKey, String apiSecret, String callback, String defaultScope,
            String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        return new TequilaAuth2Service(this, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
    }
	
	public static class TequilaBearerExtractor implements TokenExtractor<OAuth2AccessToken> {
		
		private Pattern accessTokenPattern = Pattern.compile("\"access_token\":\\s*\"(\\S*?)\"");

		@Override
		public OAuth2AccessToken extract(Response response) {
			try {
				String bodyResponse = response.getBody();
				Matcher matcher = accessTokenPattern.matcher(bodyResponse);
				if(matcher.find()) {
					return new OAuth2AccessToken(matcher.group(1), bodyResponse);
				}
				if(bodyResponse.contains("Bearer")) {
					String t = "\"access_token\": \"Bearer ";
					int index = bodyResponse.indexOf(t);
					if(index >= 0) {
						int endIndex = bodyResponse.indexOf('"', index + t.length());
						String token = bodyResponse.substring(index + t.length(), endIndex);
						String decodedToken = OAuthEncoder.decode(token);
						return new OAuth2AccessToken(decodedToken, bodyResponse);
					}
				}
			} catch (IOException e) {
				log.error("", e);
			}
			return null;
		}
	}
	
    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is not supported... all hope is lost.");
        }
    }
    
    private class TequilaAuth2Service extends OAuth20Service {
    	
        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
    	
		private final TequilaApi api;
    	
        public TequilaAuth2Service(TequilaApi api, String apiKey, String apiSecret, String callback, String defaultScope,
        	            String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
            super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
            this.api = api;
        }

		@Override
		public OAuth2AccessToken getAccessToken(String code) throws IOException, InterruptedException, ExecutionException {
			OAuthRequest request = new OAuthRequest(Verb.POST, api.getAccessTokenEndpoint());
			request.addBodyParameter(OAuthConstants.CLIENT_ID, getApiKey());
			request.addBodyParameter(OAuthConstants.CLIENT_SECRET, getApiSecret());
			request.addBodyParameter(OAuthConstants.CODE, code);
			request.addBodyParameter(OAuthConstants.REDIRECT_URI, getCallback());
			request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
			request.addBodyParameter(OAuthConstants.SCOPE, getDefaultScope());
			Response response = execute(request);
			return api.getAccessTokenExtractor().extract(response);
		}

		@Override
		public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
			request.addHeader(OAuthConstants.HEADER, "Bearer " + urlEncode(accessToken.getAccessToken()));
		}
    }
}
