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
import static org.olat.test.JunitTestHelper.random;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.course.core.CourseElement;
import org.olat.course.core.CourseElementSearchParams;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dec 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private CourseNodeServiceImpl sut;

	@Test
	public void shouldSyncCourseElements() {
		// Init
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode cn1 = createCourseNode(random());
		CourseNode cn2 = createCourseNode(random());
		CourseNode cn3 = createCourseNode(random());
		List<CourseNode> courseNodes = List.of(cn1, cn2, cn3);
		List<CourseElement> courseElements = Collections.emptyList();
		sut.syncCourseElements(courseEntry, courseNodes, courseElements);
		dbInstance.commitAndCloseSession();
		
		CourseElementSearchParams searchParams = new CourseElementSearchParams();
		searchParams.setRepositoryEntries(Collections.singletonList(courseEntry));
		courseElements = sut.getCourseElements(searchParams);
		assertThat(courseElements).hasSize(3);
		
		//Update
		CourseNode cn4 = createCourseNode(random());
		CourseNode cn5 = createCourseNode(random());
		courseNodes = List.of(cn1, cn2, cn4, cn5);
		sut.syncCourseElements(courseEntry, courseNodes, courseElements);
		dbInstance.commitAndCloseSession();
		
		courseElements = sut.getCourseElements(searchParams);
		assertThat(courseElements).hasSize(4);
		assertThat(courseElements).extracting(CourseElement::getSubIdent)
				.containsExactlyInAnyOrder(
						cn1.getIdent(),
						cn2.getIdent(),
						cn4.getIdent(),
						cn5.getIdent()
				);
	}
	
	private CourseNode createCourseNode(String title) {
		CourseNode courseNode = new SPCourseNode();
		courseNode.setIdent(random());
		courseNode.setLongTitle(title);
		return courseNode;
	}


}
