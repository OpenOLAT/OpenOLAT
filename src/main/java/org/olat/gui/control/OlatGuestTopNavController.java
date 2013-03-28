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
import org.olat.core.commons.controllers.impressum.ImpressumMainController;
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
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;

/**
 * Description:<br>
 * TODO: patrickb Class Description for OlatUpperRightCorner
 * <P>
 * Initial Date: 13.06.2006 <br>
 * 
 * @author patrickb
 */
public class OlatGuestTopNavController extends BasicController {
	private VelocityContainer topNavVC;
	private Link helpLink;
	private Link loginLink;
	private Link impressumLink;
	

	public OlatGuestTopNavController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, false);
	}

	public OlatGuestTopNavController(UserRequest ureq, WindowControl wControl, boolean impressum) {
		super(ureq, wControl);
		topNavVC = createVelocityContainer("guesttopnav");
		
		// impressum
		if(impressum) {
			impressumLink = LinkFactory.createLink("topnav.impressum", topNavVC, this);
			impressumLink.setTooltip("topnav.impressum.alt");
			impressumLink.setAjaxEnabled(false);
			impressumLink.setTarget("_blank");
		}
		
		// the help link
		if (CourseModule.isHelpCourseEnabled()) {
			helpLink = LinkFactory.createLink("topnav.help", topNavVC, this);
			helpLink.setCustomEnabledLinkCSS("b_with_small_icon_right o_help_icon");
			helpLink.setTooltip("topnav.help.alt");
			helpLink.setTarget("_help");
		}
		
		loginLink = LinkFactory.createLink("topnav.login", topNavVC, this);
		loginLink.setTooltip("topnav.login.alt");

		//
		putInitialPanel(topNavVC);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		//
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == loginLink) {
			AuthHelper.doLogout(ureq);
		} else if (source == helpLink) {
			ControllerCreator ctrlCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					return CourseFactory.createHelpCourseLaunchController(lureq, lwControl);
				}					
			};
			//wrap the content controller into a full header layout
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
			//open in new browser window
			PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
			pbw.open(ureq);
			//
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
		// controllers disposed by BasicController:
	}

}
