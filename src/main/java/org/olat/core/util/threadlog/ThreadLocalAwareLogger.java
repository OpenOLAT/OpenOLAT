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
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Specialization of the generic log4j Logger which allows 
 * thread based log level control.
 * <p>
 * That is, the log levels can be controlled in two ways:
 * <ul>
 *  <li>the standard setLevel() or via the config file, log4j way</li>
 *  <li>via {@link ThreadLocalLogLevelManager#forceThreadLocalLogLevel(Priority)} overwriting
 *      the above level if it is lower</li>
 * </ul>
 * <P>
 * Performance note: Logging can be critical to performance
 * and any modification of log4j is therefore prone to cause
 * performance 'leaks'. This method has been written with 
 * this in mind but does indeed add a (although only very minimal)
 * overhead to the normal isDebugEnabled()/debug() performance.
 * The impact should be minimal though. Still, enabling this in
 * production should be optional.
 * <p>
 * Initial Date:  05.08.2010 <br>
 * @author Stefan
 */
public class ThreadLocalAwareLogger extends Logger {

	/** The ThreadLocal used for controlling thread based log levels - passed to this Logger in the constructor **/
	private final ThreadLocal<LogConfig> threadLocalLogLevel_;
	
  /**
    (from log4j Category)
    The fully qualified name of the Category class. See also the
    getFQCN method. */
	private static final String FQCN = Category.class.getName();

	private final LogMessageModifier logMessageModifier_;
	
	/**
	 * Creates a new ThreadLocalAwareLogger of the given name, 
	 * using the given ThreadLocal - with which log levels can
	 * be overwritten on a ThreadLocal basis - and uses
	 * an optional (i.e. can be null) LogMessageModifier
	 * @param name the name of this Logger
	 * @param threadLocalLogLevel the ThreadLocal to use to fetch
	 * overwritten log levels
	 * @param logMessageModifier optional, allows to modify 
	 * log messages when they are affected by a ThreadLocal
	 * log level overwrite - NOTE: This will convert the message
	 * passed to the 
	 */
	ThreadLocalAwareLogger(String name, ThreadLocal<LogConfig> threadLocalLogLevel, LogMessageModifier logMessageModifier) {
		super(name);
		if (threadLocalLogLevel==null) {
			throw new IllegalArgumentException("threadLocalLogLevel must not be null");
		}
		threadLocalLogLevel_ = threadLocalLogLevel;
		logMessageModifier_ = logMessageModifier;
	}

	/**
	 * Internal helper method to check whether this logger 
	 * would log the given level anyway - anyway meaning without
	 * the feature 'threadLocalLogLevel_'.
	 * @param level the level which should be checked if that would anyway be logged
	 * @return true if the given level would anyway be logged by the superclass
	 */
	private final boolean isLoggingAnyway(Priority priority) {
    if(repository.isDisabled(priority.toInt()))
      return false;
    return (priority.isGreaterOrEqual(this.getEffectiveLevel()));
	}

	/**
	 * Internal helper method to check whether the 
	 * 'threadLocalLogLevel_' would force the given level
	 * to be logged - i.e. if the 'threadLocalLogLevel_'
	 * is smaller or equal to the given level.
	 * <p>
	 * Note that this is the core of this ThreadLocalAwareLogger feature.
	 * @param level the level which should be checked if that is now forced
	 * to be logged by the 'threadLocalLogLevel_'
	 * @return true if the given level is now forced to be logged
	 * by the 'threadLocalLogLevel_'
	 */
	private final boolean isForcedToLog(Priority priority) {
		final LogConfig logConfig = threadLocalLogLevel_.get();
		return (logConfig!=null) && (logConfig.getPriority()!=null) && priority.isGreaterOrEqual(logConfig.getPriority());
	}
	
	/**
	 * Return the Appender if set in this ThreadLocal - used when isForcedToLog returns true
	 * @return the Appender if set in this ThreadLocal
	 */
	private final Appender getForcedAppender() {
		final LogConfig logConfig = threadLocalLogLevel_.get();
		return (logConfig==null) ? null : logConfig.getAppender();
	}
	
	/**
	 * Internal helper method to convert an original log message
	 * using the optional logMessageModifier.
	 * <p>
	 * The logMessageModifier is a hook for users of the 
	 * ThreadLocalAwareLogger to e.g. prepend every log message
	 * which was forced to log (i.e. isForcedToLog returns true)
	 * with a prefix.
	 * @param originalMessage the original message which should be modified if
	 * there is a logMessageModifier
	 * @return the originalMessage if no logMessageModifier is set
	 * or the result from the logMessageModifier.modifyLogMessage call
	 */
	private final Object modifyLogMessage(Object originalMessage) {
		if (logMessageModifier_!=null) {
			return logMessageModifier_.modifyLogMessage(originalMessage);
		} else {
			return originalMessage;
		}
	}

	/**
	 * Logger worker method - does the actual checks whether the given message
	 * should be logged at the given priority - taking into account this logger's
	 * loglevel plus the threadLocalLogLevel.
	 * @param priority the priority at which the given message should be logged
	 * @param message the message which should be logged (or not, depending on levels)
	 */
	private final void threadLocalAwareLog(String fQCN, Priority priority, Object message, Throwable t) {
		if (isForcedToLog(priority)) {

			final Appender forcedAppender = getForcedAppender();
			
			// force the logging and modify the log message (the latter might return the original message)
			if (forcedAppender!=null) {
				// only call the forced appender
				forcedAppender.doAppend(new LoggingEvent(fQCN, this, priority, modifyLogMessage(message), t));
			} else {
				// go via the normal appenders
				forcedLog(fQCN, priority, modifyLogMessage(message), t);
			}
		
		} else {
			
			// else not forced to log - use default behaviour
			super.log(fQCN, priority, message, t);

		}
	}
	
	@Override
	public void trace(Object message) {
		threadLocalAwareLog(FQCN, Level.TRACE, message, null);
	}
	
	@Override
	public void trace(Object message, Throwable t) {
		threadLocalAwareLog(FQCN, Level.TRACE, message, t);
	}
	
	@Override
	public void debug(Object message) {
		threadLocalAwareLog(FQCN, Level.DEBUG, message, null);
	}

	@Override
	public void debug(Object message, Throwable t) {
		threadLocalAwareLog(FQCN, Level.DEBUG, message, t);
	}
	
	@Override
	public void info(Object message) {
		threadLocalAwareLog(FQCN, Level.INFO, message, null);
	}
	
	@Override
	public void info(Object message, Throwable t) {
		threadLocalAwareLog(FQCN, Level.INFO, message, t);
	}
	
	@Override
	public void warn(Object message) {
		threadLocalAwareLog(FQCN, Level.WARN, message, null);
	}
	
	@Override
	public void warn(Object message, Throwable t) {
		threadLocalAwareLog(FQCN, Level.WARN, message, t);
	}
	
	@Override
	public void error(Object message) {
		threadLocalAwareLog(FQCN, Level.ERROR, message, null);
	}
	
	@Override
	public void error(Object message, Throwable t) {
		threadLocalAwareLog(FQCN, Level.ERROR, message, t);
	}
	
	@Override
	public void fatal(Object message) {
		threadLocalAwareLog(FQCN, Level.FATAL, message, null);
	}
	
	@Override
	public void fatal(Object message, Throwable t) {
		threadLocalAwareLog(FQCN, Level.FATAL, message, t);
	}
	
	@Override
	public void log(Priority priority, Object message) {
		threadLocalAwareLog(FQCN, priority, message, null);
	}
	
	@Override
	public void log(Priority priority, Object message, Throwable t) {
		threadLocalAwareLog(FQCN, priority, message, t);
	}
	
	@Override
	public void log(String callerFQCN, Priority priority, Object message, Throwable t) {
		threadLocalAwareLog(callerFQCN, priority, message, t);
	}

	@Override
	public boolean isTraceEnabled() {
		return isLoggingAnyway(Level.TRACE) || isForcedToLog(Level.TRACE);
	}

	@Override
	public boolean isDebugEnabled() {
		return isLoggingAnyway(Level.DEBUG) || isForcedToLog(Level.DEBUG);
	}

	@Override
	public boolean isEnabledFor(Priority priority) {
		return isLoggingAnyway(priority) || isForcedToLog(priority);
	}

	@Override
	public boolean isInfoEnabled() {
		return isLoggingAnyway(Level.INFO) || isForcedToLog(Level.INFO);
	}

	@Override
	public void l7dlog(Priority priority, String key, Object[] params, Throwable t) {
		if (isForcedToLog(priority)) {
			// from super.l7dlog:
      String pattern = getResourceBundleString(key);
      String msg;
      if(pattern == null)
      	msg = key;
      else
      	msg = java.text.MessageFormat.format(pattern, params);

			final Appender forcedAppender = getForcedAppender();
      
			// force the logging and modify the log message (the latter might return the original message)
			if (forcedAppender!=null) {
				// only call the forced appender
				forcedAppender.doAppend(new LoggingEvent(FQCN, this, priority, modifyLogMessage(msg), t));
			} else {
				// go via the normal appenders
				forcedLog(FQCN, priority, modifyLogMessage(msg), t);
			}
      
		} else {
			super.l7dlog(priority, key, params, t);
		}
	}

	@Override
	public void l7dlog(Priority priority, String key, Throwable t) {
		if (isForcedToLog(priority)) {
			// from super.l7dlog:
			
      String msg = getResourceBundleString(key);
      // if message corresponding to 'key' could not be found in the
      // resource bundle, then default to 'key'.
      if(msg == null) {
      	msg = key;
      }

			final Appender forcedAppender = getForcedAppender();
      
			// force the logging and modify the log message (the latter might return the original message)
			if (forcedAppender!=null) {
				// only call the forced appender
				forcedAppender.doAppend(new LoggingEvent(FQCN, this, priority, modifyLogMessage(msg), t));
			} else {
				// go via the normal appenders
				forcedLog(FQCN, priority, modifyLogMessage(msg), t);
			}
		
		} else {
			super.l7dlog(priority, key, t);
		}
	}
	
	
}
