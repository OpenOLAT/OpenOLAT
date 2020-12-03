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

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.teams.TeamsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class MicrosoftGraphAccessTokenManagerImpl {
	
	private static final Logger log = Tracing.createLoggerFor(MicrosoftGraphAccessTokenManagerImpl.class);
	
	private TokenCacheAccessAspect cache = new TokenCacheAccessAspect();
	
	@Autowired
	private TeamsModule teamsModule;
	
	public String getAccessToken() {
		String clientId = teamsModule.getApiKey();
		String clientSecret = teamsModule.getApiSecret();
		String tenantGuid = teamsModule.getTenantGuid();
		return connect(clientId, clientSecret, tenantGuid);
	}
	
	private String connect(String clientId, String clientSecret, String tenantGuid) {
		Set<String> scopes = new HashSet<>();
		scopes.add("https://graph.microsoft.com/.default");
		String authority = "https://login.microsoftonline.com/" + tenantGuid +  "/";
		
		
		try {
			IClientCredential credential = ClientCredentialFactory.createFromSecret(clientSecret);
			ConfidentialClientApplication cca = ConfidentialClientApplication
			                 .builder(clientId, credential)
			                 .authority(authority)
			                 .setTokenCacheAccessAspect(cache)
			                 .build();
		
			IAuthenticationResult result;
	        try {
	            SilentParameters silentParameters = SilentParameters
	            	.builder(scopes)
	            	.build();
	
	            // try to acquire token silently. This call will fail since the token cache does not
	            // have a token for the application you are requesting an access token for
	            result = cca.acquireTokenSilently(silentParameters).join();
	        } catch (Exception ex) {
	            if (ex.getCause() instanceof MsalException) {
	
	                ClientCredentialParameters parameters = ClientCredentialParameters
	                		.builder(scopes)
	                		.build();
	
	                // Try to acquire a token. If successful, you should see
	                // the token information printed out to console
	                result = cca.acquireToken(parameters).join();
	            } else {
	                // Handle other exceptions accordingly
	                throw ex;
	            }
	        }
	       	String accessToken = result.accessToken();
	       	log.debug(Tracing.M_AUDIT, "Access token: {}", accessToken);
	       	return accessToken;
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	private class TokenCacheAccessAspect implements ITokenCacheAccessAspect {
		
		private String data;

		@Override
		public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
	        iTokenCacheAccessContext.tokenCache().deserialize(data);
		}

		@Override
		public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
			data = iTokenCacheAccessContext.tokenCache().serialize();
			log.debug(Tracing.M_AUDIT, "Access token: {}", data);
		}
	}

}
