/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.commons.services.taskexecutor.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskAwareRunnable;
import org.olat.core.commons.services.taskexecutor.TaskEvent;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskRunnable;
import org.olat.core.commons.services.taskexecutor.TaskRunnable.Queue;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.DBSecureRunnable;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.taskexecutor.model.PersistentTaskRunnable;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.resource.OLATResource;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * 
 * Description:<br>
 * Generic task executor to run tasks in it's own threads. Use it to decouple stuff that might
 * takes more time than a user may is willing to wait. The task gets executed by a thread pool.
 * If you look for scheduled task see @see {@link org.olat.core.commons.services.scheduler}
 * 
 * <P>
 * Initial Date:  02.05.2007 <br>
 * @author guido
 * @author srosse, stephane.rosse@frentix.com, http://www.frnetix.com
 */
public class TaskExecutorManagerImpl implements TaskExecutorManager {
	private static final Logger log = Tracing.createLoggerFor(TaskExecutorManagerImpl.class);
	
	private final ExecutorService taskExecutor;
	private final ExecutorService externalExecutor;
	private final ExecutorService sequentialTaskExecutor;
	private final ExecutorService lowPriorityTaskExecutor;
	
	private DB dbInstance;
	private Scheduler scheduler;
	private PersistentTaskDAO persistentTaskDao;
	private ConcurrentMap<Long, Future<?>> taskKeyToFuture = new ConcurrentHashMap<>();
	
	private Timer timer = new Timer();

	/**
	 * [used by spring]
	 */
	private TaskExecutorManagerImpl(ExecutorService mpTaskExecutor, ExecutorService sequentialTaskExecutor,
			ExecutorService lowPriorityTaskExecutor, ExecutorService externalExecutor) {
		this.taskExecutor = mpTaskExecutor;
		this.externalExecutor = externalExecutor;
		this.sequentialTaskExecutor = sequentialTaskExecutor;
		this.lowPriorityTaskExecutor = lowPriorityTaskExecutor;
	}
	
