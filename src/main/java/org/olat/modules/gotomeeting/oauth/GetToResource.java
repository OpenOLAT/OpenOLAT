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

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.olat.core.gui.media.MediaResource;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.login.oauth.OAuthConstants;
import org.olat.login.oauth.OAuthSPI;

import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * 
 * Initial date: 14 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GetToResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(GetToResource.class);
	
	private final HttpSession session;
	private final GoToProvider provider;
	
	public GetToResource(GoToProvider provider, HttpSession session) {
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
			@SuppressWarnings("resource")
			OAuth20Service service = (OAuth20Service)oauthProvider.getScribeProvider();
			httpSession.setAttribute(OAuthConstants.OAUTH_SERVICE, service);
			httpSession.setAttribute(OAuthConstants.OAUTH_SPI, oauthProvider);
			String state = UUID.randomUUID().toString().replace("-", "");
			String redirectUrl = service.getAuthorizationUrl(state);
			saveStateAndNonce(httpSession, redirectUrl);
			httpResponse.sendRedirect(redirectUrl);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private static void saveStateAndNonce(HttpSession httpSession, String redirectUrl) {
		try {
			URL url = new URL(redirectUrl);
			final String[] pairs = url.getQuery().split("&");
			for (String pair : pairs) {
				final int idx = pair.indexOf('=');
				final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			    final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			    
			    if(key.equals("nonce")) {
			    	httpSession.setAttribute(OAuthConstants.OAUTH_NONCE, value);
			    } else if(key.endsWith("state")) {
			    	httpSession.setAttribute(OAuthConstants.OAUTH_STATE, value);
			    }
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
