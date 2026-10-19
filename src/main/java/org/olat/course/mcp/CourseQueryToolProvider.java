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
package org.olat.course.mcp;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The tool course_query: the courses of the caller as participant, coach
 * or owner, and the details of one course.
 * <p>
 * The list uses the query of the "My courses" list
 * ({@link RepositoryService#searchMyView}) with membership mandatory. A
 * membership counts when it is granted directly on the course, through a
 * business group linked to the course or through a curriculum element the
 * course is attached to. The arguments use the concepts and the words of
 * the list:
 * <ul>
 * <li>role: the course role of {@link GroupRoles} (participant, coach,
 *   owner), the membership filter "Booked as ..." of the list
 *   ({@link Filter#asParticipant}, {@link Filter#asCoach}, {@link Filter#asOwner}).</li>
 * <li>period: the execution period filter of the list
 *   (filter.execution.period.current, .upcoming, .finished: Current,
 *   Upcoming, Finished), mapped to {@link Filter#currentCourses},
 *   {@link Filter#upcomingCourses} and {@link Filter#oldCourses} with the
 *   query of the list. A course without execution period matches none of
 *   them.</li>
 * <li>entryStatus: names of {@link RepositoryEntryStatusEnum}, narrowing the
 *   status the list shows to a member (preparation to closed).</li>
 * </ul>
 * <p>
 * The action get checks the access with {@link RepositoryManager#isAllowed}
 * (membership, organisation roles and open access), as the course runtime
 * does before it launches a course. A missing key, a resource that is not a
 * course and a course without access get the same error result, which names
 * neither the type nor the title.
 * <p>
 * The own progress (completion, score, passed, grade) follows the rules of
 * the UI, the tool shows a value only where the UI shows it:
 * <ul>
 * <li>list: the completion ring of the "My courses" list
 *   ({@code RepositoryEntryRow}, {@code RepositoryEntryListController.forgeCompletion}).
 *   Only for a course in the status published or closed, only with a
 *   completion, never for a guest and never for a conventional course
 *   (access by conditions) with a completion below 100%. Score and passed
 *   come only with the completion, the ring carries them. No grade.</li>
 * <li>get: the facts "My course" of the course info page
 *   ({@code InfoPageMyCourseController}). Nothing for a guest and nothing for
 *   a conventional course without efficiency statement. The completion only
 *   for a learning path course. Passed, score and grade only if the
 *   efficiency statement is enabled and the root node evaluates them (passed
 *   and score mode not none, grade with the grade module enabled).</li>
 * </ul>
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CourseQueryToolProvider implements McpToolProvider {

	private static final Logger log = Tracing.createLoggerFor(CourseQueryToolProvider.class);

	public static final String NAME = "course_query";

	static final String ACTION_LIST = "list";
	static final String ACTION_GET = "get";
	static final List<String> ACTIONS = List.of(ACTION_LIST, ACTION_GET);
	/** The course roles of the membership filter of the "My courses" list */
	static final List<GroupRoles> ROLES = List.of(GroupRoles.participant, GroupRoles.coach, GroupRoles.owner);
	/**
	 * The status of the learning resource a member sees in the "My courses"
	 * list, one set for every role ({@code OverviewRepositoryListController.doOpenEntries}).
	 * Trash and deleted are never listed.
	 */
	static final List<RepositoryEntryStatusEnum> ENTRY_STATUS = List.of(RepositoryEntryStatusEnum.preparationToClosed());
	static final int QUERY_MAX_LENGTH = 200;

	/**
	 * The execution period filter of the "My courses" list. The names are the
	 * English words of the filter values (filter.execution.period.current,
	 * .upcoming, .finished), the filters are the ones the list sets.
	 */
	enum Period {
		current(Filter.currentCourses),
		upcoming(Filter.upcomingCourses),
		finished(Filter.oldCourses);

		private final Filter filter;

		private Period(Filter filter) {
			this.filter = filter;
		}

		Filter filter() {
			return filter;
		}
	}

	/** The runtime types of the "My courses" list, without embedded and template entries */
	private static final RepositoryEntryRuntimeType[] RUNTIME_TYPES = { RepositoryEntryRuntimeType.curricular, RepositoryEntryRuntimeType.standalone };

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: courses",
			"Courses",
			"Returns the courses of the current account (action list) or the details of one course (action get) with "
			+ "status, execution period, the own progress, the bookmark flag and a link into OpenOlat. "
			+ "The own progress appears only where OpenOlat shows it to the account. "
			+ "list: completion only for a published or finished course and not for a conventional course below 100%; "
			+ "score and passed only together with the completion. "
			+ "get: completion only for a learning path course; score, passed and grade only if the course has the "
			+ "efficiency statement enabled and evaluates them. An absent value is not shown in OpenOlat, it does not mean zero or failed. "
			+ "Use list with role participant (default), coach or owner for the courses the account is booked in "
			+ "(directly, through a group or through a curriculum element), like the My courses list. "
			+ "The list shows every course of the membership in the status preparation, review, coachpublished, published or closed (see status in the item); "
			+ "entryStatus narrows it to some of these status, period filters by the execution period like the My courses filter "
			+ "(current, upcoming, finished; a course without execution period matches none), query by title or external reference. "
			+ "get works only for courses the account may open. "
			+ "Use get with courseKey for the description and learning objectives of a course found by list; "
			+ "this tool does not read course elements, members, lectures or ToDos.",
			inputSchema(), outputSchema(), false, true);

	@Autowired
	private MarkManager markManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;

	@Override
	public String getToolName() {
		return NAME;
	}

	@Override
	public String getPack() {
		return CourseMcpPack.NAME;
	}

	@Override
	public McpToolScope getScope() {
		return McpToolScope.READ;
	}

	/**
	 * Courses have no module switch.
	 */
	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public McpToolDefinition getDefinition() {
		return DEFINITION;
	}

	@Override
	public McpToolResult execute(McpToolContext context, Map<String,Object> arguments) {
		String action = McpToolSupport.string(arguments, "action", ACTION_LIST);
		Identity identity = context.identity();
		Roles roles = context.roles() == null ? securityManager.getRoles(identity) : context.roles();
		if(ACTION_LIST.equals(action)) {
			return list(identity, roles, arguments);
		}
		if(ACTION_GET.equals(action)) {
			Locale locale = context.locale() == null ? I18nManager.getInstance().getLocaleOrDefault(null) : context.locale();
			return get(identity, roles, locale, arguments);
		}
		return McpToolResult.error("Unknown action '" + action + "'. Use one of: " + String.join(", ", ACTIONS) + ".");
	}

	private McpToolResult list(Identity identity, Roles roles, Map<String,Object> arguments) {
		String roleValue = McpToolSupport.string(arguments, "role", GroupRoles.participant.name());
		GroupRoles role = McpToolSupport.enumValue(ROLES, roleValue);
		if(role == null) {
			return McpToolResult.error(McpToolSupport.unknownValue("role", roleValue, ROLES));
		}
		String periodValue = McpToolSupport.string(arguments, "period", null);
		Period period = McpToolSupport.enumValue(List.of(Period.values()), periodValue);
		if(periodValue != null && period == null) {
			return McpToolResult.error(McpToolSupport.unknownValue("period", periodValue, List.of(Period.values())));
		}
		List<RepositoryEntryStatusEnum> entryStatus = new ArrayList<>();
		for(String value:McpToolSupport.strings(arguments, "entryStatus")) {
			RepositoryEntryStatusEnum status = McpToolSupport.enumValue(ENTRY_STATUS, value);
			if(status == null) {
				return McpToolResult.error(McpToolSupport.unknownValue("entryStatus", value, ENTRY_STATUS));
			}
			entryStatus.add(status);
		}
		String query = McpToolSupport.string(arguments, "query", null);
		if(query != null && query.length() > QUERY_MAX_LENGTH) {
			return McpToolResult.error("The argument query is longer than " + QUERY_MAX_LENGTH + " characters.");
		}
		int limit = McpToolSupport.limit(arguments);

		return listAsMember(identity, roles, role, period, entryStatus, query, limit);
	}

	/**
	 * The sequence of the "My courses" list: membership mandatory, the role
	 * as booking filter, the status of the entries a member sees and the
	 * execution period filter.
	 */
	private McpToolResult listAsMember(Identity identity, Roles roles, GroupRoles role, Period period,
			List<RepositoryEntryStatusEnum> entryStatus, String query, int limit) {
		SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, roles, CourseModule.ORES_TYPE_COURSE);
		params.setMembershipOnly(true);
		Filter.rolesFilters(List.of(role)).forEach(params::addFilter);
		params.setEntryStatus(memberStatus(entryStatus));
		if(period != null) {
			params.addFilter(period.filter());
		}
		params.setRuntimeTypes(RUNTIME_TYPES);
		params.setIdRefsAndTitle(query);
		params.setOrderBy(OrderBy.title);
		params.setOrderByAsc(true);

		List<RepositoryEntryMyView> views = repositoryService.searchMyView(params, 0, limit);
		int total = views.size() < limit ? views.size() : repositoryService.countMyView(params);

		List<Map<String,Object>> items = new ArrayList<>(views.size());
		List<String> lines = new ArrayList<>(views.size());
		for(RepositoryEntryMyView view:views) {
			Map<String,Object> item = item(view.getKey(), view.getDisplayname(), view.getExternalRef(), view.getEntryStatus(),
					view.getLifecycle(), view.getTeaser(), view.isMarked());
			if(isCompletionShown(view, roles)) {
				putAssessment(item, view.getCompletion(), view.getScore(), view.getPassed());
			}
			items.add(item);
			lines.add(line(item));
		}
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), McpToolSupport.listText(lines, total, "courses"));
	}

	/**
	 * The rule of the completion ring of the "My courses" list: the row has
	 * values only for the status published and closed ({@code RepositoryEntryRow}),
	 * the ring needs a completion, is never shown to a guest and not for a
	 * conventional course below 100% ({@code RepositoryEntryListController.forgeCompletion}
	 * and {@code isConventionalUnpassedCourse}). The course is loaded through
	 * the course cache only for a row with a completion, as the list does.
	 *
	 * @param view The row of the list
	 * @param roles The roles of the account
	 * @return true if the list shows the completion ring with score and passed
	 */
	private boolean isCompletionShown(RepositoryEntryMyView view, Roles roles) {
		if(roles.isGuestOnly() || view.getCompletion() == null
				|| !RepositoryEntryStatusEnum.isInArray(view.getEntryStatus(), RepositoryEntryStatusEnum.publishedAndClosed())) {
			return false;
		}
		return !isConventionalUnpassedCourse(view);
	}

	private boolean isConventionalUnpassedCourse(RepositoryEntryMyView view) {
		try {
			ICourse course = CourseFactory.loadCourse(view.getOlatResource());
			return ConditionNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())
					&& view.getCompletion().doubleValue() < 1.0d;
		} catch (Exception e) {
			// the list shows the ring if the course cannot be loaded
			log.error("MCP course_query cannot load the course {} for the completion of the list", view.getKey(), e);
		}
		return false;
	}

	private McpToolResult get(Identity identity, Roles roles, Locale locale, Map<String,Object> arguments) {
		Long courseKey = McpToolSupport.longValue(arguments, "courseKey");
		if(courseKey == null) {
			return McpToolResult.error("The action get needs the argument courseKey, the key of a course from the action list.");
		}
		// One answer for missing, trash, deleted, not a course and no access: the
		// tool must not reveal the existence, the type or the title of a resource
		// the account cannot launch. Trash and deleted are handled in the UI only,
		// the launch check would let an owner in.
		RepositoryEntry entry = repositoryService.loadByKey(courseKey);
		if(entry == null || RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.deleted())
				|| !CourseModule.ORES_TYPE_COURSE.equals(entry.getOlatResource().getResourceableTypeName())
				|| !repositoryManager.isAllowed(identity, roles, entry).canLaunch()) {
			return McpToolResult.error(notFound(courseKey));
		}

		boolean marked = markManager.isMarked(entry, identity, null);
		Map<String,Object> item = item(entry.getKey(), entry.getDisplayname(), entry.getExternalRef(), entry.getEntryStatus(),
				entry.getLifecycle(), entry.getTeaser(), marked);
		if(!roles.isGuestOnly()) {
			putMyCourse(item, identity, entry, locale);
		}
		putText(item, "description", plain(entry.getDescription()));
		putText(item, "objectives", plain(entry.getObjectives()));
		putText(item, "authors", entry.getAuthors());
		if(entry.getEducationalType() != null) {
			item.put("educationalType", entry.getEducationalType().getIdentifier());
		}

		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", item);
		return McpToolResult.of(result, "Course " + line(item));
	}

	/**
	 * The facts "My course" of the course info page ({@code InfoPageMyCourseController.addCourseFacts}):
	 * nothing for a conventional course without efficiency statement, the
	 * completion only for a learning path course, passed, score and grade only
	 * with the efficiency statement and if the root node evaluates them.
	 * <p>
	 * The page reads the values with {@link CourseAssessmentService#getAssessmentEvaluation}
	 * after {@code ScoreAccounting.evaluateAll()}. evaluateAll() evaluates in
	 * memory without update, getAssessmentEvaluation loads the stored assessment
	 * entry of the root node and converts it with the assessment configuration
	 * of the root node. This method does the same load and the same conversion
	 * without the evaluation of the whole course: the result is identical.
	 */
	private void putMyCourse(Map<String,Object> item, Identity identity, RepositoryEntry entry, Locale locale) {
		ICourse course;
		try {
			course = CourseFactory.loadCourse(entry);
		} catch (CorruptedCourseException e) {
			return;
		}
		if(course == null) {
			return;
		}

		CourseConfig courseConfig = course.getCourseConfig();
		boolean learningPath = !ConditionNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType());
		boolean efficiencyStatementEnabled = courseConfig.isEfficiencyStatementEnabled();
		if(!learningPath && !efficiencyStatementEnabled) {
			return;
		}

		CourseNode rootNode = course.getRunStructure().getRootNode();
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(entry, rootNode);
		AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(identity, entry, rootNode.getIdent());
		AssessmentEvaluation evaluation = courseAssessmentService.toAssessmentEvaluation(assessmentEntry, assessmentConfig);

		Double completion = learningPath ? evaluation.getCompletion() : null;
		Float score = null;
		Boolean passed = null;
		String grade = null;
		if(efficiencyStatementEnabled) {
			if(Mode.none != assessmentConfig.getPassedMode()) {
				passed = evaluation.getPassed();
			}
			if(Mode.none != assessmentConfig.getScoreMode()
					&& StringHelper.containsNonWhitespace(AssessmentHelper.getRoundedScore(evaluation.getScore()))) {
				score = evaluation.getScore();
			}
			if(gradeModule.isEnabled() && StringHelper.containsNonWhitespace(evaluation.getGrade())) {
				Translator translator = Util.createPackageTranslator(GradeUIFactory.class, locale);
				grade = GradeUIFactory.translatePerformanceClass(translator, evaluation.getPerformanceClassIdent(), evaluation.getGrade());
			}
		}
		putAssessment(item, completion, score, passed);
		putText(item, "grade", grade);
	}

	static String notFound(Long courseKey) {
		return "No course with key " + courseKey + " that this account may view.";
	}

	/**
	 * The status set of the "My courses" list: one set for every role, from
	 * preparation to closed ({@code preparationToClosed()}, the rule of
	 * {@code OverviewRepositoryListController.doOpenEntries}). A member sees a
	 * course in preparation or review in the list, the item carries the status;
	 * only the launch is refused, which {@code get} replicates with
	 * {@link RepositoryManager#isAllowed}. Trash and deleted are never listed.
	 * The entryStatus argument narrows the set, it never widens it.
	 *
	 * @param entryStatus The status of the argument entryStatus, all of {@link #ENTRY_STATUS}
	 * @return The status set of the query, the whole visible set if the argument is empty
	 */
	static RepositoryEntryStatusEnum[] memberStatus(List<RepositoryEntryStatusEnum> entryStatus) {
		if(entryStatus == null || entryStatus.isEmpty()) {
			return RepositoryEntryStatusEnum.preparationToClosed();
		}
		return ENTRY_STATUS.stream().filter(entryStatus::contains).toArray(RepositoryEntryStatusEnum[]::new);
	}

	private static Map<String,Object> item(Long key, String displayName, String externalRef, RepositoryEntryStatusEnum status,
			RepositoryEntryLifecycle lifecycle, String teaser, boolean marked) {
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", key);
		item.put("displayName", displayName);
		putText(item, "externalRef", externalRef);
		item.put("status", status == null ? null : status.name());
		if(lifecycle != null) {
			putDate(item, "lifecycleFrom", lifecycle.getValidFrom());
			putDate(item, "lifecycleTo", lifecycle.getValidTo());
		}
		item.put("marked", Boolean.valueOf(marked));
		putText(item, "teaser", teaser);
		item.put("url", McpToolSupport.url("RepositoryEntry", key));
		return item;
	}

	private static void putAssessment(Map<String,Object> item, Double completion, Float score, Boolean passed) {
		if(completion != null) {
			item.put("completion", completion);
		}
		if(score != null) {
			item.put("score", score);
		}
		if(passed != null) {
			item.put("passed", passed);
		}
	}

	private static void putText(Map<String,Object> item, String name, String value) {
		if(StringHelper.containsNonWhitespace(value)) {
			item.put(name, value.trim());
		}
	}

	private static void putDate(Map<String,Object> item, String name, Date date) {
		if(date != null) {
			item.put(name, McpToolSupport.isoDate(date));
		}
	}

	private static String plain(String html) {
		return StringHelper.containsNonWhitespace(html) ? FilterFactory.getHtmlTagAndDescapingFilter().filter(html) : null;
	}

	private static String line(Map<String,Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append(item.get("displayName")).append(" (key ").append(item.get("key"));
		if(item.containsKey("externalRef")) {
			sb.append(", ref ").append(item.get("externalRef"));
		}
		sb.append(", ").append(item.get("status"));
		if(item.containsKey("lifecycleFrom") || item.containsKey("lifecycleTo")) {
			sb.append(", ").append(item.getOrDefault("lifecycleFrom", "open"))
			  .append(" to ").append(item.getOrDefault("lifecycleTo", "open"));
		}
		if(item.containsKey("completion")) {
			Double completion = (Double)item.get("completion");
			sb.append(", ").append(Math.round(completion.doubleValue() * 100d)).append("% done");
		}
		if(item.containsKey("passed")) {
			sb.append(Boolean.TRUE.equals(item.get("passed")) ? ", passed" : ", not passed");
		}
		if(item.containsKey("score")) {
			sb.append(", score ").append(AssessmentHelper.getRoundedScore((Float)item.get("score")));
		}
		if(item.containsKey("grade")) {
			sb.append(", grade ").append(item.get("grade"));
		}
		return sb.append(") ").append(item.get("url")).toString();
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS, "default", ACTION_LIST,
				"description", "list: the courses of the account; get: one course with description and objectives"));
		properties.put("role", Map.of("type", "string", "enum", McpToolSupport.names(ROLES), "default", GroupRoles.participant.name(),
				"description", "list only: the course role of the account, the membership filter of My courses "
						+ "(Booked as participant, coach or owner), granted directly, through a group or through a curriculum element"));
		properties.put("period", Map.of("type", "string", "enum", McpToolSupport.names(Period.values()),
				"description", "list only: the execution period filter of My courses. current: the period has started and not ended; "
						+ "upcoming: the period starts in the future; finished: the period has ended. "
						+ "A course without execution period matches none of them. Absent: no period filter"));
		properties.put("entryStatus", Map.of("type", "array",
				"items", Map.of("type", "string", "enum", McpToolSupport.names(ENTRY_STATUS)),
				"description", "list only: the status of the learning resource, one or more of preparation (Preparation), review (Review), "
						+ "coachpublished (Access for coach), published (Published), closed (Finished). Absent: all of them"));
		properties.put("query", Map.of("type", "string", "maxLength", QUERY_MAX_LENGTH,
				"description", "list only: title, external reference or key filter, case-insensitive substring"));
		properties.put("courseKey", Map.of("type", "integer", "description", "get only, required: the key of the course"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "list only: maximum number of courses returned"));
		return Map.of("type", "object", "properties", properties, "additionalProperties", Boolean.FALSE);
	}

	private static Map<String,Object> outputSchema() {
		Map<String,Object> itemProperties = new LinkedHashMap<>();
		itemProperties.put("key", Map.of("type", "integer", "description", "The course key, the courseKey of the action get"));
		itemProperties.put("displayName", Map.of("type", "string"));
		itemProperties.put("externalRef", Map.of("type", "string"));
		itemProperties.put("status", Map.of("type", "string",
				"description", "Status of the learning resource: preparation, review, coachpublished, published or closed (Finished)"));
		itemProperties.put("lifecycleFrom", Map.of("type", "string", "format", "date", "description", "Start of the execution period"));
		itemProperties.put("lifecycleTo", Map.of("type", "string", "format", "date", "description", "End of the execution period"));
		itemProperties.put("completion", Map.of("type", "number", "description", "Own progress from 0 to 1, only where OpenOlat shows it. "
				+ "list: course published or finished, not for a conventional course below 1. get: learning path course only"));
		itemProperties.put("score", Map.of("type", "number", "description", "Own score in the course, only where OpenOlat shows it. "
				+ "list: only together with completion. get: efficiency statement enabled and the course evaluates a score"));
		itemProperties.put("passed", Map.of("type", "boolean", "description", "Own passed status, only where OpenOlat shows it. "
				+ "list: only together with completion. get: efficiency statement enabled and the course evaluates passed. "
				+ "Absent when not assessed"));
		itemProperties.put("grade", Map.of("type", "string", "description", "get only: own grade, the performance class or the grade value, "
				+ "if the efficiency statement is enabled, the grade module is enabled and the account has a grade"));
		itemProperties.put("marked", Map.of("type", "boolean", "description", "true if the account bookmarked the course"));
		itemProperties.put("teaser", Map.of("type", "string"));
		itemProperties.put("url", Map.of("type", "string", "description", "Link to the course in OpenOlat"));
		itemProperties.put("description", Map.of("type", "string", "description", "get only, as plain text"));
		itemProperties.put("objectives", Map.of("type", "string", "description", "get only, learning objectives as plain text"));
		itemProperties.put("authors", Map.of("type", "string", "description", "get only"));
		itemProperties.put("educationalType", Map.of("type", "string", "description", "get only, identifier of the educational type"));
		Map<String,Object> itemSchema = Map.of("type", "object", "properties", itemProperties,
				"required", List.of("key", "displayName", "status", "marked", "url"));

		return McpToolSupport.queryOutputSchema(itemSchema, Map.of());
	}
}
