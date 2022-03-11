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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.admin.landingpages.LandingPagesModule;
import org.olat.admin.layout.LayoutModule;
import org.olat.admin.layout.LogoInformations;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.commons.chiefcontrollers.ChiefControllerMessageEvent;
import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.commons.controllers.resume.ResumeSessionController;
import org.olat.core.commons.fullWebApp.util.GlobalStickyMessage;
import org.olat.core.commons.services.analytics.AnalyticsModule;
import org.olat.core.commons.services.analytics.AnalyticsSPI;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.gui.GUIMessage;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.countdown.CountDownComponent;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.ListPanel;
import org.olat.core.gui.components.panel.OncePanel;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabImpl;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.navigation.BornSiteInstance;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.control.util.ZIndexWrapper;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.id.context.HistoryPointImpl;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.assessment.ui.mode.AssessmentModeGuardController;
import org.olat.course.assessment.ui.mode.ChooseAssessmentModeEvent;
import org.olat.gui.control.UserToolsMenuController;
import org.olat.home.HomeSite;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The BaseFullWebappController defines the outer most part of the main layout
 * <P>
 * Initial Date: 20.07.2007 <br>
 * 
 * @author patrickb, Felix Jost, Florian Gnägi
 */
public class BaseFullWebappController extends BasicController implements DTabs, ChiefController, GenericEventListener {
	private static final String PRESENTED_AFTER_LOGIN_WORKFLOW = "presentedAfterLoginWorkflow";
	private static final String USER_PROPS_ID = BaseFullWebappController.class.getCanonicalName();
	
	//Base chief
	private Panel contentPanel;
	private Controller jsServerC;
	private Controller debugC;
	private Controller inlineTranslationC;
	private Controller developmentC;
	private List<String> bodyCssClasses = new ArrayList<>(3);

	private Boolean reload;
	private final ScreenMode screenMode = new ScreenMode();
	private WindowBackOffice wbo;
	
	// PARTICIPATING
	private GuiStack currentGuiStack;
	private Panel main;
	private Panel modalPanel;
	private Panel topModalPanel;
	private Panel instantMessagePanel;
	private final GUIMessage guiMessage;
	private final OncePanel guimsgPanel;
	private Panel cssHolder, guimsgHolder, currentMsgHolder;
	private VelocityContainer guimsgVc;
	private VelocityContainer mainVc;
	private VelocityContainer navSitesVc;
	private VelocityContainer navTabsVc;
	private StickyMessageComponent stickyMessageCmp;

	private LockStatus lockStatus;
	private OLATResourceable lockResource;
	private TransientAssessmentMode lockMode;
	
	// NEW FROM FullChiefController
	private LockableController topnavCtr;
	private LockableController footerCtr;
	private UserToolsMenuController userToolsMenuCtrl;
	private SiteInstance curSite;
	private DTab curDTab;
	
	private final List<TabState> siteAndTabs = new ArrayList<>();

	// the dynamic tabs list
	private List<DTab> dtabs;
	private List<Integer> dtabsLinkNames;
	private List<Controller> dtabsControllers;
	private Map<DTab,HistoryPoint> dtabToBusinessPath = new HashMap<>();
	// used as link id which is load url safe (e.g. replayable
	private int dtabCreateCounter = 0;
	// the sites list
	private SiteInstance userTools;
	private List<SiteInstance> sites;
	private Map<SiteInstance, BornSiteInstance> siteToBornSite = new HashMap<>();
	private Map<SiteInstance,HistoryPoint> siteToBusinessPath = new HashMap<>();

	private BaseFullWebappControllerParts baseFullWebappControllerParts;
	protected Controller contentCtrl;
	private ResumeSessionController resumeSessionCtrl;
	private AssessmentModeGuardController assessmentGuardCtrl;
	
	private StackedPanel initialPanel;
	private WindowSettings wSettings;
	
	private final boolean isAdmin;
	private final int maxTabs = 20;
	
	@Autowired
	private CSPModule cspModule;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AnalyticsModule analyticsModule;
	@Autowired
	private EdusharingModule edusharingModule;
	
	public BaseFullWebappController(UserRequest ureq, BaseFullWebappControllerParts baseFullWebappControllerParts) {
		// only-use-in-super-call, since we define our own
		super(ureq, null);
		setLoggingUserRequest(ureq);

		this.baseFullWebappControllerParts = baseFullWebappControllerParts;

		guiMessage = new GUIMessage();
		guimsgPanel = new OncePanel("guimsgPanel");
		
		UserSession usess = ureq.getUserSession();
		WindowManager winman = Windows.getWindows(ureq).getWindowManager();
		String windowSettings = (String)usess.removeEntryFromNonClearedStore(Dispatcher.WINDOW_SETTINGS);
		WindowSettings settings = WindowSettings.parse(windowSettings);
		wbo = winman.createWindowBackOffice("basechiefwindow", usess.getCsrfToken(), this, settings);
		
		IdentityEnvironment identityEnv = usess.getIdentityEnvironment();
		if(identityEnv != null && identityEnv.getRoles() != null) {	
			isAdmin = identityEnv.getRoles().isAdministrator();
		} else {
			isAdmin = false;
		}

		// define the new windowcontrol
		WindowControl myWControl = new BaseFullWebappWindowControl(this, wbo);
		overrideWindowControl(myWControl);

		Window myWindow = myWControl.getWindowBackOffice().getWindow();
		myWindow.setDTabs(this);
		//REVIEW:PB remove if back support is desired
		myWindow.addListener(this);//to be able to report BACK / FORWARD / RELOAD
		
		/*
		 * does all initialisation, moved to method because of possibility to react
		 * on LanguageChangeEvents -> resets and rebuilds footer, header, topnav, sites, content etc.
		 */
		initialize(ureq);
		
		mainVc.setDomReplaceable(false);
		
		initialPanel = putInitialPanel(mainVc);
		
		initialPanel.setDomReplaceable(false);
		// ------ all the frame preparation is finished ----
		initializeBase(ureq, initialPanel);
		
		if(usess.isAuthenticated() && !isAdmin && usess.getAssessmentModes() != null && !usess.getAssessmentModes().isEmpty()) {
    		assessmentGuardCtrl = new AssessmentModeGuardController(ureq, getWindowControl(),
    				usess.getAssessmentModes(), false);
    		listenTo(assessmentGuardCtrl);
    		assessmentGuardCtrl.getInitialComponent();
    		lockStatus = LockStatus.popup;
    		//as security remove all 
    		removeRedirects(usess);
    		//lock the gui
    		lockGUI();
    	} else {
    		// present an overlay with configured afterlogin-controllers or nothing if none configured.
    		// presented only once per session.
    		Boolean alreadySeen = ((Boolean)usess.getEntry(PRESENTED_AFTER_LOGIN_WORKFLOW));
    		if (usess.isAuthenticated() && alreadySeen == null) {
    			resumeSessionCtrl = new ResumeSessionController(ureq, getWindowControl());
    			listenTo(resumeSessionCtrl);
    			resumeSessionCtrl.getInitialComponent();
    			ureq.getUserSession().putEntry(PRESENTED_AFTER_LOGIN_WORKFLOW, Boolean.TRUE);
	    	}
    	}
		
    	if(assessmentGuardCtrl == null
    			&& (resumeSessionCtrl == null || (!resumeSessionCtrl.redirect() && !resumeSessionCtrl.userInteractionNeeded()))
    			&& usess.getEntry("AuthDispatcher:businessPath") == null) {
    		String bc = initializeDefaultSite(ureq);
    		if(StringHelper.containsNonWhitespace(bc) && usess.getEntry("redirect-bc") == null) {
    			usess.putEntry("redirect-bc", bc);
    		}
    	}
		
		Object fullScreen = Windows.getWindows(ureq).getFullScreen();
		if(Boolean.TRUE.equals(fullScreen)) {
			Windows.getWindows(ureq).setFullScreen(null);
			screenMode.setMode(Mode.full, null);
		}

		// register for cycle event to be able to adjust the guimessage place
		getWindowControl().getWindowBackOffice().addCycleListener(this);
		// register for locale change events -> 
		//move to a i18nModule? languageManger? languageChooserController?
		OLATResourceable wrappedLocale = OresHelper.createOLATResourceableType(Locale.class);
		usess.getSingleUserEventCenter().registerFor(this, getIdentity(), wrappedLocale);
		//register for assessment mode
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
		
		// Register for course and group added events
		if(getIdentity() != null && getIdentity().getKey() != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.registerFor(this, getIdentity(), OresHelper.createOLATResourceableInstance(NotificationsCenter.class, getIdentity().getKey()));
		}
		
		// register for global sticky message changed events
		GlobalStickyMessage.registerForGlobalStickyMessage(this, getIdentity());	
	}
	
