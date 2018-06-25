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

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultQualityContextBuilderTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private QualityContextDAO contextDAO;
	
	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateContextAndAllRelations() {
		RepositoryEntry repositoryEntry = qualityTestHelper.createRepositoryEntry();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		TaxonomyLevel taxonomyLevel = qualityTestHelper.createTaxonomyLevel();
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		dbInstance.commit();
		
		QualityContext context = DefaultQualityContextBuilder.builder(dataCollection, evaluationFormParticipation)
			.withRepositoryEntry(repositoryEntry)
			.addCurriculum(qualityTestHelper.createCurriculum())
			.addCurriculumElement(qualityTestHelper.createCurriculumElement())
			.addOrganisation(qualityTestHelper.createOrganisation())
			.addTaxonomyLevel(qualityTestHelper.createTaxonomyLevel())
			.addCurriculum(curriculum)
			.addCurriculum(curriculum)
			.addCurriculumElement(curriculumElement)
			.addCurriculumElement(curriculumElement)
			.addTaxonomyLevel(taxonomyLevel)
			.addTaxonomyLevel(taxonomyLevel)
			.build();
		
		assertThat(context).isNotNull();
		assertThat(context.getKey()).isNotNull();
		assertThat(context.getDataCollection()).isEqualTo(dataCollection);
		assertThat(context.getEvaluationFormParticipation()).isEqualTo(evaluationFormParticipation);
		assertThat(context.getEvaluationFormSession()).isNull();
		assertThat(context.getRepositoryEntry()).isEqualTo(repositoryEntry);
		assertThat(context.getContextToCurriculum()).hasSize(2);
		assertThat(context.getContextToCurriculumElement()).hasSize(2);
		assertThat(context.getContextToOrganisation()).hasSize(1);
		assertThat(context.getContextToTaxonomyLevel()).hasSize(2);
	}
	
	@Test
	public void shouldDelteContexts() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		DefaultQualityContextBuilder builder = DefaultQualityContextBuilder.builder(dataCollection, evaluationFormParticipation);
		QualityContext context = builder.build();
		QualityContext reloadedContext = contextDAO.loadByKey(context);
		assertThat(reloadedContext).isNotNull();
		
		builder.addToDelete(context);
		QualityContext context2 = builder.build();
		QualityContext reloadedContext2 = contextDAO.loadByKey(context2);
		assertThat(reloadedContext2).isNotNull();
		reloadedContext = contextDAO.loadByKey(context);
		assertThat(reloadedContext).isNull();
	}

}
