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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.coordinate.Locker;
import org.olat.core.util.resource.OresHelper;
import org.olat.mcp.McpGrants;
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
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool note_modify: the rights per action and
 * project role, the service sequences, the metadata round trips and the
 * side effects (activity log, publisher news).
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class NoteModifyToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(NoteModifyToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private NoteModifyToolProvider sut;
	@Autowired
	private ProjectToolSupport support;
	@Autowired
	private UserManager userManager;

	@Test
	public void definition() {
		Assert.assertEquals("note_modify", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.WRITE, sut.getScope());
		Assert.assertTrue(sut.getDefinition().destructive());
		Assert.assertFalse(sut.getDefinition().idempotent());
		assertThat(sut.getDefinition().description()).contains("note_query").contains("project_query").contains("markdown")
			// V17: milestones are no link targets
			.contains("milestones cannot be linked").doesNotContain("appointments, milestones, decisions");
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "create", "projectKey", 3, "title", "T", "text", "# md",
				"tags", List.of("a"), "members", List.of("jdoe"), "links", List.of(Map.of("type", "toDo", "key", 5)))).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "update", "noteKey", 1, "title", "", "links", List.of())).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "delete", "noteKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("noteKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "archive", "noteKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "noteKey", 1, "links", List.of(Map.of("type", "course", "key", 5)))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "noteKey", 1, "links", List.of(Map.of("key", 5)))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "noteKey", 1, "status", "deleted")).valid());
	}

	@Test
	public void invalidActions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-1");

		McpToolResult missing = invoke(context(owner), Map.of());
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("action is required");

		McpToolResult unknown = invoke(context(owner), Map.of("action", "archive"));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).contains("Unknown action archive");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "update", "title", "x"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("noteKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownKey = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "delete", "noteKey", Long.valueOf(-9))));
		Assert.assertEquals("note with key -9", unknownKey.getObjectDescription());
		Assert.assertEquals("view the note", unknownKey.getPermission());

		McpToolResult noProject = invoke(context(owner), Map.of("action", "create", "title", "x"));
		Assert.assertTrue(noProject.error());
		assertThat(noProject.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "create", "projectKey", Long.valueOf(-9), "title", "x")));
		Assert.assertEquals("project with key -9", unknownProject.getObjectDescription());
		Assert.assertEquals("create notes", unknownProject.getPermission());
	}

	// create

	@Test
	public void create_minimal() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-2");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		McpToolResult result = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "text", "# Only text"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjNote note = projectService.getNote(() -> (Long)item.get("key"));
		Assert.assertNull(note.getTitle());
		Assert.assertEquals("# Only text", note.getText());
		Assert.assertEquals(project.getKey(), note.getArtefact().getProject().getKey());
		Assert.assertEquals(owner, note.getArtefact().getCreator());
		Assert.assertFalse(item.containsKey("title"));
		Assert.assertEquals("# Only text", item.get("text"));
		Assert.assertEquals(List.of(), item.get("tags"));
		Assert.assertEquals(List.of(), item.get("links"));
		// the creator is the involved member by default, as the UI pre-selects
		assertThat(persons(item, "members")).containsExactly(username(owner));
		assertThat(result.text()).startsWith("Created: (no title) (key " + note.getKey());
		assertThat(actions(project, owner)).contains(Action.noteCreate, Action.noteContentUpdate);
		// one create and one content update, the sequence of the UI (create, then save the editor)
		Assert.assertEquals(1, count(note.getArtefact(), Action.noteCreate));
		Assert.assertEquals(1, count(note.getArtefact(), Action.noteContentUpdate));
		assertNewsAfter(project, before);
	}

	@Test
	public void create_todoLinkByUrlKey() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-30");
		ProjProject project = createProject(owner);
		ProjToDo toDo = createToDo(owner, project, "Follow up");
		ProjProject foreignProject = createProject(owner);
		ProjToDo foreignToDo = createToDo(owner, foreignProject, "Foreign");
		dbInstance.commitAndCloseSession();

		// the key of the project ToDo (the number in its URL) resolves to the same ToDo
		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"title", "Linked by URL key", "links", List.of(Map.of("type", "toDo", "key", toDo.getKey())))));
		dbInstance.commitAndCloseSession();
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey());

		// the fallback stays in the project
		McpInvalidArgumentException e = Assert.assertThrows(McpInvalidArgumentException.class, () -> sut.execute(context(owner), Map.of("action", "create",
				"projectKey", project.getKey(), "title", "Foreign link", "links", List.of(Map.of("type", "toDo", "key", foreignToDo.getKey())))));
		assertThat(e.getMessage()).contains("toDo " + foreignToDo.getKey());
	}

	@Test
	public void create_withMetadataAndLinks() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-3");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-4");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote other = createNote(owner, project, "Other", "x");
		ProjToDo toDo = createToDo(owner, project, "Follow up");
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("projectKey", project.getKey());
		arguments.put("title", "Kickoff");
		arguments.put("text", "# Kickoff\n\nNotes.");
		arguments.put("tags", List.of("meeting", "kickoff"));
		arguments.put("members", List.of(username(owner), participant.getUser().getEmail()));
		arguments.put("links", List.of(Map.of("type", "note", "key", other.getKey()), Map.of("type", "toDo", "key", toDo.getToDoTask().getKey())));
		McpToolResult result = invoke(context(participant), arguments);
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		Long noteKey = (Long)item.get("key");
		ProjNote note = projectService.getNote(() -> noteKey);
		Assert.assertEquals("Kickoff", note.getTitle());
		Assert.assertEquals("# Kickoff\n\nNotes.", note.getText());
		Assert.assertEquals(participant, note.getArtefact().getCreator());
		Assert.assertEquals(List.of("kickoff", "meeting"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey(), "note:" + other.getKey());
		assertThat(projectService.getLinkedArtefacts(note.getArtefact())).extracting(ProjArtefact::getKey)
				.containsExactlyInAnyOrder(other.getArtefact().getKey(), toDo.getArtefact().getKey());
		assertThat(actions(project, participant)).contains(Action.noteCreate, Action.noteContentUpdate, Action.noteTagsUpdate,
				Action.noteMemberAdd, Action.noteReferenceAdd);
		assertThat(result.text()).startsWith("Created: Kickoff (key " + noteKey).contains("tags kickoff, meeting");
	}

	@Test
	public void create_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-5");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-6");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-7");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		dbInstance.commitAndCloseSession();

		// a client may view but not create artefacts
		try {
			invoke(context(client), Map.of("action", "create", "projectKey", project.getKey(), "title", "x"));
			Assert.fail("A client cannot create notes");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create notes", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
		try {
			invoke(context(stranger), Map.of("action", "create", "projectKey", project.getKey(), "title", "x"));
			Assert.fail("A non-member cannot create notes");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create notes", e.getPermission());
		}

		// a deleted project is read only: a state error, not a permission error
		projectService.setStatusDeleted(owner, new ProjectBCFactory(), project);
		dbInstance.commitAndCloseSession();
		McpToolResult deletedProject = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x"));
		Assert.assertTrue(deletedProject.error());
		assertThat(deletedProject.text()).startsWith("The project " + project.getKey() + " is deleted");
		// a stranger gets the permission error, the state is not revealed
		try {
			invoke(context(stranger), Map.of("action", "create", "projectKey", project.getKey(), "title", "x"));
			Assert.fail("A non-member cannot create notes");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create notes", e.getPermission());
		}
	}

	@Test
	public void create_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-8");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-9");
		ProjProject project = createProject(owner);
		ProjProject otherProject = createProject(stranger);
		ProjNote foreignNote = createNote(stranger, otherProject, "Foreign", "x");
		ProjToDo foreignToDo = createToDo(stranger, otherProject, "Foreign ToDo");
		ProjNote deletedNote = createNote(owner, project, "Deleted", "x");
		projectService.deleteNoteSoftly(owner, deletedNote);
		dbInstance.commitAndCloseSession();
		long notesBefore = countNotes(project);

		McpToolResult empty = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey()));
		Assert.assertTrue(empty.error());
		assertThat(empty.text()).contains("title or a text");

		McpToolResult longTitle = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x".repeat(129)));
		Assert.assertTrue(longTitle.error());
		assertThat(longTitle.text()).contains("title").contains("128");

		McpToolResult notMember = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"members", List.of(username(stranger))));
		Assert.assertTrue(notMember.error());
		Assert.assertEquals("Invalid argument: " + username(stranger) + " is not a valid member for this note.", notMember.text());

		// a link to an artefact of another project, to a deleted note or to an unknown key gives the same message
		String uniform = "is not an active note of this project.";
		McpToolResult foreign = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "note", "key", foreignNote.getKey()))));
		Assert.assertTrue(foreign.error());
		assertThat(foreign.text()).endsWith(uniform);
		McpToolResult deleted = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "note", "key", deletedNote.getKey()))));
		Assert.assertTrue(deleted.error());
		assertThat(deleted.text()).endsWith(uniform);
		McpToolResult unknown = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "note", "key", -1))));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).endsWith(uniform);
		McpToolResult foreignTask = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "toDo", "key", foreignToDo.getToDoTask().getKey()))));
		Assert.assertTrue(foreignTask.error());
		assertThat(foreignTask.text()).endsWith("is not an active toDo of this project.");
		McpToolResult badLink = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "note"))));
		Assert.assertTrue(badLink.error());
		assertThat(badLink.text()).contains("type").contains("key");

		// nothing was written
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(notesBefore, countNotes(project));
	}

	// update

	@Test
	public void update_onlySuppliedFields() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-10");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-11");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote note = createNote(owner, project, "Title", "Text");
		ProjNote linked = createNote(owner, project, "Linked", "x");
		projectService.updateTags(owner, note.getArtefact(), List.of("keep"));
		projectService.linkArtefacts(owner, note.getArtefact(), linked.getArtefact());
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		// a participant who is not the creator may edit
		McpToolResult result = invoke(context(participant), Map.of("action", "update", "noteKey", note.getKey(), "text", "Text v2"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjNote reloaded = projectService.getNote(note);
		Assert.assertEquals("Title", reloaded.getTitle());
		Assert.assertEquals("Text v2", reloaded.getText());
		Assert.assertEquals(List.of("keep"), item.get("tags"));
		// the service adds the editor of the content as an involved member, as the UI does
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));
		assertThat(links(item)).containsExactly("note:" + linked.getKey());
		assertThat(result.text()).startsWith("Updated: Title (key " + note.getKey());
		assertThat(actions(project, participant)).contains(Action.noteContentUpdate);
		assertNewsAfter(project, before);

		// an unchanged content writes no activity
		List<Action> actionsBefore = actions(project, participant);
		invoke(context(participant), Map.of("action", "update", "noteKey", note.getKey(), "text", "Text v2"));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(actionsBefore.size(), actions(project, participant).size());
	}

	@Test
	public void update_replaceAndClearMetadata() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-12");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-13");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote note = createNote(owner, project, "Title", "Text");
		ProjNote oldLink = createNote(owner, project, "Old link", "x");
		ProjNote newLink = createNote(owner, project, "New link", "x");
		ProjToDo toDo = createToDo(owner, project, "ToDo");
		projectService.updateTags(owner, note.getArtefact(), List.of("old"));
		projectService.linkArtefacts(owner, note.getArtefact(), oldLink.getArtefact());
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "update");
		arguments.put("noteKey", note.getKey());
		arguments.put("title", "");
		arguments.put("tags", List.of("new"));
		arguments.put("members", List.of(username(participant)));
		arguments.put("links", List.of(Map.of("type", "note", "key", newLink.getKey()), Map.of("type", "toDo", "key", toDo.getToDoTask().getKey())));
		Map<String,Object> item = item(invoke(context(owner), arguments));
		dbInstance.commitAndCloseSession();

		ProjNote reloaded = projectService.getNote(note);
		Assert.assertNull(reloaded.getTitle());
		Assert.assertEquals("Text", reloaded.getText());
		Assert.assertFalse(item.containsKey("title"));
		Assert.assertEquals(List.of("new"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactly(username(participant));
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey(), "note:" + newLink.getKey());
		assertThat(projectService.getLinkedArtefacts(note.getArtefact())).extracting(ProjArtefact::getKey)
				.containsExactlyInAnyOrder(newLink.getArtefact().getKey(), toDo.getArtefact().getKey());
		assertThat(actions(project, owner)).contains(Action.noteTagsUpdate, Action.noteMemberAdd, Action.noteMemberRemove,
				Action.noteReferenceAdd, Action.noteReferenceRemove);

		// empty lists clear tags and links, the members stay
		Map<String,Object> cleared = item(invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(),
				"tags", List.of(), "links", List.of())));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(List.of(), cleared.get("tags"));
		assertThat(persons(cleared, "members")).containsExactly(username(participant));
		Assert.assertEquals(List.of(), cleared.get("links"));
		assertThat(projectService.getLinkedArtefacts(note.getArtefact())).isEmpty();

		// an empty member list is refused, the UI requires at least one involved member
		McpToolResult noMembers = invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(), "members", List.of()));
		Assert.assertTrue(noMembers.error());
		assertThat(noMembers.text()).contains("at least one project member");
		Assert.assertFalse(VALIDATOR.validate(sut.getDefinition().inputSchema(), Map.of("action", "update", "noteKey", 1, "members", List.of())).valid());
		McpToolResult createNoMembers = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "No members", "members", List.of()));
		Assert.assertTrue(createNoMembers.error());
		assertThat(createNoMembers.text()).contains("at least one project member");

		// clearing title and text together is refused
		McpToolResult empty = invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(), "text", ""));
		Assert.assertTrue(empty.error());
		assertThat(empty.text()).contains("title or a text");
		Assert.assertEquals("Text", projectService.getNote(note).getText());
	}

	@Test
	public void update_selfLinkRefused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-14");
		ProjProject project = createProject(owner);
		ProjNote note = createNote(owner, project, "Title", "Text");
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(),
				"links", List.of(Map.of("type", "note", "key", note.getKey()))));
		Assert.assertTrue(result.error());
		assertThat(result.text()).endsWith("is not an active note of this project.");
	}

	@Test
	public void update_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-15");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-16");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-17");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		ProjNote note = createNote(owner, project, "Title", "Text");
		ProjNote deleted = createNote(owner, project, "Deleted", "Text");
		projectService.deleteNoteSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "update", "noteKey", note.getKey(), "title", "x"));
			Assert.fail("A non-member cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the note", e.getPermission());
		}
		try {
			invoke(context(client), Map.of("action", "update", "noteKey", note.getKey(), "title", "x"));
			Assert.fail("A client cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("edit the note", e.getPermission());
			Assert.assertEquals("note with key " + note.getKey(), e.getObjectDescription());
		}
		// a deleted note: a state error, not a permission error
		McpToolResult deletedNote = invoke(context(owner), Map.of("action", "update", "noteKey", deleted.getKey(), "title", "x"));
		Assert.assertTrue(deletedNote.error());
		Assert.assertEquals("The note " + deleted.getKey() + " is already deleted.", deletedNote.text());
		Assert.assertEquals("Title", projectService.getNote(note).getTitle());
	}

	// delete

	@Test
	public void delete_creatorAndAdminRoles() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-18");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-19");
		Identity otherParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-20");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		projectService.updateMember(owner, new ProjectBCFactory(), project, otherParticipant, Set.of(ProjectRole.participant), null);
		ProjNote byParticipant = createNote(participant, project, "By participant", "x");
		ProjNote byOwner = createNote(owner, project, "By owner", "x");
		dbInstance.commitAndCloseSession();

		// a participant who is not the creator cannot delete
		try {
			invoke(context(otherParticipant), Map.of("action", "delete", "noteKey", byParticipant.getKey()));
			Assert.fail("A participant cannot delete the note of another creator");
		} catch (McpPermissionException e) {
			Assert.assertEquals("delete the note", e.getPermission());
		}

		Date before = setPublisherNewsInPast(project);
		// the creator deletes the own note
		Map<String,Object> item = item(invoke(context(participant), Map.of("action", "delete", "noteKey", byParticipant.getKey())));
		Assert.assertEquals("deleted", item.get("status"));
		// the owner deletes any note
		invoke(context(owner), Map.of("action", "delete", "noteKey", byOwner.getKey()));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(ProjectStatus.deleted, projectService.getNote(byParticipant).getArtefact().getStatus());
		Assert.assertEquals(ProjectStatus.deleted, projectService.getNote(byOwner).getArtefact().getStatus());
		assertThat(actions(project, participant)).contains(Action.noteStatusDelete);
		assertNewsAfter(project, before);

		// deleting twice is refused with a state error
		McpToolResult twice = invoke(context(owner), Map.of("action", "delete", "noteKey", byOwner.getKey()));
		Assert.assertTrue(twice.error());
		Assert.assertEquals("The note " + byOwner.getKey() + " is already deleted.", twice.text());
	}

	/**
	 * V2: the edit lock of the note editor. While another user edits the
	 * note in OpenOlat, update is refused and nothing changes; afterwards the
	 * update works and leaves no lock behind.
	 */
	@Test
	public void update_lockedNote_refused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-40");
		Identity editor = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-41");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, editor, Set.of(ProjectRole.participant), null);
		ProjNote note = createNote(owner, project, "Title", "Text");
		dbInstance.commitAndCloseSession();

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ProjNote.class, note.getKey());
		Locker locker = CoordinatorManager.getInstance().getCoordinator().getLocker();
		LockResult editorLock = locker.acquireLock(ores, editor, "", null);
		Assert.assertTrue(editorLock.isSuccess());
		try {
			McpToolResult locked = invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(), "text", "By MCP", "tags", List.of("x")));
			Assert.assertTrue(locked.error());
			Assert.assertEquals("The note is being edited by " + userManager.getUserDisplayName(editor) + ". Try again later.", locked.text());
			dbInstance.commitAndCloseSession();
			Assert.assertEquals("Text", projectService.getNote(note).getText());
			Assert.assertEquals(editor.getKey(), locker.getLockEntry(ores, "").getOwner().getKey());
		} finally {
			locker.releaseLock(editorLock);
			dbInstance.commitAndCloseSession();
		}

		item(invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(), "text", "By MCP")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("By MCP", projectService.getNote(note).getText());
		// the lock of the tool is released with the change
		Assert.assertNull(locker.getLockEntry(ores, ""));
		Assert.assertFalse(locker.isLocked(ores, ""));
	}

	/**
	 * F4: the tool commits the changes, then releases the lock in its own
	 * transaction. A rollback after the tool returns (a failed commit of the
	 * registry) neither loses the change nor restores the lock row.
	 */
	@Test
	public void update_lockReleased_rollbackAfterReturn() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-45");
		ProjProject project = createProject(owner);
		ProjNote note = createNote(owner, project, "Title", "Text");
		dbInstance.commitAndCloseSession();

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ProjNote.class, note.getKey());
		Locker locker = CoordinatorManager.getInstance().getCoordinator().getLocker();
		item(invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(), "text", "Committed")));
		dbInstance.rollbackAndCloseSession();

		Assert.assertEquals("Committed", projectService.getNote(note).getText());
		Assert.assertNull(locker.getLockEntry(ores, ""));
		Assert.assertFalse(locker.isLocked(ores, ""));
		// a UI editor gets the lock
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-46");
		LockResult lock = locker.acquireLock(ores, other, "", null);
		Assert.assertTrue(lock.isSuccess());
		locker.releaseLock(lock);
		dbInstance.commitAndCloseSession();
	}

	/**
	 * V16: the markdown text is stored as passed, the title is trimmed.
	 */
	@Test
	public void create_update_textNotTrimmed() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-42");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		String text = "    indented code\n\nLast line with a hard break  \n";
		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"title", "  Code  ", "text", text)));
		dbInstance.commitAndCloseSession();
		ProjNote note = projectService.getNote(() -> (Long)item.get("key"));
		Assert.assertEquals(text, note.getText());
		Assert.assertEquals("Code", note.getTitle());

		String updated = "\n  - list item\n";
		item(invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(), "text", updated)));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(updated, projectService.getNote(note).getText());

		// a text of white space only clears the text
		item(invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(), "text", "  \n ")));
		dbInstance.commitAndCloseSession();
		Assert.assertNull(projectService.getNote(note).getText());
	}

	/**
	 * V3: tags that differ only in case are one tag, and an existing tag of
	 * the project is reused with its spelling, as the tag selection of the UI.
	 */
	@Test
	public void tags_deduplicatedAndMatched() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-43");
		ProjProject project = createProject(owner);
		ProjNote tagged = createNote(owner, project, "Tagged", "x");
		projectService.updateTags(owner, tagged.getArtefact(), List.of("Review"));
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "Tags",
				"tags", List.of("review", "REVIEW", "new", "New", "new"))));
		dbInstance.commitAndCloseSession();
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>)item.get("tags");
		assertThat(tags).containsExactlyInAnyOrder("Review", "new");
		// one tag row per tag on the note, no parallel tag "review" in the project
		ProjNote note = projectService.getNote(() -> (Long)item.get("key"));
		assertThat(projectService.getTagInfos(project, note.getArtefact())).extracting(TagInfo::getDisplayName)
				.containsExactlyInAnyOrder("Review", "new");
		assertThat(projectService.getTagInfos(project, null)).filteredOn(info -> "review".equalsIgnoreCase(info.getDisplayName()))
				.hasSize(1).first().satisfies(info -> Assert.assertEquals(Long.valueOf(2), info.getCount()));

		// update matches too
		item(invoke(context(owner), Map.of("action", "update", "noteKey", tagged.getKey(), "tags", List.of("NEW", "review"))));
		dbInstance.commitAndCloseSession();
		assertThat(projectService.getTagInfos(project, tagged.getArtefact())).extracting(TagInfo::getDisplayName)
				.containsExactlyInAnyOrder("Review", "new");
	}

	/**
	 * V10: the involved members of an artefact of a template are read only.
	 */
	@Test
	public void template_membersRefused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-44");
		ProjProject template = createProject(owner);
		template = projectService.updateProject(owner, new ProjectBCFactory(), template, null, template.getTitle(), null, null, true, false);
		ProjNote note = createNote(owner, template, "In template", "x");
		dbInstance.commitAndCloseSession();

		McpToolResult create = invoke(context(owner), Map.of("action", "create", "projectKey", template.getKey(), "title", "x",
				"members", List.of(username(owner))));
		Assert.assertTrue(create.error());
		assertThat(create.text()).contains("template project have no involved members");
		McpToolResult update = invoke(context(owner), Map.of("action", "update", "noteKey", note.getKey(), "members", List.of(username(owner))));
		Assert.assertTrue(update.error());
		assertThat(update.text()).contains("template project have no involved members");
		// without members the note is created
		item(invoke(context(owner), Map.of("action", "create", "projectKey", template.getKey(), "title", "Without members")));
	}

	@Test
	public void delete_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-21");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-22");
		ProjProject project = createProject(owner);
		ProjNote note = createNote(owner, project, "Title", "x");
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "delete", "noteKey", note.getKey()));
			Assert.fail("A non-member cannot delete");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the note", e.getPermission());
		}
		Assert.assertEquals(ProjectStatus.active, projectService.getNote(note).getArtefact().getStatus());
	}

	@Test
	public void boundedInputs() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-nm-23");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult text = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"text", "x".repeat(NoteModifyToolProvider.TEXT_MAX_LENGTH + 1)));
		Assert.assertTrue(text.error());
		assertThat(text.text()).contains("text").contains(String.valueOf(NoteModifyToolProvider.TEXT_MAX_LENGTH));

		McpToolResult tags = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"tags", List.of("y".repeat(ProjectToolSupport.TAG_MAX_LENGTH + 1))));
		Assert.assertTrue(tags.error());
		assertThat(tags.text()).contains("tags");

		List<Map<String,Object>> tooManyLinks = java.util.stream.IntStream.range(0, ProjectToolSupport.LINKS_MAX_ITEMS + 1)
				.mapToObj(i -> Map.<String,Object>of("type", "note", "key", i)).toList();
		McpToolResult links = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", tooManyLinks));
		Assert.assertTrue(links.error());
		assertThat(links.text()).contains("links").contains(String.valueOf(ProjectToolSupport.LINKS_MAX_ITEMS));
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

	private ProjToDo createToDo(Identity doer, ProjProject project, String title) {
		ProjToDo toDo = projectService.createToDo(doer, project);
		projectService.updateToDo(doer, toDo, title, ToDoStatus.open, null, null, null, null, null);
		projectService.updateMembers(doer, toDo, List.<IdentityRef>of(doer), List.of());
		return toDo;
	}

	private long countNotes(ProjProject project) {
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setProject(project);
		return projectService.getNotesCount(searchParams);
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

	@SuppressWarnings("unchecked")
	private static List<String> links(Map<String,Object> item) {
		return ((List<Map<String,Object>>)item.get("links")).stream().map(link -> link.get("type") + ":" + link.get("key")).toList();
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
		assertThat(NoteModifyToolProvider.RESULT_FIELDS).containsAll(minimal.keySet());
		assertThat(result.text()).endsWith("Call note_query get for the full item.");
		Long key = (Long)minimal.get("key");
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setNotes(List.of(() -> key));
		return support.noteItem(projectService.getNoteInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0), true);
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
