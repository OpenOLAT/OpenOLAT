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

import java.time.LocalDateTime;
import java.time.ZoneId;
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
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool decision_modify: the rights per action and
 * project role, the service sequences, the metadata round trips and the
 * side effects (activity log, publisher news).
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class DecisionModifyToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(DecisionModifyToolProvider.NAME), null, List.of("test"));
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
	private DecisionModifyToolProvider sut;
	@Autowired
	private ProjectToolSupport support;

	@Test
	public void definition() {
		Assert.assertEquals("decision_modify", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.WRITE, sut.getScope());
		Assert.assertTrue(sut.getDefinition().destructive());
		Assert.assertFalse(sut.getDefinition().idempotent());
		assertThat(sut.getDefinition().description()).contains("decision_query").contains("project_query").contains("replace").contains("clears")
			.contains("milestones cannot be linked").doesNotContain("appointments, milestones, decisions");
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "create", "projectKey", 3, "title", "T", "details", "Line 1\nLine 2",
				"decisionDate", "2026-10-01", "tags", List.of("a"), "members", List.of("jdoe"), "links", List.of(Map.of("type", "toDo", "key", 5)))).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "update", "decisionKey", 1, "details", "", "decisionDate", "", "links", List.of())).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "delete", "decisionKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("decisionKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "archive", "decisionKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "decisionKey", 1, "links", List.of(Map.of("type", "course", "key", 5)))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "decisionKey", 1, "links", List.of(Map.of("key", 5)))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "decisionKey", 1, "status", "deleted")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "create", "projectKey", 1, "title", "x".repeat(DecisionModifyToolProvider.TITLE_MAX_LENGTH + 1))).valid());
	}

	@Test
	public void invalidActions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-101");

		McpToolResult missing = invoke(context(owner), Map.of());
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("action is required");

		McpToolResult unknown = invoke(context(owner), Map.of("action", "archive"));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).contains("Unknown action archive");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "update", "title", "x"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("decisionKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownKey = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "delete", "decisionKey", Long.valueOf(-9))));
		Assert.assertEquals("decision with key -9", unknownKey.getObjectDescription());
		Assert.assertEquals("view the decision", unknownKey.getPermission());

		McpToolResult noProject = invoke(context(owner), Map.of("action", "create", "title", "x"));
		Assert.assertTrue(noProject.error());
		assertThat(noProject.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "create", "projectKey", Long.valueOf(-9), "title", "x")));
		Assert.assertEquals("project with key -9", unknownProject.getObjectDescription());
		Assert.assertEquals("create decisions", unknownProject.getPermission());
	}

	// create

	@Test
	public void create_minimal() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-102");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		McpToolResult result = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "Go live in May"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjDecision decision = projectService.getDecision(() -> (Long)item.get("key"));
		Assert.assertEquals("Go live in May", decision.getTitle());
		Assert.assertNull(decision.getDetails());
		Assert.assertEquals(project.getKey(), decision.getArtefact().getProject().getKey());
		Assert.assertEquals(owner, decision.getArtefact().getCreator());
		// the decision date defaults to now, as the UI preselects
		Assert.assertNotNull(decision.getDecisionDate());
		Assert.assertEquals(McpToolSupport.iso(decision.getDecisionDate()), item.get("decisionDate"));
		Assert.assertFalse(item.containsKey("details"));
		Assert.assertEquals(List.of(), item.get("tags"));
		Assert.assertEquals(List.of(), item.get("links"));
		// the creator is the involved member by default
		assertThat(persons(item, "members")).containsExactly(username(owner));
		assertThat(result.text()).startsWith("Created: Go live in May (key " + decision.getKey());
		assertThat(actions(project, owner)).contains(Action.decisionCreate, Action.decisionContentUpdate);
		assertNewsAfter(project, before);
		// one create and one content update, the sequence of the UI (create, then save the form)
		Assert.assertEquals(1, count(decision.getArtefact(), Action.decisionCreate));
		Assert.assertEquals(1, count(decision.getArtefact(), Action.decisionContentUpdate));
		// the same values again, the stored date is a Timestamp: no further activity
		projectService.updateDecision(owner, decision, "Go live in May", null, decision.getDecisionDate());
		dbInstance.commitAndCloseSession();
		invoke(context(owner), Map.of("action", "update", "decisionKey", decision.getKey(), "title", "Go live in May"));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, count(decision.getArtefact(), Action.decisionContentUpdate));

		// an empty decision date creates a decision without date
		Map<String,Object> undated = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"title", "Undated", "decisionDate", "")));
		dbInstance.commitAndCloseSession();
		Assert.assertFalse(undated.containsKey("decisionDate"));
		Assert.assertNull(projectService.getDecision(() -> (Long)undated.get("key")).getDecisionDate());
	}

	@Test
	public void create_withMetadataAndLinks() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-103");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-104");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote note = createNote(owner, project, "Evaluation");
		ProjToDo toDo = createToDo(owner, project, "Migrate");
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("projectKey", project.getKey());
		arguments.put("title", "Use PostgreSQL");
		arguments.put("details", "We use PostgreSQL 16.\nNo MySQL.");
		arguments.put("decisionDate", "2026-09-01");
		arguments.put("tags", List.of("database", "architecture"));
		arguments.put("members", List.of(username(owner), participant.getUser().getEmail()));
		arguments.put("links", List.of(Map.of("type", "note", "key", note.getKey()), Map.of("type", "toDo", "key", toDo.getToDoTask().getKey())));
		McpToolResult result = invoke(context(participant), arguments);
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		Long decisionKey = (Long)item.get("key");
		ProjDecision decision = projectService.getDecision(() -> decisionKey);
		Assert.assertEquals("Use PostgreSQL", decision.getTitle());
		Assert.assertEquals("We use PostgreSQL 16.\nNo MySQL.", decision.getDetails());
		Assert.assertEquals("2026-09-01", McpToolSupport.isoDate(decision.getDecisionDate()));
		Assert.assertEquals(participant, decision.getArtefact().getCreator());
		Assert.assertEquals(McpToolSupport.iso(decision.getDecisionDate()), item.get("decisionDate"));
		Assert.assertEquals("We use PostgreSQL 16.\nNo MySQL.", item.get("details"));
		// the modify results carry the full details, not the preview of the list
		Assert.assertFalse(item.containsKey("preview"));
		Assert.assertFalse(item.containsKey("detailsLength"));
		Assert.assertEquals(List.of("architecture", "database"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey(), "note:" + note.getKey());
		assertThat(projectService.getLinkedArtefacts(decision.getArtefact())).extracting(ProjArtefact::getKey)
				.containsExactlyInAnyOrder(note.getArtefact().getKey(), toDo.getArtefact().getKey());
		assertThat(actions(project, participant)).contains(Action.decisionCreate, Action.decisionContentUpdate, Action.decisionTagsUpdate,
				Action.decisionMemberAdd, Action.decisionReferenceAdd);
		assertThat(result.text()).startsWith("Created: Use PostgreSQL (key " + decisionKey + ", decided 2026-09-01")
				.contains("tags architecture, database");
	}

	@Test
	public void create_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-105");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-106");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-107");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		dbInstance.commitAndCloseSession();

		// a client may view but not create artefacts
		try {
			invoke(context(client), Map.of("action", "create", "projectKey", project.getKey(), "title", "x"));
			Assert.fail("A client cannot create decisions");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create decisions", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
		try {
			invoke(context(stranger), Map.of("action", "create", "projectKey", project.getKey(), "title", "x"));
			Assert.fail("A non-member cannot create decisions");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create decisions", e.getPermission());
		}

		// a deleted project is read only, V19: a state error
		projectService.setStatusDeleted(owner, new ProjectBCFactory(), project);
		dbInstance.commitAndCloseSession();
		McpToolResult deletedProject = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x"));
		Assert.assertTrue(deletedProject.error());
		assertThat(deletedProject.text()).startsWith("The project " + project.getKey() + " is deleted");
		Assert.assertEquals(0l, countDecisions(project));
	}

	@Test
	public void create_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-108");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-109");
		ProjProject project = createProject(owner);
		ProjProject otherProject = createProject(stranger);
		ProjDecision foreignDecision = createDecision(stranger, otherProject, "Foreign", null, null);
		ProjToDo foreignToDo = createToDo(stranger, otherProject, "Foreign ToDo");
		ProjDecision deletedDecision = createDecision(owner, project, "Deleted", null, null);
		projectService.deleteDecisionSoftly(owner, deletedDecision);
		dbInstance.commitAndCloseSession();
		long decisionsBefore = countDecisions(project);

		McpToolResult noTitle = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "details", "x"));
		Assert.assertTrue(noTitle.error());
		assertThat(noTitle.text()).contains("title");

		McpToolResult blankTitle = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "  "));
		Assert.assertTrue(blankTitle.error());
		assertThat(blankTitle.text()).contains("title");

		McpToolResult badDate = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"decisionDate", "01.09.2026"));
		Assert.assertTrue(badDate.error());
		assertThat(badDate.text()).contains("decisionDate").contains("yyyy-MM-dd");

		McpToolResult notMember = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"members", List.of(username(stranger))));
		Assert.assertTrue(notMember.error());
		Assert.assertEquals("Invalid argument: " + username(stranger) + " is not a valid member for this decision.", notMember.text());

		// a link to an artefact of another project, to a deleted decision or to an unknown key gives the same message
		String uniform = "is not an active decision of this project.";
		McpToolResult foreign = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "decision", "key", foreignDecision.getKey()))));
		Assert.assertTrue(foreign.error());
		assertThat(foreign.text()).endsWith(uniform);
		McpToolResult deleted = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "decision", "key", deletedDecision.getKey()))));
		Assert.assertTrue(deleted.error());
		assertThat(deleted.text()).endsWith(uniform);
		McpToolResult unknown = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "decision", "key", -1))));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).endsWith(uniform);
		McpToolResult foreignTask = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "toDo", "key", foreignToDo.getToDoTask().getKey()))));
		Assert.assertTrue(foreignTask.error());
		assertThat(foreignTask.text()).endsWith("is not an active toDo of this project.");
		McpToolResult badLink = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", List.of(Map.of("type", "decision"))));
		Assert.assertTrue(badLink.error());
		assertThat(badLink.text()).contains("type").contains("key");

		// nothing was written
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(decisionsBefore, countDecisions(project));
	}

	// update

	@Test
	public void update_onlySuppliedFields() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-110");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-111");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		Date decisionDate = at(2026, 9, 1, 11);
		ProjDecision decision = createDecision(owner, project, "Title", "Details", decisionDate);
		ProjNote linked = createNote(owner, project, "Linked");
		projectService.updateTags(owner, decision.getArtefact(), List.of("keep"));
		projectService.linkArtefacts(owner, decision.getArtefact(), linked.getArtefact());
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		// a participant who is not the creator may edit
		McpToolResult result = invoke(context(participant), Map.of("action", "update", "decisionKey", decision.getKey(), "details", "Details v2"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjDecision reloaded = projectService.getDecision(decision);
		Assert.assertEquals("Title", reloaded.getTitle());
		Assert.assertEquals("Details v2", reloaded.getDetails());
		// the time of the day is kept when the date is not passed
		Assert.assertEquals(decisionDate, reloaded.getDecisionDate());
		Assert.assertEquals(McpToolSupport.iso(decisionDate), item.get("decisionDate"));
		Assert.assertEquals(List.of("keep"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactly(username(owner));
		assertThat(links(item)).containsExactly("note:" + linked.getKey());
		assertThat(result.text()).startsWith("Updated: Title (key " + decision.getKey());
		assertThat(actions(project, participant)).contains(Action.decisionContentUpdate);
		assertNewsAfter(project, before);

		// an unchanged content writes no activity
		List<Action> actionsBefore = actions(project, participant);
		invoke(context(participant), Map.of("action", "update", "decisionKey", decision.getKey(), "details", "Details v2"));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(actionsBefore.size(), actions(project, participant).size());

		// a new date replaces the date
		Map<String,Object> redated = item(invoke(context(participant), Map.of("action", "update", "decisionKey", decision.getKey(),
				"decisionDate", "2026-09-30")));
		dbInstance.commitAndCloseSession();
		assertThat((String)redated.get("decisionDate")).startsWith("2026-09-30T00:00:00");
		Assert.assertEquals("Details v2", redated.get("details"));
	}

	@Test
	public void update_replaceAndClearMetadata() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-112");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-113");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjDecision decision = createDecision(owner, project, "Title", "Details", at(2026, 9, 1, 11));
		ProjNote oldLink = createNote(owner, project, "Old link");
		ProjDecision newLink = createDecision(owner, project, "New link", null, null);
		ProjToDo toDo = createToDo(owner, project, "ToDo");
		projectService.updateTags(owner, decision.getArtefact(), List.of("old"));
		projectService.linkArtefacts(owner, decision.getArtefact(), oldLink.getArtefact());
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "update");
		arguments.put("decisionKey", decision.getKey());
		arguments.put("details", "");
		arguments.put("decisionDate", "");
		arguments.put("tags", List.of("new"));
		arguments.put("members", List.of(username(participant)));
		arguments.put("links", List.of(Map.of("type", "decision", "key", newLink.getKey()), Map.of("type", "toDo", "key", toDo.getToDoTask().getKey())));
		Map<String,Object> item = item(invoke(context(owner), arguments));
		dbInstance.commitAndCloseSession();

		ProjDecision reloaded = projectService.getDecision(decision);
		Assert.assertEquals("Title", reloaded.getTitle());
		Assert.assertNull(reloaded.getDetails());
		Assert.assertNull(reloaded.getDecisionDate());
		Assert.assertFalse(item.containsKey("details"));
		Assert.assertFalse(item.containsKey("decisionDate"));
		Assert.assertEquals(List.of("new"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactly(username(participant));
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey(), "decision:" + newLink.getKey());
		assertThat(projectService.getLinkedArtefacts(decision.getArtefact())).extracting(ProjArtefact::getKey)
				.containsExactlyInAnyOrder(newLink.getArtefact().getKey(), toDo.getArtefact().getKey());
		assertThat(actions(project, owner)).contains(Action.decisionContentUpdate, Action.decisionTagsUpdate, Action.decisionMemberAdd,
				Action.decisionMemberRemove, Action.decisionReferenceAdd, Action.decisionReferenceRemove);

		// empty lists clear tags and links, the members stay
		Map<String,Object> cleared = item(invoke(context(owner), Map.of("action", "update", "decisionKey", decision.getKey(),
				"tags", List.of(), "links", List.of())));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(List.of(), cleared.get("tags"));
		assertThat(persons(cleared, "members")).containsExactly(username(participant));
		Assert.assertEquals(List.of(), cleared.get("links"));
		assertThat(projectService.getLinkedArtefacts(decision.getArtefact())).isEmpty();

		// an empty member list is refused, the UI requires at least one involved member
		McpToolResult noMembers = invoke(context(owner), Map.of("action", "update", "decisionKey", decision.getKey(), "members", List.of()));
		Assert.assertTrue(noMembers.error());
		assertThat(noMembers.text()).contains("at least one project member");
		Assert.assertFalse(VALIDATOR.validate(sut.getDefinition().inputSchema(), Map.of("action", "update", "decisionKey", 1, "members", List.of())).valid());

		// the title is mandatory
		McpToolResult empty = invoke(context(owner), Map.of("action", "update", "decisionKey", decision.getKey(), "title", ""));
		Assert.assertTrue(empty.error());
		assertThat(empty.text()).contains("title");
		Assert.assertEquals("Title", projectService.getDecision(decision).getTitle());
	}

	@Test
	public void update_selfLinkRefused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-114");
		ProjProject project = createProject(owner);
		ProjDecision decision = createDecision(owner, project, "Title", null, null);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(owner), Map.of("action", "update", "decisionKey", decision.getKey(),
				"links", List.of(Map.of("type", "decision", "key", decision.getKey()))));
		Assert.assertTrue(result.error());
		assertThat(result.text()).endsWith("is not an active decision of this project.");
	}

	@Test
	public void update_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-115");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-116");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-117");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		ProjDecision decision = createDecision(owner, project, "Title", null, null);
		ProjDecision deleted = createDecision(owner, project, "Deleted", null, null);
		projectService.deleteDecisionSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "update", "decisionKey", decision.getKey(), "title", "x"));
			Assert.fail("A non-member cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the decision", e.getPermission());
		}
		try {
			invoke(context(client), Map.of("action", "update", "decisionKey", decision.getKey(), "title", "x"));
			Assert.fail("A client cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("edit the decision", e.getPermission());
			Assert.assertEquals("decision with key " + decision.getKey(), e.getObjectDescription());
		}
		McpToolResult deletedDecision = invoke(context(owner), Map.of("action", "update", "decisionKey", deleted.getKey(), "title", "x"));
		Assert.assertTrue(deletedDecision.error());
		Assert.assertEquals("The decision " + deleted.getKey() + " is already deleted.", deletedDecision.text());
		Assert.assertEquals("Title", projectService.getDecision(decision).getTitle());
		Assert.assertEquals("Deleted", projectService.getDecision(deleted).getTitle());
	}

	// delete

	@Test
	public void delete_creatorAndAdminRoles() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-118");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-119");
		Identity otherParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-120");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		projectService.updateMember(owner, new ProjectBCFactory(), project, otherParticipant, Set.of(ProjectRole.participant), null);
		ProjDecision byParticipant = createDecision(participant, project, "By participant", null, null);
		ProjDecision byOwner = createDecision(participant, project, "Also by participant", null, null);
		dbInstance.commitAndCloseSession();

		// a participant who is not the creator cannot delete
		try {
			invoke(context(otherParticipant), Map.of("action", "delete", "decisionKey", byParticipant.getKey()));
			Assert.fail("A participant cannot delete the decision of another creator");
		} catch (McpPermissionException e) {
			Assert.assertEquals("delete the decision", e.getPermission());
		}

		Date before = setPublisherNewsInPast(project);
		// the creator deletes the own decision
		McpToolResult result = invoke(context(participant), Map.of("action", "delete", "decisionKey", byParticipant.getKey()));
		Map<String,Object> item = item(result);
		Assert.assertEquals("deleted", item.get("status"));
		assertThat(result.text()).startsWith("Deleted: By participant (key " + byParticipant.getKey());
		// the owner deletes the decision of another creator
		invoke(context(owner), Map.of("action", "delete", "decisionKey", byOwner.getKey()));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(ProjectStatus.deleted, projectService.getDecision(byParticipant).getArtefact().getStatus());
		Assert.assertEquals(ProjectStatus.deleted, projectService.getDecision(byOwner).getArtefact().getStatus());
		assertThat(actions(project, participant)).contains(Action.decisionStatusDelete);
		assertThat(actions(project, owner)).contains(Action.decisionStatusDelete);
		assertNewsAfter(project, before);

		// deleting twice is refused with a state error
		McpToolResult twice = invoke(context(owner), Map.of("action", "delete", "decisionKey", byOwner.getKey()));
		Assert.assertTrue(twice.error());
		Assert.assertEquals("The decision " + byOwner.getKey() + " is already deleted.", twice.text());
	}

	/**
	 * V15: the decision date takes a date-time as the date chooser with time
	 * of ProjDecisionContentEditController; a date stays accepted.
	 */
	@Test
	public void decisionDate_dateTime() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-140");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		Map<String,Object> local = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"title", "Local", "decisionDate", "2026-09-01T14:30")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(dateTime(2026, 9, 1, 14, 30).getTime(),
				projectService.getDecision(() -> (Long)local.get("key")).getDecisionDate().getTime());

		String offsetValue = "2026-09-01T12:15:00Z";
		Map<String,Object> offset = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"title", "Offset", "decisionDate", offsetValue)));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(java.time.Instant.parse(offsetValue).toEpochMilli(),
				projectService.getDecision(() -> (Long)offset.get("key")).getDecisionDate().getTime());

		Map<String,Object> dateOnly = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"title", "Date", "decisionDate", "2026-09-02")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(dateTime(2026, 9, 2, 0, 0).getTime(), projectService.getDecision(() -> (Long)dateOnly.get("key")).getDecisionDate().getTime());

		// update keeps the time too
		item(invoke(context(owner), Map.of("action", "update", "decisionKey", local.get("key"), "decisionDate", "2026-09-03T08:00")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(dateTime(2026, 9, 3, 8, 0).getTime(), projectService.getDecision(() -> (Long)local.get("key")).getDecisionDate().getTime());

		// F6: the output is a date-time, a write-back of the read value keeps the time and changes nothing
		Map<String,Object> read = item(invoke(context(owner), Map.of("action", "update", "decisionKey", local.get("key"), "details", "x")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(McpToolSupport.iso(dateTime(2026, 9, 3, 8, 0)), read.get("decisionDate"));
		List<Action> actionsBefore = actions(project, owner);
		item(invoke(context(owner), Map.of("action", "update", "decisionKey", local.get("key"), "decisionDate", read.get("decisionDate"))));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(dateTime(2026, 9, 3, 8, 0).getTime(), projectService.getDecision(() -> (Long)local.get("key")).getDecisionDate().getTime());
		Assert.assertEquals(actionsBefore.size(), actions(project, owner).size());

		McpToolResult bad = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x", "decisionDate", "tomorrow"));
		Assert.assertTrue(bad.error());
		assertThat(bad.text()).contains("decisionDate").contains("yyyy-MM-ddTHH:mm").contains("yyyy-MM-dd)");
	}

	/**
	 * V10 and V3: no involved members in a template, tags de-duplicated.
	 */
	@Test
	public void template_membersRefused_tagsDeduplicated() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-141");
		ProjProject template = createProject(owner);
		template = projectService.updateProject(owner, new ProjectBCFactory(), template, null, template.getTitle(), null, null, true, false);
		dbInstance.commitAndCloseSession();

		McpToolResult members = invoke(context(owner), Map.of("action", "create", "projectKey", template.getKey(), "title", "x",
				"members", List.of(username(owner))));
		Assert.assertTrue(members.error());
		assertThat(members.text()).contains("template project have no involved members");

		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "projectKey", template.getKey(), "title", "Tagged",
				"tags", List.of("Go", "go", "GO"))));
		Assert.assertEquals(List.of("Go"), item.get("tags"));
		// no decision date in a template by default
		Assert.assertFalse(item.containsKey("decisionDate"));
	}

	@Test
	public void delete_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-121");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-122");
		ProjProject project = createProject(owner);
		ProjDecision decision = createDecision(owner, project, "Title", null, null);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "delete", "decisionKey", decision.getKey()));
			Assert.fail("A non-member cannot delete");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the decision", e.getPermission());
		}
		Assert.assertEquals(ProjectStatus.active, projectService.getDecision(decision).getArtefact().getStatus());
	}

	@Test
	public void boundedInputs() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-123");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult title = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"title", "x".repeat(DecisionModifyToolProvider.TITLE_MAX_LENGTH + 1)));
		Assert.assertTrue(title.error());
		assertThat(title.text()).contains("title").contains(String.valueOf(DecisionModifyToolProvider.TITLE_MAX_LENGTH));

		McpToolResult details = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"details", "x".repeat(DecisionModifyToolProvider.DETAILS_MAX_LENGTH + 1)));
		Assert.assertTrue(details.error());
		assertThat(details.text()).contains("details").contains(String.valueOf(DecisionModifyToolProvider.DETAILS_MAX_LENGTH));

		McpToolResult tags = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"tags", List.of("y".repeat(ProjectToolSupport.TAG_MAX_LENGTH + 1))));
		Assert.assertTrue(tags.error());
		assertThat(tags.text()).contains("tags");

		List<Map<String,Object>> tooManyLinks = java.util.stream.IntStream.range(0, ProjectToolSupport.LINKS_MAX_ITEMS + 1)
				.mapToObj(i -> Map.<String,Object>of("type", "decision", "key", i)).toList();
		McpToolResult links = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "title", "x",
				"links", tooManyLinks));
		Assert.assertTrue(links.error());
		assertThat(links.text()).contains("links").contains(String.valueOf(ProjectToolSupport.LINKS_MAX_ITEMS));

		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0l, countDecisions(project));
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private ProjDecision createDecision(Identity doer, ProjProject project, String title, String details, Date decisionDate) {
		ProjDecision decision = projectService.createDecision(doer, project);
		projectService.updateDecision(doer, decision, title, details, decisionDate);
		return decision;
	}

	private ProjNote createNote(Identity doer, ProjProject project, String title) {
		ProjNote note = projectService.createNote(doer, project);
		projectService.updateNote(doer, note, UUID.randomUUID().toString(), title, "text");
		return note;
	}

	private ProjToDo createToDo(Identity doer, ProjProject project, String title) {
		ProjToDo toDo = projectService.createToDo(doer, project);
		projectService.updateToDo(doer, toDo, title, ToDoStatus.open, null, null, null, null, null);
		projectService.updateMembers(doer, toDo, List.<IdentityRef>of(doer), List.of());
		return toDo;
	}

	private static Date dateTime(int year, int month, int day, int hour, int minute) {
		return Date.from(LocalDateTime.of(year, month, day, hour, minute).atZone(ZoneId.systemDefault()).toInstant());
	}

	private static Date at(int year, int month, int day, int hour) {
		return Date.from(LocalDateTime.of(year, month, day, hour, 30).atZone(ZoneId.systemDefault()).toInstant());
	}

	private long countDecisions(ProjProject project) {
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setProject(project);
		return projectService.getDecisionsCount(searchParams);
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
		assertThat(DecisionModifyToolProvider.RESULT_FIELDS).containsAll(minimal.keySet());
		assertThat(result.text()).endsWith("Call decision_query get for the full item.");
		Long key = (Long)minimal.get("key");
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setDecisions(List.of(() -> key));
		return DecisionToolSupport.decisionItem(support, projectService.getDecisionInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0), true);
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
