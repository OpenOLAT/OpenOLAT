package org.olat.core.commons.taskExecutor;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.Tracing;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class Task implements Runnable {
	
	private final Runnable task;
	
	public Task(Runnable task) {
		this.task = task;
	}

	@Override
	public void run() {
		try {
			task.run();
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Throwable e) {
			DBFactory.getInstance().rollbackAndCloseSession();
			Tracing.logError("Error while running task in a separate thread.", e, TaskExecutorManager.class);
		}
	}
}
