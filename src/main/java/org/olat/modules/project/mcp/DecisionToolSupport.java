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
package org.olat.modules.project.mcp;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionInfo;
import org.olat.modules.project.ProjProject;

/**
 * The decision of the wire format, shared by decision_query and
 * decision_modify: the item, its line in the text result and its schema.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
class DecisionToolSupport {

	private DecisionToolSupport() {
		//
	}

	/**
	 * Parses the decision date. The date chooser of
	 * ProjDecisionContentEditController has a time, so the argument takes an
	 * ISO date-time (with offset or local in the server zone); a bare ISO
	 * date stays accepted and means the start of the day.
	 *
	 * @param arguments The arguments
	 * @param name The argument name
	 * @return The date, null when absent or an empty string
	 * @throws McpInvalidArgumentException for a value that is neither a date nor a date-time
	 */
	static Date decisionDate(Map<String,Object> arguments, String name) {
		String value = McpToolSupport.string(arguments, name, null);
		if(value == null) {
			return null;
		}
		try {
			LocalDate date = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
			return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
		} catch (DateTimeParseException e) {
			// a date-time
		}
		try {
			return AppointmentToolSupport.dateTime(arguments, name, false);
		} catch (McpInvalidArgumentException e) {
			throw new McpInvalidArgumentException("The argument " + name
					+ " must be an ISO date-time (yyyy-MM-ddTHH:mm, optional offset) or an ISO date (yyyy-MM-dd), got: " + value);
		}
	}

	/**
	 * @param support The shared project tool support for persons and links
	 * @param info The decision with members and tags
	 * @param full true for the details and the links (get and every modify result),
	 * 		false for a preview of the details (list)
	 * @return The item
	 */
	static Map<String,Object> decisionItem(ProjectToolSupport support, ProjDecisionInfo info, boolean full) {
		ProjDecision decision = info.getDecision();
		ProjArtefact artefact = decision.getArtefact();
		ProjProject project = artefact.getProject();
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", decision.getKey());
		item.put("projectKey", project.getKey());
		if(StringHelper.containsNonWhitespace(decision.getTitle())) {
			item.put("title", decision.getTitle());
		}
		String details = decision.getDetails();
		boolean hasDetails = StringHelper.containsNonWhitespace(details);
		if(full) {
			if(hasDetails) {
				item.put("details", details);
			}
		} else {
			if(hasDetails) {
				item.put("preview", ProjectToolSupport.preview(details));
			}
			item.put("detailsLength", Integer.valueOf(hasDetails ? details.length() : 0));
		}
		if(decision.getDecisionDate() != null) {
			// date-time: the date chooser has a time, a write-back of the read value keeps it
			item.put("decisionDate", McpToolSupport.iso(decision.getDecisionDate()));
		}
		item.put("status", artefact.getStatus().name());
		item.put("created", McpToolSupport.iso(artefact.getCreationDate()));
		item.put("lastModified", McpToolSupport.iso(artefact.getContentModifiedDate()));
		item.put("creator", support.toPerson(artefact.getCreator()));
		item.put("tags", ProjectToolSupport.tagNames(info));
		item.put("members", support.toPersons(info.getMembers()));
		if(full) {
			item.put("links", support.links(artefact));
		}
		item.put("url", ProjectToolSupport.url(project, ProjDecision.TYPE, decision.getKey()));
		return item;
	}

	static String decisionLine(Map<String,Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append(item.containsKey("title") ? item.get("title") : "(no title)").append(" (key ").append(item.get("key"));
		sb.append(", decided ").append(item.containsKey("decisionDate") ? item.get("decisionDate") : "(no date)");
		sb.append(", ").append(item.get("status"));
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>)item.get("tags");
		if(!tags.isEmpty()) {
			sb.append(", tags ").append(String.join(", ", tags));
		}
		return sb.append(") ").append(item.get("url")).toString();
	}

	static Map<String,Object> decisionItemSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("key", Map.of("type", "integer", "description", "The decision key, the decisionKey of get, update and delete"));
		properties.put("projectKey", Map.of("type", "integer"));
		properties.put("title", Map.of("type", "string", "description", "absent for a decision without title"));
		properties.put("preview", Map.of("type", "string", "description", "list only: the first " + ProjectToolSupport.PREVIEW_LENGTH
				+ " characters of the details, whitespace collapsed; absent when the details are empty"));
		properties.put("detailsLength", Map.of("type", "integer", "description", "list only: the length of the details in characters, 0 when empty"));
		properties.put("details", Map.of("type", "string",
				"description", "get, create, update and delete: plain text as entered in the text area of OpenOlat, line breaks kept; absent when empty"));
		properties.put("decisionDate", Map.of("type", "string", "format", "date-time",
				"description", "when the decision was taken, ISO date-time with offset; absent for a decision without date"));
		properties.put("status", ProjectToolSupport.artefactStatusSchema());
		properties.put("created", Map.of("type", "string", "format", "date-time"));
		properties.put("lastModified", Map.of("type", "string", "format", "date-time", "description", "last change of title, details or date"));
		properties.put("creator", McpToolSupport.personSchema());
		properties.put("tags", Map.of("type", "array", "items", Map.of("type", "string")));
		properties.put("members", Map.of("type", "array", "items", McpToolSupport.personSchema(),
				"description", "The project members involved in the decision"));
		properties.put("links", Map.of("type", "array", "items", ProjectToolSupport.linkSchema(),
				"description", "get, create, update and delete: the linked artefacts of the project"));
		properties.put("url", Map.of("type", "string", "description", "Link to the decision in OpenOlat"));
		return Map.of("type", "object", "properties", properties,
				"required", List.of("key", "projectKey", "status", "created", "lastModified", "creator", "tags", "members", "url"));
	}
}
