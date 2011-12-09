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

package org.olat.user;

import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date:  Jul 25, 2005
  *
 * @author Alexander Schneider
 * 
 * Comment: 
 */
public class HomePageDisplayController extends BasicController {
	private static final String usageIdentifyer = HomePageConfig.class.getCanonicalName();
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param hpc
	 */
	public HomePageDisplayController(UserRequest ureq, WindowControl wControl, HomePageConfig hpc) {
		super(ureq, wControl);

		// use property handler translator for translating of user fields
		setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));
		VelocityContainer myContent = createVelocityContainer("homepagedisplay");
		
		String userName = hpc.getUserName();
		UserManager um = UserManager.getInstance();
		Identity identity = BaseSecurityManager.getInstance().findIdentityByName(userName);
		User u = identity.getUser();
		
		myContent.contextPut("userName", identity.getName());
		myContent.contextPut("deleted", identity.getStatus().equals(Identity.STATUS_DELETED));
		myContent.contextPut("user", u);
		myContent.contextPut("locale", getLocale());
		
		// add configured property handlers and the homepage config
		// do the looping in the velocity context
		List<UserPropertyHandler> userPropertyHandlers = um.getUserPropertyHandlersFor(usageIdentifyer, false);
		myContent.contextPut("userPropertyHandlers", userPropertyHandlers);
		myContent.contextPut("homepageConfig", hpc);		
		
		Controller dpc = new DisplayPortraitController(ureq, getWindowControl(), identity, true, false);
		listenTo(dpc); // auto dispose
		myContent.put("image", dpc.getInitialComponent());
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to catch
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// child controller sposed by basic controller
	}
	
}
