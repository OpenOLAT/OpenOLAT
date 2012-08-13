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
package org.olat.core.commons.fullWebApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.ChiefControllerMessageEvent;
import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.commons.fullWebApp.util.GlobalStickyMessage;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.GUIMessage;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.htmlheader.jscss.CustomJSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.OncePanel;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.WindowControlInfoImpl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabImpl;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.gui.control.navigation.BornSiteInstance;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.control.util.ZIndexWrapper;
import org.olat.core.gui.themes.Theme;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryModule;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.id.context.StateEntry;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * The BaseFullWebappController defines the outer most part of the main layout
 * <P>
 * Initial Date: 20.07.2007 <br>
 * 
 * @author patrickb, Felix Jost, Florian Gnägi
 */
public class BaseFullWebappController extends BasicController implements GenericEventListener {
	private static final String PRESENTED_AFTER_LOGIN_WORKFLOW = "presentedAfterLoginWorkflow";
	public static final String CURRENT_CUSTOM_CSS_KEY = "currentcustomcss";
	
	// STARTED
	private GuiStack currentGuiStack;
	private Panel main;
	private Panel modalPanel;
	private GUIMessage guiMessage;
	private OncePanel guimsgPanel;
	private Panel guimsgHolder;
	private Panel currentMsgHolder;
	private VelocityContainer guimsgVc;

	private VelocityContainer mainVc, navVc;

	// NEW FROM FullChiefController
	private Controller headerCtr, topnavCtr, footerCtr;
	private SiteInstance curSite;
	private DTabImpl curDTab;
	private SiteInstance prevSite;

	// the dynamic tabs list
	private List<DTab> dtabs;
	private List<String> dtabsLinkNames;
	private List<Controller> dtabsControllers;
	// used as link id which is load url safe (e.g. replayable
	private int dtabCreateCounter = 0;
	// the sites list
	private List<SiteInstance> sites;
	private Map<SiteInstance, BornSiteInstance> siteToBornSite = new HashMap<SiteInstance, BornSiteInstance>();
	private int navLinkCounter = 1;
	//fxdiff BAKS-7 Resume function
	private Map<SiteInstance,HistoryPoint> siteToBusinessPath = new HashMap<SiteInstance,HistoryPoint>();

	//
	private BaseFullWebappControllerParts baseFullWebappControllerParts;
	protected Controller contentCtrl;
	private Panel initialPanel;
	private DTabs myDTabsImpl;
	private static Integer MAX_TAB;
	
