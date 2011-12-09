/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */ 

package org.olat.core.logging;

/**
 * <h3>Description:</h3>
 * Whenever you have a class that needs logging you can extend from this one to
 * add logging features to the class. The class uses the delegate pattern to
 * forward all logging requests to an OLog object.
 * <p>
 * Initial Date: 28.08.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class LogDelegator {
	// Note that the createLoggerFor method creates a shared logger object. For
	// your convenience the logger object is created here for your class. It is
	// implemented as instance variable because otherwhise it is not possible to
	// instantiate the logger for your class. But all objects of your class will
	// actually share the same logger object.
	private OLog logger = Tracing.createLoggerFor(this.getClass());
	/**
	 * Returns a logger for this class to do advanced logging <br>
	 * <i>Hint</i><br>
	 * You can also use the logDebug etc methods
	 * 
	 * @return logger
	 */
	protected OLog getLogger() {
		return this.logger;
	}

	/**
	 * Log an error message to the system log file. An error is when something
	 * unexpected happened that could not be resolved and as a result the
	 * current workflow has to be terminated.
	 * <p>
	 * Alternatively it is possible to throw an OLATRuntimeException if your
	 * code does not present a special alternative GUI path for the user.
	 * <p>
	 * If you can, log the error and present a useful screen to the user.
	 * 
	 * @param logMsg
	 *            Human readable log message
	 * @param cause
	 *            stack trace or NULL
	 */
	public void logError(String logMsg, Throwable cause) {
		getLogger().error(logMsg, cause);
	}

	/**
	 * Log a warn message to the system log file. A warn message should be used
	 * when something unexpected happened but the system somehow can deal with
	 * it. e.g by using default values.
	 * 
	 * @param logMsg
	 *            Human readable log message
	 * @param cause
	 *            stack trace or NULL
	 */
	public void logWarn(String logMsg, Throwable cause) {
		getLogger().warn(logMsg, cause);
	}

	/**
	 * Log a debug message to the system log file. A debug message is something
	 * that can be useful to get more relevant information in a deployed system.
	 * Thus, think very carefully about which information could be useful to
	 * debug a live system. Be very verbose.
	 * <p>
	 * Debug messages are not meant for debugging while development phase, use
	 * your debugger for this purpose. The only purpose of logDebug is to debug
	 * a live system.
	 * <p>
	 * To prevent many expensive string concatenation in a live system where the
	 * log level for this class is not set to debug you must use logDebug always
	 * in conjunction with isLogDebug(): <code>
	 * if (isLogDebugEnabled()) {
	 *    logDebug("my relevant debugging info", myUserObject);
	 * }
	 * </code>
	 * 
	 * @param logMsg
	 *            Human readable log message
	 * @param userObj
	 *            A user object with additional information or NULL
	 */
	public void logDebug(String logMsg, String userObj) {
		getLogger().debug(logMsg, userObj);
	}
	
	/**
	 * @param logMsg
	 */
	public void logDebug(String logMsg) {
		getLogger().debug(logMsg, null);
	}

	 /**
	 * See logDebug() method
	 * 
	 * @return true: debug level enabled; false: debug level not enabled
	 */
	public boolean isLogDebugEnabled() {
		return getLogger().isDebug();
	}

	/**
	 * Log an info message to the system log file. Info messages are useful to
	 * log configuration settings or very important events to the logfile. Log
	 * only information that is really important to have in the logfile.
	 * <p>
	 * If the information is only useful to debug a problem, you might consider
	 * using logDebug instead
	 * 
	 * @param logMsg
	 *            Human readable log message
	 * @param userObj
	 *            A user object with additional information or NULL
	 */
	public void logInfo(String logMsg, String userObject) {
		getLogger().info(logMsg, userObject);
	}
	
	/**
	 * 
	 * @param logMsg
	 */
	public void logInfo(String logMsg) {
		getLogger().info(logMsg, null);
	}

	/**
	 * Log an audit message to the system log file. An audit message contains
	 * information about a user behavior: a user logged into the system, a user
	 * did a critical action.
	 * 
	 * @param logMsg
	 *            Human readable log message
	 * @param userObj
	 *            A user object with additional information or NULL
	 */
	public void logAudit(String logMsg, String userObj) {
		getLogger().audit(logMsg, userObj);
	}
	
	/**
	 * 
	 * @param logMsg
	 */
	public void logAudit(String logMsg) {
		getLogger().audit(logMsg, null);
	}
}
