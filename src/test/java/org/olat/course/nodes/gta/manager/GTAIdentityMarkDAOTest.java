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
package org.olat.course.nodes.gta.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.TaskListImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GTAIdentityMarkDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
	private GTAIdentityMarkDAO sut;
	
	@Before
	public void emptyTable() {
		String statement = "delete from gtaMark";
		dbInstance.getCurrentEntityManager().createQuery(statement).executeUpdate();
	}

	
	@Test
	public void shouldCreateAndPersistMark() {
		TaskList taskList = createTaskList();
		Identity marker = JunitTestHelper.createAndPersistIdentityAsAuthor("coach");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser("participant");
		dbInstance.commitAndCloseSession();
		
		sut.createAndPersisitMark(taskList, marker, participant);
		dbInstance.commitAndCloseSession();
		
		IdentityMark reloadedMark = sut.loadMarks(taskList, marker).get(0);
		assertThat(reloadedMark).isNotNull();
		assertThat(reloadedMark.getCreationDate()).isNotNull();
		assertThat(reloadedMark.getLastModified()).isNotNull();
		assertThat(reloadedMark.getTaskList()).isEqualTo(taskList);
		assertThat(reloadedMark.getMarker()).isEqualTo(marker);
		assertThat(reloadedMark.getParticipant()).isEqualTo(participant);
	}
	
	@Test
	public void shouldLoadAllMarksOfAMarker() {
		TaskList taskList = createTaskList();
		TaskList otherTaskList = createTaskList();
		Identity marker = JunitTestHelper.createAndPersistIdentityAsAuthor("coach");
		Identity otherMarker = JunitTestHelper.createAndPersistIdentityAsAuthor("otherCoach");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsUser("participant1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsUser("participant2");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsUser("participant3");
		IdentityMark mark1 = sut.createAndPersisitMark(taskList, marker, participant1);
		IdentityMark mark2 = sut.createAndPersisitMark(taskList, marker, participant2);
		sut.createAndPersisitMark(otherTaskList, marker, participant3);
		sut.createAndPersisitMark(taskList, otherMarker, participant3);
		IdentityMark mark3 = sut.createAndPersisitMark(taskList, marker, participant3);
		dbInstance.commitAndCloseSession();
		
		List<IdentityMark> marks = sut.loadMarks(taskList, marker);
		
		assertThat(marks).hasSize(3);
		assertThat(marks).containsExactlyInAnyOrder(mark1, mark2, mark3);
	}
	
	@Test
	public void shouldCheckIfMarked() {
		TaskList taskList = createTaskList();
		Identity marker = JunitTestHelper.createAndPersistIdentityAsAuthor("coach");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser("participant");
		sut.createAndPersisitMark(taskList, marker, participant);
		dbInstance.commitAndCloseSession();
		
		boolean isMarked = sut.isMarked(taskList, marker, participant);
		
		assertThat(isMarked).isTrue();
	}
	
	@Test
	public void shouldCheckIfNotMarked() {
		TaskList taskList = createTaskList();
		Identity marker = JunitTestHelper.createAndPersistIdentityAsAuthor("coach");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser("participant");
		sut.createAndPersisitMark(taskList, marker, participant);
		Identity participantNotMarked = JunitTestHelper.createAndPersistIdentityAsUser("participantNotMarked");
		dbInstance.commitAndCloseSession();
		
		boolean isMarked = sut.isMarked(taskList, marker, participantNotMarked);
		
		assertThat(isMarked).isFalse();
	}
	
	@Test
	public void shouldCheckIfHasMarks() {
		TaskList taskList = createTaskList();
		Identity marker = JunitTestHelper.createAndPersistIdentityAsAuthor("coach");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsUser("participant1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsUser("participant2");
		sut.createAndPersisitMark(taskList, marker, participant1);
		sut.createAndPersisitMark(taskList, marker, participant2);
		dbInstance.commitAndCloseSession();
		
		boolean hasMarks = sut.hasMarks(taskList, marker);
		
		assertThat(hasMarks).isTrue();
	}
	
	@Test
	public void shouldCheckIfHasNoMarks() {
		TaskList taskList = createTaskList();
		TaskList otherTaskList = createTaskList();
		Identity marker = JunitTestHelper.createAndPersistIdentityAsAuthor("coach");
		Identity otherMarker = JunitTestHelper.createAndPersistIdentityAsAuthor("otherCoach");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser("participant3");
		sut.createAndPersisitMark(otherTaskList, marker, participant);
		sut.createAndPersisitMark(taskList, otherMarker, participant);
		dbInstance.commitAndCloseSession();
		
		boolean hasMarks = sut.hasMarks(taskList, marker);
		
		assertThat(hasMarks).isFalse();
	}
	
	@Test
	public void shouldDeleteMarkOfAMarker() {
		TaskList taskList = createTaskList();
		TaskList otherTaskList = createTaskList();
		Identity marker = JunitTestHelper.createAndPersistIdentityAsAuthor("coach");
		Identity otherMarker = JunitTestHelper.createAndPersistIdentityAsAuthor("otherCoach");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsUser("participant1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsUser("participant2");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsUser("participant3");
		IdentityMark mark1 = sut.createAndPersisitMark(taskList, marker, participant1);
		sut.createAndPersisitMark(taskList, marker, participant2);
		sut.createAndPersisitMark(otherTaskList, marker, participant3);
		sut.createAndPersisitMark(taskList, otherMarker, participant3);
		IdentityMark mark3 = sut.createAndPersisitMark(taskList, marker, participant3);
		dbInstance.commitAndCloseSession();
		
		sut.deleteMark(taskList, marker, participant2);
		dbInstance.commitAndCloseSession();
		
		List<IdentityMark> marks = sut.loadMarks(taskList, marker);
		assertThat(marks).hasSize(2);
		assertThat(marks).containsExactlyInAnyOrder(mark1, mark3);
	}
	
	@Test
	public void shouldDeleteMarkOfATaskList() {
		TaskList taskList = createTaskList();
		TaskList otherTaskList = createTaskList();
		Identity marker = JunitTestHelper.createAndPersistIdentityAsAuthor("coach");
		Identity otherMarker = JunitTestHelper.createAndPersistIdentityAsAuthor("otherCoach");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsUser("participant1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsUser("participant2");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsUser("participant3");
		sut.createAndPersisitMark(taskList, marker, participant1);
		sut.createAndPersisitMark(taskList, marker, participant2);
		sut.createAndPersisitMark(otherTaskList, marker, participant3);
		sut.createAndPersisitMark(taskList, otherMarker, participant3);
		sut.createAndPersisitMark(taskList, marker, participant3);
		dbInstance.commitAndCloseSession();
		
		int numberDeleted = sut.deleteMark(taskList);
		dbInstance.commitAndCloseSession();
		
		assertThat(numberDeleted).isSameAs(4);
		List<IdentityMark> marksOfDeletedTaskList = sut.loadMarks(taskList, marker);
		assertThat(marksOfDeletedTaskList).hasSize(0);
		List<IdentityMark> marksOfExistinTaskList = sut.loadMarks(otherTaskList, marker);
		assertThat(marksOfExistinTaskList).hasSize(1);
	}
	

	private TaskList createTaskList() {
		TaskListImpl tasksImpl = new TaskListImpl();
		Date creationDate = new Date();
		tasksImpl.setCreationDate(creationDate);
		tasksImpl.setLastModified(creationDate);
		tasksImpl.setEntry(JunitTestHelper.createAndPersistRepositoryEntry());
		tasksImpl.setCourseNodeIdent(UUID.randomUUID().toString());
		dbInstance.getCurrentEntityManager().persist(tasksImpl);
		return tasksImpl;
	}
	
}
