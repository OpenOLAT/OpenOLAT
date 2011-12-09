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
*/

package org.olat.admin.sysinfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.admin.AdminModule;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 *  @author Christian Guretzki
 */

public class SessionAdministrationController extends BasicController {
	
	OLog log = Tracing.createLoggerFor(this.getClass());
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(SysinfoController.class);

	private VelocityContainer myContent;
	private Link invalidateAllSsssionLink;
	private DialogBoxController invalidateAllConfirmController;
	private Link blockLoginLink;
	private Link allowLoginLink;
	private DialogBoxController blockLoginConfirmController;
	private Link rejectDMZReuqestsLink;
	private Link allowDMZRequestsLink;
	private DialogBoxController rejectDMZRequestsConfirmController;
  private SessionAdminForm sessionAdminForm;
  private SessionAdminOldestSessionForm sessionAdminOldestSessionForm;
  
	/**
	 * Controlls user session in admin view.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public SessionAdministrationController(UserRequest ureq, WindowControl wControl) { 
		super(ureq, wControl);
		
		myContent = createVelocityContainer("sessionadministration");
		
		invalidateAllSsssionLink = LinkFactory.createButton("session.admin.invalidate.all.link", myContent, this);
		myContent.contextPut("loginBlocked", AdminModule.isLoginBlocked());
		blockLoginLink = LinkFactory.createButton("session.admin.block.login.link", myContent, this);
		
		boolean showRejectLink = CoordinatorManager.getInstance().getCoordinator().isClusterMode();
		myContent.contextPut("showRejectDMZRequestsLink", showRejectLink);
		if (showRejectLink) {
			myContent.contextPut("rejectingDMZRequests", AuthHelper.isRejectDMZRequests());
			
			TextFactory.createTextComponentFromI18nKey("session.admin.reject.dmz.requests.intro", "session.admin.reject.dmz.requests.intro", 
					getTranslator(), null, true, myContent);
			TextFactory.createTextComponentFromI18nKey("session.admin.allow.dmz.requests.intro", "session.admin.allow.dmz.requests.intro", 
					getTranslator(), null, true, myContent);
			rejectDMZReuqestsLink = LinkFactory.createButton("session.admin.reject.dmz.requests.link", myContent, this);
			allowDMZRequestsLink = LinkFactory.createButton("session.admin.allow.dmz.requests.link", myContent, this);
		}
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, AdminModule.PROPERTY_SESSION_ADMINISTRATION);
		String sessionToken = (p == null ? "" : p.getStringValue());
		myContent.contextPut("sessionToken", sessionToken);
		allowLoginLink = LinkFactory.createButton("session.admin.allow.login.link", myContent, this);
		sessionAdminOldestSessionForm  = new SessionAdminOldestSessionForm(ureq, wControl, getTranslator());
		listenTo(sessionAdminOldestSessionForm);
		myContent.put("session.admin.oldest.session.form", sessionAdminOldestSessionForm.getInitialComponent());
		sessionAdminForm = new SessionAdminForm(ureq, wControl, getTranslator(), AdminModule.getSessionTimeout(), AdminModule.getMaxSessions() );
		listenTo(sessionAdminForm);
		myContent.put("session.admin.form", sessionAdminForm.getInitialComponent());
		myContent.contextPut("usersessions", getUsersSessionAsString(ureq));
		putInitialPanel(myContent);
	}


	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == invalidateAllSsssionLink){
			invalidateAllConfirmController = activateYesNoDialog(ureq, null, translate("invalidate.all.sure"), invalidateAllConfirmController);
			return;
		} else if(source == blockLoginLink) {
			blockLoginConfirmController = activateYesNoDialog(ureq, null, translate("block.login.sure"), invalidateAllConfirmController);			
		} else if(source == rejectDMZReuqestsLink) {
			rejectDMZRequestsConfirmController = activateYesNoDialog(ureq, null, translate("reject.dmz.requests.sure"), rejectDMZRequestsConfirmController);
		} else if(source == allowDMZRequestsLink) {
			AdminModule.setRejectDMZRequests(false);
			myContent.contextPut("rejectingDMZRequests", AdminModule.isRejectDMZRequests());
			showInfo("allow.dmz.requests.done");
		} else if(source == allowLoginLink) {
			AdminModule.setLoginBlocked(false);
			myContent.contextPut("loginBlocked", Boolean.FALSE);
			showInfo("allow.login.done");
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == invalidateAllConfirmController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				int nbrOfInvalidatedSessions = AdminModule.invalidateAllSessions();	
				showInfo("invalidate.session.done", Integer.toString(nbrOfInvalidatedSessions));
			}
		} else if (source == blockLoginConfirmController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				AdminModule.setLoginBlocked(true);
				myContent.contextPut("loginBlocked", Boolean.TRUE);
				showInfo("block.login.done");
			}
		} else if (source == rejectDMZRequestsConfirmController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				AdminModule.setRejectDMZRequests(true);
				myContent.contextPut("rejectingDMZRequests", AuthHelper.isRejectDMZRequests());
				showInfo("reject.dmz.requests.done");
			}
		} else if (source == sessionAdminOldestSessionForm) {
			int nbrOfInvalidatedSessions = AdminModule.invalidateOldestSessions(sessionAdminOldestSessionForm.getNbrSessions());
			showInfo("invalidate.session.done", Integer.toString(nbrOfInvalidatedSessions));
		} else if (source == sessionAdminForm && event == event.DONE_EVENT) {
			AdminModule.setSessionTimeout(sessionAdminForm.getSessionTimeout());
			AdminModule.setMaxSessions(sessionAdminForm.getMaxSessions());
		}

	}

	protected void doDispose() {
		// DialogBoxController and TableController get disposed by BasicController
	}
	
	private String getUsersSessionAsString(UserRequest ureq) {
		StringBuilder sb = new StringBuilder();
		int ucCnt = UserSession.getUserSessionsCnt();
		Set usesss = UserSession.getAuthenticatedUserSessions();
		int contcnt = DefaultController.getControllerCount();
		sb.append("total usersessions (auth and non auth): "+ucCnt+"<br />auth usersessions: "+usesss.size()+"<br />Total Controllers (active, not disposed) of all users:"+contcnt+"<br /><br />");
		Formatter f = Formatter.getInstance(ureq.getLocale());
		for (Iterator iter = usesss.iterator(); iter.hasNext();) {
			UserSession usess = (UserSession) iter.next();
			Identity iden = usess.getIdentity();
			sb.append("authusersession (").append(usess.hashCode()).append(") of ");
			if (iden != null) {
				sb.append(iden.getName()).append(" ").append(iden.getKey());
			}
			else {
				sb.append(" - ");
			}
			sb.append("<br />");
			Windows ws = Windows.getWindows(usess);
			for (Iterator iterator = ws.getWindowIterator(); iterator.hasNext(); ) {
				Window window = (Window) iterator.next();
				sb.append("- window ").append(window.getDispatchID()).append(" ").append(window.getLatestDispatchComponentInfo()).append("<br />");
			}
			sb.append("<br />");
		}
		return sb.toString();
	}
	
}