	/**
	 * [used by Spring]
	 * @param scheduler
	 */
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}

	/**
	 * [used by Spring]
	 * @param persistentTaskDao
	 */
	public void setPersistentTaskDao(PersistentTaskDAO persistentTaskDao) {
		this.persistentTaskDao = persistentTaskDao;
	}

	public void shutdown() {
		timer.cancel();
		taskExecutor.shutdownNow();
		externalExecutor.shutdownNow();
		sequentialTaskExecutor.shutdownNow();
		lowPriorityTaskExecutor.shutdownNow();
	}
	
	@Override
	public void execute(Runnable task) {
		//wrap call to the task here to catch all errors that are may not catched yet in the task itself
		//like outOfMemory or other system errors.
		Task persistentTask = null;
		if(task instanceof LongRunnable) {
			LongRunnable lRunnable = (LongRunnable)task;
			persistentTask = persistentTaskDao.createTask(UUID.randomUUID().toString(), lRunnable);
			dbInstance.commit();
			
			if(!lRunnable.isDelayed()) {
				Queue queue = getExecutorsQueue(lRunnable);
				processTaskToDo(persistentTask.getKey(), queue, new ArrayList<>());
			}
		} else {
			Queue queue = Queue.standard;
			if(task instanceof TaskRunnable) {
				queue = ((TaskRunnable)task).getExecutorsQueue();
			}
			execute(task, persistentTask, queue);
		}
	}

	@Override
	public void execute(LongRunnable task, Identity creator, OLATResource resource,
			String resSubPath, Date scheduledDate) {

		Task persistentTask = persistentTaskDao.createTask(UUID.randomUUID().toString(), task, creator, resource, resSubPath, scheduledDate);
		dbInstance.commit();
		
		if(!task.isDelayed()) {
			Queue queue = getExecutorsQueue(task);
			processTaskToDo(persistentTask.getKey(), queue, new ArrayList<>());
		}
	}
	
	private void execute(Runnable task, Task persistentTask, Queue queue) {
		if (taskExecutor != null) {
			if(task instanceof TaskAwareRunnable) {
				((TaskAwareRunnable)task).setTask(persistentTask);
			}
			
			DBSecureRunnable safetask = new DBSecureRunnable(task);
			Future<?> future = null;
			if(queue == Queue.sequential) {
				future = sequentialTaskExecutor.submit(safetask);
			} else if(queue == Queue.lowPriority) {
				future = lowPriorityTaskExecutor.submit(task);
			} else if(queue == Queue.external) {
				future = externalExecutor.submit(task);
			} else {
				future = taskExecutor.submit(safetask);
			}
			if(future != null) {
				if(persistentTask instanceof PersistentTask) {
					taskKeyToFuture.put(((PersistentTask)persistentTask).getKey(), future);
				} else if(task instanceof PersistentTaskRunnable) {
					taskKeyToFuture.put(((PersistentTaskRunnable)task).getTaskKey(), future);
				}
			}
		} else {
			log.error("taskExecutor is not initialized (taskExecutor=null). Do not call 'runTask' before TaskExecutorModule is initialized.");
			throw new AssertException("taskExecutor is not initialized");
		}
	}

	@Override
	public void executeTaskToDo() {
		try {
			scheduler.triggerJob(new JobKey("taskExecutorJob", Scheduler.DEFAULT_GROUP));
		} catch (SchedulerException e) {
			log.error("", e);
		}
	}
	
	protected void processTaskToDo() {
		List<Queue> filled = new ArrayList<>(3);
		
		try {
			List<Long> todos = persistentTaskDao.tasksToDo();
			for(Long todo:todos) {
				PersistentTask task = persistentTaskDao.loadTaskById(todo);
				Runnable runnable = persistentTaskDao.deserializeTask(task);
				Queue queue = getExecutorsQueue(runnable);
				if(!filled.contains(queue)) {
					processTaskToDo(todo, queue, filled);
				}
			}
		} catch (Exception e) {
			// ups, something went completely wrong! We log this but continue next time
			log.error("Error while executing task todo", e);
		}		
	}
	
	private void processTaskToDo(Long todo, Queue queue, List<Queue> filled) {
		PersistentTaskRunnable command = new PersistentTaskRunnable(todo);
		try {
			execute(command, null, queue);
		} catch(RejectedExecutionException e) {
			log.info("Queue is currently filled");
			dbInstance.rollbackAndCloseSession();
			filled.add(queue);
		}
	}
	
	private Queue getExecutorsQueue(Runnable runnable) {
		Queue queue = Queue.standard;
		if(runnable instanceof TaskRunnable) {
			queue = ((TaskRunnable)runnable).getExecutorsQueue();
		}
		return queue;
	}

	@Override
	public List<Task> getTasks(OLATResource resource) {
		return persistentTaskDao.findTasks(resource);
	}
	
	@Override
	public List<Task> getTasks(OLATResource resource, String resSubPath) {
		return persistentTaskDao.findTasks(resource, resSubPath);
	}

	@Override
	public List<Identity> getModifiers(Task task) {
		return persistentTaskDao.getModifiers(task);
	}

	@Override
	public Task pickTaskForEdition(Task task) {
		return persistentTaskDao.pickTaskForEdition(task.getKey());
	}

	@Override
	public Task returnTaskAfterEdition(Task task, TaskStatus wishedStatus) {
		return persistentTaskDao.returnTaskAfterEdition(task.getKey(), wishedStatus);
	}

	@Override
	public <T extends Runnable> T getPersistedRunnableTask(Task task, Class<T> type) {
		if(task instanceof PersistentTask) {
			PersistentTask ptask = (PersistentTask)task;
			@SuppressWarnings("unchecked")
			T runnable = (T)persistentTaskDao.deserializeTask(ptask);
			return runnable;
		}
		return null;
	}
	
	@Override
	public void updateAndReturn(Task task, LongRunnable runnableTask, Identity modifier, Date scheduledDate) {
		persistentTaskDao.updateTask(task, runnableTask, modifier, scheduledDate);
	}

	@Override
	public void updateProgress(Task task, Double progress, String checkpoint) {
		persistentTaskDao.updateProgressTask(task, progress, checkpoint);
	}

	@Override
	public Double getProgress(Task task) {
		if(task == null || task.getKey() == null) return null;
		return persistentTaskDao.getProgress(task);
	}

	@Override
	public TaskStatus getStatus(Task task) {
		if(task == null || task.getKey() == null) return null;
		return persistentTaskDao.getStatus(task);
	}

	@Override
	public Task cancel(Task task) {
		if(task instanceof PersistentTask) {
			PersistentTask pTask = (PersistentTask)task;
			Future<?> future = taskKeyToFuture.get(pTask.getKey());
			if(future != null) {
				future.cancel(true);
			}
			return persistentTaskDao.taskCancelled(pTask);
		}
		return null;
	}

	@Override
	public void delete(Task task) {
		persistentTaskDao.delete(task);
		dbInstance.commit();
		if(task instanceof PersistentTask) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new TaskEvent(TaskEvent.TASK_DELETED, task.getKey()), TaskExecutorManager.TASK_EVENTS);
		}
	}

	@Override
	public void delete(OLATResource resource) {
		persistentTaskDao.delete(resource);
	}

	@Override
	public void delete(OLATResource resource, String resSubPath) {
		persistentTaskDao.delete(resource, resSubPath);
	}

	@Override
	public void schedule(TimerTask task, long delay) {
		timer.schedule(task, delay);
	}
	

}
