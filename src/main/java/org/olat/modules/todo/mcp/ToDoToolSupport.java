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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.course.todo.CourseToDoContextFilter;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Buddy;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectSecurityCallbackFactory;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.manager.ProjToDoProvider;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.quality.manager.QualityToDoTaskProvider;
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
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.manager.PersonalToDoProvider;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The shared part of the ToDo tools: the mapping of the provider types to the
 * contexts of the tool interface, the rights of the caller on one ToDo, the
 * item of the wire format and the resolution of assignees. The rights follow
 * the UI: the "My ToDos" user tool for the generic contexts and the default
 * project callback for project ToDos.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
class ToDoToolSupport {

	static final String CONTEXT_ALL = "all";
	static final String CONTEXT_PERSONAL = "personal";
	static final String CONTEXT_PROJECT = "project";
	static final String CONTEXT_COURSE = "course";
	static final String CONTEXT_CURRICULUM = "curriculum";
	static final String CONTEXT_QUALITY = "quality";
	/** Provider types without a context of the tool interface, listed with context all only */
	static final String CONTEXT_OTHER = "other";

	static final List<String> QUERY_CONTEXTS = List.of(CONTEXT_ALL, CONTEXT_PERSONAL, CONTEXT_PROJECT,
			CONTEXT_COURSE, CONTEXT_CURRICULUM, CONTEXT_QUALITY);
	static final List<String> MODIFY_CONTEXTS = List.of(CONTEXT_PERSONAL, CONTEXT_PROJECT,
			CONTEXT_COURSE, CONTEXT_CURRICULUM, CONTEXT_QUALITY);
	/** The status a ToDo can be set to and filtered by, the names of {@link ToDoStatus} without deleted */
	static final List<ToDoStatus> STATUS_VALUES = ToDoStatus.OPEN_TO_DONE;
	/** The words of the UI for the status (task.status.*) */
	static final String STATUS_WORDING = "open (Open), inProgress (In progress), done (Done)";
	/** The priorities, the names of {@link ToDoPriority} with the words of the UI (task.priority.*) */
	static final List<ToDoPriority> PRIORITY_VALUES = List.of(ToDoPriority.values());
	static final String PRIORITY_WORDING = "urgent (Urgent), high (High), medium (Medium), low (Low)";

	/** Input bounds, the title and expenditure lengths of ToDoTaskEditForm */
	static final int TITLE_MAX_LENGTH = 120;
	static final int DESCRIPTION_MAX_LENGTH = 4000;
	static final int TAGS_MAX_ITEMS = 50;
	static final int TAG_MAX_LENGTH = 64;
	static final int USERS_MAX_ITEMS = 50;
	static final int USER_MAX_LENGTH = 255;
	static final int EXPENDITURE_MAX_LENGTH = 30;

	/** The rights of the assignees of a personal ToDo, as PersonalToDoProvider sets them */
	static final ToDoRight[] PERSONAL_ASSIGNEE_RIGHTS = new ToDoRight[] { ToDoRight.all };

	/**
	 * 1w 2d 3h, any subset in this order, or a plain number of hours. The
	 * digits are bounded, so the hours fit a long and the parser of
	 * ToDoUIFactory never throws a NumberFormatException.
	 */
	private static final Pattern EXPENDITURE_PATTERN = Pattern.compile("^\\s*(\\d{1,6}\\s*w)?\\s*(\\d{1,6}\\s*d)?\\s*(\\d{1,6}\\s*h)?\\s*$|^\\s*\\d{1,9}\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private InstantMessagingService instantMessagingService;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CourseToDoContextFilter courseContextFilter;

	/**
	 * @return true if at least one ToDo provider is enabled
	 */
	boolean isAnyProviderEnabled() {
		return toDoService.getProviders().stream().anyMatch(ToDoProvider::isEnabled);
	}

	/**
	 * @param type The type of a ToDo provider
	 * @return The context of the tool interface for this type
	 */
	String contextOf(String type) {
		if(PersonalToDoProvider.TYPE.equals(type)) {
			return CONTEXT_PERSONAL;
		}
		if(ProjToDoProvider.TYPE.equals(type)) {
			return CONTEXT_PROJECT;
		}
		if(CurriculumElementToDoProvider.TYPE.equals(type)) {
			return CONTEXT_CURRICULUM;
		}
		if(QualityToDoTaskProvider.ALL_TYPES.contains(type)) {
			return CONTEXT_QUALITY;
		}
		ToDoProvider provider = toDoService.getProvider(type);
		if(provider != null && courseContextFilter.getType().equals(provider.getContextFilterType())) {
			return CONTEXT_COURSE;
		}
		return CONTEXT_OTHER;
	}

	/**
	 * @param context A context of the tool interface or all
	 * @return The types of the enabled providers of the context, never null
	 */
	List<String> enabledTypes(String context) {
		return toDoService.getProviders().stream()
				.filter(ToDoProvider::isEnabled)
				.map(ToDoProvider::getType)
				.filter(type -> CONTEXT_ALL.equals(context) || context.equals(contextOf(type)))
				.toList();
	}

	ToDoProvider getProvider(ToDoTask toDoTask) {
		return toDoService.getProvider(toDoTask.getType());
	}

	ToDoTask loadToDo(Long toDoKey) {
		return toDoService.getToDoTask(() -> toDoKey);
	}

	ToDoTaskMembers loadMembers(ToDoTask toDoTask) {
		return toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL);
	}

