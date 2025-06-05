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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StackedBusinessControl;
import org.olat.core.logging.activity.manager.ActivityLogServiceImpl;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;

/**
 * Default implementation of the IUserActivityLogger which logs into
 * the o_loggingtable.
 * <p>
 * This class would usually not be subclassed or changed as it closely
 * works together with the ThreadLocalUserActivityLogger.
 * <p>
 * Also, this class should not have to be accessed directly or cast to in any way.
 * <p>
 * There is an instance of UserActivityLoggerImpl for every 
 * Controller and for every ThreadLocalUserActivityLogger during
 * its run methods.
 * <p>
 * The UserActivityLoggerImpl gathers all information during the course
 * of time to be ready for the log() call later - i.e. to have all 
 * mandatory information at that time. This includes such things as
 * session/identity, the businessPath and the list of LoggingResourceables.
 * <p>
 * Also, this class implements the simpleDuration calculation - which is
 * a simple time difference between log calls within a session. Note that
 * this is really simple, as it does not distinguish between having
 * multiple log calls per click and having no log calls for a number of
 * clicks - hence the simpleDuration can only be useful to some extend.
 * The plan is to introduce a semanticDuration or some other concept later.
 * Anyway, for the simpleDuration the UserActivityLoggerImpl stores
 * the last LoggingObject (the actual hibernate object) into the session
 * for comparison later.
 * <p>
 * 
 * <P>
 * Initial Date:  21.10.2009 <br>
 * @author Stefan
 */
public class UserActivityLoggerImpl implements IUserActivityLogger {

	/** the key with which the last LoggingObject is stored in the session - used for simpleDuration calculation only **/
	public static final String USESS_KEY_USER_ACTIVITY_LOGGING_LAST_LOG = "USER_ACTIVITY_LOGGING_LAST_LOG";

	/** the session -  which this UserActivityLoggerImpl should later log into the database **/
	private UserSession session_;
	
	/** if not null this stickyActionType_ overwrites whatever comes as the ActionType in the ILoggingAction in log() **/
	private ActionType stickyActionType_ = null;
	
	/** the identity - which this UserActivityLoggerImpl should later log into the database **/
	private Identity identity_;

	/** the list of LoggingResourceable objects which have been added to this UserActivityLoggerImpl yet -
	 * all used later for logging into the database
	 */
	private List<ILoggingResourceable> resourceableList_ = null;
	
	/** the current businessPath - this is used for making validity checks against.
	 * <p>
	 * The idea is to eventually achieve a complete match between LoggingResourceable and businessPath.
	 * While we are currently miles away from this, when it eventually matches we could get
	 * rid of one or the other and only use for example the businessPath, i.e. the 
	 * underlying contextEntries (which contain the actual OlatResourceable objects).
	 * The problem though always seems to remain that not all information which we would like
	 * to log is actually a OlatResourceable - sometimes it's as simple as a String (i.e. number
	 * of qti attemts etc) which we'll never convert to an OlatResourceable. Anyway, once
	 * we'd have a 100% correct businessPath match with the loggingResourceables (at least,
	 * lets say the OlatResourceables thereof) we could simplify things here a lot
	 */
	private String businessPath_;
	
	/** @see #businessPath_ **/
	private List<ContextEntry> bcContextEntries_;

	/** whether or not this UserActivityLoggerImpl should propagate setters to businessPath
	 * and bcContextEntries_ to the runtimeParent.
	 * <p>
	 * This is needed for the following reason: there are two kinds of usages for the
	 * ThreadLocalUserActivityLogger:
	 * <ul>
	 *  <li>It can happen that during an event() call a LoggingResourceable is set
	 *      on the ThreadLocalUserActivityLogger which is needed lateron in the rendering -
	 *      or in other parts which are outside the original event() method. I.e. there
	 *      should be a propagation mechanism where LoggingResourceables propagate
	 *      to the current parent-chaing of ThreadLocalUserActivityLoggers</li>
	 *  <li>During the simplevm eventbus handling though, we want to make sure that
	 *      nothing propagates back to the caller - since we're simulating basically
	 *      a new thread calling an event handler - but of course that event handler
	 *      could make changes to the ThreadLocalUserActivityLogger - for example
	 *      by adding a LoggingResourceable - this information we do not want to
	 *      propagate</li>
	 * </ul> 
	 * Hence this field controls whether or not information propagates to runtime
	 * parent ThreadLocalUserActivityLoggers or not
	 */
	private final boolean propageToThreadLocal_;
	
