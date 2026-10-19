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
import java.util.Set;

/**
 * The effective MCP rights of one identity: the union of the tools of every
 * matching enabled profile, the highest rate limit override and the
 * identifiers of the matching profiles for the audit log. Immutable, safe to
 * cache.
 *
 * @param tools The names of the granted tools, never null
 * @param requestsPerMinute The highest override of the matching profiles, null for the module default
 * @param profileIdentifiers The identifiers of the matching enabled profiles, sorted, never null
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public record McpGrants(Set<String> tools, Integer requestsPerMinute, List<String> profileIdentifiers) {

	public static final McpGrants EMPTY = new McpGrants(Set.of(), null, List.of());

	public McpGrants {
		tools = tools == null ? Set.of() : Set.copyOf(tools);
		profileIdentifiers = profileIdentifiers == null ? List.of() : List.copyOf(profileIdentifiers);
	}

	/**
	 * @return true if no profile matches, the identity has no MCP access
	 */
	public boolean isEmpty() {
		return profileIdentifiers.isEmpty();
	}

	/**
	 * @param toolName The name of a tool
	 * @return true if one of the matching profiles grants the tool
	 */
	public boolean isGranted(String toolName) {
		return toolName != null && tools.contains(toolName);
	}
}
