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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;

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
	}
	
	@Test
	public void shouldSetAssessmentAsDoneIfNodeVisited() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnNodeVisited()).thenReturn(Boolean.TRUE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onNodeVisited(courseNodeMock, participantCourseEnv);

		verify(courseAssessmentService).updateAssessmentStatus(courseNodeMock, participantCourseEnv,
				AssessmentEntryStatus.done, Role.user);
	}
	
	@Test
	public void shouldSetAssessmentAsDoneIfNodeVisitedNotEnabled() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnNodeVisited()).thenReturn(Boolean.FALSE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onNodeVisited(courseNodeMock, participantCourseEnv);

		verify(courseAssessmentService, never()).updateAssessmentStatus(courseNodeMock, participantCourseEnv,
				AssessmentEntryStatus.done, Role.user);
	}
	
	@Test
	public void shouldNotChangeAssessmentIfNotAParticipantVisitedTheNode() {
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnNodeVisited()).thenReturn(Boolean.TRUE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onNodeVisited(courseNodeMock, coachCourseEnv);

		verify(courseAssessmentService, never()).updateAssessmentStatus(courseNodeMock, coachCourseEnv,
				AssessmentEntryStatus.done, Role.user);
	}
	
	@Test
	public void shouldSetAssessmentAsDoneIfRunStatusIsReached() {
		Role role = Role.auto;
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnRunStatus(AssessmentRunStatus.done)).thenReturn(Boolean.TRUE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onCompletionUpdate(courseNodeMock, participantCourseEnv, null, AssessmentRunStatus.done, role);

		verify(courseAssessmentService).updateAssessmentStatus(courseNodeMock, participantCourseEnv,
				AssessmentEntryStatus.done, role);
	}

	@Test
	public void shouldNotChangeAssessmentStatusIfRunStatusIsNotReached() {
		Role role = Role.auto;
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnRunStatus(AssessmentRunStatus.done)).thenReturn(Boolean.TRUE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onCompletionUpdate(courseNodeMock, participantCourseEnv, null, AssessmentRunStatus.notStarted, role);

		verify(courseAssessmentService, never()).updateAssessmentStatus(courseNodeMock, participantCourseEnv,
				AssessmentEntryStatus.done, role);
	}

	@Test
	public void shouldNotChangeAssessmentStatusIfItIsNotAParticipant() {
		Role role = Role.auto;
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnRunStatus(AssessmentRunStatus.done)).thenReturn(Boolean.TRUE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onCompletionUpdate(courseNodeMock, coachCourseEnv, null, AssessmentRunStatus.done, role);

		verify(courseAssessmentService, never()).updateAssessmentStatus(courseNodeMock, coachCourseEnv,
				AssessmentEntryStatus.done, role);
	}
	
	@Test
	public void shouldSetAssessmentAsDoneIfCompletionIsReached() {
		Role role = Role.auto;
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnCompletion(Double.valueOf(0.5))).thenReturn(Boolean.TRUE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onCompletionUpdate(courseNodeMock, participantCourseEnv, Double.valueOf(0.5), null, role);

		verify(courseAssessmentService).updateAssessmentStatus(courseNodeMock, participantCourseEnv,
				AssessmentEntryStatus.done, role);
	}
	
	@Test
	public void shouldNotChangeAssessmentStatusIfCompletionNotIsReached() {
		Role role = Role.auto;
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnCompletion(Double.valueOf(0.5))).thenReturn(Boolean.FALSE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onCompletionUpdate(courseNodeMock, participantCourseEnv, Double.valueOf(0.5), null, role);

		verify(courseAssessmentService, never()).updateAssessmentStatus(courseNodeMock, participantCourseEnv,
				AssessmentEntryStatus.done, role);
	}
	
	@Test
	public void shouldSetAssessmentAsDoneIfItIsNotAParticipant() {
		Role role = Role.auto;
		LearningPathConfigs configs = mock(LearningPathConfigs.class);
		when(configs.isDoneOnCompletion(Double.valueOf(0.5))).thenReturn(Boolean.TRUE);
		LearningPathNodeHandler handler = mock(LearningPathNodeHandler.class);
		when(handler.getConfigs(courseNodeMock)).thenReturn(configs);
		when(registry.getLearningPathNodeHandler(courseNodeMock)).thenReturn(handler);

		sut.onCompletionUpdate(courseNodeMock, coachCourseEnv, Double.valueOf(0.5), null, role);

		verify(courseAssessmentService, never()).updateAssessmentStatus(courseNodeMock, coachCourseEnv,
				AssessmentEntryStatus.done, role);
	}

}
