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

import static org.olat.modules.todo.mcp.ToDoToolSupport.CONTEXT_COURSE;
import static org.olat.modules.todo.mcp.ToDoToolSupport.CONTEXT_CURRICULUM;
import static org.olat.modules.todo.mcp.ToDoToolSupport.CONTEXT_PERSONAL;
import static org.olat.modules.todo.mcp.ToDoToolSupport.CONTEXT_PROJECT;
import static org.olat.modules.todo.mcp.ToDoToolSupport.CONTEXT_QUALITY;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;
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
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.manager.ProjToDoProvider;
import org.olat.modules.todo.ToDoAssignedMailSender;
import org.olat.modules.todo.ToDoModule;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.manager.PersonalToDoProvider;
import org.olat.modules.todo.mcp.ToDoToolSupport.ToDoAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates, edits, completes and deletes ToDos. The status change and the
 * soft delete work for every context through the ToDo provider of the
 * context. Create and edit work for personal ToDos through the ToDoService,
 * as the edit form of PersonalToDoProvider does, and for project ToDos through
 * the ProjectService, as ProjToDoContentEditController does, so the project
 * activity log, the notifications and the mails fire as in the UI.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ToDoModifyToolProvider implements McpToolProvider {

	public static final String NAME = "todo_modify";

	static final String ACTION_CREATE = "create";
	static final String ACTION_UPDATE = "update";
	static final String ACTION_SET_STATUS = "set_status";
	static final String ACTION_DELETE = "delete";
	private static final List<String> ACTIONS = List.of(ACTION_CREATE, ACTION_UPDATE, ACTION_SET_STATUS, ACTION_DELETE);
	/** The fields of the result, the full ToDo comes from todo_query get */
	static final List<String> RESULT_FIELDS = List.of("key", "title", "status", "url");
	/** The argument of a field and its assignee right, as ToDoTaskEditForm enables the fields */
	private static final Map<String,ToDoRight> FIELD_RIGHTS = fieldRights();
	private static final String ASSIGNEES = "assignees";
	private static final String DELEGATEES = "delegatees";
	/** The arguments ProjToDoContentEditController disables for a project template */
	private static final List<String> TEMPLATE_FIXED_ARGUMENTS = List.of("startDate", "dueDate", ASSIGNEES, DELEGATEES);

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: change ToDos",
			"ToDos",
			"Creates, edits, completes or deletes a ToDo of the OpenOlat account and returns its key, title, status and URL; todo_query get returns the full ToDo. "
			+ "Action set_status (open, inProgress, done) and action delete work for every ToDo the account may edit, "
			+ "create and update only for the contexts personal and project; ToDos of courses, curricula and quality management are edited in OpenOlat. "
			+ "Call todo_query first to find the toDoKey; for create in context project pass the project key as originKey. "
			+ "update changes only the arguments you pass; assignees, delegatees and tags replace the current lists, users by username, email or first and last name (see assignees). "
			+ ToDoToolSupport.KEY_CONTRACT,
			inputSchema(), outputSchema(), true, false);

	@Autowired
	private ToDoModule toDoModule;
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
		return McpToolScope.WRITE;
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
		String action = McpToolSupport.string(arguments, "action", null);
		if(action == null) {
			return McpToolResult.error("The argument action is required. Use one of: " + String.join(", ", ACTIONS) + ".");
		}
		return switch(action) {
			case ACTION_CREATE -> create(context, arguments);
			case ACTION_UPDATE -> update(context, arguments);
			case ACTION_SET_STATUS -> setStatus(context, arguments);
			case ACTION_DELETE -> delete(context, arguments);
			default -> McpToolResult.error("Unknown action " + action + ". Use one of: " + String.join(", ", ACTIONS) + ".");
		};
	}

	private McpToolResult setStatus(McpToolContext context, Map<String,Object> arguments) {
		ToDoStatus status = status(arguments);
		if(status == null) {
			return McpToolResult.error("The action set_status needs the argument status: open, inProgress or done. Use action delete to delete a ToDo.");
		}
		LoadedToDo loaded = loadActiveToDo(context, arguments);
		ToDoTask toDoTask = loaded.toDoTask();
		ToDoAccess access = loaded.access();
		ToDoProvider provider = enabledProvider(toDoTask);
		if(!access.canChange(ToDoRight.status)) {
			throw new McpPermissionException(ToDoToolSupport.describe(toDoTask.getKey()), "change the status of the ToDo");
		}
		provider.upateStatus(context.identity(), toDoTask, toDoTask.getOriginId(), toDoTask.getOriginSubPath(), status);
		// Some providers ignore the change, e.g. the course collection parent or an inactive project artefact
		ToDoTask reloaded = support.loadToDo(toDoTask.getKey());
		ToDoStatus stored = reloaded == null ? null : reloaded.getStatus();
		String prefix = status == stored
				? "Status set to " + status.name() + ": "
				: "Status unchanged, the origin of the ToDo controls it, the status is " + (stored == null ? "unknown" : stored.name()) + ": ";
		return result(toDoTask.getKey(), access.projToDo(), prefix);
	}

	private McpToolResult delete(McpToolContext context, Map<String,Object> arguments) {
		LoadedToDo loaded = loadActiveToDo(context, arguments);
		ToDoTask toDoTask = loaded.toDoTask();
		ToDoAccess access = loaded.access();
		ToDoProvider provider = enabledProvider(toDoTask);
		if(!access.canDelete()) {
			throw new McpPermissionException(ToDoToolSupport.describe(toDoTask.getKey()), "delete the ToDo");
		}
		provider.deleteToDoTaskSoftly(context.identity(), toDoTask);
		return result(toDoTask.getKey(), access.projToDo(), "Deleted: ");
	}

	private McpToolResult create(McpToolContext context, Map<String,Object> arguments) {
		String toDoContext = McpToolSupport.string(arguments, "context", null);
		if(toDoContext == null) {
			return McpToolResult.error("The action create needs the argument context: personal or project.");
		}
		McpToolResult notEditable = notEditableError(toDoContext);
		if(notEditable != null) {
			return notEditable;
		}
		String title = McpToolSupport.string(arguments, "title", null);
		if(title == null) {
			return McpToolResult.error("The action create needs the argument title.");
		}
		McpToolSupport.checkLength("title", title, ToDoToolSupport.TITLE_MAX_LENGTH);
		if(CONTEXT_PERSONAL.equals(toDoContext)) {
			enabledProvider(PersonalToDoProvider.TYPE);
			return createPersonal(context, arguments, title);
		}
		// The project module switch, as update, set_status and delete check it
		enabledProvider(ProjToDoProvider.TYPE);
		return createProject(context, arguments, title);
	}

	private McpToolResult createPersonal(McpToolContext context, Map<String,Object> arguments, String title) {
		Identity identity = context.identity();
		if(context.roles() == null || !toDoModule.canCreatePersonalToDoTasks(context.roles())) {
			throw new McpPermissionException("the personal ToDos of the account", "create personal ToDos");
		}
		// Validate every argument before anything is written
		Fields fields = fields(arguments, null);
		List<Identity> assignees = personalMembers(context, arguments, ASSIGNEES, List.of(identity), true);
		List<Identity> delegatees = personalMembers(context, arguments, DELEGATEES, List.of(), true);
		checkDelegatees(assignees, delegatees);
		List<String> tags = personalTags(identity, arguments);

		// The sequence of ToDoTaskEditController.formOK for a new personal ToDo
		ToDoTask toDoTask = toDoService.createToDoTask(identity, PersonalToDoProvider.TYPE, null, null, null, null, null);
		toDoTask.setAssigneeRights(ToDoToolSupport.PERSONAL_ASSIGNEE_RIGHTS);
		applyPersonal(identity, toDoTask, title, fields);
		toDoService.updateMember(identity, toDoTask, assignees, delegatees, new ToDoAssignedMailSender(identity));
		toDoService.updateTags(toDoTask, tags);
		return result(toDoTask.getKey(), null, "Created: ");
	}

	private McpToolResult createProject(McpToolContext context, Map<String,Object> arguments, String title) {
		Identity identity = context.identity();
		Long projectKey = McpToolSupport.longValue(arguments, "originKey");
		if(projectKey == null) {
			return McpToolResult.error("The action create in context project needs the project key as originKey. Call project_query to list your projects.");
		}
		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null || !support.projectCallback(context, project).canCreateToDos()) {
			throw new McpPermissionException(ToDoToolSupport.describeProject(projectKey), "create ToDos");
		}
		// Validate every argument before anything is written
		checkTemplate(project, arguments);
		Fields fields = fields(arguments, null);
		List<Identity> projectMembers = projectService.getMembers(project, ProjectRole.PROJECT_ROLES);
		List<Identity> assignees = createAssignees(arguments, projectMembers, identity);
		List<Identity> delegatees = members(arguments, DELEGATEES, projectMembers, List.of());
		checkDelegatees(assignees, delegatees);
		List<String> tags = projectTags(project, null, arguments);

		// The sequence of ProjToDoContentEditController.formOK
		ProjToDo toDo = projectService.createToDo(identity, project);
		projectService.updateToDo(identity, toDo, title, statusOrDefault(fields.status(), ToDoStatus.open), fields.priority(),
				fields.startDate(), fields.dueDate(), fields.hours(), fields.description());
		projectService.updateMembers(identity, toDo, assignees, delegatees);
		projectService.updateTags(identity, toDo, tags);
		return result(toDo.getToDoTask().getKey(), toDo, "Created: ");
	}

	private McpToolResult update(McpToolContext context, Map<String,Object> arguments) {
		LoadedToDo loaded = loadActiveToDo(context, arguments);
		ToDoTask toDoTask = loaded.toDoTask();
		ToDoTaskMembers members = loaded.members();
		ToDoAccess access = loaded.access();
		String toDoContext = support.contextOf(toDoTask.getType());
		McpToolResult notEditable = notEditableError(toDoContext);
		if(notEditable != null) {
			return notEditable;
		}
		enabledProvider(toDoTask);
		if(!access.canEdit()) {
			throw new McpPermissionException(ToDoToolSupport.describe(toDoTask.getKey()), "edit the ToDo");
		}
		for(Map.Entry<String,ToDoRight> fieldRight:FIELD_RIGHTS.entrySet()) {
			if(arguments.containsKey(fieldRight.getKey()) && !access.canChange(fieldRight.getValue())) {
				throw new McpPermissionException(ToDoToolSupport.describe(toDoTask.getKey()), "edit the " + fieldRight.getKey() + " of the ToDo");
			}
		}
		if(arguments.containsKey("title") && McpToolSupport.string(arguments, "title", null) == null) {
			return McpToolResult.error("The title of a ToDo cannot be empty.");
		}

		Identity identity = context.identity();
		if(!CONTEXT_PERSONAL.equals(toDoContext)) {
			checkTemplate(access.project(), arguments);
		}
		Fields fields = fields(arguments, toDoTask);
		String title = McpToolSupport.string(arguments, "title", toDoTask.getTitle());
		if(arguments.containsKey("title")) {
			// the stored title is not checked, the UI may hold another limit
			McpToolSupport.checkLength("title", title, ToDoToolSupport.TITLE_MAX_LENGTH);
		}
		List<Identity> currentAssignees = new ArrayList<>(members.getMembers(ToDoRole.assignee));
		List<Identity> currentDelegatees = new ArrayList<>(members.getMembers(ToDoRole.delegatee));
		boolean membersChanged = arguments.containsKey(ASSIGNEES) || arguments.containsKey(DELEGATEES);
		boolean tagsChanged = arguments.containsKey("tags");

		if(CONTEXT_PERSONAL.equals(toDoContext)) {
			List<Identity> assignees = personalMembers(context, arguments, ASSIGNEES, currentAssignees, false);
			List<Identity> delegatees = personalMembers(context, arguments, DELEGATEES, currentDelegatees, false);
			if(membersChanged) {
				checkDelegatees(assignees, delegatees);
			}
			List<String> tags = tagsChanged ? personalTags(identity, arguments) : List.of();
			// The sequence of ToDoTaskEditController.formOK for an existing personal ToDo
			applyPersonal(identity, toDoTask, title, fields);
			if(membersChanged) {
				toDoService.updateMember(identity, toDoTask, assignees, delegatees, new ToDoAssignedMailSender(identity));
			}
			if(tagsChanged) {
				toDoService.updateTags(toDoTask, tags);
			}
		} else {
			ProjToDo toDo = access.projToDo();
			List<Identity> projectMembers = projectService.getMembers(access.project(), ProjectRole.PROJECT_ROLES);
			List<Identity> assignees = members(arguments, ASSIGNEES, projectMembers, currentAssignees);
			List<Identity> delegatees = members(arguments, DELEGATEES, projectMembers, currentDelegatees);
			if(membersChanged) {
				checkDelegatees(assignees, delegatees);
			}
			List<String> tags = tagsChanged ? projectTags(access.project(), toDo, arguments) : List.of();
			// The sequence of ProjToDoContentEditController.formOK
			projectService.updateToDo(identity, toDo, title, fields.status(), fields.priority(),
					fields.startDate(), fields.dueDate(), fields.hours(), fields.description());
			if(membersChanged) {
				projectService.updateMembers(identity, toDo, assignees, delegatees);
			}
			if(tagsChanged) {
				projectService.updateTags(identity, toDo, tags);
			}
		}
		return result(toDoTask.getKey(), access.projToDo(), "Updated: ");
	}

	/**
	 * Sets the changed fields of a personal ToDo and saves it, as
	 * ToDoTaskEditController.updateReloadedToDo does.
	 */
	private void applyPersonal(Identity doer, ToDoTask toDoTask, String title, Fields fields) {
		ToDoStatus previousStatus = toDoTask.getStatus();
		ToDoStatus status = statusOrDefault(fields.status(), ToDoStatus.open);
		boolean changed = false;
		if(!Objects.equals(toDoTask.getTitle(), title)) {
			toDoTask.setTitle(title);
			changed = true;
		}
		if(!Objects.equals(toDoTask.getStatus(), status)) {
			toDoTask.setStatus(status);
			changed = true;
		}
		if(!Objects.equals(toDoTask.getPriority(), fields.priority())) {
			toDoTask.setPriority(fields.priority());
			changed = true;
		}
		if(!DateUtils.isSameDay(toDoTask.getStartDate(), fields.startDate())) {
			toDoTask.setStartDate(fields.startDate());
			changed = true;
		}
		if(!DateUtils.isSameDay(toDoTask.getDueDate(), fields.dueDate())) {
			toDoTask.setDueDate(fields.dueDate());
			changed = true;
		}
		if(!Objects.equals(toDoTask.getExpenditureOfWork(), fields.hours())) {
			toDoTask.setExpenditureOfWork(fields.hours());
			changed = true;
		}
		if(!Objects.equals(toDoTask.getDescription(), fields.description())) {
			toDoTask.setDescription(fields.description());
			changed = true;
		}
		if(changed) {
			toDoTask.setContentModifiedDate(new Date());
			toDoService.update(doer, toDoTask, previousStatus);
		}
	}

	/**
	 * Reads the content fields. An absent argument keeps the current value
	 * of the ToDo, a present empty argument clears it.
	 *
	 * @param arguments The arguments
	 * @param current The ToDo to update, null for create
	 * @return The values to write
	 */
	private Fields fields(Map<String,Object> arguments, ToDoTask current) {
		ToDoStatus status = arguments.containsKey("status") ? status(arguments) : (current == null ? null : current.getStatus());
		ToDoPriority priority = arguments.containsKey("priority") ? priority(arguments) : (current == null ? null : current.getPriority());
		Date startDate = arguments.containsKey("startDate") ? McpToolSupport.date(arguments, "startDate") : (current == null ? null : current.getStartDate());
		Date dueDate = arguments.containsKey("dueDate") ? McpToolSupport.date(arguments, "dueDate") : (current == null ? null : current.getDueDate());
		String expenditure = McpToolSupport.string(arguments, "expenditureOfWork", null);
		McpToolSupport.checkLength("expenditureOfWork", expenditure, ToDoToolSupport.EXPENDITURE_MAX_LENGTH);
		Long hours = arguments.containsKey("expenditureOfWork")
				? support.hours(expenditure)
				: (current == null ? null : current.getExpenditureOfWork());
		String description;
		if(arguments.containsKey("description")) {
			description = McpToolSupport.string(arguments, "description", null);
			// only the passed value, the UI has no limit and the stored value may be longer
			McpToolSupport.checkLength("description", description, ToDoToolSupport.DESCRIPTION_MAX_LENGTH);
		} else {
			description = current == null ? null : current.getDescription();
		}
		if(startDate != null && dueDate != null && dueDate.before(startDate)) {
			throw new McpInvalidArgumentException("The dueDate must not be before the startDate.");
		}
		return new Fields(status, priority, startDate, dueDate, hours, description);
	}

	private record Fields(ToDoStatus status, ToDoPriority priority, Date startDate, Date dueDate, Long hours, String description) {
		//
	}

	private static ToDoStatus statusOrDefault(ToDoStatus status, ToDoStatus defaultStatus) {
		return status == null ? defaultStatus : status;
	}

	private static ToDoStatus status(Map<String,Object> arguments) {
		String value = McpToolSupport.string(arguments, "status", null);
		if(value == null) {
			return null;
		}
		ToDoStatus status = McpToolSupport.enumValue(ToDoToolSupport.STATUS_VALUES, value);
		if(status == null) {
			throw new McpInvalidArgumentException(McpToolSupport.unknownValue("status", value, ToDoToolSupport.STATUS_VALUES));
		}
		return status;
	}

	private static ToDoPriority priority(Map<String,Object> arguments) {
		String value = McpToolSupport.string(arguments, "priority", null);
		if(value == null) {
			return null;
		}
		ToDoPriority priority = McpToolSupport.enumValue(ToDoToolSupport.PRIORITY_VALUES, value);
		if(priority == null) {
			throw new McpInvalidArgumentException(McpToolSupport.unknownValue("priority", value, ToDoToolSupport.PRIORITY_VALUES));
		}
		return priority;
	}

	/**
	 * The assignees of create: an absent or empty list means the own
	 * account, as the edit form of the UI preselects it.
	 *
	 * @param arguments The arguments
	 * @param candidates The allowed identities
	 * @param identity The account
	 * @return The identities
	 */
	private List<Identity> createAssignees(Map<String,Object> arguments, List<Identity> candidates, Identity identity) {
		if(McpToolSupport.strings(arguments, ASSIGNEES).isEmpty()) {
			return List.of(identity);
		}
		return members(arguments, ASSIGNEES, candidates, List.of(identity));
	}

	/**
	 * @param arguments The arguments
	 * @param name assignees or delegatees
	 * @param candidates The allowed identities
	 * @param current The value when the argument is absent
	 * @return The identities
	 */
	private List<Identity> members(Map<String,Object> arguments, String name, List<Identity> candidates, List<Identity> current) {
		return members(arguments, name, current, (role, values) -> support.resolveIdentities(role, values, candidates));
	}

	/**
	 * @param arguments The arguments
	 * @param name assignees or delegatees
	 * @param current The value when the argument is absent
	 * @param resolver Resolves the role (assignee or delegatee) and the checked values to the identities
	 * @return The identities
	 */
	private List<Identity> members(Map<String,Object> arguments, String name, List<Identity> current,
			BiFunction<String,List<String>,List<Identity>> resolver) {
		if(!arguments.containsKey(name)) {
			return current;
		}
		List<String> values = McpToolSupport.strings(arguments, name);
		McpToolSupport.checkList(name, values, ToDoToolSupport.USERS_MAX_ITEMS, ToDoToolSupport.USER_MAX_LENGTH);
		String role = ASSIGNEES.equals(name) ? "assignee" : "delegatee";
		List<Identity> identities = resolver.apply(role, values);
		if(ASSIGNEES.equals(name) && identities.isEmpty()) {
			throw new McpInvalidArgumentException("A ToDo needs at least one assignee.");
		}
		return identities;
	}

	/**
	 * The assignees or delegatees of a personal ToDo, as the edit form of
	 * PersonalToDoProvider offers them. With the module setting none the UI
	 * disables the field: the argument is accepted only when it names the
	 * current members (on create the own account as assignee, no delegatee).
	 * With buddies or all the candidates are the own account, the current
	 * members and the buddies or the users of the user search. With all a
	 * caller without the admin properties of the user search names a new
	 * user by "First Last" only (ToDoToolSupport.resolvePersonalAll).
	 *
	 * @param context The context of the call
	 * @param arguments The arguments
	 * @param name assignees or delegatees
	 * @param current The current members, on create the defaults
	 * @param create true for the action create
	 * @return The identities
	 */
	private List<Identity> personalMembers(McpToolContext context, Map<String,Object> arguments, String name,
			List<Identity> current, boolean create) {
		List<String> values = McpToolSupport.strings(arguments, name);
		if(!arguments.containsKey(name) || (create && ASSIGNEES.equals(name) && values.isEmpty())) {
			return current;
		}
		String setting = ASSIGNEES.equals(name)
				? toDoModule.getPersonalAssigneeCandidate()
				: toDoModule.getPersonalDelegateeCandidate();
		if(ToDoModule.PERSONAL_CANDIDATE_BUDDIES.equals(setting)) {
			return members(arguments, name, support.personalCandidates(context, setting, values, current), current);
		}
		if(ToDoModule.PERSONAL_CANDIDATE_ALL.equals(setting)) {
			List<Identity> candidates = support.personalCandidates(context, setting, values, current);
			return members(arguments, name, current,
					(role, checkedValues) -> support.resolvePersonalAll(context, role, checkedValues, candidates, current));
		}
		List<Identity> candidates = new ArrayList<>(current);
		candidates.add(context.identity());
		List<Identity> identities = members(arguments, name, candidates, current);
		if(!keys(identities).equals(keys(current))) {
			throw new McpInvalidArgumentException("The " + name + " of personal ToDos cannot be changed on this OpenOlat instance. "
					+ "Omit the argument " + name + ".");
		}
		return identities;
	}

	/**
	 * The rule of ToDoTaskEditForm: the delegatees must not all be assignees.
	 * A copy of ToDoTaskEditForm.validateFormLogic: keep both in sync until a
	 * shared helper in the ToDo module replaces it. Update runs the check
	 * only when assignees or delegatees are passed: old data where every
	 * delegatee is also an assignee stays editable until the members are
	 * passed (the UI blocks every save of such a ToDo).
	 *
	 * @throws McpInvalidArgumentException if every delegatee is also an assignee
	 */
	static void checkDelegatees(List<Identity> assignees, List<Identity> delegatees) {
		if(!delegatees.isEmpty() && keys(assignees).containsAll(keys(delegatees))) {
			throw new McpInvalidArgumentException("A delegatee must not also be an assignee of the ToDo.");
		}
	}

	private static Set<Long> keys(List<Identity> identities) {
		return identities.stream().map(Identity::getKey).collect(Collectors.toSet());
	}

	/**
	 * A ToDo of a project template has no dates and no members other than
	 * the creator, ProjToDoContentEditController disables these fields. The
	 * template test is the twin of ProjectToolSupport.isTemplate, which is
	 * package private in org.olat.modules.project.mcp.
	 *
	 * @throws McpInvalidArgumentException for a date or member argument in a template project
	 */
	private static void checkTemplate(ProjProject project, Map<String,Object> arguments) {
		if(project != null && (project.isTemplatePrivate() || project.isTemplatePublic())) {
			for(String name:TEMPLATE_FIXED_ARGUMENTS) {
				if(arguments.containsKey(name)) {
					throw new McpInvalidArgumentException("ToDos of a project template have no " + name
							+ ". Omit the arguments " + String.join(", ", TEMPLATE_FIXED_ARGUMENTS) + ".");
				}
			}
		}
	}

	/**
	 * @return The tags with the spelling of the tags the personal ToDos of the account already use
	 */
	private List<String> personalTags(Identity identity, Map<String,Object> arguments) {
		List<String> tags = tags(arguments);
		if(tags.isEmpty()) {
			return tags;
		}
		// The tags the edit form of PersonalToDoProvider offers
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setAssigneeOrDelegatee(identity);
		return McpToolSupport.matchTags(tags, displayNames(toDoService.getTagInfos(searchParams, null)));
	}

	/**
	 * The tags as ProjectToolSupport.tags reads them: McpToolSupport.tags
	 * then McpToolSupport.matchTags against the tags of the project. Unlike
	 * there, the artefact of the ToDo is passed, as
	 * ProjToDoContentEditController does.
	 *
	 * @return The tags with the spelling of the tags of the project
	 */
	private List<String> projectTags(ProjProject project, ProjToDo toDo, Map<String,Object> arguments) {
		List<String> tags = tags(arguments);
		if(tags.isEmpty()) {
			return tags;
		}
		// The tags ProjToDoContentEditController offers
		List<TagInfo> tagInfos = projectService.getTagInfos(project, toDo == null ? null : toDo.getArtefact());
		return McpToolSupport.matchTags(tags, displayNames(tagInfos));
	}

	private static List<String> displayNames(List<TagInfo> tagInfos) {
		return tagInfos.stream().map(TagInfo::getDisplayName).toList();
	}

	private static List<String> tags(Map<String,Object> arguments) {
		return McpToolSupport.tags(arguments, "tags", ToDoToolSupport.TAGS_MAX_ITEMS, ToDoToolSupport.TAG_MAX_LENGTH);
	}

	/**
	 * Loads the ToDo and checks that the caller may view it. An unknown key
	 * gets the answer of a foreign ToDo, the view check comes before any other
	 * answer, so the answer does not tell whether the key exists.
	 */
	private LoadedToDo loadActiveToDo(McpToolContext context, Map<String,Object> arguments) {
		Long toDoKey = McpToolSupport.longValue(arguments, "toDoKey");
		if(toDoKey == null) {
			throw new McpInvalidArgumentException("This action needs the argument toDoKey. Call todo_query to find it.");
		}
		ToDoTask toDoTask = support.loadToDo(toDoKey);
		if(toDoTask == null) {
			throw new McpPermissionException(ToDoToolSupport.describe(toDoKey), "view the ToDo");
		}
		ToDoTaskMembers members = support.loadMembers(toDoTask);
		ToDoAccess access = support.access(context, toDoTask, members);
		if(!access.canView()) {
			throw new McpPermissionException(ToDoToolSupport.describe(toDoKey), "view the ToDo");
		}
		if(ToDoStatus.deleted == toDoTask.getStatus() || toDoTask.isOriginDeleted()) {
			throw new McpInvalidArgumentException("The ToDo with key " + toDoKey + " is deleted and cannot be changed.");
		}
		return new LoadedToDo(toDoTask, members, access);
	}

	private record LoadedToDo(ToDoTask toDoTask, ToDoTaskMembers members, ToDoAccess access) {
		//
	}

	private ToDoProvider enabledProvider(ToDoTask toDoTask) {
		return enabledProvider(toDoTask.getType());
	}

	/**
	 * @param type The type of a ToDo provider
	 * @return The provider of the type
	 * @throws McpInvalidArgumentException if the provider is missing or disabled, for example the project module is off
	 */
	private ToDoProvider enabledProvider(String type) {
		ToDoProvider provider = toDoService.getProvider(type);
		if(provider == null || !provider.isEnabled()) {
			throw new McpInvalidArgumentException("The ToDos of type " + type + " are not enabled on this OpenOlat instance.");
		}
		return provider;
	}

	/**
	 * @param toDoContext The context of the ToDo or of the create argument
	 * @return The error for a context without create and update via MCP, null for personal and project
	 */
	static McpToolResult notEditableError(String toDoContext) {
		return switch(toDoContext) {
			case CONTEXT_PERSONAL, CONTEXT_PROJECT -> null;
			case CONTEXT_COURSE -> notEditable(toDoContext, "course");
			case CONTEXT_CURRICULUM -> notEditable(toDoContext, "course planner");
			case CONTEXT_QUALITY -> notEditable(toDoContext, "quality management");
			default -> McpToolResult.error("ToDos of the context " + toDoContext + " are created and edited in OpenOlat, not via MCP. "
					+ "Use context personal or project; set_status and delete work for every ToDo.");
		};
	}

	private static McpToolResult notEditable(String toDoContext, String place) {
		return McpToolResult.error("ToDos of type " + toDoContext + " are created and edited in the OpenOlat " + place
				+ ", not via MCP. Use context personal or project; set_status and delete work for every ToDo.");
	}

	/**
	 * The result with the ToDo reloaded after the change: its fields, members
	 * and tags may have changed. The project ToDo of the URL does not change.
	 *
	 * @param toDoKey The key of the ToDo
	 * @param projToDo The project ToDo, null to load it (or for the other contexts)
	 * @param prefix The prefix of the text
	 * @return The result
	 */
	private McpToolResult result(Long toDoKey, ProjToDo projToDo, String prefix) {
		ToDoTask toDoTask = support.loadToDo(toDoKey);
		Map<String,Object> item = support.toItem(toDoTask, null, projToDo);
		return McpToolResult.of(McpToolSupport.modifyResult(item, RESULT_FIELDS),
				prefix + ToDoToolSupport.line(item) + McpToolSupport.fullItemHint(ToDoQueryToolProvider.NAME));
	}

	static Map<String,Object> outputSchema() {
		return McpToolSupport.modifyOutputSchema(ToDoToolSupport.itemSchema(), RESULT_FIELDS, List.of("key", "status"));
	}

	private static Map<String,ToDoRight> fieldRights() {
		Map<String,ToDoRight> rights = new LinkedHashMap<>();
		rights.put("title", ToDoRight.title);
		rights.put("description", ToDoRight.description);
		rights.put("status", ToDoRight.status);
		rights.put("priority", ToDoRight.priority);
		rights.put("startDate", ToDoRight.startDate);
		rights.put("dueDate", ToDoRight.dueDate);
		rights.put("expenditureOfWork", ToDoRight.expenditureOfWork);
		rights.put("assignees", ToDoRight.assignees);
		rights.put("delegatees", ToDoRight.delegates);
		rights.put("tags", ToDoRight.tags);
		return Map.copyOf(rights);
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> userItem = Map.of("type", "string", "maxLength", ToDoToolSupport.USER_MAX_LENGTH);
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS,
				"description", "create: a new ToDo; update: change the passed fields; set_status: open, inProgress or done; delete: soft delete"));
		properties.put("context", Map.of("type", "string", "enum", ToDoToolSupport.MODIFY_CONTEXTS,
				"description", "create only, required: personal or project; the other contexts are edited in OpenOlat"));
		properties.put("originKey", Map.of("type", "integer", "description", "create in context project, required: the project key"));
		properties.put("toDoKey", Map.of("type", "integer", "description", "update, set_status and delete, required: the key of the ToDo as todo_query returns it, not the number in its URL"));
		properties.put("title", Map.of("type", "string", "maxLength", ToDoToolSupport.TITLE_MAX_LENGTH, "description", "required for create"));
		properties.put("description", Map.of("type", "string", "maxLength", ToDoToolSupport.DESCRIPTION_MAX_LENGTH,
				"description", "plain text, an empty string clears it"));
		properties.put("status", Map.of("type", "string", "enum", McpToolSupport.names(ToDoToolSupport.STATUS_VALUES),
				"description", ToDoToolSupport.STATUS_WORDING + "; required for set_status; create starts with open by default"));
		properties.put("priority", Map.of("type", "string", "enum", McpToolSupport.names(ToDoToolSupport.PRIORITY_VALUES),
				"description", ToDoToolSupport.PRIORITY_WORDING));
		properties.put("startDate", Map.of("type", "string", "format", "date", "description", "yyyy-MM-dd, an empty string clears it"));
		properties.put("dueDate", Map.of("type", "string", "format", "date", "description", "yyyy-MM-dd, an empty string clears it"));
		properties.put("expenditureOfWork", Map.of("type", "string", "maxLength", ToDoToolSupport.EXPENDITURE_MAX_LENGTH,
				"description", "weeks, days and whole hours, e.g. 1w 2d 3h or 5h; an empty string clears it"));
		properties.put("assignees", Map.of("type", "array", "items", userItem, "maxItems", ToDoToolSupport.USERS_MAX_ITEMS,
				"description", "users, replaces the current list; project ToDos accept project members, personal ToDos the users the ToDo settings of OpenOlat allow (by default none, the own account stays the assignee); "
				+ "name a user by username or email, or by first and last name as \"First Last\"; when the ToDo settings allow all users, a caller without the admin properties of the user search names a new user by \"First Last\" only (the own account and the current members also by the username todo_query returns), a name that more than one user carries is refused; "
				+ "project templates have no assignees; create: absent or empty means the own account; update: an empty list is refused, a ToDo keeps at least one assignee"));
		properties.put("delegatees", Map.of("type", "array", "items", userItem, "maxItems", ToDoToolSupport.USERS_MAX_ITEMS,
				"description", "users, replaces the current list, same candidates and the same name forms as assignees (username, email or \"First Last\"); a delegatee must not also be an assignee"));
		properties.put("tags", Map.of("type", "array", "items", Map.of("type", "string", "maxLength", ToDoToolSupport.TAG_MAX_LENGTH),
				"maxItems", ToDoToolSupport.TAGS_MAX_ITEMS, "description", "replaces the current list"));
		return Map.of("type", "object", "properties", properties, "required", List.of("action"), "additionalProperties", Boolean.FALSE);
	}

}
