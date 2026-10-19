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
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
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
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
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
 * Integration tests of the tool milestone_modify: the rights per action and
 * project role, the service sequences, the metadata round trips and the
 * side effects (activity log, publisher news).
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class MilestoneModifyToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(MilestoneModifyToolProvider.NAME), null, List.of("test"));
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
	private MilestoneModifyToolProvider sut;
	@Autowired
	private MilestoneToolSupport milestoneSupport;

	@Test
	public void definition() {
		Assert.assertEquals("milestone_modify", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.WRITE, sut.getScope());
		Assert.assertTrue(sut.getDefinition().destructive());
		Assert.assertFalse(sut.getDefinition().idempotent());
		assertThat(sut.getDefinition().description()).contains("milestone_query").contains("project_query")
				.contains("replace").contains("clears");
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "create", "projectKey", 3, "subject", "Go live", "description", "d",
				"dueDate", "2026-12-01", "milestoneStatus", "open", "color", "red", "tags", List.of("a"))).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "update", "milestoneKey", 1, "description", "", "dueDate", "", "color", "")).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "set_status", "milestoneKey", 1, "milestoneStatus", "achieved")).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "delete", "milestoneKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("milestoneKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "archive", "milestoneKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "set_status", "milestoneKey", 1, "milestoneStatus", "done")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "milestoneKey", 1, "color", "pink")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "milestoneKey", 1, "members", List.of("jdoe"))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "milestoneKey", 1, "links", List.of(Map.of("type", "note", "key", 5)))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "milestoneKey", 1, "status", "deleted")).valid());
	}

	@Test
	public void invalidActions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-20");

		McpToolResult missing = invoke(context(owner), Map.of());
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("action is required");

		McpToolResult unknown = invoke(context(owner), Map.of("action", "archive"));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).contains("Unknown action archive");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "update", "subject", "x"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("milestoneKey");

		McpToolResult noKeyStatus = invoke(context(owner), Map.of("action", "set_status", "milestoneStatus", "achieved"));
		Assert.assertTrue(noKeyStatus.error());
		assertThat(noKeyStatus.text()).contains("milestoneKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownKey = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "delete", "milestoneKey", Long.valueOf(-9))));
		Assert.assertEquals("milestone with key -9", unknownKey.getObjectDescription());
		Assert.assertEquals("view the milestone", unknownKey.getPermission());

		McpToolResult noProject = invoke(context(owner), Map.of("action", "create", "subject", "x"));
		Assert.assertTrue(noProject.error());
		assertThat(noProject.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "create", "projectKey", Long.valueOf(-9), "subject", "x")));
		Assert.assertEquals("project with key -9", unknownProject.getObjectDescription());
		Assert.assertEquals("create milestones", unknownProject.getPermission());
	}

	// create

	@Test
	public void create_minimal() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-21");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		McpToolResult result = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "subject", "Go live"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjMilestone milestone = projectService.getMilestone(() -> (Long)item.get("key"));
		Assert.assertEquals("Go live", milestone.getSubject());
		Assert.assertNull(milestone.getDescription());
		Assert.assertNull(milestone.getDueDate());
		Assert.assertNull(milestone.getColor());
		Assert.assertEquals(ProjMilestoneStatus.open, milestone.getStatus());
		Assert.assertEquals(project.getKey(), milestone.getArtefact().getProject().getKey());
		Assert.assertEquals(owner, milestone.getArtefact().getCreator());
		Assert.assertEquals("Go live", item.get("subject"));
		Assert.assertEquals("open", item.get("milestoneStatus"));
		Assert.assertFalse(item.containsKey("description"));
		Assert.assertFalse(item.containsKey("dueDate"));
		Assert.assertFalse(item.containsKey("color"));
		Assert.assertEquals(List.of(), item.get("tags"));
		// the artefact DAO makes the creator a member
		Assert.assertEquals(username(owner), ((Map<?,?>)((List<?>)item.get("members")).get(0)).get("username"));
		Assert.assertEquals(List.of(), item.get("links"));
		assertThat(result.text()).startsWith("Created: Go live (key " + milestone.getKey() + ", open, no due date");
		assertThat(actions(project, owner)).contains(Action.milestoneCreate, Action.milestoneContentUpdate);
		assertNewsAfter(project, before);
	}

	@Test
	public void create_activities() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-29");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Go live", "dueDate", "2030-03-05")));
		dbInstance.commitAndCloseSession();

		ProjMilestone milestone = projectService.getMilestone(() -> (Long)item.get("key"));
		// one create and one content update, the sequence of the UI (create, then save the form)
		Assert.assertEquals(1, count(milestone.getArtefact(), Action.milestoneCreate));
		Assert.assertEquals(1, count(milestone.getArtefact(), Action.milestoneContentUpdate));

		// the same due date again, the stored date is a Timestamp: no further activity
		invoke(context(owner), Map.of("action", "update", "milestoneKey", milestone.getKey(), "subject", "Go live", "dueDate", "2030-03-05"));
		dbInstance.commitAndCloseSession();
		projectService.updateMilestone(owner, ProjectBCFactory.createFactory(project), milestone, milestone.getStatus(),
				milestone.getDueDate(), milestone.getSubject(), milestone.getDescription(), milestone.getColor());
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, count(milestone.getArtefact(), Action.milestoneContentUpdate));
	}

	@Test
	public void create_withMetadata() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-22");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-23");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("projectKey", project.getKey());
		arguments.put("subject", "Release");
		arguments.put("description", "Version 2\nwith the new module");
		arguments.put("dueDate", "2026-12-01");
		arguments.put("milestoneStatus", "achieved");
		arguments.put("color", "navy");
		arguments.put("tags", List.of("release", "alpha"));
		McpToolResult result = invoke(context(participant), arguments);
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		Long milestoneKey = (Long)item.get("key");
		ProjMilestone milestone = projectService.getMilestone(() -> milestoneKey);
		Assert.assertEquals("Release", milestone.getSubject());
		Assert.assertEquals("Version 2\nwith the new module", milestone.getDescription());
		Assert.assertEquals("2026-12-01", McpToolSupport.isoDate(milestone.getDueDate()));
		Assert.assertEquals(ProjMilestoneStatus.achieved, milestone.getStatus());
		Assert.assertEquals("navy", milestone.getColor());
		Assert.assertEquals(participant, milestone.getArtefact().getCreator());
		Assert.assertEquals("2026-12-01", item.get("dueDate"));
		Assert.assertEquals("achieved", item.get("milestoneStatus"));
		Assert.assertEquals("navy", item.get("color"));
		Assert.assertEquals(List.of("alpha", "release"), item.get("tags"));
		assertThat(actions(project, participant)).contains(Action.milestoneCreate, Action.milestoneContentUpdate, Action.milestoneTagsUpdate);
		assertThat(result.text()).startsWith("Created: Release (key " + milestoneKey + ", achieved, due 2026-12-01").contains("tags alpha, release");
	}

	@Test
	public void create_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-24");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-25");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-26");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		dbInstance.commitAndCloseSession();

		// a client may view but not create artefacts
		try {
			invoke(context(client), Map.of("action", "create", "projectKey", project.getKey(), "subject", "x"));
			Assert.fail("A client cannot create milestones");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create milestones", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
		try {
			invoke(context(stranger), Map.of("action", "create", "projectKey", project.getKey(), "subject", "x"));
			Assert.fail("A non-member cannot create milestones");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create milestones", e.getPermission());
		}

		// a deleted project is read only, V19: a state error
		projectService.setStatusDeleted(owner, new ProjectBCFactory(), project);
		dbInstance.commitAndCloseSession();
		McpToolResult deletedProject = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "subject", "x"));
		Assert.assertTrue(deletedProject.error());
		assertThat(deletedProject.text()).startsWith("The project " + project.getKey() + " is deleted");
	}

	@Test
	public void create_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-27");
		ProjProject project = createProject(owner);
		ProjNote note = projectService.createNote(owner, project);
		dbInstance.commitAndCloseSession();
		long milestonesBefore = countMilestones(project);
		Long projectKey = project.getKey();

		McpToolResult noSubject = invoke(context(owner), Map.of("action", "create", "projectKey", projectKey, "description", "x"));
		Assert.assertTrue(noSubject.error());
		assertThat(noSubject.text()).contains("subject");

		McpToolResult blankSubject = invoke(context(owner), Map.of("action", "create", "projectKey", projectKey, "subject", "  "));
		Assert.assertTrue(blankSubject.error());
		assertThat(blankSubject.text()).contains("subject");

		McpToolResult badDate = invoke(context(owner), Map.of("action", "create", "projectKey", projectKey, "subject", "x", "dueDate", "next week"));
		Assert.assertTrue(badDate.error());
		assertThat(badDate.text()).contains("dueDate");

		McpToolResult badStatus = invoke(context(owner), Map.of("action", "create", "projectKey", projectKey, "subject", "x", "milestoneStatus", "done"));
		Assert.assertTrue(badStatus.error());
		assertThat(badStatus.text()).contains("Unknown milestoneStatus 'done'");

		McpToolResult badColor = invoke(context(owner), Map.of("action", "create", "projectKey", projectKey, "subject", "x", "color", "pink"));
		Assert.assertTrue(badColor.error());
		assertThat(badColor.text()).contains("Unknown color pink").contains("rebeccapurple");

		McpToolResult members = invoke(context(owner), Map.of("action", "create", "projectKey", projectKey, "subject", "x",
				"members", List.of(username(owner))));
		Assert.assertTrue(members.error());
		assertThat(members.text()).contains("no involved members and no links");

		McpToolResult links = invoke(context(owner), Map.of("action", "create", "projectKey", projectKey, "subject", "x",
				"links", List.of(Map.of("type", "note", "key", note.getKey()))));
		Assert.assertTrue(links.error());
		assertThat(links.text()).contains("no involved members and no links");

		// nothing was written
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(milestonesBefore, countMilestones(project));
	}

	@Test
	public void create_templateProject_noDueDate() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-28");
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		project = projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Template " + random(), null, null, true, false);
		dbInstance.commitAndCloseSession();
		long milestonesBefore = countMilestones(project);

		McpToolResult dueDate = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "subject", "x", "dueDate", "2026-12-01"));
		Assert.assertTrue(dueDate.error());
		assertThat(dueDate.text()).contains("template project has no due date");
		Assert.assertEquals(milestonesBefore, countMilestones(project));

		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "subject", "Phase 1")));
		Assert.assertEquals("Phase 1", item.get("subject"));
	}

	// update

	@Test
	public void update_onlySuppliedFields() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-29");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-30");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjMilestone milestone = createMilestone(owner, project, "Subject", "Description", DateUtils.addDays(new Date(), 10), "red");
		projectService.updateTags(owner, milestone.getArtefact(), List.of("keep"));
		dbInstance.commitAndCloseSession();
		Date dueBefore = projectService.getMilestone(milestone).getDueDate();
		Date before = setPublisherNewsInPast(project);

		// a participant who is not the creator may edit
		McpToolResult result = invoke(context(participant), Map.of("action", "update", "milestoneKey", milestone.getKey(), "subject", "Subject v2"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjMilestone reloaded = projectService.getMilestone(milestone);
		Assert.assertEquals("Subject v2", reloaded.getSubject());
		Assert.assertEquals("Description", reloaded.getDescription());
		Assert.assertEquals(dueBefore, reloaded.getDueDate());
		Assert.assertEquals("red", reloaded.getColor());
		Assert.assertEquals(ProjMilestoneStatus.open, reloaded.getStatus());
		Assert.assertEquals(List.of("keep"), item.get("tags"));
		assertThat(result.text()).startsWith("Updated: Subject v2 (key " + milestone.getKey());
		assertThat(actions(project, participant)).contains(Action.milestoneContentUpdate);
		assertNewsAfter(project, before);

		// an unchanged content writes no activity; a milestone without due date because the service compares
		// the stored Timestamp with a Date and sees every due date as changed (see the report)
		ProjMilestone noDueDate = createMilestone(owner, project, "No due date", "Description", null, "red");
		dbInstance.commitAndCloseSession();
		List<Action> actionsBefore = actions(project, participant);
		invoke(context(participant), Map.of("action", "update", "milestoneKey", noDueDate.getKey(), "subject", "No due date"));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(actionsBefore.size(), actions(project, participant).size());

		// tags only do not touch the content
		Map<String,Object> tagged = item(invoke(context(participant), Map.of("action", "update", "milestoneKey", milestone.getKey(), "tags", List.of("new"))));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(List.of("new"), tagged.get("tags"));
		Assert.assertEquals("Subject v2", projectService.getMilestone(milestone).getSubject());
		assertThat(actions(project, participant)).contains(Action.milestoneTagsUpdate);
	}

	@Test
	public void update_replaceAndClear() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-31");
		ProjProject project = createProject(owner);
		ProjMilestone milestone = createMilestone(owner, project, "Subject", "Description", DateUtils.addDays(new Date(), 10), "red");
		projectService.updateTags(owner, milestone.getArtefact(), List.of("old"));
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "update");
		arguments.put("milestoneKey", milestone.getKey());
		arguments.put("subject", "New subject");
		arguments.put("description", "New description");
		arguments.put("dueDate", "2027-01-15");
		arguments.put("milestoneStatus", "achieved");
		arguments.put("color", "green");
		arguments.put("tags", List.of("new", "other"));
		Map<String,Object> item = item(invoke(context(owner), arguments));
		dbInstance.commitAndCloseSession();

		ProjMilestone reloaded = projectService.getMilestone(milestone);
		Assert.assertEquals("New subject", reloaded.getSubject());
		Assert.assertEquals("New description", reloaded.getDescription());
		Assert.assertEquals("2027-01-15", McpToolSupport.isoDate(reloaded.getDueDate()));
		Assert.assertEquals(ProjMilestoneStatus.achieved, reloaded.getStatus());
		Assert.assertEquals("green", reloaded.getColor());
		Assert.assertEquals(List.of("new", "other"), item.get("tags"));
		assertThat(actions(project, owner)).contains(Action.milestoneContentUpdate, Action.milestoneTagsUpdate);

		// empty values clear
		Map<String,Object> cleared = item(invoke(context(owner), Map.of("action", "update", "milestoneKey", milestone.getKey(),
				"description", "", "dueDate", "", "color", "", "tags", List.of())));
		dbInstance.commitAndCloseSession();
		reloaded = projectService.getMilestone(milestone);
		Assert.assertNull(reloaded.getDescription());
		Assert.assertNull(reloaded.getDueDate());
		Assert.assertNull(reloaded.getColor());
		Assert.assertEquals("New subject", reloaded.getSubject());
		Assert.assertFalse(cleared.containsKey("description"));
		Assert.assertFalse(cleared.containsKey("dueDate"));
		Assert.assertFalse(cleared.containsKey("color"));
		Assert.assertEquals(List.of(), cleared.get("tags"));

		// the subject cannot be cleared, nothing is written
		McpToolResult empty = invoke(context(owner), Map.of("action", "update", "milestoneKey", milestone.getKey(), "subject", "", "description", "x"));
		Assert.assertTrue(empty.error());
		assertThat(empty.text()).contains("needs a subject");
		McpToolResult members = invoke(context(owner), Map.of("action", "update", "milestoneKey", milestone.getKey(),
				"description", "x", "members", List.of()));
		Assert.assertTrue(members.error());
		assertThat(members.text()).contains("no involved members and no links");
		dbInstance.commitAndCloseSession();
		Assert.assertNull(projectService.getMilestone(milestone).getDescription());
	}

	@Test
	public void update_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-32");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-33");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-34");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		ProjMilestone milestone = createMilestone(owner, project, "Subject", null, null, null);
		ProjMilestone deleted = createMilestone(owner, project, "Deleted", null, null, null);
		projectService.deleteMilestoneSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "update", "milestoneKey", milestone.getKey(), "subject", "x"));
			Assert.fail("A non-member cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the milestone", e.getPermission());
		}
		try {
			invoke(context(client), Map.of("action", "update", "milestoneKey", milestone.getKey(), "subject", "x"));
			Assert.fail("A client cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("edit the milestone", e.getPermission());
			Assert.assertEquals("milestone with key " + milestone.getKey(), e.getObjectDescription());
		}
		try {
			invoke(context(client), Map.of("action", "set_status", "milestoneKey", milestone.getKey(), "milestoneStatus", "achieved"));
			Assert.fail("A client cannot change the status");
		} catch (McpPermissionException e) {
			Assert.assertEquals("edit the milestone", e.getPermission());
		}
		McpToolResult deletedMilestone = invoke(context(owner), Map.of("action", "update", "milestoneKey", deleted.getKey(), "subject", "x"));
		Assert.assertTrue(deletedMilestone.error());
		Assert.assertEquals("The milestone " + deleted.getKey() + " is already deleted.", deletedMilestone.text());
		McpToolResult deletedStatus = invoke(context(owner), Map.of("action", "set_status", "milestoneKey", deleted.getKey(), "milestoneStatus", "achieved"));
		Assert.assertTrue(deletedStatus.error());
		Assert.assertEquals("The milestone " + deleted.getKey() + " is already deleted.", deletedStatus.text());
		Assert.assertEquals("Subject", projectService.getMilestone(milestone).getSubject());
		Assert.assertEquals(ProjMilestoneStatus.open, projectService.getMilestone(milestone).getStatus());
	}

	// set_status

	@Test
	public void setStatus() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-35");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-36");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjMilestone milestone = createMilestone(owner, project, "Subject", "Description", DateUtils.addDays(new Date(), 3), "red");
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		McpToolResult result = invoke(context(participant), Map.of("action", "set_status", "milestoneKey", milestone.getKey(), "milestoneStatus", "achieved"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjMilestone reloaded = projectService.getMilestone(milestone);
		Assert.assertEquals(ProjMilestoneStatus.achieved, reloaded.getStatus());
		Assert.assertEquals("Subject", reloaded.getSubject());
		Assert.assertEquals("Description", reloaded.getDescription());
		Assert.assertEquals("red", reloaded.getColor());
		Assert.assertNotNull(reloaded.getDueDate());
		Assert.assertEquals("achieved", item.get("milestoneStatus"));
		assertThat(result.text()).startsWith("Status changed: Subject (key " + milestone.getKey() + ", achieved");
		assertThat(actions(project, participant)).contains(Action.milestoneContentUpdate);
		assertNewsAfter(project, before);

		Map<String,Object> reopened = item(invoke(context(participant), Map.of("action", "set_status", "milestoneKey", milestone.getKey(), "milestoneStatus", "open")));
		Assert.assertEquals("open", reopened.get("milestoneStatus"));

		McpToolResult missing = invoke(context(participant), Map.of("action", "set_status", "milestoneKey", milestone.getKey()));
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("milestoneStatus");
		McpToolResult unknown = invoke(context(participant), Map.of("action", "set_status", "milestoneKey", milestone.getKey(), "milestoneStatus", "done"));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).contains("Unknown milestoneStatus 'done'");
	}

	// delete

	@Test
	public void delete_creatorAndAdminRoles() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-37");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-38");
		Identity otherParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-39");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		projectService.updateMember(owner, new ProjectBCFactory(), project, otherParticipant, Set.of(ProjectRole.participant), null);
		ProjMilestone byParticipant = createMilestone(participant, project, "By participant", null, null, null);
		ProjMilestone byOwner = createMilestone(owner, project, "By owner", null, null, null);
		dbInstance.commitAndCloseSession();

		// a participant who is not the creator cannot delete
		try {
			invoke(context(otherParticipant), Map.of("action", "delete", "milestoneKey", byParticipant.getKey()));
			Assert.fail("A participant cannot delete the milestone of another creator");
		} catch (McpPermissionException e) {
			Assert.assertEquals("delete the milestone", e.getPermission());
		}

		Date before = setPublisherNewsInPast(project);
		// the creator deletes the own milestone
		Map<String,Object> item = item(invoke(context(participant), Map.of("action", "delete", "milestoneKey", byParticipant.getKey())));
		Assert.assertEquals("deleted", item.get("status"));
		// the owner deletes any milestone
		invoke(context(owner), Map.of("action", "delete", "milestoneKey", byOwner.getKey()));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(ProjectStatus.deleted, projectService.getMilestone(byParticipant).getArtefact().getStatus());
		Assert.assertEquals(ProjectStatus.deleted, projectService.getMilestone(byOwner).getArtefact().getStatus());
		assertThat(actions(project, participant)).contains(Action.milestoneStatusDelete);
		assertNewsAfter(project, before);

		// deleting twice is refused with a state error
		McpToolResult twice = invoke(context(owner), Map.of("action", "delete", "milestoneKey", byOwner.getKey()));
		Assert.assertTrue(twice.error());
		Assert.assertEquals("The milestone " + byOwner.getKey() + " is already deleted.", twice.text());
	}

	/**
	 * V3: the tags of a milestone are de-duplicated and matched to the tags of the project.
	 */
	@Test
	public void tags_deduplicatedAndMatched() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mm-140");
		ProjProject project = createProject(owner);
		ProjMilestone tagged = createMilestone(owner, project, "Tagged", null, null, null);
		projectService.updateTags(owner, tagged.getArtefact(), List.of("Phase 1"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "subject", "Next",
				"tags", List.of("phase 1", "PHASE 1", "Launch", "launch")));
		Assert.assertFalse(result.text(), result.error());
		assertThat(result.text()).contains("tags Launch, Phase 1");
	}

	@Test
	public void delete_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-40");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-41");
		ProjProject project = createProject(owner);
		ProjMilestone milestone = createMilestone(owner, project, "Subject", null, null, null);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "delete", "milestoneKey", milestone.getKey()));
			Assert.fail("A non-member cannot delete");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the milestone", e.getPermission());
		}
		Assert.assertEquals(ProjectStatus.active, projectService.getMilestone(milestone).getArtefact().getStatus());
	}

	@Test
	public void boundedInputs() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-42");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();
		long milestonesBefore = countMilestones(project);

		McpToolResult subject = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "x".repeat(MilestoneToolSupport.SUBJECT_MAX_LENGTH + 1)));
		Assert.assertTrue(subject.error());
		assertThat(subject.text()).contains("subject").contains(String.valueOf(MilestoneToolSupport.SUBJECT_MAX_LENGTH));

		McpToolResult description = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "subject", "x",
				"description", "x".repeat(MilestoneToolSupport.DESCRIPTION_MAX_LENGTH + 1)));
		Assert.assertTrue(description.error());
		assertThat(description.text()).contains("description").contains(String.valueOf(MilestoneToolSupport.DESCRIPTION_MAX_LENGTH));

		McpToolResult tags = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "subject", "x",
				"tags", List.of("y".repeat(ProjectToolSupport.TAG_MAX_LENGTH + 1))));
		Assert.assertTrue(tags.error());
		assertThat(tags.text()).contains("tags");

		List<String> tooManyTags = java.util.stream.IntStream.range(0, ProjectToolSupport.TAGS_MAX_ITEMS + 1).mapToObj(i -> "t" + i).toList();
		McpToolResult manyTags = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "subject", "x",
				"tags", tooManyTags));
		Assert.assertTrue(manyTags.error());
		assertThat(manyTags.text()).contains("tags").contains(String.valueOf(ProjectToolSupport.TAGS_MAX_ITEMS));

		dbInstance.commitAndCloseSession();
		Assert.assertEquals(milestonesBefore, countMilestones(project));
	}

	/**
	 * The reason milestone_modify has no members and links: the project
	 * service has no member and no reference activity for a milestone.
	 */
	@Test
	public void core_milestoneHasNoMemberAndReferenceActivity() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-43");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-mi-44");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjMilestone milestone = createMilestone(owner, project, "Subject", null, null, null);
		ProjNote note = projectService.createNote(owner, project);
		projectService.updateNote(owner, note, UUID.randomUUID().toString(), "Note", "x");
		dbInstance.commitAndCloseSession();

		try {
			projectService.linkArtefacts(owner, note.getArtefact(), milestone.getArtefact());
			Assert.fail("The service has no reference activity for milestones");
		} catch (AssertException e) {
			assertThat(e.getMessage()).contains(ProjMilestone.TYPE);
		} finally {
			dbInstance.rollbackAndCloseSession();
		}
		try {
			projectService.updateMembers(owner, new ProjectBCFactory(), milestone.getArtefact(), List.<IdentityRef>of(owner, participant));
			Assert.fail("The service has no member activity for milestones");
		} catch (AssertException e) {
			assertThat(e.getMessage()).contains(ProjMilestone.TYPE);
		} finally {
			dbInstance.rollbackAndCloseSession();
		}
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private ProjMilestone createMilestone(Identity doer, ProjProject project, String subject, String description, Date dueDate, String color) {
		ProjMilestone milestone = projectService.createMilestone(doer, new ProjectBCFactory(), project);
		projectService.updateMilestone(doer, new ProjectBCFactory(), milestone, ProjMilestoneStatus.open, dueDate, subject, description, color);
		return milestone;
	}

	private long countMilestones(ProjProject project) {
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setProject(project);
		return projectService.getMilestones(searchParams).size();
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
		assertThat(MilestoneModifyToolProvider.RESULT_FIELDS).containsAll(minimal.keySet());
		assertThat(result.text()).endsWith("Call milestone_query get for the full item.");
		Long key = (Long)minimal.get("key");
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setMilestones(List.of(() -> key));
		return milestoneSupport.milestoneItem(projectService.getMilestoneInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0), true);
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
