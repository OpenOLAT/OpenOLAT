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

package org.olat.core.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.commons.fullWebApp.LockResourceInfos;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentHelper;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.winmgr.WindowManagerImpl;
import org.olat.core.util.FIFOMap;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;

/**
 * @author Felix Jost
 */

public class Windows implements Disposable, Serializable {

	private static final long serialVersionUID = -106724331637750672L;
	private static final String SESSIONID_NAME_FOR_WINDOWS = Windows.class.getName();

	private transient FIFOMap<UriPrefixIdPair,ChiefController> windows = new FIFOMap<>(100); // one user may at most save 100
	// windows in a session
	private int windowId = 1;
	private Boolean fullScreen;
	private final AtomicInteger assessmentStarted = new AtomicInteger();
	private transient WindowManager windowManagerImpl;
	
	private Windows() {
		// private constructor
		windowManagerImpl = new WindowManagerImpl();
	}

	/**
	 * @param ureq
	 * @return the Windows for this user
	 */
	public static Windows getWindows(UserRequest ureq) {
		UserSession us = ureq.getUserSession();
		return getWindows(us);
	}

	/**
	 * @param us
	 * @return the Windows for this user
	 */
	public static Windows getWindows(UserSession us) {
		Windows ws = (Windows)us.getEntry(SESSIONID_NAME_FOR_WINDOWS);
		if (ws == null) {
			final Windows newWs = new Windows();
			ws = (Windows)us.putEntryIfAbsent(SESSIONID_NAME_FOR_WINDOWS, newWs);
			if(ws == null) {
				ws = newWs;
			}
		}
		return ws;
	}
	
	public boolean disposeClosedWindows(UserRequest ureq) {
		String winId = ureq.getWindowID();
		String winCmpId = ureq.getWindowComponentID();
		
		boolean canBeRemoved = false;
		Map<UriPrefixIdPair,ChiefController> entries = windows.copyEntries();
		for(Map.Entry<UriPrefixIdPair,ChiefController> entry:entries.entrySet()) {
			Window window = entry.getValue().getWindow();
			if(window.getInstanceId().equals(winId) || window.getDispatchID().equals(winCmpId)) {
				window.setMarkToBeRemoved(false);
			} else if(window.canBeRemoved()) {
				window.getWindowBackOffice().dispose();
				windows.remove(entry.getKey());
				canBeRemoved = true;
			}
		}
		return canBeRemoved;
	}

	/**
	 * @param wId
	 * @return true if there is a window with this windowId
	 */
	public boolean isExisting(String uriPrefix, String wId) {
		return (getWindow(uriPrefix, wId) != null);
	}
	
	public boolean isExisting(UserRequest ureq) {
		return (getWindow(ureq) != null);
	}
	
	public Window getFirstWindow() {
		Map<UriPrefixIdPair,ChiefController> entries = windows.copyEntries();
		for(Map.Entry<UriPrefixIdPair,ChiefController> entry:entries.entrySet()) {
			if("/auth/".equals(entry.getKey().uriPrefix)) {
				return entry.getValue().getWindow();
			}
		}
		return null;
	}

	/**
	 * the url must be a valid dispatchUri (have a windowId), otherwise an
	 * exception is thrown
	 * 
	 * @param ureq
	 * @return null if no window to this windowId could be found or if the windowid is not in the request (e.g. shibbolthe nice error msg)
	 */
	public Window getWindow(UserRequest ureq) {
		String windowID = ureq.getWindowID();
		String componentId = ureq.getComponentID();
		String windowComponentId = ureq.getWindowComponentID();
		
		Window window = null;
		if (StringHelper.containsNonWhitespace(windowID)) {
			String uriPrefix = ureq.getUriPrefix();
			window = getWindow(uriPrefix, windowID);
		}
		
		if(window == null && StringHelper.containsNonWhitespace(windowComponentId)) {
			List<ChiefController> chiefControllers = windows.values();
			for(ChiefController cc:chiefControllers) {
				if(windowComponentId.equals(cc.getWindow().getDispatchID())) {
					window = cc.getWindow();
					break;
				}
			}
		}
		
		if(window == null && StringHelper.containsNonWhitespace(componentId)) {
			List<ChiefController> chiefControllers = windows.values();
			for(ChiefController cc:chiefControllers) {
				Window w = cc.getWindow();
				List<Component> cmps = new ArrayList<>();
				Component cmp = ComponentHelper.findDescendantOrSelfByID(w.getContentPane(), componentId, cmps);
				if(cmp != null) {
					window = w;
					break;
				}
			}
		}
		return window;
	}
	
