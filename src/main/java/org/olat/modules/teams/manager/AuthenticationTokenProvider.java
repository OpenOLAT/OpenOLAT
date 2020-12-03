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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;

/**
 * 
 * Initial date: 24 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthenticationTokenProvider implements IAuthenticationProvider {
	
	private static final Logger log = Tracing.createLoggerFor(AuthenticationTokenProvider.class);
	
	private final MicrosoftGraphAccessTokenManagerImpl accessTokenManager;
	
	public AuthenticationTokenProvider(MicrosoftGraphAccessTokenManagerImpl accessTokenManager) {
		this.accessTokenManager = accessTokenManager;
	}

	@Override
	public void authenticateRequest(IHttpRequest request) {
        try {
        	String accessToken = accessTokenManager.getAccessToken();
            request.addHeader("Authorization", "Bearer " + accessToken);
        } catch (Exception e) {
        	log.error("", e);
        }
	}
}
