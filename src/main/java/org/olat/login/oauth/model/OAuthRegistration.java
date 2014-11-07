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
package org.olat.login.oauth.model;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthRegistration {
	
	private OAuthUser oauthUser;
	private Identity identity;
	private final String authProvider;
	
	public OAuthRegistration(String authProvider, OAuthUser oauthUser) {
		this.authProvider = authProvider;
		this.oauthUser = oauthUser;
	}
	
	public String getAuthProvider() {
		return authProvider;
	}

	public OAuthUser getOauthUser() {
		return oauthUser;
	}
	
	public void setOauthUser(OAuthUser oauthUser) {
		this.oauthUser = oauthUser;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
}
