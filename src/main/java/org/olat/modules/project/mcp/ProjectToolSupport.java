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

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjActivity.ActionTarget;
import org.olat.modules.project.ProjArtefactInfo;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileInfo;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectSecurityCallbackFactory;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.manager.ProjToDoProvider;
import org.olat.modules.project.site.ProjectsSiteDef;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The shared part of the project tools: the default security callback of
 * the caller on one project (as ProjProjectListController builds it), the
 * persons of the wire format and the links between artefacts of a project.
 * A link is addressed by a type of the tool interface and the key the model
 * knows: the ToDoTask key for a ToDo (as todo_query returns it), the object
 * key for the other types.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
class ProjectToolSupport {

	/**
	 * The artefact types of a link: the names of {@link ActionTarget}, the
	 * values of the type filter of the project timeline (timeline.filter.type.todo,
	 * .note, .file, .appointment, .milestone: To-do, Note, File, Appointment,
	 * Milestone) and decision.
	 */
	static final List<ActionTarget> LINK_TYPES = List.of(ActionTarget.toDo, ActionTarget.note, ActionTarget.file,
			ActionTarget.appointment, ActionTarget.milestone, ActionTarget.decision);
	/** The types a link argument accepts: OpenOlat has no reference activities for milestones, linkArtefacts throws for them */
	static final List<ActionTarget> LINK_INPUT_TYPES = List.of(ActionTarget.toDo, ActionTarget.note, ActionTarget.file,
			ActionTarget.appointment, ActionTarget.decision);
	/**
	 * The status of a project, the names of {@link ProjectStatus}; the UI
	 * calls them Active, Closed and Deleted (status.active, status.done,
	 * status.deleted).
	 */
	static final List<ProjectStatus> PROJECT_STATUS = List.of(ProjectStatus.values());
	static final String PROJECT_STATUS_WORDING = "active (Active), done (Closed), deleted (Deleted)";
	/**
	 * The status of an artefact of a project (ToDo, note, file, appointment,
	 * milestone, decision): active or deleted, the tab Deleted of the lists.
	 */
	static final List<ProjectStatus> ARTEFACT_STATUS = List.of(ProjectStatus.active, ProjectStatus.deleted);
	static final String ARTEFACT_STATUS_WORDING = "active (Active), deleted (Deleted)";
	/**
	 * The project roles a member can get, the names of {@link ProjectRole}
	 * with the words of the UI (role.*).
	 */
	static final String PROJECT_ROLES_WORDING = "owner (Owner), leader (Leader), projectOffice (Project office), participant (Participant), "
			+ "supplier (Business analyst / Supplier), client (Sponsor / Client), steeringCommitee (Steering committee)";
	/** The value of the status filter for every status, the tab All of the lists (tab.all) */
	static final String STATUS_ALL = "all";

	/** Upper bound of the links argument */
	static final int LINKS_MAX_ITEMS = 50;
	static final int MEMBERS_MAX_ITEMS = 50;
	static final int MEMBER_MAX_LENGTH = 255;
	static final int TAGS_MAX_ITEMS = 50;
	static final int TAG_MAX_LENGTH = 64;

	@Autowired
	private UserManager userManager;
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private SiteDefinitions siteDefinitions;

	/**
	 * The availability of every tool of the pack: the project module is
	 * enabled and the site "Projects" is enabled in the site administration,
	 * the condition under which the UI shows the projects (ProjectsSiteDef).
	 */
	boolean isPackAvailable() {
		return projectModule.isEnabled() && siteDefinitions.isSiteEnabled(ProjectsSiteDef.class);
	}

	static String describeProject(Long projectKey) {
		return "project with key " + projectKey;
	}

	/**
	 * The default callback of the project UI for the caller: the project
	 * roles plus the manager flag of an administrator or project manager of
	 * an organisation of the project.
	 */
	ProjProjectSecurityCallback projectCallback(McpToolContext context, ProjProject project) {
		Set<ProjectRole> roles = projectService.getRoles(project, context.identity());
		boolean manager = isManager(context, project);
		boolean canCreateProject = canCreateProject(context);
		return ProjectSecurityCallbackFactory.createDefaultCallback(project, roles, manager, canCreateProject);
	}