	/**
	 * The runtime parent UserActivityLoggerImpl.
	 * <p>
	 * This is always set except for initial creation in the OLATServlet.
	 * <p>
	 * The runtime parent is used to propagate LoggingResourceables and businessPath/contextEntries.
	 * The reason for this structure is to be able to take a Controller's 
	 * IUserActivityLogger during the event call, set it as ThreadLocalUserActivityLogger
	 * and still have the parent (i.e. the IUserActivityLogger which was set
	 * by the time the event method was called) also be informed about changes.
	 * This is because there are situations where we do logging after the event method
	 * (i.e. during rendering) and need to have the LoggingResourceables as well
	 * as the businessPath/contextEntries.
	 * <p>
	 * This is very deep framework.
	 */
	private UserActivityLoggerImpl runtimeParent_;
	
	private final boolean backgroundJob;

	/**
	 * package protected constructor of the UserActivityLoggerImpl based on a
	 * HttpServletRequest - out of which the session and identity are extracted.
	 * <p>
	 * This is used to set up a UserActivityLoggerImpl initially by OLATServlet.doPost
	 * and OLATServlet.init()
	 * <p>
	 * @param hReq the HttpServletRequest out of which the session and identity are extracted
	 */
	UserActivityLoggerImpl(HttpServletRequest hReq) {
		propageToThreadLocal_ = false;
		runtimeParent_ = null;
		backgroundJob = false;
		initWithRequest(hReq);
	}
	
	UserActivityLoggerImpl(UserSession session) {
		propageToThreadLocal_ = false;
		runtimeParent_ = null;
		backgroundJob = false;
		session_ = session;
		if (session_!=null) {
			identity_ = session_.getIdentity();
		}
	}

	/**
	 * Internal initialization method for the OLATServlet.doPost case
	 * @param hReq the HttpServletRequest out of which the session and identity are extracted
	 */
	private void initWithRequest(HttpServletRequest hReq) {
		if (hReq==null) {
			throw new IllegalArgumentException("hReq must not be null");
		}

		session_ = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionIfAlreadySet(hReq);
		if (session_!=null) {
			identity_ = session_.getIdentity();
		}
	}
	
	/**
	 * package protected constructor of the UserActivityLoggerImpl 
	 * <p>
	 * This is used to set up a UserActivityLoggerImpl initially by OLATServlet.init()
	 * 
	 * @param backgroundJob True if the logger need to work without identity, will set
	 *    a fake identity key 0l
	 */
	UserActivityLoggerImpl(boolean backgroundJob) {
		this.backgroundJob = backgroundJob;
		propageToThreadLocal_ = false;
		runtimeParent_ = null;
	}

