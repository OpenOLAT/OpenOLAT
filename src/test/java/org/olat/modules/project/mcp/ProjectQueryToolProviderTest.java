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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.mcp.McpGrants;
import org.olat.mcp.manager.McpJsonSchemaValidator;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool project_query.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class ProjectQueryToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(ProjectQueryToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private SiteDefinitions siteDefinitions;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ProjectQueryToolProvider sut;

	@Test
	public void definition() {
		Assert.assertEquals("project_query", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.READ, sut.getScope());
		assertThat(sut.getDefinition().description()).contains("todo_query").contains("originKey").contains("templates true");
		Assert.assertFalse(sut.getDefinition().destructive());
		Assert.assertTrue(sut.getDefinition().idempotent());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "get", "projectKey", 12)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("status", "all", "limit", 200)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("status", "archived")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("limit", 201)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("query", "x")).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "list", "templates", true)).valid());
	}

	@Test
	public void isAvailable_moduleGate() {
		boolean enabled = projectModule.isEnabled();
		try {
			projectModule.setEnabled(true);
			Assert.assertTrue(sut.isAvailable());
			projectModule.setEnabled(false);
			Assert.assertFalse(sut.isAvailable());
		} finally {
			projectModule.setEnabled(enabled);
		}
	}

	@Test
	public void isAvailable_siteGate() {
		siteDefinitions.getSiteDefList(); // initializes the configurations of the sites
		// the in-memory configuration of the site "Projects", the switch of the site administration
		SiteConfiguration config = siteDefinitions.getConfigurationSite("olatsites_projects");
		boolean enabled = config.isEnabled();
		try {
			config.setEnabled(true);
			Assert.assertTrue(sut.isAvailable());
			config.setEnabled(false);
			Assert.assertFalse(sut.isAvailable());
		} finally {
			config.setEnabled(enabled);
		}
	}

	@Test
	public void list_ownProjectsOnly() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-2");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-3");
		ProjProject project = createProject(owner, "Alpha " + random());
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		dbInstance.commitAndCloseSession();

		Map<String,Object> ownerResult = structured(invoke(context(owner), Map.of()));
		List<Map<String,Object>> items = items(ownerResult);
		assertThat(items).hasSize(1);
		Map<String,Object> item = items.get(0);
		Assert.assertEquals(project.getKey(), item.get("key"));
		Assert.assertEquals(project.getTitle(), item.get("title"));
		Assert.assertEquals("active", item.get("status"));
		Assert.assertEquals(List.of("owner"), item.get("myRoles"));
		assertThat((String)item.get("url")).contains("/auth/Projects/0/My/0/Project/" + project.getKey());
		Assert.assertEquals(Integer.valueOf(1), ownerResult.get("total"));
		Assert.assertEquals(Boolean.FALSE, ownerResult.get("truncated"));

		List<Map<String,Object>> participantItems = items(structured(invoke(context(participant), Map.of("action", "list"))));
		assertThat(participantItems).hasSize(1);
		Assert.assertEquals(List.of("participant"), participantItems.get(0).get("myRoles"));

		McpToolResult strangerResult = invoke(context(stranger), Map.of());
		assertThat(items(structured(strangerResult))).isEmpty();
		assertThat(strangerResult.text()).startsWith("0 projects");
	}

	@Test
	public void list_statusFilter() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-4");
		ProjProject active = createProject(owner, "Active " + random());
		ProjProject done = createProject(owner, "Done " + random());
		projectService.setStatusDone(owner, done);
		ProjProject template = createProject(owner, "Template " + random());
		projectService.updateProject(owner, new ProjectBCFactory(), template, null, template.getTitle(), null, null, true, false);
		dbInstance.commitAndCloseSession();

		assertThat(keys(items(structured(invoke(context(owner), Map.of())))))
			.containsExactly(active.getKey());
		assertThat(keys(items(structured(invoke(context(owner), Map.of("status", "done"))))))
			.containsExactly(done.getKey());
		assertThat(keys(items(structured(invoke(context(owner), Map.of("status", "all"))))))
			.containsExactlyInAnyOrder(active.getKey(), done.getKey());
		assertThat(keys(items(structured(invoke(context(owner), Map.of("status", "deleted"))))))
			.isEmpty();
	}

	/**
	 * A project without title, as left by a failed create before the fix,
	 * must not break the output validation of list and get.
	 */
	@Test
	public void listAndGet_projectWithoutTitle() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-nt");
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		dbInstance.commitAndCloseSession();
		Assert.assertNull(project.getTitle());

		List<Map<String,Object>> items = items(structured(invoke(context(owner), Map.of())));
		assertThat(items).hasSize(1);
		Assert.assertEquals("", items.get(0).get("title"));

		Map<String,Object> item = item(structured(invoke(context(owner), Map.of("action", "get", "projectKey", project.getKey()))));
		Assert.assertEquals(project.getKey(), item.get("key"));
		Assert.assertEquals("", item.get("title"));
	}

	@Test
	public void list_invalidStatus() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-5");
		McpToolResult result = invoke(context(owner), Map.of("status", "archived"));
		Assert.assertTrue(result.error());
		assertThat(result.text()).contains("archived").contains("active");
	}

	@Test
	public void list_limitAndTruncated() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-6");
		createProject(owner, "A " + random());
		createProject(owner, "B " + random());
		createProject(owner, "C " + random());
		dbInstance.commitAndCloseSession();

		Map<String,Object> result = structured(invoke(context(owner), Map.of("limit", 2)));
		assertThat(items(result)).hasSize(2);
		Assert.assertEquals(Integer.valueOf(3), result.get("total"));
		Assert.assertEquals(Boolean.TRUE, result.get("truncated"));
		// ordered by title
		assertThat((String)items(result).get(0).get("title")).startsWith("A ");
	}

	@Test
	public void get_member() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-7");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-8");
		ProjProject project = createProject(owner, "Beta " + random());
		projectService.updateProject(owner, new ProjectBCFactory(), project, "EXT-1", project.getTitle(), "Teaser", "The description", false, false);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant, ProjectRole.supplier), null);
		createToDo(owner, project, ToDoStatus.open);
		createToDo(owner, project, ToDoStatus.inProgress);
		createToDo(owner, project, ToDoStatus.done);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("action", "get", "projectKey", project.getKey()));
		Assert.assertFalse(result.text(), result.error());
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals(project.getKey(), item.get("key"));
		Assert.assertEquals("EXT-1", item.get("externalRef"));
		Assert.assertEquals("Teaser", item.get("teaser"));
		Assert.assertEquals("The description", item.get("description"));
		Assert.assertEquals(List.of("participant", "supplier"), item.get("myRoles"));
		Assert.assertEquals(Long.valueOf(2), item.get("openToDos"));
		assertThat(result.text()).contains("2 open ToDos");
		Assert.assertEquals(Integer.valueOf(2), item.get("membersTotal"));
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> members = (List<Map<String,Object>>)item.get("members");
		assertThat(members).hasSize(2);
		assertThat(members).extracting(member -> member.get("roles"))
			.containsExactlyInAnyOrder(List.of("owner"), List.of("participant", "supplier"));
		assertThat(result.text()).contains("todo_query").contains(project.getKey().toString());
	}

	@Test
	public void get_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-9");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-10");
		ProjProject project = createProject(owner, "Gamma " + random());
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "get", "projectKey", project.getKey()));
			Assert.fail("A stranger must not see the project");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the project", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_organisationManager_allowed() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-11");
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-proj-12");
		ProjProject project = createProject(owner, "Delta " + random());
		projectService.updateProjectOrganisations(owner, project, List.of(organisationService.getDefaultOrganisation()));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(admin), Map.of("action", "get", "projectKey", project.getKey()));
		Assert.assertFalse(result.text(), result.error());
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals(List.of(), item.get("myRoles"));
		// a manager without project role does not see the ToDos, as on the dashboard
		Assert.assertFalse(item.containsKey("openToDos"));
		assertThat(result.text()).doesNotContain("open ToDos");
	}

	/**
	 * list with templates true: the rule of the list "Templates", own
	 * templates and public templates of the organisations of the caller,
	 * not deleted. The items carry template true; the list of projects
	 * stays without templates.
	 */
	@Test
	public void list_templates() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Organisation otherOrganisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-30", organisation, JunitTestHelper.PWD);
		Identity viewer = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-31", organisation, JunitTestHelper.PWD);
		// the create right of the test profile (project.create.roles=projectmanager,author), the condition of the list "Templates"
		organisationService.addMember(organisation, viewer, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
		ProjProject ownTemplate = createTemplate(viewer, true, null);
		ProjProject publicTemplate = createTemplate(author, false, organisation);
		ProjProject foreignPrivate = createTemplate(author, true, null);
		ProjProject foreignPublic = createTemplate(author, false, otherOrganisation);
		ProjProject deletedTemplate = createTemplate(author, false, organisation);
		projectService.setStatusDeleted(author, new ProjectBCFactory(), deletedTemplate);
		ProjProject project = createProject(viewer, "Project " + random());
		dbInstance.commitAndCloseSession();

		Map<String,Object> structured = structured(invoke(context(viewer), Map.of("action", "list", "templates", true, "status", "deleted")));
		List<Map<String,Object>> items = items(structured);
		assertThat(keys(items)).containsExactlyInAnyOrder(ownTemplate.getKey(), publicTemplate.getKey())
			.doesNotContain(foreignPrivate.getKey(), foreignPublic.getKey(), deletedTemplate.getKey(), project.getKey());
		assertThat(items).allSatisfy(item -> Assert.assertEquals(Boolean.TRUE, item.get("template")));
		Map<String,Object> publicItem = items.stream().filter(item -> publicTemplate.getKey().equals(item.get("key"))).findFirst().orElseThrow();
		Assert.assertEquals(List.of(), publicItem.get("myRoles"));

		// the projects list has no templates and no template flag
		List<Map<String,Object>> projects = items(structured(invoke(context(viewer), Map.of("action", "list"))));
		assertThat(keys(projects)).containsExactly(project.getKey());
		Assert.assertFalse(projects.get(0).containsKey("template"));
	}

	/**
	 * get works on a template the caller may use, not on a foreign private
	 * template. F5: a user of the template without project role and without
	 * the manager flag gets the number of members, not the member list.
	 */
	@Test
	public void get_template() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-32", organisation, JunitTestHelper.PWD);
		Identity viewer = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-33", organisation, JunitTestHelper.PWD);
		organisationService.addMember(organisation, viewer, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
		ProjProject publicTemplate = createTemplate(author, false, organisation);
		ProjProject privateTemplate = createTemplate(author, true, null);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(viewer), Map.of("action", "get", "projectKey", publicTemplate.getKey()));
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals(publicTemplate.getKey(), item.get("key"));
		Assert.assertEquals(Boolean.TRUE, item.get("template"));
		Assert.assertEquals(List.of(), item.get("myRoles"));
		assertThat(result.text()).contains(", template");
		Assert.assertFalse(item.containsKey("members"));
		Assert.assertEquals(Integer.valueOf(1), item.get("membersTotal"));
		// the owner of the template gets the member list
		Map<String,Object> ownerItem = item(structured(invoke(context(author), Map.of("action", "get", "projectKey", publicTemplate.getKey()))));
		assertThat((List<?>)ownerItem.get("members")).hasSize(1);

		McpPermissionException denied = Assert.assertThrows(McpPermissionException.class,
				() -> invoke(context(viewer), Map.of("action", "get", "projectKey", privateTemplate.getKey())));
		Assert.assertEquals("view the project", denied.getPermission());
	}

	/**
	 * F2: the UI shows the templates only with the create right
	 * (ProjectsSiteController); without it the list is empty and get on a
	 * public template of the organisation is refused like an unknown key.
	 */
	@Test
	public void templates_withoutCreateRight() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), null,
				null, JunitTestHelper.getDefaultActor());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-34", organisation, JunitTestHelper.PWD);
		Identity viewer = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-35", organisation, JunitTestHelper.PWD);
		ProjProject publicTemplate = createTemplate(author, false, organisation);
		dbInstance.commitAndCloseSession();

		Set<OrganisationRoles> createRoles = Set.copyOf(projectModule.getCreateRoles());
		projectModule.setCreateRoles(Set.of(OrganisationRoles.author));
		try {
			Assert.assertFalse(projectModule.canCreateProject(securityManager.getRoles(viewer)));
			List<Map<String,Object>> items = items(structured(invoke(context(viewer), Map.of("action", "list", "templates", true))));
			Assert.assertTrue(items.isEmpty());
			McpPermissionException denied = Assert.assertThrows(McpPermissionException.class,
					() -> invoke(context(viewer), Map.of("action", "get", "projectKey", publicTemplate.getKey())));
			Assert.assertEquals("view the project", denied.getPermission());

			// with the create right the same template is listed
			organisationService.addMember(organisation, viewer, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
			dbInstance.commitAndCloseSession();
			List<Map<String,Object>> withRight = items(structured(invoke(context(viewer), Map.of("action", "list", "templates", true))));
			assertThat(keys(withRight)).containsExactly(publicTemplate.getKey());
		} finally {
			projectModule.setCreateRoles(createRoles);
		}
	}

	@Test
	public void get_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-proj-13");

		McpToolResult missingKey = invoke(context(owner), Map.of("action", "get"));
		Assert.assertTrue(missingKey.error());
		assertThat(missingKey.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownKey = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "get", "projectKey", Long.valueOf(-17))));
		Assert.assertEquals("project with key -17", unknownKey.getObjectDescription());
		Assert.assertEquals("view the project", unknownKey.getPermission());

		McpToolResult unknownAction = invoke(context(owner), Map.of("action", "delete"));
		Assert.assertTrue(unknownAction.error());
		assertThat(unknownAction.text()).contains("Unknown action");
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

	private void createToDo(Identity doer, ProjProject project, ToDoStatus status) {
		ProjToDo toDo = projectService.createToDo(doer, project);
		projectService.updateToDo(doer, toDo, random(), status, null, null, null, null, null);
	}

	private McpToolContext context(Identity identity) {
		return new McpToolContext(identity, securityManager.getRoles(identity), GRANTS, null, Locale.ENGLISH, "127.0.0.1");
	}

	/**
	 * @return The structured content, validated against the output schema of the tool
	 */
	@SuppressWarnings("unchecked")
	private Map<String,Object> structured(McpToolResult result) {
		Assert.assertFalse(result.text(), result.error());
		JsonSchemaValidator.ValidationResponse response = VALIDATOR.validate(sut.getDefinition().outputSchema(), result.structuredContent());
		Assert.assertTrue(response.errorMessage(), response.valid());
		return (Map<String,Object>)result.structuredContent();
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> items(Map<String,Object> structured) {
		return (List<Map<String,Object>>)structured.get("items");
	}

	@SuppressWarnings("unchecked")
	private static Map<String,Object> item(Map<String,Object> structured) {
		return (Map<String,Object>)structured.get("item");
	}

	private static List<Object> keys(List<Map<String,Object>> items) {
		return items.stream().map(item -> item.get("key")).toList();
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
