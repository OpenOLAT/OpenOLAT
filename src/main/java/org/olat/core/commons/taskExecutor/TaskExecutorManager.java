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
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.commons.taskExecutor;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
/**
 * 
 * Description:<br>
 * Generic task executor to run tasks in it's own threads. Use it to decouple stuff that might
 * takes more time than a user may is willing to wait. The task gets executed immediately by a thread pool.
 * If you look for scheduled task see @see {@link org.olat.core.commons.scheduler}
 * 
 * <P>
 * Initial Date:  02.05.2007 <br>
 * @author guido
 */
public class TaskExecutorManager extends BasicManager {
	ThreadPoolTaskExecutor taskExecutor;
	
	private static TaskExecutorManager INSTANCE;
	
	/**
	 * [used by spring]
	 */
	private TaskExecutorManager(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		this.taskExecutor = threadPoolTaskExecutor;
		INSTANCE = this;
	}
	
	public static TaskExecutorManager getInstance() {
		return INSTANCE;
	}
	
	public void destroy() {
		taskExecutor.shutDown();
	}
	
	/**
	 * runs the task and wraps it in a new runnable to catch uncatched errors
	 * and may close db sessions used in the task.
	 * @param task
	 */
	public void runTask(final Runnable task) {
		//wrap call to the task here to catch all errors that are may not catched yet in the task itself
		//like outOfMemory or other system errors.
		Runnable safetask = new Runnable(){
			public void run() {
				try {
					task.run();
					DBFactory.getInstance().commitAndCloseSession();
				} catch (Throwable e) {
					DBFactory.getInstance().rollbackAndCloseSession();
					Tracing.logError("Error while running task in a separate thread.", e, TaskExecutorManager.class);
				}
			}
		};
		if (taskExecutor != null) {
			taskExecutor.runTask(safetask);
		} else {
			Tracing.logError("taskExecutor is not initialized (taskExecutor=null). Do not call 'runTask' before TaskExecutorModule is initialized.", TaskExecutorManager.class);
			throw new AssertException("taskExecutor is not initialized");
		}
	}
	
	/**
	 * TODO: to be used with GUI where the programmer can start a task and set a message that will appear
	 * like "your report will be generated and you will get an email if finised"
	 * or an icon that gets updated when the task is done and the user can go on with his work
	 * @param task
	 */
	public void runTaskWithNotificationWhenFinised(final Runnable task){
		throw new NoSuchMethodError();
	}
	
}
