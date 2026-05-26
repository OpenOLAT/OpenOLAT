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
package org.olat.user;

import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionBrowserEvent;

/**
 * Initial date: 2026-05-22<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class IdentityObjectSourceBrowserWrapper extends BasicController {

	private final UserSearchController userSearchCtrl;

	public IdentityObjectSourceBrowserWrapper(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		userSearchCtrl = new UserSearchController(ureq, wControl, true, false, false);
		listenTo(userSearchCtrl);
		putInitialPanel(userSearchCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == userSearchCtrl) {
			if (event instanceof SingleIdentityChosenEvent sice && sice.getChosenIdentity() != null) {
				fireEvent(ureq, new ObjectSelectionBrowserEvent(
						List.of(sice.getChosenIdentity().getKey().toString())));
			} else if (event instanceof MultiIdentityChosenEvent mice && !mice.getChosenIdentities().isEmpty()) {
				fireEvent(ureq, new ObjectSelectionBrowserEvent(
						mice.getChosenIdentities().stream().map(id -> id.getKey().toString()).toList()));
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
