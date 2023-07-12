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
package org.olat.modules.openbadges.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * Initial date: 2023-07-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgesUserToolController extends BasicController implements Activateable2 {

	private IssuedBadgesController issuedBadgesController;

	public BadgesUserToolController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		issuedBadgesController = new IssuedBadgesController(ureq, wControl, "badges.mine.title",
				null, true, getIdentity(), "manual_user/personal_menu/OpenBadges/");
		listenTo(issuedBadgesController);

		putInitialPanel(issuedBadgesController.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {

	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries.isEmpty()) {
			return;
		}
		if ("key".equalsIgnoreCase(entries.get(0).getOLATResourceable().getResourceableTypeName())) {
			Long key = entries.get(0).getOLATResourceable().getResourceableId();
			issuedBadgesController.showAssertion(ureq, key);
		}
	}
}
