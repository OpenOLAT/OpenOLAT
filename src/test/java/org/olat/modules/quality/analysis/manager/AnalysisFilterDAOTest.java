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
package org.olat.modules.quality.analysis.manager;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 05.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisFilterDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	
	@Autowired
	private AnalysisFilterDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldLoadDistinctOrganisations() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addOrganisation(organisation1).addOrganisation(organisation2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addOrganisation(organisation2).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Organisation> filtered = sut.loadOrganisations(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(organisation1, organisation2)
				.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadDistinctCurriculum() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		// Participation with curriculum
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculum(curriculum1).build();
		// Participation with another curriculum
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculum(curriculum2).build();
		// Second participation with curriculum (to test distinct)
		QualityDataCollection dcDistinct = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsDistinct = qualityService.addParticipations(dcDistinct, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderDistinct = qualityService.createContextBuilder(dcDistinct, participationsDistinct.get(0));
		contextBuilderDistinct.addCurriculum(curriculum1).build();
		// Participation without curriculum (to test no nulls)
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcDistinct, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Curriculum> filtered = sut.loadCurriculums(searchParams);
		
		assertThat(filtered)
			.containsExactlyInAnyOrder(curriculum1, curriculum2)
			.doesNotContainNull();
	}

	@Test
	public void shouldLoadDistinctCurriculumElementPathes() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement();
		// Participation with curriculum element
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum element
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Second participation with curriculum element (to test distinct)
		QualityDataCollection dcDistinct = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsDistinct = qualityService.addParticipations(dcDistinct, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderDistinct = qualityService.createContextBuilder(dcDistinct, participationsDistinct.get(0));
		contextBuilderDistinct.addCurriculumElement(element1).build();
		// Participation without curriculum (to test no nulls)
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcDistinct, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<String> filtered = sut.loadCurriculumElementPathes(searchParams);
		
		assertThat(filtered)
			.containsExactlyInAnyOrder(element1.getMaterializedPathKeys(), element2.getMaterializedPathKeys())
			.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadDataCollectionCount() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsUser("");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsUser("");
		Identity executor3 = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = organisationService.createOrganisation("", "", null, null, null);
		// Data collection with three participations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dc1, asList(executor1, executor2, executor3));
		// Another data collection with three participations
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dc2, asList(executor1, executor2, executor3));
		// Data collection without participation
		QualityDataCollection dcWithout = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dcWithout));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		Long count = sut.loadFilterDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(3);
	}

	@Test
	public void shouldFilterByFinishedDataCollections() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dcFinished1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityTestHelper.updateStatus(dcFinished1, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dcFinishe2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityTestHelper.updateStatus(dcFinishe2, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dcRunning = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityTestHelper.updateStatus(dcRunning, QualityDataCollectionStatus.RUNNING);
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		Long count = sut.loadFilterDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByFormEntry() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherFormEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), otherFormEntry);
		finish(asList(dc1, dc2, dc3));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setFormEntryRef(formEntry);
		Long count = sut.loadFilterDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByDateRangeFrom() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Date now = new Date();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setDeadline(addDays(now, 1));
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setDeadline(addDays(now, 20));
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dcToEarly = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcToEarly.setDeadline(addDays(now, -2));
		qualityService.updateDataCollection(dcToEarly);
		finish(asList(dc1, dc2, dcToEarly));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setDateRangeFrom(now);
		Long count = sut.loadFilterDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByDateRangeTo() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Date now = new Date();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setDeadline(addDays(now, -1));
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setDeadline(addDays(now, -20));
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dcToEarly = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcToEarly.setDeadline(addDays(now, 2));
		qualityService.updateDataCollection(dcToEarly);
		finish(asList(dc1, dc2, dcToEarly));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setDateRangeTo(now);
		Long count = sut.loadFilterDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByCurriculums() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		Curriculum otherCurriculum = qualityTestHelper.createCurriculum();
		// Participation with curriculum
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculum(curriculum1).build();
		// Participation with another curriculum
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculum(curriculum2).build();
		// Participation with other curriculum
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculum(otherCurriculum).build();
		// Participation without curriculum
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setCurriculumRefs(asList(curriculum1, curriculum2));
		Long count = sut.loadFilterDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByCurriculumElements() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement();
		CurriculumElement subElement = curriculumService.createCurriculumElement("", "", null, null, element1, null, curriculum);
		CurriculumElement otherElement = qualityTestHelper.createCurriculumElement();
		// Participation with curriculum element
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum element
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Participation with a child element
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsChild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationsChild.get(0));
		contextBuilderChild.addCurriculumElement(subElement).build();
		// Participation with other curriculum element
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculumElement(otherElement).build();
		// Participation without curriculum
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcChild, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setCurriculumElementRefs(asList(element1, element2));
		Long count = sut.loadFilterDataCollectionCount(searchParams);
		
		long expected = asList(element1, element2, subElement).size();
		assertThat(count).isEqualTo(expected);
	}
	
	@Test
	public void shouldFilterByOrganisations() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Organisation subOrganisation = organisationService.createOrganisation("", "", null, organisation1, null);
		Organisation otherOrganisation = qualityTestHelper.createOrganisation();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addOrganisation(organisation1).addOrganisation(organisation2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addOrganisation(organisation2).build();
		// Participation in a child organisation (include them)
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationscild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationscild.get(0));
		contextBuilderChild.addOrganisation(subOrganisation).build();
		// Participation with an other organisation
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addOrganisation(otherOrganisation).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcChild, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setOrganisationRefs(asList(organisation1, organisation2));
		Long count = sut.loadFilterDataCollectionCount(searchParams);
		
		long expected = asList(organisation1, organisation2, subOrganisation).size();
		assertThat(count).isEqualTo(expected);
	}
	
	private void finish(Collection<QualityDataCollection> dataCollections) {
		for (QualityDataCollection dataCollection: dataCollections) {
			qualityTestHelper.updateStatus(dataCollection, QualityDataCollectionStatus.FINISHED);
		}
	}
	
	private static Date addDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

}
