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
import static org.olat.modules.quality.QualityReportAccessReference.of;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
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
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.SessionStatusHandler;
import org.olat.modules.forms.SessionStatusInformation;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionSearchParams;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionToOrganisation;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccess.EmailTrigger;
import org.olat.modules.quality.QualityReportAccess.Type;
import org.olat.modules.quality.QualityReportAccessReference;
import org.olat.modules.quality.QualityReportAccessSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityServiceImpl
		implements QualityService, RepositoryEntryDataDeletable, OrganisationDataDeletable, CurriculumDataDeletable, SessionStatusHandler {

	private static final Logger log = Tracing.createLoggerFor(QualityServiceImpl.class);
	
	private static final String REPORT_MEMBER_ROLE = "member";

	@Autowired
	private QualityDataCollectionDAO dataCollectionDao;
	@Autowired
	private QualityDataCollectionToOrganisationDAO dataCollectionToOrganisationDao;
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
	private QualityParticipationDAO participationDao;
	@Autowired
	private QualityReminderDAO reminderDao;
	@Autowired
	private QualityReportAccessDAO reportAccessDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private QualityMailing qualityMailing;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BaseSecurityManager securityManager;
	
	@Override
	public List<Organisation> getDefaultOrganisations(Identity identity) {
		if (!organisationModule.isEnabled()) {
			Organisation organisation = organisationService.getDefaultOrganisation();
			return Collections.singletonList(organisation);
		}
		
		Roles roles = securityManager.getRoles(identity, false);
		List<OrganisationRef> qualityManagerOrgs = roles.getOrganisationsWithRole(OrganisationRoles.qualitymanager);
		if (!qualityManagerOrgs.isEmpty()) {
			return qualityManagerOrgs.stream().map(ref -> organisationService.getOrganisation(ref)).collect(Collectors.toList());
		}
		
		List<OrganisationRef> administratorOrgs = roles.getOrganisationsWithRole(OrganisationRoles.administrator);
		if (!administratorOrgs.isEmpty()) {
			return administratorOrgs.stream().map(ref -> organisationService.getOrganisation(ref)).collect(Collectors.toList());
		}
		
		Organisation organisation = organisationService.getDefaultOrganisation();
		return Collections.singletonList(organisation);
	}
	
	@Override
	public QualityDataCollection createDataCollection(Collection<Organisation> organisations,
			RepositoryEntry formEntry) {
		QualityDataCollection dataCollection = dataCollectionDao.createDataCollection();
		evaluationFormManager.createSurvey(getSurveyIdent(dataCollection), formEntry);
		createDataCollectionReferences(organisations, formEntry, dataCollection);
		return dataCollection;
	}

	@Override
	public QualityDataCollection createDataCollection(Collection<Organisation> organisations, RepositoryEntry formEntry,
			QualityGenerator generator, Long generatorProviderKey) {
		QualityDataCollection dataCollection = createDataCollection(generator, generatorProviderKey);
		evaluationFormManager.createSurvey(getSurveyIdent(dataCollection), formEntry);
		createDataCollectionReferences(organisations, formEntry, dataCollection);
		return dataCollection;
	}

	@Override
	public QualityDataCollection createDataCollection(Collection<Organisation> organisations, QualityDataCollection previous,
			QualityGenerator generator, Long generatorProviderKey) {
		QualityDataCollection dataCollection = createDataCollection(generator, generatorProviderKey);
		EvaluationFormSurvey previousSurvey = evaluationFormManager.loadSurvey(getSurveyIdent(previous));
		evaluationFormManager.createSurvey(getSurveyIdent(dataCollection), previousSurvey);
		createDataCollectionReferences(organisations, previousSurvey.getFormEntry(), dataCollection);
		return dataCollection;
	}
	
	private QualityDataCollection createDataCollection(QualityGenerator generator,
			Long generatorProviderKey) {
		QualityDataCollection dataCollection = dataCollectionDao.createDataCollection(generator, generatorProviderKey);
		log.info("Quality data collection " + dataCollection + " created by generator " + generator);
		return dataCollection;
	}

	private void createDataCollectionReferences(Collection<Organisation> organisations, RepositoryEntry formEntry,
			QualityDataCollection dataCollection) {
		resourceManager.findOrPersistResourceable(dataCollection);
		referenceManager.addReference(dataCollection, formEntry.getOlatResource(), null);
		for (Organisation organisation : organisations) {
			dataCollectionToOrganisationDao.createRelation(dataCollection, organisation);
		}
	}

	@Override
	public QualityDataCollection updateDataCollection(QualityDataCollection dataCollection) {
		return dataCollectionDao.updateDataCollection(dataCollection);
	}

	@Override
	public List<QualityDataCollection> loadAllDataCollections() {
		return dataCollectionDao.loadAllDataCollections();
	}
	
	@Override
	public QualityDataCollection loadDataCollectionByKey(QualityDataCollectionRef dataCollectionRef) {
		return dataCollectionDao.loadDataCollectionByKey(dataCollectionRef);
	}

	@Override
	public QualityDataCollection loadPrevious(QualityDataCollectionRef dataCollectionRef) {
		return dataCollectionDao.loadPrevious(dataCollectionRef);
	}

	@Override
	public QualityDataCollection loadFollowUp(QualityDataCollectionRef dataCollectionRef) {
		return dataCollectionDao.loadFollowUp(dataCollectionRef);
	}

	@Override
	public List<QualityDataCollection> loadDataCollections(QualityDataCollectionSearchParams searchParams) {
		return dataCollectionDao.loadDataCollections(searchParams);
	}

	@Override
	public List<QualityDataCollection> loadDataCollectionsByTaxonomyLevel(TaxonomyLevelRef taxonomyLevel) {
		return dataCollectionDao.loadDataCollectionsByTaxonomyLevel(taxonomyLevel);
	}

	@Override
	public void startDataCollection(Date until) {
		Collection<QualityDataCollection> dataCollections = dataCollectionDao.loadWithPendingStart(until);
		log.debug("Update status to RUNNING. Number of pending data collections: " + dataCollections.size());
		for (QualityDataCollection dataCollection: dataCollections) {
			updateDataCollectionStatus(dataCollection, RUNNING);
		}
	}

	@Override
	public void stopDataCollections(Date until) {
		Collection<QualityDataCollection> dataCollections = dataCollectionDao.loadWithPendingDeadline(until);
		log.debug("Update status to FINISHED. Number of pending data collections: " + dataCollections.size());
		for (QualityDataCollection dataCollection: dataCollections) {
			updateDataCollectionStatus(dataCollection, FINISHED);
		}
	}

	@Override
	public QualityDataCollection updateDataCollectionStatus(QualityDataCollection dataCollection, QualityDataCollectionStatus status) {
		QualityDataCollection updatedDataCollection = dataCollection;
		try {
			QualityDataCollectionStatus previousStatus = dataCollection.getStatus();
			updatedDataCollection = dataCollectionDao.updateDataCollectionStatus(dataCollection, status);
			if (hasChangedToFinished(status, previousStatus)) {
				reportAccessDao.deleteUnappropriated(of(updatedDataCollection));
				sendReportAccessMails(dataCollection);
			}
			log.info("Status of quality data collection updated to " + status + ". " + updatedDataCollection.toString());
		} catch (Exception e) {
			log.error("Update of status of quality data collection to " + status + " failed! " + dataCollection.toString(), e);
		}
		return updatedDataCollection;
	}

	private boolean hasChangedToFinished(QualityDataCollectionStatus status,
			QualityDataCollectionStatus previousStatus) {
		return QualityDataCollectionStatus.FINISHED.equals(status) && !QualityDataCollectionStatus.FINISHED.equals(previousStatus);
	}

	private void sendReportAccessMails(QualityDataCollection dataCollection) {
		List<RubricStatistic> rubricStatistics = getRubricsStatistics(dataCollection);
		Set<Identity> recipients = getReportAccessRecipients(dataCollection, rubricStatistics);
		qualityMailing.sendReportAccessEmail(dataCollection, recipients, rubricStatistics);
	}
	
	private Set<Identity> getReportAccessRecipients(QualityDataCollection dataCollection, List<RubricStatistic> rubricStatistics) {
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(of(dataCollection));
		List<QualityReportAccess> reportAccesses = reportAccessDao.load(searchParams);
		Set<Identity> recipients = new HashSet<>();
		
		for (QualityReportAccess reportAccess : reportAccesses) {
			EmailTrigger emailTrigger = reportAccess.getEmailTrigger();
			if (accessEnabled(reportAccess) && containsTrigger(rubricStatistics, emailTrigger)) {
				List<Identity> identities = reportAccessDao.loadRecipients(reportAccess);
				recipients.addAll(identities);
			}
		}
		
		return recipients;
	}

	private boolean accessEnabled(QualityReportAccess reportAccess) {
		if (QualityReportAccess.Type.RelationRole.equals(reportAccess.getType())
				&& !securityModule.isRelationRoleEnabled()) {
			return false;
		}
		return true;
	}

	private boolean containsTrigger(List<RubricStatistic> rubricStatistics, EmailTrigger emailTrigger) {
		if (QualityReportAccess.EmailTrigger.always.equals(emailTrigger)) {
			return true;
		}
		for (RubricStatistic rubricStatistic : rubricStatistics) {
			RubricRating rating = rubricStatistic.getTotalStatistic().getRating();
			if (RubricRating.NEUTRAL.equals(rating)
					&& EmailTrigger.insufficientNeutral.equals(emailTrigger)) {
				return true;
			}
			if (RubricRating.INSUFFICIENT.equals(rating) 
					&& Arrays.asList(EmailTrigger.insufficientNeutral, EmailTrigger.insufficient).contains(emailTrigger)) {
				return true;
			}
		}
		return false;
	}
	
	private List<RubricStatistic> getRubricsStatistics(QualityDataCollection dataCollection) {
		RepositoryEntry formEntry = loadFormEntry(dataCollection);
		Form form = evaluationFormManager.loadForm(formEntry);
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(getSurveyIdent(dataCollection));
		SessionFilter filter = SessionFilterFactory.createSelectDone(survey);
		
		List<Rubric> rubrics = form.getElements().stream()
				.filter(e -> Rubric.TYPE.equals(e.getType()))
				.map(e -> (Rubric)e)
				.collect(Collectors.toList());
		
		List<RubricStatistic> statistics = new ArrayList<>(rubrics.size());
		for (Rubric rubric: rubrics) {
			RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, filter);
			statistics.add(rubricStatistic);
		}
		return statistics;
	}
	
	@Override
	public List<QualityGenerator> getGenerators(QualityDataCollectionViewSearchParams searchParams) {
		return dataCollectionDao.loadGenerators(searchParams);
	}
	
	@Override
	public List<RepositoryEntry> getFormEntries(QualityDataCollectionViewSearchParams searchParams) {
		return dataCollectionDao.loadFormEntries(searchParams);
	}

	@Override
	public int getDataCollectionCount(QualityDataCollectionViewSearchParams searchParams) {
		return dataCollectionDao.getDataCollectionCount(searchParams);
	}

	@Override
	public List<QualityDataCollectionView> loadDataCollections(Translator translator,
			QualityDataCollectionViewSearchParams searchParams, int firstResult, int maxResults, SortKey... orderBy) {
		return dataCollectionDao.loadDataCollections(translator, searchParams, firstResult, maxResults, orderBy);
	}

	@Override
	public void deleteDataCollection(QualityDataCollectionLight dataCollection) {
		List<QualityContext> contexts = contextDao.loadByDataCollection(dataCollection);
		for (QualityContext context: contexts) {
			deleteContext(context);
		}
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(getSurveyIdent(dataCollection));
		evaluationFormManager.deleteSurvey(survey);
		deleteReferences(dataCollection);
		resourceManager.deleteOLATResourceable(dataCollection);
		deleteReportAccess(of(dataCollection));
		reminderDao.deleteReminders(dataCollection);
		dataCollectionToOrganisationDao.deleteRelations(dataCollection);
		dataCollectionDao.deleteDataCollection(dataCollection);
		log.info("Quality management data collection deleted: " + dataCollection.toString());
	}

	@Override
	public RepositoryEntry loadFormEntry(QualityDataCollectionLight dataCollection) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(getSurveyIdent(dataCollection));
		return survey.getFormEntry() != null? survey.getFormEntry(): null;
	}
	
	@Override
	public EvaluationFormSurvey loadSurvey(QualityDataCollectionLight dataCollection) {
		return evaluationFormManager.loadSurvey(getSurveyIdent(dataCollection));
	}

	@Override
	public boolean isFormEntryUpdateable(QualityDataCollection dataCollection) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(getSurveyIdent(dataCollection));
		return evaluationFormManager.isFormUpdateable(survey);
	}

	@Override
	public void updateFormEntry(QualityDataCollection dataCollection, RepositoryEntry formEntry) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(getSurveyIdent(dataCollection));
		deleteReferences(dataCollection);
		
		evaluationFormManager.updateSurveyForm(survey, formEntry);
		referenceManager.addReference(dataCollection, formEntry.getOlatResource(), null);
	}

	private void deleteReferences(QualityDataCollectionLight dataCollection) {
		for (Reference reference : referenceManager.getReferences(dataCollection)) {
			referenceManager.delete(reference);
		}
	}

	@Override
	public List<Organisation> loadDataCollectionOrganisations(QualityDataCollectionRef dataCollectionRef) {
		return dataCollectionToOrganisationDao.loadOrganisationsByDataCollectionKey(dataCollectionRef);
	}

	@Override
	public void updateDataCollectionOrganisations(QualityDataCollection dataCollection,
			List<Organisation> organisations) {
		Set<QualityDataCollectionToOrganisation> currentRelations = new HashSet<>(dataCollectionToOrganisationDao
				.loadByDataCollectionKey(dataCollection));
		List<QualityDataCollectionToOrganisation> copyRelations = new ArrayList<>(currentRelations);
		List<Organisation> currentOrganisationsByRelations = new ArrayList<>();
		for(QualityDataCollectionToOrganisation relation:copyRelations) {
			if(!organisations.contains(relation.getOrganisation())) {
				dataCollectionToOrganisationDao.delete(relation);
				currentRelations.remove(relation);
			} else {
				currentOrganisationsByRelations.add(relation.getOrganisation());
			}
		}
		
		for(Organisation organisation:organisations) {
			if(!currentOrganisationsByRelations.contains(organisation)) {
				QualityDataCollectionToOrganisation newRelation = dataCollectionToOrganisationDao.createRelation(dataCollection, organisation);
				currentRelations.add(newRelation);
			}
		}
	}

	@Override
	public List<EvaluationFormParticipation> addParticipations(QualityDataCollectionLight dataCollection, Collection<Identity> executors) {
		List<EvaluationFormParticipation> participations = new ArrayList<>();
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(getSurveyIdent(dataCollection));
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
	public Long getExecutorParticipationCount(QualityExecutorParticipationSearchParams searchParams) {
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
	public List<QualityContext> loadContextByDataCollection(QualityDataCollectionRef dataCollectionRef) {
		return contextDao.loadByDataCollectionFetched(dataCollectionRef);
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
	public List<QualityReminder> loadReminders(QualityDataCollectionRef dataCollectionRef){
		return reminderDao.load(dataCollectionRef);
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
	public void sendReminders(Date until) {
		Collection<QualityReminder> reminders = reminderDao.loadPending(until);
		log.debug("Send emails for quality remiders. Number of pending reminders: " + reminders.size());
		for (QualityReminder reminder: reminders) {
			try {
				if (QualityReminderType.ANNOUNCEMENT_COACH_TOPIC == reminder.getType()) {
					sendAnnouncementTopicIdentity(reminder);
				} else {
					sendReminder(reminder);
				}
				reminderDao.updateDateDone(reminder, until);
			} catch (Exception e) {
				log.error("Send reminder of quality data collection failed!" + reminder.toString(), e);
			}
		}	
	}

	private void sendAnnouncementTopicIdentity(QualityReminder reminder) {
		Identity topicIdentity = reminder.getDataCollection().getTopicIdentity();
		if (topicIdentity != null) {
			qualityMailing.sendAnnouncementMail(reminder, topicIdentity);
		}
	}

	private void sendReminder(QualityReminder reminder) {
		QualityReminder invitation = getInvitation(reminder);
		List<EvaluationFormParticipation> participations = getParticipants(reminder);
		for (EvaluationFormParticipation participation : participations) {
			qualityMailing.sendReminderMail(reminder, invitation, participation);
		}
	}

	private QualityReminder getInvitation(QualityReminder reminder) {
		if (QualityReminderType.INVITATION.equals(reminder.getType())) {
			return reminder;
		}
		return reminderDao.load(reminder.getDataCollection(), QualityReminderType.INVITATION);
	}

	private List<EvaluationFormParticipation> getParticipants(QualityReminder reminder) {
		if (QualityReminderType.ANNOUNCEMENT_COACH_CONTEXT == reminder.getType()) {
			return getContextCaches(reminder);
		}
		return getSurveyParticipants(reminder);
	}

	private List<EvaluationFormParticipation> getContextCaches(QualityReminder reminder) {
		return contextDao.loadParticipationByRole(reminder.getDataCollection(), QualityContextRole.coach);
	}

	private List<EvaluationFormParticipation> getSurveyParticipants(QualityReminder reminder) {
		QualityReminderType type = reminder.getType();
		EvaluationFormParticipationStatus status = type.getParticipationStatus();
		QualityDataCollection dataCollection = reminder.getDataCollection();
		EvaluationFormSurveyRef survey = evaluationFormManager.loadSurvey(getSurveyIdent(dataCollection));
		return evaluationFormManager.loadParticipations(survey, status, true);
	}


	@Override
	public boolean deleteRepositoryEntryData(RepositoryEntry re) {
		return !dataCollectionDao.hasDataCollection(re)
				&& !contextDao.hasContexts(re);
	}

	@Override
	public boolean deleteOrganisationData(Organisation organisation, Organisation replacementOrganisation) {
		return !dataCollectionDao.hasDataCollection(organisation)
				&& !contextToOrganisationDao.hasRelations(organisation)
				&& !dataCollectionToOrganisationDao.hasRelations(organisation);
	}

	@Override
	public boolean deleteCurriculumData(Curriculum curriculum) {
		return !dataCollectionDao.hasDataCollection(curriculum)
				&& !contextToCurriculumDao.hasRelations(curriculum);
	}

	@Override
	public boolean deleteCurriculumElementData(CurriculumElement curriculumElement) {
		return !dataCollectionDao.hasDataCollection(curriculumElement)
				&& !contextToCurriculumElementDao.hasRelations(curriculumElement)
				&& !contextDao.hasContexts(curriculumElement);
	}

	@Override
	public void onFinish(SessionStatusInformation infos) {
		contextDao.finish(infos.getParticipation(), infos.getSession());
	}

	@Override
	public void onReopen(SessionStatusInformation infos) {
		contextDao.reopen(infos.getSession(), infos.getParticipation());
	}

	@Override
	public QualityReportAccess createReportAccess(QualityReportAccessReference reference, Type type, String role) {
		return reportAccessDao.create(reference, type, role);
	}

	@Override
	public QualityReportAccess copyReportAccess(QualityReportAccessReference reference, QualityReportAccess reportAccess) {
		QualityReportAccess copy = reportAccessDao.copy(reference, reportAccess);
		if (reportAccess.getGroup() != null) {
			Group copyGroup = groupDao.createGroup();
			copy = reportAccessDao.setGroup(copy, copyGroup);
			List<Identity> members = groupDao.getMembers(reportAccess.getGroup(), REPORT_MEMBER_ROLE);
			for (Identity identity : members) {
				addReportMember(copyGroup, identity);
			}
		}
		return copy;
	}

	@Override
	public QualityReportAccess updateReportAccess(QualityReportAccess reportAccess) {
		return reportAccessDao.save(reportAccess);
	}

	@Override
	public List<QualityReportAccess> loadReportAccesses(QualityReportAccessSearchParams searchParams) {
		return reportAccessDao.load(searchParams);
	}

	@Override
	public void deleteReportAccess(QualityReportAccessReference reference) {
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(reference);
		List<QualityReportAccess> reportAccesses = reportAccessDao.load(searchParams);
		for (QualityReportAccess reportAccess : reportAccesses) {
			if (reportAccess.getGroup() != null) {
				groupDao.removeMemberships(reportAccess.getGroup());
				groupDao.removeGroup(reportAccess.getGroup());
			}
		}
		reportAccessDao.deleteReportAccesses(reference);
	}

	@Override
	public QualityReportAccess loadMembersReportAccess(QualityReportAccessReference reference) {
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(reference);
		searchParams.setType(QualityReportAccess.Type.ReportMember);
		List<QualityReportAccess> reportAccesses = reportAccessDao.load(searchParams);
		return !reportAccesses.isEmpty() ? reportAccesses.get(0): null;
	}

	@Override
	public void addReportMember(QualityReportAccessReference reference, Identity identity) {
		QualityReportAccess reportAccess = loadMembersReportAccess(reference);
		if (reportAccess == null) {
			reportAccess = createMembersReportAccess(reference);
		}
		Group group = reportAccess.getGroup();
		if (group == null) {
			group = groupDao.createGroup();
			reportAccessDao.setGroup(reportAccess, group);
		}
		addReportMember(group, identity);
	}

	private void addReportMember(Group group, Identity identity) {
		if (!groupDao.hasRole(group, identity, REPORT_MEMBER_ROLE)) {
			groupDao.addMembershipOneWay(group, identity, REPORT_MEMBER_ROLE);
		}
	}

	@Override
	public void removeReportMember(QualityReportAccessReference reference, IdentityRef identityRef) {
		QualityReportAccess reportAccess = loadMembersReportAccess(reference);
		if (reportAccess != null && reportAccess.getGroup() != null) {
			groupDao.removeMembership(reportAccess.getGroup(), identityRef);
		}
	}

	private QualityReportAccess createMembersReportAccess(QualityReportAccessReference reference) {
		return reportAccessDao.create(reference, QualityReportAccess.Type.ReportMember, null);
	}

	@Override
	public List<Identity> loadReportMembers(QualityReportAccessReference reference) {
		QualityReportAccess reportAccess = loadMembersReportAccess(reference);
		if (reportAccess != null && reportAccess.getGroup() != null) {
			return groupDao.getMembers(reportAccess.getGroup(), REPORT_MEMBER_ROLE);
		}
		return Collections.emptyList();
	}
	
	private EvaluationFormSurveyIdentifier getSurveyIdent(QualityDataCollectionLight dataCollection) {
		return EvaluationFormSurveyIdentifier.of(dataCollection);
	}
	
}
