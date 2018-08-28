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

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.forms.EvaluationFormSurvey;
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
		dbInstance.commit();
		
		EvaluationFormSurvey survey = sut.createSurvey(ores, subIdent, formEntry, previous);
		dbInstance.commit();
		
		assertThat(survey).isNotNull();
		assertThat(survey.getCreationDate()).isNotNull();
		assertThat(survey.getLastModified()).isNotNull();
		assertThat(survey.getFormEntry()).isEqualTo(formEntry);
		assertThat(survey.getPrevious()).isEqualTo(previous);
	}
	
	@Test
	public void shouldLoadByResourceableWithSubident() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		String subIdent = UUID.randomUUID().toString();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(ores, subIdent, formEntry, null);
		dbInstance.commit();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, subIdent);
		
		assertThat(loadedSurvey).isEqualTo(survey);
	}

	@Test
	public void shouldLoadByResourceableWithoutSubident() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(ores, null, formEntry, null);
		dbInstance.commit();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, null);
		
		assertThat(loadedSurvey).isEqualTo(survey);
	}
	
	@Test
	public void shouldDeleteSurvey() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(ores, null, formEntry, null);
		dbInstance.commit();
		
		sut.delete(survey);
		dbInstance.commit();
		
		EvaluationFormSurvey loadedSurvey = sut.loadByResourceable(ores, null);
		assertThat(loadedSurvey).isNull();
	}

}
