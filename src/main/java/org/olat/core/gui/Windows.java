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
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.winmgr.WindowManagerImpl;
import org.olat.core.gui.util.bandwidth.SlowBandWidthSimulator;
import org.olat.core.gui.util.bandwidth.SlowBandWidthSimulatorImpl;
import org.olat.core.util.FIFOMap;
import org.olat.core.util.UserSession;

/**
 * @author Felix Jost
 */

public class Windows implements Disposable, Serializable {

	private static final long serialVersionUID = -106724331637750672L;
	private static final String SESSIONID_NAME_FOR_WINDOWS = Windows.class.getName();

	private transient FIFOMap<UriPrefixIdPair,Window> windows = new FIFOMap<>(100); // one user may at most save 100
	// windows in a session
	private int windowId = 1;
	private Boolean fullScreen;
	private final AtomicInteger assessmentStarted = new AtomicInteger();
	private transient WindowManager windowManagerImpl;
	private transient ChiefController chiefController;

	private transient SlowBandWidthSimulator sbws;
	
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

	/**
	 * @param wId
	 * @return true if there is a window with this windowId
	 */
	public boolean isExisting(String uriPrefix, String wId) {
		return (getWindow(uriPrefix, wId) != null);
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
		if (windowID == null) return null;
		String uriPrefix = ureq.getUriPrefix();
		return getWindow(uriPrefix, windowID);
	}

	/**
	 * gets the window.
	 * 
	 * @param windowID
	 * @return null if the window is not existing (here in windows)
	 */
	private Window getWindow(String uriPrefix, String windowID) {
		Window w = windows.get(new UriPrefixIdPair(uriPrefix, windowID));
		return w;
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
	public void registerWindow(Window w) {
		String uriPrefix = w.getUriPrefix();
		String id = w.getInstanceId();
		if (windows.get(new UriPrefixIdPair(uriPrefix, id)) != null) {
			return; // we are already registered
		}
		String wiid = String.valueOf(windowId++); // ok since per user session, not
		w.setInstanceId(wiid);
		windows.put(new UriPrefixIdPair(uriPrefix, wiid), w);
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
		return windows.getValueIterator();
	}

	/**
	 * @return the number of windows
	 */
	public int getWindowCount() {
		return windows.size();
	}

	/**
	 * @return Returns the windowId.
	 */
	public int getWindowId() {
		return windowId;
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

	public ChiefController getChiefController() {
		return chiefController;
	}

	public void setChiefController(ChiefController chiefController) {
		this.chiefController = chiefController;
	}

	@Override
	public void dispose() {
		if(chiefController != null) {
			chiefController.dispose();
		}
		if(windowManagerImpl != null) {
			windowManagerImpl.dispose();
		}
	}

	/**
	 * @return
	 */
	public SlowBandWidthSimulator getSlowBandWidthSimulator() {
		if (sbws == null) sbws = new SlowBandWidthSimulatorImpl();
		return sbws;
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