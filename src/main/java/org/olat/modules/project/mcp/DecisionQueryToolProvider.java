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
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionInfo;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The decisions of one project as the decisions list of the project shows
 * them (ProjDecisionListController), and one decision with its links to
 * other artefacts of the project.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class DecisionQueryToolProvider implements McpToolProvider {

	public static final String NAME = "decision_query";

	static final String ACTION_LIST = "list";
	static final String ACTION_GET = "get";
	private static final List<String> ACTIONS = List.of(ACTION_LIST, ACTION_GET);

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: project decisions",
			"Decisions",
			"Returns the decisions of an OpenOlat project: a title, plain text details and the decision date, with the metadata tags, "
			+ "involved members and links to other artefacts of the project (ToDos, notes, files, appointments, milestones, decisions). "
			+ "Use action list with projectKey (from project_query) and the filters decidedAfter, decidedBefore, status and createdByMe; "
			+ "list is sorted by the decision date, the latest first, decisions without date last, "
			+ "and returns a preview of the details with their length instead of the full details. "
			+ "Use action get with decisionKey for one decision with its full details and links; to change a decision call decision_modify.",
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
		boolean createdByMe = McpToolSupport.bool(arguments, "createdByMe", false);
		Date decidedAfter = McpToolSupport.date(arguments, "decidedAfter");
		Date decidedBefore = McpToolSupport.date(arguments, "decidedBefore");
		// both bounds include their day
		Date decidedBeforeEnd = decidedBefore == null ? null : DateUtils.addDays(decidedBefore, 1);
		int limit = McpToolSupport.limit(arguments);

		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null || !support.projectCallback(context, project).canViewDecisions()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the decisions");
		}

		// The search parameters of ProjDecisionListController.createSearchParams and its filters
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setProject(project);
		if(!status.isEmpty()) {
			searchParams.setStatus(status);
		}
		// ProjectService has no paged query and no creator or date filter for decisions; the UI loads the same set
		List<ProjDecisionInfo> infos = new ArrayList<>(projectService.getDecisionInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)));
		infos.removeIf(info -> !matches(info.getDecision(), context, createdByMe, decidedAfter, decidedBeforeEnd));
		infos.sort(Comparator.comparing((ProjDecisionInfo info) -> info.getDecision().getDecisionDate(), Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(info -> info.getDecision().getKey(), Comparator.reverseOrder()));
		int total = infos.size();

		List<Map<String,Object>> items = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		for(ProjDecisionInfo info:infos.subList(0, Math.min(limit, total))) {
			Map<String,Object> item = DecisionToolSupport.decisionItem(support, info, false);
			items.add(item);
			lines.add(DecisionToolSupport.decisionLine(item));
		}
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), McpToolSupport.listText(lines, total, "decisions"));
	}

	private static boolean matches(ProjDecision decision, McpToolContext context, boolean createdByMe, Date decidedAfter, Date decidedBeforeEnd) {
		if(createdByMe && !context.identity().getKey().equals(decision.getArtefact().getCreator().getKey())) {
			return false;
		}
		Date decisionDate = decision.getDecisionDate();
		if((decidedAfter != null || decidedBeforeEnd != null) && decisionDate == null) {
			return false;
		}
		if(decidedAfter != null && decisionDate.before(decidedAfter)) {
			return false;
		}
		return decidedBeforeEnd == null || decisionDate.before(decidedBeforeEnd);
	}

	private McpToolResult get(McpToolContext context, Map<String,Object> arguments) {
		Long decisionKey = McpToolSupport.longValue(arguments, "decisionKey");
		if(decisionKey == null) {
			return McpToolResult.error("The action get needs the argument decisionKey. Call decision_query with action list to find it.");
		}
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setDecisions(List.of(() -> decisionKey));
		List<ProjDecisionInfo> infos = projectService.getDecisionInfos(searchParams, ProjArtefactInfoParams.of(true, false, true));
		// an unknown key gets the answer of a foreign decision
		if(infos.isEmpty()) {
			throw new McpPermissionException("decision with key " + decisionKey, "view the decision");
		}
		ProjDecisionInfo info = infos.get(0);
		ProjProject project = info.getDecision().getArtefact().getProject();
		if(!support.projectCallback(context, project).canViewDecisions()) {
			throw new McpPermissionException("decision with key " + decisionKey, "view the decision");
		}
		Map<String,Object> item = DecisionToolSupport.decisionItem(support, info, true);
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", item);
		StringBuilder text = new StringBuilder("Decision ").append(DecisionToolSupport.decisionLine(item));
		if(item.containsKey("details")) {
			text.append("\n\n").append(item.get("details"));
		}
		return McpToolResult.of(result, text.toString());
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS, "default", ACTION_LIST,
				"description", "list: the decisions of a project with a preview of the details; get: one decision with the details and the links"));
		properties.put("projectKey", Map.of("type", "integer", "description", "list only, required: the key of the project"));
		properties.put("decidedAfter", Map.of("type", "string", "format", "date",
				"description", "list only: decisions with a decision date on or after this day (yyyy-MM-dd); excludes decisions without date"));
		properties.put("decidedBefore", Map.of("type", "string", "format", "date",
				"description", "list only: decisions with a decision date on or before this day (yyyy-MM-dd); excludes decisions without date"));
		properties.put("status", ProjectToolSupport.artefactStatusFilterSchema("decisions"));
		properties.put("createdByMe", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "list only: only the decisions the account created"));
		properties.put("decisionKey", Map.of("type", "integer", "description", "get only, required: the key of the decision"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "list only: maximum number of decisions returned"));
		return Map.of("type", "object", "properties", properties, "additionalProperties", Boolean.FALSE);
	}

	static Map<String,Object> outputSchema() {
		Map<String,Object> itemSchema = DecisionToolSupport.decisionItemSchema();
		return McpToolSupport.queryOutputSchema(itemSchema, ProjectToolSupport.outputDefs());
	}
}
