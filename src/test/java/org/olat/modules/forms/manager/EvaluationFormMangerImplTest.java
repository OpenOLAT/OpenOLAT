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

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 03.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormMangerImplTest {
	
	@Mock
	private EvaluationFormSurveyDAO surveyDaoMock;
	@Mock
	private EvaluationFormParticipationDAO participationDaoMock;
	@Mock
	private EvaluationFormSessionDAO sessionDaoMock;

	
	@InjectMocks
	private EvaluationFormManagerImpl sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldUpdateFormOfSurveyIfItNoSessions() {
		EvaluationFormSurvey surveyMock = mock(EvaluationFormSurvey.class);
		RepositoryEntry formEntryMock = mock(RepositoryEntry.class);
		when(sessionDaoMock.hasSessions(surveyMock)).thenReturn(Boolean.FALSE);
		
		sut.updateSurveyForm(surveyMock, formEntryMock);
		
		verify(surveyDaoMock).updateForm(surveyMock, formEntryMock);
	}
	
	@Test
	public void shouldNotUpdateFormOfSurveyIfItHasSessions() {
		EvaluationFormSurvey surveyMock = mock(EvaluationFormSurvey.class);
		RepositoryEntry formEntryMock = mock(RepositoryEntry.class);
		when(sessionDaoMock.hasSessions(surveyMock)).thenReturn(Boolean.TRUE);
		
		sut.updateSurveyForm(surveyMock, formEntryMock);
		
		verify(surveyDaoMock, never()).updateForm(surveyMock, formEntryMock);
	}
	
	@Test
	public void shouldMakeSessionDoneWhenFinishingSession() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		when(sessionDaoMock.loadSessionByKey(sessionMock)).thenReturn(sessionMock);
		
		sut.finishSession(sessionMock);

		verify(sessionDaoMock).changeStatus(sessionMock, EvaluationFormSessionStatus.done);
	}
	
	@Test
	public void shouldMakeParticipationDoneWhenFinishingSession() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		when(sessionMock.getParticipation()).thenReturn(participationMock);
		when(participationDaoMock.changeStatus(participationMock, EvaluationFormParticipationStatus.done)).then(returnsFirstArg());
		when(sessionDaoMock.loadSessionByKey(sessionMock)).thenReturn(sessionMock);
		
		sut.finishSession(sessionMock);

		verify(participationDaoMock).changeStatus(participationMock, EvaluationFormParticipationStatus.done);
	}
	
	@Test
	public void shouldMakeSessionAnonymousWhenFinishingSessionIfParticipationIsAnonymous() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		when(participationMock.isAnonymous()).thenReturn(Boolean.TRUE);
		when(sessionMock.getParticipation()).thenReturn(participationMock);
		when(participationDaoMock.changeStatus(participationMock, EvaluationFormParticipationStatus.done)).then(returnsFirstArg());
		when(sessionDaoMock.loadSessionByKey(sessionMock)).thenReturn(sessionMock);
		
		sut.finishSession(sessionMock);

		verify(sessionDaoMock).makeAnonymous(sessionMock);
	}
	
	@Test
	public void shouldMakeSessionNotAnonymousWhenFinishingSessionIfParticipationIsNotAnonymous() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		when(participationMock.isAnonymous()).thenReturn(Boolean.FALSE);
		when(sessionMock.getParticipation()).thenReturn(participationMock);
		when(participationDaoMock.changeStatus(participationMock, EvaluationFormParticipationStatus.done)).then(returnsFirstArg());
		when(sessionDaoMock.loadSessionByKey(sessionMock)).thenReturn(sessionMock);
		
		sut.finishSession(sessionMock);

		verify(sessionDaoMock, never()).makeAnonymous(sessionMock);
	}
	
	@Test
	public void shouldMakeSessionInProgressWhenReopeningSession() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		when(sessionMock.getParticipation()).thenReturn(participationMock);
		when(sessionDaoMock.loadSessionByKey(sessionMock)).thenReturn(sessionMock);
		
		sut.reopenSession(sessionMock);

		verify(sessionDaoMock).changeStatus(sessionMock, EvaluationFormSessionStatus.inProgress);
	}

	@Test
	public void shouldMakeParticipationPreparedWhenReopeningSession() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		when(sessionMock.getParticipation()).thenReturn(participationMock);
		
		sut.reopenSession(sessionMock);

		verify(participationDaoMock).changeStatus(participationMock, EvaluationFormParticipationStatus.prepared);
	}

	@Test
	public void shouldNotReopenWhenIsAnonymousSession() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		when(sessionMock.getParticipation()).thenReturn(null);
		
		sut.reopenSession(sessionMock);

		verify(sessionDaoMock, never()).changeStatus(sessionMock, EvaluationFormSessionStatus.inProgress);
		verify(participationDaoMock, never()).changeStatus(participationMock, EvaluationFormParticipationStatus.prepared);
	}

}
