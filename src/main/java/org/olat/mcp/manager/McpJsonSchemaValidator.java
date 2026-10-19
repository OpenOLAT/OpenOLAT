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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Validates tool arguments against the input schema with messages in
 * English. The default validator of the SDK formats its messages in the
 * locale of the JVM, for example German messages on a German server.
 * The compiled schemas are cached per tool, the number of tools is small and
 * fixed per deployment.
 * <p>
 * A valid response carries no JSON of the structured content. The SDK 2.0.1
 * reads it only on the output validation of a tool result without content
 * (McpStatelessAsyncServer and McpAsyncServer, to build a text content), and
 * McpToolRegistry always adds a text content. The input validation
 * (ToolInputValidator) and the client read only valid() and errorMessage().
 * Serializing the arguments would copy an upload of several megabytes
 * (file content as base64) once more for nothing.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpJsonSchemaValidator implements JsonSchemaValidator {

	private static final Logger log = Tracing.createLoggerFor(McpJsonSchemaValidator.class);

	private final ObjectMapper objectMapper;
	private final SchemaRegistry registry;
	private final Map<String,Schema> schemaCache = new ConcurrentHashMap<>();

	public McpJsonSchemaValidator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
	}

	@Override
	public ValidationResponse validate(Map<String,Object> schema, Object structuredContent) {
		try {
			Schema compiled = getOrCompile(schema);
			JsonNode node = objectMapper.valueToTree(structuredContent);
			List<Error> errors = compiled.validate(node, context -> context.executionConfig(config -> config.locale(Locale.ENGLISH)));
			if(errors.isEmpty()) {
				return ValidationResponse.asValid(null);
			}
			String message = errors.stream()
					.map(error -> error.getInstanceLocation() + ": " + error.getMessage())
					.collect(Collectors.joining("; "));
			return ValidationResponse.asInvalid("Arguments do not match the input schema of the tool: " + message);
		} catch (JsonProcessingException e) {
			log.error("", e);
			return ValidationResponse.asInvalid("Arguments cannot be serialized: " + e.getOriginalMessage());
		} catch (RuntimeException e) {
			log.error("", e);
			return ValidationResponse.asInvalid("Arguments cannot be validated: " + e.getMessage());
		}
	}

	private Schema getOrCompile(Map<String,Object> schema) throws JsonProcessingException {
		String key = objectMapper.writeValueAsString(schema);
		Schema compiled = schemaCache.get(key);
		if(compiled == null) {
			compiled = registry.getSchema(key);
			schemaCache.putIfAbsent(key, compiled);
		}
		return compiled;
	}
}
