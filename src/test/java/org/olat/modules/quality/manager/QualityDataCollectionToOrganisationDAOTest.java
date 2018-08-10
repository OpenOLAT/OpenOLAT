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
package org.olat.modules.quality.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionToOrganisation;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityDataCollectionToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityDataCollectionToOrganisationDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateRelation() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollectionWithoutOrganisation();
		Organisation organisation = qualityTestHelper.createOrganisation();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionToOrganisation relation = sut.createRelation(dataCollection, organisation);
		
		assertThat(relation).isNotNull();
		assertThat(relation.getCreationDate()).isNotNull();
		assertThat(relation.getDataCollection()).isEqualTo(dataCollection);
		assertThat(relation.getOrganisation()).isEqualTo(organisation);
	}
	
	@Test
	public void shouldLoadRelationsByDataCollectionKey() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollectionWithoutOrganisation();
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollectionWithoutOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		QualityDataCollectionToOrganisation relation1 = sut.createRelation(dataCollection, organisation1);
		QualityDataCollectionToOrganisation relation2 = sut.createRelation(dataCollection, organisation2);
		QualityDataCollectionToOrganisation otherRelation = sut.createRelation(otherDataCollection, organisation1);
		dbInstance.commitAndCloseSession();
		
		List<QualityDataCollectionToOrganisation> relations = sut.loadByDataCollectionKey(dataCollection);
		
		assertThat(relations)
				.containsExactlyInAnyOrder(relation1, relation2)
				.doesNotContain(otherRelation);
	}

	@Test
	public void shouldDeleteRelationsByKontextKey() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollectionWithoutOrganisation();
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollectionWithoutOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		sut.createRelation(dataCollection, organisation1);
		sut.createRelation(dataCollection, organisation2);
		QualityDataCollectionToOrganisation otherRelation = sut.createRelation(otherDataCollection, organisation1);
		dbInstance.commitAndCloseSession();
		
		sut.deleteRelations(dataCollection);
		dbInstance.commitAndCloseSession();
		
		List<QualityDataCollectionToOrganisation> relations = sut.loadByDataCollectionKey(dataCollection);
		assertThat(relations).hasSize(0);
		
		List<QualityDataCollectionToOrganisation> otherRelations = sut.loadByDataCollectionKey(otherDataCollection);
		assertThat(otherRelations).hasSize(1).contains(otherRelation);
	}
	
	@Test
	public void shouldLoadOrganisationsByDataCollectionKey() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollectionWithoutOrganisation();
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollectionWithoutOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Organisation otherOrganisation = qualityTestHelper.createOrganisation();
		sut.createRelation(dataCollection, organisation1);
		sut.createRelation(dataCollection, organisation2);
		sut.createRelation(otherDataCollection, otherOrganisation);
		dbInstance.commitAndCloseSession();
		
		List<Organisation> organisations = sut.loadOrganisationsByDataCollectionKey(dataCollection);
		
		assertThat(organisations)
				.containsExactlyInAnyOrder(organisation1, organisation2)
				.doesNotContain(otherOrganisation);
	}

}
