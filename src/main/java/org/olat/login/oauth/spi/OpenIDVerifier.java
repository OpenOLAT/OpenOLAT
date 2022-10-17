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

import jakarta.servlet.http.HttpSession;

import org.olat.core.gui.UserRequest;
import org.olat.login.oauth.OAuthConstants;

/**
 * 
 * Initial date: 19.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenIDVerifier {
	
	private final String state;
	private final String idToken;
	private final String accessToken;
	
	private final String sessionState;
	private final String sessionNonce;
	
	public OpenIDVerifier(String state, String idToken, String accessToken, String sessionState, String sessionNonce) {
		this.state = state;
		this.idToken = idToken;
		this.accessToken = accessToken;
		this.sessionState = sessionState;
		this.sessionNonce = sessionNonce;
	}
	
	public String getState() {
		return state;
	}

	public String getIdToken() {
		return idToken;
	}

	public String getAccessToken() {
		return accessToken;
	}
	
	public String getSessionState() {
		return sessionState;
	}

	public String getSessionNonce() {
		return sessionNonce;
	}

	public static OpenIDVerifier create(UserRequest ureq, HttpSession httpSession) {
		String idToken = ureq.getParameter("id_token");
		String accessToken = ureq.getParameter("access_token");
		String state = ureq.getParameter("state");
		String sessionNonce = (String)httpSession.getAttribute(OAuthConstants.OAUTH_NONCE);
		String sessionState = (String)httpSession.getAttribute(OAuthConstants.OAUTH_STATE);
		return new OpenIDVerifier(state, idToken, accessToken, sessionState, sessionNonce);
	}
}
