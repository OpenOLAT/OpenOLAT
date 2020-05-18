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

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.appointments.Organizer;
import org.olat.course.nodes.appointments.Topic;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.04.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganizerDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TopicDAO topicDao;
	
	@Autowired
	private OrganizerDAO sut;
	
	@Test
	public void shouldCreateOrganizer() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Organizer organizer = sut.createOrganizer(topic, identity);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(organizer).isNotNull();
		softly.assertThat(organizer.getKey()).isNotNull();
		softly.assertThat(organizer.getCreationDate()).isNotNull();
		softly.assertThat(organizer.getLastModified()).isNotNull();
		softly.assertThat(organizer.getTopic()).isEqualTo(topic);
		softly.assertThat(organizer.getIdentity()).isEqualTo(identity);
		softly.assertAll();
	}
	
	@Test
	public void shouldDeleteOrganizersByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity4 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, subIdent);
		Organizer organizer1 = sut.createOrganizer(topic, identity1);
		Organizer organizer2 = sut.createOrganizer(topic, identity2);
		Organizer organizer3 = sut.createOrganizer(topic, identity3);
		Organizer organizer4 = sut.createOrganizer(topic, identity4);
		dbInstance.commitAndCloseSession();
		
		sut.deleteOrganizers(Arrays.asList(organizer3, organizer4));
		dbInstance.commitAndCloseSession();
		
		List<Organizer> organizers = sut.loadOrganizers(entry, subIdent);
		
		assertThat(organizers)
				.containsExactlyInAnyOrder(organizer1, organizer2)
				.doesNotContain(organizer3, organizer4);
	}
	
	@Test
	public void shouldDeleteByTopic() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic1 = topicDao.createTopic(entry, subIdent);
		Topic topic2 = topicDao.createTopic(entry, subIdent);
		Organizer organizer11 = sut.createOrganizer(topic1, identity1);
		Organizer organizer12 = sut.createOrganizer(topic1, identity2);
		Organizer organizer21 = sut.createOrganizer(topic2, identity1);
		Organizer organizer22 = sut.createOrganizer(topic2, identity2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(topic1);
		dbInstance.commitAndCloseSession();
		
		List<Organizer> organizers = sut.loadOrganizers(entry, subIdent);
		
		assertThat(organizers)
				.containsExactlyInAnyOrder(organizer21, organizer22)
				.doesNotContain(organizer11, organizer12);
	}
	
	@Test
	public void shouldDeleteByEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic1 = topicDao.createTopic(entry, random());
		Topic topic2 = topicDao.createTopic(entry, subIdent);
		Topic topic3 = topicDao.createTopic(entry, random());
		Organizer organizer1 = sut.createOrganizer(topic1, identity);
		Organizer organizer2 = sut.createOrganizer(topic2, identity);
		Organizer organizer3 = sut.createOrganizer(topic3, identity);
		dbInstance.commitAndCloseSession();
		
		sut.delete(entry, subIdent);
		dbInstance.commitAndCloseSession();
		
		List<Organizer> organizers = sut.loadOrganizers(entry, null);
		
		assertThat(organizers)
				.containsExactlyInAnyOrder(organizer1, organizer3)
				.doesNotContain(organizer2);
	}
	
	@Test
	public void shouldLoadByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Organizer organizer = sut.createOrganizer(topic, identity);
		dbInstance.commitAndCloseSession();
		
		Organizer reloadedOrganizer = sut.loadByKey(organizer.getKey());
		
		assertThat(reloadedOrganizer).isEqualTo(organizer);
	}
	
	@Test
	public void shouldLoadIdentitiesByTopic() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identityOther = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Topic topicOther = topicDao.createTopic(entry, JunitTestHelper.random());
		Organizer organizer1 = sut.createOrganizer(topic, identity1);
		Organizer organizer2 = sut.createOrganizer(topic, identity2);
		Organizer organizerOther = sut.createOrganizer(topicOther, identityOther);
		dbInstance.commitAndCloseSession();
		
		List<Organizer> organizers = sut.loadOrganizers(topic);
		
		assertThat(organizers)
				.containsExactlyInAnyOrder(organizer1, organizer2)
				.doesNotContain(organizerOther);
	}
	
	@Test
	public void shouldLoadOrganizersByRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		String subIdentOther = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic1 = topicDao.createTopic(entry, subIdent);
		Topic topic2 = topicDao.createTopic(entry, subIdent);
		Topic topicOtherOtherEntry = topicDao.createTopic(entryOther, subIdent);
		Topic topicOtherOtherSubident = topicDao.createTopic(entry, subIdentOther);
		Organizer organizer11 = sut.createOrganizer(topic1, identity1);
		Organizer organizer12 = sut.createOrganizer(topic1, identity2);
		Organizer organizer21 = sut.createOrganizer(topic2, identity1);
		Organizer organizerOtherEntry = sut.createOrganizer(topicOtherOtherEntry, identity1);
		Organizer organizerOtherSubIdent = sut.createOrganizer(topicOtherOtherSubident, identity1);
		dbInstance.commitAndCloseSession();
		
		List<Organizer> organizers = sut.loadOrganizers(entry, subIdent);
		
		assertThat(organizers)
				.containsExactlyInAnyOrder(organizer11, organizer12, organizer21)
				.doesNotContain(organizerOtherEntry, organizerOtherSubIdent);
	}

}
