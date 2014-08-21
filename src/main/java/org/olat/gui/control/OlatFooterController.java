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

package org.olat.gui.control;

import org.olat.admin.layout.LayoutChangedEvent;
import org.olat.admin.layout.LayoutModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.util.UserLoggedInCounter;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.social.SocialModule;
import org.olat.social.shareLink.ShareLinkController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Overrides the default footer of the webapplication framework showing the 
 * <ul>
 * <li>connected users</li>
 * <li>username</li>
 * <li>powered by</li>
 * </ul>
 * <P>
 * Initial Date: 16.06.2006 <br>
 * 
 * @author patrickb
 */
public class OlatFooterController extends BasicController implements GenericEventListener { 
	
	private final VelocityContainer olatFootervc;
	
	@Autowired
	private LayoutModule layoutModule;
	@Autowired
	private CoordinatorManager coordinatorManager;

	public OlatFooterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(BaseFullWebappController.class, getLocale(), Util.createPackageTranslator(OlatFooterController.class,getLocale())));
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, LayoutModule.layoutCustomizingOResourceable);
		
		olatFootervc = createVelocityContainer("olatFooter");

		Identity identity = ureq.getIdentity();
		boolean isGuest = (identity == null ? true : ureq.getUserSession().getRoles().isGuestOnly());
		boolean isInvitee = (identity == null ? false : ureq.getUserSession().getRoles().isInvitee());
		
		// Show user count
		UserLoggedInCounter userCounter = new UserLoggedInCounter();
		olatFootervc.put("userCounter", userCounter);

		// share links
		SocialModule socialModule = CoreSpringFactory.getImpl(SocialModule.class);
		if (socialModule.isShareEnabled() && socialModule.getEnabledShareLinkButtons().size() > 0) {
			Controller shareLinkCtr = new ShareLinkController(ureq, wControl);
			listenTo(shareLinkCtr); // for auto-dispose
			// push to view
			olatFootervc.put("shareLink", shareLinkCtr.getInitialComponent());
		}

		// Push information about user
		if (!isGuest && ureq.getUserSession().isAuthenticated()) {
			olatFootervc.contextPut("loggedIn", Boolean.TRUE);
			if(isInvitee) {
				olatFootervc.contextPut("username", translate("logged.in.invitee"));
			} else {
				String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(ureq.getIdentity());
				olatFootervc.contextPut("username", StringHelper.escapeHtml(fullName));
			}
		} else {
			olatFootervc.contextPut("loggedIn", Boolean.FALSE);
		}

		olatFootervc.contextPut("appName", Settings.getApplicationName());
		olatFootervc.contextPut("appVersion", Settings.getVersion());
		
		olatFootervc.contextPut("buildIdentifier", Settings.getBuildIdentifier());
		olatFootervc.contextPut("revisionNumber", WebappHelper.getRevisionNumber());
		olatFootervc.contextPut("changeSet", WebappHelper.getChangeSet());
		olatFootervc.contextPut("olatversion", Settings.getFullVersionInfo() +" "+ Settings.getNodeInfo());

		updateFooterLine();
		putInitialPanel(olatFootervc);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public void event(Event event) {
		if (event instanceof LayoutChangedEvent) {
			LayoutChangedEvent lcevent = (LayoutChangedEvent) event;
			if (lcevent.toString().equals(LayoutChangedEvent.LAYOUTSETTINGSCHANGED)) {
				updateFooterLine();
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private void updateFooterLine() {
		String footerLine = convertFooterLine(layoutModule.getFooterLine());
		if(StringHelper.containsNonWhitespace(footerLine)) {
			olatFootervc.contextPut("footerLine", footerLine);
		} else {
			olatFootervc.contextRemove("footerLine");
		}
		String footerUrl = layoutModule.getFooterLinkUri();
		if(StringHelper.containsNonWhitespace(footerUrl)) {
			olatFootervc.contextPut("footerUrl", footerUrl);
		} else {
			olatFootervc.contextRemove("footerUrl");
		}
	}
	
	/**
	 * replaces email / weblink in footerLine-String and makes them clickable
	 * 
	 * @param dbFooterLine
	 * @return string with email / weblink replaced to clickable link with <a
	 *         href=...
	 */
	private String convertFooterLine(String dbFooterLine) {
		if (dbFooterLine == null) {
			return "";
		}
		
		String parsedFooterLine = null;
		String mailregex = "(\\w+)@([\\w\\.]+)";
		parsedFooterLine = dbFooterLine.replaceAll(mailregex, "<a href=\"mailto:$0\">$0</a>");
		String urlregex = "((http(s?)://)|(www.))(([\\w-.]+)*(/[^[:space:]]+)*)";
		parsedFooterLine = parsedFooterLine.replaceAll(urlregex, "<a href=\"http$3://$4$5\" target=\"_blank\">$2$4$5</a>");
		return parsedFooterLine;
	}
}
