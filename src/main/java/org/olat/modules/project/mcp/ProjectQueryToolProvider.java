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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjMemberInfo;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.todo.ToDoStatus;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The projects of the caller as the list "My projects" shows them
 * (ProjProjectMyController), the templates the caller may create a project
 * from as the list "Templates" shows them (ProjProjectTemplatesController),
 * and one project or template with its members and the number of open ToDos.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ProjectQueryToolProvider implements McpToolProvider {

	public static final String NAME = "project_query";

	static final String ACTION_LIST = "list";
	static final String ACTION_GET = "get";
	private static final List<String> ACTIONS = List.of(ACTION_LIST, ACTION_GET);
	/** Upper bound of the member list of one project */
	static final int MEMBERS_MAX = 200;

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: projects",
			"Projects",
			"Returns the OpenOlat projects the account is a member of, with status, teaser, the own project roles and a link. "
			+ "Use action list with status (active by default, done for the closed projects, deleted or all) to find a project, "
			+ "and action get with projectKey for the description, the members with their roles and the number of open ToDos. "
			+ "list with templates true returns the project templates the account may use (status active or done, the status argument is ignored); "
			+ "project_modify create with templateKey creates a project from one of them. "
			+ "To list the ToDos of a project call todo_query with context project and originKey set to the project key; "
			+ "this tool does not return ToDos, files or notes.",
			inputSchema(), outputSchema(), false, true);

	@Autowired
	private UserManager userManager;
	@Autowired
	private ProjectToolSupport support;
	@Autowired
	private ProjectService projectService;

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
		boolean templates = McpToolSupport.bool(arguments, "templates", false);
		String statusValue = McpToolSupport.string(arguments, "status", ProjectStatus.active.name());
		List<ProjectStatus> status = ProjectToolSupport.statusFilter(statusValue, ProjectToolSupport.PROJECT_STATUS);
		if(!templates && status == null) {
			return McpToolResult.error(ProjectToolSupport.unknownStatusFilter(statusValue, ProjectToolSupport.PROJECT_STATUS));
		}
		int limit = McpToolSupport.limit(arguments);

		List<ProjProject> projects;
		if(templates) {
			projects = new ArrayList<>(support.usableTemplates(context, null));
		} else {
			// The search parameters of ProjProjectMyController: member projects, no templates
			ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
			searchParams.setIdentity(context.identity());
			searchParams.setTemplate(Boolean.FALSE);
			if(!status.isEmpty()) {
				searchParams.setStatus(status);
			}
			// ProjectService has no paged query; the UI loads the same set
			projects = new ArrayList<>(projectService.getProjects(searchParams));
		}
		projects.sort(Comparator.comparing((ProjProject project) -> project.getTitle() == null ? "" : project.getTitle().toLowerCase())
				.thenComparing(ProjProject::getKey));
		int total = projects.size();

		List<Map<String,Object>> items = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		List<ProjProject> page = projects.subList(0, Math.min(limit, total));
		// the own roles of all projects of the page with one query
		Map<Long,Set<ProjectRole>> projectKeyToRoles = projectService.getRoles(page, context.identity());
		for(ProjProject project:page) {
			Map<String,Object> item = toItem(project, projectKeyToRoles.getOrDefault(project.getKey(), Set.of()));
			items.add(item);
			lines.add(line(item));
		}
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), McpToolSupport.listText(lines, total, templates ? "templates" : "projects"));
	}

	private McpToolResult get(McpToolContext context, Map<String,Object> arguments) {
		Long projectKey = McpToolSupport.longValue(arguments, "projectKey");
		if(projectKey == null) {
			return McpToolResult.error("The action get needs the argument projectKey. Call project_query with action list to find it.");
		}
		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the project");
		}
		Set<ProjectRole> roles = projectService.getRoles(project, context.identity());
		// a template the caller may use is visible as in the list "Templates"
		boolean memberOrManager = !roles.isEmpty() || support.isManager(context, project);
		if(!memberOrManager && !support.isUsableTemplate(context, project)) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the project");
		}

		Map<String,Object> item = toItem(project, roles);
		if(StringHelper.containsNonWhitespace(project.getDescription())) {
			item.put("description", project.getDescription());
		}
		ProjMemberInfoSearchParameters memberParams = new ProjMemberInfoSearchParameters();
		memberParams.setProject(project);
		memberParams.setRoles(ProjectRole.PROJECT_ROLES);
		List<Map<String,Object>> members = projectService.getMembersInfos(memberParams).stream()
				.map(this::toMember)
				.sorted(Comparator.comparing(member -> member.get("displayName").toString().toLowerCase()))
				.toList();
		// The list "Templates" shows only the owners and the portraits of the members to a user of a
		// template who cannot open it (ProjProjectListController): the member list stays out, the number stays
		if(memberOrManager) {
			item.put("members", members.subList(0, Math.min(MEMBERS_MAX, members.size())));
		}
		item.put("membersTotal", Integer.valueOf(members.size()));

		// The dashboard shows the ToDo widget only with canViewToDos (ProjProjectDashboardController),
		// a manager without project role does not see it
		String toDosText = ".";
		if(support.projectCallback(context, project).canViewToDos()) {
			ProjToDoSearchParams toDoParams = new ProjToDoSearchParams();
			toDoParams.setProject(project);
			toDoParams.setStatus(List.of(ProjectStatus.active));
			toDoParams.setToDoStatus(List.of(ToDoStatus.open, ToDoStatus.inProgress));
			item.put("openToDos", Long.valueOf(projectService.getToDosCount(toDoParams)));
			toDosText = ", " + item.get("openToDos") + " open ToDos. Call todo_query with context project and originKey " + projectKey + " for the ToDos.";
		}

		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", item);
		return McpToolResult.of(result, "Project " + line(item) + ", " + members.size() + " members" + toDosText);
	}

	private Map<String,Object> toItem(ProjProject project, Set<ProjectRole> roles) {
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", project.getKey());
		item.put("title", McpToolSupport.text(project.getTitle()));
		if(StringHelper.containsNonWhitespace(project.getExternalRef())) {
			item.put("externalRef", project.getExternalRef());
		}
		item.put("status", project.getStatus().name());
		if(ProjectToolSupport.isTemplate(project)) {
			item.put("template", Boolean.TRUE);
		}
		if(StringHelper.containsNonWhitespace(project.getTeaser())) {
			item.put("teaser", project.getTeaser());
		}
		item.put("myRoles", roles.stream().map(ProjectRole::name).sorted().toList());
		item.put("url", ProjectToolSupport.url(project, null, null));
		return item;
	}

	private Map<String,Object> toMember(ProjMemberInfo memberInfo) {
		Identity identity = memberInfo.getIdentity();
		Map<String,Object> member = new LinkedHashMap<>();
		member.put("username", McpToolSupport.username(identity));
		member.put("displayName", userManager.getUserDisplayName(identity));
		member.put("roles", memberInfo.getRoles().stream().map(ProjectRole::name).sorted().toList());
		return member;
	}

	private static String line(Map<String,Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append(item.get("title")).append(" (key ").append(item.get("key"));
		if(item.containsKey("externalRef")) {
			sb.append(", ref ").append(item.get("externalRef"));
		}
		sb.append(", ").append(item.get("status"));
		if(Boolean.TRUE.equals(item.get("template"))) {
			sb.append(", template");
		}
		@SuppressWarnings("unchecked")
		List<String> myRoles = (List<String>)item.get("myRoles");
		if(!myRoles.isEmpty()) {
			sb.append(", ").append(String.join(", ", myRoles));
		}
		return sb.append(") ").append(item.get("url")).toString();
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS, "default", ACTION_LIST,
				"description", "list: the projects of the account; get: one project with members and the number of open ToDos"));
		properties.put("status", Map.of("type", "string", "enum", ProjectToolSupport.statusFilterValues(ProjectToolSupport.PROJECT_STATUS),
				"default", ProjectStatus.active.name(),
				"description", "list only: the project status " + ProjectToolSupport.PROJECT_STATUS_WORDING
						+ " or all (tab All); ignored with templates"));
		properties.put("templates", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "list only: true lists the templates the account may create a project from instead of the projects"));
		properties.put("projectKey", Map.of("type", "integer", "description", "get only, required: the key of the project"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "list only: maximum number of projects returned"));
		return Map.of("type", "object", "properties", properties, "additionalProperties", Boolean.FALSE);
	}

	static Map<String,Object> outputSchema() {
		return McpToolSupport.queryOutputSchema(itemSchema(), ProjectToolSupport.outputDefs());
	}

	static Map<String,Object> itemSchema() {
		Map<String,Object> member = Map.of("type", "object", "properties", Map.of(
				"username", Map.of("type", "string"),
				"displayName", Map.of("type", "string"),
				"roles", Map.of("type", "array", "items", Map.of("type", "string", "enum", McpToolSupport.names(ProjectRole.ALL)),
						"description", ProjectToolSupport.PROJECT_ROLES_WORDING + ", invitee (external)")));
		Map<String,Object> itemProperties = new LinkedHashMap<>();
		itemProperties.put("key", Map.of("type", "integer", "description", "The project key, the projectKey of get and the originKey of todo_query"));
		itemProperties.put("title", Map.of("type", "string"));
		itemProperties.put("externalRef", Map.of("type", "string"));
		itemProperties.put("status", Map.of("type", "string", "enum", McpToolSupport.names(ProjectToolSupport.PROJECT_STATUS),
				"description", ProjectToolSupport.PROJECT_STATUS_WORDING));
		itemProperties.put("template", Map.of("type", "boolean",
				"description", "true for a project template, the templateKey of project_modify create; absent for a project"));
		itemProperties.put("teaser", Map.of("type", "string"));
		itemProperties.put("myRoles", Map.of("type", "array", "items", Map.of("type", "string", "enum", McpToolSupport.names(ProjectRole.ALL)),
				"description", "The project roles of the account: " + ProjectToolSupport.PROJECT_ROLES_WORDING + ", invitee (external)"));
		itemProperties.put("url", Map.of("type", "string", "description", "Link to the project in OpenOlat"));
		itemProperties.put("description", Map.of("type", "string", "description", "get only"));
		itemProperties.put("members", Map.of("type", "array", "items", member, "description", "get only, at most " + MEMBERS_MAX
				+ "; missing for a template the account may only use (no project role, no manager)"));
		itemProperties.put("membersTotal", Map.of("type", "integer", "description", "get only"));
		itemProperties.put("openToDos", Map.of("type", "integer", "description", "get only, ToDos with status open or inProgress; missing when the account cannot see the ToDos of the project"));
		return Map.of("type", "object", "properties", itemProperties,
				"required", List.of("key", "status", "myRoles", "url"));
	}
}
