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
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextToTaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityContextToTaxonomyLevelDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityContextToTaxonomyLevelDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateRelation() {
		QualityContext context = qualityTestHelper.createContext();
		TaxonomyLevel taxonomyLevel = qualityTestHelper.createTaxonomyLevel();
		dbInstance.commitAndCloseSession();
		
		QualityContextToTaxonomyLevel relation = sut.createRelation(context, taxonomyLevel);
		
		assertThat(relation).isNotNull();
		assertThat(relation.getKey()).isNotNull();
		assertThat(relation.getCreationDate()).isNotNull();
		assertThat(relation.getContext()).isEqualTo(context);
		assertThat(relation.getTaxonomyLevel()).isEqualTo(taxonomyLevel);
	}
	
	@Test
	public void shouldLoadRelationsByContextKey() {
		QualityContext context = qualityTestHelper.createContext();
		QualityContext otherContext = qualityTestHelper.createContext();
		TaxonomyLevel taxonomyLevel1 = qualityTestHelper.createTaxonomyLevel();
		TaxonomyLevel taxonomyLevel2 = qualityTestHelper.createTaxonomyLevel();
		QualityContextToTaxonomyLevel relation1 = sut.createRelation(context, taxonomyLevel1);
		QualityContextToTaxonomyLevel relation2 = sut.createRelation(context, taxonomyLevel2);
		QualityContextToTaxonomyLevel otherRelation = sut.createRelation(otherContext, taxonomyLevel1);
		dbInstance.commitAndCloseSession();
		
		List<QualityContextToTaxonomyLevel> relations = sut.loadByContextKey(context);
		
		assertThat(relations)
				.containsExactlyInAnyOrder(relation1, relation2)
				.doesNotContain(otherRelation);
	}

	@Test
	public void shouldDeleteRelationsByKontextKey() {
		QualityContext context = qualityTestHelper.createContext();
		QualityContext otherContext = qualityTestHelper.createContext();
		TaxonomyLevel taxonomyLevel1 = qualityTestHelper.createTaxonomyLevel();
		TaxonomyLevel taxonomyLevel2 = qualityTestHelper.createTaxonomyLevel();
		sut.createRelation(context, taxonomyLevel1);
		sut.createRelation(context, taxonomyLevel2);
		QualityContextToTaxonomyLevel otherRelation = sut.createRelation(otherContext, taxonomyLevel1);
		dbInstance.commitAndCloseSession();
		
		sut.deleteRelations(context);
		dbInstance.commitAndCloseSession();
		
		List<QualityContextToTaxonomyLevel> relations = sut.loadByContextKey(context);
		assertThat(relations).hasSize(0);
		
		List<QualityContextToTaxonomyLevel> otherRelations = sut.loadByContextKey(otherContext);
		assertThat(otherRelations).hasSize(1).contains(otherRelation);
	}

}
