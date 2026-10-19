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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The tool of the lecture pack. It lists the events (lecture blocks) of the
 * caller as participant or as teacher in a date window, and the roll calls a
 * teacher can do now. The queries are the ones of the lecture list in the
 * course and in the coaching tool (<code>LectureListRepositoryController</code>)
 * and of the roll call interceptor (<code>RollCallInterceptorController</code>).
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class LectureQueryToolProvider implements McpToolProvider {

	public static final String NAME = "lecture_query";

	public static final String ACTION_EVENTS = "events";
	public static final String ACTION_PENDING_ROLLCALLS = "pending_rollcalls";
	/** The roles of the argument as, the names of {@link LectureRoles} (Participant, Teacher) */
	static final List<LectureRoles> AS = List.of(LectureRoles.participant, LectureRoles.teacher);

	/** Default length of the date window in days */
	public static final int DEFAULT_WINDOW_DAYS = 14;
	/** Maximum length of the date window in days, keeps the unpaged service query bounded */
	public static final int MAX_WINDOW_DAYS = 366;

	private static final Map<String,Object> ITEM_SCHEMA = Map.of("type", "object",
			"properties", Map.ofEntries(
				Map.entry("key", Map.of("type", "integer", "description", "Key of the event (lecture block)")),
				Map.entry("title", Map.of("type", "string")),
				Map.entry("start", Map.of("type", "string", "format", "date-time")),
				Map.entry("end", Map.of("type", "string", "format", "date-time")),
				Map.entry("location", Map.of("type", "string")),
				Map.entry("course", Map.of("type", "object",
						"properties", Map.of(
							"key", Map.of("type", "integer"),
							"displayName", Map.of("type", "string")))),
				Map.entry("status", Map.of("type", "string", "enum", McpToolSupport.names(LectureBlockStatus.values()),
						"description", "The status of the event: active (Active), done (Done), cancelled (Canceled)")),
				Map.entry("rollCallStatus", Map.of("type", "string", "enum", McpToolSupport.names(LectureRollCallStatus.values()),
						"description", "The roll call status: open (Open), closed (Closed), reopen (Reopen), autoclosed (Auto-closed)")),
				Map.entry("compulsory", Map.of("type", "boolean")),
				Map.entry("teachers", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Display names of the teachers")),
				Map.entry("myRollCall", Map.of("type", "object",
						"description", "Own attendance, only for action events as participant with a courseKey",
						"properties", Map.of(
							"rollCalled", Map.of("type", "boolean", "description", "true if the teacher recorded the attendance"),
							"attended", Map.of("type", "integer", "description", "Number of lectures attended"),
							"absent", Map.of("type", "integer", "description", "Number of lectures absent"),
							"authorizedAbsence", Map.of("type", "boolean"),
							"absenceNotice", Map.of("type", "boolean")))),
				Map.entry("url", Map.of("type", "string", "description", "Link to the event in the lectures of the course"))),
			"required", List.of("key", "title", "start", "end", "status", "rollCallStatus", "compulsory", "teachers", "url"));

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: lectures and events",
			"Lectures",
			"Returns the lecture events (lecture blocks) of the current user with date, location, course, status, roll call status and teachers. "
			+ "Use action events to see the schedule as participant or as teacher in a date window (from defaults to today, to defaults to from plus 14 days, at most 366 days), "
			+ "optionally for one course with courseKey; as participant with a courseKey each item also carries the own attendance. "
			+ "Use action pending_rollcalls to find the events running now where the user as teacher still has to take the roll call. "
			+ "It does not return events of other users or courses without the lectures enabled, and it changes nothing.",
			Map.of("type", "object",
				"properties", Map.of(
					"action", Map.of("type", "string", "enum", List.of(ACTION_EVENTS, ACTION_PENDING_ROLLCALLS), "default", ACTION_EVENTS),
					"as", Map.of("type", "string", "enum", McpToolSupport.names(AS), "default", LectureRoles.participant.name(),
							"description", "The role of the user in the events; pending_rollcalls is for teachers only"),
					"from", Map.of("type", "string", "format", "date", "description", "First day (yyyy-MM-dd), default today"),
					"to", Map.of("type", "string", "format", "date", "description", "Last day (yyyy-MM-dd), default from plus 14 days"),
					"courseKey", Map.of("type", "integer", "description", "Optional, the key of one course"),
					"limit", Map.of("type", "integer", "minimum", 1, "default", McpToolSupport.DEFAULT_LIMIT, "maximum", McpToolSupport.MAX_LIMIT)),
				"additionalProperties", Boolean.FALSE),
			McpToolSupport.listSchema(ITEM_SCHEMA),
			false, true);

	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;

	@Override
	public String getToolName() {
		return NAME;
	}

	@Override
	public String getPack() {
		return LectureMcpPack.NAME;
	}

	@Override
	public McpToolScope getScope() {
		return McpToolScope.READ;
	}

	@Override
	public boolean isAvailable() {
		return lectureModule.isEnabled();
	}

	@Override
	public McpToolDefinition getDefinition() {
		return DEFINITION;
	}

	@Override
	public McpToolResult execute(McpToolContext context, Map<String,Object> arguments) {
		String action = McpToolSupport.string(arguments, "action", ACTION_EVENTS);
		if(ACTION_EVENTS.equals(action)) {
			return events(context, arguments);
		}
		if(ACTION_PENDING_ROLLCALLS.equals(action)) {
			return pendingRollCalls(context, arguments);
		}
		return McpToolResult.error("Unknown action '" + action + "', use events or pending_rollcalls.");
	}

	private McpToolResult events(McpToolContext context, Map<String,Object> arguments) {
		String asValue = McpToolSupport.string(arguments, "as", LectureRoles.participant.name());
		LectureRoles as = McpToolSupport.enumValue(AS, asValue);
		if(as == null) {
			return McpToolResult.error(McpToolSupport.unknownValue("as", asValue, AS));
		}
		
		Date from;
		Date to;
		try {
			from = McpToolSupport.date(arguments, "from");
			to = McpToolSupport.date(arguments, "to");
		} catch (IllegalArgumentException e) {
			return McpToolResult.error(e.getMessage());
		}
		if(from == null) {
			from = DateUtils.getStartOfDay(new Date());
		}
		if(to == null) {
			to = DateUtils.addDays(from, DEFAULT_WINDOW_DAYS);
		}
		if(to.before(from)) {
			return McpToolResult.error("The argument to (" + McpToolSupport.isoDate(to)
					+ ") must not be before from (" + McpToolSupport.isoDate(from) + ").");
		}
		long windowDays = TimeUnit.MILLISECONDS.toDays(to.getTime() - from.getTime());
		if(windowDays > MAX_WINDOW_DAYS) {
			return McpToolResult.error("The date window from " + McpToolSupport.isoDate(from) + " to " + McpToolSupport.isoDate(to)
					+ " is longer than " + MAX_WINDOW_DAYS + " days, use a shorter window.");
		}
		
		Identity identity = context.identity();
		RepositoryEntry entry = null;
		Long courseKey = McpToolSupport.longValue(arguments, "courseKey");
		if(courseKey != null) {
			entry = repositoryService.loadByKey(courseKey);
			if(entry == null || !isCourseStatusVisible(entry, as) || !isCourseVisible(identity, entry, as) || !isLectureEnabled(entry)) {
				// One answer for missing, denied and lectures disabled: no existence or title oracle
				return McpToolResult.error("No course with key " + courseKey + " that this account may view.");
			}
		}
		
		// Same parameters as LectureListRepositoryController for a course or the coaching tool
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setLectureConfiguredRepositoryEntry(true);
		if(entry != null) {
			searchParams.setRepositoryEntry(entry);
		}
		if(as == LectureRoles.teacher) {
			searchParams.setTeacher(identity);
		} else {
			searchParams.setParticipant(identity);
		}
		searchParams.setStartDate(from);
		searchParams.setStartDateBefore(DateUtils.getEndOfDay(to));
		
		List<LectureBlockWithTeachers> blocks = new ArrayList<>(lectureService.getLectureBlocksWithOptionalTeachers(searchParams));
		// The query excludes only trashed and deleted courses, a participant sees published and closed courses only
		blocks.removeIf(block -> !isCourseStatusVisible(block.getLectureBlock().getEntry(), as));
		blocks.sort(Comparator.comparing((LectureBlockWithTeachers b) -> b.getLectureBlock().getStartDate(), Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(b -> b.getLectureBlock().getKey()));
		
		Map<Long,LectureBlockAndRollCall> rollCalls = Map.of();
		if(entry != null && as == LectureRoles.participant) {
			rollCalls = new HashMap<>();
			for(LectureBlockAndRollCall rollCall:lectureService.getParticipantLectureBlocks(entry, identity, ", ")) {
				rollCalls.put(rollCall.getLectureBlockRef().getKey(), rollCall);
			}
		}
		
		int limit = McpToolSupport.limit(arguments);
		int total = blocks.size();
		List<Map<String,Object>> items = new ArrayList<>(Math.min(total, limit));
		List<String> lines = new ArrayList<>(Math.min(total, limit));
		for(LectureBlockWithTeachers block:blocks.subList(0, Math.min(total, limit))) {
			LectureBlock lectureBlock = block.getLectureBlock();
			Map<String,Object> item = toItem(lectureBlock, block.getTeachers());
			LectureBlockAndRollCall rollCall = rollCalls.get(lectureBlock.getKey());
			if(rollCall != null) {
				item.put("myRollCall", toRollCall(rollCall));
			}
			items.add(item);
			lines.add(toLine(lectureBlock));
		}
		
		String noun = as == LectureRoles.teacher ? "events as teacher" : "events as participant";
		String text = McpToolSupport.listText(lines, total, noun + " from " + McpToolSupport.isoDate(from)
				+ " to " + McpToolSupport.isoDate(to));
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), text);
	}
	
	private McpToolResult pendingRollCalls(McpToolContext context, Map<String,Object> arguments) {
		// The default of as is participant, an absent value means teacher for this action
		String as = McpToolSupport.string(arguments, "as", LectureRoles.teacher.name());
		if(!LectureRoles.teacher.name().equals(as)) {
			return McpToolResult.error("The action pending_rollcalls is for teachers only, use as teacher.");
		}
		
		Long courseKey = McpToolSupport.longValue(arguments, "courseKey");
		// Same query as RollCallInterceptorController: running now, not closed, roll call enabled
		List<LectureBlock> blocks = new ArrayList<>(lectureService.getRollCallAsTeacher(context.identity()));
		if(courseKey != null) {
			blocks.removeIf(block -> block.getEntry() == null || !courseKey.equals(block.getEntry().getKey()));
		}
		blocks.sort(Comparator.comparing(LectureBlock::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(LectureBlock::getKey));
		
		int limit = McpToolSupport.limit(arguments);
		int total = blocks.size();
		List<Map<String,Object>> items = new ArrayList<>(Math.min(total, limit));
		List<String> lines = new ArrayList<>(Math.min(total, limit));
		for(LectureBlock block:blocks.subList(0, Math.min(total, limit))) {
			items.add(toItem(block, lectureService.getTeachers(block)));
			lines.add(toLine(block));
		}
		String text = McpToolSupport.listText(lines, total, "events with a pending roll call");
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), text);
	}
	
	/**
	 * A participant must be member of the course. A teacher must teach an
	 * event of the course or be a member of it. The search filters by the
	 * identity anyway, the check gives the model a clear answer instead of an
	 * empty list.
	 */
	private boolean isCourseVisible(Identity identity, RepositoryEntry entry, LectureRoles as) {
		if(as == LectureRoles.teacher) {
			return lectureService.hasLecturesAsTeacher(entry, identity) || repositoryService.isMember(identity, entry);
		}
		return repositoryService.isMember(identity, entry);
	}
	
	/**
	 * The course status rule of the lecture views. A participant sees the
	 * events of published and closed courses only, as the participant user tool
	 * (LectureServiceImpl.getParticipantLecturesStatistics) and the launch of
	 * the course. A teacher sees the events of every course that is not in the
	 * trash or deleted, as the teacher cockpit.
	 *
	 * @param entry The course of an event or of the courseKey argument
	 * @param as participant or teacher
	 * @return true if the course status lets the role see the events
	 */
	static boolean isCourseStatusVisible(RepositoryEntry entry, LectureRoles as) {
		if(entry == null || entry.getEntryStatus() == null) {
			return false;
		}
		RepositoryEntryStatusEnum status = entry.getEntryStatus();
		if(as == LectureRoles.teacher) {
			return !RepositoryEntryStatusEnum.isInArray(status, RepositoryEntryStatusEnum.deleted());
		}
		return RepositoryEntryStatusEnum.isInArray(status, RepositoryEntryStatusEnum.publishedAndClosed());
	}
	
	private boolean isLectureEnabled(RepositoryEntry entry) {
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		return config != null && config.isLectureEnabled();
	}
	
	private Map<String,Object> toItem(LectureBlock block, List<Identity> teachers) {
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", block.getKey());
		item.put("title", McpToolSupport.text(block.getTitle()));
		item.put("start", McpToolSupport.iso(block.getStartDate()));
		item.put("end", McpToolSupport.iso(block.getEndDate()));
		if(StringHelper.containsNonWhitespace(block.getLocation())) {
			item.put("location", block.getLocation());
		}
		RepositoryEntry entry = block.getEntry();
		if(entry != null) {
			Map<String,Object> course = new LinkedHashMap<>();
			course.put("key", entry.getKey());
			course.put("displayName", entry.getDisplayname());
			item.put("course", course);
		}
		item.put("status", block.getStatus() == null ? null : block.getStatus().name());
		item.put("rollCallStatus", block.getRollCallStatus() == null ? null : block.getRollCallStatus().name());
		item.put("compulsory", Boolean.valueOf(block.isCompulsory()));
		List<String> teacherNames = new ArrayList<>(teachers == null ? 0 : teachers.size());
		if(teachers != null) {
			for(Identity teacher:teachers) {
				teacherNames.add(userManager.getUserDisplayName(teacher));
			}
		}
		item.put("teachers", teacherNames);
		item.put("url", entry == null ? null : McpToolSupport.url("RepositoryEntry", entry.getKey(), "LectureBlock", block.getKey()));
		return item;
	}
	
	private static Map<String,Object> toRollCall(LectureBlockAndRollCall rollCall) {
		Map<String,Object> summary = new LinkedHashMap<>();
		summary.put("rollCalled", Boolean.valueOf(rollCall.isRollCalled()));
		if(rollCall.isRollCalled()) {
			summary.put("attended", Integer.valueOf(rollCall.getLecturesAttendedNumber()));
			summary.put("absent", Integer.valueOf(rollCall.getLecturesAbsentNumber()));
			// null without a decision on the absence: absent in the item, the schema types a boolean
			if(rollCall.getLecturesAuthorizedAbsent() != null) {
				summary.put("authorizedAbsence", rollCall.getLecturesAuthorizedAbsent());
			}
		}
		summary.put("absenceNotice", Boolean.valueOf(rollCall.hasAbsenceNotice()));
		return summary;
	}
	
	private static String toLine(LectureBlock block) {
		StringBuilder sb = new StringBuilder();
		sb.append(McpToolSupport.iso(block.getStartDate())).append(' ').append(block.getTitle());
		if(block.getEntry() != null) {
			sb.append(" (").append(block.getEntry().getDisplayname()).append(')');
		}
		if(block.getLocation() != null) {
			sb.append(", ").append(block.getLocation());
		}
		sb.append(", ").append(block.getStatus()).append(", roll call ").append(block.getRollCallStatus())
		  .append(", key ").append(block.getKey());
		return sb.toString();
	}
}
