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
package org.olat.core.commons.services.taskexecutor;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.OLATResource;

/**
 * 
 * Description:<br>
 * Generic task executor to run tasks in it's own threads. Use it to decouple stuff that might
 * takes more time than a user may is willing to wait. The task gets executed by a thread pool.
 * Task only marked as Runnable are executed immediately. Task marked by interface LongRunnable
 * will be persisted to the database and run after some time.
 * 
 * If you look for scheduled task see @see {@link org.olat.core.commons.services.scheduler}
 * 
 * <P>
 * Initial Date:  02.05.2007 <br>
 * @author guido
 * @author srosse, stephane.rosse@frentix.com, http://www.frnetix.com
 */
public interface TaskExecutorManager extends Executor {
	
	public static final OLATResourceable TASK_EVENTS = OresHelper.createOLATResourceableType("ExTask");
	
	public void execute(LongRunnable task, Identity creator, OLATResource resource,
			String resSubPath, Date scheduledDate);
	
	public void executeTaskToDo();
	
	/**
	 * Pick a task for edition, but don't forget to return it!
	 * 
	 * @param task
	 * @return
	 */
	public Task pickTaskForEdition(Task task);
	
	/**
	 * 
	 * @param task
	 * @param wishedStatus
	 * @return
	 */
	public Task returnTaskAfterEdition(Task task, TaskStatus wishedStatus);
	
	/**
	 * Update the task and set the status to 'newTask' to rescheduled it.
	 * 
	 * @param task
	 * @param runnableTask
	 * @param modifier
	 * @param scheduledDate
	 */
	public void updateAndReturn(Task task, LongRunnable runnableTask, Identity modifier, Date scheduledDate);
	
	public void updateProgress(Task task, Double progress, String checkpoint);
	
	public Double getProgress(Task task);
	
	public TaskStatus getStatus(Task task);
	
	public <T extends Runnable> T getPersistedRunnableTask(Task task, Class<T> type);
	
	public List<Task> getTasks(OLATResource resource);
	
	public List<Task> getTasks(OLATResource resource, String resSubPath);
	
	public List<Identity> getModifiers(Task task);
	
	public Task cancel(Task task);
	
	public void delete(Task task);
	
	/**
	 * Delete all tasks link to the specified resource
	 * @param resource
	 */
	public void delete(OLATResource resource);
	
	/**
	 * Delete all the tasks linked to the specified resource with
	 * the specific sub path.
	 * @param resource The resource
	 * @param resSubPath The sub path (cannot be null)
	 */
	public void delete(OLATResource resource, String resSubPath);
	
	/**
	 * This is a light weight, not clustered way to delay a task
	 * a few seconds. Don't abuse of it, only delay of a few seconds
	 * is acceptable because the tasks are serialized and the task is
	 * hold in memory. 
	 * 
	 * @param task
	 * @param delay
	 */
	public void schedule(TimerTask task, long delay);

}
