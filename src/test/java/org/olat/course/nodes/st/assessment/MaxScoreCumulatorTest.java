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
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.Card2BrainCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.assessment.MaxScoreCumulator.MaxScore;

/**
 * 
 * Initial date: 20.03.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MaxScoreCumulatorTest {

	@Mock
	private CourseAssessmentService courseAssessmentService;
	
	private MaxScoreCumulator sut = new MaxScoreCumulator();
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldGetCumulatedMaxScore() {
		CourseNode parent = new STCourseNode();
		
		CourseNode child1 = new Card2BrainCourseNode();
		parent.addChild(child1);
		AssessmentConfig child1Config = new TestAssessmentConfig(Mode.setByNode, Float.valueOf(10), false);
		when(courseAssessmentService.getAssessmentConfig(child1)).thenReturn(child1Config);
		
		CourseNode child2 = new Card2BrainCourseNode();
		parent.addChild(child2);
		AssessmentConfig child2Config = new TestAssessmentConfig(Mode.setByNode, Float.valueOf(20), false);
		when(courseAssessmentService.getAssessmentConfig(child2)).thenReturn(child2Config);
		
		CourseNode child3 = new Card2BrainCourseNode();
		parent.addChild(child3);
		AssessmentConfig child3Config = new TestAssessmentConfig(Mode.setByNode, Float.valueOf(5), true);
		when(courseAssessmentService.getAssessmentConfig(child3)).thenReturn(child3Config);
		
		CourseNode child4 = new Card2BrainCourseNode();
		parent.addChild(child4);
		AssessmentConfig child4Config = new TestAssessmentConfig(Mode.setByNode, null, false);
		when(courseAssessmentService.getAssessmentConfig(child4)).thenReturn(child4Config);
		
		CourseNode child5 = new Card2BrainCourseNode();
		parent.addChild(child5);
		AssessmentConfig child5Config = new TestAssessmentConfig(Mode.none, Float.valueOf(10), false);
		when(courseAssessmentService.getAssessmentConfig(child5)).thenReturn(child5Config);
		
		CourseNode child6 = new Card2BrainCourseNode();
		parent.addChild(child6);
		AssessmentConfig child6Config = new TestAssessmentConfig(Mode.evaluated, Float.valueOf(10), false);
		when(courseAssessmentService.getAssessmentConfig(child6)).thenReturn(child6Config);
		
		CourseNode child11 = new Card2BrainCourseNode();
		child6.addChild(child11);
		AssessmentConfig child11Config = new TestAssessmentConfig(Mode.setByNode, Float.valueOf(50), false);
		when(courseAssessmentService.getAssessmentConfig(child11)).thenReturn(child11Config);
		
		MaxScore maxScore = sut.getMaxScore(parent, courseAssessmentService);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(maxScore.getSum()).isEqualTo(80);
		softly.assertThat(maxScore.getMax()).isEqualTo(50);
		softly.assertAll();
	}
	
	@Test
	public void shouldReturnNullIfNoChildrenWithScore() {
		CourseNode parent = new STCourseNode();
		
		CourseNode child1 = new Card2BrainCourseNode();
		parent.addChild(child1);
		AssessmentConfig child1Config = new TestAssessmentConfig(Mode.evaluated, Float.valueOf(10), false);
		when(courseAssessmentService.getAssessmentConfig(child1)).thenReturn(child1Config);
		
		MaxScore maxScore = sut.getMaxScore(parent, courseAssessmentService);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(maxScore.getSum()).isNull();
		softly.assertThat(maxScore.getMax()).isNull();
		softly.assertAll();
	}
	
	private static final class TestAssessmentConfig implements AssessmentConfig {

		private final Mode scoreMode;
		private final Float maxScore;
		private boolean ignoreInCourseAssessment;

		public TestAssessmentConfig(Mode scoreMode, Float maxScore, boolean ignoreInCourseAssessment) {
			this.scoreMode = scoreMode;
			this.maxScore = maxScore;
			this.ignoreInCourseAssessment = ignoreInCourseAssessment;
		}

		@Override
		public boolean isAssessable() {
			return false;
		}

		@Override
		public boolean ignoreInCourseAssessment() {
			return ignoreInCourseAssessment;
		}

		@Override
		public void setIgnoreInCourseAssessment(boolean ignoreInCourseAssessment) {
			this.ignoreInCourseAssessment = ignoreInCourseAssessment;
		}

		@Override
		public Mode getScoreMode() {
			return scoreMode;
		}

		@Override
		public Float getMaxScore() {
			return maxScore;
		}

		@Override
		public Float getMinScore() {
			return null;
		}
		
		@Override
		public boolean hasGrade() {
			return false;
		}
		
		@Override
		public boolean isAutoGrade() {
			return false;
		}

		@Override
		public Mode getPassedMode() {
			return null;
		}

		@Override
		public Float getCutValue() {
			return null;
		}

		@Override
		public boolean isPassedOverridable() {
			return false;
		}

		@Override
		public Mode getCompletionMode() {
			return null;
		}

		@Override
		public boolean hasAttempts() {
			return false;
		}

		@Override
		public boolean hasMaxAttempts() {
			return false;
		}

		@Override
		public Integer getMaxAttempts() {
			return null;
		}

		@Override
		public boolean hasComment() {
			return false;
		}

		@Override
		public boolean hasIndividualAsssessmentDocuments() {
			return false;
		}

		@Override
		public boolean hasStatus() {
			return false;
		}

		@Override
		public boolean isAssessedBusinessGroups() {
			return false;
		}

		@Override
		public boolean isEditable() {
			return false;
		}

		@Override
		public boolean isBulkEditable() {
			return false;
		}

		@Override
		public boolean hasEditableDetails() {
			return false;
		}

		@Override
		public boolean isExternalGrading() {
			return false;
		}

		@Override
		public boolean isObligationOverridable() {
			return false;
		}
		
	}

}
