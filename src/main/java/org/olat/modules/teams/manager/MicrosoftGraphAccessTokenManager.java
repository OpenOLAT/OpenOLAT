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
package org.olat.modules.teams.manager;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.SilentParameters;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class MicrosoftGraphAccessTokenManager {
	
	private static final Logger log = Tracing.createLoggerFor(MicrosoftGraphAccessTokenManager.class);
	
	private static final Set<String> SCOPES = Set.of("https://graph.microsoft.com/.default");
	
	private final TokenCacheAccessAspect cache = new TokenCacheAccessAspect();
	
	private final String clientId;
	private final String clientSecret;
	private final String tenantGuid;
	
	MicrosoftGraphAccessTokenManager(String clientId, String clientSecret, String tenantGuid) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.tenantGuid = tenantGuid;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public String getTenantGuid() {
		return tenantGuid;
	}
	
	public CompletableFuture<String> getAccessToken() {
		return connect(clientId, clientSecret, tenantGuid);
	}
	
	private CompletableFuture<String> connect(String id, String secret, String tenant) {
		ConfidentialClientApplication cca = createClientApplication(id, secret, tenant);

		CompletableFuture<IAuthenticationResult> result = null;
		if (cca != null) {
			try {
				if (cache.isEmpty()) {
					ClientCredentialParameters parameters = ClientCredentialParameters
							.builder(SCOPES)
							.build();
					result = cca
							.acquireToken(parameters);
				} else {
					SilentParameters silentParameters = SilentParameters
							.builder(SCOPES)
							.build();
					// try to acquire token silently. This call will fail since the token cache does not
					// have a token for the application you are requesting an access token for
					result = cca
							.acquireTokenSilently(silentParameters);
				}
			} catch (Exception ex) {
				if (ex.getCause() instanceof MsalException) {
					ClientCredentialParameters parameters = ClientCredentialParameters
							.builder(SCOPES)
							.build();
					result = cca
							.acquireToken(parameters);
				} else {
					log.error("", ex);
				}
			}
		}
		if(result != null) {
			return result.handleAsync((res, ex) -> {
				if (ex != null && (ex instanceof MsalException || ex.getCause() instanceof MsalException)) {
					ClientCredentialParameters parameters = ClientCredentialParameters
							.builder(SCOPES)
							.build();
					return cca.acquireToken(parameters).join();
				}
				return res;
			}).thenApply(IAuthenticationResult::accessToken);
		}
		return CompletableFuture.completedFuture((String)null);
	}
	
	private ConfidentialClientApplication createClientApplication(String id, String secret, String tenant) {
		String authority = "https://login.microsoftonline.com/" + tenant +  "/";	
		try {
			IClientCredential credential = ClientCredentialFactory.createFromSecret(secret);
			return ConfidentialClientApplication
			                 .builder(id, credential)
			                 .authority(authority)
			                 .setTokenCacheAccessAspect(cache)
			                 .build();
		} catch (MalformedURLException e) {
			log.error("Authorithy is not an URL: {}", authority, e);
			return null;
		}
	}
	
	private static class TokenCacheAccessAspect implements ITokenCacheAccessAspect {

		private String data;
		
		public boolean isEmpty() {
			return data == null;
		}

		@Override
		public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
	        iTokenCacheAccessContext.tokenCache().deserialize(data);
		}

		@Override
		public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
			data = iTokenCacheAccessContext.tokenCache().serialize();
			log.debug(Tracing.M_AUDIT, "Access cached token: {}", data);
		}
	}
}
