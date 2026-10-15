/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.controllers.impressum;

import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.login.DmzBFWCParts;

/**
 * Creates the chief controller for the public /dmz/impressum/ URL.
 *
 * Initial date: 14 Oct 2026<br>
 * @author uhensler, https://www.frentix.com
 *
 */
public class DMZImpressumContentControllerCreator implements ControllerCreator {

	@Override
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		DmzBFWCParts dmzSitesAndNav = new DmzBFWCParts();
		AutoCreator contentControllerCreator = new AutoCreator();
		contentControllerCreator.setClassName(ImpressumDmzMainController.class.getName());
		dmzSitesAndNav.setContentControllerCreator(contentControllerCreator);
		return new BaseFullWebappController(lureq, dmzSitesAndNav);
	}
}
