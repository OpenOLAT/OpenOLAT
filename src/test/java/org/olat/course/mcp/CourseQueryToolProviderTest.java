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

import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.DateUtils;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.STCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.mcp.McpGrants;
import org.olat.mcp.manager.McpJsonSchemaValidator;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The tool course_query: lists per role, period and entry status, the memberships through
 * business groups and curriculum elements, the query filter, the limit, the
 * access check of get and the argument validation.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class CourseQueryToolProviderTest extends OlatTestCase {

	/** The fixture is created once, the identities are fresh, the lists see only these courses */
	private static Fixture fixture;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
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
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private CourseQueryToolProvider provider;
	@Autowired
	private OrganisationModule organisationModule;

	private boolean organisationEnabledBefore;

	private record Fixture(String token, Identity owner, Identity otherAuthor, Identity participant, Identity coach, Identity stranger,
			RepositoryEntry current, RepositoryEntry upcoming, RepositoryEntry closed, RepositoryEntry preparation,
			RepositoryEntry coachPublished, RepositoryEntry openAccess, RepositoryEntry image) {
		//
	}

	/**
	 * The open access offer and its check need the organisations of the user:
	 * without the organisation module the list is empty. An earlier test of
	 * the suite may leave the module disabled.
	 */
	@Before
	public void enableOrganisationsAndCreateFixture() {
		organisationEnabledBefore = organisationModule.isEnabled();
		organisationModule.setEnabled(true);
		createFixture();
	}

	@After
	public void restoreOrganisations() {
		organisationModule.setEnabled(organisationEnabledBefore);
	}

	private void createFixture() {
		if(fixture != null) {
			return;
		}
		String token = JunitTestHelper.miniRandom();
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-cq-own");
		Identity otherAuthor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-cq-aut");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-par");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-coa");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-str");

		Date now = new Date();
		RepositoryEntry current = course(owner, "MCP " + token + " Alpha current", RepositoryEntryStatusEnum.published,
				DateUtils.addDays(now, -10), DateUtils.addDays(now, 10));
		RepositoryEntry upcoming = course(owner, "MCP " + token + " Beta upcoming", RepositoryEntryStatusEnum.published,
				DateUtils.addDays(now, 20), DateUtils.addDays(now, 40));
		RepositoryEntry closed = course(owner, "MCP " + token + " Gamma closed", RepositoryEntryStatusEnum.closed,
				DateUtils.addDays(now, -60), DateUtils.addDays(now, -30));
		RepositoryEntry preparation = course(owner, "MCP " + token + " Delta preparation", RepositoryEntryStatusEnum.preparation, null, null);
		RepositoryEntry coachPublished = course(owner, "MCP " + token + " Epsilon coach", RepositoryEntryStatusEnum.coachpublished, null, null);
		RepositoryEntry openAccess = course(otherAuthor, "MCP " + token + " Zeta open", RepositoryEntryStatusEnum.published, null, null);
		RepositoryEntry image = JunitTestHelper.createRandomRepositoryEntry(owner);

		for(RepositoryEntry entry:List.of(current, upcoming, closed, preparation, coachPublished)) {
			repositoryService.addRole(participant, entry, GroupRoles.participant.name());
		}
		repositoryService.addRole(coach, current, GroupRoles.coach.name());
		repositoryService.addRole(coach, coachPublished, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		// open access for all users of the default organisation
		openAccess = repositoryManager.setAccess(openAccess, true, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, false, false, null);
		Offer offer = acService.createOffer(openAccess.getOlatResource(), openAccess.getDisplayname());
		offer.setOpenAccess(true);
		offer = acService.save(offer);
		List<OrganisationRef> offerOrganisationRefs = acService.getOfferOrganisations(stranger);
		Organisation organisation = organisationService.getOrganisation(offerOrganisationRefs.get(0));
		acService.updateOfferOrganisations(offer, List.of(organisation));

		// bookmark and own progress of the participant in the current course, a
		// conventional course without efficiency statement: the UI shows no progress
		markManager.setMark(current, participant, null, "[RepositoryEntry:" + current.getKey() + "]");
		configure(current, ConditionNodeAccessProvider.TYPE, false, false);
		rootAssessment(participant, current, 0.5d);

		fixture = new Fixture(token, owner, otherAuthor, participant, coach, stranger,
				current, upcoming, closed, preparation, coachPublished, openAccess, image);
	}

	private RepositoryEntry course(Identity author, String displayName, RepositoryEntryStatusEnum status, Date from, Date to) {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, displayName, status);
		Assertions.assertThat(entry).isNotNull();
		entry = repositoryService.loadByKey(entry.getKey());
		entry.setDescription("<p>About <b>MCP</b> &amp; more</p>");
		entry.setObjectives("<ul><li>Objective one</li></ul>");
		if(from != null) {
			RepositoryEntryLifecycle lifecycle = lifecycleDao.create(displayName, null, true, from, to);
			entry.setLifecycle(lifecycle);
		}
		entry = repositoryService.update(entry);
		dbInstance.commitAndCloseSession();
		return entry;
	}

	/**
	 * Sets the node access type and the efficiency statement of the course,
	 * optionally the score (sum) and passed (progress) evaluation of the root node.
	 */
	private void configure(RepositoryEntry entry, String nodeAccessType, boolean efficiencyStatement, boolean rootScoreAndPassed) {
		Long resourceableId = entry.getOlatResource().getResourceableId();
		ICourse course = CourseFactory.openCourseEditSession(resourceableId);
		if(rootScoreAndPassed) {
			ModuleConfiguration rootConfig = course.getRunStructure().getRootNode().getModuleConfiguration();
			rootConfig.setStringValue(STCourseNode.CONFIG_SCORE_KEY, STCourseNode.CONFIG_SCORE_VALUE_SUM);
			rootConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
			CourseFactory.saveCourse(resourceableId);
		}
		CourseConfig courseConfig = course.getCourseConfig().clone();
		courseConfig.setNodeAccessType(nodeAccessType);
		courseConfig.setEfficiencyStatementIsEnabled(efficiencyStatement);
		CourseFactory.setCourseConfig(resourceableId, courseConfig);
		CourseFactory.closeCourseEditSession(resourceableId, true);
		dbInstance.commitAndCloseSession();
	}

	/**
	 * The assessment entry of the root node with the completion, the score 4.5
	 * and passed.
	 */
	private void rootAssessment(Identity participant, RepositoryEntry entry, double completion) {
		String rootIdent = CourseFactory.loadCourse(entry).getRunStructure().getRootNode().getIdent();
		AssessmentEntry assessment = assessmentService.getOrCreateAssessmentEntry(participant, null, entry, rootIdent,
				Boolean.TRUE, null, false);
		assessment.setCompletion(Double.valueOf(completion));
		assessment.setScore(new BigDecimal("4.5"));
		assessment.setPassed(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(assessment);
		dbInstance.commitAndCloseSession();
	}

	/**
	 * A course with one participant, the root assessment entry of
	 * the participant and the configuration of the course.
	 */
	private RepositoryEntry assessedCourse(Identity participant, String name, RepositoryEntryStatusEnum status,
			String nodeAccessType, boolean efficiencyStatement, boolean rootScoreAndPassed, double completion) {
		// own author and title: the lists of the fixture stay unchanged
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-cq-pra");
		RepositoryEntry entry = course(author, "MCP progress " + name, status, null, null);
		// the status event of the new course evaluates a learning path course
		// for all participants (ScoreAccountingProcessor), a late evaluation
		// would overwrite the values of the test: consume it while the course
		// is still conventional
		waitMessageAreConsumed();
		configure(entry, nodeAccessType, efficiencyStatement, rootScoreAndPassed);
		repositoryService.addRole(participant, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		rootAssessment(participant, entry, completion);
		return entry;
	}

	private static Map<String,Object> listItem(McpToolResult result, RepositoryEntry entry) {
		return items(result).stream().filter(item -> entry.getKey().equals(item.get("key"))).findFirst().orElseThrow();
	}

	private McpToolResult call(Identity identity, Object... keyValues) {
		Map<String,Object> arguments = new HashMap<>();
		for(int i=0; i<keyValues.length; i+=2) {
			arguments.put((String)keyValues[i], keyValues[i + 1]);
		}
		McpToolContext context = new McpToolContext(identity, securityManager.getRoles(identity),
				new McpGrants(Set.of(CourseQueryToolProvider.NAME), null, List.of("test")), null, Locale.ENGLISH, "127.0.0.1");
		McpToolResult result = provider.execute(context, arguments);
		dbInstance.commitAndCloseSession();
		Assertions.assertThat(result).isNotNull();
		if(!result.error()) {
			// the SDK validates the structured content against the output schema
			Assertions.assertThat(new McpJsonSchemaValidator(new ObjectMapper())
					.validate(provider.getDefinition().outputSchema(), result.structuredContent()).valid())
				.as("output schema").isTrue();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> items(McpToolResult result) {
		Assertions.assertThat(result.error()).as(result.text()).isFalse();
		return (List<Map<String,Object>>)((Map<String,Object>)result.structuredContent()).get("items");
	}

	private static List<Object> keys(McpToolResult result) {
		return items(result).stream().map(item -> item.get("key")).toList();
	}

	@SuppressWarnings("unchecked")
	private static Map<String,Object> item(McpToolResult result) {
		Assertions.assertThat(result.error()).as(result.text()).isFalse();
		return (Map<String,Object>)((Map<String,Object>)result.structuredContent()).get("item");
	}

	@Test
	public void definition() {
		Assertions.assertThat(provider.getToolName()).isEqualTo("course_query").matches(CourseQueryToolProvider.NAME_PATTERN);
		Assertions.assertThat(provider.getPack()).isEqualTo(CourseMcpPack.NAME);
		Assertions.assertThat(provider.getScope()).isEqualTo(McpToolScope.READ);
		Assertions.assertThat(provider.isAvailable()).isTrue();
		Assertions.assertThat(provider.getDefinition().inputSchema()).containsEntry("type", "object");
		Assertions.assertThat(provider.getDefinition().outputSchema()).containsEntry("type", "object");
		Assertions.assertThat(provider.getDefinition().destructive()).isFalse();
	}

	@Test
	public void inputSchemaAcceptsSpecArguments() {
		McpJsonSchemaValidator validator = new McpJsonSchemaValidator(new ObjectMapper());
		Map<String,Object> schema = provider.getDefinition().inputSchema();
		Assertions.assertThat(validator.validate(schema, Map.of("action", "list", "role", "coach", "period", "current",
				"entryStatus", List.of("published", "closed"), "query", "x", "limit", 10)).valid()).isTrue();
		Assertions.assertThat(validator.validate(schema, Map.of("action", "get", "courseKey", 12)).valid()).isTrue();
		Assertions.assertThat(validator.validate(schema, Map.of("role", "guest")).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("role", "waiting")).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("period", "old")).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("entryStatus", List.of("trash"))).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("entryStatus", List.of("deleted"))).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("status", "all")).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("limit", 500)).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("unknown", 1)).valid()).isFalse();
	}

	@Test
	public void listAsParticipantDefaultIsWholeMembership() {
		McpToolResult result = call(fixture.participant());
		// every status of the membership from preparation to closed, like the "My courses" list
		Assertions.assertThat(keys(result)).containsExactlyInAnyOrder(fixture.current().getKey(), fixture.upcoming().getKey(),
				fixture.closed().getKey(), fixture.preparation().getKey(), fixture.coachPublished().getKey());
		Map<String,Object> current = items(result).stream()
				.filter(item -> fixture.current().getKey().equals(item.get("key"))).findFirst().orElseThrow();
		Assertions.assertThat(current)
			.containsEntry("displayName", fixture.current().getDisplayname())
			.containsEntry("status", "published")
			.containsEntry("marked", Boolean.TRUE)
			.containsKey("lifecycleFrom")
			.containsKey("lifecycleTo")
			.containsEntry("url", McpToolSupport.url("RepositoryEntry", fixture.current().getKey()));
		Assertions.assertThat(result.text()).startsWith("5 courses:").contains(fixture.current().getDisplayname());
		// conventional course with a completion below 100%: the list shows no completion ring
		Assertions.assertThat(current).doesNotContainKeys("completion", "score", "passed", "grade");
		Assertions.assertThat(result.text()).doesNotContain("% done").doesNotContain("passed");
	}

	@Test
	public void listAsParticipantPerPeriod() {
		Assertions.assertThat(keys(call(fixture.participant(), "period", "current")))
			.containsExactly(fixture.current().getKey());
		Assertions.assertThat(keys(call(fixture.participant(), "period", "upcoming")))
			.containsExactly(fixture.upcoming().getKey());
		Assertions.assertThat(keys(call(fixture.participant(), "period", "finished")))
			.containsExactly(fixture.closed().getKey());
	}

	/**
	 * The period filter of the "My courses" list compares the execution
	 * period: a course without execution period is in none of them.
	 */
	@Test
	public void listCourseWithoutPeriodIsInNoPeriod() {
		for(String period:List.of("current", "upcoming", "finished")) {
			Assertions.assertThat(keys(call(fixture.participant(), "period", period))).as(period)
				.doesNotContain(fixture.preparation().getKey(), fixture.coachPublished().getKey());
			Assertions.assertThat(keys(call(fixture.coach(), "role", "coach", "period", period))).as("coach " + period)
				.doesNotContain(fixture.coachPublished().getKey());
		}
		// without period filter they are listed
		Assertions.assertThat(keys(call(fixture.coach(), "role", "coach"))).contains(fixture.coachPublished().getKey());
	}

	@Test
	public void listAsParticipantPerEntryStatus() {
		Assertions.assertThat(keys(call(fixture.participant(), "entryStatus", List.of("closed"))))
			.containsExactly(fixture.closed().getKey());
		Assertions.assertThat(keys(call(fixture.participant(), "entryStatus", "closed")))
			.containsExactly(fixture.closed().getKey());
		Assertions.assertThat(keys(call(fixture.participant(), "entryStatus", List.of("preparation", "coachpublished"))))
			.containsExactlyInAnyOrder(fixture.preparation().getKey(), fixture.coachPublished().getKey());
		// period and status combined
		Assertions.assertThat(keys(call(fixture.participant(), "period", "finished", "entryStatus", List.of("published")))).isEmpty();
		Assertions.assertThat(keys(call(fixture.participant(), "period", "upcoming", "entryStatus", List.of("published"))))
			.containsExactly(fixture.upcoming().getKey());
	}

	@Test
	public void listAsCoach() {
		Assertions.assertThat(keys(call(fixture.coach(), "role", "coach")))
			.containsExactlyInAnyOrder(fixture.current().getKey(), fixture.coachPublished().getKey());
		// the coach is no participant
		Assertions.assertThat(keys(call(fixture.coach(), "role", "participant"))).isEmpty();
	}

	@Test
	public void listAsOwner() {
		Assertions.assertThat(keys(call(fixture.owner(), "role", "owner")))
			.containsExactlyInAnyOrder(fixture.current().getKey(), fixture.upcoming().getKey(), fixture.closed().getKey(),
					fixture.preparation().getKey(), fixture.coachPublished().getKey());
		Assertions.assertThat(keys(call(fixture.owner(), "role", "owner", "entryStatus", List.of("closed"))))
			.containsExactly(fixture.closed().getKey());
	}

	/** Every status of the lifecycle, in the order of the enumeration */
	private static final RepositoryEntryStatusEnum[] ALL_STATUS = RepositoryEntryStatusEnum.values();
	/** The status every member sees in the list, as the "My courses" list shows them */
	private static final List<RepositoryEntryStatusEnum> LIST_VISIBLE = List.of(
			RepositoryEntryStatusEnum.preparation, RepositoryEntryStatusEnum.review,
			RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published, RepositoryEntryStatusEnum.closed);
	/** The status a member may open with get, as the course runtime launches them */
	private static final List<RepositoryEntryStatusEnum> PARTICIPANT_VISIBLE = List.of(
			RepositoryEntryStatusEnum.published, RepositoryEntryStatusEnum.closed);
	private static final List<RepositoryEntryStatusEnum> COACH_VISIBLE = List.of(
			RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published, RepositoryEntryStatusEnum.closed);
	private static final List<RepositoryEntryStatusEnum> OWNER_VISIBLE = LIST_VISIBLE;

	/**
	 * One course per status of the lifecycle, preparation to deleted.
	 */
	private Map<RepositoryEntryStatusEnum,RepositoryEntry> coursesInEveryStatus(Identity author, String name) {
		Map<RepositoryEntryStatusEnum,RepositoryEntry> entries = new EnumMap<>(RepositoryEntryStatusEnum.class);
		for(RepositoryEntryStatusEnum status:ALL_STATUS) {
			entries.put(status, course(author, "MCP " + fixture.token() + " " + name + " " + status.name(), status, null, null));
		}
		return entries;
	}

	/**
	 * The member sees in the list every course of its membership from
	 * preparation to closed, like the "My courses" list, and opens with get
	 * exactly the courses in the launchable status of its role; the entryStatus
	 * argument narrows the list, the other roles see nothing.
	 */
	private void assertVisibility(Identity member, String role, Map<RepositoryEntryStatusEnum,RepositoryEntry> entries,
			List<RepositoryEntryStatusEnum> visible) {
		List<Long> visibleKeys = LIST_VISIBLE.stream().map(status -> entries.get(status).getKey()).toList();
		List<RepositoryEntryStatusEnum> notClosed = LIST_VISIBLE.stream().filter(status -> status != RepositoryEntryStatusEnum.closed).toList();
		List<Long> notClosedKeys = notClosed.stream().map(status -> entries.get(status).getKey()).toList();

		Assertions.assertThat(keys(call(member, "role", role))).as(role + " all")
			.containsExactlyInAnyOrderElementsOf(visibleKeys);
		Assertions.assertThat(keys(call(member, "role", role, "entryStatus", notClosed.stream().map(Enum::name).toList())))
			.as(role + " not closed").containsExactlyInAnyOrderElementsOf(notClosedKeys);
		Assertions.assertThat(keys(call(member, "role", role, "entryStatus", List.of("closed")))).as(role + " closed")
			.containsExactly(entries.get(RepositoryEntryStatusEnum.closed).getKey());
		for(String otherRole:List.of("participant", "coach", "owner")) {
			if(!otherRole.equals(role)) {
				Assertions.assertThat(keys(call(member, "role", otherRole))).as(role + " as " + otherRole).isEmpty();
			}
		}

		// get follows the launch rule of the course runtime per role
		for(RepositoryEntryStatusEnum status:ALL_STATUS) {
			RepositoryEntry entry = entries.get(status);
			McpToolResult result = call(member, "action", "get", "courseKey", entry.getKey());
			if(visible.contains(status)) {
				Assertions.assertThat(item(result)).as(role + " get " + status).containsEntry("key", entry.getKey());
			} else {
				Assertions.assertThat(result.error()).as(role + " get " + status).isTrue();
				assertNotFound(result, entry);
			}
		}
	}

	/**
	 * Memberships directly on the course: participant, coach and owner.
	 */
	@Test
	public void visibilityByStatusOfDirectMembers() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-cq-dir");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-dpar");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-dcoa");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-down");
		Map<RepositoryEntryStatusEnum,RepositoryEntry> entries = coursesInEveryStatus(author, "Direct");
		for(RepositoryEntry entry:entries.values()) {
			repositoryService.addRole(participant, entry, GroupRoles.participant.name());
			repositoryService.addRole(coach, entry, GroupRoles.coach.name());
			repositoryService.addRole(owner, entry, GroupRoles.owner.name());
		}
		dbInstance.commitAndCloseSession();

		assertVisibility(participant, "participant", entries, PARTICIPANT_VISIBLE);
		assertVisibility(coach, "coach", entries, COACH_VISIBLE);
		assertVisibility(owner, "owner", entries, OWNER_VISIBLE);
	}

	/**
	 * Participant and coach of a business group linked to the course are
	 * members of the course with the role of the group.
	 */
	@Test
	public void visibilityByStatusOfBusinessGroupMembers() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-cq-grp");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-gpar");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-gcoa");
		Map<RepositoryEntryStatusEnum,RepositoryEntry> entries = coursesInEveryStatus(author, "Group");
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "MCP group " + fixture.token(), null,
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		for(RepositoryEntry entry:entries.values()) {
			businessGroupService.addResourceTo(group, entry);
		}
		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		assertVisibility(participant, "participant", entries, PARTICIPANT_VISIBLE);
		assertVisibility(coach, "coach", entries, COACH_VISIBLE);
	}

	/**
	 * Participant, coach and owner of a curriculum element (course planner)
	 * the course is attached to are members of the course with that role.
	 */
	@Test
	public void visibilityByStatusOfCurriculumElementMembers() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-cq-cur");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-cpar");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-ccoa");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-cown");
		Map<RepositoryEntryStatusEnum,RepositoryEntry> entries = coursesInEveryStatus(author, "Curriculum");
		Curriculum curriculum = curriculumService.createCurriculum("mcp-cq-" + fixture.token(), "MCP curriculum", null, false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("mcp-cq-el-" + fixture.token(), "MCP element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		for(RepositoryEntry entry:entries.values()) {
			curriculumService.addRepositoryEntry(element, entry, false);
		}
		curriculumService.addMember(element, participant, CurriculumRoles.participant, author);
		curriculumService.addMember(element, coach, CurriculumRoles.coach, author);
		curriculumService.addMember(element, owner, CurriculumRoles.owner, author);
		dbInstance.commitAndCloseSession();

		assertVisibility(participant, "participant", entries, PARTICIPANT_VISIBLE);
		assertVisibility(coach, "coach", entries, COACH_VISIBLE);
		assertVisibility(owner, "owner", entries, OWNER_VISIBLE);
	}

	@Test
	public void authoringIsNoRole() {
		McpToolResult result = call(fixture.owner(), "role", "authoring");
		Assertions.assertThat(result.error()).isTrue();
		Assertions.assertThat(result.text()).contains("Unknown role 'authoring'");
		Assertions.assertThat(new McpJsonSchemaValidator(new ObjectMapper())
				.validate(provider.getDefinition().inputSchema(), Map.of("role", "authoring")).valid()).isFalse();
	}

	@Test
	public void listForStrangerIsEmpty() {
		McpToolResult result = call(fixture.stranger());
		Assertions.assertThat(keys(result)).isEmpty();
		Assertions.assertThat(result.text()).isEqualTo("0 courses.");
	}

	@Test
	public void listWithQuery() {
		Assertions.assertThat(keys(call(fixture.participant(), "query", fixture.token() + " alpha")))
			.containsExactly(fixture.current().getKey());
		Assertions.assertThat(keys(call(fixture.owner(), "role", "owner", "query", fixture.token() + " gamma")))
			.containsExactly(fixture.closed().getKey());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void listLimitAndTruncated() {
		McpToolResult result = call(fixture.participant(), "limit", 2);
		Map<String,Object> content = (Map<String,Object>)result.structuredContent();
		Assertions.assertThat(items(result)).hasSize(2);
		Assertions.assertThat(content).containsEntry("total", Integer.valueOf(5)).containsEntry("truncated", Boolean.TRUE);

		McpToolResult complete = call(fixture.participant(), "limit", 5);
		Assertions.assertThat((Map<String,Object>)complete.structuredContent())
			.containsEntry("total", Integer.valueOf(5)).containsEntry("truncated", Boolean.FALSE);
		// limit below 1 is bounded
		Assertions.assertThat(items(call(fixture.participant(), "limit", 0))).hasSize(1);
	}

	@Test
	public void getAsParticipant() {
		Map<String,Object> item = item(call(fixture.participant(), "action", "get", "courseKey", fixture.current().getKey()));
		Assertions.assertThat(item)
			.containsEntry("key", fixture.current().getKey())
			.containsEntry("status", "published")
			.containsEntry("marked", Boolean.TRUE)
			.containsEntry("description", "About MCP & more")
			.containsEntry("objectives", "Objective one")
			.containsKey("url");
		// conventional course without efficiency statement: the info page shows no progress
		Assertions.assertThat(item).doesNotContainKeys("completion", "score", "passed", "grade");
	}

	/**
	 * A conventional course with a completion of 100%: the list shows the
	 * ring with score and passed. Without efficiency statement the info page
	 * shows nothing.
	 */
	@Test
	public void progressConventionalCourseCompleted() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-pr1");
		RepositoryEntry entry = assessedCourse(participant, "Conventional completed", RepositoryEntryStatusEnum.published,
				ConditionNodeAccessProvider.TYPE, false, false, 1.0d);

		McpToolResult list = call(participant);
		Assertions.assertThat(listItem(list, entry))
			.containsEntry("completion", Double.valueOf(1.0d))
			.containsEntry("score", Float.valueOf(4.5f))
			.containsEntry("passed", Boolean.TRUE)
			.doesNotContainKey("grade");
		Assertions.assertThat(list.text()).contains("100% done, passed, score 4.5");

		Map<String,Object> item = item(call(participant, "action", "get", "courseKey", entry.getKey()));
		Assertions.assertThat(item).doesNotContainKeys("completion", "score", "passed", "grade");
	}

	/**
	 * A learning path course without efficiency statement: list and info page
	 * show the completion only.
	 */
	@Test
	public void progressLearningPathWithoutEfficiencyStatement() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-pr2");
		RepositoryEntry entry = assessedCourse(participant, "Learning path", RepositoryEntryStatusEnum.published,
				LearningPathNodeAccessProvider.TYPE, false, false, 0.5d);

		McpToolResult list = call(participant);
		Assertions.assertThat(listItem(list, entry))
			.containsEntry("completion", Double.valueOf(0.5d))
			.doesNotContainKey("grade");

		McpToolResult get = call(participant, "action", "get", "courseKey", entry.getKey());
		Assertions.assertThat(item(get))
			.containsEntry("completion", Double.valueOf(0.5d))
			.doesNotContainKeys("score", "passed", "grade");
		Assertions.assertThat(get.text()).contains("50% done").doesNotContain("passed").doesNotContain("score");
	}

	/**
	 * A learning path course with efficiency statement and the evaluation of
	 * score and passed on the root node: list and info page show all three.
	 */
	@Test
	public void progressLearningPathWithEfficiencyStatement() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-pr3");
		RepositoryEntry entry = assessedCourse(participant, "Learning path assessed", RepositoryEntryStatusEnum.published,
				LearningPathNodeAccessProvider.TYPE, true, true, 0.5d);

		McpToolResult list = call(participant);
		Assertions.assertThat(listItem(list, entry))
			.containsEntry("completion", Double.valueOf(0.5d))
			.containsEntry("score", Float.valueOf(4.5f))
			.containsEntry("passed", Boolean.TRUE)
			.doesNotContainKey("grade");

		McpToolResult get = call(participant, "action", "get", "courseKey", entry.getKey());
		Assertions.assertThat(item(get))
			.containsEntry("completion", Double.valueOf(0.5d))
			.containsEntry("score", Float.valueOf(4.5f))
			.containsEntry("passed", Boolean.TRUE)
			.doesNotContainKey("grade");
		Assertions.assertThat(get.text()).contains("50% done, passed, score 4.5");
	}

	/**
	 * A course in preparation: the list shows no progress, the row of the
	 * "My courses" list has values only for published and closed courses.
	 */
	@Test
	public void progressCourseInPreparation() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-cq-pr4");
		RepositoryEntry entry = assessedCourse(participant, "Learning path preparation", RepositoryEntryStatusEnum.preparation,
				LearningPathNodeAccessProvider.TYPE, true, true, 1.0d);

		McpToolResult list = call(participant);
		Assertions.assertThat(listItem(list, entry)).doesNotContainKeys("completion", "score", "passed", "grade");
	}

	@Test
	public void getAsCoachOfCoachPublishedCourse() {
		Map<String,Object> item = item(call(fixture.coach(), "action", "get", "courseKey", fixture.coachPublished().getKey().toString()));
		Assertions.assertThat(item).containsEntry("status", "coachpublished").containsEntry("marked", Boolean.FALSE);
	}

	@Test
	public void getOpenAccessAsStranger() {
		Map<String,Object> item = item(call(fixture.stranger(), "action", "get", "courseKey", fixture.openAccess().getKey()));
		Assertions.assertThat(item).containsEntry("key", fixture.openAccess().getKey());
	}

	@Test
	public void getAsStrangerIsDenied() {
		assertNotFound(call(fixture.stranger(), "action", "get", "courseKey", fixture.current().getKey()),
				fixture.current());
	}

	@Test
	public void getPreparationAsParticipantIsDenied() {
		assertNotFound(call(fixture.participant(), "action", "get", "courseKey", fixture.preparation().getKey()),
				fixture.preparation());
	}

	/**
	 * Denied, missing and not a course give the same answer without title or type.
	 */
	private static void assertNotFound(McpToolResult result, RepositoryEntry entry) {
		Assertions.assertThat(result.error()).isTrue();
		Assertions.assertThat(result.structuredContent()).isNull();
		Assertions.assertThat(result.text())
			.isEqualTo("No course with key " + entry.getKey() + " that this account may view.")
			.doesNotContain(entry.getDisplayname());
	}

	@Test
	public void getPreparationAsOwner() {
		Map<String,Object> item = item(call(fixture.owner(), "action", "get", "courseKey", fixture.preparation().getKey()));
		Assertions.assertThat(item).containsEntry("status", "preparation");
	}

	@Test
	public void getNotACourse() {
		McpToolResult result = call(fixture.owner(), "action", "get", "courseKey", fixture.image().getKey());
		assertNotFound(result, fixture.image());
		Assertions.assertThat(result.text()).doesNotContain("Image").doesNotContain("type");
	}

	@Test
	public void getUnknownKey() {
		McpToolResult result = call(fixture.owner(), "action", "get", "courseKey", Long.valueOf(-1l));
		Assertions.assertThat(result.error()).isTrue();
		Assertions.assertThat(result.text()).isEqualTo("No course with key -1 that this account may view.");
	}

	@Test
	public void getUniformAnswerForMissingNotCourseAndDenied() {
		String missing = call(fixture.stranger(), "action", "get", "courseKey", Long.valueOf(-2l)).text();
		String image = call(fixture.stranger(), "action", "get", "courseKey", fixture.image().getKey()).text();
		String denied = call(fixture.stranger(), "action", "get", "courseKey", fixture.preparation().getKey()).text();
		Assertions.assertThat(missing.replace("-2", "K"))
			.isEqualTo(image.replace(fixture.image().getKey().toString(), "K"))
			.isEqualTo(denied.replace(fixture.preparation().getKey().toString(), "K"));
	}

	@Test
	public void queryIsBounded() {
		String tooLong = "x".repeat(CourseQueryToolProvider.QUERY_MAX_LENGTH + 1);
		McpToolResult result = call(fixture.participant(), "query", tooLong);
		Assertions.assertThat(result.error()).isTrue();
		Assertions.assertThat(result.text()).contains("longer than 200");
		Assertions.assertThat(keys(call(fixture.participant(), "query", "x".repeat(CourseQueryToolProvider.QUERY_MAX_LENGTH)))).isEmpty();
		Assertions.assertThat(new McpJsonSchemaValidator(new ObjectMapper())
				.validate(provider.getDefinition().inputSchema(), Map.of("query", tooLong)).valid()).isFalse();
	}

	@Test
	public void getWithoutKey() {
		McpToolResult result = call(fixture.owner(), "action", "get");
		Assertions.assertThat(result.error()).isTrue();
		Assertions.assertThat(result.text()).contains("courseKey");
	}

	@Test
	public void invalidArguments() {
		McpToolResult action = call(fixture.participant(), "action", "delete");
		Assertions.assertThat(action.error()).isTrue();
		Assertions.assertThat(action.text()).contains("Unknown action 'delete'");

		McpToolResult role = call(fixture.participant(), "role", "guest");
		Assertions.assertThat(role.error()).isTrue();
		Assertions.assertThat(role.text()).contains("Unknown role 'guest'");

		McpToolResult waiting = call(fixture.participant(), "role", "waiting");
		Assertions.assertThat(waiting.error()).isTrue();
		Assertions.assertThat(waiting.text()).contains("Unknown role 'waiting'. Use one of: participant, coach, owner.");

		McpToolResult period = call(fixture.participant(), "period", "old");
		Assertions.assertThat(period.error()).isTrue();
		Assertions.assertThat(period.text()).contains("Unknown period 'old'. Use one of: current, upcoming, finished.");

		McpToolResult trash = call(fixture.participant(), "entryStatus", List.of("published", "trash"));
		Assertions.assertThat(trash.error()).isTrue();
		Assertions.assertThat(trash.text()).contains("Unknown entryStatus 'trash'");

		McpToolResult deleted = call(fixture.participant(), "entryStatus", "deleted");
		Assertions.assertThat(deleted.error()).isTrue();
		Assertions.assertThat(deleted.text()).contains("Unknown entryStatus 'deleted'");
	}

	@Test
	public void contextWithoutRoles() {
		McpToolContext context = new McpToolContext(fixture.participant(), null,
				new McpGrants(Set.of(CourseQueryToolProvider.NAME), null, List.of("test")), null, Locale.ENGLISH, "127.0.0.1");
		McpToolResult result = provider.execute(context, Map.of());
		dbInstance.commitAndCloseSession();
		Assertions.assertThat(keys(result)).hasSize(5);
	}
}
