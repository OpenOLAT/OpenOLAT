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

import java.util.List;

/**
 * The result of a tool call, independent of the MCP SDK. A successful result
 * carries the structured content (a map or a list of maps that matches the
 * output schema) and a short text rendering for clients that ignore
 * structured output. An error result carries a message the model can act on:
 * it names the object and the missing permission or the invalid value, never
 * a stack trace.
 *
 * @param structuredContent The structured content, null for an error
 * @param text The text block, never null
 * @param error true for a tool execution error
 * @param attachments Binary parts of a successful result, never null
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public record McpToolResult(Object structuredContent, String text, boolean error, List<McpBinaryContent> attachments) {

	public McpToolResult {
		attachments = attachments == null ? List.of() : List.copyOf(attachments);
	}

	public static McpToolResult of(Object structuredContent, String text) {
		return new McpToolResult(structuredContent, text, false, List.of());
	}

	/**
	 * @param structuredContent The structured content
	 * @param text The text content
	 * @param attachments Binary parts, images are sent as image content
	 * @return A successful result with binary parts
	 */
	public static McpToolResult of(Object structuredContent, String text, List<McpBinaryContent> attachments) {
		return new McpToolResult(structuredContent, text, false, attachments);
	}

	public static McpToolResult error(String message) {
		return new McpToolResult(null, message, true, List.of());
	}
}
