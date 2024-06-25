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
package org.olat.modules.topicbroker.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.model.TBTopicImpl;
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
public class TBTopicDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	
	@Autowired
	private TBTopicDAO sut;
	
	@Test
	public void shouldCreateTopic() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker();
		
		TBTopic topic = sut.createTopic(identity, broker);
		dbInstance.commitAndCloseSession();
		
		assertThat(topic).isNotNull();
		assertThat(topic.getCreationDate()).isNotNull();
		assertThat(topic.getLastModified()).isNotNull();
		assertThat(topic.getBroker()).isEqualTo(broker);
	}
	
	@Test
	public void shouldCreateTopic_initSortOrder() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker();
		
		TBTopic topic = sut.createTopic(identity, broker);
		dbInstance.commitAndCloseSession();
		assertThat(topic.getSortOrder()).isEqualTo(1);
		
		sut.createTopic(identity, broker);
		TBTopic topic3 = sut.createTopic(identity, broker);
		assertThat(topic3.getSortOrder()).isEqualTo(3);
		
		int sortOrder = 6;
		((TBTopicImpl)topic).setSortOrder(sortOrder);
		sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		
		TBTopic topic4 = sut.createTopic(identity, broker);
		assertThat(topic4.getSortOrder()).isEqualTo(7);
	}
	
	@Test
	public void shouldUpdateTopic() {
		TBTopic topic = createRandomTopic();
		
		String identifier = JunitTestHelper.random();
		topic.setIdentifier(identifier);
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		Integer minParticipants = 3;
		topic.setMinParticipants(minParticipants);
		Integer maxParticipants = 22;
		topic.setMaxParticipants(maxParticipants);
		int sortOrder = 5;
		((TBTopicImpl)topic).setSortOrder(sortOrder);
		((TBTopicImpl)topic).setDeletedBy(topic.getCreator());
		((TBTopicImpl)topic).setDeletedDate(new Date());
		sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		
		TBTopicSearchParams params = new TBTopicSearchParams();
		params.setTopic(topic);
		params.setDeleted(null);
		TBTopic reloadedTopic = sut.loadTopics(params).get(0);
		assertThat(reloadedTopic.getIdentifier()).isEqualTo(identifier);
		assertThat(reloadedTopic.getTitle()).isEqualTo(title);
		assertThat(reloadedTopic.getDescription()).isEqualTo(description);
		assertThat(reloadedTopic.getMinParticipants()).isEqualTo(minParticipants);
		assertThat(reloadedTopic.getMaxParticipants()).isEqualTo(maxParticipants);
		assertThat(reloadedTopic.getSortOrder()).isEqualTo(sortOrder);
		assertThat(reloadedTopic.getDeletedBy()).isEqualTo(topic.getCreator());
		assertThat(reloadedTopic.getDeletedBy()).isNotNull();
	}
	
	@Test
	public void shouldDelete() {
		TBTopic topic = createRandomTopic();
		
		TBTopicSearchParams params = new TBTopicSearchParams();
		params.setTopic(topic);
		assertThat(sut.loadTopics(params)).isNotEmpty();
		
		sut.deleteTopic(topic);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadTopics(params)).isEmpty();
	}
	
	@Test
	public void shouldDelete_ByBroker() {
		TBTopic topic1 = createRandomTopic();
		TBTopic topic2 = createRandomTopic();
		
		TBTopicSearchParams params = new TBTopicSearchParams();
		params.setBroker(topic1.getBroker());
		assertThat(sut.loadTopics(params)).isNotEmpty();
		
		params.setBroker(topic2.getBroker());
		assertThat(sut.loadTopics(params)).isNotEmpty();
		
		sut.deleteTopics(topic2.getBroker());
		dbInstance.commitAndCloseSession();
		
		params.setBroker(topic1.getBroker());
		assertThat(sut.loadTopics(params)).isNotEmpty();
		
		params.setBroker(topic2.getBroker());
		assertThat(sut.loadTopics(params)).isEmpty();
	}
	
	@Test
	public void shouldLoadTopics_byKey() {
		TBTopic topic1 = createRandomTopic();
		TBTopic topic2 = createRandomTopic();
		createRandomTopic();
		
		TBTopicSearchParams params = new TBTopicSearchParams();
		params.setTopics(List.of(topic1, topic2));
		List<TBTopic> brokers = sut.loadTopics(params);
		
		assertThat(brokers).containsExactlyInAnyOrder(topic1, topic2);
	}
	
	@Test
	public void shouldLoadTopics_byBrokers() {
		TBTopic topic1 = createRandomTopic();
		TBTopic topic2 = createRandomTopic();
		createRandomTopic();
		
		TBTopicSearchParams params = new TBTopicSearchParams();
		params.setBrokers(List.of(topic1.getBroker(), topic2.getBroker()));
		List<TBTopic> brokers = sut.loadTopics(params);
		
		assertThat(brokers).containsExactlyInAnyOrder(topic1, topic2);
	}
	
	@Test
	public void shouldLoadTopics_byIdentifiers() {
		TBBroker broker = createRandomBroker();
		TBTopic topic1 = createRandomTopic(broker);
		topic1.setIdentifier(JunitTestHelper.random());
		topic1 = sut.updateTopic(topic1);
		TBTopic topic2 = createRandomTopic(broker);
		topic2.setIdentifier(JunitTestHelper.random());
		topic2 = sut.updateTopic(topic2);
		TBTopic topic3 = createRandomTopic(broker);
		topic3.setIdentifier(JunitTestHelper.random());
		topic3 = sut.updateTopic(topic3);
		createRandomTopic(broker);
		
		TBTopicSearchParams params = new TBTopicSearchParams();
		params.setIdentifiers(List.of(topic1.getIdentifier(), topic2.getIdentifier()));
		List<TBTopic> brokers = sut.loadTopics(params);
		
		assertThat(brokers).containsExactlyInAnyOrder(topic1, topic2);
	}
	
	@Test
	public void shouldLoadTopics_byDeleted() {
		TBTopic topic1 = createRandomTopic();
		TBTopic topic2 = createRandomTopic();
		((TBTopicImpl)topic2).setDeletedDate(new Date());
		topic2 = sut.updateTopic(topic2);
		createRandomTopic();
		
		TBTopicSearchParams params = new TBTopicSearchParams();
		params.setBrokers(List.of(topic1.getBroker(), topic2.getBroker()));
		assertThat(sut.loadTopics(params)).containsExactlyInAnyOrder(topic1);
		
		params.setDeleted(Boolean.TRUE);
		assertThat(sut.loadTopics(params)).containsExactlyInAnyOrder(topic2);
		
		params.setDeleted(null);
		assertThat(sut.loadTopics(params)).containsExactlyInAnyOrder(topic1, topic2);
	}
	
	private TBTopic createRandomTopic() {
		return createRandomTopic(createRandomBroker());
	}
	
	private TBTopic createRandomTopic(TBBroker broker) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = sut.createTopic(identity, broker);
		dbInstance.commitAndCloseSession();
		
		return topic;
	}
	
	private TBBroker createRandomBroker() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = brokerDao.createBroker(repositoryEntry, random());
		dbInstance.commitAndCloseSession();
		
		return broker;
	}


}
