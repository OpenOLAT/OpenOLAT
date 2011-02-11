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

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.ims.qti.process.AssessmentInstance;
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
public class OlatFooterController extends BasicController implements GenericEventListener { 
	private VelocityContainer olatFootervc;
	private Link showOtherUsers;
	private EventBus singleUserEventCenter;
	private OLATResourceable ass;

	public OlatFooterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		olatFootervc = createVelocityContainer("olatFooter");
		//
		Identity identity = ureq.getIdentity();
		Boolean isGuest = (identity == null ? Boolean.TRUE : new Boolean(ureq.getUserSession().getRoles().isGuestOnly()));
		
		showOtherUsers = LinkFactory.createLink("other.users.online", olatFootervc, this);
		showOtherUsers.setAjaxEnabled(false);
		showOtherUsers.setTarget("_blanc");
		if (isGuest) showOtherUsers.setEnabled(false);
		
		// some variables displayed in the footer
		Translator translator = getTranslator();
		olatFootervc.contextPut("username", identity != null && !isGuest.booleanValue() ? translator.translate("username", new String[] { identity.getName() })
				: translator.translate("not.logged.in"));
		olatFootervc.contextPut("olatversion", Settings.getFullVersionInfo() +" "+ Settings.getNodeInfo());
		// instant messaging awareness
		olatFootervc.contextPut("instantMessagingEnabled", new Boolean(InstantMessagingModule.isEnabled()));
		if (InstantMessagingModule.isEnabled()) {
			olatFootervc.contextPut("connectedUsers", new ConncectedUsersHelper());
			if (!isGuest) {
				ass = OresHelper.createOLATResourceableType(AssessmentEvent.class);
				singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
				singleUserEventCenter.registerFor(this, getIdentity(), ass);
			}
		}
		boolean ajaxOn = false;
		if(ureq.getUserSession().isAuthenticated()){
			ajaxOn = Windows.getWindows(ureq).getWindowManager().isAjaxEnabled();
		}else{
			//on construction time only global and browserdependent ajax on settings can be used
			//to show ajax gif :-)
			ajaxOn = Settings.isAjaxGloballyOn();
		}		
		
		olatFootervc.contextPut("ajaxOn", ajaxOn ? Boolean.TRUE : Boolean.FALSE);
		//
		putInitialPanel(olatFootervc);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultSpringController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showOtherUsers) {
			
			if (!showOtherUsers.isEnabled()) {
				return;
			}
				// show list of other online users that are connected to jabber server
			ControllerCreator ctrlCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					ConnectedClientsListController clientsListCtr = new ConnectedClientsListController(lureq, lwControl);
					LayoutMain3ColsController mainLayoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, clientsListCtr.getInitialComponent(), null);
					mainLayoutCtr.addDisposableChildController(clientsListCtr);
					return mainLayoutCtr;
				}					
			};
			//wrap the content controller into a full header layout
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
			//open in new browser window
			PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
			pbw.open(ureq);
			//
		}

	}

	@Override
	protected void doDispose() {
		if (singleUserEventCenter != null) {
			singleUserEventCenter.deregisterFor(this, ass);
		}
	}

	@Override
	public void event(Event event) {
		
		if (event instanceof AssessmentEvent) {
			if(((AssessmentEvent)event).getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
				showOtherUsers.setEnabled(false);
				return;
			} 
			if(((AssessmentEvent)event).getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
				OLATResourceable a = OresHelper.createOLATResourceableType(AssessmentInstance.class);
				if (singleUserEventCenter.getListeningIdentityCntFor(a)<1) {
					showOtherUsers.setEnabled(true);
				}
				return;
			} 
		}
	}
}
