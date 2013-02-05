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
package org.olat.home;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;

/**
 * Description:<br>
 * This controller wrapps the usersearchcontroller. 
 * it catches the "singleIdentityChosenEvent"  and displays a new OLAT-Tab with the users's info.
 * 
 * (implemented for use in minimalHomeController)
 * 
 * <P>
 * Initial Date:  13.09.2011 <br>
 * @author strentini, sergio.trentini@frentix.com,  www.frentix.com
 */
public class UserSearchAndInfoController extends BasicController {

	
	private UserSearchController userCtrl; 
	
	public UserSearchAndInfoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		userCtrl = new UserSearchController(ureq, wControl);
		listenTo(userCtrl);
		putInitialPanel(userCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source.equals(userCtrl)){
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent foundEvent = (SingleIdentityChosenEvent) event;
				Identity chosenIdentity = foundEvent.getChosenIdentity();
				if (chosenIdentity != null) {
					HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
					OLATResourceable ores = hpcm.loadConfigFor(chosenIdentity.getName());
					DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
					//was brasato:: DTabs dts = getWindowControl().getDTabs();
					DTab dt = dts.getDTab(ores);
					if (dt == null) {
						// does not yet exist -> create and add
						String name = UserManager.getInstance().getUserDisplayName(chosenIdentity.getUser());
						dt = dts.createDTab(ores, null, name);
						if (dt == null) return;
						UserInfoMainController uimc = new UserInfoMainController(ureq, dt.getWindowControl(), chosenIdentity);
						dt.setController(uimc);
						dts.addDTab(ureq, dt);
					}
					dts.activate(ureq, dt, null);
				}
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

}
