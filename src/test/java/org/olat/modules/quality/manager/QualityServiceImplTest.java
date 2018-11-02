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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityDataCollection;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityServiceImplTest {
	
	@Mock
	private EvaluationFormManager evaluationFormManagerMock;
	@Mock
	private EvaluationFormSurvey surveyMock;
	@Mock
	private QualityContextDAO contextDaoMock;
	@Mock
	private QualityContextToCurriculumDAO contextToCurriculumDaoMock;
	@Mock
	private QualityContextToCurriculumElementDAO contextToCurriculumElementDaoMock;
	@Mock
	private QualityContextToOrganisationDAO contextToOrganisationDaoMock;
	@Mock
	private QualityContextToTaxonomyLevelDAO contextToTaxonomyLevelDaoMock;
	
	@InjectMocks
	private QualityServiceImpl sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(evaluationFormManagerMock.loadSurvey(any(), any())).thenReturn(surveyMock);
	}
	
	@Test
	public void shouldAddParticipationIfNotExists() {
		QualityDataCollection dataCollectionMock = mock(QualityDataCollection.class);
		Identity executorMock = mock(Identity.class);
		List<Identity> executors = Arrays.asList(executorMock);
		when(evaluationFormManagerMock.loadParticipationByExecutor(surveyMock, executorMock)).thenReturn(null);
		
		sut.addParticipations(dataCollectionMock, executors);
		
		verify(evaluationFormManagerMock).createParticipation(surveyMock, executorMock, true);
	}

	@Test
	public void shouldNotAddParticipationIfExists() {
		QualityDataCollection dataCollectionMock = mock(QualityDataCollection.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		Identity executorMock = mock(Identity.class);
		List<Identity> executors = Arrays.asList(executorMock);
		when(evaluationFormManagerMock.loadParticipationByExecutor(surveyMock, executorMock)).thenReturn(participationMock);
		
		sut.addParticipations(dataCollectionMock, executors);
		
		verify(evaluationFormManagerMock, never()).createParticipation(surveyMock, executorMock);
	}
	
	@Test
	public void shouldReturnAllParticipationsOfTheExecutors() {
		QualityDataCollection dataCollectionMock = mock(QualityDataCollection.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		Identity executorWithParticipationMock = mock(Identity.class);
		Identity executorWithoutParticipationMock = mock(Identity.class);
		List<Identity> executors = Arrays.asList(executorWithParticipationMock, executorWithoutParticipationMock);
		when(evaluationFormManagerMock.loadParticipationByExecutor(surveyMock, executorWithParticipationMock))
				.thenReturn(participationMock);
		when(evaluationFormManagerMock.loadParticipationByExecutor(surveyMock, executorWithoutParticipationMock))
				.thenReturn(null);

		List<EvaluationFormParticipation> participations = sut.addParticipations(dataCollectionMock, executors);

		assertThat(participations).hasSize(2);
	}
	
	@Test
	public void shouldDeleteContextAndRelations() {
		QualityContext context = mock(QualityContext.class);
		
		sut.deleteContext(context);
		
		verify(contextDaoMock).deleteContext(context);
		verify(contextToCurriculumDaoMock).deleteRelations(context);
		verify(contextToCurriculumElementDaoMock).deleteRelations(context);
		verify(contextToOrganisationDaoMock).deleteRelations(context);
		verify(contextToTaxonomyLevelDaoMock).deleteRelations(context);
	}
	
	@Test
	public void shouldDeleteParticipationIfHasNoContext() {
		QualityContext context = mock(QualityContext.class);
		EvaluationFormParticipation participation = mock(EvaluationFormParticipation.class);
		when(context.getEvaluationFormParticipation()).thenReturn(participation);
		when(contextDaoMock.loadByKey(context)).thenReturn(context);
		when(contextDaoMock.hasContexts(participation)).thenReturn(false);
		
		Collection<QualityContextRef> contextRefs = Collections.singletonList(context);
		sut.deleteContextsAndParticipations(contextRefs);

		verify(evaluationFormManagerMock).deleteParticipations(any());
	}

	@Test
	public void shouldNotDeleteParticipationIfHasContexts() {
		QualityContext context = mock(QualityContext.class);
		EvaluationFormParticipation participation = mock(EvaluationFormParticipation.class);
		when(context.getEvaluationFormParticipation()).thenReturn(participation);
		when(contextDaoMock.loadByKey(context)).thenReturn(context);
		when(contextDaoMock.hasContexts(participation)).thenReturn(true);
		
		Collection<QualityContextRef> contextRefs = Collections.singletonList(context);
		sut.deleteContextsAndParticipations(contextRefs);

		verify(evaluationFormManagerMock, never()).deleteParticipations(any());
	}
	
}
