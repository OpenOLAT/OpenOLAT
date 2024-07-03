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
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionImpl;
import org.olat.modules.topicbroker.model.TBTopicImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCustomFieldDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	@Autowired
	private TBCustomFieldDefinitionDAO customFieldDefinitionDao;
	@Autowired
	private TBTopicDAO topicDao;
	
	@Autowired
	private TBCustomFieldDAO sut;
	
	@Test
	public void shouldCreateCustomField() {
		TBBroker broker = createRandomBroker();
		TBCustomFieldDefinition definition = createRandomDefinition(broker);
		TBTopic topic = createTopic(broker);
		
		TBCustomField customField = sut.createCustomField(definition, topic);
		dbInstance.commitAndCloseSession();
		
		assertThat(customField).isNotNull();
		assertThat(customField.getCreationDate()).isNotNull();
		assertThat(customField.getLastModified()).isNotNull();
		assertThat(customField.getDefinition()).isEqualTo(definition);
		assertThat(customField.getTopic()).isEqualTo(topic);
	}
	
	@Test
	public void shouldUpdateCustomField() {
		TBCustomField customField = createRandomCustomField();
		
		String text = random();
		customField.setText(text);
		String filename = random();
		customField.setFilename(filename);
		sut.updateCustomField(customField);
		dbInstance.commitAndCloseSession();
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setCustomField(customField);
		TBCustomField reloadedCustomField = sut.loadCustomFields(params).get(0);
		assertThat(reloadedCustomField.getText()).isEqualTo(text);
		assertThat(reloadedCustomField.getFilename()).isEqualTo(filename);
	}
	
	@Test
	public void shouldDelete() {
		TBCustomField customField = createRandomCustomField();
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setCustomField(customField);
		assertThat(sut.loadCustomFields(params)).isNotEmpty();
		
		sut.deleteCustomField(customField);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadCustomFields(params)).isEmpty();
	}
	
	@Test
	public void shouldDelete_ByBroker() {
		TBCustomField customField1 = createRandomCustomField();
		TBCustomField customField2 = createRandomCustomField();
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setCustomField(customField1);
		assertThat(sut.loadCustomFields(params)).isNotEmpty();
		
		params.setCustomField(customField2);
		assertThat(sut.loadCustomFields(params)).isNotEmpty();
		
		sut.deleteCustomFields(customField2.getTopic().getBroker());
		dbInstance.commitAndCloseSession();
		
		params.setCustomField(customField1);
		assertThat(sut.loadCustomFields(params)).isNotEmpty();
		
		params.setCustomField(customField2);
		assertThat(sut.loadCustomFields(params)).isEmpty();
	}
	
	@Test
	public void shouldLoadCustomFields_byKey() {
		TBCustomField customField1 = createRandomCustomField();
		TBCustomField customField2 = createRandomCustomField();
		createRandomCustomField();
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setCustomFields(List.of(customField1, customField2));
		List<TBCustomField> customFields = sut.loadCustomFields(params);
		
		assertThat(customFields).containsExactlyInAnyOrder(customField1, customField2);
	}
	
	@Test
	public void shouldLoadCustomFields_byDefinitions() {
		TBCustomField customField1 = createRandomCustomField();
		TBCustomField customField2 = createRandomCustomField();
		createRandomCustomField();
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setDefinitions(List.of(customField1.getDefinition(), customField2.getDefinition()));
		List<TBCustomField> customFields = sut.loadCustomFields(params);
		
		assertThat(customFields).containsExactlyInAnyOrder(customField1, customField2);
	}
	
	@Test
	public void shouldLoadCustomFields_byTopics() {
		TBCustomField customField1 = createRandomCustomField();
		TBCustomField customField2 = createRandomCustomField();
		createRandomCustomField();
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setTopics(List.of(customField1.getTopic(), customField2.getTopic()));
		List<TBCustomField> customFields = sut.loadCustomFields(params);
		
		assertThat(customFields).containsExactlyInAnyOrder(customField1, customField2);
	}
	
	@Test
	public void shouldLoadCustomFields_byBrokers() {
		TBCustomField customField1 = createRandomCustomField();
		TBCustomField customField2 = createRandomCustomField();
		createRandomCustomField();
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setBrokers(List.of(customField1.getTopic().getBroker(), customField2.getTopic().getBroker()));
		List<TBCustomField> customFields = sut.loadCustomFields(params);
		
		assertThat(customFields).containsExactlyInAnyOrder(customField1, customField2);
	}
	
	@Test
	public void shouldDefinitions_byDeletedDefinitions() {
		TBCustomField customField1 = createRandomCustomField();
		TBCustomField customField2 = createRandomCustomField();
		TBCustomFieldDefinition definition2 = customField2.getDefinition();
		((TBCustomFieldDefinitionImpl)definition2).setDeletedDate(new Date());
		definition2 = customFieldDefinitionDao.updateDefinition(definition2);
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setCustomFields(List.of(customField1, customField2));
		assertThat(sut.loadCustomFields(params)).containsExactlyInAnyOrder(customField1);
		
		params.setDeletedDefinition(Boolean.TRUE);
		assertThat(sut.loadCustomFields(params)).containsExactlyInAnyOrder(customField2);
		
		params.setDeletedDefinition(null);
		assertThat(sut.loadCustomFields(params)).containsExactlyInAnyOrder(customField1, customField2);
	}
	
	@Test
	public void shouldDefinitions_byDeletedTopics() {
		TBCustomField customField1 = createRandomCustomField();
		TBCustomField customField2 = createRandomCustomField();
		TBTopic topic2 = customField2.getTopic();
		((TBTopicImpl)topic2).setDeletedDate(new Date());
		topic2 = topicDao.updateTopic(topic2);
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setCustomFields(List.of(customField1, customField2));
		assertThat(sut.loadCustomFields(params)).containsExactlyInAnyOrder(customField1);
		
		params.setDeletedTopic(Boolean.TRUE);
		assertThat(sut.loadCustomFields(params)).containsExactlyInAnyOrder(customField2);
		
		params.setDeletedTopic(null);
		assertThat(sut.loadCustomFields(params)).containsExactlyInAnyOrder(customField1, customField2);
	}
	
	@Test
	public void shouldLoadCustomFields_fetch() {
		TBCustomField customField = createRandomCustomField();
		
		TBCustomFieldSearchParams params = new TBCustomFieldSearchParams();
		params.setCustomField(customField);
		params.setFetchDefinition(true);
		params.setFetchTopic(true);
		params.setFetchIdentities(true);
		params.setFetchBroker(true);
		params.setFetchVfsMetadata(true);
		sut.loadCustomFields(params);
		
		// Just a syntay test
	}
	
	private TBCustomField createRandomCustomField() {
		TBBroker broker = createRandomBroker();
		return createRandomCustomField(createRandomDefinition(broker), createTopic(broker));
	}
	
	private TBCustomField createRandomCustomField(TBCustomFieldDefinition definition, TBTopic topic) {
		TBCustomField customField = sut.createCustomField(definition, topic);
		dbInstance.commitAndCloseSession();
		
		return customField;
	}
	
	private TBCustomFieldDefinition createRandomDefinition(TBBroker broker) {
		TBCustomFieldDefinition definition = customFieldDefinitionDao.createDefinition(broker, random());
		dbInstance.commitAndCloseSession();
		
		return definition;
	}
	
	private TBTopic createTopic(TBBroker broker) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
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
