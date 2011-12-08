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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.control.winmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.defaults.dispatcher.ClassPathStaticDispatcher;
import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.components.velocity.VelocityContainerRenderer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.ContentableChiefController;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.intercept.InterceptHandler;
import org.olat.core.gui.render.intercept.InterceptHandlerInstance;
import org.olat.core.helpers.Settings;
import org.olat.core.manager.BasicManager;

/**
 * Initial Date:  23.03.2006 <br>
 *
 * @author Felix Jost
 */
public class WindowManagerImpl extends BasicManager implements WindowManager {
	
	private List<WindowBackOfficeImpl> wbos = new ArrayList<WindowBackOfficeImpl>();
	
	// experimental!
	
	private GlobalSettings globalSettings;
	private boolean ajaxEnabled = false;
	
	private boolean forScreenReader = false;
	private boolean showDebugInfo = false;
	private boolean idDivsForced = false;

	
	private int fontSize = 100; // default width

	private int wboId = 0;
	
	private InterceptHandler screenreader_interceptHandler = null;
	
	private Map<Class,ComponentRenderer> screenReaderRenderers = new HashMap<Class,ComponentRenderer>();

	private PopupBrowserWindowControllerCreator pbwcc;
	
	
	
	// global urls for mapped path e.g. for css, js and so on; for all users!
	private static Map<String,String> mappedPaths = new HashMap<String, String>();
	
	
	public WindowManagerImpl() {
		
		this.pbwcc = (PopupBrowserWindowControllerCreator) 
			CoreSpringFactory.getBean(PopupBrowserWindowControllerCreator.class);
		
		
		
		final AJAXFlags aflags = new AJAXFlags(this);
		globalSettings = new GlobalSettings() {

			public int getFontSize() {
				return WindowManagerImpl.this.getFontSize();
			}

			public AJAXFlags getAjaxFlags() {
				return aflags;
			}
			
			public ComponentRenderer getComponentRendererFor(Component source) {
				return WindowManagerImpl.this.getComponentRendererFor(source);
			}

			public boolean isIdDivsForced() {
				return WindowManagerImpl.this.isIdDivsForced();
			}
		};
			
		// add special classes for screenreader rendering
		//FIXME:FG: add support for multiple renderers (screenreader / iphone)
		// 1) move to a config file
		// 2) don't hardcode the theme (allow also iphone theme)
		// 3) check which special renderer are really needed
		//screenReaderRenderers.put(MenuTree.class, new MenuTreeScreenreaderRenderer());
		screenReaderRenderers.put(VelocityContainer.class, new VelocityContainerRenderer("screenreader"));	
		//screenReaderRenderers.put(TabbedPane.class, new TabbedPaneScreenreaderRenderer());
	}
	
	public void setForScreenReader(boolean forScreenReader) {
		this.forScreenReader = forScreenReader;
		if (forScreenReader) {
			screenreader_interceptHandler = new InterceptHandler() {

				public InterceptHandlerInstance createInterceptHandlerInstance() {
					return new ScreenReaderHandlerInstance();		
				}};
		} else {
			screenreader_interceptHandler = null;
		}
	}
	

	
	
	
	/**
	 * @param source
	 * @return
	 */
	protected ComponentRenderer getComponentRendererFor(Component source) {
		ComponentRenderer compRenderer;
		// to do: let "source - renderer pairs" be configured via spring for each mode like
		// default, accessibility, printing
		if (isForScreenReader()) {
			ComponentRenderer cr = screenReaderRenderers.get(source.getClass());
			if (cr != null) {
				compRenderer = cr;
			} else {
				compRenderer = source.getHTMLRendererSingleton();
			}
		} else {
			compRenderer = source.getHTMLRendererSingleton();
		}
		return compRenderer;
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.WindowManager#getGlobalSettings()
	 */
	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}
	
	
	public void setAjaxWanted(UserRequest ureq, boolean enabled) {
		boolean globalOk = Settings.isAjaxGloballyOn();
		boolean browserOk = !Settings.isBrowserAjaxBlacklisted(ureq);
		boolean all = globalOk && browserOk && enabled;
		setAjaxEnabled(all);
	}

	/**
	 * @return Returns the ajaxEnabled.
	 */
	public boolean isAjaxEnabled() {
		return ajaxEnabled;
	}

