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
*/
package org.olat.core.commons.taskExecutor;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.util.StopWatch;

class ThreadPoolTaskExecutor {
	OLog log = Tracing.createLoggerFor(this.getClass());
	ThreadPoolExecutor threadPool = null;
	//The queue all the tasks get filled in and are taken from, if the queue is full the server starts to reject new tasks
	final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1000);

	/**
	 * @param poolSize
	 * @param maxPoolSize
	 * @param keepAliveTime
	 */
	public ThreadPoolTaskExecutor(int poolSize, int maxPoolSize, int keepAliveTime) {
		threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);
	}

	public void runTask(Runnable task) {
		StopWatch watch = null;
		if (log.isDebug()) {
			watch = new StopWatch();
			watch.start();
		}		
		threadPool.execute(task);
		
		if (log.isDebug()) watch.stop();
		if (log.isDebug()) log.debug("Current size of queue is: "+queue.size()+". Running last task took (ms): "+watch.getTotalTimeMillis());
	}

	public void shutDown() {
		// Initiates orderly shutdown and don't accept creation of new threads
		threadPool.shutdown();
		if (threadPool.getActiveCount() > 0) {
			// Stop actively executing threads NOW!
			List<Runnable> stoppedThreads = threadPool.shutdownNow();
			for (Runnable runnable : stoppedThreads) {
				log.info("Shutting down acive thread", runnable.toString());
			}
		}
	}

}
