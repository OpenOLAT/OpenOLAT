/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.mcp.ui;

import java.util.Date;

import org.olat.mcp.McpToken;
import org.olat.user.UserPortraitComponent;

/**
 * One MCP session of this node in the session administration: one entry of
 * the MCP identity cache, that is one user and one token in use.
 *
 * Initial date: 14 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpSessionRow {

	private final Long identityKey;
	private final String displayName;
	private final String username;
	private final McpToken token;
	private final String client;
	private final String clientIp;
	private final Date firstRequest;
	private final Date lastRequest;
	private UserPortraitComponent portraitComponent;

	public McpSessionRow(Long identityKey, String displayName, String username, McpToken token,
			String client, String clientIp, Date firstRequest, Date lastRequest) {
		this.identityKey = identityKey;
		this.displayName = displayName;
		this.username = username;
		this.token = token;
		this.client = client;
		this.clientIp = clientIp;
		this.firstRequest = firstRequest;
		this.lastRequest = lastRequest;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUsername() {
		return username;
	}

	public McpToken getToken() {
		return token;
	}

	public String getTokenLabel() {
		return token == null ? null : token.label();
	}

	public String getClient() {
		return client;
	}

	public String getClientIp() {
		return clientIp;
	}

	public Date getFirstRequest() {
		return firstRequest;
	}

	public Date getLastRequest() {
		return lastRequest;
	}

	public UserPortraitComponent getPortraitComponent() {
		return portraitComponent;
	}

	public void setPortraitComponent(UserPortraitComponent portraitComponent) {
		this.portraitComponent = portraitComponent;
	}
}
