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
package org.olat.course.nodes.st.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.when;
import static org.olat.modules.assessment.model.AssessmentObligation.mandatory;
import static org.olat.modules.assessment.model.AssessmentObligation.optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.MappedScoreAccounting;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.Card2BrainCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 23 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NumberOfNodesCompletionEvaluatorTest {
	
	@Mock
	private AssessmentConfig configSetByNode;
	@Mock
	private AssessmentConfig configEvaluated;
	@Mock
	private AssessmentConfig configNone;

	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	@InjectMocks
	private NumberOfNodesCompletionEvaluator sut;

	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(configEvaluated.getCompletionMode()).thenReturn(Mode.evaluated);
		when(configSetByNode.getCompletionMode()).thenReturn(Mode.setByNode);
		when(configNone.getCompletionMode()).thenReturn(Mode.none);
	}
	
	@Test
	public void shouldReturnNullIfItHasNoChildren() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		CourseNode parent = new STCourseNode();
		
		Double completion = sut.getCompletion(null, parent, null, scoreAccounting);
		
		assertThat(completion).isNull();
	}

	@Test
	public void shouldGetAverageCompletionOfChildren() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		// Child: uncalculated 
		CourseNode childUncalculated = new Card2BrainCourseNode();
		parent.addChild(childUncalculated);
		AssessmentEvaluation childUncalculatedEvaluation = createAssessmentEvaluation(mandatory, Double.valueOf(0.5), null, null);
		scoreAccounting.put(childUncalculated, childUncalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childUncalculated)).thenReturn(configSetByNode);
		// Child: Calculated
		CourseNode childCalculated = new STCourseNode();
		parent.addChild(childCalculated);
		AssessmentEvaluation childCalculatedEvaluation = createAssessmentEvaluation(mandatory, Double.valueOf(0.1), null, null);
		scoreAccounting.put(childCalculated, childCalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childCalculated)).thenReturn(configEvaluated);
		
		// Child level 2: calculated
		CourseNode child2Uncalculated = new SPCourseNode();
		parent.addChild(child2Uncalculated);
		AssessmentEvaluation child2UncalculatedEvaluation = createAssessmentEvaluation(mandatory, Double.valueOf(1.0), null, null);
		scoreAccounting.put(child2Uncalculated, child2UncalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(child2Uncalculated)).thenReturn(configSetByNode);
		
		Double completion = sut.getCompletion(null, parent, null, scoreAccounting);
		
		assertThat(completion).isEqualTo(0.75);
	}
	
	@Test
	public void shouldOnlyRespectMandatoryEvaluations() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent: calculated
		CourseNode parent = new STCourseNode();
		// Child: mandatory
		CourseNode childMandatory = new Card2BrainCourseNode();
		parent.addChild(childMandatory);
		AssessmentEvaluation childMandatoryEvaluation = createAssessmentEvaluation(mandatory, Double.valueOf(0.5), null, null);
		scoreAccounting.put(childMandatory, childMandatoryEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childMandatory)).thenReturn(configSetByNode);
		// Child: optional
		CourseNode childOptional = new Card2BrainCourseNode();
		parent.addChild(childOptional);
		AssessmentEvaluation childOptionalEvaluation = createAssessmentEvaluation(optional, Double.valueOf(0.6), null, null);
		scoreAccounting.put(childOptional, childOptionalEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childOptional)).thenReturn(configSetByNode);
		// Child: no obligation
		CourseNode childNoObligation = new Card2BrainCourseNode();
		parent.addChild(childNoObligation);
		AssessmentEvaluation childNoObligationEvaluation = createAssessmentEvaluation(null, Double.valueOf(0.7), null, null);
		scoreAccounting.put(childNoObligation, childNoObligationEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childNoObligation)).thenReturn(configSetByNode);
		
		Double completion = sut.getCompletion(null, parent, null, scoreAccounting);
		
		assertThat(completion).isEqualTo(0.5);
	}
	
	@Test
	public void shouldAssumeCompletionIfTheCourseNodeDoesNotSetIt() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent: calculated
		CourseNode parent = new STCourseNode();
		// Child: fully assessed
		CourseNode childFullyAssessed = new Card2BrainCourseNode();
		parent.addChild(childFullyAssessed);
		AssessmentEvaluation childFullyAssessedEvaluation = createAssessmentEvaluation(mandatory, null, null, Boolean.TRUE);
		scoreAccounting.put(childFullyAssessed, childFullyAssessedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childFullyAssessed)).thenReturn(configNone);
		// Child: no status
		CourseNode childNoStatus = new Card2BrainCourseNode();
		parent.addChild(childNoStatus);
		AssessmentEvaluation childNoStatusEvaluation = createAssessmentEvaluation(mandatory, null, null, null);
		scoreAccounting.put(childNoStatus, childNoStatusEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childNoStatus)).thenReturn(configNone);
		// Child: notReady
		CourseNode childNotReady = new Card2BrainCourseNode();
		parent.addChild(childNotReady);
		AssessmentEvaluation childNotReadyEvaluation = createAssessmentEvaluation(mandatory, null, AssessmentEntryStatus.notReady, null);
		scoreAccounting.put(childNotReady, childNotReadyEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childNotReady)).thenReturn(configNone);
		// Child: notStarted
		CourseNode childNotStarted = new Card2BrainCourseNode();
		parent.addChild(childNotStarted);
		AssessmentEvaluation childNotStartedEvaluation = createAssessmentEvaluation(mandatory, null, AssessmentEntryStatus.notStarted, null);
		scoreAccounting.put(childNotStarted, childNotStartedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childNotStarted)).thenReturn(configNone);
		// Child: inProgress
		CourseNode childInProgress = new Card2BrainCourseNode();
		parent.addChild(childInProgress);
		AssessmentEvaluation childInProgressEvaluation = createAssessmentEvaluation(mandatory, null, AssessmentEntryStatus.inProgress, null);
		scoreAccounting.put(childInProgress, childInProgressEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInProgress)).thenReturn(configNone);
		// Child: inReview
		CourseNode childInReview = new Card2BrainCourseNode();
		parent.addChild(childInReview);
		AssessmentEvaluation childInReviewEvaluation = createAssessmentEvaluation(mandatory, null, AssessmentEntryStatus.inReview, null);
		scoreAccounting.put(childInReview, childInReviewEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInReview)).thenReturn(configNone);
		// Child: done
		CourseNode childDone = new Card2BrainCourseNode();
		parent.addChild(childDone);
		AssessmentEvaluation childDoneEvaluation = createAssessmentEvaluation(mandatory, null, AssessmentEntryStatus.done, null);
		scoreAccounting.put(childDone, childDoneEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childDone)).thenReturn(configNone);
		
		Double completion = sut.getCompletion(null, parent, null, scoreAccounting);
		
		double expected = (1.0 + 0.0 + 0.0 + 0.0 + 0.5 + 0.75 + 1.0) / 7;
		assertThat(completion).isEqualTo(expected, offset(0.001));
		
	}
	
	private AssessmentEvaluation createAssessmentEvaluation(AssessmentObligation obligation, Double completion,
			AssessmentEntryStatus status, Boolean fullyAssessed) {
		return new AssessmentEvaluation(null, null, null, completion, status, null, fullyAssessed, null, null, null,
				null, null, 0, null, null, null, null, obligation, null);
	}

}
