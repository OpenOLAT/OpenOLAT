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
import java.util.concurrent.CopyOnWriteArrayList;

import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.JSAndCSSAdderImpl;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.guistack.GuiStackNiceImpl;
import org.olat.core.gui.control.guistack.GuiStackSimpleImpl;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.gui.control.pushpoll.WindowCommand;
import org.olat.core.gui.control.state.ExtendedControllerState;
import org.olat.core.gui.control.state.GUIPath;
import org.olat.core.gui.dev.controller.DevelopmentController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.intercept.InterceptHandler;
import org.olat.core.gui.render.intercept.InterceptHandlerInstance;
import org.olat.core.gui.render.intercept.debug.GuiDebugDispatcherController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
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

	private OLog log = Tracing.createLoggerFor(WindowBackOfficeImpl.class);
	
	private final WindowManagerImpl winmgrImpl;
	private Window window;
	private ChiefController windowOwner;
	
	private InterceptHandler linkedInterceptHandler;
	// not private to avoid synthetic accessor
	InterceptHandler debug_interceptHandler = null;
	InterceptHandler inlineTranslation_interceptHandler = null;
	private AjaxController ajaxC;
	private GuiDebugDispatcherController guidebugC;
	private InlineTranslationInterceptHandlerController inlineTranslationC;
	
	private BusinessControl bookmarkBusinessControl; 
		
	private String iframeName;
	
	private Map<String, Object> data = new HashMap<String, Object>(); // request-transient render-related data
	
	private List<GUIPath> guipathentries = new ArrayList<GUIPath>();
	private int curGPpos = -1; // the position of the current guipath in the list: the one that was just dispatched (=the current state), or just restored by "backing"
	private WindowControlInfo rootWCI; // the topmost windowcontrolinfo, typically the one belonging to the windowcontrol which belongs to a basechiefcontroller
	
	private transient List<GenericEventListener> cycleListeners = new CopyOnWriteArrayList<GenericEventListener>();
	private WindowControlInfo newWci = null;
	
	private BackHandler backHandler = null;
	
	/**
	 * 
	 */
	WindowBackOfficeImpl(final WindowManagerImpl winmgrImpl, String windowName, ChiefController windowOwner, int wboId) {
		this.winmgrImpl = winmgrImpl;
		this.windowOwner = windowOwner;
		this.iframeName = "oaa"+wboId;
		window = new Window(windowName, this);
		
		
		// TODO make simpler, we do only need to support one intercept handler at a time!
		linkedInterceptHandler = new InterceptHandler() {
			public InterceptHandlerInstance createInterceptHandlerInstance() {
				InterceptHandler debugH = debug_interceptHandler;
				InterceptHandler screenReaderH = winmgrImpl.getScreenreader_interceptHandler();
				InterceptHandler inlineTranslationH = inlineTranslation_interceptHandler;
				
				final InterceptHandlerInstance debugI = debugH == null? null: debugH.createInterceptHandlerInstance(); 
				final InterceptHandlerInstance screenReaderI = screenReaderH == null? null: screenReaderH.createInterceptHandlerInstance();
				final InterceptHandlerInstance inlineTranslationI = (inlineTranslationH == null ? null : inlineTranslationH.createInterceptHandlerInstance());
				
				return new InterceptHandlerInstance() {

					public ComponentRenderer createInterceptComponentRenderer(ComponentRenderer originalRenderer) {
						ComponentRenderer toUse = originalRenderer;
						if (screenReaderI != null) {
							toUse = screenReaderI.createInterceptComponentRenderer(toUse);
						}
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
	public Window getWindow() {
		return window;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#createDevelopmentController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller createDevelopmentController(UserRequest ureq, WindowControl windowControl) {
		DevelopmentController dc = new DevelopmentController(ureq, windowControl,this);
		return dc;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#getGlobalSettings()
	 */
	public GlobalSettings getGlobalSettings() {
		return winmgrImpl.getGlobalSettings();
	}

	/**
	 * @return
	 */
	public JSAndCSSAdderImpl createJSAndCSSAdder() {
		JSAndCSSAdderImpl jcImpl = new JSAndCSSAdderImpl(this);
		return jcImpl;
	}

	/**
	 * @see org.olat.core.gui.control.WindowBackOffice#sendCommandTo(org.olat.core.gui.control.winmgr.Command)
	 */
	public void sendCommandTo(Command wco) {
		ajaxC.sendCommandTo(new WindowCommand(this,wco));
	}

	/**
	 * @param wrapHTML
	 * @return
	 */
	public MediaResource extractCommands(boolean wrapHTML) {
		return ajaxC.extractMediaResource(wrapHTML);
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
	public Controller createInlineTranslationDispatcherController(UserRequest ureq, WindowControl windowControl) {
		if (inlineTranslationC != null) throw new AssertException("Can't set the inline translation dispatcher twice!", null);
		inlineTranslationC = I18nUIFactory.createInlineTranslationIntercepHandlerController(ureq, windowControl);
		this.inlineTranslation_interceptHandler  = inlineTranslationC;
		return inlineTranslationC;
	}

	public Controller createAJAXController(UserRequest ureq) {
		boolean ajaxEnabled = winmgrImpl.isAjaxEnabled();
		ajaxC = new AjaxController(ureq, this, ajaxEnabled, iframeName);
		return ajaxC;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#isDebuging()
	 */
	public boolean isDebuging() {
		return Settings.isDebuging();
	}

	public WindowManagerImpl getWinmgrImpl() {
		return winmgrImpl;
	}

	/**
	 * 
	 */
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
	 * @param enabled
	 */
	public void setHighLightingEnabled(boolean enabled) {
		if (ajaxC != null) ajaxC.setHighLightingEnabled(enabled);
	}

	/**
	 * @param enabled
	 */
	public void setShowJSON(boolean enabled) {
		if (ajaxC != null) ajaxC.setShowJSON(enabled);
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
	public WindowManager getWindowManager() {
		return winmgrImpl;
	}

	public void informControllerDispatched(WindowControl wControl, Controller controller, Component source, Event event) {
		
		// add the gui path to the gui-path-history, if there has been a change at all (otherwise that transition is ignored;
		// when the user presses the back-button of the browser, he/she expects that some change on the screen happens)
		
		// VALIDATE events are special events which are to be ignored here - they are no user-dispatched events, but a result thereof.
		if (event == ComponentUtil.VALIDATE_EVENT) return;
		
		GUIPath guiPath = new GUIPathImpl();
		WindowControlInfo curwcI; 
		if (newWci == null) {
			curwcI = wControl.getWindowControlInfo();
		} else {
			// take the special, supplied info
			curwcI = newWci;
			newWci = null;
		}
			
		WindowControlInfo prev = null;
		do {
			ExtendedControllerState state = curwcI.getExtendedControllerState();
			guiPath.addStateInfo(state);
			prev = curwcI;
		} while ((curwcI = curwcI.getParentWindowControlInfo()) != null);
		// the prev is now the windowcontrol with a null parent = the root 
		// -> remember for later back-button handling/traversing
		
		if (rootWCI == null) {
			rootWCI = prev;
		} else if (rootWCI != prev) {
			//REVIEW:2008-02-28 PB: this happens for example in DOCKCONTROLLER in popuwindow
			//REVIEW:2008-02-28 PB: gets important for BackHandling
			log.warn("WindowControlInfo different root now?? for Controller:"+controller.getClass().getCanonicalName());
			//throw new AssertException("different root now??");
		}
		// clear old forward gui paths if needed
		int esize = guipathentries.size();
		if (curGPpos < esize-1) { // we are not rightmost,
			// that is we have traversed back in history -> clear forward history of gui paths, because those are dumped by the browser. (concept of only having one forward path)
			guipathentries.subList(curGPpos+1, esize).clear();
			guipathentries.add(guiPath);
			//System.out.println("first guipath on click after back:"+guiPath.toString());
			curGPpos++;
		} else { // curGP >= esize-1
			if (esize > 0) {
				// only add the new guipath if it is not the same as the previous 
				// (meaning that no state change occured and thus should not be remembered - 
				// we don't user to experience "nothing" when they press the browser back button)
				GUIPath latestGP = guipathentries.get(esize-1);
				if (!guiPath.isSame(latestGP)) {
					guipathentries.add(guiPath);
					//System.out.println("new guipath:"+guiPath.toString());
					curGPpos++;
				} else {
					//System.out.println("same guipath");
					// else ignore in history, because there is no (noticed) change
				}
			} else {
				// first entry: add
				guipathentries.add(guiPath);
				//System.out.println("first new guipath:"+guiPath.toString());
				curGPpos++;
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#informControllerCreated(org.olat.core.gui.control.DefaultController)
	 */
	public void informControllerCreated(WindowControl wControl, Controller controller) {
		//System.out.println(controller);
	}
	
	/**
	 * diff == 0 -> reload (->ignore, so it will cause a simple rerendering)<br>
	 * diff < 0  -> browser-back<br>
	 * diff > 0  -> browser-forward<br>
	 * @param diff
	 */
	public void browserBackOrForward(UserRequest ureq, int diff) {
		// 1. check if there is a controller which registered for a custom behaviour
		// e.g. a iqruncontroller (= a test or a survey, which should ignore or block the back/forward button)
		// the controller should <init>: acquireBackHandling(), and upon dispose: releaseBackHandling()
		if (backHandler != null) {
			backHandler.browserBackOrForward(ureq, diff);
		} else {
			// no special handler: back means we revert to the latest gui path.
			//
			// if back: check in guipathhistory: fetch latest guipath -> reactivate.
			// the reactivate is done by traversing from the root windowcontrolinfo to its children.
			// the following algorithm is used to determine which child matches the current path of the guipath:
			// a guipathentry consists of
			// a) the classname of the owning controller
			// b) the hashcode of the owning controller
			// c) the container-path of the initial-component of the owning controller 
			
			// for each windowcontrolinfo, starting at the root entry:
			// 1. take the current guipathentry (and inc the pos)
			// 2. take the current windowcontrolinfo and apply the guipathentry to it, meaning the owning controller will adjust its
			//    state accordingly if needed. the state change is specific per controller and must be programmed.
			//    the notification is done via ...
			// 3. search the correct childwindowcontrolinfo where the next guipathentry is applied:
			//    if (classname and hashcode match) take this child (=same instance), else if (classname matches) then
			//    take this if only one child matches. if there are at least two children with the same classname that matches,
			//    then (a rather rare case) verify which has the matching container-parent-hierarchy.
			//	  if no child is found, then the current controller did not adjust its state correctly (bug or not implemented) -> stop
			// 4. make the matching child windowcontrolinfo current and proceed with step 1.
	
			if (rootWCI == null) {
				// this may happen -only- right after the login when the home page appears and as the next click the browser-reload button is pressed.
				// to avoid this rare case red screen, we return silently.
				if (diff == 0) { // reload
					return;					
				} else {
					throw new AssertException("rootWCI was null: never dispatched, but pressed back or forward??");
				}
			}
			int esize = guipathentries.size();
			
			// BACK case
			if (diff < 0) { 			
				if (esize < 1) {
					//throw new AssertException("back, but no entries??");
					// this case can happen when the user presses reload on the first page after the login (olat home page)
					return;
				}
				// get the guipath to restore our state : the latest one is the current one which needs to be backed
				GUIPath guiPath = guipathentries.get(curGPpos);
				curGPpos--;
				//System.out.println("backing using guipath:"+guiPath);
				adjustState(true, rootWCI, guiPath, ureq);			
			} else if (diff > 0) { // handle FORWARD case
				// ignore when we have reached then end of the history. 
				// when e.g. a user traverses back in history using one click, but traverses more than one step, then olat can only detect "back", but
				// not how much (it could be done, but 99,9% the use case I guess to be the "normal" back, that is, hitting the browser back button (maybe repeatedly)).
				// so the browser may offer the "forward" button, even though we cannot forward.
				if (curGPpos == esize-1) return;
				GUIPath guiPath = guipathentries.get(curGPpos+1);
				//System.out.println("forwarding using guipath:"+guiPath);
				curGPpos++;
				adjustState(false, rootWCI, guiPath, ureq);	
			}
		}
	}
	
	private void adjustState(boolean back, WindowControlInfo curWCInfo, GUIPath guiPath, UserRequest ureq) {
		int ecnt = guiPath.getExtendedControllerStateCount();
		// traverse from the parent to the children and grandchildren
		for (int i = ecnt-1; i >= 0 && curWCInfo != null; i--) {
			ExtendedControllerState ecstate = guiPath.getExtendedControllerStateAt(i);
			curWCInfo.adjustControllerState(back, ecstate, ureq);
			if (i >= 1) { // for all but the leaf
				ExtendedControllerState extChildState = guiPath.getExtendedControllerStateAt(i-1);
				// changeing a controller's state means there are maybe child controllers disposed and others newly created
				curWCInfo = findMatchingChild(extChildState, curWCInfo);
				// can return null if no child is found (see comments for step 3 above)
				if (curWCInfo == null) return;
				// else found: proceed with this child and the next guiPath Entry
			}
		}
		// either no matching child was found or the bottom of the guiPath-chain was reached -> we are done -> return and later rerender gui
	}
		
	/**
	 * @param curWCI
	 * @return
	 */
	private WindowControlInfo findMatchingChild(ExtendedControllerState childState, WindowControlInfo curWCI) {
		List<WindowControlInfo> children = curWCI.getChildren();
		if (children == null) return null;
		List<WindowControlInfo> classMatches = new ArrayList<WindowControlInfo>(2);
		
		String contClassName = childState.getControllerClassName();
		long contId = childState.getControllerUniqueId();
		
		int size = children.size();
		for (int i = 0; i < size; i++) {
			WindowControlInfo wciChild = children.get(i);
			if (!wciChild.isControllerDisposed()) {
				ExtendedControllerState estate = wciChild.getExtendedControllerState();
				if (estate.getControllerUniqueId() == contId) {
					// same instance is still there, we have the match
					return wciChild;
				} else {
					// test on matching classnames
					if (contClassName.equals(estate.getControllerClassName())) {
						// add to potentials
						classMatches.add(wciChild);
					}
				}
			}
		}
		
		int matchesCnt = classMatches.size();
		if (matchesCnt == 0) {
			// no instance match and also no class name match: the parent controller did not exactly restore its state -> stop here
			return null;
		} else if (matchesCnt == 1) {
			// since exactly one match, there is no ambiguity -> return the match
			return classMatches.get(0);
		} else {
			// classMatches > 1 -> ambiguity
			// brasato:: todo now compare the controller initialcomponents parent-hierarchy
			throw new AssertException("todo!!");
		}
	}

	public String getBusinessControlPath() {
		return bookmarkBusinessControl == null? null : bookmarkBusinessControl.getAsString();
	}

	
	/**
	 * @return
	 */
	public String getIframeTargetName() {
		return iframeName;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#createGuiStack(org.olat.core.gui.components.Component)
	 */
	public GuiStack createGuiStack(Component initialComponent) {
		// only needed for a on-screen mode change = only for demo purposes.
		// normally the following code is appropriate:

		GuiStack gsh;
		if (winmgrImpl.isForScreenReader()) {
			gsh = new GuiStackSimpleImpl(initialComponent);
		} else {
			gsh = new GuiStackNiceImpl(this, initialComponent);			
		}
		return gsh;

		/*final GuiStack deleg = currentGuiStack;
		GuiStack switchable = new GuiStack() {
			public void pushModalDialog(Component content) {
				deleg.pushModalDialog(content);
			}

			public void pushContent(Component newContent) {
				deleg.pushContent(newContent);
			}

			public void popContent() {
				deleg.popContent();
			}

			public Panel getPanel() {
				return deleg.getPanel();
			}

			public Panel getModalPanel() {
				return deleg.getModalPanel();
			}};
		
		GuiStack gsh;
		if (winmgrImpl.isForScreenReader()) {
			gsh = new GuiStackSimpleImpl(initialComponent);
		} else {
			gsh = new GuiStackNiceImpl(initialComponent);			
		}
		return gsh;*/
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#invokeLater(java.lang.Runnable)
	 */
	public void invokeLater(Runnable runnable) {
		// brasato:::: verify that this is now as it should be.
		// improve by handling those tasks after a ongoing dispatch/render is finished,
		// or when handleDirties is called.
		// the current solution below blocks the async caller - e.g. a user firing an eventbus message may then wait for all users to complete their
		// rendering process - normally way below 0.1 sec, but if we have 100 users which happen to just being rendered when the notifications takes place.
		// -> 10 seconds delay! this is not very probable but possible.
		
		synchronized(window) {//cluster_ok
			runnable.run();
		}
	}

	public void fireCycleEvent(Event cycleEvent) {
		for (GenericEventListener gel : cycleListeners) {
			gel.event(cycleEvent);
		}
		if (cycleEvent == Window.AFTER_VALIDATING) {
			// clear the added data for this cycle
			data.clear();
		}
		
	}

	public void addCycleListener(GenericEventListener gel) {
		cycleListeners.add(gel);
	}
	
	public void removeCycleListener(GenericEventListener gel) {
		// Since we use a CopyOnWriteArrayList it is save to remove an event
		// listener even when we are in the fireCycleEvent() method at the same time
		cycleListeners.remove(gel);
	}

	public Object getData(String key) {
		return data.get(key);
	}

	public void putData(String key, Object value) {
		data.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#adjustGuiPathCenter(org.olat.core.gui.control.WindowBackOffice)
	 */
	public void adjustGuiPathCenter(WindowControlInfo wci) {
		newWci  = wci;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#acquireBackHandling(org.olat.core.gui.control.winmgr.BackHandler)
	 */
	public void acquireBackHandling(BackHandler backHandler) {
		this.backHandler = backHandler;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowBackOffice#releaseBackHandling(org.olat.core.gui.control.winmgr.BackHandler)
	 */
	public void releaseBackHandling(BackHandler backHandler) {
		/* FIXME:pb:2008-08-26: OLAT-3411 encountered on 6.0.3 release, must ne enabled again with BACK
		if (this.backHandler == null || this.backHandler != backHandler) throw new AssertException("wrong acquire/release order. backhandler:"+backHandler); 
		this.backHandler = null;
		*/
	}

}
