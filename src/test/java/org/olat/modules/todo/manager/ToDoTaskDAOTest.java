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
package org.olat.modules.todo.manager;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addHours;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ToDoService toDoService;
	
	@Autowired
	private ToDoTaskDAO sut;
	
	@Test
	public void shouldCreate() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		String type = random();
		Long originId = Long.valueOf(33);
		String originSubPath = random();
		ToDoTask toDoTask = sut.create(identity, type, originId, originSubPath);
		dbInstance.commitAndCloseSession();
		
		assertThat(toDoTask).isNotNull();
		assertThat(toDoTask.getCreationDate()).isNotNull();
		assertThat(toDoTask.getLastModified()).isNotNull();
		assertThat(toDoTask.getContentModifiedDate()).isNotNull();
		assertThat(toDoTask.getBaseGroup()).isNotNull();
		assertThat(toDoTask.getOriginId()).isEqualTo(originId);
		assertThat(toDoTask.getOriginSubPath()).isEqualTo(originSubPath);
		assertThat(toDoTask.getStatus()).isEqualTo(ToDoStatus.open);
		assertThat(toDoTask.isOriginDeleted()).isFalse();
	}
	
	@Test
	public void shouldUpdate() {
		ToDoTask toDoTask = createRandomToDoTask();
		
		String title = random();
		toDoTask.setTitle(title);
		String description = random();
		toDoTask.setDescription(description);
		ToDoStatus status = ToDoStatus.inProgress;
		toDoTask.setStatus(status);
		ToDoPriority priority = ToDoPriority.low;
		toDoTask.setPriority(priority);
		Long expenditureOfWork = Long.valueOf(4);
		toDoTask.setExpenditureOfWork(expenditureOfWork);
		Date startDate = DateUtils.addDays(new Date(), 1);
		toDoTask.setStartDate(startDate);
		Date dueDate= DateUtils.addDays(new Date(), 2);
		toDoTask.setDueDate(dueDate);
		
		sut.save(toDoTask);
		dbInstance.commitAndCloseSession();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		ToDoTask reloaded = sut.loadToDoTasks(searchParams).get(0);
		
		assertThat(reloaded.getTitle()).isEqualTo(title);
		assertThat(reloaded.getDescription()).isEqualTo(description);
		assertThat(reloaded.getStatus()).isEqualTo(status);
		assertThat(reloaded.getPriority()).isEqualTo(priority);
		assertThat(reloaded.getExpenditureOfWork()).isEqualTo(expenditureOfWork);
		assertThat(reloaded.getStartDate()).isCloseTo(startDate, 1000);
		assertThat(reloaded.getDueDate()).isCloseTo(dueDate, 1000);
	}
	
	@Test
	public void shouldUpdateOriginTitle() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		String type = random();
		Long originId = Long.valueOf(33);
		String originSubPath = random();
		ToDoTask toDoTask1 = sut.create(identity, type, originId, originSubPath);
		dbInstance.commitAndCloseSession();
		
		String newTitle = random();
		sut.save(type, originId, originSubPath, newTitle);
		dbInstance.commitAndCloseSession();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask1));
		List<ToDoTask> toDoTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toDoTasks.get(0).getOriginTitle()).isEqualTo(newTitle);
	}
	
	@Test
	public void shouldUpdateOriginDeleted() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		String type = random();
		Long originId = Long.valueOf(33);
		String originSubPath = random();
		ToDoTask toDoTask1 = sut.create(identity, type, originId, originSubPath);
		dbInstance.commitAndCloseSession();
		
		sut.save(type, originId, originSubPath, true);
		dbInstance.commitAndCloseSession();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask1));
		List<ToDoTask> toDoTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toDoTasks.get(0).isOriginDeleted()).isTrue();
	}
	
	@Test
	public void shouldDelete() {
		ToDoTask toDoTask = createRandomToDoTask();
		
		sut.delete(toDoTask);
		dbInstance.commitAndCloseSession();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		List<ToDoTask> toToTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toToTasks).isEmpty();
	}
	
	@Test
	public void shouldLoadByOrigin() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		String type = random();
		Long originId = Long.valueOf(33);
		String originSubPath = random();
		ToDoTask toDoTask = sut.create(identity, type, originId, originSubPath);
		dbInstance.commitAndCloseSession();
		
		ToDoTask reloaded = sut.load(toDoTask.getType(), toDoTask.getOriginId(), toDoTask.getOriginSubPath());
		
		assertThat(reloaded).isEqualTo(toDoTask);
	}

	
	@Test
	public void shouldLoad_filer_toToTaskKeys() {
		ToDoTask toDoTask1 = createRandomToDoTask();
		ToDoTask toDoTask2 = createRandomToDoTask();
		createRandomToDoTask();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask1, toDoTask2));
		List<ToDoTask> toToTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toToTasks).containsExactlyInAnyOrder(toDoTask1, toDoTask2);
	}
	
	@Test
	public void shouldLoad_filer_status() {
		ToDoTask toDoTask1 = createRandomToDoTask();
		toDoTask1.setStatus(ToDoStatus.deleted);
		sut.save(toDoTask1);
		ToDoTask toDoTask2 = createRandomToDoTask();
		toDoTask2.setStatus(ToDoStatus.deleted);
		sut.save(toDoTask2);
		ToDoTask toDoTask3 = createRandomToDoTask();
		toDoTask3.setStatus(ToDoStatus.inProgress);
		sut.save(toDoTask3);
		ToDoTask toDoTask4 = createRandomToDoTask();
		toDoTask4.setStatus(ToDoStatus.open);
		sut.save(toDoTask4);
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask1, toDoTask2, toDoTask3, toDoTask4));
		searchParams.setStatus(List.of(ToDoStatus.deleted, ToDoStatus.inProgress));
		List<ToDoTask> toToTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toToTasks).containsExactlyInAnyOrder(toDoTask1, toDoTask2, toDoTask3);
	}
	
	@Test
	public void shouldLoad_filer_priorities() {
		ToDoTask toDoTask1 = createRandomToDoTask();
		toDoTask1.setPriority(ToDoPriority.high);
		sut.save(toDoTask1);
		ToDoTask toDoTask2 = createRandomToDoTask();
		toDoTask2.setPriority(ToDoPriority.high);
		sut.save(toDoTask2);
		ToDoTask toDoTask3 = createRandomToDoTask();
		toDoTask3.setPriority(ToDoPriority.urgent);
		sut.save(toDoTask3);
		ToDoTask toDoTask4 = createRandomToDoTask();
		toDoTask4.setPriority(ToDoPriority.low);
		sut.save(toDoTask4);
		ToDoTask toDoTask5 = createRandomToDoTask();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask1, toDoTask2, toDoTask3, toDoTask4, toDoTask5));
		searchParams.setPriorities(List.of(ToDoPriority.high, ToDoPriority.urgent));
		List<ToDoTask> toToTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toToTasks).containsExactlyInAnyOrder(toDoTask1, toDoTask2, toDoTask3);
	}
	
	@Test
	public void shouldLoad_filter_type() {
		String type1 = random();
		String type2 = random();
		String type3 = random();
		ToDoTask toDoTask11 = createRandomToDoTask(type1, null);
		ToDoTask toDoTask12 = createRandomToDoTask(type1, null);
		ToDoTask toDoTask21 = createRandomToDoTask(type2, null);
		createRandomToDoTask(type3, null);
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setTypes(List.of(type1, type2));
		List<ToDoTask> toToTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toToTasks).containsExactlyInAnyOrder(toDoTask11, toDoTask12, toDoTask21);
	}
	
	@Test
	public void shouldLoad_filter_originId() {
		String type = random();
		ToDoTask toDoTask1 = createRandomToDoTask(type, Long.valueOf(1));
		ToDoTask toDoTask2 = createRandomToDoTask(type, Long.valueOf(2));
		createRandomToDoTask(type, Long.valueOf(3));
		createRandomToDoTask(random(), null);
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setTypes(List.of(type));
		searchParams.setOriginIds(List.of(Long.valueOf(1), Long.valueOf(2)));
		List<ToDoTask> toToTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toToTasks).containsExactlyInAnyOrder(toDoTask1, toDoTask2);
	}
	
	@Test
	public void shouldLoad_filter_createdAfter() {
		ToDoTask toDoTask = createRandomToDoTask();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		searchParams.setCreatedAfter(new Date());
		sut.loadToDoTasks(searchParams);
		
		// Just syntax check because created date can't be modified.
	}
	
	@Test
	public void shouldLoad_filter_due_dat_range() {
		Date now = new Date();
		ToDoTask toDoTask1 = createRandomToDoTask();
		toDoTask1.setDueDate(DateUtils.addDays(now, 1));
		sut.save(toDoTask1);
		ToDoTask toDoTask2 = createRandomToDoTask();
		toDoTask2.setDueDate(DateUtils.addDays(now, 2));
		sut.save(toDoTask2);
		ToDoTask toDoTask3 = createRandomToDoTask();
		toDoTask3.setDueDate(DateUtils.addDays(now, 3));
		sut.save(toDoTask3);
		ToDoTask toDoTask4 = createRandomToDoTask();
		toDoTask4.setDueDate(DateUtils.addDays(now, 4));
		sut.save(toDoTask4);
		ToDoTask toDoTask5 = createRandomToDoTask();
		toDoTask5.setDueDate(DateUtils.addDays(now, 5));
		sut.save(toDoTask5);
		ToDoTask toDoTask6 = createRandomToDoTask();
		toDoTask6.setDueDate(DateUtils.addDays(now, 6));
		sut.save(toDoTask6);
		ToDoTask toDoTask7 = createRandomToDoTask();
		toDoTask7.setDueDate(DateUtils.addDays(now, 7));
		sut.save(toDoTask7);
		ToDoTask toDoTask8 = createRandomToDoTask();
		toDoTask8.setDueDate(DateUtils.addDays(now, 8));
		sut.save(toDoTask8);
		// No due date
		ToDoTask toDoTask9 = createRandomToDoTask();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask1, toDoTask2, toDoTask3, toDoTask4, toDoTask5, toDoTask6, toDoTask7, toDoTask8, toDoTask9));
		searchParams.setDueDateNull(true);
		searchParams.setDueDateRanges(List.of(
				new DateRange(addHours(addDays(now, -1), -1), addHours(addDays(now, 1), 1)),
				new DateRange(addHours(addDays(now, 6), -1), addHours(addDays(now, 8), 1))));
		List<ToDoTask> toToTasks = sut.loadToDoTasks(searchParams);
		
		assertThat(toToTasks).containsExactlyInAnyOrder(toDoTask1, toDoTask6, toDoTask7, toDoTask8, toDoTask9);
	}
	
	@Test
	public void shouldLoadTags() {
		ToDoTask toDoTask1 = createRandomToDoTask();
		String tag1 = random();
		String tag2 = random();
		toDoService.updateTags(toDoTask1, List.of(tag1, tag2));
		dbInstance.commitAndCloseSession();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask1));
		List<ToDoTaskTag> toDoTaskTags = sut.loadToDoTaskTags(searchParams);
		
		assertThat(toDoTaskTags)
				.extracting(ToDoTaskTag::getTag)
				.extracting(Tag::getDisplayName)
				.containsExactlyInAnyOrder(tag1, tag2);
	}
	
	@Test
	public void shouldSetDoneDate() {
		ToDoTask toDoTask = createRandomToDoTask();
		assertThat(toDoTask.getDoneDate()).isNull();
		
		toDoTask.setStatus(ToDoStatus.deleted);
		assertThat(toDoTask.getDoneDate()).isNull();
		
		toDoTask.setStatus(ToDoStatus.open);
		assertThat(toDoTask.getDoneDate()).isNull();
		
		toDoTask.setStatus(ToDoStatus.inProgress);
		assertThat(toDoTask.getDoneDate()).isNull();
		
		toDoTask.setStatus(ToDoStatus.done);
		assertThat(toDoTask.getDoneDate()).isNotNull();
		
		Date doneDate = toDoTask.getDoneDate();
		toDoTask.setStatus(ToDoStatus.done);
		assertThat(toDoTask.getDoneDate()).isEqualTo(doneDate);
		
		toDoTask.setStatus(ToDoStatus.deleted);
		assertThat(toDoTask.getDoneDate()).isEqualTo(doneDate);
		
		toDoTask.setStatus(ToDoStatus.done);
		assertThat(toDoTask.getDoneDate()).isEqualTo(doneDate);
		
		toDoTask.setStatus(ToDoStatus.inProgress);
		assertThat(toDoTask.getDoneDate()).isNull();
		
		toDoTask.setStatus(ToDoStatus.done);
		assertThat(toDoTask.getDoneDate()).isNotNull();
	}

	private ToDoTask createRandomToDoTask() {
		return createRandomToDoTask(random(), null);
	}
	
	private ToDoTask createRandomToDoTask(String type, Long originId) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		ToDoTask toDoTask = sut.create(identity, type, originId, null);
		dbInstance.commitAndCloseSession();
		return toDoTask;
	}

}
