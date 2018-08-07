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

import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.RUNNING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumDataDeletable;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityServiceImpl implements QualityService, OrganisationDataDeletable, CurriculumDataDeletable {

	private static final OLog log = Tracing.createLoggerFor(QualityServiceImpl.class);

	@Autowired
	private QualityDataCollectionDAO dataCollectionDao;
	@Autowired
	private QualityContextDAO contextDao;
	@Autowired
	private QualityContextToCurriculumDAO contextToCurriculumDao;
	@Autowired
	private QualityContextToCurriculumElementDAO contextToCurriculumElementDao;
	@Autowired
	private QualityContextToOrganisationDAO contextToOrganisationDao;
	@Autowired
	private QualityContextToTaxonomyLevelDAO contextToTaxonomyLevelDao;
	@Autowired
	private QualityReminderDAO reminderDao;
	@Autowired
	private QualityParticipationDAO participationDao;
	@Autowired
	private QualityMailing qualityMailing;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	@Override
	public QualityDataCollection createDataCollection(RepositoryEntry formEntry) {
		QualityDataCollection dataCollection = dataCollectionDao.createDataCollection();
		evaluationFormManager.createSurvey(dataCollection, null, formEntry);
		return dataCollection;
	}

	@Override
	public QualityDataCollection updateDataCollection(QualityDataCollection dataCollection) {
		return dataCollectionDao.updateDataCollection(dataCollection);
	}

	@Override
	public QualityDataCollection loadDataCollectionByKey(QualityDataCollectionRef dataCollectionRef) {
		return dataCollectionDao.loadDataCollectionByKey(dataCollectionRef);
	}

	@Override
	public List<QualityDataCollection> loadDataCollectionsByTaxonomyLevel(TaxonomyLevelRef taxonomyLevel) {
		return dataCollectionDao.loadDataCollectionsByTaxonomyLevel(taxonomyLevel);
	}

	@Override
	public void stopDataCollections(Date until) {
		Collection<QualityDataCollection> dataCollections = dataCollectionDao.loadWithPendingStart(until);
		log.debug("Update status to RUNNING. Number of pending data collections: " + dataCollections.size());
		for (QualityDataCollection dataCollection: dataCollections) {
			updateDataCollection(dataCollection, RUNNING);
		}
	}

	@Override
	public void startDataCollection(Date until) {
		Collection<QualityDataCollection> dataCollections = dataCollectionDao.loadWithPendingDeadline(until);
		log.debug("Update status to FINISHED. Number of pending data collections: " + dataCollections.size());
		for (QualityDataCollection dataCollection: dataCollections) {
			updateDataCollection(dataCollection, FINISHED);
		}
	}

	private void updateDataCollection(QualityDataCollection dataCollection, QualityDataCollectionStatus status) {
		try {
			dataCollection.setStatus(status);
			QualityDataCollection updatedDataCollection = dataCollectionDao.updateDataCollection(dataCollection);
			log.info("Status of quality data collection updated to " + status + ". " + updatedDataCollection.toString());
		} catch (Exception e) {
			log.error("Update of status of quality data collection to " + status + " failed! " + dataCollection.toString(), e);
		}
	}

	@Override
	public int getDataCollectionCount() {
		return dataCollectionDao.getDataCollectionCount();
	}

	@Override
	public List<QualityDataCollectionView> loadDataCollections(Translator translator,
			QualityDataCollectionViewSearchParams searchParams, int firstResult, int maxResults, SortKey... orderBy) {
		return dataCollectionDao.loadDataCollections(translator, null, firstResult, maxResults, orderBy);
	}

	@Override
	public void deleteDataCollection(QualityDataCollectionLight dataCollection) {
		List<QualityContext> contexts = contextDao.loadByDataCollection(dataCollection);
		for (QualityContext context: contexts) {
			deleteContext(context);
		}
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		evaluationFormManager.deleteSurvey(survey);
		dataCollectionDao.deleteDataCollection(dataCollection);
		log.info("Quality management data collection deleted: " + dataCollection.toString());
	}

	@Override
	public RepositoryEntry loadFormEntry(QualityDataCollectionLight dataCollection) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		return survey.getFormEntry() != null? survey.getFormEntry(): null;
	}

	@Override
	public boolean isFormEntryUpdateable(QualityDataCollection dataCollection) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		return evaluationFormManager.isFormUpdateable(survey);
	}

	@Override
	public void updateFormEntry(QualityDataCollection dataCollection, RepositoryEntry formEntry) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		evaluationFormManager.updateSurveyForm(survey, formEntry);
	}

	@Override
	public List<EvaluationFormParticipation> addParticipations(QualityDataCollectionLight dataCollection, Collection<Identity> executors) {
		List<EvaluationFormParticipation> participations = new ArrayList<>();
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		for (Identity executor: executors) {
			EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByExecutor(survey, executor);
			if (participation == null) {
				participation = evaluationFormManager.createParticipation(survey, executor, true);
			}
			participations.add(participation);
		}
		return participations;
	}

	@Override
	public int getParticipationCount(QualityDataCollectionLight dataCollection) {
		return participationDao.getParticipationCount(dataCollection);
	}

	@Override
	public List<QualityParticipation> loadParticipations(QualityDataCollectionLight dataCollection,
			int firstResult, int maxResults, SortKey... orderBy) {
		return participationDao.loadParticipations(dataCollection, firstResult, maxResults, orderBy);
	}

	@Override
	public int getExecutorParticipationCount(QualityExecutorParticipationSearchParams searchParams) {
		return participationDao.getExecutorParticipationCount(searchParams);
	}

	@Override
	public List<QualityExecutorParticipation> loadExecutorParticipations(Translator translator,
			QualityExecutorParticipationSearchParams searchParams, int firstResult, int maxResults,
			SortKey... orderBy) {
		return participationDao.loadExecutorParticipations(translator, searchParams, firstResult, maxResults, orderBy);
	}

	@Override
	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation) {
		return AudiencelessQualityContextBuilder.builder(dataCollection, participation);
	}
	
	@Override
	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation, RepositoryEntry entry, GroupRoles role) {
		return RepositoryEntryQualityContextBuilder.builder(dataCollection, participation, entry, role);
	}

	@Override
	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation, CurriculumElement curriculumElement, CurriculumRoles role) {
		return CurriculumElementQualityContextBuilder.builder(dataCollection, participation, curriculumElement, role);
	}

	@Override
	public List<QualityContext> loadContextByParticipation(EvaluationFormParticipationRef participationRef) {
		return contextDao.loadByParticipation(participationRef);
	}
	
	@Override
	public void deleteContext(QualityContextRef contextRef) {
		if (contextRef != null) {
			contextToCurriculumDao.deleteRelations(contextRef);
			contextToCurriculumElementDao.deleteRelations(contextRef);
			contextToOrganisationDao.deleteRelations(contextRef);
			contextToTaxonomyLevelDao.deleteRelations(contextRef);
			contextDao.deleteContext(contextRef);
		}
	}

	@Override
	public void deleteContextsAndParticipations(Collection<QualityContextRef> contextRefs) {
		for (QualityContextRef contextRef: contextRefs) {
			QualityContext context = contextDao.loadByKey(contextRef);
			EvaluationFormParticipationRef participationRef = context.getEvaluationFormParticipation();
			deleteContext(contextRef);
			if (!contextDao.hasContexts(participationRef)) {
				evaluationFormManager.deleteParticipations(Collections.singletonList(participationRef));
			}
		}
	}

	@Override
	public QualityReminder createReminder(QualityDataCollectionRef dataCollectionRef, Date sendDate,
			QualityReminderType type) {
		return reminderDao.create(dataCollectionRef, sendDate, type);
	}

	@Override
	public QualityReminder updateReminderDatePlaned(QualityReminder reminder, Date datePlaned) {
		return reminderDao.updateDatePlaned(reminder, datePlaned);
	}

	@Override
	public QualityReminder loadReminder(QualityDataCollectionRef dataCollectionRef, QualityReminderType type) {
		return reminderDao.load(dataCollectionRef, type);
	}

	@Override
	public void deleteReminder(QualityReminder reminder) {
		reminderDao.delete(reminder);
	}

	@Override
	public void sendRemainders(Date until) {		
		Collection<QualityReminder> reminders = reminderDao.loadPending(until);
		log.debug("Send emails for quality remiders. Number of pending reminders: " + reminders.size());
		for (QualityReminder reminder: reminders) {
			try {
				sendReminder(reminder);
				reminderDao.updateDateDone(reminder, until);
			} catch (Exception e) {
				log.error("Send reminder of quality data collection failed!" + reminder.toString(), e);
			}
		}	
	}

	private void sendReminder(QualityReminder reminder) {
		QualityReminder invitation = getInvitation(reminder);
		List<EvaluationFormParticipation> participations = getParticipants(reminder);
		for (EvaluationFormParticipation participation : participations) {
			qualityMailing.sendMail(reminder, invitation, participation);
		}
	}

	private QualityReminder getInvitation(QualityReminder reminder) {
		if (QualityReminderType.INVITATION.equals(reminder.getType())) {
			return reminder;
		}
		return reminderDao.load(reminder.getDataCollection(), QualityReminderType.INVITATION);
	}

	private List<EvaluationFormParticipation> getParticipants(QualityReminder reminder) {
		QualityReminderType type = reminder.getType();
		EvaluationFormParticipationStatus status = type.getParticipationStatus();
		OLATResourceable dataCollection = reminder.getDataCollection();
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		return evaluationFormManager.loadParticipations(survey, status);
	}

	/**
	 * If the organisation has relations to a context, send a veto.
	 */
	@Override
	public boolean deleteOrganisationData(Organisation organisation) {
		return !contextToOrganisationDao.hasRelations(organisation)
				&& !dataCollectionDao.hasDataCollection(organisation);
	}

	@Override
	public boolean deleteCurriculumData(Curriculum curriculum) {
		return true;
	}

	@Override
	public boolean deleteCurriculumElementData(CurriculumElement curriculumElement) {
		return !contextDao.hasContexts(curriculumElement)
				&& !contextToCurriculumElementDao.hasRelations(curriculumElement)
				&& !dataCollectionDao.hasDataCollection(curriculumElement);
	}
	
}
