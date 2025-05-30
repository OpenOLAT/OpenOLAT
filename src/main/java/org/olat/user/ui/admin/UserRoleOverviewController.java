/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 23 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserRoleOverviewController extends BasicController {
	
	private final UserRolesController rolesCtrl;
	private final UserRoleHistoryController roleHistoryCtrl;
	
	public UserRoleOverviewController(UserRequest ureq, WindowControl wControl, Identity editedIdentity) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("user_role_overview");
		
		rolesCtrl = new UserRolesController(ureq, wControl, editedIdentity);
		listenTo(rolesCtrl);
		mainVC.put("roles", rolesCtrl.getInitialComponent());
		
		roleHistoryCtrl = new UserRoleHistoryController(ureq, wControl, editedIdentity);
		listenTo(roleHistoryCtrl);
		mainVC.put("history", roleHistoryCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rolesCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				roleHistoryCtrl.reloadModel();
				fireEvent(ureq, event);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