	/**
	 * @param toDoKey The key of a ToDo
	 * @return The object description for an error message; it never reveals the title
	 */
	static String describe(Long toDoKey) {
		return "ToDo with key " + toDoKey;
	}

	static String describeProject(Long projectKey) {
		return "project with key " + projectKey;
	}

	/**
	 * The default project callback, exactly as ProjProjectListController builds it.
	 *
	 * @param context The caller
	 * @param project The project
	 * @return The callback
	 */
	ProjProjectSecurityCallback projectCallback(McpToolContext context, ProjProject project) {
		Set<ProjectRole> roles = projectService.getRoles(project, context.identity());
		return projectCallback(context, project, roles, isProjectManager(context, project));
	}

	private ProjProjectSecurityCallback projectCallback(McpToolContext context, ProjProject project,
			Set<ProjectRole> roles, boolean manager) {
		Roles mcpRoles = context.roles();
		boolean canCreateProject = mcpRoles != null && projectModule.canCreateProject(mcpRoles);
		return ProjectSecurityCallbackFactory.createDefaultCallback(project, roles, manager, canCreateProject);
	}

	/**
	 * @return true if the caller is administrator or project manager of an
	 *         organisation of the project, the manager flag of ProjProjectListController
	 */
	private boolean isProjectManager(McpToolContext context, ProjProject project) {
		Roles mcpRoles = context.roles();
		if(mcpRoles == null) {
			return false;
		}
		List<OrganisationRef> managedOrganisations = mcpRoles.getOrganisationsWithRoles(OrganisationRoles.administrator, OrganisationRoles.projectmanager);
		return !managedOrganisations.isEmpty() && projectService.isInOrganisation(project, managedOrganisations);
	}

	/**
	 * Computes the rights of the caller on one ToDo. Project ToDos use the
	 * default project callback, every other context the rules of the "My
	 * ToDos" user tool: the caller is assignee or delegatee and the assignee
	 * rights of the ToDo allow the operation.
	 * <p>
	 * The creator alone gives no right to view: no UI lists ToDos by creator,
	 * a former coach, quality manager or project member who created a ToDo
	 * does not see it any more. A project ToDo can be deleted only by a caller
	 * who can open the project (a project role or the manager flag), as the
	 * delete action exists in the project UI only.
	 *
	 * @param context The caller
	 * @param toDoTask The ToDo
	 * @param members The members of the ToDo
	 * @return The rights
	 */
	ToDoAccess access(McpToolContext context, ToDoTask toDoTask, ToDoTaskMembers members) {
		Long identityKey = context.identity().getKey();
		boolean creator = hasMember(members, ToDoRole.creator, identityKey);
		boolean assignee = hasMember(members, ToDoRole.assignee, identityKey);
		boolean delegatee = hasMember(members, ToDoRole.delegatee, identityKey);
		boolean member = assignee || delegatee;

		if(ProjToDoProvider.TYPE.equals(toDoTask.getType())) {
			ProjToDo projToDo = projectService.getToDo(toDoTask.getOriginSubPath());
			if(projToDo == null) {
				return new ToDoAccess(member, false, false, null, null, null);
			}
			ProjProject project = projToDo.getArtefact().getProject();
			Set<ProjectRole> projectRoles = projectService.getRoles(project, context.identity());
			boolean manager = isProjectManager(context, project);
			ProjProjectSecurityCallback secCallback = projectCallback(context, project, projectRoles, manager);
			boolean canOpenProject = manager || !projectRoles.isEmpty();
			boolean canView = member || secCallback.canViewToDos();
			boolean canEdit = secCallback.canEdit(toDoTask, creator, assignee, delegatee);
			boolean canDelete = canOpenProject && secCallback.canDeleteToDo(projToDo, context.identity());
			return new ToDoAccess(canView, canEdit, canDelete, projToDo, project, null);
		}

		// The rules of ToDoUserToolListController.ToDoUserToolSecurityCallback.
		// The user tool lists only ToDos the caller is assignee or delegatee of
		// and with assignee rights (ToDoTaskSearchParams.setAssigneeRightsNull(false)).
		boolean canView = member && toDoTask.getAssigneeRights() != null && toDoTask.getAssigneeRights().length > 0;
		boolean active = ToDoStatus.deleted != toDoTask.getStatus() && !toDoTask.isOriginDeleted();
		boolean canEdit = member && active && ToDoRight.contains(toDoTask.getAssigneeRights(), ToDoRight.EDIT_CHILDREN);
		boolean canDelete = member && active && ToDoRight.contains(toDoTask.getAssigneeRights(), ToDoRight.delete);
		ToDoRight[] rights = member && active ? toDoTask.getAssigneeRights() : ToDoRight.EMPTY_ARRAY;
		return new ToDoAccess(canView, canEdit, canDelete, null, null, rights);
	}

