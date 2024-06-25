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

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBSelectionImpl;
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
public class TBSelectionDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	@Autowired
	private TBParticipantDAO participantDao;
	@Autowired
	private TBTopicDAO topicDao;
	
	@Autowired
	private TBSelectionDAO sut;
	
	@Test
	public void shouldCreateSelection() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker();
		TBParticipant participant = createRandomParticipant(broker);
		TBTopic topic = createRandomTopic(broker);
		
		TBSelection selection = sut.createSelection(identity, participant, topic, 3);
		dbInstance.commitAndCloseSession();
		
		assertThat(selection).isNotNull();
		assertThat(selection.getCreationDate()).isNotNull();
		assertThat(selection.getLastModified()).isNotNull();
		assertThat(selection.getParticipant()).isEqualTo(participant);
		assertThat(selection.getTopic()).isEqualTo(topic);
		assertThat(selection.getSortOrder()).isEqualTo(3);
	}
	
	@Test
	public void shouldUpdateSelection() {
		TBBroker broker = createRandomBroker();
		TBParticipant participant = createRandomParticipant(broker);
		TBTopic topic = createRandomTopic(broker);
		TBSelection selection = createRandomSelection(participant, topic);
		
		int sortOrder = 5;
		((TBSelectionImpl)selection).setSortOrder(sortOrder);
		sut.updateSelection(selection);
		dbInstance.commitAndCloseSession();
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setSelection(selection);
		TBSelection reloadedSelection = sut.loadSelections(params).get(0);
		assertThat(reloadedSelection.getSortOrder()).isEqualTo(sortOrder);
	}
	
	@Test
	public void shouldDelete() {
		TBBroker broker = createRandomBroker();
		TBSelection selection = createRandomSelection(createRandomParticipant(broker), createRandomTopic(broker));
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setSelections(List.of(selection));
		assertThat(sut.loadSelections(params)).isNotEmpty();
		
		sut.deleteSelection(selection);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadSelections(params)).isEmpty();
	}
	
	@Test
	public void shouldDelete_ByBroker() {
		TBBroker broker1 = createRandomBroker();
		createRandomSelection(createRandomParticipant(broker1), createRandomTopic(broker1));
		TBBroker broker2 = createRandomBroker();
		createRandomSelection(createRandomParticipant(broker2), createRandomTopic(broker2));
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setBroker(broker1);
		assertThat(sut.loadSelections(params)).isNotEmpty();
		
		params.setBroker(broker2);
		assertThat(sut.loadSelections(params)).isNotEmpty();
		
		sut.deleteSelections(broker2);
		dbInstance.commitAndCloseSession();
		
		params.setBroker(broker1);
		assertThat(sut.loadSelections(params)).isNotEmpty();
		
		params.setBroker(broker2);
		assertThat(sut.loadSelections(params)).isEmpty();
	}
	
	@Test
	public void shouldLoadSelections_byKeys() {
		TBBroker broker = createRandomBroker();
		TBParticipant participant = createRandomParticipant(broker);
		TBTopic topic = createRandomTopic(broker);
		TBSelection selection1 = createRandomSelection(participant, topic);
		TBSelection selection2 = createRandomSelection(participant, topic);
		createRandomSelection(participant, topic);
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setSelections(List.of(selection1, selection2));
		List<TBSelection> selections = sut.loadSelections(params);
		
		assertThat(selections).containsExactlyInAnyOrder(selection1, selection2);
	}
	
	@Test
	public void shouldLoadSelections_byBrokers() {
		TBBroker broker1 = createRandomBroker();
		TBBroker broker2 = createRandomBroker();
		TBBroker broker3 = createRandomBroker();
		TBSelection selection11 = createRandomSelection(createRandomParticipant(broker1), createRandomTopic(broker1));
		TBSelection selection12 = createRandomSelection(createRandomParticipant(broker1), createRandomTopic(broker1));
		TBSelection selection2 = createRandomSelection(createRandomParticipant(broker2), createRandomTopic(broker2));
		createRandomSelection(createRandomParticipant(broker3), createRandomTopic(broker3));
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setBrokers(List.of(broker1, broker2));
		List<TBSelection> selections = sut.loadSelections(params);
		
		assertThat(selections).containsExactlyInAnyOrder(selection11, selection12, selection2);
	}
	
	@Test
	public void shouldLoadSelections_byParticipants() {
		TBBroker broker = createRandomBroker();
		TBParticipant participant1 = createRandomParticipant(broker);
		TBParticipant participant2 = createRandomParticipant(broker);
		TBParticipant participant3 = createRandomParticipant(broker);
		TBTopic topic = createRandomTopic(broker);
		TBSelection selection1 = createRandomSelection(participant1, topic);
		TBSelection selection2 = createRandomSelection(participant2, topic);
		createRandomSelection(participant3, topic);
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setParticipants(List.of(participant1, participant2));
		List<TBSelection> selections = sut.loadSelections(params);
		
		assertThat(selections).containsExactlyInAnyOrder(selection1, selection2);
	}
	
	@Test
	public void shouldLoadSelections_byIdentities() {
		TBBroker broker = createRandomBroker();
		TBParticipant participant1 = createRandomParticipant(broker);
		TBParticipant participant2 = createRandomParticipant(broker);
		TBParticipant participant3 = createRandomParticipant(broker);
		TBTopic topic = createRandomTopic(broker);
		TBSelection selection1 = createRandomSelection(participant1, topic);
		TBSelection selection2 = createRandomSelection(participant2, topic);
		createRandomSelection(participant3, topic);
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setIdentities(List.of(participant1.getIdentity(), participant2.getIdentity()));
		List<TBSelection> selections = sut.loadSelections(params);
		
		assertThat(selections).containsExactlyInAnyOrder(selection1, selection2);
	}
	
	@Test
	public void shouldLoadSelections_byTopics() {
		TBBroker broker = createRandomBroker();
		TBParticipant participant = createRandomParticipant(broker);
		TBTopic topic1 = createRandomTopic(broker);
		TBTopic topic2 = createRandomTopic(broker);
		TBTopic topic3 = createRandomTopic(broker);
		TBSelection selection1 = createRandomSelection(participant, topic1);
		TBSelection selection2 = createRandomSelection(participant, topic2);
		createRandomSelection(participant, topic3);
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setTopics(List.of(topic1, topic2));
		List<TBSelection> selections = sut.loadSelections(params);
		
		assertThat(selections).containsExactlyInAnyOrder(selection1, selection2);
	}
	
	@Test
	public void shouldLoadSelections_byEnrolledOrMaxSortOrder() {
		TBBroker broker = createRandomBroker();
		TBParticipant participant1 = createRandomParticipant(broker);
		TBParticipant participant2 = createRandomParticipant(broker);
		TBTopic topic1 = createRandomTopic(broker);
		TBTopic topic2 = createRandomTopic(broker);
		TBTopic topic3 = createRandomTopic(broker);
		TBTopic topic4 = createRandomTopic(broker);
		TBSelection selection11 = sut.createSelection(participant1.getIdentity(), participant1, topic1, 1);
		TBSelection selection12 = sut.createSelection(participant1.getIdentity(), participant1, topic2, 2);
		sut.createSelection(participant1.getIdentity(), participant1, topic3, 3);
		TBSelection selection21 = sut.createSelection(participant1.getIdentity(), participant2, topic1, 1);
		TBSelection selection22 = sut.createSelection(participant1.getIdentity(), participant2, topic2, 2);
		sut.createSelection(participant1.getIdentity(), participant2, topic3, 3);
		TBSelection selection24 = sut.createSelection(participant1.getIdentity(), participant2, topic4, 4);
		((TBSelectionImpl)selection24).setEnrolled(true);
		sut.updateSelection(selection24);
		dbInstance.commitAndCloseSession();
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setBroker(broker);
		params.setEnrolledOrMaxSortOrder(2);
		List<TBSelection> selections = sut.loadSelections(params);
		
		assertThat(selections).containsExactlyInAnyOrder(selection11, selection12, selection21, selection22, selection24);
	}
	
	@Test
	public void shouldLoadSelections_fetch() {
		TBBroker broker = createRandomBroker();
		TBParticipant participant = createRandomParticipant(broker);
		TBTopic topic = createRandomTopic(broker);
		TBSelection selection = createRandomSelection(participant, topic);
		
		TBSelectionSearchParams params = new TBSelectionSearchParams();
		params.setSelection(selection);
		params.setFetchIdentity(true);
		params.setFetchTopic(true);
		sut.loadSelections(params);
		
		// Just a syntay test
	}
	
	private TBSelection createRandomSelection(TBParticipant participant, TBTopic topic) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBSelection selection = sut.createSelection(identity, participant, topic, 1);
		dbInstance.commitAndCloseSession();
		
		return selection;
	}
	
	private TBParticipant createRandomParticipant(TBBroker broker) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBParticipant participant = participantDao.createParticipant(broker, identity);
		dbInstance.commitAndCloseSession();
		
		return participant;
	}
	
	private TBTopic createRandomTopic(TBBroker broker) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBTopic topic = topicDao.createTopic(identity, broker);
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
