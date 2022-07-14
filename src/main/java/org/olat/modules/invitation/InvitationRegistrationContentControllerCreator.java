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
package org.olat.modules.invitation;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.login.DmzBFWCParts;
import org.olat.registration.RegistrationController;

/**
 * 
 * Initial date: 11 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationRegistrationContentControllerCreator implements ControllerCreator {

	@Override
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		DmzBFWCParts dmzSitesAndNav = new DmzBFWCParts();
		dmzSitesAndNav.showTopNav(false);
		ControllerCreator contentControllerCreator = (uureq, wwControl) -> {
			Invitation invitation = (Invitation)uureq.getUserSession().getEntry(AuthHelper.ATTRIBUTE_INVITATION);
			return new RegistrationController(uureq, wwControl, invitation);
		};
		dmzSitesAndNav.setContentControllerCreator(contentControllerCreator);
		return new BaseFullWebappController(lureq, dmzSitesAndNav);
	}
}