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
package org.olat.core.commons.taskExecutor.manager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.taskExecutor.LongRunnable;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.commons.taskExecutor.model.DBSecureRunnable;
import org.olat.core.commons.taskExecutor.model.PersistentTaskRunnable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;

/**
 * 
 * Description:<br>
 * Generic task executor to run tasks in it's own threads. Use it to decouple stuff that might
 * takes more time than a user may is willing to wait. The task gets executed by a thread pool.
 * If you look for scheduled task see @see {@link org.olat.core.commons.scheduler}
 * 
 * <P>
 * Initial Date:  02.05.2007 <br>
 * @author guido
 * @author srosse, stephane.rosse@frentix.com, http://www.frnetix.com
 */
public class TaskExecutorManagerImpl extends BasicManager implements TaskExecutorManager {
	private static final OLog log = Tracing.createLoggerFor(TaskExecutorManagerImpl.class);
	private final ExecutorService taskExecutor;
	
	private DB dbInstance;
	private PersistentTaskDAO persistentTaskDao;

	/**
	 * [used by spring]
	 */
	private TaskExecutorManagerImpl(ExecutorService threadPoolTaskExecutor) {
		this.taskExecutor = threadPoolTaskExecutor;
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
		taskExecutor.shutdown();
	}
	
	@Override
	public void execute(Runnable task) {
		//wrap call to the task here to catch all errors that are may not catched yet in the task itself
		//like outOfMemory or other system errors.
		
		if(task instanceof LongRunnable) {
			persistentTaskDao.createTask(UUID.randomUUID().toString(), (LongRunnable)task);
			dbInstance.commit();
		} else {
			if (taskExecutor != null) {
				DBSecureRunnable safetask = new DBSecureRunnable(task);
				taskExecutor.submit(safetask);
			} else {
				logError("taskExecutor is not initialized (taskExecutor=null). Do not call 'runTask' before TaskExecutorModule is initialized.", null);
				throw new AssertException("taskExecutor is not initialized");
			}
		}
	}

	@Override
	public void executeTaskToDo() {
		try {
			PersistentTaskDAO taskDao = CoreSpringFactory.getImpl(PersistentTaskDAO.class);
			TaskExecutorManager executor = CoreSpringFactory.getImpl(TaskExecutorManager.class);
			
			List<Long> todos = taskDao.tasksToDo();
			for(Long todo:todos) {
				PersistentTaskRunnable command = new PersistentTaskRunnable(todo);
				executor.execute(command);
			}
		} catch (Exception e) {
			// ups, something went completely wrong! We log this but continue next time
			log.error("Error while executing task todo", e);
		}		
	}
}
