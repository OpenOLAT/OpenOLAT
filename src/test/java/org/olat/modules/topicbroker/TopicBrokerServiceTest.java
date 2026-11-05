/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.topicbroker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private TopicBrokerService sut;
	
	@Test
	public void shouldCheckIfIdentifierAvailable() {
		Identity identity = JunitTestHelper.getDefaultAuthor();
		TBBroker broker = createRandomBroker(identity);
		TBTopic topic = sut.createTopic(identity, broker);
		topic = sut.updateTopic(identity, topic, "Identifier", random(), random(), TBTopicExecutionType.period, null, null, 0, 6, null);
		dbInstance.commitAndCloseSession();
		
		boolean identifierAvailable = sut.isTopicIdentifierAvailable(broker, "Identifier2");
		
		assertThat(identifierAvailable).isTrue();
	}
	
	@Test
	public void shouldCheckIfIdentifierAvailable_notUnique() {
		Identity identity = JunitTestHelper.getDefaultAuthor();
		TBBroker broker = createRandomBroker(identity);
		TBTopic topic = sut.createTopic(identity, broker);
		topic = sut.updateTopic(identity, topic, random(), random(), random(), TBTopicExecutionType.period, null, null, 0, 6, null);
		dbInstance.commitAndCloseSession();
		
		boolean identifierAvailable = sut.isTopicIdentifierAvailable(broker, topic.getIdentifier());
		
		assertThat(identifierAvailable).isFalse();
	}
	
	@Test
	public void shouldCheckIfIdentifierAvailable_notUniqueLowercase() {
		Identity identity = JunitTestHelper.getDefaultAuthor();
		TBBroker broker = createRandomBroker(identity);
		TBTopic topic = sut.createTopic(identity, broker);
		String identifier = "Identifier";
		topic = sut.updateTopic(identity, topic, identifier, random(), random(), TBTopicExecutionType.period, null, null, 0, 6, null);
		dbInstance.commitAndCloseSession();
		
		boolean identifierAvailable = sut.isTopicIdentifierAvailable(broker, identifier.toLowerCase());
		
		assertThat(identifierAvailable).isFalse();
	}
	
	@Test
	public void shouldCheckIfIdentifierAvailable_containsNotFilesystemSafeCharacter() {
		Identity identity = JunitTestHelper.getDefaultAuthor();
		TBBroker broker = createRandomBroker(identity);
		
		boolean identifierAvailable = sut.isTopicIdentifierAvailable(broker, "idebtifier?");
		
		assertThat(identifierAvailable).isFalse();
	}
	
	@Test
	public void shouldCheckIfIdentifierValid() {
		assertThat(sut.isTopicIdentifierValid("Identifier", List.of())).isTrue();
	}
	
	@Test
	public void shouldCheckIfIdentifierValid_containsNotFilesystemSafeCharacter() {
		assertThat(sut.isTopicIdentifierValid("Identifier?", List.of())).isFalse();
	}
	
	@Test
	public void shouldCheckIfIdentifierValid_notUniqueLowercase() {
		assertThat(sut.isTopicIdentifierValid("Identifier", List.of("identifier"))).isFalse();
	}
	
	@Test
	public void shouldTopicMoveUp() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic1 = createRandomTopic(identity);
		TBTopic topic2 = createRandomTopic(identity);
		
		sut.moveTopic(identity, topic2, true);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getTopic(topic1).getSortOrder()).isEqualTo(topic2.getSortOrder());
		softly.assertThat(sut.getTopic(topic2).getSortOrder()).isEqualTo(topic1.getSortOrder());
		softly.assertAll();
	}
	
	@Test
	public void shouldTopicMoveDown() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic1 = createRandomTopic(identity);
		TBTopic topic2 = createRandomTopic(identity);
		
		sut.moveTopic(identity, topic1, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getTopic(topic1).getSortOrder()).isEqualTo(topic2.getSortOrder());
		softly.assertThat(sut.getTopic(topic2).getSortOrder()).isEqualTo(topic1.getSortOrder());
		softly.assertAll();
	}
	
	@Test
	public void shouldTopicNotMoveUpTopmost() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker(identity);
		createRandomTopic(identity, broker);
		createRandomTopic(identity, broker);
		
		TBTopicSearchParams searchParams = new TBTopicSearchParams();
		searchParams.setBroker(broker);
		TBTopic topmost = sut.getTopics(searchParams).stream()
				.sorted((t1, t2) -> Integer.compare(t1.getSortOrder(), t2.getSortOrder()))
				.findFirst().get();
		
		sut.moveTopic(identity, topmost, true);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getTopic(topmost).getSortOrder()).isEqualTo(topmost.getSortOrder());
	}
	
	@Test
	public void shouldTopicNotMoveDownLowermost() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker(identity);
		createRandomTopic(identity, broker);
		TBTopic lowermost = createRandomTopic(identity, broker);
		
		sut.moveTopic(identity, lowermost, false);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getTopic(lowermost).getSortOrder()).isEqualTo(lowermost.getSortOrder());
	}
	
	@Test
	public void shouldUpdateTopicsSortOrder() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker(identity);
		TBTopic topic1 = createRandomTopic(identity, broker);
		TBTopic topic2 = createRandomTopic(identity, broker);
		TBTopic topic3 = createRandomTopic(identity, broker);
		
		List<String> orderedIdentificators = List.of(
				topic2.getIdentifier(),
				topic3.getIdentifier(),
				topic1.getIdentifier());
		sut.updateTopicSortOrder(identity, broker, orderedIdentificators);
		dbInstance.commitAndCloseSession();

		assertThat(sut.getTopic(topic2).getSortOrder()).isEqualTo(1);
		assertThat(sut.getTopic(topic3).getSortOrder()).isEqualTo(2);
		assertThat(sut.getTopic(topic1).getSortOrder()).isEqualTo(3);
	}
	
	@Test
	public void shouldUpdateTopicsSortOrder_topicMissing() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker(identity);
		TBTopic topic1 = createRandomTopic(identity, broker);
		TBTopic topic2 = createRandomTopic(identity, broker);
		TBTopic topic3 = createRandomTopic(identity, broker);
		
		List<String> orderedIdentificators = List.of(
				topic2.getIdentifier(),
				topic3.getIdentifier());
		sut.updateTopicSortOrder(identity, broker, orderedIdentificators);
		dbInstance.commitAndCloseSession();

		assertThat(sut.getTopic(topic1).getSortOrder()).isEqualTo(1);
		assertThat(sut.getTopic(topic2).getSortOrder()).isEqualTo(2);
		assertThat(sut.getTopic(topic3).getSortOrder()).isEqualTo(3);
	}
	
	@Test
	public void shouldSelect_sortOrder() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker(identity);
		TBTopic topic1 = createRandomTopic(identity, broker);
		TBTopic topic2 = createRandomTopic(identity, broker);
		TBTopic topic3 = createRandomTopic(identity, broker);
		TBTopic topic4 = createRandomTopic(identity, broker);
		
		// Select a first topic
		sut.select(identity, identity, topic1, null);
		dbInstance.commitAndCloseSession();
		
		List<TBSelection> selections = getSelections(identity, broker);
		assertThat(selections).hasSize(1);
		assertThat(selections.get(0).getSortOrder()).isEqualTo(1);
		
		// Select the same topic again
		sut.select(identity, identity, topic1, null);
		dbInstance.commitAndCloseSession();
		
		selections = getSelections(identity, broker);
		assertThat(selections).hasSize(1);
		assertThat(selections.get(0).getSortOrder()).isEqualTo(1);
		
		// Select the second topic (no sort order)
		sut.select(identity, identity, topic2, null);
		dbInstance.commitAndCloseSession();
		
		selections = getSelections(identity, broker);
		assertThat(selections).hasSize(2);
		Collections.sort(selections, (s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
		assertThat(selections.get(1).getSortOrder()).isEqualTo(2);
		assertThat(selections.get(1).getTopic().getKey()).isEqualTo(topic2.getKey());
		
		// Select the third topic (sort order 2)
		sut.select(identity, identity, topic3, 2);
		dbInstance.commitAndCloseSession();
		
		selections = getSelections(identity, broker);
		assertThat(selections).hasSize(3);
		Collections.sort(selections, (s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
		assertThat(selections.get(1).getSortOrder()).isEqualTo(2);
		assertThat(selections.get(1).getTopic().getKey()).isEqualTo(topic3.getKey());
		assertThat(selections.get(2).getSortOrder()).isEqualTo(3);
		assertThat(selections.get(2).getTopic().getKey()).isEqualTo(topic2.getKey());
		
		// Select the third topic (sort order 100). No gap allowed.
		sut.select(identity, identity, topic4, 100);
		dbInstance.commitAndCloseSession();
		
		selections = getSelections(identity, broker);
		assertThat(selections).hasSize(4);
		Collections.sort(selections, (s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
		assertThat(selections.get(3).getSortOrder()).isEqualTo(4);
		assertThat(selections.get(3).getTopic().getKey()).isEqualTo(topic4.getKey());
	}
	
	@Test
	public void shouldUpdateTopic_datesTypeWithoutPeriod() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic(identity);
		Date beginDate = DateUtils.truncateSeconds(DateUtils.addDays(new Date(), 1));
		Date endDate = DateUtils.truncateSeconds(DateUtils.addDays(new Date(), 2));
		
		topic = sut.updateTopic(identity, topic, topic.getIdentifier(), topic.getTitle(), topic.getDescription(),
				TBTopicExecutionType.dates, beginDate, endDate, 0, 6, null);
		dbInstance.commitAndCloseSession();
		
		TBTopic reloadedTopic = sut.getTopic(topic);
		assertThat(reloadedTopic.getExecutionType()).isEqualTo(TBTopicExecutionType.dates);
		assertThat(reloadedTopic.getBeginDate()).isNull();
		assertThat(reloadedTopic.getEndDate()).isNull();
	}
	
	@Test
	public void shouldUpdateTopicDates() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic(identity);
		Date start1 = DateUtils.truncateSeconds(DateUtils.addDays(new Date(), 1));
		Date start2 = DateUtils.truncateSeconds(DateUtils.addDays(new Date(), 2));
		Date start3 = DateUtils.truncateSeconds(DateUtils.addDays(new Date(), 3));
		
		sut.updateTopicDates(identity, topic, List.of(
				new TBTopicDateValues(null, start1, DateUtils.addHours(start1, 1), "Location 1", null),
				new TBTopicDateValues(null, start2, DateUtils.addHours(start2, 1), null, "Description 2")));
		dbInstance.commitAndCloseSession();
		
		List<TBTopicDate> dates = getTopicDates(topic);
		assertThat(dates).hasSize(2);
		assertThat(dates.get(0).getLocation()).isEqualTo("Location 1");
		assertThat(dates.get(1).getDescription()).isEqualTo("Description 2");
		
		TBTopicDate date2 = dates.get(1);
		sut.updateTopicDates(identity, topic, List.of(
				new TBTopicDateValues(date2.getKey(), start2, DateUtils.addHours(start2, 2), "Location 2", "Description 2"),
				new TBTopicDateValues(null, start3, DateUtils.addHours(start3, 1), null, null)));
		dbInstance.commitAndCloseSession();
		
		dates = getTopicDates(topic);
		assertThat(dates).hasSize(2);
		assertThat(dates.get(0).getKey()).isEqualTo(date2.getKey());
		assertThat(dates.get(0).getLocation()).isEqualTo("Location 2");
		assertThat(DateUtils.isSameMinute(dates.get(0).getEndDate(), DateUtils.addHours(start2, 2))).isTrue();
		assertThat(DateUtils.isSameMinute(dates.get(1).getStartDate(), start3)).isTrue();
		
		sut.updateTopicDates(identity, topic, List.of());
		dbInstance.commitAndCloseSession();
		
		assertThat(getTopicDates(topic)).isEmpty();
	}
	
	@Test
	public void shouldUpdateTopicDates_auditLog() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic(identity);
		Date start = DateUtils.truncateSeconds(DateUtils.addDays(new Date(), 1));
		
		sut.updateTopicDates(identity, topic, List.of(new TBTopicDateValues(null, start, null, null, null)));
		dbInstance.commitAndCloseSession();
		TBTopicDate date = getTopicDates(topic).get(0);
		
		sut.updateTopicDates(identity, topic, List.of(new TBTopicDateValues(date.getKey(), start, null, null, null)));
		sut.updateTopicDates(identity, topic, List.of(new TBTopicDateValues(date.getKey(), start, null, random(), null)));
		sut.updateTopicDates(identity, topic, List.of());
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setTopic(topic);
		List<TBAuditLog.Action> actions = sut.getAuditLog(searchParams, 0, -1).stream()
				.map(TBAuditLog::getAction)
				.filter(action -> action.name().startsWith("topicDate"))
				.toList();
		assertThat(actions).containsExactlyInAnyOrder(
				TBAuditLog.Action.topicDateCreate,
				TBAuditLog.Action.topicDateUpdateContent,
				TBAuditLog.Action.topicDateDelete);
	}
	
	@Test
	public void shouldDeleteBroker_withTopicDates() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic(identity);
		sut.updateTopicDates(identity, topic, List.of(new TBTopicDateValues(null, new Date(), null, null, null)));
		dbInstance.commitAndCloseSession();
		TBBroker broker = sut.getBroker(topic.getBroker());
		
		sut.deleteBroker(broker.getRepositoryEntry(), broker.getSubIdent());
		dbInstance.commitAndCloseSession();
		
		assertThat(getTopicDates(topic)).isEmpty();
	}
	
	@Test
	public void shouldGetExecutionDates() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker(identity);
		TBTopic periodTopic = createRandomTopic(identity, broker);
		sut.updateTopicDates(identity, periodTopic, List.of(new TBTopicDateValues(null, new Date(), new Date(), null, null)));
		TBTopic datesTopic = createRandomTopic(identity, broker);
		datesTopic = sut.updateTopic(identity, datesTopic, datesTopic.getIdentifier(), datesTopic.getTitle(),
				datesTopic.getDescription(), TBTopicExecutionType.dates, null, null, 0, 6, null);
		sut.updateTopicDates(identity, datesTopic, List.of(new TBTopicDateValues(null, new Date(), new Date(), null, null)));
		dbInstance.commitAndCloseSession();
		
		Map<Long, List<TBTopicDate>> topicKeyToDates = sut.getExecutionDates(List.of(periodTopic, datesTopic));
		
		assertThat(topicKeyToDates).containsOnlyKeys(datesTopic.getKey());
		assertThat(topicKeyToDates.get(datesTopic.getKey())).hasSize(1);
	}
	
	private List<TBTopicDate> getTopicDates(TBTopic topic) {
		TBTopicDateSearchParams searchParams = new TBTopicDateSearchParams();
		searchParams.setTopic(topic);
		searchParams.setDeletedTopic(null);
		return sut.getTopicDates(searchParams);
	}
	
	private TBTopic createRandomTopic(Identity identity) {
		TBBroker broker = createRandomBroker(identity);
		return createRandomTopic(identity, broker);
	}

	private TBTopic createRandomTopic(Identity identity, TBBroker broker) {
		TBTopic topic = sut.createTopic(identity, broker);
		topic = sut.updateTopic(identity, topic, random(), random(), random(), TBTopicExecutionType.period, null, null, 0, 6, null);
		dbInstance.commitAndCloseSession();
		
		return topic;
	}
	
	private TBBroker createRandomBroker(Identity identity) {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = sut.createBroker(identity, repositoryEntry, random());
		dbInstance.commitAndCloseSession();
		
		return broker;
	}

	private List<TBSelection> getSelections(Identity identity, TBBroker broker) {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setIdentity(identity);
		return sut.getSelections(searchParams);
	}

}
