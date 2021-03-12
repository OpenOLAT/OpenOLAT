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
package org.olat.modules.appointments.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.appointments.TopicRef;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.04.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private TopicDAO sut;
	@Autowired
	private GroupDAO groupDao;
	
	@Test
	public void shouldCreateTopic() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		Topic topic = sut.createTopic(entry, subIdent);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(topic).isNotNull();
		softly.assertThat(topic.getKey()).isNotNull();
		softly.assertThat(topic.getCreationDate()).isNotNull();
		softly.assertThat(topic.getLastModified()).isNotNull();
		softly.assertThat(topic.getType()).isEqualTo(Topic.Type.enrollment);
		softly.assertThat(topic.isMultiParticipation()).isTrue();
		softly.assertThat(topic.isAutoConfirmation()).isFalse();
		softly.assertThat(topic.isParticipationVisible()).isTrue();
		softly.assertThat(topic.getEntry()).isEqualTo(entry);
		softly.assertThat(topic.getSubIdent()).isEqualTo(subIdent);
		softly.assertAll();
	}
	
	@Test
	public void shouldDelete() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Topic topic1 = sut.createTopic(entry, subIdent);
		Topic topic2 = sut.createTopic(entry, subIdent);
		Topic topic3 = sut.createTopic(entry, subIdent);
		dbInstance.commitAndCloseSession();
		
		sut.delete(topic2);
		dbInstance.commitAndCloseSession();
		
		List<Topic> topics = sut.loadTopics(entry, subIdent);
		
		assertThat(topics)
				.containsExactlyInAnyOrder(topic1, topic3)
				.doesNotContain(topic2);
	}
	
	@Test
	public void shouldDeleteByEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		Topic topic1 = sut.createTopic(entry, random());
		Topic topic2 = sut.createTopic(entry, subIdent);
		Topic topic3 = sut.createTopic(entry, random());
		dbInstance.commitAndCloseSession();
		
		sut.delete(entry, subIdent);
		dbInstance.commitAndCloseSession();
		
		List<Topic> topics = sut.loadTopics(entry, null);
		
		assertThat(topics)
				.containsExactlyInAnyOrder(topic1, topic3)
				.doesNotContain(topic2);
	}
	
	@Test
	public void shouldLoadByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		TopicRef topic = sut.createTopic(entry, subIdent);
		dbInstance.commitAndCloseSession();
		
		Topic reloadedTopic = sut.loadByKey(topic);
		
		assertThat(reloadedTopic).isEqualTo(topic);
	}

	@Test
	public void shouldUpdateTopic() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		Topic topic = sut.createTopic(entry, subIdent);
		dbInstance.commitAndCloseSession();
		
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		topic.setType(Type.finding);
		topic.setAutoConfirmation(true);
		topic.setMultiParticipation(false);
		topic.setParticipationVisible(false);
		sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		
		topic = sut.loadByKey(topic);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(topic.getTitle()).isEqualTo(title);
		softly.assertThat(topic.getDescription()).isEqualTo(description);
		softly.assertThat(topic.getType()).isEqualByComparingTo(Type.finding);
		softly.assertThat(topic.isAutoConfirmation()).isTrue();
		softly.assertThat(topic.isMultiParticipation()).isFalse();
		softly.assertThat(topic.isParticipationVisible()).isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldSetGroup() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = sut.createTopic(entry, random());
		dbInstance.commitAndCloseSession();
		
		Group group = groupDao.createGroup();
		topic = sut.setGroup(topic, group);
		
		assertThat(topic.getGroup()).isEqualTo(group);
	}
	
	@Test
	public void shouldLoadByRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		Topic topic1 = sut.createTopic(entry, subIdent);
		Topic topic2 = sut.createTopic(entry, subIdent);
		Topic topicOtherEntry = sut.createTopic(entryOther, subIdent);
		Topic topicOtherSubident = sut.createTopic(entry, random());
		dbInstance.commitAndCloseSession();
		
		List<Topic> topics = sut.loadTopics(entry, subIdent);
		
		assertThat(topics)
				.containsExactlyInAnyOrder(topic1, topic2)
				.doesNotContain(topicOtherEntry, topicOtherSubident);
	}

}
