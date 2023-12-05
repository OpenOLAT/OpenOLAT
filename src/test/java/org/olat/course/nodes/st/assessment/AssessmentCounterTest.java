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
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.assessment.AssessmentCounter.AssessmentCounts;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 19 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCounterTest {

	@Mock
	private AssessmentConfig configPassedSetByNode;
	@Mock
	private AssessmentConfig configPassedSetByNodeIgnore;
	@Mock
	private AssessmentConfig configPassedEvaluated;
	@Mock
	private AssessmentConfig configPassedNone;
	@Mock
	private AssessmentConfig configScoreSetByNode;
	@Mock
	private AssessmentConfig configScoreSetByNodeIgnore;
	@Mock
	private AssessmentConfig configScoreEvaluated;
	@Mock
	private AssessmentConfig configScoreNone;
	@Mock
	private RepositoryEntryRef courseEntry;

	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	private AssessmentCounter sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		
		when(configPassedEvaluated.getPassedMode()).thenReturn(Mode.evaluated);
		when(configPassedSetByNode.getPassedMode()).thenReturn(Mode.setByNode);
		when(configPassedSetByNode.ignoreInCourseAssessment()).thenReturn(Boolean.FALSE);
		when(configPassedSetByNodeIgnore.getPassedMode()).thenReturn(Mode.setByNode);
		when(configPassedSetByNodeIgnore.ignoreInCourseAssessment()).thenReturn(Boolean.TRUE);
		when(configPassedNone.getPassedMode()).thenReturn(Mode.none);
		when(configScoreEvaluated.getScoreMode()).thenReturn(Mode.evaluated);
		when(configScoreSetByNode.getScoreMode()).thenReturn(Mode.setByNode);
		when(configScoreSetByNode.ignoreInCourseAssessment()).thenReturn(Boolean.FALSE);
		when(configScoreSetByNodeIgnore.getScoreMode()).thenReturn(Mode.setByNode);
		when(configScoreSetByNodeIgnore.ignoreInCourseAssessment()).thenReturn(Boolean.TRUE);
		when(configScoreNone.getScoreMode()).thenReturn(Mode.none);
		
		sut = new AssessmentCounter();
	}

	@Test
	public void shouldGetCummulatingScore() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		
		// Pass
		// Child: set by node, passed, user visible
		CourseNode childPassed11 = new Card2BrainCourseNode();
		parent.addChild(childPassed11);
		AssessmentEvaluation childPassed11Evaluation = createAssessmentEvaluation(Boolean.TRUE, null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed11, childPassed11Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed11)).thenReturn(configPassedSetByNode);
		// Child: set by node, not passed, user visible
		CourseNode childPassed12 = new Card2BrainCourseNode();
		parent.addChild(childPassed12);
		AssessmentEvaluation childPassed12Evaluation = createAssessmentEvaluation(Boolean.FALSE, null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed12, childPassed12Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed12)).thenReturn(configPassedSetByNode);
		// Child: set by node, not passed, not user visible
		CourseNode childPassed13 = new Card2BrainCourseNode();
		parent.addChild(childPassed13);
		AssessmentEvaluation childPassed13Evaluation = createAssessmentEvaluation(Boolean.FALSE, null, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed13, childPassed13Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed13)).thenReturn(configPassedSetByNode);
		// Child: set by node, passed, not user visible
		CourseNode childPassed14 = new Card2BrainCourseNode();
		parent.addChild(childPassed14);
		AssessmentEvaluation childPassed14Evaluation = createAssessmentEvaluation(Boolean.TRUE, null, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed14, childPassed14Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed14)).thenReturn(configPassedSetByNode);
		// Child: none, passed, user visible
		CourseNode childPassed15 = new Card2BrainCourseNode();
		parent.addChild(childPassed15);
		AssessmentEvaluation childPassed15Evaluation = createAssessmentEvaluation(Boolean.TRUE, null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed15, childPassed15Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed15)).thenReturn(configPassedNone);
		// Child: set by node, null passed, not user visible
		CourseNode childPassed16 = new Card2BrainCourseNode();
		parent.addChild(childPassed16);
		AssessmentEvaluation childPassed16Evaluation = createAssessmentEvaluation(null, null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed16, childPassed16Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed16)).thenReturn(configPassedSetByNode);
		// Child: evaluated, passed, user visible
		CourseNode childPassed17 = new STCourseNode();
		parent.addChild(childPassed17);
		AssessmentEvaluation childPassed17Evaluation = createAssessmentEvaluation(Boolean.TRUE, null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed17, childPassed17Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed17)).thenReturn(configPassedEvaluated);
		// Child: set by node, not passed, user visible, ignored
		CourseNode childPassed18 = new Card2BrainCourseNode();
		parent.addChild(childPassed18);
		AssessmentEvaluation childPassed18Evaluation = createAssessmentEvaluation(Boolean.FALSE, null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed18, childPassed18Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed18)).thenReturn(configPassedSetByNodeIgnore);
		// Child: set by node, not passed, user visible, invisible obligation
		CourseNode childPassed19 = new Card2BrainCourseNode();
		parent.addChild(childPassed19);
		AssessmentEvaluation childPassed19Evaluation = createAssessmentEvaluation(Boolean.FALSE, null, Boolean.TRUE, AssessmentObligation.excluded);
		scoreAccounting.put(childPassed19, childPassed19Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed19)).thenReturn(configPassedSetByNode);
		
		// Child level 2: set by node, passed, user visible
		CourseNode childPassed21 = new Card2BrainCourseNode();
		childPassed17.addChild(childPassed21);
		AssessmentEvaluation childPassed21Evaluation = createAssessmentEvaluation(Boolean.TRUE, null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childPassed21, childPassed21Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childPassed21)).thenReturn(configPassedSetByNode);
		
		// Score
		CourseNode childScore11 = new Card2BrainCourseNode();
		parent.addChild(childScore11);
		AssessmentEvaluation childScore11Evaluation = createAssessmentEvaluation(null, 1f, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore11, childScore11Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore11)).thenReturn(configScoreSetByNode);
		
		CourseNode childScore12 = new Card2BrainCourseNode();
		parent.addChild(childScore12);
		AssessmentEvaluation childScore12Evaluation = createAssessmentEvaluation(null, 0f, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore12, childScore12Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore12)).thenReturn(configScoreSetByNode);
		
		CourseNode childScore13 = new Card2BrainCourseNode();
		parent.addChild(childScore13);
		AssessmentEvaluation childScore13Evaluation = createAssessmentEvaluation(null, 0f, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore13, childScore13Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore13)).thenReturn(configScoreSetByNode);
		
		CourseNode childScore14 = new Card2BrainCourseNode();
		parent.addChild(childScore14);
		AssessmentEvaluation childScore14Evaluation = createAssessmentEvaluation(null, 1f, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore14, childScore14Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore14)).thenReturn(configScoreSetByNode);
		
		CourseNode childScore15 = new Card2BrainCourseNode();
		parent.addChild(childScore15);
		AssessmentEvaluation childScore15Evaluation = createAssessmentEvaluation(null, 1f, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore15, childScore15Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore15)).thenReturn(configScoreNone);
		
		CourseNode childScore16 = new Card2BrainCourseNode();
		parent.addChild(childScore16);
		AssessmentEvaluation childScore16Evaluation = createAssessmentEvaluation(null, null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore16, childScore16Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore16)).thenReturn(configScoreSetByNode);
		
		CourseNode childScore17 = new STCourseNode();
		parent.addChild(childScore17);
		AssessmentEvaluation childScore17Evaluation = createAssessmentEvaluation(null, 1f, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore17, childScore17Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore17)).thenReturn(configScoreEvaluated);
		
		CourseNode childScore18 = new Card2BrainCourseNode();
		parent.addChild(childScore18);
		AssessmentEvaluation childScore18Evaluation = createAssessmentEvaluation(null, 0f, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore18, childScore18Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore18)).thenReturn(configScoreSetByNodeIgnore);
		
		CourseNode childScore19 = new Card2BrainCourseNode();
		parent.addChild(childScore19);
		AssessmentEvaluation childScore19Evaluation = createAssessmentEvaluation(null, 0f, Boolean.TRUE, AssessmentObligation.excluded);
		scoreAccounting.put(childScore19, childScore19Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore19)).thenReturn(configScoreSetByNode);
		
		CourseNode childScore21 = new Card2BrainCourseNode();
		childScore17.addChild(childScore21);
		AssessmentEvaluation childScore21Evaluation = createAssessmentEvaluation(null, 1f, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(childScore21, childScore21Evaluation);
		when(courseAssessmentService.getAssessmentConfig(courseEntry, childScore21)).thenReturn(configScoreSetByNode);
		
		AssessmentCounts counts = sut.getCounts(courseEntry, parent, scoreAccounting, courseAssessmentService);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(counts.getNumAssessable()).isEqualTo(12);
		softly.assertThat(counts.getNumUserVisible()).isEqualTo(8);
		softly.assertAll();
	}
	
	private AssessmentEvaluation createAssessmentEvaluation(Boolean passed, Float score, Boolean userVisibility, AssessmentObligation obligation) {
		return new AssessmentEvaluation(score, null, null, null, null, null, null, null, passed, null, null, null, null, null, userVisibility, null,
				null, null, null, null, null, null, null, 0, null, null, null, null, null, null, ObligationOverridable.of(obligation), null, null, null);
	}
}
