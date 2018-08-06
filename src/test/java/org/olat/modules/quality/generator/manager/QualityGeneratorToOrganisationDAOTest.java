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
package org.olat.modules.quality.generator.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorToOrganisation;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityGeneratorToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityGeneratorToOrganisationDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateRelation() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		Organisation organisation = qualityTestHelper.createOrganisation();
		dbInstance.commitAndCloseSession();
		
		QualityGeneratorToOrganisation relation = sut.createRelation(generator, organisation);
		
		assertThat(relation).isNotNull();
		assertThat(relation.getCreationDate()).isNotNull();
		assertThat(relation.getGenerator()).isEqualTo(generator);
		assertThat(relation.getOrganisation()).isEqualTo(organisation);
	}
	
	@Test
	public void shouldLoadRelationsByGeneratorKey() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityGenerator otherGenerator = qualityTestHelper.createGenerator();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		QualityGeneratorToOrganisation relation1 = sut.createRelation(generator, organisation1);
		QualityGeneratorToOrganisation relation2 = sut.createRelation(generator, organisation2);
		QualityGeneratorToOrganisation otherRelation = sut.createRelation(otherGenerator, organisation1);
		dbInstance.commitAndCloseSession();
		
		List<QualityGeneratorToOrganisation> relations = sut.loadByGeneratorKey(generator);
		
		assertThat(relations)
				.containsExactlyInAnyOrder(relation1, relation2)
				.doesNotContain(otherRelation);
	}

	@Test
	public void shouldDeleteRelationsByKontextKey() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityGenerator otherGenerator = qualityTestHelper.createGenerator();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		sut.createRelation(generator, organisation1);
		sut.createRelation(generator, organisation2);
		QualityGeneratorToOrganisation otherRelation = sut.createRelation(otherGenerator, organisation1);
		dbInstance.commitAndCloseSession();
		
		sut.deleteRelations(generator);
		dbInstance.commitAndCloseSession();
		
		List<QualityGeneratorToOrganisation> relations = sut.loadByGeneratorKey(generator);
		assertThat(relations).hasSize(0);
		
		List<QualityGeneratorToOrganisation> otherRelations = sut.loadByGeneratorKey(otherGenerator);
		assertThat(otherRelations).hasSize(1).contains(otherRelation);
	}
	
	@Test
	public void shouldLoadOrganisationsByGeneratorKey() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityGenerator otherGenerator = qualityTestHelper.createGenerator();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Organisation otherOrganisation = qualityTestHelper.createOrganisation();
		sut.createRelation(generator, organisation1);
		sut.createRelation(generator, organisation2);
		sut.createRelation(otherGenerator, otherOrganisation);
		dbInstance.commitAndCloseSession();
		
		List<Organisation> organisations = sut.loadOrganisationsByGeneratorKey(generator);
		
		assertThat(organisations)
				.containsExactlyInAnyOrder(organisation1, organisation2)
				.doesNotContain(otherOrganisation);
	}

}
