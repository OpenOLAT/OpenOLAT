/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBAuditLog;
import org.olat.modules.topicbroker.TBAuditLogSearchParams;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBTopic;
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
public class TBAuditLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	@Autowired
	private TBTopicDAO topicDAO;
	
	@Autowired
	private TBAuditLogDAO sut;

	@Test
	public void shouldCreate() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic();
		dbInstance.commitAndCloseSession();
		
		TBAuditLog.Action action = TBAuditLog.Action.topicCreate;
		String before = random();
		String after = random();
		TBAuditLog activity = sut.create(action, before, after, doer, topic);
		dbInstance.commitAndCloseSession();
		
		assertThat(activity.getKey()).isNotNull();
		assertThat(activity.getCreationDate()).isNotNull();
		assertThat(activity.getAction()).isEqualTo(action);
		assertThat(activity.getBefore()).isEqualTo(before);
		assertThat(activity.getAfter()).isEqualTo(after);
		assertThat(activity.getDoer()).isEqualTo(doer);
		assertThat(activity.getBroker()).isEqualTo(topic.getBroker());
		assertThat(activity.getTopic()).isEqualTo(topic);
	}
	
	@Test
	public void shouldDelete() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic();
		TBAuditLog activity1 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic);
		TBAuditLog activity2 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic);
		TBAuditLog activity3 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic);
		dbInstance.commitAndCloseSession();
		
		sut.delete(List.of(activity1, activity2));
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setTopic(topic);
		List<TBAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		assertThat(activities).containsExactlyInAnyOrder(activity3);
	}
	
	@Test
	public void shouldDelete_ByBroker() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic1 = createRandomTopic();
		TBTopic topic2 = createRandomTopic();
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic1);
		TBAuditLog activity2 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic2);
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setBroker(topic1.getBroker());
		assertThat(sut.loadAuditLogs(searchParams, 0, -1)).isNotEmpty();
		
		searchParams.setBroker(topic2.getBroker());
		assertThat(sut.loadAuditLogs(searchParams, 0, -1)).isNotEmpty();
		
		sut.delete(activity2.getBroker());
		dbInstance.commitAndCloseSession();
		
		searchParams = new TBAuditLogSearchParams();
		searchParams.setBroker(topic1.getBroker());
		assertThat(sut.loadAuditLogs(searchParams, 0, -1)).isNotEmpty();
		
		searchParams.setBroker(topic2.getBroker());
		assertThat(sut.loadAuditLogs(searchParams, 0, -1)).isEmpty();
	}
	
	@Test
	public void shouldLoadDoers() {
		Identity doer1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic1 = createRandomTopic();
		TBTopic topic2 = createRandomTopic();
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer1, topic1);
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer1, topic1);
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer2, topic1);
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer3, topic2);
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setTopic(topic1);
		List<Identity> doers = sut.loadAuditLogDoers(searchParams);
		
		assertThat(doers).hasSize(2).containsExactlyInAnyOrder(doer1, doer2);
	}
	
	@Test
	public void shouldLoad_filter_actions() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic();
		TBAuditLog activity1 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic);
		TBAuditLog activity2 = sut.create(TBAuditLog.Action.topicUpdateSortOrder, null, null, doer, topic);
		sut.create(TBAuditLog.Action.topicDeleteSoftly, null, null, doer, topic);
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setTopic(topic);
		searchParams.setActions(List.of(TBAuditLog.Action.topicUpdateContent, TBAuditLog.Action.topicUpdateSortOrder));
		List<TBAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_doers() {
		Identity doer1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic();
		TBAuditLog activity1 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer1, topic);
		TBAuditLog activity2 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer1, topic);
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer2, topic);
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setDoer(doer1);
		List<TBAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_brokers() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic1 = createRandomTopic();
		TBTopic topic2 = createRandomTopic();
		TBTopic topic3 = createRandomTopic();
		TBAuditLog activity1 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic1);
		TBAuditLog activity2 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic2);
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic3);
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setBrokers(List.of(topic1.getBroker(), topic2.getBroker()));
		List<TBAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_topics() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic1 = createRandomTopic();
		TBTopic topic2 = createRandomTopic();
		TBTopic topic3 = createRandomTopic();
		TBAuditLog activity1 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic1);
		TBAuditLog activity2 = sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic2);
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic3);
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setTopics(List.of(topic1, topic2));
		List<TBAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_orderAcs() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic();
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic);
		sut.create(TBAuditLog.Action.topicDeleteSoftly, null, null, doer, topic);
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setTopic(topic);
		searchParams.setOrderAsc(Boolean.TRUE);
		sut.loadAuditLogs(searchParams, 0, -1);
		
		// Just a syntax check;
	}
	
	@Test
	public void shouldLoad_fetch_doer() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = createRandomTopic();
		sut.create(TBAuditLog.Action.topicUpdateContent, null, null, doer, topic);
		dbInstance.commitAndCloseSession();
		
		TBAuditLogSearchParams searchParams = new TBAuditLogSearchParams();
		searchParams.setDoer(doer);
		searchParams.setFetchDoer(true);
		sut.loadAuditLogs(searchParams, 0, -1);
		
		// Just a syntax check
	}

	
	private TBTopic createRandomTopic() {
		TBBroker broker = createRandomBroker();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = topicDAO.createTopic(identity, broker);
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
