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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.proxy.HibernateProxy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.manager.McpJsonSchemaValidator;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool project_modify: create, metadata update,
 * status changes and member management with the rights of the UI.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class ProjectModifyToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(ProjectModifyToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private ProjectModifyToolProvider sut;

	private boolean organisationEnabledBefore;

	/**
	 * The organisation tests need the organisation module: without it the
	 * service puts every project into the default organisation. An earlier
	 * test of the suite may leave the module disabled.
	 */
	@Before
	public void enableOrganisations() {
		organisationEnabledBefore = organisationModule.isEnabled();
		organisationModule.setEnabled(true);
	}

	@After
	public void restoreOrganisations() {
		organisationModule.setEnabled(organisationEnabledBefore);
	}

	@Test
	public void definition() {
		Assert.assertEquals("project_modify", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.WRITE, sut.getScope());
		Assert.assertTrue(sut.getDefinition().destructive());
		Assert.assertFalse(sut.getDefinition().idempotent());
		assertThat(sut.getDefinition().description()).contains("project_query").contains("add_members").contains("templateKey");
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "create", "title", "T", "teaser", "t", "description", "d", "externalRef", "R-1")).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "set_status", "projectKey", 1, "status", "done")).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "add_members", "projectKey", 1, "users", List.of("jdoe"), "roles", List.of("participant", "supplier"), "sendMail", false)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "set_status", "projectKey", 1, "status", "archived")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "add_members", "projectKey", 1, "users", List.of("jdoe"), "roles", List.of("admin"))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "projectKey", 1, "members", List.of())).valid());
	}

	@Test
	public void invalidActions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-1");

		McpToolResult missing = invoke(context(owner), Map.of());
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("action is required");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "update", "title", "x"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknown = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "update", "projectKey", Long.valueOf(-3), "title", "x")));
		Assert.assertEquals("project with key -3", unknown.getObjectDescription());
		Assert.assertEquals("view the project", unknown.getPermission());

		// project.create.roles is projectmanager by default: a plain user cannot create, an administrator can
		try {
			invoke(context(owner), Map.of("action", "create", "title", "x"));
			Assert.fail("A plain user cannot create projects");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create projects", e.getPermission());
		}
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-pm-1a");
		McpToolResult noTitle = invoke(context(admin), Map.of("action", "create"));
		Assert.assertTrue(noTitle.error());
		assertThat(noTitle.text()).contains("title");
	}

	@Test
	public void create() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-pm-2");
		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("title", "Alpha " + random());
		arguments.put("externalRef", "A-1");
		arguments.put("teaser", "The teaser");
		arguments.put("description", "The description");
		McpToolResult result = invoke(context(creator), arguments);
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		Long key = (Long)item.get("key");
		ProjProject project = projectService.getProject(() -> key);
		Assert.assertEquals(arguments.get("title"), project.getTitle());
		Assert.assertEquals("A-1", project.getExternalRef());
		Assert.assertEquals("The teaser", project.getTeaser());
		Assert.assertEquals("The description", project.getDescription());
		Assert.assertEquals(ProjectStatus.active, project.getStatus());
		Assert.assertFalse(project.isTemplatePrivate());
		assertThat(projectService.getRoles(project, creator)).containsExactly(ProjectRole.owner);
		Assert.assertEquals(arguments.get("title"), item.get("title"));
		Assert.assertEquals("active", item.get("status"));
		assertThat(result.text()).startsWith("Created: " + arguments.get("title") + " (key " + key)
			.contains(", 1 members) ")
			.endsWith("Call project_query get for the full item.");
		assertThat(actions(project, creator)).contains(Action.projectCreate, Action.projectContentUpdate);
		// V11: of the organisations the edit form offers (manager or create role) the ones in which the creator is a user;
		// an administrator manages many organisations, the project does not land in all of them
		List<Organisation> managed = organisationService.getOrganisations(creator, securityManager.getRoles(creator),
				OrganisationRoles.administrator, OrganisationRoles.projectmanager);
		List<Organisation> expected = managed.stream()
				.filter(organisationService.getOrganisations(creator, OrganisationRoles.user)::contains)
				.toList();
		assertThat(projectService.getOrganisations(project))
			.isNotEmpty()
			.containsExactlyInAnyOrderElementsOf(expected);
		Assert.assertFalse(project.isTemplatePrivate() || project.isTemplatePublic());
	}

	/**
	 * F3, rule B: a project manager of organisation B who is a user in
	 * organisation A, without overlap, gets the project in A, the user
	 * organisation; never the full manager list.
	 */
	@Test
	public void create_organisationsOfTheProjectManagerRole() {
		Organisation userOrganisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Organisation managerOrganisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-40", userOrganisation, JunitTestHelper.PWD);
		organisationService.addMember(managerOrganisation, creator, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(creator), Map.of("action", "create", "title", "Managed " + random())));
		dbInstance.commitAndCloseSession();
		ProjProject project = projectService.getProject(() -> (Long)item.get("key"));
		assertThat(projectService.getOrganisations(project)).containsExactly(userOrganisation);

		// manager in both organisations: the one in which the creator is also a user
		organisationService.addMember(userOrganisation, creator, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		Map<String,Object> homeItem = item(invoke(context(creator), Map.of("action", "create", "title", "Home " + random())));
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getOrganisations(projectService.getProject(() -> (Long)homeItem.get("key")))).containsExactly(userOrganisation);
	}

	/**
	 * F3, rule B: a project manager of a parent organisation gets none of the
	 * child organisations the role reaches, only the intersection with the
	 * user organisations.
	 */
	@Test
	public void create_parentManager_notInEveryChild() {
		Organisation parent = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Organisation child1 = organisationService.createOrganisation(random(), random(), random(), parent,
				null, JunitTestHelper.getDefaultActor());
		Organisation child2 = organisationService.createOrganisation(random(), random(), random(), parent,
				null, JunitTestHelper.getDefaultActor());
		Organisation userOrganisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-60", userOrganisation, JunitTestHelper.PWD);
		organisationService.addMember(parent, creator, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		assertThat(securityManager.getRoles(creator).getOrganisationsWithRoles(OrganisationRoles.projectmanager))
			.extracting(OrganisationRef::getKey).contains(child1.getKey(), child2.getKey());

		Map<String,Object> item = item(invoke(context(creator), Map.of("action", "create", "title", "Parent " + random())));
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getOrganisations(projectService.getProject(() -> (Long)item.get("key")))).containsExactly(userOrganisation);
	}

	/**
	 * V11: without any organisation the edit form cannot be saved, create is refused.
	 */
	@Test
	public void create_noOrganisation_refused() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-41");
		organisationService.removeMember(creator, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(organisationService.getOrganisations(creator, OrganisationRoles.user).isEmpty());

		Set<OrganisationRoles> createRoles = Set.copyOf(projectModule.getCreateRoles());
		// everybody may create projects, so the creator passes the create check without any organisation
		projectModule.setCreateRoles(Set.of());
		try {
			McpToolResult result = invoke(context(creator), Map.of("action", "create", "title", "Nowhere " + random()));
			Assert.assertTrue(result.text(), result.error());
			assertThat(result.text()).startsWith("A project needs an organisation");
			dbInstance.commitAndCloseSession();
			ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
			searchParams.setIdentity(creator);
			Assert.assertTrue(projectService.getProjects(searchParams).isEmpty());
		} finally {
			projectModule.setCreateRoles(createRoles);
		}
	}

	/**
	 * V9: title and external reference have the 100 characters of the edit form.
	 */
	@Test
	public void create_update_lengthsOfTheForm() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-pm-42");
		Assert.assertEquals(100, ProjectModifyToolProvider.TITLE_MAX_LENGTH);
		Assert.assertEquals(100, ProjectModifyToolProvider.EXTERNAL_REF_MAX_LENGTH);

		McpToolResult longTitle = invoke(context(creator), Map.of("action", "create", "title", "t".repeat(101)));
		Assert.assertTrue(longTitle.error());
		assertThat(longTitle.text()).contains("title").contains("100");
		McpToolResult longRef = invoke(context(creator), Map.of("action", "create", "title", "Ref", "externalRef", "r".repeat(101)));
		Assert.assertTrue(longRef.error());
		assertThat(longRef.text()).contains("externalRef").contains("100");
		Assert.assertFalse(VALIDATOR.validate(sut.getDefinition().inputSchema(), Map.of("action", "create", "title", "t".repeat(101))).valid());

		Map<String,Object> item = item(invoke(context(creator), Map.of("action", "create", "title", "t".repeat(100), "externalRef", "r".repeat(100))));
		dbInstance.commitAndCloseSession();
		McpToolResult longUpdate = invoke(context(creator), Map.of("action", "update", "projectKey", item.get("key"), "title", "u".repeat(101)));
		Assert.assertTrue(longUpdate.error());
		Assert.assertEquals("t".repeat(100), projectService.getProject(() -> (Long)item.get("key")).getTitle());
	}

	/**
	 * V19: a state problem is a state error, not a permission error.
	 */
	@Test
	public void stateErrors() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-43");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-44");
		ProjProject deleted = createProject(owner, "Deleted " + random());
		projectService.setStatusDeleted(owner, new ProjectBCFactory(), deleted);
		ProjProject template = createProject(owner, "Template " + random());
		template = projectService.updateProject(owner, new ProjectBCFactory(), template, null, template.getTitle(), null, null, true, false);
		dbInstance.commitAndCloseSession();

		String deletedMessage = "The project " + deleted.getKey() + " is deleted";
		McpToolResult update = invoke(context(owner), Map.of("action", "update", "projectKey", deleted.getKey(), "teaser", "x"));
		Assert.assertTrue(update.error());
		assertThat(update.text()).startsWith(deletedMessage);
		McpToolResult done = invoke(context(owner), Map.of("action", "set_status", "projectKey", deleted.getKey(), "status", "done"));
		Assert.assertTrue(done.error());
		assertThat(done.text()).startsWith(deletedMessage).contains("set_status active");
		McpToolResult addDeleted = invoke(context(owner), Map.of("action", "add_members", "projectKey", deleted.getKey(),
				"users", List.of(username(other)), "roles", List.of("participant"), "sendMail", false));
		Assert.assertTrue(addDeleted.error());
		assertThat(addDeleted.text()).startsWith(deletedMessage);

		McpToolResult addTemplate = invoke(context(owner), Map.of("action", "add_members", "projectKey", template.getKey(),
				"users", List.of(username(other)), "roles", List.of("participant"), "sendMail", false));
		Assert.assertTrue(addTemplate.error());
		Assert.assertEquals("The project " + template.getKey() + " is a template, a template has no members to manage.", addTemplate.text());
		McpToolResult removeTemplate = invoke(context(owner), Map.of("action", "remove_members", "projectKey", template.getKey(),
				"users", List.of(username(owner))));
		Assert.assertTrue(removeTemplate.error());
		assertThat(removeTemplate.text()).contains("is a template");

		// the reopen of the deleted project still works
		Assert.assertEquals("active", item(invoke(context(owner), Map.of("action", "set_status", "projectKey", deleted.getKey(), "status", "active"))).get("status"));
		// the template metadata stay editable by the owner, the template flags stay
		item(invoke(context(owner), Map.of("action", "update", "projectKey", template.getKey(), "teaser", "Template teaser")));
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(projectService.getProject(template).isTemplatePrivate());
	}

	/**
	 * Create from a template as ProjProjectListController.doCreateFromTemplate:
	 * a regular project with the artefacts of the template, the passed
	 * fields replace the values of the template, the organisations follow
	 * the rule of the edit form.
	 */
	@Test
	public void create_fromTemplate() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-45", organisation, JunitTestHelper.PWD);
		organisationService.addMember(organisation, owner, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		ProjProject template = createProject(owner, "Template " + random());
		template = projectService.updateProject(owner, new ProjectBCFactory(), template, "T-1", template.getTitle(), "Template teaser",
				"Template description", true, false);
		ProjNote note = projectService.createNote(owner, template);
		projectService.updateNote(owner, note, null, "Template note", "Text");
		projectService.updateTags(owner, note.getArtefact(), List.of("checklist"));
		dbInstance.commitAndCloseSession();

		String title = "From template " + random();
		McpToolResult result = invoke(context(owner), Map.of("action", "create", "templateKey", template.getKey(),
				"title", title, "teaser", ""));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();
		assertThat(result.text()).startsWith("Created from the template " + template.getKey() + ": " + title);

		ProjProject project = projectService.getProject(() -> (Long)item.get("key"));
		Assert.assertNotEquals(template.getKey(), project.getKey());
		Assert.assertFalse(project.isTemplatePrivate());
		Assert.assertFalse(project.isTemplatePublic());
		Assert.assertEquals(ProjectStatus.active, project.getStatus());
		Assert.assertEquals(title, project.getTitle());
		Assert.assertEquals("T-1", project.getExternalRef());
		Assert.assertNull(project.getTeaser());
		Assert.assertEquals("Template description", project.getDescription());
		assertThat(projectService.getRoles(project, owner)).containsExactly(ProjectRole.owner);
		assertThat(projectService.getOrganisations(project)).containsExactly(organisation);
		// the artefacts of the template are copied
		ProjNoteSearchParams noteParams = new ProjNoteSearchParams();
		noteParams.setProject(project);
		assertThat(projectService.getNotes(noteParams)).extracting(ProjNote::getTitle).containsExactly("Template note");
		assertThat(projectService.getTagInfos(project, null)).extracting(TagInfo::getDisplayName).containsExactly("checklist");
		// the template is unchanged
		ProjProject reloadedTemplate = projectService.getProject(template);
		Assert.assertTrue(reloadedTemplate.isTemplatePrivate());
		Assert.assertEquals("Template teaser", reloadedTemplate.getTeaser());

		// F1: the organisations of the copy equal the rule, so no add and remove pair is written
		List<Action> actions = actions(project, owner);
		assertThat(actions).filteredOn(Action.projectOrganisationAdd::equals).hasSize(1);
		assertThat(actions).doesNotContain(Action.projectOrganisationRemove);

		// without title the title of the template is taken
		Map<String,Object> untitled = item(invoke(context(owner), Map.of("action", "create", "templateKey", template.getKey())));
		Assert.assertEquals(template.getTitle(), untitled.get("title"));
	}

	/**
	 * F1: the copy of a template follows the same rule as a plain create. A
	 * project manager of B without user membership in B gets the project in
	 * the user organisation A, as the copy service puts it; no add and remove
	 * activities.
	 */
	@Test
	public void create_fromTemplate_organisationsOfTheRule() {
		Organisation userOrganisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Organisation managerOrganisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-61", userOrganisation, JunitTestHelper.PWD);
		organisationService.addMember(managerOrganisation, creator, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		ProjProject template = createTemplate(creator, true, null);
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(creator), Map.of("action", "create", "templateKey", template.getKey(), "title", "Copy " + random())));
		dbInstance.commitAndCloseSession();
		ProjProject project = projectService.getProject(() -> (Long)item.get("key"));
		assertThat(projectService.getOrganisations(project)).containsExactly(userOrganisation);
		List<Action> actions = actions(project, creator);
		assertThat(actions).filteredOn(Action.projectOrganisationAdd::equals).hasSize(1);
		assertThat(actions).doesNotContain(Action.projectOrganisationRemove);

		// a second user organisation which is also a manager organisation: the copy is in both, the rule keeps one
		Organisation home = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		organisationService.addMember(home, creator, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		organisationService.addMember(home, creator, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		Map<String,Object> homeItem = item(invoke(context(creator), Map.of("action", "create", "templateKey", template.getKey(), "title", "Home " + random())));
		dbInstance.commitAndCloseSession();
		ProjProject homeProject = projectService.getProject(() -> (Long)homeItem.get("key"));
		assertThat(projectService.getOrganisations(homeProject)).containsExactly(home);
		assertThat(actions(homeProject, creator)).contains(Action.projectOrganisationRemove);
	}

	/**
	 * F7: a template title longer than the 100 characters of the form needs
	 * a title argument, the message names the template title.
	 */
	@Test
	public void create_fromTemplate_longTitle() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-pm-62");
		ProjProject template = createTemplate(creator, true, null);
		template = projectService.updateProject(creator, new ProjectBCFactory(), template, null, "l".repeat(120), null, null, true, false);
		dbInstance.commitAndCloseSession();

		McpToolResult refused = invoke(context(creator), Map.of("action", "create", "templateKey", template.getKey()));
		Assert.assertTrue(refused.error());
		Assert.assertEquals("The title of the template " + template.getKey() + " has 120 characters, a project title has at most 100. "
				+ "Pass the argument title for the new project.", refused.text());
		Map<String,Object> item = item(invoke(context(creator), Map.of("action", "create", "templateKey", template.getKey(), "title", "Short")));
		Assert.assertEquals("Short", item.get("title"));
	}

	/**
	 * N8: a templateKey that is not a number is not echoed in the message.
	 */
	@Test
	public void create_fromTemplate_rawKeyNotEchoed() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-pm-63");
		dbInstance.commitAndCloseSession();
		McpToolResult refused = invoke(context(creator), Map.of("action", "create", "templateKey", "<script>x</script>", "title", "x"));
		Assert.assertTrue(refused.error());
		Assert.assertEquals("The argument templateKey is not a template the account may use. "
				+ "Call project_query list with templates true for the templates.", refused.text());
	}

	/**
	 * Only the templates of the list "Templates" can be used: own templates
	 * and public templates of the organisations of the caller. Every other
	 * key gets the answer of an unknown key.
	 */
	@Test
	public void create_fromTemplate_onlyUsableTemplates() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Organisation otherOrganisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-46", organisation, JunitTestHelper.PWD);
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-47", organisation, JunitTestHelper.PWD);
		organisationService.addMember(organisation, creator, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		ProjProject privateTemplate = createTemplate(author, true, null);
		ProjProject publicTemplate = createTemplate(author, false, organisation);
		ProjProject foreignTemplate = createTemplate(author, false, otherOrganisation);
		ProjProject deletedTemplate = createTemplate(author, false, organisation);
		projectService.setStatusDeleted(author, new ProjectBCFactory(), deletedTemplate);
		ProjProject project = createProject(creator, "Not a template " + random());
		dbInstance.commitAndCloseSession();

		for(Long key:List.of(privateTemplate.getKey(), foreignTemplate.getKey(), deletedTemplate.getKey(), project.getKey(), Long.valueOf(-4))) {
			McpToolResult refused = invoke(context(creator), Map.of("action", "create", "templateKey", key, "title", "x"));
			Assert.assertTrue(refused.error());
			Assert.assertEquals("The templateKey " + key + " is not a template the account may use. "
					+ "Call project_query list with templates true for the templates.", refused.text());
		}
		// the public template of the organisation of the creator
		Map<String,Object> item = item(invoke(context(creator), Map.of("action", "create", "templateKey", publicTemplate.getKey(), "title", "From public")));
		dbInstance.commitAndCloseSession();
		ProjProject created = projectService.getProject(() -> (Long)item.get("key"));
		assertThat(projectService.getRoles(created, creator)).containsExactly(ProjectRole.owner);
		assertThat(projectService.getRoles(created, author)).isEmpty();
		// a plain user cannot create projects at all
		Identity plain = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-48", organisation, JunitTestHelper.PWD);
		Assert.assertThrows(McpPermissionException.class, () -> invoke(context(plain), Map.of("action", "create", "templateKey", publicTemplate.getKey())));
	}

	/**
	 * A project created by MCP belongs to the organisation of the creator, so
	 * the project managers of this organisation see and manage it.
	 */
	@Test
	public void create_organisationOfCreator() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-2o", organisation, JunitTestHelper.PWD);
		organisationService.addMember(organisation, creator, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-2m", organisation, JunitTestHelper.PWD);
		organisationService.addMember(organisation, manager, OrganisationRoles.projectmanager, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(creator), Map.of("action", "create", "title", "Org " + random())));
		dbInstance.commitAndCloseSession();

		ProjProject project = projectService.getProject(() -> (Long)item.get("key"));
		assertThat(projectService.getOrganisations(project)).containsExactly(organisation);
		// the project manager of the organisation, not a member, edits the project
		item(invoke(context(manager), Map.of("action", "update", "projectKey", project.getKey(), "teaser", "by the manager")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("by the manager", projectService.getProject(project).getTeaser());
	}

	/**
	 * The live path: the identity comes from the validation of the token in
	 * an earlier request and is detached when the tool runs. Before the fix
	 * it was an initialized Hibernate proxy (the lazy identity of the
	 * authentication) and the reload of the new project in updateProject
	 * failed with a NullPointerException in Hibernate (entityHolder is null).
	 */
	@Test
	public void create_tokenIdentity() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-pm-2d");
		McpTokenCreation creation = tokenService.generateToken(admin, "Create");
		dbInstance.commitAndCloseSession();
		Identity detached = tokenService.validateToken(creation.plaintext()).token().identity();
		McpToolContext context = context(detached);
		dbInstance.commitAndCloseSession();

		String title = "Detached " + random();
		McpToolResult result = invoke(context, Map.of("action", "create", "title", title));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjProject project = projectService.getProject(() -> (Long)item.get("key"));
		Assert.assertEquals(title, project.getTitle());
		assertThat(projectService.getRoles(project, admin)).containsExactly(ProjectRole.owner);
		Assert.assertFalse(detached instanceof HibernateProxy);
	}

	/**
	 * A failure after the commit of createProject must not leave an active
	 * project without title. The failure is injected with a detached
	 * Hibernate proxy as identity: Hibernate 7.4 fails in the reload of the
	 * new project inside updateProject (the bug of the token identity before
	 * its fix). If a later Hibernate loads it, the create succeeds and the
	 * test checks the normal result.
	 */
	@Test
	public void create_failureAfterCreate_trashesProject() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-pm-2f");
		dbInstance.commitAndCloseSession();
		Identity proxy = dbInstance.getCurrentEntityManager().getReference(IdentityImpl.class, admin.getKey());
		Assert.assertNotNull(proxy.getStatus());
		McpToolContext context = context(proxy);
		dbInstance.commitAndCloseSession();

		String title = "Failure " + random();
		RuntimeException failure = null;
		try {
			invoke(context, Map.of("action", "create", "title", title));
		} catch (RuntimeException e) {
			failure = e;
		}
		dbInstance.commitAndCloseSession();

		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setIdentity(admin);
		List<ProjProject> projects = projectService.getProjects(searchParams);
		Assert.assertEquals(1, projects.size());
		ProjProject project = projects.get(0);
		if(failure != null) {
			Assert.assertEquals(ProjectStatus.deleted, project.getStatus());
			Assert.assertNull(project.getTitle());
		} else {
			Assert.assertEquals(ProjectStatus.active, project.getStatus());
			Assert.assertEquals(title, project.getTitle());
		}
	}

	@Test
	public void update_onlySuppliedFields() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-3");
		Identity leader = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-5");
		ProjProject project = createProject(owner, "Beta " + random());
		projectService.updateProject(owner, new ProjectBCFactory(), project, "B-1", project.getTitle(), "Teaser", "Description", false, false);
		projectService.updateMember(owner, new ProjectBCFactory(), project, leader, Set.of(ProjectRole.leader), null);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		dbInstance.commitAndCloseSession();

		// a leader may edit the metadata
		Map<String,Object> item = item(invoke(context(leader), Map.of("action", "update", "projectKey", project.getKey(), "teaser", "", "description", "Description v2")));
		dbInstance.commitAndCloseSession();
		ProjProject reloaded = projectService.getProject(project);
		Assert.assertEquals(project.getTitle(), reloaded.getTitle());
		Assert.assertEquals("B-1", reloaded.getExternalRef());
		Assert.assertNull(reloaded.getTeaser());
		Assert.assertEquals("Description v2", reloaded.getDescription());
		Assert.assertEquals(project.getTitle(), item.get("title"));

		// a participant may not
		try {
			invoke(context(participant), Map.of("action", "update", "projectKey", project.getKey(), "title", "x"));
			Assert.fail("A participant cannot edit the project");
		} catch (McpPermissionException e) {
			Assert.assertEquals("edit the project", e.getPermission());
		}
		McpToolResult emptyTitle = invoke(context(owner), Map.of("action", "update", "projectKey", project.getKey(), "title", ""));
		Assert.assertTrue(emptyTitle.error());
		assertThat(emptyTitle.text()).contains("title");
	}

	@Test
	public void setStatus_doneActiveDeleted() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-6");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-7");
		ProjProject project = createProject(owner, "Gamma " + random());
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(participant), Map.of("action", "set_status", "projectKey", project.getKey(), "status", "done"));
			Assert.fail("A participant cannot close the project");
		} catch (McpPermissionException e) {
			Assert.assertEquals("change the status of the project", e.getPermission());
		}
		Assert.assertEquals("done", item(invoke(context(owner), Map.of("action", "set_status", "projectKey", project.getKey(), "status", "done"))).get("status"));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(ProjectStatus.done, projectService.getProject(project).getStatus());

		McpToolResult same = invoke(context(owner), Map.of("action", "set_status", "projectKey", project.getKey(), "status", "done"));
		assertThat(same.text()).startsWith("Unchanged");

		Assert.assertEquals("active", item(invoke(context(owner), Map.of("action", "set_status", "projectKey", project.getKey(), "status", "active"))).get("status"));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(ProjectStatus.active, projectService.getProject(project).getStatus());

		Assert.assertEquals("deleted", item(invoke(context(owner), Map.of("action", "set_status", "projectKey", project.getKey(), "status", "deleted"))).get("status"));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(ProjectStatus.deleted, projectService.getProject(project).getStatus());
		assertThat(actions(project, owner)).contains(Action.projectStatusDone, Action.projectStatusActive, Action.projectStatusDelete);

		McpToolResult badStatus = invoke(context(owner), Map.of("action", "set_status", "projectKey", project.getKey(), "status", "archived"));
		Assert.assertTrue(badStatus.error());
	}

	@Test
	public void members_addAndRemove() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-8");
		Identity newMember = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-9");
		Identity byEmail = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-10");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-11");
		ProjProject project = createProject(owner, "Delta " + random());
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		dbInstance.commitAndCloseSession();

		// a participant may not manage members
		try {
			invoke(context(participant), Map.of("action", "add_members", "projectKey", project.getKey(),
					"users", List.of(username(newMember)), "roles", List.of("participant")));
			Assert.fail("A participant cannot add members");
		} catch (McpPermissionException e) {
			Assert.assertEquals("manage the members of the project", e.getPermission());
		}

		McpToolResult added = invoke(context(owner), Map.of("action", "add_members", "projectKey", project.getKey(),
				"users", List.of(username(newMember), byEmail.getUser().getEmail()), "roles", List.of("participant", "supplier"), "sendMail", false));
		item(added);
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getRoles(project, newMember)).containsExactlyInAnyOrder(ProjectRole.participant, ProjectRole.supplier);
		assertThat(projectService.getRoles(project, byEmail)).containsExactlyInAnyOrder(ProjectRole.participant, ProjectRole.supplier);
		assertThat(added.text()).contains(", 4 members) ");
		assertThat(actions(project, owner)).contains(Action.projectMemberAdd);

		// roles replace: the participant becomes a client
		invoke(context(owner), Map.of("action", "add_members", "projectKey", project.getKey(),
				"users", List.of(username(participant)), "roles", List.of("client"), "sendMail", false));
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getRoles(project, participant)).containsExactly(ProjectRole.client);

		// unknown user, uniform message, nothing written
		McpToolResult unknown = invoke(context(owner), Map.of("action", "add_members", "projectKey", project.getKey(),
				"users", List.of("nobody-" + random()), "roles", List.of("participant")));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).endsWith("cannot be added to this project.");
		McpToolResult noRoles = invoke(context(owner), Map.of("action", "add_members", "projectKey", project.getKey(), "users", List.of(username(newMember))));
		Assert.assertTrue(noRoles.error());
		assertThat(noRoles.text()).contains("roles");

		McpToolResult removed = invoke(context(owner), Map.of("action", "remove_members", "projectKey", project.getKey(),
				"users", List.of(username(newMember), username(byEmail))));
		item(removed);
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getRoles(project, newMember)).isEmpty();
		assertThat(projectService.getRoles(project, byEmail)).isEmpty();
		assertThat(removed.text()).contains(", 2 members) ");
		assertThat(actions(project, owner)).contains(Action.projectMemberRemove);

		McpToolResult notMember = invoke(context(owner), Map.of("action", "remove_members", "projectKey", project.getKey(), "users", List.of(username(newMember))));
		Assert.assertTrue(notMember.error());
		Assert.assertEquals("Invalid argument: " + username(newMember) + " is not a valid member for this project.", notMember.text());
	}

	/**
	 * add_members finds users only in the organisations of the caller, as the
	 * name import of the member wizard. A user of another organisation gets
	 * the message of an unknown user.
	 */
	@Test
	public void members_add_otherOrganisation() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-15");
		Organisation otherOrganisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity foreigner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-16", otherOrganisation, JunitTestHelper.PWD);
		ProjProject project = createProject(owner, "Zeta " + random());
		dbInstance.commitAndCloseSession();

		McpToolResult byName = invoke(context(owner), Map.of("action", "add_members", "projectKey", project.getKey(),
				"users", List.of(username(foreigner)), "roles", List.of("participant"), "sendMail", false));
		Assert.assertTrue(byName.error());
		Assert.assertEquals("Invalid argument: " + username(foreigner) + " cannot be added to this project.", byName.text());
		McpToolResult byEmail = invoke(context(owner), Map.of("action", "add_members", "projectKey", project.getKey(),
				"users", List.of(foreigner.getUser().getEmail()), "roles", List.of("participant"), "sendMail", false));
		Assert.assertTrue(byEmail.error());
		Assert.assertEquals("Invalid argument: " + foreigner.getUser().getEmail() + " cannot be added to this project.", byEmail.text());
		// the same message as for an unknown user
		String nobody = "nobody-" + random();
		McpToolResult unknown = invoke(context(owner), Map.of("action", "add_members", "projectKey", project.getKey(),
				"users", List.of(nobody), "roles", List.of("participant"), "sendMail", false));
		Assert.assertEquals("Invalid argument: " + nobody + " cannot be added to this project.", unknown.text());
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getRoles(project, foreigner)).isEmpty();
	}

	/**
	 * A project keeps at least one owner: a leader cannot remove or demote
	 * the only owner.
	 */
	@Test
	public void members_lastOwner_refused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-17");
		Identity leader = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-18");
		ProjProject project = createProject(owner, "Eta " + random());
		projectService.updateMember(owner, new ProjectBCFactory(), project, leader, Set.of(ProjectRole.leader), null);
		dbInstance.commitAndCloseSession();

		McpToolResult removed = invoke(context(leader), Map.of("action", "remove_members", "projectKey", project.getKey(),
				"users", List.of(username(owner))));
		Assert.assertTrue(removed.error());
		assertThat(removed.text()).contains("at least one owner");
		McpToolResult demoted = invoke(context(leader), Map.of("action", "add_members", "projectKey", project.getKey(),
				"users", List.of(username(owner)), "roles", List.of("participant"), "sendMail", false));
		Assert.assertTrue(demoted.error());
		assertThat(demoted.text()).contains("at least one owner");
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getRoles(project, owner)).containsExactly(ProjectRole.owner);

		// with a second owner the first one can go
		item(invoke(context(leader), Map.of("action", "add_members", "projectKey", project.getKey(),
				"users", List.of(username(leader)), "roles", List.of("owner", "leader"), "sendMail", false)));
		dbInstance.commitAndCloseSession();
		item(invoke(context(leader), Map.of("action", "remove_members", "projectKey", project.getKey(),
				"users", List.of(username(owner)))));
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getRoles(project, owner)).isEmpty();
		// removing both owners at once is refused too
		McpToolResult all = invoke(context(leader), Map.of("action", "remove_members", "projectKey", project.getKey(),
				"users", List.of(username(leader))));
		Assert.assertTrue(all.error());
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getRoles(project, leader)).containsExactlyInAnyOrder(ProjectRole.owner, ProjectRole.leader);
	}

	@Test
	public void notMember_denied_managerAllowed() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-12");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-pm-13");
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-pm-14");
		ProjProject project = createProject(owner, "Epsilon " + random());
		projectService.updateProjectOrganisations(owner, project, List.of(organisationService.getDefaultOrganisation()));
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "update", "projectKey", project.getKey(), "title", "x"));
			Assert.fail("A stranger cannot touch the project");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the project", e.getPermission());
		}
		// the administrator of the organisation edits the metadata as the project list allows
		Map<String,Object> item = item(invoke(context(admin), Map.of("action", "update", "projectKey", project.getKey(), "teaser", "by the admin")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(project.getKey(), item.get("key"));
		Assert.assertEquals("by the admin", projectService.getProject(project).getTeaser());
		assertThat(projectService.getRoles(project, admin)).isEmpty();
	}

	private ProjProject createTemplate(Identity owner, boolean privateTemplate, Organisation organisation) {
		ProjProject template = createProject(owner, "Template " + random());
		template = projectService.updateProject(owner, new ProjectBCFactory(), template, null, template.getTitle(), null, null,
				privateTemplate, !privateTemplate);
		if(organisation != null) {
			projectService.updateTemplateOrganisations(owner, template, List.of(organisation));
		}
		return template;
	}

	private ProjProject createProject(Identity owner, String title) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, title, null, null, false, false);
	}

	private List<Action> actions(ProjProject project, Identity doer) {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setDoer(doer);
		return projectService.getActivities(searchParams, 0, 100).stream().map(ProjActivity::getAction).toList();
	}

	private McpToolContext context(Identity identity) {
		return new McpToolContext(identity, securityManager.getRoles(identity), GRANTS, null, Locale.ENGLISH, "127.0.0.1");
	}

	private static String username(Identity identity) {
		return identity.getUser().getNickName();
	}

	@SuppressWarnings("unchecked")
	private Map<String,Object> item(McpToolResult result) {
		Assert.assertFalse(result.text(), result.error());
		JsonSchemaValidator.ValidationResponse response = VALIDATOR.validate(sut.getDefinition().outputSchema(), result.structuredContent());
		Assert.assertTrue(response.errorMessage(), response.valid());
		Map<String,Object> item = (Map<String,Object>)((Map<String,Object>)result.structuredContent()).get("item");
		Assert.assertNotNull(item);
		// the minimal item, project_query get returns the full project
		assertThat(item.keySet()).containsExactlyElementsOf(ProjectModifyToolProvider.RESULT_FIELDS);
		return item;
	}
	/**
	 * Calls the tool and maps an invalid argument as {@link McpToolRegistry#call} does.
	 */
	private McpToolResult invoke(McpToolContext toolContext, Map<String,Object> arguments) {
		try {
			return sut.execute(toolContext, arguments);
		} catch (McpInvalidArgumentException e) {
			return McpToolResult.error(McpToolRegistry.invalidArgumentText(e));
		}
	}
}
