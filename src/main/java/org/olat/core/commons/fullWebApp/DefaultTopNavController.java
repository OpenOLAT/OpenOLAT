/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.fullWebApp;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.session.UserSessionManager;

/**
 * <h3>Description:</h3>
 * A simple top nav controller that features a logout link
 * <p>
 * Initial Date: 11.10.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class DefaultTopNavController extends BasicController {
	private VelocityContainer topNavVC;

	/**
	 * Constructor, creates a velocity page with a list
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public DefaultTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		topNavVC = createVelocityContainer("defaulttopnav");
		topNavVC.contextPut("isGuest",
				(ureq.getIdentity() == null ? Boolean.TRUE : new Boolean(ureq
						.getUserSession().getRoles().isGuestOnly())));
		putInitialPanel(topNavVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		String command = event.getCommand();
		if (source == topNavVC) {
			if (command.equals("logout")) {
				CoreSpringFactory.getImpl(UserSessionManager.class).signOffAndClear(ureq.getUserSession());
			} else if (command.equals("login")) {
				DispatcherAction
						.redirectToDefaultDispatcher(ureq.getHttpResp());
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	protected void doDispose() {
		// nothing to dispose
	}

}