	/**
	 * The manager flag of ProjProjectListController: administrator or project
	 * manager in an organisation of the project.
	 *
	 * @param context The caller
	 * @param project The project
	 * @return true if the caller manages the project without a project role
	 */
	boolean isManager(McpToolContext context, ProjProject project) {
		Roles roles = context.roles();
		if(roles == null) {
			return false;
		}
		List<OrganisationRef> managedOrganisations = roles.getOrganisationsWithRoles(OrganisationRoles.administrator, OrganisationRoles.projectmanager);
		return !managedOrganisations.isEmpty() && projectService.isInOrganisation(project, managedOrganisations);
	}

	/**
	 * The create right of the UI (ProjectModule.canCreateProject), which also
	 * shows the segment "Templates" (ProjectsSiteController).
	 *
	 * @param context The caller
	 * @return true if the caller may create projects
	 */
	boolean canCreateProject(McpToolContext context) {
		return context.roles() != null && projectModule.canCreateProject(context.roles());
	}

	Map<String,Object> toPerson(Identity identity) {
		Map<String,Object> person = new LinkedHashMap<>();
		person.put("username", McpToolSupport.username(identity));
		person.put("displayName", userManager.getUserDisplayName(identity));
		return person;
	}

	List<Map<String,Object>> toPersons(Collection<Identity> identities) {
		if(identities == null) {
			return List.of();
		}
		return identities.stream()
				.sorted(Comparator.comparing(Identity::getKey))
				.map(this::toPerson)
				.toList();
	}

	/**
	 * @param arguments The arguments
	 * @param name The argument name, e.g. members
	 * @param project The project
	 * @param objectDescription For the error message, e.g. "this note"
	 * @return The project members named by the argument, at least one
	 * @throws McpInvalidArgumentException if the list is empty: the UI
	 *         requires at least one involved member (ProjArtefactMembersEditController)
	 */
	List<Identity> resolveMembers(Map<String,Object> arguments, String name, ProjProject project, String objectDescription) {
		if(isTemplate(project)) {
			// the edit controllers of the artefacts show the members read only in a template (e.g. ProjNoteEditController)
			throw new McpInvalidArgumentException("The artefacts of a template project have no involved members. Omit the argument " + name + ".");
		}
		List<String> values = McpToolSupport.strings(arguments, name);
		McpToolSupport.checkList(name, values, MEMBERS_MAX_ITEMS, MEMBER_MAX_LENGTH);
		if(values.isEmpty()) {
			throw new McpInvalidArgumentException("The argument " + name + " needs at least one project member involved in "
					+ objectDescription + ". Omit the argument to keep the current members.");
		}
		List<Identity> candidates = projectService.getMembers(project, ProjectRole.PROJECT_ROLES);
		return McpToolSupport.resolveIdentities("member", objectDescription, values, candidates);
	}

	/**
	 * Reads the tags argument as the tag selection of the project UI does
	 * (TagSelectionImpl, TagSelectionController): values that differ only in
	 * case are one tag, and a value matches an existing tag of the project
	 * regardless of case and takes its spelling.
	 *
	 * @param arguments The arguments
	 * @param project The project of the artefact
	 * @return The tags, empty when absent
	 */
	List<String> tags(Map<String,Object> arguments, ProjProject project) {
		List<String> tags = McpToolSupport.tags(arguments, "tags", TAGS_MAX_ITEMS, TAG_MAX_LENGTH);
		if(tags.isEmpty()) {
			return tags;
		}
		List<String> projectTags = projectService.getTagInfos(project, null).stream()
				.map(Tag::getDisplayName)
				.toList();
		return McpToolSupport.matchTags(tags, projectTags);
	}

	/**
	 * @param project A project
	 * @return true for a private or public template
	 */
	static boolean isTemplate(ProjProject project) {
		return project.isTemplatePrivate() || project.isTemplatePublic();
	}

	/**
	 * The state rules of RoleProjectSecurityCallback that do not depend on
	 * the roles of the caller: nothing changes in a deleted project, a
	 * deleted artefact cannot be changed or deleted again. The UI hides the
	 * actions; the tools answer with a state error instead of a permission
	 * error, so the model does not report missing rights. Call it after the
	 * view check and before the edit or delete check.
	 *
	 * @param project The project
	 * @param artefact The artefact or null for a project action or a create
	 * @param objectDescription For the message, e.g. "note 12"
	 * @return The error result or null if the state allows the change
	 */
	static McpToolResult stateError(ProjProject project, ProjArtefact artefact, String objectDescription) {
		if(ProjectStatus.deleted == project.getStatus()) {
			return McpToolResult.error("The project " + project.getKey() + " is deleted, nothing in it can be changed. "
					+ "Restore it first with project_modify set_status active.");
		}
		if(artefact != null && ProjectStatus.deleted == artefact.getStatus()) {
			return McpToolResult.error("The " + objectDescription + " is already deleted.");
		}
		return null;
	}

