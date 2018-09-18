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
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextToCurriculum;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityContextToCurriculumDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityContextToCurriculumDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateRelation() {
		QualityContext context = qualityTestHelper.createContext();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		dbInstance.commitAndCloseSession();
		
		QualityContextToCurriculum relation = sut.createRelation(context, curriculum);
		
		assertThat(relation).isNotNull();
		assertThat(relation.getKey()).isNotNull();
		assertThat(relation.getCreationDate()).isNotNull();
		assertThat(relation.getContext()).isEqualTo(context);
		assertThat(relation.getCurriculum()).isEqualTo(curriculum);
	}
	
	@Test
	public void shouldLoadRelationsByContextKey() {
		QualityContext context = qualityTestHelper.createContext();
		QualityContext otherContext = qualityTestHelper.createContext();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		QualityContextToCurriculum relation1 = sut.createRelation(context, curriculum1);
		QualityContextToCurriculum relation2 = sut.createRelation(context, curriculum2);
		QualityContextToCurriculum otherRelation = sut.createRelation(otherContext, curriculum1);
		dbInstance.commitAndCloseSession();
		
		List<QualityContextToCurriculum> relations = sut.loadByContextKey(context);
		
		assertThat(relations)
				.containsExactlyInAnyOrder(relation1, relation2)
				.doesNotContain(otherRelation);
	}

	@Test
	public void shouldDeleteRelationsByKontextKey() {
		QualityContext context = qualityTestHelper.createContext();
		QualityContext otherContext = qualityTestHelper.createContext();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		sut.createRelation(context, curriculum1);
		sut.createRelation(context, curriculum2);
		QualityContextToCurriculum otherRelation = sut.createRelation(otherContext, curriculum1);
		dbInstance.commitAndCloseSession();
		
		sut.deleteRelations(context);
		dbInstance.commitAndCloseSession();
		
		List<QualityContextToCurriculum> relations = sut.loadByContextKey(context);
		assertThat(relations).hasSize(0);
		
		List<QualityContextToCurriculum> otherRelations = sut.loadByContextKey(otherContext);
		assertThat(otherRelations).hasSize(1).contains(otherRelation);
	}
	
	@Test
	public void shouldCheckIfHasARelationToCurriculumElement() {
		QualityContext context = qualityTestHelper.createContext();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		sut.createRelation(context, curriculum);
		dbInstance.commitAndCloseSession();
		
		boolean hasRelations = sut.hasRelations(curriculum);
		
		assertThat(hasRelations).isTrue();
	}

	@Test
	public void shouldCheckIfHasNoRelationToCurriculum() {
		QualityContext context = qualityTestHelper.createContext();
		Curriculum element = qualityTestHelper.createCurriculum();
		Curriculum other = qualityTestHelper.createCurriculum();
		sut.createRelation(context, other);
		dbInstance.commitAndCloseSession();
		
		boolean hasRelations = sut.hasRelations(element);
		
		assertThat(hasRelations).isFalse();
	}

}
