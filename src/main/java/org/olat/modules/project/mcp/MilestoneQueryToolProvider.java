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
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.util.DateUtils;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneInfo;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The milestones of one project as the project calendar and the timeline
 * show them, and one milestone with its links to other artefacts of the
 * project.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class MilestoneQueryToolProvider implements McpToolProvider {

	public static final String NAME = "milestone_query";

	static final String ACTION_LIST = "list";
	static final String ACTION_GET = "get";
	private static final List<String> ACTIONS = List.of(ACTION_LIST, ACTION_GET);

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: project milestones",
			"Milestones",
			"Returns the milestones of an OpenOlat project: subject, description, the milestone status (open or achieved), "
			+ "the due date, the color, tags and links to other artefacts of the project. "
			+ "Use action list with projectKey (from project_query) and the filters milestoneStatus (default open and achieved), "
			+ "dueAfter and dueBefore (days, inclusive; a milestone without due date does not match), status and createdByMe; "
			+ "list is sorted by the due date, milestones without due date last. "
			+ "Use action get with milestoneKey for one milestone with its links; to change a milestone call milestone_modify.",
			inputSchema(), outputSchema(), false, true);

	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectToolSupport support;
	@Autowired
	private MilestoneToolSupport milestoneSupport;

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
		String action = McpToolSupport.string(arguments, "action", ACTION_LIST);
		return switch(action) {
			case ACTION_LIST -> list(context, arguments);
			case ACTION_GET -> get(context, arguments);
			default -> McpToolResult.error("Unknown action " + action + ". Use one of: " + String.join(", ", ACTIONS) + ".");
		};
	}

	private McpToolResult list(McpToolContext context, Map<String,Object> arguments) {
		Long projectKey = McpToolSupport.longValue(arguments, "projectKey");
		if(projectKey == null) {
			return McpToolResult.error("The action list needs the argument projectKey. Call project_query to list your projects.");
		}
		String statusValue = McpToolSupport.string(arguments, "status", ProjectStatus.active.name());
		List<ProjectStatus> status = ProjectToolSupport.statusFilter(statusValue, ProjectToolSupport.ARTEFACT_STATUS);
		if(status == null) {
			return McpToolResult.error(ProjectToolSupport.unknownStatusFilter(statusValue, ProjectToolSupport.ARTEFACT_STATUS));
		}
		List<ProjMilestoneStatus> milestoneStatus = milestoneStatus(arguments);
		Date dueAfter = McpToolSupport.date(arguments, "dueAfter");
		Date dueBefore = McpToolSupport.date(arguments, "dueBefore");
		if(dueAfter != null && dueBefore != null && dueBefore.before(dueAfter)) {
			return McpToolResult.error("The argument dueBefore is before dueAfter.");
		}
		// dueBefore is inclusive: before the start of the next day
		Date dueBeforeExclusive = dueBefore == null ? null : DateUtils.addDays(dueBefore, 1);
		boolean createdByMe = McpToolSupport.bool(arguments, "createdByMe", false);
		int limit = McpToolSupport.limit(arguments);

		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null || !support.projectCallback(context, project).canViewMilestones()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the milestones");
		}

		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setProject(project);
		if(!status.isEmpty()) {
			searchParams.setStatus(status);
		}
		searchParams.setMilestoneStatus(milestoneStatus);
		if(dueAfter != null || dueBefore != null) {
			searchParams.setDueDateNull(Boolean.FALSE);
		}
		// ProjectService has no paged query and no filter by creator or due date range; the UI loads the same set
		List<ProjMilestoneInfo> infos = new ArrayList<>(projectService.getMilestoneInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)));
		infos.removeIf(info -> {
			ProjMilestone milestone = info.getMilestone();
			Date dueDate = milestone.getDueDate();
			return (createdByMe && !context.identity().getKey().equals(milestone.getArtefact().getCreator().getKey()))
					|| (dueAfter != null && (dueDate == null || dueDate.before(dueAfter)))
					|| (dueBeforeExclusive != null && (dueDate == null || !dueDate.before(dueBeforeExclusive)));
		});
		infos.sort(Comparator.comparing((ProjMilestoneInfo info) -> info.getMilestone().getDueDate(), Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(info -> info.getMilestone().getKey()));
		int total = infos.size();

		List<Map<String,Object>> items = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		for(ProjMilestoneInfo info:infos.subList(0, Math.min(limit, total))) {
			Map<String,Object> item = milestoneSupport.milestoneItem(info, false);
			items.add(item);
			lines.add(MilestoneToolSupport.milestoneLine(item));
		}
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), McpToolSupport.listText(lines, total, "milestones"));
	}

	/**
	 * @param arguments The arguments
	 * @return The milestone status of the argument milestoneStatus, open and achieved when absent or empty
	 */
	private static List<ProjMilestoneStatus> milestoneStatus(Map<String,Object> arguments) {
		List<String> values = McpToolSupport.strings(arguments, "milestoneStatus");
		if(values.isEmpty()) {
			return List.of(ProjMilestoneStatus.values());
		}
		List<ProjMilestoneStatus> milestoneStatus = new ArrayList<>();
		for(String value:values) {
			ProjMilestoneStatus status = McpToolSupport.enumValue(MilestoneToolSupport.MILESTONE_STATUS, value);
			if(status == null) {
				throw new McpInvalidArgumentException(McpToolSupport.unknownValue("milestoneStatus", value, MilestoneToolSupport.MILESTONE_STATUS));
			}
			milestoneStatus.add(status);
		}
		return milestoneStatus;
	}

	private McpToolResult get(McpToolContext context, Map<String,Object> arguments) {
		Long milestoneKey = McpToolSupport.longValue(arguments, "milestoneKey");
		if(milestoneKey == null) {
			return McpToolResult.error("The action get needs the argument milestoneKey. Call milestone_query with action list to find it.");
		}
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setMilestones(List.of(() -> milestoneKey));
		List<ProjMilestoneInfo> infos = projectService.getMilestoneInfos(searchParams, ProjArtefactInfoParams.of(true, false, true));
		// an unknown key gets the answer of a foreign milestone
		if(infos.isEmpty()) {
			throw new McpPermissionException("milestone with key " + milestoneKey, "view the milestone");
		}
		ProjMilestoneInfo info = infos.get(0);
		ProjProject project = info.getMilestone().getArtefact().getProject();
		if(!support.projectCallback(context, project).canViewMilestones()) {
			throw new McpPermissionException("milestone with key " + milestoneKey, "view the milestone");
		}
		Map<String,Object> item = milestoneSupport.milestoneItem(info, true);
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", item);
		String text = "Milestone " + MilestoneToolSupport.milestoneLine(item);
		if(item.containsKey("description")) {
			text += "\n\n" + item.get("description");
		}
		return McpToolResult.of(result, text);
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS, "default", ACTION_LIST,
				"description", "list: the milestones of a project; get: one milestone with the links"));
		properties.put("projectKey", Map.of("type", "integer", "description", "list only, required: the key of the project"));
		properties.put("milestoneStatus", Map.of("type", "array", "items", Map.of("type", "string", "enum", McpToolSupport.names(MilestoneToolSupport.MILESTONE_STATUS)),
				"maxItems", MilestoneToolSupport.MILESTONE_STATUS.size(),
				"description", "list only: " + MilestoneToolSupport.MILESTONE_STATUS_WORDING + " or both; default both"));
		properties.put("dueAfter", Map.of("type", "string", "format", "date",
				"description", "list only: milestones due on or after this day (yyyy-MM-dd)"));
		properties.put("dueBefore", Map.of("type", "string", "format", "date",
				"description", "list only: milestones due on or before this day (yyyy-MM-dd)"));
		properties.put("status", ProjectToolSupport.artefactStatusFilterSchema("milestones"));
		properties.put("createdByMe", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "list only: only the milestones the account created"));
		properties.put("milestoneKey", Map.of("type", "integer", "description", "get only, required: the key of the milestone"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "list only: maximum number of milestones returned"));
		return Map.of("type", "object", "properties", properties, "additionalProperties", Boolean.FALSE);
	}

	static Map<String,Object> outputSchema() {
		Map<String,Object> itemSchema = MilestoneToolSupport.milestoneItemSchema();
		return McpToolSupport.queryOutputSchema(itemSchema, ProjectToolSupport.outputDefs());
	}
}