	/**
	 * The templates the caller may create a project from, the rule of the
	 * list "Templates" (ProjProjectTemplatesController) and of the template
	 * links of ProjProjectListController: templates of which the caller is a
	 * member (not invitee), and public templates of the organisations of the
	 * caller. Deleted templates are excluded, ProjectCopyService does not copy them.
	 * Without the create right the list is empty: the UI shows the segment
	 * "Templates" and the create links only with ProjectModule.canCreateProject
	 * (ProjectsSiteController, ProjProjectListController).
	 *
	 * @param context The caller
	 * @param templateKey One template or null for all
	 * @return The templates, empty if the caller cannot create projects
	 */
	List<ProjProject> usableTemplates(McpToolContext context, Long templateKey) {
		if(!canCreateProject(context)) {
			return List.of();
		}
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setIdentity(context.identity());
		searchParams.setTemplateOrganisations(context.roles().getOrganisations());
		searchParams.setTemplate(Boolean.TRUE);
		searchParams.setStatus(List.of(ProjectStatus.active, ProjectStatus.done));
		if(templateKey != null) {
			searchParams.setProjectKeys(List.of(() -> templateKey));
		}
		return projectService.getProjects(searchParams);
	}

	/**
	 * @param context The caller
	 * @param project A project
	 * @return true if the project is a template the caller may create a project from
	 */
	boolean isUsableTemplate(McpToolContext context, ProjProject project) {
		return isTemplate(project) && !usableTemplates(context, project.getKey()).isEmpty();
	}

	/**
	 * Resolves the links argument to active artefacts of the project. Every
	 * entry must resolve, the message does not tell whether a key exists in
	 * another project. A todo link takes the toDoKey of todo_query; if the key
	 * is not a ToDo of the project, the key of a project ToDo (the number in
	 * the URL of the ToDo) is accepted as a fallback, one extra query on the
	 * miss path only.
	 *
	 * @param arguments The arguments
	 * @param project The project of the note
	 * @param self The artefact of the note itself, excluded, or null for create
	 * @return The artefacts in the order of the argument, without duplicates
	 */
	Set<ProjArtefact> resolveLinks(Map<String,Object> arguments, ProjProject project, ProjArtefact self) {
		Object value = arguments.get("links");
		if(!(value instanceof Collection<?> entries)) {
			return Set.of();
		}
		if(entries.size() > LINKS_MAX_ITEMS) {
			throw new McpInvalidArgumentException("The argument links has more than " + LINKS_MAX_ITEMS + " items.");
		}
		Set<ProjArtefact> artefacts = new LinkedHashSet<>();
		for(Object entry:entries) {
			if(!(entry instanceof Map<?,?> link)) {
				throw new McpInvalidArgumentException("Every item of links must be an object with type and key.");
			}
			@SuppressWarnings("unchecked")
			Map<String,Object> linkArguments = (Map<String,Object>)link;
			ActionTarget type = McpToolSupport.enumValue(LINK_TYPES, McpToolSupport.string(linkArguments, "type", null));
			Long key = McpToolSupport.longValue(linkArguments, "key");
			if(type == ActionTarget.milestone) {
				throw new McpInvalidArgumentException("Milestones cannot be linked to other artefacts in OpenOlat.");
			}
			if(type == null || !LINK_INPUT_TYPES.contains(type) || key == null) {
				throw new McpInvalidArgumentException("Every item of links needs a type (" + String.join(", ", McpToolSupport.names(LINK_INPUT_TYPES)) + ") and an integer key.");
			}
			ProjArtefact artefact = resolveLink(type, key, project);
			if(artefact == null || (self != null && self.getKey().equals(artefact.getKey()))) {
				throw new McpInvalidArgumentException("The link " + type + " " + key + " is not an active " + type + " of this project.");
			}
			artefacts.add(artefact);
		}
		return artefacts;
	}

