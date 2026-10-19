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
package org.olat.modules.todo.mcp;

import static org.olat.modules.todo.mcp.ToDoToolSupport.CONTEXT_ALL;
import static org.olat.modules.todo.mcp.ToDoToolSupport.CONTEXT_PROJECT;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.mcp.ToDoToolSupport.ToDoAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The ToDos of the caller across all contexts, as the "My ToDos" user tool
 * shows them (ToDoUserToolListController), plus the full ToDo list of one
 * project for project members.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ToDoQueryToolProvider implements McpToolProvider {

	public static final String NAME = "todo_query";

	static final String ACTION_LIST = "list";
	static final String ACTION_GET = "get";
	private static final List<String> ACTIONS = List.of(ACTION_LIST, ACTION_GET);

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: ToDos",
			"ToDos",
			"Returns the ToDos of the OpenOlat account from every context (personal, project, course, curriculum, quality management) "
			+ "with status, priority, dates, assignees, tags, context and a link, as the user tool \"My ToDos\" shows them. "
			+ "Use action list with the filters context, originKey (e.g. the project key from project_query), status, dueBefore and overdue; "
			+ "assignedToMe=false lists every ToDo of one project and needs context project and originKey. "
			+ "Use action get with toDoKey for one ToDo; to change a ToDo call todo_modify. "
			+ ToDoToolSupport.KEY_CONTRACT,
			inputSchema(), ToDoToolSupport.outputSchema(), false, true);

	@Autowired
	private ToDoService toDoService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ToDoToolSupport support;

	@Override
	public String getToolName() {
		return NAME;
	}

	@Override
	public String getPack() {
		return ToDoMcpPack.NAME;
	}

	@Override
	public McpToolScope getScope() {
		return McpToolScope.READ;
	}

	@Override
	public boolean isAvailable() {
		return support.isAnyProviderEnabled();
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
		String toDoContext = McpToolSupport.string(arguments, "context", CONTEXT_ALL);
		if(!ToDoToolSupport.QUERY_CONTEXTS.contains(toDoContext)) {
			return McpToolResult.error("Unknown context " + toDoContext + ". Use one of: " + String.join(", ", ToDoToolSupport.QUERY_CONTEXTS) + ".");
		}
		Long originKey = McpToolSupport.longValue(arguments, "originKey");
		if(originKey != null && CONTEXT_ALL.equals(toDoContext)) {
			return McpToolResult.error("The argument originKey needs a context other than all, e.g. context project for a project key.");
		}
		boolean assignedToMe = McpToolSupport.bool(arguments, "assignedToMe", true);
		if(!assignedToMe) {
			if(!CONTEXT_PROJECT.equals(toDoContext) || originKey == null) {
				return McpToolResult.error("assignedToMe=false is allowed for context project with an originKey (the project key) only.");
			}
			ProjProject project = projectService.getProject(() -> originKey);
			// an unknown key gets the answer of a foreign project
			if(project == null || !support.projectCallback(context, project).canViewToDos()) {
				throw new McpPermissionException(ToDoToolSupport.describeProject(originKey), "view the ToDos");
			}
		}
		List<ToDoStatus> status = status(arguments);
		Date dueBefore = McpToolSupport.date(arguments, "dueBefore");
		boolean overdue = McpToolSupport.bool(arguments, "overdue", false);
		int limit = McpToolSupport.limit(arguments);

		List<String> types = support.enabledTypes(toDoContext);
		if(types.isEmpty()) {
			return McpToolResult.of(McpToolSupport.listResult(List.of(), 0, limit),
					"0 ToDos. The context " + toDoContext + " is not enabled on this OpenOlat instance.");
		}

		// The search parameters of ToDoUserToolListController.createSearchParams
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		if(assignedToMe) {
			searchParams.setAssigneeOrDelegatee(context.identity());
			searchParams.setAssigneeRightsNull(Boolean.FALSE);
		}
		searchParams.setTypes(types);
		searchParams.setStatus(status);
		if(originKey != null) {
			searchParams.setOriginIds(List.of(originKey));
		}
		searchParams.setOriginDeleted(Boolean.FALSE);
		DateRange dueRange = dueRange(dueBefore, overdue);
		if(dueRange != null) {
			searchParams.setDueDateRanges(List.of(dueRange));
		}

		// ToDoService has no paged query; the user tool loads the same set
		List<ToDoTask> toDoTasks = new ArrayList<>(toDoService.getToDoTasks(searchParams));
		toDoTasks.sort(ToDoToolSupport.DUE_DATE_ORDER);
		int total = toDoTasks.size();
		List<ToDoTask> page = toDoTasks.subList(0, Math.min(limit, total));
		List<Map<String,Object>> items = support.toItems(page);
		List<String> lines = items.stream().map(ToDoToolSupport::line).toList();
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), McpToolSupport.listText(lines, total, "ToDos"));
	}

	private McpToolResult get(McpToolContext context, Map<String,Object> arguments) {
		Long toDoKey = McpToolSupport.longValue(arguments, "toDoKey");
		if(toDoKey == null) {
			return McpToolResult.error("The action get needs the argument toDoKey.");
		}
		// an unknown key gets the answer of a foreign ToDo, the access check comes before any other answer
		ToDoTask toDoTask = support.loadToDo(toDoKey);
		if(toDoTask == null) {
			throw new McpPermissionException(ToDoToolSupport.describe(toDoKey), "view the ToDo");
		}
		ToDoTaskMembers members = support.loadMembers(toDoTask);
		ToDoAccess access = support.access(context, toDoTask, members);
		if(!access.canView()) {
			throw new McpPermissionException(ToDoToolSupport.describe(toDoKey), "view the ToDo");
		}
		ToDoProvider provider = support.getProvider(toDoTask);
		if(provider == null || !provider.isEnabled()) {
			return McpToolResult.error("The ToDos of type " + toDoTask.getType() + " are not enabled on this OpenOlat instance.");
		}
		Map<String,Object> item = support.toItem(toDoTask, members, access.projToDo());
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", item);
		return McpToolResult.of(result, "ToDo " + ToDoToolSupport.line(item));
	}

	private static List<ToDoStatus> status(Map<String,Object> arguments) {
		List<String> values = McpToolSupport.strings(arguments, "status");
		if(values.isEmpty()) {
			return List.of(ToDoStatus.open, ToDoStatus.inProgress);
		}
		List<ToDoStatus> status = new ArrayList<>(values.size());
		for(String value:values) {
			ToDoStatus toDoStatus = McpToolSupport.enumValue(ToDoToolSupport.STATUS_VALUES, value);
			if(toDoStatus == null) {
				throw new McpInvalidArgumentException(McpToolSupport.unknownValue("status", value, ToDoToolSupport.STATUS_VALUES));
			}
			status.add(toDoStatus);
		}
		return status;
	}

	/**
	 * @param dueBefore Due strictly before this day, or null
	 * @param overdue Due before today, the overdue filter of the UI
	 * @return The range or null for no due date filter
	 */
	static DateRange dueRange(Date dueBefore, boolean overdue) {
		Date now = new Date();
		Date to = null;
		if(overdue) {
			to = DateUtils.setTime(now, 0, 0, 0);
		}
		if(dueBefore != null && (to == null || dueBefore.before(to))) {
			to = dueBefore;
		}
		return to == null ? null : new DateRange(DateUtils.addYears(now, -100), to);
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS, "default", ACTION_LIST,
				"description", "list: the ToDos matching the filters; get: one ToDo by toDoKey"));
		properties.put("context", Map.of("type", "string", "enum", ToDoToolSupport.QUERY_CONTEXTS, "default", CONTEXT_ALL,
				"description", "list only: the origin of the ToDos"));
		properties.put("originKey", Map.of("type", "integer",
				"description", "list only: key of the context object (project key, course key, curriculum key); needs a context other than all"));
		properties.put("status", Map.of("type", "array", "items", Map.of("type", "string", "enum", McpToolSupport.names(ToDoToolSupport.STATUS_VALUES)),
				"default", McpToolSupport.names(ToDoStatus.open, ToDoStatus.inProgress),
				"description", "list only: the status values to include: " + ToDoToolSupport.STATUS_WORDING));
		properties.put("dueBefore", Map.of("type", "string", "format", "date",
				"description", "list only: ToDos due before this day (yyyy-MM-dd)"));
		properties.put("overdue", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "list only: ToDos with a due date before today"));
		properties.put("assignedToMe", Map.of("type", "boolean", "default", Boolean.TRUE,
				"description", "list only: false lists every ToDo of the project the account may see; context project with originKey only"));
		properties.put("toDoKey", Map.of("type", "integer", "description", "get only, required: the key of the ToDo as list returns it, not the number in its URL"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "list only: maximum number of ToDos returned"));
		return Map.of("type", "object", "properties", properties, "additionalProperties", Boolean.FALSE);
	}
}
