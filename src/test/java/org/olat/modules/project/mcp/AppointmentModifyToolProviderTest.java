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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
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
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactInfoParams;
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
 * Integration tests of the tool appointment_modify: the rights per action
 * and project role, the service sequences, the dates and the recurrence
 * rule, the metadata round trips and the side effects (activity log,
 * publisher news).
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AppointmentModifyToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(AppointmentModifyToolProvider.NAME), null, List.of("test"));
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
	private AppointmentModifyToolProvider sut;
	@Autowired
	private ProjectToolSupport support;
	@Autowired
	private CalendarManager calendarManager;

	@Test
	public void definition() {
		Assert.assertEquals("appointment_modify", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.WRITE, sut.getScope());
		Assert.assertTrue(sut.getDefinition().destructive());
		Assert.assertFalse(sut.getDefinition().idempotent());
		assertThat(sut.getDefinition().description()).contains("appointment_query").contains("project_query")
				.contains("replace").contains("clears")
				.contains("milestones cannot be linked").doesNotContain("appointments, milestones, decisions");
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Map<String,Object> create = new HashMap<>();
		create.put("action", "create");
		create.put("projectKey", 3);
		create.put("subject", "Kickoff");
		create.put("start", "2030-03-05T10:00");
		create.put("end", "2030-03-05T11:00+01:00");
		create.put("allDay", false);
		create.put("location", "Room 1");
		create.put("description", "Agenda");
		create.put("color", "red");
		create.put("recurrence", "weekly");
		create.put("recurrenceEnd", "2030-04-30");
		create.put("tags", List.of("a"));
		create.put("members", List.of("jdoe"));
		create.put("links", List.of(Map.of("type", "note", "key", 5)));
		Assert.assertTrue(VALIDATOR.validate(schema, create).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "update", "appointmentKey", 1, "color", "", "links", List.of())).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "delete", "appointmentKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("appointmentKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "move", "appointmentKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "appointmentKey", 1, "color", "pink")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "appointmentKey", 1, "recurrence", "hourly")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "appointmentKey", 1, "links", List.of(Map.of("type", "course", "key", 5)))).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "appointmentKey", 1, "status", "deleted")).valid());
	}

	@Test
	public void invalidActions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-101");

		McpToolResult missing = invoke(context(owner), Map.of());
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("action is required");

		McpToolResult unknown = invoke(context(owner), Map.of("action", "move"));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).contains("Unknown action move");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "update", "subject", "x"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("appointmentKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownKey = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "delete", "appointmentKey", Long.valueOf(-9))));
		Assert.assertEquals("appointment with key -9", unknownKey.getObjectDescription());
		Assert.assertEquals("view the appointment", unknownKey.getPermission());

		McpToolResult noProject = invoke(context(owner), Map.of("action", "create", "subject", "x", "start", "2030-03-05T10:00"));
		Assert.assertTrue(noProject.error());
		assertThat(noProject.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "create", "projectKey", Long.valueOf(-9),
				"subject", "x", "start", "2030-03-05T10:00")));
		Assert.assertEquals("project with key -9", unknownProject.getObjectDescription());
		Assert.assertEquals("create appointments", unknownProject.getPermission());
	}

	// create

	@Test
	public void create_minimal() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-102");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		McpToolResult result = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Kickoff", "start", "2030-03-05T10:00"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjAppointment appointment = projectService.getAppointment(() -> (Long)item.get("key"));
		Assert.assertEquals("Kickoff", appointment.getSubject());
		Assert.assertEquals(toDate(at(2030, 3, 5, 10)), new Date(appointment.getStartDate().getTime()));
		// the end defaults to one hour after the start
		Assert.assertEquals(toDate(at(2030, 3, 5, 11)), new Date(appointment.getEndDate().getTime()));
		Assert.assertFalse(appointment.isAllDay());
		Assert.assertNull(appointment.getRecurrenceRule());
		Assert.assertNull(appointment.getLocation());
		Assert.assertNull(appointment.getColor());
		Assert.assertEquals(owner, appointment.getArtefact().getCreator());
		Assert.assertEquals(iso(at(2030, 3, 5, 10)), item.get("start"));
		Assert.assertEquals(iso(at(2030, 3, 5, 11)), item.get("end"));
		Assert.assertEquals("none", item.get("recurrence"));
		Assert.assertFalse(item.containsKey("location"));
		Assert.assertFalse(item.containsKey("description"));
		Assert.assertEquals(List.of(), item.get("tags"));
		Assert.assertEquals(List.of(), item.get("links"));
		// the creator is the involved member by default, as the UI pre-selects
		assertThat(persons(item, "members")).containsExactly(username(owner));
		assertThat(result.text()).startsWith("Created: Kickoff (key " + appointment.getKey());
		assertThat(actions(project, owner)).contains(Action.appointmentCreate, Action.appointmentContentUpdate);
		assertNewsAfter(project, before);
		// one create and one content update, the sequence of the UI (create with the dates, then save the form)
		Assert.assertEquals(1, count(appointment.getArtefact(), Action.appointmentCreate));
		Assert.assertEquals(1, count(appointment.getArtefact(), Action.appointmentContentUpdate));
		// the same start again, the stored dates are Timestamps: no further activity
		invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(), "start", "2030-03-05T10:00"));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, count(appointment.getArtefact(), Action.appointmentContentUpdate));
	}

	@Test
	public void create_allDay() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-103");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Retreat", "start", "2030-03-05", "allDay", true)));
		Assert.assertEquals(Boolean.TRUE, item.get("allDay"));
		// the end defaults to the same day
		Assert.assertEquals(iso(at(2030, 3, 5, 0)), item.get("start"));
		Assert.assertEquals(iso(at(2030, 3, 5, 0)), item.get("end"));

		// a date-time is cut to the day
		Map<String,Object> twoDays = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Fair", "start", "2030-03-05T14:30", "end", "2030-03-06", "allDay", true)));
		Assert.assertEquals(iso(at(2030, 3, 5, 0)), twoDays.get("start"));
		Assert.assertEquals(iso(at(2030, 3, 6, 0)), twoDays.get("end"));

		// a bare date needs allDay
		McpToolResult bareDate = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Fair", "start", "2030-03-05"));
		Assert.assertTrue(bareDate.error());
		assertThat(bareDate.text()).contains("start").contains("ISO date-time");
	}

	@Test
	public void create_withMetadataAndLinks() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-104");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-105");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote note = createNote(owner, project, "Minutes");
		ProjToDo toDo = createToDo(owner, project, "Follow up");
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("projectKey", project.getKey());
		arguments.put("subject", "Kickoff");
		arguments.put("start", "2030-03-05T10:00:00Z");
		arguments.put("end", "2030-03-05T12:30:00Z");
		arguments.put("location", "Room 1");
		arguments.put("description", "Agenda\nline two");
		arguments.put("color", "red");
		arguments.put("tags", List.of("meeting", "kickoff"));
		arguments.put("members", List.of(username(owner), participant.getUser().getEmail()));
		arguments.put("links", List.of(Map.of("type", "note", "key", note.getKey()), Map.of("type", "toDo", "key", toDo.getToDoTask().getKey())));
		McpToolResult result = invoke(context(participant), arguments);
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		Long appointmentKey = (Long)item.get("key");
		ProjAppointment appointment = projectService.getAppointment(() -> appointmentKey);
		Assert.assertEquals("Kickoff", appointment.getSubject());
		Assert.assertEquals("Room 1", appointment.getLocation());
		Assert.assertEquals("Agenda\nline two", appointment.getDescription());
		Assert.assertEquals("red", appointment.getColor());
		Assert.assertEquals(Date.from(Instant.parse("2030-03-05T10:00:00Z")), new Date(appointment.getStartDate().getTime()));
		Assert.assertEquals(Date.from(Instant.parse("2030-03-05T12:30:00Z")), new Date(appointment.getEndDate().getTime()));
		Assert.assertEquals(participant, appointment.getArtefact().getCreator());
		Assert.assertEquals("red", item.get("color"));
		Assert.assertEquals(List.of("kickoff", "meeting"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey(), "note:" + note.getKey());
		assertThat(projectService.getLinkedArtefacts(appointment.getArtefact())).extracting(ProjArtefact::getKey)
				.containsExactlyInAnyOrder(note.getArtefact().getKey(), toDo.getArtefact().getKey());
		assertThat(actions(project, participant)).contains(Action.appointmentCreate, Action.appointmentContentUpdate,
				Action.appointmentTagsUpdate, Action.appointmentMemberAdd, Action.appointmentReferenceAdd);
		assertThat(result.text()).startsWith("Created: Kickoff (key " + appointmentKey).contains("tags kickoff, meeting");
	}

	@Test
	public void create_recurring() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-106");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		Map<String,Object> weekly = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Jour fixe", "start", "2030-03-04T09:00", "recurrence", "weekly", "recurrenceEnd", "2030-04-29")));
		Assert.assertEquals("weekly", weekly.get("recurrence"));
		Assert.assertEquals("2030-04-29", weekly.get("recurrenceEnd"));
		assertThat(weekly.get("recurrenceRule").toString()).contains("FREQ=WEEKLY").contains("UNTIL=20300429");
		ProjAppointment appointment = projectService.getAppointment(() -> (Long)weekly.get("key"));
		Assert.assertEquals(weekly.get("recurrenceRule"), appointment.getRecurrenceRule());

		Map<String,Object> workdaily = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Standup", "start", "2030-03-04T09:00", "recurrence", "workdaily", "recurrenceEnd", "2030-03-29")));
		Assert.assertEquals("workdaily", workdaily.get("recurrence"));
		assertThat(workdaily.get("recurrenceRule").toString()).contains("FREQ=DAILY").contains("BYDAY=MO,TU,WE,TH,FR");

		Map<String,Object> biweekly = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Review", "start", "2030-03-04", "allDay", true, "recurrence", "biweekly", "recurrenceEnd", "2030-06-30")));
		Assert.assertEquals("biweekly", biweekly.get("recurrence"));
		Assert.assertEquals("2030-06-30", biweekly.get("recurrenceEnd"));
		assertThat(biweekly.get("recurrenceRule").toString()).contains("FREQ=WEEKLY").contains("INTERVAL=2").contains("UNTIL=20300630");

		Map<String,Object> none = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"subject", "Once", "start", "2030-03-04T09:00", "recurrence", "none")));
		Assert.assertEquals("none", none.get("recurrence"));
		Assert.assertFalse(none.containsKey("recurrenceRule"));
	}

	@Test
	public void create_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-107");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-108");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-109");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		dbInstance.commitAndCloseSession();
		Map<String,Object> arguments = Map.of("action", "create", "projectKey", project.getKey(), "subject", "x", "start", "2030-03-05T10:00");

		// a client may view but not create artefacts
		try {
			invoke(context(client), arguments);
			Assert.fail("A client cannot create appointments");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create appointments", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
		try {
			invoke(context(stranger), arguments);
			Assert.fail("A non-member cannot create appointments");
		} catch (McpPermissionException e) {
			Assert.assertEquals("create appointments", e.getPermission());
		}

		// a deleted project is read only, V19: a state error
		projectService.setStatusDeleted(owner, new ProjectBCFactory(), project);
		dbInstance.commitAndCloseSession();
		McpToolResult deletedProject = invoke(context(owner), arguments);
		Assert.assertTrue(deletedProject.error());
		assertThat(deletedProject.text()).startsWith("The project " + project.getKey() + " is deleted");
		Assert.assertEquals(0, countAppointments(project));
	}

	@Test
	public void create_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-110");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-111");
		ProjProject project = createProject(owner);
		ProjProject otherProject = createProject(stranger);
		ProjNote foreignNote = createNote(stranger, otherProject, "Foreign");
		ProjNote deletedNote = createNote(owner, project, "Deleted");
		projectService.deleteNoteSoftly(owner, deletedNote);
		dbInstance.commitAndCloseSession();
		int appointmentsBefore = countAppointments(project);

		assertError(owner, create(project, "start", "2030-03-05T10:00"), "needs a subject");
		assertError(owner, create(project, "subject", "x"), "needs the argument start");
		assertError(owner, create(project, "subject", "x", "start", "tomorrow at ten"), "start must be an ISO date-time");
		assertError(owner, create(project, "subject", "x", "start", ""), "start cannot be empty");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "end", "2030-03-05T09:00"), "is before the start");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "color", "pink"), "Unknown color pink");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "recurrence", "hourly"), "Unknown recurrence hourly");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "recurrence", "weekly"), "needs the argument recurrenceEnd");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "recurrence", "weekly", "recurrenceEnd", "2030-03-04"),
				"recurrenceEnd 2030-03-04 must be after the start");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "recurrence", "weekly", "recurrenceEnd", "end of april"),
				"recurrenceEnd must be an ISO date");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "recurrenceEnd", "2030-04-30"),
				"recurrenceEnd needs a recurrence");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "members", List.of(username(stranger))),
				username(stranger) + " is not a valid member for this appointment.");
		// a link to an artefact of another project, to a deleted note or to an unknown key gives the same message
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "links", List.of(Map.of("type", "note", "key", foreignNote.getKey()))),
				"is not an active note of this project.");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "links", List.of(Map.of("type", "note", "key", deletedNote.getKey()))),
				"is not an active note of this project.");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "links", List.of(Map.of("type", "appointment", "key", -1))),
				"is not an active appointment of this project.");

		// nothing was written
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(appointmentsBefore, countAppointments(project));
	}

	// update

	@Test
	public void update_onlySuppliedFields() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-112");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-113");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjAppointment appointment = createAppointment(owner, project, "Kickoff", at(2030, 3, 5, 10), at(2030, 3, 5, 12));
		ProjNote linked = createNote(owner, project, "Linked");
		projectService.updateTags(owner, appointment.getArtefact(), List.of("keep"));
		projectService.linkArtefacts(owner, appointment.getArtefact(), linked.getArtefact());
		dbInstance.commitAndCloseSession();

		// a participant who is not the creator may edit
		McpToolResult result = invoke(context(participant), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"location", "Room 2"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjAppointment reloaded = projectService.getAppointment(appointment);
		Assert.assertEquals("Kickoff", reloaded.getSubject());
		Assert.assertEquals("Room 2", reloaded.getLocation());
		Assert.assertEquals("Agenda of Kickoff", reloaded.getDescription());
		Assert.assertEquals(toDate(at(2030, 3, 5, 10)), new Date(reloaded.getStartDate().getTime()));
		Assert.assertEquals(toDate(at(2030, 3, 5, 12)), new Date(reloaded.getEndDate().getTime()));
		Assert.assertEquals(List.of("keep"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactly(username(owner));
		assertThat(links(item)).containsExactly("note:" + linked.getKey());
		assertThat(result.text()).startsWith("Updated: Kickoff (key " + appointment.getKey());
		assertThat(actions(project, participant)).contains(Action.appointmentContentUpdate);
		// ProjectServiceImpl.updateAppointment does not mark the publisher news, unlike updateNote; create and delete do

		// a new start moves the end by the same time, as the form does
		Map<String,Object> moved = item(invoke(context(participant), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"start", "2030-03-06T14:00")));
		Assert.assertEquals(iso(at(2030, 3, 6, 14)), moved.get("start"));
		Assert.assertEquals(iso(at(2030, 3, 6, 16)), moved.get("end"));
		Assert.assertEquals("Room 2", moved.get("location"));

		// only the metadata: the content is not written
		dbInstance.commitAndCloseSession();
		long contentUpdates = actions(project, participant).stream().filter(Action.appointmentContentUpdate::equals).count();
		invoke(context(participant), Map.of("action", "update", "appointmentKey", appointment.getKey(), "tags", List.of("keep", "new")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(contentUpdates, actions(project, participant).stream().filter(Action.appointmentContentUpdate::equals).count());
	}

	@Test
	public void update_replaceAndClearMetadata() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-114");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-115");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjAppointment appointment = createAppointment(owner, project, "Kickoff", at(2030, 3, 5, 10), at(2030, 3, 5, 12));
		projectService.updateAppointment(owner, new ProjectBCFactory(), appointment, toDate(at(2030, 3, 5, 10)), toDate(at(2030, 3, 5, 12)),
				"Kickoff", "Agenda", "Room 1", "red", false, null);
		ProjNote oldLink = createNote(owner, project, "Old link");
		ProjNote newLink = createNote(owner, project, "New link");
		ProjToDo toDo = createToDo(owner, project, "ToDo");
		projectService.updateTags(owner, appointment.getArtefact(), List.of("old"));
		projectService.linkArtefacts(owner, appointment.getArtefact(), oldLink.getArtefact());
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "update");
		arguments.put("appointmentKey", appointment.getKey());
		arguments.put("description", "");
		arguments.put("location", "");
		arguments.put("color", "");
		arguments.put("tags", List.of("new"));
		arguments.put("members", List.of(username(participant)));
		arguments.put("links", List.of(Map.of("type", "note", "key", newLink.getKey()), Map.of("type", "toDo", "key", toDo.getToDoTask().getKey())));
		Map<String,Object> item = item(invoke(context(owner), arguments));
		dbInstance.commitAndCloseSession();

		ProjAppointment reloaded = projectService.getAppointment(appointment);
		Assert.assertEquals("Kickoff", reloaded.getSubject());
		Assert.assertNull(reloaded.getDescription());
		Assert.assertNull(reloaded.getLocation());
		Assert.assertNull(reloaded.getColor());
		Assert.assertFalse(item.containsKey("description"));
		Assert.assertFalse(item.containsKey("location"));
		Assert.assertFalse(item.containsKey("color"));
		Assert.assertEquals(List.of("new"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactly(username(participant));
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey(), "note:" + newLink.getKey());
		assertThat(projectService.getLinkedArtefacts(appointment.getArtefact())).extracting(ProjArtefact::getKey)
				.containsExactlyInAnyOrder(newLink.getArtefact().getKey(), toDo.getArtefact().getKey());
		assertThat(actions(project, owner)).contains(Action.appointmentTagsUpdate, Action.appointmentMemberAdd,
				Action.appointmentMemberRemove, Action.appointmentReferenceAdd, Action.appointmentReferenceRemove);

		// empty lists clear tags and links, the members stay
		Map<String,Object> cleared = item(invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"tags", List.of(), "links", List.of())));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(List.of(), cleared.get("tags"));
		assertThat(persons(cleared, "members")).containsExactly(username(participant));
		Assert.assertEquals(List.of(), cleared.get("links"));
		assertThat(projectService.getLinkedArtefacts(appointment.getArtefact())).isEmpty();

		// an empty member list is refused, the UI requires at least one involved member
		McpToolResult noMembers = invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(), "members", List.of()));
		Assert.assertTrue(noMembers.error());
		assertThat(noMembers.text()).contains("at least one project member");
		Assert.assertFalse(VALIDATOR.validate(sut.getDefinition().inputSchema(), Map.of("action", "update", "appointmentKey", 1, "members", List.of())).valid());

		// the subject cannot be cleared
		McpToolResult empty = invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(), "subject", ""));
		Assert.assertTrue(empty.error());
		assertThat(empty.text()).contains("needs a subject");
		// the end cannot move before the start
		McpToolResult endBeforeStart = invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"end", "2030-03-05T09:00"));
		Assert.assertTrue(endBeforeStart.error());
		assertThat(endBeforeStart.text()).contains("is before the start");
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("Kickoff", projectService.getAppointment(appointment).getSubject());
		Assert.assertEquals(toDate(at(2030, 3, 5, 12)), new Date(projectService.getAppointment(appointment).getEndDate().getTime()));
	}

	@Test
	public void update_recurrence() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-116");
		ProjProject project = createProject(owner);
		ProjAppointment appointment = createAppointment(owner, project, "Jour fixe", at(2030, 3, 4, 9), at(2030, 3, 4, 10));
		dbInstance.commitAndCloseSession();

		// a recurrence needs its end
		McpToolResult noEnd = invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(), "recurrence", "weekly"));
		Assert.assertTrue(noEnd.error());
		assertThat(noEnd.text()).contains("needs the argument recurrenceEnd");

		Map<String,Object> weekly = item(invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"recurrence", "weekly", "recurrenceEnd", "2030-04-29")));
		Assert.assertEquals("weekly", weekly.get("recurrence"));
		Assert.assertEquals("2030-04-29", weekly.get("recurrenceEnd"));

		// the end alone keeps the recurrence, the recurrence alone keeps the end
		Map<String,Object> longer = item(invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"recurrenceEnd", "2030-06-24")));
		Assert.assertEquals("weekly", longer.get("recurrence"));
		Assert.assertEquals("2030-06-24", longer.get("recurrenceEnd"));
		Map<String,Object> monthly = item(invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"recurrence", "monthly")));
		Assert.assertEquals("monthly", monthly.get("recurrence"));
		Assert.assertEquals("2030-06-24", monthly.get("recurrenceEnd"));

		// a change of allDay rebuilds the rule with the date form of all day
		Map<String,Object> allDay = item(invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"allDay", true)));
		Assert.assertEquals(Boolean.TRUE, allDay.get("allDay"));
		Assert.assertEquals(iso(at(2030, 3, 4, 0)), allDay.get("start"));
		Assert.assertEquals("monthly", allDay.get("recurrence"));
		Assert.assertEquals("2030-06-24", allDay.get("recurrenceEnd"));
		assertThat(allDay.get("recurrenceRule").toString()).contains("UNTIL=20300624").doesNotContain("UNTIL=20300624T");

		// none ends the repetition
		Map<String,Object> none = item(invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"recurrence", "none")));
		Assert.assertEquals("none", none.get("recurrence"));
		Assert.assertFalse(none.containsKey("recurrenceRule"));
		Assert.assertFalse(none.containsKey("recurrenceEnd"));
		dbInstance.commitAndCloseSession();
		Assert.assertNull(projectService.getAppointment(appointment).getRecurrenceRule());

		// a recurrence end without recurrence
		McpToolResult endOnly = invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(),
				"recurrenceEnd", "2030-06-24"));
		Assert.assertTrue(endOnly.error());
		assertThat(endOnly.text()).contains("recurrenceEnd needs a recurrence");
	}

	@Test
	public void update_delete_occurrenceRefused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-117");
		ProjProject project = createProject(owner);
		ProjAppointment series = createAppointment(owner, project, "Weekly", at(2030, 3, 4, 9), at(2030, 3, 4, 10));
		Map<String,Object> weekly = item(invoke(context(owner), Map.of("action", "update", "appointmentKey", series.getKey(),
				"recurrence", "weekly", "recurrenceEnd", "2030-04-29")));
		Assert.assertEquals("weekly", weekly.get("recurrence"));
		ProjAppointment occurrence = createOccurrence(owner, series, at(2030, 3, 11, 9));
		dbInstance.commitAndCloseSession();

		McpToolResult update = invoke(context(owner), Map.of("action", "update", "appointmentKey", occurrence.getKey(), "subject", "x"));
		Assert.assertTrue(update.error());
		assertThat(update.text()).contains("single occurrence of a recurring appointment").contains("Edit it in OpenOlat")
				.contains("/Appointment/" + occurrence.getKey());
		McpToolResult delete = invoke(context(owner), Map.of("action", "delete", "appointmentKey", occurrence.getKey()));
		Assert.assertTrue(delete.error());
		assertThat(delete.text()).contains("Delete it in OpenOlat");
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("Weekly", projectService.getAppointment(occurrence).getSubject());
		Assert.assertEquals(ProjectStatus.active, projectService.getAppointment(occurrence).getArtefact().getStatus());

		// the series is deleted with its changed occurrences
		Map<String,Object> deleted = item(invoke(context(owner), Map.of("action", "delete", "appointmentKey", series.getKey())));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("deleted", deleted.get("status"));
		Assert.assertEquals(ProjectStatus.deleted, projectService.getAppointment(series).getArtefact().getStatus());
		Assert.assertEquals(ProjectStatus.deleted, projectService.getAppointment(occurrence).getArtefact().getStatus());
	}

	@Test
	public void update_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-118");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-119");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-120");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		ProjAppointment appointment = createAppointment(owner, project, "Kickoff", at(2030, 3, 5, 10), at(2030, 3, 5, 11));
		ProjAppointment deleted = createAppointment(owner, project, "Deleted", at(2030, 3, 5, 10), at(2030, 3, 5, 11));
		projectService.deleteAppointmentSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "update", "appointmentKey", appointment.getKey(), "subject", "x"));
			Assert.fail("A non-member cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the appointment", e.getPermission());
		}
		try {
			invoke(context(client), Map.of("action", "update", "appointmentKey", appointment.getKey(), "subject", "x"));
			Assert.fail("A client cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("edit the appointment", e.getPermission());
			Assert.assertEquals("appointment with key " + appointment.getKey(), e.getObjectDescription());
		}
		McpToolResult deletedAppointment = invoke(context(owner), Map.of("action", "update", "appointmentKey", deleted.getKey(), "subject", "x"));
		Assert.assertTrue(deletedAppointment.error());
		Assert.assertEquals("The appointment " + deleted.getKey() + " is already deleted.", deletedAppointment.text());
		Assert.assertEquals("Kickoff", projectService.getAppointment(appointment).getSubject());
	}

	// delete

	@Test
	public void delete_creatorAndAdminRoles() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-121");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-122");
		Identity otherParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-123");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		projectService.updateMember(owner, new ProjectBCFactory(), project, otherParticipant, Set.of(ProjectRole.participant), null);
		ProjAppointment byParticipant = createAppointment(participant, project, "By participant", at(2030, 3, 5, 10), at(2030, 3, 5, 11));
		ProjAppointment byOwner = createAppointment(owner, project, "By owner", at(2030, 3, 5, 10), at(2030, 3, 5, 11));
		dbInstance.commitAndCloseSession();

		// a participant who is not the creator cannot delete
		try {
			invoke(context(otherParticipant), Map.of("action", "delete", "appointmentKey", byParticipant.getKey()));
			Assert.fail("A participant cannot delete the appointment of another creator");
		} catch (McpPermissionException e) {
			Assert.assertEquals("delete the appointment", e.getPermission());
		}

		Date before = setPublisherNewsInPast(project);
		// the creator deletes the own appointment
		Map<String,Object> item = item(invoke(context(participant), Map.of("action", "delete", "appointmentKey", byParticipant.getKey())));
		Assert.assertEquals("deleted", item.get("status"));
		// the owner deletes any appointment
		invoke(context(owner), Map.of("action", "delete", "appointmentKey", byOwner.getKey()));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(ProjectStatus.deleted, projectService.getAppointment(byParticipant).getArtefact().getStatus());
		Assert.assertEquals(ProjectStatus.deleted, projectService.getAppointment(byOwner).getArtefact().getStatus());
		assertThat(actions(project, participant)).contains(Action.appointmentStatusDelete);
		assertNewsAfter(project, before);

		// deleting twice is refused with a state error
		McpToolResult twice = invoke(context(owner), Map.of("action", "delete", "appointmentKey", byOwner.getKey()));
		Assert.assertTrue(twice.error());
		Assert.assertEquals("The appointment " + byOwner.getKey() + " is already deleted.", twice.text());
	}

	/**
	 * V1 and V10: an appointment of a template has no dates and no involved
	 * members, as ProjAppointmentContentEditForm and ProjAppointmentEditController
	 * show it. create needs no start there, a recurrence needs no start either.
	 */
	@Test
	public void template_noDatesNoMembers() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-140");
		ProjProject template = createProject(owner);
		template = projectService.updateProject(owner, new ProjectBCFactory(), template, null, template.getTitle(), null, null, true, false);
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(owner), create(template, "subject", "Kickoff", "location", "Room 1")));
		dbInstance.commitAndCloseSession();
		ProjAppointment appointment = projectService.getAppointment(() -> (Long)item.get("key"));
		Assert.assertNull(appointment.getStartDate());
		Assert.assertNull(appointment.getEndDate());
		Assert.assertFalse(item.containsKey("start"));

		assertError(owner, create(template, "subject", "x", "start", "2030-03-05T10:00"), "template project has no dates");
		assertError(owner, create(template, "subject", "x", "end", "2030-03-05T10:00"), "template project has no dates");
		assertError(owner, Map.of("action", "update", "appointmentKey", appointment.getKey(), "start", "2030-03-05T10:00"),
				"template project has no dates");
		assertError(owner, create(template, "subject", "x", "members", List.of(username(owner))), "template project have no involved members");
		assertError(owner, Map.of("action", "update", "appointmentKey", appointment.getKey(), "members", List.of(username(owner))),
				"template project have no involved members");

		// a series without dates, as the form allows it in a template
		Map<String,Object> series = item(invoke(context(owner), create(template, "subject", "Jour fixe", "recurrence", "weekly", "recurrenceEnd", "2030-04-29")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("weekly", series.get("recurrence"));
		Assert.assertNull(projectService.getAppointment(() -> (Long)series.get("key")).getStartDate());

		// other fields of an appointment without dates can be changed
		item(invoke(context(owner), Map.of("action", "update", "appointmentKey", appointment.getKey(), "subject", "Kickoff v2", "allDay", true)));
		dbInstance.commitAndCloseSession();
		ProjAppointment reloaded = projectService.getAppointment(appointment);
		Assert.assertEquals("Kickoff v2", reloaded.getSubject());
		Assert.assertNull(reloaded.getStartDate());
	}

	/**
	 * V18: a series that ends on its start day is refused unless it starts
	 * at midnight, the comparison of ProjAppointmentContentEditForm.
	 */
	@Test
	public void create_recurrenceEndOnStartDay() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-141");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "recurrence", "weekly", "recurrenceEnd", "2030-03-05"),
				"recurrenceEnd 2030-03-05 must be after the start");
		// an all day appointment starts at midnight, the same day is accepted as in the form
		Map<String,Object> allDay = item(invoke(context(owner), create(project, "subject", "All day", "start", "2030-03-05", "allDay", true,
				"recurrence", "daily", "recurrenceEnd", "2030-03-05")));
		Assert.assertEquals("2030-03-05", allDay.get("recurrenceEnd"));
		// the next day is accepted
		item(invoke(context(owner), create(project, "subject", "x", "start", "2030-03-05T10:00", "recurrence", "daily", "recurrenceEnd", "2030-03-06")));
	}

	@Test
	public void delete_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-124");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-125");
		ProjProject project = createProject(owner);
		ProjAppointment appointment = createAppointment(owner, project, "Kickoff", at(2030, 3, 5, 10), at(2030, 3, 5, 11));
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "delete", "appointmentKey", appointment.getKey()));
			Assert.fail("A non-member cannot delete");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the appointment", e.getPermission());
		}
		Assert.assertEquals(ProjectStatus.active, projectService.getAppointment(appointment).getArtefact().getStatus());
	}

	@Test
	public void boundedInputs() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-126");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		assertError(owner, create(project, "subject", "x".repeat(AppointmentModifyToolProvider.SUBJECT_MAX_LENGTH + 1), "start", "2030-03-05T10:00"),
				String.valueOf(AppointmentModifyToolProvider.SUBJECT_MAX_LENGTH));
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00",
				"location", "x".repeat(AppointmentModifyToolProvider.LOCATION_MAX_LENGTH + 1)), "location");
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00",
				"description", "x".repeat(AppointmentModifyToolProvider.DESCRIPTION_MAX_LENGTH + 1)),
				String.valueOf(AppointmentModifyToolProvider.DESCRIPTION_MAX_LENGTH));
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00",
				"tags", List.of("y".repeat(ProjectToolSupport.TAG_MAX_LENGTH + 1))), "tags");
		List<Map<String,Object>> tooManyLinks = IntStream.range(0, ProjectToolSupport.LINKS_MAX_ITEMS + 1)
				.mapToObj(i -> Map.<String,Object>of("type", "note", "key", i)).toList();
		assertError(owner, create(project, "subject", "x", "start", "2030-03-05T10:00", "links", tooManyLinks),
				String.valueOf(ProjectToolSupport.LINKS_MAX_ITEMS));
		Assert.assertEquals(0, countAppointments(project));
	}

	private void assertError(Identity doer, Map<String,Object> arguments, String expected) {
		McpToolResult result = invoke(context(doer), arguments);
		Assert.assertTrue(result.text(), result.error());
		assertThat(result.text()).contains(expected);
	}

	private static Map<String,Object> create(ProjProject project, Object... nameValues) {
		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("projectKey", project.getKey());
		for(int i=0; i<nameValues.length; i+=2) {
			arguments.put((String)nameValues[i], nameValues[i + 1]);
		}
		return arguments;
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private ProjAppointment createAppointment(Identity doer, ProjProject project, String subject, LocalDateTime start, LocalDateTime end) {
		ProjAppointment appointment = projectService.createAppointment(doer, new ProjectBCFactory(), project, toDate(start), toDate(end));
		projectService.updateAppointment(doer, new ProjectBCFactory(), appointment, toDate(start), toDate(end), subject,
				"Agenda of " + subject, "Room 1", null, false, null);
		return appointment;
	}

	private ProjAppointment createOccurrence(Identity doer, ProjAppointment series, LocalDateTime start) {
		String recurrenceId = CalendarUtils.formatRecurrenceDate(start.atZone(ZoneId.systemDefault()), false);
		return projectService.createAppointmentOcurrence(doer, new ProjectBCFactory(), series.getIdentifier(), recurrenceId,
				toDate(start), toDate(start.plusHours(1)));
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

	private int countAppointments(ProjProject project) {
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setProject(project);
		return projectService.getAppointments(searchParams).size();
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

	private static LocalDateTime at(int year, int month, int day, int hour) {
		return LocalDateTime.of(year, month, day, hour, 0);
	}

	private static Date toDate(LocalDateTime dateTime) {
		return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private static String iso(LocalDateTime dateTime) {
		return McpToolSupport.iso(toDate(dateTime));
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
		assertThat(AppointmentModifyToolProvider.RESULT_FIELDS).containsAll(minimal.keySet());
		assertThat(result.text()).endsWith("Call appointment_query get for the full item.");
		Long key = (Long)minimal.get("key");
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setAppointments(List.of(() -> key));
		return AppointmentToolSupport.appointmentItem(support, calendarManager,
				projectService.getAppointmentInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0), true);
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
