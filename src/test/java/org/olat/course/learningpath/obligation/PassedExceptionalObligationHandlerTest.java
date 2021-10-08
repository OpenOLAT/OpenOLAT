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
package org.olat.course.learningpath.obligation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.course.Structure;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.MappedScoreAccounting;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.learningpath.obligation.PassedExceptionalObligationHandler.Status;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 20 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PassedExceptionalObligationHandlerTest {
	
	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	@InjectMocks
	private PassedExceptionalObligationHandler sut;
	
	@Before
	public void initMocks(){
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void passedShouldMatchIfPassed() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedPassed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isTrue();
	}
	
	@Test
	public void passedShouldMatchIfNoPassedConfigured() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.none);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedPassed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}
	
	@Test
	public void passedShoudNotMatchIfNotUserVisible() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedPassed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	@Test
	public void passedShoudNotMatchIfInvisible() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.excluded);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedPassed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	@Test
	public void passedShoudNotMatchIfFailed() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedPassed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	@Test
	public void passedShoudNotMatchIfNotGraded() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedPassed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}
	
	@Test
	public void failedShouldMatchIfFailed() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedFailed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isTrue();
	}
	
	@Test
	public void failedShouldMatchIfNoPassedConfigured() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.none);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedPassed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}
	
	@Test
	public void failedShoudNotMatchIfNotUserVisible() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedFailed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	@Test
	public void failedShoudNotMatchIfInvisible() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE, AssessmentObligation.excluded);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedFailed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	@Test
	public void failedShoudNotMatchIfPassed() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedFailed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	@Test
	public void failedShoudNotMatchIfNotGraded() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.gradedFailed.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}
	
	@Test
	public void notGradedShouldMatchIfNoPassed() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.notGraded.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isTrue();
	}
	
	@Test
	public void notGradedShouldMatchIfNoPassedConfigured() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.none);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.notGraded.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}
	
	@Test
	public void notGradedShoudMatchIfNotUserVisible() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(null, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.notGraded.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isTrue();
	}

	@Test
	public void notGradedShoudNotMatchIfInvisible() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(null, Boolean.TRUE, AssessmentObligation.excluded);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.notGraded.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	@Test
	public void notGradedShoudNotMatchIfFailed() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.notGraded.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	@Test
	public void notGradedShoudNotMatchIfNotPassed() {
		CourseNode courseNode = mock(CourseNode.class);
		Structure runStructure = mock(Structure.class);
		when(runStructure.getNode(any())).thenReturn(courseNode);
		AssessmentConfig assessmentConfig = mock(AssessmentConfig.class);
		when(assessmentConfig.getPassedMode()).thenReturn(Mode.setByNode);
		when(courseAssessmentService.getAssessmentConfig(courseNode)).thenReturn(assessmentConfig);
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		AssessmentEvaluation evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(courseNode, evaluation);
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setStatus(Status.notGraded.name());
		
		boolean matchesIdentity = sut.matchesIdentity(exceptionalObligation, null, null, runStructure, scoreAccounting);
		
		assertThat(matchesIdentity).isFalse();
	}

	private AssessmentEvaluation createAssessmentEvaluation(Boolean passed, Boolean userVisibility, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, passed, null, null, null, null, null, userVisibility, null, null, null,
				null, null, null, null, null, 1, null, null, null, null, null, null, ObligationOverridable.of(obligation), null, null, null);
	}

}
