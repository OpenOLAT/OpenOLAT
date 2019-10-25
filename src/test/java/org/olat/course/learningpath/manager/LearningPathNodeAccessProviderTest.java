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
package org.olat.course.learningpath.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.olat.course.learningpath.LearningPathConfigs.fullyAssessed;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathConfigs.FullyAssessedResult;
import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 10 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNodeAccessProviderTest {
	
	@Mock
	private CourseNode courseNodeMock;
	@Mock
	private LearningPathConfigs configMock;
	@Mock
	private LearningPathRegistry registry;
	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	@InjectMocks
	private LearningPathNodeAccessProvider sut;
	
	private UserCourseEnvironment participantCourseEnv;
	private UserCourseEnvironment coachCourseEnv;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		participantCourseEnv = mock(UserCourseEnvironment.class);
		when(participantCourseEnv.isParticipant()).thenReturn(Boolean.TRUE);
		
		coachCourseEnv = mock(UserCourseEnvironment.class);
		when(coachCourseEnv.isCoach()).thenReturn(Boolean.TRUE);

		LearningPathNodeHandler handlerMock = mock(LearningPathNodeHandler.class);
		when(handlerMock.getConfigs(courseNodeMock)).thenReturn(configMock);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handlerMock);
	}
	
	@Test
	public void shouldSetFullyAssessedIfNodeVisited() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isFullyAssessedOnNodeVisited()).thenReturn(fullyAssessed(true, true, true));
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onNodeVisited(courseNodeMock, participantCourseEnv);

		verify(courseAssessmentService).updateFullyAssessed(courseNodeMock, participantCourseEnv, Boolean.TRUE,
				AssessmentEntryStatus.done, Role.user);
	}
	
	@Test
	public void shouldNotSetFullyAssessedIfNodeVisitedNotEnabled() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isFullyAssessedOnNodeVisited()).thenReturn(fullyAssessed(false, true, true));
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onNodeVisited(courseNodeMock, participantCourseEnv);

		verify(courseAssessmentService, never()).updateFullyAssessed(courseNodeMock, participantCourseEnv, Boolean.TRUE,
				AssessmentEntryStatus.done, Role.user);
	}
	
	@Test
	public void shouldNotSetFullyAssessedIfNotAParticipantVisitedTheNode() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isFullyAssessedOnNodeVisited()).thenReturn(fullyAssessed(true, true, true));
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onNodeVisited(courseNodeMock, coachCourseEnv);

		verify(courseAssessmentService, never()).updateFullyAssessed(courseNodeMock, participantCourseEnv, Boolean.TRUE,
				AssessmentEntryStatus.done, Role.user);
	}
	
	@Test
	public void shouldReturnConfirmEnabled() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isFullyAssessedOnConfirmation()).thenReturn(fullyAssessed(true, true, true));
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		boolean enabled = sut.isAssessmentConfirmationEnabled(courseNodeMock, participantCourseEnv);

		assertThat(enabled).isTrue();
	}
	
	@Test
	public void shouldNotReturnConfirmEnabledNotEnabledInConfiguration() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isFullyAssessedOnConfirmation()).thenReturn(fullyAssessed(false, true, true));
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		boolean enabled = sut.isAssessmentConfirmationEnabled(courseNodeMock, participantCourseEnv);

		assertThat(enabled).isFalse();
	}
	
	@Test
	public void shouldNotReturnConfirmEnabledNotAParticipant() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isFullyAssessedOnConfirmation()).thenReturn(fullyAssessed(true, true, true));
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		boolean enabled = sut.isAssessmentConfirmationEnabled(courseNodeMock, coachCourseEnv);

		assertThat(enabled).isFalse();
	}
	
	@Test
	public void shouldNotSetFullyAssessedIfNotEnabled() {
		FullyAssessedResult result = fullyAssessed(false, true, true);
		
		sut.updateFullyAssessed(courseNodeMock, participantCourseEnv, Role.auto, result);

		verify(courseAssessmentService, never()).updateFullyAssessed(courseNodeMock, participantCourseEnv, Boolean.TRUE,
				AssessmentEntryStatus.done, Role.auto);
	}
	
	@Test
	public void shouldNotSetFullyAssessedIfNotParticipant() {
		FullyAssessedResult result = fullyAssessed(false, true, true);
		
		sut.updateFullyAssessed(courseNodeMock, participantCourseEnv, Role.auto, result);

		verify(courseAssessmentService, never()).updateFullyAssessed(courseNodeMock, coachCourseEnv, Boolean.TRUE,
				AssessmentEntryStatus.done, Role.auto);
	}
	
	@Test
	public void shouldSetFullyAssessedToTrue() {
		FullyAssessedResult result = fullyAssessed(true, true, true);
		
		sut.updateFullyAssessed(courseNodeMock, participantCourseEnv, Role.auto, result);

		verify(courseAssessmentService).updateFullyAssessed(courseNodeMock, participantCourseEnv, Boolean.TRUE,
				AssessmentEntryStatus.done, Role.auto);
	}
	@Test
	public void shouldSetFullyAssessedToFalse() {
		FullyAssessedResult result = fullyAssessed(true, false, true);
		
		sut.updateFullyAssessed(courseNodeMock, participantCourseEnv, Role.auto, result);

		verify(courseAssessmentService).updateFullyAssessed(courseNodeMock, participantCourseEnv, Boolean.FALSE,
				AssessmentEntryStatus.done, Role.auto);
	}
	
	@Test
	public void shouldSetStatusDone() {
		FullyAssessedResult result = fullyAssessed(true, true, true);
		
		sut.updateFullyAssessed(courseNodeMock, participantCourseEnv, Role.auto, result);

		verify(courseAssessmentService).updateFullyAssessed(courseNodeMock, participantCourseEnv, Boolean.TRUE,
				AssessmentEntryStatus.done, Role.auto);
	}

	@Test
	public void shouldNotSetStatusDone() {
		AssessmentEntry assessmentEntry = new AssessmentEntryImpl();
		assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.inReview);
		when(courseAssessmentService.getAssessmentEntry(courseNodeMock, participantCourseEnv))
				.thenReturn(assessmentEntry);
		FullyAssessedResult result = fullyAssessed(true, true, false);
		
		sut.updateFullyAssessed(courseNodeMock, participantCourseEnv, Role.auto, result);

		verify(courseAssessmentService).updateFullyAssessed(courseNodeMock, participantCourseEnv, Boolean.TRUE,
				AssessmentEntryStatus.inReview, Role.auto);
	}
	
	@Test
	public void shouldInvokeConformedConfig() {
		sut.onAssessmentConfirmed(courseNodeMock, coachCourseEnv);

		verify(configMock).isFullyAssessedOnConfirmation();
	}
	
	@Test
	public void shouldInvokePassedConfig() {
		sut.onPassedUpdated(courseNodeMock, coachCourseEnv, null, null);

		verify(configMock).isFullyAssessedOnPassed(any());
	}
	
	@Test
	public void onStatusUpdated() {
		sut.onStatusUpdated(courseNodeMock, coachCourseEnv, null, null);

		verify(configMock).isFullyAssessedOnStatus(any());
	}

}
