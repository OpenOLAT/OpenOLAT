/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.teams.manager;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OAuth2Tokens;
import org.olat.basesecurity.model.OAuth2TokensImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.spi.MicrosoftAzureADFSProvider;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import reactor.core.publisher.Mono;

/**
 * 
 * Initial date: 24 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuth2TokenCredential implements TokenCredential {
	
	private static final Logger log = Tracing.createLoggerFor(OAuth2TokenCredential.class);
	
	private OAuth2Tokens tokens;
	
	public OAuth2TokenCredential(OAuth2Tokens tokens) {
		this.tokens = tokens;
	}

	@Override
	public Mono<AccessToken> getToken(TokenRequestContext request) {
		if(StringHelper.containsNonWhitespace(tokens.getAccessToken()) && !tokens.isExpired()) {
			String accessToken = tokens.getAccessToken();
			return Mono.just(new AccessToken(accessToken, null));
		}
		if(StringHelper.containsNonWhitespace(tokens.getRefreshToken())) {
			String refreshToken = tokens.getRefreshToken();
			OAuth2AccessToken newAccessToken = getAccessToken(refreshToken);
			((OAuth2TokensImpl)tokens).refresh(newAccessToken);
			String accessToken = tokens.getAccessToken();
			return Mono.just(new AccessToken(accessToken, null));
		}
		return Mono.empty(); 
	}
	
	
	private OAuth2AccessToken getAccessToken(String refreshToken) {
		try {
			MicrosoftAzureADFSProvider provider = CoreSpringFactory.getImpl(MicrosoftAzureADFSProvider.class);
			OAuth20Service oauth20Service = provider.getScribeProvider();
			return oauth20Service.refreshAccessToken(refreshToken);
		} catch (IOException | InterruptedException | ExecutionException e) {
			log.error("", e);
			return null;
		}
	}
}
