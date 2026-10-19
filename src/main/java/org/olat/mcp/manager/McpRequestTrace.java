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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.mcp.tool.McpToolScope;

/**
 * What happened inside one MCP request, collected on the way through the
 * transport decorator and the tool registry and read by the filter after the
 * response for the audit row. One instance per request, used on the request
 * thread only.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpRequestTrace {

	/** The key of the trace in the transport context and the request attributes */
	public static final String KEY = "org.olat.mcp.trace";
	/** A longer string argument is cut to this number of characters in the trace */
	public static final int MAX_ARGUMENT_LENGTH = 1024;

	public enum Outcome {
		/** No tool call, or the tool returned a result */
		success,
		/** The tool returned isError, for example an invalid value */
		toolError,
		/** The grant or a security callback denied the call */
		denied,
		/** An exception in the tool, the transaction was rolled back */
		serverError
	}

	private String jsonRpcMethod;
	private String toolName;
	private McpToolScope scope;
	private String action;
	private Map<String,Object> arguments;
	private Outcome outcome = Outcome.success;
	private String errorReference;

	public String getJsonRpcMethod() {
		return jsonRpcMethod;
	}

	public void setJsonRpcMethod(String jsonRpcMethod) {
		this.jsonRpcMethod = jsonRpcMethod;
	}

	public String getToolName() {
		return toolName;
	}

	public String getAction() {
		return action;
	}

	public Map<String,Object> getArguments() {
		return arguments;
	}

	/**
	 * Records the tool call before the execution. The trace keeps a copy of
	 * the arguments for the audit in which every string longer than
	 * {@link #MAX_ARGUMENT_LENGTH} characters is cut, so a large upload (file
	 * content as base64) is not held until the end of the request. The map
	 * of the tool stays untouched.
	 *
	 * @param name The tool name
	 * @param toolScope The scope of the tool, null for an unknown tool
	 * @param args The arguments
	 */
	public void toolCall(String name, McpToolScope toolScope, Map<String,Object> args) {
		this.toolName = name;
		this.scope = toolScope;
		this.arguments = args == null ? null : boundedMap(args);
		Object actionValue = args == null ? null : args.get("action");
		this.action = actionValue == null ? null : actionValue.toString();
	}

	private static Map<String,Object> boundedMap(Map<?,?> map) {
		Map<String,Object> copy = new LinkedHashMap<>();
		for(Map.Entry<?,?> entry:map.entrySet()) {
			copy.put(String.valueOf(entry.getKey()), bounded(entry.getValue()));
		}
		return copy;
	}

	private static Object bounded(Object value) {
		if(value instanceof String string) {
			return boundedString(string);
		}
		if(value instanceof Map<?,?> map) {
			return boundedMap(map);
		}
		if(value instanceof Collection<?> collection) {
			List<Object> copy = new ArrayList<>(collection.size());
			for(Object element:collection) {
				copy.add(bounded(element));
			}
			return copy;
		}
		return value;
	}

	/**
	 * @param value A string argument
	 * @return The value, or its first {@link #MAX_ARGUMENT_LENGTH} characters
	 *         and the original length if it is longer
	 */
	static String boundedString(String value) {
		if(value.length() <= MAX_ARGUMENT_LENGTH) {
			return value;
		}
		int cut = MAX_ARGUMENT_LENGTH;
		// do not split a surrogate pair
		if(Character.isHighSurrogate(value.charAt(cut - 1))) {
			cut--;
		}
		return value.substring(0, cut) + " \u2026 [" + value.length() + " chars]";
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public void setOutcome(Outcome outcome) {
		this.outcome = outcome;
	}

	/**
	 * @return The reference logged with a server error, for support
	 */
	public String getErrorReference() {
		return errorReference;
	}

	public void setErrorReference(String errorReference) {
		this.errorReference = errorReference;
	}

	/**
	 * @return true for tools/list, initialize, ping and the calls of read tools
	 *         that succeeded; false for write tools and for every failed call
	 */
	public boolean isReadOperation() {
		if(outcome != Outcome.success) {
			return false;
		}
		if(toolName == null) {
			return true;
		}
		return scope == null || !scope.isWrite();
	}

	/**
	 * @return The HTTP-like status stored in the audit row
	 */
	public int getAuditStatus() {
		return switch(outcome) {
			case success -> 200;
			case toolError -> 422;
			case denied -> 403;
			case serverError -> 500;
		};
	}
}
