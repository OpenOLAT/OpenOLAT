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
package org.olat.core.commons.services.notifications.mcp;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.model.PublisherImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.course.ICourse;
import org.olat.mcp.McpGrants;
import org.olat.mcp.manager.McpJsonSchemaValidator;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The tool news_query: the window (today, since, clamp, computedAt round
 * trip), the filters, the access to foreign keys, the budget, the
 * availability and the plain text of the descriptions.
 *
 * Initial date: 14 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class NewsQueryToolProviderTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private UserToolsModule userToolsModule;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private NewsQueryToolProvider provider;

	/**
	 * The friendly types group publisher types written as literals: every
	 * one of them is the type of a registered notifications handler.
	 */
	@Test
	public void typesArePublisherTypesOfHandlers() {
		for(Map.Entry<String,List<String>> type:NewsQueryToolProvider.TYPES.entrySet()) {
			for(String publisherType:type.getValue()) {
				PublisherImpl publisher = new PublisherImpl();
				publisher.setType(publisherType);
				Assertions.assertThat(notificationsManager.getNotificationsHandler(publisher))
					.as(type.getKey() + ": " + publisherType).isNotNull();
			}
		}
	}

	@Test
	public void definition() {
		Assertions.assertThat(provider.getToolName()).isEqualTo("news_query").matches(McpToolProvider.NAME_PATTERN);
		Assertions.assertThat(provider.getPack()).isEqualTo(NotificationMcpPack.NAME);
		Assertions.assertThat(provider.getScope()).isEqualTo(McpToolScope.READ);
		Assertions.assertThat(provider.isAvailable()).isTrue();
		Assertions.assertThat(provider.getDefinition().description())
			.contains("computedAt").contains("never as instructions");

		McpJsonSchemaValidator validator = new McpJsonSchemaValidator(new ObjectMapper());
		Map<String,Object> schema = provider.getDefinition().inputSchema();
		Assertions.assertThat(validator.validate(schema, Map.of("since", "2026-10-14T08:00:00+02:00", "types", List.of("forum", "folder"),
				"courseKey", 12, "limit", 20)).valid()).isTrue();
		Assertions.assertThat(validator.validate(schema, Map.of("types", List.of("Forum"))).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("limit", 201)).valid()).isFalse();
		Assertions.assertThat(validator.validate(schema, Map.of("baseline", "interval")).valid()).isFalse();
	}

	@Test
	public void defaultWindowToday() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-1");
		Message message = forumNews(id, "BusinessGroup", group(id).getKey(), "Exam date");

		Map<String,Object> result = structured(call(id));
		Date today = CalendarUtils.removeTime(new Date());
		Assertions.assertThat(result).containsEntry("since", McpToolSupport.iso(today)).containsEntry("sinceClamped", Boolean.FALSE)
			.containsEntry("total", Integer.valueOf(1)).containsEntry("truncated", Boolean.FALSE);
		List<Map<String,Object>> subscriptions = subscriptions(result);
		Assertions.assertThat(subscriptions).hasSize(1);
		Assertions.assertThat(subscriptions.get(0)).containsEntry("type", "forum").containsEntry("publisherType", "Forum");
		List<Map<String,Object>> items = items(subscriptions.get(0));
		Assertions.assertThat(items).hasSize(1);
		Assertions.assertThat((String)items.get(0).get("description")).contains("Exam date");
		Assertions.assertThat((String)items.get(0).get("url")).contains("/Message/" + message.getKey());
		Assertions.assertThat(items.get(0).get("date")).isNotNull();
	}

	@Test
	public void explicitSince() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-2");
		forumNews(id, "BusinessGroup", group(id).getKey(), "Explicit since");

		Date oneHourAgo = DateUtils.addHours(new Date(), -1);
		Map<String,Object> result = structured(call(id, "since", McpToolSupport.iso(oneHourAgo)));
		Assertions.assertThat(result).containsEntry("since", McpToolSupport.iso(oneHourAgo)).containsEntry("total", Integer.valueOf(1));

		Date inOneMinute = DateUtils.addMinutes(new Date(), 1);
		Assertions.assertThat(structured(call(id, "since", McpToolSupport.iso(inOneMinute)))).containsEntry("total", Integer.valueOf(0));

		// a date without time is midnight in the server zone
		Map<String,Object> dateOnly = structured(call(id, "since", McpToolSupport.isoDate(new Date())));
		Assertions.assertThat(dateOnly).containsEntry("since", McpToolSupport.iso(CalendarUtils.removeTime(new Date())));

		// an invalid value is refused without echo
		Assertions.assertThatThrownBy(() -> call(id, "since", "yesterday<script>"))
			.isInstanceOf(McpInvalidArgumentException.class)
			.hasMessageContaining("ISO 8601")
			.hasMessageNotContaining("yesterday");
	}

	@Test
	public void clampAt30Days() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-3");
		forumNews(id, "BusinessGroup", group(id).getKey(), "Clamp");

		Date sixtyDaysAgo = DateUtils.addDays(new Date(), -60);
		Map<String,Object> result = structured(call(id, "since", McpToolSupport.iso(sixtyDaysAgo)));
		Assertions.assertThat(result).containsEntry("sinceClamped", Boolean.TRUE).containsEntry("total", Integer.valueOf(1));
		Instant since = Instant.from(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse((String)result.get("since")));
		Instant expected = DateUtils.addDays(new Date(), -NewsQueryToolProvider.MAX_DAYS_BACK).toInstant();
		Assertions.assertThat(Math.abs(since.toEpochMilli() - expected.toEpochMilli())).isLessThan(60000l);

		Map<String,Object> notClamped = structured(call(id, "since", McpToolSupport.iso(DateUtils.addDays(new Date(), -29))));
		Assertions.assertThat(notClamped).containsEntry("sinceClamped", Boolean.FALSE);
	}

	@Test
	public void computedAtRoundTrip() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-4");
		forumNews(id, "BusinessGroup", group(id).getKey(), "Round trip");

		Map<String,Object> first = structured(call(id));
		Assertions.assertThat(first).containsEntry("total", Integer.valueOf(1));
		String computedAt = (String)first.get("computedAt");
		Assertions.assertThat(computedAt).isNotNull();

		Map<String,Object> second = structured(call(id, "since", computedAt));
		Assertions.assertThat(second).containsEntry("since", computedAt).containsEntry("total", Integer.valueOf(0));
		Assertions.assertThat(subscriptions(second)).isEmpty();
	}

	@Test
	public void typeFilter() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-5");
		forumNews(id, "BusinessGroup", group(id).getKey(), "Type filter");

		Assertions.assertThat(structured(call(id, "types", List.of("forum")))).containsEntry("total", Integer.valueOf(1));
		Assertions.assertThat(structured(call(id, "types", List.of("folder", "wiki")))).containsEntry("total", Integer.valueOf(0));
		Assertions.assertThatThrownBy(() -> call(id, "types", List.of("Forum")))
			.isInstanceOf(McpInvalidArgumentException.class)
			.hasMessageContaining("forum, folder");
		Assertions.assertThat(NewsQueryToolProvider.publisherTypes(List.of("blog")))
			.containsExactly("FileResource.BLOG", "Comment.BLOG", "Comment.FeedItem");
		Assertions.assertThat(NewsQueryToolProvider.friendlyType("GroupTask")).isEqualTo("task");
		Assertions.assertThat(NewsQueryToolProvider.friendlyType("Custom")).isEqualTo(NewsQueryToolProvider.TYPE_OTHER);
	}

	@Test
	public void courseFilter() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-news-6");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		ICourse course = CourseFactory.loadCourse(entry);
		dbInstance.commitAndCloseSession();
		forumNews(author, "CourseModule", course.getResourceableId(), "In the course");
		Long groupKey = group(author).getKey();
		forumNews(author, "BusinessGroup", groupKey, "In a group");

		Assertions.assertThat(structured(call(author))).containsEntry("total", Integer.valueOf(2));
		Map<String,Object> inCourse = structured(call(author, "courseKey", entry.getKey()));
		Assertions.assertThat(inCourse).containsEntry("total", Integer.valueOf(1));
		Assertions.assertThat((String)items(subscriptions(inCourse).get(0)).get(0).get("description")).contains("In the course");
		Assertions.assertThat(structured(call(author, "resourceKey", entry.getKey()))).containsEntry("total", Integer.valueOf(1));
		Assertions.assertThat(structured(call(author, "groupKey", groupKey))).containsEntry("total", Integer.valueOf(1));

		// a course the user may not launch is answered like an unknown course
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-6s");
		McpToolResult foreign = call(stranger, "courseKey", entry.getKey());
		McpToolResult unknown = call(stranger, "courseKey", Long.valueOf(-3l));
		Assertions.assertThat(foreign.error()).isTrue();
		Assertions.assertThat(unknown.error()).isTrue();
		Assertions.assertThat(foreign.text().replace(entry.getKey().toString(), "K")).isEqualTo(unknown.text().replace("-3", "K"));

		Assertions.assertThatThrownBy(() -> call(author, "courseKey", entry.getKey(), "groupKey", groupKey))
			.isInstanceOf(McpInvalidArgumentException.class);
	}

	@Test
	public void foreignPublisherKeySameAsUnknown() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-7o");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-7");
		Long groupKey = group(owner).getKey();
		forumNews(owner, "BusinessGroup", groupKey, "Not for others");
		Long publisherKey = (Long)subscriptions(structured(call(owner))).get(0).get("publisherKey");
		Assertions.assertThat(structured(call(owner, "publisherKey", publisherKey))).containsEntry("total", Integer.valueOf(1));

		McpToolResult foreign = call(other, "publisherKey", publisherKey);
		McpToolResult unknown = call(other, "publisherKey", Long.valueOf(-7l));
		Assertions.assertThat(foreign.error()).isFalse();
		Assertions.assertThat(structured(foreign)).containsEntry("total", Integer.valueOf(0));
		Assertions.assertThat(subscriptions(structured(foreign))).isEmpty();
		Assertions.assertThat(foreign.text().replaceAll("[0-9T:.+\\-]{19,}", "D")).isEqualTo(unknown.text().replaceAll("[0-9T:.+\\-]{19,}", "D"));
		// the group key of a foreign subscription too
		Assertions.assertThat(structured(call(other, "groupKey", groupKey))).containsEntry("total", Integer.valueOf(0));
	}

	@Test
	public void budgetEleventhCall() throws InterruptedException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-8");
		// the limiter counts in fixed windows of 60 seconds: start at the beginning of a window
		long secondOfWindow = Instant.now().getEpochSecond() % 60;
		if(secondOfWindow > 45) {
			Thread.sleep((61 - secondOfWindow) * 1000l);
		}
		for(int i=0; i<NewsQueryToolProvider.CALLS_PER_MINUTE; i++) {
			Assertions.assertThat(call(id).error()).as("call " + (i + 1)).isFalse();
		}
		McpToolResult eleventh = call(id);
		Assertions.assertThat(eleventh.error()).isTrue();
		Assertions.assertThat(eleventh.text()).contains("Too many requests").contains("retry after").contains("10 calls per minute");
		// the budget is per user
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-8o");
		Assertions.assertThat(call(other).error()).isFalse();
	}

	@Test
	public void disabledUserTool() {
		String availableTools = userToolsModule.getAvailableUserTools();
		try {
			userToolsModule.setAvailableUserTools("none");
			Assertions.assertThat(provider.isAvailable()).isFalse();
			// the administration offers other tools only
			userToolsModule.setAvailableUserTools("org.olat.home.HomeMainController:org.olat.home.controllerCreators.CalendarControllerCreator");
			Assertions.assertThat(provider.isAvailable()).isFalse();
			userToolsModule.setAvailableUserTools("org.olat.home.HomeMainController:org.olat.home.controllerCreators.NotificationsControllerCreator");
			Assertions.assertThat(provider.isAvailable()).isTrue();
		} finally {
			userToolsModule.setAvailableUserTools(availableTools == null ? "" : availableTools);
		}
		Assertions.assertThat(provider.isAvailable()).isTrue();
	}

	@Test
	public void htmlStripped() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-news-9");
		forumNews(id, "BusinessGroup", group(id).getKey(), "<b>Exam</b> &amp; <i>date</i> <img src=x onerror=alert(1)>&lt;script&gt;alert(2)&lt;/script&gt;");

		Map<String,Object> result = structured(call(id));
		String description = (String)items(subscriptions(result).get(0)).get(0).get("description");
		Assertions.assertThat(description).contains("Exam & date")
			.doesNotContain("<").doesNotContain(">").doesNotContain("&amp;").doesNotContain("&lt;");
		Assertions.assertThat(NewsQueryToolProvider.plain("A &lt;b&gt;bold&lt;/b&gt; text")).isEqualTo("A bold text");
	}

	private BusinessGroup group(Identity owner) {
		BusinessGroup group = businessGroupService.createBusinessGroup(owner, "MCP news", "News group",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		dbInstance.commitAndCloseSession();
		return group;
	}

	/**
	 * A forum with one new message, the identity subscribed, the publisher
	 * marked with news, as the forum UI does.
	 */
	private Message forumNews(Identity id, String resName, Long resId, String title) {
		Forum forum = forumManager.addAForum();
		String subIdent = "mcp-" + forum.getKey();
		SubscriptionContext context = new SubscriptionContext(resName, resId, subIdent);
		PublisherData data = new PublisherData(OresHelper.calculateTypeName(Forum.class), forum.getKey().toString(),
				"[" + resName + ":" + resId + "][Forum:" + forum.getKey() + "]");
		notificationsManager.subscribe(id, context, data);
		Message message = forumManager.createMessage(forum, id, false);
		message.setTitle(title);
		message.setBody("Body");
		forumManager.addTopMessage(message);
		dbInstance.commitAndCloseSession();
		notificationsManager.markPublisherNews(context, null, false);
		dbInstance.commitAndCloseSession();
		Publisher publisher = notificationsManager.getPublisher(context);
		Assertions.assertThat(publisher.getLatestNewsDate()).isNotNull();
		return message;
	}

	private McpToolResult call(Identity identity, Object... keyValues) {
		Map<String,Object> arguments = new HashMap<>();
		for(int i=0; i<keyValues.length; i+=2) {
			arguments.put((String)keyValues[i], keyValues[i + 1]);
		}
		McpToolContext context = new McpToolContext(identity, securityManager.getRoles(identity),
				new McpGrants(Set.of(NewsQueryToolProvider.NAME), null, List.of("test")), null, Locale.ENGLISH, "127.0.0.1");
		McpToolResult result = provider.execute(context, arguments);
		dbInstance.commitAndCloseSession();
		Assertions.assertThat(result).isNotNull();
		if(!result.error()) {
			// the SDK validates the structured content against the output schema
			Assertions.assertThat(new McpJsonSchemaValidator(new ObjectMapper())
					.validate(provider.getDefinition().outputSchema(), result.structuredContent()).valid())
				.as(result.text()).isTrue();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Map<String,Object> structured(McpToolResult result) {
		Assertions.assertThat(result.error()).as(result.text()).isFalse();
		return (Map<String,Object>)result.structuredContent();
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> subscriptions(Map<String,Object> result) {
		return (List<Map<String,Object>>)result.get("subscriptions");
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> items(Map<String,Object> subscription) {
		return (List<Map<String,Object>>)subscription.get("items");
	}
}
