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
package org.olat.modules.quality.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.olat.modules.forms.EvaluationFormParticipationStatus.prepared;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.ProviderHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExampleData extends OlatTestCase {
	
	private static final Random RANDOM = new SecureRandom();
	private static final String PREFIX = "bo-";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private BaseSecurityManager securityManager;
	
	@Ignore
	@Test
	public void createOrganisations() {
		List<Organisation> all = organisationService.getOrganisations();
		Organisation root = createIfNotExists(all, new OrganisationInput("Example", "root", null));
		Organisation mathnat = createIfNotExists(all, new OrganisationInput("Fakultät Mathematik/Naturwissenschaften", "mathnat", root));
		Organisation chemie = createIfNotExists(all, new OrganisationInput("Institut für Chemie", "chem", mathnat));
		Organisation physik = createIfNotExists(all, new OrganisationInput("Institut für Physik", "phys", mathnat));
		Organisation geo = createIfNotExists(all, new OrganisationInput("Geographisches Institut", "geo", mathnat));
		Organisation mathe = createIfNotExists(all, new OrganisationInput("Institut für Mathematik und Geometrie", "mathe", mathnat));
		Organisation phil = createIfNotExists(all, new OrganisationInput("Philosophische Fakultät", "phil", root));
		Organisation germ = createIfNotExists(all, new OrganisationInput("Seminar für Germanistik", "ger", phil));
		Organisation eng = createIfNotExists(all, new OrganisationInput("Englisches Seminar", "eng", phil));
		Organisation rus = createIfNotExists(all, new OrganisationInput("ру́сский институ́т", "rus", phil));
		dbInstance.commitAndCloseSession();
	}
	
	@Ignore
	@Test
	public void createCurriculumWithWithData() {
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Parameter parameter = Parameter.builder()
				.withName("Curriculum " + PREFIX)
				.withNumberLevel1(1)
				.withNumberLevel2(1)
				.withNumberLevel3(1)
				.withMinNumberParticipants(2)
				.withMaxNumberParticipants(5)
				.build();
		createCurriculum(defaultOrganisation, parameter);
		dbInstance.commitAndCloseSession();
	}
	
	private Organisation createIfNotExists(List<Organisation> all, OrganisationInput input) {
		String identifier = PREFIX + input.getIdentifier();
		String displayName = PREFIX + input.getDisplayName();
		
		for (Organisation organisation: all) {
			if (identifier.equals(organisation.getIdentifier())) {
				return organisation;
			}
		}
		
		return organisationService.createOrganisation(displayName, identifier, null, input.getParent(), null);
	}

	private Curriculum createIfNotExists(CurriculumInput input) {
		String identifier = PREFIX + input.getIdentifier();
		String displayName = PREFIX + input.getDisplayName();

		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setSearchString(displayName);
		List<Curriculum> curriculums = curriculumService.getCurriculums(params);
		if (!curriculums.isEmpty()) {
			return curriculums.get(0);
		}
		
		return curriculumService.createCurriculum(identifier, displayName, null, input.getOrganisation());
	}
	
	private CurriculumElement createIfNotExists(String prefix, CurriculumElementInput input) {
		String identifier = prefix + input.getIdentifier();
		String displayName = prefix + input.getDisplayName();
		
		List<CurriculumElement> curriculumElements = curriculumService.searchCurriculumElements(null, identifier, null);
		if (!curriculumElements.isEmpty()) {
			return curriculumElements.get(0);
		}
		
		return curriculumService.createCurriculumElement(identifier, displayName, CurriculumElementStatus.active, null,
				null, input.getParent(), null, CurriculumCalendars.disabled, CurriculumLectures.disabled,
				CurriculumLearningProgress.disabled, input.getCurriculum());
	}
	
	private void createCurriculum(Organisation org, Parameter param) {
		Curriculum cur = createIfNotExists(new CurriculumInput(param.getName(), param.getIdentifier(), org));

		Collection<Identity> participants = createIdentities(param.getMinNumberParticipants(), param.getMaxNumberParticipants());
		for (int i = 1; i <= param.getNumberLevel1(); i++) {
			createCurriculumElementLevel1(cur, param, participants, i);
		}
	}
	
	private Collection<Identity> createIdentities(int min, int max) {
		List<Identity> identities = new ArrayList<>();
		
		int numberParticipants = RANDOM.nextInt(max - min) + min;
		for (int i = 0; i < numberParticipants; i++) {
			Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("gen");
			identities.add(identity);
		}
		return identities;
	}
	
	private void createCurriculumElementLevel1(Curriculum cur, Parameter param, Collection<Identity> participants, int index) {
		String name = cur.getDisplayName() + " " + param.getNameLevel1() + index;
		String identifier = cur.getIdentifier() + param.getIdentifierDelim() + param.getIdentifierLevel1() + index;
		CurriculumElement element = createIfNotExists("", new CurriculumElementInput(name, identifier, cur, null));
		
		for (int i = 1; i <= param.getNumberLevel2(); i++) {
			createCurriculumElementLevel2(element, param, participants, i);
		}
	}
	
	private void createCurriculumElementLevel2(CurriculumElement parent, Parameter param, Collection<Identity> participants, int index) {
		String name = parent.getDisplayName() + " " + param.getNameLevel2() + index;
		String identifier = parent.getIdentifier() + param.getIdentifierDelim() + param.getIdentifierLevel2() + index;
		CurriculumElement element = createIfNotExists("", new CurriculumElementInput(name, identifier, parent.getCurriculum(), parent));
		
		for (int i = 1; i <= param.getNumberLevel3(); i++) {
			createCurriculumElementLevel3(element, param, participants, i);
		}
	}
	
	private void createCurriculumElementLevel3(CurriculumElement parent, Parameter param, Collection<Identity> participants, int index) {
		String name = parent.getDisplayName() + " " + param.getNameLevel3() + index;
		String identifier = parent.getIdentifier() + param.getIdentifierDelim() + param.getIdentifierLevel3() + index;
		CurriculumElement element = createIfNotExists("", new CurriculumElementInput(name, identifier, parent.getCurriculum(), parent));
		
		for (Identity identity: participants) {
			curriculumService.addMember(element.getCurriculum(), identity, CurriculumRoles.participant);
			curriculumService.addMember(element, identity, CurriculumRoles.participant);
		}
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gen");
		curriculumService.addMember(element.getCurriculum(), coach, CurriculumRoles.coach);
		curriculumService.addMember(element, coach, CurriculumRoles.coach);
		
		for (int i = 0; i < param.getNumberCourses(); i++) {
			Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
			if (admin != null) {
				RepositoryEntry course = JunitTestHelper.deployBasicCourse(admin);
				curriculumService.addRepositoryEntry(element, course, true);
				QualityDataCollection dataCollection = createDataCollection(getFormEntry(), course);
				generateRandomData(dataCollection);
			}
		}
	}
	
	@Ignore
	@Test
	public void createDataCollectionsForCourses() {
		Collection<Long> courseKeys = asList(93061120l);
		RepositoryEntry formEntry = getFormEntry();
		
		for (Long key: courseKeys) {
			RepositoryEntry course = repositoryService.loadByKey(key);
			QualityDataCollection dataCollection = createDataCollection(formEntry, course);
			generateRandomData(dataCollection);
		}
	}

	public RepositoryEntry getFormEntry() {
		Long formEntryKey = 39223296l;
		RepositoryEntry formEntry = repositoryService.loadByKey(formEntryKey);
		return formEntry;
	}
	
	public QualityDataCollection createDataCollection(RepositoryEntry formEntry, RepositoryEntry course) {
		Organisation organisation = organisationService.getDefaultOrganisation();
		Collection<Organisation> organisations = singletonList(organisation);
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry);
		dataCollection.setTitle(PREFIX + "Coach: " + course.getDisplayname());
		dataCollection.setStart(ProviderHelper.addDays(new Date(), "-1"));
		dataCollection.setDeadline(ProviderHelper.addDays(new Date(), "100"));
		dataCollection.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
		dataCollection.setTopicRepositoryEntry(course);
		dataCollection = qualityService.updateDataCollection(dataCollection);
		dataCollection = qualityService.updateDataCollectionStatus(dataCollection, QualityDataCollectionStatus.RUNNING);
		
		List<Identity> participants = repositoryService.getMembers(course, RepositoryEntryRelationType.all, "participant");
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, participants);
		for (EvaluationFormParticipation participation: participations) {
			qualityService.createContextBuilder(dataCollection, participation, course, GroupRoles.participant).build();
		}
		
		List<Identity> coaches = repositoryService.getMembers(course, RepositoryEntryRelationType.all, "coach");
		List<EvaluationFormParticipation> coachParticipations = qualityService.addParticipations(dataCollection, coaches);
		for (EvaluationFormParticipation coach: coachParticipations) {
			qualityService.createContextBuilder(dataCollection, coach, course, GroupRoles.coach).build();
		}
		return dataCollection;
	}
	
	@Ignore
	@Test
	public void generateRandomResponsesForDataCollections() {
		Collection<Long> dataCollectionKeys = asList(420l, 419l);
		
		generateRandomResponsesForDataCollection(dataCollectionKeys);
	}

	private void generateRandomResponsesForDataCollection(Collection<Long> dataCollectionKeys) {
		for (Long key: dataCollectionKeys) {
			QualityDataCollection dataCollection = qualityService.loadDataCollectionByKey(() -> key);
			generateRandomData(dataCollection);
		}
	}

	private void generateRandomData(QualityDataCollection dataCollection) {
		generateRandomResponses(dataCollection);
		dataCollection.setDeadline(new Date());
		dataCollection = qualityService.updateDataCollection(dataCollection);
		dataCollection = qualityService.updateDataCollectionStatus(dataCollection, QualityDataCollectionStatus.FINISHED);
	}
	
	private void generateRandomResponses(QualityDataCollection dataCollection) {
		EvaluationFormSurvey survey = qualityService.loadSurvey(dataCollection);
		if (survey != null) {
			generateRandomResponses(survey);
		}
	}
	
	public void generateRandomResponses(EvaluationFormSurvey survey) {
		Form form = evaluationFormManager.loadForm(survey.getFormEntry());
		if (form != null) {
			List<EvaluationFormParticipation> participations = evaluationFormManager.loadParticipations(survey, prepared, true);
			for (EvaluationFormParticipation participation : participations) {
				EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
				if (session == null) {
					session = evaluationFormManager.createSession(participation);
				}
				generateRandomResponses(session, form);
			}
		}
	}
	
	public void generateRandomResponses(EvaluationFormSession session, Form form) {
		ResponseSkipper skipper = new ResponseSkipper();
		for (AbstractElement element : form.getElements()) {
			if (element instanceof Rubric) {
				Rubric rubric = (Rubric) element;
				generateRandomResponses(session, skipper, rubric);
			} 
			if (skipper.isFillIn()) {
				if (element instanceof TextInput) {
					TextInput textInput = (TextInput) element;
					generateRandomStringResponse(session, textInput.getId());
				}
			}
		}
		evaluationFormManager.finishSession(session);
	}

	private void generateRandomResponses(EvaluationFormSession session, ResponseSkipper skipper, Rubric rubric) {
		int steps = rubric.getSteps();
		for (Slider slider : rubric.getSliders()) {
			if (skipper.isFillIn()) {
				generateResponse(session, slider, steps);
			}
		}
	}

	private void generateResponse(EvaluationFormSession session, Slider slider, int steps) {
		double value = RANDOM.nextInt(steps) + 1;
		evaluationFormManager.createNumericalResponse(slider.getId(), session, new BigDecimal(value));
	}
	
	private void generateRandomStringResponse(EvaluationFormSession session, String identifier) {
		String value = UUID.randomUUID().toString();
		evaluationFormManager.createStringResponse(identifier, session, value);
	}

}
