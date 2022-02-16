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
package org.olat.core.commons.services.taskexecutor.model;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.taskexecutor.TaskAwareRunnable;
import org.olat.core.commons.services.taskexecutor.TaskEvent;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.manager.PersistentTaskDAO;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PersistentTaskRunnable implements Runnable {
	
	private static final Logger log = Tracing.createLoggerFor(PersistentTaskRunnable.class);
	private final Long taskKey;
	
	public PersistentTaskRunnable(Long taskKey) {
		this.taskKey = taskKey;
	}
	
	public Long getTaskKey() {
		return taskKey;
	}

	@Override
	public void run() {
		PersistentTaskDAO taskDao = CoreSpringFactory.getImpl(PersistentTaskDAO.class);
		PersistentTask task = null;
		try {
			task = taskDao.loadTaskById(taskKey);
			if(task != null) {
				task = taskDao.pickTaskForRun(task);
				if(task != null) {
					CoordinatorManager.getInstance().getCoordinator().getEventBus()
						.fireEventToListenersOf(new TaskEvent(TaskEvent.TASK_STARTED, taskKey), TaskExecutorManager.TASK_EVENTS);
					
					Runnable runnable = taskDao.deserializeTask(task);
					if(runnable instanceof TaskAwareRunnable) {
						((TaskAwareRunnable)runnable).setTask(task);
					}
					runnable.run();
					
					markAsDone(task);
					CoordinatorManager.getInstance().getCoordinator().getEventBus()
						.fireEventToListenersOf(new TaskEvent(TaskEvent.TASK_DONE, taskKey), TaskExecutorManager.TASK_EVENTS);
				}
			}
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Throwable e) {
			DBFactory.getInstance().rollbackAndCloseSession();
			markAsFailed(task);
			log.error("Error while running task in a separate thread: {}", (task == null ? "NULL" : task.getKey()), e);
		}
	}
	
	private void markAsDone(PersistentTask task) {
		if(task == null) return;
		try {
			// remove it from the cache
			DBFactory.getInstance().getCurrentEntityManager().detach(task);

			PersistentTaskDAO taskDao = CoreSpringFactory.getImpl(PersistentTaskDAO.class);
			task = taskDao.loadTaskById(task.getKey());
			if(task != null) {
				taskDao.taskDone(task);
				DBFactory.getInstance().commitAndCloseSession();
			}
		} catch (Exception e1) {
			DBFactory.getInstance().rollbackAndCloseSession();
		}
	}
	
	private void markAsFailed(PersistentTask task) {
		if(task == null) return;
		try {
			PersistentTaskDAO taskDao = CoreSpringFactory.getImpl(PersistentTaskDAO.class);
			taskDao.taskFailed(task);
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e1) {
			DBFactory.getInstance().rollbackAndCloseSession();
		}
	}

	@Override
	public int hashCode() {
		return taskKey == null ? 45786 : taskKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof PersistentTaskRunnable) {
			PersistentTaskRunnable task = (PersistentTaskRunnable)obj;
			return taskKey != null && taskKey.equals(task.taskKey);
		}
		return false;
	}
}
