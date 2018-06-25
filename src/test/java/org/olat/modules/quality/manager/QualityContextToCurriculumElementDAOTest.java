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
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextToCurriculumElement;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityContextToCurriculumElementDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityContextToCurriculumElementDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateRelation() {
		QualityContext context = qualityTestHelper.createContext();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		dbInstance.commitAndCloseSession();
		
		QualityContextToCurriculumElement relation = sut.createRelation(context, curriculumElement);
		
		assertThat(relation).isNotNull();
		assertThat(relation.getKey()).isNotNull();
		assertThat(relation.getCreationDate()).isNotNull();
		assertThat(relation.getContext()).isEqualTo(context);
		assertThat(relation.getCurriculumElement()).isEqualTo(curriculumElement);
	}
	
	@Test
	public void shouldLoadRelationsByContextKey() {
		QualityContext context = qualityTestHelper.createContext();
		QualityContext otherContext = qualityTestHelper.createContext();
		CurriculumElement curriculumElement1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement curriculumElement2 = qualityTestHelper.createCurriculumElement();
		QualityContextToCurriculumElement relation1 = sut.createRelation(context, curriculumElement1);
		QualityContextToCurriculumElement relation2 = sut.createRelation(context, curriculumElement2);
		QualityContextToCurriculumElement otherRelation = sut.createRelation(otherContext, curriculumElement1);
		dbInstance.commitAndCloseSession();
		
		List<QualityContextToCurriculumElement> relations = sut.loadByContextKey(context);
		
		assertThat(relations)
				.containsExactlyInAnyOrder(relation1, relation2)
				.doesNotContain(otherRelation);
	}

	@Test
	public void shouldDeleteRelationsByKontextKey() {
		QualityContext context = qualityTestHelper.createContext();
		QualityContext otherContext = qualityTestHelper.createContext();
		CurriculumElement curriculumElement1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement curriculumElement2 = qualityTestHelper.createCurriculumElement();
		sut.createRelation(context, curriculumElement1);
		sut.createRelation(context, curriculumElement2);
		QualityContextToCurriculumElement otherRelation = sut.createRelation(otherContext, curriculumElement1);
		dbInstance.commitAndCloseSession();
		
		sut.deleteRelations(context);
		dbInstance.commitAndCloseSession();
		
		List<QualityContextToCurriculumElement> relations = sut.loadByContextKey(context);
		assertThat(relations).hasSize(0);
		
		List<QualityContextToCurriculumElement> otherRelations = sut.loadByContextKey(otherContext);
		assertThat(otherRelations).hasSize(1).contains(otherRelation);
	}

}
