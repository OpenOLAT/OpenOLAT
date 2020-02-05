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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.ui.model.CountRatioResult;
import org.olat.modules.forms.ui.model.CountResult;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 03.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormMangerImplTest {
	
	private static final double LOWER_BOUND_INSUFFICIENT = 1.0;
	private static final double UPPER_BOUND_INSUFFICIENT = 4.0;
	private static final double LOWER_BOUND_SUFFICIENT = 4.5;
	private static final double UPPER_BOUND_SUFFICIENT = 6.0;
	private static final double LOWER_BOUND_NEUTRAL = UPPER_BOUND_INSUFFICIENT;
	private static final double UPPER_BOUND_NEUTRAL = LOWER_BOUND_SUFFICIENT;
	
	@Mock
	private EvaluationFormSurveyDAO surveyDaoMock;
	@Mock
	private EvaluationFormParticipationDAO participationDaoMock;
	@Mock
	private EvaluationFormSessionDAO sessionDaoMock;
	@Mock
	private EvaluationFormResponseDAO responseDao;
	@Mock
	private SessionStatusPublisher sessionStatusPublisherMock;

	
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
	public void shouldNotUpdateFormOfSurveyIfItPartOfASeries() {
		EvaluationFormSurvey previousMock = mock(EvaluationFormSurvey.class);
		EvaluationFormSurvey surveyMock = mock(EvaluationFormSurvey.class);
		RepositoryEntry formEntryMock = mock(RepositoryEntry.class);
		when(surveyMock.getSeriesPrevious()).thenReturn(previousMock);
		
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
	public void shouldPublishStatusWhenFinishingSession() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		when(sessionDaoMock.loadSessionByKey(sessionMock)).thenReturn(sessionMock);
		
		sut.finishSession(sessionMock);

		verify(sessionStatusPublisherMock).onFinish(any());
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
	
	@Test
	public void shouldPublishStatusWhenReopenSession() {
		EvaluationFormSession sessionMock = mock(EvaluationFormSession.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		when(sessionMock.getParticipation()).thenReturn(participationMock);
		
		sut.reopenSession(sessionMock);

		verify(sessionStatusPublisherMock).onReopen(any());
	}

	@Test
	public void shouldRateHigherThanUpperSufficient() {
		assertRubricRating(UPPER_BOUND_SUFFICIENT + 1, RubricRating.NOT_RATED);
	}

	@Test
	public void shouldRateUpperAsSufficient() {
		assertRubricRating(UPPER_BOUND_SUFFICIENT, RubricRating.SUFFICIENT);
	}

	@Test
	public void shouldRateInsideSufficient() {
		assertRubricRating(LOWER_BOUND_SUFFICIENT + 0.1, RubricRating.SUFFICIENT);
	}

	@Test
	public void shouldRateLowerAsSufficient() {
		assertRubricRating(LOWER_BOUND_SUFFICIENT, RubricRating.SUFFICIENT);
	}
	
	@Test
	public void shouldRateUpperNeutralAsSufficient() {
		assertRubricRating(UPPER_BOUND_NEUTRAL, RubricRating.SUFFICIENT);
	}

	@Test
	public void shouldRateInsideNeutral() {
		assertRubricRating(LOWER_BOUND_NEUTRAL + 0.1, RubricRating.NEUTRAL);
	}
	
	@Test
	public void shouldLowerAsInside() {
		assertRubricRating(LOWER_BOUND_NEUTRAL, RubricRating.NEUTRAL);
	}
	@Test
	public void shouldRateUpperInsufficientAsNeutral() {
		assertRubricRating(UPPER_BOUND_INSUFFICIENT, RubricRating.NEUTRAL);
	}

	@Test
	public void shouldRateInsideInsufficient() {
		assertRubricRating(LOWER_BOUND_INSUFFICIENT + 0.5, RubricRating.INSUFFICIENT);
	}

	@Test
	public void shouldRateLowerAsInsufficient() {
		assertRubricRating(LOWER_BOUND_INSUFFICIENT, RubricRating.INSUFFICIENT);
	}

	@Test
	public void shouldRateLoweThanInsufficient() {
		assertRubricRating(LOWER_BOUND_INSUFFICIENT - 1, RubricRating.NOT_RATED);
	}

	public void assertRubricRating(Double value, RubricRating expectedRating) {
		Rubric rubric = new Rubric();
		rubric.setLowerBoundInsufficient(LOWER_BOUND_INSUFFICIENT);
		rubric.setUpperBoundInsufficient(UPPER_BOUND_INSUFFICIENT);
		rubric.setLowerBoundNeutral(LOWER_BOUND_NEUTRAL);
		rubric.setUpperBoundNeutral(UPPER_BOUND_NEUTRAL);
		rubric.setLowerBoundSufficient(LOWER_BOUND_SUFFICIENT);
		rubric.setUpperBoundSufficient(UPPER_BOUND_SUFFICIENT);
		
		RubricRating rating = sut.getRubricRating(rubric, value);
			
		assertThat(rating).isEqualTo(expectedRating);
	}
	
	@Test
	public void shouldCalculateRatioList() {
		List<CountResult> countResults = new ArrayList<>();
		countResults.add(new CountResult("1", 5));
		countResults.add(new CountResult("2", 5));
		countResults.add(new CountResult("3", 10));
		countResults.add(new CountResult("4", 0));
		
		List<CountRatioResult> ratios = sut.calculateRatio(countResults);

		Map<String, Double> percentMap = ratios.stream()
				.collect(Collectors.toMap(CountRatioResult::getName, CountRatioResult::getRatio));
		assertThat(percentMap.get("1")).isEqualTo(0.25);
		assertThat(percentMap.get("2")).isEqualTo(0.25);
		assertThat(percentMap.get("3")).isEqualTo(0.5);
		assertThat(percentMap.get("4")).isEqualTo(0);
	}



}
