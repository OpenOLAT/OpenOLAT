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

package org.olat.core.gui.control.winmgr;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowControllerCreator;
import org.olat.core.helpers.Settings;

/**
 * Initial Date:  23.03.2006 <br>
 *
 * @author Felix Jost
 */
public class WindowManagerImpl implements WindowManager {
	
	private List<WindowBackOfficeImpl> wbos = new ArrayList<>();
	
	private GlobalSettings globalSettings;
	private boolean ajaxEnabled = false;
	
	private boolean showDebugInfo = false;
	private boolean idDivsForced = false;

	private int fontSize = 100; // default width

	private PopupBrowserWindowControllerCreator pbwcc;
	
	public WindowManagerImpl() {
		pbwcc = (PopupBrowserWindowControllerCreator) 
			CoreSpringFactory.getBean(PopupBrowserWindowControllerCreator.class);

		final AJAXFlags aflags = new AJAXFlags(this);
		globalSettings = new GlobalSettings() {
			@Override
			public int getFontSize() {
				return WindowManagerImpl.this.getFontSize();
			}
			@Override
			public AJAXFlags getAjaxFlags() {
				return aflags;
			}
			@Override
			public boolean isIdDivsForced() {
				return WindowManagerImpl.this.isIdDivsForced();
			}
		};
	}

	@Override
	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}
	
	@Override
	public void setAjaxWanted(UserRequest ureq) {
		setAjaxEnabled(!Settings.isBrowserAjaxBlacklisted(ureq));
	}

	/**
	 * @return Returns the ajaxEnabled.
	 */
	@Override
	public boolean isAjaxEnabled() {
		return ajaxEnabled;
	}

	/**
	 * <b>Only use for debug mode!!!<b><br>
	 * use setAjaxWanted(ureq) instead
	 * 
	 * sets the ajax on/off flag, -ignoring the browser-
	 * @param enabled if true, ajax is on, renderers can render their links to post to the background frame and so on
	 */
	@Override
	public void setAjaxEnabled(boolean enabled) {
		this.ajaxEnabled  = enabled;
		for (WindowBackOfficeImpl wboImpl : wbos) {
			wboImpl.setAjaxEnabled(enabled);
		}			
	}
	
	public void setShowDebugInfo(boolean showDebugInfo) {
		this.showDebugInfo = showDebugInfo;
		for (WindowBackOfficeImpl wboImpl : wbos) {
			wboImpl.setShowDebugInfo(showDebugInfo);
		}			
	}

	public int getFontSize() {
		return fontSize;
	}

	@Override
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.WindowManager#createWindowBackOffice(java.lang.String, org.olat.core.gui.control.ChiefController)
	 */
	@Override
	public WindowBackOffice createWindowBackOffice(String windowName, ChiefController owner, WindowSettings settings) {
		WindowBackOfficeImpl wbo = new WindowBackOfficeImpl(this, windowName, owner, settings);
		wbos.add(wbo);
		return wbo;
	}

	@Override
	public void dispose() {
		for (WindowBackOfficeImpl wboImpl : wbos) {
			wboImpl.dispose();
		}		
	}

	protected boolean isShowDebugInfo() {
		return showDebugInfo;
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.WindowManager#createNewPopupBrowserWindowFor(org.olat.core.gui.UserRequest, org.olat.core.gui.control.creator.ControllerCreator, boolean)
	 */
	@Override
	public PopupBrowserWindow createNewPopupBrowserWindowFor(UserRequest ureq, ControllerCreator contentControllerCreator) {
		//supports the open(ureq) method
		PopupBrowserWindowController cc = pbwcc.createNewPopupBrowserController(ureq, contentControllerCreator);
		cc.addBodyCssClass("o_body_popup");
		return cc;
	}
	
	@Override
	public PopupBrowserWindow createNewUnauthenticatedPopupWindowFor(UserRequest ureq, ControllerCreator contentControllerCreator) {
		//supports the open(ureq) method
		PopupBrowserWindowController cc = pbwcc.createNewUnauthenticatedPopupWindowController(ureq, contentControllerCreator);
		//the content controller for the popupwindow is generated and set
		//at the moment the open method is called!!
		cc.addBodyCssClass("o_body_popup");
		return cc;
	}
	
	
	/**
	 * needed only by guidebugdispatchercontroller for the gui debug mode!
	 * @param idDivsForced
	 */
	public void setIdDivsForced(boolean idDivsForced) {
		this.idDivsForced = idDivsForced;
	}
	
	/**
	 * @return
	 */
	public boolean isIdDivsForced() {
		return idDivsForced;
	}
}