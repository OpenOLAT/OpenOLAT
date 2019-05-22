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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.JSAndCSSAdderImpl;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.guistack.GuiStackNiceImpl;
import org.olat.core.gui.control.pushpoll.WindowCommand;
import org.olat.core.gui.control.util.ZIndexWrapper;
import org.olat.core.gui.dev.controller.DevelopmentController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.render.intercept.InterceptHandler;
import org.olat.core.gui.render.intercept.InterceptHandlerInstance;
import org.olat.core.gui.render.intercept.debug.GuiDebugDispatcherController;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.ui.I18nUIFactory;
import org.olat.core.util.i18n.ui.InlineTranslationInterceptHandlerController;

/**
 * Description:<br>
 * impl of windowbackoffice - responsible for several activities around a (browser)window
 * 
 * <P>
 * Initial Date: 10.02.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class WindowBackOfficeImpl implements WindowBackOffice {
	
	private static final Logger log = Tracing.createLoggerFor(WindowBackOfficeImpl.class);

	private final WindowManagerImpl winmgrImpl;
	private final Window window;
	private WindowSettings settings;
	private final ChiefController windowOwner;
	
	private InterceptHandler linkedInterceptHandler;
	private InterceptHandler debug_interceptHandler;
	private InterceptHandler inlineTranslation_interceptHandler;
	
	private AjaxController ajaxC;
	private GuiDebugDispatcherController guidebugC;
	private InlineTranslationInterceptHandlerController inlineTranslationC;
	
	private List<ZIndexWrapper> guiMessages = new ArrayList<>(); // request-transient render-related data
	
	private transient List<GenericEventListener> cycleListeners = new CopyOnWriteArrayList<>();
	
	WindowBackOfficeImpl(final WindowManagerImpl winmgrImpl, String windowName, ChiefController windowOwner, WindowSettings settings) {
		this.winmgrImpl = winmgrImpl;
		this.windowOwner = windowOwner;
		window = new Window(windowName, this);
		this.settings = settings;

		linkedInterceptHandler = new InterceptHandler() {
			@Override
			public InterceptHandlerInstance createInterceptHandlerInstance() {
				InterceptHandler debugH = debug_interceptHandler;
				InterceptHandler inlineTranslationH = inlineTranslation_interceptHandler;
				final InterceptHandlerInstance debugI = debugH == null? null: debugH.createInterceptHandlerInstance(); 
				final InterceptHandlerInstance inlineTranslationI = (inlineTranslationH == null ? null : inlineTranslationH.createInterceptHandlerInstance());
				return new InterceptHandlerInstance() {
					@Override
					public ComponentRenderer createInterceptComponentRenderer(ComponentRenderer originalRenderer) {
						ComponentRenderer toUse = originalRenderer;

						if (winmgrImpl.isShowDebugInfo() && debugI != null) {
							toUse = debugI.createInterceptComponentRenderer(toUse);
						}
						if (I18nManager.getInstance().isCurrentThreadMarkLocalizedStringsEnabled() && inlineTranslationI != null) {
							toUse = inlineTranslationI.createInterceptComponentRenderer(toUse);
						}
						return toUse;
					}};
			}};
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#getWindow()
	 */
	@Override
	public Window getWindow() {
		return window;
	}

	@Override
	public ChiefController getChiefController() {
		return windowOwner;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#createDevelopmentController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createDevelopmentController(UserRequest ureq, WindowControl windowControl) {
		return new DevelopmentController(ureq, windowControl,this);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#getGlobalSettings()
	 */
	@Override
	public GlobalSettings getGlobalSettings() {
		return winmgrImpl.getGlobalSettings();
	}

	@Override
	public WindowSettings getWindowSettings() {
		if(settings == null) {
			settings = new WindowSettings();
		}
		return settings;
	}

	@Override
	public void setWindowSettings(WindowSettings settings) {
		this.settings = settings;
	}

	/**
	 * @return
	 */
	public JSAndCSSAdderImpl createJSAndCSSAdder() {
		return new JSAndCSSAdderImpl(this);
	}

	@Override
	public void sendCommandTo(Command wco) {
		if (ajaxC != null) ajaxC.sendCommandTo(new WindowCommand(this,wco));
	}
	
	public void pushCommands(UserRequest ureq, HttpServletRequest request, HttpServletResponse response) {
		try {
			boolean acceptJson = ServletUtil.acceptJson(request);
			//first set the headers with the content-type
			//and after get the writer with the encoding
			//fixed by the content-type
			if(acceptJson) {
				ServletUtil.setJSONResourceHeaders(response);
				Writer w = response.getWriter();
				ajaxC.pushJSONAndClear(ureq, w);
			} else {
				ServletUtil.setStringResourceHeaders(response);
				Writer w = response.getWriter();
				ajaxC.pushResource(ureq, w, true);
			}
		} catch (IOException e) {
			log.error("Error pushing commans to the AJAX canal.", e);
		}
	}

	/**
	 * @param wrapHTML
	 * @return
	 */
	public MediaResource extractCommands(HttpServletRequest request) {
		boolean acceptJson = ServletUtil.acceptJson(request);
		return ajaxC.extractMediaResource(!acceptJson);
	}

	/**
	 * @return
	 */
	public InterceptHandler getInterceptHandler() {
		return linkedInterceptHandler;
	}

	/**
	 * @param ureq
	 * @param windowControl
	 * @return the debug controller (not visible on screen, only in debug mode it wraps around each component for dispatching of gui debug info)
	 */
	public Controller createDebugDispatcherController(UserRequest ureq, WindowControl windowControl) {
		guidebugC = new GuiDebugDispatcherController(ureq, windowControl);
		this.debug_interceptHandler  = guidebugC;
		return guidebugC;
	}

	/**
	 * Factory method to create the inline translation tool dispatcher controller.
	 * This implicitly sets the translation controller on the window back office
	 * 
	 * @param ureq
	 * @param windowControl
	 * @return
	 */
	@Override
	public Controller createInlineTranslationDispatcherController(UserRequest ureq, WindowControl windowControl) {
		if (inlineTranslationC != null) throw new AssertException("Can't set the inline translation dispatcher twice!", null);
		inlineTranslationC = I18nUIFactory.createInlineTranslationIntercepHandlerController(ureq, windowControl);
		this.inlineTranslation_interceptHandler  = inlineTranslationC;
		return inlineTranslationC;
	}

	@Override
	public Controller createAJAXController(UserRequest ureq) {
		boolean ajaxEnabled = winmgrImpl.isAjaxEnabled();
		ajaxC = new AjaxController(ureq, this, ajaxEnabled);
		return ajaxC;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#isDebuging()
	 */
	@Override
	public boolean isDebuging() {
		return Settings.isDebuging();
	}

	public WindowManagerImpl getWinmgrImpl() {
		return winmgrImpl;
	}

	@Override
	public void dispose() {
		windowOwner.dispose();
	}

	/**
	 * @param enabled
	 */
	public void setAjaxEnabled(boolean enabled) {
		if (ajaxC != null) ajaxC.setAjaxEnabled(enabled);
	}

	/**
	 * @param refreshInterval
	 */
	public void setRequiredRefreshInterval(int refreshInterval) {
		if (ajaxC != null) ajaxC.setPollPeriod(refreshInterval);
	}

	/**
	 * @param showDebugInfo
	 */
	public void setShowDebugInfo(boolean showDebugInfo) {
		if (guidebugC != null) {
			guidebugC.setShowDebugInfo(showDebugInfo);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#getWindowManager()
	 */
	@Override
	public WindowManager getWindowManager() {
		return winmgrImpl;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#createGuiStack(org.olat.core.gui.components.Component)
	 */
	@Override
	public GuiStack createGuiStack(Component initialComponent) {
		return new GuiStackNiceImpl(this, initialComponent);
	}

	public void fireCycleEvent(Event cycleEvent) {
		for (GenericEventListener gel : cycleListeners) {
			gel.event(cycleEvent);
		}
		if (cycleEvent == Window.AFTER_VALIDATING) {
			// clear the added data for this cycle
			guiMessages.clear();
		}
	}

	@Override
	public void addCycleListener(GenericEventListener gel) {
		cycleListeners.add(gel);
	}

	@Override
	public void removeCycleListener(GenericEventListener gel) {
		// Since we use a CopyOnWriteArrayList it is save to remove an event
		// listener even when we are in the fireCycleEvent() method at the same time
		cycleListeners.remove(gel);
	}

	@Override
	public List<ZIndexWrapper> getGuiMessages() {
		return guiMessages;
	}
}
