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

package org.olat.core.commons.chiefcontrollers;

import java.util.HashSet;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.controller.simple.SimpleBaseController;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.mapper.GlobalMapperRegistry;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.WindowSettings;
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
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.JavaScriptTracingController;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
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

	private Panel contentPanel;

	private boolean autoDisposeOnWindowClose;
	private Controller contentController;
	private Controller jsServerC;

	// not private to avoid synthetic accessor
	GuiStack currentGuiStack;

	private Controller debugC;

	private Controller inlineTranslationC;
	
	private Controller developmentC;

	private Controller jsLoggerC;

	private Set<String> bodyCssClasses = new HashSet<String>();

	private final WindowBackOffice wbo;
	
	private static Mapper jsTranslationMapper;
	private static String jsTranslationMapperPath;

	static {
		// initialize global javascript translation mapper - shared in VM by all
		// users
		jsTranslationMapper = new JSTranslatorMapper();
		jsTranslationMapperPath = GlobalMapperRegistry.getInstance().register(JSTranslatorMapper.class, jsTranslationMapper);
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
		Translator translator = Util.createPackageTranslator(this.getClass(), ureq.getLocale());

		// main layout/structure
		Panel mainPanel = new Panel("brasatoMainPanel");
		mainPanel.setDomReplaceable(false);

		VelocityContainer mainvc = new VelocityContainer("baseccvc", VELOCITY_ROOT + "/body.html", translator, this);
		// disallow wrapping of divs around the panel and the main velocity page
		// (since it contains the "<html><head... intro of the html page,
		// and thus has better to be replaced as a whole (new page load) instead of
		// a dom replacement)
		mainvc.setDomReplaceable(false);

		// component-id of mainPanel for the window id
		mainvc.contextPut("o_winid", mainPanel.getDispatchID());
		
		BaseSecurityModule securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		mainvc.contextPut("enforceTopFrame", securityModule.isForceTopFrame());
		
		// add jsMath library
		mainvc.contextPut("jsMathEnabled", Boolean.TRUE);
		// add optional css classes
		mainvc.contextPut("bodyCssClasses", bodyCssClasses);

		mainPanel.setContent(mainvc);

		WindowManager winman = Windows.getWindows(ureq).getWindowManager();
		String wSettings = (String)ureq.getUserSession().removeEntryFromNonClearedStore(Dispatcher.WINDOW_SETTINGS);
		WindowSettings settings = WindowSettings.parse(wSettings);
		wbo = winman.createWindowBackOffice("basechiefwindow", this, settings);
		Window w = wbo.getWindow();

		mainvc.put("jsCssRawHtmlHeader", w.getJsCssRawHtmlHeader());

		// control part for ajax-communication. returns an empty panel if ajax
		// is not enabled, so that ajax can be turned on on the fly for
		// development mode
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
		mainvc.contextPut("o_winid", w.getDispatchID());
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

			@Override
			public void pushAsCallout(Component comp, String targetId) {
				currentGuiStack.pushCallout(comp, targetId);
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
		// fxdiff: allow user-managers to use the inline translation also. TODO:
		// do this with a proper right-mgmt!
		if (ureq.getUserSession().isAuthenticated()
				&& (ureq.getUserSession().getRoles().isOLATAdmin() || ureq.getUserSession().getRoles().isUserManager())
				&& (I18nModule.isTransToolEnabled() || I18nModule.isOverlayEnabled())) {
			inlineTranslationC = wbo.createInlineTranslationDispatcherController(ureq, getWindowControl());
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			Boolean isInlineTranslationEnabled = (Boolean) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_INLINE_TRANSLATION_ENABLED,
					Boolean.FALSE);
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
		//
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
		if (this.contentController != null)
			throw new AssertException("can only set contentController once!");
		this.contentController = contentController;
		this.autoDisposeOnWindowClose = autoDisposeOnWindowClose;
		
		currentGuiStack = new GuiStackSimpleImpl(contentController.getInitialComponent());
		contentPanel.setContent(currentGuiStack.getPanel());
		// REVIEW:12-2007:CodeCleanup
		// contentPanel.setContent(contentController.getInitialComponent());
	}
	
	/**
	 * Method to check if the jsmath javascript is loaded in the main body html file
	 * @return
	 */
	public static boolean isJsMathEnabled() {
		return true;
	}
	
	@Override
	public boolean hasStaticSite(Class<? extends SiteInstance> type) {
		if(contentController instanceof SimpleBaseController) {
			return ((SimpleBaseController)contentController).hasStaticSite(type);
		}
		return false;
	}

	/**
	 * adds a css-Classname to the OLAT body-tag
	 * 
	 * @param cssClass
	 *            the name of a css-Class
	 */
	public void addBodyCssClass(String cssClass) {
		// sets class for full page refreshes
		bodyCssClasses.add(cssClass);

		// only relevant in AJAX mode
		JSCommand jsc = new JSCommand("try { jQuery('#b_body').addClass('" + cssClass + "'); } catch(e){if(o_info.debug) console.log(e) }");
		getWindowControl().getWindowBackOffice().sendCommandTo(jsc);

	}

	/**
	 * removes the given css-Classname from the OLAT body-tag
	 * 
	 * @param cssClass
	 *            the name of a css-Class
	 */
	public void removeBodyCssClass(String cssClass) {
		// sets class for full page refreshes
		bodyCssClasses.remove(cssClass);
		
		//only relevant in AJAX mode
		JSCommand jsc = new JSCommand("try { jQuery('#b_body').removeClass('" + cssClass + "'); } catch(e){if(o_info.debug) console.log(e) }");
		getWindowControl().getWindowBackOffice().sendCommandTo(jsc);
	}

}
