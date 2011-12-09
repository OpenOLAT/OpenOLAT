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
