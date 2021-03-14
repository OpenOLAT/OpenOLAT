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


package org.olat.core.gui.exception;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.logging.KnownIssueException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ExceptionWindowController extends DefaultChiefController {
	private static final Logger log = Tracing.createLoggerFor(ExceptionWindowController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ExceptionWindowController.class);

	private VelocityContainer msg;

	/**
	 * @param ureq
	 * @param th
	 */
	public ExceptionWindowController(UserRequest ureq, Throwable th, boolean allowBackButton) {
		log.warn("ExceptionWindowController<init>: Throwable occurred, logging the full stacktrace:", th);

		// Disable inline translation mode whenever an exception occurs
		I18nManager i18nMgr = I18nManager.getInstance();
		if (i18nMgr.isCurrentThreadMarkLocalizedStringsEnabled()) {
			// Don't show back button when previously in inline translation mode, will not work
			allowBackButton = false;
			i18nMgr.setMarkLocalizedStringsEnabled(ureq.getUserSession(), false);			
		}

		Translator trans = Util.createPackageTranslator(ExceptionWindowController.class, ureq.getLocale());
		Formatter formatter = Formatter.getInstance(ureq.getLocale());
		msg = new VelocityContainer("olatmain", VELOCITY_ROOT + "/exception_page.html", trans, this);

		CSPModule securityModule = CoreSpringFactory.getImpl(CSPModule.class);
		msg.contextPut("enforceTopFrame", Boolean.valueOf(securityModule.isForceTopFrame()));
		
		// Disallow wrapping of divs around the panel and the main velocity page
		// (since it contains the "<html><head... intro of the html page,
		// and thus has better to be replaced as a whole (new page load) instead of
		// a dom replacement)
		msg.setDomReplaceable(false);
		
		msg.contextPut("buildversion", Settings.getVersion());

		OLATRuntimeException o3e;
		
		if (th == null){
			o3e = new OLATRuntimeException("Error Screen with a Throwable == null", null);
		} else if (!(th instanceof OLATRuntimeException)) {
			o3e = new OLATRuntimeException(th.getMessage(), th);
		} else {
			o3e = (OLATRuntimeException) th;
		}

		String detailedmessage = null;
		// translate user message if available
		if (o3e.getUsrMsgKey() != null && o3e.getUsrMsgPackage() != null) {
			PackageTranslator usrMsgTrans = new PackageTranslator(o3e.getUsrMsgPackage(), ureq.getLocale());
			if (o3e.getUsrMsgArgs() == null) {
				detailedmessage = usrMsgTrans.translate(o3e.getUsrMsgKey());
			} else {
				detailedmessage = usrMsgTrans.translate(o3e.getUsrMsgKey(), o3e.getUsrMsgArgs());
			}
		}
		// fix detailed message
		if (detailedmessage == null) {
			detailedmessage = "-";
		}

		// fetch more info
		// get the latest window which caused this exception
		String componentListenerInfo = "";
		Windows ws = Windows.getWindows(ureq);
		
		Window window = ws.getWindow(ureq);
		if (window != null) {
			Component target = window.getAndClearLatestDispatchedComponent();
			if (target != null) {
				// there was a component id given, and a matching target could be found
				componentListenerInfo = "<dispatchinfo>\n\t<componentinfo>\n\t\t<compname>" + target.getComponentName() + "</compname>\n\t\t<compclass>"
						+ target.getClass().getName() + "</compclass>\n\t\t<extendedinfo>" + target.getExtendedDebugInfo()
						+ "</extendedinfo>\n\t\t<event>";
				Event latestEv = target.getAndClearLatestFiredEvent();
				if (latestEv != null) {
					componentListenerInfo += "\n\t\t\t<class>"+latestEv.getClass().getName()+"</class>\n\t\t\t<command>"+latestEv.getCommand()+"</command>\n\t\t\t<tostring>"+latestEv+"</tostring>";
				}
				componentListenerInfo += "\n\t\t</event>\n\t</componentinfo>\n\t<controllerinfo>";
				Controller c = target.getLatestDispatchedController();
				if (c != null) {
					// can be null if the error occured in the component itself
					// sorry, getting windowcontrol on a controller which does not have one (all should have one, legacy) throws an exception
					try {
						
						WindowControlInfo wci = c.getWindowControlForDebug().getWindowControlInfo();
						while (wci != null) {
							String cName = wci.getControllerClassName();
							componentListenerInfo += "\n\t\t<controllername>" + cName + "</controllername>";
							wci = wci.getParentWindowControlInfo();
						}
					} catch (Exception e) {
						componentListenerInfo += "no info, probably no windowcontrol set: "+e.getClass().getName()+", "+e.getMessage();
					}
				}
				componentListenerInfo += "\n\t</controllerinfo>\n</dispatchinfo>";
			}
		}

		if(o3e instanceof KnownIssueException){
			KnownIssueException kie = (KnownIssueException)o3e;
			msg.contextPut("knownissuelink", kie.getJiraLink());
		}
		
		Logger o3log = Tracing.createLoggerFor(o3e.getThrowingClazz());
		String refNum = ureq.getUuid();
		String componentListenerInfoFlat = componentListenerInfo.replace('\n', ' ').replace('\t', ' ');
		o3log.error("**RedScreen** {} ::_::{} ::_::", o3e.getLogMsg(), componentListenerInfoFlat, o3e);
		// only if debug
		if (Settings.isDebuging()) {
			msg.contextPut("debug", Boolean.TRUE);
		} else {
			msg.contextPut("debug", Boolean.FALSE);			
		}
		msg.contextPut("listenerInfo", Formatter.escWithBR(componentListenerInfo).toString());			
		msg.contextPut("stacktrace", OLATRuntimeException.throwableToHtml(th));			
		
		Identity curIdent = ureq.getIdentity();
		msg.contextPut("username", curIdent == null? "n/a" : curIdent.getKey());
		msg.contextPut("allowBackButton", Boolean.valueOf(allowBackButton));
		msg.contextPut("detailedmessage", detailedmessage);
		// Cluster request reference number
		msg.contextPut("errnum", "I" + refNum + "-J");
		msg.contextPut("supportaddress", WebappHelper.getMailConfig("mailError"));
		msg.contextPut("time", formatter.formatDateAndTime(new Date()));

		WindowBackOffice wbo = ws.getWindowManager().createWindowBackOffice("errormessagewindow", ureq.getUserSession().getCsrfToken(), this, new WindowSettings());
		Window w = wbo.getWindow();
		
		msg.put("jsCssRawHtmlHeader", w.getJsCssRawHtmlHeader());		
		
		msg.contextPut("theme", w.getGuiTheme());		
		
		UserSession session = ureq.getUserSession();
		if(session != null &&  session.getLastHistoryPoint() != null) {
			HistoryPoint point = session.getLastHistoryPoint();
			String businessPath = point.getBusinessPath();
			if(StringHelper.containsNonWhitespace(businessPath)) {
				List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
				String url = BusinessControlFactory.getInstance().getAsURIString(entries, true);
				msg.contextPut("lastbusinesspath", url);
			}
			
			List<HistoryPoint> stack = session.getHistoryStack();
			if(stack != null && stack.size() > 1) {
				HistoryPoint prevPoint = stack.get(stack.size() - 2);
				String prevBusinessPath = prevPoint.getBusinessPath();
				if(StringHelper.containsNonWhitespace(prevBusinessPath)) {
					List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(prevBusinessPath);
					String url = BusinessControlFactory.getInstance().getAsURIString(entries, true);
					msg.contextPut("prevbusinesspath", url);
				}
			}
			
		}

		w.setContentPane(msg);
		setWindow(w);
	}

	@Override
	public boolean isLoginInterceptionInProgress() {
		return false;
	}
	
	@Override
	public boolean delayLaunch(UserRequest ureq, BusinessControl bc) {
		return false;
	}

	@Override
	public void addBodyCssClass(String cssClass) {
		//
	}

	@Override
	public void removeBodyCssClass(String cssClass) {
		//
	}
	
	@Override
	public void addCurrentCustomCSSToView(CustomCSS customCSS) {
		//
	}

	@Override
	public void removeCurrentCustomCSSFromView() {
		//
	}

	@Override
	public boolean hasStaticSite(Class<? extends SiteInstance> type){
		return false;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
	//
	}

	@Override
	protected void doDispose() {
		// nothing to do here
	}
}