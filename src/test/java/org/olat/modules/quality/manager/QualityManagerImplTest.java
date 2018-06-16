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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import org.olat.modules.quality.QualityDataCollection;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityManagerImplTest {
	
	@Mock
	private EvaluationFormManager evaluationFormManagerMock;
	@Mock
	private EvaluationFormSurvey surveyMock;
	
	@InjectMocks
	private QualityManagerImpl sut;
	
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
		
		sut.addParticipants(dataCollectionMock, executors);
		
		verify(evaluationFormManagerMock).createParticipation(surveyMock, executorMock, true);
	}

	@Test
	public void shouldNotAddParticipationIfExists() {
		QualityDataCollection dataCollectionMock = mock(QualityDataCollection.class);
		EvaluationFormParticipation participationMock = mock(EvaluationFormParticipation.class);
		Identity executorMock = mock(Identity.class);
		List<Identity> executors = Arrays.asList(executorMock);
		when(evaluationFormManagerMock.loadParticipationByExecutor(surveyMock, executorMock)).thenReturn(participationMock);
		
		sut.addParticipants(dataCollectionMock, executors);
		
		verify(evaluationFormManagerMock, never()).createParticipation(surveyMock, executorMock);
	}

}
