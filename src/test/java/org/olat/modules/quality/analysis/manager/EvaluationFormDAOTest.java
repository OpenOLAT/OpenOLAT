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
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.modules.quality.analysis.EvaluationFormViewSearchParams;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private EvaluationFormManager evaManager;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private EvaluationFormDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldLoadOneEntryForEveryUsedEvaluationForm() {
		RepositoryEntry formEntryOnceUsed = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry formEntryTwiceUsed = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry formEntryNotUsed = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dataCollection1 = qualityService.createDataCollection(emptyList(), formEntryOnceUsed);
		qualityTestHelper.updateStatus(dataCollection1, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dataCollection2 = qualityService.createDataCollection(emptyList(), formEntryTwiceUsed);
		qualityTestHelper.updateStatus(dataCollection2, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dataCollection3 = qualityService.createDataCollection(emptyList(), formEntryTwiceUsed);
		qualityTestHelper.updateStatus(dataCollection3, QualityDataCollectionStatus.FINISHED);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormViewSearchParams searchParams = new EvaluationFormViewSearchParams();
		List<EvaluationFormView> forms = sut.load(searchParams );
		
		assertThat(forms).extracting(EvaluationFormView::getFormEntryKey)
				.containsExactlyInAnyOrder(formEntryOnceUsed.getKey(), formEntryTwiceUsed.getKey())
				.doesNotContain(formEntryNotUsed.getKey());
	}
	
	@Test
	public void shouldLoadNumberDataCollections() {
		RepositoryEntry formEntryOnceUsed = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry formEntryTwiceUsed = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dataCollection1 = qualityService.createDataCollection(emptyList(), formEntryOnceUsed);
		qualityTestHelper.updateStatus(dataCollection1, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dataCollection2 = qualityService.createDataCollection(emptyList(), formEntryTwiceUsed);
		qualityTestHelper.updateStatus(dataCollection2, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dataCollection3 = qualityService.createDataCollection(emptyList(), formEntryTwiceUsed);
		qualityTestHelper.updateStatus(dataCollection3, QualityDataCollectionStatus.FINISHED);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormViewSearchParams searchParams = new EvaluationFormViewSearchParams();
		List<EvaluationFormView> forms = sut.load(searchParams);
		
		Map<Long, Long> keyToNumerDataCollections = forms.stream()
				.collect(toMap(EvaluationFormView::getFormEntryKey, EvaluationFormView::getNumberDataCollections));
		assertThat(keyToNumerDataCollections.get(formEntryOnceUsed.getKey())).isEqualTo(1);
		assertThat(keyToNumerDataCollections.get(formEntryTwiceUsed.getKey())).isEqualTo(2);
	}
	
	@Test
	public void shouldLoadSoonestDataCollectionDate() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		// soon
		Date soon = new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime();
		QualityDataCollection dcSoon = qualityService.createDataCollection(emptyList(), formEntry);
		dcSoon.setStart(soon);
		dcSoon.setStatus(QualityDataCollectionStatus.FINISHED);
		dcSoon =qualityService.updateDataCollection(dcSoon);
		// sooner
		Date sooner = new GregorianCalendar(2014, Calendar.FEBRUARY, 10).getTime();
		QualityDataCollection dcSooner = qualityService.createDataCollection(emptyList(), formEntry);
		dcSooner.setStart(sooner);
		dcSooner.setStatus(QualityDataCollectionStatus.FINISHED);
		dcSooner =qualityService.updateDataCollection(dcSooner);
		// soonest
		Date soonest = new GregorianCalendar(2014, Calendar.FEBRUARY, 9).getTime();
		QualityDataCollection dcSoonest = qualityService.createDataCollection(emptyList(), formEntry);
		dcSoonest.setStart(soonest);
		dcSoonest.setStatus(QualityDataCollectionStatus.FINISHED);
		dcSoonest =qualityService.updateDataCollection(dcSoonest);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormViewSearchParams searchParams = new EvaluationFormViewSearchParams();
		List<EvaluationFormView> forms = sut.load(searchParams);
		
		assertThat(forms.get(0).getSoonestDataCollectionDate()).isEqualToIgnoringMinutes(soonest);
	}
	
	@Test
	public void shouldLoadLatestDataCollectionDate() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		// latest
		Date latest = new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime();
		QualityDataCollection dcLatest = qualityService.createDataCollection(emptyList(), formEntry);
		dcLatest.setDeadline(latest);
		dcLatest.setStatus(QualityDataCollectionStatus.FINISHED);
		dcLatest =qualityService.updateDataCollection(dcLatest);
		// later
		Date later = new GregorianCalendar(2014, Calendar.FEBRUARY, 10).getTime();
		QualityDataCollection dcLater = qualityService.createDataCollection(emptyList(), formEntry);
		dcLater.setDeadline(later);
		dcLater.setStatus(QualityDataCollectionStatus.FINISHED);
		dcLater =qualityService.updateDataCollection(dcLater);
		// late
		Date late = new GregorianCalendar(2014, Calendar.FEBRUARY, 9).getTime();
		QualityDataCollection dcLate = qualityService.createDataCollection(emptyList(), formEntry);
		dcLate.setDeadline(late);
		dcLate.setStatus(QualityDataCollectionStatus.FINISHED);
		dcLate =qualityService.updateDataCollection(dcLate);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormViewSearchParams searchParams = new EvaluationFormViewSearchParams();
		List<EvaluationFormView> forms = sut.load(searchParams);
		
		assertThat(forms.get(0).getLatestDataCollectionDate()).isEqualToIgnoringMinutes(latest);
	}
	
	@Test
	public void shouldLoadNumberDoneParticipations() {
		RepositoryEntry formEntryOneParticipation = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry formEntryThreeParticipations = JunitTestHelper.createAndPersistRepositoryEntry();
		// Form used in one data collection with one done session
		QualityDataCollection dcOneParticipation = qualityService.createDataCollection(emptyList(), formEntryOneParticipation);
		qualityTestHelper.updateStatus(dcOneParticipation, QualityDataCollectionStatus.FINISHED);
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		List<EvaluationFormParticipation> participationsOne = qualityService.addParticipations(dcOneParticipation, asList(identity1));
		EvaluationFormParticipation participationOne1 = participationsOne.get(0);
		EvaluationFormSession sessionOne1 = evaManager.createSession(participationOne1);
		evaManager.finishSession(sessionOne1);
		// Form used in two data collections with three done and one started session
		QualityDataCollection dcThreeParticipation1 = qualityService.createDataCollection(emptyList(), formEntryThreeParticipations);
		qualityTestHelper.updateStatus(dcThreeParticipation1, QualityDataCollectionStatus.FINISHED);
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Identity identity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		List<EvaluationFormParticipation> participationsThree1 = qualityService.addParticipations(dcThreeParticipation1, asList(identity2, identity3, identity4));
		EvaluationFormParticipation participationThree1 = participationsThree1.get(0);
		EvaluationFormSession sessionThree1 = evaManager.createSession(participationThree1);
		evaManager.finishSession(sessionThree1);
		EvaluationFormParticipation participationThree2 = participationsThree1.get(1);
		EvaluationFormSession sessionThree2 = evaManager.createSession(participationThree2);
		evaManager.finishSession(sessionThree2);
		QualityDataCollection dcThreeParticipation2 = qualityService.createDataCollection(emptyList(), formEntryThreeParticipations);
		qualityTestHelper.updateStatus(dcThreeParticipation2, QualityDataCollectionStatus.FINISHED);
		Identity identity5 = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		List<EvaluationFormParticipation> participationsThree2 = qualityService.addParticipations(dcThreeParticipation2, asList(identity5));
		EvaluationFormParticipation participationThree4 = participationsThree2.get(0);
		EvaluationFormSession sessionThree4 = evaManager.createSession(participationThree4);
		evaManager.finishSession(sessionThree4);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormViewSearchParams searchParams = new EvaluationFormViewSearchParams();
		List<EvaluationFormView> forms = sut.load(searchParams);
		
		Map<Long, Long> keyToNumerParticipations = forms.stream()
				.collect(toMap(EvaluationFormView::getFormEntryKey, EvaluationFormView::getNumberParticipationsDone));
		assertThat(keyToNumerParticipations.get(formEntryOneParticipation.getKey())).isEqualTo(1);
		assertThat(keyToNumerParticipations.get(formEntryThreeParticipations.getKey())).isEqualTo(3);
	}
	
	@Test
	public void shouldFilterByOrganisations() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherFormEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation organisation = organisationService.createOrganisation("", "", null, null, null);
		List<Organisation> organisations = Collections.singletonList(organisation);
		Organisation otherOrganisation = organisationService.createOrganisation("", "", null, null, null);
		List<Organisation> otherOrganisations = Collections.singletonList(otherOrganisation);
		QualityDataCollection dataCollection1 = qualityService.createDataCollection(organisations, formEntry);
		qualityTestHelper.updateStatus(dataCollection1, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dataCollection2 = qualityService.createDataCollection(organisations, formEntry);
		qualityTestHelper.updateStatus(dataCollection2, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dataCollectionOtherOrganisation = qualityService.createDataCollection(otherOrganisations, formEntry);
		qualityTestHelper.updateStatus(dataCollectionOtherOrganisation, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dataCollectionOtherForm = qualityService.createDataCollection(otherOrganisations, otherFormEntry);
		qualityTestHelper.updateStatus(dataCollectionOtherForm, QualityDataCollectionStatus.FINISHED);
		qualityService.createDataCollection(organisations, formEntry);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormViewSearchParams searchParams = new EvaluationFormViewSearchParams();
		searchParams.setOrganisationRefs(organisations);
		List<EvaluationFormView> forms = sut.load(searchParams);
		
		Long expectedNumberDataCollections = (long) asList(dataCollection1, dataCollection2).size();
		assertThat(forms)
				.hasSize(1)
				.extracting(EvaluationFormView::getNumberDataCollections)
				.containsExactly(expectedNumberDataCollections);
	}

}