	public BaseFullWebappController(UserRequest ureq, WindowControl ouisc_wControl,
			BaseFullWebappControllerParts baseFullWebappControllerParts) {
		// only-use-in-super-call, since we define our own
		super(ureq, null);

		this.baseFullWebappControllerParts = baseFullWebappControllerParts;

		guiMessage = new GUIMessage();
		guimsgPanel = new OncePanel("guimsgPanel");

		// define the new windowcontrol
		final WindowControl origWCo = ouisc_wControl;

		WindowControl myWControl = new WindowControl() {
			private WindowControlInfo wci;

			{
				wci = new WindowControlInfoImpl(BaseFullWebappController.this, origWCo.getWindowControlInfo());
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#pushToMainArea(org.olat.core.gui.components.Component)
			 */
			@SuppressWarnings("synthetic-access")
			public void pushToMainArea(Component newMainArea) {
				currentGuiStack.pushContent(newMainArea);
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#pushAsModalDialog(java.lang.String,
			 *      org.olat.core.gui.components.Component)
			 */

			@SuppressWarnings("synthetic-access")
			public void pushAsModalDialog(Component newModalDialog) {
				currentGuiStack.pushModalDialog(newModalDialog);
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#pop()
			 */
			@SuppressWarnings("synthetic-access")
			public void pop() {
				// reactivate latest dialog from stack, dumping current one
				currentGuiStack.popContent();
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#setInfo(java.lang.String)
			 */
			@SuppressWarnings("synthetic-access")
			public void setInfo(String info) {
				guiMessage.setInfo(info);
				guimsgPanel.setContent(guimsgVc);

				// setInfo is called input guimsgPanel into the correct place
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#setError(java.lang.String)
			 */
			@SuppressWarnings("synthetic-access")
			public void setError(String error) {
				guiMessage.setError(error);
				guimsgPanel.setContent(guimsgVc);
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#setWarning(java.lang.String)
			 */
			@SuppressWarnings("synthetic-access")
			public void setWarning(String warning) {
				guiMessage.setWarn(warning);
				guimsgPanel.setContent(guimsgVc);
			}

			public WindowControlInfo getWindowControlInfo() {
				return wci;
			}

			public void makeFlat() {
				throw new AssertException("should never be called!");
			}

			public BusinessControl getBusinessControl() {
				return origWCo.getBusinessControl();
			}

			public WindowBackOffice getWindowBackOffice() {
				return origWCo.getWindowBackOffice();
			}

		};
		overrideWindowControl(myWControl);


		/*
		 * BaseFullWebappController provides access to Dynamic Tabs
		 * on the same window and not on all Windows! 
		 * TODO:pb discuss with HJZ multi window concept.
		 */
		
		// detach DTabs implementation from the controller - DTabs may be fetched from the window and locked on (synchronized access).
		// if this is controller the controller is locked instead of only the DTabs part.
		myDTabsImpl = new DTabs() {

			@Override
			public void activate(UserRequest ureq, DTab dTab, List<ContextEntry> entries) {
				BaseFullWebappController.this.activate(ureq, dTab, null, entries);
			}

			@Override
			public void activateStatic(UserRequest ureq, String className, List<ContextEntry> entries) {
				BaseFullWebappController.this.activateStatic(ureq, className, null, entries);
			}

			public void addDTab(DTab dt) {
				BaseFullWebappController.this.addDTab(dt);
			}
			//fxdiff BAKS-7 Resume function
			public DTab createDTab(OLATResourceable ores, String title) {
				return BaseFullWebappController.this.createDTab(ores, null, title);
			}
			
			public DTab createDTab(OLATResourceable ores, OLATResourceable initialOres, String title) {
				return BaseFullWebappController.this.createDTab(ores, initialOres, title);
			}

			public DTab getDTab(OLATResourceable ores) {
				return BaseFullWebappController.this.getDTab(ores);
			}

			public void removeDTab(DTab dt) {
				BaseFullWebappController.this.removeDTab(dt);
			}
			
		};
		
		
		
		Window myWindow = myWControl.getWindowBackOffice().getWindow();
		myWindow.setAttribute("DTabs", myDTabsImpl);
		//REVIEW:PB remove if back support is desired
		myWindow.addListener(this);//to be able to report BACK / FORWARD / RELOAD
		/*
		 * use getAttribute on i.e. Windows(ureq).getWindow(ureq) to retrieve this "service"
		 */

		
		
		/*
		 * does all initialisation, moved to method because of possibility to react
		 * on LanguageChangeEvents -> resets and rebuilds footer, header, topnav, sites, content etc.
		 */
		initialize(ureq);
		
		initialPanel = putInitialPanel(mainVc);
		// ------ all the frame preparation is finished ----
		
		if (CoreSpringFactory.containsBean("fullWebApp.AfterLoginInterceptionControllerCreator")){
        		// present an overlay with configured afterlogin-controllers or nothing if none configured.
        		// presented only once per session.
        		Boolean alreadySeen = ((Boolean)ureq.getUserSession().getEntry(PRESENTED_AFTER_LOGIN_WORKFLOW));
        		if (ureq.getUserSession().isAuthenticated() && alreadySeen == null) {
        			Controller aftLHookCtr = ((ControllerCreator) CoreSpringFactory.getBean("fullWebApp.AfterLoginInterceptionControllerCreator")).createController(ureq, getWindowControl());
        			listenTo(aftLHookCtr);
        			aftLHookCtr.getInitialComponent();
        			ureq.getUserSession().putEntry(PRESENTED_AFTER_LOGIN_WORKFLOW, Boolean.TRUE);
        		}
		}

		/*
		 * register for cycle event to be able to adjust the guimessage place
		 */
		getWindowControl().getWindowBackOffice().addCycleListener(this);
		/*
		 * register for locale change events -> 
		 */
		//move to a i18nModule? languageManger? languageChooserController?
		OLATResourceable wrappedLocale = OresHelper.createOLATResourceableType(Locale.class);
		ureq.getUserSession().getSingleUserEventCenter().registerFor(this, getIdentity(), wrappedLocale);
		/*
		 * register for global sticky message changed events
		 */
		GlobalStickyMessage.registerForGlobalStickyMessage(this, ureq.getIdentity());

	}

	private void initialize(UserRequest ureq) {
		mainVc = createVelocityContainer("fullwebapplayout");
		// use separate container for navigation to prevent full page refresh in ajax mode on site change
		// nav is not a controller part because it is a fundamental part of the BaseFullWebAppConroller.
		navVc = createVelocityContainer("nav");
		mainVc.put("navComponent", navVc);

		// GUI messages
		guimsgVc = createVelocityContainer("guimsg");
		// FIXME fg: create controller that uses monolog controller after monolog
		// controller refactoring. is all the same...

		guimsgVc.contextPut("guiMessage", guiMessage);
		guimsgHolder = new Panel("guimsgholder");
		guimsgHolder.setContent(guimsgPanel);
		currentMsgHolder = guimsgHolder;

		mainVc.put("guimessage", guimsgHolder);
		
		
		dtabs = new ArrayList<DTab>();
		dtabsLinkNames = new ArrayList<String>();
		dtabsControllers = new ArrayList<Controller>();

		// -- sites -- by definition the first site is activated at the beginning
		sites = baseFullWebappControllerParts.getSiteInstances(ureq, getWindowControl());
		if (sites != null && sites.size() == 0) {
			sites = null;
		}
		// either sites is null or contains at least one SiteInstance.
		if (sites != null) {
			// create the links for the sites
			for (Iterator<SiteInstance> iterator = sites.iterator(); iterator.hasNext();) {
				SiteInstance si = iterator.next();
				Link link = LinkFactory.createCustomLink("t" + navLinkCounter, "t", "", Link.NONTRANSLATED, navVc, this);
				link.setCustomDisplayText(si.getNavElement().getTitle());
				link.setTitle(si.getNavElement().getDescription());
				link.setUserObject(si);
				Character accessKey = si.getNavElement().getAccessKey();
				if (accessKey != null && StringHelper.containsNonWhitespace(accessKey.toString())) {
					link.setAccessKey(accessKey.toString());					
				}
				navLinkCounter++;
			}
		}

		navVc.contextPut("sites", sites);
		navVc.contextPut("dtabs", dtabs);
		navVc.contextPut("dtabsLinkNames", dtabsLinkNames);
		navVc.contextPut("tabhelper", this);

		// ----------- header, optional (e.g. for logo, advertising )
		headerCtr = baseFullWebappControllerParts.createHeaderController(ureq, getWindowControl());
		if (headerCtr != null) {
			listenTo(headerCtr); // cleanup on dispose
			mainVc.put("headerComponent", headerCtr.getInitialComponent());
		}

		// ----------- topnav, optional (e.g. for imprint, logout)
		topnavCtr = baseFullWebappControllerParts.createTopNavController(ureq, getWindowControl());
		if (topnavCtr != null) {
			listenTo(topnavCtr); // cleanup on dispose
			mainVc.put("topnavComponent", topnavCtr.getInitialComponent());
		}

		// ----------- nav, optional (e.g. site navigation with tabs)
		// TODO fg: refactor to its own component // REVIEW:(pb) should then go also
		// into ..Parts?!

		// panel for modal overlays, placed right after the olat-header-div
		modalPanel = new Panel("ccmodalpanel");
		mainVc.put("modalpanel", modalPanel);

		// ----------- main, mandatory (e.g. a LayoutMain3ColsController)
		// ------------------
		main = new Panel("main");
		mainVc.put("main", main);

		// ----------- footer, optional (e.g. for copyright, powered by)
		footerCtr = baseFullWebappControllerParts.createFooterController(ureq, getWindowControl());
		if (footerCtr != null) {
			listenTo(footerCtr); // cleanup on dispose
			mainVc.put("footerComponent", footerCtr.getInitialComponent());
		}
		
		
		contentCtrl = baseFullWebappControllerParts.getContentController(ureq, getWindowControl());
		if (contentCtrl != null) {
			listenTo(contentCtrl);
			GuiStack gs = getWindowControl().getWindowBackOffice().createGuiStack(contentCtrl.getInitialComponent());
			setGuiStack(gs);
			main.setContent(contentCtrl.getInitialComponent());
		}
		if (sites != null) {
			// ------ activate now main
			prevSite = sites.get(0);
			if (contentCtrl == null) {
				//activate site only if no content was set -> allow content before activation of default site.
				activateSite(prevSite, ureq, null, null);
				siteToBusinessPath.put(prevSite, ureq.getUserSession().getLastHistoryPoint());
			}
		}
		if (sites == null && contentCtrl == null) { 
		  // fxdiff: FXOLAT-190  RS if no sites displayed... show empty page instead
			main.setContent(TextFactory.createTextComponentFromString("empty", "", null, false, null));
		}

		// set maintenance message
		String stickyMessage = GlobalStickyMessage.getGlobalStickyMessage();
		this.mainVc.contextPut("hasStickyMessage", (stickyMessage == null ? Boolean.FALSE : Boolean.TRUE));					
		this.mainVc.contextPut("stickyMessage", stickyMessage);		
		
		addCustomThemeJS();
	}

	/**
	 * adds the custom js-code to the mainVc
	 * FXOLAT-310
	 */
	private void addCustomThemeJS() {
		Theme currentTheme = getWindowControl().getWindowBackOffice().getWindow().getGuiTheme();
		if (currentTheme.hasCustomJS()) {
			String fullPath = currentTheme.getFullPathToCustomJS();
			CustomJSComponent customJS = new CustomJSComponent("customThemejs", new String[] { fullPath });
			if (isLogDebugEnabled())
				logDebug("injecting custom javascript from current OLAT-Theme", fullPath);
			mainVc.put(customJS.getComponentName(), customJS);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			String mC = link.getCommand().substring(0, 1);
			if (mC.equals("t")) { // activate normal tab
				SiteInstance s = (SiteInstance) link.getUserObject();
				//fxdiff BAKS-7 Resume function
				if(prevSite != null) {
					siteToBusinessPath.put(prevSite, ureq.getUserSession().getLastHistoryPoint());
				}
				
				HistoryPoint point = null;
				if(siteToBusinessPath.containsKey(s)) {
					point = siteToBusinessPath.get(s);
				}
				activateSite(s, ureq, null, null);
				if(point != null) {
					BusinessControlFactory.getInstance().addToHistory(ureq, point);
				}

				HistoryPoint currentPoint = ureq.getUserSession().getLastHistoryPoint();
				siteToBusinessPath.put(s, currentPoint);
			} else if (mC.equals("a")) { // activate dyntab
				DTab dt = (DTab) link.getUserObject();
				doActivateDTab((DTabImpl) dt);
			} else if (mC.equals("c")) { // close dyntab
				DTab dt = (DTab) link.getUserObject();
				requestCloseTab(dt);
			}
		} else if (source == getWindowControl().getWindowBackOffice().getWindow()) {
			if (event == Window.OLDTIMESTAMPCALL) {
				if (GUIInterna.isLoadPerformanceMode()) {
					getLogger().info("loadtestMode RELOAD");
				} else {
					getLogger().info("RELOAD");
				}
				
				if(isBackEnabled(ureq)) {
					HistoryPoint point = ureq.getUserSession().popLastHistoryEntry();
					if(point != null) {
						back(ureq, point);
					}
				}
			}
		}
	}
	
	private boolean isBackEnabled(UserRequest ureq) {
		HistoryModule historyModule = (HistoryModule)CoreSpringFactory.getBean("historyModule");
		if(historyModule.isBackEnabled()) {
			Preferences prefs =  ureq.getUserSession().getGuiPreferences();
			Boolean be = (Boolean)prefs.get(WindowManager.class, "back-enabled");
			if (be != null) {
				return be.booleanValue();
			}
			else {
				return historyModule.isBackDefaultSetting();
			}
		}
		return false;
	}
	
	protected void back(UserRequest ureq, HistoryPoint cstate) {
		List<ContextEntry> entries = cstate.getEntries();
		if(entries.isEmpty()) return;
		
		entries = new ArrayList<ContextEntry>(entries);
		
		ContextEntry state = entries.remove(0);
		if(state == null) return;//no red screen for this
		
		OLATResourceable ores = state.getOLATResourceable();
		DTab dt = getDTab(ores);
		if(dt instanceof DTabImpl) {
			DTabImpl dti = (DTabImpl)dt;
			doActivateDTab(dti);
			if(dti.getController() instanceof Activateable2) {
				((Activateable2)dti.getController()).activate(ureq, entries, null);
			}
		} else if (dt == null) {
			StateEntry s = state.getTransientState();
			if(s instanceof StateSite && sites != null) {
				SiteInstance site = ((StateSite)s).getSite();
				for(SiteInstance savedSite:sites) {
					if(site.getClass().equals(savedSite.getClass())) {
						activateSite(savedSite, ureq, null, entries);
					}
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Controller source, Event event) {
		int tabIndex = dtabsControllers.indexOf(source);
		if (tabIndex > -1) {
			// Event comes from a controller in a dtab. Check if the controller is
			// finished and close the tab. Cancel and failed is interpreted as
			// finished.
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT || event == Event.FAILED_EVENT) {
				DTab tab = dtabs.get(tabIndex);
				removeDTab(tab);//disposes also tab and controllers
			}
		}

	}

	@Override
	protected void doDispose() {
		// deregister for chief global sticky messages events
		GlobalStickyMessage.deregisterForGlobalStickyMessage(this);
		// dispose sites and tabs
		
		if (dtabs != null) {
			for (DTab tab : dtabs) {
				tab.dispose();
			}
			for (BornSiteInstance bornSite : siteToBornSite.values()) {
				bornSite.dispose();
			}
			dtabs = null;
			dtabsControllers = null;
			sites = null;
			siteToBornSite = null;
		}
		//clear the DTabs Service
		Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
		myDTabsImpl = null;
		myWindow.setAttribute("DTabs", null);		

		getWindowControl().getWindowBackOffice().removeCycleListener(this);
	}

	private void setGuiStack(GuiStack guiStack) {
		currentGuiStack = guiStack;
		Panel guiStackPanel = currentGuiStack.getPanel();
		main.setContent(guiStackPanel);
		// place for modal dialogs, which are overlayd over the normal layout (using
		// css alpha blending)
		// maybe null if no current modal dialog -> clears the panel
		Panel modalStackP = currentGuiStack.getModalPanel();
		modalPanel.setContent(modalStackP);
	}

	// FROM FULLCHIEFCONTROLLER
	//fxdiff BAKS-7 Resume function
	private void activateSite(SiteInstance s, UserRequest ureq, String viewIdentifier, List<ContextEntry> entries) {
		BornSiteInstance bs = siteToBornSite.get(s);
		GuiStack gs;
		Controller resC;
		//PB//WindowControl site_wControl;
		if (bs != null && s != curSite) {
			// single - click -> fetch guistack from cache
			gs = bs.getGuiStackHandle();
			resC = bs.getController();
			//PB//site_wControl = bs.getWindowControl();
		} else {
			// bs == null (not yet in cache) || s == curSite
			// double click or not yet in cache.
			// dispose old controller
			if (bs != null) {
				// already in cache -> dispose old
				bs.getController().dispose();
			}
			// reset site and create new controller
			s.reset();
			resC = s.createController(ureq, getWindowControl());
			gs = getWindowControl().getWindowBackOffice().createGuiStack(resC.getInitialComponent());
			//PB//site_wControl = bwControl;			
			//PB//siteToBornSite.put(s, new BornSiteInstance(gs, resC, bwControl));
			siteToBornSite.put(s, new BornSiteInstance(gs, resC));
		}
		doActivateSite(s, gs);
		//fxdiff BAKS-7 Resume function
		if(resC instanceof Activateable2) {
			((Activateable2)resC).activate(ureq, entries, null);
		}
		//fxdiff perhaps has activation changed the gui stack and it need to be updated
		setGuiStack(gs);

		//set current BusPath for extraction in the TopNav Controller
		getWindowControl().getWindowBackOffice().getWindow().setAttribute("BUSPATH", getWindowControl());
	}

	private void doActivateSite(SiteInstance s, GuiStack gs) {
		removeCurrentCustomCSSFromView();

		curDTab = null;
		if (curSite != null) {
			prevSite = s;
		}
		curSite = s;
		setGuiStack(gs);
		navVc.contextPut("pageTitle", curSite.getNavElement().getTitle());
		navVc.setDirty(true);
		// add css for this site
		BornSiteInstance bs = siteToBornSite.get(s);
		if (bs != null)	addCurrentCustomCSSToView(bs.getCustomCSS());
	}

	private void doActivateDTab(DTabImpl dtabi) {
		
		//System.err.println(">>>>> dynamic site >>>>");
		getWindowControl().getWindowBackOffice().getWindow().setAttribute("BUSPATH", dtabi.getWindowControl());
		//System.err.println(busPath);
		//System.err.println("wControl:"+dtabi.getWindowControl());
		//System.err.println("<<<<< dynamic site <<<<");
		
		removeCurrentCustomCSSFromView();

		curDTab = dtabi;
		curSite = null;
		setGuiStack(dtabi.getGuiStackHandle());
		// set description as page title, getTitel() might contain trucated values
		//TODO:gs:a html escaping or removing should be done everywhere where text input fields are written to velocity. Best would be to not allow it in the input fields or escape it there
		navVc.contextPut("pageTitle", curDTab.getNavElement().getDescription());
		navVc.setDirty(true);
		// add css for this tab
		addCurrentCustomCSSToView(curDTab.getCustomCSS());
	}

	/**
	 * Remove the current custom css from the view
	 */
	protected void removeCurrentCustomCSSFromView() {
		Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
		CustomCSS currentCustomCSS = (CustomCSS) myWindow.getAttribute(CURRENT_CUSTOM_CSS_KEY);
		if (currentCustomCSS != null) {
			// remove css and js from view
			mainVc.remove(currentCustomCSS.getJSAndCSSComponent());
			myWindow.removeAttribute(CURRENT_CUSTOM_CSS_KEY);
		}
	}

	/**
	 * Add a custom css to the view and mark it as the curent custom CSS.
	 * 
	 * @param customCSS
	 */
	protected void addCurrentCustomCSSToView(CustomCSS customCSS) {
		if (customCSS == null) return;
		// The current CSS is stored as a window attribute so that is can be
		// accessed by the IFrameDisplayController
		Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
		myWindow.setAttribute(CURRENT_CUSTOM_CSS_KEY, customCSS);
		// add css component to view
		mainVc.put("jsAndCss", customCSS.getJSAndCSSComponent());		
		
		addCustomThemeJS();
	}
	
	/**
	 * @param pos
	 * @return the dtab at pos pos
	 */
	public DTab getDTabAt(int pos) {
		return dtabs.get(pos);
	}

	private void removeDTab(DTabImpl delt) {
		// remove from tab list and mapper table
		synchronized (dtabs) {//o_clusterOK dtabs are per user session only - user session is always in the same vm
			// make dtabs and dtabsControllers access synchronized
			int dtabIndex = dtabs.indexOf(delt);
			if(dtabIndex == -1){
				// OLAT-3343 :: although one session only is implemented, a user can 
				// open multiple "main windows" in different _browser tabs_.
				// closing one dtab in _browser tab one_ and then closing the same dtab
				// once again in the _browser tab two_ leads to the case where dtabIndex
				// is -1, e.g. not found causing the redscreen described in the issue.
				// TODO:2008-07-25: pb: define concept of "multi windowing" "main windowing"
				// and reconsider this place.
				//
				// NOTHING TO REMOVE, return
				return;
			}
			// Remove tab itself
			dtabs.remove(delt);
			dtabsLinkNames.remove(dtabIndex);
			Controller tabCtr = dtabsControllers.get(dtabIndex);
			dtabsControllers.remove(tabCtr);

			navVc.setDirty(true);
			// remove created links for dtab out of container
			navVc.remove(navVc.getComponent("a" + delt.hashCode()));
			navVc.remove(navVc.getComponent("ca" + delt.hashCode()));
			navVc.remove(navVc.getComponent("cp" + delt.hashCode()));
			if (delt == curDTab && prevSite != null) { // if we close the current tab -> return to the
				// latest selected static tab
				// pre: prevSite != null
				// activate previous chosen static site -> this site has already been
				// constructed and is thus in the cache
				SiteInstance si = prevSite;
				BornSiteInstance bs = siteToBornSite.get(si);
				// bs != null since clicked previously
				GuiStack gsh = bs.getGuiStackHandle();
				doActivateSite(si, gsh);
			} // else just remove the dtabs
			delt.dispose();//dispose tab and controllers in tab
		}
	}

	/**
	 * @param dt
	 */
	private void requestCloseTab(DTab dt) {
		final DTabImpl delt = (DTabImpl) dt;
		Controller c = delt.getController(); // FIXME:fj: test
		// vetoableclosecontroller
		if (c instanceof VetoableCloseController) {
			VetoableCloseController vcc = (VetoableCloseController) c;
			// rembember current dtab, and swap to the temporary one
			DTabImpl reTab = curDTab;
			doActivateDTab(delt);
			boolean immediateClose = vcc.requestForClose();
			if (!immediateClose) {
				return;
			} else {
				if (reTab != null) {
					doActivateDTab(reTab);
				}
				removeDTab(delt);
			}
		} else {
			removeDTab(delt);
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.DTabs#getDTab(org.olat.core.id.OLATResourceable
	 */
	public DTab getDTab(OLATResourceable ores) {
		for (Iterator<DTab> it_dts = dtabs.iterator(); it_dts.hasNext();) {
			DTab dtab = it_dts.next();
			if (OresHelper.equals(dtab.getOLATResourceable(), ores)) return dtab;
			//fxdiff BAKS-7 Resume function
			if (OresHelper.equals(dtab.getInitialOLATResourceable(), ores)) return dtab;
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.DTabs#createDTab(org.olat.core.id.OLATResourceable
	 *      java.lang.String)
	 */
	//fxdiff BAKS-7 Resume function
	public DTab createDTab(OLATResourceable ores, OLATResourceable repoOres, String title) {
		// fxdiff: read from props
		if (dtabs.size() >= getMaxTabs()) {
			getWindowControl().setError(translate("warn.tabsfull"));
			return null;
		}
		DTabImpl dt = new DTabImpl(ores, repoOres, title, getWindowControl());
		return dt;
	}

	/**
	 * fxdiff: load max dTab-Amount from Properties, set default to 5
	 * @return
	 */
	private int getMaxTabs() {
		if (MAX_TAB == null) {
			PersistedProperties prop = new PersistedProperties(this);
			prop.init();
			prop.setIntPropertyDefault("max.dtabs", 5);
			MAX_TAB = prop.getIntPropertyValue("max.dtabs");
		}
		return MAX_TAB;
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.DTabs#addDTab(org.olat.core.gui.control.generic.dtabs.DTab)
	 */
	public void addDTab(DTab dt) {
		// FIXME:fj:restrict to say 7 elements
		DTab old = getDTab(dt.getOLATResourceable());
		if (old != null) {
			//do make a red screen for that
			//throw new AssertException("dtabs already contained: " + old);
			getWindowControl().getWindowBackOffice().getWindow().setAttribute("BUSPATH", dt.getWindowControl());
			return;
		}
		// add to tabs list
		synchronized (dtabs) {
			// make dtabs and dtabsControllers access synchronized
			dtabs.add(dt);
			dtabsLinkNames.add(Integer.toString(dtabCreateCounter));
			Link link = LinkFactory.createCustomLink("a" + dtabCreateCounter, "a" + dtabCreateCounter, "", Link.NONTRANSLATED, navVc, this);
			link.setCustomDisplayText(((DTabImpl) dt).getNavElement().getTitle());
			link.setTitle(dt.getTitle());
			link.setUserObject(dt);
			// Set accessibility access key using the 's' key. You can loop through all opened tabs by
			// pressing s repetitively (works only in IE/FF which is normally used by blind people)
			link.setAccessKey("s");
			// add close links
			Link calink = LinkFactory.createCustomLink("ca" + dtabCreateCounter, "ca" + dtabCreateCounter, "", Link.NONTRANSLATED, navVc, this);
			calink.setCustomEnabledLinkCSS("b_nav_tab_close");
			calink.setTitle(translate("close"));
			calink.setTooltip(translate("close"), false);
			calink.setUserObject(dt);
			Link cplink = LinkFactory.createCustomLink("cp" + dtabCreateCounter, "cp" + dtabCreateCounter, "", Link.NONTRANSLATED, navVc, this);
			cplink.setCustomEnabledLinkCSS("b_nav_tab_close");
			cplink.setTitle(translate("close"));
			cplink.setTooltip(translate("close"), false);
			cplink.setUserObject(dt);

			Controller dtabCtr = ((DTabImpl) dt).getController();
			dtabCtr.addControllerListener(this);
			// add to tabs controller lookup table for later event dispatching
			dtabsControllers.add(dtabCtr);
			// increase DTab added counter.
			dtabCreateCounter++;
		}
		
		//set current BusPath for extraction in the TopNav Controller
		//FIXME:pb:2009-06-21:move core
		getWindowControl().getWindowBackOffice().getWindow().setAttribute("BUSPATH", dt.getWindowControl());		
		
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.DTabs#activate(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.generic.dtabs.DTab, java.lang.String)
	 */
	public void activate(final UserRequest ureq, DTab dTab, final String viewIdentifier, final List<ContextEntry> entries) {
		// FIXME:fj:c if viewIdentifier is DTABS.initialView -> activate to this
		// init view (e.g. kurs in run mode, repo-detail-edit...)
		// jump here via external link or just open a new tab from e.g. repository
		//fxdiff FXOLAT-113: business path in DMZ
		if(dTab == null && contentCtrl instanceof Activateable2) {
			((Activateable2)contentCtrl).activate(ureq, entries, null);
			return;
		}

		DTabImpl dtabi = (DTabImpl) dTab;
		Controller c = dtabi.getController();
		if (c == null) throw new AssertException("no controller set yet! " + dTab + ", view: " + viewIdentifier);
		doActivateDTab(dtabi);
		//fxdiff BAKS-7 Resume function
		if(entries != null && !entries.isEmpty() && c instanceof Activateable2) {
			final Activateable2 activateable = ((Activateable2) c);
			activateable.activate(ureq, entries, null);
		}
		//fxdiff BAKS-7 Resume function
		//update the panels after activation
		setGuiStack(dtabi.getGuiStackHandle());

		// activating a tab is like focusing a new window - we need to adjust the
		// guipath since e.g. the button triggering the activation is not
		// part of the guipath, but rather the new tab in its initial state.
		// in all other cases the "focus of interest" (where the calculation of the
		// guipath is started) matches the controller which listens to the
		// event caused by a user interaction.
		// WindowBackOffice wboNew = getWindowControl().getWindowBackOffice(); //
		// this is the starting point.
		// getWindowControl().getWindowBackOffice().adjustGuiPathCenter(getWindowControl().getWindowControlInfo());
	}

	/**
	 * FIXME:fj: change className to class
	 * 
	 * @see org.olat.core.gui.control.generic.dtabs.DTabs#activateStatic(org.olat.core.gui.UserRequest,
	 *      java.lang.String, java.lang.String)
	 */
	// brasato:: remove
	//fxdiff BAKS-7 Resume function
	public void activateStatic(UserRequest ureq, String className, String viewIdentifier, List<ContextEntry> entries) {
		for (Iterator<SiteInstance> it_sites = sites.iterator(); it_sites.hasNext();) {
			SiteInstance site = it_sites.next();
			String cName = site.getClass().getName();
			if (cName.equals(className)) {
				activateSite(site, ureq, viewIdentifier, entries);
				return;
			}
		}

	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.DTabs#removeDTab(org.olat.core.gui.control.generic.dtabs.DTab)
	 */
	public void removeDTab(DTab dt) {
		this.removeDTab((DTabImpl) dt);
	}

	public void event(Event event) {
		if (event == Window.AFTER_VALIDATING) {
			// now update the guimessage

			List places = (List) getWindowControl().getWindowBackOffice().getData("guimessage");
			Panel winnerP = null;
			int maxZ = -1;
			if (places != null) {
				// we have places where we can put the gui message
				for (Iterator it_places = places.iterator(); it_places.hasNext();) {
					ZIndexWrapper ziw = (ZIndexWrapper) it_places.next();
					int cind = ziw.getZindex();
					if (cind > maxZ) {
						maxZ = cind;
						winnerP = ziw.getPanel();
					}
				}
			} else {
				winnerP = guimsgHolder;
			}

			if (winnerP != currentMsgHolder) {
				currentMsgHolder.setContent(null);
				winnerP.setContent(guimsgPanel);
				currentMsgHolder = winnerP;
			} // else same place, nothing to change
			//
		} else if(event instanceof LanguageChangedEvent){
			LanguageChangedEvent lce = (LanguageChangedEvent)event;
			getTranslator().setLocale(lce.getNewLocale());
			initialize(lce.getCurrentUreq());
			initialPanel.popContent();
			initialPanel.pushContent(mainVc);
			//
		} else if (event instanceof ChiefControllerMessageEvent) {
			// msg can be set to show only on one node or on all nodes
			String msg = GlobalStickyMessage.getGlobalStickyMessage();//either null, or the global message or the per-node-message
			Boolean hasStickyMessage = Boolean.valueOf(msg != null);
			this.mainVc.contextPut("hasStickyMessage", hasStickyMessage);
			this.mainVc.contextPut("stickyMessage", msg != null ? msg : "");		}
	}

	/**
	 * [used by velocity] helper for velocity
	 * 
	 */
	public boolean isSiteActive(SiteInstance si) {
		return curSite != null && si == curSite;
	}

	/**
	 * 
	 * [used by velocity
	 * 
	 * @return
	 */
	public boolean isDTabActive(DTab dtab) {
		return curDTab != null && dtab == curDTab;
	}

	
}