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
package org.olat.mcp;

import java.util.Date;

import org.olat.core.id.Identity;

/**
 * The metadata of an MCP token, read from its row in o_bs_authentication
 * (provider MCP). Never holds the plaintext or the hash.
 *
 * @param key The key of the authentication row
 * @param tokenId The token id, stored in authusername, e.g. t_7Qz…
 * @param label The label the user gave the token
 * @param identity The owner
 * @param creationDate Start of the validity
 * @param expirationDate End of the validity, null for no expiry
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public record McpToken(Long key, String tokenId, String label, Identity identity, Date creationDate, Date expirationDate) {

	/**
	 * @param now The reference date
	 * @return true if the token has an expiration date in the past
	 */
	public boolean isExpired(Date now) {
		return expirationDate != null && expirationDate.before(now);
	}
}
