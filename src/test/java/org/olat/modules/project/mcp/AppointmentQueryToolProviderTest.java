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

import java.time.LocalDate;
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
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
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
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjAppointment;
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
 * Integration tests of the tool appointment_query: the list of a project in
 * a time window with its filters, the series listed once, the appointment
 * with metadata and links, the rights.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AppointmentQueryToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(AppointmentQueryToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private AppointmentQueryToolProvider sut;

	@Test
	public void definition() {
		Assert.assertEquals("appointment_query", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.READ, sut.getScope());
		assertThat(sut.getDefinition().description()).contains("project_query").contains("appointment_modify")
				.contains("A recurring appointment is listed once with its rule");
		Assert.assertFalse(sut.getDefinition().destructive());
		Assert.assertTrue(sut.getDefinition().idempotent());
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("projectKey", 12)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "list", "projectKey", 12, "from", "2026-10-01", "to", "2026-10-31",
				"datesOnly", false, "status", "all", "createdByMe", true, "limit", 200)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "get", "appointmentKey", 3)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "status", "archived")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "limit", 201)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "createdAfter", "2026-10-01")).valid());
	}

	@Test
	public void list_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-1");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult noKey = invoke(context(owner), Map.of());
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("projectKey", Long.valueOf(-5))));
		Assert.assertEquals("project with key -5", unknownProject.getObjectDescription());
		Assert.assertEquals("view the appointments", unknownProject.getPermission());

		McpToolResult badStatus = invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "archived"));
		Assert.assertTrue(badStatus.error());
		assertThat(badStatus.text()).contains("Unknown status 'archived'");

		McpToolResult badDate = invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "tomorrow"));
		Assert.assertTrue(badDate.error());
		assertThat(badDate.text()).contains("from");

		McpToolResult toBeforeFrom = invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "2030-03-10", "to", "2030-03-01"));
		Assert.assertTrue(toBeforeFrom.error());
		assertThat(toBeforeFrom.text()).contains("before from");

		McpToolResult tooLong = invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "2030-01-01", "to", "2031-01-03"));
		Assert.assertTrue(tooLong.error());
		assertThat(tooLong.text()).contains("366 days");
		// 366 days is the maximum
		Assert.assertFalse(invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "2030-01-01", "to", "2031-01-02")).error());

		McpToolResult unknownAction = invoke(context(owner), Map.of("action", "search"));
		Assert.assertTrue(unknownAction.error());
		assertThat(unknownAction.text()).contains("Unknown action search");
	}

	@Test
	public void list_windowAndSorting() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-3");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjAppointment later = createAppointment(owner, project, "Later", at(2030, 3, 5, 10), at(2030, 3, 5, 11));
		ProjAppointment earlier = createAppointment(participant, project, "Earlier", at(2030, 3, 2, 9), at(2030, 3, 2, 10));
		ProjAppointment overlapping = createAppointment(owner, project, "Overlapping", at(2030, 2, 28, 20), at(2030, 3, 1, 8));
		ProjAppointment lastDay = createAppointment(owner, project, "Last day", at(2030, 3, 10, 23), at(2030, 3, 11, 1));
		createAppointment(owner, project, "After", at(2030, 3, 11, 10), at(2030, 3, 11, 11));
		createAppointment(owner, project, "Before", at(2030, 2, 27, 10), at(2030, 2, 27, 11));
		ProjAppointment noDates = createAppointment(owner, project, "No dates", null, null);
		projectService.updateTags(owner, later.getArtefact(), List.of("meeting", "alpha"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("projectKey", project.getKey(), "from", "2030-03-01", "to", "2030-03-10"));
		Map<String,Object> structured = structured(result);
		List<Map<String,Object>> items = items(structured);
		Assert.assertEquals(Integer.valueOf(4), structured.get("total"));
		Assert.assertEquals(Boolean.FALSE, structured.get("truncated"));
		// sorted by the start
		assertThat(keys(items)).containsExactly(overlapping.getKey(), earlier.getKey(), later.getKey(), lastDay.getKey());

		Map<String,Object> laterItem = items.get(2);
		Assert.assertEquals("Later", laterItem.get("subject"));
		Assert.assertEquals("Room 1", laterItem.get("location"));
		Assert.assertEquals("Agenda of Later", laterItem.get("description"));
		Assert.assertEquals(iso(at(2030, 3, 5, 10)), laterItem.get("start"));
		Assert.assertEquals(iso(at(2030, 3, 5, 11)), laterItem.get("end"));
		Assert.assertEquals(Boolean.FALSE, laterItem.get("allDay"));
		Assert.assertEquals("none", laterItem.get("recurrence"));
		Assert.assertFalse(laterItem.containsKey("recurrenceRule"));
		Assert.assertFalse(laterItem.containsKey("recurrenceEnd"));
		Assert.assertFalse(laterItem.containsKey("color"));
		Assert.assertFalse(laterItem.containsKey("links"));
		Assert.assertEquals("active", laterItem.get("status"));
		Assert.assertEquals(project.getKey(), laterItem.get("projectKey"));
		Assert.assertEquals(List.of("alpha", "meeting"), laterItem.get("tags"));
		assertThat(persons(laterItem, "members")).containsExactly(username(owner));
		Assert.assertEquals(username(owner), ((Map<?,?>)laterItem.get("creator")).get("username"));
		assertThat(laterItem.get("url").toString()).contains("/Project/" + project.getKey()).contains("/Appointment/" + later.getKey());
		assertThat(result.text()).contains("Window 2030-03-01 to 2030-03-10").contains("4 appointments")
				.contains("Later (key " + later.getKey()).contains("at Room 1");

		// the appointments without dates follow at the end
		assertThat(keys(items(structured(invoke(context(participant), Map.of("projectKey", project.getKey(),
				"from", "2030-03-01", "to", "2030-03-10", "datesOnly", false))))))
				.containsExactly(overlapping.getKey(), earlier.getKey(), later.getKey(), lastDay.getKey(), noDates.getKey());
		Map<String,Object> noDatesItem = items(structured(invoke(context(participant), Map.of("projectKey", project.getKey(),
				"from", "2029-01-01", "to", "2029-01-02", "datesOnly", false)))).get(0);
		Assert.assertFalse(noDatesItem.containsKey("start"));
		Assert.assertFalse(noDatesItem.containsKey("end"));
	}

	@Test
	public void list_defaultWindow() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-4");
		ProjProject project = createProject(owner);
		LocalDateTime tomorrow = LocalDate.now().plusDays(1).atTime(10, 0);
		ProjAppointment soon = createAppointment(owner, project, "Soon", tomorrow, tomorrow.plusHours(1));
		createAppointment(owner, project, "Far", tomorrow.plusDays(40), tomorrow.plusDays(40).plusHours(1));
		createAppointment(owner, project, "Past", tomorrow.minusDays(3), tomorrow.minusDays(3).plusHours(1));
		dbInstance.commitAndCloseSession();

		assertThat(keys(items(structured(invoke(context(owner), Map.of("projectKey", project.getKey()))))))
				.containsExactly(soon.getKey());
		// to alone counts from today
		assertThat(keys(items(structured(invoke(context(owner), Map.of("projectKey", project.getKey(),
				"to", LocalDate.now().plusDays(60).toString())))))).hasSize(2);
	}

	@Test
	public void list_filters() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-5");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-6");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjAppointment mine = createAppointment(owner, project, "Mine", at(2030, 3, 2, 9), at(2030, 3, 2, 10));
		ProjAppointment theirs = createAppointment(participant, project, "Theirs", at(2030, 3, 3, 9), at(2030, 3, 3, 10));
		ProjAppointment deleted = createAppointment(owner, project, "Deleted", at(2030, 3, 4, 9), at(2030, 3, 4, 10));
		projectService.deleteAppointmentSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		Map<String,Object> window = Map.of("projectKey", project.getKey(), "from", "2030-03-01", "to", "2030-03-10");
		assertThat(keys(items(structured(invoke(context(owner), window)))))
				.containsExactly(mine.getKey(), theirs.getKey());
		assertThat(keys(items(structured(invoke(context(owner), with(window, "status", "deleted"))))))
				.containsExactly(deleted.getKey());
		assertThat(keys(items(structured(invoke(context(owner), with(window, "status", "all"))))))
				.containsExactly(mine.getKey(), theirs.getKey(), deleted.getKey());
		assertThat(keys(items(structured(invoke(context(owner), with(window, "createdByMe", Boolean.TRUE))))))
				.containsExactly(mine.getKey());
		assertThat(keys(items(structured(invoke(context(participant), with(window, "createdByMe", Boolean.TRUE))))))
				.containsExactly(theirs.getKey());
	}

	@Test
	public void list_seriesOnce() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-7");
		ProjProject project = createProject(owner);
		ProjAppointment series = createAppointment(owner, project, "Weekly", at(2030, 3, 4, 9), at(2030, 3, 4, 10));
		String rule = calendarManager.getRecurrenceRule(KalendarEvent.WEEKLY, toDate(at(2030, 4, 29, 0)), false);
		projectService.updateAppointment(owner, new ProjectBCFactory(), series, toDate(at(2030, 3, 4, 9)), toDate(at(2030, 3, 4, 10)),
				"Weekly", null, null, null, false, rule);
		// an occurrence changed in the calendar is an appointment of its own
		ProjAppointment occurrence = createOccurrence(owner, series, at(2030, 3, 11, 9));
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(occurrence.getRecurrenceId());

		McpToolResult result = invoke(context(owner), Map.of("projectKey", project.getKey(), "from", "2030-03-01", "to", "2030-03-31"));
		List<Map<String,Object>> items = items(structured(result));
		assertThat(keys(items)).containsExactly(series.getKey());
		Map<String,Object> item = items.get(0);
		Assert.assertEquals("weekly", item.get("recurrence"));
		Assert.assertEquals(rule, item.get("recurrenceRule"));
		Assert.assertEquals("2030-04-29", item.get("recurrenceEnd"));
		Assert.assertEquals(iso(at(2030, 3, 4, 9)), item.get("start"));
		assertThat(result.text()).contains("weekly until 2030-04-29");

		// the window test uses the dates of the first occurrence
		assertThat(keys(items(structured(invoke(context(owner), Map.of("projectKey", project.getKey(),
				"from", "2030-03-20", "to", "2030-03-31")))))).isEmpty();

		// the occurrence itself is readable by its key
		Map<String,Object> occurrenceItem = item(structured(invoke(context(owner), Map.of("action", "get", "appointmentKey", occurrence.getKey()))));
		Assert.assertEquals("none", occurrenceItem.get("recurrence"));
		Assert.assertEquals(iso(at(2030, 3, 11, 9)), occurrenceItem.get("start"));
	}

	@Test
	public void list_recurrenceWords() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-8");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		Date end = toDate(at(2030, 6, 30, 0));
		Assert.assertEquals("none", AppointmentToolSupport.recurrence(null));
		Assert.assertEquals("daily", AppointmentToolSupport.recurrence(calendarManager.getRecurrenceRule(KalendarEvent.DAILY, end, false)));
		Assert.assertEquals("workdaily", AppointmentToolSupport.recurrence(calendarManager.getRecurrenceRule(KalendarEvent.WORKDAILY, end, false)));
		Assert.assertEquals("weekly", AppointmentToolSupport.recurrence(calendarManager.getRecurrenceRule(KalendarEvent.WEEKLY, end, false)));
		Assert.assertEquals("biweekly", AppointmentToolSupport.recurrence(calendarManager.getRecurrenceRule(KalendarEvent.BIWEEKLY, end, false)));
		Assert.assertEquals("monthly", AppointmentToolSupport.recurrence(calendarManager.getRecurrenceRule(KalendarEvent.MONTHLY, end, true)));
		Assert.assertEquals("yearly", AppointmentToolSupport.recurrence(calendarManager.getRecurrenceRule(KalendarEvent.YEARLY, end, true)));
		Assert.assertEquals("other", AppointmentToolSupport.recurrence("FREQ=HOURLY;COUNT=3"));

		// an all day series
		ProjAppointment series = createAppointment(owner, project, "Daily", at(2030, 3, 4, 0), at(2030, 3, 4, 0));
		projectService.updateAppointment(owner, new ProjectBCFactory(), series, toDate(at(2030, 3, 4, 0)), toDate(at(2030, 3, 4, 0)),
				"Daily", null, null, "red", true, calendarManager.getRecurrenceRule(KalendarEvent.DAILY, toDate(at(2030, 3, 8, 0)), true));
		dbInstance.commitAndCloseSession();
		Map<String,Object> item = item(structured(invoke(context(owner), Map.of("action", "get", "appointmentKey", series.getKey()))));
		Assert.assertEquals(Boolean.TRUE, item.get("allDay"));
		Assert.assertEquals("daily", item.get("recurrence"));
		Assert.assertEquals("2030-03-08", item.get("recurrenceEnd"));
		Assert.assertEquals("red", item.get("color"));
	}

	@Test
	public void list_limitAndTruncated() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-9");
		ProjProject project = createProject(owner);
		ProjAppointment first = createAppointment(owner, project, "A", at(2030, 3, 2, 9), at(2030, 3, 2, 10));
		ProjAppointment second = createAppointment(owner, project, "B", at(2030, 3, 3, 9), at(2030, 3, 3, 10));
		createAppointment(owner, project, "C", at(2030, 3, 4, 9), at(2030, 3, 4, 10));
		dbInstance.commitAndCloseSession();

		Map<String,Object> structured = structured(invoke(context(owner), Map.of("projectKey", project.getKey(),
				"from", "2030-03-01", "to", "2030-03-10", "limit", 2)));
		assertThat(keys(items(structured))).containsExactly(first.getKey(), second.getKey());
		Assert.assertEquals(Integer.valueOf(3), structured.get("total"));
		Assert.assertEquals(Boolean.TRUE, structured.get("truncated"));
	}

	@Test
	public void list_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-10");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-11");
		ProjProject project = createProject(owner);
		createAppointment(owner, project, "Secret", at(2030, 3, 2, 9), at(2030, 3, 2, 10));
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("projectKey", project.getKey()));
			Assert.fail("A non-member cannot list the appointments");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the appointments", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_withLinks() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-12");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-13");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjAppointment appointment = createAppointment(owner, project, "Kickoff", at(2030, 3, 2, 9), at(2030, 3, 2, 10));
		ProjNote note = projectService.createNote(owner, project);
		projectService.updateNote(owner, note, UUID.randomUUID().toString(), "Minutes", "text");
		ProjToDo toDo = projectService.createToDo(owner, project);
		projectService.updateToDo(owner, toDo, "Follow up", ToDoStatus.open, null, null, null, null, null);
		ProjNote deletedLinked = projectService.createNote(owner, project);
		projectService.updateNote(owner, deletedLinked, UUID.randomUUID().toString(), "Deleted linked", "x");
		projectService.linkArtefacts(owner, appointment.getArtefact(), note.getArtefact());
		projectService.linkArtefacts(owner, appointment.getArtefact(), toDo.getArtefact());
		projectService.linkArtefacts(owner, appointment.getArtefact(), deletedLinked.getArtefact());
		projectService.deleteNoteSoftly(owner, deletedLinked);
		projectService.updateMembers(owner, new ProjectBCFactory(), appointment.getArtefact(), List.<IdentityRef>of(owner, participant));
		projectService.updateTags(owner, appointment.getArtefact(), List.of("kickoff"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("action", "get", "appointmentKey", appointment.getKey()));
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals(appointment.getKey(), item.get("key"));
		Assert.assertEquals("Kickoff", item.get("subject"));
		Assert.assertEquals("Agenda of Kickoff", item.get("description"));
		Assert.assertEquals(List.of("kickoff"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));

		@SuppressWarnings("unchecked")
		List<Map<String,Object>> links = (List<Map<String,Object>>)item.get("links");
		// the deleted linked note is not listed
		Assert.assertEquals(2, links.size());
		Assert.assertEquals("toDo", links.get(0).get("type"));
		Assert.assertEquals(toDo.getToDoTask().getKey(), links.get(0).get("key"));
		Assert.assertEquals("Follow up", links.get(0).get("title"));
		Assert.assertEquals("note", links.get(1).get("type"));
		Assert.assertEquals(note.getKey(), links.get(1).get("key"));
		Assert.assertEquals("Minutes", links.get(1).get("title"));
		assertThat(result.text()).contains("Appointment Kickoff (key " + appointment.getKey()).contains("Agenda of Kickoff");
	}

	@Test
	public void get_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-14");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-15");
		ProjProject project = createProject(owner);
		ProjAppointment appointment = createAppointment(owner, project, "Secret", at(2030, 3, 2, 9), at(2030, 3, 2, 10));
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "get", "appointmentKey", appointment.getKey()));
			Assert.fail("A non-member cannot read the appointment");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the appointment", e.getPermission());
			Assert.assertEquals("appointment with key " + appointment.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-ap-16");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "get"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("appointmentKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknown = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "get", "appointmentKey", Long.valueOf(-7))));
		Assert.assertEquals("appointment with key -7", unknown.getObjectDescription());
		Assert.assertEquals("view the appointment", unknown.getPermission());
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

	private static LocalDateTime at(int year, int month, int day, int hour) {
		return LocalDateTime.of(year, month, day, hour, 0);
	}

	private static Date toDate(LocalDateTime dateTime) {
		return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private static String iso(LocalDateTime dateTime) {
		return McpToolSupport.iso(toDate(dateTime));
	}

	private static Map<String,Object> with(Map<String,Object> arguments, String name, Object value) {
		Map<String,Object> copy = new HashMap<>(arguments);
		copy.put(name, value);
		return copy;
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

	private static List<Long> keys(List<Map<String,Object>> items) {
		return items.stream().map(item -> (Long)item.get("key")).toList();
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
