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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.DateUtils;
import org.olat.course.todo.manager.CourseCollectionToDoTaskProvider;
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
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoAssignedMailSender;
import org.olat.modules.todo.ToDoModule;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.manager.PersonalToDoProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool todo_modify: the permission matrix per
 * action and context, the service sequences and the side effects of project
 * ToDos (activity log, publisher news).
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class ToDoModifyToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(ToDoModifyToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private ToDoModule toDoModule;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ToDoModifyToolProvider sut;
	@Autowired
	private ToDoToolSupport toDoSupport;

	@Test
	public void definition() {
		Assert.assertEquals("todo_modify", sut.getToolName());
		Assert.assertEquals(ToDoMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.WRITE, sut.getScope());
		Assert.assertTrue(sut.getDefinition().destructive());
		Assert.assertFalse(sut.getDefinition().idempotent());
		assertThat(sut.getDefinition().description()).contains("todo_query").contains("set_status");
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "update", "toDoKey", 1, "dueDate", "",
				"assignees", List.of("jdoe", "jdoe@example.com"), "tags", List.of())).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "create", "context", "project", "originKey", 3,
				"title", "T", "priority", "urgent", "status", "inProgress", "startDate", "2026-10-01", "expenditureOfWork", "1w")).valid());
		// course is accepted by the schema, the tool answers with the actionable error
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "create", "context", "course", "title", "T")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("toDoKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "set_status", "toDoKey", 1, "status", "deleted")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "archive")).valid());
	}

	@Test
	public void invalidActions() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-1");

		McpToolResult missing = invoke(context(id), Map.of());
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("action is required");

		McpToolResult unknown = invoke(context(id), Map.of("action", "archive"));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).contains("Unknown action archive");

		McpToolResult noKey = invoke(context(id), Map.of("action", "set_status", "status", "done"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("toDoKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownKey = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(id), Map.of("action", "delete", "toDoKey", Long.valueOf(-9))));
		Assert.assertEquals("ToDo with key -9", unknownKey.getObjectDescription());
		Assert.assertEquals("view the ToDo", unknownKey.getPermission());
	}

	// create personal

	@Test
	public void create_personal() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-2");
		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("context", "personal");
		arguments.put("title", "Buy milk");
		arguments.put("description", "Two litres");
		arguments.put("priority", "high");
		arguments.put("startDate", "2026-10-05");
		arguments.put("dueDate", "2026-10-07");
		arguments.put("expenditureOfWork", "1d 2h");
		arguments.put("tags", List.of("shopping", "home"));

		McpToolResult result = invoke(context(id), arguments);
		Map<String,Object> item = item(result);
		assertThat(result.text()).startsWith("Created: [open] Buy milk");
		dbInstance.commitAndCloseSession();

		ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
		Assert.assertEquals(PersonalToDoProvider.TYPE, toDoTask.getType());
		Assert.assertEquals("Buy milk", toDoTask.getTitle());
		Assert.assertEquals("Two litres", toDoTask.getDescription());
		Assert.assertEquals(ToDoStatus.open, toDoTask.getStatus());
		Assert.assertEquals(ToDoPriority.high, toDoTask.getPriority());
		Assert.assertEquals("2026-10-05", item.get("startDate"));
		Assert.assertEquals("2026-10-07", item.get("dueDate"));
		Assert.assertEquals(toDoService.getHours(toDoService.getExpenditureOfWork(toDoTask.getExpenditureOfWork())), toDoTask.getExpenditureOfWork());
		Assert.assertEquals("1d 2h", item.get("expenditureOfWork"));
		assertThat(toDoTask.getAssigneeRights()).containsExactly(ToDoRight.all);
		ToDoTaskMembers members = toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL);
		assertThat(members.getMembers(ToDoRole.creator)).containsExactly(id);
		assertThat(members.getMembers(ToDoRole.assignee)).containsExactly(id);
		assertThat(members.getMembers(ToDoRole.delegatee)).isEmpty();
		assertThat(tags(toDoTask)).containsExactlyInAnyOrder("shopping", "home");
	}

	@Test
	public void create_personal_otherAssignee_refused() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-3");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-4");
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"assignees", List.of(McpToolSupport.username(other))));
		Assert.assertTrue(result.error());
		Assert.assertEquals("Invalid argument: " + McpToolSupport.username(other) + " is not a valid assignee for this ToDo.", result.text());
		Assert.assertEquals(Long.valueOf(0), countPersonalToDos(id));

		// An unknown user gets the same answer as an existing user outside the candidates
		String unknownName = "no-such-user-" + random();
		McpToolResult unknown = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"assignees", List.of(unknownName)));
		Assert.assertTrue(unknown.error());
		Assert.assertEquals("Invalid argument: " + unknownName + " is not a valid assignee for this ToDo.", unknown.text());

		McpToolResult otherEmail = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"delegatees", List.of(other.getUser().getEmail())));
		Assert.assertTrue(otherEmail.error());
		Assert.assertEquals("Invalid argument: " + other.getUser().getEmail() + " is not a valid delegatee for this ToDo.", otherEmail.text());

		// The own email is a valid candidate
		McpToolResult ownEmail = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"assignees", List.of(id.getUser().getEmail().toUpperCase())));
		Assert.assertFalse(ownEmail.text(), ownEmail.error());
		dbInstance.rollbackAndCloseSession();

		Assert.assertEquals(Long.valueOf(0), countPersonalToDos(id));

		// An empty list on create means the own account, as an absent argument
		Map<String,Object> empty = item(invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"assignees", List.of())));
		dbInstance.commitAndCloseSession();
		ToDoTask emptyTask = toDoService.getToDoTask(() -> (Long)empty.get("key"));
		assertThat(toDoService.getToDoTaskMembers(emptyTask, ToDoRole.ALL).getMembers(ToDoRole.assignee)).containsExactly(id);
	}

	@Test
	public void create_personal_notAllowedByModule() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-5");
		String personalCreate = toDoModule.getPersonalCreate();
		try {
			toDoModule.setPersonalCreate(ToDoModule.PERSONAL_CREATE_NONE);
			invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X"));
			Assert.fail("The module forbids personal ToDos");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create personal ToDos", e.getPermission());
		} finally {
			toDoModule.setPersonalCreate(personalCreate);
		}
	}

	@Test
	public void create_invalidArguments() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-6");

		McpToolResult noContext = invoke(context(id), Map.of("action", "create", "title", "X"));
		Assert.assertTrue(noContext.error());
		assertThat(noContext.text()).contains("context");

		McpToolResult noTitle = invoke(context(id), Map.of("action", "create", "context", "personal"));
		Assert.assertTrue(noTitle.error());
		assertThat(noTitle.text()).contains("title");

		McpToolResult date = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X", "dueDate", "tomorrow"));
		Assert.assertTrue(date.error());
		assertThat(date.text()).contains("dueDate");

		McpToolResult dates = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"startDate", "2026-10-10", "dueDate", "2026-10-01"));
		Assert.assertTrue(dates.error());
		assertThat(dates.text()).contains("before the startDate");

		McpToolResult expenditure = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"expenditureOfWork", "2h 30m"));
		Assert.assertTrue(expenditure.error());
		assertThat(expenditure.text()).contains("expenditureOfWork");

		McpToolResult priority = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"priority", "blocker"));
		Assert.assertTrue(priority.error());
		assertThat(priority.text()).contains("Unknown priority 'blocker'");
		Assert.assertEquals(Long.valueOf(0), countPersonalToDos(id));
	}

	@Test
	public void create_course_refused() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-7");

		McpToolResult course = invoke(context(id), Map.of("action", "create", "context", "course", "title", "X", "originKey", Long.valueOf(1)));
		Assert.assertTrue(course.error());
		assertThat(course.text()).isEqualTo("ToDos of type course are created and edited in the OpenOlat course, not via MCP. "
				+ "Use context personal or project; set_status and delete work for every ToDo.");

		McpToolResult curriculum = invoke(context(id), Map.of("action", "create", "context", "curriculum", "title", "X"));
		Assert.assertTrue(curriculum.error());
		assertThat(curriculum.text()).contains("course planner");

		McpToolResult quality = invoke(context(id), Map.of("action", "create", "context", "quality", "title", "X"));
		Assert.assertTrue(quality.error());
		assertThat(quality.text()).contains("quality management");
	}

	// create project

	@Test
	public void create_project() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-8");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-9");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("context", "project");
		arguments.put("originKey", project.getKey());
		arguments.put("title", "Write the report");
		arguments.put("status", "inProgress");
		arguments.put("dueDate", "2026-11-01");
		arguments.put("assignees", List.of(McpToolSupport.username(owner)));
		arguments.put("delegatees", List.of(participant.getUser().getEmail()));
		arguments.put("tags", List.of("report"));
		McpToolResult result = invoke(context(participant), arguments);
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
		Assert.assertEquals("project", toDoTask.getType());
		Assert.assertEquals(project.getKey(), toDoTask.getOriginId());
		Assert.assertEquals("Write the report", toDoTask.getTitle());
		Assert.assertEquals(ToDoStatus.inProgress, toDoTask.getStatus());
		assertThat(toDoTask.getAssigneeRights()).containsExactly(ToDoRight.edit);
		ToDoTaskMembers members = toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL);
		assertThat(members.getMembers(ToDoRole.creator)).containsExactly(participant);
		assertThat(members.getMembers(ToDoRole.assignee)).containsExactly(owner);
		assertThat(members.getMembers(ToDoRole.delegatee)).containsExactly(participant);
		assertThat(tags(toDoTask)).containsExactly("report");

		ProjToDo projToDo = projectService.getToDo(toDoTask.getOriginSubPath());
		Assert.assertNotNull(projToDo);
		assertThat(actions(project, participant)).contains(Action.toDoCreate, Action.toDoContentUpdate, Action.toDoMemberAdd, Action.toDoTagsUpdate);
		// one create and one content update, the sequence of the UI (create, then save the form)
		Assert.assertEquals(1, count(projToDo.getArtefact(), Action.toDoCreate));
		Assert.assertEquals(1, count(projToDo.getArtefact(), Action.toDoContentUpdate));
		assertNewsAfter(project, before);
	}

	@Test
	public void create_project_defaultAssignee() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-10");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "context", "project",
				"originKey", project.getKey(), "title", "Default")));
		dbInstance.commitAndCloseSession();

		ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
		Assert.assertEquals(ToDoStatus.open, toDoTask.getStatus());
		assertThat(toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL).getMembers(ToDoRole.assignee)).containsExactly(owner);
	}

	@Test
	public void create_project_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-11");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-12");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-13");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		dbInstance.commitAndCloseSession();

		for(Identity identity:List.of(client, stranger)) {
			try {
				invoke(context(identity), Map.of("action", "create", "context", "project", "originKey", project.getKey(), "title", "X"));
				Assert.fail("Clients and strangers cannot create project ToDos");
			} catch (McpPermissionException e) {
				Assert.assertEquals("create ToDos", e.getPermission());
				Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
			}
		}

		// A deleted project is read only
		projectService.setStatusDeleted(owner, new ProjectBCFactory(), project);
		dbInstance.commitAndCloseSession();
		try {
			invoke(context(owner), Map.of("action", "create", "context", "project", "originKey", project.getKey(), "title", "X"));
			Assert.fail("A deleted project is read only");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create ToDos", e.getPermission());
		}
		Assert.assertEquals(0l, countProjectToDos(project));
	}

	@Test
	public void create_project_moduleDisabled() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-60");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		boolean enabledBefore = projectModule.isEnabled();
		projectModule.setEnabled(false);
		try {
			McpToolResult result = invoke(context(owner), Map.of("action", "create", "context", "project",
					"originKey", project.getKey(), "title", "Module off"));
			Assert.assertTrue(result.error());
			assertThat(result.text()).contains("are not enabled on this OpenOlat instance");
		} finally {
			projectModule.setEnabled(enabledBefore);
		}
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0l, countProjectToDos(project));
	}

	@Test
	public void create_project_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-14");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-15");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult noOrigin = invoke(context(owner), Map.of("action", "create", "context", "project", "title", "X"));
		Assert.assertTrue(noOrigin.error());
		assertThat(noOrigin.text()).contains("originKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "create", "context", "project",
				"originKey", Long.valueOf(-4), "title", "X")));
		Assert.assertEquals("project with key -4", unknownProject.getObjectDescription());
		Assert.assertEquals("create ToDos", unknownProject.getPermission());

		McpToolResult notMember = invoke(context(owner), Map.of("action", "create", "context", "project",
				"originKey", project.getKey(), "title", "X", "assignees", List.of(McpToolSupport.username(stranger))));
		Assert.assertTrue(notMember.error());
		Assert.assertEquals("Invalid argument: " + McpToolSupport.username(stranger) + " is not a valid assignee for this ToDo.", notMember.text());

		String unknownName = "no-such-user-" + random();
		McpToolResult unknown = invoke(context(owner), Map.of("action", "create", "context", "project",
				"originKey", project.getKey(), "title", "X", "assignees", List.of(unknownName)));
		Assert.assertTrue(unknown.error());
		Assert.assertEquals("Invalid argument: " + unknownName + " is not a valid assignee for this ToDo.", unknown.text());
		// Nothing written before the validation
		Assert.assertEquals(0l, countProjectToDos(project));
	}

	// update

	@Test
	public void update_personal_onlySuppliedFields() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-16");
		ToDoTask toDo = createPersonalToDo(id, "Original", DateUtils.addDays(new Date(), 5));
		toDo.setDescription("Keep me");
		toDo = toDoService.update(id, toDo, ToDoStatus.open);
		toDoService.updateTags(toDo, List.of("old"));
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(id), Map.of("action", "update", "toDoKey", toDo.getKey(),
				"priority", "urgent", "dueDate", "", "tags", List.of("new1", "new2"))));
		dbInstance.commitAndCloseSession();

		ToDoTask reloaded = toDoService.getToDoTask(toDo);
		Assert.assertEquals("Original", reloaded.getTitle());
		Assert.assertEquals("Keep me", reloaded.getDescription());
		Assert.assertEquals(ToDoPriority.urgent, reloaded.getPriority());
		Assert.assertNull(reloaded.getDueDate());
		Assert.assertEquals(ToDoStatus.open, reloaded.getStatus());
		assertThat(tags(reloaded)).containsExactlyInAnyOrder("new1", "new2");
		Assert.assertEquals("urgent", item.get("priority"));

		// update cannot clear the assignees
		McpToolResult noAssignee = invoke(context(id), Map.of("action", "update", "toDoKey", toDo.getKey(), "assignees", List.of()));
		Assert.assertTrue(noAssignee.error());
		assertThat(noAssignee.text()).contains("at least one assignee");
		dbInstance.rollbackAndCloseSession();
		assertThat(toDoService.getToDoTaskMembers(reloaded, ToDoRole.ALL).getMembers(ToDoRole.assignee)).containsExactly(id);
	}

	@Test
	public void update_personal_foreign_denied() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-17");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-18");
		ToDoTask toDo = createPersonalToDo(other, "Foreign", null);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(id), Map.of("action", "update", "toDoKey", toDo.getKey(), "title", "Hijacked"));
			Assert.fail("A foreign personal ToDo cannot be edited");
		} catch (McpPermissionException e) {
			// who may not view the ToDo gets the answer of an unknown key
			Assert.assertEquals("view the ToDo", e.getPermission());
			Assert.assertEquals("ToDo with key " + toDo.getKey(), e.getObjectDescription());
		}
		McpToolResult emptyTitle = invoke(context(other), Map.of("action", "update", "toDoKey", toDo.getKey(), "title", " "));
		Assert.assertTrue(emptyTitle.error());
		assertThat(emptyTitle.text()).contains("title");
	}

	@Test
	public void update_project() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-19");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-20");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjToDo toDo = createProjectToDo(owner, project, List.of(owner));
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		Map<String,Object> item = item(invoke(context(participant), Map.of("action", "update",
				"toDoKey", toDo.getToDoTask().getKey(), "title", "Renamed", "assignees", List.of(McpToolSupport.username(participant)))));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals("Renamed", item.get("title"));
		ToDoTask reloaded = toDoService.getToDoTask(toDo.getToDoTask());
		Assert.assertEquals("Renamed", reloaded.getTitle());
		Assert.assertEquals(ToDoStatus.open, reloaded.getStatus());
		ToDoTaskMembers members = toDoService.getToDoTaskMembers(reloaded, ToDoRole.ALL);
		assertThat(members.getMembers(ToDoRole.assignee)).containsExactly(participant);
		assertThat(actions(project, participant)).contains(Action.toDoContentUpdate, Action.toDoMemberAdd);
		assertNewsAfter(project, before);
	}

	@Test
	public void update_project_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-21");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-22");
		Identity assignedClient = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-23");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		projectService.updateMember(owner, new ProjectBCFactory(), project, assignedClient, Set.of(ProjectRole.client), null);
		ProjToDo toDo = createProjectToDo(owner, project, List.of(assignedClient));
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(client), Map.of("action", "update", "toDoKey", toDo.getToDoTask().getKey(), "title", "No"));
			Assert.fail("A client without assignment cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("edit the ToDo", e.getPermission());
		}
		// The callback allows the assignee to edit
		McpToolResult result = invoke(context(assignedClient), Map.of("action", "update", "toDoKey", toDo.getToDoTask().getKey(), "title", "Yes"));
		Assert.assertFalse(result.text(), result.error());
	}

	@Test
	public void update_course_refused() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-24");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-25");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ToDoTask toDo = createCourseToDo(coach, participant, entry);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("action", "update", "toDoKey", toDo.getKey(), "title", "X"));
		Assert.assertTrue(result.error());
		assertThat(result.text()).contains("created and edited in the OpenOlat course");
	}

	// set_status

	@Test
	public void setStatus_personal() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-26");
		ToDoTask toDo = createPersonalToDo(id, "Status", null);
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(id), Map.of("action", "set_status", "toDoKey", toDo.getKey(), "status", "done")));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals("done", item.get("status"));
		ToDoTask reloaded = toDoService.getToDoTask(toDo);
		Assert.assertEquals(ToDoStatus.done, reloaded.getStatus());
		Assert.assertNotNull(reloaded.getDoneDate());
	}

	@Test
	public void setStatus_invalid() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-27");
		ToDoTask toDo = createPersonalToDo(id, "Status", null);
		dbInstance.commitAndCloseSession();

		McpToolResult missing = invoke(context(id), Map.of("action", "set_status", "toDoKey", toDo.getKey()));
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("status");

		McpToolResult deleted = invoke(context(id), Map.of("action", "set_status", "toDoKey", toDo.getKey(), "status", "deleted"));
		Assert.assertTrue(deleted.error());
		assertThat(deleted.text()).contains("Unknown status 'deleted'");
	}

	@Test
	public void setStatus_project_throughProvider() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-28");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-29");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-30");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjToDo toDo = createProjectToDo(owner, project, List.of(owner));
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		Map<String,Object> item = item(invoke(context(participant), Map.of("action", "set_status",
				"toDoKey", toDo.getToDoTask().getKey(), "status", "done")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("done", item.get("status"));
		Assert.assertEquals(ToDoStatus.done, toDoService.getToDoTask(toDo.getToDoTask()).getStatus());
		assertThat(actions(project, participant)).contains(Action.toDoContentUpdate);
		assertNewsAfter(project, before);

		try {
			invoke(context(stranger), Map.of("action", "set_status", "toDoKey", toDo.getToDoTask().getKey(), "status", "open"));
			Assert.fail("A stranger cannot change the status");
		} catch (McpPermissionException e) {
			// who may not view the ToDo gets the answer of an unknown key
			Assert.assertEquals("view the ToDo", e.getPermission());
		}
	}

	@Test
	public void setStatus_course_throughProvider() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-31");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-32");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-33");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ToDoTask toDo = createCourseToDo(coach, participant, entry);
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(participant), Map.of("action", "set_status", "toDoKey", toDo.getKey(), "status", "inProgress")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("inProgress", item.get("status"));
		Assert.assertEquals(ToDoStatus.inProgress, toDoService.getToDoTask(toDo).getStatus());

		try {
			invoke(context(stranger), Map.of("action", "set_status", "toDoKey", toDo.getKey(), "status", "done"));
			Assert.fail("A stranger cannot change the status");
		} catch (McpPermissionException e) {
			// who may not view the ToDo gets the answer of an unknown key
			Assert.assertEquals("view the ToDo", e.getPermission());
		}
	}

	// delete

	@Test
	public void delete_personal() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-34");
		ToDoTask toDo = createPersonalToDo(id, "Delete", null);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(id), Map.of("action", "delete", "toDoKey", toDo.getKey()));
		Assert.assertEquals("deleted", item(result).get("status"));
		dbInstance.commitAndCloseSession();

		ToDoTask reloaded = toDoService.getToDoTask(toDo);
		Assert.assertEquals(ToDoStatus.deleted, reloaded.getStatus());
		Assert.assertEquals(id, reloaded.getDeletedBy());

		McpToolResult again = invoke(context(id), Map.of("action", "delete", "toDoKey", toDo.getKey()));
		Assert.assertTrue(again.error());
		assertThat(again.text()).contains("is deleted");
	}

	@Test
	public void delete_course_assigneeWithoutRight_denied() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-35");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-36");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ToDoTask toDo = createCourseToDo(coach, participant, entry);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(participant), Map.of("action", "delete", "toDoKey", toDo.getKey()));
			Assert.fail("The assignee rights of a course ToDo do not include delete");
		} catch (McpPermissionException e) {
			Assert.assertEquals("delete the ToDo", e.getPermission());
		}
		Assert.assertEquals(ToDoStatus.open, toDoService.getToDoTask(toDo).getStatus());
	}

	@Test
	public void delete_project() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-37");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-38");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjToDo toDo = createProjectToDo(owner, project, List.of(participant));
		dbInstance.commitAndCloseSession();

		// A participant who is not the creator cannot delete
		try {
			invoke(context(participant), Map.of("action", "delete", "toDoKey", toDo.getToDoTask().getKey()));
			Assert.fail("A participant cannot delete a ToDo of another creator");
		} catch (McpPermissionException e) {
			Assert.assertEquals("delete the ToDo", e.getPermission());
		}

		Date before = setPublisherNewsInPast(project);
		McpToolResult result = invoke(context(owner), Map.of("action", "delete", "toDoKey", toDo.getToDoTask().getKey()));
		Assert.assertEquals("deleted", item(result).get("status"));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(ToDoStatus.deleted, toDoService.getToDoTask(toDo.getToDoTask()).getStatus());
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setToDos(List.of(toDo));
		Assert.assertEquals(ProjectStatus.deleted, projectService.getToDos(searchParams).get(0).getArtefact().getStatus());
		assertThat(actions(project, owner)).contains(Action.toDoStatusDelete);
		assertNewsAfter(project, before);
	}

	@Test
	public void delete_project_creatorRemovedFromProject_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-61");
		Identity former = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-62");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, former, Set.of(ProjectRole.participant), null);
		// the former member created the ToDo and stays assignee, so he can still view it
		ProjToDo toDo = createProjectToDo(former, project, List.of(owner, former));
		dbInstance.commitAndCloseSession();

		projectService.removeMembers(owner, new ProjectBCFactory(), project, List.of(former));
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(former), Map.of("action", "delete", "toDoKey", toDo.getToDoTask().getKey()));
			Assert.fail("The creator removed from the project cannot delete the ToDo");
		} catch (McpPermissionException e) {
			Assert.assertEquals("delete the ToDo", e.getPermission());
		}
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(ToDoStatus.open, toDoService.getToDoTask(toDo.getToDoTask()).getStatus());

		// the owner can open the project and delete it
		McpToolResult result = invoke(context(owner), Map.of("action", "delete", "toDoKey", toDo.getToDoTask().getKey()));
		Assert.assertEquals("deleted", item(result).get("status"));
	}

	// field rights of the assignee (ToDoTaskEditForm.updateUIByAssigneeRight)

	@Test
	public void assigneeRights_perField() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-40");
		ToDoTask toDo = createPersonalToDo(id, "Rights", null);
		toDo.setAssigneeRights(new ToDoRight[] { ToDoRight.title, ToDoRight.dueDate });
		toDo = toDoService.update(id, toDo, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(id), Map.of("action", "set_status", "toDoKey", toDo.getKey(), "status", "done"));
			Assert.fail("Without the status right the status cannot change");
		} catch (McpPermissionException e) {
			Assert.assertEquals("change the status of the ToDo", e.getPermission());
		}
		for(String field:List.of("description", "status", "priority", "startDate", "expenditureOfWork", "assignees", "delegatees", "tags")) {
			Object value = switch(field) {
				case "status" -> "done";
				case "priority" -> "low";
				case "startDate" -> "2026-10-01";
				case "expenditureOfWork" -> "2h";
				case "assignees", "delegatees", "tags" -> List.of(McpToolSupport.username(id));
				default -> "text";
			};
			try {
				invoke(context(id), Map.of("action", "update", "toDoKey", toDo.getKey(), field, value));
				Assert.fail("Without the right the field " + field + " cannot change");
			} catch (McpPermissionException e) {
				Assert.assertEquals("edit the " + field + " of the ToDo", e.getPermission());
			}
		}

		Map<String,Object> item = item(invoke(context(id), Map.of("action", "update", "toDoKey", toDo.getKey(),
				"title", "Allowed", "dueDate", "2026-12-24")));
		Assert.assertEquals("Allowed", item.get("title"));
		Assert.assertEquals("2026-12-24", item.get("dueDate"));
		Assert.assertEquals("open", item.get("status"));
	}

	@Test
	public void assigneeRights_statusOnly() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-41");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-42");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ToDoTask toDo = createCourseToDo(coach, participant, entry);
		toDo.setAssigneeRights(new ToDoRight[] { ToDoRight.title });
		toDo = toDoService.update(coach, toDo, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		// The edit right on the title does not include the status
		try {
			invoke(context(participant), Map.of("action", "set_status", "toDoKey", toDo.getKey(), "status", "done"));
			Assert.fail("Without the status right the status cannot change");
		} catch (McpPermissionException e) {
			Assert.assertEquals("change the status of the ToDo", e.getPermission());
		}
		Assert.assertEquals(ToDoStatus.open, toDoService.getToDoTask(toDo).getStatus());
	}

	// validation rules of the UI

	/**
	 * V3: tags are de-duplicated regardless of case and reuse the spelling of
	 * the existing tags of the context, as the tag selection of the UI.
	 */
	@Test
	public void tags_deduplicatedAndMatched_personal() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-50");
		ToDoTask existing = createPersonalToDo(id, "Existing", null);
		String urgent = "Urgent" + random();
		toDoService.updateTags(existing, List.of(urgent));
		dbInstance.commitAndCloseSession();

		String review = "review" + random();
		Map<String,Object> item = item(invoke(context(id), Map.of("action", "create", "context", "personal", "title", "Tags",
				"tags", List.of(urgent.toLowerCase(), urgent.toUpperCase(), review, review.toUpperCase()))));
		dbInstance.commitAndCloseSession();

		ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
		assertThat(tags(toDoTask)).containsExactlyInAnyOrder(urgent, review);
	}

	@Test
	public void tags_deduplicatedAndMatched_project() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-51");
		ProjProject project = createProject(owner);
		ProjToDo existing = createProjectToDo(owner, project, List.of(owner));
		String report = "Report" + random();
		projectService.updateTags(owner, existing, List.of(report));
		ProjToDo toDo = createProjectToDo(owner, project, List.of(owner));
		dbInstance.commitAndCloseSession();

		item(invoke(context(owner), Map.of("action", "update", "toDoKey", toDo.getToDoTask().getKey(),
				"tags", List.of(report.toLowerCase(), report.toLowerCase()))));
		dbInstance.commitAndCloseSession();

		assertThat(tags(toDo.getToDoTask())).containsExactly(report);
	}

	/**
	 * V4: the rule of ToDoTaskEditForm, the delegatees must not all be assignees.
	 */
	@Test
	public void delegateeIsAssignee_refused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-52");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-53");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjToDo toDo = createProjectToDo(owner, project, List.of(owner));
		dbInstance.commitAndCloseSession();
		String ownerName = McpToolSupport.username(owner);
		String participantName = McpToolSupport.username(participant);

		McpToolResult create = invoke(context(owner), Map.of("action", "create", "context", "project", "originKey", project.getKey(),
				"title", "X", "assignees", List.of(ownerName), "delegatees", List.of(ownerName)));
		Assert.assertTrue(create.error());
		assertThat(create.text()).contains("A delegatee must not also be an assignee");
		Assert.assertEquals(1l, countProjectToDos(project));

		// The current assignee as the only delegatee
		McpToolResult update = invoke(context(owner), Map.of("action", "update", "toDoKey", toDo.getToDoTask().getKey(),
				"delegatees", List.of(ownerName)));
		Assert.assertTrue(update.error());
		assertThat(update.text()).contains("A delegatee must not also be an assignee");
		dbInstance.rollbackAndCloseSession();

		// One delegatee who is not an assignee is enough
		item(invoke(context(owner), Map.of("action", "update", "toDoKey", toDo.getToDoTask().getKey(),
				"assignees", List.of(ownerName), "delegatees", List.of(ownerName, participantName))));
		dbInstance.commitAndCloseSession();
		ToDoTaskMembers members = toDoService.getToDoTaskMembers(toDo.getToDoTask(), ToDoRole.ALL);
		assertThat(members.getMembers(ToDoRole.delegatee)).containsExactlyInAnyOrder(owner, participant);
	}

	/**
	 * V5: with the default setting none the members of a personal ToDo are
	 * fixed, as the disabled fields of the UI.
	 */
	@Test
	public void personalCandidates_none() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-54");
		Identity delegatee = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-55");
		String assigneeCandidate = toDoModule.getPersonalAssigneeCandidate();
		String delegateeCandidate = toDoModule.getPersonalDelegateeCandidate();
		try {
			toDoModule.setPersonalAssigneeCandidate(ToDoModule.PERSONAL_CANDIDATE_NONE);
			toDoModule.setPersonalDelegateeCandidate(ToDoModule.PERSONAL_CANDIDATE_NONE);
			ToDoTask toDo = createPersonalToDo(id, "Fixed", null);
			toDoService.updateMember(id, toDo, List.of(id), List.of(delegatee), new ToDoAssignedMailSender(id));
			dbInstance.commitAndCloseSession();

			// The own account as the only assignee is accepted on create
			item(invoke(context(id), Map.of("action", "create", "context", "personal", "title", "Own",
					"assignees", List.of(McpToolSupport.username(id)))));
			dbInstance.commitAndCloseSession();

			// No delegatee on create
			McpToolResult ownDelegatee = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
					"delegatees", List.of(McpToolSupport.username(id))));
			Assert.assertTrue(ownDelegatee.error());
			assertThat(ownDelegatee.text()).contains("delegatees of personal ToDos cannot be changed");

			// The delegatee cannot take over the ToDo
			McpToolResult takeOver = invoke(context(delegatee), Map.of("action", "update", "toDoKey", toDo.getKey(),
					"assignees", List.of(McpToolSupport.username(delegatee))));
			Assert.assertTrue(takeOver.error());
			assertThat(takeOver.text()).contains("assignees of personal ToDos cannot be changed");
			dbInstance.rollbackAndCloseSession();

			// The current lists are accepted unchanged
			item(invoke(context(id), Map.of("action", "update", "toDoKey", toDo.getKey(), "title", "Same members",
					"assignees", List.of(McpToolSupport.username(id)), "delegatees", List.of(McpToolSupport.username(delegatee)))));
			dbInstance.commitAndCloseSession();
			ToDoTaskMembers members = toDoService.getToDoTaskMembers(toDo, ToDoRole.ALL);
			assertThat(members.getMembers(ToDoRole.assignee)).containsExactly(id);
			assertThat(members.getMembers(ToDoRole.delegatee)).containsExactly(delegatee);
		} finally {
			toDoModule.setPersonalAssigneeCandidate(assigneeCandidate);
			toDoModule.setPersonalDelegateeCandidate(delegateeCandidate);
		}
	}

	/**
	 * V5, N1: with the setting all the users of the user search are
	 * candidates. A caller without the admin properties of the user search
	 * names a new user by "First Last" only, never by email or username.
	 */
	@Test
	public void personalCandidates_all() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-56");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-57");
		Identity third = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-58");
		Identity fourth = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-58b");
		dbInstance.commitAndCloseSession();
		String assigneeCandidate = toDoModule.getPersonalAssigneeCandidate();
		String delegateeCandidate = toDoModule.getPersonalDelegateeCandidate();
		try {
			toDoModule.setPersonalAssigneeCandidate(ToDoModule.PERSONAL_CANDIDATE_ALL);
			toDoModule.setPersonalDelegateeCandidate(ToDoModule.PERSONAL_CANDIDATE_ALL);
			Assert.assertFalse(securityModule.isUserAllowedAdminProps(securityManager.getRoles(id)));

			// By display name, case insensitive
			Map<String,Object> item = item(invoke(context(id), Map.of("action", "create", "context", "personal", "title", "Shared",
					"assignees", List.of(McpToolSupport.username(id), displayName(other)),
					"delegatees", List.of(displayName(third).toUpperCase()))));
			dbInstance.commitAndCloseSession();
			ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
			ToDoTaskMembers members = toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL);
			assertThat(members.getMembers(ToDoRole.assignee)).containsExactlyInAnyOrder(id, other);
			assertThat(members.getMembers(ToDoRole.delegatee)).containsExactly(third);

			// The current members keep the username todo_query returns
			item(invoke(context(id), Map.of("action", "update", "toDoKey", toDoTask.getKey(),
					"assignees", List.of(McpToolSupport.username(id), McpToolSupport.username(other)))));
			dbInstance.commitAndCloseSession();

			// An unknown user is refused
			String unknownName = "no-such-user-" + random();
			assertInvalidAssignee(context(id), toDoTask, unknownName);
			// A new user by email or by username is refused with the same text
			assertInvalidAssignee(context(id), toDoTask, fourth.getUser().getEmail());
			assertInvalidAssignee(context(id), toDoTask, McpToolSupport.username(fourth));

			// The delegate rule applies to personal ToDos too
			McpToolResult self = invoke(context(id), Map.of("action", "update", "toDoKey", toDoTask.getKey(),
					"delegatees", List.of(McpToolSupport.username(id))));
			Assert.assertTrue(self.error());
			assertThat(self.text()).contains("A delegatee must not also be an assignee");
		} finally {
			toDoModule.setPersonalAssigneeCandidate(assigneeCandidate);
			toDoModule.setPersonalDelegateeCandidate(delegateeCandidate);
		}
	}

	/**
	 * N1: a display name that more than one user carries is refused.
	 */
	@Test
	public void personalCandidates_all_ambiguous() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-80");
		Identity twin1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-81");
		Identity twin2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-82");
		String lastName = "Twin" + random();
		rename(twin1, "Alex", lastName);
		rename(twin2, "Alex", lastName);
		ToDoTask toDoTask = createPersonalToDo(id, "Ambiguous", null);
		dbInstance.commitAndCloseSession();
		String assigneeCandidate = toDoModule.getPersonalAssigneeCandidate();
		try {
			toDoModule.setPersonalAssigneeCandidate(ToDoModule.PERSONAL_CANDIDATE_ALL);

			McpToolResult ambiguous = invoke(context(id), Map.of("action", "update", "toDoKey", toDoTask.getKey(),
					"assignees", List.of("alex " + lastName.toLowerCase())));
			Assert.assertTrue(ambiguous.error());
			assertThat(ambiguous.text()).contains("is ambiguous: more than one user has this name");
			dbInstance.rollbackAndCloseSession();
			assertThat(toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL).getMembers(ToDoRole.assignee)).containsExactly(id);
		} finally {
			toDoModule.setPersonalAssigneeCandidate(assigneeCandidate);
		}
	}

	/**
	 * N1: a caller with the admin properties of the user search names a user
	 * by email too.
	 */
	@Test
	public void personalCandidates_all_adminByEmail() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-tdm-83");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-84");
		dbInstance.commitAndCloseSession();
		String assigneeCandidate = toDoModule.getPersonalAssigneeCandidate();
		try {
			toDoModule.setPersonalAssigneeCandidate(ToDoModule.PERSONAL_CANDIDATE_ALL);
			Assert.assertTrue(securityModule.isUserAllowedAdminProps(securityManager.getRoles(admin)));

			Map<String,Object> item = item(invoke(context(admin), Map.of("action", "create", "context", "personal", "title", "By email",
					"assignees", List.of(other.getUser().getEmail()))));
			dbInstance.commitAndCloseSession();
			ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
			assertThat(toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL).getMembers(ToDoRole.assignee)).containsExactly(other);
		} finally {
			toDoModule.setPersonalAssigneeCandidate(assigneeCandidate);
		}
	}

	/**
	 * N2: a user of another organisation and a deleted user get the text of
	 * an unknown user, by display name and, for an administrator, by email.
	 */
	@Test
	public void personalCandidates_all_outOfScope() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-85");
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-tdm-86");
		Organisation otherOrganisation = organisationService.createOrganisation("mcp-tdm-org-" + random(), "mcp-tdm-org", null, null, null, admin);
		Identity foreign = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-87", otherOrganisation, null);
		Identity deleted = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-88");
		deleted = securityManager.saveIdentityStatus(deleted, Identity.STATUS_DELETED, admin);
		ToDoTask toDoTask = createPersonalToDo(id, "Scope", null);
		ToDoTask adminToDoTask = createPersonalToDo(admin, "Scope admin", null);
		dbInstance.commitAndCloseSession();
		String assigneeCandidate = toDoModule.getPersonalAssigneeCandidate();
		try {
			toDoModule.setPersonalAssigneeCandidate(ToDoModule.PERSONAL_CANDIDATE_ALL);

			assertInvalidAssignee(context(id), toDoTask, displayName(foreign));
			assertInvalidAssignee(context(id), toDoTask, displayName(deleted));
			assertInvalidAssignee(context(admin), adminToDoTask, foreign.getUser().getEmail());
			assertInvalidAssignee(context(admin), adminToDoTask, deleted.getUser().getEmail());
		} finally {
			toDoModule.setPersonalAssigneeCandidate(assigneeCandidate);
		}
	}

	private void assertInvalidAssignee(McpToolContext toolContext, ToDoTask toDoTask, String value) {
		McpToolResult result = invoke(toolContext, Map.of("action", "update", "toDoKey", toDoTask.getKey(),
				"assignees", List.of(value)));
		Assert.assertTrue(value, result.error());
		Assert.assertEquals("Invalid argument: " + value + " is not a valid assignee for this ToDo.", result.text());
		dbInstance.rollbackAndCloseSession();
	}

	private static String displayName(Identity identity) {
		return identity.getUser().getFirstName() + " " + identity.getUser().getLastName();
	}

	private void rename(Identity identity, String firstName, String lastName) {
		User user = identity.getUser();
		user.setProperty(UserConstants.FIRSTNAME, firstName);
		user.setProperty(UserConstants.LASTNAME, lastName);
		userManager.updateUser(identity, user);
	}

	/**
	 * V6: a ToDo of a project template has no dates and no members.
	 */
	@Test
	public void template_datesAndMembers_refused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-59");
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		project = projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Template " + random(), null, null, true, false);
		ProjToDo toDo = createProjectToDo(owner, project, List.of(owner));
		dbInstance.commitAndCloseSession();

		for(String name:List.of("startDate", "dueDate", "assignees", "delegatees")) {
			Object value = name.endsWith("Date") ? "2026-12-01" : List.of(McpToolSupport.username(owner));
			McpToolResult create = invoke(context(owner), Map.of("action", "create", "context", "project", "originKey", project.getKey(),
					"title", "X", name, value));
			Assert.assertTrue(name, create.error());
			assertThat(create.text()).contains("ToDos of a project template have no " + name);

			McpToolResult update = invoke(context(owner), Map.of("action", "update", "toDoKey", toDo.getToDoTask().getKey(), name, value));
			Assert.assertTrue(name, update.error());
			assertThat(update.text()).contains("ToDos of a project template have no " + name);
		}
		Assert.assertEquals(1l, countProjectToDos(project));

		// Without these arguments the own account is the assignee, as in the UI
		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "context", "project",
				"originKey", project.getKey(), "title", "Template ToDo")));
		dbInstance.commitAndCloseSession();
		ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
		Assert.assertNull(toDoTask.getDueDate());
		assertThat(toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL).getMembers(ToDoRole.assignee)).containsExactly(owner);
	}

	/**
	 * V7: the stored description and title are not checked against the limits of MCP.
	 */
	@Test
	public void update_longStoredDescription() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-60");
		ToDoTask toDo = createPersonalToDo(id, "Long", null);
		toDo.setDescription("x".repeat(5000));
		toDo = toDoService.update(id, toDo, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		item(invoke(context(id), Map.of("action", "update", "toDoKey", toDo.getKey(), "priority", "high")));
		dbInstance.commitAndCloseSession();
		ToDoTask reloaded = toDoService.getToDoTask(toDo);
		Assert.assertEquals(ToDoPriority.high, reloaded.getPriority());
		Assert.assertEquals(5000, reloaded.getDescription().length());

		McpToolResult tooLong = invoke(context(id), Map.of("action", "update", "toDoKey", toDo.getKey(), "description", "y".repeat(4001)));
		Assert.assertTrue(tooLong.error());
		assertThat(tooLong.text()).contains("description is longer than 4000");
	}

	/**
	 * V8: control characters are removed as the text fields of the UI do.
	 */
	@Test
	public void create_controlCharactersRemoved() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-61");
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(id), Map.of("action", "create", "context", "personal",
				"title", "Clean\u0000 title\u0007", "description", "a\u0000b\nc")));
		dbInstance.commitAndCloseSession();
		ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
		Assert.assertEquals("Clean title", toDoTask.getTitle());
		Assert.assertEquals("ab\nc", toDoTask.getDescription());
	}

	/**
	 * V12: the expenditure of work has bounded digits and a clear error.
	 */
	@Test
	public void expenditure_bounded() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-62");
		dbInstance.commitAndCloseSession();

		for(String value:List.of("999999999999999999w", "1234567890", "1234567w", "1.5h")) {
			McpToolResult result = invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
					"expenditureOfWork", value));
			Assert.assertTrue(value, result.error());
			assertThat(result.text()).contains("expenditureOfWork must have the format");
		}
		Assert.assertEquals(Long.valueOf(0), countPersonalToDos(id));

		Map<String,Object> item = item(invoke(context(id), Map.of("action", "create", "context", "personal", "title", "X",
				"expenditureOfWork", "999999w 999999d 999999h")));
		dbInstance.commitAndCloseSession();
		ToDoTask toDoTask = toDoService.getToDoTask(() -> (Long)item.get("key"));
		assertThat(toDoTask.getExpenditureOfWork()).isPositive();
	}

	/**
	 * V13: a provider that ignores the status change gets an answer with the stored status.
	 */
	@Test
	public void setStatus_ignoredByProvider() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-63");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-64");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ToDoTask toDoTask = toDoService.createToDoTask(coach, CourseCollectionToDoTaskProvider.TYPE, entry.getKey(), null,
				entry.getDisplayname(), null, null);
		toDoTask.setTitle("Collection " + random());
		toDoTask.setAssigneeRights(new ToDoRight[] { ToDoRight.status });
		toDoTask = toDoService.update(coach, toDoTask, ToDoStatus.open);
		toDoService.updateMember(coach, toDoTask, List.of(participant), List.of(), new ToDoAssignedMailSender(coach));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("action", "set_status", "toDoKey", toDoTask.getKey(), "status", "done"));
		Map<String,Object> item = item(result);
		Assert.assertEquals("open", item.get("status"));
		assertThat(result.text()).startsWith("Status unchanged").contains("the status is open");
		Assert.assertEquals(ToDoStatus.open, toDoService.getToDoTask(toDoTask).getStatus());
	}

	// bounded inputs

	@Test
	public void boundedInputs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tdm-43");
		dbInstance.commitAndCloseSession();
		String self = McpToolSupport.username(id);

		assertBoundError(id, Map.of("title", "x".repeat(121)), "title is longer than 120");
		assertBoundError(id, Map.of("description", "x".repeat(4001)), "description is longer than 4000");
		assertBoundError(id, Map.of("expenditureOfWork", "1".repeat(31)), "expenditureOfWork is longer than 30");
		// 51 different tags, equal tags are merged before the count
		assertBoundError(id, Map.of("tags", IntStream.range(0, 51).mapToObj(i -> "t" + i).toList()), "tags has more than 50");
		assertBoundError(id, Map.of("tags", List.of("x".repeat(65))), "tags is longer than 64");
		assertBoundError(id, Map.of("assignees", java.util.Collections.nCopies(51, self)), "assignees has more than 50");
		assertBoundError(id, Map.of("delegatees", List.of("x".repeat(256))), "delegatees is longer than 255");
		Assert.assertEquals(Long.valueOf(0), countPersonalToDos(id));

		// The upper bounds themselves are accepted
		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("context", "personal");
		arguments.put("title", "x".repeat(120));
		arguments.put("description", "x".repeat(4000));
		arguments.put("tags", List.of("x".repeat(64)));
		McpToolResult result = invoke(context(id), arguments);
		Assert.assertFalse(result.text(), result.error());
	}

	private void assertBoundError(Identity id, Map<String,Object> bound, String message) {
		Map<String,Object> arguments = new HashMap<>(bound);
		arguments.put("action", "create");
		arguments.put("context", "personal");
		arguments.putIfAbsent("title", "Bounded");
		McpToolResult result = invoke(context(id), arguments);
		Assert.assertTrue(result.error());
		assertThat(result.text()).contains(message);
		JsonSchemaValidator.ValidationResponse response = VALIDATOR.validate(sut.getDefinition().inputSchema(), arguments);
		Assert.assertFalse("The schema rejects it too: " + bound.keySet(), response.valid());
	}

	// helpers

	private ToDoTask createPersonalToDo(Identity identity, String title, Date dueDate) {
		ToDoTask toDoTask = toDoService.createToDoTask(identity, PersonalToDoProvider.TYPE, null, null, null, null, null);
		toDoTask.setAssigneeRights(new ToDoRight[] { ToDoRight.all });
		toDoTask.setTitle(title);
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

	private int count(ProjArtefact artefact, Action action) {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setArtefacts(List.of(artefact));
		searchParams.setActions(List.of(action));
		return projectService.getActivities(searchParams, 0, 100).size();
	}

	private List<Action> actions(ProjProject project, Identity doer) {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setDoer(doer);
		return projectService.getActivities(searchParams, 0, 100).stream().map(ProjActivity::getAction).toList();
	}

	/**
	 * Sets the news date of the project publisher one day back. The owner
	 * subscribed at creation, so the publisher exists.
	 *
	 * @return The time before the tool call
	 */
	private Date setPublisherNewsInPast(ProjProject project) {
		Publisher publisher = notificationsManager.getPublisher(projectService.getSubscriptionContext(project));
		Assert.assertNotNull(publisher);
		publisher.setLatestNewsDate(DateUtils.addDays(new Date(), -1));
		dbInstance.getCurrentEntityManager().merge(publisher);
		dbInstance.commitAndCloseSession();
		return DateUtils.addSeconds(new Date(), -1);
	}

	private void assertNewsAfter(ProjProject project, Date before) {
		dbInstance.commitAndCloseSession();
		Publisher publisher = notificationsManager.getPublisher(projectService.getSubscriptionContext(project));
		assertThat(publisher.getLatestNewsDate()).isAfterOrEqualTo(before);
	}

	private List<String> tags(ToDoTask toDoTask) {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		return toDoService.getToDoTaskTags(searchParams).stream().map(ToDoTaskTag::getTag).map(tag -> tag.getDisplayName()).toList();
	}

	private Long countPersonalToDos(Identity identity) {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setAssigneeOrDelegatee(identity);
		searchParams.setTypes(List.of(PersonalToDoProvider.TYPE));
		return toDoService.getToDoTaskCount(searchParams);
	}

	private long countProjectToDos(ProjProject project) {
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setProject(project);
		return projectService.getToDosCount(searchParams);
	}

	private McpToolContext context(Identity identity) {
		return new McpToolContext(identity, securityManager.getRoles(identity), GRANTS, null, Locale.ENGLISH, "127.0.0.1");
	}

	/**
	 * @return The item of the structured content, validated against the output schema of the tool
	 */
	@SuppressWarnings("unchecked")
	/**
	 * Checks the minimal result of the modify tool and returns the full item
	 * as the query tool builds it, for the assertions on the persisted object.
	 */
	private Map<String,Object> item(McpToolResult result) {
		Assert.assertFalse(result.text(), result.error());
		JsonSchemaValidator.ValidationResponse response = VALIDATOR.validate(sut.getDefinition().outputSchema(), result.structuredContent());
		Assert.assertTrue(response.errorMessage(), response.valid());
		Map<String,Object> minimal = (Map<String,Object>)((Map<String,Object>)result.structuredContent()).get("item");
		Assert.assertNotNull(minimal);
		assertThat(ToDoModifyToolProvider.RESULT_FIELDS).containsAll(minimal.keySet());
		assertThat(result.text()).endsWith("Call todo_query get for the full item.");
		Long key = (Long)minimal.get("key");
		return toDoSupport.toItem(toDoSupport.loadToDo(key), null, null);
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
