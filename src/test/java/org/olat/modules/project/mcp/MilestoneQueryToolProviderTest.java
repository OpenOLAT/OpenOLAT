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
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.manager.ProjArtefactToArtefactDAO;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool milestone_query: the list of a project with
 * its filters and the sorting by due date, one milestone with its metadata
 * and links, the rights.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class MilestoneQueryToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(MilestoneQueryToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjArtefactToArtefactDAO artefactToArtefactDao;
	@Autowired
	private MilestoneQueryToolProvider sut;

	@Test
	public void definition() {
		Assert.assertEquals("milestone_query", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.READ, sut.getScope());
		assertThat(sut.getDefinition().description()).contains("project_query").contains("milestone_modify").contains("due date");
		Assert.assertFalse(sut.getDefinition().destructive());
		Assert.assertTrue(sut.getDefinition().idempotent());
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("projectKey", 12)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "list", "projectKey", 12, "milestoneStatus", List.of("open", "achieved"),
				"dueAfter", "2026-10-01", "dueBefore", "2026-12-31", "status", "all", "createdByMe", true, "limit", 200)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "get", "milestoneKey", 3)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "milestoneStatus", List.of("done"))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "status", "archived")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "limit", 201)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "createdAfter", "2026-10-01")).valid());
	}

	@Test
	public void list_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-1");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult noKey = invoke(context(owner), Map.of());
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("projectKey", Long.valueOf(-5))));
		Assert.assertEquals("project with key -5", unknownProject.getObjectDescription());
		Assert.assertEquals("view the milestones", unknownProject.getPermission());

		McpToolResult badStatus = invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "archived"));
		Assert.assertTrue(badStatus.error());
		assertThat(badStatus.text()).contains("Unknown status 'archived'");

		McpToolResult badMilestoneStatus = invoke(context(owner), Map.of("projectKey", project.getKey(), "milestoneStatus", List.of("done")));
		Assert.assertTrue(badMilestoneStatus.error());
		assertThat(badMilestoneStatus.text()).contains("Unknown milestoneStatus 'done'");

		McpToolResult badDate = invoke(context(owner), Map.of("projectKey", project.getKey(), "dueAfter", "tomorrow"));
		Assert.assertTrue(badDate.error());
		assertThat(badDate.text()).contains("dueAfter");

		McpToolResult reversed = invoke(context(owner), Map.of("projectKey", project.getKey(), "dueAfter", "2026-10-10", "dueBefore", "2026-10-01"));
		Assert.assertTrue(reversed.error());
		assertThat(reversed.text()).contains("dueBefore is before dueAfter");

		McpToolResult unknownAction = invoke(context(owner), Map.of("action", "search"));
		Assert.assertTrue(unknownAction.error());
		assertThat(unknownAction.text()).contains("Unknown action search");
	}

	@Test
	public void list_itemsAndSorting() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-3");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		Date today = DateUtils.setTime(new Date(), 0, 0, 0);
		ProjMilestone noDue = createMilestone(owner, project, "No due date", null, ProjMilestoneStatus.open);
		ProjMilestone later = createMilestone(owner, project, "Later", DateUtils.addDays(today, 20), ProjMilestoneStatus.open);
		ProjMilestone sooner = createMilestone(participant, project, "Sooner", DateUtils.addDays(today, 5), ProjMilestoneStatus.achieved);
		ProjMilestone sameDay = createMilestone(owner, project, "Same day", DateUtils.addDays(today, 5), ProjMilestoneStatus.open);
		projectService.updateMilestone(owner, new ProjectBCFactory(), later, ProjMilestoneStatus.open, DateUtils.addDays(today, 20),
				"Later", "The release\nof the product", "red");
		projectService.updateTags(owner, later.getArtefact(), List.of("release", "alpha"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("projectKey", project.getKey()));
		Map<String,Object> structured = structured(result);
		Assert.assertEquals(Integer.valueOf(4), structured.get("total"));
		Assert.assertEquals(Boolean.FALSE, structured.get("truncated"));
		// due date ascending, the same day by key, no due date last
		assertThat(keys(result)).containsExactly(sooner.getKey(), sameDay.getKey(), later.getKey(), noDue.getKey());

		Map<String,Object> laterItem = items(structured).get(2);
		Assert.assertEquals(project.getKey(), laterItem.get("projectKey"));
		Assert.assertEquals("Later", laterItem.get("subject"));
		Assert.assertEquals("The release\nof the product", laterItem.get("description"));
		Assert.assertEquals("open", laterItem.get("milestoneStatus"));
		Assert.assertEquals(McpToolSupport.isoDate(DateUtils.addDays(today, 20)), laterItem.get("dueDate"));
		Assert.assertEquals("red", laterItem.get("color"));
		Assert.assertEquals("active", laterItem.get("status"));
		Assert.assertEquals(List.of("alpha", "release"), laterItem.get("tags"));
		// the artefact DAO makes the creator a member, the service cannot change the members of a milestone
		assertThat(((List<?>)laterItem.get("members"))).hasSize(1);
		Assert.assertEquals(username(owner), ((Map<?,?>)((List<?>)laterItem.get("members")).get(0)).get("username"));
		Assert.assertEquals(username(owner), ((Map<?,?>)laterItem.get("creator")).get("username"));
		Assert.assertNotNull(laterItem.get("created"));
		Assert.assertNotNull(laterItem.get("lastModified"));
		Assert.assertFalse(laterItem.containsKey("links"));
		assertThat(laterItem.get("url").toString()).contains("/Project/" + project.getKey()).contains("/Milestone/" + later.getKey());

		Map<String,Object> soonerItem = items(structured).get(0);
		Assert.assertEquals("achieved", soonerItem.get("milestoneStatus"));
		Assert.assertFalse(soonerItem.containsKey("description"));
		Assert.assertFalse(soonerItem.containsKey("color"));
		Map<String,Object> noDueItem = items(structured).get(3);
		Assert.assertFalse(noDueItem.containsKey("dueDate"));

		assertThat(result.text()).contains("4 milestones")
				.contains("Later (key " + later.getKey() + ", open, due " + McpToolSupport.isoDate(DateUtils.addDays(today, 20)))
				.contains("tags alpha, release")
				.contains("No due date (key " + noDue.getKey() + ", open, no due date");
	}

	@Test
	public void list_filters() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-5");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		Date today = DateUtils.setTime(new Date(), 0, 0, 0);
		ProjMilestone open = createMilestone(owner, project, "Open", DateUtils.addDays(today, 2), ProjMilestoneStatus.open);
		ProjMilestone achieved = createMilestone(owner, project, "Achieved", DateUtils.addDays(today, 10), ProjMilestoneStatus.achieved);
		ProjMilestone theirs = createMilestone(participant, project, "Theirs", null, ProjMilestoneStatus.open);
		ProjMilestone deleted = createMilestone(owner, project, "Deleted", DateUtils.addDays(today, 2), ProjMilestoneStatus.open);
		projectService.deleteMilestoneSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		Long projectKey = project.getKey();
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey))))
				.containsExactly(open.getKey(), achieved.getKey(), theirs.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "milestoneStatus", List.of("open")))))
				.containsExactly(open.getKey(), theirs.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "milestoneStatus", List.of("achieved")))))
				.containsExactly(achieved.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "milestoneStatus", List.of()))))
				.containsExactly(open.getKey(), achieved.getKey(), theirs.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "status", "deleted"))))
				.containsExactly(deleted.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "status", "all"))))
				.containsExactlyInAnyOrder(open.getKey(), achieved.getKey(), theirs.getKey(), deleted.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "createdByMe", true))))
				.containsExactly(open.getKey(), achieved.getKey());
		// the due date filters are inclusive days and skip milestones without due date
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "dueAfter", McpToolSupport.isoDate(DateUtils.addDays(today, 2))))))
				.containsExactly(open.getKey(), achieved.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "dueAfter", McpToolSupport.isoDate(DateUtils.addDays(today, 3))))))
				.containsExactly(achieved.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "dueBefore", McpToolSupport.isoDate(DateUtils.addDays(today, 2))))))
				.containsExactly(open.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey, "dueBefore", McpToolSupport.isoDate(DateUtils.addDays(today, 1))))))
				.isEmpty();
		assertThat(keys(invoke(context(owner), Map.of("projectKey", projectKey,
				"dueAfter", McpToolSupport.isoDate(DateUtils.addDays(today, 10)), "dueBefore", McpToolSupport.isoDate(DateUtils.addDays(today, 10))))))
				.containsExactly(achieved.getKey());
	}

	@Test
	public void list_limitAndTruncated() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-6");
		ProjProject project = createProject(owner);
		createMilestone(owner, project, "A", null, ProjMilestoneStatus.open);
		createMilestone(owner, project, "B", null, ProjMilestoneStatus.open);
		createMilestone(owner, project, "C", null, ProjMilestoneStatus.open);
		dbInstance.commitAndCloseSession();

		Map<String,Object> structured = structured(invoke(context(owner), Map.of("projectKey", project.getKey(), "limit", 2)));
		Assert.assertEquals(2, items(structured).size());
		Assert.assertEquals(Integer.valueOf(3), structured.get("total"));
		Assert.assertEquals(Boolean.TRUE, structured.get("truncated"));
	}

	@Test
	public void list_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-7");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-8");
		ProjProject project = createProject(owner);
		createMilestone(owner, project, "Secret", null, ProjMilestoneStatus.open);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("projectKey", project.getKey()));
			Assert.fail("A non-member cannot list the milestones");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the milestones", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_withLinks() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-9");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-10");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		ProjMilestone milestone = createMilestone(owner, project, "Go live", null, ProjMilestoneStatus.open);
		projectService.updateMilestone(owner, new ProjectBCFactory(), milestone, ProjMilestoneStatus.open, null, "Go live", "All systems ready", null);
		ProjNote note = projectService.createNote(owner, project);
		projectService.updateNote(owner, note, UUID.randomUUID().toString(), "Checklist", "x");
		ProjToDo toDo = projectService.createToDo(owner, project);
		projectService.updateToDo(owner, toDo, "Follow up", ToDoStatus.open, null, null, null, null, null);
		ProjNote deletedLinked = projectService.createNote(owner, project);
		projectService.updateNote(owner, deletedLinked, UUID.randomUUID().toString(), "Deleted linked", "x");
		// ProjectService.linkArtefacts has no reference activity for milestones and throws: link through the DAO
		artefactToArtefactDao.create(project, owner, milestone.getArtefact(), note.getArtefact());
		artefactToArtefactDao.create(project, owner, toDo.getArtefact(), milestone.getArtefact());
		artefactToArtefactDao.create(project, owner, milestone.getArtefact(), deletedLinked.getArtefact());
		projectService.deleteNoteSoftly(owner, deletedLinked);
		dbInstance.commitAndCloseSession();

		// a client views the milestones
		McpToolResult result = invoke(context(client), Map.of("action", "get", "milestoneKey", milestone.getKey()));
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals(milestone.getKey(), item.get("key"));
		Assert.assertEquals("Go live", item.get("subject"));
		Assert.assertEquals("All systems ready", item.get("description"));

		@SuppressWarnings("unchecked")
		List<Map<String,Object>> links = (List<Map<String,Object>>)item.get("links");
		// the deleted linked note is not listed
		Assert.assertEquals(2, links.size());
		Assert.assertEquals("toDo", links.get(0).get("type"));
		Assert.assertEquals(toDo.getToDoTask().getKey(), links.get(0).get("key"));
		Assert.assertEquals("Follow up", links.get(0).get("title"));
		Assert.assertEquals("note", links.get(1).get("type"));
		Assert.assertEquals(note.getKey(), links.get(1).get("key"));
		Assert.assertEquals("Checklist", links.get(1).get("title"));
		assertThat(result.text()).contains("Milestone Go live (key " + milestone.getKey()).contains("All systems ready");
	}

	@Test
	public void get_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-11");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-12");
		ProjProject project = createProject(owner);
		ProjMilestone milestone = createMilestone(owner, project, "Secret", null, ProjMilestoneStatus.open);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "get", "milestoneKey", milestone.getKey()));
			Assert.fail("A non-member cannot read the milestone");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the milestone", e.getPermission());
			Assert.assertEquals("milestone with key " + milestone.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-13");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "get"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("milestoneKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknown = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "get", "milestoneKey", Long.valueOf(-7))));
		Assert.assertEquals("milestone with key -7", unknown.getObjectDescription());
		Assert.assertEquals("view the milestone", unknown.getPermission());
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private ProjMilestone createMilestone(Identity doer, ProjProject project, String subject, Date dueDate, ProjMilestoneStatus status) {
		ProjMilestone milestone = projectService.createMilestone(doer, new ProjectBCFactory(), project);
		projectService.updateMilestone(doer, new ProjectBCFactory(), milestone, status, dueDate, subject, null, null);
		return milestone;
	}

	private McpToolContext context(Identity identity) {
		return new McpToolContext(identity, securityManager.getRoles(identity), GRANTS, null, Locale.ENGLISH, "127.0.0.1");
	}

	private static String username(Identity identity) {
		return identity.getUser().getNickName();
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
