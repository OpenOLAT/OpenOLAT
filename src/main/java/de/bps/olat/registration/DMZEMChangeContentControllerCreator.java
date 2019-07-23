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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.registration;

import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.login.DmzBFWCParts;

import de.bps.olat.user.ChangeEMailController;

/**
 * Initial Date:  21.11.2008 <br>
 * @author bja
 */
public class DMZEMChangeContentControllerCreator implements ControllerCreator {

	@Override
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		DmzBFWCParts dmzSitesAndNav = new DmzBFWCParts();
		dmzSitesAndNav.showTopNav(false);
		AutoCreator contentControllerCreator = new AutoCreator();
		contentControllerCreator.setClassName(ChangeEMailController.class.getName());
		dmzSitesAndNav.setContentControllerCreator(contentControllerCreator);
		return new BaseFullWebappController(lureq, dmzSitesAndNav);		
	}
}