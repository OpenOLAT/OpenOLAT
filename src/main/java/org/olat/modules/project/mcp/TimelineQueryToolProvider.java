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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivity.ActionTarget;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The timeline of a project: the activities the project timeline shows
 * (ProjTimelineController, ProjActivity.TIMELINE_ACTIONS), newest first,
 * with the doer and the artefact. For "what happened since Monday".
 * <p>
 * The read and download actions (projectRead, *Read, *Download) are never
 * returned, also not with allActions=true: no view of the UI shows who read
 * or downloaded what across the whole project, the member list only shows
 * the last visit.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TimelineQueryToolProvider implements McpToolProvider {

	public static final String NAME = "timeline_query";

	/**
	 * The targets of the type filter of the timeline (ProjTimelineController,
	 * timeline.filter.type.*) and decision; the whiteboard has no activities
	 * the timeline shows.
	 */
	private static final List<ActionTarget> TYPES = List.of(ActionTarget.project, ActionTarget.file, ActionTarget.toDo,
			ActionTarget.note, ActionTarget.appointment, ActionTarget.milestone, ActionTarget.decision);
	/** Default and maximum window in days */
	static final int DEFAULT_DAYS = 14;
	static final int MAX_DAYS = 366;
	/** All actions without the read and download actions, the filter of allActions=true */
	static final List<Action> ALL_ACTIONS_WITHOUT_READS = Arrays.stream(Action.values())
			.filter(action -> !isReadAction(action))
			.toList();

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: project timeline",
			"Timeline",
			"Returns what happened in an OpenOlat project: the activities of the project timeline, newest first, with date, action, "
			+ "the user who acted and the artefact (file, ToDo, note, appointment, milestone, decision) with its key for the other tools. "
			+ "Use projectKey (from project_query) with from and to (default the last 14 days), types to narrow to artefact types, "
			+ "and allActions=true to include the detail actions (tags, members, links, roles) the timeline hides. "
			+ "Who read or downloaded what is never returned.",
			inputSchema(), outputSchema(), false, true);

	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectToolSupport support;

	@Override
	public String getToolName() {
		return NAME;
	}

	@Override
	public String getPack() {
		return ProjectMcpPack.NAME;
	}

	@Override
	public McpToolScope getScope() {
		return McpToolScope.READ;
	}

	@Override
	public boolean isAvailable() {
		return support.isPackAvailable();
	}

	@Override
	public McpToolDefinition getDefinition() {
		return DEFINITION;
	}

	@Override
	public McpToolResult execute(McpToolContext context, Map<String,Object> arguments) {
		return list(context, arguments);
	}

	private McpToolResult list(McpToolContext context, Map<String,Object> arguments) {
		Long projectKey = McpToolSupport.longValue(arguments, "projectKey");
		if(projectKey == null) {
			return McpToolResult.error("The argument projectKey is required. Call project_query to list your projects.");
		}
		Date to = McpToolSupport.date(arguments, "to");
		Date from = McpToolSupport.date(arguments, "from");
		if(to == null) {
			to = DateUtils.setTime(new Date(), 0, 0, 0);
		}
		if(from == null) {
			from = DateUtils.addDays(to, -DEFAULT_DAYS);
		}
		if(to.before(from)) {
			return McpToolResult.error("The argument to must not be before from.");
		}
		if(DateUtils.addDays(from, MAX_DAYS).before(to)) {
			return McpToolResult.error("The window from to to is longer than " + MAX_DAYS + " days.");
		}
		List<String> typeValues = McpToolSupport.strings(arguments, "types");
		List<ActionTarget> targets = new ArrayList<>();
		for(String value:typeValues) {
			ActionTarget target = McpToolSupport.enumValue(TYPES, value);
			if(target == null) {
				return McpToolResult.error(McpToolSupport.unknownValue("type", value, TYPES));
			}
			targets.add(target);
		}
		boolean allActions = McpToolSupport.bool(arguments, "allActions", false);
		int limit = McpToolSupport.limit(arguments);

		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null || !support.projectCallback(context, project).canViewTimeline()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the timeline");
		}

		// The query of ProjTimelineController.loadActivites; the day of "to" is included
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		// the read and download actions are excluded in both cases
		searchParams.setActions(allActions ? ALL_ACTIONS_WITHOUT_READS : ProjActivity.TIMELINE_ACTIONS);
		if(!targets.isEmpty()) {
			searchParams.setTargets(targets);
		}
		searchParams.setCreatedDateRanges(List.of(new DateRange(from, DateUtils.addDays(to, 1))));
		searchParams.setFetchDoer(true);
		// one more than the limit tells whether the list is truncated
		List<ProjActivity> activities = projectService.getActivities(searchParams, 0, limit + 1);
		boolean truncated = activities.size() > limit;
		if(truncated) {
			activities = activities.subList(0, limit);
		}

		Set<ProjArtefact> artefacts = new HashSet<>();
		for(ProjActivity activity:activities) {
			if(activity.getArtefact() != null) {
				artefacts.add(activity.getArtefact());
			}
		}
		ProjArtefactItems items = null;
		if(!artefacts.isEmpty()) {
			ProjArtefactSearchParams artefactParams = new ProjArtefactSearchParams();
			artefactParams.setProject(project);
			artefactParams.setArtefacts(artefacts);
			items = projectService.getArtefactItems(artefactParams);
		}
		List<Map<String,Object>> rows = new ArrayList<>(activities.size());
		List<String> lines = new ArrayList<>(activities.size());
		for(ProjActivity activity:activities) {
			Map<String,Object> row = toRow(activity, items, project);
			rows.add(row);
			lines.add(line(row));
		}
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("items", rows);
		result.put("total", Integer.valueOf(rows.size()));
		result.put("truncated", Boolean.valueOf(truncated));
		result.put("from", McpToolSupport.isoDate(from));
		result.put("to", McpToolSupport.isoDate(to));
		String text = McpToolSupport.listText(lines, rows.size(), "activities between " + McpToolSupport.isoDate(from) + " and " + McpToolSupport.isoDate(to)
				+ (truncated ? " (more exist, narrow the window or raise the limit)" : ""));
		return McpToolResult.of(result, text);
	}

	private Map<String,Object> toRow(ProjActivity activity, ProjArtefactItems items, ProjProject project) {
		Map<String,Object> row = new LinkedHashMap<>();
		row.put("key", activity.getKey());
		row.put("date", McpToolSupport.iso(activity.getCreationDate()));
		row.put("action", activity.getAction().name());
		row.put("target", activity.getActionTarget().name());
		Identity doer = activity.getDoer();
		if(doer != null) {
			row.put("doer", support.toPerson(doer));
		}
		if(activity.getMember() != null) {
			row.put("member", support.toPerson(activity.getMember()));
		}
		ProjArtefact artefact = activity.getArtefact();
		if(artefact != null) {
			row.put("artefact", artefactRef(artefact, items, project));
		}
		return row;
	}

	/**
	 * The artefact of an activity addressed as the links of the other tools:
	 * the ToDoTask key for a ToDo, the object key for the other types.
	 */
	private static Map<String,Object> artefactRef(ProjArtefact artefact, ProjArtefactItems items, ProjProject project) {
		Map<String,Object> ref = new LinkedHashMap<>();
		String type = null;
		Long key = null;
		Long businessKey = null;
		String title = null;
		if(items != null) {
			switch(artefact.getType()) {
				case ProjToDo.TYPE -> {
					ProjToDo toDo = items.getToDo(artefact);
					if(toDo != null) {
						type = ActionTarget.toDo.name(); key = toDo.getToDoTask().getKey(); businessKey = toDo.getKey(); title = toDo.getToDoTask().getTitle();
					}
				}
				case ProjNote.TYPE -> {
					ProjNote note = items.getNote(artefact);
					if(note != null) {
						type = ActionTarget.note.name(); key = note.getKey(); businessKey = key; title = note.getTitle();
					}
				}
				case ProjFile.TYPE -> {
					ProjFile file = items.getFile(artefact);
					if(file != null) {
						type = ActionTarget.file.name(); key = file.getKey(); businessKey = key;
						title = StringHelper.containsNonWhitespace(file.getVfsMetadata().getTitle()) ? file.getVfsMetadata().getTitle() : file.getVfsMetadata().getFilename();
					}
				}
				case ProjAppointment.TYPE -> {
					ProjAppointment appointment = items.getAppointment(artefact);
					if(appointment != null) {
						type = ActionTarget.appointment.name(); key = appointment.getKey(); businessKey = key; title = appointment.getSubject();
					}
				}
				case ProjMilestone.TYPE -> {
					ProjMilestone milestone = items.getMilestone(artefact);
					if(milestone != null) {
						type = ActionTarget.milestone.name(); key = milestone.getKey(); businessKey = key; title = milestone.getSubject();
					}
				}
				case ProjDecision.TYPE -> {
					ProjDecision decision = items.getDecision(artefact);
					if(decision != null) {
						type = ActionTarget.decision.name(); key = decision.getKey(); businessKey = key; title = decision.getTitle();
					}
				}
				default -> { /* unknown type */ }
			}
		}
		ref.put("type", Objects.requireNonNullElse(type, artefact.getType()));
		if(key != null) {
			ref.put("key", key);
		}
		if(StringHelper.containsNonWhitespace(title)) {
			ref.put("title", title);
		}
		ref.put("status", artefact.getStatus().name());
		if(businessKey != null) {
			ref.put("url", ProjectToolSupport.url(project, artefact.getType(), businessKey));
		}
		return ref;
	}

	private static String line(Map<String,Object> row) {
		StringBuilder sb = new StringBuilder();
		sb.append(row.get("date")).append(' ').append(row.get("action"));
		if(row.containsKey("doer")) {
			sb.append(" by ").append(((Map<?,?>)row.get("doer")).get("displayName"));
		}
		if(row.containsKey("member")) {
			sb.append(", member ").append(((Map<?,?>)row.get("member")).get("displayName"));
		}
		if(row.containsKey("artefact")) {
			Map<?,?> artefact = (Map<?,?>)row.get("artefact");
			sb.append(": ").append(artefact.get("type"));
			if(artefact.containsKey("title")) {
				sb.append(" \"").append(artefact.get("title")).append('"');
			}
			if(artefact.containsKey("key")) {
				sb.append(" (key ").append(artefact.get("key")).append(')');
			}
		}
		return sb.toString();
	}

	/**
	 * @return true for projectRead and the read and download actions of the
	 *         artefacts (fileRead, fileDownload, noteRead, noteDownload)
	 */
	static boolean isReadAction(Action action) {
		String name = action.name();
		return name.endsWith("Read") || name.endsWith("Download");
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("projectKey", Map.of("type", "integer", "description", "required: the key of the project"));
		properties.put("from", Map.of("type", "string", "format", "date", "description", "first day of the window (yyyy-MM-dd), default to minus " + DEFAULT_DAYS + " days"));
		properties.put("to", Map.of("type", "string", "format", "date", "description", "last day of the window, included; default today"));
		properties.put("types", Map.of("type", "array", "items", Map.of("type", "string", "enum", McpToolSupport.names(TYPES)),
				"description", "only activities on these targets, the type filter of the timeline: project (Project), file (File), "
						+ "toDo (To-do), note (Note), appointment (Appointment), milestone (Milestone), decision; default all"));
		properties.put("allActions", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "true includes the detail actions the timeline hides: tags, members, links, roles; reads and downloads are never returned"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "maximum number of activities, newest first"));
		return Map.of("type", "object", "properties", properties, "required", List.of("projectKey"), "additionalProperties", Boolean.FALSE);
	}

	private static Map<String,Object> outputSchema() {
		Map<String,Object> artefactProperties = new LinkedHashMap<>();
		artefactProperties.put("type", Map.of("type", "string", "description",
				"the link type of the other tools: " + String.join(", ", McpToolSupport.names(ProjectToolSupport.LINK_TYPES))
				+ "; the internal artefact type if the artefact is no longer readable"));
		artefactProperties.put("key", Map.of("type", "integer", "description", "the key the query and modify tools use: the ToDoTask key for a ToDo"));
		artefactProperties.put("title", Map.of("type", "string"));
		artefactProperties.put("status", ProjectToolSupport.artefactStatusSchema());
		artefactProperties.put("url", Map.of("type", "string"));
		Map<String,Object> itemProperties = new LinkedHashMap<>();
		itemProperties.put("key", Map.of("type", "integer"));
		itemProperties.put("date", Map.of("type", "string", "format", "date-time"));
		itemProperties.put("action", Map.of("type", "string", "description", "the activity action of OpenOlat, e.g. toDoCreate, noteContentUpdate, projectMemberAdd"));
		itemProperties.put("target", Map.of("type", "string", "enum", McpToolSupport.names(TYPES)));
		itemProperties.put("doer", McpToolSupport.personSchema());
		itemProperties.put("member", Map.of("type", "object", "properties", Map.of("username", Map.of("type", "string"), "displayName", Map.of("type", "string")),
				"description", "the affected member of a member action"));
		itemProperties.put("artefact", Map.of("type", "object", "properties", artefactProperties, "required", List.of("type", "status")));
		Map<String,Object> itemSchema = Map.of("type", "object", "properties", itemProperties, "required", List.of("key", "date", "action", "target"));
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("items", Map.of("type", "array", "items", itemSchema));
		properties.put("total", Map.of("type", "integer", "description", "number of returned activities"));
		properties.put("truncated", Map.of("type", "boolean", "description", "true if more activities exist in the window"));
		properties.put("from", Map.of("type", "string", "format", "date"));
		properties.put("to", Map.of("type", "string", "format", "date"));
		return Map.of("type", "object", "properties", properties, "required", List.of("items", "total", "truncated", "from", "to"));
	}
}
