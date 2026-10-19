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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;

/**
 * Helpers every tool uses: argument access with defaults, bounded limits,
 * ISO dates, business path URLs and list results. Keeps the tools free of
 * boilerplate and uniform in their wire format.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public final class McpToolSupport {

	public static final int DEFAULT_LIMIT = 50;
	public static final int MAX_LIMIT = 200;
	/** Maximum lines of the text rendering of a list */
	public static final int TEXT_LINES_MAX = 20;

	private McpToolSupport() {
		//
	}

	/**
	 * Reads a text argument the way a text field of the UI reads its input
	 * (AbstractTextElement.setValue): the value is trimmed and the characters
	 * XML 1.0 does not allow (NUL and other control characters) are removed
	 * with {@link StringHelper#cleanUTF8ForXml(String)}.
	 *
	 * @param arguments The validated arguments
	 * @param name The argument name
	 * @param defaultValue The value when absent or blank
	 * @return The string value trimmed and cleaned, or the default
	 */
	public static String string(Map<String,Object> arguments, String name, String defaultValue) {
		Object value = arguments == null ? null : arguments.get(name);
		if(value == null) {
			return defaultValue;
		}
		String string = clean(value.toString().trim());
		return StringHelper.containsNonWhitespace(string) ? string : defaultValue;
	}

	/**
	 * Reads a text argument without trimming, for texts where leading or
	 * trailing white space is content, for example the markdown of a note
	 * (the UI sets preventTrim on such fields). The characters XML 1.0 does
	 * not allow are removed as in {@link #string(Map, String, String)}.
	 *
	 * @param arguments The validated arguments
	 * @param name The argument name
	 * @param defaultValue The value when absent or blank
	 * @return The string value cleaned but not trimmed, or the default
	 */
	public static String stringUntrimmed(Map<String,Object> arguments, String name, String defaultValue) {
		Object value = arguments == null ? null : arguments.get(name);
		if(value == null) {
			return defaultValue;
		}
		String string = clean(value.toString());
		return StringHelper.containsNonWhitespace(string) ? string : defaultValue;
	}

	/**
	 * @param value A text
	 * @return The text without the characters XML 1.0 does not allow
	 */
	private static String clean(String value) {
		return StringHelper.cleanUTF8ForXml(value);
	}

	/**
	 * @param arguments The validated arguments
	 * @param name The argument name
	 * @return The value as Long, null when absent or not a number
	 */
	public static Long longValue(Map<String,Object> arguments, String name) {
		Object value = arguments == null ? null : arguments.get(name);
		if(value instanceof Number number) {
			return Long.valueOf(number.longValue());
		}
		if(value instanceof String string && StringHelper.isLong(string.trim())) {
			return Long.valueOf(string.trim());
		}
		return null;
	}

	public static boolean bool(Map<String,Object> arguments, String name, boolean defaultValue) {
		Object value = arguments == null ? null : arguments.get(name);
		if(value instanceof Boolean bool) {
			return bool.booleanValue();
		}
		if(value instanceof String string) {
			return "true".equalsIgnoreCase(string.trim());
		}
		return defaultValue;
	}

	/**
	 * The values are trimmed and cleaned as in {@link #string(Map, String, String)},
	 * blank values are dropped.
	 *
	 * @param arguments The validated arguments
	 * @param name The argument name
	 * @return The string values of an array argument, empty when absent
	 */
	public static List<String> strings(Map<String,Object> arguments, String name) {
		Object value = arguments == null ? null : arguments.get(name);
		List<String> strings = new ArrayList<>();
		if(value instanceof Collection<?> collection) {
			for(Object item:collection) {
				addCleaned(strings, item);
			}
		} else if(value instanceof String) {
			addCleaned(strings, value);
		}
		return strings;
	}

	private static void addCleaned(List<String> strings, Object item) {
		if(item != null) {
			String string = clean(item.toString().trim());
			if(StringHelper.containsNonWhitespace(string)) {
				strings.add(string);
			}
		}
	}

	/**
	 * Reads a tag list as the tag selection of the UI does (TagSelectionImpl):
	 * the values are trimmed and cleaned, blank values are dropped and values
	 * that differ only in case are removed, the first spelling wins. Then the
	 * limits are checked.
	 * <p>
	 * The TagService does not match case-insensitively:
	 * TagService.getOrCreateTags looks the display names up with an exact
	 * <code>in</code> query, so on PostgreSQL "urgent" creates a new tag next
	 * to "Urgent", and on MySQL the case-insensitive collation returns
	 * "Urgent" twice. The caller must therefore map the result to the tags
	 * of the context with {@link #matchTags(List, Collection)} before it
	 * writes, as the UI does (TagSelectionController.isTagExists).
	 *
	 * @param arguments The validated arguments
	 * @param name The argument name, e.g. tags
	 * @param maxItems The maximum number of tags after the de-duplication
	 * @param maxLength The maximum number of characters of one tag
	 * @return The tags, empty when absent
	 * @throws McpInvalidArgumentException for a longer list or tag
	 */
	public static List<String> tags(Map<String,Object> arguments, String name, int maxItems, int maxLength) {
		Map<String,String> tags = new LinkedHashMap<>();
		for(String tag:strings(arguments, name)) {
			tags.putIfAbsent(tag.toLowerCase(), tag);
		}
		List<String> values = new ArrayList<>(tags.values());
		checkList(name, values, maxItems, maxLength);
		return values;
	}

	/**
	 * Replaces every tag by the spelling of an existing tag with the same
	 * name regardless of case, so the tool reuses the tag the UI would offer
	 * instead of creating a parallel tag that differs only in case. The
	 * existing tags are the tags of the context as the tag selection of the
	 * UI lists them, e.g. ProjectService.getTagInfos or ToDoService.getTagInfos.
	 *
	 * @param tags The tags of {@link #tags(Map, String, int, int)}
	 * @param existingDisplayNames The display names of the existing tags of the context
	 * @return The tags with the spelling of the existing tags, in the order of the tags, without duplicates
	 */
	public static List<String> matchTags(List<String> tags, Collection<String> existingDisplayNames) {
		Map<String,String> existing = new LinkedHashMap<>();
		for(String displayName:existingDisplayNames) {
			if(displayName != null) {
				existing.putIfAbsent(displayName.toLowerCase(), displayName);
			}
		}
		Map<String,String> matched = new LinkedHashMap<>();
		for(String tag:tags) {
			String lowerCase = tag.toLowerCase();
			matched.putIfAbsent(lowerCase, existing.getOrDefault(lowerCase, tag));
		}
		return new ArrayList<>(matched.values());
	}

	/**
	 * The values of an argument that maps to an enumeration of OpenOlat are
	 * the names of the enum constants as the code spells them. The schema
	 * enum and the parsing derive from the constants, the tools have no own
	 * vocabulary for them.
	 *
	 * @param values Enum constants
	 * @return The names of the constants in the given order
	 */
	public static List<String> names(Collection<? extends Enum<?>> values) {
		return values.stream().map(Enum::name).toList();
	}

	/**
	 * @param values Enum constants
	 * @return The names of the constants in the given order
	 */
	@SafeVarargs
	public static <E extends Enum<E>> List<String> names(E... values) {
		return names(Arrays.asList(values));
	}

	/**
	 * @param allowed The constants the argument accepts
	 * @param value The value of the argument
	 * @return The constant of the allowed ones with the value as name, null if none matches
	 */
	public static <E extends Enum<E>> E enumValue(Collection<E> allowed, String value) {
		if(value == null) {
			return null;
		}
		for(E constant:allowed) {
			if(constant.name().equals(value)) {
				return constant;
			}
		}
		return null;
	}

	/**
	 * @param name The argument name
	 * @param value The value that matches no constant
	 * @param allowed The constants the argument accepts
	 * @return The error text: Unknown name 'value'. Use one of: a, b.
	 */
	public static String unknownValue(String name, String value, Collection<? extends Enum<?>> allowed) {
		return "Unknown " + name + " '" + value + "'. Use one of: " + String.join(", ", names(allowed)) + ".";
	}

	/**
	 * @param arguments The validated arguments
	 * @return The limit argument bounded to 1 .. {@link #MAX_LIMIT}, {@link #DEFAULT_LIMIT} when absent
	 */
	public static int limit(Map<String,Object> arguments) {
		Long value = longValue(arguments, "limit");
		if(value == null) {
			return DEFAULT_LIMIT;
		}
		return (int)Math.max(1L, Math.min(MAX_LIMIT, value.longValue()));
	}

	/**
	 * @param arguments The validated arguments
	 * @param name The argument name
	 * @return The date of an ISO date argument (yyyy-MM-dd) at the start of the day in the server zone, null when absent
	 * @throws McpInvalidArgumentException for a value that is not an ISO date
	 */
	public static Date date(Map<String,Object> arguments, String name) {
		String value = string(arguments, name, null);
		if(value == null) {
			return null;
		}
		try {
			LocalDate date = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
			return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
		} catch (DateTimeParseException e) {
			throw new McpInvalidArgumentException("The argument " + name + " must be an ISO date (yyyy-MM-dd), got: " + value);
		}
	}

	/**
	 * For text fields the output schema types as string: an entity can hold
	 * null in a column the UI treats as mandatory (for example the title of
	 * a project whose creation failed), and null fails the output validation
	 * of the SDK for the whole result.
	 *
	 * @param value A text, may be null
	 * @return The text, an empty string for null
	 */
	public static String text(String value) {
		return value == null ? "" : value;
	}

	/**
	 * @param date A date
	 * @return The ISO date-time with offset, null for null
	 */
	public static String iso(Date date) {
		return date == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date.toInstant().atZone(ZoneId.systemDefault()));
	}

	/**
	 * @param date A date
	 * @return The ISO date (yyyy-MM-dd) in the server zone, null for null
	 */
	public static String isoDate(Date date) {
		return date == null ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(date.toInstant().atZone(ZoneId.systemDefault()));
	}

	/**
	 * @param segments Pairs of type and key, e.g. "RepositoryEntry", 123L, "CourseNode", "456"
	 * @return The absolute URL into the OpenOlat UI, /auth/RepositoryEntry/123/CourseNode/456
	 */
	public static String url(Object... segments) {
		StringBuilder sb = new StringBuilder(Settings.getServerContextPathURI()).append("/auth");
		for(Object segment:segments) {
			sb.append('/').append(segment);
		}
		return sb.toString();
	}
	
	/**
	 * @param identity An identity
	 * @return The username the user sees and types: the nick name, else the identity name
	 */
	public static String username(Identity identity) {
		String nickName = identity.getUser() == null ? null : identity.getUser().getNickName();
		return StringHelper.containsNonWhitespace(nickName) ? nickName : identity.getName();
	}

	/**
	 * @param candidate An identity
	 * @param value A username, nick name or email as the model passes it
	 * @return true if the value names the identity, case insensitive
	 */
	public static boolean matchesUser(Identity candidate, String value) {
		if(value.equalsIgnoreCase(candidate.getName())) {
			return true;
		}
		User user = candidate.getUser();
		return user != null
				&& (value.equalsIgnoreCase(user.getNickName()) || value.equalsIgnoreCase(user.getEmail()));
	}

	/**
	 * Resolves usernames or emails against a bounded candidate list, so the
	 * tool is not a user enumeration oracle. Every value must match one
	 * candidate.
	 *
	 * @param role The role name for the error message, e.g. assignee or member
	 * @param objectDescription The object for the error message, e.g. "this ToDo"
	 * @param values The usernames or emails
	 * @param candidates The allowed identities
	 * @return The identities in the order of the values, without duplicates
	 * @throws McpInvalidArgumentException for a value without candidate
	 */
	public static List<Identity> resolveIdentities(String role, String objectDescription, List<String> values, Collection<Identity> candidates) {
		Map<Long,Identity> resolved = new LinkedHashMap<>();
		for(String value:values) {
			Identity identity = candidates.stream()
					.filter(candidate -> matchesUser(candidate, value))
					.findFirst()
					.orElseThrow(() -> new McpInvalidArgumentException(value + " is not a valid " + role + " for " + objectDescription + "."));
			resolved.put(identity.getKey(), identity);
		}
		return new ArrayList<>(resolved.values());
	}

	/**
	 * @param candidate An identity
	 * @param value A name as the model passes it
	 * @return true if the value is the first and the last name of the
	 *         identity, separated by one space, case insensitive (the name
	 *         the user search of the UI finds, as IdentityDAO.findByNames
	 *         compares it)
	 */
	public static boolean matchesDisplayName(Identity candidate, String value) {
		User user = candidate.getUser();
		if(user == null || value == null) {
			return false;
		}
		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		return value.equalsIgnoreCase((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName));
	}

	/**
	 * Resolves the values against a bounded candidate list, as
	 * {@link #resolveIdentities(String, String, List, Collection)}, with a
	 * caller-specific rule for the unique names and the display name
	 * ("First Last") as a second form. A value is first compared with the
	 * unique names of the candidates; without a match it is compared with
	 * the display names, and a display name that more than one candidate
	 * carries is refused, the tool never guesses the person.
	 *
	 * @param role The role name for the error message, e.g. assignee
	 * @param objectDescription The object for the error message, e.g. "this ToDo"
	 * @param values The names
	 * @param candidates The allowed identities
	 * @param uniqueNameMatcher Decides if a value is a unique name (username, nick name, email) of a candidate
	 * @return The identities in the order of the values, without duplicates
	 * @throws McpInvalidArgumentException for a value without candidate or with more than one candidate
	 */
	public static List<Identity> resolveIdentities(String role, String objectDescription, List<String> values,
			Collection<Identity> candidates, BiPredicate<Identity,String> uniqueNameMatcher) {
		Map<Long,Identity> resolved = new LinkedHashMap<>();
		for(String value:values) {
			Identity identity = candidates.stream()
					.filter(candidate -> uniqueNameMatcher.test(candidate, value))
					.findFirst()
					.orElse(null);
			if(identity == null) {
				List<Identity> byDisplayName = candidates.stream()
						.filter(candidate -> matchesDisplayName(candidate, value))
						.toList();
				if(byDisplayName.size() > 1) {
					throw new McpInvalidArgumentException(value + " is ambiguous: more than one user has this name. "
							+ "Ask the user which person is meant, a unique " + role + " for " + objectDescription + " is needed.");
				}
				if(byDisplayName.isEmpty()) {
					throw new McpInvalidArgumentException(value + " is not a valid " + role + " for " + objectDescription + ".");
				}
				identity = byDisplayName.get(0);
			}
			resolved.put(identity.getKey(), identity);
		}
		return new ArrayList<>(resolved.values());
	}

	/**
	 * @param name The argument name
	 * @param value The value or null
	 * @param max The maximum number of characters
	 * @throws McpInvalidArgumentException for a longer value
	 */
	public static void checkLength(String name, String value, int max) {
		if(value != null && value.length() > max) {
			throw new McpInvalidArgumentException("The argument " + name + " is longer than " + max + " characters.");
		}
	}

	/**
	 * @param name The argument name
	 * @param values The values
	 * @param maxItems The maximum number of items
	 * @param maxLength The maximum number of characters of one item
	 * @throws McpInvalidArgumentException for a longer list or item
	 */
	public static void checkList(String name, List<String> values, int maxItems, int maxLength) {
		if(values.size() > maxItems) {
			throw new McpInvalidArgumentException("The argument " + name + " has more than " + maxItems + " items.");
		}
		for(String value:values) {
			if(value.length() > maxLength) {
				throw new McpInvalidArgumentException("An item of the argument " + name + " is longer than " + maxLength + " characters.");
			}
		}
	}

	/**
	 * The uniform list result: items, total and the truncated flag.
	 *
	 * @param items The items of this page, at most limit entries
	 * @param total The total number of matching items, or the size of items if unknown
	 * @param limit The requested limit
	 * @return The structured content
	 */
	public static Map<String,Object> listResult(List<? extends Map<String,Object>> items, int total, int limit) {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("items", items);
		result.put("total", Integer.valueOf(total));
		result.put("truncated", Boolean.valueOf(total > items.size() || items.size() > limit));
		return result;
	}

	/**
	 * @param lines One line per item
	 * @param total The total number of items
	 * @param noun The plural noun for the summary line, e.g. "courses"
	 * @return The text rendering, at most {@link #TEXT_LINES_MAX} lines plus a summary
	 */
	public static String listText(List<String> lines, int total, String noun) {
		StringBuilder sb = new StringBuilder();
		sb.append(total).append(' ').append(noun);
		if(lines.isEmpty()) {
			return sb.append('.').toString();
		}
		sb.append(':');
		int shown = Math.min(lines.size(), TEXT_LINES_MAX);
		for(int i=0; i<shown; i++) {
			sb.append('\n').append("- ").append(lines.get(i));
		}
		if(lines.size() > shown) {
			sb.append("\n- ... ").append(lines.size() - shown).append(" more in structuredContent");
		}
		return sb.toString();
	}

	/**
	 * The uniform object result schema for lists.
	 *
	 * @param itemSchema The schema of one item
	 * @return The output schema with items, total and truncated
	 */
	public static Map<String,Object> listSchema(Map<String,Object> itemSchema) {
		return Map.of("type", "object",
				"properties", Map.of(
						"items", Map.of("type", "array", "items", itemSchema),
						"total", Map.of("type", "integer", "description", "Number of matching items"),
						"truncated", Map.of("type", "boolean", "description", "true if more items exist than returned")),
				"required", List.of("items", "total", "truncated"));
	}

	/**
	 * The schema of the person of the wire format: username and display name.
	 *
	 * @return The schema
	 */
	public static Map<String,Object> personSchema() {
		return Map.of("type", "object", "properties", Map.of(
				"username", Map.of("type", "string"),
				"displayName", Map.of("type", "string")));
	}

	/**
	 * The output schema of a modify tool: the item with the few fields that
	 * identify the object after the change. The query tool returns the full
	 * item.
	 *
	 * @param querySchema The item schema of the query tool of the object
	 * @param fields The fields of the minimal item, properties of the item schema
	 * @param required The fields every result carries
	 * @return The output schema with the property item
	 */
	public static Map<String,Object> modifyOutputSchema(Map<String,Object> querySchema, List<String> fields, List<String> required) {
		@SuppressWarnings("unchecked")
		Map<String,Object> queryProperties = (Map<String,Object>)querySchema.get("properties");
		Map<String,Object> properties = new LinkedHashMap<>();
		for(String field:fields) {
			Object property = queryProperties.get(field);
			if(property == null) {
				throw new IllegalArgumentException("No property " + field + " in the item schema");
			}
			properties.put(field, property);
		}
		Map<String,Object> itemSchema = Map.of("type", "object", "properties", properties, "required", required);
		return Map.of("type", "object", "properties", Map.of("item", itemSchema), "required", List.of("item"));
	}

	/**
	 * @param item The full item of the object
	 * @param fields The fields of the minimal item
	 * @return The structured content of a modify tool: the item with the present fields
	 */
	public static Map<String,Object> modifyResult(Map<String,Object> item, List<String> fields) {
		Map<String,Object> minimalItem = new LinkedHashMap<>();
		for(String field:fields) {
			Object value = item.get(field);
			if(value != null) {
				minimalItem.put(field, value);
			}
		}
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", minimalItem);
		return result;
	}

	/**
	 * @param queryToolName The query tool of the object, e.g. note_query
	 * @return The hint at the end of the text of a modify tool
	 */
	public static String fullItemHint(String queryToolName) {
		return " Call " + queryToolName + " get for the full item.";
	}

	/**
	 * The uniform output schema of a query tool: items, total and truncated
	 * for a list, item for a single object. The item schema is defined once
	 * under $defs.
	 *
	 * @param itemSchema The schema of one item
	 * @param defs Further named sub-schemas of the item, e.g. person, see {@link #withDefs(Map, Map)}
	 * @return The output schema
	 */
	public static Map<String,Object> queryOutputSchema(Map<String,Object> itemSchema, Map<String,Map<String,Object>> defs) {
		Map<String,Object> listSchema = listSchema(itemSchema);
		@SuppressWarnings("unchecked")
		Map<String,Object> properties = new LinkedHashMap<>((Map<String,Object>)listSchema.get("properties"));
		properties.put("item", itemSchema);
		// list returns items, total and truncated, get returns item
		Map<String,Map<String,Object>> allDefs = new LinkedHashMap<>();
		allDefs.put("item", itemSchema);
		allDefs.putAll(defs);
		return withDefs(Map.of("type", "object", "properties", properties), allDefs);
	}

	/**
	 * Defines repeated sub-schemas once: every sub-schema of the schema equal
	 * to one of the named schemas becomes <code>{"$ref": "#/$defs/name"}</code>
	 * and the root gets a <code>$defs</code> block with the used schemas
	 * (JSON Schema 2020-12). The named schemas themselves are compacted the
	 * same way, a person inside an item becomes a reference too. Only for
	 * output schemas: the clients differ in their $ref support for inputs.
	 *
	 * @param schema The schema
	 * @param defs The named sub-schemas, compared by equality
	 * @return The schema with $defs, the schema unchanged if no sub-schema matches
	 */
	public static Map<String,Object> withDefs(Map<String,Object> schema, Map<String,Map<String,Object>> defs) {
		Set<String> used = new LinkedHashSet<>();
		@SuppressWarnings("unchecked")
		Map<String,Object> root = (Map<String,Object>)replaceChildren(schema, defs, used);
		Map<String,Object> compactedDefs = new LinkedHashMap<>();
		boolean added = true;
		while(added) {
			added = false;
			for(String name:List.copyOf(used)) {
				if(!compactedDefs.containsKey(name)) {
					compactedDefs.put(name, replaceChildren(defs.get(name), defs, used));
					added = true;
				}
			}
		}
		if(compactedDefs.isEmpty()) {
			return schema;
		}
		Map<String,Object> orderedDefs = new LinkedHashMap<>();
		for(String name:defs.keySet()) {
			if(compactedDefs.containsKey(name)) {
				orderedDefs.put(name, compactedDefs.get(name));
			}
		}
		Map<String,Object> result = new LinkedHashMap<>(root);
		result.put("$defs", orderedDefs);
		return result;
	}

	private static Object replace(Object node, Map<String,Map<String,Object>> defs, Set<String> used) {
		if(node instanceof Map<?,?> map) {
			for(Map.Entry<String,Map<String,Object>> def:defs.entrySet()) {
				if(def.getValue().equals(map)) {
					used.add(def.getKey());
					return Map.of("$ref", "#/$defs/" + def.getKey());
				}
			}
		}
		return replaceChildren(node, defs, used);
	}

	private static Object replaceChildren(Object node, Map<String,Map<String,Object>> defs, Set<String> used) {
		if(node instanceof Map<?,?> map) {
			Map<String,Object> copy = new LinkedHashMap<>();
			for(Map.Entry<?,?> entry:map.entrySet()) {
				copy.put(entry.getKey().toString(), replace(entry.getValue(), defs, used));
			}
			return copy;
		}
		if(node instanceof List<?> list) {
			List<Object> copy = new ArrayList<>(list.size());
			for(Object element:list) {
				copy.add(replace(element, defs, used));
			}
			return copy;
		}
		return node;
	}
}
