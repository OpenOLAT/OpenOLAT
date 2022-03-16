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
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
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
		
		when(configWithPassed.getPassedMode()).thenReturn(Mode.setByNode);
		when(configWithoutPassed.getPassedMode()).thenReturn(Mode.none);
		
		sut= new ConventionalSTCompletionEvaluator(courseAssessmentService);
	}
	
	@Test
	public void shouldReturnNullIfItIsNotRoot() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		CourseNode child = new STCourseNode();
		root.addChild(child);
		AssessmentEvaluation parrentEvaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE);
		scoreAccounting.put(child, parrentEvaluation);
		when(courseAssessmentService.getAssessmentConfig(child)).thenReturn(configWithPassed);
		
		Double completion = sut.getCompletion(parrentEvaluation, child, scoreAccounting);
		
		assertThat(completion).isNull();
	}
	
	@Test
	public void shouldReturnNullIfHasNoPassConfig() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		AssessmentEvaluation parrentEvaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE);
		scoreAccounting.put(root, parrentEvaluation);
		when(courseAssessmentService.getAssessmentConfig(root)).thenReturn(configWithoutPassed);
		
		Double completion = sut.getCompletion(parrentEvaluation, root, scoreAccounting);
		
		assertThat(completion).isNull();
	}
	
	@Test
	public void shouldReturn0IfIsFailed() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		AssessmentEvaluation parrentEvaluation = createAssessmentEvaluation(Boolean.FALSE, Boolean.TRUE);
		scoreAccounting.put(root, parrentEvaluation);
		when(courseAssessmentService.getAssessmentConfig(root)).thenReturn(configWithPassed);
		
		Double completion = sut.getCompletion(parrentEvaluation, root, scoreAccounting);
		
		assertThat(completion).isEqualTo(0.0);
	}
	
	@Test
	public void shouldReturn0IfNOtPassedYet() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		AssessmentEvaluation parrentEvaluation = createAssessmentEvaluation(null, Boolean.TRUE);
		scoreAccounting.put(root, parrentEvaluation);
		when(courseAssessmentService.getAssessmentConfig(root)).thenReturn(configWithPassed);
		
		Double completion = sut.getCompletion(parrentEvaluation, root, scoreAccounting);
		
		assertThat(completion).isEqualTo(0.0);
	}
	
	@Test
	public void shouldReturn0IfIsPassedButNotUserVisible() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		AssessmentEvaluation parrentEvaluation = createAssessmentEvaluation(Boolean.TRUE, null);
		scoreAccounting.put(root, parrentEvaluation);
		when(courseAssessmentService.getAssessmentConfig(root)).thenReturn(configWithPassed);
		
		Double completion = sut.getCompletion(parrentEvaluation, root, scoreAccounting);
		
		assertThat(completion).isEqualTo(0.0);
	}
	
	@Test
	public void shouldReturn1IfIsPassedAndUserVisible() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		AssessmentEvaluation parrentEvaluation = createAssessmentEvaluation(Boolean.TRUE, Boolean.TRUE);
		scoreAccounting.put(root, parrentEvaluation);
		when(courseAssessmentService.getAssessmentConfig(root)).thenReturn(configWithPassed);
		
		Double completion = sut.getCompletion(parrentEvaluation, root, scoreAccounting);
		
		assertThat(completion).isEqualTo(1.0);
	}
	
	private AssessmentEvaluation createAssessmentEvaluation(Boolean passed, Boolean userVisibility) {
		return new AssessmentEvaluation(null, null, null, null, passed, null, null, null, null, null, userVisibility, null, null,
				null, null, null, null, null, null, 0, null, null, null, null, null, null, null, null, null, null);
	}
}