	private UserActivityLoggerImpl(IUserActivityLogger userActivityLogger, WindowControl wControl, boolean propageToThreadLocal) {
		if (userActivityLogger==null) {
			throw new IllegalArgumentException("userActivityLogger must not be null");
		}
		if (!(userActivityLogger instanceof UserActivityLoggerImpl)) {
			// currently we require the userActivityLogger to be UserActivityLoggerImpl
			// the reason for this is that we do direct field accesses - which would otherwise
			// have to be added to the IUserActivityLogger interface (making it less clean)
			// or by having a IUserActivityLogger.getFieldAccessor() of some sort which
			// would then achieve the same thing.
			// For the current purpose of User Activity Logging run, there is no
			// need to support other implementator yet - it is not nice though, agreed.
			throw new IllegalArgumentException("userActivityLogger other than UserActivityLoggerImpl not yet supported: "+userActivityLogger.getClass());
		}
		final UserActivityLoggerImpl bluePrint = (UserActivityLoggerImpl)userActivityLogger;

		// initialize fields
		propageToThreadLocal_ = propageToThreadLocal;
		this.backgroundJob = false;

		// copy field values from the blueprint
		session_ = bluePrint.session_;
		identity_ = bluePrint.identity_;
		stickyActionType_ = bluePrint.stickyActionType_;
		resourceableList_ = bluePrint.resourceableList_==null ? null : new LinkedList<>(bluePrint.resourceableList_);
		
		// get the businessPath from the windowControl if possible
		String businessPath = null;
		if (wControl!=null && wControl.getBusinessControl()!=null) {
			if (wControl.getBusinessControl() instanceof StackedBusinessControl) {
				StackedBusinessControl sbc = (StackedBusinessControl)wControl.getBusinessControl();
				final List<ContextEntry> ces = sbc.getContextEntryStack();
				bcContextEntries_ = ces!=null ? new LinkedList<>(ces) : null;
			}
			
			businessPath = wControl.getBusinessControl().getAsString();
		}
		if (businessPath!=null && businessPath.length()!=0) {
			businessPath_ = businessPath;
			if (propageToThreadLocal) {
				ThreadLocalUserActivityLogger.setBusinessPath(businessPath, this);
				ThreadLocalUserActivityLogger.setBCContextEntries(bcContextEntries_, this);
			}
		} else {
			businessPath_ = bluePrint.businessPath_;
			bcContextEntries_ = bluePrint.bcContextEntries_;
		}
	}

	/**
	 * This method is used by the DefaultController to setup a new logger based on what's currently
	 * set in the ThreadLocalUserActivityLogger.
	 * @param wControl the WindowControl from which the businessPath and contextEntries are extracted
	 * @return a new UserActivityLoggerImpl purpose built for the DefaultController - never returns null
	 */
	public static UserActivityLoggerImpl setupLoggerForController(WindowControl wControl) {
		final IUserActivityLogger threadLocalUserActivityLogger = ThreadLocalUserActivityLogger.getUserActivityLogger();
		if (!(threadLocalUserActivityLogger instanceof UserActivityLoggerImpl)) {
			throw new IllegalStateException("threadLocalUserActivityLogger must be of type GenericUserActivityLogger");
		}
		final UserActivityLoggerImpl genUserActivityLogger = (UserActivityLoggerImpl)threadLocalUserActivityLogger;
		final UserActivityLoggerImpl newLogger = new UserActivityLoggerImpl(genUserActivityLogger, wControl, true);
		newLogger.runtimeParent_ = genUserActivityLogger;
		return newLogger;
	}
	
	/**
	 * This method is used by ThreadLocalUserActivityLogger for runtime setup during event handling
	 * @param userActivityLogger the "blue print" UserActivityLogger which should be copied for runtime use
	 * @return a new UserActivityLoggerImpl purpose built for runtime use
	 */
	static UserActivityLoggerImpl copyLoggerForRuntime(IUserActivityLogger userActivityLogger) {
		if (userActivityLogger==null) {
			throw new IllegalArgumentException("userActivityLogger must not be null");
		}
		final IUserActivityLogger threadLocalUserActivityLogger = ThreadLocalUserActivityLogger.getUserActivityLogger();
		if (!(threadLocalUserActivityLogger instanceof UserActivityLoggerImpl)) {
			throw new IllegalStateException("threadLocalUserActivityLogger must be of type GenericUserActivityLogger");
		}
		final UserActivityLoggerImpl genUserActivityLogger = (UserActivityLoggerImpl)threadLocalUserActivityLogger;
		final UserActivityLoggerImpl newLogger = new UserActivityLoggerImpl(userActivityLogger, null, true);
		newLogger.runtimeParent_ = genUserActivityLogger;
		return newLogger;
	}
	 
