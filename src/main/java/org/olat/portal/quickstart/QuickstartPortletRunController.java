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

package org.olat.portal.quickstart;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;

/**
 * Description:<br>
 * Run view controller of quickstart portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class QuickstartPortletRunController extends DefaultController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(QuickstartPortletRunController.class);
	private final Translator trans;
	private final VelocityContainer quickstartVC;
	private Link helpLink;
	
	/**
	 * Constructor
	 * @param ureq
	 * @param wControl
	 */
	protected QuickstartPortletRunController(UserRequest ureq, WindowControl wControl) {
		super(wControl);
		this.trans = Util.createPackageTranslator(QuickstartPortletRunController.class, ureq.getLocale());

		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			quickstartVC = new VelocityContainer("quickstartVC", VELOCITY_ROOT + "/quickstartPortletGuest.html", trans, this);
		} else {
			quickstartVC = new VelocityContainer("quickstartVC", VELOCITY_ROOT + "/quickstartPortlet.html", trans, this);
		}
		helpLink = LinkFactory.createLink("quickstart.link.help", quickstartVC, this);
		helpLink.setTooltip("quickstart.ttip.help");
		helpLink.setTarget("_help");		

		setInitialComponent(quickstartVC);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == quickstartVC) {
			String cmd = event.getCommand();
			String businessPath = null;
			if (cmd.equals("cmd.repo.course")) {
				businessPath = "[MyCoursesSite:0]";
			} else if (cmd.equals("cmd.repo.catalog")) {
				businessPath = "[MyCoursesSite:0][Catalog:0]";
			} else if (cmd.equals("cmd.settings")) {
				businessPath = "[HomeSite:" + ureq.getIdentity().getKey() + "][mysettings:0]";
			}	else if (cmd.equals("cmd.buddygroup.new")) {
				businessPath = "[GroupsSite:0]";
			}
			if(StringHelper.containsNonWhitespace(businessPath)) {
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
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
		}
	}
}
