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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.core.commons.chiefcontrollers;

import java.io.File;

import org.olat.core.dispatcher.mapper.GlobalMapperRegistry;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ContentableChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.WindowControlInfoImpl;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.guistack.GuiStackSimpleImpl;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.JavaScriptTracingController;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.prefs.Preferences;

/**
 * Description: <br>
 * This is the main chief controller for the olat application. It controls the
 * window, implements a windowcontrol and has a header, content, and footer
 * area.
 * <P>
 * Initial Date: 18.10.2004 <br>
 * 
 * @author Felix Jost
 */

public class BaseChiefController extends DefaultChiefController implements ContentableChiefController {
	static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(BaseChiefController.class);
	static final OLog log = Tracing.createLoggerFor(BaseChiefController.class);

	private VelocityContainer mainvc;
	private Panel contentPanel, mainPanel;
	private Translator translator;

	private boolean autoDisposeOnWindowClose;
	private Controller contentController;
	private Controller jsServerC;

	// not private to avoid synthetic accessor
	GuiStack currentGuiStack;

	private Controller debugC;

	private Controller inlineTranslationC;
	
	private Controller developmentC;

	private Controller jsLoggerC;
	
	private final WindowBackOffice wbo;
	
	private static Mapper jsTranslationMapper;
	private static String jsTranslationMapperPath;
	
	private static final boolean jsMathEnabled;
	
	static {
		// initialize global javascript translation mapper - shared in VM by all users
		jsTranslationMapper = new JSTranslatorMapper();
		jsTranslationMapperPath = GlobalMapperRegistry.getInstance().register(JSTranslatorMapper.class, jsTranslationMapper);

		// check if mandatory jsmath files are unzipped, write error otherwhise
		File jsMathImages = new File(WebappHelper.getContextRoot() + "/static/js/jsMath/fonts");
		if (!jsMathImages.exists() || !jsMathImages.isDirectory() || !(jsMathImages.list().length > 0)) {
			log.error("jsMath images needed by body.html are not deployed properly. This can result in JS errors. Run \"mvn olat:font\" to deploy the necessary jsMath images and restart tomcat");
			jsMathEnabled = false;
		} else {
			jsMathEnabled = true;
		}
	}
	
	//REVIEW:12-2007:CodeCleanup
	//private boolean bookmarkWaitOnce = false;
	

