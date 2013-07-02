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
package org.olat.core.commons.taskExecutor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.taskExecutor.manager.PersistentTaskDAO;
import org.olat.core.commons.taskExecutor.model.PersistentTask;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PersistentTaskDAOTest extends OlatTestCase  {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PersistentTaskDAO persistentTaskDao;
	
	
	@Test
	public void testCreateTask() {
		String name = "Task 0";
		PersistentTask task = persistentTaskDao.createTask(name, new DummyTask());
		
		dbInstance.commit();
		
		Assert.assertNotNull(task);
		Assert.assertNotNull(task.getKey());
		Assert.assertNotNull(task.getCreationDate());
		Assert.assertNotNull(task.getLastModified());
		Assert.assertNotNull(task.getTask());
		Assert.assertEquals(TaskStatus.newTask.name(), task.getStatus());
	}

	@Test
	public void testPickTask() {
		String name = "Task 1";
		PersistentTask task = persistentTaskDao.createTask(name, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		PersistentTask todo = persistentTaskDao.pickTask(task.getKey());

		Assert.assertNotNull(todo);
		Assert.assertEquals(task.getKey(), todo.getKey());
		Assert.assertEquals(TaskStatus.inWork.name(), todo.getStatus());
	}
	
	@Test
	public void testTodo() {
		String name = UUID.randomUUID().toString();
		PersistentTask task = persistentTaskDao.createTask(name, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		List<Long> todos = persistentTaskDao.tasksToDo();

		Assert.assertNotNull(todos);
		Assert.assertTrue(todos.contains(task.getKey()));
	}
	
	@Test
	public void testTodo_workflow() {
		String name = UUID.randomUUID().toString();
		persistentTaskDao.createTask(name, new DummyTask());
		dbInstance.commitAndCloseSession();
		
		int count = 0;
		List<Long> todos = persistentTaskDao.tasksToDo();
		for(Long todo:todos) {
			PersistentTask taskToDo = persistentTaskDao.pickTask(todo);
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
	public void testTodo_oldTasks() {
		String name = UUID.randomUUID().toString();
		PersistentTask ctask = persistentTaskDao.createTask(name, new DummyTask());
		
		//simulate a task from a previous boot
		PersistentTask ptask = new PersistentTask();
		ptask.setCreationDate(new Date());
		ptask.setLastModified(new Date());
		ptask.setName(UUID.randomUUID().toString());
		ptask.setStatus(TaskStatus.inWork.name());
		ptask.setExecutorBootId(UUID.randomUUID().toString());
		ptask.setExecutorNode(Integer.toString(WebappHelper.getNodeId()));
		ptask.setTask(XStreamHelper.createXStreamInstance().toXML(new DummyTask()));
		dbInstance.getCurrentEntityManager().persist(ptask);

		//simulate a task from an other node
		PersistentTask alienTask = new PersistentTask();
		alienTask.setCreationDate(new Date());
		alienTask.setLastModified(new Date());
		alienTask.setName(UUID.randomUUID().toString());
		alienTask.setStatus(TaskStatus.inWork.name());
		alienTask.setExecutorBootId(UUID.randomUUID().toString());
		alienTask.setExecutorNode(Integer.toString(WebappHelper.getNodeId() + 1));
		alienTask.setTask(XStreamHelper.createXStreamInstance().toXML(new DummyTask()));
		dbInstance.getCurrentEntityManager().persist(alienTask);

		dbInstance.commitAndCloseSession();

		List<Long> todos = persistentTaskDao.tasksToDo();
		Assert.assertNotNull(todos);
		Assert.assertFalse(todos.isEmpty());
		Assert.assertTrue(todos.contains(ptask.getKey()));
		Assert.assertTrue(todos.contains(ctask.getKey()));
		Assert.assertFalse(todos.contains(alienTask.getKey()));
	}
	
	
	public static class DummyTask implements Runnable, Serializable {
		private static final long serialVersionUID = 5193785402425324970L;

		@Override
		public void run() {
			//
		}
	}
}
