/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.core.util.DateUtils.addDays;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_AFTER_BEGIN;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_BEFORE_END;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_SAME_DAY_BEGIN;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_SAME_DAY_END;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.todo.ToDoDateUnit;
import org.olat.modules.todo.ToDoRelativeDates;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.manager.PersonalToDoProvider;
import org.olat.modules.todo.model.ToDoTaskImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 8 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumElementToDoProviderTest extends OlatTestCase {

	private static final long DELTA_MS = 2000L;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementToDoProvider curriculumElementToDoProvider;

	@Test
	public void shouldMaterializeDates_onCurriculumElementUpdated() {
		Date begin = addDays(new Date(), 10);
		Date end = addDays(begin, 30);
		CurriculumElement element = createCurriculumElement(begin, end);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, element);

		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartValue(2);
		config.setStartUnit(ToDoDateUnit.DAYS);
		config.setStartRef(DATE_REF_AFTER_BEGIN);
		config.setDueValue(5);
		config.setDueUnit(ToDoDateUnit.DAYS);
		config.setDueRef(DATE_REF_BEFORE_END);
		task.setRelativeDates(config);
		toDoService.update(doer, task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element);
		dbInstance.commitAndCloseSession();

		ToDoTask reloaded = reload(task);
		assertThat(reloaded.getStartDate()).isCloseTo(addDays(begin, 2), DELTA_MS);
		assertThat(reloaded.getDueDate()).isCloseTo(addDays(end, -5), DELTA_MS);
	}

	@Test
	public void shouldMaterializeOnlyStart_whenOnlyStartConfigured() {
		Date begin = addDays(new Date(), 10);
		Date end = addDays(begin, 30);
		CurriculumElement element = createCurriculumElement(begin, end);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, element);
		task.setDueDate(end);

		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartValue(1);
		config.setStartUnit(ToDoDateUnit.DAYS);
		config.setStartRef(DATE_REF_AFTER_BEGIN);
		task.setRelativeDates(config);
		toDoService.update(doer, task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element);
		dbInstance.commitAndCloseSession();

		ToDoTask reloaded = reload(task);
		assertThat(reloaded.getStartDate()).isCloseTo(addDays(begin, 1), DELTA_MS);
		assertThat(reloaded.getDueDate()).isCloseTo(end, DELTA_MS);
	}

	@Test
	public void shouldMaterializeOnlyDue_whenOnlyDueConfigured() {
		Date begin = addDays(new Date(), 10);
		Date end = addDays(begin, 30);
		CurriculumElement element = createCurriculumElement(begin, end);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, element);
		task.setStartDate(begin);

		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setDueValue(3);
		config.setDueUnit(ToDoDateUnit.DAYS);
		config.setDueRef(DATE_REF_BEFORE_END);
		task.setRelativeDates(config);
		toDoService.update(doer, task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element);
		dbInstance.commitAndCloseSession();

		ToDoTask reloaded = reload(task);
		assertThat(reloaded.getStartDate()).isCloseTo(begin, DELTA_MS);
		assertThat(reloaded.getDueDate()).isCloseTo(addDays(end, -3), DELTA_MS);
	}

	@Test
	public void shouldMaterializeSameDay_withBeginRef() {
		Date begin = addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(begin, addDays(begin, 30));
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, element);

		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartUnit(ToDoDateUnit.SAME_DAY);
		config.setStartRef(DATE_REF_SAME_DAY_BEGIN);
		task.setRelativeDates(config);
		toDoService.update(doer, task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element);
		dbInstance.commitAndCloseSession();

		assertThat(reload(task).getStartDate()).isCloseTo(begin, DELTA_MS);
	}

	@Test
	public void shouldMaterializeSameDay_withEndRef() {
		Date begin = addDays(new Date(), 10);
		Date end = addDays(begin, 30);
		CurriculumElement element = createCurriculumElement(begin, end);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, element);

		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setDueUnit(ToDoDateUnit.SAME_DAY);
		config.setDueRef(DATE_REF_SAME_DAY_END);
		task.setRelativeDates(config);
		toDoService.update(doer, task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element);
		dbInstance.commitAndCloseSession();

		assertThat(reload(task).getDueDate()).isCloseTo(end, DELTA_MS);
	}

	@Test
	public void shouldNotMaterialize_whenConfigIsNull() {
		Date begin = addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(begin, addDays(begin, 30));
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, element);
		task.setStartDate(begin);
		toDoService.update(doer, task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element);
		dbInstance.commitAndCloseSession();

		assertThat(reload(task).getStartDate()).isCloseTo(begin, DELTA_MS);
	}

	@Test
	public void shouldOnlyResyncTasksOfMatchingElement() {
		Date begin1 = addDays(new Date(), 10);
		CurriculumElement element1 = createCurriculumElement(begin1, addDays(begin1, 30));
		Date begin2 = addDays(new Date(), 20);
		CurriculumElement element2 = createCurriculumElement(begin2, addDays(begin2, 30));
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());

		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartValue(1);
		config.setStartUnit(ToDoDateUnit.DAYS);
		config.setStartRef(DATE_REF_AFTER_BEGIN);

		ToDoTask task1 = createCurriculumElementTask(doer, element1);
		task1.setRelativeDates(config);
		toDoService.update(doer, task1, ToDoStatus.open);

		ToDoTask task2 = createCurriculumElementTask(doer, element2);
		task2.setRelativeDates(config);
		toDoService.update(doer, task2, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element1);
		dbInstance.commitAndCloseSession();

		assertThat(reload(task1).getStartDate()).isCloseTo(addDays(begin1, 1), DELTA_MS);
		assertThat(reload(task2).getStartDate()).isNull();
	}

	@Test
	public void shouldNotResyncTasksWithDifferentProviderType() {
		Date begin = addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(begin, addDays(begin, 30));
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());

		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartValue(1);
		config.setStartUnit(ToDoDateUnit.DAYS);
		config.setStartRef(DATE_REF_AFTER_BEGIN);

		ToDoTask curriculumTask = createCurriculumElementTask(doer, element);
		curriculumTask.setRelativeDates(config);
		toDoService.update(doer, curriculumTask, ToDoStatus.open);

		ToDoTask personalTask = toDoService.createToDoTask(doer, PersonalToDoProvider.TYPE,
				element.getCurriculum().getKey(), String.valueOf(element.getKey()), null, null, null);
		personalTask.setRelativeDates(config);
		Date manualStart = addDays(new Date(), 5);
		personalTask.setStartDate(manualStart);
		toDoService.update(doer, personalTask, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element);
		dbInstance.commitAndCloseSession();

		assertThat(reload(curriculumTask).getStartDate()).isCloseTo(addDays(begin, 1), DELTA_MS);
		assertThat(reload(personalTask).getStartDate()).isCloseTo(manualStart, DELTA_MS);
	}

	@Test
	public void shouldUpdateOriginTitle_onCurriculumElementUpdated() {
		Date begin = addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(begin, addDays(begin, 30));
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, element);
		dbInstance.commitAndCloseSession();

		String newDisplayName = random();
		element.setDisplayName(newDisplayName);
		curriculumElementToDoProvider.onCurriculumElementUpdated(doer, element);
		dbInstance.commitAndCloseSession();

		ToDoTaskImpl reloaded = (ToDoTaskImpl) reload(task);
		assertThat(reloaded.getOriginSubTitle()).isEqualTo(curriculumElementToDoProvider.getContextSubTitle(element));
	}

	@Test
	public void shouldMarkOriginDeleted_onCurriculumElementDeletedSoftly() {
		Date begin = addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(begin, addDays(begin, 30));
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, element);
		dbInstance.commitAndCloseSession();

		curriculumElementToDoProvider.onCurriculumElementDeletedSoftly(element, doer);
		dbInstance.commitAndCloseSession();

		assertThat(reload(task).isOriginDeleted()).isTrue();
	}

	@Test
	public void shouldNotFail_onCurriculumElementDeletedSoftly_whenCurriculumIsNull() {
		CurriculumElement mockElement = Mockito.mock(CurriculumElement.class);
		Mockito.when(mockElement.getCurriculum()).thenReturn(null);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());

		curriculumElementToDoProvider.onCurriculumElementDeletedSoftly(mockElement, doer);
	}

	@Test
	public void shouldCountActiveToDoTasks() {
		Date begin = addDays(new Date(), 10);
		CurriculumElement element1 = createCurriculumElement(begin, addDays(begin, 30));
		CurriculumElement element2 = createCurriculumElement(begin, addDays(begin, 30));
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());

		ToDoTask openTask1 = createCurriculumElementTask(doer, element1);
		toDoService.update(doer, openTask1, ToDoStatus.open);

		ToDoTask inProgressTask = createCurriculumElementTask(doer, element1);
		inProgressTask.setStatus(ToDoStatus.inProgress);
		toDoService.update(doer, inProgressTask, ToDoStatus.open);

		ToDoTask doneTask = createCurriculumElementTask(doer, element1);
		doneTask.setStatus(ToDoStatus.done);
		toDoService.update(doer, doneTask, ToDoStatus.open);

		ToDoTask deletedTask = createCurriculumElementTask(doer, element1);
		deletedTask.setStatus(ToDoStatus.deleted);
		toDoService.update(doer, deletedTask, ToDoStatus.open);

		ToDoTask element2Task = createCurriculumElementTask(doer, element2);
		toDoService.update(doer, element2Task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		long count = curriculumElementToDoProvider.countActiveToDoTasks(element1, List.of(element1));
		assertThat(count).isEqualTo(3);
	}

	@Test
	public void shouldCountZero_whenCurriculumIsNull() {
		CurriculumElement mockElement = Mockito.mock(CurriculumElement.class);
		Mockito.when(mockElement.getCurriculum()).thenReturn(null);

		long count = curriculumElementToDoProvider.countActiveToDoTasks(mockElement, List.of(mockElement));
		assertThat(count).isEqualTo(0);
	}

	@Test
	public void shouldMaterializeDates_onCopyToDoTasks() {
		CurriculumElement source = createCurriculumElement(null, null);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, source);

		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartValue(2);
		config.setStartUnit(ToDoDateUnit.DAYS);
		config.setStartRef(DATE_REF_AFTER_BEGIN);
		config.setDueValue(5);
		config.setDueUnit(ToDoDateUnit.DAYS);
		config.setDueRef(DATE_REF_BEFORE_END);
		task.setRelativeDates(config);
		toDoService.update(doer, task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		Date targetBegin = addDays(new Date(), 10);
		Date targetEnd = addDays(targetBegin, 30);
		CurriculumElement target = createCurriculumElement(targetBegin, targetEnd);

		curriculumElementToDoProvider.copyToDoTasks(source, target, false, null, doer);
		dbInstance.commitAndCloseSession();

		ToDoTaskSearchParams searchParams = curriculumElementToDoProvider.createActiveSearchParams(
				List.of(target.getKey().toString()));
		List<ToDoTask> copies = toDoService.getToDoTasks(searchParams);
		assertThat(copies).hasSize(1);
		assertThat(copies.get(0).getStartDate()).isCloseTo(addDays(targetBegin, 2), DELTA_MS);
		assertThat(copies.get(0).getDueDate()).isCloseTo(addDays(targetEnd, -5), DELTA_MS);
		assertThat(copies.get(0).getRelativeDates()).isNotNull();
	}

	@Test
	public void shouldCopyAbsoluteDates_whenNoRelativeConfig() {
		Date sourceBegin = addDays(new Date(), 5);
		Date sourceEnd = addDays(sourceBegin, 20);
		CurriculumElement source = createCurriculumElement(sourceBegin, sourceEnd);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask task = createCurriculumElementTask(doer, source);
		task.setStartDate(sourceBegin);
		task.setDueDate(sourceEnd);
		toDoService.update(doer, task, ToDoStatus.open);
		dbInstance.commitAndCloseSession();

		Date targetBegin = addDays(new Date(), 15);
		Date targetEnd = addDays(targetBegin, 30);
		CurriculumElement target = createCurriculumElement(targetBegin, targetEnd);

		curriculumElementToDoProvider.copyToDoTasks(source, target, false, null, doer);
		dbInstance.commitAndCloseSession();

		ToDoTaskSearchParams searchParams = curriculumElementToDoProvider.createActiveSearchParams(
				List.of(target.getKey().toString()));
		List<ToDoTask> copies = toDoService.getToDoTasks(searchParams);
		assertThat(copies).hasSize(1);
		assertThat(copies.get(0).getStartDate()).isCloseTo(sourceBegin, DELTA_MS);
		assertThat(copies.get(0).getDueDate()).isCloseTo(sourceEnd, DELTA_MS);
		assertThat(copies.get(0).getRelativeDates()).isNull();
	}

	@Test
	public void shouldCountOnlyToDoTasksOfAccessibleElements_forCurriculumOwner() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculumowner);
		CurriculumElement element1 = createCurriculumElement(curriculum);
		CurriculumElement element2 = createCurriculumElement(curriculum);
		CurriculumElement foreignElement = createCurriculumElement(null, null);
		createCurriculumElementTask(manager, element1);
		createCurriculumElementTask(manager, element2);
		createCurriculumElementTask(manager, foreignElement);

		Long count = toDoService.getToDoTaskCount(curriculumElementToDoProvider.createManagerSearchParams(manager));

		assertThat(count).isEqualTo(2);
	}

	@Test
	public void shouldListOnlyToDoTasksOfAccessibleElements_forCurriculumOwner() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculumowner);
		CurriculumElement element = createCurriculumElement(curriculum);
		CurriculumElement foreignElement = createCurriculumElement(null, null);
		ToDoTask task = createCurriculumElementTask(manager, element);
		ToDoTask foreignTask = createCurriculumElementTask(manager, foreignElement);

		List<ToDoTask> tasks = toDoService.getToDoTasks(curriculumElementToDoProvider.createManagerSearchParams(manager));

		assertThat(tasks)
				.extracting(ToDoTask::getKey)
				.containsExactly(task.getKey())
				.doesNotContain(foreignTask.getKey());
	}

	@Test
	public void shouldCountOnlyToDoTasksOfOwnedElement_forElementOwner() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		CurriculumElement ownedElement = createCurriculumElement(curriculum);
		CurriculumElement otherElement = createCurriculumElement(curriculum);
		curriculumService.addMember(ownedElement, owner, CurriculumRoles.curriculumelementowner, owner);
		dbInstance.commitAndCloseSession();
		createCurriculumElementTask(owner, ownedElement);
		createCurriculumElementTask(owner, otherElement);

		Long count = toDoService.getToDoTaskCount(curriculumElementToDoProvider.createManagerSearchParams(owner));

		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldCountNoToDoTasks_forIdentityWithoutAccess() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity noManager = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculumowner);
		CurriculumElement element = createCurriculumElement(curriculum);
		createCurriculumElementTask(manager, element);

		Long count = toDoService.getToDoTaskCount(curriculumElementToDoProvider.createManagerSearchParams(noManager));

		assertThat(count).isZero();
	}

	@Test
	public void shouldCountOnlyToDoTasksOfOrganisationCurricula_forCurriculumManager() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null, manager);
		organisationService.addMember(organisation, manager, OrganisationRoles.curriculummanager, manager);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		CurriculumElement element = createCurriculumElement(curriculum);
		CurriculumElement foreignElement = createCurriculumElement(null, null);
		createCurriculumElementTask(manager, element);
		createCurriculumElementTask(manager, foreignElement);

		Long count = toDoService.getToDoTaskCount(curriculumElementToDoProvider.createManagerSearchParams(manager));

		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldCountOnlyToDoTasksOfGivenCurriculum_whenOriginIdIsSet() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Curriculum curriculum1 = curriculumService.createCurriculum(random(), random(), null, false, null);
		Curriculum curriculum2 = curriculumService.createCurriculum(random(), random(), null, false, null);
		curriculumService.addMember(curriculum1, manager, CurriculumRoles.curriculumowner);
		curriculumService.addMember(curriculum2, manager, CurriculumRoles.curriculumowner);
		CurriculumElement element1 = createCurriculumElement(curriculum1);
		CurriculumElement element2 = createCurriculumElement(curriculum2);
		createCurriculumElementTask(manager, element1);
		createCurriculumElementTask(manager, element2);

		ToDoTaskSearchParams params = curriculumElementToDoProvider.createManagerSearchParams(manager);
		params.setOriginIds(List.of(curriculum1.getKey()));
		Long count = toDoService.getToDoTaskCount(params);

		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldCountToDoTasksOfElementAndDescendants_forCurriculumOwner() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculumowner);
		CurriculumElement root = createCurriculumElement(curriculum);
		CurriculumElement child = createChildElement(curriculum, root);
		CurriculumElement grandChild = createChildElement(curriculum, child);
		CurriculumElement sibling = createCurriculumElement(curriculum);
		createCurriculumElementTask(manager, root);
		createCurriculumElementTask(manager, child);
		createCurriculumElementTask(manager, grandChild);
		createCurriculumElementTask(manager, sibling);

		Long allLevels = toDoService.getToDoTaskCount(curriculumElementToDoProvider.createManagerSearchParams(manager, reloadElement(root), true));
		Long thisLevel = toDoService.getToDoTaskCount(curriculumElementToDoProvider.createManagerSearchParams(manager, reloadElement(root), false));

		assertThat(allLevels).isEqualTo(3);
		assertThat(thisLevel).isEqualTo(1);
	}

	@Test
	public void shouldCountOnlyAccessibleToDoTasksOfDescendants_forElementOwner() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		CurriculumElement root = createCurriculumElement(curriculum);
		CurriculumElement child = createChildElement(curriculum, root);
		CurriculumElement otherChild = createChildElement(curriculum, root);
		curriculumService.addMember(child, owner, CurriculumRoles.curriculumelementowner, owner);
		dbInstance.commitAndCloseSession();
		createCurriculumElementTask(owner, root);
		createCurriculumElementTask(owner, child);
		createCurriculumElementTask(owner, otherChild);

		Long allLevels = toDoService.getToDoTaskCount(curriculumElementToDoProvider.createManagerSearchParams(owner, reloadElement(root), true));
		Long thisLevel = toDoService.getToDoTaskCount(curriculumElementToDoProvider.createManagerSearchParams(owner, reloadElement(root), false));

		assertThat(allLevels).isEqualTo(1);
		assertThat(thisLevel).isZero();
	}

	@Test
	public void shouldCountActiveToDoTasksOfElementAndDescendants() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		CurriculumElement root = createCurriculumElement(curriculum);
		CurriculumElement child = createChildElement(curriculum, root);
		CurriculumElement sibling = createCurriculumElement(curriculum);
		createCurriculumElementTask(doer, root);
		createCurriculumElementTask(doer, child);
		createCurriculumElementTask(doer, sibling);
		ToDoTask deleted = createCurriculumElementTask(doer, child);
		toDoService.deleteToDoTaskPermanently(deleted);
		dbInstance.commitAndCloseSession();

		long count = curriculumElementToDoProvider.countActiveToDoTasksOfElementAndDescendants(reloadElement(root));

		assertThat(count).isEqualTo(2);
	}

	private CurriculumElement reloadElement(CurriculumElement element) {
		return curriculumService.getCurriculumElement(element);
	}

	private CurriculumElement createChildElement(Curriculum curriculum, CurriculumElement parent) {
		CurriculumElement element = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, parent, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commitAndCloseSession();
		return element;
	}

	private CurriculumElement createCurriculumElement(Curriculum curriculum) {
		CurriculumElement element = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commitAndCloseSession();
		return element;
	}

	private CurriculumElement createCurriculumElement(Date beginDate, Date endDate) {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		CurriculumElement element = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, beginDate, endDate, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commitAndCloseSession();
		return element;
	}

	private ToDoTask createCurriculumElementTask(Identity doer, CurriculumElement element) {
		ToDoTask task = toDoService.createToDoTask(doer, CurriculumElementToDoProvider.TYPE,
				element.getCurriculum().getKey(), String.valueOf(element.getKey()), null, null, null);
		dbInstance.commitAndCloseSession();
		return task;
	}

	private ToDoTask reload(ToDoTask task) {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(task));
		return toDoService.getToDoTasks(searchParams).get(0);
	}

}
