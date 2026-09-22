/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi.audit.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Replaces the values of the secret keys of a JSON body or of a query string
 * before the audit log stores them.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditMasking {
	
	public static final String MASK = "***";
	public static final Set<String> SECRET_KEYS = Set.of("password", "credential", "secret", "clientsecret");
	
	private static final ObjectMapper mapper = new ObjectMapper();
	/** Used when the body is not valid JSON, typically because it was truncated */
	private static final Pattern secretPattern = Pattern.compile(
			"\"(password|credential|secret|clientSecret)\"\\s*:\\s*\"(?:[^\"\\\\]|\\\\.)*\"", Pattern.CASE_INSENSITIVE);
	
	private ApiAuditMasking() {
		//
	}
	
	/**
	 * @param json The request body
	 * @return The body with the values of the secret keys replaced by {@link #MASK}
	 */
	public static String maskJson(String json) {
		if(json == null || json.isEmpty()) {
			return json;
		}
		
		try {
			JsonNode root = mapper.readTree(json);
			maskNode(root);
			return mapper.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			return secretPattern.matcher(json).replaceAll(match -> "\"" + match.group(1) + "\":\"" + MASK + "\"");
		}
	}
	
	private static void maskNode(JsonNode node) {
		if(node instanceof ObjectNode object) {
			List<String> names = new ArrayList<>();
			object.fieldNames().forEachRemaining(names::add);
			for(String name:names) {
				if(SECRET_KEYS.contains(name.toLowerCase(Locale.ROOT))) {
					object.put(name, MASK);
				} else {
					maskNode(object.get(name));
				}
			}
		} else if(node instanceof ArrayNode array) {
			for(JsonNode child:array) {
				maskNode(child);
			}
		}
	}
	
	/**
	 * @param query The query string of the request
	 * @return The query with the values of the secret parameters replaced by {@link #MASK}
	 */
	public static String maskQuery(String query) {
		if(query == null || query.isEmpty()) {
			return query;
		}
		
		StringBuilder sb = new StringBuilder(query.length());
		for(String pair:query.split("&")) {
			if(sb.length() > 0) {
				sb.append('&');
			}
			int separator = pair.indexOf('=');
			String key = separator < 0 ? pair : pair.substring(0, separator);
			if(separator >= 0 && SECRET_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
				sb.append(key).append('=').append(MASK);
			} else {
				sb.append(pair);
			}
		}
		return sb.toString();
	}
}
