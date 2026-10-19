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
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationService;
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
 * Integration tests of the tool note_query: the list of a project with its
 * filters, the full note with text, metadata and links, the rights.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class NoteQueryToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(NoteQueryToolProvider.NAME), null, List.of("test"));
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
	private NoteQueryToolProvider sut;

	@Test
	public void preview_sameAsRegex() {
		String longText = "Lorem ipsum dolor sit amet, ".repeat(20);
		List<String> texts = List.of("", " ", "\t\n\r ", "abc", "  abc  ", "a \t\n b\r\n\r\nc", "\u0001 a \u0001",
				"\u000B\fa\u000Bb\f", "a\u00A0\u00A0b", longText, "\n\n" + longText, longText + "\n\n   ",
				"x".repeat(199) + "   \n y", "x".repeat(199) + "   \n ", "x".repeat(199) + " \u0001", "x".repeat(199) + " \u0001z",
				"x".repeat(200) + "  ", "x".repeat(201), " x".repeat(150), "x\n".repeat(150));
		for(String text:texts) {
			String collapsed = text.replaceAll("\\s+", " ").trim();
			String expected = collapsed.length() <= ProjectToolSupport.PREVIEW_LENGTH ? collapsed : collapsed.substring(0, ProjectToolSupport.PREVIEW_LENGTH);
			Assert.assertEquals("Preview of: " + text, expected, ProjectToolSupport.preview(text));
		}
	}

	@Test
	public void definition() {
		Assert.assertEquals("note_query", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.READ, sut.getScope());
		assertThat(sut.getDefinition().description()).contains("project_query").contains("note_modify").contains("markdown");
		Assert.assertFalse(sut.getDefinition().destructive());
		Assert.assertTrue(sut.getDefinition().idempotent());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("projectKey", 12)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "list", "projectKey", 12, "status", "all", "createdByMe", true,
				"createdAfter", "2026-10-01", "limit", 200)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "get", "noteKey", 3)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "status", "archived")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "limit", 201)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "query", "x")).valid());
	}

	@Test
	public void list_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-1");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult noKey = invoke(context(owner), Map.of());
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("projectKey", Long.valueOf(-5))));
		Assert.assertEquals("project with key -5", unknownProject.getObjectDescription());
		Assert.assertEquals("view the notes", unknownProject.getPermission());

		McpToolResult badStatus = invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "archived"));
		Assert.assertTrue(badStatus.error());
		assertThat(badStatus.text()).contains("Unknown status 'archived'");

		McpToolResult badDate = invoke(context(owner), Map.of("projectKey", project.getKey(), "createdAfter", "yesterday"));
		Assert.assertTrue(badDate.error());
		assertThat(badDate.text()).contains("createdAfter");

		McpToolResult unknownAction = invoke(context(owner), Map.of("action", "search"));
		Assert.assertTrue(unknownAction.error());
		assertThat(unknownAction.text()).contains("Unknown action search");
	}

	@Test
	public void list_membersAndSorting() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-3");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote first = createNote(owner, project, "First", "# Heading\n\nSome text.");
		ProjNote second = createNote(participant, project, null, "Untitled note of the participant");
		projectService.updateTags(owner, first.getArtefact(), List.of("meeting", "alpha"));
		// the content change moves the first note to the top of the list
		projectService.updateNote(owner, first, UUID.randomUUID().toString(), "First", "# Heading\n\nSome **markdown** text.");
		dbInstance.commit();
		
		// MySQL doesn't save milliseconds, we need a bigger time difference to make the order works
		dbInstance.getCurrentEntityManager()
	        .createQuery("update projartefact set contentModifiedDate=:date where key=:key")
	        .setParameter("date", DateUtils.addMinutes(new Date(), -1))
	        .setParameter("key", second.getArtefact().getKey())
	        .executeUpdate();
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("projectKey", project.getKey()));
		Map<String,Object> structured = structured(result);
		List<Map<String,Object>> items = items(structured);
		Assert.assertEquals(Integer.valueOf(2), structured.get("total"));
		Assert.assertEquals(Boolean.FALSE, structured.get("truncated"));
		// last modified first
		Assert.assertEquals(first.getKey(), items.get(0).get("key"));
		Assert.assertEquals(second.getKey(), items.get(1).get("key"));

		Map<String,Object> firstItem = items.get(0);
		Assert.assertEquals("First", firstItem.get("title"));
		Assert.assertEquals("markdown", firstItem.get("format"));
		Assert.assertEquals("active", firstItem.get("status"));
		Assert.assertEquals(project.getKey(), firstItem.get("projectKey"));
		Assert.assertEquals("# Heading Some **markdown** text.", firstItem.get("preview"));
		Assert.assertEquals(Integer.valueOf(34), firstItem.get("textLength"));
		Assert.assertFalse(firstItem.containsKey("text"));
		Assert.assertFalse(firstItem.containsKey("links"));
		Assert.assertEquals(List.of("alpha", "meeting"), firstItem.get("tags"));
		assertThat(persons(firstItem, "members")).containsExactly(username(owner));
		Assert.assertEquals(username(owner), ((Map<?,?>)firstItem.get("creator")).get("username"));
		assertThat(firstItem.get("url").toString()).contains("/Project/" + project.getKey()).contains("/Note/" + first.getKey());

		Map<String,Object> secondItem = items.get(1);
		Assert.assertFalse(secondItem.containsKey("title"));
		assertThat(result.text()).contains("2 notes").contains("First (key " + first.getKey()).contains("(no title) (key " + second.getKey());
	}

	@Test
	public void list_filters() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-5");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote mine = createNote(owner, project, "Mine", "text");
		ProjNote theirs = createNote(participant, project, "Theirs", "text");
		ProjNote deleted = createNote(owner, project, "Deleted", "text");
		projectService.deleteNoteSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey()))))
				.containsExactlyInAnyOrder(mine.getKey(), theirs.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "deleted"))))
				.containsExactly(deleted.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "all"))))
				.containsExactlyInAnyOrder(mine.getKey(), theirs.getKey(), deleted.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "createdByMe", true))))
				.containsExactly(mine.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "createdAfter",
				McpToolSupport.isoDate(DateUtils.addDays(new Date(), 1)))))).isEmpty();
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "createdAfter",
				McpToolSupport.isoDate(DateUtils.addDays(new Date(), -1))))))
				.containsExactlyInAnyOrder(mine.getKey(), theirs.getKey());
	}

	@Test
	public void list_limitAndTruncated() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-6");
		ProjProject project = createProject(owner);
		createNote(owner, project, "A", "a");
		createNote(owner, project, "B", "b");
		createNote(owner, project, "C", "c");
		dbInstance.commitAndCloseSession();

		Map<String,Object> structured = structured(invoke(context(owner), Map.of("projectKey", project.getKey(), "limit", 2)));
		Assert.assertEquals(2, items(structured).size());
		Assert.assertEquals(Integer.valueOf(3), structured.get("total"));
		Assert.assertEquals(Boolean.TRUE, structured.get("truncated"));
	}

	@Test
	public void list_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-7");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-8");
		ProjProject project = createProject(owner);
		createNote(owner, project, "Secret", "text");
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("projectKey", project.getKey()));
			Assert.fail("A non-member cannot list the notes");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the notes", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void list_organisationManagerWithoutRole_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-9");
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-nq-10");
		ProjProject project = createProject(owner);
		projectService.updateProjectOrganisations(owner, project, List.of(organisationService.getDefaultOrganisation()));
		createNote(owner, project, "Not visible to the admin", "text");
		dbInstance.commitAndCloseSession();

		// As in the UI: the manager of the organisation opens the project metadata but not the artefacts of a normal project
		try {
			invoke(context(admin), Map.of("projectKey", project.getKey()));
			Assert.fail("The organisation manager without project role cannot list the notes");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the notes", e.getPermission());
		}
	}

	@Test
	public void get_withTextAndLinks() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-11");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-12");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		String text = "# Kickoff\n\n- decision one\n- decision two\n";
		ProjNote note = createNote(owner, project, "Kickoff", text);
		ProjNote other = createNote(owner, project, "Other", "other");
		ProjToDo toDo = projectService.createToDo(owner, project);
		projectService.updateToDo(owner, toDo, "Follow up", ToDoStatus.open, null, null, null, null, null);
		ProjNote deletedLinked = createNote(owner, project, "Deleted linked", "x");
		projectService.linkArtefacts(owner, note.getArtefact(), other.getArtefact());
		projectService.linkArtefacts(owner, note.getArtefact(), toDo.getArtefact());
		projectService.linkArtefacts(owner, note.getArtefact(), deletedLinked.getArtefact());
		projectService.deleteNoteSoftly(owner, deletedLinked);
		projectService.updateMembers(owner, new ProjectBCFactory(), note.getArtefact(), List.<IdentityRef>of(owner, participant));
		projectService.updateTags(owner, note.getArtefact(), List.of("kickoff"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("action", "get", "noteKey", note.getKey()));
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals(note.getKey(), item.get("key"));
		Assert.assertEquals("Kickoff", item.get("title"));
		Assert.assertEquals(text, item.get("text"));
		Assert.assertEquals("markdown", item.get("format"));
		Assert.assertFalse(item.containsKey("preview"));
		Assert.assertEquals(List.of("kickoff"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));

		@SuppressWarnings("unchecked")
		List<Map<String,Object>> links = (List<Map<String,Object>>)item.get("links");
		// the deleted linked note is not listed
		Assert.assertEquals(2, links.size());
		Assert.assertEquals("toDo", links.get(0).get("type"));
		Assert.assertEquals(toDo.getToDoTask().getKey(), links.get(0).get("key"));
		Assert.assertEquals("Follow up", links.get(0).get("title"));
		assertThat(links.get(0).get("url").toString()).contains("/ToDo/" + toDo.getKey());
		Assert.assertEquals("note", links.get(1).get("type"));
		Assert.assertEquals(other.getKey(), links.get(1).get("key"));
		Assert.assertEquals("Other", links.get(1).get("title"));
		assertThat(links.get(1).get("url").toString()).contains("/Note/" + other.getKey());
		assertThat(result.text()).contains("Note Kickoff (key " + note.getKey()).contains("# Kickoff");
	}

	@Test
	public void get_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-13");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-14");
		ProjProject project = createProject(owner);
		ProjNote note = createNote(owner, project, "Secret", "text");
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "get", "noteKey", note.getKey()));
			Assert.fail("A non-member cannot read the note");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the note", e.getPermission());
			Assert.assertEquals("note with key " + note.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nq-15");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "get"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("noteKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknown = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "get", "noteKey", Long.valueOf(-7))));
		Assert.assertEquals("note with key -7", unknown.getObjectDescription());
		Assert.assertEquals("view the note", unknown.getPermission());
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private ProjNote createNote(Identity doer, ProjProject project, String title, String text) {
		ProjNote note = projectService.createNote(doer, project);
		projectService.updateNote(doer, note, UUID.randomUUID().toString(), title, text);
		return note;
	}

	private McpToolContext context(Identity identity) {
		return new McpToolContext(identity, securityManager.getRoles(identity), GRANTS, null, Locale.ENGLISH, "127.0.0.1");
	}

	private static String username(Identity identity) {
		return identity.getUser().getNickName();
	}

	@SuppressWarnings("unchecked")
	private static List<String> persons(Map<String,Object> item, String name) {
		return ((List<Map<String,Object>>)item.get(name)).stream().map(person -> person.get("username").toString()).toList();
	}

	private List<Long> keys(McpToolResult result) {
		return items(structured(result)).stream().map(item -> (Long)item.get("key")).toList();
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
