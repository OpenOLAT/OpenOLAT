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
* <p>
*/ 

package org.olat.gui.control;

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.instantMessaging.ConncectedUsersHelper;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.ui.ConnectedClientsListController;

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
public class OlatGuestFooterController extends BasicController {
	private VelocityContainer olatFootervc;
	private Link showOtherUsers, loginLink;

	public OlatGuestFooterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		olatFootervc = createVelocityContainer("olatGuestFooter");
		//
		Identity identity = ureq.getIdentity();
		// 6.1.0 Code => Nullpointer @ OlatGuestFooterController.java:65
		// Boolean isGuest = (identity == null ? Boolean.TRUE : new Boolean(ureq.getUserSession().getRoles().isGuestOnly()));
		boolean isGuest = true;
		if ( (identity == null) || (ureq.getUserSession() == null) || (ureq.getUserSession().getRoles() == null) ) {
			isGuest = true;
		} else {
			isGuest = ureq.getUserSession().getRoles().isGuestOnly();
		}
		// some variables displayed in the footer
		olatFootervc.contextPut("username", identity != null ? getTranslator().translate("username", new String[] { identity.getName() })
				: getTranslator().translate("not.logged.in"));
		// is user guest or not looged in?
		olatFootervc.contextPut("isGuest", isGuest);
		
		showOtherUsers = LinkFactory.createLink("other.users.online", olatFootervc, this);
		showOtherUsers.setAjaxEnabled(false);
		showOtherUsers.setTarget("_blanc");
		if (isGuest) showOtherUsers.setEnabled(false);
		
		loginLink = LinkFactory.createLink("footer.login", olatFootervc, this);
		
		olatFootervc.contextPut("olatversion", Settings.getFullVersionInfo() +" "+Settings.getNodeInfo());
		// instant messaging awareness
		olatFootervc.contextPut("instantMessagingEnabled", new Boolean(InstantMessagingModule.isEnabled()));
		if (InstantMessagingModule.isEnabled()) {
			olatFootervc.contextPut("connectedUsers", new ConncectedUsersHelper());
		}
		//
		putInitialPanel(olatFootervc);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showOtherUsers) {
				// show list of other online users that are connected to jabber server
			ControllerCreator ctrlCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					return new ConnectedClientsListController(lureq, lwControl);
				}					
			};
			//wrap the content controller into a full header layout
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
			//open in new browser window
			PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
			pbw.open(ureq);
			//
		} else if (source == loginLink) {
			DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
		}

	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

}
