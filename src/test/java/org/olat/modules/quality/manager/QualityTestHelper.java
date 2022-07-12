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

import static java.util.Collections.singletonList;
import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;
import static org.olat.test.JunitTestHelper.random;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.OrganisationService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.manager.EvaluationFormTestsHelper;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
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
	private QualityService qualityService;
	@Autowired
	private QualityContextDAO qualityContextDao;
	@Autowired
	private QualityReminderDAO reminderDao;
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
	@Autowired
	private QualityGeneratorService generatorService;
	
	public void deleteAll() {
		evaTestHelper.deleteAll();
	}
	
	public QualityDataCollection createDataCollection(Organisation organisation, RepositoryEntry formEntry) {
		List<Organisation> organisations = Collections.singletonList(organisation);
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry);
		return dataCollection;	
	}

	QualityDataCollection createDataCollection(String title, Organisation organisation) {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		List<Organisation> organisations = Collections.singletonList(organisation);
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry);
		initDataCollection(dataCollection, title);
		return dataCollection;
	}

	private void initDataCollection(QualityDataCollection dataCollection, String title) {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		dataCollection.setTitle(title);
		dataCollection.setStart(new Date());
		dataCollection.setDeadline(new Date());
		dataCollection.setTopicRepositoryEntry(formEntry);
		dataCollection.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
	}
	
	QualityDataCollection createDataCollection() {
		return createDataCollection(UUID.randomUUID().toString());
	}
	
	QualityDataCollection createDataCollection(String title) {
		return createDataCollection(title, organisationService.getDefaultOrganisation());
	}
	
	QualityDataCollection createDataCollection(Organisation organisation) {
		return createDataCollection(UUID.randomUUID().toString(), organisation);
	}
	
	QualityDataCollection createDataCollection(QualityDataCollection previous) {
		List<Organisation> organisations = Collections.singletonList(organisationService.getDefaultOrganisation());
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, previous, null, null);
		initDataCollection(dataCollection, UUID.randomUUID().toString());
		return dataCollection;
	}
	
	QualityDataCollection createDataCollectionWithoutValues() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		List<Organisation> organisations = Collections.singletonList(organisationService.getDefaultOrganisation());
		return qualityService.createDataCollection(organisations, formEntry);
	}

	QualityDataCollection createDataCollectionWithoutOrganisation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		return qualityService.createDataCollection(Collections.emptyList(), formEntry);
	}
	
	QualityDataCollection createDataCollectionWithStartInFuture() {
		QualityDataCollection dataCollection = createDataCollection();
		dataCollection.setStart(getDateInFuture());
		qualityService.updateDataCollection(dataCollection);
		return dataCollection;
	}
	
	QualityDataCollection createDataCollectionWithStartInPast() {
		QualityDataCollection dataCollection = createDataCollection();
		dataCollection.setStart(getDateInPast());
		qualityService.updateDataCollection(dataCollection);
		return dataCollection;
	}
	
	QualityDataCollection createDataCollectionWithDeadlineInFuture() {
		QualityDataCollection dataCollection = createDataCollection();
		dataCollection.setDeadline(getDateInFuture());
		qualityService.updateDataCollection(dataCollection);
		return dataCollection;
	}
	
	QualityDataCollection createDataCollectionWithDeadlineInPast() {
		QualityDataCollection dataCollection = createDataCollection();
		dataCollection.setDeadline(getDateInPast());
		qualityService.updateDataCollection(dataCollection);
		return dataCollection;
	}

	public Date getDateInPast() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		return calendar.getTime();
	}

	public Date getDateInFuture() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		return calendar.getTime();
	}
	
	public QualityDataCollection updateStatus(QualityDataCollection dataCollection, QualityDataCollectionStatus status) {
		return qualityService.updateDataCollectionStatus(dataCollection, status);
	}

	QualityContext createContext() {
		return qualityContextDao.createContext(createDataCollection(), createParticipation(), QualityContextRole.owner,
				null, createRepositoryEntry(), createCurriculumElement());
	}

	QualityContext createContext(QualityDataCollection dataCollection, EvaluationFormParticipation participation) {
		return qualityContextDao.createContext(dataCollection, participation, QualityContextRole.owner, null,
				createRepositoryEntry(), createCurriculumElement());
	}

	EvaluationFormSurvey createSurvey(QualityDataCollection dataCollection) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		return evaluationFormManager.createSurvey(of(dataCollection), entry);
	}

	EvaluationFormSurvey createRandomSurvey() {
		OLATResource ores = JunitTestHelper.createRandomResource();
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		return evaluationFormManager.createSurvey(of(ores), entry);
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

	EvaluationFormSession createSession(EvaluationFormParticipation participation) {
		return evaluationFormManager.createSession(participation);
	}

	List<EvaluationFormParticipation> addParticipations(QualityDataCollection dataCollection, List<Identity> executors) {
		return qualityService.addParticipations(dataCollection, executors);
	}

	public Organisation createOrganisation() {
		return createOrganisation(null);
	}
	
	public Organisation createOrganisation(Organisation parent) {
		return organisationService.createOrganisation(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, parent, null);
	}
	
	public Curriculum createCurriculum() {
		return createCurriculum(createOrganisation());
	}

	public Curriculum createCurriculum(Organisation organisation) {
		return curriculumService.createCurriculum("i", "d", "d", organisation);
	}

	public CurriculumElement createCurriculumElement() {
		return createCurriculumElement(createCurriculum());
	}
	
	public CurriculumElement createCurriculumElement(Curriculum curriculum) {
		return curriculumService.createCurriculumElement("i", "d", CurriculumElementStatus.active, null, null, null,
				null, CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
	}

	public RepositoryEntry createRepositoryEntry() {
		return JunitTestHelper.createAndPersistRepositoryEntry();
	}

	public Taxonomy createTaxonomy() {
		return taxonomyService.createTaxonomy(UUID.randomUUID().toString(), "d", "d", null);
	}

	public TaxonomyLevel createTaxonomyLevel() {
		Taxonomy taxonomy = createTaxonomy();
		return createTaxonomyLevel(taxonomy);
	}
	
	public TaxonomyLevel createTaxonomyLevel(Taxonomy taxonomy) {
		return taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, taxonomy);
	}
	
	public TaxonomyLevel createTaxonomyLevel(TaxonomyLevel parent) {
		return taxonomyService.createTaxonomyLevel(random(), random(), null, null, parent, parent.getTaxonomy());
	}

	QualityReminder createReminder() {
		return createReminder(createDataCollection());
	}

	QualityReminder createReminder(QualityDataCollectionRef dataCollectionRef) {
		Date sendDate = new Date();
		QualityReminderType type = QualityReminderType.REMINDER1;
		return reminderDao.create(dataCollectionRef, sendDate, type);
	}

	public QualityGenerator createGenerator() {
		return generatorService.createGenerator("type", singletonList(createOrganisation()));
	}
	
	public QualityGenerator createGenerator(Collection<Organisation> organsations) {
		return generatorService.createGenerator("type", organsations);
	}

}

