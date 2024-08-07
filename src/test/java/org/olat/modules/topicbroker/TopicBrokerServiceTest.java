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
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
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
	
	private TBTopic createRandomTopic(Identity identity) {
		TBBroker broker = createRandomBroker(identity);
		return createRandomTopic(identity, broker);
	}

	private TBTopic createRandomTopic(Identity identity, TBBroker broker) {
		TBTopic topic = sut.createTopic(identity, broker);
		topic = sut.updateTopic(identity, topic, random(), random(), random(), 0, 6, null);
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
