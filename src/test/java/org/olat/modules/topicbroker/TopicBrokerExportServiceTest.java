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

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.topicbroker.model.TBImportTopic;
import org.olat.modules.topicbroker.model.TBTransientTopic;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 5 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerExportServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TopicBrokerService topicBrokerService;
	
	@Autowired
	private TopicBrokerExportService sut;
	
	@Test
	public void shouldCreateOrUpdateTopics_ignorePeriodOfDatesTopic() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = topicBrokerService.createBroker(identity, repositoryEntry, random());
		TBTopic datesTopic = createTopic(identity, broker, TBTopicExecutionType.dates);
		TBTopic periodTopic = createTopic(identity, broker, TBTopicExecutionType.period);
		Date dateStart = DateUtils.toDate(LocalDate.of(2027, 3, 10));
		topicBrokerService.updateTopicDates(identity, datesTopic,
				List.of(new TBTopicDateValues(null, dateStart, DateUtils.addHours(dateStart, 2), null, null)));
		dbInstance.commitAndCloseSession();
		
		Date beginDate = DateUtils.toDate(LocalDate.of(2027, 3, 1));
		Date endDate = DateUtils.toDate(LocalDate.of(2027, 3, 31));
		sut.createOrUpdateTopics(identity, broker, List.of(
				createImportTopic(datesTopic.getIdentifier(), beginDate, endDate),
				createImportTopic(periodTopic.getIdentifier(), beginDate, endDate)),
				null);
		dbInstance.commitAndCloseSession();
		
		TBTopic reloadedDatesTopic = topicBrokerService.getTopic(datesTopic);
		assertThat(reloadedDatesTopic.getExecutionType()).isEqualTo(TBTopicExecutionType.dates);
		assertThat(reloadedDatesTopic.getBeginDate()).isNull();
		assertThat(reloadedDatesTopic.getEndDate()).isNull();
		TBTopicDateSearchParams datesSearchParams = new TBTopicDateSearchParams();
		datesSearchParams.setTopic(datesTopic);
		assertThat(topicBrokerService.getTopicDates(datesSearchParams)).hasSize(1);
		TBTopic reloadedPeriodTopic = topicBrokerService.getTopic(periodTopic);
		assertThat(reloadedPeriodTopic.getExecutionType()).isEqualTo(TBTopicExecutionType.period);
		assertThat(DateUtils.isSameDay(reloadedPeriodTopic.getBeginDate(), beginDate)).isTrue();
		assertThat(DateUtils.isSameDay(reloadedPeriodTopic.getEndDate(), endDate)).isTrue();
	}
	
	private TBTopic createTopic(Identity identity, TBBroker broker, TBTopicExecutionType executionType) {
		TBTopic topic = topicBrokerService.createTopic(identity, broker);
		return topicBrokerService.updateTopic(identity, topic, random(), random(), null, executionType, null, null, 0, 6, null);
	}
	
	private TBImportTopic createImportTopic(String identifier, Date beginDate, Date endDate) {
		TBTransientTopic topic = new TBTransientTopic();
		topic.setIdentifier(identifier);
		topic.setTitle(random());
		topic.setBeginDate(beginDate);
		topic.setEndDate(endDate);
		topic.setMinParticipants(Integer.valueOf(0));
		topic.setMaxParticipants(Integer.valueOf(6));
		
		TBImportTopic importTopic = new TBImportTopic();
		importTopic.setIdentifier(identifier);
		importTopic.setTopic(topic);
		return importTopic;
	}
	
}
