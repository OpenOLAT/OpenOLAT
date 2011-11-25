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
* <p>
*/
package org.olat.repository.async;

import java.util.Queue;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * this is a special task executor system used only for learning resources. If you look for a general task Executor for scheduled tasks
 * search for TaskExecutorManager which does Quartz scheduled tasks.
 * TODO:gs may convert this to taskexecutor manager
 * @author Christian Guretzki
 */
public class TaskExecutorThread extends Thread {
	private static OLog log = Tracing.createLoggerFor(TaskExecutorThread.class);
	Queue<BackgroundTask> taskQueue;

	public TaskExecutorThread(Queue<BackgroundTask> taskQueue) {
		super("TaskExecutorThread");
		this.taskQueue = taskQueue;
	}
	
	public void run() {
		log.info("TaskExecutorThread started");
		boolean running = true;
		while(running) {
			synchronized(taskQueue) {
				if (taskQueue.size() == 0) {
					try {
						log.debug("TaskExecutorThread waiting...");
						taskQueue.wait();
					} catch (InterruptedException e) {
						log.error("Execption when waiting", e);
					}
				}
			}
			log.debug("TaskExecutorThread in working loop");
			if (taskQueue.size() > 0) {
				// Queue is not empty
				long startTime = 0;
				if (log.isDebug()) {
					startTime = System.currentTimeMillis();
				}
				if (taskQueue.size() > 10) {
					if (taskQueue.size() > 20) {
						log.error("Too many item in background-job queue, queue-size=" + taskQueue.size() + ", check execution-time");
					} else {
						log.warn("Many item in background-job queue, queue-size=" + taskQueue.size() + ", check execution-time");
					}
				}
				int executeCount = 0;
				while(!taskQueue.isEmpty()) {
					log.debug("TaskExecutorThread taskQueue is not empty => execute task");
					taskQueue.poll().execute();
					executeCount++;
		    }
				// running in a seperate thread, we must close db-session after each run.
				DBFactory.getInstance().commitAndCloseSession();
				if (log.isDebug()) {
					long endTime = System.currentTimeMillis();
					log.debug("TaskExecutorThread executed in " + (endTime - startTime) + "ms, executeCount=" + executeCount );
				}
			} else {
				log.warn("taskQueue notified but queue was empty");
			}
		}
		log.info("TaskExecutorThread finished");
	}

}
