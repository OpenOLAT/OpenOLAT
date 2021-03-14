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
package org.olat.ims.lti13.model;

import java.io.Serializable;

import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * 
 * Initial date: 12 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessTokenTimed implements Serializable {

	private static final long serialVersionUID = -2923786817169814746L;
	
	private final OAuth2AccessToken accessToken;
	private final long expirationTimeMs;
	
	public AccessTokenTimed(OAuth2AccessToken accessToken) {
		this.accessToken = accessToken;
		if(accessToken.getExpiresIn() == null) {
			expirationTimeMs = 0l;
		} else {
			expirationTimeMs = System.currentTimeMillis()
					+ (accessToken.getExpiresIn().intValue() * 1000l)
					- (5 * 60 * 1000);// remove a buffer of 5 minutes
		}
	}
	
	public OAuth2AccessToken getAccessToken() {
		return accessToken;
	}
	
	public boolean hasExpired() {
		return System.currentTimeMillis() > expirationTimeMs;
	}
}
