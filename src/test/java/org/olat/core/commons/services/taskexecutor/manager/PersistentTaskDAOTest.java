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
package org.olat.core.commons.services.taskexecutor.manager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.WebappHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PersistentTaskDAOTest extends OlatTestCase  {
	
	private static final Logger log = Tracing.createLoggerFor(PersistentTaskDAOTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PersistentTaskDAO persistentTaskDao;
	
	@Test
	public void createTask() {
		String taskName = "Task 0";
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask());
		
		dbInstance.commit();
		
		Assert.assertNotNull(task);
		Assert.assertNotNull(task.getKey());
		Assert.assertNotNull(task.getCreationDate());
		Assert.assertNotNull(task.getLastModified());
		Assert.assertNotNull(task.getTask());
		Assert.assertEquals(TaskStatus.newTask, task.getStatus());
	}
	
	@Test
	public void createTask_withResource() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "test", null);
		Assert.assertNotNull(task);
		Assert.assertNotNull(task.getKey());
		Assert.assertNotNull(task.getCreationDate());
		Assert.assertNotNull(task.getLastModified());
		Assert.assertNotNull(task.getTask());
		Assert.assertEquals(TaskStatus.newTask, task.getStatus());
		Assert.assertEquals(re.getOlatResource(), task.getResource());
		Assert.assertEquals("test", task.getResSubPath());
		Assert.assertEquals(creator, task.getCreator());
	}
	
	@Test
	public void loadTask_byId() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-21-" + UUID.randomUUID().toString());
		PersistentTask task = persistentTaskDao.createTask("task-21", new DummyTask(), creator, re.getOlatResource(), "test", null);
		dbInstance.commitAndCloseSession();
		
		//load
		PersistentTask loadedTask = persistentTaskDao.loadTaskById(task.getKey());
		Assert.assertNotNull(loadedTask);
		Assert.assertEquals(task, loadedTask);
		
		//check return null if id doesn't exists
		PersistentTask unkownTask = persistentTaskDao.loadTaskById(1l);
		Assert.assertNull(unkownTask);
	}

	@Test
	public void pickTask() {
		String taskName = "Task 1";
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		PersistentTask todo = persistentTaskDao.pickTaskForRun(task);

		Assert.assertNotNull(todo);
		Assert.assertEquals(task.getKey(), todo.getKey());
		Assert.assertEquals(TaskStatus.inWork, todo.getStatus());
	}
	
	@Test
	public void updateTask() {
		//create
		String taskName = "Task to update";
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		//update
		PersistentTask todo = persistentTaskDao.pickTaskForRun(task);
		DummyTask taskToUpdate = new DummyTask();
		taskToUpdate.setMarkerValue("new marker");
		persistentTaskDao.updateTask(todo, taskToUpdate, null, null);
		dbInstance.commitAndCloseSession();
		
		//reload and check
		PersistentTask loadedTask = persistentTaskDao.loadTaskById(task.getKey());
		Runnable runnable = persistentTaskDao.deserializeTask(loadedTask);

		Assert.assertNotNull(runnable);
		Assert.assertTrue(runnable instanceof DummyTask);
		DummyTask dummyRunnable = (DummyTask)runnable;
		Assert.assertEquals("new marker", dummyRunnable.getMarkerValue());
	}
	
	@Test
	public void todo() {
		String taskname = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskname, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		List<Long> todos = persistentTaskDao.tasksToDo();

		Assert.assertNotNull(todos);
		Assert.assertTrue(todos.contains(task.getKey()));
	}
	
	@Test
	public void todo_workflow() {
		String taskName = UUID.randomUUID().toString();
		persistentTaskDao.createTask(taskName, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		int count = 0;
		List<Long> todos = persistentTaskDao.tasksToDo();
		for(Long todo:todos) {
			PersistentTask loadedTask = persistentTaskDao.loadTaskById(todo);
			PersistentTask taskToDo = persistentTaskDao.pickTaskForRun(loadedTask);
			persistentTaskDao.taskDone(taskToDo);
			count++;
		}
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(count > 0);
		
		List<Long> nothingTodos = persistentTaskDao.tasksToDo();
		Assert.assertNotNull(nothingTodos);
		Assert.assertTrue(nothingTodos.isEmpty());
	}
	
	@Test
	public void todo_oldTasks() {
		String taskName = UUID.randomUUID().toString();
		PersistentTask ctask = persistentTaskDao.createTask(taskName, new DummyTask());
		
		//simulate a task from a previous boot
		PersistentTask ptask = new PersistentTask();
		ptask.setCreationDate(new Date());
		ptask.setLastModified(new Date());
		ptask.setName(UUID.randomUUID().toString());
		ptask.setStatus(TaskStatus.inWork);
		ptask.setExecutorBootId(UUID.randomUUID().toString());
		ptask.setExecutorNode(Integer.toString(WebappHelper.getNodeId()));
		ptask.setTask(PersistentTaskDAO.toXML(new DummyTask()));
		dbInstance.getCurrentEntityManager().persist(ptask);

		//simulate a task from an other node
		PersistentTask alienTask = new PersistentTask();
		alienTask.setCreationDate(new Date());
		alienTask.setLastModified(new Date());
		alienTask.setName(UUID.randomUUID().toString());
		alienTask.setStatus(TaskStatus.inWork);
		alienTask.setExecutorBootId(UUID.randomUUID().toString());
		alienTask.setExecutorNode(Integer.toString(WebappHelper.getNodeId() + 1));
		alienTask.setTask(PersistentTaskDAO.toXML(new DummyTask()));
		dbInstance.getCurrentEntityManager().persist(alienTask);

		dbInstance.commitAndCloseSession();

		List<Long> todos = persistentTaskDao.tasksToDo();
		Assert.assertNotNull(todos);
		Assert.assertFalse(todos.isEmpty());
		Assert.assertTrue(todos.contains(ptask.getKey()));
		Assert.assertTrue(todos.contains(ctask.getKey()));
		Assert.assertFalse(todos.contains(alienTask.getKey()));
	}
	
	@Test
	public void taskDone() {
		String taskName = UUID.randomUUID().toString();
		PersistentTask taskToDo = persistentTaskDao.createTask(taskName, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		PersistentTask doneTask = persistentTaskDao.loadTaskById(taskToDo.getKey());
		persistentTaskDao.taskDone(doneTask);
		dbInstance.commitAndCloseSession();

		PersistentTask deletedTask = persistentTaskDao.loadTaskById(doneTask.getKey());
		Assert.assertNull(deletedTask);
	}
	
	@Test
	public void taskFailed() {
		String taskName = UUID.randomUUID().toString();
		PersistentTask taskToFail = persistentTaskDao.createTask(taskName, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		PersistentTask failedTask = persistentTaskDao.loadTaskById(taskToFail.getKey());
		persistentTaskDao.taskFailed(failedTask);
		dbInstance.commitAndCloseSession();
		
		TaskStatus status = persistentTaskDao.getStatus(failedTask);
		Assert.assertNotNull(status);
		Assert.assertEquals(TaskStatus.failed, status);
	}
	
	@Test
	public void taskCancelled() {
		String taskName = UUID.randomUUID().toString();
		PersistentTask taskToCancel = persistentTaskDao.createTask(taskName, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		PersistentTask cancelledTask = persistentTaskDao.loadTaskById(taskToCancel.getKey());
		persistentTaskDao.taskCancelled(cancelledTask);
		dbInstance.commitAndCloseSession();
		
		TaskStatus status = persistentTaskDao.getStatus(cancelledTask);
		Assert.assertNotNull(status);
		Assert.assertEquals(TaskStatus.cancelled, status);
	}
	
	@Test
	public void findTask_withResource() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "test", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();

		List<Task> tasks = persistentTaskDao.findTasks(re.getOlatResource());
		Assert.assertNotNull(tasks);
		Assert.assertEquals(1, tasks.size());
		Assert.assertTrue(tasks.get(0) instanceof PersistentTask);
		
		//check reloaded task
		PersistentTask ptask = (PersistentTask)tasks.get(0);
		Assert.assertEquals(task, ptask);
		Assert.assertEquals(task.getKey(), ptask.getKey());
		Assert.assertNotNull(ptask.getCreationDate());
		Assert.assertNotNull(ptask.getLastModified());
		Assert.assertNotNull(ptask.getTask());
		Assert.assertEquals(TaskStatus.newTask, ptask.getStatus());
		Assert.assertEquals(re.getOlatResource(), ptask.getResource());
		Assert.assertEquals("test", ptask.getResSubPath());
		Assert.assertEquals(creator, ptask.getCreator());
	}
	
	@Test
	public void findTask_withResourceAndSubPath() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "mySubPath", null);
		PersistentTask taskAlt = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "otherPath", null);
		Assert.assertNotNull(task);
		Assert.assertNotNull(taskAlt);
		dbInstance.commitAndCloseSession();

		List<Task> tasks = persistentTaskDao.findTasks(re.getOlatResource(), "mySubPath");
		Assert.assertNotNull(tasks);
		Assert.assertEquals(1, tasks.size());
		Assert.assertEquals(task, tasks.get(0));
	}
	
	@Test
	public void deleteTask_withResource() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "test", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		PersistentTask reloaded = dbInstance.getCurrentEntityManager().find(PersistentTask.class, task.getKey());
		Assert.assertEquals(task, reloaded);
		dbInstance.commitAndCloseSession();

		boolean deleted = persistentTaskDao.delete(task);
		Assert.assertTrue(deleted);
		dbInstance.commitAndCloseSession();
		
		boolean found = true;
		try {
			PersistentTask deletedTask = dbInstance.getCurrentEntityManager().find(PersistentTask.class, task.getKey());
			if(deletedTask == null) {
				found = false;
			}
		} catch (EntityNotFoundException e) {
			found = false;
		}
		Assert.assertFalse(found);
	}
	
	@Test
	public void deletedTask_byResource() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry reMark = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-8-" + UUID.randomUUID().toString());
		String taskName = UUID.randomUUID().toString();
		PersistentTask task1 = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "byResource", null);
		PersistentTask task2 = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "byResource", null);
		PersistentTask taskMark = persistentTaskDao.createTask(taskName, new DummyTask(), creator, reMark.getOlatResource(), "byResourcemarker", null);
		Assert.assertNotNull(task1);
		Assert.assertNotNull(task2);
		Assert.assertNotNull(taskMark);
		dbInstance.commitAndCloseSession();

		//delete tasks 1 and 2
		persistentTaskDao.delete(re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		//check if the tasks are deleted
		List<Task> deletedTasks = persistentTaskDao.findTasks(re.getOlatResource());
		Assert.assertNotNull(deletedTasks);
		Assert.assertTrue(deletedTasks.isEmpty());
		//check if the marker is there
		List<Task> notDeletedTasks = persistentTaskDao.findTasks(reMark.getOlatResource());
		Assert.assertNotNull(notDeletedTasks);
		Assert.assertFalse(notDeletedTasks.isEmpty());
		Assert.assertTrue(notDeletedTasks.contains(taskMark));
	}
	
	@Test
	public void deletedTask_byResourceAndSubpath() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-9-" + UUID.randomUUID().toString());
		String taskName = UUID.randomUUID().toString();
		PersistentTask task1 = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "byResource", null);
		PersistentTask task2 = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "byResource", null);
		PersistentTask taskMark = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "byResourceMarker", null);
		Assert.assertNotNull(task1);
		Assert.assertNotNull(task2);
		Assert.assertNotNull(taskMark);
		dbInstance.commitAndCloseSession();

		//delete tasks 1 and 2
		persistentTaskDao.delete(re.getOlatResource(), "byResource");
		dbInstance.commitAndCloseSession();
		
		//check if the tasks are deleted
		List<Task> tasks = persistentTaskDao.findTasks(re.getOlatResource());
		Assert.assertNotNull(tasks);
		Assert.assertFalse(tasks.isEmpty());
		Assert.assertTrue(tasks.contains(taskMark));
	}
	
	@Test
	public void updateTask_withResource() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-2-" + UUID.randomUUID().toString());
		Identity modifier = JunitTestHelper.createAndPersistIdentityAsUser("extask-3-" + UUID.randomUUID().toString());
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "test", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		//update
		PersistentTask updatedTask = persistentTaskDao.updateTask(task, new DummyTask(), modifier, new Date());
		Assert.assertNotNull(updatedTask);
		Assert.assertEquals(task, updatedTask);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void updateTask_modifiers() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-4-" + UUID.randomUUID().toString());
		Identity modifier1 = JunitTestHelper.createAndPersistIdentityAsUser("extask-5-" + UUID.randomUUID().toString());
		Identity modifier2 = JunitTestHelper.createAndPersistIdentityAsUser("extask-6-" + UUID.randomUUID().toString());
		Identity modifier3 = JunitTestHelper.createAndPersistIdentityAsUser("extask-7-" + UUID.randomUUID().toString());
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "test", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		//updates
		persistentTaskDao.updateTask(task, new DummyTask(), modifier1, new Date());
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier2, new Date());
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier3, new Date());
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier1, new Date());
		dbInstance.commitAndCloseSession();
		
		//load mofifiers
		List<Identity> modifiers = persistentTaskDao.getModifiers(task);
		Assert.assertNotNull(modifiers);
		Assert.assertEquals(3, modifiers.size());
	}
	
	@Test
	public void deletedTask_modifiers() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-8-" + UUID.randomUUID().toString());
		Identity modifier1 = JunitTestHelper.createAndPersistIdentityAsUser("extask-9-" + UUID.randomUUID().toString());
		Identity modifier2 = JunitTestHelper.createAndPersistIdentityAsUser("extask-10-" + UUID.randomUUID().toString());
		Identity modifier3 = JunitTestHelper.createAndPersistIdentityAsUser("extask-11-" + UUID.randomUUID().toString());
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "test", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		//updates
		persistentTaskDao.updateTask(task, new DummyTask(), modifier1, new Date());
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier2, new Date());
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier3, new Date());
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier1, new Date());
		dbInstance.commitAndCloseSession();
		
		//delete task and modifiers mofifiers
		persistentTaskDao.delete(task);
		dbInstance.commitAndCloseSession();
		
		//check modifiers
		List<Identity> modifiers = persistentTaskDao.getModifiers(task);
		Assert.assertNotNull(modifiers);
		Assert.assertTrue(modifiers.isEmpty());
	}
	
	@Test
	public void todo_workflow_withModifiers() {
		String tname = UUID.randomUUID().toString();
		persistentTaskDao.createTask(tname, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-12-" + UUID.randomUUID().toString());
		Identity modifier1 = JunitTestHelper.createAndPersistIdentityAsUser("extask-13-" + UUID.randomUUID().toString());
		Identity modifier2 = JunitTestHelper.createAndPersistIdentityAsUser("extask-14-" + UUID.randomUUID().toString());
		Identity modifier3 = JunitTestHelper.createAndPersistIdentityAsUser("extask-15-" + UUID.randomUUID().toString());
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "test", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		//updates
		persistentTaskDao.updateTask(task, new DummyTask(), modifier1, null);
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier2, null);
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier3, null);
		dbInstance.commitAndCloseSession();
		persistentTaskDao.updateTask(task, new DummyTask(), modifier1, null);
		dbInstance.commitAndCloseSession();
		
		int count = 0;
		List<Long> todos = persistentTaskDao.tasksToDo();
		for(Long todo:todos) {
			PersistentTask loadedTask = persistentTaskDao.loadTaskById(todo);
			PersistentTask taskToDo = persistentTaskDao.pickTaskForRun(loadedTask);
			persistentTaskDao.taskDone(taskToDo);
			count++;
		}
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(count > 0);
		
		List<Long> nothingTodos = persistentTaskDao.tasksToDo();
		Assert.assertNotNull(nothingTodos);
		Assert.assertFalse(nothingTodos.contains(task.getKey()));
	}
	
	@Test
	public void edit_workflow() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser("extask-20-" + UUID.randomUUID().toString());
		String taskName = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(taskName, new DummyTask(), creator, re.getOlatResource(), "edition", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		//pick
		PersistentTask editableTask = persistentTaskDao.pickTaskForEdition(task.getKey());
		Assert.assertNotNull(editableTask);
		Assert.assertEquals(TaskStatus.edition, editableTask.getStatus());
		dbInstance.commitAndCloseSession();
		
		//second pick
		PersistentTask notEditableTask = persistentTaskDao.pickTaskForEdition(task.getKey());
		Assert.assertNull(notEditableTask);
		dbInstance.commitAndCloseSession();
		
		//return
		persistentTaskDao.returnTaskAfterEdition(task.getKey(), null);
		dbInstance.commitAndCloseSession();
		
		//third pick
		PersistentTask againEditableTask = persistentTaskDao.pickTaskForEdition(task.getKey());
		Assert.assertNotNull(againEditableTask);
		Assert.assertEquals(TaskStatus.edition, againEditableTask.getStatus());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void updateProgress() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("extask-21");
	
		PersistentTask task = persistentTaskDao.createTask("Update progress" + CodeHelper.getRAMUniqueID(), new DummyTask(), creator,
				re.getOlatResource(), "progress", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		//update
		persistentTaskDao.updateProgressTask(task, 0.1, "check");
		dbInstance.commitAndCloseSession();
		
		PersistentTask reloadedTask = persistentTaskDao.loadTaskById(task.getKey());
		Assert.assertNotNull(reloadedTask.getProgress());
		Assert.assertEquals(0.1, reloadedTask.getProgress().doubleValue(), 0.0001d);
		Assert.assertEquals("check", reloadedTask.getCheckpoint());
	}
	
	@Test
	public void getProgress() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("extask-22");
	
		PersistentTask task = persistentTaskDao.createTask("Update progress" + CodeHelper.getRAMUniqueID(), new DummyTask(), creator,
				resource, "progress", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		//update
		persistentTaskDao.updateProgressTask(task, 0.1, "check");
		dbInstance.commitAndCloseSession();
		
		Double progress = persistentTaskDao.getProgress(task);
		Assert.assertNotNull(progress);
		Assert.assertEquals(0.1, progress.doubleValue(), 0.0001d);
	}
	
	@Test
	public void getStatus() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("extask-23");
	
		PersistentTask task = persistentTaskDao.createTask("Update progress" + CodeHelper.getRAMUniqueID(), new DummyTask(), creator,
				resource, "progress", null);
		Assert.assertNotNull(task);
		dbInstance.commitAndCloseSession();
		
		TaskStatus status = persistentTaskDao.getStatus(task);
		Assert.assertNotNull(status);
		Assert.assertEquals(TaskStatus.newTask, status);
	}
	
	public static class DummyTask implements Runnable, Serializable {
		private static final long serialVersionUID = 5193785402425324970L;
		
		private String markerValue;
		
		public DummyTask() {
			this.markerValue = UUID.randomUUID().toString();
		}

		public String getMarkerValue() {
			return markerValue;
		}

		public void setMarkerValue(String markerValue) {
			this.markerValue = markerValue;
		}



		@Override
		public void run() {
			log.info("Run: " + markerValue);
		}
	}
}
