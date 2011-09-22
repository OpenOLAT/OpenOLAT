package org.olat.core.logging.activity;


import javax.servlet.http.HttpServletRequest;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.RunnableWithException;

/**
 * Helper class to install the IUserActivityLogger with the ThreadLocalUserActivityLogger.
 * <p>
 * This class should only be used in a few select core olat places to assure
 * a ThreadLocalUserActivityLogger is set up during event handling and in doDispose().
 * <p>
 * There are two groups of methods in this class: those used for running
 * a piece of code with a guaranteed and managed IUserActivityLogger set in the
 * ThreadLocalUserActivityLogger - and those doing initialization. 
 * <P>
 * Initial Date:  21.10.2009 <br>
 * @author Stefan
 */
public class ThreadLocalUserActivityLoggerInstaller {

	private static final OLog log_ = Tracing.createLoggerFor(ThreadLocalUserActivityLoggerInstaller.class);

	/**
	 * Run the given runnable (which allows an Exception to be thrown) with
	 * the given IUserActivityLogger as the ThreadLocalUserActivityLogger.
	 * <p>
	 * @param runnable the runnable to be run
	 * @param logger the logger to be set during the runnable.run call
	 * @throws Exception thrown by runnable.run
	 */
	public static void runWithUserActivityLoggerWithException(RunnableWithException runnable, IUserActivityLogger logger) throws Exception {
		// support nested ThreadLocalUserActivityLogger calls
		final IUserActivityLogger originalLogger = ThreadLocalUserActivityLogger.userActivityLogger_.get();
		
		// set the new logger
		ThreadLocalUserActivityLogger.userActivityLogger_.set(UserActivityLoggerImpl.copyLoggerForRuntime(logger));
		
		// now run the runnable
		try{
			runnable.run();
		} finally {
			// make sure to reset the original logger in the end
			ThreadLocalUserActivityLogger.userActivityLogger_.set(originalLogger);
		}
	}

	/**
	 * Run the given runnable with the given IUserActivityLogger as the
	 * ThreadLocalUserActivityLogger.
	 * <p>
	 * @param runnable the runnable to be run
	 * @param logger the logger to be set during the runnable.run call
	 */
	public static void runWithUserActivityLogger(Runnable runnable, IUserActivityLogger logger) {
		// support nested ThreadLocalUserActivityLogger calls
		final IUserActivityLogger originalLogger = ThreadLocalUserActivityLogger.userActivityLogger_.get();
		
		if (logger==null) {
			if (originalLogger==null) {
				log_.warn("runWithUserActivityLogger: no logger available. Reinitializing...");
				logger = new UserActivityLoggerImpl();
//				throw new IllegalStateException("logger was null and no ThreadLocal UserActivityLogger set");
			}else{ 
				logger = originalLogger;
			}
		}
		if(logger==null) throw new IllegalStateException("PostCondition logger != null violated:");
		
		// set the new logger
		if (originalLogger==null) {
			// avoid a WARN within copyLoggerForRuntime/getUserActivityLogger about no logger being avail.
			// this is a valid situation for e.g. cluster events via jms
			ThreadLocalUserActivityLoggerInstaller.initEmptyUserActivityLogger();
		}
		final UserActivityLoggerImpl newThreadLocalUserActivityLogger = UserActivityLoggerImpl.copyLoggerForRuntime(logger);
		ThreadLocalUserActivityLogger.userActivityLogger_.set(newThreadLocalUserActivityLogger);
		
		// now run the runnable
		try{
			runnable.run();
		} finally {
			// make sure to reset the original logger in the end
			if(log_.isDebug() && originalLogger == null) log_.debug("reset original logger back with originalLogger == null!");
			ThreadLocalUserActivityLogger.userActivityLogger_.set(originalLogger);
		}
	}

	/**
	 * FRAMEWORK USE ONLY
	 * Sets the ThreadLocal's UserActivityLogger to a generic one wrapping the given request's session
	 * @param request
	 */
	public static void initUserActivityLogger(HttpServletRequest request) {
		ThreadLocalUserActivityLogger.userActivityLogger_.set(new UserActivityLoggerImpl(request));
	}

	/**
	 * FRAMEWORK USE ONLY
	 * Sets the ThreadLocal's UserActivityLogger to a generic one
	 * @param request
	 */
	public static void initEmptyUserActivityLogger() {
		ThreadLocalUserActivityLogger.userActivityLogger_.set(new UserActivityLoggerImpl());
	}

	/**
	 * FRAMEWORK USE ONLY
	 * Returns an empty UserActivityLogger
	 */
	public static IUserActivityLogger createEmptyUserActivityLogger() {
		return new UserActivityLoggerImpl();
	}

	/**
	 * FRAMEWORK USE ONLY
	 * Sets the ThreadLocal's UserActivityLogger back to null
	 */
	public static void resetUserActivityLogger() {
		ThreadLocalUserActivityLogger.userActivityLogger_.set(null);
	}

}
