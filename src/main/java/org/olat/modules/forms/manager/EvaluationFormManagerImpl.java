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
package org.olat.modules.forms.manager;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormStatistic;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormManagerImpl implements EvaluationFormManager {
	
	@Autowired
	private EvaluationFormSurveyDAO evaluationFormSurveyDao;
	@Autowired
	private EvaluationFormParticipationDAO evaluationFormParticipationDao;
	@Autowired
	private EvaluationFormSessionDAO evaluationFormSessionDao;
	@Autowired
	private EvaluationFormResponseDAO evaluationFormResponseDao;
	@Autowired
	private EvaluationFormStorage evaluationFormStorage;
	
	@Override
	public EvaluationFormSurvey createSurvey(OLATResourceable ores, String subIdent, RepositoryEntry formEntry) {
		return evaluationFormSurveyDao.createSurvey(ores, subIdent, formEntry);
	}

	@Override
	public EvaluationFormSurvey loadSurvey(OLATResourceable ores, String subIdent) {
		return evaluationFormSurveyDao.loadByResourceable(ores, subIdent);
	}

	@Override
	public boolean isFormUpdateable(EvaluationFormSurvey survey) {
		return !evaluationFormSessionDao.hasSessions(survey);
	}

	@Override
	public EvaluationFormSurvey updateSurveyForm(EvaluationFormSurvey survey, RepositoryEntry formEntry) {
		if (isFormUpdateable(survey)) {
			return evaluationFormSurveyDao.updateForm(survey, formEntry);
		}
		return survey;
	}

	@Override
	public void deleteAllData(EvaluationFormSurvey survey) {
		if (survey == null) return;
		
		List<EvaluationFormResponse> responses = evaluationFormResponseDao.loadResponsesBySurvey(survey);
		deleteResponses(responses);
		evaluationFormSessionDao.deleteSessions(survey);
		evaluationFormParticipationDao.deleteParticipations(survey);
	}
	
	@Override
	public void deleteSurvey(EvaluationFormSurvey survey) {
		if (survey == null) return;
		deleteAllData(survey);
		evaluationFormSurveyDao.delete(survey);
	}

	@Override
	public EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey) {
		return evaluationFormParticipationDao.createParticipation(survey, new EvaluationFormParticipationIdentifier(),
				false, null);
	}

	@Override
	public EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey, Identity executor) {
		return evaluationFormParticipationDao.createParticipation(survey, new EvaluationFormParticipationIdentifier(),
				false, executor);
	}
	
	@Override
	public EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey, Identity executor, boolean anonymous) {
		return evaluationFormParticipationDao.createParticipation(survey, new EvaluationFormParticipationIdentifier(),
				anonymous, executor);
	}

	@Override
	public EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey,
			EvaluationFormParticipationIdentifier identifier) {
		return evaluationFormParticipationDao.createParticipation(survey, identifier, false, null);
	}

	@Override
	public EvaluationFormParticipation updateParticipation(EvaluationFormParticipation participation) {
		return evaluationFormParticipationDao.updateParticipation(participation);
	}

	@Override
	public EvaluationFormParticipation loadParticipationByExecutor(EvaluationFormSurvey survey, IdentityRef executor) {
		return evaluationFormParticipationDao.loadByExecutor(survey, executor);
	}

	@Override
	public EvaluationFormParticipationRef loadParticipationByIdentifier(EvaluationFormParticipationIdentifier identifier) {
		return evaluationFormParticipationDao.loadByIdentifier(identifier);
	}

	@Override
	public EvaluationFormParticipation loadParticipationByIdentifier(EvaluationFormSurvey survey,
			EvaluationFormParticipationIdentifier identifier) {
		return evaluationFormParticipationDao.loadByIdentifier(survey, identifier);
	}

	@Override
	public void deleteParticipations(List<? extends EvaluationFormParticipationRef> participationRefs) {
		if (participationRefs == null || participationRefs.isEmpty()) return;
		
		List<EvaluationFormResponse> responses = evaluationFormResponseDao.loadResponsesByParticipations(participationRefs);
		deleteResponses(responses);
		evaluationFormSessionDao.deleteSessions(participationRefs);
		evaluationFormParticipationDao.deleteParticipations(participationRefs);
	}

	@Override
	public EvaluationFormSession createSession(EvaluationFormParticipation participation) {
		return evaluationFormSessionDao.createSession(participation);
	}

	@Override
	public EvaluationFormSession loadSessionByKey(EvaluationFormSessionRef sessionRef) {
		return evaluationFormSessionDao.loadSessionByKey(sessionRef);
	}

	@Override
	public EvaluationFormSession loadSessionByParticipation(EvaluationFormParticipationRef participation) {
		return evaluationFormSessionDao.loadSessionByParticipation(participation);
	}

	@Override
	public List<EvaluationFormSession> loadSessionsByKey(List<? extends EvaluationFormSessionRef> sessionRefs,
			int firstResult, int maxResults, SortKey... orderBy) {
		return evaluationFormSessionDao.loadSessionsByKey(sessionRefs, firstResult, maxResults, orderBy);
	}

	@Override
	public List<EvaluationFormSession> loadSessionsBySurvey(EvaluationFormSurvey survey,
			EvaluationFormSessionStatus status) {
		return evaluationFormSessionDao.loadSessionsBySurvey(survey, status);
	}

	@Override
	public EvaluationFormSession updateSession(EvaluationFormSession session, String email, String firstname, String lastname,
			String age, String gender, String orgUnit, String studySubject) {
		return evaluationFormSessionDao.updateSession(session, email, firstname, lastname, age, gender, orgUnit, studySubject);
	}

	@Override
	public EvaluationFormSession finishSession(EvaluationFormSessionRef sessionRef) {
		if (sessionRef == null) return null;
		EvaluationFormSession sesssion = evaluationFormSessionDao.loadSessionByKey(sessionRef);
		EvaluationFormParticipation participation = sesssion.getParticipation();
		if (participation != null) {
			participation = evaluationFormParticipationDao.changeStatus(participation, EvaluationFormParticipationStatus.done);
			if (participation.isAnonymous()) {
				sesssion = evaluationFormSessionDao.makeAnonymous(sesssion);
			}
		}
		sesssion = evaluationFormSessionDao.changeStatus(sesssion, EvaluationFormSessionStatus.done);
		return sesssion;
	}
	
	@Override
	public EvaluationFormSession reopenSession(EvaluationFormSession session) {
		if (session == null) return null;
		EvaluationFormSession finishedSesssion = session;
		EvaluationFormParticipation participation = session.getParticipation();
		if (participation != null) {
			participation = evaluationFormParticipationDao.changeStatus(participation, EvaluationFormParticipationStatus.prepared);
			finishedSesssion = evaluationFormSessionDao.changeStatus(finishedSesssion, EvaluationFormSessionStatus.inProgress);
		}
		return finishedSesssion;
	}

	@Override
	public long getCountOfSessions(EvaluationFormSurvey survey) {
		return evaluationFormSessionDao.getCountOfSessions(survey);
	}

	@Override
	public EvaluationFormResponse createFileResponse(String responseIdentifier, EvaluationFormSession session,
			File file, String filename) throws IOException {
		Path relativePath = evaluationFormStorage.save(file, filename);
		return evaluationFormResponseDao.createResponse(responseIdentifier, null, filename, relativePath,
				session);
	}

	@Override
	public EvaluationFormResponse createStringResponse(String responseIdentifier, EvaluationFormSession session,
			String value) {
		return evaluationFormResponseDao.createResponse(responseIdentifier, null, value, null, session);
	}

	@Override
	public EvaluationFormResponse createNumericalResponse(String responseIdentifier, EvaluationFormSession session,
			BigDecimal value) {
		return evaluationFormResponseDao.createResponse(responseIdentifier, value, value.toPlainString(), null, session);
	}

	@Override
	public EvaluationFormResponse createNoResponse(String responseIdentifier, EvaluationFormSession session) {
		return evaluationFormResponseDao.createNoResponse(responseIdentifier, session);
	}

	@Override
	public EvaluationFormResponse updateNumericalResponse(EvaluationFormResponse response, BigDecimal value) {
		return evaluationFormResponseDao.updateResponse(value, value.toPlainString(), null, response);
	}

	@Override
	public EvaluationFormResponse updateFileResponse(EvaluationFormResponse response, File file,
			String filename) throws IOException {
		if (response.getFileResponse() != null) {
			evaluationFormStorage.delete(response.getFileResponse());
		}
		Path relativePath = null;
		String filenameToSave = null;
		if (file != null) {
			relativePath = evaluationFormStorage.save(file, filename);
			filenameToSave = filename;
		}
		return evaluationFormResponseDao.updateResponse(null, filenameToSave, relativePath, response);
	}

	@Override
	public EvaluationFormResponse updateStringResponse(EvaluationFormResponse response, String stringValue) {
		return evaluationFormResponseDao.updateResponse(null, stringValue, null, response);
	}

	@Override
	public EvaluationFormResponse updateNoResponse(EvaluationFormResponse response) {
		return evaluationFormResponseDao.updateNoResponse(response);
	}

	@Override
	public EvaluationFormResponses loadResponsesBySessions(List<? extends EvaluationFormSessionRef> sessionRefs) {
		List<EvaluationFormResponse> responses = evaluationFormResponseDao.loadResponsesBySessions(sessionRefs);
		return new EvaluationFormResponses(responses);
	}

	@Override
	public File loadResponseFile(EvaluationFormResponse response) {
		if (response == null || response.getFileResponse() == null) return null;
		
		return evaluationFormStorage.load(response.getFileResponse());
	}

	@Override
	public VFSLeaf loadResponseLeaf(EvaluationFormResponse response) {
		if (response == null || response.getFileResponse() == null) return null;
		
		return evaluationFormStorage.resolve(response.getFileResponse());
	}

	@Override
	public void deleteResponse(EvaluationFormResponse response) {
		deleteResponses(Collections.singletonList(response));
	}

	@Override
	public void deleteResponses(List<EvaluationFormResponse> responses) {
		List<Long> responseKeys = new ArrayList<>();
		for (EvaluationFormResponse response: responses) {
			responseKeys.add(response.getKey());
			Path fileResponse = response.getFileResponse();
			if (fileResponse != null) {
				evaluationFormStorage.delete(response.getFileResponse());
			}
		}
		evaluationFormResponseDao.deleteResponses(responseKeys);
	}

	@Override
	public boolean isEvaluationFormActivelyUsed(RepositoryEntryRef formEntry) {
		return evaluationFormSessionDao.hasSessions(formEntry);
	}

	@Override
	public EvaluationFormStatistic getSessionsStatistic(List<? extends EvaluationFormSessionRef> sessionRefs) {
		EvaluationFormStatistic statistic = new EvaluationFormStatistic();
		
		List<EvaluationFormSession> sessions = evaluationFormSessionDao.loadSessionsByKey(sessionRefs, 0, -1);
		int numOfDoneSessions = 0;
		Date firstSubmission = null;
		Date lastSubmission = null;
		for (EvaluationFormSession session: sessions) {
			if (EvaluationFormSessionStatus.done.equals(session.getEvaluationFormSessionStatus())) {
				numOfDoneSessions++;
				
				if (firstSubmission == null || firstSubmission.after(session.getFirstSubmissionDate())) {
					firstSubmission = session.getFirstSubmissionDate();
				}
				
				if (lastSubmission == null || lastSubmission.before(session.getSubmissionDate())) {
					lastSubmission = session.getSubmissionDate();
				}
			}
		}
		statistic.setNumOfDoneSessions(numOfDoneSessions);
		statistic.setFirstSubmission(firstSubmission);
		statistic.setLastSubmission(lastSubmission);
		
		long[] durations = new long[numOfDoneSessions];
		int durationsIndex = 0;
		long totalDuration = 0;
		for (EvaluationFormSession session: sessions) {
			if (EvaluationFormSessionStatus.done.equals(session.getEvaluationFormSessionStatus())) {
				long duration = session.getSubmissionDate().getTime() - session.getCreationDate().getTime();
				durations[durationsIndex++] = duration;
				totalDuration += duration;
			}
		}
		statistic.setDurations(durations);
		
		long averageDuration = numOfDoneSessions > 0? Math.round(totalDuration / numOfDoneSessions): 0;
		statistic.setAverageDuration(averageDuration);
		
		return statistic;
	}

}
