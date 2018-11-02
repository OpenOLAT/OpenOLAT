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
package org.olat.modules.quality;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QualityService {

	public QualityDataCollection createDataCollection(Collection<Organisation> organisations,
			RepositoryEntry formEntry);
	
	public QualityDataCollection createDataCollection(Collection<Organisation> organisations, RepositoryEntry formEntry,
			QualityGenerator generator, Long generatorProviderKey);

	public QualityDataCollection createDataCollection(Collection<Organisation> organisations, QualityDataCollection previous,
			QualityGenerator generator, Long generatorProviderKey);
	
	public QualityDataCollection updateDataCollectionStatus(QualityDataCollection dataCollection,
			QualityDataCollectionStatus status);
	
	public QualityDataCollection updateDataCollection(QualityDataCollection dataCollection);

	public List<QualityDataCollection> loadAllDataCollections();

	public QualityDataCollection loadDataCollectionByKey(QualityDataCollectionRef dataCollectionRef);
	
	public QualityDataCollection loadPrevious(QualityDataCollectionRef dataCollectionRef);

	public QualityDataCollection loadFollowUp(QualityDataCollectionRef dataCollectionRef);

	public List<QualityDataCollection> loadDataCollections(QualityDataCollectionSearchParams searchParams);
	
	/**
	 * Updates the status to RUNNING for data collections which are READY and have passed the start date.
	 * 
	 * @param until the date to evaluate if a start date has passed
	 */
	public void stopDataCollections(Date until);

	/**
	 * Updates the status to FINISHED of all data collections which are PARTICIPATING and have passed the deadline.
	 * 
	 * @param until the date to evaluate if a deadline has passed
	 */
	public void startDataCollection(Date until);

	public int getDataCollectionCount(QualityDataCollectionViewSearchParams searchParams);

	public List<QualityDataCollectionView> loadDataCollections(Translator translator,
			QualityDataCollectionViewSearchParams searchParams, int firstResult, int maxResults, SortKey... orderBy);

	public List<QualityDataCollection> loadDataCollectionsByTaxonomyLevel(TaxonomyLevelRef taxonomyLevel);

	/**
	 * Deletes a data collection, the whole survey and all contexts.
	 *
	 * @param dataCollection
	 */
	public void deleteDataCollection(QualityDataCollectionLight dataCollection);
	
	public RepositoryEntry loadFormEntry(QualityDataCollectionLight dataCollection);
	
	public EvaluationFormSurvey loadSurvey(QualityDataCollectionLight dataCollection);

	public boolean isFormEntryUpdateable(QualityDataCollection dataCollection);

	public void updateFormEntry(QualityDataCollection dataCollection, RepositoryEntry formEntry);

	public List<Organisation> loadDataCollectionOrganisations(QualityDataCollectionRef dataCollectionRef);

	public void updateDataCollectionOrganisations(QualityDataCollection dataCollection,
			List<Organisation> organisations);

	/**
	 * Add the executors to the data collection and returns the participations of
	 * the executors. If already a participation for an executor exists, no further
	 * participation is created, but the existing participation is used and
	 * returned.
	 *
	 * @param dataCollection
	 * @param executors
	 * @return
	 */
	public List<EvaluationFormParticipation> addParticipations(QualityDataCollectionLight dataCollection,
			Collection<Identity> executors);

	public int getParticipationCount(QualityDataCollectionLight dataCollection);

	public List<QualityParticipation> loadParticipations(QualityDataCollectionLight dataCollection,
			int firstResult, int maxResults, SortKey... orderBy);

	public int getExecutorParticipationCount(QualityExecutorParticipationSearchParams searchParams);

	public List<QualityExecutorParticipation> loadExecutorParticipations(Translator translator,
			QualityExecutorParticipationSearchParams searchParams, int firstResult, int maxResults, SortKey... orderBy);

	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation);
	
	/**
	 * Create a QualityContextBuilder and populate it with the data according to the
	 * participation and the repository entry.
	 *
	 * @param dataCollection
	 * @param participation
	 * @param entry
	 * @param name 
	 * @return
	 */
	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation, RepositoryEntry entry, GroupRoles role);
	
	/**
	 * Create a QualityContextBuilder and populate it with the data according to the
	 * participation and the curriculum element.
	 *
	 * @param dataCollection
	 * @param participation
	 * @param curriculumElement
	 * @param name
	 * @return
	 */
	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation, CurriculumElement curriculumElement, CurriculumRoles role);
	
	public List<QualityContext> loadContextByParticipation(EvaluationFormParticipationRef participationRef);
	
	public void deleteContext(QualityContextRef contextRef);

	/**
	 * Deletes the contexts of the specified references. If a deleted context was
	 * the last one of a participation, the participation is deleted as well.
	 *
	 * @param contetxtRefs
	 */
	public void deleteContextsAndParticipations(Collection<QualityContextRef> contextRefs);

	public QualityReminder createReminder(QualityDataCollectionRef dataCollectionRef, Date sendDate,
			QualityReminderType type);

	public QualityReminder updateReminderDatePlaned(QualityReminder invitation, Date datePlaned);

	public QualityReminder loadReminder(QualityDataCollectionRef dataCollectionRef, QualityReminderType type);

	public void deleteReminder(QualityReminder reminder);
	
	/**
	 * Send all pending reminders.
	 *
	 * @param until all reminders with a planed date before this date
	 */
	public void sendReminders(Date until);
	
	public QualityReportAccess createReportAccess(QualityReportAccessReference reference, QualityReportAccess.Type type,
			String role);

	public QualityReportAccess updateReportAccess(QualityReportAccess reportAccess);
	
	public List<QualityReportAccess> loadReportAccesses(QualityReportAccessSearchParams searchParams);

}
