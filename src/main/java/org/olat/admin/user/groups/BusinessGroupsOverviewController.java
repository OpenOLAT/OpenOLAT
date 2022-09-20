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
package org.olat.admin.user.groups;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.ui.IdentityInvitationListController;

/**
 * 
 * Initial date: 5 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupsOverviewController extends BasicController {
	
	private final GroupOverviewController businessGroupsCtrl;
	private final IdentityInvitationListController invitationsCtrl;
	
	public BusinessGroupsOverviewController(UserRequest ureq, WindowControl wControl, Identity editedIdentity,
			boolean canEditInvitation) {
		super(ureq, wControl);
		
		businessGroupsCtrl = new GroupOverviewController(ureq, wControl, editedIdentity, false, true);
		listenTo(businessGroupsCtrl);
		
		invitationsCtrl = new IdentityInvitationListController(ureq, wControl, editedIdentity,
				InvitationTypeEnum.businessGroup, !canEditInvitation);
		listenTo(invitationsCtrl);
		
		VelocityContainer mainVC = createVelocityContainer("overview");
		mainVC.put("businessGroups", businessGroupsCtrl.getInitialComponent());
		mainVC.put("invitations", invitationsCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	

}
