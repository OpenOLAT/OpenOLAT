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
import org.olat.core.util.DateUtils;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerSearchParams;
import org.olat.modules.topicbroker.model.TBBrokerImpl;
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
public class TBBrokerDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
	private TBBrokerDAO sut;
	
	@Test
	public void shouldCreateBroker() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subident = random();
		dbInstance.commitAndCloseSession();
		
		TBBroker broker = sut.createBroker(repositoryEntry, subident);
		dbInstance.commitAndCloseSession();
		
		assertThat(broker).isNotNull();
		assertThat(broker.getCreationDate()).isNotNull();
		assertThat(broker.getLastModified()).isNotNull();
		assertThat(broker.getEnrollmentDoneDate()).isNull();
	}
	
	@Test
	public void shouldUpdateBroker() {
		TBBroker broker = createRandomBroker();
		
		Integer maxSelections = 99;
		broker.setMaxSelections(maxSelections);
		broker.setSelectionStartDate(new Date());
		broker.setSelectionEndDate(new Date());
		Integer requiredEnrollments = 77;
		broker.setRequiredEnrollments(requiredEnrollments);
		broker.setParticipantCanEditRequiredEnrollments(true);
		broker.setAutoEnrollment(true);
		((TBBrokerImpl)broker).setEnrollmentStartDate(new Date());
		((TBBrokerImpl)broker).setEnrollmentDoneDate(new Date());
		broker.setParticipantCanWithdraw(true);
		broker.setWithdrawEndDate(new Date());
		sut.updateBroker(broker);
		dbInstance.commitAndCloseSession();
		
		TBBrokerSearchParams params = new TBBrokerSearchParams();
		params.setBroker(broker);
		TBBroker reloadedBroker = sut.loadBrokers(params).get(0);
		assertThat(reloadedBroker.getMaxSelections()).isEqualTo(maxSelections);
		assertThat(reloadedBroker.getSelectionStartDate()).isNotNull();
		assertThat(reloadedBroker.getSelectionEndDate()).isNotNull();
		assertThat(reloadedBroker.getRequiredEnrollments()).isEqualTo(requiredEnrollments);
		assertThat(reloadedBroker.isParticipantCanEditRequiredEnrollments()).isTrue();
		assertThat(reloadedBroker.isAutoEnrollment()).isTrue();
		assertThat(reloadedBroker.getEnrollmentStartDate()).isNotNull();
		assertThat(reloadedBroker.getEnrollmentDoneDate()).isNotNull();
		assertThat(reloadedBroker.isParticipantCanWithdraw()).isTrue();
		assertThat(reloadedBroker.getWithdrawEndDate()).isNotNull();
	}
	
	@Test
	public void shouldDelete() {
		TBBroker broker = createRandomBroker();
		broker = sut.loadBroker(broker.getRepositoryEntry(), broker.getSubIdent());
		assertThat(broker).isNotNull();
		
		sut.deleteBroker(broker);
		dbInstance.commitAndCloseSession();
		
		broker = sut.loadBroker(broker.getRepositoryEntry(), broker.getSubIdent());
		assertThat(broker).isNull();
	}
	
	@Test
	public void shouldLoadBroker_byRepositoryEntry() {
		TBBroker broker = createRandomBroker();
		
		TBBroker reloadedBroker = sut.loadBroker(broker.getRepositoryEntry(), broker.getSubIdent());
		
		assertThat(reloadedBroker).isEqualTo(broker);
	}
	
	@Test
	public void shouldLoadBrokers_byKey() {
		TBBroker broker1 = createRandomBroker();
		TBBroker broker2 = createRandomBroker();
		createRandomBroker();
		
		TBBrokerSearchParams params = new TBBrokerSearchParams();
		params.setBrokers(List.of(broker1, broker2));
		List<TBBroker> brokers = sut.loadBrokers(params);
		
		assertThat(brokers).containsExactlyInAnyOrder(broker1, broker2);
	}
	
	@Test
	public void shouldLoadBrokers_bySelectionEndBefore() {
		TBBroker brokerBefore = createRandomBroker();
		((TBBrokerImpl)brokerBefore).setSelectionEndDate(DateUtils.addDays(new Date(), -12));
		brokerBefore = sut.updateBroker(brokerBefore);
		TBBroker brokerAfter = createRandomBroker();
		((TBBrokerImpl)brokerAfter).setSelectionEndDate(DateUtils.addDays(new Date(), 3));
		brokerAfter = sut.updateBroker(brokerAfter);
		TBBroker brokerNull = createRandomBroker();
		
		TBBrokerSearchParams params = new TBBrokerSearchParams();
		params.setBrokers(List.of(brokerBefore, brokerAfter, brokerNull));
		assertThat(sut.loadBrokers(params)).containsExactlyInAnyOrder(brokerBefore, brokerAfter, brokerNull);
		
		params.setSelectionEndDateBefore(new Date());
		assertThat(sut.loadBrokers(params)).containsExactlyInAnyOrder(brokerBefore);
	}
	
	@Test
	public void shouldLoadBrokers_byAutoEnrollment() {
		TBBroker broker1 = createRandomBroker();
		broker1.setAutoEnrollment(true);
		broker1 = sut.updateBroker(broker1);
		TBBroker broker2 = createRandomBroker();
		broker2.setAutoEnrollment(false);
		broker2 = sut.updateBroker(broker2);
		
		TBBrokerSearchParams params = new TBBrokerSearchParams();
		params.setBrokers(List.of(broker1, broker2));
		assertThat(sut.loadBrokers(params)).containsExactlyInAnyOrder(broker1, broker2);
		
		params.setAutoEnrollment(Boolean.TRUE);
		assertThat(sut.loadBrokers(params)).containsExactlyInAnyOrder(broker1);
		
		params.setAutoEnrollment(Boolean.FALSE);
		assertThat(sut.loadBrokers(params)).containsExactlyInAnyOrder(broker2);
	}
	
	@Test
	public void shouldLoadBrokers_byEnrollmentStartNull() {
		TBBroker broker1 = createRandomBroker();
		((TBBrokerImpl)broker1).setEnrollmentStartDate(new Date());
		broker1 = sut.updateBroker(broker1);
		TBBroker broker2 = createRandomBroker();
		
		TBBrokerSearchParams params = new TBBrokerSearchParams();
		params.setBrokers(List.of(broker1, broker2));
		assertThat(sut.loadBrokers(params)).containsExactlyInAnyOrder(broker1, broker2);
		
		params.setEnrollmentStartNull(Boolean.TRUE);
		assertThat(sut.loadBrokers(params)).containsExactlyInAnyOrder(broker2);
		
		params.setEnrollmentStartNull(Boolean.FALSE);
		assertThat(sut.loadBrokers(params)).containsExactlyInAnyOrder(broker1);
	}
	
	private TBBroker createRandomBroker() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = sut.createBroker(repositoryEntry, random());
		dbInstance.commitAndCloseSession();
		
		return broker;
	}

}
