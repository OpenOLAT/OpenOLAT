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
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StackedBusinessControl;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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

	/** the logging object used in this class **/
	private static final Logger log_ = Tracing.createLoggerFor(UserActivityLoggerImpl.class);
	
	/** the key with which the last LoggingObject is stored in the session - used for simpleDuration calculation only **/
	public static final String USESS_KEY_USER_ACTIVITY_LOGGING_LAST_LOG = "USER_ACTIVITY_LOGGING_LAST_LOG";

	/** the session -  which this UserActivityLoggerImpl should later log into the database **/
	private UserSession session_;
	
	/** if isLogAnonymous equal to true and if resourceAdminAction equal to false then anonymize log entries **/
	private boolean isLogAnonymous_ = LogModule.isLogAnonymous();
	
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
		initWithRequest(hReq);
	}
	
	UserActivityLoggerImpl(UserSession session) {
		propageToThreadLocal_ = false;
		runtimeParent_ = null;
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
	 */
	UserActivityLoggerImpl() {
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
		if (businessPath!=null) {
			this.businessPath_ = businessPath;
		}
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
		if (bcEntries!=null) {
			this.bcContextEntries_ = bcEntries;
		}
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
	
	/**
	 * Returns the combined and orderd list of LoggingResourceables which are set on this
	 * UserActivityLoggerImpl and are matching the contextEntries.
	 * <p>
	 * Note that this method fails if there is a contextEntry which doesn't have a corresponding
	 * LoggingResourceable: This would be a situation where the businessPath/contextEntry has a
	 * resource defined which is unknown to this UserActivityLoggerImpl - i.e. which has not been
	 * set by the Controller or not been passed via the log() call.
	 * <p>
	 * The safety check with the LoggingAction's ResourceableTypeList is not done in this method.
	 * @param lriOrNull an 
	 * @return the combined and ordered list of LoggingResourceables which should go right to the database
	 * in the corresponding fields
	 */
	private List<ILoggingResourceable> getCombinedOrderedLoggingResourceables(ILoggingResourceable... additionalLoggingResourceables) {
		List<ILoggingResourceable> result = new LinkedList<>();
		List<ILoggingResourceable> inputCopy = new LinkedList<>(getLoggingResourceableList());
		if (additionalLoggingResourceables!=null) {
			for (int i = 0; i < additionalLoggingResourceables.length; i++) {
				ILoggingResourceable additionalLoggingResourceable = additionalLoggingResourceables[i];

				int existingPos = inputCopy.indexOf(additionalLoggingResourceable);
				if (existingPos!=-1) {
					ILoggingResourceable existingRI = getLoggingResourceableList().get(existingPos);
					if (existingRI.getName()!=null && additionalLoggingResourceable.getName()!=null &&
							existingRI.getName().equals(additionalLoggingResourceable.getName())) {
						// ignore - already set
						continue;
					} else if (existingRI.getName()==null && additionalLoggingResourceable.getName()==null) {
						// both names are null and we otherwiese assume that they are equal
						// so ignore them
						continue;
					}
					// otherwise we have a matching resourceInfo already registered (same type,id) but with a different name
					// let's update it
					inputCopy.remove(existingPos);
				}
				
				inputCopy.add(additionalLoggingResourceable);
			}
		}
		if (bcContextEntries_!=null) {
			LinkedList<ContextEntry> bcContextEntriesCopy = new LinkedList<>();
			for (Iterator<ContextEntry> it = bcContextEntries_.iterator(); it.hasNext();) {
				ContextEntry ce = it.next();
				if (!bcContextEntriesCopy.contains(ce)) {
					bcContextEntriesCopy.add(ce);
				}
			}
			for (Iterator<ContextEntry> it = bcContextEntriesCopy.iterator(); it.hasNext();) {
				ContextEntry ce = it.next();
				// SR: see below boolean foundIt = false;
				for (Iterator<ILoggingResourceable> it2 = inputCopy.iterator(); it2.hasNext();) {
					ILoggingResourceable resourceInfo = it2.next();
					if (resourceInfo.correspondsTo(ce)) {
						// perfecto
						result.add(resourceInfo);
						it2.remove();
						// SR: see below foundIt = true;
						break;
					}
				}
				/*
				if (!foundIt) {
					String oresourceableOres = "n/a (null)";
					// SR: why generate exception for unuseable information???
					if (log_.isDebug() && ce !=null && ce.getOLATResourceable() !=null) {
							try {
								java.lang.reflect.Method getOlatResource = ce.getOLATResourceable().getClass().getDeclaredMethod("getOlatResource");
								if (getOlatResource!=null) {
									oresourceableOres = String.valueOf(getOlatResource.invoke(ce.getOLATResourceable()));
								}
							} catch (SecurityException e) {
								log_.error("SecurityException while retrieving getOlatResource() Method from "+ce.getOLATResourceable().getClass());
							} catch (NoSuchMethodException e) {
								log_.info("(OK) ContextEntry's OLATResourceable had no further getOlatResource() method: "+ce.getOLATResourceable().getClass());
							} catch (IllegalArgumentException e) {
								log_.error("IllegalArgumentException while calling getOlatResource() Method from "+ce.getOLATResourceable().getClass(), e);
							} catch (IllegalAccessException e) {
								log_.error("IllegalAccessException while calling getOlatResource() Method from "+ce.getOLATResourceable().getClass(), e);
							} catch (InvocationTargetException e) {
								log_.error("IllegalAccessException while calling getOlatResource() Method from "+ce.getOLATResourceable().getClass(), e);
							}
					}
					log_.info("Could not find any LoggingResourceable corresponding to this ContextEntry: "+ce.toString()+", ce.getOLATResourceable()="+ce.getOLATResourceable()+", ce.getOLATResourceable().getOlatResource()="+oresourceableOres+", dump of resource infos:");
					for (Iterator<ILoggingResourceable> it2 = inputCopy.iterator(); it2.hasNext();) {
						ILoggingResourceable resourceInfo = it2.next();
						log_.info("id: "+resourceInfo.getId()+", name="+resourceInfo.getName()+", type="+resourceInfo.getType()+", toString: "+resourceInfo.toString());
					}
					if(log_.isDebug()) {//only generate the stacktrace in debug mode
						log_.warn("Could not find any LoggingResourceable corresponding to this ContextEntry: "+ce.toString(), 
								new Exception("UserActivityLoggerImpl.getCombinedOrderedLoggingResourceables()"));
					} else {
						log_.warn("Could not find any LoggingResourceable corresponding to this ContextEntry: "+ce.toString(), null);
					}
				}
				*/
			}
		}
		
		if (inputCopy.size()!=0) {
			// otherwise we have an inconsistency 
			
			// just add all the remaining from inputCopy to result
			// no idea about the ordering - but the inputCopy has some sort of useful ordering as well, presumably
			result.addAll(inputCopy);
		}
		
		return result;
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
		Long logStart = null;
		if (log_.isDebugEnabled()) {
			logStart = System.currentTimeMillis();
		}
		final ActionType actionType = stickyActionType_!=null ? stickyActionType_ : loggingAction.getResourceActionType();

		// don't log entries with loggingAction type 'tracking'
		if(isLogAnonymous_ && actionType.equals(ActionType.tracking)) {
			return;
		}
		
		// fetch some of the loggingAction fields - used for error logging below
		final CrudAction crudAction = loggingAction.getCrudAction();
		final ActionVerb actionVerb = loggingAction.getActionVerb();
		final String actionObject = loggingAction.getActionObject();

		// calculate the combined and ordered list of LoggingResourceables which should go 
		// to the database below right away
		List<ILoggingResourceable> resourceInfos = getCombinedOrderedLoggingResourceables(lriOrNull);
		
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
		} else {
			for (ILoggingResourceable lr:resourceInfos) {
				if (lr.getResourceableType() == StringResourceableType.targetIdentity && StringHelper.isLong(lr.getId())) {
					identityKey = Long.valueOf(lr.getId());
				}
			}
		}
		if (identityKey == null) {
			// no identity available - odd
			log_.error("No identity available to UserActivityLogger. Cannot write log entry: {}:{}, {}, {}",
					crudAction, actionVerb, actionObject, convertLoggingResourceableListToString(resourceInfos),
					new Exception());
			return;
		}		
		
				
		if (actionType!=ActionType.admin) {
			final String identityKeyStr = String.valueOf(identityKey);
			for (Iterator<ILoggingResourceable> it = resourceInfos.iterator(); it.hasNext();) {
				ILoggingResourceable lr = it.next();
				// we want this info as too much actionTypes are non-admin and log-entry will then be without value not containing targetIdent!, see FXOLAT-104
				if (lr.getResourceableType()==StringResourceableType.targetIdentity && lr.getId().equals(identityKeyStr)) {
					if (log_.isDebugEnabled()) {
						// complain
						log_.debug("OLAT-4955: Not storing targetIdentity for non-admin logging actions. A non-admin logging action wanted to store a user other than the one from the session: action="+loggingAction+", fieldId="+loggingAction.getJavaFieldIdForDebug(), new Exception("OLAT-4955 debug stacktrac"));
					}
					// remove targetIdentity (fxdiff: only if same as executing identity!)
					it.remove();
				}
			}
		}
		// end of moved code
		if(resourceInfos != null) {
			//remove all ignorable resources
			for(Iterator<ILoggingResourceable> riIterator=resourceInfos.iterator(); riIterator.hasNext(); ) {
				if(riIterator.next().isIgnorable()) {
					riIterator.remove();
				}
			}
		}
		
		if (loggingAction.getTypeListDefinition()==null) {
			// this is a foul!
			log_.warn("LoggingAction has no ResourceableTypeList defined: action="+loggingAction+", fieldId="+loggingAction.getJavaFieldIdForDebug());
		} else if(log_.isDebugEnabled()) {
			// good boy
			String errorMsg = loggingAction.getTypeListDefinition().executeCheckAndGetErrorMessage(resourceInfos);
			if (errorMsg!=null) {
				// we found an inconsistency
				// lets make this a warn
				log_.warn("LoggingAction reported an inconsistency (" + errorMsg + ") while logging: "+loggingAction.getActionVerb()+" "+loggingAction.getActionObject()+", action="+loggingAction+", fieldId="+loggingAction.getJavaFieldIdForDebug()+
						", expected: "+loggingAction.getTypeListDefinition().toString()+
						", actual: "+convertLoggingResourceableListToString(resourceInfos), new Exception("OLAT-4653"));
			}
		}
		
		// start creating the LoggingObject 
		final LoggingObject logObj = new LoggingObject(sessionId, identityKey, crudAction.name().substring(0,1), actionVerb.name(), actionObject);

		if (resourceInfos != null && !resourceInfos.isEmpty()) {
			// this should be the normal case - we do have LoggingResourceables which we can log
			// alongside the log message

			if (resourceInfos.size()>4) {
				log_.warn("More than 4 resource infos set on a user activity log. Can only have 4. Having: "+resourceInfos.size());
				int diff = resourceInfos.size()-4;
				for(int i=0; i<diff; i++) {
					resourceInfos.remove(3);
				}
			}
			
			// get the target resourceable
			ILoggingResourceable ri = resourceInfos.get(resourceInfos.size()-1);
			logObj.setTargetResourceInfo(ri);
			
			// now set parent - if applicable
			if (resourceInfos.size()>1) {
				ri = resourceInfos.get(resourceInfos.size()-2);
				logObj.setParentResourceInfo(ri);
			}
			
			// and set the grand parent - if applicable
			if (resourceInfos.size()>2) {
				ri = resourceInfos.get(resourceInfos.size()-3);
				logObj.setGrandParentResourceInfo(ri);
			}
			
			// and set the great grand parent - if applicable
			if (resourceInfos.size()>3) {
				ri = resourceInfos.get(resourceInfos.size()-4);
				logObj.setGreatGrandParentResourceInfo(ri);
			}
		}
		
		// fill the remaining fields
		logObj.setBusinessPath(businessPath_);
		logObj.setSourceClass(callingClass.getCanonicalName());
		logObj.setResourceAdminAction(actionType.equals(ActionType.admin)?true:false);
		
		// and store it
		DB db = DBFactory.getInstance();
		if (db!=null && db.isError()) {
			// then we would run into an ERROR when we'd do more with this DB
			// hence we just issue a log.info here with the details
			//@TODO: lower to log_.info once we checked that it doesn't occur very often (best for 6.4)
			log_.warn("log: DB is in Error state therefore the UserActivityLoggerImpl cannot store the following logging action into the loggingtable: "+logObj);
		} else {
			DBFactory.getInstance().saveObject(logObj);
		}
		if (log_.isDebugEnabled()) {
			Long logEnd = System.currentTimeMillis();
			log_.debug("log duration = " + (logEnd - logStart));
		}
	}

	/** toString for debug **/
	private String convertLoggingResourceableListToString(List<ILoggingResourceable> resourceInfos) {
		StringBuilder loggingResourceableListToString = new StringBuilder("[LoggingResourceables: ");
		loggingResourceableListToString.append(resourceInfos.size());
		for (Iterator<ILoggingResourceable> iterator = resourceInfos.iterator(); iterator.hasNext();) {
			ILoggingResourceable loggingResourceable = iterator.next();
			loggingResourceableListToString.append(", ");
			loggingResourceableListToString.append(loggingResourceable);
		}
		loggingResourceableListToString.append("]");
		return loggingResourceableListToString.toString();
	}
}
