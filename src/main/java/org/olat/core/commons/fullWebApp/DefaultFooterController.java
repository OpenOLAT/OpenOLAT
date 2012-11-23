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
import org.olat.core.defaults.dispatcher.ClassPathStaticDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.session.UserSessionManager;

/**
 * <h3>Description:</h3>
 * This is a simple controller that displays the brasato web app framework
 * default footer.
 * <p>
 * Initial Date: 10.10.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class DefaultFooterController extends BasicController {
	private VelocityContainer footerVC;

	/**
	 * Constructor: usedd with AutoCreator
	 */
	public DefaultFooterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		// Initialize velocity container
		footerVC = createVelocityContainer("defaultfooter");

		String ressourceMapperUri = ClassPathStaticDispatcher.getInstance().getMapperBasePath(this.getClass());
		footerVC.contextPut("ressourceMapperUri", ressourceMapperUri);
		footerVC.contextPut("olatversion", Settings.getFullVersionInfo() +" "+ Settings.getNodeInfo());

		// Push information about AJAX mode
		boolean ajaxOn = false;
		if (ureq.getUserSession().isAuthenticated()) {
			ajaxOn = Windows.getWindows(ureq).getWindowManager()
					.isAjaxEnabled();
		} else {
			// on construction time only global and browserdependent ajax on
			// settings can be used
			// to show ajax gif :-)
			ajaxOn = Settings.isAjaxGloballyOn();
		}
		footerVC.contextPut("ajaxOn", ajaxOn ? Boolean.TRUE : Boolean.FALSE);

		// Push information about logged in users
		footerVC
				.contextPut("userSessionsCnt", CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionsCnt());

		// Push information about user
		if (ureq.getUserSession().isAuthenticated()) {
			footerVC.contextPut("loggedIn", Boolean.TRUE);
			footerVC.contextPut("username", ureq.getIdentity().getName());
		} else {
			footerVC.contextPut("loggedIn", Boolean.FALSE);
		}

		putInitialPanel(footerVC);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}
}
