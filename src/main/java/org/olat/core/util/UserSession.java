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

package org.olat.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Predicate;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.id.context.HistoryPointImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.PreferencesFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.resource.WindowedResourceableList;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.assessment.model.TransientAssessmentMode;

/**
 * Description: <BR/>the httpsession contains an instance of this class. the
 * UserSession is either authenticated or not; and if it is, then it also
 * contains things like the Identity, the locale etc. of the current user. <P/>
 * 
 * @author Felix Jost
 */
public class UserSession implements HttpSessionBindingListener, GenericEventListener, Serializable  {
	
	private static final Logger log = Tracing.createLoggerFor(UserSession.class);	
	private static final long serialVersionUID = 1975177605776990868L;

	// the environment (identity, locale, ..) of the identity
	private IdentityEnvironment identityEnvironment;
	private SessionInfo sessionInfo;
	
	private OLATResourceable lockResource;
	private TransientAssessmentMode lockMode;
	private List<TransientAssessmentMode> assessmentModes;
	private final List<OLATResourceable> secondaryLockResources = new ArrayList<>();
	
	private transient WindowedResourceableList resourceList = new WindowedResourceableList();
	
	private transient Map<String,Object> store;
	/**
	 * things to put into that should not be clear when signing on (e.g. remember url for a direct jump)
	 */
	private transient Map<String,Object> nonClearedStore = new HashMap<>();
	private transient Object lockStores = new Object();
	private boolean authenticated = false;
	private boolean savedSession = false;
	private transient Preferences guiPreferences;
	private transient EventBus singleUserSystemBus;
	private List<String> chats;
	private final Stack<HistoryPoint> history = new Stack<>();
	
	private String csrfToken;

	public UserSession() {
		init();
		csrfToken = UUID.randomUUID().toString();
	}
	
	public UserSession(String csrfToken) {
		init();
		this.csrfToken = csrfToken;
	}

	public void init() {
		store = new HashMap<>(4);
		identityEnvironment = new IdentityEnvironment();
		singleUserSystemBus = CoordinatorManager.getInstance().getCoordinator().createSingleUserInstance();
		authenticated = false;
		sessionInfo = null;
	}
	
	protected Object readResolve() {
		store = new HashMap<>(4);
		nonClearedStore = new HashMap<>();
		lockStores = new Object();
		resourceList = new WindowedResourceableList();
		singleUserSystemBus = CoordinatorManager.getInstance().getCoordinator().createSingleUserInstance();
		savedSession = true;
		authenticated = false;//reset authentication
		return this;
	}

	/**
	 * @return true if is authenticated
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}
	
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public boolean isSavedSession() {
		return savedSession;
	}

	public void setSavedSession(boolean savedSession) {
		this.savedSession = savedSession;
	}
	
	public String getCsrfToken() {
		return csrfToken;
	}

	public List<Object> getStoreValues() {
		List<Object> values;
		synchronized(lockStores) {
			values = new ArrayList<>(store.values());
		}
		return values;
	}

	/**
	 * @param key
	 * @param o
	 */
	public void putEntry(String key, Object o) {
		synchronized(lockStores) {
			store.put(key, o);
		}
	}
	
	public Object putEntryIfAbsent(String key, Object o) {
		synchronized(lockStores) {
			if (!store.containsKey(key)) {
				return store.put(key, o);
			} else {
				return store.get(key);
			}
		}
	}

	/**
	 * @param key
	 * @return entry
	 */
	public Object getEntry(String key) {
		if (key == null) {
			return null;
		}
		synchronized(lockStores) {
			if (store.get(key) != null) {
				return store.get(key);
			}
			if (nonClearedStore.get(key) != null) {
				return nonClearedStore.get(key);
			}
		}
		return null;
	}

	/**
	 * @param key
	 * @return removed entry
	 */
	public Object removeEntry(String key) {
		synchronized(lockStores) {
			return store.remove(key);
		}
	}

	/**
	 * put an entry in the usersession that even survives login/logouts from the
	 * users. needed e.g. for a direct jump url, when the url is remembered in the
	 * dmz, but used in auth. since a login occurs, all data from the previous
	 * user will be cleared, that is why we introduced this store.
	 * 
	 * @param key
	 * @param o
	 */
	public void putEntryInNonClearedStore(String key, Object o) {
		synchronized(lockStores) {
			nonClearedStore.put(key, o);
		}
	}

