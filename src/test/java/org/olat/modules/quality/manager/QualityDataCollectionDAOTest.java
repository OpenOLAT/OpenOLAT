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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;
import static org.olat.modules.quality.QualityDataCollectionStatus.RUNNING;
import static org.olat.modules.quality.QualityReportAccessReference.of;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionSearchParams;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.ui.DataCollectionDataModel.DataCollectionCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityDataCollectionDAOTest extends OlatTestCase {
	
	private static final TranslatorMock TRANSLATOR = new TranslatorMock();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	@Autowired
	private QualityDataCollectionDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateNewDataCollection() {
		QualityDataCollection dataCollection = sut.createDataCollection();
		dbInstance.commitAndCloseSession();

		assertThat(dataCollection).isNotNull();
		assertThat(dataCollection.getCreationDate()).isNotNull();
		assertThat(dataCollection.getLastModified()).isNotNull();
		assertThat(dataCollection.getStatus()).isEqualTo(QualityDataCollectionStatus.PREPARATION);
		assertThat(dataCollection.getGenerator()).isNull();
		assertThat(dataCollection.getGeneratorProviderKey()).isNull();
	}
	
	@Test
	public void shouldCreateNewDataCollectionFromGenerator() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		Long providerKey = 123l;
		QualityDataCollection dataCollection = sut.createDataCollection(generator, 123l);
		dbInstance.commitAndCloseSession();

		assertThat(dataCollection).isNotNull();
		assertThat(dataCollection.getCreationDate()).isNotNull();
		assertThat(dataCollection.getLastModified()).isNotNull();
		assertThat(dataCollection.getStatus()).isEqualTo(QualityDataCollectionStatus.PREPARATION);
		assertThat(dataCollection.getGenerator()).isEqualTo(generator);
		assertThat(dataCollection.getGeneratorProviderKey()).isEqualTo(providerKey);
	}
	
	@Test
	public void shouldUpdateDataCollectionStatus() {
		QualityDataCollectionStatus status = QualityDataCollectionStatus.FINISHED;
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection updatedDataCollection = sut.updateDataCollectionStatus(dataCollection, status);
		dbInstance.commitAndCloseSession();

		updatedDataCollection = sut.loadDataCollectionByKey(updatedDataCollection);
		assertThat(updatedDataCollection.getStatus()).isEqualByComparingTo(status);
	}

	@Test
	public void shouldUpdateDataCollection() {
		String title = "changed title";
		Date start = new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime();
		Date end = new GregorianCalendar(2014, Calendar.FEBRUARY, 20).getTime();
		QualityDataCollectionTopicType topicType = QualityDataCollectionTopicType.CURRICULUM;
		String topicCustom = "custom topic";
		Organisation organisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		RepositoryEntry entry = qualityTestHelper.createRepositoryEntry();
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		dataCollection.setTitle(title);
		dataCollection.setStart(start);
		dataCollection.setDeadline(end);
		dataCollection.setTopicType(topicType);
		dataCollection.setTopicCustom(topicCustom);
		dataCollection.setTopicOrganisation(organisation);
		dataCollection.setTopicCurriculum(curriculum);
		dataCollection.setTopicCurriculumElement(curriculumElement);
		dataCollection.setTopicRepositoryEntry(entry);
		QualityDataCollection updatedDataCollection = sut.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();

		updatedDataCollection = sut.loadDataCollectionByKey(updatedDataCollection);
		assertThat(updatedDataCollection.getTitle()).isEqualTo(title);
		assertThat(updatedDataCollection.getStart()).isEqualToIgnoringSeconds(start);
		assertThat(updatedDataCollection.getDeadline()).isEqualToIgnoringSeconds(end);
		assertThat(updatedDataCollection.getTopicType()).isEqualTo(topicType);
		assertThat(updatedDataCollection.getTopicCustom()).isEqualTo(topicCustom);
		assertThat(updatedDataCollection.getTopicOrganisation()).isEqualTo(organisation);
		assertThat(updatedDataCollection.getTopicCurriculum()).isEqualTo(curriculum);
		assertThat(updatedDataCollection.getTopicCurriculumElement()).isEqualTo(curriculumElement);
		assertThat(updatedDataCollection.getTopicRepositoryEntry()).isEqualTo(entry);
	}

	@Test
	public void shouldDeleteDataCollection() {
		QualityDataCollectionRef dataCollection = sut.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		sut.deleteDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		List<QualityDataCollectionView> collections = sut.loadDataCollections(TRANSLATOR, searchParams , 0, -1);
		
		assertThat(collections).isEmpty();
	}
	
	@Test
	public void shouldReturnAllDataCollections() {
		QualityDataCollection dataCollection1 = sut.createDataCollection();
		QualityDataCollection dataCollection2 = sut.createDataCollection();
		QualityDataCollection dataCollection3 = sut.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		List<QualityDataCollection> dataCollections = sut.loadAllDataCollections();
		
		assertThat(dataCollections).containsExactlyInAnyOrder(dataCollection1, dataCollection2, dataCollection3);
	}
	
	@Test
	public void shouldLoadDataCollectionByKey() {
		QualityDataCollection dataCollection = sut.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection loadDataCollection = sut.loadDataCollectionByKey(dataCollection);
		
		assertThat(loadDataCollection).isEqualTo(dataCollection);
	}
	
	@Test
	public void shouldLoadPreviousDataCollection() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection first = qualityService.createDataCollection(emptyList(), formEntry);
		QualityDataCollection second = qualityService.createDataCollection(emptyList(), first, null, null);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection previous = sut.loadPrevious(second);
		
		assertThat(previous).isEqualTo(first);
	}

	@Test
	public void shouldLoadFollowUpDataCollection() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection first = qualityService.createDataCollection(emptyList(), formEntry);
		QualityDataCollection second = qualityService.createDataCollection(emptyList(), first, null, null);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection followUp = sut.loadFollowUp(first);
		
		assertThat(followUp).isEqualTo(second);
	}
	
	@Test
	public void shouldLoadDataCollectionsWithPendingStart() {
		Date until = new Date();
		QualityDataCollection future1 = qualityTestHelper.createDataCollectionWithStartInFuture();
		QualityDataCollection future2 = qualityTestHelper.createDataCollectionWithStartInFuture();
		QualityDataCollection pastReady1 = qualityTestHelper.createDataCollectionWithStartInPast();
		pastReady1 = qualityTestHelper.updateStatus(pastReady1, READY);
		QualityDataCollection pastReady2 = qualityTestHelper.createDataCollectionWithStartInPast();
		pastReady2 = qualityTestHelper.updateStatus(pastReady2, READY);
		QualityDataCollection pastPreparation = qualityTestHelper.createDataCollectionWithStartInPast();
		pastPreparation = qualityTestHelper.updateStatus(pastPreparation, PREPARATION);
		QualityDataCollection pastRunning = qualityTestHelper.createDataCollectionWithStartInPast();
		pastRunning = qualityTestHelper.updateStatus(pastRunning, RUNNING);
		QualityDataCollection pastFinished = qualityTestHelper.createDataCollectionWithStartInPast();
		pastFinished = qualityTestHelper.updateStatus(pastFinished, FINISHED);
		QualityDataCollection noStart = qualityTestHelper.createDataCollectionWithoutValues();
		dbInstance.commitAndCloseSession();
		
		Collection<QualityDataCollection> dataCollections = sut.loadWithPendingStart(until);
		
		assertThat(dataCollections)
				.containsExactlyInAnyOrder(pastReady1, pastReady2)
				.doesNotContain(future1, future2, noStart, pastPreparation, pastRunning, pastFinished);
	}
	
	@Test
	public void shouldLoadDataCollectionsWithPendingDeadline() {
		Date until = new Date();
		QualityDataCollection future1 = qualityTestHelper.createDataCollectionWithDeadlineInFuture();
		QualityDataCollection future2 = qualityTestHelper.createDataCollectionWithDeadlineInFuture();
		QualityDataCollection pastRunning1 = qualityTestHelper.createDataCollectionWithDeadlineInPast();
		pastRunning1 = qualityTestHelper.updateStatus(pastRunning1, RUNNING);
		QualityDataCollection pastRunning2 = qualityTestHelper.createDataCollectionWithDeadlineInPast();
		pastRunning2 = qualityTestHelper.updateStatus(pastRunning2, QualityDataCollectionStatus.RUNNING);
		QualityDataCollection pastPreparation = qualityTestHelper.createDataCollectionWithDeadlineInPast();
		pastPreparation = qualityTestHelper.updateStatus(pastPreparation, QualityDataCollectionStatus.PREPARATION);
		QualityDataCollection pastReady = qualityTestHelper.createDataCollectionWithDeadlineInPast();
		pastReady = qualityTestHelper.updateStatus(pastReady, READY);
		QualityDataCollection pastFinished = qualityTestHelper.createDataCollectionWithDeadlineInPast();
		pastFinished = qualityTestHelper.updateStatus(pastFinished, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection noDeadline = qualityTestHelper.createDataCollectionWithoutValues();
		dbInstance.commitAndCloseSession();
		
		Collection<QualityDataCollection> dataCollections = sut.loadWithPendingDeadline(until);
		
		assertThat(dataCollections)
				.containsExactlyInAnyOrder(pastRunning1, pastRunning2, pastReady)
				.doesNotContain(future1, future2, noDeadline, pastFinished, pastPreparation);
	}
	
	@Test
	public void shouldCheckIfHasARelationToOrganisation() {
		Organisation organisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		dataCollection.setTopicOrganisation(organisation);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		boolean hasDataCollection = sut.hasDataCollection(organisation);
		
		assertThat(hasDataCollection).isTrue();
	}
	
	@Test
	public void shouldCheckIfHasNoRelationToOrganisation() {
		Organisation organisation = qualityTestHelper.createOrganisation();
		qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		boolean hasDataCollection = sut.hasDataCollection(organisation);
		
		assertThat(hasDataCollection).isFalse();
	}
	
	@Test
	public void shouldCheckIfHasARelationToCurriculum() {
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		dataCollection.setTopicCurriculum(curriculum);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		boolean hasDataCollection = sut.hasDataCollection(curriculum);
		
		assertThat(hasDataCollection).isTrue();
	}
	
	@Test
	public void shouldCheckIfHasNoRelationToCurriculum() {
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		boolean hasDataCollection = sut.hasDataCollection(curriculum);
		
		assertThat(hasDataCollection).isFalse();
	}
	
	@Test
	public void shouldCheckIfHasARelationToCurriculumElement() {
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		dataCollection.setTopicCurriculumElement(curriculumElement);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		boolean hasDataCollection = sut.hasDataCollection(curriculumElement);
		
		assertThat(hasDataCollection).isTrue();
	}
	
	@Test
	public void shouldCheckIfHasNoRelationToCurriculumElement() {
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		boolean hasDataCollection = sut.hasDataCollection(curriculumElement);
		
		assertThat(hasDataCollection).isFalse();
	}
	
	@Test
	public void shouldFilterDataCollectionsByTopicIdentity() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection();
		dataCollection1.setTopicIdentity(identity);
		dataCollection1 = sut.updateDataCollection(dataCollection1);
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection();
		dataCollection2.setTopicIdentity(identity);
		dataCollection2 = sut.updateDataCollection(dataCollection2);
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		QualityDataCollection dataCollectionOtherIdentity = qualityTestHelper.createDataCollection();
		dataCollectionOtherIdentity.setTopicIdentity(otherIdentity);
		dataCollectionOtherIdentity = sut.updateDataCollection(dataCollectionOtherIdentity);
		QualityDataCollection dataCollectionNoIdentity = qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionSearchParams searchParams = new QualityDataCollectionSearchParams();
		searchParams.setTopicIdentityRef(identity);
		List<QualityDataCollection> collections = sut.loadDataCollections(searchParams);
		
		assertThat(collections)
				.containsExactlyInAnyOrder(dataCollection1, dataCollection2)
				.doesNotContain(dataCollectionOtherIdentity, dataCollectionNoIdentity);
	}
	
	@Test
	public void shouldFilterByTopicRepository() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection();
		dataCollection1.setTopicRepositoryEntry(entry);
		dataCollection1 = sut.updateDataCollection(dataCollection1);
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection();
		dataCollection2.setTopicRepositoryEntry(entry);
		dataCollection2 = sut.updateDataCollection(dataCollection2);
		RepositoryEntry otherEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dataCollectionOtherEntry = qualityTestHelper.createDataCollection();
		dataCollectionOtherEntry.setTopicRepositoryEntry(otherEntry);
		dataCollectionOtherEntry = sut.updateDataCollection(dataCollectionOtherEntry);
		QualityDataCollection dataCollectionNoEntry = qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionSearchParams searchParams = new QualityDataCollectionSearchParams();
		searchParams.setTopicRepositoryRef(entry);
		List<QualityDataCollection> collections = sut.loadDataCollections(searchParams);
		
		assertThat(collections)
				.containsExactlyInAnyOrder(dataCollection1, dataCollection2)
				.doesNotContain(dataCollectionOtherEntry, dataCollectionNoEntry);
	}

	@Test
	public void shouldFilterByGenerator() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityDataCollection dataCollection1 = sut.createDataCollection(generator, null);
		QualityDataCollection dataCollection2 = sut.createDataCollection(generator, null);
		QualityGenerator otherGenerator = qualityTestHelper.createGenerator();
		QualityDataCollection dataCollectionOtherGenerator = sut.createDataCollection(otherGenerator, null);
		QualityDataCollection dataCollectionNoGenerator = sut.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionSearchParams searchParams = new QualityDataCollectionSearchParams();
		searchParams.setGeneratorRef(generator);
		List<QualityDataCollection> collections = sut.loadDataCollections(searchParams);
		
		assertThat(collections)
				.containsExactlyInAnyOrder(dataCollection1, dataCollection2)
				.doesNotContain(dataCollectionOtherGenerator, dataCollectionNoGenerator);
	}

	@Test
	public void shouldFilterByGeneratorReferenceKey() {
		Long key = 123L;
		QualityDataCollection dataCollection1 = sut.createDataCollection(null, key);
		QualityDataCollection dataCollection2 = sut.createDataCollection(null, key);
		Long otherKey = 999L;
		QualityDataCollection dataCollectionOtherKey = sut.createDataCollection(null, otherKey);
		QualityDataCollection dataCollectionNoKey = sut.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionSearchParams searchParams = new QualityDataCollectionSearchParams();
		searchParams.setGeneratorProviderKey(key);
		List<QualityDataCollection> collections = sut.loadDataCollections(searchParams);
		
		assertThat(collections)
				.containsExactlyInAnyOrder(dataCollection1, dataCollection2)
				.doesNotContain(dataCollectionOtherKey, dataCollectionNoKey);
	}

	@Test
	public void shouldGetDataCollectionCount() {
		int numberOfDataCollections = 3;
		for (int i = 0; i < numberOfDataCollections; i++) {
			qualityTestHelper.createDataCollection();
		}
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		int count = sut.getDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(numberOfDataCollections);
	}
	
	@Test
	public void shouldLoadDataCollections() {
		qualityTestHelper.createDataCollection();
		qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections).hasSize(2);
	}
	
	@Test
	public void shouldLoadDataCollectionsPaged() {
		qualityTestHelper.createDataCollection();
		qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 1, 1);
		
		assertThat(dataCollections).hasSize(1);
	}
	
	@Test
	public void shouldLoadDataCollectionsOrdered() {
		QualityDataCollection dataCollectionZ = qualityTestHelper.createDataCollection();
		dataCollectionZ.setTitle("Z");
		QualityDataCollection dataCollectionA = qualityTestHelper.createDataCollection();
		dataCollectionA.setTitle("A");
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		SortKey sortKey = new SortKey(DataCollectionCols.title.name(), true);
		List<QualityDataCollectionView> dataCollectionViews = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1, sortKey);
		
		assertThat(dataCollectionViews.get(0).getTitle()).isEqualTo("A");
		assertThat(dataCollectionViews.get(1).getTitle()).isEqualTo("Z");
	}
	
	@Test
	public void shouldLoadDataCollectionOrderedByAllColumns() {
		qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		for (DataCollectionCols col: DataCollectionCols.values()) {
			SortKey sortKey = new SortKey(col.name(), true);
			sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1, sortKey);
		}
		
		// Only check that no Exception is thrown to be sure that hql syntax is ok.
	}
	
	@Test
	public void shouldFilterDataCollectionsByKey() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setDataCollectionRef(dataCollection);
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections).hasSize(1);
		QualityDataCollectionView dataCollectionView = dataCollections.get(0);
		assertThat(dataCollectionView.getKey()).isEqualTo(dataCollection.getKey());
	}
	
	@Test
	public void shouldFilterDataCollectionsByOrganisations() {
		Organisation organisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection(organisation);
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection(organisation);
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();

		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setOrgansationRefs(Collections.singletonList(organisation));
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections)
				.extracting(QualityDataCollectionView::getKey)
				.containsExactlyInAnyOrder(dataCollection1.getKey(), dataCollection2.getKey())
				.doesNotContain(otherDataCollection.getKey());
	}
	
	@Test
	public void shouldFilterDataCollectionsByAllOrganisations() {
		Organisation organisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection(organisation);
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection(organisation);
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();

		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections).extracting(QualityDataCollectionView::getKey)
				.containsExactlyInAnyOrder(dataCollection1.getKey(), dataCollection2.getKey(), otherDataCollection.getKey());
	}
	
	@Test
	public void shouldFilterDataCollectionsByNoOrganisations() {
		Organisation organisation = qualityTestHelper.createOrganisation();
		qualityTestHelper.createDataCollection(organisation);
		qualityTestHelper.createDataCollection(organisation);
		qualityTestHelper.createDataCollection();
		dbInstance.commitAndCloseSession();

		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setOrgansationRefs(Collections.emptyList());
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections).isEmpty();
	}
	
	@Test
	public void shouldFilterDataCollectionsByReportAccessGroupRoleOfCourses() {
		Identity reportViewer = JunitTestHelper.createAndPersistIdentityAsRndUser("report-viewer");
		GroupRoles reportViewerRole = GroupRoles.owner;
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("executer");
		GroupRoles executorRole = GroupRoles.participant;
		// Everything fulfilled: Data collection has participant of the course
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(reportViewer, entry, reportViewerRole.name());
		repositoryService.addRole(executor, entry, executorRole.name());
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		dc = qualityService.updateDataCollectionStatus(dc, FINISHED);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0), entry, executorRole).build();
		QualityReportAccess ra = qualityService.createReportAccess(of(dc), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		ra.setOnline(true);
		qualityService.updateReportAccess(ra);
		// Report viewer has other course role
		RepositoryEntry entryOtherRole = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(reportViewer, entryOtherRole, GroupRoles.participant.name());
		repositoryService.addRole(executor, entryOtherRole, executorRole.name());
		QualityDataCollection dcOtherRole = qualityTestHelper.createDataCollection();
		dcOtherRole = qualityService.updateDataCollectionStatus(dcOtherRole, FINISHED);
		List<EvaluationFormParticipation> participationsOtherRole = qualityService.addParticipations(dcOtherRole, singletonList(executor));
		qualityService.createContextBuilder(dcOtherRole, participationsOtherRole.get(0), entryOtherRole, executorRole).build();
		QualityReportAccess raOtherRole = qualityService.createReportAccess(of(dcOtherRole), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		raOtherRole.setOnline(true);
		qualityService.updateReportAccess(raOtherRole);
		// Report viewer is not member in the course
		RepositoryEntry entryNotMember = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(executor, entryNotMember, executorRole.name());
		QualityDataCollection dcNotMember = qualityTestHelper.createDataCollection();
		dcNotMember = qualityService.updateDataCollectionStatus(dcNotMember, FINISHED);
		List<EvaluationFormParticipation> participationsNotMember = qualityService.addParticipations(dcNotMember, singletonList(executor));
		qualityService.createContextBuilder(dcNotMember, participationsNotMember.get(0), entryNotMember, executorRole).build();
		QualityReportAccess raNotMember = qualityService.createReportAccess(of(dcNotMember), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		raNotMember.setOnline(true);
		qualityService.updateReportAccess(raNotMember);
		// Data collection is not finished
		RepositoryEntry entryNotFinished = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(reportViewer, entryNotFinished, reportViewerRole.name());
		repositoryService.addRole(executor, entryNotFinished, executorRole.name());
		QualityDataCollection dcNotFinished = qualityTestHelper.createDataCollection();
		dcNotFinished = qualityService.updateDataCollectionStatus(dcNotFinished, QualityDataCollectionStatus.RUNNING);
		List<EvaluationFormParticipation> participationsNotFinished = qualityService.addParticipations(dcNotFinished, singletonList(executor));
		qualityService.createContextBuilder(dcNotFinished, participationsNotFinished.get(0), entryNotFinished, executorRole).build();
		QualityReportAccess raNotFinished = qualityService.createReportAccess(of(dcNotFinished), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		raNotFinished.setOnline(true);
		qualityService.updateReportAccess(raNotFinished);
		// Data collection has no report access configured
		RepositoryEntry entryNoAccess = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(reportViewer, entryNoAccess, reportViewerRole.name());
		repositoryService.addRole(executor, entryNoAccess, executorRole.name());
		QualityDataCollection dcNoAccess = qualityTestHelper.createDataCollection();
		dcNoAccess = qualityService.updateDataCollectionStatus(dcNoAccess, FINISHED);
		List<EvaluationFormParticipation> participationsNoAccess = qualityService.addParticipations(dcNoAccess, singletonList(executor));
		qualityService.createContextBuilder(dcNoAccess, participationsNoAccess.get(0), entryNoAccess, executorRole).build();
		// Report user has access denied
		RepositoryEntry entryAccessDenied = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(reportViewer, entryAccessDenied, reportViewerRole.name());
		repositoryService.addRole(executor, entryAccessDenied, executorRole.name());
		QualityDataCollection dcAccessDenied = qualityTestHelper.createDataCollection();
		dcAccessDenied = qualityService.updateDataCollectionStatus(dcAccessDenied, FINISHED);
		List<EvaluationFormParticipation> participationsAccessDenied = qualityService.addParticipations(dcAccessDenied, singletonList(executor));
		qualityService.createContextBuilder(dcAccessDenied, participationsAccessDenied.get(0), entryAccessDenied, executorRole).build();
		QualityReportAccess raAccessDenied = qualityService.createReportAccess(of(dcAccessDenied), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		raAccessDenied.setOnline(false);
		qualityService.updateReportAccess(raAccessDenied);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		Organisation organisation = qualityTestHelper.createOrganisation();
		searchParams.setOrgansationRefs(Collections.singletonList(organisation));
		searchParams.setReportAccessIdentity(reportViewer);
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections)
				.extracting(QualityDataCollectionView::getKey)
				.containsExactlyInAnyOrder(dc.getKey())
				.doesNotContain(
						dcOtherRole.getKey(),
						dcNotMember.getKey(),
						dcNotFinished.getKey(),
						dcNoAccess.getKey(),
						dcAccessDenied.getKey()
						);
	}
	
	@Test
	public void shouldFilterDataCollectionsByReportAccessGroupRoleOfCurriculumElements() {
		Identity reportViewer = JunitTestHelper.createAndPersistIdentityAsRndUser("report-viewer");
		CurriculumRoles reportViewerRole = CurriculumRoles.owner;
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("executer");
		CurriculumRoles executorRole = CurriculumRoles.participant;
		// Everything fulfilled: Data collection has participant of the curriculum element
		CurriculumElement element = qualityTestHelper.createCurriculumElement();
		curriculumService.addMember(element, reportViewer, reportViewerRole);
		curriculumService.addMember(element, executor, executorRole);
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		dc = qualityService.updateDataCollectionStatus(dc, FINISHED);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0), element, executorRole).build();
		QualityReportAccess ra = qualityService.createReportAccess(of(dc), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		ra.setOnline(true);
		qualityService.updateReportAccess(ra);
		// Report viewer has other curriculum element role
		CurriculumElement elementOtherRole = qualityTestHelper.createCurriculumElement();
		curriculumService.addMember(elementOtherRole, reportViewer, CurriculumRoles.participant);
		curriculumService.addMember(elementOtherRole, executor, executorRole);
		QualityDataCollection dcOtherRole = qualityTestHelper.createDataCollection();
		dcOtherRole = qualityService.updateDataCollectionStatus(dcOtherRole, FINISHED);
		List<EvaluationFormParticipation> participationsOtherRole = qualityService.addParticipations(dcOtherRole, singletonList(executor));
		qualityService.createContextBuilder(dcOtherRole, participationsOtherRole.get(0), elementOtherRole, executorRole).build();
		QualityReportAccess raOtherRole = qualityService.createReportAccess(of(dcOtherRole), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		raOtherRole.setOnline(true);
		qualityService.updateReportAccess(raOtherRole);
		// Report viewer is not member in the curriculum element
		CurriculumElement elementNotMember = qualityTestHelper.createCurriculumElement();
		curriculumService.addMember(elementOtherRole, executor, executorRole);
		QualityDataCollection dcNotMember = qualityTestHelper.createDataCollection();
		dcNotMember = qualityService.updateDataCollectionStatus(dcNotMember, FINISHED);
		List<EvaluationFormParticipation> participationsNotMember = qualityService.addParticipations(dcNotMember, singletonList(executor));
		qualityService.createContextBuilder(dcNotMember, participationsNotMember.get(0), elementNotMember, executorRole).build();
		QualityReportAccess raNotMember = qualityService.createReportAccess(of(dcNotMember), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		raNotMember.setOnline(true);
		qualityService.updateReportAccess(raNotMember);
		// Data collection is not finished
		CurriculumElement elementNotFinished = qualityTestHelper.createCurriculumElement();
		curriculumService.addMember(elementNotFinished, reportViewer, reportViewerRole);
		curriculumService.addMember(elementNotFinished, executor, executorRole);
		QualityDataCollection dcNotFinished = qualityTestHelper.createDataCollection();
		dcNotFinished = qualityService.updateDataCollectionStatus(dcNotFinished, QualityDataCollectionStatus.RUNNING);
		List<EvaluationFormParticipation> participationsNotFinished = qualityService.addParticipations(dcNotFinished, singletonList(executor));
		qualityService.createContextBuilder(dcNotFinished, participationsNotFinished.get(0), elementNotFinished, executorRole).build();
		QualityReportAccess raNotFinished = qualityService.createReportAccess(of(dcNotFinished), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		raNotFinished.setOnline(true);
		qualityService.updateReportAccess(raNotFinished);
		// Data collection has no report access configured
		CurriculumElement elementNoAccess = qualityTestHelper.createCurriculumElement();
		curriculumService.addMember(elementNoAccess, reportViewer, reportViewerRole);
		curriculumService.addMember(elementNoAccess, executor, executorRole);
		QualityDataCollection dcNoAccess = qualityTestHelper.createDataCollection();
		dcNoAccess = qualityService.updateDataCollectionStatus(dcNoAccess, FINISHED);
		List<EvaluationFormParticipation> participationsNoAccess = qualityService.addParticipations(dcNoAccess, singletonList(executor));
		qualityService.createContextBuilder(dcNoAccess, participationsNoAccess.get(0), elementNoAccess, executorRole).build();
		// Report user has access denied
		CurriculumElement elementAccessDenied = qualityTestHelper.createCurriculumElement();
		curriculumService.addMember(elementAccessDenied, reportViewer, reportViewerRole);
		curriculumService.addMember(elementAccessDenied, executor, executorRole);
		QualityDataCollection dcAccessDenied = qualityTestHelper.createDataCollection();
		dcAccessDenied = qualityService.updateDataCollectionStatus(dcAccessDenied, FINISHED);
		List<EvaluationFormParticipation> participationsAccessDenied = qualityService.addParticipations(dcAccessDenied, singletonList(executor));
		qualityService.createContextBuilder(dcAccessDenied, participationsAccessDenied.get(0), elementAccessDenied, executorRole).build();
		QualityReportAccess raAccessDenied = qualityService.createReportAccess(of(dcAccessDenied), QualityReportAccess.Type.GroupRoles, reportViewerRole.name());
		raAccessDenied.setOnline(false);
		qualityService.updateReportAccess(raAccessDenied);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		Organisation organisation = qualityTestHelper.createOrganisation();
		searchParams.setOrgansationRefs(Collections.singletonList(organisation));
		searchParams.setReportAccessIdentity(reportViewer);
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections)
				.extracting(QualityDataCollectionView::getKey)
				.containsExactlyInAnyOrder(dc.getKey())
				.doesNotContain(
						dcOtherRole.getKey(),
						dcNotMember.getKey(),
						dcNotFinished.getKey(),
						dcNoAccess.getKey(),
						dcAccessDenied.getKey()
						);
	}
	
	@Test
	public void shouldFilterDataCollectionsByReportDoneParticipants() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("p1");
		// participated and session finished
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		dc = qualityService.updateDataCollectionStatus(dc, QualityDataCollectionStatus.FINISHED);
		EvaluationFormParticipation participationFinished = qualityService.addParticipations(dc, singletonList(executor)).get(0);
		EvaluationFormSession session = evaluationFormManager.createSession(participationFinished);
		evaluationFormManager.finishSession(session);
		QualityReportAccess ra = qualityService.createReportAccess(of(dc), QualityReportAccess.Type.Participants, EvaluationFormParticipationStatus.done.name());
		ra.setOnline(true);
		qualityService.updateReportAccess(ra);
		// participation but session not finished
		QualityDataCollection dcSessionNotFinished = qualityTestHelper.createDataCollection();
		dcSessionNotFinished = qualityService.updateDataCollectionStatus(dcSessionNotFinished, QualityDataCollectionStatus.FINISHED);
		EvaluationFormParticipation participationNotFinished = qualityService.addParticipations(dc, singletonList(executor)).get(0);
		evaluationFormManager.createSession(participationNotFinished);
		QualityReportAccess raSessionNotFinished = qualityService.createReportAccess(of(dcSessionNotFinished), QualityReportAccess.Type.Participants, EvaluationFormParticipationStatus.done.name());
		raSessionNotFinished.setOnline(true);
		qualityService.updateReportAccess(raSessionNotFinished);
		// participated and data collection finished
		QualityDataCollection dcNotFinished = qualityTestHelper.createDataCollection();
		dcNotFinished = qualityService.updateDataCollectionStatus(dcNotFinished, QualityDataCollectionStatus.RUNNING);
		EvaluationFormParticipation participationDcNotFinished = qualityService.addParticipations(dcNotFinished, singletonList(executor)).get(0);
		EvaluationFormSession sessionSessionDcNotFinished = evaluationFormManager.createSession(participationDcNotFinished);
		evaluationFormManager.finishSession(sessionSessionDcNotFinished);
		QualityReportAccess raSessionDcNotFinished = qualityService.createReportAccess(of(dcNotFinished), QualityReportAccess.Type.Participants, EvaluationFormParticipationStatus.done.name());
		raSessionDcNotFinished.setOnline(true);
		qualityService.updateReportAccess(raSessionDcNotFinished);
		// No access configured
		QualityDataCollection dcNoAccess = qualityTestHelper.createDataCollection();
		dcNoAccess = qualityService.updateDataCollectionStatus(dcNoAccess, QualityDataCollectionStatus.FINISHED);
		EvaluationFormParticipation participationBoAccess = qualityService.addParticipations(dcNoAccess, singletonList(executor)).get(0);
		EvaluationFormSession sessionNoAccess = evaluationFormManager.createSession(participationBoAccess);
		evaluationFormManager.finishSession(sessionNoAccess);
		// Executor has access denied
		QualityDataCollection dcAccessDenied = qualityTestHelper.createDataCollection();
		dcAccessDenied = qualityService.updateDataCollectionStatus(dcAccessDenied, QualityDataCollectionStatus.FINISHED);
		EvaluationFormParticipation participationAccessDenied = qualityService.addParticipations(dcAccessDenied, singletonList(executor)).get(0);
		EvaluationFormSession sessionAccessDenied = evaluationFormManager.createSession(participationAccessDenied);
		evaluationFormManager.finishSession(sessionAccessDenied);
		QualityReportAccess raAccessDenied = qualityService.createReportAccess(of(dcAccessDenied), QualityReportAccess.Type.Participants, EvaluationFormParticipationStatus.done.name());
		raAccessDenied.setOnline(false);
		qualityService.updateReportAccess(raAccessDenied);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setOrgansationRefs(Collections.emptyList());
		searchParams.setReportAccessIdentity(executor);
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections)
				.extracting(QualityDataCollectionView::getKey)
				.containsExactlyInAnyOrder(dc.getKey())
				.doesNotContain(
						dcSessionNotFinished.getKey(),
						dcNotFinished.getKey(),
						dcNoAccess.getKey(),
						dcAccessDenied.getKey()
						);
	}
	
	@Test
	public void shouldFilterDataCollectionsByReportAllParticipants() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("p1");
		// participated and session finished
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		dc = qualityService.updateDataCollectionStatus(dc, QualityDataCollectionStatus.FINISHED);
		EvaluationFormParticipation participationFinished = qualityService.addParticipations(dc, singletonList(executor)).get(0);
		EvaluationFormSession session = evaluationFormManager.createSession(participationFinished);
		evaluationFormManager.finishSession(session);
		QualityReportAccess ra = qualityService.createReportAccess(of(dc), QualityReportAccess.Type.Participants, null);
		ra.setOnline(true);
		qualityService.updateReportAccess(ra);
		// participation but session not finished
		QualityDataCollection dcSessionNotFinished = qualityTestHelper.createDataCollection();
		dcSessionNotFinished = qualityService.updateDataCollectionStatus(dcSessionNotFinished, QualityDataCollectionStatus.FINISHED);
		qualityService.addParticipations(dcSessionNotFinished, singletonList(executor));
		QualityReportAccess raSessionNotFinished = qualityService.createReportAccess(of(dcSessionNotFinished), QualityReportAccess.Type.Participants, null);
		raSessionNotFinished.setOnline(true);
		qualityService.updateReportAccess(raSessionNotFinished);
		// participated and data collection finished
		QualityDataCollection dcNotFinished = qualityTestHelper.createDataCollection();
		dcNotFinished = qualityService.updateDataCollectionStatus(dcNotFinished, QualityDataCollectionStatus.RUNNING);
		EvaluationFormParticipation participationDcNotFinished = qualityService.addParticipations(dcNotFinished, singletonList(executor)).get(0);
		EvaluationFormSession sessionSessionDcNotFinished = evaluationFormManager.createSession(participationDcNotFinished);
		evaluationFormManager.finishSession(sessionSessionDcNotFinished);
		QualityReportAccess raSessionDcNotFinished = qualityService.createReportAccess(of(dcNotFinished), QualityReportAccess.Type.Participants, null);
		raSessionDcNotFinished.setOnline(true);
		qualityService.updateReportAccess(raSessionDcNotFinished);
		// No access configured
		QualityDataCollection dcNoAccess = qualityTestHelper.createDataCollection();
		dcNoAccess = qualityService.updateDataCollectionStatus(dcNoAccess, QualityDataCollectionStatus.FINISHED);
		EvaluationFormParticipation participationBoAccess = qualityService.addParticipations(dcNoAccess, singletonList(executor)).get(0);
		EvaluationFormSession sessionNoAccess = evaluationFormManager.createSession(participationBoAccess);
		evaluationFormManager.finishSession(sessionNoAccess);
		// Executor has access denied
		QualityDataCollection dcAccessDenied = qualityTestHelper.createDataCollection();
		dcAccessDenied = qualityService.updateDataCollectionStatus(dcAccessDenied, QualityDataCollectionStatus.FINISHED);
		EvaluationFormParticipation participationAccessDenied = qualityService.addParticipations(dcAccessDenied, singletonList(executor)).get(0);
		EvaluationFormSession sessionAccessDenied = evaluationFormManager.createSession(participationAccessDenied);
		evaluationFormManager.finishSession(sessionAccessDenied);
		QualityReportAccess raAccessDenied = qualityService.createReportAccess(of(dcAccessDenied), QualityReportAccess.Type.Participants, null);
		raAccessDenied.setOnline(false);
		qualityService.updateReportAccess(raAccessDenied);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setOrgansationRefs(Collections.emptyList());
		searchParams.setReportAccessIdentity(executor);
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections)
				.extracting(QualityDataCollectionView::getKey)
				.containsExactlyInAnyOrder(
						dc.getKey(),
						dcSessionNotFinished.getKey())
				.doesNotContain(
						dcNotFinished.getKey(),
						dcNoAccess.getKey(),
						dcAccessDenied.getKey()
						);
	}
	
	@Test
	public void shouldFilterDataCollectionsByReportTopicIdentity() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("p1");
		// Everything fulfilled
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		dc.setTopicIdentity(coach);
		dc = qualityService.updateDataCollection(dc);
		dc = qualityService.updateDataCollectionStatus(dc, QualityDataCollectionStatus.FINISHED);
		QualityReportAccess ra = qualityService.createReportAccess(of(dc), QualityReportAccess.Type.TopicIdentity, null);
		ra.setOnline(true);
		qualityService.updateReportAccess(ra);
		// Other topic identity
		Identity coachOther = JunitTestHelper.createAndPersistIdentityAsRndUser("p1");
		QualityDataCollection dcOther = qualityTestHelper.createDataCollection();
		dcOther.setTopicIdentity(coachOther);
		dcOther = qualityService.updateDataCollection(dcOther);
		dcOther = qualityService.updateDataCollectionStatus(dcOther, QualityDataCollectionStatus.FINISHED);
		QualityReportAccess raOther = qualityService.createReportAccess(of(dcOther), QualityReportAccess.Type.TopicIdentity, null);
		raOther.setOnline(true);
		qualityService.updateReportAccess(raOther);
		// data collection not finished
		QualityDataCollection dcNotFinished = qualityTestHelper.createDataCollection();
		dcNotFinished.setTopicIdentity(coach);
		dcNotFinished = qualityService.updateDataCollection(dcNotFinished);
		QualityReportAccess raBotFinished = qualityService.createReportAccess(of(dcNotFinished), QualityReportAccess.Type.TopicIdentity, null);
		raBotFinished.setOnline(true);
		qualityService.updateReportAccess(raBotFinished);
		// Report access not configured
		QualityDataCollection dcNoAccess = qualityTestHelper.createDataCollection();
		dcNoAccess.setTopicIdentity(coach);
		dcNoAccess = qualityService.updateDataCollection(dcNoAccess);
		dcNoAccess = qualityService.updateDataCollectionStatus(dcNoAccess, QualityDataCollectionStatus.FINISHED);
		// Report access denied
		QualityDataCollection dcAccessDenied = qualityTestHelper.createDataCollection();
		dcAccessDenied.setTopicIdentity(coach);
		dcAccessDenied = qualityService.updateDataCollection(dcAccessDenied);
		dcAccessDenied = qualityService.updateDataCollectionStatus(dcAccessDenied, QualityDataCollectionStatus.FINISHED);
		QualityReportAccess raAccessDenied = qualityService.createReportAccess(of(dcAccessDenied), QualityReportAccess.Type.TopicIdentity, null);
		raAccessDenied.setOnline(false);
		qualityService.updateReportAccess(raAccessDenied);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setOrgansationRefs(Collections.emptyList());
		searchParams.setReportAccessIdentity(coach);
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections)
				.extracting(QualityDataCollectionView::getKey)
				.containsExactlyInAnyOrder(
						dc.getKey())
				.doesNotContain(
						dcOther.getKey(),
						dcNotFinished.getKey(),
						dcNoAccess.getKey(),
						dcAccessDenied.getKey()
						);
	}
	
	@Test
	public void shouldFilterDataCollectionsByReportMembers() {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("p1");
		// Everything fulfilled
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		qualityService.addReportMember(of(dc), member);
		dc = qualityService.updateDataCollectionStatus(dc, QualityDataCollectionStatus.FINISHED);
		QualityReportAccess ra = qualityService.loadMembersReportAccess(of(dc));
		ra.setOnline(true);
		qualityService.updateReportAccess(ra);
		// Data collection has other members
		Identity memberOther = JunitTestHelper.createAndPersistIdentityAsRndUser("p1");
		QualityDataCollection dcOther = qualityTestHelper.createDataCollection();
		qualityService.addReportMember(of(dcOther), memberOther);
		dcOther = qualityService.updateDataCollectionStatus(dcOther, QualityDataCollectionStatus.FINISHED);
		QualityReportAccess raOther = qualityService.loadMembersReportAccess(of(dcOther));
		raOther.setOnline(true);
		qualityService.updateReportAccess(raOther);
		// data collection not finished
		QualityDataCollection dcNotFinished = qualityTestHelper.createDataCollection();
		qualityService.addReportMember(of(dcNotFinished), member);
		QualityReportAccess raBotFinished = qualityService.loadMembersReportAccess(of(dcNotFinished));
		raBotFinished.setOnline(true);
		qualityService.updateReportAccess(raBotFinished);
		// Report access denied
		QualityDataCollection dcAccessDenied = qualityTestHelper.createDataCollection();
		qualityService.addReportMember(of(dcAccessDenied), member);
		dcAccessDenied = qualityService.updateDataCollectionStatus(dcAccessDenied, QualityDataCollectionStatus.FINISHED);
		QualityReportAccess raAccessDenied = qualityService.loadMembersReportAccess(of(dcAccessDenied));
		raAccessDenied.setOnline(false);
		qualityService.updateReportAccess(raAccessDenied);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setOrgansationRefs(Collections.emptyList());
		searchParams.setReportAccessIdentity(member);
		List<QualityDataCollectionView> dataCollections = sut.loadDataCollections(TRANSLATOR, searchParams, 0, -1);
		
		assertThat(dataCollections)
				.extracting(QualityDataCollectionView::getKey)
				.containsExactlyInAnyOrder(
						dc.getKey())
				.doesNotContain(
						dcOther.getKey(),
						dcNotFinished.getKey(),
						dcAccessDenied.getKey()
						);
	}
}
