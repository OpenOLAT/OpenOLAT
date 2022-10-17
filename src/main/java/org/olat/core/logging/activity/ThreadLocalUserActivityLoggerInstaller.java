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
package org.olat.core.logging.activity;


import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.RunnableWithException;
import org.olat.core.util.UserSession;

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

	private static final Logger log_ = Tracing.createLoggerFor(ThreadLocalUserActivityLoggerInstaller.class);

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
			if(log_.isDebugEnabled() && originalLogger == null) log_.debug("reset original logger back with originalLogger == null!");
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
	
	public static IUserActivityLogger initUserActivityLogger(UserSession session) {
		ThreadLocalUserActivityLogger.userActivityLogger_.set(new UserActivityLoggerImpl(session));
		return ThreadLocalUserActivityLogger.userActivityLogger_.get();
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
