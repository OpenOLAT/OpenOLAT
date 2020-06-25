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
package org.olat.course.nodes.appointments.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Topic;
import org.olat.course.nodes.appointments.TopicToGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicToGroupDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private GroupDAO groupDAO;
	
	@Autowired
	private TopicToGroupDAO sut;

	@Test
	public void shouldCreate() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = appointmentsService.createTopic(entry, random());
		Group group = groupDAO.createGroup();
		dbInstance.commitAndCloseSession();
		
		TopicToGroup topicToGroup = sut.create(topic, group);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(topicToGroup.getKey()).isNotNull();
		softly.assertThat(topicToGroup.getCreationDate()).isNotNull();
		softly.assertThat(topicToGroup.getTopic()).isEqualTo(topic);
		softly.assertThat(topicToGroup.getGroup()).isEqualTo(group);
		softly.assertAll();
	}
	
	@Test
	public void shouldDelete() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = appointmentsService.createTopic(entry, random());
		Group group = groupDAO.createGroup();
		TopicToGroup topicToGroup = sut.create(topic, group);
		dbInstance.commitAndCloseSession();
		
		sut.delete(topicToGroup);
		
		assertThat(sut.loadByKey(topicToGroup.getKey())).isNull();
	}
	
	@Test
	public void shouldDeleteByTopic() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic1 = appointmentsService.createTopic(entry1, random());
		Topic topic2 = appointmentsService.createTopic(entry1, random());
		Group group11 = groupDAO.createGroup();
		Group group12 = groupDAO.createGroup();
		Group group21 = groupDAO.createGroup();
		TopicToGroup topicToGroup11 = sut.create(topic1, group11);
		TopicToGroup topicToGroup12 = sut.create(topic1, group12);
		TopicToGroup topicToGroup21 = sut.create(topic2, group21);
		dbInstance.commitAndCloseSession();
		
		sut.delete(topic1);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.loadByKey(topicToGroup11.getKey())).isNull();
		softly.assertThat(sut.loadByKey(topicToGroup12.getKey())).isNull();
		softly.assertThat(sut.loadByKey(topicToGroup21.getKey())).isNotNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldDeleteByGroup() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic1 = appointmentsService.createTopic(entry1, random());
		Topic topic2 = appointmentsService.createTopic(entry1, random());
		Group group11 = groupDAO.createGroup();
		Group group12 = groupDAO.createGroup();
		Group group21 = groupDAO.createGroup();
		TopicToGroup topicToGroup11 = sut.create(topic1, group11);
		TopicToGroup topicToGroup12 = sut.create(topic1, group12);
		TopicToGroup topicToGroup21 = sut.create(topic2, group21);
		dbInstance.commitAndCloseSession();
		
		sut.delete(group12);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.loadByKey(topicToGroup11.getKey())).isNotNull();
		softly.assertThat(sut.loadByKey(topicToGroup12.getKey())).isNull();
		softly.assertThat(sut.loadByKey(topicToGroup21.getKey())).isNotNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldDeleteByRepositoryEntry() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic1 = appointmentsService.createTopic(entry1, random());
		Topic topic2 = appointmentsService.createTopic(entry2, random());
		Group group11 = groupDAO.createGroup();
		Group group12 = groupDAO.createGroup();
		Group group21 = groupDAO.createGroup();
		TopicToGroup topicToGroup11 = sut.create(topic1, group11);
		TopicToGroup topicToGroup12 = sut.create(topic1, group12);
		TopicToGroup topicToGroup21 = sut.create(topic2, group21);
		dbInstance.commitAndCloseSession();
		
		sut.delete(entry1, null);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.loadByKey(topicToGroup11.getKey())).isNull();
		softly.assertThat(sut.loadByKey(topicToGroup12.getKey())).isNull();
		softly.assertThat(sut.loadByKey(topicToGroup21.getKey())).isNotNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = appointmentsService.createTopic(entry, random());
		Group group = groupDAO.createGroup();
		TopicToGroup topicToGroup = sut.create(topic, group);
		dbInstance.commitAndCloseSession();
		
		TopicToGroup reloaded = sut.loadByKey(topicToGroup.getKey());
		
		assertThat(reloaded).isEqualTo(topicToGroup);
	}
	
	@Test
	public void shouldLoadByTopic() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic1 = appointmentsService.createTopic(entry1, random());
		Topic topic2 = appointmentsService.createTopic(entry1, random());
		Group group11 = groupDAO.createGroup();
		Group group12 = groupDAO.createGroup();
		Group group21 = groupDAO.createGroup();
		TopicToGroup topicToGroup11 = sut.create(topic1, group11);
		TopicToGroup topicToGroup12 = sut.create(topic1, group12);
		TopicToGroup topicToGroup21 = sut.create(topic2, group21);
		dbInstance.commitAndCloseSession();
		
		List<TopicToGroup> groups = sut.load(topic1);
		
		assertThat(groups)
				.containsExactlyInAnyOrder(topicToGroup11, topicToGroup12)
				.doesNotContain(topicToGroup21);
	}
	
	@Test
	public void shouldLoadGroupCountByTopic() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic1 = appointmentsService.createTopic(entry1, random());
		Topic topic2 = appointmentsService.createTopic(entry1, random());
		Group group11 = groupDAO.createGroup();
		Group group12 = groupDAO.createGroup();
		Group group21 = groupDAO.createGroup();
		sut.create(topic1, group11);
		sut.create(topic1, group12);
		sut.create(topic2, group21);
		dbInstance.commitAndCloseSession();
		
		Long count = sut.loadGroupCount(topic1);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoadGroupsByTopic() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic1 = appointmentsService.createTopic(entry1, random());
		Topic topic2 = appointmentsService.createTopic(entry1, random());
		Group group11 = groupDAO.createGroup();
		Group group12 = groupDAO.createGroup();
		Group group21 = groupDAO.createGroup();
		sut.create(topic1, group11);
		sut.create(topic1, group12);
		sut.create(topic2, group21);
		dbInstance.commitAndCloseSession();
		
		List<Group> groups = sut.loadGroups(topic1);
		
		assertThat(groups)
				.containsExactlyInAnyOrder(group11, group12)
				.doesNotContain(group21);
	}
	
	@Test
	public void shouldLoadRestrictedTopics() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Topic topicParticipant = appointmentsService.createTopic(entry, random());
		Topic topicCoach = appointmentsService.createTopic(entry, random());
		Topic topicNoMembership = appointmentsService.createTopic(entry, random());
		Topic topicNoGroups = appointmentsService.createTopic(entry, random());
		Group group11 = groupDAO.createGroup();
		Group group12 = groupDAO.createGroup();
		Group group2 = groupDAO.createGroup();
		Group group3 = groupDAO.createGroup();
		sut.create(topicParticipant, group11);
		sut.create(topicParticipant, group12);
		sut.create(topicCoach, group2);
		sut.create(topicNoMembership, group3);
		groupDAO.addMembershipOneWay(group11, identity, GroupRoles.participant.toString());
		groupDAO.addMembershipOneWay(group12, identity, GroupRoles.participant.toString());
		groupDAO.addMembershipOneWay(group2, identity, GroupRoles.coach.toString());
		dbInstance.commitAndCloseSession();
		
		List<Topic> topics = sut.loadRestrictedTopics(entry, null, identity);
		
		assertThat(topics)
				.containsExactlyInAnyOrder(
						topicParticipant,
						topicNoGroups)
				.doesNotContain(
						topicCoach,
						topicNoMembership
						);
	}

}
