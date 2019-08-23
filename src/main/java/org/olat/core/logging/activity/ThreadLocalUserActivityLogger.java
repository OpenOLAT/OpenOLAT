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

import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Wrapper around a ThreadLocal holding a IUserActivityLogger.
 * <p>
 * The ThreadLocalUserActivityLogger can be called any time during
 * event handling and in dispose in order to do 
 * User Activity Logging - i.e. to call the log() method.
 * <p>
 * The idea of the ThreadLocalUserActivityLogger is to avoid
 * having to pass all sorts of resource objects between Controller
 * and Manager methods for the sole purpose of having them available
 * at log time.
 * <p>
 * In the current design each Controller has a IUserActivityLogger
 * which is set up with the ThreadLocalUserActivityLogger's content
 * at Controller construction time (done in DefaultController<init>).
 * <p>
 * Each Controller is then suggested to add LoggingResourcables 
 * in the constructor - i.e. to add those resourceables which it knows
 * at construction time and which will be used later in its event()
 * methods to do logging.
 * <p>
 * With this simplification of having data (i.e. LoggingResourceables)
 * collected by a ThreadLocal one might easily loose oversight over
 * what exactly is set at what time where. <br>
 * To help work around this the ILoggingAction/ResourceableTypeList
 * concept was introduced: it is a runtime safety check comparing
 * all the LoggingResourceables available in an IUserActivityLogger
 * versus what is in the businesPath and what the programmer knows
 * at implementation time as to which LoggingResourceables are mandatory 
 * or optional.
 * <p>
 * Note that there is a Helper class called ThreadLocalUserActivityLoggerInstaller
 * which is a peer class working together with ThreadLocalUserActivityLogger
 * but was separated for logical reasons:
 * <ul>
 *   <li>The ThreadLocalUserActivityLogger mainly has two public functions:
 *       the addLoggingResourceInfo and the log method: the OLAT developer
 *       should mainly get in contact with these two method</li>
 *   <li>The ThreadLocalUserActivityLoggerInstaller's job though is to
 *       set up/tear down IUserActivityLogger's at event handling time.</li>
 * </ul>
 * Initial Date:  21.10.2009 <br>
 * @author Stefan
 * @see ILoggingAction
 * @see org.olat.util.logging.activity.LoggingResourceable
 * @see IUserActivityLogger
 * @see ResourceableTypeList
 */
public class ThreadLocalUserActivityLogger {

	private static final Logger log_ = Tracing.createLoggerFor(ThreadLocalUserActivityLogger.class);

	/** THE ThreadLocal IUserActivityLogger - initialized by ThreadLocalUserActivityLoggerInstaller **/
	static final  ThreadLocal<IUserActivityLogger> userActivityLogger_ = new ThreadLocal<>();

	/** package protected getter for the ThreadLocal userActivityLogger - assumes initialized and complains if not ! **/
	static IUserActivityLogger getUserActivityLogger() {
		IUserActivityLogger logger = userActivityLogger_.get();
		if (logger==null) {
			if(log_.isDebugEnabled()) {//only generate this exception in debug
				log_.warn("No UserActivityLogger set! Reinitializing now.", new Exception("stacktrace"));
			}
			return new UserActivityLoggerImpl();
		}
		return logger;
	}
	
	public static Identity getLoggedIdentity() {
		IUserActivityLogger logger = userActivityLogger_.get();
		if (logger!=null) {
			return logger.getLoggedIdentity();
		}
		return null;
	}

	/**
	 * Adds the given LoggingResourceInfo to the ThreadLocalUserActivityLogger.
	 * <p>
	 * Only use this method before Controller constructors - if you are 
	 * inside a Controller constructor and want to add a LoggingResourceable
	 * to the Controller's IUserActivityLogger call DefaultController.addLoggingResourceInfo instead!
	 * <p>
	 * For temporarily adding/removing LoggingResourceInfos use one of the log() methods directly instead
	 * <p>
	 * @param resourceInfo
	 */
	public static void addLoggingResourceInfo(ILoggingResourceable resourceInfo) {
		IUserActivityLogger logger = userActivityLogger_.get();
		if (logger!=null) {
			logger.addLoggingResourceInfo(resourceInfo);
		}
	}
	
