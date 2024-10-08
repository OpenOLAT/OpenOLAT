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
package org.olat.login.oauth;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.Tracing;
import org.olat.login.oauth.model.OAuthSession;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(OAuthResource.class);
	
	private final HttpSession session;
	private final OAuthSPI provider;
	
	public OAuthResource(OAuthSPI provider, HttpSession session) {
		this.provider = provider;
		this.session = session;
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		redirect(provider, hres, session);
	}

	@Override
	public void release() {
		//
	}
	
	public static void redirect(OAuthSPI oauthProvider, HttpServletResponse httpResponse, HttpSession httpSession) {
		//Configure
		try {
			OAuthService service = oauthProvider.getScribeProvider();
			httpSession.setMaxInactiveInterval(900);
			
			if(service instanceof OAuth20Service oauthService) {
				String state = UUID.randomUUID().toString().replace("-", "");
				String redirectUrl = oauthService.getAuthorizationUrl(state);
				String nonce = extractNonce(redirectUrl);
				
				OAuthSession oSession = new OAuthSession(state, nonce, null, oauthProvider, service);
				CoreSpringFactory.getImpl(OAuthLoginManager.class).registerAuthorizationRequest(state, oSession);
				httpSession.setAttribute(OAuthConstants.OAUTH_SESSION, oSession);
				httpResponse.sendRedirect(redirectUrl);
			} else if(service instanceof OAuth10aService oauthService) {
				OAuth1RequestToken token = oauthService.getRequestToken();
				String redirectUrl = oauthService.getAuthorizationUrl(token);

				OAuthSession oSession = new OAuthSession(null, null, token, oauthProvider, service);
				CoreSpringFactory.getImpl(OAuthLoginManager.class).registerAuthorizationRequest(token.getTokenSecret(), oSession);
				httpSession.setAttribute(OAuthConstants.OAUTH_SESSION, oSession);
				httpResponse.sendRedirect(redirectUrl);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private static String extractNonce(String redirectUrl) {
		try {
			String nonce = null;
			
			URL url = new URL(redirectUrl);
			final String[] pairs = url.getQuery().split("&");
			for (String pair : pairs) {
				final int idx = pair.indexOf('=');
				final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			    final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			    
			    if(key.equals("nonce")) {
			    	nonce = value;
			    }
			}
			
			return nonce;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
