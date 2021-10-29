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
package org.olat.course.run.scoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.when;
import static org.olat.course.run.scoring.AverageCompletionEvaluator.DURATION_WEIGHTED;
import static org.olat.course.run.scoring.AverageCompletionEvaluator.UNWEIGHTED;
import static org.olat.modules.assessment.model.AssessmentObligation.excluded;
import static org.olat.modules.assessment.model.AssessmentObligation.mandatory;
import static org.olat.modules.assessment.model.AssessmentObligation.optional;

import org.junit.Before;
import org.junit.Test;
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
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 23 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AverageCompletionEvaluatorTest {
	
	@Mock
	private AssessmentConfig configSetByNode;
	@Mock
	private AssessmentConfig configEvaluated;
	@Mock
	private AssessmentConfig configNone;

	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	private AverageCompletionEvaluator sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(configEvaluated.getCompletionMode()).thenReturn(Mode.evaluated);
		when(configSetByNode.getCompletionMode()).thenReturn(Mode.setByNode);
		when(configNone.getCompletionMode()).thenReturn(Mode.none);

		sut = new AverageCompletionEvaluator(courseAssessmentService, UNWEIGHTED);
	}

	@Test
	public void shouldGetAverageCompletionOfChildren() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		// Child: uncalculated 
		CourseNode childUncalculated = new Card2BrainCourseNode();
		parent.addChild(childUncalculated);
		AssessmentEvaluation childUncalculatedEvaluation = createAssessmentEvaluation(mandatory, null, Double.valueOf(0.5), null, null);
		scoreAccounting.put(childUncalculated, childUncalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childUncalculated)).thenReturn(configSetByNode);
		// Child: Calculated
		CourseNode childCalculated = new STCourseNode();
		parent.addChild(childCalculated);
		AssessmentEvaluation childCalculatedEvaluation = createAssessmentEvaluation(mandatory, null, Double.valueOf(0.1), null, null);
		scoreAccounting.put(childCalculated, childCalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childCalculated)).thenReturn(configEvaluated);
		// Child: Calculated, fully assessed
		CourseNode childCalculated2 = new STCourseNode();
		parent.addChild(childCalculated2);
		AssessmentEvaluation childCalculatedEvaluation2 = createAssessmentEvaluation(mandatory, null, Double.valueOf(0.1), null, Boolean.TRUE);
		scoreAccounting.put(childCalculated2, childCalculatedEvaluation2);
		when(courseAssessmentService.getAssessmentConfig(childCalculated2)).thenReturn(configEvaluated);
		// Child: Uncalculated, invisible
		CourseNode childInvisible = new Card2BrainCourseNode();
		parent.addChild(childInvisible);
		AssessmentEvaluation childInvisibleEvaluation = createAssessmentEvaluation(excluded, null, Double.valueOf(0.1), null, Boolean.TRUE);
		scoreAccounting.put(childInvisible, childInvisibleEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInvisible)).thenReturn(configSetByNode);
		
		// Child level 2: calculated
		CourseNode child2Uncalculated = new SPCourseNode();
		childCalculated2.addChild(child2Uncalculated);
		AssessmentEvaluation child2UncalculatedEvaluation = createAssessmentEvaluation(mandatory, null, Double.valueOf(1.0), null, null);
		scoreAccounting.put(child2Uncalculated, child2UncalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(child2Uncalculated)).thenReturn(configSetByNode);
		
		Double completion = sut.getCompletion(null, parent, scoreAccounting);
		
		assertThat(completion).isEqualTo(0.75);
	}
	
	@Test
	public void shouldGetAverageWeightedByDuration() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		// Child 1
		CourseNode child1 = new Card2BrainCourseNode();
		parent.addChild(child1);
		AssessmentEvaluation child1Evaluation = createAssessmentEvaluation(mandatory, null, Double.valueOf(0.0), null, null);
		scoreAccounting.put(child1, child1Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child1)).thenReturn(configSetByNode);
		// Child 2
		CourseNode child2 = new Card2BrainCourseNode();
		parent.addChild(child2);
		AssessmentEvaluation child2Evaluation = createAssessmentEvaluation(mandatory, 2, Double.valueOf(0.5), null, null);
		scoreAccounting.put(child2, child2Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child2)).thenReturn(configSetByNode);
		// Child 3
		CourseNode child3 = new Card2BrainCourseNode();
		parent.addChild(child3);
		AssessmentEvaluation child3Evaluation = createAssessmentEvaluation(mandatory, 3, Double.valueOf(1.0), null, null);
		scoreAccounting.put(child3, child3Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child3)).thenReturn(configSetByNode);
		// Child: Uncalculated, invisible
		CourseNode childInvisible = new Card2BrainCourseNode();
		parent.addChild(childInvisible);
		AssessmentEvaluation childInvisibleEvaluation = createAssessmentEvaluation(excluded, null, Double.valueOf(0.1), null, Boolean.TRUE);
		scoreAccounting.put(childInvisible, childInvisibleEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInvisible)).thenReturn(configSetByNode);

		AverageCompletionEvaluator weightedSut = new AverageCompletionEvaluator(courseAssessmentService, DURATION_WEIGHTED);
		Double completion = weightedSut.getCompletion(null, parent, scoreAccounting);
		
		double expected = (1 * 0.0 + 2 * 0.5 + 3 * 1.0) / 6;
		assertThat(completion).isEqualTo(expected, offset(0.001));
	}
	
	@Test
	public void shouldOnlyRespectMandatoryEvaluations() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent: calculated
		CourseNode parent = new STCourseNode();
		// Child: mandatory
		CourseNode childMandatory = new Card2BrainCourseNode();
		parent.addChild(childMandatory);
		AssessmentEvaluation childMandatoryEvaluation = createAssessmentEvaluation(mandatory, null, Double.valueOf(0.5), null, null);
		scoreAccounting.put(childMandatory, childMandatoryEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childMandatory)).thenReturn(configSetByNode);
		// Child: optional
		CourseNode childOptional = new Card2BrainCourseNode();
		parent.addChild(childOptional);
		AssessmentEvaluation childOptionalEvaluation = createAssessmentEvaluation(optional, null, Double.valueOf(0.6), null, null);
		scoreAccounting.put(childOptional, childOptionalEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childOptional)).thenReturn(configSetByNode);
		// Child: no obligation
		CourseNode childNoObligation = new Card2BrainCourseNode();
		parent.addChild(childNoObligation);
		AssessmentEvaluation childNoObligationEvaluation = createAssessmentEvaluation(null, null, Double.valueOf(0.7), null, null);
		scoreAccounting.put(childNoObligation, childNoObligationEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childNoObligation)).thenReturn(configSetByNode);
		// Child: Uncalculated, invisible
		CourseNode childInvisible = new Card2BrainCourseNode();
		parent.addChild(childInvisible);
		AssessmentEvaluation childInvisibleEvaluation = createAssessmentEvaluation(excluded, null, Double.valueOf(0.1), null, Boolean.TRUE);
		scoreAccounting.put(childInvisible, childInvisibleEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInvisible)).thenReturn(configSetByNode);
		
		Double completion = sut.getCompletion(null, parent, scoreAccounting);
		
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
		AssessmentEvaluation childFullyAssessedEvaluation = createAssessmentEvaluation(mandatory, null, null, null, Boolean.TRUE);
		scoreAccounting.put(childFullyAssessed, childFullyAssessedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childFullyAssessed)).thenReturn(configNone);
		// Child: no status
		CourseNode childNoStatus = new Card2BrainCourseNode();
		parent.addChild(childNoStatus);
		AssessmentEvaluation childNoStatusEvaluation = createAssessmentEvaluation(mandatory, null, null, null, null);
		scoreAccounting.put(childNoStatus, childNoStatusEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childNoStatus)).thenReturn(configNone);
		// Child: notReady
		CourseNode childNotReady = new Card2BrainCourseNode();
		parent.addChild(childNotReady);
		AssessmentEvaluation childNotReadyEvaluation = createAssessmentEvaluation(mandatory, null, null, AssessmentEntryStatus.notReady, null);
		scoreAccounting.put(childNotReady, childNotReadyEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childNotReady)).thenReturn(configNone);
		// Child: notStarted
		CourseNode childNotStarted = new Card2BrainCourseNode();
		parent.addChild(childNotStarted);
		AssessmentEvaluation childNotStartedEvaluation = createAssessmentEvaluation(mandatory, null, null, AssessmentEntryStatus.notStarted, null);
		scoreAccounting.put(childNotStarted, childNotStartedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childNotStarted)).thenReturn(configNone);
		// Child: inProgress
		CourseNode childInProgress = new Card2BrainCourseNode();
		parent.addChild(childInProgress);
		AssessmentEvaluation childInProgressEvaluation = createAssessmentEvaluation(mandatory, null, null, AssessmentEntryStatus.inProgress, null);
		scoreAccounting.put(childInProgress, childInProgressEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInProgress)).thenReturn(configNone);
		// Child: inReview
		CourseNode childInReview = new Card2BrainCourseNode();
		parent.addChild(childInReview);
		AssessmentEvaluation childInReviewEvaluation = createAssessmentEvaluation(mandatory, null, null, AssessmentEntryStatus.inReview, null);
		scoreAccounting.put(childInReview, childInReviewEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInReview)).thenReturn(configNone);
		// Child: done
		CourseNode childDone = new Card2BrainCourseNode();
		parent.addChild(childDone);
		AssessmentEvaluation childDoneEvaluation = createAssessmentEvaluation(mandatory, null, null, AssessmentEntryStatus.done, null);
		scoreAccounting.put(childDone, childDoneEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childDone)).thenReturn(configNone);
		// Child: Uncalculated, invisible
		CourseNode childInvisible = new Card2BrainCourseNode();
		parent.addChild(childInvisible);
		AssessmentEvaluation childInvisibleEvaluation = createAssessmentEvaluation(excluded, null, Double.valueOf(0.1), null, Boolean.TRUE);
		scoreAccounting.put(childInvisible, childInvisibleEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInvisible)).thenReturn(configSetByNode);
		
		Double completion = sut.getCompletion(null, parent, scoreAccounting);
		
		double expected = (1.0 + 0.0 + 0.0 + 0.0 + 0.5 + 0.75 + 0.9) / 7;
		assertThat(completion).isEqualTo(expected, offset(0.001));
	}
	
	@Test
	public void shouldTreatFullyAssessedAsCompleted() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent: calculated
		CourseNode parent = new STCourseNode();
		// Child without own completion: fully assessed
		CourseNode child1 = new Card2BrainCourseNode();
		parent.addChild(child1);
		AssessmentEvaluation assessedEvaluation1 = createAssessmentEvaluation(mandatory, null, null, null, Boolean.TRUE);
		scoreAccounting.put(child1, assessedEvaluation1);
		when(courseAssessmentService.getAssessmentConfig(child1)).thenReturn(configNone);
		// Child without own completion: not ready
		CourseNode child2 = new Card2BrainCourseNode();
		parent.addChild(child2);
		AssessmentEvaluation assessedEvaluation2 = createAssessmentEvaluation(mandatory, null, null, AssessmentEntryStatus.notReady, Boolean.FALSE);
		scoreAccounting.put(child2, assessedEvaluation2);
		when(courseAssessmentService.getAssessmentConfig(child2)).thenReturn(configNone);
		// Child with own completion: completion 0.5, fully assessed
		CourseNode child3 = new Card2BrainCourseNode();
		parent.addChild(child3);
		AssessmentEvaluation childEvaluation3 = createAssessmentEvaluation(mandatory, null, Double.valueOf(0.5), null, Boolean.TRUE);
		scoreAccounting.put(child3, childEvaluation3);
		when(courseAssessmentService.getAssessmentConfig(child3)).thenReturn(configSetByNode);
		// Child with own completion: completion 0.5, not fully assessed
		CourseNode child4 = new Card2BrainCourseNode();
		parent.addChild(child4);
		AssessmentEvaluation childEvaluation4 = createAssessmentEvaluation(mandatory, null, Double.valueOf(0.5), null, null);
		scoreAccounting.put(child4, childEvaluation4);
		when(courseAssessmentService.getAssessmentConfig(child4)).thenReturn(configSetByNode);
		
		Double completion = sut.getCompletion(null, parent, scoreAccounting);
		
		double expected = (1.0 + 0.0 + 1.0 + 0.5 ) / 4;
		assertThat(completion).isEqualTo(expected, offset(0.001));
	}
	
	@Test
	public void shouldReturnFullCompletionIfItHasNoChildren() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		CourseNode parent = new STCourseNode();
		
		Double completion = sut.getCompletion(null, parent, scoreAccounting);
		
		assertThat(completion).isEqualTo(1.0);
	}

	@Test
	public void shouldReturnFullCompletionIfNoChildrenAreMandatory() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent: calculated
		CourseNode parent = new STCourseNode();
		// Child without own completion: fully assessed
		CourseNode child1 = new Card2BrainCourseNode();
		parent.addChild(child1);
		AssessmentEvaluation assessedEvaluation1 = createAssessmentEvaluation(optional, null, null, null, Boolean.TRUE);
		scoreAccounting.put(child1, assessedEvaluation1);
		when(courseAssessmentService.getAssessmentConfig(child1)).thenReturn(configNone);
		// Child without own completion: not ready
		CourseNode child2 = new Card2BrainCourseNode();
		parent.addChild(child2);
		AssessmentEvaluation assessedEvaluation2 = createAssessmentEvaluation(optional, null, null, AssessmentEntryStatus.notReady, Boolean.FALSE);
		scoreAccounting.put(child2, assessedEvaluation2);
		when(courseAssessmentService.getAssessmentConfig(child2)).thenReturn(configNone);
		// Child with own completion: completion 0.5, fully assessed
		CourseNode child3 = new Card2BrainCourseNode();
		parent.addChild(child3);
		AssessmentEvaluation childEvaluation3 = createAssessmentEvaluation(optional, null, Double.valueOf(0.5), null, Boolean.TRUE);
		scoreAccounting.put(child3, childEvaluation3);
		when(courseAssessmentService.getAssessmentConfig(child3)).thenReturn(configSetByNode);
		// Child with own completion: completion 0.5, not fully assessed
		CourseNode child4 = new Card2BrainCourseNode();
		parent.addChild(child4);
		AssessmentEvaluation childEvaluation4 = createAssessmentEvaluation(optional, null, Double.valueOf(0.5), null, null);
		scoreAccounting.put(child4, childEvaluation4);
		when(courseAssessmentService.getAssessmentConfig(child4)).thenReturn(configSetByNode);
		// Child: Uncalculated, invisible
		CourseNode childInvisible = new Card2BrainCourseNode();
		parent.addChild(childInvisible);
		AssessmentEvaluation childInvisibleEvaluation = createAssessmentEvaluation(excluded, null, Double.valueOf(0.1), null, Boolean.TRUE);
		scoreAccounting.put(childInvisible, childInvisibleEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInvisible)).thenReturn(configSetByNode);
		
		Double completion = sut.getCompletion(null, parent, scoreAccounting);
		
		assertThat(completion).isEqualTo(1.0);
	}

	
	private AssessmentEvaluation createAssessmentEvaluation(AssessmentObligation obligation, Integer duration,
			Double completion, AssessmentEntryStatus status, Boolean fullyAssessed) {
		return new AssessmentEvaluation(null, null, null, null, null, null, completion, status, null, fullyAssessed,
				null, null, null, null, null, null, null, 0, null, null, null, null, null, null,
				ObligationOverridable.of(obligation), duration, null, null);
	}

}
