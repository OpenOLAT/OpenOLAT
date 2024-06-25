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
import org.olat.modules.topicbroker.TBParticipantSearchParams;
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
public class TBParticipantDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	
	@Autowired
	private TBParticipantDAO sut;
	
	@Test
	public void shouldCreateParticipant() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBBroker broker = createRandomBroker();
		
		TBParticipant participant = sut.createParticipant(broker, identity);
		dbInstance.commitAndCloseSession();
		
		assertThat(participant).isNotNull();
		assertThat(participant.getCreationDate()).isNotNull();
		assertThat(participant.getLastModified()).isNotNull();
		assertThat(participant.getBroker()).isEqualTo(broker);
	}
	
	@Test
	public void shouldUpdateParticipant() {
		TBParticipant participant = createRandomParticipant();
		
		Integer boost = 4;
		participant.setBoost(boost);
		Integer maxEnrollments = 9;
		participant.setRequiredEnrollments(maxEnrollments);
		sut.updateParticipant(participant);
		dbInstance.commitAndCloseSession();
		
		TBParticipantSearchParams params = new TBParticipantSearchParams();
		params.setParticipant(participant);
		TBParticipant reloadedParticipant = sut.loadParticipants(params).get(0);
		assertThat(reloadedParticipant.getBoost()).isEqualTo(boost);
		assertThat(reloadedParticipant.getRequiredEnrollments()).isEqualTo(maxEnrollments);
	}
	
	@Test
	public void shouldDelete() {
		TBParticipant participant = createRandomParticipant();
		
		TBParticipantSearchParams params = new TBParticipantSearchParams();
		params.setParticipant(participant);
		assertThat(sut.loadParticipants(params)).isNotEmpty();
		
		sut.deleteParticipant(participant);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadParticipants(params)).isEmpty();
	}
	
	@Test
	public void shouldDelete_ByBroker() {
		TBParticipant participant1 = createRandomParticipant();
		TBParticipant participant2 = createRandomParticipant();
		
		TBParticipantSearchParams params = new TBParticipantSearchParams();
		params.setBroker(participant1.getBroker());
		assertThat(sut.loadParticipants(params)).isNotEmpty();
		
		params.setBroker(participant2.getBroker());
		assertThat(sut.loadParticipants(params)).isNotEmpty();
		
		sut.deleteParticipants(participant2.getBroker());
		dbInstance.commitAndCloseSession();
		
		params.setBroker(participant1.getBroker());
		assertThat(sut.loadParticipants(params)).isNotEmpty();
		
		params.setBroker(participant2.getBroker());
		assertThat(sut.loadParticipants(params)).isEmpty();
	}
	
	@Test
	public void shouldLoadParticipants_byKey() {
		TBParticipant participant1 = createRandomParticipant();
		TBParticipant participant2 = createRandomParticipant();
		createRandomParticipant();
		
		TBParticipantSearchParams params = new TBParticipantSearchParams();
		params.setParticipants(List.of(participant1, participant2));
		List<TBParticipant> brokers = sut.loadParticipants(params);
		
		assertThat(brokers).containsExactlyInAnyOrder(participant1, participant2);
	}
	
	@Test
	public void shouldLoadParticipants_byBrokers() {
		TBParticipant participant1 = createRandomParticipant();
		TBParticipant participant2 = createRandomParticipant();
		createRandomParticipant();
		
		TBParticipantSearchParams params = new TBParticipantSearchParams();
		params.setBrokers(List.of(participant1.getBroker(), participant2.getBroker()));
		List<TBParticipant> brokers = sut.loadParticipants(params);
		
		assertThat(brokers).containsExactlyInAnyOrder(participant1, participant2);
	}
	
	@Test
	public void shouldLoadParticipants_byIdentities() {
		TBParticipant participant1 = createRandomParticipant();
		TBParticipant participant2 = createRandomParticipant();
		createRandomParticipant();
		
		TBParticipantSearchParams params = new TBParticipantSearchParams();
		params.setIdentities(List.of(participant1.getIdentity(), participant2.getIdentity()));
		List<TBParticipant> brokers = sut.loadParticipants(params);
		
		assertThat(brokers).containsExactlyInAnyOrder(participant1, participant2);
	}
	
	private TBParticipant createRandomParticipant() {
		TBBroker broker = createRandomBroker();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		TBParticipant participant = sut.createParticipant(broker, identity);
		dbInstance.commitAndCloseSession();
		
		return participant;
	}
	
	private TBBroker createRandomBroker() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = brokerDao.createBroker(repositoryEntry, random());
		dbInstance.commitAndCloseSession();
		
		return broker;
	}

}
