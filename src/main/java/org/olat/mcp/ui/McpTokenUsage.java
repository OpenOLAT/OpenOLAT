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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.mcp.McpToken;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogService;

/**
 * The usage of a token as the audit log sees it: the number of
 * the rows of the channel mcp with the token id in the path params and the
 * date of the latest row. The audit log holds the calls of changing tools,
 * the denied and the failed calls, and the reading calls only if their
 * logging is switched on.
 *
 * Initial date: 12 Oct 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public record McpTokenUsage(int count, Date lastUse) {

	public static final McpTokenUsage NONE = new McpTokenUsage(0, null);

	private static final Pattern TOKEN_ID = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

	/**
	 * Loads the audit rows of the owners of the tokens in one query and counts
	 * them per token id in Java. Token lists are small, the query uses the
	 * index on the identity.
	 *
	 * @param auditLogService The audit log service
	 * @param tokens The tokens
	 * @return The usage per token id, without entry for an unused token
	 */
	public static Map<String,McpTokenUsage> load(ApiAuditLogService auditLogService, Collection<McpToken> tokens) {
		Map<String,McpTokenUsage> usages = new HashMap<>();
		if(tokens == null || tokens.isEmpty()) {
			return usages;
		}

		Set<String> tokenIds = new HashSet<>();
		Set<Long> identityKeys = new HashSet<>();
		for(McpToken token:tokens) {
			tokenIds.add(token.tokenId());
			if(token.identity() != null) {
				identityKeys.add(token.identity().getKey());
			}
		}
		if(identityKeys.isEmpty()) {
			return usages;
		}

		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKeys(identityKeys);
		params.setChannels(List.of(ApiAuditChannel.mcp));
		List<ApiAuditLog> rows = auditLogService.search(params, 0, -1);
		for(ApiAuditLog row:rows) {
			String tokenId = getTokenId(row.getPathParams());
			if(tokenId != null && tokenIds.contains(tokenId)) {
				McpTokenUsage usage = usages.getOrDefault(tokenId, NONE);
				Date lastUse = usage.lastUse();
				if(lastUse == null || (row.getCreationDate() != null && row.getCreationDate().after(lastUse))) {
					lastUse = row.getCreationDate();
				}
				usages.put(tokenId, new McpTokenUsage(usage.count() + 1, lastUse));
			}
		}
		return usages;
	}

	/**
	 * @param pathParams The path params of an audit row as JSON
	 * @return The token id or null
	 */
	static String getTokenId(String pathParams) {
		if(pathParams == null || pathParams.indexOf("\"token\"") < 0) {
			return null;
		}
		Matcher matcher = TOKEN_ID.matcher(pathParams);
		return matcher.find() ? matcher.group(1) : null;
	}
}
