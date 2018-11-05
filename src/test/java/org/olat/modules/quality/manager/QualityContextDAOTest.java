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

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.repository.RepositoryEntry;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityContextDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityContextDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateContext() {
		QualityContextRole role = QualityContextRole.owner;
		String location = "Zürich";
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		RepositoryEntry repositoryEntry = qualityTestHelper.createRepositoryEntry();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		dbInstance.commitAndCloseSession();

		QualityContext context = sut.createContext(dataCollection, evaluationFormParticipation, role, location,
				repositoryEntry, curriculumElement);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(context).isNotNull();
		softly.assertThat(context.getKey()).isNotNull();
		softly.assertThat(context.getCreationDate()).isNotNull();
		softly.assertThat(context.getLastModified()).isNotNull();
		softly.assertThat(context.getDataCollection()).isEqualTo(dataCollection);
		softly.assertThat(context.getEvaluationFormParticipation()).isEqualTo(evaluationFormParticipation);
		softly.assertThat(context.getRole()).isEqualTo(role);
		softly.assertThat(context.getLocation()).isEqualTo(location);
		softly.assertThat(context.getAudienceRepositoryEntry()).isEqualTo(repositoryEntry);
		softly.assertThat(context.getAudienceCurriculumElement()).isEqualTo(curriculumElement);
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadByKey() {
		QualityContext context = qualityTestHelper.createContext();
		dbInstance.commitAndCloseSession();
		
		QualityContext reloadedContext = sut.loadByKey(context);
		
		assertThat(reloadedContext).isEqualTo(context);
	}
	
	@Test
	public void shouldLoadByDataCollection() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		QualityContext context1 = sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, null);
		QualityContext context2 = sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, null);
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation otherParticpation = qualityTestHelper.createParticipation();
		QualityContext otherContext = sut.createContext(otherDataCollection, otherParticpation, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		List<QualityContext> reloadedContext = sut.loadByDataCollection(dataCollection);
		
		assertThat(reloadedContext)
				.containsExactlyInAnyOrder(context1, context2)
				.doesNotContain(otherContext);
	}
	
	@Test
	public void shouldLoadByDataCollectionFetched() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		QualityContext context1 = sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, null);
		QualityContext context2 = sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, null);
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation otherParticpation = qualityTestHelper.createParticipation();
		QualityContext otherContext = sut.createContext(otherDataCollection, otherParticpation, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		List<QualityContext> reloadedContext = sut.loadByDataCollectionFetched(dataCollection);
		
		assertThat(reloadedContext)
				.containsExactlyInAnyOrder(context1, context2)
				.doesNotContain(otherContext);
	}
	
	@Test
	public void shouldLoadByParticipation() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		QualityContext context1 = sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, null);
		QualityContext context2 = sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, null);
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation otherParticpation = qualityTestHelper.createParticipation();
		QualityContext otherContext = sut.createContext(otherDataCollection, otherParticpation, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		List<QualityContext> reloadedContext = sut.loadByParticipation(evaluationFormParticipation);
		
		assertThat(reloadedContext)
				.containsExactlyInAnyOrder(context1, context2)
				.doesNotContain(otherContext);
	}
	
	@Test
	public void shouldLoadByWithoutAudience() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		RepositoryEntry repositoryEntry = qualityTestHelper.createRepositoryEntry();
		sut.createContext(dataCollection, evaluationFormParticipation, null, null, repositoryEntry, null);
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, curriculumElement);
		QualityContext context = sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		List<QualityContext> reloadedContext = sut.loadByWithoutAudience(evaluationFormParticipation);
		
		assertThat(reloadedContext.get(0)).isEqualTo(context);
	}
	
	@Test
	public void shouldLoadByAudienceRepositoryEntry() {
		QualityContextRole role = QualityContextRole.owner;
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		RepositoryEntry repositoryEntry = qualityTestHelper.createRepositoryEntry();
		QualityContext context = sut.createContext(dataCollection, evaluationFormParticipation, role, null, repositoryEntry, null);
		sut.createContext(dataCollection, evaluationFormParticipation, QualityContextRole.none, null, repositoryEntry, null);
		dbInstance.commitAndCloseSession();
		
		List<QualityContext> reloadedContext = sut.loadByAudienceRepositoryEntry(evaluationFormParticipation,
				repositoryEntry, role);
		
		assertThat(reloadedContext.get(0)).isEqualTo(context);
	}
	
	@Test
	public void shouldLoadByAudienceCurriculumElement() {
		QualityContextRole role = QualityContextRole.owner;
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		QualityContext context = sut.createContext(dataCollection, evaluationFormParticipation, role, null, null, curriculumElement);
		sut.createContext(dataCollection, evaluationFormParticipation, QualityContextRole.none, null, null, curriculumElement);
		dbInstance.commitAndCloseSession();
		
		List<QualityContext> reloadedContext = sut.loadByAudienceCurriculumElement(evaluationFormParticipation,
				curriculumElement, role);
		
		assertThat(reloadedContext.get(0)).isEqualTo(context);
	}
	
	@Test
	public void shouldCheckWhetherParticipationHasContexts() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		sut.createContext(dataCollection, evaluationFormParticipation, null, null, null, null);
		
		boolean hasContexts = sut.hasContexts(evaluationFormParticipation);

		assertThat(hasContexts).isTrue();
	}

	@Test
	public void shouldCheckWhetherParticipationHasNoContexts() {
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		
		boolean hasContexts = sut.hasContexts(evaluationFormParticipation);

		assertThat(hasContexts).isFalse();
	}
	
	@Test
	public void shouldAddSessionAndRemovePaticipationWhenFinishing() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation participation = qualityTestHelper.createParticipation();
		sut.createContext(dataCollection, participation, null,null, null, null);
		// Second context should be handled as well
		sut.createContext(dataCollection, participation, null,null, null, null);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSession session = qualityTestHelper.createSession(participation);
		sut.finish(participation, session);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		List<QualityContext> contexts = sut.loadByParticipation(participation);
		softly.assertThat(contexts).isEmpty();
		contexts = sut.loadBySession(session);
		for (QualityContext context : contexts) {
			softly.assertThat(context.getEvaluationFormParticipation()).isNull();
			softly.assertThat(context.getEvaluationFormSession()).isEqualTo(session);
		}
		softly.assertAll();
	}

	@Test
	public void shouldAddParticipationAndRemoveSessionWhenReopen() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation participation = qualityTestHelper.createParticipation();
		sut.createContext(dataCollection, participation, null, null, null, null);
		sut.createContext(dataCollection, participation, null, null, null, null);
		EvaluationFormSession session = qualityTestHelper.createSession(participation);
		sut.finish(participation, session);
		dbInstance.commitAndCloseSession();
		
		sut.reopen(session, participation);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		List<QualityContext> contexts = sut.loadBySession(session);
		softly.assertThat(contexts).isEmpty();
		contexts = sut.loadByParticipation(participation);
		for (QualityContext context : contexts) {
			assertThat(context.getEvaluationFormSession()).isNull();
			assertThat(context.getEvaluationFormParticipation()).isEqualTo(participation);
		}
		softly.assertAll();
	}

}