	private static boolean hasMember(ToDoTaskMembers members, ToDoRole role, Long identityKey) {
		if(members == null) {
			return false;
		}
		return members.getMembers(role).stream().anyMatch(member -> identityKey.equals(member.getKey()));
	}

	/**
	 * Sorts by due date (ToDos without due date last), then by key.
	 */
	static final Comparator<ToDoTask> DUE_DATE_ORDER = Comparator
			.comparing(ToDoTask::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing(ToDoTask::getKey);

	/**
	 * Builds the items of one page with bulk queries for the members, the
	 * tags and the project ToDos of the URLs.
	 *
	 * @param page The ToDos, at most the limit
	 * @return The items in the order of the page
	 */
	List<Map<String,Object>> toItems(List<ToDoTask> page) {
		if(page.isEmpty()) {
			return new ArrayList<>(0);
		}
		Map<Long,ToDoTaskMembers> groupKeyToMembers = toDoService.getToDoTaskGroupKeyToMembers(page, ToDoRole.ALL);
		Map<Long,List<String>> toDoKeyToTags = loadTags(page);
		Map<String,ProjToDo> identifierToProjToDo = loadProjToDos(page);

		List<Map<String,Object>> items = new ArrayList<>(page.size());
		for(ToDoTask toDoTask:page) {
			ToDoTaskMembers members = groupKeyToMembers.get(toDoTask.getBaseGroup().getKey());
			items.add(toItem(toDoTask, members, toDoKeyToTags.getOrDefault(toDoTask.getKey(), List.of()), identifierToProjToDo));
		}
		return items;
	}

	/**
	 * Builds the item of one ToDo with the objects the caller has already
	 * loaded; only the tags are loaded here.
	 *
	 * @param toDoTask The ToDo
	 * @param members The members of the ToDo as loaded for the access check, null to load them
	 * @param projToDo The project ToDo of a project ToDo, null to load it (or for the other contexts)
	 * @return The item
	 */
	Map<String,Object> toItem(ToDoTask toDoTask, ToDoTaskMembers members, ProjToDo projToDo) {
		List<ToDoTask> toDoTasks = List.of(toDoTask);
		if(members == null) {
			members = loadMembers(toDoTask);
		}
		Map<String,ProjToDo> identifierToProjToDo = projToDo == null
				? loadProjToDos(toDoTasks)
				: Map.of(projToDo.getIdentifier(), projToDo);
		return toItem(toDoTask, members, loadTags(toDoTasks).getOrDefault(toDoTask.getKey(), List.of()), identifierToProjToDo);
	}

	private Map<Long,List<String>> loadTags(List<ToDoTask> toDoTasks) {
		ToDoTaskSearchParams tagParams = new ToDoTaskSearchParams();
		tagParams.setToDoTasks(toDoTasks);
		return toDoService.getToDoTaskTags(tagParams).stream()
				.collect(Collectors.groupingBy(tag -> tag.getToDoTask().getKey(),
						Collectors.mapping(ToDoTaskTag::getTag, Collectors.mapping(tag -> tag.getDisplayName(), Collectors.toList()))));
	}

	/**
	 * Loads the project ToDos of the project ToDos of a page with one query,
	 * the query of ProjToDoProvider.getBusinessPath for all of them.
	 *
	 * @param toDoTasks The ToDos of all types
	 * @return The project ToDos by identifier, the originSubPath of the ToDo
	 */
	private Map<String,ProjToDo> loadProjToDos(List<ToDoTask> toDoTasks) {
		Set<String> identifiers = toDoTasks.stream()
				.filter(toDoTask -> ProjToDoProvider.TYPE.equals(toDoTask.getType()))
				.map(ToDoTask::getOriginSubPath)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		if(identifiers.isEmpty()) {
			return Map.of();
		}
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setIdentifiers(identifiers);
		return projectService.getToDos(searchParams).stream()
				.collect(Collectors.toMap(ProjToDo::getIdentifier, Function.identity(), (first, second) -> first));
	}

	private Map<String,Object> toItem(ToDoTask toDoTask, ToDoTaskMembers members, List<String> tags,
			Map<String,ProjToDo> identifierToProjToDo) {
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", toDoTask.getKey());
		putIfNotNull(item, "title", toDoTask.getTitle());
		putIfNotNull(item, "status", toDoTask.getStatus() == null ? null : toDoTask.getStatus().name());
		putIfNotNull(item, "priority", toDoTask.getPriority() == null ? null : toDoTask.getPriority().name());
		putIfNotNull(item, "startDate", McpToolSupport.isoDate(toDoTask.getStartDate()));
		putIfNotNull(item, "dueDate", McpToolSupport.isoDate(toDoTask.getDueDate()));
		putIfNotNull(item, "doneDate", McpToolSupport.iso(toDoTask.getDoneDate()));
		putIfNotNull(item, "expenditureOfWork", ToDoUIFactory.format(toDoService.getExpenditureOfWork(toDoTask.getExpenditureOfWork())));
		putIfNotNull(item, "description", StringHelper.containsNonWhitespace(toDoTask.getDescription()) ? toDoTask.getDescription() : null);
		item.put("tags", tags);
		item.put("assignees", toPersons(members, ToDoRole.assignee));
		item.put("delegatees", toPersons(members, ToDoRole.delegatee));

		Map<String,Object> context = new LinkedHashMap<>();
		context.put("type", toDoTask.getType());
		context.put("context", contextOf(toDoTask.getType()));
		putIfNotNull(context, "originKey", toDoTask.getOriginId());
		putIfNotNull(context, "originTitle", toDoTask.getOriginTitle());
		putIfNotNull(context, "originSubTitle", toDoTask.getOriginSubTitle());
		item.put("context", context);
		putIfNotNull(item, "url", url(toDoTask, identifierToProjToDo));
		return item;
	}

	private List<Map<String,Object>> toPersons(ToDoTaskMembers members, ToDoRole role) {
		if(members == null) {
			return List.of();
		}
		return members.getMembers(role).stream()
				.sorted(Comparator.comparing(Identity::getKey))
				.map(this::toPerson)
				.toList();
	}

	Map<String,Object> toPerson(Identity identity) {
		Map<String,Object> person = new LinkedHashMap<>();
		person.put("username", McpToolSupport.username(identity));
		person.put("displayName", userManager.getUserDisplayName(identity));
		return person;
	}

	/**
	 * @param toDoTask The ToDo
	 * @param identifierToProjToDo The preloaded project ToDos
	 * @return The URL, null if the ToDo has no business path
	 */
	private String url(ToDoTask toDoTask, Map<String,ProjToDo> identifierToProjToDo) {
		ToDoProvider provider = getProvider(toDoTask);
		if(provider == null) {
			return null;
		}
		String businessPath;
		if(ProjToDoProvider.TYPE.equals(toDoTask.getType())) {
			// ProjToDoProvider.getBusinessPath with the preloaded project ToDo
			ProjToDo projToDo = toDoTask.getOriginSubPath() == null ? null : identifierToProjToDo.get(toDoTask.getOriginSubPath());
			if(projToDo == null) {
				return null;
			}
			ProjProject project = projToDo.getArtefact().getProject();
			businessPath = ProjectBCFactory.createFactory(project).getBusinessPath(project, ProjToDo.TYPE, projToDo.getKey());
		} else {
			businessPath = provider.getBusinessPath(toDoTask);
		}
		return businessPath == null ? null : BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
	}

	private static void putIfNotNull(Map<String,Object> map, String name, Object value) {
		if(value != null) {
			map.put(name, value);
		}
	}

	/**
	 * @param item An item of {@link #toItems(List)} or {@link #toItem(ToDoTask, ToDoTaskMembers, ProjToDo)}
	 * @return One line of the text rendering
	 */
	static String line(Map<String,Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(item.get("status")).append("] ").append(item.get("title"))
		  .append(" (key ").append(item.get("key"));
		if(item.get("dueDate") != null) {
			sb.append(", due ").append(item.get("dueDate"));
		}
		@SuppressWarnings("unchecked")
		Map<String,Object> context = (Map<String,Object>)item.get("context");
		sb.append(", ").append(context.get("context"));
		if(context.get("originTitle") != null) {
			sb.append(' ').append(context.get("originTitle"));
		}
		sb.append(')');
		if(item.get("url") != null) {
			sb.append(' ').append(item.get("url"));
		}
		return sb.toString();
	}

	/**
	 * @param value The expenditure of work, e.g. 1w 2d 3h
	 * @return The hours, null for null
	 * @throws McpInvalidArgumentException for an invalid format
	 */
	Long hours(String value) {
		if(value == null) {
			return null;
		}
		if(!EXPENDITURE_PATTERN.matcher(value).matches()) {
			throw new McpInvalidArgumentException("The argument expenditureOfWork must have the format 1w 2d 3h "
					+ "(weeks, days, whole hours, each part optional, at most 6 digits each) or a number of hours (at most 9 digits), got: " + value);
		}
		try {
			return toDoService.getHours(ToDoUIFactory.parseHours(value));
		} catch (NumberFormatException e) {
			// the pattern prevents it, a clear answer in any case
			throw new McpInvalidArgumentException("The argument expenditureOfWork is not a valid number of weeks, days and hours: " + value);
		}
	}

	/**
	 * Resolves usernames, email addresses or "First Last" against the
	 * candidates of the context: the project members, the own account and
	 * the current members, or the buddies. A "First Last" of more than one
	 * candidate is refused. The candidates of the setting all come from
	 * {@link #personalCandidates} and are resolved with
	 * {@link #resolvePersonalAll}. An unknown user and a user outside the
	 * candidates get the same answer, so the tool cannot be used to probe the
	 * accounts of the instance.
	 *
	 * @param role assignee or delegatee, for the message
	 * @param values The usernames, emails or "First Last"
	 * @param candidates The allowed identities
	 * @return The identities in the order of the values, without duplicates
	 * @throws McpInvalidArgumentException for a value that matches no candidate or more than one
	 */
	List<Identity> resolveIdentities(String role, List<String> values, Collection<Identity> candidates) {
		return McpToolSupport.resolveIdentities(role, "this ToDo", values, candidates, McpToolSupport::matchesUser);
	}

	/**
	 * Resolves the values of a personal ToDo with the module setting all, by
	 * the fields the user search of the UI offers the caller
	 * (UserSearchForm, userPropertiesContext.xml): a caller with the admin
	 * properties of the user search ({@link BaseSecurityModule#isUserAllowedAdminProps})
	 * names a user by username, nick name, email or "First Last". Any other
	 * caller names a user by "First Last" only, so the tool does not map an
	 * email address to an account; the own account and the current members,
	 * whose username todo_query already returns, also match by that username.
	 * A "First Last" of more than one candidate is refused.
	 *
	 * @param context The context of the call
	 * @param role assignee or delegatee, for the message
	 * @param values The names of the argument
	 * @param candidates The candidates of {@link #personalCandidates}
	 * @param current The current members of the role
	 * @return The identities in the order of the values, without duplicates
	 * @throws McpInvalidArgumentException for an unknown, out of scope or ambiguous value
	 */
	List<Identity> resolvePersonalAll(McpToolContext context, String role, List<String> values,
			Collection<Identity> candidates, Collection<Identity> current) {
		if(isUserAllowedAdminProps(context)) {
			return McpToolSupport.resolveIdentities(role, "this ToDo", values, candidates, McpToolSupport::matchesUser);
		}
		Set<Long> knownKeys = current.stream().map(Identity::getKey).collect(Collectors.toSet());
		knownKeys.add(context.identity().getKey());
		return McpToolSupport.resolveIdentities(role, "this ToDo", values, candidates,
				(candidate, value) -> knownKeys.contains(candidate.getKey()) && value.equalsIgnoreCase(McpToolSupport.username(candidate)));
	}

	private boolean isUserAllowedAdminProps(McpToolContext context) {
		return context.roles() != null && securityModule.isUserAllowedAdminProps(context.roles());
	}

	/**
	 * The candidates of the assignees or the delegatees of a personal ToDo
	 * for the settings buddies and all of the ToDo module, as
	 * PersonalToDoProvider.createEditController offers them: the own account,
	 * the current members (the UI keeps them preselected) and the buddies,
	 * or for all the users of the values the user search of the UI finds
	 * (UserSearchController: visible accounts in the organisations of the
	 * caller without the guest and invitee roles). For all only the users
	 * named by the values are loaded, never the whole user base, and an
	 * unknown user gets the same answer as a user outside the search scope.
	 * Resolve the candidates of all with {@link #resolvePersonalAll}.
	 *
	 * @param context The context of the call
	 * @param setting The module setting, {@link ToDoModule#PERSONAL_CANDIDATE_BUDDIES} or {@link ToDoModule#PERSONAL_CANDIDATE_ALL}
	 * @param values The names of the argument
	 * @param current The current members of the role
	 * @return The candidates
	 */
	List<Identity> personalCandidates(McpToolContext context, String setting, List<String> values, Collection<Identity> current) {
		Identity identity = context.identity();
		Map<Long,Identity> candidates = new LinkedHashMap<>();
		candidates.put(identity.getKey(), identity);
		current.forEach(member -> candidates.putIfAbsent(member.getKey(), member));
		if(ToDoModule.PERSONAL_CANDIDATE_BUDDIES.equals(setting)) {
			buddies(identity).forEach(buddy -> candidates.putIfAbsent(buddy.getKey(), buddy));
		} else if(ToDoModule.PERSONAL_CANDIDATE_ALL.equals(setting)) {
			searchableUsers(context, values).forEach(user -> candidates.putIfAbsent(user.getKey(), user));
		}
		return new ArrayList<>(candidates.values());
	}

	/**
	 * The buddies of PersonalToDoProvider.getMemberCandidates (private
	 * there). A copy: keep both in sync until a shared method in the ToDo
	 * module replaces it.
	 */
	private List<Identity> buddies(Identity identity) {
		Set<Long> buddyIdentityKeys = instantMessagingService.getBuddyGroups(identity, true).stream()
				.flatMap(buddyGroup -> buddyGroup.getBuddy().stream())
				.filter(buddy -> !buddy.isAnonym())
				.map(Buddy::getIdentityKey)
				.collect(Collectors.toSet());
		return buddyIdentityKeys.isEmpty() ? List.of() : securityManager.loadIdentityByKeys(buddyIdentityKeys);
	}

	/**
	 * The users named by the values within the scope of the user search of
	 * the UI. A copy of the scope of UserSearchController (organisations of
	 * the caller without guest and invitee roles, STATUS_VISIBLE_LIMIT):
	 * keep both in sync until a shared method replaces it. A caller without
	 * the admin properties of the user search finds users by "First Last"
	 * only, as {@link #resolvePersonalAll} resolves them.
	 */
	private List<Identity> searchableUsers(McpToolContext context, List<String> values) {
		if(values.isEmpty() || context.roles() == null) {
			return List.of();
		}
		boolean adminProps = isUserAllowedAdminProps(context);
		Set<Long> identityKeys = securityManager.findIdentitiesBy(values).stream()
				.map(FindNamedIdentity::getIdentity)
				.filter(identity -> adminProps || values.stream().anyMatch(value -> McpToolSupport.matchesDisplayName(identity, value)))
				.map(Identity::getKey)
				.collect(Collectors.toSet());
		if(identityKeys.isEmpty()) {
			return List.of();
		}
		List<Organisation> searchableOrganisations = organisationService.getOrganisations(context.identity(), context.roles(),
				OrganisationRoles.valuesWithoutGuestAndInvitee());
		if(searchableOrganisations.isEmpty()) {
			// Stricter than the UI: without an organisation the UI search applies
			// no organisation filter at all (SearchIdentityParams.hasOrganisations),
			// MCP finds nobody
			return List.of();
		}
		SearchIdentityParams params = new SearchIdentityParams();
		params.setIdentityKeys(identityKeys);
		params.setOrganisations(searchableOrganisations);
		params.setStatus(Identity.STATUS_VISIBLE_LIMIT);
		return securityManager.getIdentitiesByPowerSearch(params, 0, -1);
	}

	/**
	 * The rights of the caller on one ToDo.
	 *
	 * @param canView The caller may see the ToDo
	 * @param canEdit The caller may edit the ToDo and change its status
	 * @param canDelete The caller may delete the ToDo
	 * @param projToDo The project ToDo, null for other contexts
	 * @param project The project, null for other contexts
	 * @param assigneeRights The rights of the caller as assignee or delegatee, null for project ToDos
	 */
	record ToDoAccess(boolean canView, boolean canEdit, boolean canDelete, ProjToDo projToDo, ProjProject project,
			ToDoRight[] assigneeRights) {

		/**
		 * The per-field rule of ToDoTaskEditForm.updateUIByAssigneeRight. A
		 * project ToDo follows the project callback, which has no field rights.
		 *
		 * @param right The right of one field, e.g. status or title
		 * @return true if the caller may change the field
		 */
		boolean canChange(ToDoRight right) {
			if(assigneeRights == null) {
				return canEdit;
			}
			return canEdit && ToDoRight.contains(assigneeRights, right);
		}
	}

	/**
	 * A ToDo has two keys: the key of the ToDo task, which every ToDo tool
	 * returns as key and takes as toDoKey, and the key of the project ToDo,
	 * which ends the URL of a project ToDo. Only the first is the contract.
	 */
	static final String KEY_CONTRACT = "The key of a ToDo is its toDoKey: use it for every argument and in links of type toDo. "
			+ "The number at the end of the URL of a project ToDo is a different, internal key; do not use it as toDoKey.";

	/**
	 * @return The JSON schema of one ToDo item
	 */
	static Map<String,Object> itemSchema() {
		Map<String,Object> person = McpToolSupport.personSchema();
		Map<String,Object> context = Map.of("type", "object", "properties", Map.of(
				"type", Map.of("type", "string", "description", "The provider type, e.g. personal, project, course.todo.individual"),
				"context", Map.of("type", "string", "enum", List.of(CONTEXT_PERSONAL, CONTEXT_PROJECT, CONTEXT_COURSE, CONTEXT_CURRICULUM, CONTEXT_QUALITY, CONTEXT_OTHER)),
				"originKey", Map.of("type", "integer", "description", "Key of the context object: project key, course key or curriculum key"),
				"originTitle", Map.of("type", "string"),
				"originSubTitle", Map.of("type", "string")));

		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("key", Map.of("type", "integer", "description", "The toDoKey: use it for every argument and in links. "
				+ "The number in the url is a different, internal key."));
		properties.put("title", Map.of("type", "string"));
		properties.put("status", Map.of("type", "string", "enum", McpToolSupport.names(ToDoStatus.values()),
				"description", STATUS_WORDING + ", deleted (Deleted)"));
		properties.put("priority", Map.of("type", "string", "enum", McpToolSupport.names(PRIORITY_VALUES), "description", PRIORITY_WORDING));
		properties.put("startDate", Map.of("type", "string", "format", "date"));
		properties.put("dueDate", Map.of("type", "string", "format", "date"));
		properties.put("doneDate", Map.of("type", "string", "format", "date-time"));
		properties.put("expenditureOfWork", Map.of("type", "string", "description", "e.g. 1w 2d 3h"));
		properties.put("description", Map.of("type", "string"));
		properties.put("tags", Map.of("type", "array", "items", Map.of("type", "string")));
		properties.put("assignees", Map.of("type", "array", "items", person));
		properties.put("delegatees", Map.of("type", "array", "items", person));
		properties.put("context", context);
		properties.put("url", Map.of("type", "string", "description", "Link to the ToDo in OpenOlat; its last number is not the toDoKey"));
		return Map.of("type", "object", "properties", properties,
				"required", List.of("key", "status", "tags", "assignees", "delegatees", "context"));
	}

	/**
	 * @return The output schema: items, total and truncated for a list, item for a single ToDo
	 */
	static Map<String,Object> outputSchema() {
		return McpToolSupport.queryOutputSchema(itemSchema(), Map.of("person", McpToolSupport.personSchema()));
	}
}
