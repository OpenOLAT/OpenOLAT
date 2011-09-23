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
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.logging;

/**
 * Description:<br>
 * Log Impl class, for method details see Tracing.java in the same package
 * 
 * <P>
 * Initial Date:  01.11.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class OLogImpl implements OLog {
	Class loggingClazz;
	OLogImpl(Class clazz) {
		this.loggingClazz = clazz;
	}
	
	public boolean isDebug() {
		return Tracing.isDebugEnabled(loggingClazz);
	}
	
	public void error(String logMsg, Throwable cause) {
		Tracing.logError(logMsg, cause, loggingClazz);
	}

	public void error(String logMsg) {
		Tracing.logError(logMsg, loggingClazz);
	}

	/**
	 * See package.html for proper usage!
	 */
	public void warn(String logMsg, Throwable cause) {
		Tracing.logWarn(logMsg, cause, loggingClazz);
	}

	/**
 	 * See package.html for proper usage!
	 */
	public void warn(String logMsg) {
		Tracing.logWarn(logMsg, loggingClazz);
	}

	/**
	 * Add debug log entry. Always use together with
	 * if (log.isDebug()) log.debug(...) to let the compiler
	 * optimize it for a performance gain
	 * 
	 */
	public void debug(String logMsg, String userObj) {
		Tracing.logDebug(logMsg, userObj, loggingClazz);
	}

	/**
	 * Add debug log entry
	 */
	public void debug(String logMsg) {
		Tracing.logDebug(logMsg, loggingClazz);
	}

	public void info(String logMsg, String userObject) {
		Tracing.logInfo(logMsg, userObject, loggingClazz);
	}

	public void info(String logMsg) {
		Tracing.logInfo(logMsg, loggingClazz);	
	}

	/**
	 * Add audit log entry.
	 */
	public void audit(String logMsg) {
		Tracing.logAudit(logMsg, loggingClazz);
	}

	/**
	 * Add audit log entry with a user object. 
	 */
	public void audit(String logMsg, String userObj) {
		Tracing.logAudit(logMsg, userObj, loggingClazz);
	}

}
