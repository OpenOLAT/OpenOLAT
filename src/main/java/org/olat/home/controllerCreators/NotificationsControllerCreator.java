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
package org.olat.home.controllerCreators;

import org.olat.core.commons.services.notifications.NotificationUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;

/**
 * wrapper to create a notificationcontroller with the uifactory. 
 * usable with default arguments (by config in minimalHome).
 * 
 * Initial Date: 10.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class NotificationsControllerCreator extends AutoCreator  {

	/**
	 * @see org.olat.core.gui.control.creator.AutoCreator#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}

	public NotificationsControllerCreator() {
		super();
	}	
	
	/**
	 * @see org.olat.core.gui.control.creator.ControllerCreator#createController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createController(UserRequest ureq, WindowControl lwControl) {
		return NotificationUIFactory.createCombinedSubscriptionsAndNewsController(ureq.getIdentity(), ureq, lwControl);
	}

}
