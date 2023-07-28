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
package org.olat.course.nodes.form.manager;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.nodes.form.FormParticipation;
import org.olat.course.nodes.form.FormParticipationSearchParams;
import org.olat.course.nodes.form.model.FormParticipationImpl;
import org.olat.course.nodes.form.ui.FormConfigController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.EvaluationFormExcelExport.UserColumns;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.04.2021<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FormManagerImpl implements FormManager {
	
	private static final Logger log = Tracing.createLoggerFor(FormManagerImpl.class);

	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private FormMailing formMailing;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private DueDateService dueDateService;

	@Override
	public EvaluationFormSurveyIdentifier getSurveyIdentifier(CourseNode courseNode, ICourse course) {
		RepositoryEntry courseEntry = repositoryManager.lookupRepositoryEntry(course, true);
		return getSurveyIdentifier(courseNode, courseEntry);
	}
			
	@Override
	public EvaluationFormSurveyIdentifier getSurveyIdentifier(CourseNode courseNode,
			RepositoryEntry courseEntry) {
		String subIdent = courseNode.getIdent();
		return EvaluationFormSurveyIdentifier.of(courseEntry, subIdent);
	}

	@Override
	public EvaluationFormSurvey loadSurvey(EvaluationFormSurveyIdentifier surveyIdent) {
		return evaluationFormManager.loadSurvey(surveyIdent);
	}
	
	@Override
	public void deleteSurvey(EvaluationFormSurvey survey) {
		evaluationFormManager.deleteSurvey(survey);
	}

	@Override
	public EvaluationFormSurvey createSurvey(EvaluationFormSurveyIdentifier surveyIdent, RepositoryEntry formEntry) {
		return evaluationFormManager.createSurvey(surveyIdent, formEntry);
	}

	@Override
	public boolean isFormUpdateable(EvaluationFormSurvey survey) {
		return evaluationFormManager.isFormUpdateable(survey);
	}

	@Override
	public EvaluationFormSurvey updateSurveyForm(EvaluationFormSurvey survey, RepositoryEntry formEntry) {
		return evaluationFormManager.updateSurveyForm(survey, formEntry);
	}

	@Override
	public Form loadForm(EvaluationFormSurvey survey) {
		RepositoryEntry formEntry = survey.getFormEntry();
		return evaluationFormManager.loadForm(formEntry);
	}

	@Override
	public File getFormFile(RepositoryEntry formEntry) {
		File repositoryDir = new File(
				FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()),
				FileResourceManager.ZIPDIR);
		return new File(repositoryDir, FORM_XML_FILE);
	}

	@Override
	public DataStorage loadStorage(RepositoryEntry formEntry) {
		return evaluationFormManager.loadStorage(formEntry);
	}

	@Override
	public EvaluationFormParticipation loadParticipation(EvaluationFormSurvey survey, Identity identity) {
		return evaluationFormManager.loadParticipationByExecutor(survey, identity);
	}

	@Override
	public EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSurvey survey, Identity identity) {
		EvaluationFormParticipation loadedParticipation = evaluationFormManager.loadParticipationByExecutor(survey, identity);
		if (loadedParticipation == null) {
			loadedParticipation = evaluationFormManager.createParticipation(survey, identity);
		}
		return loadedParticipation;
	}

	@Override
	public List<EvaluationFormParticipation> getParticipations(EvaluationFormSurvey survey,
			EvaluationFormParticipationStatus status, boolean fetchExecutor) {
		return evaluationFormManager.loadParticipations(survey, status, fetchExecutor);
	}
	
	@Override
	public void reopenParticipation(EvaluationFormParticipationRef participationRef, CourseNode courseNode, CourseEnvironment courseEnv) {
		EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByKey(participationRef);
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participationRef);
		if (session != null && participation != null && EvaluationFormSessionStatus.done == session.getEvaluationFormSessionStatus()) {
			evaluationFormManager.reopenSession(session);
			
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(
					participation.getExecutor(), courseEnv);
			// Unfortunately we do not know the exact progress
			courseAssessmentService.updateCompletion(courseNode, assessedUserCourseEnv, Double.valueOf(0.5),
					AssessmentEntryStatus.inProgress, Role.coach);
			
			log.info(Tracing.M_AUDIT, "Form reopend: {}, course node {}, participant {}", 
					courseEnv.getCourseGroupManager().getCourseEntry(), courseNode.getIdent(), participation.getExecutor());
		}
	}
	
	@Override
	public void deleteParticipation(EvaluationFormParticipationRef participationRef, CourseNode courseNode, CourseEnvironment courseEnv) {
		EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByKey(participationRef);
		if (participation != null) {
			evaluationFormManager.deleteParticipations(Collections.singletonList(participationRef));
			
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(
					participation.getExecutor(), courseEnv);
			courseAssessmentService.updateCompletion(courseNode, assessedUserCourseEnv, Double.valueOf(0.0),
					AssessmentEntryStatus.notStarted, Role.coach);
			
			log.info(Tracing.M_AUDIT, "Form data deleted: {}, course node {}, participant {}", 
					courseEnv.getCourseGroupManager().getCourseEntry(), courseNode.getIdent(), participation.getExecutor());
		}
	}

	@Override
	public EvaluationFormSession loadOrCreateSession(EvaluationFormParticipation participation) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}
	
	@Override
	public EvaluationFormSession getSession(EvaluationFormSurvey survey, Identity identity) {
		EvaluationFormParticipation participation = loadParticipation(survey, identity);
		if (participation != null) {
			return evaluationFormManager.loadSessionByParticipation(participation);
		}
		return null;
	}
	
	@Override
	public EvaluationFormSession getDoneSession(EvaluationFormSurvey survey, Identity identity) {
		EvaluationFormParticipation participation = loadParticipation(survey, identity);
		if (participation != null) {
			EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
			if (EvaluationFormSessionStatus.done == session.getEvaluationFormSessionStatus()) {
				return session;
			}
		}
		return null;
	}
	
	@Override
	public void onQuickSave(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Double competion) {
		courseAssessmentService.updateCompletion(courseNode, userCourseEnv, competion, AssessmentEntryStatus.inProgress,
				Role.user);
	}

	@Override
	public void onExecutionFinished(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		courseAssessmentService.incrementAttempts(courseNode, userCourseEnv, Role.user);
		courseAssessmentService.updateCompletion(courseNode, userCourseEnv, Double.valueOf(1),
				AssessmentEntryStatus.done, Role.user);

		if (courseNode.getModuleConfiguration().getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_OWNERS)
		|| courseNode.getModuleConfiguration().getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_COACHES)
		|| courseNode.getModuleConfiguration().getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_PARTICIPANT)
		|| courseNode.getModuleConfiguration().getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL)) {
			sendConfirmationEmail(courseNode, userCourseEnv);
		}
		
		log.info(Tracing.M_AUDIT, "Form filled in: {}, course node {}", 
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				courseNode.getIdent());
	}

	private void sendConfirmationEmail(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		Translator translator = Util.createPackageTranslator(FormConfigController.class, Locale.getDefault());
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		EvaluationFormSurveyIdentifier surveyIdent = getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = loadSurvey(surveyIdent);
		EvaluationFormParticipation participation = loadParticipation(survey, identity);
		EvaluationFormSession session = participation != null? loadOrCreateSession(participation): null;
		List<ContactList> contactList = new ArrayList<>();
		ModuleConfiguration moduleConfiguration = courseNode.getModuleConfiguration();

		if (moduleConfiguration.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_OWNERS)) {
			contactList.add(getOwnersContactList(courseEntry, translator));
		}
		if (moduleConfiguration.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_COACHES)) {
			contactList.add(getCoachesContactList(courseEntry, translator));
		}
		if (moduleConfiguration.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_PARTICIPANT)) {
			ContactList cl = new ContactList(translator.translate("recipient.participant"));
			cl.add(identity);
			contactList.add(cl);
		}
		if (moduleConfiguration.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL)) {
			ContactList cl = new ContactList(translator.translate("recipient.external"));
			for (String externalMail : getExternalMailList(moduleConfiguration)) {
				cl.add(externalMail);
			}
			contactList.add(cl);
		}

		MailTemplate mailTemplate = formMailing.getConfirmationTemplate(courseEntry, courseNode, identity, session);
		formMailing.addFormPdfAttachment(mailTemplate, courseNode, userCourseEnv);
		formMailing.sendEmails(mailTemplate, courseEntry, courseNode, contactList, identity);
		formMailing.deleteTempDir(mailTemplate);
	}

	private ContactList getOwnersContactList(RepositoryEntry entry, Translator translator) {
		ContactList cl = new ContactList(translator.translate("recipient.owners"));
		List<Identity> identities = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
		cl.addAllIdentites(identities);
		return cl;
	}

	private ContactList getCoachesContactList(RepositoryEntry entry, Translator translator) {
		ContactList cl = new ContactList(translator.translate("recipient.coaches"));
		Collection<Identity> identities = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.coach.name());
		cl.addAllIdentites(identities);
		return cl;
	}

	private List<String> getExternalMailList(ModuleConfiguration moduleConfiguration) {
		List<String> externalMails = Stream.of(moduleConfiguration.getStringValue(
						FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL_MAILS).split(","))
				.collect(Collectors.toCollection(ArrayList<String>::new));
		externalMails.removeIf(externalMail -> !MailHelper.isValidEmailAddress(externalMail));

		return externalMails;
	}

	@Override
	public List<FormParticipation> getFormParticipations(EvaluationFormSurvey survey,
			FormParticipationSearchParams searchParams) {
		List<EvaluationFormParticipation> participations = getParticipations(survey, null, searchParams.isAdmin());
		
		List<Identity> coachedIdentities = getCoachedIdentities(searchParams);
		Set<Identity> allIdentities = new HashSet<>();
		boolean allParticipants = searchParams.getParticipants() == null || searchParams.getParticipants().isEmpty();
		if (allParticipants || searchParams.getParticipants().contains(ParticipantType.member)) {
			allIdentities.addAll(coachedIdentities);
		}
		if (allParticipants || searchParams.getParticipants().contains(ParticipantType.nonMember)) {
			List<Identity> executors = participations.stream().map(EvaluationFormParticipation::getExecutor).collect(Collectors.toList());
			executors.removeAll(coachedIdentities);
			Set<Long> fakeParticipantKeys = searchParams.getFakeParticipants() != null
					? searchParams.getFakeParticipants().stream().map(Identity::getKey).collect(Collectors.toSet())
					: Collections.emptySet();
			executors.removeIf(identity -> fakeParticipantKeys.contains(identity.getKey()));
			allIdentities.addAll(executors);
		}
		if (allParticipants || searchParams.getParticipants().contains(ParticipantType.fakeParticipant)) {
			if (searchParams.getFakeParticipants() != null) {
				allIdentities.addAll(searchParams.getFakeParticipants());
			}
		}
		
		Map<Long, EvaluationFormParticipation> identityKeyToParticipations = participations
				.stream()
				.collect(Collectors.toMap(
						participation -> participation.getExecutor().getKey(), 
						Function.identity()));
		SessionFilter doneSessionsFilter = SessionFilterFactory.createSelectDone(survey);
		Map<Long, Date> participationKeyToSubmissionDate = evaluationFormManager.loadSessionsFiltered(doneSessionsFilter, 0, -1)
				.stream()
				.collect(Collectors.toMap(
						session -> session.getParticipation().getKey(),
						EvaluationFormSession::getSubmissionDate));
		
		Map<Long, AssessmentObligation> identityKeyToObligation = searchParams.getObligations() != null && !searchParams.getObligations().isEmpty()
				? assessmentService.loadAssessmentEntriesBySubIdent( searchParams.getCourseEntry(), survey.getIdentifier().getSubident())
						.stream()
						// Allow null as value of the map
						.collect(HashMap::new, (m,ae)->m.put(ae.getIdentity().getKey(), extractObligation(ae)), HashMap::putAll)
				: Collections.emptyMap();
		
		List<FormParticipation> formParticipations = new ArrayList<>(coachedIdentities.size());
		for (Identity identity : allIdentities) {
			if (isExcludedByObligation(searchParams.getObligations(), identityKeyToObligation.get(identity.getKey()))) {
				continue;
			}
			
			EvaluationFormParticipation participation = identityKeyToParticipations.get(identity.getKey());
			if (isExcludedByStatus(searchParams.getStatus(), participation)) {
				continue;
			}
			
			FormParticipationImpl formParticipationImpl = new FormParticipationImpl();
			formParticipationImpl.setIdentity(identity);
			if (participation != null) {
				formParticipationImpl.setEvaluationFormParticipation(participation);
				if (EvaluationFormParticipationStatus.done == participation.getStatus()) {
					formParticipationImpl.setSubmissionDate(participationKeyToSubmissionDate.get(participation.getKey()));
				}
			}
			formParticipations.add(formParticipationImpl);
		}
		return formParticipations;
	}
	
	private boolean isExcludedByStatus(Collection<FormParticipationSearchParams.Status> status, EvaluationFormParticipation participation) {
		if (status == null || status.isEmpty()) {
			return false;
		}
		if (status.contains(FormParticipationSearchParams.Status.notStarted) && participation == null) {
			return false;
		}
		if (status.contains(FormParticipationSearchParams.Status.inProgress) 
				&& participation != null && EvaluationFormParticipationStatus.prepared == participation.getStatus()) {
			return false;
		}
		if (status.contains(FormParticipationSearchParams.Status.done) 
				&& participation != null && EvaluationFormParticipationStatus. done == participation.getStatus()) {
			return false;
		}
		
		return true;
	}

	private boolean isExcludedByObligation(Collection<AssessmentObligation> filterObligations, AssessmentObligation obligation) {
		if (filterObligations == null || filterObligations.isEmpty()) {
			return false;
		}
		if (obligation != null && !filterObligations.contains(obligation)) {
			return true;
		}
		if (obligation == null && !filterObligations.contains(AssessmentObligation.mandatory)) {
			return true;
		}
		return false;
	}
	
	private List<Identity> getCoachedIdentities(FormParticipationSearchParams searchParams) {
		if (searchParams.isAdmin()) {
			return repositoryService.getMembers(searchParams.getCourseEntry(), RepositoryEntryRelationType.all, GroupRoles.participant.name());
		} else if (searchParams.isCoach()) {
			return repositoryService.getCoachedParticipants(
					searchParams.getIdentity(),
					searchParams.getCourseEntry());
		}
		return Collections.emptyList();
	}
	
	private AssessmentObligation extractObligation(AssessmentEntry assessmentEntry) {
		return assessmentEntry != null && assessmentEntry.getObligation() != null
				? assessmentEntry.getObligation().getCurrent()
				: null;
	}

	@Override
	public Long getSessionsCount(SessionFilter filter) {
		return evaluationFormManager.loadSessionsCount(filter);
	}

	@Override
	public void deleteAllData(EvaluationFormSurvey survey, CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		evaluationFormManager.deleteAllData(survey);
		
		AssessmentManager assessmentManager = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		List<AssessmentEntry> assessmentEntries = assessmentManager.getAssessmentEntries(courseNode);
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			assessmentEntry.setCurrentRunCompletion(null);
			assessmentEntry.setCurrentRunStatus(null);
			assessmentEntry.setCompletion(null);
			assessmentEntry.setAssessmentStatus(null);
			assessmentEntry.setFullyAssessed(null);
			assessmentManager.updateAssessmentEntry(assessmentEntry);
		}
		
		log.info(Tracing.M_AUDIT, "All form data deleted: {}, course node {}", 
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				courseNode.getIdent());
	}

	@Override
	public EvaluationFormExcelExport getExcelExport(FormCourseNode courseNode,
			EvaluationFormSurveyIdentifier identifier, UserColumns userColumns) {
		EvaluationFormSurvey survey = loadSurvey(identifier);
		SessionFilter filter = SessionFilterFactory.createSelectDone(survey, true);
		return getExcelExport(courseNode, identifier, filter, userColumns);
	}
	
	

	@Override
	public EvaluationFormExcelExport getExcelExport(FormCourseNode courseNode,
			EvaluationFormSurveyIdentifier identifier, SessionFilter filter, UserColumns userColumns) {
		EvaluationFormSurvey survey = loadSurvey(identifier);
		Form form = loadForm(survey);
		String surveyName = courseNode.getShortName();
		return new EvaluationFormExcelExport(form, filter, null, userColumns, surveyName);
	}

	@Override
	public List<String> getRelativeToDateTypes(RepositoryEntry courseEntry) {
		return dueDateService.getCourseRelativeToDateTypes(courseEntry);
	}

	@Override
	public Date getParticipationDeadline(FormCourseNode courseNode, RepositoryEntry courseEntry, Identity identity) {
		DueDateConfig dueDateConfig = courseNode.getDueDateConfig(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE);
		return dueDateService.getDueDate(dueDateConfig, courseEntry, identity);
	}

}
