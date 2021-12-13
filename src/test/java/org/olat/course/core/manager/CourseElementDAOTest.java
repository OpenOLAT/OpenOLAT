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
import static org.assertj.core.api.Assertions.offset;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.course.assessment.AssessmentConfigMock;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.core.CourseElement;
import org.olat.course.core.CourseElementSearchParams;
import org.olat.course.core.model.CourseElementImpl;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Dec 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseElementDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private CourseElementDAO sut;

	@Test
	public void shouldCreateCourseElement() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode courseNode = new SPCourseNode();
		String shortTitle = miniRandom();
		courseNode.setShortTitle(shortTitle);
		String longTitle = random();
		courseNode.setLongTitle(longTitle);
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setAssessable(true);
		assessmentConfig.setScoreMode(Mode.none);
		assessmentConfig.setPassedMode(Mode.setByNode);
		assessmentConfig.setCutValue(Float.valueOf(2.4f));
		
		CourseElement courseElement = sut.create(entry, courseNode, assessmentConfig);
		dbInstance.commitAndCloseSession();
		
		assertThat(courseElement.getKey()).isNotNull();
		assertThat(((CourseElementImpl)courseElement).getCreationDate()).isNotNull();
		assertThat(((CourseElementImpl)courseElement).getLastModified()).isNotNull();
		assertThat(courseElement.getType()).isEqualTo(courseNode.getType());
		assertThat(courseElement.getShortTitle()).isEqualTo(shortTitle);
		assertThat(courseElement.getLongTitle()).isEqualTo(longTitle);
		assertThat(courseElement.isAssesseable()).isTrue();
		assertThat(courseElement.getScoreMode()).isEqualTo(Mode.none);
		assertThat(courseElement.getPassedMode()).isEqualTo(Mode.setByNode);
		assertThat(courseElement.getCutValue().floatValue()).isCloseTo(assessmentConfig.getCutValue(), offset(0.001f));
	}
	
	@Test
	public void shouldUpdate() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode courseNode = new SPCourseNode();
		courseNode.setShortTitle(miniRandom());
		courseNode.setLongTitle(random());
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setAssessable(true);
		assessmentConfig.setScoreMode(Mode.none);
		assessmentConfig.setPassedMode(Mode.setByNode);
		assessmentConfig.setCutValue(Float.valueOf(2.4f));
		CourseElement courseElement = sut.create(entry, courseNode, assessmentConfig);
		dbInstance.commitAndCloseSession();
		
		String shortTitle = miniRandom();
		courseNode.setShortTitle(shortTitle);
		String longTitle = random();
		courseNode.setLongTitle(longTitle);
		assessmentConfig.setAssessable(false);
		assessmentConfig.setScoreMode(Mode.evaluated);
		assessmentConfig.setPassedMode(Mode.evaluated);
		assessmentConfig.setCutValue(Float.valueOf(1.1f));
		courseElement = sut.update(courseElement, courseNode, assessmentConfig);
		dbInstance.commitAndCloseSession();
		
		assertThat(courseElement.getType()).isEqualTo(courseNode.getType());
		assertThat(courseElement.getShortTitle()).isEqualTo(shortTitle);
		assertThat(courseElement.getLongTitle()).isEqualTo(longTitle);
		assertThat(courseElement.isAssesseable()).isFalse();
		assertThat(courseElement.getScoreMode()).isEqualTo(Mode.evaluated);
		assertThat(courseElement.getPassedMode()).isEqualTo(Mode.evaluated);
		assertThat(courseElement.getCutValue().floatValue()).isCloseTo(assessmentConfig.getCutValue(), offset(0.001f));
	}
	
	@Test
	public void shouldLoadByRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseElement courseElement1 = createRandomCourseElement(entry);
		CourseElement courseElement2 = createRandomCourseElement(entry);
		CourseElement courseElementOther = createRandomCourseElement(entryOther);
		dbInstance.commitAndCloseSession();
		
		CourseElementSearchParams searchParams = new CourseElementSearchParams();
		searchParams.setRepositoryEntries(Collections.singletonList(entry));
		List<CourseElement> courseElements = sut.load(searchParams);
		
		assertThat(courseElements)
				.containsExactlyInAnyOrder(courseElement1, courseElement2)
				.doesNotContain(courseElementOther);
	}
	
	@Test
	public void shouldDeleteByIds() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseElement courseElement1 = createRandomCourseElement(entry);
		CourseElement courseElement2 = createRandomCourseElement(entry);
		CourseElement courseElement3 = createRandomCourseElement(entry);
		dbInstance.commitAndCloseSession();
		
		sut.delete(List.of(courseElement1, courseElement2));
		dbInstance.commitAndCloseSession();
		
		CourseElementSearchParams searchParams = new CourseElementSearchParams();
		searchParams.setRepositoryEntries(Collections.singletonList(entry));
		List<CourseElement> courseElements = sut.load(searchParams);
		assertThat(courseElements)
				.containsExactlyInAnyOrder(courseElement3)
				.doesNotContain(courseElement1, courseElement2);
	}
	
	@Test
	public void shouldDeleteByRepositoryEntry() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseElement courseElement11 = createRandomCourseElement(entry1);
		CourseElement courseElement12 = createRandomCourseElement(entry1);
		CourseElement courseElement13 = createRandomCourseElement(entry1);
		CourseElement courseElement21 = createRandomCourseElement(entry2);
		((CourseElementImpl)courseElement21).setSubIdent(courseElement11.getSubIdent());
		sut.update(courseElement21);
		dbInstance.commitAndCloseSession();
		
		sut.delete(entry1);
		dbInstance.commitAndCloseSession();
		
		CourseElementSearchParams searchParams = new CourseElementSearchParams();
		searchParams.setRepositoryEntries(List.of(entry1, entry2));
		List<CourseElement> courseElements = sut.load(searchParams);
		assertThat(courseElements)
				.containsExactlyInAnyOrder(courseElement21)
				.doesNotContain(courseElement11, courseElement12, courseElement13);
	}
	
	@Test
	public void shouldDeleteByRepositoryEntryAndSubidents() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseElement courseElement11 = createRandomCourseElement(entry1);
		CourseElement courseElement12 = createRandomCourseElement(entry1);
		CourseElement courseElement13 = createRandomCourseElement(entry1);
		CourseElement courseElement21 = createRandomCourseElement(entry2);
		((CourseElementImpl)courseElement21).setSubIdent(courseElement11.getSubIdent());
		sut.update(courseElement21);
		dbInstance.commitAndCloseSession();
		
		sut.delete(entry1, List.of(courseElement11.getSubIdent(), courseElement12.getSubIdent()));
		dbInstance.commitAndCloseSession();
		
		CourseElementSearchParams searchParams = new CourseElementSearchParams();
		searchParams.setRepositoryEntries(List.of(entry1, entry2));
		List<CourseElement> courseElements = sut.load(searchParams);
		assertThat(courseElements)
				.containsExactlyInAnyOrder(courseElement13, courseElement21)
				.doesNotContain(courseElement11, courseElement12);
	}

	private CourseElement createRandomCourseElement(RepositoryEntry entry) {
		CourseNode courseNode = new SPCourseNode();
		String shortTitle = miniRandom();
		courseNode.setShortTitle(shortTitle);
		String longTitle = random();
		courseNode.setLongTitle(longTitle);
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setAssessable(true);
		assessmentConfig.setScoreMode(Mode.none);
		assessmentConfig.setPassedMode(Mode.setByNode);
		assessmentConfig.setCutValue(Float.valueOf(2.4f));
		
		return sut.create(entry, courseNode, assessmentConfig);
	}

}
