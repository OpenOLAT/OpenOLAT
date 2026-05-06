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
package org.olat.modules.todo.ui;

import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public final class ToDoTaskDefaultMemberSearchProvider implements ToDoTaskMemberSearchProvider {

	public static final ToDoTaskDefaultMemberSearchProvider INSTANCE = new ToDoTaskDefaultMemberSearchProvider();

	private ToDoTaskDefaultMemberSearchProvider() {
		//
	}

	@Override
	public Controller createSearchController(UserRequest ureq, WindowControl wc) {
		return new UserSearchController(ureq, wc, true, false, false);
	}

	@Override
	public List<Identity> getSelectedIdentities(Controller ctrl, Event event) {
		if (event instanceof SingleIdentityChosenEvent sice && sice.getChosenIdentity() != null) {
			return List.of(sice.getChosenIdentity());
		}
		return List.of();
	}
}
