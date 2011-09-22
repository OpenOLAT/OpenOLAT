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