	private ProjArtefact resolveLink(ActionTarget type, Long key, ProjProject project) {
		ProjArtefact artefact = switch(type) {
			case toDo -> {
				ToDoTask toDoTask = toDoService.getToDoTask(() -> key);
				if(toDoTask == null || !ProjToDoProvider.TYPE.equals(toDoTask.getType()) || toDoTask.getOriginSubPath() == null) {
					yield null;
				}
				ProjToDo toDo = projectService.getToDo(toDoTask.getOriginSubPath());
				yield toDo == null ? null : toDo.getArtefact();
			}
			case note -> {
				ProjNote note = projectService.getNote(() -> key);
				yield note == null ? null : note.getArtefact();
			}
			case file -> {
				ProjFile file = projectService.getFile(() -> key);
				yield file == null ? null : file.getArtefact();
			}
			case appointment -> {
				ProjAppointment appointment = projectService.getAppointment(() -> key);
				yield appointment == null ? null : appointment.getArtefact();
			}
			case milestone -> {
				ProjMilestone milestone = projectService.getMilestone(() -> key);
				yield milestone == null ? null : milestone.getArtefact();
			}
			case decision -> {
				ProjDecision decision = projectService.getDecision(() -> key);
				yield decision == null ? null : decision.getArtefact();
			}
			default -> null;
		};
		if(!isActiveInProject(artefact, project)) {
			if(type == ActionTarget.toDo) {
				// tolerant: the number of the ToDo URL is the key of the project ToDo, not the toDoKey
				return resolveProjToDoLink(key, project);
			}
			return null;
		}
		return artefact;
	}

	private ProjArtefact resolveProjToDoLink(Long projToDoKey, ProjProject project) {
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setProject(project);
		searchParams.setToDos(List.of(() -> projToDoKey));
		List<ProjToDo> toDos = projectService.getToDos(searchParams);
		ProjArtefact artefact = toDos.isEmpty() ? null : toDos.get(0).getArtefact();
		return isActiveInProject(artefact, project) ? artefact : null;
	}

	private static boolean isActiveInProject(ProjArtefact artefact, ProjProject project) {
		return artefact != null && ProjectStatus.active == artefact.getStatus()
				&& project.getKey().equals(artefact.getProject().getKey());
	}

	/**
	 * @param artefact An artefact
	 * @return The links of the artefact to the active artefacts of the project, in the wire format
	 */
	List<Map<String,Object>> links(ProjArtefact artefact) {
		ProjArtefactItems items = projectService.getLinkedArtefactItems(artefact);
		ProjProject project = artefact.getProject();
		List<Map<String,Object>> links = new ArrayList<>();
		for(ProjToDo toDo:orEmpty(items.getToDos())) {
			links.add(link(ActionTarget.toDo, toDo.getToDoTask().getKey(), toDo.getToDoTask().getTitle(),
					url(project, ProjToDo.TYPE, toDo.getKey())));
		}
		for(ProjNote note:orEmpty(items.getNotes())) {
			links.add(link(ActionTarget.note, note.getKey(), note.getTitle(), url(project, ProjNote.TYPE, note.getKey())));
		}
		for(ProjFile file:orEmpty(items.getFiles())) {
			String title = StringHelper.containsNonWhitespace(file.getVfsMetadata().getTitle())
					? file.getVfsMetadata().getTitle() : file.getVfsMetadata().getFilename();
			links.add(link(ActionTarget.file, file.getKey(), title, url(project, ProjFile.TYPE, file.getKey())));
		}
		for(ProjAppointment appointment:orEmpty(items.getAppointments())) {
			links.add(link(ActionTarget.appointment, appointment.getKey(), appointment.getSubject(),
					url(project, ProjAppointment.TYPE, appointment.getKey())));
		}
		for(ProjMilestone milestone:orEmpty(items.getMilestones())) {
			links.add(link(ActionTarget.milestone, milestone.getKey(), milestone.getSubject(),
					url(project, ProjMilestone.TYPE, milestone.getKey())));
		}
		for(ProjDecision decision:orEmpty(items.getDecisions())) {
			links.add(link(ActionTarget.decision, decision.getKey(), decision.getTitle(),
					url(project, ProjDecision.TYPE, decision.getKey())));
		}
		links.sort(Comparator.comparing((Map<String,Object> link) -> LINK_TYPES.indexOf(ActionTarget.valueOf((String)link.get("type"))))
				.thenComparing(link -> ((Long)link.get("key"))));
		return links;
	}

	private static <T> List<T> orEmpty(List<T> list) {
		return list == null ? List.of() : list;
	}

