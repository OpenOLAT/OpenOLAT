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

import javax.crypto.SecretKey;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.crypto.AesGcmCipher;
import org.olat.modules.teams.TeamsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TeamsCryptoHelper {
	
	private static final Logger log = Tracing.createLoggerFor(TeamsCryptoHelper.class);
	
	@Autowired
	private TeamsModule teamsModule;
	
	private SecretKey key;
	private String resolvedKeyValue;
	
	/**
	 * @return true if a valid key is configured and tokens can be saved
	 */
	public boolean isConfigured() {
		return getKey() != null;
	}
	
	/**
	 * @param refreshToken The token to protect
	 * @param organizerAzureId The Azure id of the organizer, used as authenticated data
	 * @return The encrypted token or null if it cannot be protected and must not be saved
	 */
	public String encryptToken(String refreshToken, String organizerAzureId) {
		if(!StringHelper.containsNonWhitespace(refreshToken)) {
			return null;
		}
		
		SecretKey secretKey = getKey();
		if(secretKey == null) {
			log.error("Refresh token of organizer {} is not saved, the property vc.teams.recording.token.key is missing or not valid",
					organizerAzureId);
			return null;
		}
		return AesGcmCipher.encrypt(secretKey, refreshToken, organizerAzureId);
	}
	
	/**
	 * @param encryptedToken The encrypted token as saved in the database
	 * @param organizerAzureId The Azure id of the organizer, used as authenticated data
	 * @return The refresh token or null if it cannot be read
	 */
	public String decryptToken(String encryptedToken, String organizerAzureId) {
		if(!StringHelper.containsNonWhitespace(encryptedToken)) {
			return null;
		}
		
		SecretKey secretKey = getKey();
		if(secretKey == null) {
			log.error("Refresh token of organizer {} cannot be read, the property vc.teams.recording.token.key is missing or not valid",
					organizerAzureId);
			return null;
		}
		return AesGcmCipher.decrypt(secretKey, encryptedToken, organizerAzureId);
	}
	
	private synchronized SecretKey getKey() {
		String configuredKey = teamsModule.getRecordingTokenKey();
		if(!StringHelper.containsNonWhitespace(configuredKey)) {
			key = null;
			resolvedKeyValue = null;
			return null;
		}
		
		if(key == null || !configuredKey.equals(resolvedKeyValue)) {
			key = AesGcmCipher.keyFromBase64(configuredKey);
			resolvedKeyValue = configuredKey;
			if(key == null) {
				log.error("The property vc.teams.recording.token.key is not a valid base64 encoded AES-256 key");
			}
		}
		return key;
	}
}