	public ChiefController getChiefController(UserRequest ureq) {
		String uriPrefix = ureq.getUriPrefix();
		String windowID = ureq.getWindowID();
		String componentId = ureq.getComponentID();
		String windowComponentId = ureq.getWindowComponentID();
		
		ChiefController chief = null;
		if (windowID != null) {
			chief = windows.get(new UriPrefixIdPair(uriPrefix, windowID));
		}
		
		if(chief == null && StringHelper.containsNonWhitespace(windowComponentId)) {
			List<ChiefController> chiefControllers = windows.values();
			for(ChiefController cc:chiefControllers) {
				if(windowComponentId.equals(cc.getWindow().getDispatchID())) {
					chief = cc;
					break;
				}
			}
		}
		
		if(chief == null && StringHelper.containsNonWhitespace(componentId)) {
			List<ChiefController> chiefControllers = windows.values();
			for(ChiefController cc:chiefControllers) {
				List<Component> path = new ArrayList<>();
				Component cmp = ComponentHelper.findDescendantOrSelfByID(cc.getWindow().getContentPane(), componentId, path);
				if(cmp != null) {
					chief = cc;
					break;
				}
			}
		}
		return chief;
	}

	/**
	 * gets the window.
	 * 
	 * @param windowID
	 * @return null if the window is not existing (here in windows)
	 */
	private Window getWindow(String uriPrefix, String windowID) {
		ChiefController chief = windows.get(new UriPrefixIdPair(uriPrefix, windowID));
		return chief == null ? null : chief.getWindow();
	}

	/**
	 * @param w
	 * @return true if already registered
	 */
	public boolean isRegistered(Window w) {
		String uriPrefix = w.getUriPrefix();
		String id = w.getInstanceId();
		return windows.get(new UriPrefixIdPair(uriPrefix, id)) != null;
	}

	/**
	 * registers the window. if the window is already registered, then nothing is
	 * done
	 * 
	 * @param w
	 */
	public void registerWindow(ChiefController chief) {
		Window w = chief.getWindow();
		String uriPrefix = w.getUriPrefix();
		String id = w.getInstanceId();
		if (windows.get(new UriPrefixIdPair(uriPrefix, id)) != null) {
			return; // we are already registered
		}
		String wiid = String.valueOf(windowId++); // ok since per user session, not
		w.setInstanceId(wiid);
		windows.put(new UriPrefixIdPair(uriPrefix, wiid), chief);
	}

	/**
	 * in case you close a window you should also deregister it. This implies
	 * that deregistering does not clean up, e.g. dispose the window!
	 * @param w
	 */
	public void deregisterWindow(Window w) {
		String uriPrefix = w.getUriPrefix();
		String id = w.getInstanceId();
		//no longer available
		if(windows.get(new UriPrefixIdPair(uriPrefix, id)) == null) return;
		windows.remove(new UriPrefixIdPair(uriPrefix, id));
	}
	
	/**
	 * @return the iterator with windows in it
	 */
	public Iterator<Window> getWindowIterator() {
		List<Window> ws = new ArrayList<>(windows.size());
		List<ChiefController> chiefControllers = windows.values();
		for(ChiefController chiefController:chiefControllers) {
			ws.add(chiefController.getWindow());
		}
		return ws.iterator();
	}
	
	/**
	 * Search all the windows to find the first locked chief controller
	 * and returns informations about the locked resource.
	 * 
	 * @return Informations about the current locked resource, or null if the
	 * 		user has not a chief controller locked.
	 */
	public LockResourceInfos getLockResourceInfos() {
		List<ChiefController> chiefControllers = windows.values();
		for(ChiefController chiefController:chiefControllers) {
			LockResourceInfos lockInfos = chiefController.getLockResourceInfos();
			if(lockInfos != null) {
				return lockInfos;
			}
		}
		return null;
	}

	/**
	 * @return the number of windows
	 */
	public int getWindowCount() {
		return windows.size();
	}

	/**
	 * @return Returns the windowManager.
	 */
	public WindowManager getWindowManager() {
		return windowManagerImpl;
	}

	public Boolean getFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(Boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public AtomicInteger getAssessmentStarted() {
		return assessmentStarted;
	}

	@Override
	public void dispose() {
		List<ChiefController> chiefControllers = windows.values();
		for(ChiefController chiefController:chiefControllers) {
			chiefController.getWindow().getWindowBackOffice().dispose();
		}
		windows.clear();
	}
	
	private static class UriPrefixIdPair {
		private final String uriPrefix;
		private final String wId;
		
		public UriPrefixIdPair(String uriPrefix, String wId) {
			this.uriPrefix = uriPrefix;
			this.wId = wId;
		}
		
		@Override
		public int hashCode() {
			return (uriPrefix == null ? 364587 : uriPrefix.hashCode())
					+ (wId == null ? 9827 : wId.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof UriPrefixIdPair) {
				UriPrefixIdPair pair = (UriPrefixIdPair)obj;
				return uriPrefix != null && uriPrefix.equals(pair.uriPrefix)
						&& wId != null && wId.equals(pair.wId);
			}
			return false;
		}
	}
}