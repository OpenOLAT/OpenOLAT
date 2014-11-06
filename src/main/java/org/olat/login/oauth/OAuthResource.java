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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthResource implements MediaResource {
	
	private static final OLog log = Tracing.createLoggerFor(OAuthResource.class);
	
	private final HttpSession session;
	private final OAuthSPI provider;
	
	public OAuthResource(OAuthSPI provider, HttpSession session) {
		this.provider = provider;
		this.session = session;
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
		//Configure
		try {
			ServiceBuilder builder= new ServiceBuilder(); 
			builder.provider(provider.getScribeProvider())
					.apiKey(provider.getAppKey())
					.apiSecret(provider.getAppSecret());
			String[] scopes = provider.getScopes();
			for(String scope:scopes) {
				builder = builder.scope(scope);
			}

			String callbackUrl = Settings.getServerContextPathURI() + "/oauthcallback";
			OAuthService service = builder
					.callback(callbackUrl)
					.build(); //Now build the call
			
			session.setAttribute(OAuthConstants.OAUTH_SERVICE, service);
			session.setAttribute(OAuthConstants.OAUTH_SPI, provider);
			
			if("2.0".equals(service.getVersion())) {
				String redirectUrl = service.getAuthorizationUrl(null);
				hres.sendRedirect(redirectUrl);
			} else {
				Token token = service.getRequestToken();
				session.setAttribute(OAuthConstants.REQUEST_TOKEN, token);
				String redirectUrl = service.getAuthorizationUrl(token);
				hres.sendRedirect(redirectUrl);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
