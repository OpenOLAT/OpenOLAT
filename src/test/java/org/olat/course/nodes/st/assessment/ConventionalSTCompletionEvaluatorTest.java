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
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.MappedScoreAccounting;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.Card2BrainCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;

/**
 * 
 * Initial date: 22 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConventionalSTCompletionEvaluatorTest {
	
	@Mock
	private AssessmentConfig configWithPassed;
	@Mock
	private AssessmentConfig configWithoutPassed;
	
	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	private ConventionalSTCompletionEvaluator sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(configWithPassed.hasPassed()).thenReturn(Boolean.TRUE);
		when(configWithoutPassed.hasPassed()).thenReturn(Boolean.FALSE);
		
		sut= new ConventionalSTCompletionEvaluator(courseAssessmentService);
	}
	
	@Test
	public void shouldReturn1IfCondittionIsPassed() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode parent = new STCourseNode();
		AssessmentEvaluation parrentEvaluation = createAssessmentEvaluation(Boolean.TRUE, null);
		scoreAccounting.put(parent, parrentEvaluation);
		
		Double completion = sut.getCompletion(parrentEvaluation, parent, scoreAccounting);
		
		assertThat(completion).isEqualByComparingTo(1.0);
	}
	
	@Test
	public void shouldReturnNumberOfPassedNodes() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode parent = new STCourseNode();
		AssessmentEvaluation parrentEvaluation = createAssessmentEvaluation(Boolean.FALSE, null);
		scoreAccounting.put(parent, parrentEvaluation);
		// Child with passed configured: passed
		CourseNode child1 = new Card2BrainCourseNode();
		parent.addChild(child1);
		AssessmentEvaluation assessedEvaluation1 = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE);
		scoreAccounting.put(child1, assessedEvaluation1);
		when(courseAssessmentService.getAssessmentConfig(child1)).thenReturn(configWithPassed);
		// Child with passed configured: passed but not user visible
		CourseNode child2= new Card2BrainCourseNode();
		parent.addChild(child2);
		AssessmentEvaluation assessedEvaluation2 = createAssessmentEvaluation(Boolean.TRUE, Boolean.FALSE);
		scoreAccounting.put(child2, assessedEvaluation2);
		when(courseAssessmentService.getAssessmentConfig(child2)).thenReturn(configWithPassed);
		// Child with passed configured: not passed
		CourseNode child3 = new Card2BrainCourseNode();
		parent.addChild(child3);
		AssessmentEvaluation assessedEvaluation3 = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE);
		scoreAccounting.put(child3, assessedEvaluation3);
		when(courseAssessmentService.getAssessmentConfig(child3)).thenReturn(configWithPassed);
		// Child without passed configured:
		CourseNode child4 = new Card2BrainCourseNode();
		parent.addChild(child4);
		AssessmentEvaluation assessedEvaluation4 = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE);
		scoreAccounting.put(child4, assessedEvaluation4);
		when(courseAssessmentService.getAssessmentConfig(child4)).thenReturn(configWithPassed);
		
		Double completion = sut.getCompletion(parrentEvaluation, parent, scoreAccounting);
		
		assertThat(completion).isEqualByComparingTo(0.5);
	}
	
	private AssessmentEvaluation createAssessmentEvaluation(Boolean passed, Boolean userVisibility) {
		return new AssessmentEvaluation(null, passed, null, null, null, userVisibility, null, null, null, null, null,
				null, null, 0, null, null, null, null, null, null, null, null, null, null);
	}
}
