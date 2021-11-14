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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.login.auth.OLATAuthManager;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Jul 29, 2003
 * 
 * @author Felix Jost, Florian Gnaegi
 * 
 * <pre>
 * Comment:  
 * Subworkflow that presents a form to change the OLAT local password of the given user. 
 * 
 * </pre>
 */
public class UserChangePasswordController extends BasicController {
	
	private ChangeUserPasswordForm chPwdForm;
	private SendTokenToUserForm tokenForm;
	private VelocityContainer mainVC;
	private Identity user;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;

	/**
	 * @param ureq
	 * @param wControl
	 * @param changeableUser
	 */
	public UserChangePasswordController(UserRequest ureq, WindowControl wControl, Identity changeableUser) { 
		super(ureq, wControl);

		user = changeableUser;
		mainVC = createVelocityContainer("pwd");
		String authenticationUsername = olatAuthenticationSpi.getAuthenticationUsername(changeableUser);
		if (authenticationUsername == null) { // create new authentication for provider OLAT
			authenticationUsername = olatAuthenticationSpi.getOlatAuthusernameFromIdentity(changeableUser);
		}
		
		chPwdForm = new ChangeUserPasswordForm(ureq, wControl, user, authenticationUsername);
		listenTo(chPwdForm);
		mainVC.put("chPwdForm", chPwdForm.getInitialComponent());
		if (userModule.isAnyPasswordChangeAllowed()) {
			tokenForm = new SendTokenToUserForm(ureq, wControl, user);
			listenTo(tokenForm);
			mainVC.put("tokenForm", tokenForm.getInitialComponent());
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == chPwdForm && event.equals(Event.DONE_EVENT)) {
			if (olatAuthenticationSpi.changePassword(ureq.getIdentity(), user, chPwdForm.getNewPassword())) {
				showInfo("changeuserpwd.successful");
				logAudit("user password changed successfully of " + user.getKey());
			} else {
				showError("changeuserpwd.failed");
			}
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