	/**
	 * Webcontainer thread for expired sessions triggers value unbound which in turn disposes the whole gui session.
	 * This UserTrackingLogger must be specially rooted.
	 * @return
	 */
	public static IUserActivityLogger newLoggerForValueUnbound(UserSession session){
		//
		// propagate is set to true -  ValueUnbound triggers dispose
		// after dispose is done, UserSession must clean up
		//
		IUserActivityLogger ual = ThreadLocalUserActivityLogger.getUserActivityLogger();
		
		
		UserActivityLoggerImpl newLogger = new UserActivityLoggerImpl(ual, null, true);
		// Note: can't use frameworkSetSession here as that one might complain about
		//       no threadlocal useractivitylogger being set - but actually, what we
		//       want to do here is to just initialize the newLogger without any side-effects
		newLogger.session_ = session;
		return newLogger;
	}
	
	
	/**
	 * The special thing about this 'logger for EventBus' is, that it doesn't propagate resourceInfos/businessPath etc
	 * to the parent nor the ThreadLocalUserActivityLogger.
	 * <p>
	 * @param controller the Controller from which the IUserActivityLogger and the WindowControl (businessPath) should
	 * be extracted
	 * @return a new UserActivityLoggerImpl purpose built for use by the EventBus during fireEvent
	 */
	public static UserActivityLoggerImpl newLoggerForEventBus(Controller controller) {
		IUserActivityLogger logger = controller.getUserActivityLogger();
		WindowControl wControl = controller.getWindowControlForDebug();
		UserActivityLoggerImpl newLogger = new UserActivityLoggerImpl(logger, wControl, false);
		newLogger.frameworkSetBusinessPathFromWindowControl(controller.getWindowControlForDebug());
		return newLogger;
		// don't set runtimeParent !
	}
	
	@Override
	public Identity getLoggedIdentity() {
		if(identity_ == null && session_ != null) {
			return session_.getIdentity();
		}
		return identity_;
	}

	@Override
	public void frameworkSetSession(UserSession session) {
		if (session_==session) {
			return;
		}
		session_ = session;
		if (runtimeParent_!=null) {
			runtimeParent_.frameworkSetSession(session);
		}
		IUserActivityLogger threadLocalUserActivityLogger = ThreadLocalUserActivityLogger.getUserActivityLogger();
		if (propageToThreadLocal_ && runtimeParent_!=threadLocalUserActivityLogger) {
			threadLocalUserActivityLogger.frameworkSetSession(session);
		}
	}
	
	/**
	 * Internal getter for the resourceableList - initializes the list if it's null
	 * @return the resourcableList - initializes the list if it's null
	 */
	private List<ILoggingResourceable> getLoggingResourceableList() {
		if (resourceableList_==null) {
			resourceableList_ = new LinkedList<>();
		}
		return resourceableList_;
	}
	
	@Override
	public void addLoggingResourceInfo(ILoggingResourceable loggingResourceable) {
		if (loggingResourceable==null) {
			throw new IllegalArgumentException("resourceInfo must not be null");
		}

		List<ILoggingResourceable> loggingResourceableList = getLoggingResourceableList();
		int existingPos = loggingResourceableList.indexOf(loggingResourceable);
		if (existingPos!=-1) {
			ILoggingResourceable existingRI = loggingResourceableList.get(existingPos);
			if (existingRI.getName()!=null && loggingResourceable.getName()!=null &&
					existingRI.getName().equals(loggingResourceable.getName())) {
				// ignore - already set
				return;
			} else if (existingRI.getName()==null && loggingResourceable.getName()==null) {
				// both names are null and we otherwiese assume that they are equal
				// so ignore them
				return;
			}
			// otherwise we have a matching resourceInfo already registered (same type,id) but with a different name
			// let's update it
			loggingResourceableList.remove(existingPos);
		}
		
		if(OlatResourceableType.node.equals(loggingResourceable.getResourceableType())) {
			//remove other node resource
			for(Iterator<ILoggingResourceable> logIt=loggingResourceableList.iterator(); logIt.hasNext(); ) {
				if(OlatResourceableType.node.equals(logIt.next().getResourceableType())) {
					logIt.remove();
				}
			}
		}
		
		loggingResourceableList.add(loggingResourceable);

		if (runtimeParent_!=null) {
			runtimeParent_.addLoggingResourceInfo(loggingResourceable);
		}
		IUserActivityLogger threadLocalUserActivityLogger = ThreadLocalUserActivityLogger.getUserActivityLogger();
		if (propageToThreadLocal_ && runtimeParent_!=threadLocalUserActivityLogger) {
			threadLocalUserActivityLogger.addLoggingResourceInfo(loggingResourceable);
		}
	}

