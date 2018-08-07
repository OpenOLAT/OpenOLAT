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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;
import static org.olat.modules.quality.QualityDataCollectionStatus.RUNNING;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.ui.DataCollectionDataModel.DataCollectionCols;
import org.olat.repository.RepositoryEntry;
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
		assertThat(dataCollection.getStatus()).isEqualByComparingTo(QualityDataCollectionStatus.PREPARATION);
	}

	@Test
	public void shouldUpdateDataCollection() {
		QualityDataCollectionStatus status = QualityDataCollectionStatus.FINISHED;
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
		
		dataCollection.setStatus(status);
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
		assertThat(updatedDataCollection.getStatus()).isEqualByComparingTo(status);
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
	public void shouldLoadDataCollectionByKey() {
		QualityDataCollection dataCollection = sut.createDataCollection();
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection loadDataCollection = sut.loadDataCollectionByKey(dataCollection);
		
		assertThat(loadDataCollection).isEqualTo(dataCollection);
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
	public void shouldGetDataCollectionCount() {
		int numberOfDataCollections = 3;
		for (int i = 0; i < numberOfDataCollections; i++) {
			qualityTestHelper.createDataCollection();
		}
		dbInstance.commitAndCloseSession();
		
		int count = sut.getDataCollectionCount();
		
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


}
