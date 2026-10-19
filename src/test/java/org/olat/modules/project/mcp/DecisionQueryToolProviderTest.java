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
import org.olat.core.id.Identity;
import org.olat.mcp.McpGrants;
import org.olat.mcp.manager.McpJsonSchemaValidator;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.modules.project.ProjDecision;
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
 * Integration tests of the tool decision_query: the list of a project with
 * its filters and the sorting by decision date, one decision with details,
 * metadata and links, the rights.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class DecisionQueryToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(DecisionQueryToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private DecisionQueryToolProvider sut;

	@Test
	public void definition() {
		Assert.assertEquals("decision_query", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.READ, sut.getScope());
		assertThat(sut.getDefinition().description()).contains("project_query").contains("decision_modify");
		Assert.assertFalse(sut.getDefinition().destructive());
		Assert.assertTrue(sut.getDefinition().idempotent());
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("projectKey", 12)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "list", "projectKey", 12, "status", "all", "createdByMe", true,
				"decidedAfter", "2026-01-01", "decidedBefore", "2026-12-31", "limit", 200)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "get", "decisionKey", 3)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "status", "archived")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "limit", 201)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "search", "projectKey", 12)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "createdAfter", "2026-01-01")).valid());
	}

	@Test
	public void list_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-1");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult noKey = invoke(context(owner), Map.of());
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("projectKey", Long.valueOf(-5))));
		Assert.assertEquals("project with key -5", unknownProject.getObjectDescription());
		Assert.assertEquals("view the decisions", unknownProject.getPermission());

		McpToolResult badStatus = invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "archived"));
		Assert.assertTrue(badStatus.error());
		assertThat(badStatus.text()).contains("Unknown status 'archived'");

		McpToolResult badAfter = invoke(context(owner), Map.of("projectKey", project.getKey(), "decidedAfter", "yesterday"));
		Assert.assertTrue(badAfter.error());
		assertThat(badAfter.text()).contains("decidedAfter");

		McpToolResult badBefore = invoke(context(owner), Map.of("projectKey", project.getKey(), "decidedBefore", "2026-13-01"));
		Assert.assertTrue(badBefore.error());
		assertThat(badBefore.text()).contains("decidedBefore");

		McpToolResult unknownAction = invoke(context(owner), Map.of("action", "search"));
		Assert.assertTrue(unknownAction.error());
		assertThat(unknownAction.text()).contains("Unknown action search");
	}

	@Test
	public void list_membersAndSorting() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-3");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjDecision undated = createDecision(owner, project, "Undated", null, null);
		ProjDecision older = createDecision(owner, project, "Older", "Some details\nsecond line", at(2026, 3, 2, 9));
		ProjDecision latest = createDecision(participant, project, "Latest", null, at(2026, 9, 15, 14));
		ProjDecision sameDayFirst = createDecision(owner, project, "Same day 1", null, at(2026, 5, 10, 10));
		ProjDecision sameDaySecond = createDecision(owner, project, "Same day 2", null, at(2026, 5, 10, 10));
		projectService.updateTags(owner, older.getArtefact(), List.of("budget", "alpha"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("projectKey", project.getKey()));
		Map<String,Object> structured = structured(result);
		Assert.assertEquals(Integer.valueOf(5), structured.get("total"));
		Assert.assertEquals(Boolean.FALSE, structured.get("truncated"));
		// latest decision first, the same date by key descending, without date last
		Assert.assertEquals(List.of(latest.getKey(), sameDaySecond.getKey(), sameDayFirst.getKey(), older.getKey(), undated.getKey()), keys(result));

		Map<String,Object> olderItem = items(structured).get(3);
		Assert.assertEquals("Older", olderItem.get("title"));
		// list returns a preview of the details, get the details
		Assert.assertFalse(olderItem.containsKey("details"));
		Assert.assertEquals("Some details second line", olderItem.get("preview"));
		Assert.assertEquals(Integer.valueOf(24), olderItem.get("detailsLength"));
		// F6: a date-time with offset
		assertThat((String)olderItem.get("decisionDate")).startsWith("2026-03-02T");
		Assert.assertEquals("active", olderItem.get("status"));
		Assert.assertEquals(project.getKey(), olderItem.get("projectKey"));
		Assert.assertEquals(List.of("alpha", "budget"), olderItem.get("tags"));
		Assert.assertFalse(olderItem.containsKey("links"));
		assertThat(persons(olderItem, "members")).containsExactly(username(owner));
		Assert.assertEquals(username(owner), ((Map<?,?>)olderItem.get("creator")).get("username"));
		assertThat(olderItem.get("url").toString()).contains("/Project/" + project.getKey()).contains("/Decision/" + older.getKey());

		Map<String,Object> undatedItem = items(structured).get(4);
		Assert.assertFalse(undatedItem.containsKey("decisionDate"));
		Assert.assertFalse(undatedItem.containsKey("details"));
		Assert.assertFalse(undatedItem.containsKey("preview"));
		Assert.assertEquals(Integer.valueOf(0), undatedItem.get("detailsLength"));
		assertThat(result.text()).contains("5 decisions").contains("Older (key " + older.getKey() + ", decided 2026-03-02")
				.contains("Undated (key " + undated.getKey() + ", decided (no date)");
	}

	@Test
	public void list_filters() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-5");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjDecision march = createDecision(owner, project, "March", null, at(2026, 3, 31, 23));
		ProjDecision april = createDecision(participant, project, "April", null, at(2026, 4, 1, 0));
		ProjDecision undated = createDecision(owner, project, "Undated", null, null);
		ProjDecision deleted = createDecision(owner, project, "Deleted", null, at(2026, 4, 2, 12));
		projectService.deleteDecisionSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		Long projectKey = project.getKey();
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey))))
				.containsExactly(april.getKey(), march.getKey(), undated.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "status", "deleted"))))
				.containsExactly(deleted.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "status", "all"))))
				.containsExactly(deleted.getKey(), april.getKey(), march.getKey(), undated.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "createdByMe", true))))
				.containsExactly(march.getKey(), undated.getKey());
		// both bounds include their day, a decision without date never matches a date filter
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "decidedAfter", "2026-04-01"))))
				.containsExactly(april.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "decidedBefore", "2026-03-31"))))
				.containsExactly(march.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "decidedAfter", "2026-03-31", "decidedBefore", "2026-04-01"))))
				.containsExactly(april.getKey(), march.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "decidedAfter", "2026-04-02")))).isEmpty();
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "status", "all", "decidedAfter", "2026-04-02"))))
				.containsExactly(deleted.getKey());
	}

	@Test
	public void list_limitAndTruncated() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-6");
		ProjProject project = createProject(owner);
		createDecision(owner, project, "A", null, at(2026, 1, 1, 10));
		createDecision(owner, project, "B", null, at(2026, 1, 2, 10));
		ProjDecision c = createDecision(owner, project, "C", null, at(2026, 1, 3, 10));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(owner), Map.of("projectKey", project.getKey(), "limit", 1));
		Map<String,Object> structured = structured(result);
		Assert.assertEquals(1, items(structured).size());
		Assert.assertEquals(c.getKey(), items(structured).get(0).get("key"));
		Assert.assertEquals(Integer.valueOf(3), structured.get("total"));
		Assert.assertEquals(Boolean.TRUE, structured.get("truncated"));
	}

	@Test
	public void list_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-7");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-8");
		ProjProject project = createProject(owner);
		createDecision(owner, project, "Secret", null, new Date());
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("projectKey", project.getKey()));
			Assert.fail("A non-member cannot list the decisions");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the decisions", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_withDetailsAndLinks() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-9");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-10");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjDecision decision = createDecision(owner, project, "Use PostgreSQL", "We use PostgreSQL 16.\nNo MySQL.", at(2026, 9, 1, 11));
		ProjNote note = createNote(owner, project, "Evaluation");
		ProjToDo toDo = projectService.createToDo(owner, project);
		projectService.updateToDo(owner, toDo, "Migrate", ToDoStatus.open, null, null, null, null, null);
		ProjNote deletedLinked = createNote(owner, project, "Deleted linked");
		projectService.linkArtefacts(owner, decision.getArtefact(), note.getArtefact());
		projectService.linkArtefacts(owner, decision.getArtefact(), toDo.getArtefact());
		projectService.linkArtefacts(owner, decision.getArtefact(), deletedLinked.getArtefact());
		projectService.deleteNoteSoftly(owner, deletedLinked);
		projectService.updateMembers(owner, new ProjectBCFactory(), decision.getArtefact(), List.<IdentityRef>of(owner, participant));
		projectService.updateTags(owner, decision.getArtefact(), List.of("database"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("action", "get", "decisionKey", decision.getKey()));
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals(decision.getKey(), item.get("key"));
		Assert.assertEquals("Use PostgreSQL", item.get("title"));
		Assert.assertEquals("We use PostgreSQL 16.\nNo MySQL.", item.get("details"));
		Assert.assertFalse(item.containsKey("preview"));
		Assert.assertFalse(item.containsKey("detailsLength"));
		// F6: a date-time with offset
		assertThat((String)item.get("decisionDate")).startsWith("2026-09-01T");
		Assert.assertEquals(List.of("database"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));

		@SuppressWarnings("unchecked")
		List<Map<String,Object>> links = (List<Map<String,Object>>)item.get("links");
		// the deleted linked note is not listed
		Assert.assertEquals(2, links.size());
		Assert.assertEquals("toDo", links.get(0).get("type"));
		Assert.assertEquals(toDo.getToDoTask().getKey(), links.get(0).get("key"));
		Assert.assertEquals("Migrate", links.get(0).get("title"));
		assertThat(links.get(0).get("url").toString()).contains("/ToDo/" + toDo.getKey());
		Assert.assertEquals("note", links.get(1).get("type"));
		Assert.assertEquals(note.getKey(), links.get(1).get("key"));
		Assert.assertEquals("Evaluation", links.get(1).get("title"));
		assertThat(result.text()).contains("Decision Use PostgreSQL (key " + decision.getKey()).contains("No MySQL.");
	}

	@Test
	public void get_deletedDecision() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-11");
		ProjProject project = createProject(owner);
		ProjDecision decision = createDecision(owner, project, "Revoked", null, null);
		projectService.deleteDecisionSoftly(owner, decision);
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(structured(invoke(context(owner), Map.of("action", "get", "decisionKey", decision.getKey()))));
		Assert.assertEquals("deleted", item.get("status"));
	}

	@Test
	public void get_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-12");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-13");
		ProjProject project = createProject(owner);
		ProjDecision decision = createDecision(owner, project, "Secret", null, new Date());
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "get", "decisionKey", decision.getKey()));
			Assert.fail("A non-member cannot read the decision");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the decision", e.getPermission());
			Assert.assertEquals("decision with key " + decision.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-de-14");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "get"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("decisionKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknown = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "get", "decisionKey", Long.valueOf(-7))));
		Assert.assertEquals("decision with key -7", unknown.getObjectDescription());
		Assert.assertEquals("view the decision", unknown.getPermission());
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

	private static Date at(int year, int month, int day, int hour) {
		return Date.from(LocalDateTime.of(year, month, day, hour, 30).atZone(ZoneId.systemDefault()).toInstant());
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
