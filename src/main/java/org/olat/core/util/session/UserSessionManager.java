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
package org.olat.core.util.session;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryManager;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.OlatLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.logging.activity.UserActivityLoggerImpl;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.SignOnOffEvent;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("userSessionManager")
public class UserSessionManager implements GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(UserSessionManager.class);
	private static final String USERSESSIONKEY = UserSession.class.getName();
	private static final String CSRFSESSIONTOKEN = "user-session-csrf-token";
	
	public static final OLATResourceable ORES_USERSESSION = OresHelper.createOLATResourceableType(UserSession.class);
	public static final String STORE_KEY_KILLED_EXISTING_SESSION = "killedExistingSession";
	
	public static final String EXTENDED_DMZ_TIMEOUT = "extended-session-timeout--oo";

	//clusterNOK cache ??
	private static final Set<UserSession> authUserSessions = ConcurrentHashMap.newKeySet();
	private static final Set<Long> userNameToIdentity = ConcurrentHashMap.newKeySet();
	private static final Set<Long> authUsersNamesOtherNodes = ConcurrentHashMap.newKeySet();

	private static final AtomicInteger sessionCountWeb = new AtomicInteger();
	private static final AtomicInteger sessionCountRest = new AtomicInteger();
	private static final AtomicInteger sessionCountDav = new AtomicInteger();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserSessionModule sessionModule;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private HistoryManager historyManager;
	
	private CacheWrapper<Long,Integer> userSessionCache;
	
	@PostConstruct
	public void initBean() {
		coordinator.getCoordinator().getEventBus().registerFor(this, null, ORES_USERSESSION);
		userSessionCache = coordinator.getCoordinator().getCacher().getCache(UserSessionManager.class.getSimpleName(), "usersession");
	}

	/**
	 * @param session
	 * @return associated user session
	 */
	public UserSession getUserSession(HttpServletRequest hreq, HttpSession session) {
		UserSession us = (UserSession) session.getAttribute(USERSESSIONKEY);
		if(us == null) {
			synchronized (session) {//o_clusterOK by:fj
				us = (UserSession) session.getAttribute(USERSESSIONKEY);
				if (us == null) {
					String csrfToken = (String)session.getAttribute(CSRFSESSIONTOKEN);
					if(csrfToken == null) {
						csrfToken = UUID.randomUUID().toString();
						session.setAttribute(CSRFSESSIONTOKEN, csrfToken);
					}
					us = new UserSession(csrfToken);
					session.setAttribute(USERSESSIONKEY, us); // triggers the
					// valueBoundEvent -> nothing
					// more to do here
				}
			}
		}
		//set a possible changed session timeout interval
		setHttpSessionTimeout(hreq, session, us);
		return us;
	}

	/**
	 * @param hreq
	 * @return associated user session
	 */
	public UserSession getUserSession(HttpServletRequest hreq) {
		// get existing or create new session
		HttpSession httpSession = hreq.getSession(true);
		return getUserSession(hreq, httpSession);
	}
	
	/**
	 * Return the UserSession of the given request if it is already set or null otherwise
	 * @param hreq
	 * @return
	 */
	public UserSession getUserSessionIfAlreadySet(HttpServletRequest hreq) {
		HttpSession session = hreq.getSession(false);
		if (session==null) {
			return null;
		}

		UserSession us = (UserSession) session.getAttribute(USERSESSIONKEY);
		setHttpSessionTimeout(hreq, session, us);
		return us;
	}

	private void setHttpSessionTimeout(HttpServletRequest hreq, HttpSession session, UserSession us) {
		if(us == null || session == null) return;
		
		int interval;
		if(us.isAuthenticated()) {
			if(us.getSessionInfo() != null && (us.getSessionInfo().isREST() || us.getSessionInfo().isWebDAV())) {
				if(extendedSessionTimeout(hreq)) {
					interval = sessionModule.getSessionTimeoutAuthenticated();
				} else {
					interval = 600;
				}
			} else {
				interval = sessionModule.getSessionTimeoutAuthenticated();
			}
		} else if(us.getEntry(EXTENDED_DMZ_TIMEOUT) instanceof Boolean) {
			interval = 3 * sessionModule.getSessionTimeout();
		} else {
			interval = sessionModule.getSessionTimeout();
		}
		if(interval != session.getMaxInactiveInterval()) {
			session.setMaxInactiveInterval(interval);
		}
	}
	
	/**
	 * @param hreq The HTTP servlet request
	 * @return true if the user agent allow to extend the session timeout
	 */
	private boolean extendedSessionTimeout(HttpServletRequest hreq) {
		if(hreq != null && StringHelper.containsNonWhitespace(hreq.getHeader("User-Agent"))) {
			String userAgent = hreq.getHeader("User-Agent");
			String[] userAgentsWithExtendedTimeout = sessionModule.getSessionTimeoutExtendedFor();
			for(String userAgentWithExtendedTimeout:userAgentsWithExtendedTimeout) {
				if(userAgent.contains(userAgentWithExtendedTimeout)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
     * @param identityKey The identity primary key
	 * @return true if the user with the specified key is currently logged
	 *         on
	 */
	public boolean isSignedOnIdentity(Long identityKey) {
		return userNameToIdentity.contains(identityKey);
	}
	
	/**
	 * @return set of authenticated active user sessions
	 */
	public Set<UserSession> getAuthenticatedUserSessions() {
		return new HashSet<>(authUserSessions);
	}
	
	public int getNumberOfAuthenticatedUserSessions() {
		return authUserSessions.size();
	}
	
	/**
	 * This method returns only the number of local sessions.
	 * 
	 * @return Returns the userSessionsCnt (Web, WebDAV, REST) from this VM
	 */
	public int getUserSessionsCnt() {
		return authUserSessions.size();
	}
	
	/**
	 * @return The number of users currently logged in using the web interface
	 *         (guests and authenticated users).
	 */
	public int getUserSessionWebCounter() {
		return userSessionCache.size();
	}
	
	public boolean isOnline(Long identityKey) {
		return userSessionCache.containsKey(identityKey);
	}
	
	/**
	 * @return The number of users currently logged in using a WebDAV client.
	 *         Note that currently this only returns the users from this VM as
	 *         the synchronization of user between cluster node is not
	 *         correctly. In the long run we return all users here.
	 */
	public int getUserSessionDavCounter() {
		// clusterNOK ?? return only number of locale sessions ?
		return sessionCountDav.get();
	}
	
	/**
	 * @return The number of users currently logged in using the REST API. Note
	 *         that currently this only returns the users from this VM as the
	 *         synchronization of user between cluster node is not correctly. In
	 *         the long run we return all users here.
	 */
	public int getUserSessionRestCounter() {
		// clusterNOK ?? return only number of locale sessions ?
		return sessionCountRest.get();
	}



	/**
	 * prior to calling this method, all instance vars must be set.
	 */
	public void signOn(UserSession usess) {
		boolean isDebug = log.isDebugEnabled();
		// Added synchronized to be symmetric with sign off and to
		// fix a possible dead-lock see also OLAT-3390
		synchronized(usess) {
			if(isDebug) log.debug("signOn() START");
			if (usess.isAuthenticated()) {
				throw new AssertException("sign on: already signed on!");
			}
			
			IdentityEnvironment identityEnvironment = usess.getIdentityEnvironment();
			Identity identity = identityEnvironment.getIdentity();
			if (identity == null) {
				throw new AssertException("identity is null in identityEnvironment!");
			}
			SessionInfo sessionInfo = usess.getSessionInfo();
			if (sessionInfo == null) {
				throw new AssertException("sessionInfo was null for identity " + identity);
			}
			usess.setAuthenticated(true);
	
			if (sessionInfo.isWebDAV()) {
				// load user prefs
				usess.reloadPreferences();
				// we're only adding this webdav session to the authUserSessions - not to the userNameToIdentity.
				// userNameToIdentity is only needed for IM which can't do anything with a webdav session
				authUserSessions.add(usess);
				log.info(Tracing.M_AUDIT, "Logged on [via webdav]: " + sessionInfo.toString());
			} else {	
				UserSession invalidatedSession = null;

				if(isDebug) {
					log.debug("signOn() authUsersNamesOtherNodes.contains " + identity.getKey() + ": " + authUsersNamesOtherNodes.contains(identity.getKey()));
				}
					
			    // check if already a session exist for this user
			    if ( (userNameToIdentity.contains(identity.getKey()) || userSessionCache.containsKey(identity.getKey()) ) 
			         && !sessionInfo.isWebDAV() && !sessionInfo.isREST() && !usess.getRoles().isGuestOnly()) {
			        log.info("Loggin-process II: User has already a session => signOffAndClear existing session");
			        
			        invalidatedSession = getUserSessionForGui(identity.getKey());
			        //remove session to be invalidated
			        //SIDEEFFECT!! to signOffAndClear
			        //if invalidatedSession is removed from authUserSessions
			        //signOffAndClear does not remove the identity.getName().toLowerCase() from the userNameToIdentity
			        if(invalidatedSession != null) {
			        	authUserSessions.remove(invalidatedSession);
			        }
		    	}
		    	authUserSessions.add(usess);
				// user can choose upercase letters in identity name, but this has no effect on the
				// database queries, the login form or the IM account. IM works only with lowercase
				// characters -> map stores values as such
				if(isDebug) log.debug("signOn() adding to userNameToIdentity: " + identity.getKey());
				userNameToIdentity.add(identity.getKey());
				userSessionCache.put(identity.getKey(), new Integer(Settings.getNodeId()));
			
			
				//reload user prefs
				usess.reloadPreferences();
	
				log.info(Tracing.M_AUDIT, "Logged on: " + sessionInfo.toString());
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new SignOnOffEvent(identity, true), ORES_USERSESSION);
	
				// THE FOLLOWING CHECK MUST BE PLACED HERE NOT TO PRODUCE A DEAD-LOCK WITH SIGNOFFANDCLEAR
				// check if a session from any browser was invalidated (IE has a cookie set per Browserinstance!!)
				if (invalidatedSession != null || authUsersNamesOtherNodes.contains(identity.getKey())) {
					// put flag killed-existing-session into session-store to show info-message 'only one session for each user' on user-home screen
					usess.putEntry(STORE_KEY_KILLED_EXISTING_SESSION, Boolean.TRUE);
					if(isDebug) log.debug("signOn() removing from authUsersNamesOtherNodes: " + identity.getKey());
					authUsersNamesOtherNodes.remove(identity.getKey());
					//OLAT-3381 & OLAT-3382
					if(invalidatedSession != null) {
						signOffAndClear(invalidatedSession);
					}
				}
	
				if(isDebug) log.debug("signOn() END");
			}
			
			// update logged in users counters
			if (sessionInfo.isREST()) {
				sessionCountRest.incrementAndGet();
			} else if (sessionInfo.isWebDAV()) {
				sessionCountDav.incrementAndGet();
			} else {
				sessionCountWeb.incrementAndGet();
			}
		}
	}
	
	/**
	 * The method will logout the specified user from all of its
	 * session, UI, WebDAV, REST...
	 * @param identity
	 */
	public void signOffAndClearAll(IdentityRef identity) {
		List<UserSession> userSessions = authUserSessions.stream()
				.filter(userSession -> userSession.getIdentity() != null && userSession.getIdentity().getKey().equals(identity.getKey()))
				.collect(Collectors.toList());
		for(UserSession userSession:userSessions) {
			internSignOffAndClear(userSession);
			dbInstance.commit();
		}
	}
	
	/**
	 * called to make sure the current authenticated user (if there is one at all)
	 * is cleared and signed off. This method is firing the SignOnOffEvent Multiuserevent.
	 */
	public void signOffAndClear(UserSession usess) {  //o_clusterOK by:fj
		internSignOffAndClear(usess);
		//commit all changes after sign off, especially commit lock which were
		//deleted by dispose methods
		dbInstance.commit();
	}
	
	private void internSignOffAndClear(UserSession usess) {
		boolean isDebug = log.isDebugEnabled();
		if(isDebug) log.debug("signOffAndClear() START");
		
		signOffAndClearWithout(usess);
		// handle safely
		try {
			if (usess.isAuthenticated()) {
				SessionInfo sessionInfo = usess.getSessionInfo();
				IdentityEnvironment identityEnvironment = usess.getIdentityEnvironment();
				Identity identity = identityEnvironment.getIdentity();
				log.info(Tracing.M_AUDIT, "Logged off: {}", sessionInfo);
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new SignOnOffEvent(identity, false), ORES_USERSESSION);
				if(isDebug) log.debug("signOffAndClear() deregistering usersession from eventbus, id={}", sessionInfo);
				//fxdiff FXOLAT-231: event on GUI Preferences extern changes
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(Preferences.class, identity.getKey());
				CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(usess, ores);
			}
		} catch (Exception e) {
			log.error("exception in signOffAndClear: while sending signonoffevent!", e);
		}
		// clear all instance variables, set authenticated to false
		usess.init();
		if(isDebug) log.debug("signOffAndClear() END");
	}

	/**
	 * called from signOffAndClear()
	 * called from event -> MUEvent
	 * the real work to do during sign off but without sending the multiuserevent
	 * this is used in case the user logs in to node1 and was logged in on node2 => 
	 * node2 catches the sign on event and invalidates the user on node2 "silently", e.g.
	 * without firing an event.
	 */
	private void signOffAndClearWithout(final UserSession usess) {
		boolean isDebug = log.isDebugEnabled();
		if(isDebug) log.debug("signOffAndClearWithout() START");
		
		final IdentityEnvironment identityEnvironment = usess.getIdentityEnvironment();
		final SessionInfo sessionInfo = usess.getSessionInfo();
		final Identity ident = identityEnvironment.getIdentity();
		if (isDebug) log.debug("UserSession:::logging off: {}", sessionInfo);

		persistHistory(usess, ident);

		/**
		 * use not RunnableWithException, as exceptionHandlng is inside the run
		 */
		Runnable run = new Runnable() {
			@Override
			public void run() {
				Object obj = null;
				try {
					// do logging
					if (ident != null) {
						ThreadLocalUserActivityLogger.log(OlatLoggingAction.OLAT_LOGOUT, UserSession.class, CoreLoggingResourceable.wrap(ident));
					}
					// notify all variables in the store (the values) about the disposal
					// if
					// Disposable
					List<Object> storeList = usess.getStoreValues();

					for (Iterator<Object> it_storevals = storeList.iterator(); it_storevals.hasNext();) {
						obj = it_storevals.next();
						if (obj instanceof Disposable) {
							// synchronous, since triggered by tomcat session timeout or user
							// click and
							// asynchronous, if kicked out by administrator.
							// we assume synchronous
							// !!!!
							// As a reminder, this .dispose() calls dispose on
							// DefaultController which is synchronized.
							// (Windows/WindowManagerImpl/WindowBackOfficeImpl/BaseChiefController/../
							// dispose()
							// !!!! was important for bug OLAT-3390

							((Disposable) obj).dispose();
						}
					}
				} catch (Exception e) {

					String objtostr = "n/a";
					try {
						objtostr = (obj == null ? "NULL" : obj.toString());
					} catch (Exception ee) {
						// ignore
					}
					log.error("exception in signOffAndClear: while disposing object:" + objtostr, e);
				}
			}
		};

		ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(run, UserActivityLoggerImpl.newLoggerForValueUnbound(usess));

		if(authUserSessions.remove(usess)) {
			//remove only from identityEnvironment if found in sessions.
			//see also SIDEEFFECT!! line in signOn(..)
			Identity previousSignedOn = identityEnvironment.getIdentity();
			if (previousSignedOn != null && previousSignedOn.getKey() != null) {
				if(isDebug) log.debug("signOffAndClearWithout() removing from userNameToIdentity: {}", previousSignedOn.getKey());
				userNameToIdentity.remove(previousSignedOn.getKey());
				userSessionCache.remove(previousSignedOn.getKey());
			}
		} else if (isDebug) {
			log.info("UserSession already removed! for [{}]", ident);			
		}
			
		// update logged in users counters
		if (sessionInfo != null) {
			if (sessionInfo.isREST()) {
				sessionCountRest.decrementAndGet();
			} else if (sessionInfo.isWebDAV()) {
				sessionCountDav.decrementAndGet();
			} else {
				sessionCountWeb.decrementAndGet();
			}
		}
		
		if (isDebug) log.debug("signOffAndClearWithout() END");
	}
	
	private void persistHistory(final UserSession usess, final Identity ident) {
		if(usess.isAuthenticated() && usess.getLastHistoryPoint() != null && !usess.getRoles().isGuestOnly()) {
			Predicate<HistoryPoint> filter =  point -> {
				List<ContextEntry> entries = point.getEntries();
				if(entries == null || entries.isEmpty()) {
					return false;
				}
				String resType = entries.get(0).getOLATResourceable().getResourceableTypeName();
				return NewControllerFactory.getInstance().canResume(resType);
			};
			HistoryPoint lastPoint = usess.getLastHistoryPoint(filter);
			if(lastPoint != null) {
				historyManager.persistHistoryPoint(ident, lastPoint);
			}
		}
	}

	/**
	 * only for SignOffEvents
	 * - Usersession keeps book about usernames
	 * - WindowManager responsible to dispose controller chain
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(Event event) {
		if(event instanceof SignOnOffEvent) {
			SignOnOffEvent se = (SignOnOffEvent) event;
			processSignOnOffEvent(se);
		}
	}
	
	private void processSignOnOffEvent(SignOnOffEvent se) {
		try {
			boolean debug = log.isDebugEnabled();
			if(debug) log.debug("event() START");
			if(debug) log.debug("event() is SignOnOffEvent. isSignOn="+se.isSignOn());
			if (!se.isEventOnThisNode()) {
				// - signOnOff from other node
				// - Single OLAT Instance is never passing by here.
				if (se.isSignOn()) {
					// it is a logged on event
					// -> remember other nodes logged usernames
					if(debug) log.debug("event() adding to authUsersNamesOtherNodes: "+se.getIdentityKey());
					authUsersNamesOtherNodes.add(se.getIdentityKey());
					UserSession usess = getUserSessionForGui(se.getIdentityKey());
					if (usess != null && usess.getSessionInfo() != null && se.getIdentityKey().equals(usess.getSessionInfo().getIdentityKey())
							&& !usess.getSessionInfo().isWebDAV() && !usess.getRoles().isGuestOnly()) {
						
						// if this listening UserSession instance is from the same user
						// and it is not a WebDAV Session, and it is not GuestSession
						// => log user off on this node
						signOffAndClearWithout(usess);
						usess.init();
					}
				} else {
					// it is logged off event
					// -> remove from other nodes logged on list.
					if(debug) log.debug("event() removing from authUsersNamesOtherNodes: "+se.getIdentityKey());
					authUsersNamesOtherNodes.remove(se.getIdentityKey());
				}
			}
			if(debug) log.debug("event() END");
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * Invalidate all sessions except admin-sessions.
	 * @return  Number of invalidated sessions.
	 */
	public int invalidateAllSessions() {
		log.debug("invalidateAllSessions() START");
		int invalidateCounter = 0;
		log.info(Tracing.M_AUDIT, "All sessions were invalidated by an administrator");
		//clusterNOK ?? invalidate only locale sessions ?
		Set<UserSession> userSessions = getAuthenticatedUserSessions();
		for (UserSession userSession : userSessions) {
			Roles userRoles = userSession != null ? userSession.getRoles() : null; 
			if (userRoles != null && !userRoles.isAdministrator() && !userRoles.isSystemAdmin()) {
				//do not logout administrators
				try {
					internSignOffAndClear(userSession);
					if(userSession.getSessionInfo() != null && userSession.getSessionInfo().getSession() != null) {
						userSession.getSessionInfo().getSession().invalidate();
					}
					invalidateCounter++;
				} catch(Exception ex) {
					// Session already signed off => do nothing and continues
				}
			}
		}
		log.debug("invalidateAllSessions() END");
		return invalidateCounter;
	}
	
	/**
	 * Invalidate a given number of oldest (last-click-time) sessions except admin-sessions.
	 * @param nbrSessions  number of sessions whisch will be invalidated
	 * @return  Number of invalidated sessions. 
	 */
	public int invalidateOldestSessions(int nbrSessions) {
		int invalidateCounter = 0;
		// 1. Copy authUserSessions in sorted TreeMap
		// This is the Comparator that will be used to sort the TreeSet:
		Comparator<UserSession> sessionComparator = (o1, o2) -> {
			Long long1 = Long.valueOf((o1).getSessionInfo().getLastClickTime());
			Long long2 = Long.valueOf((o2).getSessionInfo().getLastClickTime());
			return long1.compareTo(long2);
		};
		// clusterNOK ?? invalidate only locale sessions ?
		TreeSet<UserSession> sortedSet = new TreeSet<>(sessionComparator);
		sortedSet.addAll(authUserSessions);
		int i = 0;	
		for (Iterator<UserSession> iterator = sortedSet.iterator(); iterator.hasNext() && i++<nbrSessions;) {
			try {
				UserSession userSession = iterator.next();
				if (!userSession.getRoles().isAdministrator() && !userSession.getRoles().isSystemAdmin()
						&& !userSession.getSessionInfo().isWebDAV()) {
					internSignOffAndClear(userSession);
					invalidateCounter++;
				}
			} catch (Throwable th) {
				log.warn("Error signOffAndClear ", th);
			}
		}
		return invalidateCounter;		
	}

	/**
	 * set session timeout on http session - 
	 * @param sessionTimeoutInSec
	 */
	public void setGlobalSessionTimeout(int sessionTimeoutInSec) {
		UserSession[] currentSessions = authUserSessions.toArray(new UserSession[0]);
		for(int i=currentSessions.length; i-->0; ) {
			try{
				SessionInfo sessionInfo = currentSessions[i].getSessionInfo();
				if(sessionInfo != null && sessionInfo.getSession() != null) {
					sessionInfo.getSession().setMaxInactiveInterval(sessionTimeoutInSec);
				}
			} catch(Throwable th){
				log.error("error setting sesssionTimeout", th);
			}
		}
	}
	
	/**
	 * Lookup non-webdav, non-REST UserSession for identity key.
	 * @param identityKey
	 * @return user-session or null when no session was founded. 
	 */
	private UserSession getUserSessionForGui(Long identityKey) {
		UserSession identitySession = null;
		if(identityKey != null) {
			//do not call from somewhere else then signOffAndClear!!
			Optional<UserSession> optionalSession = authUserSessions.stream().filter(userSession -> {
				Identity identity = userSession.getIdentity();
				if (identity != null && identityKey.equals(identity.getKey())
						&& userSession.getSessionInfo() != null
						&& !userSession.getSessionInfo().isWebDAV()
						&& !userSession.getSessionInfo().isREST()) {
					return true;
				}
				return false;
			}).findFirst();
			
			identitySession = optionalSession.isPresent() ? optionalSession.get() : null;
		}
		return identitySession;
	}
}
