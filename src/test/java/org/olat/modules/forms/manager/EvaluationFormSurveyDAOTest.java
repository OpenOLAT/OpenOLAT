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
package org.olat.modules.forms.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormSurveyDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	
	@Autowired
	private EvaluationFormSurveyDAO sut;
	
	@Before
	public void cleanUp() {
		evaTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateSurvey() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		String subIdent = UUID.randomUUID().toString();
		String subIdent2 = UUID.randomUUID().toString();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey previous = evaTestHelper.createSurvey();
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey survey = sut.createSurvey(ores, subIdent, subIdent2, formEntry, previous);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(survey).isNotNull();
		softly.assertThat(survey.getCreationDate()).isNotNull();
		softly.assertThat(survey.getLastModified()).isNotNull();
		softly.assertThat(survey.getIdentifier().getSubident()).isEqualTo(subIdent);
		softly.assertThat(survey.getIdentifier().getSubident2()).isEqualTo(subIdent2);
		softly.assertThat(survey.getFormEntry()).isEqualTo(formEntry);
		softly.assertThat(survey.getSeriesKey()).isEqualTo(previous.getSeriesKey());
		softly.assertThat(survey.getSeriesPrevious()).isEqualTo(previous);
		softly.assertThat(survey.getSeriesIndex()).isEqualTo(2);
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadByResourceableWithSubident() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		String subIdent = UUID.randomUUID().toString();
		String subIdent2 = null;
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, subIdent, subIdent2, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, subIdent, subIdent2);
		
		assertThat(loadedSurvey).isEqualTo(survey);
	}
	
	@Test
	public void shouldLoadByResourceableWithSubident2() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		String subIdent = UUID.randomUUID().toString();
		String subIdent2 = UUID.randomUUID().toString();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, subIdent, subIdent2, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, subIdent, subIdent2);
		
		assertThat(loadedSurvey).isEqualTo(survey);
	}

	@Test
	public void shouldLoadByResourceableWithoutSubident() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, null, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, null, null);
		
		assertThat(loadedSurvey).isEqualTo(survey);
	}
	
	@Test
	public void shouldLoadSurveysByResourceableWithoutSubident() {
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		OLATResourceable oresA = JunitTestHelper.createRandomResource();
		String subIdentA1 = UUID.randomUUID().toString();
		EvaluationFormSurvey surveyA = sut.createSurvey(oresA, subIdentA1, null, formEntry, null);
		String subIdentB1 = UUID.randomUUID().toString();
		EvaluationFormSurvey surveyB = sut.createSurvey(oresA, subIdentB1, null, formEntry, null);
		OLATResourceable oresOther = JunitTestHelper.createRandomResource();
		EvaluationFormSurvey surveyOther = sut.createSurvey(oresOther, subIdentA1, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		List<EvaluationFormSurvey> surveys = sut.loadSurveysByResourceable(oresA, null, null);
		
		assertThat(surveys)
				.containsExactlyInAnyOrder(
					surveyA,
					surveyB)
				.doesNotContain(
					surveyOther
				);
	}
	
	@Test
	public void shouldDeleteSurvey() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, null, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		sut.delete(survey);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, null, null);
		assertThat(loadedSurvey).isNull();
	}
	
	@Test
	public void shouldUpdateSeriesPrevious() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey previous = sut.createSurvey(ores, null, null, formEntry, null);
		EvaluationFormSurvey survey = sut.createSurvey(ores, null, null, formEntry, previous);
		EvaluationFormSurvey newPrevious = sut.createSurvey(ores, null, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		survey = sut.updateSeriesPrevious(survey, newPrevious);
		
		assertThat(survey.getSeriesPrevious()).isEqualTo(newPrevious);
	}

	@Test
	public void shouldCheckIfItHasNotSeriesNext() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, null, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		boolean hasSeriesNext = sut.hasSeriesNext(survey);
		
		assertThat(hasSeriesNext).isFalse();
	}
	
	@Test
	public void shouldCheckIfItHasSeriesNext() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(ores, null, null, formEntry, null);
		sut.createSurvey(ores, null, null, formEntry, survey);
		dbInstance.commitAndCloseSession();
		
		boolean hasSeriesNext = sut.hasSeriesNext(survey);
		
		assertThat(hasSeriesNext).isTrue();
	}

	@Test
	public void shouldLoadSeriesNext() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(ores, null, null, formEntry, null);
		EvaluationFormSurvey next = sut.createSurvey(ores, null, null, formEntry, survey);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedNext = sut.loadSeriesNext(survey);
		
		assertThat(loadedNext).isEqualTo(next);
	}
	
	@Test
	public void shouldReindexSeries() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey1 = sut.createSurvey(ores, "1", null, formEntry, null);
		EvaluationFormSurvey survey2 = sut.createSurvey(ores, "2", null, formEntry, survey1);
		EvaluationFormSurvey survey3 = sut.createSurvey(ores, "3", null, formEntry, survey2);
		EvaluationFormSurvey survey4 = sut.createSurvey(ores, "4", null, formEntry, survey3);
		dbInstance.commitAndCloseSession();
		EvaluationFormSurvey next = sut.loadSeriesNext(survey2);
		EvaluationFormSurvey seriesPrevious = survey2.getSeriesPrevious();
		sut.updateSeriesPrevious(survey2, null);
		sut.updateSeriesPrevious(next, seriesPrevious);
		sut.delete(survey2);
		dbInstance.commitAndCloseSession();
		
		sut.reindexSeries(survey2.getKey());
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		survey1 = sut.loadByResourceable(ores, "1", null);
		softly.assertThat(survey1.getSeriesIndex()).isEqualTo(1);
		survey3 = sut.loadByResourceable(ores, "3", null);
		softly.assertThat(survey3.getSeriesIndex()).isEqualTo(2);
		survey4 = sut.loadByResourceable(ores, "4", null);
		softly.assertThat(survey4.getSeriesIndex()).isEqualTo(3);
	}


}
