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

import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.manager.EvaluationFormTestsHelper;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityManager;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityTestHelper {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityManager qualityManager;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	
	void deleteAll() {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitydatacollection")
				.executeUpdate();
		evaTestHelper.deleteAll();
		dbInstance.commitAndCloseSession();
	}

	QualityDataCollection createDataCollection() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		return qualityManager.createDataCollection(formEntry);
	}

	EvaluationFormSurvey createSurvey(QualityDataCollection dataCollection) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		return evaluationFormManager.createSurvey(dataCollection, null, entry);
	}

	EvaluationFormParticipationRef createParticipation(EvaluationFormSurvey survey) {
		return evaluationFormManager.createParticipation(survey);
	}

	EvaluationFormParticipationRef createParticipation(EvaluationFormSurvey survey, Identity identity) {
		return evaluationFormManager.createParticipation(survey, identity);
	}

	Organisation getOrganisation() {
		return organisationService.getDefaultOrganisation();
	}

	Curriculum createCuriculum() {
		return curriculumService.createCurriculum("i", "d", "d", getOrganisation());
	}

	CurriculumElement createCurriculumElement() {
		return curriculumService.createCurriculumElement("i", "d", null, null, null, null, createCuriculum());
	}

	RepositoryEntry createRepositoryEntry() {
		return JunitTestHelper.createAndPersistRepositoryEntry();
	}

}
