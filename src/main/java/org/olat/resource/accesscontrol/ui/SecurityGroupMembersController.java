/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.ui;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * Show and manage the members of a security group
 * 
 * <P>
 * Initial Date:  15 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SecurityGroupMembersController extends BasicController {

	private final VelocityContainer mainVC;
	
	public SecurityGroupMembersController(UserRequest ureq, WindowControl wControl, SecurityGroup secGroup, 
			String title, String info, boolean mayModifierMembers, boolean keepAtLeastOne) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("members");

		GroupController groupController = new GroupController(ureq, wControl, mayModifierMembers, keepAtLeastOne, false, secGroup);
		listenTo(groupController);
		
		mainVC.put("memberList", groupController.getInitialComponent());
		if(StringHelper.containsNonWhitespace(title)) {
			mainVC.contextPut("title", title);			
		}
		if(StringHelper.containsNonWhitespace(info)) {
			mainVC.contextPut("info", info);			
		}
		putInitialPanel(mainVC);
	}
	
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof IdentitiesAddEvent) {
			fireEvent(ureq, event);
		} else if(event instanceof IdentitiesRemoveEvent) {
			fireEvent(ureq, event);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}