	private static Map<String,Object> link(ActionTarget type, Long key, String title, String url) {
		Map<String,Object> link = new LinkedHashMap<>();
		link.put("type", type.name());
		link.put("key", key);
		if(StringHelper.containsNonWhitespace(title)) {
			link.put("title", title);
		}
		link.put("url", url);
		return link;
	}

	/**
	 * The URL of a project or of one of its artefacts, the business path of
	 * the project UI ({@link ProjectBCFactory}, the segment My for a project,
	 * Templates for a template).
	 *
	 * @param project The project
	 * @param artefactType The type of the artefact, e.g. {@link ProjNote#TYPE}, null for the project
	 * @param key The key of the artefact (of the project ToDo for a ToDo), null for the project
	 * @return The absolute URL into the OpenOlat UI
	 */
	static String url(ProjProject project, String artefactType, Long key) {
		String businessPath = ProjectBCFactory.createFactory(project).getBusinessPath(project, artefactType, key);
		return BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
	}

	/**
	 * @param info An artefact with its tags
	 * @return The display names of the tags, sorted, empty without tags
	 */
	static List<String> tagNames(ProjArtefactInfo info) {
		return info.getTags() == null ? List.of() : info.getTags().stream().map(Tag::getDisplayName).sorted().toList();
	}

	/** Length of the preview of a note text in the list */
	static final int PREVIEW_LENGTH = 200;
	static final String FORMAT_MARKDOWN = "markdown";

	/**
	 * The note of the wire format. The list carries a preview, get the text
	 * and the links.
	 *
	 * @param info The note with members and tags
	 * @param full true for the text and the links
	 * @return The item
	 */
	Map<String,Object> noteItem(ProjNoteInfo info, boolean full) {
		ProjNote note = info.getNote();
		ProjArtefact artefact = note.getArtefact();
		ProjProject project = artefact.getProject();
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", note.getKey());
		item.put("projectKey", project.getKey());
		if(StringHelper.containsNonWhitespace(note.getTitle())) {
			item.put("title", note.getTitle());
		}
		item.put("format", FORMAT_MARKDOWN);
		item.put("status", artefact.getStatus().name());
		item.put("created", McpToolSupport.iso(artefact.getCreationDate()));
		item.put("lastModified", McpToolSupport.iso(artefact.getContentModifiedDate()));
		item.put("creator", toPerson(artefact.getCreator()));
		item.put("tags", tagNames(info));
		item.put("members", toPersons(info.getMembers()));
		String text = note.getText() == null ? "" : note.getText();
		if(full) {
			item.put("text", text);
			item.put("links", links(artefact));
		} else {
			item.put("preview", preview(text));
			item.put("textLength", Integer.valueOf(text.length()));
		}
		item.put("url", url(project, ProjNote.TYPE, note.getKey()));
		return item;
	}

	/**
	 * The first characters of a text with the whitespace collapsed. The result
	 * is the same as <code>text.replaceAll("\\s+", " ").trim()</code> cut to
	 * PREVIEW_LENGTH characters, but the scan stops after the preview and
	 * does not copy the full text.
	 *
	 * @param text The text
	 * @return The first characters with the whitespace collapsed
	 */
	static String preview(String text) {
		int length = text.length();
		int index = 0;
		// String.trim at the start
		while(index < length && text.charAt(index) <= ' ') {
			index++;
		}
		StringBuilder sb = new StringBuilder(Math.min(PREVIEW_LENGTH, length - index));
		while(index < length && sb.length() < PREVIEW_LENGTH) {
			char ch = text.charAt(index++);
			if(isRegexWhitespace(ch)) {
				sb.append(' ');
				while(index < length && isRegexWhitespace(text.charAt(index))) {
					index++;
				}
			} else {
				sb.append(ch);
			}
		}
		// String.trim at the end applies only if nothing visible follows the preview
		if(sb.length() > 0 && sb.charAt(sb.length() - 1) <= ' ' && !hasVisibleCharacter(text, index)) {
			int end = sb.length();
			while(end > 0 && sb.charAt(end - 1) <= ' ') {
				end--;
			}
			sb.setLength(end);
		}
		return sb.toString();
	}

	/** The characters of \s in a regular expression: [ \t\n\x0B\f\r] */
	private static boolean isRegexWhitespace(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\u000B' || ch == '\f' || ch == '\r';
	}

	private static boolean hasVisibleCharacter(String text, int from) {
		for(int i=from; i<text.length(); i++) {
			if(text.charAt(i) > ' ') {
				return true;
			}
		}
		return false;
	}

