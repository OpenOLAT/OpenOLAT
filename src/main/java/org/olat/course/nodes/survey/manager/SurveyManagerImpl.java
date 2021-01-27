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
package org.olat.course.nodes.survey.manager;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.survey.SurveyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10 Sep 2019<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SurveyManagerImpl implements SurveyManager {

	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	@Override
	public EvaluationFormSurveyIdentifier getSurveyIdentifier(SurveyCourseNode surveyCourseNode,
			RepositoryEntry courseEntry) {
		String subIdent = surveyCourseNode.getIdent();
		return EvaluationFormSurveyIdentifier.of(courseEntry, subIdent);
	}

	@Override
	public EvaluationFormSurvey loadSurvey(EvaluationFormSurveyIdentifier surveyIdent) {
		return evaluationFormManager.loadSurvey(surveyIdent);
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
	public EvaluationFormParticipation loadOrCreateGuestParticipation(EvaluationFormSurvey survey, UserSession usess) {
		String anonymousIdentifier = getAnonymousIdentifier(usess);
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier("course-node",
				anonymousIdentifier);
		return loadOrCreateParticipation(survey, identifier);
	}

	private String getAnonymousIdentifier(UserSession usess) {
		String sessionId = usess.getSessionInfo().getSession().getId();
		Object id = usess.getEntry(sessionId);
		if (id instanceof String) {
			return (String) id;
		}

		String newId = UUID.randomUUID().toString();
		usess.putEntryInNonClearedStore(sessionId, newId);
		return newId;
	}

	public EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSurvey survey,
			EvaluationFormParticipationIdentifier identifier) {
		EvaluationFormParticipation loadedParticipation = evaluationFormManager.loadParticipationByIdentifier(survey, identifier);
		if (loadedParticipation == null) {
			loadedParticipation = evaluationFormManager.createParticipation(survey, identifier);
			loadedParticipation.setAnonymous(true);
			loadedParticipation = evaluationFormManager.updateParticipation(loadedParticipation);
		}
		return loadedParticipation;
	}

	@Override
	public EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSurvey survey, Identity executor) {
		EvaluationFormParticipation loadedParticipation = evaluationFormManager.loadParticipationByExecutor(survey, executor);
		if (loadedParticipation == null) {
			loadedParticipation = evaluationFormManager.createParticipation(survey, executor);
			loadedParticipation.setAnonymous(true);
			loadedParticipation = evaluationFormManager.updateParticipation(loadedParticipation);
		}
		return loadedParticipation;
	}

	@Override
	public EvaluationFormSession loadOrCreateSesssion(EvaluationFormParticipation participation) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}

	@Override
	public void onQuickSave(SurveyCourseNode courseNode, UserCourseEnvironment userCourseEnv, Double competion) {
		courseAssessmentService.updateCompletion(courseNode, userCourseEnv, competion, AssessmentEntryStatus.inProgress,
				Role.user);
	}

	@Override
	public void onExecutionFinished(SurveyCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		courseAssessmentService.incrementAttempts(courseNode, userCourseEnv, Role.user);
		courseAssessmentService.updateCompletion(courseNode, userCourseEnv, Double.valueOf(1),
				AssessmentEntryStatus.done, Role.user);
	}

	@Override
	public Long getSessionsCount(SessionFilter filter) {
		return evaluationFormManager.loadSessionsCount(filter);
	}

	@Override
	public void deleteAllData(EvaluationFormSurvey survey, SurveyCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
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

	}

}