	/**
	 * The OLAT main window controller. This controller is responsible for a OLAT
	 * window
	 * 
	 * @param ureq The user request
	 */
	public BaseChiefController(UserRequest ureq) {
		setLoggingUserRequest(ureq);
		translator = Util.createPackageTranslator(this.getClass(), ureq.getLocale());

		// main layout/structure
		mainPanel = new Panel("brasatoMainPanel");
		mainPanel.setDomReplaceable(false);

		mainvc = new VelocityContainer("baseccvc", VELOCITY_ROOT + "/body.html", translator, this);
		// disallow wrapping of divs around the panel and the main velocity page
		// (since it contains the "<html><head... intro of the html page,
		// and thus has better to be replaced as a whole (new page load) instead of
		// a dom replacement)
		mainvc.setDomReplaceable(false);

		// component-id of mainPanel for the window id
		mainvc.contextPut("o_winid", String.valueOf(mainPanel.getDispatchID()));
		// add jsMath library
		mainvc.contextPut("jsMathEnabled", Boolean.valueOf(jsMathEnabled));
		mainPanel.setContent(mainvc);

		WindowManager winman = Windows.getWindows(ureq).getWindowManager();
		wbo  = winman.createWindowBackOffice("basechiefwindow", this);
		Window w = wbo.getWindow();
		
		// part that builds the css and javascript lib includes (<script src="..."> and <rel link
		// e.g. 
		// <script type="text/javascript" src="/demo/g/2/js/jscalendar/calendar.js"></script>

		mainvc.put("jsCssRawHtmlHeader", w.getJsCssRawHtmlHeader());	
		
		// control part for ajax-communication. returns an empty panel if ajax is not enabled, so that ajax can be turned on on the fly for development mode
		jsServerC = wbo.createAJAXController(ureq);
		mainvc.put("jsServer", jsServerC.getInitialComponent());	
		
		// init with no bookmark (=empty bc)
		mainvc.contextPut("o_bc", "");
		
		// the current language; used e.g. by screenreaders
		mainvc.contextPut("lang", ureq.getLocale().toString());

		// the current GUI theme and the global settings that contains the
		// font-size. both are pushed as objects so that window.dirty always reads
		// out the correct value
		mainvc.contextPut("theme", w.getGuiTheme());		
		mainvc.contextPut("globalSettings", winman.getGlobalSettings());		
		mainvc.contextPut("isScreenReader", winman.isForScreenReader());		
		
		// content panel
		contentPanel = new Panel("olatContentPanel");
		mainvc.put("olatContentPanel", contentPanel);
		mainvc.contextPut("o_winid", Long.valueOf(w.getDispatchID()));
		mainvc.contextPut("buildversion", Settings.getVersion());
		
		WindowControl wControl = new WindowControl() {
			private WindowControlInfo wci;

			{
				wci = new WindowControlInfoImpl(BaseChiefController.this, null);
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#pushToMainArea(org.olat.core.gui.components.Component)
			 */
			public void pushToMainArea(Component newMainArea) {
				currentGuiStack.pushContent(newMainArea);
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#pushAsModalDialog(java.lang.String,
			 *      org.olat.core.gui.components.Component)
			 */
			public void pushAsModalDialog(Component newModalDialog) {
				currentGuiStack.pushModalDialog(newModalDialog);
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#pop()
			 */
			public void pop() {
				// reactivate latest dialog from stack, dumping current one
				currentGuiStack.popContent();
			}
			
			
			/**
			 * @see org.olat.core.gui.control.WindowControl#setInfo(java.lang.String)
			 */
			public void setInfo(String info) {
				throw new AssertException("not implemented, need e.g. a SimplBaseController on top of this class here to implement this function");
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#setError(java.lang.String)
			 */
			public void setError(String error) {
				throw new AssertException("not implemented, need e.g. a SimplBaseController on top of this class here to implement this function");
			}

			/**
			 * @see org.olat.core.gui.control.WindowControl#setWarning(java.lang.String)
			 */
			public void setWarning(String warning) {
				throw new AssertException("not implemented, need e.g. a SimplBaseController on top of this class here to implement this function");
			}

			public WindowControlInfo getWindowControlInfo() {
				return wci;
			}

			public void makeFlat() {
				throw new AssertException("not implemented, need e.g. a SimplBaseController on top of this class here to implement this function");
			}

			public BusinessControl getBusinessControl() {
				return BusinessControlFactory.getInstance().getEmptyBusinessControl();
			}

			public WindowBackOffice getWindowBackOffice() {
				return wbo;
			}

		};
		super.setWindowControl(wControl);

		if (wbo.isDebuging()) {
			debugC = wbo.createDebugDispatcherController(ureq, getWindowControl());
			mainvc.put("guidebug", debugC.getInitialComponent());
		}		
		
		// Inline translation interceptor. when the translation tool is enabled it
		// will start the translation tool in translation mode, if the overlay
		// feature is enabled it will start in customizing mode
		if (ureq.getUserSession().isAuthenticated() && ureq.getUserSession().getRoles().isOLATAdmin() && (I18nModule.isTransToolEnabled() || I18nModule.isOverlayEnabled())) {
			inlineTranslationC = wbo.createInlineTranslationDispatcherController(ureq, getWindowControl());
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			Boolean isInlineTranslationEnabled = (Boolean) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_INLINE_TRANSLATION_ENABLED, Boolean.FALSE);
			I18nManager.getInstance().setMarkLocalizedStringsEnabled(ureq.getUserSession(), isInlineTranslationEnabled);			
			mainvc.put("inlineTranslation", inlineTranslationC.getInitialComponent());
		}

		// debug info if debugging
		if (wbo.isDebuging()) {
			developmentC = wbo.createDevelopmentController(ureq, getWindowControl());
			mainvc.put("development", developmentC.getInitialComponent());
		}

		// attach AJAX javascript console
		jsLoggerC = new JavaScriptTracingController(ureq, getWindowControl());
		// the js logger provides only a header element, nevertheless we need to
		// put it into the main velocity container.
		mainvc.put("jsLoggerC", jsLoggerC.getInitialComponent());

		// put the globals path like "/olat/classpath/61x/" into the main win,
		// used for some dynamic injected js libs like jsMath
		String resourcePath = getWindowControl().getWindowBackOffice().getWindowManager().getMapPathFor(this.getClass());
		mainvc.contextPut("classPathStaticBaseURI", resourcePath.substring(0, resourcePath.indexOf("org.olat")));

		// put the global js translator mapper path into the main window
		mainvc.contextPut("jsTranslationMapperPath", jsTranslationMapperPath);

		// master window
		w.addListener(this); // to be able to report "browser reload" to the user
		w.setContentPane(mainPanel);
		setWindow(w);

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
	// nothing to listen to at the moment
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == getWindow()) {
//		REVIEW:12-2007:CodeCleanup PB:27.02.2008 let those codefragments exist
			//if (event == Window.END_OF_DISPATCH_CYCLE) {
				/*String bc = wbo.getBusinessControlPath();
				wbo.getWindowManager().
				mainvc.contextPut("o_bc", bc);
				*/
		//	}	else if (event == Window.BEFORE_INLINE_RENDERING) {
				//String bc = wbo.getBusinessControlPath();				
				//mainvc.contextPut("o_bc", bc);
				
				/*if (!bookmarkWaitOnce) {
					mainvc.contextPut("bkme", "true");
				} else {
					bookmarkWaitOnce = false;
				}*/
				
			//}	else if (event == WindowBackOffice.IGNORE_BOOKMARK_ONE_TIME) {
				//bookmarkWaitOnce = true;
				//mainvc.contextPut("bkme", "false");
			//}	else 
				
			if (event == Window.OLDTIMESTAMPCALL) {
				// we have a "reload" push or such -> set Warn Msg
				// but now WindowControl which allows setting a warn message -> the
				// (first) main layouting controller should also listen to window
				// and set the appropriate message
			} else if (event == Window.COMPONENTNOTFOUND) {
				// we tried to dispatch to a nonexisting component: -> set Warn Msg
				// the reason may be:
				// a) a bug
				// b) the current component was removed by another thread from the
				// rendering tree. (e.g. in the course when a course is published while
				// users are working on it)
				// if (wbo.isDebuging())
				// guiMessage.setWarn(translator.translate("warn.notdispatched"));
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (autoDisposeOnWindowClose && contentController != null) {
			contentController.dispose();
			contentController = null;
		}

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

		if (jsLoggerC != null) {
			jsLoggerC.dispose();
			jsLoggerC = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.ContentableChiefController#setContentController(boolean,
	 *      org.olat.core.gui.control.Controller)
	 */
	public void setContentController(boolean autoDisposeOnWindowClose, Controller contentController) {
		if (this.contentController != null) throw new AssertException("can only set contentController once!");
		this.contentController = contentController;
		this.autoDisposeOnWindowClose = autoDisposeOnWindowClose;
		
		currentGuiStack = new GuiStackSimpleImpl(contentController.getInitialComponent());
		contentPanel.setContent(currentGuiStack.getPanel());		
//	REVIEW:12-2007:CodeCleanup
		//contentPanel.setContent(contentController.getInitialComponent());
	}
	
	/**
	 * Method to check if the jsmath javascript is loaded in the main body html file
	 * @return
	 */
	public static boolean isJsMathEnabled() {
		return jsMathEnabled;
	}

}