	@Override
	public void frameworkSetBusinessPath(String businessPath) {
		if (businessPath==businessPath_ || businessPath==null || businessPath.length()==0 || (businessPath_!=null && businessPath.length()<businessPath_.length())) {
			return;
		}
		
		this.businessPath_ = businessPath;

		if (runtimeParent_!=null) {
			runtimeParent_.frameworkSetBusinessPath(businessPath);
		}
		IUserActivityLogger threadLocalUserActivityLogger = ThreadLocalUserActivityLogger.getUserActivityLogger();
		if (propageToThreadLocal_ && runtimeParent_!=threadLocalUserActivityLogger) {
			threadLocalUserActivityLogger.frameworkSetBusinessPath(businessPath);
		}
	}
	
	@Override
	public void frameworkSetBCContextEntries(List<ContextEntry> bcEntries) {
		if (bcContextEntries_==bcEntries || bcEntries==null || bcEntries.isEmpty() || (bcContextEntries_!=null && bcEntries.size()<bcContextEntries_.size())) {
			return;
		}
		
		this.bcContextEntries_ = bcEntries;

		if (runtimeParent_!=null) {
			runtimeParent_.frameworkSetBCContextEntries(bcEntries);
		}
		IUserActivityLogger threadLocalUserActivityLogger = ThreadLocalUserActivityLogger.getUserActivityLogger();
		if (propageToThreadLocal_ && runtimeParent_!=threadLocalUserActivityLogger) {
			threadLocalUserActivityLogger.frameworkSetBCContextEntries(bcEntries);
		}
	}

	@Override
	public void frameworkSetBusinessPathFromWindowControl(WindowControl wControl) {
		if (wControl!=null && wControl.getBusinessControl()!=null) {
			if (wControl.getBusinessControl() instanceof StackedBusinessControl) {
				StackedBusinessControl sbc = (StackedBusinessControl)wControl.getBusinessControl();
				final List<ContextEntry> ces = sbc.getContextEntryStack();
				if (ces!=null) {
					frameworkSetBCContextEntries(new LinkedList<>(ces));
				}
			}
			
			final String bp = wControl.getBusinessControl().getAsString();
			frameworkSetBusinessPath(bp);
		}
	}
	
	@Override
	public void setStickyActionType(ActionType actionType) {
		stickyActionType_ = actionType;
	}
	
	@Override
	public ActionType getStickyActionType() {
		return stickyActionType_;
	}

	@Override
	public void log(ILoggingAction loggingAction, Class<?> callingClass, ILoggingResourceable... lriOrNull) {

		final ActionType actionType = stickyActionType_!=null ? stickyActionType_ : loggingAction.getResourceActionType();
		
		// Move up here to remove targetIdentity before checking the LoggingResourcables, because of often obsolete delivery of targetIdentity. 
		// TargetIdentity is often missing in XYLoggingAction.
		final String sessionId;
		if(session_ == null || session_.getSessionInfo() == null
				|| (session_.getSessionInfo() != null && session_.getSessionInfo().getSession() == null)) {
			//background task
			sessionId = Thread.currentThread().getName();
		} else {
			sessionId = Long.toString(session_.getSessionInfo().getCreationTime());
		}

		Long identityKey = null;
		if(session_ != null && session_.getIdentity() != null) {
			identityKey = session_.getIdentity().getKey();
		}
		CoreSpringFactory.getImpl(ActivityLogServiceImpl.class).log(loggingAction, actionType, sessionId, identityKey,
				callingClass, backgroundJob, businessPath_, bcContextEntries_, getLoggingResourceableList(), lriOrNull);
	}
}
