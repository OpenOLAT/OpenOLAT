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


package org.olat.core.commons.fullWebApp;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

/**
 * 
 * Displays a simple error message to the user and a link to the loginpage. The
 * message is also logged to the log file
 * 
 * <P>
 * Initial Date: 05.11.2007 <br>
 * 
 * @author Lavinia Dumitrescu
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MessageWindowController extends DefaultChiefController {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(MessageWindowController.class);

	public MessageWindowController(UserRequest ureq, String message) {
		this(ureq, message, WebappHelper.getMailConfig("mailSupport"));
	}
	
	/**
	 * 
	 * @param ureq
	 * @param message
	 * @param supportEmail
	 */
	public MessageWindowController(UserRequest ureq, String message, String supportEmail) {
		Translator trans = Util.createPackageTranslator(MessageWindowController.class, ureq.getLocale());
		VelocityContainer msg = new VelocityContainer("olatmain", VELOCITY_ROOT + "/message.html", trans, this);
		CSPModule securityModule = CoreSpringFactory.getImpl(CSPModule.class);
		msg.contextPut("enforceTopFrame", Boolean.valueOf(securityModule.isForceTopFrame()));
		msg.contextPut("buildversion", Settings.getVersion());
		msg.contextPut("detailedmessage", message);					
		if(supportEmail != null) {
			msg.contextPut("supportEmail", supportEmail);
		}

		Windows ws = Windows.getWindows(ureq);
		WindowBackOffice wbo = ws.getWindowManager().createWindowBackOffice("messagewindow", ureq.getUserSession().getCsrfToken(), this, new WindowSettings());
		Window w = wbo.getWindow();
		
		msg.put("jsAndCssC", w.getJsCssRawHtmlHeader());
		msg.contextPut("theme", w.getGuiTheme());	
		
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
	public boolean hasStaticSite(Class<? extends SiteInstance> type) {
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

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}
}