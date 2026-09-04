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

import java.util.Objects;

import org.olat.basesecurity.model.OAuth2TokensImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.model.TeamsMeetingImpl;

/**
 * 
 * Initial date: 1 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OAuth2TokensOffline extends OAuth2TokensImpl {
	
	private final String organizerId;
	private String initialRefreshToken;
	
	public OAuth2TokensOffline(String refreshToken, String organizerId) {
		this.organizerId = organizerId;
		this.initialRefreshToken = refreshToken;
		setRefreshToken(refreshToken);
	}
	
	public static OAuth2TokensOffline valueOf(TeamsMeeting meeting) {
		final String organizerId = ((TeamsMeetingImpl)meeting).getOrganizerAzureId();
		
		String refreshToken = null;
		String encryptedToken = ((TeamsMeetingImpl)meeting).getOrganizerTokenEncrypted();
		if(StringHelper.containsNonWhitespace(encryptedToken) && StringHelper.containsNonWhitespace(organizerId)) {
			refreshToken = CoreSpringFactory.getImpl(TeamsCryptoHelper.class).decryptToken(encryptedToken, organizerId);
		}
		return new OAuth2TokensOffline(refreshToken, organizerId);
	}

	public String getOrganizerId() {
		return organizerId;
	}
	
	public boolean hasRefreshTokenChanged() {
		return !Objects.equals(initialRefreshToken, getRefreshToken());
	}
	
	public void tokenRefreshed() {
		initialRefreshToken = getRefreshToken();
	}
}
