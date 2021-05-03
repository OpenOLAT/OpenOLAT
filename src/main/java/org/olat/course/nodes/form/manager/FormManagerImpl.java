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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
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
	public File getFormFile(EvaluationFormSurvey survey) {
		RepositoryEntry formEntry = survey.getFormEntry();
		File repositoryDir = new File(
				FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()),
				FileResourceManager.ZIPDIR);
		return new File(repositoryDir, FORM_XML_FILE);
	}

	@Override
	public DataStorage loadStorage(EvaluationFormSurvey survey) {
		RepositoryEntry formEntry = survey.getFormEntry();
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
	public void reopenParticipation(EvaluationFormParticipation participation) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		evaluationFormManager.reopenSession(session);
	}
	
	@Override
	public void deleteParticipation(EvaluationFormParticipation participation, FormCourseNode courseNode,
			UserCourseEnvironment userCourseEnv) {
		evaluationFormManager.deleteParticipations(Collections.singletonList(participation));
		
		AssessmentManager assessmentManager = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		AssessmentEntry assessmentEntry = assessmentManager.getAssessmentEntry(courseNode, participation.getExecutor());
		if (assessmentEntry != null) {
			assessmentEntry.setCurrentRunCompletion(null);
			assessmentEntry.setCurrentRunStatus(null);
			assessmentEntry.setCompletion(null);
			assessmentEntry.setAssessmentStatus(null);
			assessmentEntry.setFullyAssessed(null);
			assessmentManager.updateAssessmentEntry(assessmentEntry);
		}
		
		log.info(Tracing.M_AUDIT, "Form data deleted: {}, course node {}, participant {}", 
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				courseNode.getIdent(), participation.getExecutor());
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
	public List<EvaluationFormSession> getDoneSessions(EvaluationFormSurvey survey) {
		SessionFilter filter = SessionFilterFactory.createSelectDone(survey);
		return evaluationFormManager.loadSessionsFiltered(filter, 0, -1);
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
		
		if (courseNode.getModuleConfiguration().getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_ENABLED)) {
			sendConfirmationEmail(courseNode, userCourseEnv);
		}
		
		log.info(Tracing.M_AUDIT, "Form filled in: {}, course node {}", 
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				courseNode.getIdent());
	}

	private void sendConfirmationEmail(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		EvaluationFormSurveyIdentifier surveyIdent = getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = loadSurvey(surveyIdent);
		EvaluationFormParticipation participation = loadParticipation(survey, identity);
		EvaluationFormSession session = participation != null? loadOrCreateSession(participation): null;
		
		MailTemplate mailTemplate = formMailing.getConfirmationTemplate(courseEntry, courseNode, identity, session);
		formMailing.addFormPdfAttachment(mailTemplate, courseNode, userCourseEnv);
		formMailing.sendEmail(mailTemplate, courseEntry, courseNode, identity);
		formMailing.deleteTempDir(mailTemplate);
	}

	@Override
	public List<Identity> getCoachedIdentities(UserCourseEnvironment userCourseEnv) {
		if (userCourseEnv.isAdmin()) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			return repositoryService.getMembers(courseEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name())
					.stream()
					.distinct()
					.collect(Collectors.toList());
		} else if (userCourseEnv.isCoach()) {
			return repositoryService.getCoachedParticipants(
					userCourseEnv.getIdentityEnvironment().getIdentity(),
					userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		}
		return Collections.emptyList();
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
		Form form = loadForm(survey);
		String surveyName = courseNode.getShortName();
		return new EvaluationFormExcelExport(form, filter, null, userColumns, surveyName);
	}

}
