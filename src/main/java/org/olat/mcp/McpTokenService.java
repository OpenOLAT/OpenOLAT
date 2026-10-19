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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.basesecurity.IdentityRef;

/**
 * MCP tokens live in o_bs_authentication with the provider {@link #MCP_AUTH_PROVIDER}:
 * one row per token, the token id in authusername, the label in externalid
 * prefixed by the identity key, the SHA-256 hex of the plaintext in
 * credential. The token authenticates the identity only, the grants are
 * resolved per request by {@link McpService}.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface McpTokenService {

	/** The provider in o_bs_authentication, max. 8 characters */
	public static final String MCP_AUTH_PROVIDER = "MCP";
	/** Every plaintext token starts with this prefix */
	public static final String TOKEN_PREFIX = "oomcp_";
	/** Every token id starts with this prefix */
	public static final String TOKEN_ID_PREFIX = "t_";
	/** Maximum length of a label */
	public static final int LABEL_MAX_LENGTH = 200;

	/**
	 * @param identity The owner
	 * @return The number of tokens of the identity
	 */
	int countTokens(IdentityRef identity);

	/**
	 * Generates a token with 192 bit of entropy, persists its hash and returns
	 * the plaintext once. The caller checks beforehand that the identity has
	 * grants and that the maximum per user is not reached; this method only
	 * enforces the maximum of {@link McpModule#getUserTokenMax()}.
	 *
	 * @param identity The owner
	 * @param label A label, trimmed, max. {@link #LABEL_MAX_LENGTH} characters
	 * @return The plaintext and the persisted metadata
	 * @throws IllegalArgumentException if the label is empty or too long.
	 *         Throws IllegalArgumentException for a label the identity already uses, compared ignoring case.
	 * @throws IllegalStateException if the maximum number of tokens is reached
	 */
	McpTokenCreation generateToken(Identity identity, String label);

	/**
	 * Hashes the plaintext, loads the row, checks the validity and the status
	 * of the identity. Never throws for a malformed plaintext.
	 *
	 * @param plaintext The bearer value of the request
	 * @return The validation, never null
	 */
	McpTokenValidation validateToken(String plaintext);

	/**
	 * @param identity The owner
	 * @return The tokens of the identity ordered by creation date
	 */
	List<McpToken> getTokens(IdentityRef identity);

	/**
	 * @param tokenId The token id
	 * @return The token or null
	 */
	McpToken getToken(String tokenId);

	/**
	 * Paginated list of every token for the administration.
	 *
	 * @param searchString Filter on label, token id, username or email, null for all
	 * @param firstResult The offset
	 * @param maxResults The page size
	 * @return The tokens ordered by creation date descending
	 */
	List<McpToken> searchTokens(String searchString, int firstResult, int maxResults);

	/**
	 * Counts the tokens of the administration list.
	 *
	 * @param searchString Filter on label, token id, username or email, null for all
	 * @return The number of tokens that match, see {@link #searchTokens(String, int, int)}
	 */
	int countSearchTokens(String searchString);

	/**
	 * Deletes the row. An owner may only revoke its own tokens.
	 *
	 * @param tokenId The token id
	 * @param owner The identity that revokes, null for an administrator
	 * @return true if a row was deleted
	 */
	boolean revokeToken(String tokenId, IdentityRef owner);

	/**
	 * Deletes every token whose validity ended, see {@link McpModule#getTokenValidityDays()}.
	 * Called by the nightly job.
	 *
	 * @return The number of deleted rows
	 */
	int deleteExpiredTokens();

	/**
	 * @param plaintext A token
	 * @return The SHA-256 hex of the plaintext, the value stored in credential
	 */
	String hash(String plaintext);

}
