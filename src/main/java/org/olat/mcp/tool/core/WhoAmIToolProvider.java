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
package org.olat.mcp.tool.core;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The reference tool of the core pack. It tells the model who it acts for and
 * which tools it holds. The model is told to call it first in a conversation.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class WhoAmIToolProvider implements McpToolProvider {

	public static final String NAME = "openolat_whoami";

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: who am I",
			"Account",
			"Returns the OpenOlat account this connection acts for: user key, username, display name, "
			+ "organisation roles, the MCP profiles that apply and the list of tools granted to this account with their scope. "
			+ "Call it first in a new conversation to learn what you may do, and whenever a tool is refused. "
			+ "It has no parameters, reads nothing but the account and changes nothing.",
			McpToolDefinition.NO_INPUT,
			Map.of("type", "object",
				"properties", Map.of(
					"identityKey", Map.of("type", "integer", "description", "The key of the OpenOlat account"),
					"username", Map.of("type", "string"),
					"displayName", Map.of("type", "string"),
					"roles", Map.of("type", "array", "items", Map.of("type", "string", "enum", McpToolSupport.names(OrganisationRoles.values())),
							"description", "Organisation roles of the account, the names of OrganisationRoles"),
					"profiles", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Identifiers of the MCP profiles that apply"),
					"tools", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Granted tool names, the tools of disabled modules left out"),
					"instanceUrl", Map.of("type", "string", "description", "Base URL of this OpenOlat instance"),
					"version", Map.of("type", "string"),
					"serverTime", Map.of("type", "string", "format", "date-time"),
					"locale", Map.of("type", "string")),
				"required", List.of("identityKey", "username", "displayName", "roles", "profiles", "tools", "instanceUrl", "version", "serverTime")),
			false, true);

	@Autowired
	private UserManager userManager;

	@Override
	public String getToolName() {
		return NAME;
	}

	@Override
	public String getPack() {
		return PACK_CORE;
	}

	@Override
	public McpToolScope getScope() {
		return McpToolScope.READ;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public McpToolDefinition getDefinition() {
		return DEFINITION;
	}

	@Override
	public McpToolResult execute(McpToolContext context, Map<String,Object> arguments) {
		Identity identity = context.identity();
		List<String> roleNames = roleNames(context.roles());
		List<String> tools = availableGrantedTools(context);
		String displayName = userManager.getUserDisplayName(identity);

		Map<String,Object> result = new LinkedHashMap<>();
		result.put("identityKey", identity.getKey());
		result.put("username", McpToolSupport.username(identity));
		result.put("displayName", displayName);
		result.put("roles", roleNames);
		result.put("profiles", context.grants().profileIdentifiers());
		result.put("tools", tools);
		result.put("instanceUrl", Settings.getServerContextPathURI());
		result.put("version", Settings.getVersion());
		result.put("serverTime", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		result.put("locale", context.locale() == null ? null : context.locale().toString());

		String text = "You act for " + displayName + " (" + McpToolSupport.username(identity) + ", key " + identity.getKey() + ") on "
				+ Settings.getServerContextPathURI() + ". Roles: " + String.join(", ", roleNames)
				+ ". Profiles: " + String.join(", ", context.grants().profileIdentifiers())
				+ ". Granted tools: " + String.join(", ", tools) + ".";
		return McpToolResult.of(result, text);
	}

	/**
	 * The same list as tools/list (McpGrantFilteringTransport): granted, known
	 * to the registry and available, that is the module of the pack is enabled.
	 * The registry is looked up lazily, it collects this provider itself.
	 *
	 * @param context The caller
	 * @return The sorted tool names
	 */
	private List<String> availableGrantedTools(McpToolContext context) {
		McpToolRegistry registry = CoreSpringFactory.getImpl(McpToolRegistry.class);
		List<String> tools = new ArrayList<>(context.grants().tools().size());
		for(String tool:context.grants().tools()) {
			McpToolProvider provider = registry.getProvider(tool);
			if(provider != null && provider.isAvailable()) {
				tools.add(tool);
			}
		}
		tools.sort(null);
		return tools;
	}

	private static List<String> roleNames(Roles roles) {
		List<String> names = new ArrayList<>();
		if(roles != null) {
			for(OrganisationRoles role:OrganisationRoles.values()) {
				if(roles.hasRole(role)) {
					names.add(role.name());
				}
			}
		}
		return names;
	}
}
