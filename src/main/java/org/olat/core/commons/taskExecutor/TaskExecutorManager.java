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
package org.olat.core.commons.taskExecutor;

import java.util.concurrent.Executor;

/**
 * 
 * Description:<br>
 * Generic task executor to run tasks in it's own threads. Use it to decouple stuff that might
 * takes more time than a user may is willing to wait. The task gets executed by a thread pool.
 * Task only marked as Runnable are executed immediately. Task marked by interface LongRunnable
 * will be persisted to the database and run after some time.
 * 
 * If you look for scheduled task see @see {@link org.olat.core.commons.scheduler}
 * 
 * <P>
 * Initial Date:  02.05.2007 <br>
 * @author guido
 * @author srosse, stephane.rosse@frentix.com, http://www.frnetix.com
 */
public interface TaskExecutorManager extends Executor {
	
	
	public void executeTaskToDo();


}
