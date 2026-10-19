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
package org.olat.mcp.tool;

import java.util.Map;

/**
 * One MCP tool. Implementations are Spring beans, the registry collects them
 * at startup. The contract for every implementation:
 * <ul>
 *  <li>the name is unique, lowercase, underscores only, pattern
 *      {@link #NAME_PATTERN}: <code>&lt;object&gt;_query</code> or <code>&lt;object&gt;_modify</code></li>
 *  <li>the scope is READ for a query tool and WRITE for a modify tool</li>
 *  <li>{@link #execute(McpToolContext, Map)} uses the service methods the UI
 *      controllers use, so activity log, notifications and mails fire identically</li>
 *  <li>object permissions are re-checked with the security callbacks of the
 *      module and denied with {@link McpPermissionException}</li>
 *  <li>results carry stable keys and business path URLs, lists respect a
 *      <code>limit</code> argument with a maximum</li>
 * </ul>
 * The registry wraps every call: it checks the grant, installs the activity
 * logger, commits or rolls back the transaction and maps exceptions to error
 * results. A provider does not handle transactions itself, except where a
 * service commits inside or a lock needs its own transaction; the provider
 * documents such a case in its javadoc (e.g. project_modify create,
 * note_modify update).
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface McpToolProvider {

	/** Lowercase letters, digits and underscores, starts with a letter, 3 to 64 characters */
	public static final String NAME_PATTERN = "^[a-z][a-z0-9_]{2,63}$";

	/** Lowercase letters, digits and underscores, starts with a letter, 1 to 32 characters */
	public static final String PACK_PATTERN = "^[a-z][a-z0-9_]{0,31}$";

	/** The pack of the tools of the MCP module itself, e.g. openolat_whoami */
	public static final String PACK_CORE = "core";

	public static final String QUERY_SUFFIX = "_query";
	public static final String MODIFY_SUFFIX = "_modify";

	/**
	 * @return The unique tool name, e.g. todo_query
	 */
	String getToolName();

	/**
	 * The pack groups the tools in the profile editor and orders tools/list.
	 * Every module declares its pack name once as a constant in its
	 * <code>.mcp</code> package, e.g. <code>ProjectMcpPack.NAME</code>.
	 *
	 * @return The pack name, pattern {@link #PACK_PATTERN}
	 */
	String getPack();

	/**
	 * The object the tool works on: the part of the tool name before
	 * <code>_query</code> or <code>_modify</code>. The query and the modify
	 * tool of one object share a row in the profile editor. A tool name with
	 * neither suffix keeps its name without the <code>openolat_</code> prefix,
	 * e.g. openolat_whoami returns whoami.
	 *
	 * @return The object id, e.g. appointment for appointment_query
	 */
	default String getObject() {
		String name = getToolName();
		if(name.endsWith(QUERY_SUFFIX)) {
			return name.substring(0, name.length() - QUERY_SUFFIX.length());
		}
		if(name.endsWith(MODIFY_SUFFIX)) {
			return name.substring(0, name.length() - MODIFY_SUFFIX.length());
		}
		if(name.startsWith("openolat_")) {
			return name.substring("openolat_".length());
		}
		return name;
	}

	McpToolScope getScope();

	/**
	 * @return false if the module behind the tool is disabled; the tool is
	 *         then hidden from every list and refused on call
	 */
	boolean isAvailable();

	/**
	 * @return The catalog entry, built once, no per-request data
	 */
	McpToolDefinition getDefinition();

	/**
	 * Executes the tool. The arguments are validated against the input
	 * schema by the SDK before this method runs.
	 *
	 * @param context The caller
	 * @param arguments The validated arguments, never null
	 * @return The result, never null
	 * @throws McpPermissionException if a security callback denies the operation
	 */
	McpToolResult execute(McpToolContext context, Map<String,Object> arguments);

}
