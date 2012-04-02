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
* <p>
*/ 

package org.olat.core.logging.activity;

import java.util.List;

import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.UserSession;

/**
 * Interface for doing user activity logging.
 * <p>
 * There are two types of logging in OLAT:
 * <ul>
 *  <li>User Activity Logging: traces actual actions of users, writing them
 *      into a database for later statistics and reporting</li>
 *  <li>Technical logging: this is for debugging OLAT code and is 
 *      stored in log files going via log4j</li>
 * </ul>
 * 
 * This class is used for User Activity Logging only.
 * <p>
 * Note that the logged information is composed from the following:
 * <ul>
 *  <li>the loggingAction which contains static information such as
 *      the crudAction, the logMessage and resourceAdminAction.</li>
 *  <li>the parameters which are passed to the log message - which
 *      are callingClass and a list of resourceables of which 
 *      four are stored to the database (the outermost and the
 *      three innermost ones)</li>
 *  <li>furthermore some of the logged properties are held with
 *      the ThreadLocalUserActivityLogger - these include
 *      the session (and derived from it the user) and the 
 *      businessPath</li>
 * </ul>
 * <p>
 * Initial Date: Feb 3, 2005
 * @author gnaegi, Stefan
 */
public interface IUserActivityLogger {
	
	/**
	 * Return the identity which is logged
	 * @return
	 */
	public Identity getLoggedIdentity();

	/**
	 * Stores a new log entry with the available information to the logging table.
	 * <p>
	 * Note that the logged information is composed from the following:
	 * <ul>
	 *  <li>the loggingAction which contains static information such as
	 *      the crudAction, the logMessage and resourceAdminAction.</li>
	 *  <li>the parameters which are passed to the log message - which
	 *      are callingClass and a list of resourceables of which 
	 *      four are stored to the database (the outermost and the
	 *      three innermost ones)</li>
	 *  <li>furthermore some of the logged properties are held with
	 *      the ThreadLocalUserActivityLogger - these include
	 *      the session (and derived from it the user) and the 
	 *      businessPath</li>
	 * </ul>
	 * <p>
	 * @see LoggingObject for details on the database table
	 * @param loggingAction the logging action which contains the log message to log
	 * @param callingClass the class which calls this log method - stored to the databae
	 * @param loggingResourceInfosOrNull zero or many LoggingResourceable objects - they 
	 * will be stored to the database (four of them - the outermost and the three innermost ones)
	 */
	public void log(ILoggingAction loggingAction, Class<?> callingClass, ILoggingResourceable... loggingResourceInfosOrNull);

	/**
	 * Adds the given LoggingResourceable - which can be thought of as a simple
	 * wrapper around say an ICourse, a Node or in other cases just a String -
	 * to this IUserActivityLogger permanently.
	 * <p>
	 * This should be used by Controller constructors while setting up the 
	 * IUserActivityLogger.
	 * <p>
	 * Anything set on the Controller's IUserActivityLogger will later be available
	 * for logging during event/doDispose calls.
	 * <p>
	 * @param resourceInfo the LoggingResourceable which should be added
	 * to this IUserActivityLogger
	 */
	public void addLoggingResourceInfo(ILoggingResourceable resourceInfo);

	/**
	 * Gets 'sticky' ActionType of this IUserActivityLogger - or null
	 * if none is set
	 */
	public ActionType getStickyActionType();
	
	/**
	 * Sets the given ActionType 'sticky' to this IUserActivityLogger -
	 * i.e. when you set the sticky ActionType any ActionType passed along to
	 * the log() method in the ILoggingAction is overwritten.
	 * @param actionType the sticky ActionType which should overwrite
	 * whatever comes in the ILoggingAction in log()
	 */
	public void setStickyActionType(ActionType actionType);
	
	/**
	 * INTERNAL FRAMEWORK METHOD!
	 * <p>
	 * Sets the session on this IUserActivityLogger directly.
	 * <p>
	 * Note that there are two ways the session is set on an IUserActivityLogger:
	 * <ul>
	 *  <li>via this method</li>
	 *  <li>via ThreadLocalUserActivityLogger.initUserAcitvityLogger(HttpServletRequest)
	 *      which is called as early as in the OLATServlet.doPost()</li>
	 * </ul>
	 * @param session the session which should be set on this IUserActivityLogger
	 */
	public void frameworkSetSession(UserSession session);

	/**
	 * INTERNAL FRAMEWORK METHOD!
	 * <p>
	 * Sets the businesspath - as String - on this IUserActivityLogger.
	 * <p>
	 * This method is called in a few carefully selected places only.
	 * <p>
	 * You should usually not call this otherwise - if you have to
	 * consider checking with the other places to verify why you have to set it
	 * <p>
	 * @param businessPath
	 */
	public void frameworkSetBusinessPath(String businessPath);
	
	/**
	 * INTERNAL FRAMEWORK METHOD!
	 * <p>
	 * Sets the businesspath - by retrieving it from the WindowControl - on this IUserActivityLogger.
	 * <p>
	 * This method is called in a few carefully selected places only.
	 * <p>
	 * You should usually not call this otherwise - if you have to
	 * consider checking with the other places to verify why you have to set it
	 * <p>
	 * @param wControl
	 */
	public void frameworkSetBusinessPathFromWindowControl(WindowControl wControl);
	
	/**
	 * INTERNAL FRAMEWORK METHOD!
	 * <p>
	 * Sets the business context entries on this IUserActivityLogger.
	 * <p>
	 * This method is called in a few carefully selected places only.
	 * <p>
	 * PS: The context entries are used to make safety checks to ensure
	 * the business path matches the ResourceableInfos.
	 * It is likely that this will become redundant (i.e. overkill) at 
	 * some point and that we'll get rid of this
	 * <p>
	 * @param wControl
	 */
	public void frameworkSetBCContextEntries(List<ContextEntry> bcEntries);

	

}