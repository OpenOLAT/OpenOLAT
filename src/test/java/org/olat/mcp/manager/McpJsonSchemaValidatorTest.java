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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.mcp.tool.McpToolSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpJsonSchemaValidatorTest {

	private static final Map<String,Object> SCHEMA = Map.of(
			"type", "object",
			"properties", Map.of(
					"message", Map.of("type", "string"),
					"limit", Map.of("type", "integer", "maximum", 200)),
			"required", List.of("message"),
			"additionalProperties", Boolean.FALSE);

	@Test
	public void validArguments() {
		McpJsonSchemaValidator validator = new McpJsonSchemaValidator(new ObjectMapper());
		JsonSchemaValidator.ValidationResponse response = validator.validate(SCHEMA, Map.of("message", "hello", "limit", 10));
		Assertions.assertThat(response.valid()).isTrue();
		// no serialized copy of the arguments, the SDK does not read it
		Assertions.assertThat(response.jsonStructuredOutput()).isNull();
	}

	@Test
	public void missingRequiredArgumentHasEnglishMessage() {
		Locale defaultLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.GERMAN);
			McpJsonSchemaValidator validator = new McpJsonSchemaValidator(new ObjectMapper());
			JsonSchemaValidator.ValidationResponse response = validator.validate(SCHEMA, Map.of("limit", 10));
			Assertions.assertThat(response.valid()).isFalse();
			Assertions.assertThat(response.errorMessage())
				.startsWith("Arguments do not match the input schema of the tool")
				.contains("message")
				.contains("required")
				.doesNotContain("erforderlich");
		} finally {
			Locale.setDefault(defaultLocale);
		}
	}

	@Test
	public void invalidValueAndUnknownProperty() {
		McpJsonSchemaValidator validator = new McpJsonSchemaValidator(new ObjectMapper());
		JsonSchemaValidator.ValidationResponse response = validator.validate(SCHEMA, Map.of("message", "x", "limit", 500, "foo", "bar"));
		Assertions.assertThat(response.valid()).isFalse();
		Assertions.assertThat(response.errorMessage()).contains("limit").contains("foo");
	}

	@Test
	public void localRefToDefs() {
		Map<String,Object> person = Map.of("type", "object",
				"properties", Map.of("username", Map.of("type", "string")),
				"required", List.of("username"));
		Map<String,Object> schema = Map.of(
				"type", "object",
				"properties", Map.of(
						"creator", Map.of("$ref", "#/$defs/person"),
						"members", Map.of("type", "array", "items", Map.of("$ref", "#/$defs/person"))),
				"$defs", Map.of("person", person));
		McpJsonSchemaValidator validator = new McpJsonSchemaValidator(new ObjectMapper());

		JsonSchemaValidator.ValidationResponse valid = validator.validate(schema,
				Map.of("creator", Map.of("username", "alice"), "members", List.of(Map.of("username", "bob"))));
		Assertions.assertThat(valid.valid()).isTrue();

		JsonSchemaValidator.ValidationResponse invalid = validator.validate(schema,
				Map.of("creator", Map.of("username", "alice"), "members", List.of(Map.of("username", Integer.valueOf(3)))));
		Assertions.assertThat(invalid.valid()).isFalse();
		Assertions.assertThat(invalid.errorMessage()).contains("/members/0/username");

		JsonSchemaValidator.ValidationResponse missing = validator.validate(schema, Map.of("creator", Map.of()));
		Assertions.assertThat(missing.valid()).isFalse();
		Assertions.assertThat(missing.errorMessage()).contains("username");
	}

	@Test
	public void queryOutputSchemaWithDefs() {
		Map<String,Object> itemSchema = Map.of("type", "object",
				"properties", Map.of(
						"key", Map.of("type", "integer"),
						"creator", McpToolSupport.personSchema(),
						"members", Map.of("type", "array", "items", McpToolSupport.personSchema())),
				"required", List.of("key"));
		Map<String,Object> schema = McpToolSupport.queryOutputSchema(itemSchema, Map.of("person", McpToolSupport.personSchema()));

		// item and person are defined once, every use is a reference
		@SuppressWarnings("unchecked")
		Map<String,Object> defs = (Map<String,Object>)schema.get("$defs");
		Assertions.assertThat(defs).containsOnlyKeys("item", "person");
		String json = schema.toString();
		Assertions.assertThat(json).containsOnlyOnce("displayName");
		@SuppressWarnings("unchecked")
		Map<String,Object> properties = (Map<String,Object>)schema.get("properties");
		Assertions.assertThat(properties.get("item")).isEqualTo(Map.of("$ref", "#/$defs/item"));

		McpJsonSchemaValidator validator = new McpJsonSchemaValidator(new ObjectMapper());
		Map<String,Object> item = Map.of("key", 1, "creator", Map.of("username", "alice", "displayName", "Alice"),
				"members", List.of(Map.of("username", "bob")));
		Assertions.assertThat(validator.validate(schema, Map.of("item", item)).valid()).isTrue();
		Assertions.assertThat(validator.validate(schema, Map.of("items", List.of(item), "total", 1, "truncated", Boolean.FALSE)).valid()).isTrue();

		JsonSchemaValidator.ValidationResponse invalid = validator.validate(schema,
				Map.of("items", List.of(Map.of("key", 2, "members", List.of(Map.of("username", Boolean.TRUE)))), "total", 1, "truncated", Boolean.FALSE));
		Assertions.assertThat(invalid.valid()).isFalse();
		Assertions.assertThat(invalid.errorMessage()).contains("/items/0/members/0/username");
		Assertions.assertThat(validator.validate(schema, Map.of("item", Map.of("creator", Map.of()))).valid()).isFalse();
	}
}
