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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.commons.calendar.ui.CalendarColors;
import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneInfo;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The milestone of the wire format, shared by milestone_query and
 * milestone_modify.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
class MilestoneToolSupport {

	/** The DB column o_proj_milestone.p_subject and the field of ProjMilestoneContentEditController */
	static final int SUBJECT_MAX_LENGTH = 256;
	static final int DESCRIPTION_MAX_LENGTH = 4000;
	/** The milestone status, the names of {@link ProjMilestoneStatus} (milestone.status.open, .achieved: Open, Accomplished) */
	static final List<ProjMilestoneStatus> MILESTONE_STATUS = List.of(ProjMilestoneStatus.values());
	static final String MILESTONE_STATUS_WORDING = "open (Open), achieved (Accomplished)";
	/** The colors of the color picker of ProjMilestoneContentEditController */
	static final List<String> COLORS = CalendarColors.getColorsList();

	@Autowired
	private ProjectToolSupport support;

	/**
	 * @param info The milestone with members and tags
	 * @param withLinks true for the links
	 * @return The item
	 */
	Map<String,Object> milestoneItem(ProjMilestoneInfo info, boolean withLinks) {
		ProjMilestone milestone = info.getMilestone();
		ProjArtefact artefact = milestone.getArtefact();
		ProjProject project = artefact.getProject();
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", milestone.getKey());
		item.put("projectKey", project.getKey());
		if(StringHelper.containsNonWhitespace(milestone.getSubject())) {
			item.put("subject", milestone.getSubject());
		}
		if(StringHelper.containsNonWhitespace(milestone.getDescription())) {
			item.put("description", milestone.getDescription());
		}
		ProjMilestoneStatus milestoneStatus = milestone.getStatus() == null ? ProjMilestoneStatus.open : milestone.getStatus();
		item.put("milestoneStatus", milestoneStatus.name());
		if(milestone.getDueDate() != null) {
			item.put("dueDate", McpToolSupport.isoDate(milestone.getDueDate()));
		}
		if(StringHelper.containsNonWhitespace(milestone.getColor())) {
			item.put("color", milestone.getColor());
		}
		item.put("status", artefact.getStatus().name());
		item.put("created", McpToolSupport.iso(artefact.getCreationDate()));
		item.put("lastModified", McpToolSupport.iso(artefact.getContentModifiedDate()));
		item.put("creator", support.toPerson(artefact.getCreator()));
		item.put("tags", ProjectToolSupport.tagNames(info));
		item.put("members", support.toPersons(info.getMembers()));
		if(withLinks) {
			item.put("links", support.links(artefact));
		}
		item.put("url", ProjectToolSupport.url(project, ProjMilestone.TYPE, milestone.getKey()));
		return item;
	}

	static String milestoneLine(Map<String,Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append(item.containsKey("subject") ? item.get("subject") : "(no subject)").append(" (key ").append(item.get("key"));
		sb.append(", ").append(item.get("milestoneStatus"));
		if(item.containsKey("dueDate")) {
			sb.append(", due ").append(item.get("dueDate"));
		} else {
			sb.append(", no due date");
		}
		sb.append(", ").append(item.get("status"));
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>)item.get("tags");
		if(!tags.isEmpty()) {
			sb.append(", tags ").append(String.join(", ", tags));
		}
		return sb.append(") ").append(item.get("url")).toString();
	}

	static Map<String,Object> milestoneItemSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("key", Map.of("type", "integer", "description", "The milestone key, the milestoneKey of get, update, set_status and delete"));
		properties.put("projectKey", Map.of("type", "integer"));
		properties.put("subject", Map.of("type", "string", "description", "absent for a milestone without subject"));
		properties.put("description", Map.of("type", "string", "description", "absent for a milestone without description"));
		properties.put("milestoneStatus", Map.of("type", "string", "enum", McpToolSupport.names(MILESTONE_STATUS),
				"description", MILESTONE_STATUS_WORDING));
		properties.put("dueDate", Map.of("type", "string", "format", "date", "description", "absent for a milestone without due date"));
		properties.put("color", Map.of("type", "string", "description", "absent for the default milestone color"));
		properties.put("status", ProjectToolSupport.artefactStatusSchema());
		properties.put("created", Map.of("type", "string", "format", "date-time"));
		properties.put("lastModified", Map.of("type", "string", "format", "date-time", "description", "last change of content or metadata"));
		properties.put("creator", McpToolSupport.personSchema());
		properties.put("tags", Map.of("type", "array", "items", Map.of("type", "string")));
		properties.put("members", Map.of("type", "array", "items", McpToolSupport.personSchema(), "description", "The project members involved in the milestone"));
		properties.put("links", Map.of("type", "array", "items", ProjectToolSupport.linkSchema(), "description", "get, create, update, set_status and delete: the linked artefacts of the project"));
		properties.put("url", Map.of("type", "string", "description", "Link to the milestone in OpenOlat"));
		return Map.of("type", "object", "properties", properties,
				"required", List.of("key", "projectKey", "milestoneStatus", "status", "created", "lastModified", "creator", "tags", "members", "url"));
	}
}