	/**
	 * Issues a log entry to the logging database based on the given loggingAction and
	 * the loggingResourceables.
	 * <p>
	 * The loggingAction defines - besides the actual logMessage - which loggingResourceables
	 * are expected and allowed to be logged alongside the logMessage. 
	 * If this check fails, a log.warn() is issued. This should then be fixed by either
	 * adding the required loggingResourceable to the UserActivityLogger in the Controller's
	 * constructor (preferred way) - or by passing it in this log call (less preferred way).
	 * <p>
	 * @param loggingAction the logging action which should be logged
	 * @param callingClass the class which calls this log method - stored to the database
	 * @param loggingResourceables optional and the less preferred way of passing
	 * loggingResourceables to be stored alongside this log message to the database.
	 * the preferred way though is to have it all added to the UserActivityLogger earlier - 
	 * that is, usually and typically in the Controller' constructor.
	 * Preferred way of using loggingResourceables is via comma separated 'list'
	 */
	public static void log(ILoggingAction loggingAction, Class<?> callingClass, ILoggingResourceable... loggingResourceables) {
		IUserActivityLogger logger = userActivityLogger_.get();
		if (logger==null) {
			/*
			 * TODO sev26
			 * An error must abort the execution. Therefore change it to a
			 * warning log message.
			 */
			log_.info("No ThreadLocal IUserActivityLogger set - cannot log to database: "+loggingAction.getActionVerb());
		} else {
			logger.log(loggingAction, callingClass, loggingResourceables);
		}
	}

	/**
	 * Sets the given ActionType 'sticky' to this Thread's ThreadLocal IUserActivityLogger -
	 * i.e. when you set the sticky ActionType any ActionType passed along to
	 * the log() method in the ILoggingAction is overwritten.
	 * @param actionType the sticky ActionType which should overwrite
	 * whatever comes in the ILoggingAction in log()
	 */
	public static void setStickyActionType(ActionType actionType) {
		IUserActivityLogger logger = userActivityLogger_.get();
		if (logger!=null) {
			logger.setStickyActionType(actionType);
		}
	}
	
	/**
	 * Gets 'sticky' ActionType of this Thread's ThreadLocal IUserActivityLogger - or null
	 * if none is set
	 */
	public static ActionType getStickyActionType() {
		IUserActivityLogger logger = userActivityLogger_.get();
		if (logger!=null) {
			return logger.getStickyActionType();
		} else {
			return null;
		}
	}

	/**
	 * Sets the businessPath on the ThreadLocal' UserActivityLogger.
	 * <p>
	 * Internal framework use only
	 */
	static void setBusinessPath(String businessPath, UserActivityLoggerImpl current) {
		IUserActivityLogger logger = userActivityLogger_.get();
		if (logger==current) {
			// stop
			return;
		}
		if (logger instanceof UserActivityLoggerImpl) {
			UserActivityLoggerImpl genLogger= (UserActivityLoggerImpl)logger;
			genLogger.frameworkSetBusinessPath(businessPath);
		}
	}

	/**
	 * Sets the context entries on the ThreadLocal' UserActivityLogger.
	 * <p>
	 * Internal framework use only
	 */
	static void setBCContextEntries(List<ContextEntry> bcContextEntries, UserActivityLoggerImpl current) {
		IUserActivityLogger logger = userActivityLogger_.get();
		if (logger==current) {
			// stop
			return;
		}
		if (logger instanceof UserActivityLoggerImpl) {
			UserActivityLoggerImpl genLogger= (UserActivityLoggerImpl)logger;
			genLogger.frameworkSetBCContextEntries(bcContextEntries);
		}
	}
}
