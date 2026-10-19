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

/**
 * The result of a bearer token validation. The filter answers 401 for every
 * status except {@link Status#valid}, with one uniform message.
 *
 * @param status The outcome
 * @param token The token metadata, null unless the row was found
 * @param tokenHash The SHA-256 hex of the plaintext, the key of the identity cache, null for a malformed token
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public record McpTokenValidation(Status status, McpToken token, String tokenHash) {

	public enum Status {
		/** The token is known, not expired and the identity is active */
		valid,
		/** No row with this hash, or the plaintext has not the token format */
		unknown,
		/** The row exists but the validity ended */
		expired,
		/** The row exists but the identity is not in status active or permanent */
		identityInactive
	}

	public static McpTokenValidation unknown(String tokenHash) {
		return new McpTokenValidation(Status.unknown, null, tokenHash);
	}

	public static McpTokenValidation valid(McpToken token, String tokenHash) {
		return new McpTokenValidation(Status.valid, token, tokenHash);
	}

	public static McpTokenValidation expired(McpToken token, String tokenHash) {
		return new McpTokenValidation(Status.expired, token, tokenHash);
	}

	public static McpTokenValidation identityInactive(McpToken token, String tokenHash) {
		return new McpTokenValidation(Status.identityInactive, token, tokenHash);
	}

	public boolean isValid() {
		return status == Status.valid;
	}
}
