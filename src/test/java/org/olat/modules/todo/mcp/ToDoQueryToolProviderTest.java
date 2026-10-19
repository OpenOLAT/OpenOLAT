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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.DateUtils;
import org.olat.course.todo.manager.CourseIndividualToDoTaskProvider;
import org.olat.mcp.McpGrants;
import org.olat.mcp.manager.McpJsonSchemaValidator;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoAssignedMailSender;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.manager.PersonalToDoProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool todo_query.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class ToDoQueryToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(ToDoQueryToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ToDoToolSupport support;
	@Autowired
	private ToDoQueryToolProvider sut;

	@Test
	public void definition() {
		Assert.assertEquals("todo_query", sut.getToolName());
		Assert.assertEquals(ToDoMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.READ, sut.getScope());
		assertThat(sut.getDefinition().description()).contains("todo_modify").contains("assignedToMe").contains("different, internal key");
		// The personal provider is always enabled
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("context", "project", "originKey", 1, "status", List.of("open", "done"),
				"dueBefore", "2026-10-10", "overdue", Boolean.TRUE, "assignedToMe", Boolean.FALSE, "limit", 10)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "get", "toDoKey", 5)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("status", List.of("deleted"))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("context", "calendar")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("limit", 0)).valid());
	}

	@Test
	public void contextMapping() {
		Assert.assertEquals("personal", support.contextOf(PersonalToDoProvider.TYPE));
		Assert.assertEquals("project", support.contextOf("project"));
		Assert.assertEquals("course", support.contextOf(CourseIndividualToDoTaskProvider.TYPE));
		Assert.assertEquals("course", support.contextOf("course.todo.collection"));
		Assert.assertEquals("course", support.contextOf("course.todo.collection.element"));
		Assert.assertEquals("course", support.contextOf("gta.assignment"));
		Assert.assertEquals("curriculum", support.contextOf("curriculum.element"));
		Assert.assertEquals("quality", support.contextOf("quality.general"));
		Assert.assertEquals("other", support.contextOf("unknown.type"));
		assertThat(support.enabledTypes("personal")).containsExactly(PersonalToDoProvider.TYPE);
		assertThat(support.enabledTypes("course")).contains(CourseIndividualToDoTaskProvider.TYPE, "gta.submit")
			.doesNotContain(PersonalToDoProvider.TYPE);
		assertThat(support.enabledTypes("all")).contains(PersonalToDoProvider.TYPE, CourseIndividualToDoTaskProvider.TYPE);
	}

	@Test
	public void list_defaultOpenAndAssigned() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-1");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-2");
		ToDoTask open = createPersonalToDo(id, "Open " + random(), ToDoStatus.open, null);
		ToDoTask inProgress = createPersonalToDo(id, "Progress " + random(), ToDoStatus.inProgress, null);
		ToDoTask done = createPersonalToDo(id, "Done " + random(), ToDoStatus.done, null);
		createPersonalToDo(other, "Other " + random(), ToDoStatus.open, null);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(id), Map.of());
		assertThat(keys(items(structured(result)))).containsExactlyInAnyOrder(open.getKey(), inProgress.getKey());
		assertThat(result.text()).startsWith("2 ToDos:");

		assertThat(keys(items(structured(invoke(context(id), Map.of("status", List.of("done")))))))
			.containsExactly(done.getKey());
		assertThat(keys(items(structured(invoke(context(id), Map.of("status", List.of("open", "inProgress", "done")))))))
			.containsExactlyInAnyOrder(open.getKey(), inProgress.getKey(), done.getKey());
	}

	@Test
	public void list_itemFields() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-3");
		ToDoTask toDo = createPersonalToDo(id, "Fields " + random(), ToDoStatus.open, DateUtils.addDays(new Date(), 3));
		toDo.setPriority(ToDoPriority.high);
		toDo.setDescription("Some description");
		toDo.setExpenditureOfWork(Long.valueOf(3));
		toDoService.update(id, toDo, ToDoStatus.open);
		toDoService.updateTags(toDo, List.of("alpha", "beta"));
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = items(structured(invoke(context(id), Map.of("context", "personal")))).get(0);
		Assert.assertEquals(toDo.getKey(), item.get("key"));
		Assert.assertEquals(toDo.getTitle(), item.get("title"));
		Assert.assertEquals("open", item.get("status"));
		Assert.assertEquals("high", item.get("priority"));
		Assert.assertEquals(McpToolSupport.isoDate(toDo.getDueDate()), item.get("dueDate"));
		Assert.assertEquals("3h", item.get("expenditureOfWork"));
		Assert.assertEquals("Some description", item.get("description"));
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>)item.get("tags");
		assertThat(tags).containsExactlyInAnyOrder("alpha", "beta");
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> assignees = (List<Map<String,Object>>)item.get("assignees");
		assertThat(assignees).hasSize(1);
		Assert.assertEquals(McpToolSupport.username(id), assignees.get(0).get("username"));
		@SuppressWarnings("unchecked")
		Map<String,Object> context = (Map<String,Object>)item.get("context");
		Assert.assertEquals("personal", context.get("type"));
		Assert.assertEquals("personal", context.get("context"));
		assertThat((String)item.get("url")).contains("/auth/HomeSite/0/ToDos/0/");
	}

	@Test
	public void list_contextFilter() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-4");
		ToDoTask personal = createPersonalToDo(owner, "Personal " + random(), ToDoStatus.open, null);
		ProjProject project = createProject(owner);
		ProjToDo projectToDo = createProjectToDo(owner, project, List.of(owner));
		ProjProject otherProject = createProject(owner);
		ProjToDo otherProjectToDo = createProjectToDo(owner, otherProject, List.of(owner));
		dbInstance.commitAndCloseSession();

		assertThat(keys(items(structured(invoke(context(owner), Map.of())))))
			.containsExactlyInAnyOrder(personal.getKey(), projectToDo.getToDoTask().getKey(), otherProjectToDo.getToDoTask().getKey());
		assertThat(keys(items(structured(invoke(context(owner), Map.of("context", "personal"))))))
			.containsExactly(personal.getKey());
		assertThat(keys(items(structured(invoke(context(owner), Map.of("context", "project"))))))
			.containsExactlyInAnyOrder(projectToDo.getToDoTask().getKey(), otherProjectToDo.getToDoTask().getKey());
		assertThat(keys(items(structured(invoke(context(owner), Map.of("context", "project", "originKey", project.getKey()))))))
			.containsExactly(projectToDo.getToDoTask().getKey());
		assertThat(keys(items(structured(invoke(context(owner), Map.of("context", "course"))))))
			.isEmpty();

		Map<String,Object> projectItem = items(structured(invoke(context(owner), Map.of("context", "project", "originKey", project.getKey())))).get(0);
		assertThat((String)projectItem.get("url")).contains("/Project/" + project.getKey() + "/ToDos/0/ToDo/" + projectToDo.getKey());

		// the URLs of the bulk loaded project ToDos are the URLs of the providers, in the list and in get
		List<ToDoTask> toDoTasks = List.of(personal, projectToDo.getToDoTask(), otherProjectToDo.getToDoTask());
		for(Map<String,Object> item:items(structured(invoke(context(owner), Map.of())))) {
			ToDoTask toDoTask = toDoTasks.stream().filter(task -> task.getKey().equals(item.get("key"))).findFirst().orElseThrow();
			Assert.assertEquals(providerUrl(toDoTask), item.get("url"));
		}
		Map<String,Object> getItem = item(structured(invoke(context(owner), Map.of("action", "get", "toDoKey", projectToDo.getToDoTask().getKey()))));
		Assert.assertEquals(providerUrl(projectToDo.getToDoTask()), getItem.get("url"));
	}

	private String providerUrl(ToDoTask toDoTask) {
		String businessPath = toDoService.getProvider(toDoTask.getType()).getBusinessPath(toDoTask);
		return BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
	}

	@Test
	public void list_courseContext() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-5");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ToDoTask courseToDo = createCourseToDo(coach, participant, entry);
		dbInstance.commitAndCloseSession();

		List<Map<String,Object>> items = items(structured(invoke(context(participant), Map.of("context", "course", "originKey", entry.getKey()))));
		assertThat(keys(items)).containsExactly(courseToDo.getKey());
		@SuppressWarnings("unchecked")
		Map<String,Object> context = (Map<String,Object>)items.get(0).get("context");
		Assert.assertEquals("course", context.get("context"));
		Assert.assertEquals(entry.getKey(), context.get("originKey"));
		assertThat((String)items.get(0).get("url")).contains("/RepositoryEntry/" + entry.getKey() + "/ToDoTasks/0/");
	}

	@Test
	public void list_projectAll() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-7");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-8");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-9");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjToDo ownerToDo = createProjectToDo(owner, project, List.of(owner));
		ProjToDo participantToDo = createProjectToDo(owner, project, List.of(participant));
		dbInstance.commitAndCloseSession();

		assertThat(keys(items(structured(invoke(context(participant), Map.of("context", "project", "originKey", project.getKey()))))))
			.containsExactly(participantToDo.getToDoTask().getKey());
		assertThat(keys(items(structured(invoke(context(participant),
				Map.of("context", "project", "originKey", project.getKey(), "assignedToMe", Boolean.FALSE))))))
			.containsExactlyInAnyOrder(ownerToDo.getToDoTask().getKey(), participantToDo.getToDoTask().getKey());

		try {
			invoke(context(stranger), Map.of("context", "project", "originKey", project.getKey(), "assignedToMe", Boolean.FALSE));
			Assert.fail("A stranger must not list the ToDos of the project");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the ToDos", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void list_assignedToMeFalse_onlyProject() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-10");

		McpToolResult personal = invoke(context(id), Map.of("context", "personal", "assignedToMe", Boolean.FALSE));
		Assert.assertTrue(personal.error());
		assertThat(personal.text()).contains("context project");

		McpToolResult noOrigin = invoke(context(id), Map.of("context", "project", "assignedToMe", Boolean.FALSE));
		Assert.assertTrue(noOrigin.error());

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(id), Map.of("context", "project", "originKey", Long.valueOf(-3), "assignedToMe", Boolean.FALSE)));
		Assert.assertEquals("project with key -3", unknownProject.getObjectDescription());
		Assert.assertEquals("view the ToDos", unknownProject.getPermission());
	}

	@Test
	public void list_dueFilters() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-11");
		Date now = new Date();
		ToDoTask overdue = createPersonalToDo(id, "Overdue " + random(), ToDoStatus.open, DateUtils.addDays(now, -2));
		ToDoTask soon = createPersonalToDo(id, "Soon " + random(), ToDoStatus.open, DateUtils.addDays(now, 3));
		ToDoTask later = createPersonalToDo(id, "Later " + random(), ToDoStatus.open, DateUtils.addDays(now, 30));
		ToDoTask noDue = createPersonalToDo(id, "No due " + random(), ToDoStatus.open, null);
		dbInstance.commitAndCloseSession();

		assertThat(keys(items(structured(invoke(context(id), Map.of("overdue", Boolean.TRUE))))))
			.containsExactly(overdue.getKey());
		String inTenDays = McpToolSupport.isoDate(DateUtils.addDays(now, 10));
		assertThat(keys(items(structured(invoke(context(id), Map.of("dueBefore", inTenDays))))))
			.containsExactly(overdue.getKey(), soon.getKey());
		assertThat(keys(items(structured(invoke(context(id), Map.of("dueBefore", inTenDays, "overdue", Boolean.TRUE))))))
			.containsExactly(overdue.getKey());
		// due date order, ToDos without due date last
		assertThat(keys(items(structured(invoke(context(id), Map.of())))))
			.containsExactly(overdue.getKey(), soon.getKey(), later.getKey(), noDue.getKey());
	}

	@Test
	public void list_limitAndTruncated() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-12");
		for(int i=0; i<3; i++) {
			createPersonalToDo(id, "Limit " + i, ToDoStatus.open, null);
		}
		dbInstance.commitAndCloseSession();

		Map<String,Object> result = structured(invoke(context(id), Map.of("limit", 2)));
		assertThat(items(result)).hasSize(2);
		Assert.assertEquals(Integer.valueOf(3), result.get("total"));
		Assert.assertEquals(Boolean.TRUE, result.get("truncated"));
	}

	@Test
	public void list_invalidArguments() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-13");

		McpToolResult context = invoke(context(id), Map.of("context", "calendar"));
		Assert.assertTrue(context.error());
		assertThat(context.text()).contains("Unknown context calendar");

		McpToolResult originWithAll = invoke(context(id), Map.of("originKey", Long.valueOf(12)));
		Assert.assertTrue(originWithAll.error());
		assertThat(originWithAll.text()).contains("originKey");

		McpToolResult status = invoke(context(id), Map.of("status", List.of("deleted")));
		Assert.assertTrue(status.error());
		assertThat(status.text()).contains("Unknown status 'deleted'");

		McpToolResult date = invoke(context(id), Map.of("dueBefore", "next week"));
		Assert.assertTrue(date.error());
		assertThat(date.text()).contains("dueBefore").contains("ISO date");

		McpToolResult action = invoke(context(id), Map.of("action", "count"));
		Assert.assertTrue(action.error());
		assertThat(action.text()).contains("Unknown action count");
	}

	@Test
	public void get_assignee() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-14");
		ToDoTask toDo = createPersonalToDo(id, "Get " + random(), ToDoStatus.done, null);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(id), Map.of("action", "get", "toDoKey", toDo.getKey()));
		@SuppressWarnings("unchecked")
		Map<String,Object> item = (Map<String,Object>)structured(result).get("item");
		Assert.assertEquals(toDo.getKey(), item.get("key"));
		Assert.assertEquals("done", item.get("status"));
		assertThat(result.text()).startsWith("ToDo [done]");
	}

	@Test
	public void get_foreignPersonal_denied() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-15");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-16");
		ToDoTask toDo = createPersonalToDo(other, "Foreign " + random(), ToDoStatus.open, null);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(id), Map.of("action", "get", "toDoKey", toDo.getKey()));
			Assert.fail("A foreign personal ToDo must not be visible");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the ToDo", e.getPermission());
			Assert.assertEquals("ToDo with key " + toDo.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_projectMember_notAssigned() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-17");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-18");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-19");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		ProjToDo toDo = createProjectToDo(owner, project, List.of(owner));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(client), Map.of("action", "get", "toDoKey", toDo.getToDoTask().getKey()));
		Assert.assertFalse(result.text(), result.error());

		try {
			invoke(context(stranger), Map.of("action", "get", "toDoKey", toDo.getToDoTask().getKey()));
			Assert.fail("A stranger must not see the project ToDo");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the ToDo", e.getPermission());
		}
	}

	@Test
	public void get_courseToDo_creatorOnly_denied() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-30");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-31");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ToDoTask toDo = createCourseToDo(coach, participant, entry);
		dbInstance.commitAndCloseSession();

		// the assignee sees it, as in "My ToDos"
		McpToolResult result = invoke(context(participant), Map.of("action", "get", "toDoKey", toDo.getKey()));
		Assert.assertEquals(toDo.getKey(), item(structured(result)).get("key"));

		// the creator alone, for example a former coach, does not
		try {
			invoke(context(coach), Map.of("action", "get", "toDoKey", toDo.getKey()));
			Assert.fail("The creator alone must not see a course ToDo");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the ToDo", e.getPermission());
			Assert.assertEquals("ToDo with key " + toDo.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_projectToDo_creatorRemovedFromProject_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-32");
		Identity former = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-33");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, former, Set.of(ProjectRole.participant), null);
		ProjToDo toDo = createProjectToDo(former, project, List.of(owner));
		dbInstance.commitAndCloseSession();

		// a member of the project sees it
		McpToolResult result = invoke(context(former), Map.of("action", "get", "toDoKey", toDo.getToDoTask().getKey()));
		Assert.assertFalse(result.text(), result.error());

		projectService.removeMembers(owner, new ProjectBCFactory(), project, List.of(former));
		dbInstance.commitAndCloseSession();

		// the creator is no member any more and not assigned
		try {
			invoke(context(former), Map.of("action", "get", "toDoKey", toDo.getToDoTask().getKey()));
			Assert.fail("The creator removed from the project must not see the project ToDo");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the ToDo", e.getPermission());
		}
	}

	@Test
	public void get_invalidArguments() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-todo-20");

		McpToolResult missing = invoke(context(id), Map.of("action", "get"));
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("toDoKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknown = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(id), Map.of("action", "get", "toDoKey", Long.valueOf(-5))));
		Assert.assertEquals("ToDo with key -5", unknown.getObjectDescription());
		Assert.assertEquals("view the ToDo", unknown.getPermission());
	}

	private ToDoTask createPersonalToDo(Identity identity, String title, ToDoStatus status, Date dueDate) {
		ToDoTask toDoTask = toDoService.createToDoTask(identity, PersonalToDoProvider.TYPE, null, null, null, null, null);
		toDoTask.setAssigneeRights(new ToDoRight[] { ToDoRight.all });
		toDoTask.setTitle(title);
		toDoTask.setStatus(status);
		toDoTask.setDueDate(dueDate);
		toDoTask = toDoService.update(identity, toDoTask, ToDoStatus.open);
		toDoService.updateMember(identity, toDoTask, List.of(identity), List.of(), new ToDoAssignedMailSender(identity));
		return toDoTask;
	}

	private ToDoTask createCourseToDo(Identity coach, Identity participant, RepositoryEntry entry) {
		ToDoTask toDoTask = toDoService.createToDoTask(coach, CourseIndividualToDoTaskProvider.TYPE, entry.getKey(), null,
				entry.getDisplayname(), null, null);
		toDoTask.setTitle("Course " + random());
		toDoTask.setAssigneeRights(new ToDoRight[] { ToDoRight.status });
		toDoTask = toDoService.update(coach, toDoTask, ToDoStatus.open);
		toDoService.updateMember(coach, toDoTask, List.of(participant), List.of(), new ToDoAssignedMailSender(coach));
		return toDoTask;
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private ProjToDo createProjectToDo(Identity doer, ProjProject project, List<Identity> assignees) {
		ProjToDo toDo = projectService.createToDo(doer, project);
		projectService.updateToDo(doer, toDo, "Project ToDo " + random(), ToDoStatus.open, null, null, null, null, null);
		projectService.updateMembers(doer, toDo, assignees, List.of());
		return toDo;
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
	private static Map<String,Object> item(Map<String,Object> structured) {
		return (Map<String,Object>)structured.get("item");
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> items(Map<String,Object> structured) {
		return (List<Map<String,Object>>)structured.get("items");
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
