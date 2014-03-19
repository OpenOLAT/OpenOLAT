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
package org.olat.core.util.threadlog;

import org.apache.log4j.Appender;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;

/**
 * Helper Manager which provides a feature called
 * thread-based log-level control.
 * <p>
 * This allows a thread to be instructed to control
 * log4j's log-level overwriting any log level otherwise
 * set in the individual Logger instances.
 * <p>
 * This way it allows for example for a certain
 * request or requests for a certain user etc to be
 * switched to log level DEBUG irrespective of any
 * other log level set. Hence it allows to see more
 * details in for example a productive system which 
 * has certain problems which can't be detected in a
 * test system.
 * <p>
 * Clearly this 'technique' is not a replacement for 
 * a proper test environment and test suite - but it 
 * provides an instrument for debugging challenging
 * production issues.
 * <p>
 * Note: Any Logger created before ThreadLocalLogLevelManager.install()
 * is called will not be redirected! This can be overwritten
 * though by modifying the code in
 * ThreadLocalAwareLoggerRepository.getLogger but be careful if you do!
 * <P>
 * Initial Date:  04.08.2010 <br>
 * @author Stefan
 */
public class ThreadLocalLogLevelManager {

	/** this is the guard used for locking the RepositorySelector with log4j - see {@link LogManager#setRepositorySelector(RepositorySelector, Object)} **/
	private final Object guard = new Object();
	
	/** The actual - static - ThreadLocal used for controlling thread based log levels **/
	private final ThreadLocal<LogConfig> threadLocalLogLevel_ = new ThreadLocal<LogConfig>();
	
	/** part of the glue code - represents a Repository which basically overwrites the getLogger, allowing Loggers
	 * to be returned properly which were created before the ThreadLocalLogLevelManager was created! **/
	private static class ThreadLocalAwareLoggerRepository extends Hierarchy {

		private final LoggerFactory loggerFactory_;
		private final LoggerRepository parentLoggerRepository_;

		public ThreadLocalAwareLoggerRepository(Logger originalRoot, LoggerRepository parentRepository, LoggerFactory loggerFactory) {
			super(originalRoot);
			if (loggerFactory==null) {
				throw new IllegalArgumentException("loggerFactory must not be null");
			}
			loggerFactory_ = loggerFactory;
			
			parentLoggerRepository_ = parentRepository;
		}
		
		@Override
		public Logger getLogger(String name, LoggerFactory factory) {
			Logger existingLogger = parentLoggerRepository_.exists(name);
			if (existingLogger!=null) {
				// Returning the original logger here - note that this will prevent certain loggers from being 
				// taken under ThreadLocalAwareLogger's control - hence any logger created before 
				// ThreadLocalLogLevelManager.install() will not be redirected!
				return existingLogger;
			} else {
				return super.getLogger(name, loggerFactory_);
			}
		}
	}
	
	/**
	 * Creates and installs the SINGLETON ThreadLocalLogLevelManager - used by Spring
	 */
	public ThreadLocalLogLevelManager() {
		install();
	}
	
	/**
	 * Installs the ThreadLogManager in this system.
	 * <p>
	 * Note that this can fail if some other framework
	 * has done a call to LogManager.setRepositorySelector
	 * with a guard already.
	 * <p>
	 * Note that this variant of install will automatically
	 * prepend each log message with the text
	 * <pre>
	 * [ThreadLocal-LogLevel-Overwrite]
	 * </pre>
	 * @see org.apache.log4j.LogManager#setRepositorySelector(org.apache.log4j.spi.RepositorySelector, Object)
	 */
	void install() {
		install(new LogMessageModifier() {
				
				@Override
				public Object modifyLogMessage(Object logMessage) {
					return "[ThreadLocal-LogLevel-Overwrite] "+logMessage;
				}
		});
	}
	
	/**
	 * Installs the ThreadLogManager in this system.
	 * <p>
	 * Note that this can fail if some other framework
	 * has done a call to LogManager.setRepositorySelector
	 * with a guard already.
	 * @see org.apache.log4j.LogManager#setRepositorySelector(org.apache.log4j.spi.RepositorySelector, Object)
	 * @param logMessageModifier optional implementation of LogMessageModifier
	 * which allows messages to be modified should they be affected by
	 * a threadlocal loglevel overwrite. This allows for example for 
	 * messages to be prepended with a token so that they can be easier
	 * found in the log
	 */
	void install(final LogMessageModifier logMessageModifier) {
		
		try{
			final LoggerFactory loggerFactory = new LoggerFactory() {
				
				@SuppressWarnings("synthetic-access")
				@Override
				public Logger makeNewLoggerInstance(String name) {
					return new ThreadLocalAwareLogger(name, threadLocalLogLevel_, logMessageModifier);
				}
			};
			
			final Logger originalRootLogger = LogManager.getRootLogger();
			
			final LoggerRepository parentRepository = originalRootLogger.getLoggerRepository();
			
			final LoggerRepository repository = new ThreadLocalAwareLoggerRepository(originalRootLogger, parentRepository, loggerFactory);
			
			LogManager.setRepositorySelector(new RepositorySelector() {
				
				@Override
				public LoggerRepository getLoggerRepository() {
					return repository;
				}
			}, guard);
		} catch (IllegalArgumentException | SecurityException re) {
			// thrown by LogManager.setRepositorySelector
			Logger.getLogger(ThreadLocalLogLevelManager.class).error("Could not install ThreadLocalLogLevelManager");
		}
		
	}
	
	void forceThreadLocalLogLevel(LogConfig logConfig) {
		if (logConfig==null) {
			throw new IllegalArgumentException("logConfig must not be null");
		}
		threadLocalLogLevel_.set(logConfig);
	}
	
	void forceThreadLocalLogLevel(Priority forcedPriority, Appender forcedAppender) {
		if (forcedPriority==null && forcedAppender==null) {
			throw new IllegalArgumentException("forcedPriority and forcedAppender cannot be both null");
		}
		threadLocalLogLevel_.set(new LogConfig(forcedPriority, forcedAppender));
	}
	
	void releaseForcedThreadLocalLogLevel() {
		threadLocalLogLevel_.remove();
	}
}