	/**
	 * @see org.olat.core.gui.WindowManager#getMapPathFor(java.lang.Class)
	 */
	public String getMapPathFor(final Class baseClass) {
		return ClassPathStaticDispatcher.getInstance().getMapperBasePath(baseClass);
	}
	
	/**
	 * @see org.olat.core.gui.WindowManager#createMediaResourceFor(java.lang.Class, java.lang.String)
	 */
	public MediaResource createMediaResourceFor(final Class baseClass, String relPath) {
		return ClassPathStaticDispatcher.getInstance().createClassPathStaticFileMediaResourceFor(baseClass, relPath);
	}

	/**
	 * <b>Only use for debug mode!!!<b><br>
	 * use setAjaxWanted(ureq) instead
	 * 
	 * sets the ajax on/off flag, -ignoring the browser-
	 * @param enabled if true, ajax is on, renderers can render their links to post to the background frame and so on
	 */
	public void setAjaxEnabled(boolean enabled) {
		this.ajaxEnabled  = enabled;
		for (WindowBackOfficeImpl wboImpl : wbos) {
			wboImpl.setAjaxEnabled(enabled);
		}			
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.WindowManager#setHighLightingEnabled(boolean)
	 */
	public void setHighLightingEnabled(boolean enabled) {
		for (WindowBackOfficeImpl wboImpl : wbos) {
			wboImpl.setHighLightingEnabled(enabled);
		}			
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.WindowManager#setShowJSON(boolean)
	 */
	public void setShowJSON(boolean enabled) {
		for (WindowBackOfficeImpl wboImpl : wbos) {
			wboImpl.setShowJSON(enabled);
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

	public void setFontSize(int fontSize) {
		
		this.fontSize = fontSize;
	}

	public boolean isForScreenReader() {
		return forScreenReader;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.WindowManager#createWindowBackOffice(java.lang.String, org.olat.core.gui.control.ChiefController)
	 */
	public WindowBackOffice createWindowBackOffice(String windowName, ChiefController owner) {
		WindowBackOfficeImpl wbo = new WindowBackOfficeImpl(this, windowName, owner, wboId++);
		wbos.add(wbo);
		return wbo;
	}

	/**
	 * 
	 */
	public void dispose() {
		for (WindowBackOfficeImpl wboImpl : wbos) {
			wboImpl.dispose();
		}		
	}

	protected InterceptHandler getScreenreader_interceptHandler() {
		return screenreader_interceptHandler;
	}

	protected boolean isShowDebugInfo() {
		return showDebugInfo;
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.WindowManager#createContentableChiefController(org.olat.core.gui.UserRequest)
	 */
	public ContentableChiefController createContentableChiefController(UserRequest ureq) {
		return new BaseChiefController(ureq);
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.WindowManager#createNewPopupBrowserWindowFor(org.olat.core.gui.UserRequest, org.olat.core.gui.control.creator.ControllerCreator, boolean)
	 */
	public PopupBrowserWindow createNewPopupBrowserWindowFor(UserRequest ureq, ControllerCreator contentControllerCreator) {
		BaseChiefController cc = new BaseChiefController(ureq);
		cc.addBodyCssClass("b_body_popup");
		//supports the open(ureq) method
		PopupBrowserWindowController sbasec = pbwcc.createNewPopupBrowserController(ureq, cc.getWindowControl(), contentControllerCreator);
		//the content controller for the popupwindow is generated and set
		//at the moment the open method is called!!
		cc.setContentController(true, sbasec);
		return sbasec;
	}
	
	//fxdiff
	public PopupBrowserWindow createNewUnauthenticatedPopupWindowFor(UserRequest ureq, ControllerCreator contentControllerCreator) {
		BaseChiefController cc = new BaseChiefController(ureq);
		cc.addBodyCssClass("b_body_popup");
		//supports the open(ureq) method
		PopupBrowserWindowController sbasec = pbwcc.createNewUnauthenticatedPopupWindowController(ureq, cc.getWindowControl(), contentControllerCreator);
		//the content controller for the popupwindow is generated and set
		//at the moment the open method is called!!
		cc.setContentController(true, sbasec);
		return sbasec;
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
