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
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextToOrganisation;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityContextToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityContextToOrganisationDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateRelation() {
		QualityContext context = qualityTestHelper.createContext();
		Organisation curriculum = qualityTestHelper.createOrganisation();
		dbInstance.commitAndCloseSession();
		
		QualityContextToOrganisation relation = sut.createRelation(context, curriculum);
		
		assertThat(relation).isNotNull();
		assertThat(relation.getKey()).isNotNull();
		assertThat(relation.getCreationDate()).isNotNull();
		assertThat(relation.getContext()).isEqualTo(context);
		assertThat(relation.getOrganisation()).isEqualTo(curriculum);
	}
	
	@Test
	public void shouldLoadRelationsByContextKey() {
		QualityContext context = qualityTestHelper.createContext();
		QualityContext otherContext = qualityTestHelper.createContext();
		Organisation curriculum1 = qualityTestHelper.createOrganisation();
		Organisation curriculum2 = qualityTestHelper.createOrganisation();
		QualityContextToOrganisation relation1 = sut.createRelation(context, curriculum1);
		QualityContextToOrganisation relation2 = sut.createRelation(context, curriculum2);
		QualityContextToOrganisation otherRelation = sut.createRelation(otherContext, curriculum1);
		dbInstance.commitAndCloseSession();
		
		List<QualityContextToOrganisation> relations = sut.loadByContextKey(context);
		
		assertThat(relations)
				.containsExactlyInAnyOrder(relation1, relation2)
				.doesNotContain(otherRelation);
	}

	@Test
	public void shouldDeleteRelationsByKontextKey() {
		QualityContext context = qualityTestHelper.createContext();
		QualityContext otherContext = qualityTestHelper.createContext();
		Organisation curriculum1 = qualityTestHelper.createOrganisation();
		Organisation curriculum2 = qualityTestHelper.createOrganisation();
		sut.createRelation(context, curriculum1);
		sut.createRelation(context, curriculum2);
		QualityContextToOrganisation otherRelation = sut.createRelation(otherContext, curriculum1);
		dbInstance.commitAndCloseSession();
		
		sut.deleteRelations(context);
		dbInstance.commitAndCloseSession();
		
		List<QualityContextToOrganisation> relations = sut.loadByContextKey(context);
		assertThat(relations).hasSize(0);
		
		List<QualityContextToOrganisation> otherRelations = sut.loadByContextKey(otherContext);
		assertThat(otherRelations).hasSize(1).contains(otherRelation);
	}

}
