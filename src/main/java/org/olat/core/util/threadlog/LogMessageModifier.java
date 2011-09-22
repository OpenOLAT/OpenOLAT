package org.olat.core.util.threadlog;

/**
 * A LogMessageModifier is a hook interface which is used at
 * ThreadLocalLogLevelManager install time allowing the
 * implementor of this interface to modify a log message
 * before it is being sent to the log appenders.
 * <P>
 * Initial Date:  13.09.2010 <br>
 * @author Stefan
 * @see ThreadLocalLogLevelManager#install(LogMessageModifier)
 * @see ThreadLocalAwareLogger#modifyLogMessage
 */
public interface LogMessageModifier {

	/**
	 * Hook method invoked by the ThreadLocalAwareLogger
	 * before a log message is being sent to the log appenders.
	 * @see ThreadLocalAwareLogger#modifyLogMessage
	 * @param logMessage the original log message
	 * @return the modified log message
	 */
	public Object modifyLogMessage(Object logMessage);
	
}
