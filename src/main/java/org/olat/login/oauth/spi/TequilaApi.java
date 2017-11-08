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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.CoreSpringFactory;
import org.olat.login.oauth.OAuthLoginModule;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
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
 * Initial date: 30 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TequilaApi extends DefaultApi20 {

	@Override
	public String getAccessTokenEndpoint() {
		OAuthLoginModule oauthModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
		String endpoint = oauthModule.getTequilaOAuth2Endpoint();
		if(!endpoint.endsWith("/")) {
			endpoint += "/";
		}
		endpoint += "token";
		return endpoint;
	}

	@Override
	public String getAuthorizationUrl(OAuthConfig config) {
		OAuthLoginModule oauthModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
		String endpoint = oauthModule.getTequilaOAuth2Endpoint();
		if(!endpoint.endsWith("/")) {
			endpoint += "/";
		}
		String url = endpoint + "auth?response_type=code" +
                "&client_id=" + urlEncode(config.getApiKey()) +
                "&scope=" + config.getScope() + 
                "&redirect_uri=" + urlEncode(config.getCallback());
		return url;
	}
	
    @Override
	public AccessTokenExtractor getAccessTokenExtractor() {
		return new TequilaBearerExtractor();
	}
    
	@Override
    public OAuthService createService(OAuthConfig config) {
        return new TequilaAuth2Service(this, config);
    }
	
	public static class TequilaBearerExtractor implements AccessTokenExtractor {
		
		private Pattern accessTokenPattern = Pattern.compile("\"access_token\":\\s*\"(\\S*?)\"");

		@Override
		public Token extract(String response) {
			Matcher matcher = accessTokenPattern.matcher(response);
			if(matcher.find()) {
				return new Token(matcher.group(1), "", response);
			}
			if(response.contains("Bearer")) {
				String t = "\"access_token\": \"Bearer ";
				int index = response.indexOf(t);
				if(index >= 0) {
					int endIndex = response.indexOf("\"", index + t.length());
					String token = response.substring(index + t.length(), endIndex);
					String decodedToken = OAuthEncoder.decode(token);
					return new Token(decodedToken, "", response);
				}
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
    
    private class TequilaAuth2Service extends OAuth20ServiceImpl {
    	
        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
    	
		private final TequilaApi api;
		private OAuthConfig config;
    	
        public TequilaAuth2Service(TequilaApi api, OAuthConfig config) {
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
            	request.addBodyParameter(OAuthConstants.SCOPE, config.getScope());
		    Response response = request.send();
		    Token token = api.getAccessTokenExtractor().extract(response.getBody());
		    return token;
		}

		@Override
		public void signRequest(Token accessToken, OAuthRequest request) {
			request.addHeader(OAuthConstants.HEADER, "Bearer " + urlEncode(accessToken.getToken()));
		}
    }
}
