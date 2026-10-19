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
package org.olat.mcp.manager;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpToken;

/**
 * What the filter caches per token between two requests: the non-GUI user
 * session with identity and roles, the resolved grants and the token
 * metadata. The bearer is verified on every request, the entry only saves
 * the identity, roles and grants queries.
 *
 * @param userSession The detached non-GUI session, never signed on. It carries
 *        the identity, the client IP and the auth provider MCP for the user
 *        activity logger
 * @param roles The roles of the identity at creation time
 * @param grants The resolved grants at creation time
 * @param token The token metadata
 * @param createdAt System.currentTimeMillis() at creation
 * @param assessmentLocked true if an assessment mode or an assessment
 *        inspection locks the identity at creation time, the rule of the UI
 *        login (AuthHelper.doLogin). Re-evaluated with the token after
 *        {@link McpAuthFilter#REVALIDATE_AFTER_MS}.
 * @param activity The client IP, the client and the times of the first and
 *        the last request, for the session administration. Never null.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public record McpIdentityEntry(UserSession userSession, Roles roles, McpGrants grants, McpToken token, long createdAt,
		boolean assessmentLocked, McpSessionActivity activity) {

	public McpIdentityEntry {
		if(activity == null) {
			activity = new McpSessionActivity(null, null, createdAt, createdAt);
		}
	}

	/**
	 * An entry without assessment lock and without request data.
	 */
	public McpIdentityEntry(UserSession userSession, Roles roles, McpGrants grants, McpToken token, long createdAt) {
		this(userSession, roles, grants, token, createdAt, false, null);
	}

	/**
	 * @return The identity of the session, or the owner of the token for an
	 *         entry without session (an identity without grants)
	 */
	public Identity getIdentity() {
		if(userSession != null && userSession.getIdentity() != null) {
			return userSession.getIdentity();
		}
		return token == null ? null : token.identity();
	}

	/**
	 * @return The key of the identity or null
	 */
	public Long getIdentityKey() {
		Identity identity = getIdentity();
		return identity == null ? null : identity.getKey();
	}
}
