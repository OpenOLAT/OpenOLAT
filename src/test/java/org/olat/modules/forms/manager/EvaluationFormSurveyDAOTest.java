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
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey previous = evaTestHelper.createSurvey();
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey survey = sut.createSurvey(ores, subIdent, formEntry, previous);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(survey).isNotNull();
		softly.assertThat(survey.getCreationDate()).isNotNull();
		softly.assertThat(survey.getLastModified()).isNotNull();
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
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, subIdent, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, subIdent);
		
		assertThat(loadedSurvey).isEqualTo(survey);
	}

	@Test
	public void shouldLoadByResourceableWithoutSubident() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, null);
		
		assertThat(loadedSurvey).isEqualTo(survey);
	}
	
	@Test
	public void shouldDeleteSurvey() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		sut.delete(survey);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, null);
		assertThat(loadedSurvey).isNull();
	}
	
	@Test
	public void shouldUpdateSeriesPrevious() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey previous = sut.createSurvey(ores, null, formEntry, null);
		EvaluationFormSurvey survey = sut.createSurvey(ores, null, formEntry, previous);
		EvaluationFormSurvey newPrevious = sut.createSurvey(ores, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		survey = sut.updateSeriesPrevious(survey, newPrevious);
		
		assertThat(survey.getSeriesPrevious()).isEqualTo(newPrevious);
	}

	@Test
	public void shouldCheckIfItHasNotSeriesNext() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurveyRef survey = sut.createSurvey(ores, null, formEntry, null);
		dbInstance.commitAndCloseSession();
		
		boolean hasSeriesNext = sut.hasSeriesNext(survey);
		
		assertThat(hasSeriesNext).isFalse();
	}
	
	@Test
	public void shouldCheckIfItHasSeriesNext() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(ores, null, formEntry, null);
		sut.createSurvey(ores, null, formEntry, survey);
		dbInstance.commitAndCloseSession();
		
		boolean hasSeriesNext = sut.hasSeriesNext(survey);
		
		assertThat(hasSeriesNext).isTrue();
	}

	@Test
	public void shouldLoadSeriesNext() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(ores, null, formEntry, null);
		EvaluationFormSurvey next = sut.createSurvey(ores, null, formEntry, survey);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSurvey loadedNext = sut.loadSeriesNext(survey);
		
		assertThat(loadedNext).isEqualTo(next);
	}
	
	@Test
	public void shouldReindexSeries() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey1 = sut.createSurvey(ores, "1", formEntry, null);
		EvaluationFormSurvey survey2 = sut.createSurvey(ores, "2", formEntry, survey1);
		EvaluationFormSurvey survey3 = sut.createSurvey(ores, "3", formEntry, survey2);
		EvaluationFormSurvey survey4 = sut.createSurvey(ores, "4", formEntry, survey3);
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
		survey1 = sut.loadByResourceable(ores, "1");
		softly.assertThat(survey1.getSeriesIndex()).isEqualTo(1);
		survey3 = sut.loadByResourceable(ores, "3");
		softly.assertThat(survey3.getSeriesIndex()).isEqualTo(2);
		survey4 = sut.loadByResourceable(ores, "4");
		softly.assertThat(survey4.getSeriesIndex()).isEqualTo(3);
	}


}
