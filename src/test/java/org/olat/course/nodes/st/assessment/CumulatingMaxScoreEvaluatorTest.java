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

import static org.mockito.Mockito.when;

import org.assertj.core.api.SoftAssertions;
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
import org.olat.course.nodes.st.assessment.CumulatingMaxScoreEvaluator.MaxScore;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 29 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CumulatingMaxScoreEvaluatorTest {

	@Mock
	private AssessmentConfig configSetByNode;
	@Mock
	private AssessmentConfig configSetByNodeIgnore;
	@Mock
	private AssessmentConfig configEvaluated;
	@Mock
	private AssessmentConfig configNone;

	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	private CumulatingMaxScoreEvaluator sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(configEvaluated.getScoreMode()).thenReturn(Mode.evaluated);
		when(configSetByNode.getScoreMode()).thenReturn(Mode.setByNode);
		when(configSetByNode.ignoreInCourseAssessment()).thenReturn(Boolean.FALSE);
		when(configSetByNodeIgnore.getScoreMode()).thenReturn(Mode.setByNode);
		when(configSetByNodeIgnore.ignoreInCourseAssessment()).thenReturn(Boolean.TRUE);
		when(configNone.getScoreMode()).thenReturn(Mode.none);

		sut = new CumulatingMaxScoreEvaluator(true);
	}

	@Test
	public void shouldGetCummulatingMaxScore() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		// Child: uncalculated 
		CourseNode childUncalculated = new Card2BrainCourseNode();
		parent.addChild(childUncalculated);
		AssessmentEvaluation childUncalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(14), AssessmentObligation.mandatory);
		scoreAccounting.put(childUncalculated, childUncalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childUncalculated)).thenReturn(configSetByNode);
		// Child: Calculated
		CourseNode childCalculated = new STCourseNode();
		parent.addChild(childCalculated);
		AssessmentEvaluation childCalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(4), AssessmentObligation.mandatory);
		scoreAccounting.put(childCalculated, childCalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childCalculated)).thenReturn(configEvaluated);
		// Child: uncalculated, not visible
		CourseNode childUncalculated2 = new STCourseNode();
		parent.addChild(childUncalculated2);
		AssessmentEvaluation childCalculatedEvaluation2 = createAssessmentEvaluation(Float.valueOf(3), AssessmentObligation.excluded);
		scoreAccounting.put(childUncalculated2, childCalculatedEvaluation2);
		when(courseAssessmentService.getAssessmentConfig(childUncalculated2)).thenReturn(configSetByNode);
		// Child: uncalculated 
		CourseNode childUncalculated3 = new Card2BrainCourseNode();
		parent.addChild(childUncalculated3);
		AssessmentEvaluation childUncalculatedEvaluation3 = createAssessmentEvaluation(Float.valueOf(5), AssessmentObligation.mandatory);
		scoreAccounting.put(childUncalculated3, childUncalculatedEvaluation3);
		when(courseAssessmentService.getAssessmentConfig(childUncalculated3)).thenReturn(configSetByNode);
		// Child: uncalculated 
		CourseNode childUncalculatedIgnored = new Card2BrainCourseNode();
		parent.addChild(childUncalculatedIgnored);
		AssessmentEvaluation childUncalculatedEvaluationIgnored = createAssessmentEvaluation(Float.valueOf(5), AssessmentObligation.mandatory);
		scoreAccounting.put(childUncalculatedIgnored, childUncalculatedEvaluationIgnored);
		when(courseAssessmentService.getAssessmentConfig(childUncalculatedIgnored)).thenReturn(configSetByNodeIgnore);
		
		// Child level 2: uncalculated
		CourseNode child2Uncalculated = new SPCourseNode();
		childCalculated.addChild(child2Uncalculated);
		AssessmentEvaluation child2UncalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(2), AssessmentObligation.mandatory);
		scoreAccounting.put(child2Uncalculated, child2UncalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(child2Uncalculated)).thenReturn(configSetByNode);
		// Child level 2: calculated
		CourseNode child2Calculated = new STCourseNode();
		childCalculated.addChild(child2Calculated);
		AssessmentEvaluation child2CalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(1), AssessmentObligation.mandatory);
		scoreAccounting.put(child2Calculated, child2CalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(child2Calculated)).thenReturn(configEvaluated);
		// Child level 2: uncalculated, invisible
		CourseNode child2Invisible = new SPCourseNode();
		childCalculated.addChild(child2Invisible);
		AssessmentEvaluation child2Evaluation = createAssessmentEvaluation(Float.valueOf(2), AssessmentObligation.excluded);
		scoreAccounting.put(child2Invisible, child2Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child2Invisible)).thenReturn(configSetByNode);
		
		MaxScore maxScore = sut.getMaxScore(parent, scoreAccounting, courseAssessmentService);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(maxScore.getSum()).isEqualTo(21);
		softly.assertThat(maxScore.getAverage()).isEqualTo(7);
		softly.assertAll();
	}
	
	@Test
	public void shouldReturnNullIfNoChildrenWithMaxScore() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		// Child: Calculated
		CourseNode childCalculated = new STCourseNode();
		parent.addChild(childCalculated);
		AssessmentEvaluation childCalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(4), AssessmentObligation.mandatory);
		scoreAccounting.put(childCalculated, childCalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childCalculated)).thenReturn(configEvaluated);
		// Child: uncalculated, invisible
		CourseNode childInvisible = new SPCourseNode();
		childCalculated.addChild(childInvisible);
		AssessmentEvaluation child2InvisibleEvaluation = createAssessmentEvaluation(Float.valueOf(2), AssessmentObligation.excluded);
		scoreAccounting.put(childInvisible, child2InvisibleEvaluation);
		when(courseAssessmentService.getAssessmentConfig(childInvisible)).thenReturn(configSetByNode);
		
		MaxScore maxScore = sut.getMaxScore(parent, scoreAccounting, courseAssessmentService);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(maxScore.getSum()).isNull();
		softly.assertThat(maxScore.getAverage()).isNull();
		softly.assertAll();
	}

	private AssessmentEvaluation createAssessmentEvaluation(Float maxScore, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, maxScore,  null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, 0, null, null, null, null, null, null, ObligationOverridable.of(obligation), null, null, null);
	}

}
