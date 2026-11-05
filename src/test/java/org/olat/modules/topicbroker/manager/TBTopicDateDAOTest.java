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
import org.olat.core.util.DateUtils;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicDate;
import org.olat.modules.topicbroker.TBTopicDateSearchParams;
import org.olat.modules.topicbroker.TBTopicDateValues;
import org.olat.modules.topicbroker.model.TBTopicImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicDateDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	@Autowired
	private TBTopicDAO topicDao;
	
	@Autowired
	private TBTopicDateDAO sut;
	
	@Test
	public void shouldCreateDate() {
		TBTopic topic = createRandomTopic();
		
		Date startDate = DateUtils.truncateSeconds(DateUtils.addDays(new Date(), 2));
		Date endDate = DateUtils.addHours(startDate, 2);
		String location = random();
		String description = random();
		TBTopicDateValues values = new TBTopicDateValues(null, startDate, endDate, location, description);
		
		TBTopicDate date = sut.createDate(topic, values);
		dbInstance.commitAndCloseSession();
		
		assertThat(date).isNotNull();
		assertThat(date.getKey()).isNotNull();
		assertThat(date.getCreationDate()).isNotNull();
		assertThat(date.getLastModified()).isNotNull();
		assertThat(DateUtils.isSameMinute(date.getStartDate(), startDate)).isTrue();
		assertThat(DateUtils.isSameMinute(date.getEndDate(), endDate)).isTrue();
		assertThat(date.getLocation()).isEqualTo(location);
		assertThat(date.getDescription()).isEqualTo(description);
		assertThat(date.getTopic()).isEqualTo(topic);
	}
	
	@Test
	public void shouldUpdateDate() {
		TBTopicDate date = createRandomDate();
		
		Date startDate = DateUtils.truncateSeconds(DateUtils.addDays(new Date(), 2));
		date.setStartDate(startDate);
		Date endDate = DateUtils.addHours(startDate, 2);
		date.setEndDate(endDate);
		String location = random();
		date.setLocation(location);
		String description = random();
		date.setDescription(description);
		sut.updateDate(date);
		dbInstance.commitAndCloseSession();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setTopic(date.getTopic());
		TBTopicDate reloadedDate = sut.loadDates(params).get(0);
		assertThat(DateUtils.isSameMinute(reloadedDate.getStartDate(), startDate)).isTrue();
		assertThat(DateUtils.isSameMinute(reloadedDate.getEndDate(), endDate)).isTrue();
		assertThat(reloadedDate.getLocation()).isEqualTo(location);
		assertThat(reloadedDate.getDescription()).isEqualTo(description);
	}
	
	@Test
	public void shouldDelete() {
		TBTopic topic = createRandomTopic();
		TBTopicDate date1 = createRandomDate(topic);
		TBTopicDate date2 = createRandomDate(topic);
		
		sut.deleteDate(date1);
		dbInstance.commitAndCloseSession();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setTopic(topic);
		assertThat(sut.loadDates(params)).containsExactlyInAnyOrder(date2);
	}
	
	@Test
	public void shouldDelete_byBroker() {
		TBTopicDate date1 = createRandomDate();
		TBTopicDate date2 = createRandomDate();
		
		sut.deleteDates(date2.getTopic().getBroker());
		dbInstance.commitAndCloseSession();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setTopics(List.of(date1.getTopic(), date2.getTopic()));
		assertThat(sut.loadDates(params)).containsExactlyInAnyOrder(date1);
	}
	
	@Test
	public void shouldLoadDates_byTopics() {
		TBTopicDate date1 = createRandomDate();
		TBTopicDate date2 = createRandomDate();
		createRandomDate();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setTopics(List.of(date1.getTopic(), date2.getTopic()));
		
		assertThat(sut.loadDates(params)).containsExactlyInAnyOrder(date1, date2);
	}
	
	@Test
	public void shouldLoadDates_emptyKeys() {
		createRandomDate();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setTopics(List.of());
		assertThat(sut.loadDates(params)).isEmpty();
		
		params = new TBTopicDateSearchParams();
		params.setBrokers(List.of());
		assertThat(sut.loadDates(params)).isEmpty();
	}
	
	@Test
	public void shouldLoadDates_byBrokers() {
		TBTopicDate date1 = createRandomDate();
		TBTopicDate date2 = createRandomDate();
		createRandomDate();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setBrokers(List.of(date1.getTopic().getBroker(), date2.getTopic().getBroker()));
		
		assertThat(sut.loadDates(params)).containsExactlyInAnyOrder(date1, date2);
	}
	
	@Test
	public void shouldLoadDates_byDeletedTopics() {
		TBTopicDate date1 = createRandomDate();
		TBTopicDate date2 = createRandomDate();
		TBTopic topic2 = date2.getTopic();
		((TBTopicImpl)topic2).setDeletedDate(new Date());
		topicDao.updateTopic(topic2);
		dbInstance.commitAndCloseSession();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setTopics(List.of(date1.getTopic(), topic2));
		assertThat(sut.loadDates(params)).containsExactlyInAnyOrder(date1);
		
		params.setDeletedTopic(Boolean.TRUE);
		assertThat(sut.loadDates(params)).containsExactlyInAnyOrder(date2);
		
		params.setDeletedTopic(null);
		assertThat(sut.loadDates(params)).containsExactlyInAnyOrder(date1, date2);
	}
	
	@Test
	public void shouldLoadDates_orderedByStartDate() {
		TBTopic topic = createRandomTopic();
		TBTopicDate date1 = createRandomDate(topic);
		date1.setStartDate(DateUtils.addDays(new Date(), 2));
		sut.updateDate(date1);
		TBTopicDate date2 = createRandomDate(topic);
		date2.setStartDate(DateUtils.addDays(new Date(), 1));
		sut.updateDate(date2);
		dbInstance.commitAndCloseSession();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setTopic(topic);
		
		assertThat(sut.loadDates(params)).containsExactly(date2, date1);
	}
	
	@Test
	public void shouldLoadDates_fetch() {
		TBTopicDate date = createRandomDate();
		
		TBTopicDateSearchParams params = new TBTopicDateSearchParams();
		params.setTopic(date.getTopic());
		params.setFetchTopic(true);
		
		assertThat(sut.loadDates(params)).containsExactly(date);
	}
	
	private TBTopicDate createRandomDate() {
		return createRandomDate(createRandomTopic());
	}
	
	private TBTopicDate createRandomDate(TBTopic topic) {
		TBTopicDate date = sut.createDate(topic, new TBTopicDateValues(null, new Date(), null, null, null));
		dbInstance.commitAndCloseSession();
		
		return date;
	}
	
	private TBTopic createRandomTopic() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = brokerDao.createBroker(repositoryEntry, random());
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		TBTopic topic = topicDao.createTopic(identity, broker);
		dbInstance.commitAndCloseSession();
		
		return topic;
	}
	
}
