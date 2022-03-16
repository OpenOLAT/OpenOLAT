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
import org.olat.course.nodes.st.assessment.PassCounter.Counts;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 13 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PassCounterTest {

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
	
	private PassCounter sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(configEvaluated.getPassedMode()).thenReturn(Mode.evaluated);
		when(configSetByNode.getPassedMode()).thenReturn(Mode.setByNode);
		when(configSetByNode.ignoreInCourseAssessment()).thenReturn(Boolean.FALSE);
		when(configSetByNodeIgnore.getPassedMode()).thenReturn(Mode.setByNode);
		when(configSetByNodeIgnore.ignoreInCourseAssessment()).thenReturn(Boolean.TRUE);
		when(configNone.getPassedMode()).thenReturn(Mode.none);

		sut = new PassCounter();
	}

	@Test
	public void shouldGetCummulatingScore() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		// Parent
		CourseNode parent = new STCourseNode();
		// Child: set by node, passed, user visible
		CourseNode child11 = new Card2BrainCourseNode();
		parent.addChild(child11);
		AssessmentEvaluation child11Evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child11, child11Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child11)).thenReturn(configSetByNode);
		// Child: set by node, not passed, user visible
		CourseNode child12 = new Card2BrainCourseNode();
		parent.addChild(child12);
		AssessmentEvaluation child12Evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child12, child12Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child12)).thenReturn(configSetByNode);
		// Child: set by node, not passed, not user visible
		CourseNode child13 = new Card2BrainCourseNode();
		parent.addChild(child13);
		AssessmentEvaluation child13Evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(child13, child13Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child13)).thenReturn(configSetByNode);
		// Child: set by node, passed, not user visible
		CourseNode child14 = new Card2BrainCourseNode();
		parent.addChild(child14);
		AssessmentEvaluation child14Evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.FALSE, AssessmentObligation.mandatory);
		scoreAccounting.put(child14, child14Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child14)).thenReturn(configSetByNode);
		// Child: none, passed, user visible
		CourseNode child15 = new Card2BrainCourseNode();
		parent.addChild(child15);
		AssessmentEvaluation child15Evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child15, child15Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child15)).thenReturn(configNone);
		// Child: set by node, null passed, not user visible
		CourseNode child16 = new Card2BrainCourseNode();
		parent.addChild(child16);
		AssessmentEvaluation child16Evaluation = createAssessmentEvaluation(null, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child16, child16Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child16)).thenReturn(configSetByNode);
		// Child: evaluated, passed, user visible
		CourseNode child17 = new STCourseNode();
		parent.addChild(child17);
		AssessmentEvaluation child17Evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child17, child17Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child17)).thenReturn(configEvaluated);
		// Child: set by node, not passed, user visible, ignored
		CourseNode child18 = new Card2BrainCourseNode();
		parent.addChild(child18);
		AssessmentEvaluation child18Evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child18, child18Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child18)).thenReturn(configSetByNodeIgnore);
		// Child: set by node, not passed, user visible, invisible obligation
		CourseNode child19 = new Card2BrainCourseNode();
		parent.addChild(child19);
		AssessmentEvaluation child19Evaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE, AssessmentObligation.excluded);
		scoreAccounting.put(child19, child19Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child19)).thenReturn(configSetByNode);
		
		// Child level 2: set by node, passed, user visible
		CourseNode child21 = new Card2BrainCourseNode();
		child17.addChild(child21);
		AssessmentEvaluation child21Evaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE, AssessmentObligation.mandatory);
		scoreAccounting.put(child21, child21Evaluation);
		when(courseAssessmentService.getAssessmentConfig(child21)).thenReturn(configSetByNode);
		
		Counts counts = sut.getCounts(parent, scoreAccounting, courseAssessmentService);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(counts.getPassable()).isEqualTo(6);
		softly.assertThat(counts.getPassed()).isEqualTo(2);
		softly.assertThat(counts.getFailed()).isEqualTo(1);
		softly.assertAll();
	}
	
	private AssessmentEvaluation createAssessmentEvaluation(Boolean passed, Boolean userVisibility, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, passed, null, null, null, null, null, userVisibility, null, null,
				null, null, null, null, null, null, 0, null, null, null, null, null, null, ObligationOverridable.of(obligation), null, null, null);
	}
}
