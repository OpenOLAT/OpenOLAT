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

package org.olat.portal.quickstart;

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.group.site.GroupsSite;
import org.olat.home.site.HomeSite;
import org.olat.repository.site.RepositorySite;

/**
 * Description:<br>
 * Run view controller of quickstart portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class QuickstartPortletRunController extends DefaultController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(QuickstartPortletRunController.class);
	private Translator trans;
	private VelocityContainer quickstartVC;
	private Link helpLink;
	
	/**
	 * Constructor
	 * @param ureq
	 * @param wControl
	 */
	protected QuickstartPortletRunController(UserRequest ureq, WindowControl wControl) {
		super(wControl);
		this.trans = new PackageTranslator(Util.getPackageName(QuickstartPortletRunController.class), ureq.getLocale());

		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			this.quickstartVC = new VelocityContainer("quickstartVC", VELOCITY_ROOT + "/quickstartPortletGuest.html", trans, this);
		} else {
			this.quickstartVC = new VelocityContainer("quickstartVC", VELOCITY_ROOT + "/quickstartPortlet.html", trans, this);
		}
		helpLink = LinkFactory.createLink("quickstart.link.help", quickstartVC, this);
		helpLink.setTooltip("quickstart.ttip.help", false);
		helpLink.setTarget("_help");		

		setInitialComponent(this.quickstartVC);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == quickstartVC) {
			String cmd = event.getCommand();
			DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
			if (cmd.equals("cmd.repo.course")) {
				//was brasato:: getWindowControl().getDTabs().activateStatic(ureq, RepositorySite.class.getName(), "search.course");
				dts.activateStatic(ureq, RepositorySite.class.getName(), "search.course");
			} else if (cmd.equals("cmd.repo.catalog")) {
				//was brasato:: getWindowControl().getDTabs().activateStatic(ureq, RepositorySite.class.getName(), "search.catalog");
				dts.activateStatic(ureq, RepositorySite.class.getName(), "search.catalog");
			} else if (cmd.equals("cmd.settings")) {
				//was brasato:: getWindowControl().getDTabs().activateStatic(ureq, HomeSite.class.getName(), "mysettings");
				dts.activateStatic(ureq, HomeSite.class.getName(), "mysettings");
			}	else if (cmd.equals("cmd.buddygroup.new")) {
				//was brasato:: getWindowControl().getDTabs().activateStatic(ureq, GroupsSite.class.getName(), "addBuddyGroup");
				dts.activateStatic(ureq, GroupsSite.class.getName(), "addBuddyGroup");
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

	protected void doDispose() {
	// nothing to dispose
	}

}