	static String noteLine(Map<String,Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append(item.containsKey("title") ? item.get("title") : "(no title)").append(" (key ").append(item.get("key"));
		sb.append(", ").append(item.get("status"));
		sb.append(", modified ").append(item.get("lastModified"));
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>)item.get("tags");
		if(!tags.isEmpty()) {
			sb.append(", tags ").append(String.join(", ", tags));
		}
		return sb.append(") ").append(item.get("url")).toString();
	}

	static Map<String,Object> noteItemSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("key", Map.of("type", "integer", "description", "The note key, the noteKey of get, update and delete"));
		properties.put("projectKey", Map.of("type", "integer"));
		properties.put("title", Map.of("type", "string", "description", "absent for a note without title"));
		properties.put("format", Map.of("type", "string", "enum", List.of(FORMAT_MARKDOWN)));
		properties.put("status", artefactStatusSchema());
		properties.put("created", Map.of("type", "string", "format", "date-time"));
		properties.put("lastModified", Map.of("type", "string", "format", "date-time", "description", "last change of text or metadata"));
		properties.put("creator", McpToolSupport.personSchema());
		properties.put("tags", Map.of("type", "array", "items", Map.of("type", "string")));
		properties.put("members", Map.of("type", "array", "items", McpToolSupport.personSchema(), "description", "The project members involved in the note"));
		properties.put("preview", Map.of("type", "string", "description", "list only: the first " + PREVIEW_LENGTH + " characters of the text"));
		properties.put("textLength", Map.of("type", "integer", "description", "list only"));
		properties.put("text", Map.of("type", "string", "description", "get, create, update and delete: the markdown text"));
		properties.put("links", Map.of("type", "array", "items", linkSchema(), "description", "get, create, update and delete: the linked artefacts of the project"));
		properties.put("url", Map.of("type", "string", "description", "Link to the note in OpenOlat"));
		return Map.of("type", "object", "properties", properties,
				"required", List.of("key", "projectKey", "format", "status", "created", "lastModified", "creator", "tags", "members", "url"));
	}

	/** Maximum size of a text file returned inline, 1 MiB */
	static final long FILE_TEXT_MAX_BYTES = 1024L * 1024L;
	/** Maximum size of a binary file returned inline as base64, 4 MiB */
	static final long FILE_BINARY_MAX_BYTES = 4L * 1024L * 1024L;
	static final String MIME_OCTET_STREAM = "application/octet-stream";
	/** Types the servlet container does not know but a model reads as text */
	private static final Map<String,String> SUFFIX_MIME = Map.ofEntries(
			Map.entry("md", "text/markdown"), Map.entry("markdown", "text/markdown"), Map.entry("txt", "text/plain"),
			Map.entry("log", "text/plain"), Map.entry("csv", "text/csv"), Map.entry("json", "application/json"),
			Map.entry("yaml", "application/yaml"), Map.entry("yml", "application/yaml"), Map.entry("xml", "application/xml"),
			Map.entry("java", "text/x-java-source"), Map.entry("py", "text/x-python"), Map.entry("sh", "text/x-shellscript"),
			Map.entry("properties", "text/plain"), Map.entry("ini", "text/plain"), Map.entry("sql", "text/plain"));
	private static final Set<String> TEXT_MIMES = Set.of("application/json", "application/xml", "application/yaml",
			"application/javascript", "application/x-sh", "application/x-yaml", "image/svg+xml");

	/**
	 * @param filename A filename
	 * @return The MIME type from the servlet container, the own suffix table or the JDK, never null
	 */
	static String mimeType(String filename) {
		String mime = null;
		try {
			mime = WebappHelper.getMimeType(filename);
		} catch (Exception e) {
			// no servlet context in a unit test
		}
		if(mime == null) {
			String suffix = FileUtils.getFileSuffix(filename);
			mime = suffix == null ? null : SUFFIX_MIME.get(suffix.toLowerCase());
		}
		if(mime == null) {
			mime = URLConnection.guessContentTypeFromName(filename);
		}
		return mime == null ? MIME_OCTET_STREAM : mime;
	}

	/**
	 * @param mimeType A MIME type
	 * @return true if the content is text a model can read
	 */
	static boolean isText(String mimeType) {
		return mimeType.startsWith("text/") || TEXT_MIMES.contains(mimeType)
				|| mimeType.endsWith("+xml") || mimeType.endsWith("+json");
	}

	/**
	 * The file of the wire format: the metadata of the artefact and of the
	 * VFS entry, no content.
	 *
	 * @param info The file with members and tags
	 * @param withLinks true for the links
	 * @return The item
	 */
	Map<String,Object> fileItem(ProjFileInfo info, boolean withLinks) {
		ProjFile file = info.getFile();
		ProjArtefact artefact = file.getArtefact();
		ProjProject project = artefact.getProject();
		VFSMetadata metadata = file.getVfsMetadata();
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", file.getKey());
		item.put("projectKey", project.getKey());
		item.put("filename", metadata.getFilename());
		if(StringHelper.containsNonWhitespace(metadata.getTitle())) {
			item.put("title", metadata.getTitle());
		}
		if(StringHelper.containsNonWhitespace(metadata.getComment())) {
			item.put("description", metadata.getComment());
		}
		item.put("mimeType", mimeType(metadata.getFilename()));
		item.put("size", Long.valueOf(metadata.getFileSize()));
		item.put("status", artefact.getStatus().name());
		item.put("created", McpToolSupport.iso(artefact.getCreationDate()));
		item.put("lastModified", McpToolSupport.iso(artefact.getContentModifiedDate()));
		if(metadata.getFileLastModified() != null) {
			item.put("fileLastModified", McpToolSupport.iso(metadata.getFileLastModified()));
		}
		item.put("creator", toPerson(artefact.getCreator()));
		item.put("tags", tagNames(info));
		item.put("members", toPersons(info.getMembers()));
		if(withLinks) {
			item.put("links", links(artefact));
		}
		item.put("url", url(project, ProjFile.TYPE, file.getKey()));
		return item;
	}

	static String fileLine(Map<String,Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append(item.get("filename")).append(" (key ").append(item.get("key"));
		if(item.containsKey("title")) {
			sb.append(", \"").append(item.get("title")).append('"');
		}
		sb.append(", ").append(item.get("mimeType")).append(", ").append(item.get("size")).append(" bytes");
		sb.append(", ").append(item.get("status"));
		sb.append(", modified ").append(item.get("lastModified"));
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>)item.get("tags");
		if(!tags.isEmpty()) {
			sb.append(", tags ").append(String.join(", ", tags));
		}
		return sb.append(") ").append(item.get("url")).toString();
	}

	static Map<String,Object> fileItemSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("key", Map.of("type", "integer", "description", "The file key, the fileKey of get, update and delete"));
		properties.put("projectKey", Map.of("type", "integer"));
		properties.put("filename", Map.of("type", "string"));
		properties.put("title", Map.of("type", "string", "description", "absent for a file without title"));
		properties.put("description", Map.of("type", "string", "description", "absent for a file without description"));
		properties.put("mimeType", Map.of("type", "string"));
		properties.put("size", Map.of("type", "integer", "description", "bytes"));
		properties.put("status", artefactStatusSchema());
		properties.put("created", Map.of("type", "string", "format", "date-time"));
		properties.put("lastModified", Map.of("type", "string", "format", "date-time", "description", "last change of content or metadata"));
		properties.put("fileLastModified", Map.of("type", "string", "format", "date-time", "description", "last change of the content"));
		properties.put("creator", McpToolSupport.personSchema());
		properties.put("tags", Map.of("type", "array", "items", Map.of("type", "string")));
		properties.put("members", Map.of("type", "array", "items", McpToolSupport.personSchema(), "description", "The project members involved in the file"));
		properties.put("links", Map.of("type", "array", "items", linkSchema(), "description", "get, create, update and delete: the linked artefacts of the project"));
		properties.put("content", Map.of("type", "string", "enum", List.of("text", "image", "blob"),
				"description", "get with content=true: how the content is delivered; text files carry the field text, images and other files a binary content part"));
		properties.put("text", Map.of("type", "string", "description", "get with content=true for a text file: the content"));
		properties.put("url", Map.of("type", "string", "description", "Link to the file in OpenOlat"));
		return Map.of("type", "object", "properties", properties,
				"required", List.of("key", "projectKey", "filename", "mimeType", "size", "status", "created", "lastModified", "creator", "tags", "members", "url"));
	}

	static Map<String,Object> linkSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("type", Map.of("type", "string", "enum", McpToolSupport.names(LINK_TYPES),
				"description", "the artefact type as the type filter of the project timeline names it: toDo (To-do), note, file, appointment, milestone, decision"));
		properties.put("key", Map.of("type", "integer",
				"description", "toDo: the key of todo_query (toDoKey), not the number in the URL of the ToDo; note, file, appointment, milestone, decision: the key of the object"));
		properties.put("title", Map.of("type", "string"));
		properties.put("url", Map.of("type", "string"));
		return Map.of("type", "object", "properties", properties, "required", List.of("type", "key", "url"));
	}

	static Map<String,Object> linkInputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("type", Map.of("type", "string", "enum", McpToolSupport.names(LINK_INPUT_TYPES),
				"description", "toDo (To-do), note, file, appointment or decision; milestones cannot be linked"));
		properties.put("key", Map.of("type", "integer",
				"description", "toDo: the key of todo_query (toDoKey); the number in the URL of a project ToDo is accepted as a fallback. "
						+ "note, file, appointment, decision: the key of the object"));
		return Map.of("type", "object", "properties", properties, "required", List.of("type", "key"), "additionalProperties", Boolean.FALSE);
	}

	/**
	 * @return The input schema of the argument tags of the modify tools
	 */
	static Map<String,Object> tagsInputSchema() {
		return Map.of("type", "array", "items", Map.of("type", "string", "maxLength", TAG_MAX_LENGTH),
				"maxItems", TAGS_MAX_ITEMS, "description", "replaces the current list");
	}

	/**
	 * @param object The artefact in singular, e.g. note
	 * @return The input schema of the argument members of the modify tools
	 */
	static Map<String,Object> membersInputSchema(String object) {
		return Map.of("type", "array", "items", Map.of("type", "string", "maxLength", MEMBER_MAX_LENGTH),
				"minItems", 1, "maxItems", MEMBERS_MAX_ITEMS,
				"description", "usernames or emails of project members involved in the " + object
						+ ", at least one, replaces the current list; create defaults to the own account; not allowed in a template project");
	}

	/**
	 * @return The input schema of the argument links of the modify tools
	 */
	static Map<String,Object> linksInputSchema() {
		return Map.of("type", "array", "items", linkInputSchema(), "maxItems", LINKS_MAX_ITEMS,
				"description", "links to other artefacts of the same project, replaces the current list");
	}

	/**
	 * Reads the status filter of a list.
	 *
	 * @param value The value of the argument status
	 * @param allowed The status of the list
	 * @return The status for the search parameters, empty for all, null for an unknown value
	 */
	static List<ProjectStatus> statusFilter(String value, List<ProjectStatus> allowed) {
		if(STATUS_ALL.equals(value)) {
			return List.of();
		}
		ProjectStatus status = McpToolSupport.enumValue(allowed, value);
		return status == null ? null : List.of(status);
	}

	/**
	 * @param value The unknown value of the argument status
	 * @param allowed The status of the list
	 * @return The error text with the values of the filter
	 */
	static String unknownStatusFilter(String value, List<ProjectStatus> allowed) {
		return "Unknown status '" + value + "'. Use one of: " + String.join(", ", statusFilterValues(allowed)) + ".";
	}

	/**
	 * @param allowed The status of the list
	 * @return The names of the status and all
	 */
	static List<String> statusFilterValues(List<ProjectStatus> allowed) {
		List<String> values = new ArrayList<>(McpToolSupport.names(allowed));
		values.add(STATUS_ALL);
		return values;
	}

	/**
	 * @param objects The objects of the list in plural, e.g. notes
	 * @return The schema of the status filter of an artefact list, default active
	 */
	static Map<String,Object> artefactStatusFilterSchema(String objects) {
		return Map.of("type", "string", "enum", statusFilterValues(ARTEFACT_STATUS), "default", ProjectStatus.active.name(),
				"description", "list only: active " + objects + " (Active), deleted " + objects + " (tab Deleted) or all (tab All)");
	}

	/**
	 * @return The schema of the status of an artefact in an item
	 */
	static Map<String,Object> artefactStatusSchema() {
		return Map.of("type", "string", "enum", McpToolSupport.names(ARTEFACT_STATUS), "description", ARTEFACT_STATUS_WORDING);
	}

	/**
	 * @return The sub-schemas the output schemas of the project tools define once under $defs
	 */
	static Map<String,Map<String,Object>> outputDefs() {
		Map<String,Map<String,Object>> defs = new LinkedHashMap<>();
		defs.put("person", McpToolSupport.personSchema());
		defs.put("link", linkSchema());
		return defs;
	}
}
