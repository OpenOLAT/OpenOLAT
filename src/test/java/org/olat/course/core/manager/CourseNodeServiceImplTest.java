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
package org.olat.course.core.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;
import org.olat.course.assessment.AssessmentConfigMock;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.core.CourseElementMock;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;

/**
 * 
 * Initial date: 3 Dec 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeServiceImplTest {
	
	private CourseNodeServiceImpl sut = new CourseNodeServiceImpl();

	@Test
	public void shouldCompareCourseElementAndCourseNodeBySortTitle() {
		CourseElementMock courseElement = new CourseElementMock();
		courseElement.setShortTitle("short");
		courseElement.setLongTitle("long");
		CourseNode courseNode = new SPCourseNode();
		courseNode.setShortTitle("short");
		courseNode.setLongTitle("long");
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isTrue();
		
		courseElement.setShortTitle("new");
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isFalse();
	}

	@Test
	public void shouldCompareCourseElementAndCourseNodeByLongTitle() {
		CourseElementMock courseElement = new CourseElementMock();
		courseElement.setShortTitle("short");
		courseElement.setLongTitle("long");
		CourseNode courseNode = new SPCourseNode();
		courseNode.setShortTitle("short");
		courseNode.setLongTitle("long");
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isTrue();
		
		courseElement.setLongTitle("new");
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isFalse();
	}
	
	@Test
	public void shouldCompareCourseElementAndCourseNodeByAssessable() {
		CourseElementMock courseElement = new CourseElementMock();
		courseElement.setAssesseable(true);
		CourseNode courseNode = new SPCourseNode();
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setAssessable(true);
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isTrue();
		
		assessmentConfig.setAssessable(false);
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isFalse();
	}
	
	@Test
	public void shouldCompareCourseElementAndCourseNodeByScoreMode() {
		CourseElementMock courseElement = new CourseElementMock();
		courseElement.setScoreMode(Mode.setByNode);
		CourseNode courseNode = new SPCourseNode();
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setScoreMode(Mode.setByNode);
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isTrue();
		
		assessmentConfig.setScoreMode(Mode.none);
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isFalse();
	}
	
	@Test
	public void shouldCompareCourseElementAndCourseNodeByPassedMode() {
		CourseElementMock courseElement = new CourseElementMock();
		courseElement.setPassedMode(Mode.setByNode);
		CourseNode courseNode = new SPCourseNode();
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setPassedMode(Mode.setByNode);
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isTrue();
		
		assessmentConfig.setPassedMode(Mode.none);
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isFalse();
	}
	
	@Test
	public void shouldCompareCourseElementAndCourseNodeByCutValue() {
		CourseElementMock courseElement = new CourseElementMock();
		courseElement.setCutValue(BigDecimal.ONE);
		CourseNode courseNode = new SPCourseNode();
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setCutValue(1f);
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isTrue();
		
		assessmentConfig.setCutValue(1.1f);
		assertThat(sut.isSame(courseElement, courseNode, assessmentConfig)).isFalse();
	}

}
