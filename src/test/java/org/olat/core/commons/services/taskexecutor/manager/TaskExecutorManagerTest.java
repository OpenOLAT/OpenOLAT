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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.logging.Tracing;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskExecutorManagerTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(TaskExecutorManagerTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PersistentTaskDAO persistentTaskDao;
	@Autowired
	private TaskExecutorManagerImpl taskExecutorManager;
	
	@Test
	public void testRunTask() {
		final CountDownLatch finishCount = new CountDownLatch(1);
		taskExecutorManager.execute(new DummyTask(finishCount));
		
		try {
			boolean zero = finishCount.await(10, TimeUnit.SECONDS);
			Assert.assertTrue(zero);
		} catch (InterruptedException e) {
			Assert.fail("Takes too long (more than 10sec)");
		}
	}
	

	private final static int numOfMpTasks = 100;
	private final static Map<String,AtomicInteger> taskCounter = new ConcurrentHashMap<>();
	private final static CountDownLatch mpFinishCount = new CountDownLatch(numOfMpTasks);
	
	@Test
	public void testRunParallelTasks() {
		for(int i=numOfMpTasks; i-->0;) {
			String taskName = "Task-" + i;
			persistentTaskDao.createTask(taskName, new DummySerializableTask(taskName));
			taskCounter.put(taskName, new AtomicInteger(0));
			dbInstance.commitAndCloseSession();
		}

		final int numOfExecutors = 5;
		final Thread[] executors = new Thread[numOfExecutors];
		for(int i=numOfExecutors; i-->0; ) {
			executors[i] = new Thread(new ProcessTask(taskExecutorManager, dbInstance));
		}
		
		try {
			for(int i=numOfExecutors; i-->0; ) {
				executors[i].start();
			}

			boolean zero = mpFinishCount.await(120, TimeUnit.SECONDS);
			Assert.assertTrue(zero);
			
			// make sure the task is done only once
			for(AtomicInteger count:taskCounter.values()) {
				Assert.assertEquals(1, count.get());
			}
			log.info("All task done: " + taskCounter.size());
		} catch (InterruptedException e) {
			Assert.fail("Takes too long (more than 10sec)");
		}
	}
	
	public static class ProcessTask implements Runnable {
		
		private final DB db;
		private final TaskExecutorManagerImpl executor;
		
		public ProcessTask(TaskExecutorManagerImpl executor, DB db) {
			this.executor = executor;
			this.db = db;
		}

		@Override
		public void run() {
			executor.processTaskToDo();
			db.commitAndCloseSession();	
		}
	}
	
	public static class DummySerializableTask implements LongRunnable, Runnable {

		private static final long serialVersionUID = 7459138015999298102L;
		
		private final String taskName;
		public DummySerializableTask(String taskName) {
			this.taskName = taskName;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				//
			}
			taskCounter.get(taskName).incrementAndGet();
			mpFinishCount.countDown();
		}
	}
	
	public static class DummyTask implements Runnable {
		
		private final CountDownLatch finishCount;
		
		public DummyTask(CountDownLatch finishCount) {
			this.finishCount = finishCount;
		}
		
		@Override
		public void run() {
			finishCount.countDown();
		}
	}
}
