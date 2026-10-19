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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.FindNamedIdentityCollection;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectCopyService;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.manager.ProjectMailing;
import org.olat.modules.project.manager.ProjectMailing.ProjProjectMailTemplate;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates projects, changes their metadata and status and manages the
 * members through the ProjectService, the sequences of
 * ProjProjectEditController, ProjProjectListController and
 * ProjMemberListController. Templates, organisations and images stay in
 * the UI: the tool creates no template and does not change the template
 * flags, but create can copy a template the account may use, as the create
 * from template of ProjProjectListController does.
 * <p>
 * Organisations of a new project, with and without template: see
 * newProjectOrganisations. Without any organisation create is refused, the
 * form cannot be saved either. With the organisation module disabled the
 * service assigns the default organisation.
 * <p>
 * Members: add_members finds users only in the organisations the caller may
 * search, the rule of ImportMemberByUsernamesController. A project keeps at
 * least one owner: add_members and remove_members refuse to demote or
 * remove the last owner, the rule of ProjMemberListController (the service
 * has no such guard).
 * <p>
 * Transactions of create: ProjectService.createProject commits inside (the
 * subscription of the owner), so the project, its owner membership and the
 * creation activity are stored before the title, the texts and the
 * organisations are set. The copy of a template commits the same way. If
 * one of the later steps fails, the tool rolls the
 * open transaction back, moves the new project to the trash (status deleted,
 * the owner keeps it) and rethrows the exception, the registry answers with
 * an internal error and a reference. The guarantee: a failed create never
 * leaves an active project without title or organisation. The trashed
 * project can stay without title; the item builders of project_query map a
 * missing title to an empty string. If the move to the trash fails too, the
 * key of the project is logged with the error.
 * All other actions run in one transaction, committed or rolled back by the
 * registry.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ProjectModifyToolProvider implements McpToolProvider {

	private static final Logger log = Tracing.createLoggerFor(ProjectModifyToolProvider.class);

	public static final String NAME = "project_modify";

	static final String ACTION_CREATE = "create";
	static final String ACTION_UPDATE = "update";
	static final String ACTION_SET_STATUS = "set_status";
	static final String ACTION_ADD_MEMBERS = "add_members";
	static final String ACTION_REMOVE_MEMBERS = "remove_members";
	private static final List<String> ACTIONS = List.of(ACTION_CREATE, ACTION_UPDATE, ACTION_SET_STATUS, ACTION_ADD_MEMBERS, ACTION_REMOVE_MEMBERS);
	/** The fields of the result, the full project comes from project_query get */
	static final List<String> RESULT_FIELDS = List.of("key", "title", "status", "url");
	/** The text fields of ProjProjectEditController */
	static final int TITLE_MAX_LENGTH = 100;
	static final int EXTERNAL_REF_MAX_LENGTH = 100;
	static final int TEASER_MAX_LENGTH = 150;
	static final int DESCRIPTION_MAX_LENGTH = 4000;

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: change projects",
			"Projects",
			"Creates a project, empty or as a copy of a template, changes its title, external reference, teaser or description, sets its status "
			+ "(done closes it, Closed in the UI; active reopens it; deleted moves it to the trash) and adds or removes members with their project roles. "
			+ "Returns the key, title, status and URL of the project; project_query get returns the full project. create makes the account the owner; "
			+ "update changes only the arguments you pass, an empty string clears a text; add_members sets the passed roles for the users "
			+ "(usernames or emails, an invitation mail is sent to new members), remove_members removes every role. "
			+ "A project keeps at least one owner. Call project_query for the projectKey. "
			+ "create with templateKey copies a template with its artefacts (project_query list with templates true lists the templates the account may use); "
			+ "the passed title, externalRef, teaser and description replace the values of the template. "
			+ "create assigns the organisations in which the account is a user and manages or may create projects, else all organisations in which it is a user. "
			+ "Templates cannot be created or changed into projects, other organisations and images are managed in OpenOlat.",
			inputSchema(), outputSchema(), true, false);

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectCopyService projectCopyService;
	@Autowired
	private ProjectMailing projectMailing;
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
		return McpToolScope.WRITE;
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
		String action = McpToolSupport.string(arguments, "action", null);
		if(action == null) {
			return McpToolResult.error("The argument action is required. Use one of: " + String.join(", ", ACTIONS) + ".");
		}
		return switch(action) {
			case ACTION_CREATE -> create(context, arguments);
			case ACTION_UPDATE -> update(context, arguments);
			case ACTION_SET_STATUS -> setStatus(context, arguments);
			case ACTION_ADD_MEMBERS -> addMembers(context, arguments);
			case ACTION_REMOVE_MEMBERS -> removeMembers(context, arguments);
			default -> McpToolResult.error("Unknown action " + action + ". Use one of: " + String.join(", ", ACTIONS) + ".");
		};
	}

	private McpToolResult create(McpToolContext context, Map<String,Object> arguments) {
		if(!support.canCreateProject(context)) {
			throw new McpPermissionException("the projects of the account", "create projects");
		}
		ProjProject template = null;
		if(arguments.containsKey("templateKey")) {
			Long templateKey = McpToolSupport.longValue(arguments, "templateKey");
			template = usableTemplate(context, templateKey);
			if(template == null) {
				// only the parsed number goes into the message, never the raw input
				String described = templateKey == null ? "The argument templateKey" : "The templateKey " + templateKey;
				return McpToolResult.error(described + " is not a template the account may use. "
						+ "Call project_query list with templates true for the templates.");
			}
		}
		// Validate every argument before anything is written: the values of the template unless an argument replaces them
		String title = McpToolSupport.string(arguments, "title", template == null ? null : template.getTitle());
		if(title == null) {
			return McpToolResult.error("The action create needs the argument title.");
		}
		if(template != null && !arguments.containsKey("title") && title.length() > TITLE_MAX_LENGTH) {
			// the UI copies the long title and shortens it only on the next save of the edit form
			return McpToolResult.error("The title of the template " + template.getKey() + " has " + title.length()
					+ " characters, a project title has at most " + TITLE_MAX_LENGTH + ". Pass the argument title for the new project.");
		}
		Fields fields = fields(title,
				text(arguments, "externalRef", template == null ? null : template.getExternalRef()),
				text(arguments, "teaser", template == null ? null : template.getTeaser()),
				text(arguments, "description", template == null ? null : template.getDescription()));
		List<Organisation> organisations = newProjectOrganisations(context);
		if(organisationModule.isEnabled() && organisations.isEmpty()) {
			return McpToolResult.error("A project needs an organisation, and the account has no organisation in which it may create a project. "
					+ "An administrator must add the account to an organisation.");
		}

		Identity doer = context.identity();
		ProjProject project;
		if(template == null) {
			// The sequence of ProjProjectEditController.formOK for a new project without template
			project = projectService.createProject(doer, ProjectBCFactory.createFactoryProject(), doer);
		} else {
			// ProjProjectListController.doCreateFromTemplate: a regular project with the metadata, images and artefacts of the template
			project = projectCopyService.copyProjectFromTemplate(doer, template);
			if(project == null) {
				return McpToolResult.error("The template " + template.getKey() + " cannot be copied, it was deleted.");
			}
		}
		// createProject commits (the subscription of the owner), a rollback cannot remove the project
		try {
			// ProjProjectEditController.formOK for the new project, the edit form the user saves after the copy
			project = projectService.updateProject(doer, ProjectBCFactory.createFactory(project), project,
					fields.externalRef(), fields.title(), fields.teaser(), fields.description(), false, false);
			if(project == null) {
				throw new IllegalStateException("The new project disappeared before its update");
			}
			// The copy has the user organisations already, mostly the same set: no add and remove activities without a change
			if(template == null || !sameOrganisations(projectService.getOrganisations(project), organisations)) {
				projectService.updateProjectOrganisations(doer, project, organisations);
			}
			dbInstance.commit();
		} catch (RuntimeException e) {
			dbInstance.rollbackAndCloseSession();
			trashIncompleteProject(doer, project, e);
			throw e;
		}
		return result(project, template == null ? "Created: " : "Created from the template " + template.getKey() + ": ");
	}

	/**
	 * @param templateKey The parsed argument templateKey or null
	 * @return The template if the caller may use it, null for every other key
	 */
	private ProjProject usableTemplate(McpToolContext context, Long templateKey) {
		if(templateKey == null) {
			return null;
		}
		ProjProject template = projectService.getProject(() -> templateKey);
		// unknown and foreign keys get the same answer
		return template != null && support.isUsableTemplate(context, template) ? template : null;
	}

	/**
	 * The organisations of a new project, with and without template.
	 * <p>
	 * Rule (option B of the verification, decided by James for Florian, who
	 * may overrule it): the organisations in which the creator is
	 * administrator, project manager or has a role of the setting "create
	 * projects", intersected with the organisations in which the creator is a
	 * user. If the intersection is empty, the organisations in which the
	 * creator is a user. If both are empty, the list is empty and create is
	 * refused.
	 * <p>
	 * Why: ProjProjectEditController.initOrganisations offers the manager
	 * organisations without preselection and the user chooses; the tool has no
	 * choice. The full manager list is never taken: an administrator of a
	 * parent organisation manages every child organisation, the project must
	 * not land in all of them. The user organisations are the organisations of
	 * the copy of a template (ProjectCopyServiceImpl.copyProjectFromTemplate)
	 * and the preselection of the form for a creator without manager
	 * organisation. The result is always a subset of the user organisations,
	 * so for a copy it mostly equals the organisations the copy already has.
	 *
	 * @return The organisations, empty if the creator is a user in no organisation
	 */
	private List<Organisation> newProjectOrganisations(McpToolContext context) {
		Identity doer = context.identity();
		List<Organisation> userOrganisations = organisationService.getOrganisations(doer, OrganisationRoles.user);
		if(context.roles() == null || userOrganisations.isEmpty()) {
			return userOrganisations;
		}
		Set<OrganisationRoles> managerRoles = new HashSet<>(projectModule.getCreateRoles());
		managerRoles.add(OrganisationRoles.administrator);
		managerRoles.add(OrganisationRoles.projectmanager);
		List<Organisation> managerOrganisations = organisationService.getOrganisations(doer, context.roles(), managerRoles.toArray(OrganisationRoles[]::new));
		List<Organisation> homeOrganisations = managerOrganisations.stream()
				.filter(userOrganisations::contains)
				.toList();
		return homeOrganisations.isEmpty() ? userOrganisations : homeOrganisations;
	}

	private static boolean sameOrganisations(List<Organisation> current, List<Organisation> target) {
		Set<Long> currentKeys = new HashSet<>();
		current.forEach(organisation -> currentKeys.add(organisation.getKey()));
		Set<Long> targetKeys = new HashSet<>();
		target.forEach(organisation -> targetKeys.add(organisation.getKey()));
		return currentKeys.equals(targetKeys);
	}

	/**
	 * Moves a project whose creation failed after the commit of createProject
	 * to the trash, so no project without title and organisation stays active.
	 * A failure of this step is logged and attached to the original exception.
	 */
	private void trashIncompleteProject(Identity doer, ProjProject project, RuntimeException cause) {
		try {
			projectService.setStatusDeleted(doer, ProjectBCFactory.createFactory(project), project);
			dbInstance.commitAndCloseSession();
			log.warn("MCP project_modify create failed, the incomplete project {} is moved to the trash", project.getKey());
		} catch (RuntimeException e) {
			dbInstance.rollbackAndCloseSession();
			log.error("MCP project_modify create failed, the incomplete project {} cannot be moved to the trash", project.getKey(), e);
			cause.addSuppressed(e);
		}
	}

	private McpToolResult update(McpToolContext context, Map<String,Object> arguments) {
		ProjProject project = loadProject(arguments);
		ProjProjectSecurityCallback secCallback = callback(context, project);
		McpToolResult stateError = ProjectToolSupport.stateError(project, null, null);
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canEditProjectMetadata()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(project.getKey()), "edit the project");
		}
		if(arguments.containsKey("title") && McpToolSupport.string(arguments, "title", null) == null) {
			return McpToolResult.error("The title of a project cannot be empty.");
		}
		Fields fields = fields(McpToolSupport.string(arguments, "title", project.getTitle()),
				text(arguments, "externalRef", project.getExternalRef()),
				text(arguments, "teaser", project.getTeaser()),
				text(arguments, "description", project.getDescription()));

		Identity doer = context.identity();
		project = projectService.updateProject(doer, ProjectBCFactory.createFactory(project), project,
				fields.externalRef(), fields.title(), fields.teaser(), fields.description(),
				project.isTemplatePrivate(), project.isTemplatePublic());
		return result(project, "Updated: ");
	}

	private McpToolResult setStatus(McpToolContext context, Map<String,Object> arguments) {
		String status = McpToolSupport.string(arguments, "status", null);
		ProjectStatus target = McpToolSupport.enumValue(ProjectToolSupport.PROJECT_STATUS, status);
		if(target == null) {
			return McpToolResult.error("The action set_status needs the argument status: "
					+ String.join(", ", McpToolSupport.names(ProjectToolSupport.PROJECT_STATUS)) + ".");
		}
		ProjProject project = loadProject(arguments);
		ProjProjectSecurityCallback secCallback = callback(context, project);
		if(target == project.getStatus()) {
			return result(project, "Unchanged, the status is already " + status + ": ");
		}
		Identity doer = context.identity();
		// The rules of ProjProjectListController: done and active need the metadata right, deleted the delete right
		if(target == ProjectStatus.deleted) {
			if(!secCallback.canDeleteProject()) {
				throw new McpPermissionException(ProjectToolSupport.describeProject(project.getKey()), "delete the project");
			}
			project = projectService.setStatusDeleted(doer, ProjectBCFactory.createFactory(project), project);
		} else if(target == ProjectStatus.done) {
			// ProjProjectDashboardController offers done only for an active project
			McpToolResult stateError = ProjectToolSupport.stateError(project, null, null);
			if(stateError != null) {
				return stateError;
			}
			if(!secCallback.canEditProjectStatus()) {
				throw new McpPermissionException(ProjectToolSupport.describeProject(project.getKey()), "change the status of the project");
			}
			project = projectService.setStatusDone(doer, project);
		} else {
			if(!secCallback.canEditProjectStatus() && !(project.getStatus() == ProjectStatus.deleted && secCallback.canDeleteProject())) {
				throw new McpPermissionException(ProjectToolSupport.describeProject(project.getKey()), "change the status of the project");
			}
			project = projectService.reopen(doer, project);
		}
		return result(project, "Status set to " + status + ": ");
	}

	private McpToolResult addMembers(McpToolContext context, Map<String,Object> arguments) {
		ProjProject project = loadProject(arguments);
		ProjProjectSecurityCallback secCallback = callback(context, project);
		McpToolResult stateError = memberStateError(project);
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canEditMembers()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(project.getKey()), "manage the members of the project");
		}
		Set<ProjectRole> roles = roles(arguments);
		if(roles.isEmpty()) {
			return McpToolResult.error("The action add_members needs the argument roles with at least one of: " + String.join(", ", McpToolSupport.names(ProjectRole.PROJECT_ROLES)) + ".");
		}
		List<Identity> identities = users(context, arguments);
		if(!roles.contains(ProjectRole.owner) && removesAllOwners(project, identities)) {
			return lastOwnerError();
		}
		boolean invite = McpToolSupport.bool(arguments, "sendMail", true);

		// The sequence of ProjMemberListController.doImportMembers
		Identity doer = context.identity();
		ProjectBCFactory bcFactory = ProjectBCFactory.createFactory(project);
		ProjProjectMailTemplate template = invite ? projectMailing.createMemberAddTemplate(doer, project, bcFactory) : null;
		Map<Identity,Set<ProjectRole>> identityToRoles = new HashMap<>();
		identities.forEach(identity -> identityToRoles.put(identity, roles));
		projectService.updateMembers(doer, bcFactory, project, identityToRoles, template);
		return result(project, identities.size() + " members set to " + roles.stream().map(ProjectRole::name).sorted().toList() + ": ");
	}

	private McpToolResult removeMembers(McpToolContext context, Map<String,Object> arguments) {
		ProjProject project = loadProject(arguments);
		ProjProjectSecurityCallback secCallback = callback(context, project);
		McpToolResult stateError = memberStateError(project);
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canEditMembers()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(project.getKey()), "manage the members of the project");
		}
		List<String> values = McpToolSupport.strings(arguments, "users");
		McpToolSupport.checkList("users", values, ProjectToolSupport.MEMBERS_MAX_ITEMS, ProjectToolSupport.MEMBER_MAX_LENGTH);
		if(values.isEmpty()) {
			return McpToolResult.error("The action remove_members needs the argument users with usernames or emails of project members.");
		}
		List<Identity> candidates = projectService.getMembers(project, ProjectRole.ALL);
		List<Identity> identities = McpToolSupport.resolveIdentities("member", "this project", values, candidates);
		if(removesAllOwners(project, identities)) {
			return lastOwnerError();
		}
		Identity doer = context.identity();
		projectService.removeMembers(doer, ProjectBCFactory.createFactory(project), project, identities);
		return result(project, identities.size() + " members removed: ");
	}

	/**
	 * The state rules of RoleProjectSecurityCallback.canEditMembers: no member
	 * management in a deleted project and in a template.
	 */
	private static McpToolResult memberStateError(ProjProject project) {
		McpToolResult stateError = ProjectToolSupport.stateError(project, null, null);
		if(stateError == null && ProjectToolSupport.isTemplate(project)) {
			stateError = McpToolResult.error("The project " + project.getKey() + " is a template, a template has no members to manage.");
		}
		return stateError;
	}

	/**
	 * The rule of ProjMemberListController: a change must leave at least one
	 * owner.
	 *
	 * @param identities The members who lose the owner role
	 * @return true if no owner remains after the change
	 */
	private boolean removesAllOwners(ProjProject project, List<Identity> identities) {
		List<Identity> owners = new ArrayList<>(projectService.getMembers(project, List.of(ProjectRole.owner)));
		if(owners.isEmpty()) {
			return false;
		}
		owners.removeAll(identities);
		return owners.isEmpty();
	}

	private static McpToolResult lastOwnerError() {
		return McpToolResult.error("A project needs at least one owner. Add another owner first, then remove or change the last owner.");
	}

	/**
	 * Resolves users for add_members as the name import of the member wizard
	 * (ImportMemberByUsernamesController): username, email or other name of
	 * an active user in the organisations the caller may search (all roles
	 * of the caller except guest and invitee), guests excluded. One uniform
	 * message for every value that does not resolve, is ambiguous or names a
	 * user outside these organisations, in the order of the argument.
	 */
	private List<Identity> users(McpToolContext context, Map<String,Object> arguments) {
		List<String> values = McpToolSupport.strings(arguments, "users");
		McpToolSupport.checkList("users", values, ProjectToolSupport.MEMBERS_MAX_ITEMS, ProjectToolSupport.MEMBER_MAX_LENGTH);
		if(values.isEmpty()) {
			throw new McpInvalidArgumentException("The action add_members needs the argument users with usernames or emails.");
		}
		List<Organisation> searchableOrganisations = context.roles() == null ? List.of()
				: organisationService.getOrganisations(context.identity(), context.roles(), OrganisationRoles.valuesWithoutGuestAndInvitee());
		if(searchableOrganisations.isEmpty()) {
			// an empty list would search all organisations
			throw new McpInvalidArgumentException(values.get(0) + " cannot be added to this project.");
		}
		FindNamedIdentityCollection found = securityManager.findAndCollectIdentitiesBy(values,
				Identity.STATUS_VISIBLE_LIMIT.intValue(), searchableOrganisations);
		Set<Identity> identities = new LinkedHashSet<>();
		for(String value:values) {
			Set<Identity> named = found.getNameToIdentities().get(value.toLowerCase());
			if(named == null || named.size() != 1) {
				throw new McpInvalidArgumentException(value + " cannot be added to this project.");
			}
			identities.add(named.iterator().next());
		}
		return new ArrayList<>(identities);
	}

	private static Set<ProjectRole> roles(Map<String,Object> arguments) {
		List<String> values = McpToolSupport.strings(arguments, "roles");
		Set<ProjectRole> roles = new LinkedHashSet<>();
		for(String value:values) {
			ProjectRole role = McpToolSupport.enumValue(ProjectRole.PROJECT_ROLES, value);
			if(role == null) {
				throw new McpInvalidArgumentException(McpToolSupport.unknownValue("role", value, ProjectRole.PROJECT_ROLES));
			}
			roles.add(role);
		}
		return roles;
	}

	/**
	 * @return The argument if passed (an empty string clears it), else the current value
	 */
	private static String text(Map<String,Object> arguments, String name, String currentValue) {
		return arguments.containsKey(name) ? McpToolSupport.string(arguments, name, null) : currentValue;
	}

	/**
	 * Checks the lengths of the resolved values, the limits of ProjProjectEditController.
	 */
	private static Fields fields(String title, String externalRef, String teaser, String description) {
		McpToolSupport.checkLength("title", title, TITLE_MAX_LENGTH);
		McpToolSupport.checkLength("externalRef", externalRef, EXTERNAL_REF_MAX_LENGTH);
		McpToolSupport.checkLength("teaser", teaser, TEASER_MAX_LENGTH);
		McpToolSupport.checkLength("description", description, DESCRIPTION_MAX_LENGTH);
		return new Fields(title, externalRef, teaser, description);
	}

	private record Fields(String title, String externalRef, String teaser, String description) {
		//
	}

	private ProjProjectSecurityCallback callback(McpToolContext context, ProjProject project) {
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(projectService.getRoles(project, context.identity()).isEmpty() && !support.isManager(context, project)) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(project.getKey()), "view the project");
		}
		return secCallback;
	}

	private ProjProject loadProject(Map<String,Object> arguments) {
		Long projectKey = McpToolSupport.longValue(arguments, "projectKey");
		if(projectKey == null) {
			throw new McpInvalidArgumentException("This action needs the argument projectKey. Call project_query to find it.");
		}
		ProjProject project = projectService.getProject(() -> projectKey);
		if(project == null) {
			// an unknown key gets the answer of a foreign project, see callback
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the project");
		}
		return project;
	}

	/**
	 * The minimal item: key, title, status and URL. The text names the
	 * number of members, project_query get returns the full project.
	 */
	private McpToolResult result(ProjProject project, String prefix) {
		ProjProject reloaded = projectService.getProject(project);
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", reloaded.getKey());
		item.put("title", McpToolSupport.text(reloaded.getTitle()));
		item.put("status", reloaded.getStatus().name());
		item.put("url", ProjectToolSupport.url(reloaded, null, null));
		ProjMemberInfoSearchParameters memberParams = new ProjMemberInfoSearchParameters();
		memberParams.setProject(reloaded);
		memberParams.setRoles(ProjectRole.PROJECT_ROLES);
		int members = projectService.getMembersInfos(memberParams).size();
		return McpToolResult.of(McpToolSupport.modifyResult(item, RESULT_FIELDS), prefix + item.get("title") + " (key " + reloaded.getKey()
				+ ", " + reloaded.getStatus().name() + ", " + members + " members) " + item.get("url")
				+ McpToolSupport.fullItemHint(ProjectQueryToolProvider.NAME));
	}

	static Map<String,Object> outputSchema() {
		return McpToolSupport.modifyOutputSchema(ProjectQueryToolProvider.itemSchema(), RESULT_FIELDS, RESULT_FIELDS);
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> userItem = Map.of("type", "string", "maxLength", ProjectToolSupport.MEMBER_MAX_LENGTH);
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS,
				"description", "create: a new project with the account as owner; update: change the passed fields; set_status: active, done (Closed) or deleted; "
						+ "add_members: set the roles of users; remove_members: remove users"));
		properties.put("projectKey", Map.of("type", "integer", "description", "every action except create, required"));
		properties.put("templateKey", Map.of("type", "integer",
				"description", "create only: copy this template with its artefacts, a key of project_query list with templates true"));
		properties.put("title", Map.of("type", "string", "maxLength", TITLE_MAX_LENGTH,
				"description", "required for create, except with templateKey (default: the title of the template)"));
		properties.put("externalRef", Map.of("type", "string", "maxLength", EXTERNAL_REF_MAX_LENGTH, "description", "an empty string clears it"));
		properties.put("teaser", Map.of("type", "string", "maxLength", TEASER_MAX_LENGTH, "description", "plain text, an empty string clears it"));
		properties.put("description", Map.of("type", "string", "maxLength", DESCRIPTION_MAX_LENGTH, "description", "plain text, an empty string clears it"));
		properties.put("status", Map.of("type", "string", "enum", McpToolSupport.names(ProjectToolSupport.PROJECT_STATUS),
				"description", "set_status only, required: " + ProjectToolSupport.PROJECT_STATUS_WORDING));
		properties.put("users", Map.of("type", "array", "items", userItem, "maxItems", ProjectToolSupport.MEMBERS_MAX_ITEMS,
				"description", "add_members and remove_members, required: usernames or emails"));
		properties.put("roles", Map.of("type", "array", "items", Map.of("type", "string", "enum", McpToolSupport.names(ProjectRole.PROJECT_ROLES)),
				"description", "add_members, required: the project roles the users get, replaces their current roles: "
						+ ProjectToolSupport.PROJECT_ROLES_WORDING));
		properties.put("sendMail", Map.of("type", "boolean", "default", Boolean.TRUE,
				"description", "add_members: send the invitation mail of OpenOlat to new members"));
		return Map.of("type", "object", "properties", properties, "required", List.of("action"), "additionalProperties", Boolean.FALSE);
	}
}
