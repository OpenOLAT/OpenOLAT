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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.manager.EvaluationFormTestsHelper;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityManager;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
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
	private QualityContextDAO qualityContextDao;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private TaxonomyService taxonomyService;
	
	void deleteAll() {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from contexttocurriculum")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from contexttocurriculumelement")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from contexttoorganisation")
					.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from contexttotaxonomylevel")
					.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitycontext")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitydatacollection")
				.executeUpdate();
		evaTestHelper.deleteAll();
		dbInstance.commitAndCloseSession();
	}

	public QualityDataCollection createDataCollection(String title) {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dataCollection = qualityManager.createDataCollection(formEntry);
		dataCollection.setTitle(title);
		dataCollection.setStart(new Date());
		dataCollection.setDeadline(new Date());
		return dataCollection;
	}
	
	QualityDataCollection createDataCollection() {
		return createDataCollection(UUID.randomUUID().toString());
	}
	
	QualityContext createContext() {
		return qualityContextDao.createContext(createDataCollection(), createParticipation(), createRepositoryEntry());
	}

	EvaluationFormSurvey createSurvey(QualityDataCollection dataCollection) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		return evaluationFormManager.createSurvey(dataCollection, null, entry);
	}

	EvaluationFormSurvey createRandomSurvey() {
		OLATResource ores = JunitTestHelper.createRandomResource();
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		return evaluationFormManager.createSurvey(ores, null, entry);
	}
	
	EvaluationFormParticipation createParticipation() {
		return evaluationFormManager.createParticipation(createRandomSurvey());
	}

	EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey) {
		return evaluationFormManager.createParticipation(survey);
	}

	EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey, Identity identity) {
		return evaluationFormManager.createParticipation(survey, identity);
	}

	void addParticipations(QualityDataCollection dataCollection, List<Identity> executors) {
		qualityManager.addParticipations(dataCollection, executors);
	}

	Organisation createOrganisation() {
		return organisationService.createOrganisation(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, null, null);
	}

	Curriculum createCurriculum() {
		return curriculumService.createCurriculum("i", "d", "d", createOrganisation());
	}

	CurriculumElement createCurriculumElement() {
		return curriculumService.createCurriculumElement("i", "d", null, null, null, null, createCurriculum());
	}

	RepositoryEntry createRepositoryEntry() {
		return JunitTestHelper.createAndPersistRepositoryEntry();
	}

	TaxonomyLevel createTaxonomyLevel() {
		Taxonomy taxonomy = taxonomyService.createTaxonomy(UUID.randomUUID().toString(), "d", "d", null);
		return taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), "d", "d", null, null, null, taxonomy);
	}

}
