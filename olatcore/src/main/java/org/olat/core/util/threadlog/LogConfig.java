package org.olat.core.util.threadlog;

import org.apache.log4j.Appender;
import org.apache.log4j.Priority;

/**
 * Configures a ThreadLocalAwareLogger with
 * a priority and an appender.
 * <P>
 * Initial Date:  10.08.2010 <br>
 * @author Stefan
 */
class LogConfig {

	/** this LogConfig's priority - can be null **/
	private final Priority priority_;
	
	/** this LogConfig's appender - can be null **/
	private final Appender appender_;

	/**
	 * Creates a new LogConfig object with the given priority and appender.
	 * <p>
	 * One of priority or appender is allowed to be null - but not both. 
	 * @param priority the priority - this or the appender is allowed to be null, but not both
	 * @param appender the appender - this or the priority is allowed to be null, but not both
	 */
	LogConfig(Priority priority, Appender appender) {
		if (priority==null && appender==null) {
			throw new IllegalArgumentException("priority and appender cannot be both null");
		}
		priority_ = priority;
		appender_ = appender;
	}
	
	/**
	 * Returns this LogConfig's priority - can be null
	 * @return this LogConfig's priority - can be null
	 */
	public Priority getPriority() {
		return priority_;
	}
	
	/**
	 * Returns this LogConfig's appender - can be null
	 * @return this LogConfig's appender - can be null
	 */
	public Appender getAppender() {
		return appender_;
	}
}