	@Override
	public boolean isLoginInterceptionInProgress() {
		return resumeSessionCtrl != null && resumeSessionCtrl.userInteractionNeeded();
	}

	@Override
	public boolean delayLaunch(UserRequest ureq, BusinessControl bc) {
		return false;
	}

	/**
	 * Remove all possible redirect commands in session.
	 * 
	 * @param usess
	 */
	private void removeRedirects(UserSession usess) {
    	usess.removeEntry("AuthDispatcher:businessPath");
    	usess.removeEntry("redirect-bc");
    	usess.removeEntryFromNonClearedStore("AuthDispatcher:businessPath");
    	usess.removeEntryFromNonClearedStore("redirect-bc");
	}
	
	private void initializeBase(UserRequest ureq, ComponentCollection mainPanel) {
		UserSession usess = ureq.getUserSession();
		mainVc.contextPut("enforceTopFrame", cspModule.isForceTopFrame());

		// add optional css classes
		mainVc.contextPut("bodyCssClasses", bodyCssClasses);
		
		// add page width css. Init empty on login (full page state not persisted)
		mainVc.contextPut("pageSizeCss", "");
		
		// business path set with a full page refresh
		mainVc.contextPut("startBusinessPath", "");

		Window w = wbo.getWindow();

		mainVc.put("jsCssRawHtmlHeader", w.getJsCssRawHtmlHeader());

		// control part for ajax-communication. returns an empty panel if ajax
		// is not enabled, so that ajax can be turned on on the fly for
		// development mode
		jsServerC = wbo.createAJAXController(ureq);
		mainVc.put("jsServer", jsServerC.getInitialComponent());

		// init with no bookmark (=empty bc)
		mainVc.contextPut("o_bc", "");
		mainVc.contextPut("o_serverUri", Settings.createServerURI());
		
		
		// the current language; used e.g. by screenreaders
		mainVc.contextPut("lang", ureq.getLocale().toString());
		
		// some user properties
		if (ureq.getUserSession().isAuthenticated()) {
			Identity ident = ureq.getIdentity();
			StringBuilder sb = new StringBuilder();
			sb.append("{ identity : ").append( ident.getKey());
			User user = ident.getUser();
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, usess.getRoles().isAdministrator());
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				String escapedValue = StringHelper.escapeJavaScript(userPropertyHandler.getUserProperty(user, getLocale()));
				sb.append(", ").append(userPropertyHandler.getName()).append(" : \"").append(escapedValue).append("\"");				
			}
			sb.append("}");
			mainVc.contextPut("userJSON", sb);
		}

		mainVc.contextPut("theme", w.getGuiTheme());
		
		// Add JS analytics code, e.g. for google analytics
		if (analyticsModule.isAnalyticsEnabled()) {
			AnalyticsSPI analyticsSPI = analyticsModule.getAnalyticsProvider();
			if(analyticsSPI != null) {
				mainVc.contextPut("analytics",analyticsSPI.analyticsInitPageJavaScript());
			}
		}
		
		// Enable edu-sharing html snippet replacement
		if (edusharingModule.isEnabled()) {
			mainVc.contextPut("edusharingEnabled", Boolean.TRUE);
		}
		
		// content panel
		contentPanel = new Panel("olatContentPanel");
		mainVc.put("olatContentPanel", contentPanel);
		mainVc.contextPut("o_winid", w.getDispatchID());
		mainVc.contextPut("buildversion", Settings.getVersion());
		

		if (wbo.isDebuging()) {
			debugC = wbo.createDebugDispatcherController(ureq, getWindowControl());
			mainVc.put("guidebug", debugC.getInitialComponent());
		}		
		
		// Inline translation interceptor. when the translation tool is enabled it
		// will start the translation tool in translation mode, if the overlay
		// feature is enabled it will start in customizing mode
		// fxdiff: allow user-managers to use the inline translation also.
		if (usess.isAuthenticated()
				&& (usess.getRoles().isAdministrator() || usess.getRoles().isSystemAdmin())
				&& (i18nModule.isTransToolEnabled() || i18nModule.isOverlayEnabled())) {
			inlineTranslationC = wbo.createInlineTranslationDispatcherController(ureq, getWindowControl());
			Preferences guiPrefs = usess.getGuiPreferences();
			Boolean isInlineTranslationEnabled = (Boolean) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_INLINE_TRANSLATION_ENABLED,
					Boolean.FALSE);
			i18nManager.setMarkLocalizedStringsEnabled(usess, isInlineTranslationEnabled);
			mainVc.put("inlineTranslation", inlineTranslationC.getInitialComponent());
		}

		// debug info if debugging
		if (wbo.isDebuging()) {
			developmentC = wbo.createDevelopmentController(ureq, getWindowControl());
			mainVc.put("development", developmentC.getInitialComponent());
		}

		// put the global js translator mapper path into the main window
		mainVc.contextPut("jsTranslationMapperPath", BaseChiefController.jsTranslationMapperPath);

		// master window
		//w.addListener(this); // to be able to report "browser reload" to the user
		w.setContentPane(mainPanel);
	}

	@Override
	public Window getWindow() {
		return wbo.getWindow();
	}

	@Override
	public WindowControl getWindowControl() {
		return super.getWindowControl();
	}
	
	public void setStartBusinessPath(String path) {
		String businessPath = BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathString(path);
		mainVc.contextPut("startBusinessPath", businessPath);
	}

	private void initialize(UserRequest ureq) {
		mainVc = createVelocityContainer("fullwebapplayout");
		mainVc.contextPut("screenMode", screenMode);

		LayoutModule layoutModule = CoreSpringFactory.getImpl(LayoutModule.class);
		LandingPagesModule landingPagesModule = CoreSpringFactory.getImpl(LandingPagesModule.class);
		LogoInformations logoInfos = new LogoInformations(ureq, layoutModule, landingPagesModule);
		mainVc.contextPut("logoInfos", logoInfos);
		
		// use separate container for navigation to prevent full page refresh in ajax mode on site change
		// nav is not a controller part because it is a fundamental part of the BaseFullWebAppConroller.
		navSitesVc = createVelocityContainer("nav_sites");
		navSitesVc.setDomReplacementWrapperRequired(false);
		navSitesVc.contextPut("visible", Boolean.TRUE);
		mainVc.put("sitesComponent", navSitesVc);
		
		navTabsVc = createVelocityContainer("nav_tabs");
		navTabsVc.setDomReplacementWrapperRequired(false);
		mainVc.put("tabsComponent", navTabsVc);

		// GUI messages
		guimsgVc = createVelocityContainer("guimsg");
		guimsgVc.contextPut("guiMessage", guiMessage);
		guimsgHolder = new Panel("guimsgholder");
		guimsgHolder.setContent(guimsgPanel);
		currentMsgHolder = guimsgHolder;
		mainVc.put("guimessage", guimsgHolder);
		
		// CSS panel
		cssHolder = new Panel("customCss");
		mainVc.put("customCssHolder", cssHolder);

		// sticky maintenance message
		stickyMessageCmp = new StickyMessageComponent("stickymsg", screenMode);
		mainVc.put("stickymsg", stickyMessageCmp);
		updateStickyMessage();
		
		dtabs = new ArrayList<>();
		dtabsLinkNames = new ArrayList<>();
		dtabsControllers = new ArrayList<>();

		// -- sites -- by definition the first site is activated at the beginning
		userTools = new HomeSite(null);
		sites = baseFullWebappControllerParts.getSiteInstances(ureq, getWindowControl());
		if (sites != null && sites.isEmpty()) {
			sites = null;
		}
		
		List<String> siteLinks = new ArrayList<>();
		
		// either sites is null or contains at least one SiteInstance.
		if (sites != null) {
			// create the links for the sites
			for (Iterator<SiteInstance> iterator = sites.iterator(); iterator.hasNext();) {
				SiteInstance si = iterator.next();
				NavElement navEl = si.getNavElement();
				if(navEl != null) {
					String linkName = "t" + CodeHelper.getRAMUniqueID();
					siteLinks.add(linkName);
					Link link = LinkFactory.createCustomLink(linkName, "t", "", Link.NONTRANSLATED, navSitesVc, this);
					link.setCustomDisplayText(StringHelper.xssScan(navEl.getTitle()));
					link.setTitle(navEl.getDescription());
					link.setUserObject(si);
					if(StringHelper.containsNonWhitespace(navEl.getBusinessPath())) {
						String navUrl = BusinessControlFactory.getInstance()
								.getRelativeURLFromBusinessPathString(navEl.getBusinessPath());
						link.setUrl(navUrl);
					}
					Character accessKey = navEl.getAccessKey();
					if (accessKey != null && StringHelper.containsNonWhitespace(accessKey.toString())) {
						link.setAccessKey(accessKey.toString());					
					}
				}
			}
		}
		
		navSitesVc.contextPut("sites", siteLinks);
		navSitesVc.contextPut("tabhelper", this);
		navTabsVc.contextPut("dtabs", dtabs);
		navTabsVc.contextPut("dtabsLinkNames", dtabsLinkNames);
		navTabsVc.contextPut("tabhelper", this);

		// header, optional (e.g. for logo, advertising )
		Controller headerCtr = baseFullWebappControllerParts.createHeaderController(ureq, getWindowControl());
		if (headerCtr != null) {
			listenTo(headerCtr); // cleanup on dispose
			Component headerCmp = headerCtr.getInitialComponent();
			mainVc.put("headerComponent", headerCmp);
		}

		// topnav, optional (e.g. for imprint, logout)
		topnavCtr = baseFullWebappControllerParts.createTopNavController(ureq, getWindowControl());
		if (topnavCtr != null) {
			listenTo(topnavCtr); // cleanup on dispose
			mainVc.put("topnavComponent", topnavCtr.getInitialComponent());
			
			userToolsMenuCtrl = new UserToolsMenuController(ureq, getWindowControl());
			listenTo(userToolsMenuCtrl);
			mainVc.put("menuComponent", userToolsMenuCtrl.getInitialComponent());
		}

		// panel for modal overlays, placed right after the olat-header-div
		modalPanel = new Panel("ccmodalpanel");
		mainVc.put("modalpanel", modalPanel);
		
		topModalPanel = new Panel("topmodalpanel");
		mainVc.put("topmodalpanel", topModalPanel);
		
		instantMessagePanel = new Panel("impanel");
		mainVc.put("instantmessagepanel", instantMessagePanel);

		// main, mandatory (e.g. a LayoutMain3ColsController)
		main = new Panel("mainContent");
		mainVc.put("main", main);

		// footer, optional (e.g. for copyright, powered by)
		footerCtr = baseFullWebappControllerParts.createFooterController(ureq, getWindowControl());
		if (footerCtr != null) {
			listenTo(footerCtr); // cleanup on dispose
			Component footerCmp = footerCtr.getInitialComponent();
			mainVc.put("footerComponent", footerCmp);
		}
		
		contentCtrl = baseFullWebappControllerParts.getContentController(ureq, getWindowControl());
		if (contentCtrl != null) {
			listenTo(contentCtrl);
			GuiStack gs = getWindowControl().getWindowBackOffice().createGuiStack(contentCtrl.getInitialComponent());
			setGuiStack(gs);
			main.setContent(contentCtrl.getInitialComponent());
		} else {
			main.setContent(TextFactory.createTextComponentFromString("empty", "", null, false, null));
			//set a guistack for the after login interceptor
			GuiStack gs = getWindowControl().getWindowBackOffice().createGuiStack(new Panel("dummy"));
			setGuiStack(gs);
		}

		setWindowSettings(getWindowControl().getWindowBackOffice().getWindowSettings());
	}
	
	private void updateStickyMessage() {
		stickyMessageCmp.setText(GlobalStickyMessage.getGlobalStickyMessage());
	}

	/**
	 * @param ureq
	 * @return The current business path if a site is initialized or null
	 */
	private String initializeDefaultSite(UserRequest ureq) {
		String businessPath = null;
		if (sites != null && !sites.isEmpty()
				&& curSite == null && curDTab == null
				&& contentCtrl == null && lockResource == null) {
			SiteInstance s = sites.get(0);
			//activate site only if no content was set -> allow content before activation of default site.
			activateSite(s, ureq, null, false);
			businessPath = updateBusinessPath(ureq, s);
		}
		return businessPath;
	}
	
	protected GUIMessage getGUIMessage() {
		return guiMessage;
	}
	
	protected OncePanel getGUIMsgPanel() {
		return guimsgPanel;
	}
	
	protected GuiStack getCurrentGuiStack() {
		return currentGuiStack;
	}
	
	protected VelocityContainer getGUIMsgVc() {
		return guimsgVc;
	}
	
	private void setWindowSettings(WindowSettings wSettings) {
		if((this.wSettings == null && wSettings != null)
				|| (this.wSettings != null && !this.wSettings.equals(wSettings))) {
			this.wSettings = wSettings;
			boolean navVisible = wSettings == null || !wSettings.isHideNavigation();
			navSitesVc.setVisible(navVisible);
			navTabsVc.setVisible(navVisible);
			if (topnavCtr != null) {
				topnavCtr.getInitialComponent().setVisible(wSettings == null || !wSettings.isHideHeader());
			}
			if (footerCtr != null) {
				footerCtr.getInitialComponent().setVisible(wSettings == null || !wSettings.isHideFooter());
			}
			mainVc.setDirty(true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			String mC = link.getCommand().substring(0, 1);
			if (mC.equals("t")) { // activate normal tab
				SiteInstance s = (SiteInstance) link.getUserObject();
				//fix the state of the last tab/site
				updateBusinessPath(ureq);
				
				HistoryPoint point = null;
				if(siteToBusinessPath.containsKey(s)) {
					point = siteToBusinessPath.get(s);
				}
				activateSite(s, ureq, null, true);
				if(point != null) {
					BusinessControlFactory.getInstance().addToHistory(ureq, point);
				}
				updateBusinessPath(ureq, s);
			} else if (mC.equals("a")) { // activate dyntab
				DTab dt = (DTab) link.getUserObject();
				//fix the state of the last tab/site
				updateBusinessPath(ureq);
				
				HistoryPoint point = null;
				if(dtabToBusinessPath.containsKey(dt)) {
					point = dtabToBusinessPath.get(dt);
				}

				doActivateDTab(dt);
				if(dt.getController() instanceof Activateable2) {
					((Activateable2)dt.getController()).activate(ureq, null, new ReloadEvent());
				}
				if(point != null) {
					BusinessControlFactory.getInstance().addToHistory(ureq, point);
				}
			} else if (mC.equals("c")) { // close dyntab
				DTab dt = (DTab) link.getUserObject();
				requestCloseTab(ureq, dt);
			}
		} else if (source == getWindowControl().getWindowBackOffice().getWindow()) {
			if (event == Window.OLDTIMESTAMPCALL) {
				getLogger().info("RELOAD");
				
				HistoryPoint point = ureq.getUserSession().popLastHistoryEntry();
				if(point != null) {
					Command reloadCmd = CommandFactory.reloadWindow();
					getWindow().getWindowBackOffice().sendCommandTo(reloadCmd);
				}
			}
		} else if (source == mainVc) {
			// Set CSS on body to maintain user selected page width. 
			// Add it to velocity just in case of a full page reload. The CSS class has already been added via JS in the user browser!
			if ("width.full".equals(event.getCommand())) {
				mainVc.contextPut("pageSizeCss", "o_width_full");
				mainVc.setDirty(false);
			} else if ("width.standard".equals(event.getCommand())) {
				mainVc.contextPut("pageSizeCss", "");			
				mainVc.setDirty(false);
			} else if("close-window".equals(event.getCommand())) {
				getWindow().setMarkToBeRemoved(true);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(resumeSessionCtrl == source) {
			resumeSessionCtrl.redirect();
			resumeSessionCtrl = null;
			initializeDefaultSite(ureq);
		} else if(assessmentGuardCtrl == source) {
			if(event instanceof ChooseAssessmentModeEvent) {
				ChooseAssessmentModeEvent came = (ChooseAssessmentModeEvent)event;
				lockMode = came.getAssessmentMode();
				lockStatus = LockStatus.locked;
				removeAsListenerAndDispose(assessmentGuardCtrl);
				assessmentGuardCtrl = null;
			} else if("continue".equals(event.getCommand())) {
				//unlock session
				ureq.getUserSession().unlockResource();
				unlockResource();
				
				initializeDefaultSite(ureq);
				removeAsListenerAndDispose(assessmentGuardCtrl);
				assessmentGuardCtrl = null;
				lockStatus = null;
				lockMode = null;
			}
		} else {
			int tabIndex = dtabsControllers.indexOf(source);
			if (tabIndex > -1) {
				// Event comes from a controller in a dtab. Check if the controller is
				// finished and close the tab. Cancel and failed is interpreted as
				// finished.
				if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT || event == Event.FAILED_EVENT) {
					DTab tab = dtabs.get(tabIndex);
					removeDTab(ureq, tab);//disposes also tab and controllers
				}
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
			siteToBusinessPath = null;
			dtabToBusinessPath = null;	
		}
		//clear the DTabs Service
		WindowBackOffice wbackOffice = getWindowControl().getWindowBackOffice();
		wbackOffice.getWindow().setDTabs(null);
		wbackOffice.removeCycleListener(this);
		
		if (jsServerC != null) {
			jsServerC.dispose();
			jsServerC = null;
		}
		if (debugC != null) {
			debugC.dispose();
			debugC = null;
		}
		if (inlineTranslationC != null) {
			inlineTranslationC.dispose();
			inlineTranslationC = null;
		}
		if (developmentC != null) {
			developmentC.dispose();
			developmentC = null;
		}

		//deregister for assessment mode
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
        super.doDispose();
	}

	private void setGuiStack(GuiStack guiStack) {
		currentGuiStack = guiStack;
		StackedPanel guiStackPanel = currentGuiStack.getPanel();
		main.setContent(guiStackPanel);
		// place for modal dialogs, which are overlayd over the normal layout (using
		// css alpha blending)
		// maybe null if no current modal dialog -> clears the panel
		StackedPanel modalStackP = currentGuiStack.getModalPanel();
		modalPanel.setContent(modalStackP);
		
		StackedPanel topModalStackP = currentGuiStack.getTopModalPanel();
		topModalPanel.setContent(topModalStackP);
		
		ListPanel instantMessageP = currentGuiStack.getInstantMessagePanel();
		instantMessagePanel.setContent(instantMessageP);
	}

	/**
	 * Activate a site if not locked
	 * 
	 * @param s
	 * @param ureq
	 * @param entries
	 * @param forceReload
	 */
	private void activateSite(SiteInstance s, UserRequest ureq,
			List<ContextEntry> entries, boolean forceReload) {
		if(lockResource != null) {
			return;
		}
		
		BornSiteInstance bs = siteToBornSite.get(s);
		GuiStack gs;
		Controller resC;
		//PB//WindowControl site_wControl;
		if (bs != null && s != curSite) {
			// single - click -> fetch guistack from cache
			gs = bs.getGuiStackHandle();
			resC = bs.getController();
		} else if (bs != null && s == curSite && !forceReload) {
			//via activate, don't force the reload
			gs = bs.getGuiStackHandle();
			resC = bs.getController();
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
		if(resC instanceof Activateable2) {
			((Activateable2)resC).activate(ureq, entries, null);
		}
		//perhaps has activation changed the gui stack and it need to be updated
		setGuiStack(gs);
	}

	private void doActivateSite(SiteInstance s, GuiStack gs) {
		removeCurrentCustomCSSFromView();

		// set curSite
		setCurrent(s, null);
		setGuiStack(gs);
		NavElement navEl = s.getNavElement();
		if(navEl != null) {
			getWindow().setTitle(getTranslator(), navEl.getTitle());
			setBodyDataResource("site", s.getClass().getSimpleName(), null);
		}
		// update marking of active site/tab
		navSitesVc.setDirty(true);
		navTabsVc.setDirty(true);
		// add css for this site
		BornSiteInstance bs = siteToBornSite.get(s);
		if (bs != null) {
			addCurrentCustomCSSToView(bs.getCustomCSS());
		}
	}

	private void doActivateDTab(DTab dtabi) {
		removeCurrentCustomCSSFromView();

		//set curDTab
		setCurrent(null, dtabi);
		setGuiStack(dtabi.getGuiStackHandle());
		// set description as page title, getTitel() might contain trucated values
		getWindow().setTitle(getTranslator(), dtabi.getNavElement().getDescription());
		// set data-* values on body for css and javascript customizations
		OLATResourceable ores = dtabi.getOLATResourceable();
		String restype = (ores == null ? null : ores.getResourceableTypeName());
		String resid = (ores == null ? null : ores.getResourceableId() + "");
		OLATResourceable initialOres = dtabi.getInitialOLATResourceable();
		String repoid = (initialOres == null ? null : initialOres.getResourceableId() + "");
		setBodyDataResource(restype, resid, repoid);
		// update marking of active site/tab
		navSitesVc.setDirty(true);
		navTabsVc.setDirty(true);
		// add css for this tab
		addCurrentCustomCSSToView(dtabi.getCustomCSS());
	}

	/**
	 * Helper method to set data-" attributes to the body element in the DOM.
	 * Using the data attributes it is possible to implement css styles specific
	 * to certain areas (sites, groups, courses) of for specific course id's.
	 * The data attributes are removed if null
	 * 
	 * @param restype The resource type or NULL if n.a.
	 * @param resid The resource ID that matchtes the restype or NULL if n.a.
	 * @param repoentryid the repository entry ID if available or NULL if n.a.
	 */
	private void setBodyDataResource(String restype, String resid, String repoentryid) {
		StringBuilder sb = new StringBuilder();
		sb.append("try {var oobody = jQuery('body');");
		// The source type info: for sites value is 'site', for courses
		// 'CourseModule' and groups 'BusinessGroup'
		if (restype == null) {
			sb.append("oobody.removeAttr('data-restype');");						
		} else {
			sb.append("oobody.attr('data-restype','");
			sb.append(Formatter.escapeDoubleQuotes(restype));
			sb.append("');");			
		}
		// The resource id: for sites this is the name of the site (e.g.
		// MyCoursesSite") for courses it is the numeric resource id (not the
		// course/repo entry id)
		if (resid == null) {
			sb.append("oobody.removeAttr('data-resid');");									
		} else {
			sb.append("oobody.attr('data-resid','");
			sb.append(Formatter.escapeDoubleQuotes(resid));
			sb.append("');");
		}
		// The repository id, aka course-id. Normally only available for courses
		// (not for sites or groups)
		if (repoentryid == null) {
			sb.append("oobody.removeAttr('data-repoid');");									
		} else {
			sb.append("oobody.attr('data-repoid','");
			sb.append(Formatter.escapeDoubleQuotes(repoentryid));
			sb.append("');");
		}
		sb.append("oobody=null;}catch(e){}");
		JSCommand jsc = new JSCommand(sb.toString());
		WindowControl wControl = getWindowControl();
		if (wControl != null && wControl.getWindowBackOffice() != null) {
			wControl.getWindowBackOffice().sendCommandTo(jsc);			
		}
	}

	/**
	 * Remove the current custom css from the view
	 */
	@Override
	public void removeCurrentCustomCSSFromView() {
		Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
		CustomCSS currentCustomCSS = myWindow.getCustomCSS();
		if (currentCustomCSS != null) {
			// remove css and js from view
			cssHolder.setContent(null);
			myWindow.setCustomCSS(null);
		}
	}

	/**
	 * Add a custom css to the view and mark it as the current custom CSS.
	 * 
	 * @param customCSS
	 */
	@Override
	public void addCurrentCustomCSSToView(CustomCSS customCSS) {
		if (customCSS == null) return;
		// The current CSS is stored as a window attribute so that is can be
		// accessed by the IFrameDisplayController
		Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
		myWindow.setCustomCSS(customCSS);
		// add css component to view
		cssHolder.setContent(customCSS.getJSAndCSSComponent());
	}
	
	@Override
	public boolean wishReload(UserRequest ureq, boolean erase) {
		if(Window.NO_RESPONSE_VALUE_MARKER.equals(ureq.getParameter(Window.NO_RESPONSE_PARAMETER_MARKER))) {
			return false;// background request cannot change screen size, need to wait async
		}

		boolean wishFullScreen = getScreenMode().isWishFullScreen();
		boolean screen = getScreenMode().wishScreenModeSwitch(erase);
		if(screen && StringHelper.containsNonWhitespace(getScreenMode().getBusinessPath())) {
			String businessPath;
			if(ureq.getUserSession().isAuthenticated()) {
				businessPath = BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathString(getScreenMode().getBusinessPath());
			} else {
				businessPath = BusinessControlFactory.getInstance()
						.getURLFromBusinessPathString(getScreenMode().getBusinessPath());
			}
			mainVc.getContext().put("startBusinessPath", businessPath);
		}

		if(screen && StringHelper.containsNonWhitespace(getScreenMode().getFullScreenBodyClass())) {
			if(wishFullScreen) {
				bodyCssClasses.add(getScreenMode().getFullScreenBodyClass());
			} else {
				bodyCssClasses.remove(getScreenMode().getFullScreenBodyClass());
			}
		}
		
		boolean r = reload != null && reload.booleanValue();
		if(erase && reload != null) {
			reload = null;
		}
		boolean l = checkAssessmentGuard(ureq, lockMode);
		return l || r || screen;
	}

	@Override
	public boolean wishAsyncReload(UserRequest ureq, boolean erase) {
		boolean wishFullScreen = getScreenMode().isWishFullScreen();
		boolean screen = getScreenMode().wishScreenModeSwitch(erase);
		
		if(screen && StringHelper.containsNonWhitespace(getScreenMode().getFullScreenBodyClass())) {
			if(wishFullScreen) {
				bodyCssClasses.add(getScreenMode().getFullScreenBodyClass());
			} else {
				bodyCssClasses.remove(getScreenMode().getFullScreenBodyClass());
			}
		}
		
		boolean l = checkAssessmentGuard(ureq, lockMode);
		return screen || l; 
	}

	@Override
	public void resetReload() {
		getScreenMode().reset();
		reload = null;
	}

	@Override
	public ScreenMode getScreenMode() {
		return screenMode;
	}

	/**
	 * adds a css-Classname to the OLAT body-tag
	 * 
	 * @param cssClass
	 *            the name of a css-Class
	 */
	@Override
	public void addBodyCssClass(String cssClass) {
		// sets class for full page refreshes
		bodyCssClasses.add(cssClass);

		// only relevant in AJAX mode
		JSCommand jsc = new JSCommand("try { jQuery('#o_body').addClass('" + cssClass + "'); } catch(e){if(window.console) console.log(e) }");
		getWindowControl().getWindowBackOffice().sendCommandTo(jsc);
	}

	/**
	 * removes the given css-Classname from the OLAT body-tag
	 * 
	 * @param cssClass
	 *            the name of a css-Class
	 */
	@Override
	public void removeBodyCssClass(String cssClass) {
		// sets class for full page refreshes
		bodyCssClasses.remove(cssClass);
		
		//only relevant in AJAX mode
		JSCommand jsc = new JSCommand("try { jQuery('#o_body').removeClass('" + cssClass + "'); } catch(e){if(window.console) console.log(e) }");
		getWindowControl().getWindowBackOffice().sendCommandTo(jsc);
	}
	
	/**
	 * @param pos
	 * @return the dtab at pos pos
	 */
	public DTab getDTabAt(int pos) {
		synchronized (dtabs) {
			return dtabs.get(pos);
		}
	}

	@Override
	public void removeDTab(UserRequest ureq, DTab delt) {
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
				//
				// NOTHING TO REMOVE, return
				return;
			}
			// Remove tab itself
			dtabs.remove(delt);
			dtabToBusinessPath.remove(delt);
			Integer tabId = dtabsLinkNames.remove(dtabIndex);
			Controller tabCtr = dtabsControllers.get(dtabIndex);
			dtabsControllers.remove(tabCtr);

			for(Iterator<TabState> it=siteAndTabs.iterator(); it.hasNext(); ) {
				if(it.next().getDtab() == delt) {
					it.remove();
				}
			}

			navTabsVc.setDirty(true);
			// remove created links for dtab out of container
			navTabsVc.remove(navTabsVc.getComponent("a" + tabId));
			navTabsVc.remove(navTabsVc.getComponent("c" + tabId));
			navTabsVc.remove(navTabsVc.getComponent("ca" + tabId));
			navTabsVc.remove(navTabsVc.getComponent("cp" + tabId));
			if (delt == curDTab && ureq != null) { // if we close the current tab -> return to the previous
				popTheTabState(ureq);
			} // else just remove the dtabs
			delt.dispose();//dispose tab and controllers in tab
		}
	}
	
	private void popTheTabState(UserRequest ureq) {
		if(siteAndTabs.isEmpty() && sites != null) {
			SiteInstance firstSite = sites.get(0);
			BornSiteInstance bs = siteToBornSite.get(firstSite);
			if(bs == null) {
				activateSite(firstSite, ureq, null, false);
			} else {
				doActivateSite(firstSite, bs.getGuiStackHandle());
			}
		} else if(!siteAndTabs.isEmpty()) {
			TabState state = siteAndTabs.remove(siteAndTabs.size() - 1);
			if(state.getSite() != null) {
				// latest selected static tab
				// activate previous chosen static site -> this site has already been
				// constructed and is thus in the cache
				SiteInstance si = state.getSite();
				BornSiteInstance bs = siteToBornSite.get(si);
				// bs != null since clicked previously
				GuiStack gsh = bs.getGuiStackHandle();
				doActivateSite(si, gsh);
				if(siteToBusinessPath.containsValue(si)) {
					ureq.getUserSession().addToHistory(ureq, siteToBusinessPath.get(si));
				}
			} else if (state.getDtab() != null && !state.getDtab().getController().isDisposed()) {
				DTab tab = state.getDtab();
				doActivateDTab(tab);
				if(dtabToBusinessPath.containsKey(tab)) {
					ureq.getUserSession().addToHistory(ureq, dtabToBusinessPath.get(tab));
				}
			} else {
				popTheTabState(ureq);
			}
		}
	}

	/**
	 * @param dt
	 */
	private void requestCloseTab(UserRequest ureq, DTab delt) {
		Controller c = delt.getController();
		if (c instanceof VetoableCloseController) {
			VetoableCloseController vcc = (VetoableCloseController) c;
			// rembember current dtab, and swap to the temporary one
			DTab reTab = curDTab;
			doActivateDTab(delt);
			boolean immediateClose = vcc.requestForClose(ureq);
			if (!immediateClose) {
				return;
			} else {
				if (reTab != null) {
					doActivateDTab(reTab);
				}
				removeDTab(ureq, delt);
			}
		} else {
			removeDTab(ureq, delt);
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.DTabs#getDTab(org.olat.core.id.OLATResourceable
	 */
	@Override
	public DTab getDTab(OLATResourceable ores) {
		synchronized (dtabs) {
			for (Iterator<DTab> it_dts = dtabs.iterator(); it_dts.hasNext();) {
				DTab dtab = it_dts.next();
				if (OresHelper.equals(dtab.getOLATResourceable(), ores)) {
					return dtab;
				}
				if (OresHelper.equals(dtab.getInitialOLATResourceable(), ores)) {
					return dtab;
				}
			}
			return null;
		}
	}

	@Override
	public DTab createDTab(UserRequest ureq, OLATResourceable ores, OLATResourceable repoOres, Controller rootController, String title) {
		final DTabImpl dt;
		if (dtabs.size() >= maxTabs) {
			getWindowControl().setError(translate("warn.tabsfull"));
			dt = null;
		} else if(lockResource != null && !matchLockedResource(ureq, ores)) {
			dt = null;
		} else {
			dt = new DTabImpl(ores, repoOres, title, rootController, getWindowControl());
		}
		return dt;
	}
	
	private boolean matchLockedResource(UserRequest ureq, OLATResourceable ores) {
		UserSession usess = ureq.getUserSession();
		return lockResource != null && (OresHelper.equals(lockResource, ores) || usess.matchSecondaryResource(ores));
	}

	@Override
	public boolean addDTab(UserRequest ureq, DTab dt) {
		if(isDisposed()) {
			return false;
		}

		DTab old = getDTab(dt.getOLATResourceable());
		if (old != null) {
			return true;
		}
		// add to tabs list
		synchronized (dtabs) {
			// make dtabs and dtabsControllers access synchronized
			dtabs.add(dt);
			dtabsLinkNames.add(dtabCreateCounter);
			String linkId = "a" + dtabCreateCounter;
			Link link = LinkFactory.createCustomLink(linkId, linkId, "", Link.NONTRANSLATED, navTabsVc, this);
			link.setCustomDisplayText(StringHelper.xssScan(dt.getNavElement().getTitle()));
			link.setIconLeftCSS("o_icon o_icon-fw ".concat(dt.getNavElement().getIconCSSClass()));
			link.setTitle(dt.getTitle());
			link.setUserObject(dt);
			// Set accessibility access key using the 's' key. You can loop through all opened tabs by
			// pressing s repetitively (works only in IE/FF which is normally used by blind people)
			link.setAccessKey("s");
			// add close links
			Link calink = LinkFactory.createCustomLink("c" + dtabCreateCounter, "c" + dtabCreateCounter, "", Link.NONTRANSLATED, navTabsVc, this);
			calink.setCustomEnabledLinkCSS("o_navbar_tab_close");
			calink.setIconLeftCSS("o_icon o_icon_close_tab");
			calink.setTitle(translate("close"));
			calink.setUserObject(dt);

			Controller dtabCtr = dt.getController();
			dtabCtr.addControllerListener(this);
			updateBusinessPath(ureq, dt);
			// add to tabs controller lookup table for later event dispatching
			dtabsControllers.add(dtabCtr);
			// increase DTab added counter.
			dtabCreateCounter++;
		}
		return true;
	}
	
	@Override
	public void updateDTabTitle(OLATResourceable ores, String newTitle) {
		DTab dTab = getDTab(ores);
		if (dTab != null) {
			dTab.getNavElement().setTitle(newTitle);
			// search all dtab links and find the one with the correct dtab as user object
			for (int i = 0; i <= dtabCreateCounter; i++) {
				Link link = (Link)navTabsVc.getComponent("a" + i);
				if (link != null && dTab.equals(link.getUserObject())) {
					// correct link found - updte titel and we are done
					link.setCustomDisplayText(StringHelper.xssScan(newTitle));
					return;
				}				
			}			
		}
	}
	
	/**
	 * Activating a tab is like focusing a new window - we need to adjust the
	 * guipath since e.g. the button triggering the activation is not
	 * part of the guipath, but rather the new tab in its initial state.
	 * in all other cases the "focus of interest" (where the calculation of the
	 * guipath is started) matches the controller which listens to the
	 * event caused by a user interaction.
	 * this is the starting point.
	 */
	@Override
	public void activate(UserRequest ureq, DTab dTab, List<ContextEntry> entries) {
		UserSession usess = ureq.getUserSession();
		if((lockStatus != null || usess.isInAssessmentModeProcess())
				&& (!usess.matchLockResource(dTab.getOLATResourceable()))) {
			return;
		}
		
		//update window settings if needed
		setWindowSettings(getWindowControl().getWindowBackOffice().getWindowSettings());

		// init view (e.g. kurs in run mode, repo-detail-edit...)
		// jump here via external link or just open a new tab from e.g. repository
		if(dTab == null && contentCtrl instanceof Activateable2) {
			((Activateable2)contentCtrl).activate(ureq, entries, null);
		} else {
			DTabImpl dtabi = (DTabImpl) dTab;
			Controller c = dtabi.getController();
			if (c == null) {
				throw new AssertException("no controller set yet! " + dTab);
			}
			doActivateDTab(dtabi);
	
			if(c instanceof Activateable2) {
				final Activateable2 activateable = ((Activateable2) c);
				activateable.activate(ureq, entries, null);
			}
			updateBusinessPath(ureq, dtabi);
			//update the panels after activation
			setGuiStack(dtabi.getGuiStackHandle());
		}
	}

	@Override
	public void activateStatic(UserRequest ureq, String className, List<ContextEntry> entries) {
		if(className != null && className.endsWith("HomeSite")) {
			activateSite(userTools, ureq, entries, false);
		} else if(sites != null) {
			for (Iterator<SiteInstance> it_sites = sites.iterator(); it_sites.hasNext();) {
				SiteInstance site = it_sites.next();
				String cName = site.getClass().getName();
				if (cName.equals(className)) {
					activateSite(site, ureq, entries, false);
					return;
				}
			}
		}
	}
	
	@Override
	public void closeDTab(UserRequest ureq, OLATResourceable ores, HistoryPoint launchedFromPoint) {
		// Now try to go back to place that is attached to (optional) root back business path
		if (launchedFromPoint != null && StringHelper.containsNonWhitespace(launchedFromPoint.getBusinessPath())
				&& launchedFromPoint.getEntries() != null && !launchedFromPoint.getEntries().isEmpty()
				&& startsWithBusinessPath(launchedFromPoint.getEntries().get(0))) {
			BusinessControl bc = BusinessControlFactory.getInstance().createFromPoint(launchedFromPoint);
			if(bc.hasContextEntry()) {
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
				try {//make the resume secure. If something fail, don't generate a red screen
					NewControllerFactory.getInstance().launch(ureq, bwControl);
				} catch (Exception e) {
					logError("Error while resuming with root level back business path::" + launchedFromPoint.getBusinessPath(), e);
				}
			}
		}
		
		// Navigate beyond the stack, our own layout has been popped - close this tab
		DTabs tabs = getWindowControl().getWindowBackOffice().getWindow().getDTabs();
		if (tabs != null) {
			DTab tab = tabs.getDTab(ores);
			if (tab != null) {
				tabs.removeDTab(ureq, tab);						
			}
		}
	}
	
	/**
	 * 
	 * @param path
	 * @return true if a site or tabs starts with the business path
	 */
	private boolean startsWithBusinessPath(ContextEntry entry) {
		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(List.of(entry));
		String path = BusinessControlFactory.getInstance().getAsString(bc);
		
		try {
			if(sites != null && siteToBornSite != null) {
				for(SiteInstance site:sites) {
					BornSiteInstance bs = siteToBornSite.get(site);
					if (bs != null && bs.getController() != null) {
						String bp = bs.getController().getWindowControlForDebug().getBusinessControl().getAsString();
						if(bp != null && bp.startsWith(path)) {
							return true;
						}
					}
				}
			}
			
			if(dtabsControllers != null) {
				for(Controller ctrl:dtabsControllers) {
					String bp = ctrl.getWindowControlForDebug().getBusinessControl().getAsString();
					if(bp != null && bp.startsWith(path)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			logError("", e);
		}
		
		return false;
	}

	@Override
	public void event(Event event) {
		if (event == Window.AFTER_VALIDATING) {
			// now update the guimessage

			List<ZIndexWrapper> places = getWindowControl().getWindowBackOffice().getGuiMessages();
			Panel winnerP = null;
			int maxZ = -1;
			if (places != null) {
				// we have places where we can put the gui message
				for (Iterator<ZIndexWrapper> it_places = places.iterator(); it_places.hasNext();) {
					ZIndexWrapper ziw = it_places.next();
					int cind = ziw.getZindex();
					if (cind > maxZ) {
						maxZ = cind;
						winnerP = ziw.getPanel();
					}
				}
			} else {
				winnerP = guimsgHolder;
			}

			if (winnerP != null && winnerP != currentMsgHolder) {
				currentMsgHolder.setContent(null);
				winnerP.setContent(guimsgPanel);
				currentMsgHolder = winnerP;
			} else {
				currentMsgHolder = guimsgHolder;
				currentMsgHolder.setContent(guimsgPanel);
				currentMsgHolder.setDirty(guimsgPanel.isDirty());
			}
		} else if(event == Window.AFTER_INLINE_RENDERING) {
			// don't make the panel dirty
			mainVc.getContext().put("startBusinessPath", "");
		} else if(event instanceof LanguageChangedEvent){
			LanguageChangedEvent lce = (LanguageChangedEvent)event;
			UserRequest ureq = lce.getCurrentUreq();
			getTranslator().setLocale(lce.getNewLocale());
			initialize(ureq);
			initializeBase(ureq, initialPanel);
			initialPanel.setContent(mainVc);
			
			reload = Boolean.TRUE;
		} else if (event instanceof ChiefControllerMessageEvent) {
			// msg can be set to show only on one node or on all nodes
			updateStickyMessage();
		} else if (event instanceof AssessmentModeNotificationEvent) {
			try {
				processAssessmentModeNotificationEvent((AssessmentModeNotificationEvent)event);
			} catch (Exception e) {
				logError("", e);
			}
		} 
		// Check for group or course updates
		else if (event instanceof NotificationEvent) {
			try {
				processNotificationEvent((NotificationEvent)event);
			} catch (Exception e) {
				logError("", e);
			}
		}
	}
	
	private void processNotificationEvent(NotificationEvent event) {
		// Only check if not in assessment mode and not in full screen mode
		if (lockResource == null && (getScreenMode() == null || !(getScreenMode().isFullScreen() || getScreenMode().isWishFullScreen()))
				&& getIdentity() != null) {
			Translator translator = Util.createPackageTranslator(event.getI18nPackage(), getLocale());
			String message = translator.translate(event.getI18nKey(), event.getArguments());
			getWindowControl().setInfo(message);
			
			// replace by our message holder
			currentMsgHolder = guimsgHolder;
			currentMsgHolder.setContent(guimsgPanel);
			currentMsgHolder.setDirty(guimsgPanel.isDirty());
		}
	}
	
	private void processAssessmentModeNotificationEvent(AssessmentModeNotificationEvent event) {
		if(getIdentity() == null || !event.isModeOf(lockMode, getIdentity())) {
			return;
		}

		String cmd = event.getCommand();
		switch(cmd) {
			case AssessmentModeNotificationEvent.STOP_WARNING:
				lockResourceMessage(event.getAssessementMode());
				break;
			case AssessmentModeNotificationEvent.BEFORE:
				if(asyncUnlockResource(event.getAssessementMode())) {
					stickyMessageCmp.setDelegateComponent(null);
				}
				break;	
			case AssessmentModeNotificationEvent.LEADTIME:
				if(asyncLockResource(event.getAssessementMode())) {
					stickyMessageCmp.setDelegateComponent(null);
				}
				break;
			case AssessmentModeNotificationEvent.START_ASSESSMENT:
				if(event.getAssessedIdentityKeys().contains(getIdentity().getKey())) {
					asyncLockResource(event.getAssessementMode());
				}
				break;
			case AssessmentModeNotificationEvent.STOP_ASSESSMENT:
				if(event.getAssessedIdentityKeys().contains(getIdentity().getKey())
						&& asyncLockResource(event.getAssessementMode())) {
					stickyMessageCmp.setDelegateComponent(null);
				}
				break;
			case AssessmentModeNotificationEvent.END:
				if(event.getAssessedIdentityKeys().contains(getIdentity().getKey())
						&& asyncUnlockResource(event.getAssessementMode())) {
					stickyMessageCmp.setDelegateComponent(null);
				}
				break;	
		}
	}

	@Override
	public boolean hasStaticSite(Class<? extends SiteInstance> type) {
		boolean hasSite = false;
		if(sites != null && !sites.isEmpty()) {
			for(SiteInstance site:sites) {
				if(site.getClass().equals(type)) {
					hasSite = true;
				}
			}
		}
		return hasSite;
	}
	
	@Override
	public LockResourceInfos getLockResourceInfos() {
		if(lockResource == null) return null;
		return new LockResourceInfos(lockStatus, lockResource, lockMode);
	}

	@Override
	public void lockResource(OLATResourceable resource) {
		this.lockResource = resource;
		lockGUI();
	}

	@Override
	public void hardLockResource(LockResourceInfos lockInfos) {
		if(lockInfos == null) return;
		
		lockResource = lockInfos.getLockResource();
		lockMode = lockInfos.getLockMode();
		lockStatus = lockInfos.getLockStatus();
		lockGUI();
	}
	
	private void lockGUI() {
		if(topnavCtr != null) {
			topnavCtr.lock();
		}
		if(footerCtr != null) {
			footerCtr.lock();
		}
		
		if(userToolsMenuCtrl != null) {
			userToolsMenuCtrl.lock();
		}
		
		if(dtabsControllers != null) {
			for(int i=dtabsControllers.size(); i-->0; ) {
				DTab tab = dtabs.get(i);
				if(lockResource == null
						|| !lockResource.getResourceableId().equals(tab.getOLATResourceable().getResourceableId())) {
					removeDTab(null, tab);
				} else if (lockResource != null
						&& lockResource.getResourceableId().equals(tab.getOLATResourceable().getResourceableId())
						&& lockStatus != LockStatus.locked) {
					removeDTab(null, tab);
				}
			}
		}
		navSitesVc.contextPut("visible", Boolean.FALSE);
		navSitesVc.setDirty(true);
		navTabsVc.setDirty(true);
		main.setContent(new Panel("empty-mode"));
	}

	private void unlockResource() {
		this.lockResource = null;
		if(topnavCtr != null) {
			topnavCtr.unlock();
		}
		if(footerCtr != null) {
			footerCtr.unlock();
		}
		if(userToolsMenuCtrl != null) {
			userToolsMenuCtrl.unlock();
		}
		navSitesVc.contextPut("visible", Boolean.TRUE);
		navSitesVc.setDirty(true);
		navTabsVc.setDirty(true);
	}

	private boolean asyncLockResource(TransientAssessmentMode mode) {
		boolean lock;
		if(isAdmin) {
			lock = false;
		} else if(lockResource == null) {
			logAudit("Async lock resource for identity: " + getIdentity().getKey() + " (" + mode.getResource() + ")");
			lockResource(mode.getResource());
			lock = true;
			lockMode = mode;
			lockStatus = LockStatus.need;
		} else if(lockResource.getResourceableId().equals(mode.getResource().getResourceableId())) {
			if(mode.getStatus() == Status.leadtime || (mode.getStatus() == Status.followup
					&& (mode.getEndStatus() == EndStatus.all
						|| ((mode.getEndStatus() == null || mode.getEndStatus() == EndStatus.withoutDisadvantage) && !hasDisadvantageCompensation(mode))))) {
				if(assessmentGuardCtrl == null) {
					lockStatus = LockStatus.need;
				}
				lockMode = mode;
			}
			lock = true;
		} else {
			lock = false;
		}
		return lock;
	}
	
	private boolean hasDisadvantageCompensation(TransientAssessmentMode mode) {
		return CoreSpringFactory.getImpl(DisadvantageCompensationService.class)
			.isActiveDisadvantageCompensation(getIdentity(), new RepositoryEntryRefImpl(mode.getRepositoryEntryKey()), mode.getElementList());
	}
	
	private boolean asyncUnlockResource(TransientAssessmentMode mode) {
		boolean unlock;
		if(lockResource != null && lockResource.getResourceableId().equals(mode.getResource().getResourceableId())) {
			logAudit("Async unlock resource for identity: " + getIdentity().getKey() + " (" + mode.getResource() + ")");
			unlockResource();
			if(lockMode != null) {
				//check if there is a locked resource first
				lockStatus = LockStatus.need;
			} else {
				lockStatus = null;
			}
			lockMode = null;
			unlock = true;
		} else {
			unlock = false;
		}
		return unlock;
	}
	
	private void lockResourceMessage(TransientAssessmentMode mode) {
		if(lockResource != null && lockResource.getResourceableId().equals(mode.getResource().getResourceableId())) {
			Translator trans = Util.createPackageTranslator(AssessmentModeGuardController.class, getLocale());
			if(stickyMessageCmp.getDelegateComponent() instanceof CountDownComponent) {
				CountDownComponent cmp = (CountDownComponent)stickyMessageCmp.getDelegateComponent();
				cmp.setDate(mode.getEnd());
			} else {
				CountDownComponent cmp = new CountDownComponent("stickcountdown", mode.getEnd(), trans);
				cmp.setI18nKey("assessment.countdown");
				stickyMessageCmp.setDelegateComponent(cmp);
			}
		}
	}
	
	private boolean checkAssessmentGuard(UserRequest ureq, TransientAssessmentMode mode) {
		boolean needUpdate;
		if(assessmentGuardCtrl == null) {
			if(lockStatus == LockStatus.need) {
				List<TransientAssessmentMode> modes = mode == null ?
						Collections.<TransientAssessmentMode>emptyList() : Collections.singletonList(mode);
				assessmentGuardCtrl = new AssessmentModeGuardController(ureq, getWindowControl(),
						modes , true);
				listenTo(assessmentGuardCtrl);
				assessmentGuardCtrl.getInitialComponent();
				lockStatus = LockStatus.popup;
				lockGUI();
				needUpdate = true;
			} else {
				needUpdate = false;
			}
		} else {
			needUpdate = assessmentGuardCtrl.updateAssessmentMode(ureq);
		}
		
		return needUpdate;
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
	 * [used by velocity]
	 * 
	 * @return
	 */
	public boolean isDTabActive(DTab dtab) {
		return curDTab != null && dtab == curDTab;
	}
	
	/**
	 * Invitee have only one dynamic tab. They are not allowed
	 * to close it.
	 * [used by velocity]
	 * 
	 * @return
	 */
	public boolean isCanCloseDTab(DTab dtab) {
		boolean canClose = true;
		if(lockResource != null
				&& lockResource.getResourceableId().equals(dtab.getOLATResourceable().getResourceableId())
				&& lockResource.getResourceableTypeName().equals(dtab.getOLATResourceable().getResourceableTypeName())) {
			canClose = false;
		} else {
			canClose = (sites != null && sites.size() > 0);
			if(!canClose && dtabs != null) {
				synchronized (dtabs) {
					canClose = (dtabs != null && dtabs.size() > 1);
				}
			}
		}
		return canClose;
	}
	
	private void setCurrent(SiteInstance site, DTab tab) {
		curSite = site;
		curDTab = tab;
		siteAndTabs.add(new TabState(tab, site));
		
		//limite the size
		if(siteAndTabs.size() > 30) {
			while(siteAndTabs.size() > 30) {
				siteAndTabs.remove(0);
			}
		}
	}
	
	private void updateBusinessPath(UserRequest ureq) {
		if(siteAndTabs.isEmpty()) return;
		
		TabState tabState = siteAndTabs.get(siteAndTabs.size() - 1);
		if(tabState.getSite() != null) {
			updateBusinessPath(ureq, tabState.getSite());
		} else if (tabState.getDtab() != null) {
			updateBusinessPath(ureq, tabState.getDtab());
		}
	}
	
	private String updateBusinessPath(UserRequest ureq, SiteInstance site) {
		if(site == null) return null;

		try {
			String businessPath = siteToBornSite.get(site).getController().getWindowControlForDebug().getBusinessControl().getAsString();
			HistoryPoint point = ureq.getUserSession().getLastHistoryPoint();
			int index = businessPath.indexOf(':');
			if(index > 0 && point != null && point.getBusinessPath() != null) {
				String start = businessPath.substring(0, index);
				if(!point.getBusinessPath().startsWith(start)) {
					//if a controller has not set its business path, don't pollute the mapping
					List<ContextEntry> entries = siteToBornSite.get(site).getController().getWindowControlForDebug().getBusinessControl().getEntries();
					siteToBusinessPath.put(site, new HistoryPointImpl(ureq.getUuid(), businessPath, entries));
					return BusinessControlFactory.getInstance().getAsRestPart(entries, true);
				}
				List<ContextEntry> entries = siteToBornSite.get(site).getController().getWindowControlForDebug().getBusinessControl().getEntries();
				businessPath = BusinessControlFactory.getInstance().getAsRestPart(entries, true);
			}
			
			siteToBusinessPath.put(site, point);
			return businessPath;
		} catch (Exception e) {
			logError("", e);
			return null;
		}
	}
	
	private void updateBusinessPath(UserRequest ureq, DTab tab) {
		//dtabToBusinessPath is null if the controller is disposed
		if(tab == null || dtabToBusinessPath == null) return;

		try {
			String businessPath = tab.getController().getWindowControlForDebug().getBusinessControl().getAsString();
			HistoryPoint point = ureq.getUserSession().getLastHistoryPoint();
			int index = businessPath.indexOf(']');
			if(index > 0 && point != null && point.getBusinessPath() != null) {
				String start = businessPath.substring(0, index);
				if(!point.getBusinessPath().startsWith(start)) {
					//if a controller has not set its business path, don't pollute the mapping
					List<ContextEntry> entries = tab.getController().getWindowControlForDebug().getBusinessControl().getEntries();
					dtabToBusinessPath.put(tab, new HistoryPointImpl(ureq.getUuid(), businessPath, entries));
					return;
				}
			}
			dtabToBusinessPath.put(tab, point);
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	public static class TabState {
		private final DTab dtab;
		private final SiteInstance site;
		
		private TabState(DTab dtab, SiteInstance site) {
			this.dtab = dtab;
			this.site = site;
		}

		public DTab getDtab() {
			return dtab;
		}

		public SiteInstance getSite() {
			return site;
		}
		
		public String getTitle() {
			if(site != null && site.getNavElement() != null) {
				return site.getNavElement().getTitle();
			} else if(dtab != null) {
				return dtab.getTitle();
			}
			return null;
		}
	}
	
	protected enum LockStatus {
		need,
		popup,
		locked
	}
}