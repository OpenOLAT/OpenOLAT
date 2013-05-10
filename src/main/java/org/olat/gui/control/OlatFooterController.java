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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.DefaultFooterController;
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
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.social.SocialModule;
import org.olat.social.shareLink.ShareLinkController;

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
public class OlatFooterController extends BasicController { 
	private final VelocityContainer olatFootervc;

	public OlatFooterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(
				DefaultFooterController.class, getLocale(), Util
						.createPackageTranslator(OlatFooterController.class,
								getLocale())));
		
		olatFootervc = createVelocityContainer("olatFooter");
		//
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
				olatFootervc.contextPut("username", translate("invitee"));
			} else {
				olatFootervc.contextPut("username", ureq.getIdentity().getName());
			}
		} else {
			olatFootervc.contextPut("loggedIn", Boolean.FALSE);
		}

		olatFootervc.contextPut("appName", Settings.getApplicationName());
		olatFootervc.contextPut("appVersion", Settings.getVersion());
		olatFootervc.contextPut("buildIdentifier", Settings.getBuildIdentifier());
		olatFootervc.contextPut("changeSet", WebappHelper.getChangeSet());
		olatFootervc.contextPut("changeSetDate", WebappHelper.getChangeSetDate());
		olatFootervc.contextPut("node", Settings.getNodeInfo());
		olatFootervc.contextPut("olatversion", Settings.getFullVersionInfo() +" "+ Settings.getNodeInfo());

		putInitialPanel(olatFootervc);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}
