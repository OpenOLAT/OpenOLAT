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
import org.olat.course.nodes.st.assessment.CumulatingScoreEvaluator.Score;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 10.03.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CumulatingScoreEvaluatorTest {
	
	@Mock
	private AssessmentConfig configSetByNode;
	@Mock
	private AssessmentConfig configSetByNodeIgnore;
	@Mock
	private AssessmentConfig configEvaluated;
	@Mock
	private AssessmentConfig configNone;
	@Mock
	private RepositoryEntryRef courseEntry;

	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	private CumulatingScoreEvaluator sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(configEvaluated.getScoreMode()).thenReturn(Mode.evaluated);
		when(configSetByNode.getScoreMode()).thenReturn(Mode.setByNode);
		when(configSetByNode.ignoreInCourseAssessment()).thenReturn(Boolean.FALSE);
		when(configSetByNodeIgnore.getScoreMode()).thenReturn(Mode.setByNode);
		when(configSetByNodeIgnore.ignoreInCourseAssessment()).thenReturn(Boolean.TRUE);
		when(configNone.getScoreMode()).thenReturn(Mode.none);

		sut = new CumulatingScoreEvaluator(true);
	}

	@Test
	public void shouldGetCummulatingScore() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		// Child: uncalculated 
		CourseNode childUncalculated = new Card2BrainCourseNode();
		parent.addChild(childUncalculated);
		AssessmentEvaluation childUncalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(14), Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childUncalculated, childUncalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childUncalculated)).thenReturn(configSetByNode);
		// Child: Calculated
		CourseNode childCalculated = new STCourseNode();
		parent.addChild(childCalculated);
		AssessmentEvaluation childCalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(4), Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childCalculated, childCalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childCalculated)).thenReturn(configEvaluated);
		// Child: uncalculated, not visible
		CourseNode childUncalculated2 = new STCourseNode();
		parent.addChild(childUncalculated2);
		AssessmentEvaluation childCalculatedEvaluation2 = createAssessmentEvaluation(Float.valueOf(3), Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(childUncalculated2, childCalculatedEvaluation2);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childUncalculated2)).thenReturn(configSetByNode);
		// Child: uncalculated 
		CourseNode childUncalculated3 = new Card2BrainCourseNode();
		parent.addChild(childUncalculated3);
		AssessmentEvaluation childUncalculatedEvaluation3 = createAssessmentEvaluation(Float.valueOf(5), Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childUncalculated3, childUncalculatedEvaluation3);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childUncalculated3)).thenReturn(configSetByNode);
		// Child: uncalculated 
		CourseNode childUncalculatedIgnored = new Card2BrainCourseNode();
		parent.addChild(childUncalculatedIgnored);
		AssessmentEvaluation childUncalculatedEvaluationIgnored = createAssessmentEvaluation(Float.valueOf(5), Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childUncalculatedIgnored, childUncalculatedEvaluationIgnored);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childUncalculatedIgnored)).thenReturn(configSetByNodeIgnore);
		
		// Child level 2: uncalculated
		CourseNode child2Uncalculated = new SPCourseNode();
		childCalculated.addChild(child2Uncalculated);
		AssessmentEvaluation child2UncalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(2), Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child2Uncalculated, child2UncalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, child2Uncalculated)).thenReturn(configSetByNode);
		// Child level 2: calculated
		CourseNode child2Calculated = new STCourseNode();
		childCalculated.addChild(child2Calculated);
		AssessmentEvaluation child2CalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(1), Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child2Calculated, child2CalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, child2Calculated)).thenReturn(configEvaluated);
		// Child level 2: uncalculated, invisible
		CourseNode child2Invisible = new SPCourseNode();
		childCalculated.addChild(child2Invisible);
		AssessmentEvaluation child2Evaluation = createAssessmentEvaluation(Float.valueOf(2), Boolean.TRUE, AssessmentObligation.excluded);
		scoreAccounting.put(child2Invisible, child2Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, child2Invisible)).thenReturn(configSetByNode);
		
		Score score = sut.getScore(parent, scoreAccounting, courseEntry, courseAssessmentService);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(score.getSum()).isEqualTo(21);
		softly.assertThat(score.getAverage()).isEqualTo(7);
		softly.assertAll();
	}
	
	@Test
	public void shouldReturnNullIfNoChildrenWithScore() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		// Child: Calculated
		CourseNode childCalculated = new STCourseNode();
		parent.addChild(childCalculated);
		AssessmentEvaluation childCalculatedEvaluation = createAssessmentEvaluation(Float.valueOf(4), Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childCalculated, childCalculatedEvaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childCalculated)).thenReturn(configEvaluated);
		// Child: uncalculated, not visible
		CourseNode childNotVisible = new SPCourseNode();
		parent.addChild(childNotVisible);
		AssessmentEvaluation childNotVisibleEvaluation = createAssessmentEvaluation(Float.valueOf(3), Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(childNotVisible, childNotVisibleEvaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childNotVisible)).thenReturn(configSetByNode);
		// Child: uncalculated, invisible
		CourseNode childInvisible = new SPCourseNode();
		childCalculated.addChild(childInvisible);
		AssessmentEvaluation child2InvisibleEvaluation = createAssessmentEvaluation(Float.valueOf(2), Boolean.TRUE, AssessmentObligation.excluded);
		scoreAccounting.put(childInvisible, child2InvisibleEvaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childInvisible)).thenReturn(configSetByNode);
		
		Score score = sut.getScore(parent, scoreAccounting, courseEntry, courseAssessmentService);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(score.getSum()).isNull();
		softly.assertThat(score.getAverage()).isNull();
		softly.assertAll();
	}

	private AssessmentEvaluation createAssessmentEvaluation(Float score, Boolean userVisibility, AssessmentObligation obligation) {
		return new AssessmentEvaluation(score, null, null, null, null, null, null, null, null, null, null, userVisibility, null, null,
				null, null, null, null, null, null, 0, null, null, null, null, null, null, ObligationOverridable.of(obligation), null, null, null);
	}

}
