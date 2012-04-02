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

import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.impressum.ImpressumMainController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.services.search.ui.SearchController;
import org.olat.core.commons.services.search.ui.SearchServiceUIFactory;
import org.olat.core.commons.services.search.ui.SearchServiceUIFactory.DisplayOption;
import org.olat.core.dispatcher.DispatcherAction;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.groupchat.GroupChatManagerController;

/**
 * Description:<br>
 * TODO: patrickb Class Description for OlatTopNavController
 * <P>
 * Initial Date: 13.06.2006 <br>
 * 
 * @author patrickb
 */
public class OlatTopNavController extends BasicController implements GenericEventListener {
	
	private static final String ACTION_LOGOUT = "logout";
	private VelocityContainer topNavVC;
	private Controller imController;
	private GroupChatManagerController groupChatController;
	private SearchController searchC;
	private Link helpLink, loginLink, impressumLink;

	
	private EventBus singleUserEventCenter;
	private OLATResourceable ass;
	
	public OlatTopNavController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, false, true);
	}
	
	public OlatTopNavController(UserRequest ureq, WindowControl wControl, boolean impressum,	boolean search) {
		super(ureq, wControl);
		
		topNavVC = createVelocityContainer("topnav");
		
		boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();
		boolean isInvitee = ureq.getUserSession().getRoles().isInvitee();
		
		// instant messaging area, only when enabled and user is not a guest user
		if (InstantMessagingModule.isEnabled() && !isGuest && !isInvitee) {
			imController = InstantMessagingModule.getAdapter().createClientController(ureq, this.getWindowControl());
			listenTo(imController);
			topNavVC.put("imclient", imController.getInitialComponent());
			groupChatController = InstantMessagingModule.getAdapter().getGroupChatManagerController(ureq);
			listenTo(groupChatController);
			topNavVC.put("groupchatcontroller", groupChatController.getGroupChatContainer());
		}
		//
		// the help link
		if(!isInvitee && CourseModule.isHelpCourseEnabled()) {
			helpLink = LinkFactory.createLink("topnav.help", topNavVC, this);
			helpLink.setCustomEnabledLinkCSS("b_with_small_icon_right o_help_icon");
			helpLink.setTooltip("topnav.help.alt", false);
			helpLink.setTarget("_help");
		}
		
		// login link
		if (ureq.getIdentity() == null) {
			topNavVC.contextPut("isGuest", Boolean.TRUE);
			loginLink = LinkFactory.createLink("topnav.login", topNavVC, this);
			loginLink.setTooltip("topnav.login.alt", false);
		}
		
		if(impressum) {
			impressumLink = LinkFactory.createLink("topnav.impressum", topNavVC, this);
			impressumLink.setTooltip("topnav.impressum.alt", false);
			impressumLink.setCustomEnabledLinkCSS("o_topnav_impressum");
			impressumLink.setAjaxEnabled(false);
			impressumLink.setTarget("_blank");
		}
		
		if(ureq.getIdentity() != null && !isGuest && !isInvitee) {
			SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
			searchC = searchUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD, null);
			searchC.setResourceContextEnable(false);
			topNavVC.put("search_input", searchC.getInitialComponent());

			ass = OresHelper.createOLATResourceableType(AssessmentEvent.class);
			singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
			singleUserEventCenter.registerFor(this, getIdentity(), ass);
		}
		
		putInitialPanel(topNavVC);
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		String command = event.getCommand();
			if (source == helpLink) {
				ControllerCreator ctrlCreator = new ControllerCreator() {
					public Controller createController(UserRequest lureq, WindowControl lwControl) {
						return CourseFactory.createHelpCourseLaunchController(lureq, lwControl);
					}					
				};
				//wrap the content controller into a full header layout
				ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
				//open in new browser window
				openInNewBrowserWindow(ureq, layoutCtrlr);
				//
			} else if (source == loginLink) {
				DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
			} else if (source == topNavVC) {
			if (command.equals(ACTION_LOGOUT)) {
				AuthHelper.doLogout(ureq);
			}
		}	else if (source == impressumLink) {
			ControllerCreator impressumControllerCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					return new ImpressumMainController(lureq, lwControl);
				}
			};
			PopupBrowserWindow popupBrowserWindow = Windows.getWindows(ureq).getWindowManager().createNewPopupBrowserWindowFor(ureq, impressumControllerCreator);
			popupBrowserWindow.open(ureq);
		}
	}

	protected void doDispose() {
		//controllers are disposed by BasicController
		// im header controller mus be disposed last - content or navigation control
		// controller
		// might try to send a IM presence message which would lazy generate a new
		// IM client.
		// the IM client gets disposed in the header controller
		
		if (singleUserEventCenter != null) {
			singleUserEventCenter.deregisterFor(this, ass);
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof AssessmentEvent) {
			if(((AssessmentEvent)event).getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
				topNavVC.contextPut("inAssessment", true);
				return;
			} 
			if(((AssessmentEvent)event).getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
				OLATResourceable a = OresHelper.createOLATResourceableType(AssessmentInstance.class);
				if (singleUserEventCenter.getListeningIdentityCntFor(a)<1) {
					topNavVC.contextPut("inAssessment", false);
				}
				return;
			} 
		}
	}
}
