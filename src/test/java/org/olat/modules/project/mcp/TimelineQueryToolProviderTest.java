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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.mcp.McpGrants;
import org.olat.mcp.manager.McpJsonSchemaValidator;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
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
 * Integration tests of the tool timeline_query: the activities of a project
 * with window, type filter, detail actions and the artefact references.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class TimelineQueryToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(TimelineQueryToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TimelineQueryToolProvider sut;

	@Test
	public void definition() {
		Assert.assertEquals("timeline_query", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.READ, sut.getScope());
		assertThat(sut.getDefinition().description()).contains("project_query").contains("allActions");
		Assert.assertFalse(sut.getDefinition().destructive());
		Assert.assertTrue(sut.getDefinition().idempotent());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("projectKey", 1)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("projectKey", 1, "from", "2026-09-01", "to", "2026-09-30", "types", List.of("toDo", "note"), "allActions", true, "limit", 10)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of()).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 1, "types", List.of("whiteboard"))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 1, "action", "list")).valid());
	}

	@Test
	public void invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tl-1");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult noKey = invoke(context(owner), Map.of());
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknown = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("projectKey", Long.valueOf(-4))));
		Assert.assertEquals("project with key -4", unknown.getObjectDescription());
		Assert.assertEquals("view the timeline", unknown.getPermission());

		McpToolResult reversed = invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "2026-10-02", "to", "2026-10-01"));
		Assert.assertTrue(reversed.error());
		assertThat(reversed.text()).contains("before from");

		McpToolResult tooLong = invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "2024-01-01", "to", "2026-10-01"));
		Assert.assertTrue(tooLong.error());
		assertThat(tooLong.text()).contains("366");

		McpToolResult badDate = invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "last week"));
		Assert.assertTrue(badDate.error());
		assertThat(badDate.text()).contains("from");
	}

	@Test
	public void list_activitiesWithArtefacts() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tl-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tl-3");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote note = projectService.createNote(participant, project);
		projectService.updateNote(participant, note, UUID.randomUUID().toString(), "Kickoff note", "text");
		ProjToDo toDo = projectService.createToDo(owner, project);
		projectService.updateToDo(owner, toDo, "Follow up", ToDoStatus.open, null, null, null, null, null);
		projectService.updateTags(owner, toDo.getArtefact(), List.of("tag"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("projectKey", project.getKey()));
		Map<String,Object> structured = structured(result);
		List<Map<String,Object>> items = items(structured);
		Assert.assertEquals(McpToolSupport.isoDate(new Date()), structured.get("to"));
		Assert.assertEquals(McpToolSupport.isoDate(DateUtils.addDays(new Date(), -14)), structured.get("from"));
		Assert.assertEquals(Boolean.FALSE, structured.get("truncated"));
		List<String> actions = items.stream().map(item -> item.get("action").toString()).toList();
		assertThat(actions).contains("projectCreate", "projectMemberAdd", "noteCreate", "noteContentUpdate", "toDoCreate", "toDoContentUpdate");
		// the detail action of the tags is hidden as in the timeline
		assertThat(actions).doesNotContain("toDoTagsUpdate");
		// newest first
		for(int i=1; i<items.size(); i++) {
			assertThat(items.get(i - 1).get("date").toString()).isGreaterThanOrEqualTo(items.get(i).get("date").toString());
		}

		Map<String,Object> noteRow = items.stream().filter(item -> "noteContentUpdate".equals(item.get("action"))).findFirst().orElseThrow();
		Assert.assertEquals("note", noteRow.get("target"));
		Assert.assertEquals(username(participant), ((Map<?,?>)noteRow.get("doer")).get("username"));
		Map<?,?> noteRef = (Map<?,?>)noteRow.get("artefact");
		Assert.assertEquals("note", noteRef.get("type"));
		Assert.assertEquals(note.getKey(), noteRef.get("key"));
		Assert.assertEquals("Kickoff note", noteRef.get("title"));
		Assert.assertEquals("active", noteRef.get("status"));
		assertThat(noteRef.get("url").toString()).contains("/Note/" + note.getKey());

		Map<String,Object> toDoRow = items.stream().filter(item -> "toDoCreate".equals(item.get("action"))).findFirst().orElseThrow();
		Map<?,?> toDoRef = (Map<?,?>)toDoRow.get("artefact");
		Assert.assertEquals("toDo", toDoRef.get("type"));
		Assert.assertEquals(toDo.getToDoTask().getKey(), toDoRef.get("key"));

		Map<String,Object> memberRow = items.stream().filter(item -> "projectMemberAdd".equals(item.get("action"))).findFirst().orElseThrow();
		Assert.assertEquals(username(participant), ((Map<?,?>)memberRow.get("member")).get("username"));
		Assert.assertEquals(username(owner), ((Map<?,?>)memberRow.get("doer")).get("username"));
		assertThat(result.text()).contains("activities between").contains("noteContentUpdate by").contains("note \"Kickoff note\" (key " + note.getKey() + ")");
	}

	@Test
	public void list_filtersAndLimit() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tl-4");
		ProjProject project = createProject(owner);
		ProjNote note = projectService.createNote(owner, project);
		projectService.updateNote(owner, note, UUID.randomUUID().toString(), "Note", "text");
		projectService.updateTags(owner, note.getArtefact(), List.of("tag"));
		ProjToDo toDo = projectService.createToDo(owner, project);
		projectService.updateToDo(owner, toDo, "ToDo", ToDoStatus.open, null, null, null, null, null);
		projectService.createActivityRead(owner, project);
		projectService.createActivityRead(owner, note.getArtefact());
		projectService.createActivityDownload(owner, note.getArtefact());
		dbInstance.commitAndCloseSession();

		List<String> noteActions = items(structured(invoke(context(owner), Map.of("projectKey", project.getKey(), "types", List.of("note")))))
				.stream().map(item -> item.get("action").toString()).toList();
		assertThat(noteActions).containsExactly("noteContentUpdate", "noteCreate");

		List<String> allNoteActions = items(structured(invoke(context(owner), Map.of("projectKey", project.getKey(), "types", List.of("note"), "allActions", true))))
				.stream().map(item -> item.get("action").toString()).toList();
		assertThat(allNoteActions).contains("noteTagsUpdate", "noteContentUpdate", "noteCreate")
			.doesNotContain("noteRead", "noteDownload");

		// the read and download history is never returned, also not with allActions
		List<String> allActions = items(structured(invoke(context(owner), Map.of("projectKey", project.getKey(), "allActions", true))))
				.stream().map(item -> item.get("action").toString()).toList();
		assertThat(allActions).contains("projectCreate", "noteTagsUpdate")
			.doesNotContain("projectRead", "noteRead", "noteDownload");
		assertThat(TimelineQueryToolProvider.ALL_ACTIONS_WITHOUT_READS).doesNotContain(Action.projectRead,
				Action.fileRead, Action.fileDownload, Action.noteRead, Action.noteDownload);

		Map<String,Object> limited = structured(invoke(context(owner), Map.of("projectKey", project.getKey(), "limit", 2)));
		Assert.assertEquals(2, items(limited).size());
		Assert.assertEquals(Boolean.TRUE, limited.get("truncated"));

		// a window in the past is empty
		Map<String,Object> past = structured(invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "2020-01-01", "to", "2020-01-31")));
		Assert.assertTrue(items(past).isEmpty());
		Assert.assertEquals("2020-01-01", past.get("from"));

		McpToolResult badType = invoke(context(owner), Map.of("projectKey", project.getKey(), "types", List.of("whiteboard")));
		Assert.assertTrue(badType.error());
		assertThat(badType.text()).contains("Unknown type 'whiteboard'");
	}

	@Test
	public void list_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tl-5");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-tl-6");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("projectKey", project.getKey()));
			Assert.fail("A non-member cannot read the timeline");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the timeline", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private McpToolContext context(Identity identity) {
		return new McpToolContext(identity, securityManager.getRoles(identity), GRANTS, null, Locale.ENGLISH, "127.0.0.1");
	}

	private static String username(Identity identity) {
		return identity.getUser().getNickName();
	}

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
