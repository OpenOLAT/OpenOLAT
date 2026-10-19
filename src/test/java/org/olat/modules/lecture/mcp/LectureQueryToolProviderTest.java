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
package org.olat.modules.lecture.mcp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.mcp.McpGrants;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.manager.LectureParticipantSummaryDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration test of the tool lecture_query. Every test builds its own
 * courses, events, teachers and participants with random names.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class LectureQueryToolProviderTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	@Autowired
	private LectureQueryToolProvider provider;

	private boolean lectureEnabledBefore;

	@Before
	public void enableLectures() {
		lectureEnabledBefore = lectureModule.isEnabled();
		if(!lectureEnabledBefore) {
			lectureModule.setEnabled(true);
		}
	}

	@After
	public void restoreLectures() {
		if(lectureModule.isEnabled() != lectureEnabledBefore) {
			lectureModule.setEnabled(lectureEnabledBefore);
		}
	}

	@Test
	public void definition() {
		Assertions.assertThat(provider.getToolName()).isEqualTo("lecture_query");
		Assertions.assertThat(provider.getPack()).isEqualTo(LectureMcpPack.NAME);
		Assertions.assertThat(provider.getScope()).isEqualTo(McpToolScope.READ);
		Assertions.assertThat(provider.getDefinition().description()).contains("pending_rollcalls");
		Assertions.assertThat(provider.getDefinition().inputSchema()).containsKey("properties");
		Assertions.assertThat(provider.getDefinition().outputSchema()).containsKey("properties");
	}

	@Test
	public void isAvailableFollowsLectureModule() {
		Assertions.assertThat(provider.isAvailable()).isTrue();
		lectureModule.setEnabled(false);
		Assertions.assertThat(provider.isAvailable()).isFalse();
		lectureModule.setEnabled(true);
		Assertions.assertThat(provider.isAvailable()).isTrue();
	}

	@Test
	public void eventsAsParticipantDefaultWindow() {
		Fixture f = new Fixture();

		McpToolResult result = provider.execute(context(f.participant), Map.of());

		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(keys(result)).containsExactly(f.running.getKey(), f.otherCourseIn2Days.getKey(), f.in3Days.getKey());
		Assertions.assertThat(total(result)).isEqualTo(3);
		Assertions.assertThat(truncated(result)).isFalse();
		Assertions.assertThat(result.text()).startsWith("3 events as participant from ");

		Map<String,Object> item = items(result).get(0);
		Assertions.assertThat(item)
			.containsEntry("key", f.running.getKey())
			.containsEntry("title", f.running.getTitle())
			.containsEntry("location", "Room 101")
			.containsEntry("status", "active")
			.containsEntry("compulsory", Boolean.TRUE)
			.containsEntry("start", McpToolSupport.iso(f.running.getStartDate()))
			.containsEntry("url", McpToolSupport.url("RepositoryEntry", f.course.getKey(), "LectureBlock", f.running.getKey()))
			.doesNotContainKey("myRollCall");
		Assertions.assertThat(item.get("rollCallStatus")).isNotNull();
		@SuppressWarnings("unchecked")
		Map<String,Object> course = (Map<String,Object>)item.get("course");
		Assertions.assertThat(course).containsEntry("key", f.course.getKey()).containsEntry("displayName", f.course.getDisplayname());
		@SuppressWarnings("unchecked")
		List<String> teachers = (List<String>)item.get("teachers");
		Assertions.assertThat(teachers).hasSize(1);
	}

	@Test
	public void eventsAsParticipantOfOneCourseWithRollCall() {
		Fixture f = new Fixture();

		McpToolResult result = provider.execute(context(f.participant), Map.of("courseKey", f.course.getKey()));

		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(keys(result)).containsExactly(f.running.getKey(), f.in3Days.getKey());
		Map<String,Object> running = items(result).get(0);
		@SuppressWarnings("unchecked")
		Map<String,Object> rollCall = (Map<String,Object>)running.get("myRollCall");
		Assertions.assertThat(rollCall)
			.containsEntry("rollCalled", Boolean.TRUE)
			.containsEntry("absent", Integer.valueOf(1))
			.containsEntry("absenceNotice", Boolean.FALSE);
		@SuppressWarnings("unchecked")
		Map<String,Object> notCalled = (Map<String,Object>)items(result).get(1).get("myRollCall");
		Assertions.assertThat(notCalled)
			.containsEntry("rollCalled", Boolean.FALSE)
			.doesNotContainKey("attended");
	}

	@Test
	public void eventsAsTeacher() {
		Fixture f = new Fixture();

		McpToolResult result = provider.execute(context(f.teacher), Map.of("as", "teacher"));
		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(keys(result)).containsExactly(f.running.getKey(), f.in3Days.getKey());
		Assertions.assertThat(result.text()).startsWith("2 events as teacher");
		Assertions.assertThat(items(result).get(0)).doesNotContainKey("myRollCall");

		String from = McpToolSupport.isoDate(new Date());
		String to = McpToolSupport.isoDate(DateUtils.addDays(new Date(), 40));
		McpToolResult wide = provider.execute(context(f.teacher), Map.of("as", "teacher", "from", from, "to", to));
		Assertions.assertThat(keys(wide)).containsExactly(f.running.getKey(), f.in3Days.getKey(), f.in30Days.getKey());
	}

	@Test
	public void eventsAsTeacherOfOneCourseWithoutMembership() {
		Fixture f = new Fixture();
		// The teacher is no member of the course, teaching an event is enough
		Assertions.assertThat(repositoryService.isMember(f.teacher, f.course)).isFalse();

		McpToolResult result = provider.execute(context(f.teacher), Map.of("as", "teacher", "courseKey", f.course.getKey()));
		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(keys(result)).containsExactly(f.running.getKey(), f.in3Days.getKey());
	}

	@Test
	public void eventsInExplicitWindow() {
		Fixture f = new Fixture();
		String from = McpToolSupport.isoDate(DateUtils.addDays(new Date(), 3));
		String to = McpToolSupport.isoDate(DateUtils.addDays(new Date(), 5));

		McpToolResult result = provider.execute(context(f.participant), Map.of("from", from, "to", to));
		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(keys(result)).containsExactly(f.in3Days.getKey());

		// One day window: from equals to includes the whole day
		McpToolResult oneDay = provider.execute(context(f.participant), Map.of("from", from, "to", from));
		Assertions.assertThat(keys(oneDay)).containsExactly(f.in3Days.getKey());
	}

	@Test
	public void eventsLimitAndTruncated() {
		Fixture f = new Fixture();

		McpToolResult result = provider.execute(context(f.participant), Map.of("limit", 1));
		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(keys(result)).containsExactly(f.running.getKey());
		Assertions.assertThat(total(result)).isEqualTo(3);
		Assertions.assertThat(truncated(result)).isTrue();
	}

	@Test
	public void eventsOfOutsiderAreEmpty() {
		new Fixture();
		Identity outsider = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-lec-out");

		McpToolResult result = provider.execute(context(outsider), Map.of());
		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(items(result)).isEmpty();
		Assertions.assertThat(total(result)).isZero();
		Assertions.assertThat(result.text()).startsWith("0 events as participant");
	}

	@Test
	public void eventsInvalidArguments() {
		Fixture f = new Fixture();
		McpToolContext ctx = context(f.participant);

		assertError(provider.execute(ctx, Map.of("from", "2026-13-45")), "must be an ISO date");
		assertError(provider.execute(ctx, Map.of("to", "tomorrow")), "must be an ISO date");
		assertError(provider.execute(ctx, Map.of("from", "2026-10-10", "to", "2026-10-01")), "must not be before from");
		assertError(provider.execute(ctx, Map.of("from", "2026-01-01", "to", "2027-06-01")), "longer than 366 days");
		assertError(provider.execute(ctx, Map.of("as", "student")), "Unknown as 'student'");
		assertError(provider.execute(ctx, Map.of("action", "delete")), "Unknown action 'delete'");
		assertError(provider.execute(ctx, Map.of("courseKey", Long.valueOf(-17L))), "No course with key -17 that this account may view.");
	}

	@Test
	public void eventsOfCourseWithoutLectures() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-lec-nol");
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(participant, course, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		McpToolResult result = provider.execute(context(participant), Map.of("courseKey", course.getKey()));
		assertError(result, "No course with key " + course.getKey() + " that this account may view.");
		Assertions.assertThat(result.text()).doesNotContain(course.getDisplayname());
	}

	@Test
	public void eventsOfForeignCourseDenied() {
		Fixture f = new Fixture();
		Identity outsider = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-lec-den");

		String expected = "No course with key " + f.course.getKey() + " that this account may view.";
		for(String as:List.of("participant", "teacher")) {
			McpToolResult result = provider.execute(context(outsider), Map.of("as", as, "courseKey", f.course.getKey()));
			assertError(result, expected);
			Assertions.assertThat(result.text()).isEqualTo(expected).doesNotContain(f.course.getDisplayname());
		}
		// The teacher of an event is no participant of the course
		McpToolResult asParticipant = provider.execute(context(f.teacher), Map.of("courseKey", f.course.getKey()));
		Assertions.assertThat(asParticipant.text()).isEqualTo(expected);
		// Missing, denied and lectures disabled answer identically
		McpToolResult missing = provider.execute(context(outsider), Map.of("courseKey", Long.valueOf(-17L)));
		Assertions.assertThat(missing.text()).isEqualTo("No course with key -17 that this account may view.");
	}

	@Test
	public void eventsAsParticipantOnlyOfPublishedAndClosedCourses() {
		Fixture f = new Fixture();
		String expected = "No course with key " + f.course.getKey() + " that this account may view.";

		for(RepositoryEntryStatusEnum status:List.of(RepositoryEntryStatusEnum.preparation,
				RepositoryEntryStatusEnum.review, RepositoryEntryStatusEnum.coachpublished)) {
			repositoryManager.setStatus(f.course, status);
			dbInstance.commitAndCloseSession();

			// the participant sees the events of the published course only
			McpToolResult result = provider.execute(context(f.participant), Map.of());
			Assertions.assertThat(result.error()).isFalse();
			Assertions.assertThat(keys(result)).as("Status %s", status).containsExactly(f.otherCourseIn2Days.getKey());
			Assertions.assertThat(total(result)).isEqualTo(1);

			// the course itself gets the answer of a foreign course
			McpToolResult byCourse = provider.execute(context(f.participant), Map.of("courseKey", f.course.getKey()));
			assertError(byCourse, expected);

			// the teacher still sees his events, as in the teacher cockpit
			McpToolResult asTeacher = provider.execute(context(f.teacher), Map.of("as", "teacher"));
			Assertions.assertThat(keys(asTeacher)).as("Status %s", status).containsExactly(f.running.getKey(), f.in3Days.getKey());
			McpToolResult asTeacherByCourse = provider.execute(context(f.teacher), Map.of("as", "teacher", "courseKey", f.course.getKey()));
			Assertions.assertThat(keys(asTeacherByCourse)).containsExactly(f.running.getKey(), f.in3Days.getKey());
		}

		// a closed course stays visible to its participants
		repositoryManager.setStatus(f.course, RepositoryEntryStatusEnum.closed);
		dbInstance.commitAndCloseSession();
		McpToolResult closed = provider.execute(context(f.participant), Map.of());
		Assertions.assertThat(keys(closed)).containsExactly(f.running.getKey(), f.otherCourseIn2Days.getKey(), f.in3Days.getKey());
		McpToolResult closedByCourse = provider.execute(context(f.participant), Map.of("courseKey", f.course.getKey()));
		Assertions.assertThat(keys(closedByCourse)).containsExactly(f.running.getKey(), f.in3Days.getKey());
	}

	@Test
	public void eventsOfTrashedCourseByCourseKeyDenied() {
		Fixture f = new Fixture();
		String expected = "No course with key " + f.course.getKey() + " that this account may view.";
		repositoryManager.setStatus(f.course, RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();

		McpToolResult asParticipant = provider.execute(context(f.participant), Map.of("courseKey", f.course.getKey()));
		assertError(asParticipant, expected);
		Assertions.assertThat(asParticipant.text()).isEqualTo(expected);
		McpToolResult asTeacher = provider.execute(context(f.teacher), Map.of("as", "teacher", "courseKey", f.course.getKey()));
		assertError(asTeacher, expected);

		// without courseKey the events of the trashed course are hidden as well
		Assertions.assertThat(keys(provider.execute(context(f.teacher), Map.of("as", "teacher")))).isEmpty();
		Assertions.assertThat(keys(provider.execute(context(f.participant), Map.of()))).containsExactly(f.otherCourseIn2Days.getKey());

		// the same for a deleted course
		repositoryManager.setStatus(f.course, RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		assertError(provider.execute(context(f.teacher), Map.of("as", "teacher", "courseKey", f.course.getKey())), expected);
		assertError(provider.execute(context(f.participant), Map.of("courseKey", f.course.getKey())), expected);
	}

	@Test
	public void pendingRollCalls() {
		Fixture f = new Fixture();

		McpToolResult result = provider.execute(context(f.teacher), Map.of("action", "pending_rollcalls"));
		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(keys(result)).containsExactly(f.running.getKey());
		Assertions.assertThat(result.text()).startsWith("1 events with a pending roll call");
		Assertions.assertThat(items(result).get(0))
			.containsEntry("url", McpToolSupport.url("RepositoryEntry", f.course.getKey(), "LectureBlock", f.running.getKey()));

		McpToolResult explicit = provider.execute(context(f.teacher), Map.of("action", "pending_rollcalls", "as", "teacher",
				"courseKey", f.course.getKey()));
		Assertions.assertThat(keys(explicit)).containsExactly(f.running.getKey());

		McpToolResult otherCourse = provider.execute(context(f.teacher), Map.of("action", "pending_rollcalls",
				"courseKey", f.otherCourse.getKey()));
		Assertions.assertThat(items(otherCourse)).isEmpty();
	}

	@Test
	public void pendingRollCallsLimit() {
		Fixture f = new Fixture();
		LectureBlock secondRunning = f.createBlock(f.course, "Second running", runningStart(), DateUtils.addMinutes(new Date(), 60), f.teacher);

		McpToolResult result = provider.execute(context(f.teacher), Map.of("action", "pending_rollcalls", "limit", 1));
		Assertions.assertThat(items(result)).hasSize(1);
		Assertions.assertThat(total(result)).isEqualTo(2);
		Assertions.assertThat(truncated(result)).isTrue();
		Assertions.assertThat(keys(result)).containsAnyOf(f.running.getKey(), secondRunning.getKey());
	}

	@Test
	public void pendingRollCallsTeacherOnly() {
		Fixture f = new Fixture();

		assertError(provider.execute(context(f.participant), Map.of("action", "pending_rollcalls", "as", "participant")),
				"for teachers only");
		// A participant without teaching has nothing pending
		McpToolResult result = provider.execute(context(f.participant), Map.of("action", "pending_rollcalls"));
		Assertions.assertThat(result.error()).isFalse();
		Assertions.assertThat(items(result)).isEmpty();
	}

	private static void assertError(McpToolResult result, String message) {
		Assertions.assertThat(result.error()).isTrue();
		Assertions.assertThat(result.structuredContent()).isNull();
		Assertions.assertThat(result.text()).contains(message);
	}

	private static McpToolContext context(Identity identity) {
		McpGrants grants = new McpGrants(Set.of(LectureQueryToolProvider.NAME), null, List.of("test"));
		return new McpToolContext(identity, Roles.userRoles(), grants, null, Locale.ENGLISH, "127.0.0.1");
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> items(McpToolResult result) {
		return (List<Map<String,Object>>)((Map<String,Object>)result.structuredContent()).get("items");
	}

	private static List<Object> keys(McpToolResult result) {
		return items(result).stream().map(item -> item.get("key")).toList();
	}

	@SuppressWarnings("unchecked")
	private static int total(McpToolResult result) {
		return ((Integer)((Map<String,Object>)result.structuredContent()).get("total")).intValue();
	}

	@SuppressWarnings("unchecked")
	private static boolean truncated(McpToolResult result) {
		return ((Boolean)((Map<String,Object>)result.structuredContent()).get("truncated")).booleanValue();
	}

	/**
	 * A start some minutes ago, but never before today, the default window starts today.
	 */
	private static Date runningStart() {
		Date now = new Date();
		Date start = DateUtils.addMinutes(now, -30);
		Date startOfDay = DateUtils.getStartOfDay(now);
		return start.before(startOfDay) ? startOfDay : start;
	}

	/**
	 * Two courses with lectures and roll call enabled. The participant is
	 * member of both. The teacher teaches the events of the first course and
	 * is no member of it.
	 */
	private class Fixture {

		private final Identity teacher;
		private final Identity participant;
		private final RepositoryEntry course;
		private final RepositoryEntry otherCourse;
		private final LectureBlock running;
		private final LectureBlock in3Days;
		private final LectureBlock in30Days;
		private final LectureBlock otherCourseIn2Days;
		private final Map<RepositoryEntry,Group> defaultGroups = new HashMap<>();

		Fixture() {
			teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-lec-tea");
			participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-lec-par");
			course = createCourse();
			otherCourse = createCourse();

			Date now = new Date();
			running = createBlock(course, "Running", runningStart(), DateUtils.addMinutes(now, 60), teacher);
			in3Days = createBlock(course, "In 3 days", DateUtils.addDays(now, 3), DateUtils.addMinutes(DateUtils.addDays(now, 3), 90), teacher);
			in30Days = createBlock(course, "In 30 days", DateUtils.addDays(now, 30), DateUtils.addMinutes(DateUtils.addDays(now, 30), 90), teacher);
			otherCourseIn2Days = createBlock(otherCourse, "Other course", DateUtils.addDays(now, 2), DateUtils.addMinutes(DateUtils.addDays(now, 2), 90), null);

			lectureParticipantSummaryDao.createSummary(course, participant, DateUtils.addDays(now, -10));
			dbInstance.commitAndCloseSession();
			lectureService.addRollCall(participant, running, null, List.of(Integer.valueOf(3)));
			dbInstance.commitAndCloseSession();
		}

		private RepositoryEntry createCourse() {
			RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
			repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
			dbInstance.commitAndCloseSession();

			RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
			config.setLectureEnabled(true);
			config.setOverrideModuleDefault(true);
			config.setRollCallEnabled(Boolean.TRUE);
			lectureService.updateRepositoryEntryLectureConfiguration(config);
			dbInstance.commitAndCloseSession();
			defaultGroups.put(entry, repositoryService.getDefaultGroup(entry));
			return entry;
		}

		LectureBlock createBlock(RepositoryEntry entry, String title, Date start, Date end, Identity blockTeacher) {
			LectureBlock block = lectureService.createLectureBlock(entry);
			block.setTitle(title + " " + JunitTestHelper.random());
			block.setStartDate(DateUtils.truncateMilliSeconds(start));
			block.setEndDate(DateUtils.truncateMilliSeconds(end));
			block.setLocation("Room 101");
			block.setCompulsory(true);
			block.setPlannedLecturesNumber(4);
			block = lectureService.save(block, List.of(defaultGroups.get(entry)));
			dbInstance.commitAndCloseSession();
			if(blockTeacher != null) {
				lectureService.addTeacher(block, blockTeacher);
				dbInstance.commitAndCloseSession();
			}
			return block;
		}
	}
}
