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
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionImpl;
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
public class TBCustomFieldDefinitionDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	
	@Autowired
	private TBCustomFieldDefinitionDAO sut;
	
	@Test
	public void shouldCreateDefinition() {
		TBBroker broker = createRandomBroker();
		
		TBCustomFieldDefinition definition = sut.createDefinition(broker, random());
		dbInstance.commitAndCloseSession();
		
		assertThat(definition).isNotNull();
		assertThat(definition.getCreationDate()).isNotNull();
		assertThat(definition.getLastModified()).isNotNull();
		assertThat(definition.getBroker()).isEqualTo(broker);
	}
	
	@Test
	public void shouldCreateDefinition_initSortOrder() {
		TBBroker broker = createRandomBroker();
		
		TBCustomFieldDefinition definition = sut.createDefinition(broker, random());
		dbInstance.commitAndCloseSession();
		assertThat(definition.getSortOrder()).isEqualTo(1);
		
		sut.createDefinition(broker, random());
		TBCustomFieldDefinition definition3 = sut.createDefinition(broker, random());
		assertThat(definition3.getSortOrder()).isEqualTo(3);
		
		int sortOrder = 6;
		((TBCustomFieldDefinitionImpl)definition).setSortOrder(sortOrder);
		sut.updateDefinition(definition);
		dbInstance.commitAndCloseSession();
		
		TBCustomFieldDefinition definition4 = sut.createDefinition(broker, random());
		assertThat(definition4.getSortOrder()).isEqualTo(7);
	}
	
	@Test
	public void shouldUpdateDefinition() {
		TBCustomFieldDefinition definition = createRandomDefinition();
		
		String identifier = JunitTestHelper.random();
		definition.setIdentifier(identifier);
		String name = random();
		definition.setName(name);
		int sortOrder = 5;
		((TBCustomFieldDefinitionImpl)definition).setSortOrder(sortOrder);
		((TBCustomFieldDefinitionImpl)definition).setDeletedDate(new Date());
		sut.updateDefinition(definition);
		dbInstance.commitAndCloseSession();
		
		TBCustomFieldDefinitionSearchParams params = new TBCustomFieldDefinitionSearchParams();
		params.setDefinition(definition);
		params.setDeleted(Boolean.TRUE);
		TBCustomFieldDefinition reloadedDefinition = sut.loadDefinitions(params).get(0);
		assertThat(reloadedDefinition.getIdentifier()).isEqualTo(identifier);
		assertThat(reloadedDefinition.getName()).isEqualTo(name);
		assertThat(reloadedDefinition.getSortOrder()).isEqualTo(sortOrder);
		assertThat(reloadedDefinition.getDeletedDate()).isNotNull();
	}
	
	@Test
	public void shouldDelete() {
		TBCustomFieldDefinition definition = createRandomDefinition();
		
		TBCustomFieldDefinitionSearchParams params = new TBCustomFieldDefinitionSearchParams();
		params.setDefinition(definition);
		assertThat(sut.loadDefinitions(params)).isNotEmpty();
		
		sut.deleteDefinition(definition);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadDefinitions(params)).isEmpty();
	}
	
	@Test
	public void shouldDelete_ByBroker() {
		TBCustomFieldDefinition definition1 = createRandomDefinition();
		TBCustomFieldDefinition definition2 = createRandomDefinition();
		
		TBCustomFieldDefinitionSearchParams params = new TBCustomFieldDefinitionSearchParams();
		params.setBroker(definition1.getBroker());
		assertThat(sut.loadDefinitions(params)).isNotEmpty();
		
		params.setBroker(definition2.getBroker());
		assertThat(sut.loadDefinitions(params)).isNotEmpty();
		
		sut.deleteDefinitions(definition2.getBroker());
		dbInstance.commitAndCloseSession();
		
		params.setBroker(definition1.getBroker());
		assertThat(sut.loadDefinitions(params)).isNotEmpty();
		
		params.setBroker(definition2.getBroker());
		assertThat(sut.loadDefinitions(params)).isEmpty();
	}
	
	@Test
	public void shouldLoadDefinitions_byKey() {
		TBCustomFieldDefinition definition1 = createRandomDefinition();
		TBCustomFieldDefinition definition2 = createRandomDefinition();
		createRandomDefinition();
		
		TBCustomFieldDefinitionSearchParams params = new TBCustomFieldDefinitionSearchParams();
		params.setDefinitions(List.of(definition1, definition2));
		List<TBCustomFieldDefinition> definitions = sut.loadDefinitions(params);
		
		assertThat(definitions).containsExactlyInAnyOrder(definition1, definition2);
	}
	
	@Test
	public void shouldLoadDefinitions_byBrokers() {
		TBCustomFieldDefinition definition1 = createRandomDefinition();
		TBCustomFieldDefinition definition2 = createRandomDefinition();
		createRandomDefinition();
		
		TBCustomFieldDefinitionSearchParams params = new TBCustomFieldDefinitionSearchParams();
		params.setBrokers(List.of(definition1.getBroker(), definition2.getBroker()));
		List<TBCustomFieldDefinition> definitions = sut.loadDefinitions(params);
		
		assertThat(definitions).containsExactlyInAnyOrder(definition1, definition2);
	}
	
	@Test
	public void shouldLoadDefinitions_byIdentifiers() {
		TBBroker broker = createRandomBroker();
		TBCustomFieldDefinition definition1 = createRandomDefinition(broker);
		definition1.setIdentifier(JunitTestHelper.random());
		definition1 = sut.updateDefinition(definition1);
		TBCustomFieldDefinition definition2 = createRandomDefinition(broker);
		definition2.setIdentifier(JunitTestHelper.random());
		definition2 = sut.updateDefinition(definition2);
		TBCustomFieldDefinition definition3 = createRandomDefinition(broker);
		definition3.setIdentifier(JunitTestHelper.random());
		definition3 = sut.updateDefinition(definition3);
		createRandomDefinition(broker);
		
		TBCustomFieldDefinitionSearchParams params = new TBCustomFieldDefinitionSearchParams();
		params.setIdentifiers(List.of(definition1.getIdentifier(), definition2.getIdentifier()));
		List<TBCustomFieldDefinition> definitions = sut.loadDefinitions(params);
		
		assertThat(definitions).containsExactlyInAnyOrder(definition1, definition2);
	}
	
	@Test
	public void shouldDefinitions_byDeleted() {
		TBCustomFieldDefinition definition1 = createRandomDefinition();
		TBCustomFieldDefinition definition2 = createRandomDefinition();
		((TBCustomFieldDefinitionImpl)definition2).setDeletedDate(new Date());
		definition2 = sut.updateDefinition(definition2);
		createRandomDefinition();
		
		TBCustomFieldDefinitionSearchParams params = new TBCustomFieldDefinitionSearchParams();
		params.setBrokers(List.of(definition1.getBroker(), definition2.getBroker()));
		assertThat(sut.loadDefinitions(params)).containsExactlyInAnyOrder(definition1);
		
		params.setDeleted(Boolean.TRUE);
		assertThat(sut.loadDefinitions(params)).containsExactlyInAnyOrder(definition2);
		
		params.setDeleted(null);
		assertThat(sut.loadDefinitions(params)).containsExactlyInAnyOrder(definition1, definition2);
	}
	
	private TBCustomFieldDefinition createRandomDefinition() {
		return createRandomDefinition(createRandomBroker());
	}
	
	private TBCustomFieldDefinition createRandomDefinition(TBBroker broker) {
		TBCustomFieldDefinition definition = sut.createDefinition(broker, random());
		dbInstance.commitAndCloseSession();
		
		return definition;
	}
	
	private TBBroker createRandomBroker() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = brokerDao.createBroker(repositoryEntry, random());
		dbInstance.commitAndCloseSession();
		
		return broker;
	}


}