	/**
	 * @param key
	 * @return removed entry
	 */
	public Object removeEntryFromNonClearedStore(String key) {
		synchronized(lockStores) {
			return nonClearedStore.remove(key);
		}
	}
	
	public List<String> getChats() {
		if(chats == null) {
			synchronized(lockStores) {
				if(chats == null) {
					chats = new ArrayList<>(5);
				}
			}
		}
		return chats;
	}

	/**
	 * @return Locale
	 */
	public Locale getLocale() {
		return identityEnvironment.getLocale();
	}

	/**
	 * @return Identity
	 */
	public Identity getIdentity() {
		return identityEnvironment.getIdentity();
	}

	/**
	 * Sets the locale.
	 * 
	 * @param locale The locale to set
	 */
	public void setLocale(Locale locale) {
		identityEnvironment.setLocale(locale);
	}

	/**
	 * Sets the identity.
	 * 
	 * @param identity The identity to set
	 */
	public void setIdentity(Identity identity) {
		identityEnvironment.setIdentity(identity);
		//event on GUI Preferences external changes
		if(identity.getKey() != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(Preferences.class, identity.getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, ores);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, ores);
		}
	}

	/**
	 * @return Roles
	 */
	public Roles getRoles() {
		Roles result = identityEnvironment.getRoles();
		if (result == null) {
			log.warn("getRoles: null, this="+this, new RuntimeException("getRoles"));
		}
		return result;
	}

	/**
	 * Sets the roles.
	 * 
	 * @param roles The roles to set
	 */
	public void setRoles(Roles roles) {
		identityEnvironment.setRoles(roles);
	}
	
	/**
	 * @return identity environment
	 */
	public IdentityEnvironment getIdentityEnvironment() {
		return identityEnvironment;
	}
	
	public boolean isInAssessmentModeProcess() {
		return lockResource != null || lockMode != null
				|| (assessmentModes != null && assessmentModes.size() > 0);
	}

	public OLATResourceable getLockResource() {
		return lockResource;
	}
	
	public TransientAssessmentMode getLockMode() {
		return lockMode;
	}

	public void setLockResource(OLATResourceable lockResource, TransientAssessmentMode mode) {
		this.lockMode = mode;
		this.lockResource = lockResource;
	}
	
	public void unlockResource() {
		lockMode = null;
		lockResource = null;
		secondaryLockResources.clear();
	}
	
	/**
	 * @param ores The OLAT resource to compare
	 * @return true if the specified resource matches the primary or one
	 * 		of the secondary resources.
	 */
	public boolean matchLockResource(OLATResourceable ores) {
		return matchPrimaryLockResource(ores) || matchSecondaryResource(ores);
	}
	
	/**
	 * 
	 * @param ores The OLAT resource to compare
	 * @return true if a locked resource is present and match the specified one
	 */
	public boolean matchPrimaryLockResource(OLATResourceable ores) {
		return (lockResource != null && OresHelper.equals(lockResource, ores));
	}
	
	/**
	 * @param ores The OLAT resource to compare
	 * @return true if the specified resource matches one of the secondary resources
	 */
	public boolean matchSecondaryResource(OLATResourceable ores) {
		for(OLATResourceable secondaryLockResource:secondaryLockResources) {
			if(OresHelper.equals(secondaryLockResource, ores)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Can add a secondary resources. The primary is the course,
	 * secondary ones are document edited within the course.
	 * 
	 * @param ores Additional resources
	 */
	public void addSecondaryLockResource(OLATResourceable ores) {
		if(ores != null) {
			secondaryLockResources.add(OresHelper.clone(ores));
		}
	}

	public List<TransientAssessmentMode> getAssessmentModes() {
		return assessmentModes;
	}

	public void setAssessmentModes(List<TransientAssessmentMode> assessmentModes) {
		this.assessmentModes = assessmentModes;
	}

	/**
	 * may be null
	 * <p>
	 * @return session info object
	 */
	public SessionInfo getSessionInfo() {
		return sessionInfo;
	}

	/**
	 * @param sessionInfo
	 */
	public void setSessionInfo(SessionInfo sessionInfo) {
		this.sessionInfo = sessionInfo;
	}

	/**
	 * @return Returns the guiPreferences.
	 */
	public Preferences getGuiPreferences() {
	    return guiPreferences;
	}
	
	public void reloadPreferences() {
		Identity identity = identityEnvironment.getIdentity();
		guiPreferences = PreferencesFactory.getInstance().getPreferencesFor(identity, identityEnvironment.getRoles().isGuestOnly());
	}

	/**
	 * This is the olatsystembus to broadcast event amongst controllers of a single user only
	 * (the one whom this usersession belongs to)
	 * 
	 * @return the olatsystembus for the local user
	 */
	public EventBus getSingleUserEventCenter() {
		return singleUserSystemBus;
	}

	public List<HistoryPoint> getHistoryStack() {
		return new ArrayList<>(history);
	}
	
	public HistoryPoint getLastHistoryPoint() {
		if(history.isEmpty()) {
			return null;
		}
		return history.lastElement();
	}
	
	public HistoryPoint getLastHistoryPoint(Predicate<HistoryPoint> accept) {
		if(history.isEmpty()) {
			return null;
		}
		
		for(int i=history.size(); i-->0; ) {
			HistoryPoint point = history.get(i);
			if(accept.test(point)) {
				return point;
			}
		}
		return null;
	}
	
	public HistoryPoint getHistoryPoint(String id) {
		if(history.isEmpty()) {
			return null;
		}
		for(HistoryPoint point:history) {
			if(id.equals(point.getUuid())) {
				return point;
			}
		}
		return null;
	}
	
	public HistoryPoint popLastHistoryEntry() {
		if(history.isEmpty()) return null;
		history.pop();//current point
		if(history.isEmpty()) return null;
		return history.pop();//remove last point from history
	}
	
	public void addToHistory(UserRequest ureq, HistoryPoint point) {
		if(point == null) return;
		history.add(new HistoryPointImpl(ureq.getUuid(), point.getBusinessPath(), point.getEntries()));
	}
	
	public void addToHistory(UserRequest ureq, BusinessControl businessControl) {
		List<ContextEntry> entries = businessControl.getEntries();
		String businessPath = businessControl.getAsString();
		if(StringHelper.containsNonWhitespace(businessPath)) {
			String uuid = ureq.getUuid();
			if(!history.isEmpty()) {
				//consolidate
				synchronized(history) {
					for(Iterator<HistoryPoint> it=history.iterator(); it.hasNext(); ) {
						HistoryPoint p = it.next();
						if(uuid.equals(p.getUuid())) {
							it.remove();
						}
					}
				}
			}
			history.push(new HistoryPointImpl(ureq.getUuid(), businessPath, entries));
			if(history.size() > 20) {
				history.remove(0);
			}
		}
	}
	
	public void removeFromHistory(BusinessControl businessControl) {
		String businessPath = businessControl.getAsString();
		synchronized(history) {
			for(Iterator<HistoryPoint> it=history.iterator(); it.hasNext(); ) {
				String path = it.next().getBusinessPath();
				if(path.startsWith(businessPath)) {
					it.remove();
				}
			}
		}
	}
	
	public WindowedResourceableList getResourceList() {
		return resourceList;
	}

	@Override
	public void valueBound(HttpSessionBindingEvent be) {
		if (log.isDebugEnabled()) {
			log.debug("Opened UserSession: {}", this);
		}
	}

	/**
	 * called when the session is invalidated either by app. server timeout or manual session.invalidate (logout)
	 * 
	 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	@Override
	public void valueUnbound(HttpSessionBindingEvent be) {
		try {
			// the identity can be null if an loginscreen only session gets invalidated
			// (no user was authenticated yet but a tomcat session was created)
			Identity ident = identityEnvironment.getIdentity();
			CoreSpringFactory.getImpl(UserSessionManager.class).signOffAndClear(this);
			log.debug("Closed UserSession: identity = {}", (ident == null ? "n/a" : ident.getKey()));
			//we do not have a request in the null case (app. server triggered) and user not yet logged in
			//-> in this case we use the special empty activity logger
			if (ident == null) {
				ThreadLocalUserActivityLoggerInstaller.initEmptyUserActivityLogger();
			}
		} catch (Exception e) {
			log.error("exception while session was unbound!", e);
		}
		// called by tomcat's timer thread -> we need to close!! since the next unbound will be called from the same tomcat-thread
		finally {
			//o_clusterNOK: put into managed transaction wrapper
			DBFactory.getInstance().commitAndCloseSession();
		}
	}

	/**
	 * only for preference changed event
	 */
	@Override
	public void event(Event event) {
		if("preferences.changed".equals(event.getCommand())) {
			reloadPreferences();
		}
	}

	@Override
	public String toString() {
		return "Session of " + identityEnvironment + ", " + super.toString();
	}
}