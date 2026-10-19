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
 * The catalog entry of a tool, independent of the MCP SDK. The registry maps
 * it to the wire format. The description is prompt engineering: 3 to 4
 * sentences in English that tell the model what the tool returns, when to use
 * it and when not. Schemas are JSON Schema 2020-12 as nested maps.
 *
 * @param title Short human-readable title
 * @param objectTitle The plural English label of the object of the tool, e.g.
 *        Appointments; the query and the modify tool of one object carry the
 *        same label (row label of the profile editor)
 * @param description The description the model reads
 * @param inputSchema The JSON schema of the arguments, an object schema, never null
 * @param outputSchema The JSON schema of the structured result, may be null
 * @param destructive true if one of the actions deletes or overwrites data
 * @param idempotent true if a repeated call with the same arguments has no additional effect
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public record McpToolDefinition(String title, String objectTitle, String description, Map<String,Object> inputSchema,
		Map<String,Object> outputSchema, boolean destructive, boolean idempotent) {

	/** An input schema that accepts only the empty object */
	public static final Map<String,Object> NO_INPUT = Map.of("type", "object", "additionalProperties", Boolean.FALSE);

	public McpToolDefinition {
		if(inputSchema == null) {
			inputSchema = NO_INPUT;
		}
	}
}
